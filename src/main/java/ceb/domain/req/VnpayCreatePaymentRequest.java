package ceb.domain.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class VnpayCreatePaymentRequest {

    @Positive(message = "amount phai lon hon 0")
    private long amount;

    @NotBlank(message = "orderId khong duoc de trong")
    private String orderId;

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
