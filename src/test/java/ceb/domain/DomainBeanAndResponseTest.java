package ceb.domain;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import ceb.TestData;
import ceb.domain.entity.Users;
import ceb.domain.model.Categories;
import ceb.domain.model.Products;
import ceb.domain.res.CartItemResponse;
import ceb.domain.res.CategoryResponse;
import ceb.domain.res.OrderItemResponse;
import ceb.domain.res.OrderResponse;
import ceb.domain.res.PaymentResponse;
import ceb.domain.res.ProductResponse;
import ceb.domain.res.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

class DomainBeanAndResponseTest {

    @Test
    void domainBeansExposeUsableAccessors() {
        beanClasses().forEach(this::exerciseAccessors);
    }

    @Test
    void responseFactoriesMapDomainObjects() {
        ProductResponse product = ProductResponse.from(TestData.product());
        assertThat(product.getProductId()).isEqualTo(TestData.product().getProductId());
        assertThat(product.getProductName()).isEqualTo(TestData.product().getProductName());

        CategoryResponse category = CategoryResponse.from(TestData.category());
        assertThat(category.getCategoryId()).isEqualTo(TestData.category().getCategoryId());

        UserResponse user = UserResponse.from(TestData.user());
        assertThat(user.getEmail()).isEqualTo(TestData.user().getEmail());

        CartItemResponse cartItem = CartItemResponse.from(TestData.cartItem());
        assertThat(cartItem.getProduct().getProductName()).isEqualTo(TestData.product().getProductName());

        OrderItemResponse orderItem = OrderItemResponse.from(TestData.orderItem());
        assertThat(orderItem.getProductId()).isEqualTo(TestData.product().getProductId());

        OrderResponse order = OrderResponse.from(TestData.order());
        assertThat(order.getItems()).hasSize(1);

        PaymentResponse payment = PaymentResponse.from(TestData.payment());
        assertThat(payment.getPaymentMethod()).isEqualTo("VNPAY");
    }

    @Test
    void usersAuthoritiesUseRolePrefix() {
        Users user = TestData.user();

        assertThat(user.getUsername()).isEqualTo(user.getEmail());
        assertThat(user.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_USER");
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.toString()).contains("userId=7", "a@example.com");
    }

    private Stream<Class<?>> beanClasses() {
        return Stream.of("ceb/domain/entity", "ceb/domain/model", "ceb/domain/req", "ceb/domain/res")
                .flatMap(this::classesInPackagePath)
                .filter(type -> !type.isInterface() && !type.isEnum() && !type.isRecord());
    }

    private Stream<Class<?>> classesInPackagePath(String packagePath) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(packagePath);
        if (url == null || !"file".equals(url.getProtocol())) {
            return Stream.empty();
        }

        File directory = new File(url.getFile());
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".class") && !name.contains("$"));
        if (files == null) {
            return Stream.empty();
        }

        String packageName = packagePath.replace('/', '.');
        return Stream.of(files).map(file -> loadClass(packageName + "." + file.getName().replace(".class", "")));
    }

    private Class<?> loadClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ex) {
            throw new AssertionError(ex);
        }
    }

    private void exerciseAccessors(Class<?> type) {
        Object bean = newInstance(type);
        if (bean == null) {
            return;
        }

        Set<String> setterNames = Stream.of(type.getMethods())
                .filter(method -> method.getName().startsWith("set"))
                .filter(method -> method.getParameterCount() == 1)
                .map(Method::getName)
                .collect(java.util.stream.Collectors.toSet());

        for (Method setter : type.getMethods()) {
            if (!setter.getName().startsWith("set") || setter.getParameterCount() != 1) {
                continue;
            }

            Object value = sampleValue(setter.getParameterTypes()[0]);
            invoke(setter, bean, value);

            String property = setter.getName().substring(3);
            Method getter = findGetter(type, property);
            if (getter != null) {
                assertThat(invoke(getter, bean)).isEqualTo(value);
            }
        }

        // Exercise read-only getters such as Lombok all-args/no-args defaults.
        Stream.of(type.getMethods())
                .filter(method -> method.getParameterCount() == 0)
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .filter(method -> method.getDeclaringClass() != Object.class)
                .filter(method -> method.getName().startsWith("get") || method.getName().startsWith("is"))
                .filter(method -> !setterNames.contains("set" + propertyName(method)))
                .forEach(method -> invoke(method, bean));
    }

    private String propertyName(Method getter) {
        String prefix = getter.getName().startsWith("is") ? "is" : "get";
        return getter.getName().substring(prefix.length());
    }

    private Method findGetter(Class<?> type, String property) {
        for (String getterName : List.of("get" + property, "is" + property)) {
            try {
                return type.getMethod(getterName);
            } catch (NoSuchMethodException ignored) {
                // Try the next JavaBean getter convention.
            }
        }
        return null;
    }

    private Object newInstance(Class<?> type) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (Exception ex) {
            throw new AssertionError("Cannot instantiate " + type.getName(), ex);
        }
    }

    private Object invoke(Method method, Object target, Object... args) {
        try {
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception ex) {
            throw new AssertionError("Cannot invoke " + method, ex);
        }
    }

    private Object sampleValue(Class<?> type) {
        if (type == String.class) {
            return "value";
        }
        if (type == int.class || type == Integer.class) {
            return 42;
        }
        if (type == long.class || type == Long.class) {
            return 42L;
        }
        if (type == double.class || type == Double.class) {
            return 42.5;
        }
        if (type == boolean.class || type == Boolean.class) {
            return true;
        }
        if (type == Date.class) {
            return new Date(1_700_000_000_000L);
        }
        if (type == LocalDateTime.class) {
            return LocalDateTime.of(2026, 1, 1, 12, 0);
        }
        if (List.class.isAssignableFrom(type)) {
            return List.of();
        }
        if (type == Products.class) {
            return TestData.product();
        }
        if (type == Categories.class) {
            return TestData.category();
        }
        if (type.getName().equals("org.springframework.web.multipart.MultipartFile")) {
            return new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[] {1, 2, 3});
        }
        return null;
    }
}
