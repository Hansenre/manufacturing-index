package com.manufacturing.manufacturingindex.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.KpiRecord;
import com.manufacturing.manufacturingindex.model.User;
import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.repository.KpiRecordRepository;
import com.manufacturing.manufacturingindex.repository.UserRepository;

@Controller
@RequestMapping("/kpis")
public class KpisController {

    private final FactoryRepository factoryRepo;
    private final UserRepository userRepo;
    private final KpiRecordRepository kpiRepo;

    public KpisController(FactoryRepository factoryRepo,
                          UserRepository userRepo,
                          KpiRecordRepository kpiRepo) {
        this.factoryRepo = factoryRepo;
        this.userRepo = userRepo;
        this.kpiRepo = kpiRepo;
    }

    // =========================
    // VIEW KPIs (por fábrica) - SOMENTE MQAAS/BTP/DEFECT
    // =========================
    @GetMapping("/{factoryId}")
    public String viewKpis(@PathVariable Long factoryId,
                           Model model,
                           Principal principal) {

        Factory factory = validateOwnership(factoryId, principal);

        List<KpiRecord> kpis = kpiRepo.findByFactoryId(factoryId).stream()
                .filter(this::isQualityKpi) // remove OPERATION
                .collect(Collectors.toList());

        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        model.addAttribute("factories", factoryRepo.findByOwner(user));
        model.addAttribute("factory", factory);
        model.addAttribute("kpis", kpis);

        return "kpis"; // kpis.html
    }

    // =========================
    // FORMS
    // =========================
    @GetMapping("/new/mqaas/{factoryId}")
    public String newMqaas(@PathVariable Long factoryId,
                           Model model,
                           Principal principal) {
        validateOwnership(factoryId, principal);
        model.addAttribute("factoryId", factoryId);
        return "kpis-new-mqaas";
    }

    @GetMapping("/new/btp/{factoryId}")
    public String newBtp(@PathVariable Long factoryId,
                         Model model,
                         Principal principal) {
        validateOwnership(factoryId, principal);
        model.addAttribute("factoryId", factoryId);
        return "kpis-new-btp";
    }

    @GetMapping("/new/defect/{factoryId}")
    public String newDefect(@PathVariable Long factoryId,
                            Model model,
                            Principal principal) {
        validateOwnership(factoryId, principal);
        model.addAttribute("factoryId", factoryId);
        return "kpis-new-defect";
    }

    // =========================
    // SAVE (POST /kpis)
    // Form manda: factoryId, type, fy, quarter, a, b, (price opcional)
    // =========================
    @PostMapping
    public String save(@RequestParam Long factoryId,
                       @RequestParam String type,
                       @RequestParam String fy,
                       @RequestParam String quarter,
                       @RequestParam Double a,
                       @RequestParam Double b,
                       @RequestParam(required = false) Double price,
                       Principal principal,
                       RedirectAttributes ra) {

        Factory factory = validateOwnership(factoryId, principal);

        String t = type.trim().toUpperCase(); // MQAAS / BTP / DEFECT

        double value = calcValue(t, a, b, price);
        double points = calcPoints(t, value);

        KpiRecord r = new KpiRecord();
        r.setFactory(factory);
        r.setType(t);
        r.setMetric(t);
        r.setFy(fy);
        r.setQuarter(quarter);

        // ✅ monthRef no teu banco está NOT NULL -> nunca salvar null
        r.setMonthRef("ALL");

        r.setKpiValue(value);
        r.setPoints(points);

        kpiRepo.save(r);

        ra.addFlashAttribute("success", "KPI saved.");
        return "redirect:/kpis/" + factoryId;
    }

