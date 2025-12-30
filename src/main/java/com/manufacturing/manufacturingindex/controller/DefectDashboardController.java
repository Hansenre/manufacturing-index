package com.manufacturing.manufacturingindex.controller;

import com.manufacturing.manufacturingindex.dto.DefectCountDTO;
import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.repository.HfpiEventRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/hfpi/defects")
public class DefectDashboardController {

    private final FactoryRepository factoryRepository;
    private final HfpiEventRepository eventRepository;

    public DefectDashboardController(
            FactoryRepository factoryRepository,
            HfpiEventRepository eventRepository) {

        this.factoryRepository = factoryRepository;
        this.eventRepository = eventRepository;
    }

    /* =====================================================
       📄 PÁGINA DO DASHBOARD (COM SELETOR DE FÁBRICA)
       ===================================================== */
    @GetMapping("/{factoryId}")
    public String defectDashboard(
            @PathVariable Long factoryId,
            Model model) {

        Factory factory = factoryRepository
                .findById(factoryId)
                .orElseThrow();

        /* =====================================================
           🔽 NOVO — SELETOR DE FÁBRICAS
           ===================================================== */
        model.addAttribute("factory", factory);
        model.addAttribute("factoryId", factoryId);
        model.addAttribute("factories", factoryRepository.findAll());

        /* =====================================================
           🍕 PIZZA – Defeitos por descrição
           ===================================================== */
        List<Object[]> rawPie =
                eventRepository.countDefectsByDescription(factoryId);

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

        model.addAttribute("pieLabels", pieLabels);
        model.addAttribute("pieValues", pieValues);

        model.addAttribute("stackedLabels", stackedLabels);
        model.addAttribute("stackedMenor", stackedMenor);
        model.addAttribute("stackedModerado", stackedModerado);
        model.addAttribute("stackedSevero", stackedSevero);

        return "hfpi-defects-dashboard";
    }

    /* =====================================================
       🔹 ENDPOINT AJAX – Pareto por TIPO (FY + Quarter)
       ===================================================== */
    @GetMapping("/data")
    @ResponseBody
    public List<DefectCountDTO> defectData(
            @RequestParam Long factoryId,
            @RequestParam String fy,
            @RequestParam String quarter) {

        List<Object[]> raw =
                eventRepository.countDefectsParetoByType(factoryId, fy, quarter);

        Map<String, Long> consolidated = new LinkedHashMap<>();

        for (Object[] r : raw) {
            String defectName = (String) r[0];
            Long count = ((Number) r[1]).longValue();
            consolidated.merge(defectName, count, Long::sum);
        }

        List<DefectCountDTO> result = new ArrayList<>();

        for (Map.Entry<String, Long> e : consolidated.entrySet()) {
            result.add(new DefectCountDTO(e.getKey(), e.getValue()));
        }

        return result;
    }

    /* =====================================================
       🔽 FY DISPONÍVEIS
       ===================================================== */
    @GetMapping("/fys")
    @ResponseBody
    public List<String> getFYs() {
        return eventRepository.findDistinctFYs();
    }

    /* =====================================================
       🔽 QUARTERS POR FY
       ===================================================== */
    @GetMapping("/quarters")
    @ResponseBody
    public List<String> getQuartersByFY(
            @RequestParam String fy) {

        return eventRepository.findQuartersByFY(fy);
    }

    /* =====================================================
       📊 Pareto 80/20 – Defeitos por MODELO
       ===================================================== */
    @GetMapping("/pareto/model")
    @ResponseBody
    public List<DefectCountDTO> paretoByModel(
            @RequestParam Long factoryId,
            @RequestParam String fy,
            @RequestParam String quarter,
            @RequestParam String modelName) {

        List<Object[]> raw =
                eventRepository.countDefectsParetoByModel(
                        factoryId, fy, quarter, modelName
                );

        List<DefectCountDTO> result = new ArrayList<>();

        for (Object[] r : raw) {
            result.add(new DefectCountDTO(
                    (String) r[0],
                    ((Number) r[1]).longValue()
            ));
        }

        return result;
    }

    /* =====================================================
       👟 Defeitos por MODELO (AJAX)
       ===================================================== */
    @GetMapping("/data/models")
    @ResponseBody
    public List<DefectCountDTO> defectsByModel(
            @RequestParam Long factoryId,
            @RequestParam String fy,
            @RequestParam String quarter) {

        List<Object[]> raw =
                eventRepository.countDefectsByModelRaw(factoryId, fy, quarter);

        List<DefectCountDTO> result = new ArrayList<>();

        for (Object[] r : raw) {
            result.add(new DefectCountDTO(
                    (String) r[0],
                    ((Number) r[1]).longValue()
            ));
        }

        return result;
    }
}
