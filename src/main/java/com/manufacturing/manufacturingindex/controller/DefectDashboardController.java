package com.manufacturing.manufacturingindex.controller;

import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.repository.HfpiEventRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;

@Controller
public class DefectDashboardController {

    private final FactoryRepository factoryRepository;
    private final HfpiEventRepository eventRepository;

    public DefectDashboardController(FactoryRepository factoryRepository,
                                     HfpiEventRepository eventRepository) {
        this.factoryRepository = factoryRepository;
        this.eventRepository = eventRepository;
    }

    @GetMapping("/hfpi/defects/{factoryId}")
    public String defectDashboard(@PathVariable Long factoryId, Model model) {

        Factory factory = factoryRepository.findById(factoryId).orElseThrow();

        /* =====================================================
           🍕 PIZZA – Defeitos por descrição
           ===================================================== */
        List<Object[]> rawPie = eventRepository.countDefectsByDescription(factoryId);

        List<String> pieLabels = new ArrayList<>();
        List<Long> pieValues = new ArrayList<>();

        for (Object[] row : rawPie) {
            pieLabels.add((String) row[0]);
            pieValues.add(((Number) row[1]).longValue());
        }

        /* =====================================================
           📊 BARRA EMPILHADA – Defeitos por severidade
           ===================================================== */
        List<Object[]> rawSeverity =
                eventRepository.countDefectsBySeverity(factoryId);

        // defect -> [MENOR, MODERADO, SEVERO]
        Map<String, long[]> map = new LinkedHashMap<>();

        for (Object[] row : rawSeverity) {
            String defect = (String) row[0];
            String severity = (String) row[1];
            long count = ((Number) row[2]).longValue();

            map.putIfAbsent(defect, new long[]{0, 0, 0});

            switch (severity.toUpperCase()) {
                case "MENOR" -> map.get(defect)[0] += count;
                case "MODERADO" -> map.get(defect)[1] += count;
                case "SEVERO" -> map.get(defect)[2] += count;
            }
        }

        List<String> stackedLabels = new ArrayList<>();
        List<Long> stackedMenor = new ArrayList<>();
        List<Long> stackedModerado = new ArrayList<>();
        List<Long> stackedSevero = new ArrayList<>();

        for (Map.Entry<String, long[]> e : map.entrySet()) {
            stackedLabels.add(e.getKey());
            stackedMenor.add(e.getValue()[0]);
            stackedModerado.add(e.getValue()[1]);
            stackedSevero.add(e.getValue()[2]);
        }

        /* =====================================================
           MODEL
           ===================================================== */
        model.addAttribute("factory", factory);

        // Pizza
        model.addAttribute("pieLabels", pieLabels);
        model.addAttribute("pieValues", pieValues);

        // Empilhado
        model.addAttribute("stackedLabels", stackedLabels);
        model.addAttribute("stackedMenor", stackedMenor);
        model.addAttribute("stackedModerado", stackedModerado);
        model.addAttribute("stackedSevero", stackedSevero);

        return "hfpi-defects-dashboard";
    }
}
