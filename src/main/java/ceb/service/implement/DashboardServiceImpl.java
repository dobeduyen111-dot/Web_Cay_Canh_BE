package ceb.service.implement;

import org.springframework.stereotype.Service;

import ceb.domain.res.DashboardResponse;
import ceb.repository.DashboardRepository;
import ceb.service.service.DashboardService;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardServiceImpl(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Override
    public DashboardResponse getDashboard() {
        return new DashboardResponse(
                dashboardRepository.getTotalOrders(),
                dashboardRepository.getTotalRevenue(),
                dashboardRepository.getTotalCustomers());
    }
}
