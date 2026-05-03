package ceb.service.implement;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import ceb.domain.model.Cart;
import ceb.domain.model.CartItem;
import ceb.exception.CartItemNotFoundException;
import ceb.exception.CartItemNotOwnedException;
import ceb.exception.ProductNotFoundException;
import ceb.exception.QuantityMustBePositiveException;
import ceb.repository.CartItemsRepository;
import ceb.repository.CartRepository;
import ceb.repository.ProductsRepository;
import ceb.service.service.CartService;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemsRepository cartItemsRepository;
    private final ProductsRepository productsRepository;

    public CartServiceImpl(
            CartRepository cartRepository,
            CartItemsRepository cartItemsRepository,
            ProductsRepository productsRepository) {
        this.cartRepository = cartRepository;
        this.cartItemsRepository = cartItemsRepository;
        this.productsRepository = productsRepository;
    }

    @Override
    public List<CartItem> getItems(int userId) {
        int cartId = getOrCreateCartId(userId);
        return cartItemsRepository.findAll(cartId);
    }

    @Override
    public CartItem addItem(int userId, int productId, int quantity) {
        if (quantity <= 0) {
            throw new QuantityMustBePositiveException();
        }

        try {
            productsRepository.findById(productId);
        } catch (EmptyResultDataAccessException ex) {
            throw new ProductNotFoundException(productId);
        }

        int cartId = getOrCreateCartId(userId);
        CartItem existingItem = cartItemsRepository.findItem(cartId, productId);

        if (existingItem == null) {
            cartItemsRepository.addItem(cartId, productId, quantity);
        } else {
            cartItemsRepository.updateQuantity(existingItem.getCartItemId(), existingItem.getQuantity() + quantity);
        }

        return cartItemsRepository.findItem(cartId, productId);
    }

    @Override
    public CartItem updateQuantity(int userId, int cartItemId, int quantity) {
        if (quantity <= 0) {
            throw new QuantityMustBePositiveException();
        }

        CartItem cartItem = getOwnedCartItem(userId, cartItemId);
        cartItemsRepository.updateQuantity(cartItemId, quantity);
        return cartItemsRepository.findById(cartItemId);
    }

    @Override
    public void removeItem(int userId, int cartItemId) {
        getOwnedCartItem(userId, cartItemId);
        cartItemsRepository.delete(cartItemId);
    }

    @Override
    public void clear(int userId) {
        int cartId = getOrCreateCartId(userId);
        cartItemsRepository.clear(cartId);
    }

    @Override
    public int getTotalQuantity(int userId) {
        return getItems(userId).stream().mapToInt(CartItem::getQuantity).sum();
    }

    @Override
    public double getTotalAmount(int userId) {
        return getItems(userId).stream()
                .mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice())
                .sum();
    }

    private int getOrCreateCartId(int userId) {
        Cart cart = cartRepository.getByUserId(userId);
        return cart != null ? cart.getCartId() : cartRepository.createCart(userId);
    }

    private CartItem getOwnedCartItem(int userId, int cartItemId) {
        CartItem cartItem = cartItemsRepository.findById(cartItemId);
        if (cartItem == null) {
            throw new CartItemNotFoundException(cartItemId);
        }

        int cartId = getOrCreateCartId(userId);
        if (cartItem.getCartId() != cartId) {
            throw new CartItemNotOwnedException();
        }
        return cartItem;
    }
}
