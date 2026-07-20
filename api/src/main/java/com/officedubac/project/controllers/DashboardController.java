package com.officedubac.project.controllers;

import com.officedubac.project.dto.KpiSummaryDTO;
import com.officedubac.project.dto.RecapTableDTO;
import com.officedubac.project.dto.SerieChartDataDTO;
import com.officedubac.project.services.DashboardStatsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@CrossOrigin("*")
public class DashboardController {

    private final DashboardStatsService dashboardStatsService;

    public DashboardController(DashboardStatsService dashboardStatsService) {
        this.dashboardStatsService = dashboardStatsService;
    }

    @GetMapping("/summary")
    public KpiSummaryDTO getSummary(
            @RequestParam(required = false) String academie,
            @RequestParam(required = false) Integer session) {
        return dashboardStatsService.getKpiSummary(academie, session);
    }

    @GetMapping("/recap")
    public RecapTableDTO getRecap(
            @RequestParam(required = false) String academie,
            @RequestParam(required = false) Integer session) {
        return dashboardStatsService.getRecapTable(academie, session);
    }

    @GetMapping("/series-chart")
    public List<SerieChartDataDTO> getSerieChart(
            @RequestParam(required = false) String academie,
            @RequestParam(required = false) Integer session) {
        return dashboardStatsService.getSerieChartData(academie, session);
    }

    @GetMapping("/academies")
    public List<String> getAcademies() {
        return dashboardStatsService.getAcademiesDisponibles();
    }
}
