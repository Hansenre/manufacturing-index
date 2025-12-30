package com.manufacturing.manufacturingindex.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.manufacturing.manufacturingindex.config.HfpiDashboardService;
import com.manufacturing.manufacturingindex.dto.HfpiDashboardDTO;

@Controller
@RequestMapping("/hfpi/dashboard")
public class HfpiDashboardController {

    private final HfpiDashboardService service;

    public HfpiDashboardController(HfpiDashboardService service) {
        this.service = service;
    }

    // =========================
    // JSON – usado pelo gráfico
    // =========================
    @GetMapping("/{factoryId}")
    @ResponseBody
    public HfpiDashboardDTO get(
            @PathVariable Long factoryId,
            @RequestParam(required = false) String fy,
            @RequestParam(required = false) String quarter) {

        return service.build(factoryId, fy, quarter);
    }

    // =========================
    // HTML – página final
    // =========================
    @GetMapping("/final/{factoryId}")
    public String finalDashboard(
            @PathVariable Long factoryId,
            org.springframework.ui.Model model) {

        model.addAttribute("factoryId", factoryId);
        return "hfpi-dashboard-final";
    }
}
