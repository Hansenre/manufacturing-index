package com.manufacturing.manufacturingindex.controller;

import com.manufacturing.manufacturingindex.config.HfpiDashboardService;
import com.manufacturing.manufacturingindex.dto.HfpiDashboardDTO;
import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.repository.HfpiEventRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/hfpi/dashboard")
public class HfpiDashboardController {

    private final HfpiDashboardService service;
    private final FactoryRepository factoryRepo;
    private final HfpiEventRepository eventRepo;

    public HfpiDashboardController(
            HfpiDashboardService service,
            FactoryRepository factoryRepo,
            HfpiEventRepository eventRepo) {

        this.service = service;
        this.factoryRepo = factoryRepo;
        this.eventRepo = eventRepo;
    }

    /* =====================================================
       JSON – usado pelo gráfico (NÃO MUDA)
       ===================================================== */
    @GetMapping("/{factoryId}")
    @ResponseBody
    public HfpiDashboardDTO get(
            @PathVariable Long factoryId,
            @RequestParam(required = false) String fy,
            @RequestParam(required = false) String quarter) {

        return service.build(factoryId, fy, quarter);
    }

    /* =====================================================
       HTML – página final (COM SELETOR DE FÁBRICA)
       ===================================================== */
    @GetMapping("/final/{factoryId}")
    public String finalDashboard(
            @PathVariable Long factoryId,
            @RequestParam(required = false) String fy,
            @RequestParam(required = false) String quarter,
            Model model) {

        Factory factory = factoryRepo.findById(factoryId)
                .orElseThrow(() -> new IllegalArgumentException("Factory not found"));

        // defaults (mantém dashboard sempre carregado)
        if (fy == null || fy.isBlank()) {
            fy = eventRepo.findDistinctFYs().stream().findFirst().orElse("FY26");
        }
        if (quarter == null || quarter.isBlank()) {
            quarter = eventRepo.findQuartersByFY(fy).stream().findFirst().orElse("Q1");
        }

        model.addAttribute("factory", factory);
        model.addAttribute("factoryId", factoryId);
        model.addAttribute("factories", factoryRepo.findAll());

        model.addAttribute("fy", fy);
        model.addAttribute("quarter", quarter);

        model.addAttribute("fyOptions", eventRepo.findDistinctFYs());
        model.addAttribute("quarterOptions", eventRepo.findQuartersByFY(fy));

        return "hfpi-dashboard-final";
    }
}
