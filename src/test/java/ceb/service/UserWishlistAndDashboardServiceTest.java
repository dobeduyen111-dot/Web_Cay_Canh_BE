package ceb.service;

import java.util.List;
import java.util.Optional;

import ceb.TestData;
import ceb.domain.entity.Users;
import ceb.domain.model.Products;
import ceb.domain.res.DashboardResponse;
import ceb.exception.CurrentPasswordIncorrectException;
import ceb.exception.ProductNotFoundException;
import ceb.exception.UserNotFoundException;
import ceb.repository.DashboardRepository;
import ceb.repository.ProductsRepository;
import ceb.repository.UsersRepository;
import ceb.repository.WishlistRepository;
import ceb.security.CustomUserDetails;
import ceb.service.implement.CurrentUserServiceImpl;
import ceb.service.implement.DashboardServiceImpl;
import ceb.service.implement.DatabaseUserDetailsService;
import ceb.service.implement.UsersServiceImpl;
import ceb.service.implement.WishlistServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserWishlistAndDashboardServiceTest {

    @Test
    void currentUserServiceReadsUsersAndCustomUserDetailsPrincipals() {
        CurrentUserServiceImpl service = new CurrentUserServiceImpl();
        Users user = TestData.user();

        TestingAuthenticationToken usersAuth = new TestingAuthenticationToken(user, null);
        usersAuth.setAuthenticated(true);
        assertThat(service.getCurrentUserId(usersAuth)).isEqualTo(user.getUserId());

        TestingAuthenticationToken customAuth = new TestingAuthenticationToken(new CustomUserDetails(user), null);
        customAuth.setAuthenticated(true);
        assertThat(service.getCurrentUser(customAuth)).isSameAs(user);
    }

    @Test
    void usersServiceChangesPasswordAfterCurrentPasswordCheck() {
        UsersRepository usersRepository = mock(UsersRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        CurrentUserServiceImpl currentUserService = new CurrentUserServiceImpl();
        UsersServiceImpl service = new UsersServiceImpl(usersRepository, passwordEncoder, currentUserService);
        Users user = TestData.user();
        TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
        auth.setAuthenticated(true);

        when(passwordEncoder.matches("current", user.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("new-secret", user.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("new-secret")).thenReturn("encoded-new");

        service.changePassword(auth, "current", "new-secret");

        verify(usersRepository).updatePassword(user.getUserId(), "encoded-new");
    }

    @Test
    void usersServiceRejectsWrongCurrentPasswordAndMissingUsers() {
        UsersRepository usersRepository = mock(UsersRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UsersServiceImpl service = new UsersServiceImpl(usersRepository, passwordEncoder, new CurrentUserServiceImpl());
        Users user = TestData.user();
        TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
        auth.setAuthenticated(true);
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(auth, "wrong", "new-secret"))
                .isInstanceOf(CurrentPasswordIncorrectException.class);
        assertThatThrownBy(() -> service.getUsersByEmail("missing@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void wishlistAddsRemovesAndCountsExistingProducts() {
        WishlistRepository wishlistRepository = mock(WishlistRepository.class);
        ProductsRepository productsRepository = mock(ProductsRepository.class);
        WishlistServiceImpl service = new WishlistServiceImpl(wishlistRepository, productsRepository);
        Products product = TestData.product();
        when(productsRepository.findById(product.getProductId())).thenReturn(product);
        when(wishlistRepository.getWishlist(7)).thenReturn(List.of(product));
        when(wishlistRepository.countWishlist(7)).thenReturn(1);

        assertThat(service.getWishlist(7)).containsExactly(product);
        assertThat(service.add(7, product.getProductId())).isSameAs(product);
        service.remove(7, product.getProductId());
        assertThat(service.count(7)).isEqualTo(1);

        verify(wishlistRepository).add(7, product.getProductId());
        verify(wishlistRepository).remove(7, product.getProductId());
    }

    @Test
    void wishlistRejectsMissingProducts() {
        ProductsRepository productsRepository = mock(ProductsRepository.class);
        WishlistServiceImpl service = new WishlistServiceImpl(mock(WishlistRepository.class), productsRepository);
        when(productsRepository.findById(404)).thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> service.add(7, 404))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void dashboardServiceAggregatesRepositoryCounters() {
        DashboardRepository repository = mock(DashboardRepository.class);
        when(repository.getTotalOrders()).thenReturn(10);
        when(repository.getTotalRevenue()).thenReturn(500_000.0);
        when(repository.getTotalCustomers()).thenReturn(3);

        DashboardResponse response = new DashboardServiceImpl(repository).getDashboard();

        assertThat(response.getTotalOrders()).isEqualTo(10);
        assertThat(response.getTotalRevenue()).isEqualTo(500_000.0);
        assertThat(response.getTotalCustomers()).isEqualTo(3);
    }

    @Test
    void databaseUserDetailsServiceWrapsFoundUser() {
        UsersRepository usersRepository = mock(UsersRepository.class);
        Users user = TestData.user();
        when(usersRepository.findByUsername(user.getEmail())).thenReturn(Optional.of(user));
        DatabaseUserDetailsService service = new DatabaseUserDetailsService(usersRepository);

        assertThat(service.loadUserByUsername(user.getEmail()).getUsername()).isEqualTo(user.getEmail());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
