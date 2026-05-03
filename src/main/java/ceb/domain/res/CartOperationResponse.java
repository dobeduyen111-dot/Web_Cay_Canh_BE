package ceb.domain.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartOperationResponse {

    private String message;
    private CartItemResponse item;
    private CartResponse cart;
}
