package com.manufacturing.manufacturingindex.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.repository.KpiRecordRepository;
import com.manufacturing.manufacturingindex.service.OnePagerService;

@Controller
@RequestMapping("/one-pager")
public class OnePagerController {

    private final FactoryRepository factoryRepo;
    private final KpiRecordRepository kpiRepo;
    private final OnePagerService service;

    public OnePagerController(FactoryRepository factoryRepo,
                              KpiRecordRepository kpiRepo,
                              OnePagerService service) {
        this.factoryRepo = factoryRepo;
        this.kpiRepo = kpiRepo;
        this.service = service;
    }

    @GetMapping
    public String selectFactory(Model model, Principal principal) {

        if (principal == null) return "redirect:/login";

        model.addAttribute("factories",
                factoryRepo.findByOwnerUsername(principal.getName()));

        return "one-pager/select";
    }

    @GetMapping("/manage")
    public String manageFactory(@RequestParam Long factoryId, Model model) {
        var factory = factoryRepo.findById(factoryId).orElseThrow();
        model.addAttribute("factory", factory);
        return "one-pager/manage";
    }

    @GetMapping("/{factoryId}")
    public String view(
            @PathVariable Long factoryId,
            @RequestParam(required = false) String fy,
            @RequestParam(required = false) String quarter,
            @RequestParam(required = false) String month,
            Model model) {

        var factory = factoryRepo.findById(factoryId).orElseThrow();

        if (fy == null || fy.isBlank()) fy = "FY25";
        if (quarter == null || quarter.isBlank()) quarter = "Q3";
        if (month == null) month = ""; // "" = All Months

        model.addAttribute("factory", factory);
        model.addAttribute("fy", fy);
        model.addAttribute("quarter", quarter);
        model.addAttribute("month", month);

        // ✅ OPERATION KPI respeitando o month (se vier)
        model.addAttribute(
                "operationKpi",
                service.getOperationKpi(factoryId, fy, quarter, month)
        );

        model.addAttribute("hfpi", service.getHfpi(factoryId, fy, quarter));
        model.addAttribute("summary", service.getSummary(factoryId, fy, quarter));
        model.addAttribute("voc", service.getVoc(factoryId, fy, quarter));
        model.addAttribute("drTopTypes", service.getDrTopTypes(factoryId, fy, quarter));

        model.addAttribute("fyList", List.of("FY24", "FY25", "FY26"));
        model.addAttribute("quarters", List.of("Q1", "Q2", "Q3", "Q4"));

        // ✅ lista de meses (tem que bater com o que você salva no monthRef)
        model.addAttribute("months", List.of("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"));

        return "one-pager/view";
    }

}
