package com.manufacturing.manufacturingindex.controller;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.manufacturing.manufacturingindex.dto.OperationKpiDTO;
import com.manufacturing.manufacturingindex.dto.OperationKpiViewDTO;
import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.KpiRecord;
import com.manufacturing.manufacturingindex.model.User;
import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.repository.KpiRecordRepository;
import com.manufacturing.manufacturingindex.repository.UserRepository;

@Controller
@RequestMapping("/kpi/operation")
public class KpiController {

    private final FactoryRepository factoryRepo;
    private final UserRepository userRepo;
    private final KpiRecordRepository kpiRepo;

    public KpiController(FactoryRepository factoryRepo,
                         UserRepository userRepo,
                         KpiRecordRepository kpiRepo) {
        this.factoryRepo = factoryRepo;
        this.userRepo = userRepo;
        this.kpiRepo = kpiRepo;
    }

    /* ===============================
       FORM + LIST
    =============================== */
    @GetMapping("/new/{factoryId}")
    public String newOperationKpi(@PathVariable Long factoryId,
                                  @RequestParam(required = false) String fy,
                                  @RequestParam(required = false) String quarter,
                                  @RequestParam(required = false) String monthRef,
                                  Model model,
                                  Principal principal) {

        Factory factory = validateOwnership(factoryId, principal);

        // ✅ default do form (se não vier nada)
        if (fy == null) fy = "FY26";
        if (quarter == null) quarter = "Q1";

        OperationKpiDTO form = new OperationKpiDTO();
        form.setFy(fy);
        form.setQuarter(quarter);
        form.setMonthRef(normalizeMonthRef(monthRef)); // pode ser ALL, JUN, JUL...

        // ✅ BUSCA SOMENTE OPERATION (não mistura com MQAAS/BTP/DEFECT)
        List<KpiRecord> records = kpiRepo.findByFactoryId(factoryId).stream()
                .filter(r -> r.getType() != null && r.getType().equalsIgnoreCase("OPERATION"))
                .collect(Collectors.toList());

        // 🔹 CONSOLIDA (FY + Quarter + MonthRef)
        Map<String, OperationKpiViewDTO> map = new LinkedHashMap<>();

        for (KpiRecord r : records) {

            String mref = normalizeMonthRef(r.getMonthRef());
            String key = r.getFy() + "-" + r.getQuarter() + "-" + mref;

            OperationKpiViewDTO view = map.getOrDefault(key, new OperationKpiViewDTO());

            view.setFy(r.getFy());
            view.setQuarter(r.getQuarter());
            view.setMonthRef(mref);

            if (r.getMetric() == null) continue;

            switch (r.getMetric()) {
                case "PAIRS_PRODUCED" -> view.setPairsProduced(r.getKpiValue());
                case "WORKING_DAYS" -> view.setWorkingDays(r.getKpiValue());
                case "WORKFORCE_NIKE" -> view.setWorkforceNike(r.getKpiValue());
                case "PPH" -> view.setPph(r.getKpiValue());
                case "DR" -> view.setDr(r.getKpiValue());
            }

            map.put(key, view);
        }

        model.addAttribute("factory", factory);
        model.addAttribute("form", form);
        model.addAttribute("existingKpis", new ArrayList<>(map.values()));

        // flags (se quiser usar no HTML)
        model.addAttribute("editMode", false);

        return "kpi/operation-form";
    }

    /* ===============================
       EDIT (abre o mesmo form já preenchido)
       /kpi/operation/edit/{factoryId}?fy=FY26&quarter=Q1&monthRef=JUN
    =============================== */
    @GetMapping("/edit/{factoryId}")
    public String editOperationKpi(@PathVariable Long factoryId,
                                   @RequestParam String fy,
                                   @RequestParam String quarter,
                                   @RequestParam String monthRef,
                                   Model model,
                                   Principal principal) {

        Factory factory = validateOwnership(factoryId, principal);

        String mref = normalizeMonthRef(monthRef);

        // carrega registros OPERATION daquele FY/Q/Month
        List<KpiRecord> set = kpiRepo.findByFactoryId(factoryId).stream()
                .filter(r -> "OPERATION".equalsIgnoreCase(r.getType()))
                .filter(r -> safeEq(fy, r.getFy()))
                .filter(r -> safeEq(quarter, r.getQuarter()))
                .filter(r -> safeEq(mref, normalizeMonthRef(r.getMonthRef())))
                .collect(Collectors.toList());

        OperationKpiDTO form = new OperationKpiDTO();
        form.setFy(fy);
        form.setQuarter(quarter);
        form.setMonthRef(mref);

        for (KpiRecord r : set) {
            if (r.getMetric() == null) continue;
            switch (r.getMetric()) {
                case "PAIRS_PRODUCED" -> form.setPairsProduced(r.getKpiValue());
                case "WORKING_DAYS" -> form.setWorkingDays(r.getKpiValue());
                case "WORKFORCE_NIKE" -> form.setWorkforceNike(r.getKpiValue());
                case "PPH" -> form.setPph(r.getKpiValue());
                case "DR" -> form.setDr(r.getKpiValue());
            }
        }

        // lista para a tabela (mesmo método)
        List<KpiRecord> records = kpiRepo.findByFactoryId(factoryId).stream()
                .filter(r -> r.getType() != null && r.getType().equalsIgnoreCase("OPERATION"))
                .collect(Collectors.toList());

        Map<String, OperationKpiViewDTO> map = new LinkedHashMap<>();
        for (KpiRecord r : records) {
            String mm = normalizeMonthRef(r.getMonthRef());
            String key = r.getFy() + "-" + r.getQuarter() + "-" + mm;

            OperationKpiViewDTO view = map.getOrDefault(key, new OperationKpiViewDTO());
            view.setFy(r.getFy());
            view.setQuarter(r.getQuarter());
            view.setMonthRef(mm);

            if (r.getMetric() == null) continue;
            switch (r.getMetric()) {
                case "PAIRS_PRODUCED" -> view.setPairsProduced(r.getKpiValue());
                case "WORKING_DAYS" -> view.setWorkingDays(r.getKpiValue());
                case "WORKFORCE_NIKE" -> view.setWorkforceNike(r.getKpiValue());
                case "PPH" -> view.setPph(r.getKpiValue());
                case "DR" -> view.setDr(r.getKpiValue());
            }
            map.put(key, view);
        }

        model.addAttribute("factory", factory);
        model.addAttribute("form", form);
        model.addAttribute("existingKpis", new ArrayList<>(map.values()));
        model.addAttribute("editMode", true);

        // se teu HTML quiser trocar action do form
        model.addAttribute("editFy", fy);
        model.addAttribute("editQuarter", quarter);
        model.addAttribute("editMonthRef", mref);

        return "kpi/operation-form";
    }

