package ceb.repository;

import java.util.List;
import java.util.UUID;

import ceb.TestData;
import ceb.domain.entity.Users;
import ceb.domain.model.Cart;
import ceb.domain.model.Categories;
import ceb.domain.model.Orders;
import ceb.domain.model.Payments;
import ceb.domain.model.Products;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryIntegrationTest {

    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        jdbc = new JdbcTemplate(dataSource);
        createSchema();
        seedData();
    }

    @Test
    void usersRepositoryFindsSavesUpdatesAndDeletesUsers() {
        UsersRepository repository = repository(new UsersRepository());

        assertThat(repository.findByEmail("a@example.com")).isPresent();
        assertThat(repository.findByUsername("a@example.com")).isPresent();
        assertThat(repository.findByPhone("0900000000")).isPresent();
        assertThat(repository.getUserIdByEmail("a@example.com")).contains(7);
        assertThat(repository.findAlls()).hasSize(2);

        Users user = TestData.user();
        user.setEmail("new@example.com");
        user.setPhone("0911111111");
        assertThat(repository.save(user)).isEqualTo(1);
        assertThat(repository.updatePassword(7, "changed")).isEqualTo(1);
        assertThat(repository.deleteById(8)).isEqualTo(1);
    }

    @Test
    void categoriesAndProductsRepositoriesCoverCrudAndQueries() {
        CategoriesRepository categoriesRepository = repository(new CategoriesRepository());
        ProductsRepository productsRepository = repository(new ProductsRepository());

        assertThat(categoriesRepository.findAll()).hasSize(1);
        assertThat(categoriesRepository.findById(1).getCategoryName()).isEqualTo("Plants");
        Categories category = TestData.category();
        category.setCategoryId(null);
        category.setCategoryName("Pots");
        assertThat(categoriesRepository.save(category)).isEqualTo(1);
        category.setCategoryId(1);
        assertThat(categoriesRepository.save(category)).isEqualTo(1);

        assertThat(productsRepository.findAll()).hasSize(1);
        assertThat(productsRepository.findById(11).getProductName()).isEqualTo("Monstera");
        assertThat(productsRepository.search("mon")).hasSize(1);
        assertThat(productsRepository.findByCategory(1)).hasSize(1);
        assertThat(productsRepository.getByCategoryLimit(1, 1)).hasSize(1);
        Products product = TestData.product();
        product.setProductId(0);
        product.setProductName("Palm");
        assertThat(productsRepository.save(product)).isEqualTo(1);
        product.setProductId(11);
        assertThat(productsRepository.update(product)).isEqualTo(1);
        assertThat(productsRepository.delete(11)).isEqualTo(1);
    }

    @Test
    void cartWishlistOrderAndPaymentRepositoriesUseJdbcMappings() {
        CartRepository cartRepository = repository(new CartRepository());
        CartItemsRepository cartItemsRepository = repository(new CartItemsRepository());
        WishlistRepository wishlistRepository = repository(new WishlistRepository());
        OrdersRepository ordersRepository = repository(new OrdersRepository());
        OrderItemsRepository orderItemsRepository = repository(new OrderItemsRepository());
        PaymentsRepository paymentsRepository = repository(new PaymentsRepository());
        DashboardRepository dashboardRepository = repository(new DashboardRepository());

        Cart cart = cartRepository.getByUserId(7);
        assertThat(cart.getCartId()).isEqualTo(3);
        assertThat(cartRepository.createCart(8)).isPositive();
        assertThat(cartItemsRepository.findAll(3)).hasSize(1);
        assertThat(cartItemsRepository.findItem(3, 11).getQuantity()).isEqualTo(2);
        assertThat(cartItemsRepository.findById(5).getProduct().getProductName()).isEqualTo("Monstera");
        cartItemsRepository.addItem(3, 11, 1);
        cartItemsRepository.updateQuantity(5, 4);
        cartItemsRepository.delete(5);

        assertThat(wishlistRepository.getWishlist(7)).hasSize(1);
        assertThat(wishlistRepository.countWishlist(7)).isEqualTo(1);
        wishlistRepository.remove(7, 11);

        Orders order = TestData.order();
        int newOrderId = ordersRepository.createOrder(order);
        assertThat(ordersRepository.findById(17).getStatus()).isEqualTo("Cho xu ly");
        assertThat(ordersRepository.findByUserId(7)).isNotEmpty();
        assertThat(ordersRepository.findAll()).isNotEmpty();
        ordersRepository.updateStatus(newOrderId, "Da giao");
        assertThat(orderItemsRepository.insertItem(TestData.orderItem())).isEqualTo(1);
        assertThat(orderItemsRepository.findByOrderId(17)).isNotEmpty();

        Payments payment = TestData.payment();
        int paymentId = paymentsRepository.create(payment);
        assertThat(paymentsRepository.findById(paymentId).getPaymentMethod()).isEqualTo("VNPAY");
        assertThat(paymentsRepository.findByOrderId(17)).isNotEmpty();

        assertThat(dashboardRepository.getTotalOrders()).isPositive();
        assertThat(dashboardRepository.getTotalRevenue()).isPositive();
        assertThat(dashboardRepository.getTotalCustomers()).isPositive();

        ordersRepository.delete(17);
    }

    private <T> T repository(T repository) {
        ReflectionTestUtils.setField(repository, "jdbc", jdbc);
        return repository;
    }

    private void createSchema() {
        jdbc.execute("""
                CREATE TABLE Users (
                    UserId INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    FullName VARCHAR(100),
                    Email VARCHAR(150),
                    Password VARCHAR(255),
                    Phone VARCHAR(30),
                    Address VARCHAR(255),
                    Role VARCHAR(20),
                    Enabled BOOLEAN,
                    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
        jdbc.execute("""
                CREATE TABLE Categories (
                    CategoryId INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    CategoryName VARCHAR(100),
                    Description VARCHAR(500),
                    Icon VARCHAR(100)
                )
                """);
        jdbc.execute("""
                CREATE TABLE Products (
                    ProductId INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    CategoryId INT,
                    ProductName VARCHAR(150),
                    Description VARCHAR(5000),
                    CareGuide VARCHAR(5000),
                    Price DOUBLE PRECISION,
                    Stock INT,
                    Image VARCHAR(500),
                    IsActive BOOLEAN,
                    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
        jdbc.execute("CREATE TABLE Cart (CartId INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, UserId INT)");
        jdbc.execute("""
                CREATE TABLE CartItems (
                    CartItemId INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    CartId INT,
                    ProductId INT,
                    Quantity INT,
                    AddedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
        jdbc.execute("""
                CREATE TABLE Wishlist (
                    UserId INT,
                    ProductId INT,
                    PRIMARY KEY (UserId, ProductId)
                )
                """);
        jdbc.execute("""
                CREATE TABLE Orders (
                    OrderId INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    UserId INT,
                    OrderDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    Status VARCHAR(50),
                    TotalAmount DOUBLE PRECISION,
                    ShippingAddress VARCHAR(255),
                    Note VARCHAR(1000)
                )
                """);
        jdbc.execute("""
                CREATE TABLE OrderItems (
                    OrderItemId INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    OrderId INT,
                    ProductId INT,
                    Quantity INT,
                    Price DOUBLE PRECISION
                )
                """);
        jdbc.execute("""
                CREATE TABLE Payments (
                    PaymentId INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    OrderId INT,
                    PaymentMethod VARCHAR(50),
                    Amount DOUBLE PRECISION,
                    IsSuccessful BOOLEAN,
                    PaymentDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
    }

    private void seedData() {
        jdbc.update("""
                INSERT INTO Users (UserId, FullName, Email, Password, Phone, Address, Role, Enabled)
                VALUES
                (7, 'Nguyen Van A', 'a@example.com', 'encoded', '0900000000', 'Da Lat', 'USER', TRUE),
                (8, 'Admin', 'admin@example.com', 'encoded', '0911111111', 'Da Lat', 'ADMIN', TRUE)
                """);
        jdbc.update("INSERT INTO Categories (CategoryId, CategoryName, Description, Icon) VALUES (1, 'Plants', 'Green', 'leaf')");
        jdbc.update("""
                INSERT INTO Products (ProductId, CategoryId, ProductName, Description, CareGuide, Price, Stock, Image, IsActive)
                VALUES (11, 1, 'Monstera', 'Indoor plant', 'Water weekly', 120000, 9, 'image.jpg', TRUE)
                """);
        jdbc.update("INSERT INTO Cart (CartId, UserId) VALUES (3, 7)");
        jdbc.update("INSERT INTO CartItems (CartItemId, CartId, ProductId, Quantity) VALUES (5, 3, 11, 2)");
        jdbc.update("INSERT INTO Wishlist (UserId, ProductId) VALUES (7, 11)");
        jdbc.update("""
                INSERT INTO Orders (OrderId, UserId, Status, TotalAmount, ShippingAddress, Note)
                VALUES (17, 7, 'Cho xu ly', 240000, 'Da Lat', 'note')
                """);
        jdbc.update("INSERT INTO OrderItems (OrderItemId, OrderId, ProductId, Quantity, Price) VALUES (19, 17, 11, 2, 120000)");
        jdbc.update("INSERT INTO Payments (PaymentId, OrderId, PaymentMethod, Amount, IsSuccessful) VALUES (23, 17, 'VNPAY', 240000, TRUE)");
        jdbc.execute("ALTER TABLE Users ALTER COLUMN UserId RESTART WITH 9");
        jdbc.execute("ALTER TABLE Categories ALTER COLUMN CategoryId RESTART WITH 2");
        jdbc.execute("ALTER TABLE Products ALTER COLUMN ProductId RESTART WITH 12");
        jdbc.execute("ALTER TABLE Cart ALTER COLUMN CartId RESTART WITH 4");
        jdbc.execute("ALTER TABLE Orders ALTER COLUMN OrderId RESTART WITH 18");
        jdbc.execute("ALTER TABLE OrderItems ALTER COLUMN OrderItemId RESTART WITH 20");
        jdbc.execute("ALTER TABLE Payments ALTER COLUMN PaymentId RESTART WITH 24");
    }
}
