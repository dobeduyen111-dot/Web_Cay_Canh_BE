package ceb.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ceb.TestData;
import ceb.config.VnpayConfig;
import ceb.domain.model.CartItem;
import ceb.domain.model.Orders;
import ceb.domain.model.Payments;
import ceb.exception.EmptyCartException;
import ceb.exception.OrderAccessDeniedException;
import ceb.exception.ShippingAddressRequiredException;
import ceb.exception.VnpayException;
import ceb.repository.OrderItemsRepository;
import ceb.repository.OrdersRepository;
import ceb.repository.PaymentsRepository;
import ceb.service.implement.OrderServiceImpl;
import ceb.service.implement.PaymentServiceImpl;
import ceb.service.service.CartService;
import ceb.service.service.OrderService;
import ceb.util.VnpayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderAndPaymentServiceTest {

    private OrdersRepository ordersRepository;
    private OrderItemsRepository orderItemsRepository;
    private CartService cartService;
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        ordersRepository = mock(OrdersRepository.class);
        orderItemsRepository = mock(OrderItemsRepository.class);
        cartService = mock(CartService.class);
        orderService = new OrderServiceImpl(ordersRepository, orderItemsRepository, cartService);
    }

    @Test
    void checkoutCreatesOrderItemsAndClearsCart() {
        CartItem cartItem = TestData.cartItem();
        Orders createdOrder = TestData.order();
        when(cartService.getItems(7)).thenReturn(List.of(cartItem));
        when(ordersRepository.createOrder(any(Orders.class))).thenReturn(createdOrder.getOrderId());
        when(ordersRepository.findById(createdOrder.getOrderId())).thenReturn(createdOrder);
        when(orderItemsRepository.findByOrderId(createdOrder.getOrderId())).thenReturn(List.of(TestData.orderItem()));

        Orders result = orderService.checkout(7, " Da Lat ", "note");

        assertThat(result.getOrderId()).isEqualTo(createdOrder.getOrderId());
        assertThat(result.getItems()).hasSize(1);
        verify(ordersRepository).createOrder(argThat(order ->
                order.getUserId() == 7
                        && order.getShippingAddress().equals("Da Lat")
                        && order.getTotalAmount() == 240_000));
        verify(orderItemsRepository).insertItem(argThat(item ->
                item.getProductId() == cartItem.getProductId()
                        && item.getQuantity() == cartItem.getQuantity()));
        verify(cartService).clear(7);
    }

    @Test
    void checkoutValidatesShippingAddressAndCart() {
        assertThatThrownBy(() -> orderService.checkout(7, " ", null))
                .isInstanceOf(ShippingAddressRequiredException.class);

        when(cartService.getItems(7)).thenReturn(List.of());
        assertThatThrownBy(() -> orderService.checkout(7, "Da Lat", null))
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    void orderAccessIsLimitedToOwner() {
        Orders order = TestData.order();
        when(ordersRepository.findById(order.getOrderId())).thenReturn(order);
        when(orderItemsRepository.findByOrderId(order.getOrderId())).thenReturn(List.of(TestData.orderItem()));

        assertThat(orderService.findByIdForUser(7, order.getOrderId())).isSameAs(order);
        assertThatThrownBy(() -> orderService.findByIdForUser(999, order.getOrderId()))
                .isInstanceOf(OrderAccessDeniedException.class);
    }

    @Test
    void updateStatusAndDeleteRequireExistingOrder() {
        Orders order = TestData.order();
        when(ordersRepository.findById(order.getOrderId())).thenReturn(order);
        when(orderItemsRepository.findByOrderId(order.getOrderId())).thenReturn(List.of());

        assertThat(orderService.updateStatus(order.getOrderId(), "Da giao").getStatus()).isEqualTo("Cho xu ly");
        orderService.delete(order.getOrderId());

        verify(ordersRepository).updateStatus(order.getOrderId(), "Da giao");
        verify(ordersRepository).delete(order.getOrderId());
    }

    @Test
    void paymentCreateUsesOrderTotalWhenAmountIsMissing() {
        PaymentsRepository paymentsRepository = mock(PaymentsRepository.class);
        OrderService ownerAwareOrderService = mock(OrderService.class);
        PaymentServiceImpl paymentService = new PaymentServiceImpl(
                paymentsRepository,
                ownerAwareOrderService,
                ordersRepository,
                vnpayConfig());
        Payments payment = TestData.payment();
        payment.setAmount(0);
        when(ownerAwareOrderService.findByIdForUser(7, payment.getOrderId())).thenReturn(TestData.order());
        when(paymentsRepository.create(payment)).thenReturn(payment.getPaymentId());
        when(paymentsRepository.findById(payment.getPaymentId())).thenReturn(payment);

        Payments result = paymentService.create(7, payment);

        assertThat(result).isSameAs(payment);
        assertThat(payment.getAmount()).isEqualTo(TestData.order().getTotalAmount());
    }

    @Test
    void createVnpayUrlSignsExpectedPendingOrderAmount() {
        PaymentServiceImpl paymentService = new PaymentServiceImpl(
                mock(PaymentsRepository.class),
                mock(OrderService.class),
                ordersRepository,
                vnpayConfig());
        Orders order = TestData.order();
        when(ordersRepository.findById(order.getOrderId())).thenReturn(order);

        String url = paymentService.createPaymentUrl(240_000, String.valueOf(order.getOrderId()), "127.0.0.1");

        assertThat(url).startsWith("https://sandbox.example.test/pay?");
        assertThat(url).contains("vnp_TxnRef=17");
        assertThat(url).contains("vnp_SecureHash=");
    }

    @Test
    void vnpayIpnVerifiesSignatureAmountAndUpdatesOrderOnce() {
        PaymentServiceImpl paymentService = new PaymentServiceImpl(
                mock(PaymentsRepository.class),
                mock(OrderService.class),
                ordersRepository,
                vnpayConfig());
        Orders order = TestData.order();
        when(ordersRepository.findById(order.getOrderId())).thenReturn(order);

        Map<String, String> params = signedVnpayParams("17", "24000000", "00", "00");
        Map<String, String> result = paymentService.processIpn(params);

        assertThat(result).containsEntry("RspCode", "00");
        verify(ordersRepository).updateStatus(order.getOrderId(), "Da thanh toan");
    }

    @Test
    void vnpayReturnRejectsInvalidSignature() {
        PaymentServiceImpl paymentService = new PaymentServiceImpl(
                mock(PaymentsRepository.class),
                mock(OrderService.class),
                ordersRepository,
                vnpayConfig());
        Map<String, String> params = new HashMap<>();
        params.put("vnp_SecureHash", "bad");

        assertThatThrownBy(() -> paymentService.processReturn(params))
                .isInstanceOf(VnpayException.class);
    }

    private VnpayConfig vnpayConfig() {
        VnpayConfig config = mock(VnpayConfig.class);
        when(config.getVnpTmnCode()).thenReturn("TEST");
        when(config.getVnpHashSecret()).thenReturn("secret");
        when(config.getVnpPayUrl()).thenReturn("https://sandbox.example.test/pay");
        when(config.getVnpReturnUrl()).thenReturn("https://frontend.example.test/return");
        return config;
    }

    private Map<String, String> signedVnpayParams(
            String orderId,
            String amount,
            String responseCode,
            String transactionStatus) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("vnp_TxnRef", orderId);
        params.put("vnp_Amount", amount);
        params.put("vnp_ResponseCode", responseCode);
        params.put("vnp_TransactionStatus", transactionStatus);
        params.put("vnp_TransactionNo", "123456");
        params.put("vnp_SecureHash", VnpayUtils.hmacSHA512("secret", VnpayUtils.buildQueryString(params)));
        return params;
    }
}