    /* ===============================
       SAVE / UPDATE
       - Para não duplicar:
         se já existe FY+Q+MonthRef, apaga o conjunto e grava de novo
    =============================== */
    @PostMapping("/save/{factoryId}")
    public String saveOperationKpi(@PathVariable Long factoryId,
                                   @ModelAttribute("form") OperationKpiDTO form,
                                   Principal principal) {

        validateOwnership(factoryId, principal);

        Factory factory = factoryRepo.findById(factoryId).orElseThrow();

        // ✅ monthRef obrigatório
        String mref = normalizeMonthRef(form.getMonthRef());
        form.setMonthRef(mref);

        // ✅ remove conjunto existente (FY+Quarter+MonthRef) para evitar duplicar linhas
        deleteSetIfExists(factoryId, form.getFy(), form.getQuarter(), mref);

        // grava o conjunto
        save(factory, form, "PAIRS_PRODUCED", form.getPairsProduced());
        save(factory, form, "WORKING_DAYS", form.getWorkingDays());
        save(factory, form, "WORKFORCE_NIKE", form.getWorkforceNike());
        save(factory, form, "PPH", form.getPph());
        save(factory, form, "DR", form.getDr());

        return "redirect:/kpi/operation/new/" + factoryId;
    }

    /* ===============================
       DELETE (apaga o conjunto inteiro)
       /kpi/operation/delete/{factoryId}?fy=FY26&quarter=Q1&monthRef=JUN
    =============================== */
    @GetMapping("/delete/{factoryId}")
    public String deleteOperationKpi(@PathVariable Long factoryId,
                                     @RequestParam String fy,
                                     @RequestParam String quarter,
                                     @RequestParam String monthRef,
                                     Principal principal) {

        validateOwnership(factoryId, principal);

        deleteSetIfExists(factoryId, fy, quarter, normalizeMonthRef(monthRef));

        return "redirect:/kpi/operation/new/" + factoryId;
    }

    /* ===============================
       AUX SAVE
    =============================== */
    private void save(Factory factory,
                      OperationKpiDTO dto,
                      String metric,
                      Number value) {

        if (value == null) return;

        KpiRecord r = new KpiRecord();
        r.setFactory(factory);
        r.setType("OPERATION");
        r.setMetric(metric);
        r.setFy(dto.getFy());
        r.setQuarter(dto.getQuarter());

        // ✅ garante not-null
        r.setMonthRef(normalizeMonthRef(dto.getMonthRef()));

        r.setKpiValue(value.doubleValue());
        r.setPoints(0.0);

        kpiRepo.save(r);
    }

    private void deleteSetIfExists(Long factoryId, String fy, String quarter, String monthRef) {

        String mref = normalizeMonthRef(monthRef);

        List<KpiRecord> toDelete = kpiRepo.findByFactoryId(factoryId).stream()
                .filter(r -> "OPERATION".equalsIgnoreCase(r.getType()))
                .filter(r -> safeEq(fy, r.getFy()))
                .filter(r -> safeEq(quarter, r.getQuarter()))
                .filter(r -> safeEq(mref, normalizeMonthRef(r.getMonthRef())))
                .collect(Collectors.toList());

        if (!toDelete.isEmpty()) {
            kpiRepo.deleteAll(toDelete);
        }
    }

    private Factory validateOwnership(Long factoryId, Principal principal) {

        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }

        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        Factory factory = factoryRepo.findById(factoryId).orElseThrow();

        if (!factory.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return factory;
    }

    private String normalizeMonthRef(String monthRef) {
        if (monthRef == null) return "ALL";
        String m = monthRef.trim();
        if (m.isEmpty()) return "ALL";
        return m.toUpperCase();
    }

    private boolean safeEq(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.trim().equalsIgnoreCase(b.trim());
    }
}
