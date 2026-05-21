package ceb.domain.res;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private int totalOrders;
    private double totalRevenue;
    private int totalCustomers;
    private int totalProducts;
    private List<DashboardPointResponse> revenueSeries;
    private List<OrderResponse> recentOrders;

    public DashboardResponse(int totalOrders, double totalRevenue, int totalCustomers) {
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.totalCustomers = totalCustomers;
        this.totalProducts = 0;
        this.revenueSeries = List.of();
        this.recentOrders = List.of();
    }
}
