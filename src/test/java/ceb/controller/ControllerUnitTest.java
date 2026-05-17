package ceb.controller;

import java.util.List;
import java.util.Map;

import ceb.TestData;
import ceb.domain.entity.Users;
import ceb.domain.model.Payments;
import ceb.domain.req.CartAddItemRequest;
import ceb.domain.req.CartUpdateItemRequest;
import ceb.domain.req.CategoryRequest;
import ceb.domain.req.CheckoutRequest;
import ceb.domain.req.PaymentRequest;
import ceb.domain.req.ProductRequest;
import ceb.domain.req.UserChangePasswordRequest;
import ceb.domain.req.VnpayCreatePaymentRequest;
import ceb.service.service.CartService;
import ceb.service.service.CategoriesService;
import ceb.service.service.CloudinaryService;
import ceb.service.service.CurrentUserService;
import ceb.service.service.OrderService;
import ceb.service.service.PaymentService;
import ceb.service.service.ProductsService;
import ceb.service.service.UsersService;
import ceb.service.service.WishlistService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ControllerUnitTest {

    private final Authentication authentication = new TestingAuthenticationToken("user", null);

    @Test
    void cartControllerReturnsCartSnapshotsForMutations() {
        CartService cartService = mock(CartService.class);
        CurrentUserService currentUserService = currentUserService();
        CartController controller = new CartController(cartService, currentUserService);
        when(cartService.getItems(7)).thenReturn(List.of(TestData.cartItem()));
        when(cartService.getTotalQuantity(7)).thenReturn(2);
        when(cartService.getTotalAmount(7)).thenReturn(240_000.0);
        when(cartService.addItem(7, 11, 1)).thenReturn(TestData.cartItem());
        when(cartService.updateQuantity(7, 5, 3)).thenReturn(TestData.cartItem());

        assertThat(controller.view(authentication).getTotalQuantity()).isEqualTo(2);

        CartAddItemRequest add = new CartAddItemRequest();
        add.setProductId(11);
        assertThat(controller.addItem(authentication, add).getCart().getTotalAmount()).isEqualTo(240_000.0);

        CartUpdateItemRequest update = new CartUpdateItemRequest();
        update.setQuantity(3);
        assertThat(controller.updateQuantity(authentication, 5, update).getItem().getCartItemId()).isEqualTo(5);
        assertThat(controller.removeItem(authentication, 5).getMessage()).contains("Xoa san pham");
        assertThat(controller.clear(authentication).getMessage()).contains("Da xoa");
    }

    @Test
    void orderAndCheckoutControllersUseCurrentUser() {
        CurrentUserService currentUserService = currentUserService();
        OrderService orderService = mock(OrderService.class);
        when(orderService.checkout(7, "Da Lat", "note")).thenReturn(TestData.order());
        when(orderService.findByUserId(7)).thenReturn(List.of(TestData.order()));
        when(orderService.findByIdForUser(7, 17)).thenReturn(TestData.order());
        when(orderService.findItemsByOrderIdForUser(7, 17)).thenReturn(List.of(TestData.orderItem()));

        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setShippingAddress("Da Lat");
        checkoutRequest.setNote("note");
        assertThat(new CheckoutController(orderService, currentUserService)
                .checkout(authentication, checkoutRequest).getOrder().getOrderId()).isEqualTo(17);

        OrderController orderController = new OrderController(orderService, currentUserService);
        assertThat(orderController.getMyOrders(authentication)).hasSize(1);
        assertThat(orderController.getOrder(authentication, 17).getOrderId()).isEqualTo(17);
        assertThat(orderController.getOrderItems(authentication, 17)).hasSize(1);

        assertThat(new OrderHistoryController(orderService, currentUserService)
                .history(authentication).getTotalOrders()).isEqualTo(1);
    }

    @Test
    void catalogControllersMapRequestsAndResponses() {
        CategoriesService categoriesService = mock(CategoriesService.class);
        ProductsService productsService = mock(ProductsService.class);
        CloudinaryService cloudinaryService = mock(CloudinaryService.class);
        CategoriesController categoriesController = new CategoriesController(categoriesService);
        ProductsController productsController = new ProductsController(productsService, cloudinaryService);
        when(categoriesService.findAll()).thenReturn(List.of(TestData.category()));
        when(categoriesService.findById(1)).thenReturn(TestData.category());
        when(categoriesService.create(any())).thenReturn(TestData.category());
        when(categoriesService.update(anyInt(), any())).thenReturn(TestData.category());
        when(productsService.findAll()).thenReturn(List.of(TestData.product()));
        when(productsService.findById(11)).thenReturn(TestData.product());
        when(productsService.search("mon")).thenReturn(List.of(TestData.product()));
        when(productsService.findByCategory(1)).thenReturn(List.of(TestData.product()));
        when(productsService.create(any())).thenReturn(TestData.product());
        when(productsService.update(anyInt(), any())).thenReturn(TestData.product());

        CategoryRequest categoryRequest = new CategoryRequest("Plants", "Green", "leaf");
        assertThat(categoriesController.findAll()).hasSize(1);
        assertThat(categoriesController.findById(1).getCategoryName()).isEqualTo("Plants");
        assertThat(categoriesController.create(categoryRequest).getCategoryId()).isEqualTo(1);
        assertThat(categoriesController.update(1, categoryRequest).getCategoryId()).isEqualTo(1);
        assertThat(categoriesController.delete(1).getMessage()).contains("Xoa");

        ProductRequest productRequest = new ProductRequest();
        productRequest.setCategoryId(1);
        productRequest.setProductName("Monstera");
        productRequest.setPrice(120_000.0);
        productRequest.setStock(9);
        productRequest.setActive(true);
        assertThat(productsController.findAll()).hasSize(1);
        assertThat(productsController.findById(11).getProductId()).isEqualTo(11);
        assertThat(productsController.search("mon")).hasSize(1);
        assertThat(productsController.findByCategory(1)).hasSize(1);
        assertThat(productsController.create(productRequest).getProductName()).isEqualTo("Monstera");
        assertThat(productsController.update(11, productRequest).getProductId()).isEqualTo(11);
        assertThat(productsController.delete(11).getMessage()).contains("Xoa");
    }

    @Test
    void userWishlistAndPaymentControllersMapUserScopedOperations() {
        CurrentUserService currentUserService = currentUserService();
        UsersService usersService = mock(UsersService.class);
        WishlistService wishlistService = mock(WishlistService.class);
        PaymentService paymentService = mock(PaymentService.class);
        when(usersService.findAll()).thenReturn(List.of(TestData.user()));
        when(usersService.getUsersByEmail("a@example.com")).thenReturn(TestData.user());
        when(currentUserService.getCurrentUser(authentication)).thenReturn(TestData.user());
        when(wishlistService.getWishlist(7)).thenReturn(List.of(TestData.product()));
        when(wishlistService.count(7)).thenReturn(1);
        when(wishlistService.add(7, 11)).thenReturn(TestData.product());
        when(paymentService.create(anyInt(), any(Payments.class))).thenReturn(TestData.payment());
        when(paymentService.findByIdForUser(7, 23)).thenReturn(TestData.payment());
        when(paymentService.findByOrderIdForUser(7, 17)).thenReturn(List.of(TestData.payment()));
        when(paymentService.createPaymentUrl(240_000, "17", "127.0.0.1")).thenReturn("https://pay");
        when(paymentService.processReturn(Map.of("ok", "1"))).thenReturn(Map.of("success", true));

        UsersController usersController = new UsersController(usersService, currentUserService);
        assertThat(usersController.getAlls()).hasSize(1);
        assertThat(usersController.getByEmail(" a@example.com ").getEmail()).isEqualTo("a@example.com");
        assertThat(usersController.me(authentication).getUserId()).isEqualTo(7);
        assertThat(usersController.changePassword(authentication, new UserChangePasswordRequest("old", "new123")).getMessage())
                .contains("Doi");

        WishlistController wishlistController = new WishlistController(wishlistService, currentUserService);
        assertThat(wishlistController.getWishlist(authentication).getCount()).isEqualTo(1);
        assertThat(wishlistController.add(authentication, 11).getProduct().getProductId()).isEqualTo(11);
        assertThat(wishlistController.remove(authentication, 11).getCount()).isEqualTo(1);

        PaymentController paymentController = new PaymentController(paymentService, currentUserService);
        PaymentRequest paymentRequest = new PaymentRequest(17, "VNPAY", null, true);
        assertThat(paymentController.create(authentication, paymentRequest).getPayment().getPaymentId()).isEqualTo(23);
        assertThat(paymentController.getById(authentication, 23).getPaymentMethod()).isEqualTo("VNPAY");
        assertThat(paymentController.getByOrderId(authentication, 17)).hasSize(1);

        VnpayCreatePaymentRequest vnpayRequest = new VnpayCreatePaymentRequest();
        vnpayRequest.setOrderId("17");
        vnpayRequest.setAmount(240_000);
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("127.0.0.1");
        assertThat(paymentController.createVnpayUrl(vnpayRequest, httpRequest)).containsEntry("paymentUrl", "https://pay");
        assertThat(paymentController.vnpayReturn(Map.of("ok", "1"))).containsEntry("success", true);
    }

    @Test
    void homeControllerBuildsThreeProductGroups() {
        ProductsService productsService = mock(ProductsService.class);
        when(productsService.getByCategoryLimit(1, 5)).thenReturn(List.of(TestData.product()));
        when(productsService.getByCategoryLimit(2, 4)).thenReturn(List.of(TestData.product()));
        when(productsService.getByCategoryLimit(3, 3)).thenReturn(List.of(TestData.product()));

        assertThat(new HomeController(productsService).home(5, 4, 3).getCayCanh()).hasSize(1);
    }

    private CurrentUserService currentUserService() {
        CurrentUserService service = mock(CurrentUserService.class);
        Users user = TestData.user();
        when(service.getCurrentUserId(authentication)).thenReturn(user.getUserId());
        when(service.getCurrentUser(authentication)).thenReturn(user);
        return service;
    }
}
