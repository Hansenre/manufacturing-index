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

        if (fy == null || fy.isBlank()) fy = "FY26";
        if (quarter == null || quarter.isBlank()) quarter = "Q1";

        // month: "" = All Months
        String monthRef = (month == null) ? "" : month.trim().toUpperCase();
        if (monthRef.equals("ALL")) monthRef = "";

        model.addAttribute("factory", factory);
        model.addAttribute("fy", fy);
        model.addAttribute("quarter", quarter);
        model.addAttribute("month", monthRef);

        model.addAttribute("operationKpi", service.getOperationKpi(factoryId, fy, quarter, monthRef));

        model.addAttribute("hfpi", service.getHfpi(factoryId, fy, quarter));
        model.addAttribute("summary", service.getSummary(factoryId, fy, quarter));
        model.addAttribute("voc", service.getVoc(factoryId, fy, quarter));
        model.addAttribute("drTopTypes", service.getDrTopTypes(factoryId, fy, quarter));

        model.addAttribute("fyList", List.of("FY24", "FY25", "FY26"));
        model.addAttribute("quarters", List.of("Q1", "Q2", "Q3", "Q4"));

        // ✅ TEM que bater com o monthRef salvo no banco
        model.addAttribute("months", List.of(
                "JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"
        ));

        return "one-pager/view";
    }
}
