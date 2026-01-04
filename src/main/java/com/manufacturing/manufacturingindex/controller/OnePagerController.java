package com.manufacturing.manufacturingindex.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manufacturing.manufacturingindex.model.KpiQualityEvidence.MetricKey;
import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.repository.HfpiEventRepository;
import com.manufacturing.manufacturingindex.repository.HfpiFactoryRepository;
import com.manufacturing.manufacturingindex.repository.KpiRecordRepository;
import com.manufacturing.manufacturingindex.repository.WasteRecordRepository;
import com.manufacturing.manufacturingindex.service.KpiQualityEvidenceService;
import com.manufacturing.manufacturingindex.service.OnePagerService;

@Controller
@RequestMapping("/one-pager")
public class OnePagerController {

    private final FactoryRepository factoryRepo;
    private final KpiRecordRepository kpiRepo;
    private final HfpiFactoryRepository hfpiFactoryRepo;
    private final HfpiEventRepository hfpiEventRepo;
    private final WasteRecordRepository wasteRepo;

    private final OnePagerService service;
    private final KpiQualityEvidenceService qualityEvidenceService;

    private final ObjectMapper om = new ObjectMapper();

    public OnePagerController(FactoryRepository factoryRepo,
                              KpiRecordRepository kpiRepo,
                              HfpiFactoryRepository hfpiFactoryRepo,
                              HfpiEventRepository hfpiEventRepo,
                              WasteRecordRepository wasteRepo,
                              OnePagerService service,
                              KpiQualityEvidenceService qualityEvidenceService) {
        this.factoryRepo = factoryRepo;
        this.kpiRepo = kpiRepo;
        this.hfpiFactoryRepo = hfpiFactoryRepo;
        this.hfpiEventRepo = hfpiEventRepo;
        this.wasteRepo = wasteRepo;
        this.service = service;
        this.qualityEvidenceService = qualityEvidenceService;
    }

    @GetMapping
    public String selectFactory(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        model.addAttribute("factories",
                factoryRepo.findByOwnerUsername(principal.getName()));

        return "one-pager/select";
    }

    @GetMapping("/manage")
    public String manageFactory(@RequestParam Long factoryId,
                                @RequestParam(required = false) String fy,
                                @RequestParam(required = false) String quarter,
                                @RequestParam(required = false) String month,
                                Model model) {

        var factory = factoryRepo.findById(factoryId).orElseThrow();

        if (fy == null || fy.isBlank()) fy = "FY26";
        if (quarter == null || quarter.isBlank()) quarter = "Q1";

        String monthRef = normalizeMonthRef(month);
        if (monthRef.isBlank()) monthRef = "JAN";

        model.addAttribute("factory", factory);
        model.addAttribute("fy", fy);
        model.addAttribute("quarter", quarter);
        model.addAttribute("month", monthRef);

        model.addAttribute("fyList", List.of("FY24", "FY25", "FY26", "FY27", "FY28"));
        model.addAttribute("quarters", List.of("Q1", "Q2", "Q3", "Q4"));
        model.addAttribute("months", List.of("JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"));

        return "one-pager/manage";
    }

