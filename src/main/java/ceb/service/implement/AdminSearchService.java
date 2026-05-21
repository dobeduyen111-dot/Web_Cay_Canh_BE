package ceb.service.implement;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import ceb.domain.entity.Users;
import ceb.domain.model.Orders;
import ceb.domain.model.Products;
import ceb.domain.res.AdminSearchItemResponse;
import ceb.domain.res.AdminSearchResponse;
import ceb.repository.OrdersRepository;
import ceb.repository.ProductsRepository;
import ceb.repository.UsersRepository;

@Service
public class AdminSearchService {

    private static final int MAX_RESULTS = 12;

    private final ProductsRepository productsRepository;
    private final UsersRepository usersRepository;
    private final OrdersRepository ordersRepository;

    public AdminSearchService(
            ProductsRepository productsRepository,
            UsersRepository usersRepository,
            OrdersRepository ordersRepository) {
        this.productsRepository = productsRepository;
        this.usersRepository = usersRepository;
        this.ordersRepository = ordersRepository;
    }

    public AdminSearchResponse search(String query) {
        String trimmedQuery = query == null ? "" : query.trim();
        String normalizedQuery = normalize(trimmedQuery);
        if (normalizedQuery.isBlank()) {
            return new AdminSearchResponse(trimmedQuery, List.of());
        }

        List<AdminSearchItemResponse> results = new ArrayList<>();

        long totalProducts = productsRepository.countAdminProducts(null);
        int productLimit = (int) Math.min(totalProducts, 5000L);
        productsRepository.findAdminPage(0, Math.max(productLimit, 1), null)
                .forEach(product -> addProductResult(results, product, trimmedQuery, normalizedQuery));

        usersRepository.findAlls()
                .forEach(user -> addUserResult(results, user, trimmedQuery, normalizedQuery));

        ordersRepository.findAll()
                .forEach(order -> addOrderResult(results, order, trimmedQuery, normalizedQuery));

        List<AdminSearchItemResponse> sorted = results.stream()
                .filter(item -> item.getScore() > 0)
                .sorted(Comparator.comparingDouble(AdminSearchItemResponse::getScore).reversed()
                        .thenComparing(AdminSearchItemResponse::getTitle, String.CASE_INSENSITIVE_ORDER))
                .limit(MAX_RESULTS)
                .toList();

        return new AdminSearchResponse(trimmedQuery, sorted);
    }

    private void addProductResult(List<AdminSearchItemResponse> results, Products product, String query, String normalizedQuery) {
        double score = score(normalizedQuery, product.getProductName(), String.valueOf(product.getProductId()));
        if (score <= 0) {
            return;
        }

        results.add(new AdminSearchItemResponse(
                "PRODUCT",
                String.valueOf(product.getProductId()),
                product.getProductName(),
                "Sản phẩm #" + product.getProductId(),
                product.isActive() ? "Đang bán" : "Đang ẩn",
                "/admin/products?edit=" + product.getProductId() + "&highlight=" + product.getProductId(),
                score));
    }

    private void addUserResult(List<AdminSearchItemResponse> results, Users user, String query, String normalizedQuery) {
        double score = score(normalizedQuery, user.getFullName(), user.getEmail(), user.getPhone(), String.valueOf(user.getUserId()));
        if (score <= 0) {
            return;
        }

        String keyword = encode(user.getEmail() == null || user.getEmail().isBlank() ? user.getFullName() : user.getEmail());
        results.add(new AdminSearchItemResponse(
                "CUSTOMER",
                String.valueOf(user.getUserId()),
                user.getFullName(),
                user.getEmail(),
                user.isEnabled() ? user.getRole() : user.getRole() + " • Đã khóa",
                "/admin/customers?keyword=" + keyword + "&highlight=" + user.getUserId(),
                score));
    }

    private void addOrderResult(List<AdminSearchItemResponse> results, Orders order, String query, String normalizedQuery) {
        double score = score(normalizedQuery, String.valueOf(order.getOrderId()), order.getFullName(), order.getShippingAddress());
        if (score <= 0) {
            return;
        }

        results.add(new AdminSearchItemResponse(
                "ORDER",
                String.valueOf(order.getOrderId()),
                "Đơn hàng #" + order.getOrderId(),
                order.getFullName(),
                order.getStatus(),
                "/admin/orders?highlight=" + order.getOrderId(),
                score));
    }

    private double score(String normalizedQuery, String... candidates) {
        double bestScore = 0D;
        for (String candidate : candidates) {
            String normalizedCandidate = normalize(candidate);
            if (normalizedCandidate.isBlank()) {
                continue;
            }

            if (normalizedCandidate.equals(normalizedQuery)) {
                bestScore = Math.max(bestScore, 120D);
                continue;
            }

            if (normalizedCandidate.contains(normalizedQuery)) {
                bestScore = Math.max(bestScore, 100D - normalizedCandidate.indexOf(normalizedQuery));
                continue;
            }

            String[] queryTokens = normalizedQuery.split("\\s+");
            boolean containsAllTokens = true;
            for (String token : queryTokens) {
                if (!normalizedCandidate.contains(token)) {
                    containsAllTokens = false;
                    break;
                }
            }
            if (containsAllTokens) {
                bestScore = Math.max(bestScore, 90D);
                continue;
            }

            int distance = levenshtein(normalizedQuery, normalizedCandidate);
            int threshold = normalizedQuery.length() <= 4 ? 1 : 2;
            if (distance <= threshold) {
                bestScore = Math.max(bestScore, 75D - (distance * 10D));
                continue;
            }

            for (String word : normalizedCandidate.split("\\s+")) {
                int wordDistance = levenshtein(normalizedQuery, word);
                if (wordDistance <= threshold) {
                    bestScore = Math.max(bestScore, 65D - (wordDistance * 10D));
                }
            }
        }
        return bestScore;
    }

    private String normalize(String input) {
        if (input == null) {
            return "";
        }

        String withoutAccents = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");

        return withoutAccents
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    private int levenshtein(String left, String right) {
        int[][] dp = new int[left.length() + 1][right.length() + 1];
        for (int i = 0; i <= left.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= right.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= left.length(); i++) {
            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost);
            }
        }

        return dp[left.length()][right.length()];
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
