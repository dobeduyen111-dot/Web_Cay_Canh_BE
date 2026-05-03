package ceb.service.implement;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import ceb.domain.model.Products;
import ceb.exception.ProductNotFoundException;
import ceb.repository.ProductsRepository;
import ceb.repository.WishlistRepository;
import ceb.service.service.WishlistService;

@Service
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductsRepository productsRepository;

    public WishlistServiceImpl(WishlistRepository wishlistRepository, ProductsRepository productsRepository) {
        this.wishlistRepository = wishlistRepository;
        this.productsRepository = productsRepository;
    }

    @Override
    public List<Products> getWishlist(int userId) {
        return wishlistRepository.getWishlist(userId);
    }

    @Override
    public Products add(int userId, int productId) {
        Products product = findProduct(productId);
        wishlistRepository.add(userId, productId);
        return product;
    }

    @Override
    public void remove(int userId, int productId) {
        findProduct(productId);
        wishlistRepository.remove(userId, productId);
    }

    @Override
    public int count(int userId) {
        return wishlistRepository.countWishlist(userId);
    }

    private Products findProduct(int productId) {
        try {
            return productsRepository.findById(productId);
        } catch (EmptyResultDataAccessException ex) {
            throw new ProductNotFoundException(productId);
        }
    }
}
