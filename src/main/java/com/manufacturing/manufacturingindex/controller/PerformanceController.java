package com.manufacturing.manufacturingindex.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.KpiRecord;
import com.manufacturing.manufacturingindex.model.User;
import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.repository.KpiRecordRepository;
import com.manufacturing.manufacturingindex.repository.UserRepository;

@Controller
public class PerformanceController {

    private final FactoryRepository factoryRepo;
    private final KpiRecordRepository kpiRepo;
    private final UserRepository userRepo;

    public PerformanceController(FactoryRepository factoryRepo,
                                 KpiRecordRepository kpiRepo,
                                 UserRepository userRepo) {
        this.factoryRepo = factoryRepo;
        this.kpiRepo = kpiRepo;
        this.userRepo = userRepo;
    }
    
    @GetMapping("/performance")
    public String performanceSelect(Model model, Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        List<Factory> factories = factoryRepo.findByOwner(user);

        if (factories.isEmpty()) {
            return "redirect:/factories";
        }

        model.addAttribute("factories", factories);
        model.addAttribute("selectedFactory", factories.get(0));
        model.addAttribute("fy", "FY26");
        model.addAttribute("quarter", "Q2");

        return "performance-select";
    }



    @GetMapping("/performance/{factoryId}")
    public String performanceByQuarter(
            @PathVariable Long factoryId,
            @RequestParam(required = false) String fy,
            @RequestParam(required = false) String quarter,
            Model model,
            Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        // defaults
        if (fy == null) fy = "FY26";
        if (quarter == null) quarter = "Q2";

        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        Factory factory = factoryRepo.findById(factoryId).orElseThrow();

        if (!factory.getOwner().getId().equals(user.getId())) {
            return "redirect:/factories";
        }

        List<KpiRecord> kpis =
                kpiRepo.findByFactoryAndFyAndQuarter(factory, fy, quarter);

        double totalPoints = kpis.stream()
                .mapToDouble(KpiRecord::getPoints)
                .sum();

        String level;
        if (totalPoints >= 95) level = "GOLD";
        else if (totalPoints >= 85) level = "SILVER";
        else if (totalPoints >= 70) level = "BRONZE";
        else if (totalPoints >= 60) level = "YELLOW";
        else level = "RED";

        model.addAttribute("selectedFactory", factory);
        model.addAttribute("factories", factoryRepo.findByOwner(user));
        model.addAttribute("fy", fy);
        model.addAttribute("quarter", quarter);
        model.addAttribute("kpis", kpis);
        model.addAttribute("totalPoints", totalPoints);
        model.addAttribute("level", level);

        return "performance-quarter";
    }

}
