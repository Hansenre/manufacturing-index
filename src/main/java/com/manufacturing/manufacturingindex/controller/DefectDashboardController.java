package com.manufacturing.manufacturingindex.controller;

import com.manufacturing.manufacturingindex.repository.HfpiItemDefectRepository;
import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.model.Factory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class DefectDashboardController {

    private final HfpiItemDefectRepository defectRepository;
    private final FactoryRepository factoryRepository;

    public DefectDashboardController(HfpiItemDefectRepository defectRepository,
                                     FactoryRepository factoryRepository) {
        this.defectRepository = defectRepository;
        this.factoryRepository = factoryRepository;
    }

    @GetMapping("/hfpi/defects/{factoryId}")
    public String defectDashboard(@PathVariable Long factoryId, Model model) {

        Factory factory = factoryRepository.findById(factoryId).orElseThrow();

        List<Object[]> data = defectRepository.countDefectsBySeverity(factoryId);

        model.addAttribute("factory", factory);
        model.addAttribute("defectData", data);

        return "hfpi-defects-dashboard";
    }
}