    // =========================
    // DELETE
    // =========================
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         Principal principal) {

        if (principal == null) return "redirect:/login";

        KpiRecord r = kpiRepo.findById(id).orElseThrow();
        Long factoryId = r.getFactory().getId();

        validateOwnership(factoryId, principal);

        kpiRepo.delete(r);
        return "redirect:/kpis/" + factoryId;
    }

    // =========================
    // EDIT (mantido)
    // =========================
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           Model model,
                           Principal principal) {

        if (principal == null) return "redirect:/login";

        KpiRecord r = kpiRepo.findById(id).orElseThrow();
        validateOwnership(r.getFactory().getId(), principal);

        model.addAttribute("kpi", r);
        return "kpis-edit";
    }

    @PostMapping("/{id}/edit")
    public String editSave(@PathVariable Long id,
                           @RequestParam String fy,
                           @RequestParam String quarter,
                           @RequestParam Double kpiValue,
                           @RequestParam Double points,
                           Principal principal) {

        if (principal == null) return "redirect:/login";

        KpiRecord r = kpiRepo.findById(id).orElseThrow();
        Long factoryId = r.getFactory().getId();

        validateOwnership(factoryId, principal);

        r.setFy(fy);
        r.setQuarter(quarter);
        r.setKpiValue(kpiValue);
        r.setPoints(points);

        // ✅ segurança: se já existir dado antigo sem monthRef
        if (r.getMonthRef() == null || r.getMonthRef().isBlank()) {
            r.setMonthRef("ALL");
        }

        kpiRepo.save(r);

        return "redirect:/kpis/" + factoryId;
    }

    // =========================
    // AUX
    // =========================
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

    private boolean isQualityKpi(KpiRecord k) {
        if (k == null || k.getType() == null) return false;
        String t = k.getType().trim().toUpperCase();
        return t.equals("MQAAS") || t.equals("BTP") || t.equals("DEFECT");
    }

    /**
     * Cálculos (conforme teu último ajuste):
     * - MQAAS: achieved / available
     * - BTP  : approved / tested
     * - DEFECT: (pairsReturned * wholesale) / pairsShipped  -> $ per pair
     *
     * OBS: a = total (available/tested/shipped)
     *      b = achieved/approved/returned
     */
    private double calcValue(String type, Double a, Double b, Double price) {
        if (a == null || a <= 0 || b == null) return 0.0;

        return switch (type) {
            case "MQAAS" -> (b / a) * 100.0; // %
            case "BTP" -> (b / a) * 100.0;   // %
            case "DEFECT" -> {
                if (price == null || price < 0) yield 0.0;
                yield (b * price) / a;        // $ per pair
            }
            default -> 0.0;
        };
    }

    /**
     * Pontuação por faixas (igual suas imagens):
     * - MQAAS: 40/35/30/20/10/0
     * - BTP:   40/30/20/10/0
     * - DEFECT ($/pair): 20/16/10/5/0
     */
    private double calcPoints(String type, double value) {
        return switch (type) {
            case "MQAAS" -> pointsMqaas(value);
            case "BTP" -> pointsBtp(value);
            case "DEFECT" -> pointsDefect(value);
            default -> 0.0;
        };
    }

    private double pointsMqaas(double percent) {
        if (percent >= 98.0) return 40;
        if (percent >= 96.5) return 35;
        if (percent >= 95.5) return 30;
        if (percent >= 93.0) return 20;
        if (percent >= 90.01) return 10;
        return 0;
    }

    private double pointsBtp(double percent) {
        if (percent >= 99.0) return 40;
        if (percent >= 97.0) return 30;
        if (percent >= 93.5) return 20;
        if (percent >= 91.0) return 10;
        return 0;
    }

    private double pointsDefect(double dollarsPerPair) {

        // ✅ usa o mesmo arredondamento que tu mostra na tela (2 casas)
        double v = round2(dollarsPerPair);

        // Tabela:
        // $0.00 to $0.03 per pair  -> 20
        // $0.04 to $0.08 per pair  -> 16
        // $0.09 to $0.20 per pair  -> 10
        // $0.21 to $0.49 per pair  -> 5
        // >= $0.50                 -> 0

        if (v <= 0.03) return 20;
        if (v <= 0.08) return 16;
        if (v <= 0.20) return 10;
        if (v <= 0.49) return 5;
        return 0;
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
