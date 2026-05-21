package ceb.service.service;

import java.time.LocalDate;

import ceb.domain.res.DashboardResponse;

public interface DashboardService {

    default DashboardResponse getDashboard() {
        return getDashboard(null, null, 5);
    }

    DashboardResponse getDashboard(LocalDate fromDate, LocalDate toDate, int recentLimit);
}
