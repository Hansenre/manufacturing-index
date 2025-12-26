package com.manufacturing.manufacturingindex.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.User;
import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.repository.KpiRecordRepository;
import com.manufacturing.manufacturingindex.repository.UserRepository;

@Controller
public class DashboardController {

    private final UserRepository userRepo;
    private final FactoryRepository factoryRepo;
    private final KpiRecordRepository kpiRepo;

    public DashboardController(UserRepository userRepo,
                               FactoryRepository factoryRepo,
                               KpiRecordRepository kpiRepo) {
        this.userRepo = userRepo;
        this.factoryRepo = factoryRepo;
        this.kpiRepo = kpiRepo;
    }

    @GetMapping("/")
    public String dashboard(Model model, Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        List<Factory> factories = factoryRepo.findByOwner(user);

        model.addAttribute("username", user.getUsername());
        model.addAttribute("factories", factories);
        model.addAttribute("factoryCount", factories.size());

        Factory selectedFactory = null;

        if (!factories.isEmpty()) {
            selectedFactory = factories.get(0); // fábrica padrão
            model.addAttribute("firstFactoryId", selectedFactory.getId());
            model.addAttribute("totalKpis",
                    kpiRepo.countByFactoryId(selectedFactory.getId()));
        } else {
            model.addAttribute("firstFactoryId", null);
            model.addAttribute("totalKpis", 0);
        }

        model.addAttribute("selectedFactory", selectedFactory);

        return "dashboard";
    }

}
