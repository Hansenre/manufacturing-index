package com.manufacturing.manufacturingindex.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.WasteRecord;
import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.repository.WasteRecordRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/waste")
public class WasteController {

    private final FactoryRepository factoryRepo;
    private final WasteRecordRepository wasteRepo;

    private final ObjectMapper om = new ObjectMapper();

    public WasteController(FactoryRepository factoryRepo, WasteRecordRepository wasteRepo) {
        this.factoryRepo = factoryRepo;
        this.wasteRepo = wasteRepo;
    }

    @GetMapping({"/{factoryId}", "/new/{factoryId}"})
    public String form(@PathVariable Long factoryId,
                       @RequestParam(required = false) String fy,
                       @RequestParam(required = false) String quarter,
                       @RequestParam(required = false) String monthRef,
                       Model model) throws Exception {

        Factory factory = factoryRepo.findById(factoryId).orElseThrow();

        if (fy == null || fy.isBlank()) fy = "FY26";
        if (quarter == null || quarter.isBlank()) quarter = "Q1";
        if (monthRef == null || monthRef.isBlank()) monthRef = "JUN";

        String fyNorm = fy.trim();
        String qNorm  = quarter.trim();
        String mNorm  = monthRef.trim().toUpperCase();

        model.addAttribute("factory", factory);
        model.addAttribute("fy", fyNorm);
        model.addAttribute("quarter", qNorm);
        model.addAttribute("monthRef", mNorm);

        model.addAttribute("fyList", List.of("FY24", "FY25", "FY26"));
        model.addAttribute("quarters", List.of("Q1", "Q2", "Q3", "Q4"));
        model.addAttribute("months", List.of("JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"));

        // ✅ Latest do mês (repo usa o atributo "month")
        WasteRecord latest = wasteRepo
                .findTopByFactoryIdAndFyAndQuarterAndMonthOrderByCreatedAtDesc(factoryId, fyNorm, qNorm, mNorm)
                .orElse(null);
        model.addAttribute("wasteLatest", latest);

        // ✅ Últimos 6 (gráfico)
        var page = PageRequest.of(0, 6);
        List<Object[]> last6 = wasteRepo.findLast6WasteByFyAndQuarter(factoryId, fyNorm, qNorm, page);
        if (last6 == null || last6.isEmpty()) {
            last6 = wasteRepo.findLast6WasteByFy(factoryId, fyNorm, page);
        }

        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        if (last6 != null) {
            for (Object[] row : last6) {
                String m = row[0] != null ? row[0].toString() : "";
                double v = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                labels.add(m);
                values.add(v);
            }
        }

        Collections.reverse(labels);
        Collections.reverse(values);

        model.addAttribute("wasteEmpty", labels.isEmpty());
        model.addAttribute("wasteLabelsJson", om.writeValueAsString(labels));
        model.addAttribute("wasteValuesJson", om.writeValueAsString(values));

        return "waste/form";
    }

    @PostMapping("/save/{factoryId}")
    public String save(@PathVariable Long factoryId,
                       @RequestParam String fy,
                       @RequestParam String quarter,
                       @RequestParam String monthRef,
                       @RequestParam Double wasteGrPairs) {

        Factory factory = factoryRepo.findById(factoryId).orElseThrow();

        String fyNorm = fy.trim();
        String qNorm  = quarter.trim();
        String mNorm  = monthRef.trim().toUpperCase();

        WasteRecord rec = new WasteRecord(factory, fyNorm, qNorm, mNorm, wasteGrPairs);
        wasteRepo.save(rec);

        return "redirect:/waste/" + factoryId + "?fy=" + fyNorm + "&quarter=" + qNorm + "&monthRef=" + mNorm;
    }

    @PostMapping("/{factoryId}/delete-latest")
    public String deleteLatest(@PathVariable Long factoryId,
                               @RequestParam String fy,
                               @RequestParam String quarter,
                               @RequestParam String monthRef) {

        String fyNorm = fy.trim();
        String qNorm  = quarter.trim();
        String mNorm  = monthRef.trim().toUpperCase();

        wasteRepo.findTopByFactoryIdAndFyAndQuarterAndMonthOrderByCreatedAtDesc(factoryId, fyNorm, qNorm, mNorm)
                .ifPresent(wasteRepo::delete);

        return "redirect:/waste/" + factoryId + "?fy=" + fyNorm + "&quarter=" + qNorm + "&monthRef=" + mNorm;
    }
}
