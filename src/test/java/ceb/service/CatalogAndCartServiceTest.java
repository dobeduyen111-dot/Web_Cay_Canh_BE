package ceb.service;

import java.util.List;

import ceb.TestData;
import ceb.domain.model.CartItem;
import ceb.domain.model.Categories;
import ceb.domain.model.Products;
import ceb.exception.CartItemNotOwnedException;
import ceb.exception.CategoryNameRequiredException;
import ceb.exception.CategoryNotFoundException;
import ceb.exception.InvalidProductPriceException;
import ceb.exception.KeywordRequiredException;
import ceb.exception.ProductNotFoundException;
import ceb.exception.QuantityMustBePositiveException;
import ceb.repository.CartItemsRepository;
import ceb.repository.CartRepository;
import ceb.repository.CategoriesRepository;
import ceb.repository.ProductsRepository;
import ceb.service.implement.CartServiceImpl;
import ceb.service.implement.CategoriesServiceImpl;
import ceb.service.implement.ProductsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CatalogAndCartServiceTest {

    @Test
    void categoryServiceCreatesUpdatesAndDeletesAfterValidation() {
        CategoriesRepository repository = mock(CategoriesRepository.class);
        CategoriesServiceImpl service = new CategoriesServiceImpl(repository);
        Categories category = TestData.category();

        assertThat(service.create(category)).isSameAs(category);
        verify(repository).save(category);

        Categories existing = TestData.category();
        when(repository.findById(1)).thenReturn(existing);
        Categories update = TestData.category();
        update.setCategoryId(null);
        assertThat(service.update(1, update).getCategoryId()).isEqualTo(1);
        service.delete(1);

        verify(repository).delete(1);
    }

    @Test
    void categoryServiceRejectsMissingCategoryOrName() {
        CategoriesRepository repository = mock(CategoriesRepository.class);
        CategoriesServiceImpl service = new CategoriesServiceImpl(repository);

        assertThatThrownBy(() -> service.findById(404))
                .isInstanceOf(CategoryNotFoundException.class);
        assertThatThrownBy(() -> service.create(new Categories()))
                .isInstanceOf(CategoryNameRequiredException.class);
    }

    @Test
    void productServiceValidatesAndDelegatesCatalogOperations() {
        ProductsRepository productsRepository = mock(ProductsRepository.class);
        CategoriesRepository categoriesRepository = mock(CategoriesRepository.class);
        ProductsServiceImpl service = new ProductsServiceImpl(productsRepository, categoriesRepository);
        Products product = TestData.product();
        when(categoriesRepository.findById(product.getCategoryId())).thenReturn(TestData.category());
        when(productsRepository.findById(product.getProductId())).thenReturn(product);
        when(productsRepository.findAll()).thenReturn(List.of(product));
        when(productsRepository.search("mon")).thenReturn(List.of(product));

        assertThat(service.findAll()).containsExactly(product);
        assertThat(service.findById(product.getProductId())).isSameAs(product);
        assertThat(service.search(" mon ")).containsExactly(product);
        assertThat(service.create(product)).isSameAs(product);
        assertThat(service.update(product.getProductId(), product)).isSameAs(product);

        service.delete(product.getProductId());
        verify(productsRepository).delete(product.getProductId());
    }

    @Test
    void productServiceMapsRepositoryAndValidationErrors() {
        ProductsRepository productsRepository = mock(ProductsRepository.class);
        CategoriesRepository categoriesRepository = mock(CategoriesRepository.class);
        ProductsServiceImpl service = new ProductsServiceImpl(productsRepository, categoriesRepository);
        when(productsRepository.findById(404)).thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> service.findById(404))
                .isInstanceOf(ProductNotFoundException.class);
        assertThatThrownBy(() -> service.search(" "))
                .isInstanceOf(KeywordRequiredException.class);

        Products invalid = TestData.product();
        invalid.setPrice(0);
        assertThatThrownBy(() -> service.create(invalid))
                .isInstanceOf(InvalidProductPriceException.class);
    }

    @Test
    void cartServiceAddsNewItemAndCalculatesTotals() {
        CartRepository cartRepository = mock(CartRepository.class);
        CartItemsRepository cartItemsRepository = mock(CartItemsRepository.class);
        ProductsRepository productsRepository = mock(ProductsRepository.class);
        CartServiceImpl service = new CartServiceImpl(cartRepository, cartItemsRepository, productsRepository);
        CartItem item = TestData.cartItem();

        when(productsRepository.findById(item.getProductId())).thenReturn(TestData.product());
        when(cartRepository.getByUserId(7)).thenReturn(TestData.cart());
        when(cartItemsRepository.findItem(item.getCartId(), item.getProductId()))
                .thenReturn(null)
                .thenReturn(item);
        when(cartItemsRepository.findAll(item.getCartId())).thenReturn(List.of(item));

        assertThat(service.addItem(7, item.getProductId(), 2)).isSameAs(item);
        assertThat(service.getTotalQuantity(7)).isEqualTo(2);
        assertThat(service.getTotalAmount(7)).isEqualTo(240_000);
        verify(cartItemsRepository).addItem(item.getCartId(), item.getProductId(), 2);
    }

    @Test
    void cartServiceUpdatesExistingItemAndProtectsOwnership() {
        CartRepository cartRepository = mock(CartRepository.class);
        CartItemsRepository cartItemsRepository = mock(CartItemsRepository.class);
        ProductsRepository productsRepository = mock(ProductsRepository.class);
        CartServiceImpl service = new CartServiceImpl(cartRepository, cartItemsRepository, productsRepository);
        CartItem item = TestData.cartItem();

        when(cartRepository.getByUserId(7)).thenReturn(TestData.cart());
        when(cartItemsRepository.findById(item.getCartItemId())).thenReturn(item);
        when(cartItemsRepository.findById(99)).thenReturn(item);
        when(cartItemsRepository.findById(5)).thenReturn(item);
        when(cartItemsRepository.findById(item.getCartItemId())).thenReturn(item);
        when(cartItemsRepository.findById(88)).thenReturn(null);
        when(cartItemsRepository.findById(55)).thenReturn(item);
        when(cartItemsRepository.findById(item.getCartItemId())).thenReturn(item);
        when(cartItemsRepository.findById(123)).thenReturn(item);
        when(cartItemsRepository.findById(5)).thenReturn(item);

        assertThat(service.updateQuantity(7, item.getCartItemId(), 4)).isSameAs(item);
        service.removeItem(7, item.getCartItemId());
        service.clear(7);

        assertThatThrownBy(() -> service.updateQuantity(7, item.getCartItemId(), 0))
                .isInstanceOf(QuantityMustBePositiveException.class);

        CartItem otherUserItem = TestData.cartItem();
        otherUserItem.setCartId(999);
        when(cartItemsRepository.findById(44)).thenReturn(otherUserItem);
        assertThatThrownBy(() -> service.removeItem(7, 44))
                .isInstanceOf(CartItemNotOwnedException.class);
    }
}