    @GetMapping("/{factoryId}")
    public String view(@PathVariable Long factoryId,
                       @RequestParam(required = false) String fy,
                       @RequestParam(required = false) String quarter,
                       @RequestParam(required = false) String month,
                       Model model) throws Exception {

        var factory = factoryRepo.findById(factoryId).orElseThrow();

        if (fy == null || fy.isBlank()) fy = "FY26";
        if (quarter == null || quarter.isBlank()) quarter = "Q1";

        // monthRef: "" (All) ou "JAN"..."DEC"
        String monthRef = normalizeMonthRef(month);
        String monthUi  = monthRef.isBlank() ? null : monthRef;

        model.addAttribute("factory", factory);
        model.addAttribute("fy", fy);
        model.addAttribute("quarter", quarter);
        model.addAttribute("month", monthUi);

        // =========================
        // Mantém o que já funciona (quarter selecionado)
        // =========================
        model.addAttribute("operationKpi", service.getOperationKpi(factoryId, fy, quarter, monthRef));
        model.addAttribute("hfpi", service.getHfpi(factoryId, fy, quarter));
        model.addAttribute("summary", service.getSummary(factoryId, fy, quarter));
        model.addAttribute("voc", service.getVoc(factoryId, fy, quarter));
        model.addAttribute("drTopTypes", service.getDrTopTypes(factoryId, fy, quarter));

        // =========================
        // Evidence (MPPA/MQAA/BTP) – só busca se month selecionado
        // =========================
        if (!monthRef.isBlank()) {
            Integer monthNumber = monthRefToNumber(monthRef);

            model.addAttribute("mppaEv",
                    qualityEvidenceService.findOne(factoryId, fy, quarter, monthNumber, MetricKey.MPPA).orElse(null));
            model.addAttribute("mqaaEv",
                    qualityEvidenceService.findOne(factoryId, fy, quarter, monthNumber, MetricKey.MQAA).orElse(null));
            model.addAttribute("btpEv",
                    qualityEvidenceService.findOne(factoryId, fy, quarter, monthNumber, MetricKey.BTP).orElse(null));
        } else {
            model.addAttribute("mppaEv", null);
            model.addAttribute("mqaaEv", null);
            model.addAttribute("btpEv", null);
        }

        // =========================
        // ✅ HFPI – regra por mês:
        // - Se escolheu um mês: define hfpiQuarter pelo mês
        // - HFPI usa "ALL MONTHS" desse hfpiQuarter (não varia por mês)
        // =========================
        String hfpiQuarter = monthRef.isBlank()
                ? quarter
                : quarterForHfpiByMonth(monthRef, quarter);

        // ONLINE (%): HFPI EVENTS (ALL MONTHS do quarter HFPI)
        Object[] onlineRaw = hfpiEventRepo.getHfpiOnlinePercent(factoryId, fy, hfpiQuarter);
        double onlinePercent = extractPercentFromQueryResult(onlineRaw);

        // FACTORY (%): HFPI_FACTORY (ALL MONTHS do quarter HFPI) ✅ igual Online (não depende do mês)
        Object[] factoryRaw = hfpiFactoryRepo.getHfpiFactoryPercent(factoryId, fy, hfpiQuarter);
        double factoryPercent = extractPercentFromQueryResult(factoryRaw);

        // GENERAL (%): 60/40
        double generalPercent = (onlinePercent * 0.6) + (factoryPercent * 0.4);

        model.addAttribute("hfpiOnline",  formatPercentNumber(onlinePercent));
        model.addAttribute("hfpiFactory", formatPercentNumber(factoryPercent));
        model.addAttribute("hfpiGeneral", formatPercentNumber(generalPercent));

        // =========================
        // VOC – LAST SIX MONTHS
        // =========================
        List<Object[]> last6Voc = kpiRepo.findLast6VocFromVocRecord(factoryId, fy, quarter);
        if (last6Voc == null || last6Voc.isEmpty()) {
            last6Voc = kpiRepo.findLast6VocFromVocRecordByFy(factoryId, fy);
        }

        List<String> vocLabels = new ArrayList<>();
        List<Double> vocValues = new ArrayList<>();

        if (last6Voc != null) {
            for (Object[] row : last6Voc) {
                String m = row[0] != null ? row[0].toString() : "";
                double v = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                vocLabels.add(m);
                vocValues.add(v);
            }
        }

        Collections.reverse(vocLabels);
        Collections.reverse(vocValues);

        model.addAttribute("vocEmpty", vocLabels.isEmpty());
        model.addAttribute("vocLabelsJson", om.writeValueAsString(vocLabels));
        model.addAttribute("vocValuesJson", om.writeValueAsString(vocValues));

        // =========================
        // ✅ WASTE – LAST SIX MONTHS (igual VOC, em JSON)
        // =========================
        var pageW = PageRequest.of(0, 6);

        List<Object[]> last6Waste = wasteRepo.findLast6WasteByFyAndQuarter(factoryId, fy, quarter, pageW);
        if (last6Waste == null || last6Waste.isEmpty()) {
            last6Waste = wasteRepo.findLast6WasteByFy(factoryId, fy, pageW);
        }

        List<String> wasteLabels = new ArrayList<>();
        List<Double> wasteValues = new ArrayList<>();

        if (last6Waste != null) {
            for (Object[] row : last6Waste) {
                String m = row[0] != null ? row[0].toString() : "";
                double v = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                wasteLabels.add(m);
                wasteValues.add(v);
            }
        }

        Collections.reverse(wasteLabels);
        Collections.reverse(wasteValues);

        model.addAttribute("wasteEmpty", wasteLabels.isEmpty());
        model.addAttribute("wasteLabelsJson", om.writeValueAsString(wasteLabels));
        model.addAttribute("wasteValuesJson", om.writeValueAsString(wasteValues));

        // filtros
        model.addAttribute("fyList", List.of("FY24", "FY25", "FY26"));
        model.addAttribute("quarters", List.of("Q1", "Q2", "Q3", "Q4"));
        model.addAttribute("months", List.of("JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"));

        return "one-pager/view";
    }

    // =========================
    // Helpers
    // =========================

    private String normalizeMonthRef(String month) {
        if (month == null) return "";
        String m = month.trim().toUpperCase();
        if (m.isBlank()) return "";
        if (m.equals("ALL")) return "";
        return m;
    }

    private Integer monthRefToNumber(String monthRef) {
        if (monthRef == null || monthRef.isBlank()) return null;

        return switch (monthRef.toUpperCase()) {
            case "JAN" -> 1;
            case "FEB" -> 2;
            case "MAR" -> 3;
            case "APR" -> 4;
            case "MAY" -> 5;
            case "JUN" -> 6;
            case "JUL" -> 7;
            case "AUG" -> 8;
            case "SEP" -> 9;
            case "OCT" -> 10;
            case "NOV" -> 11;
            case "DEC" -> 12;
            default -> null;
        };
    }

    // ✅ Pega percent mesmo se vier [a,b,p] OU [[a,b,p]]
    private double extractPercentFromQueryResult(Object[] raw) {
        if (raw == null || raw.length == 0) return 0.0;

        if (raw[0] instanceof Object[]) {
            Object[] row = (Object[]) raw[0];
            if (row.length >= 3 && row[2] != null) return ((Number) row[2]).doubleValue();
            return 0.0;
        }

        if (raw.length >= 3 && raw[2] != null) return ((Number) raw[2]).doubleValue();
        return 0.0;
    }

    private String formatPercentNumber(double v) {
        return String.format(java.util.Locale.US, "%.2f%%", v);
    }

    // ✅ regra HFPI por mês
    private String quarterForHfpiByMonth(String monthRef, String fallbackQuarter) {
        if (monthRef == null) return fallbackQuarter;

        String m = monthRef.trim().toUpperCase();
        if (m.isBlank()) return fallbackQuarter;

        return switch (m) {
            case "JUN", "JUL", "AUG" -> "Q1";
            case "SEP", "OCT", "NOV" -> "Q2";
            case "DEC", "JAN", "FEB" -> "Q3";
            case "MAR", "APR", "MAY" -> "Q4";
            default -> fallbackQuarter;
        };
    }
}
