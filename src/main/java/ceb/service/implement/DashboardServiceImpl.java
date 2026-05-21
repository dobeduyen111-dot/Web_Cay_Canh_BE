package ceb.service.implement;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import ceb.domain.res.DashboardPointResponse;
import ceb.domain.res.DashboardResponse;
import ceb.domain.res.OrderResponse;
import ceb.repository.DashboardRepository;
import ceb.repository.OrdersRepository;
import ceb.service.service.DashboardService;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;
    private final OrdersRepository ordersRepository;

    public DashboardServiceImpl(DashboardRepository dashboardRepository, OrdersRepository ordersRepository) {
        this.dashboardRepository = dashboardRepository;
        this.ordersRepository = ordersRepository;
    }

    @Override
    public DashboardResponse getDashboard() {
        return new DashboardResponse(
                dashboardRepository.getTotalOrders(),
                dashboardRepository.getTotalRevenue(),
                dashboardRepository.getTotalCustomers(),
                dashboardRepository.getTotalProducts(),
                java.util.List.of(),
                java.util.List.of());
    }

    @Override
    public DashboardResponse getDashboard(LocalDate fromDate, LocalDate toDate, int recentLimit) {
        LocalDate safeFromDate = fromDate == null ? LocalDate.now() : fromDate;
        LocalDate safeToDate = toDate == null ? safeFromDate : toDate;
        if (safeToDate.isBefore(safeFromDate)) {
            LocalDate temp = safeFromDate;
            safeFromDate = safeToDate;
            safeToDate = temp;
        }

        Timestamp start = Timestamp.valueOf(safeFromDate.atStartOfDay());
        Timestamp end = Timestamp.valueOf(safeToDate.plusDays(1).atStartOfDay());

        Map<String, DashboardPointResponse> seriesByDate = new LinkedHashMap<>();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("dd/MM");

        for (LocalDate cursor = safeFromDate; !cursor.isAfter(safeToDate); cursor = cursor.plusDays(1)) {
            seriesByDate.put(
                    cursor.toString(),
                    new DashboardPointResponse(cursor.toString(), cursor.format(labelFormatter), 0D, 0));
        }

        dashboardRepository.getRevenueSeries(start, end).forEach(point -> {
            DashboardPointResponse existing = seriesByDate.get(point.getDate());
            if (existing != null) {
                existing.setRevenue(point.getRevenue());
                existing.setOrders(point.getOrders());
            }
        });

        return new DashboardResponse(
                dashboardRepository.getTotalOrders(start, end),
                dashboardRepository.getTotalRevenue(start, end),
                dashboardRepository.getTotalCustomers(start, end),
                dashboardRepository.getTotalProducts(),
                seriesByDate.values().stream().toList(),
                ordersRepository == null
                        ? java.util.List.of()
                        : ordersRepository.findRecentByDateRange(start, end, Math.max(recentLimit, 1)).stream().map(OrderResponse::from).toList());
    }
}
