package com.manufacturing.manufacturingindex.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.repository.HfpiEventRepository;
import com.manufacturing.manufacturingindex.repository.HfpiFactoryRepository;
import com.manufacturing.manufacturingindex.repository.KpiRecordRepository;
import com.manufacturing.manufacturingindex.service.OnePagerService;

@Controller
@RequestMapping("/one-pager")
public class OnePagerController {

    private final FactoryRepository factoryRepo;
    private final KpiRecordRepository kpiRepo;
    private final HfpiFactoryRepository hfpiFactoryRepo;
    private final HfpiEventRepository hfpiEventRepo;
    private final OnePagerService service;

    private final ObjectMapper om = new ObjectMapper();

    public OnePagerController(FactoryRepository factoryRepo,
                              KpiRecordRepository kpiRepo,
                              HfpiFactoryRepository hfpiFactoryRepo,
                              HfpiEventRepository hfpiEventRepo,
                              OnePagerService service) {
        this.factoryRepo = factoryRepo;
        this.kpiRepo = kpiRepo;
        this.hfpiFactoryRepo = hfpiFactoryRepo;
        this.hfpiEventRepo = hfpiEventRepo;
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
            Model model) throws Exception {

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

        // =========================
        // Mantém o que já funciona
        // =========================
        model.addAttribute("operationKpi", service.getOperationKpi(factoryId, fy, quarter, monthRef));
        model.addAttribute("hfpi", service.getHfpi(factoryId, fy, quarter));
        model.addAttribute("summary", service.getSummary(factoryId, fy, quarter));
        model.addAttribute("voc", service.getVoc(factoryId, fy, quarter));
        model.addAttribute("drTopTypes", service.getDrTopTypes(factoryId, fy, quarter));

        // =========================
        // ✅ HFPI (3 cards): Online / Factory / General (60/40)
        // =========================

        // --- ONLINE (%): vem do HFPI EVENTS
        Object[] onlineRaw = hfpiEventRepo.getHfpiOnlinePercent(factoryId, fy, quarter);
        double onlinePercent = extractPercentFromQueryResult(onlineRaw); // 0-100

        // --- FACTORY (%): vem do HFPI_FACTORY
        double factoryPercent = 0.0;

        if (monthRef.isBlank()) {
            Object[] factoryRaw = hfpiFactoryRepo.getHfpiFactoryPercent(factoryId, fy, quarter);
            factoryPercent = extractPercentFromQueryResult(factoryRaw); // 0-100
        } else {
            String monthDb = normalizeHfpiMonthForDb(monthRef);

            var opt = hfpiFactoryRepo.findTopByFactoryIdAndFyAndQuarterAndMonthOrderByIdDesc(
                    factoryId, fy, quarter, monthDb
            );

            if (opt.isPresent()) {
                var h = opt.get();
                double aprov = (h.getHfpiAprovados() == null) ? 0.0 : h.getHfpiAprovados();
                double real  = (h.getHfpiRealizado() == null) ? 0.0 : h.getHfpiRealizado();
                factoryPercent = (real <= 0.0) ? 0.0 : (aprov * 100.0 / real);
            }
        }

        // --- GENERAL (%): 60% online + 40% factory
        double generalPercent = (onlinePercent * 0.6) + (factoryPercent * 0.4);

        model.addAttribute("hfpiOnline",  formatPercentNumber(onlinePercent));
        model.addAttribute("hfpiFactory", formatPercentNumber(factoryPercent));
        model.addAttribute("hfpiGeneral", formatPercentNumber(generalPercent));

        // =========================
        // ✅ VOC – LAST SIX MONTHS (VOC_RECORD)
        // - tenta FY+Quarter
        // - se vazio: fallback por FY (funciona nos 4 quarters)
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

        // deixa do mais antigo -> mais novo
        Collections.reverse(vocLabels);
        Collections.reverse(vocValues);

        model.addAttribute("vocEmpty", vocLabels.isEmpty());
        model.addAttribute("vocLabelsJson", om.writeValueAsString(vocLabels));
        model.addAttribute("vocValuesJson", om.writeValueAsString(vocValues));

        // =========================
        // filtros (mantidos)
        // =========================
        model.addAttribute("fyList", List.of("FY24", "FY25", "FY26"));
        model.addAttribute("quarters", List.of("Q1", "Q2", "Q3", "Q4"));
        model.addAttribute("months", List.of(
                "JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"
        ));

        return "one-pager/view";
    }

    // ✅ Pega percent mesmo se vier [a,b,p] OU [[a,b,p]]
    private double extractPercentFromQueryResult(Object[] raw) {
        if (raw == null || raw.length == 0) return 0.0;

        // caso venha "embrulhado": [ [ aprov, real, percent ] ]
        if (raw[0] instanceof Object[]) {
            Object[] row = (Object[]) raw[0];
            if (row.length >= 3 && row[2] != null) return ((Number) row[2]).doubleValue();
            return 0.0;
        }

        // caso normal: [ aprov, real, percent ]
        if (raw.length >= 3 && raw[2] != null) return ((Number) raw[2]).doubleValue();
        return 0.0;
    }

    private String normalizeHfpiMonthForDb(String monthRef) {
        if (monthRef == null || monthRef.isBlank()) return "";

        return switch (monthRef.toUpperCase()) {
            case "JAN" -> "Janeiro";
            case "FEB" -> "Fevereiro";
            case "MAR" -> "Março";
            case "APR" -> "Abril";
            case "MAY" -> "Maio";
            case "JUN" -> "Junho";
            case "JUL" -> "Julho";
            case "AUG" -> "Agosto";
            case "SEP" -> "Setembro";
            case "OCT" -> "Outubro";
            case "NOV" -> "Novembro";
            case "DEC" -> "Dezembro";
            default -> monthRef;
        };
    }

    private String formatPercentNumber(double v) {
        return String.format(java.util.Locale.US, "%.1f%%", v);
    }
}
