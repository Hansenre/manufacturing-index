package com.manufacturing.manufacturingindex.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.manufacturing.manufacturingindex.model.BestPractice;
import com.manufacturing.manufacturingindex.model.Btp;
import com.manufacturing.manufacturingindex.model.DefectReturns;
import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.KpiRecord;
import com.manufacturing.manufacturingindex.model.Mqaas;
import com.manufacturing.manufacturingindex.model.User;
import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.repository.KpiRecordRepository;
import com.manufacturing.manufacturingindex.repository.UserRepository;

@Controller
@RequestMapping("/kpis")
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

    // =====================================================
    // LISTAR KPIs DE UMA FÁBRICA
    // =====================================================
   
    @GetMapping("/{factoryId}")
    public String listKpis(@PathVariable Long factoryId,
                           Model model,
                           Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        Factory factory = factoryRepo.findById(factoryId).orElseThrow();

        if (factory.getOwner() == null ||
            !factory.getOwner().getId().equals(user.getId())) {
            return "redirect:/factories";
        }

        List<KpiRecord> kpis = kpiRepo.findByFactory(factory);

        model.addAttribute("factory", factory);
        model.addAttribute("kpis", kpis);

        return "kpis";
    }

    // =====================================================
    // FORMULÁRIOS DE CRIAÇÃO
    // =====================================================

    // ---- MQAAS ----
    @GetMapping("/new/mqaas/{factoryId}")
    public String newMqaasForm(@PathVariable Long factoryId,
                               Model model,
                               Principal principal) {

        validateOwnership(factoryId, principal);
        model.addAttribute("factoryId", factoryId);

        return "kpi-form-mqaas";
    }

    // ---- BTP ----
    @GetMapping("/new/btp/{factoryId}")
    public String newBtpForm(@PathVariable Long factoryId,
                             Model model,
                             Principal principal) {

        validateOwnership(factoryId, principal);
        model.addAttribute("factoryId", factoryId);

        return "kpi-form-btp";
    }

    // ---- DEFECT RETURNS ----
    @GetMapping("/new/defect/{factoryId}")
    public String newDefectForm(@PathVariable Long factoryId,
                                Model model,
                                Principal principal) {

        validateOwnership(factoryId, principal);
        model.addAttribute("factoryId", factoryId);

        return "kpi-form-defect";
    }

    // =====================================================
    // EDITAR KPI
    // =====================================================
    @GetMapping("/{id}/edit")
    public String editKpi(@PathVariable Long id,
                          Model model,
                          Principal principal) {

        KpiRecord kpi = kpiRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid KPI Id: " + id));

        validateOwnership(kpi.getFactory().getId(), principal);

        model.addAttribute("kpi", kpi);
        return "kpi-edit";
    }

    // =====================================================
    // ATUALIZAR KPI
    // =====================================================
    @PostMapping("/{id}/update")
    public String updateKpi(@PathVariable Long id,
                            @ModelAttribute KpiRecord kpi,
                            Principal principal) {

        KpiRecord existing = kpiRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid KPI Id: " + id));

        validateOwnership(existing.getFactory().getId(), principal);

        // garante que a factory não seja alterada
        kpi.setFactory(existing.getFactory());

        kpiRepo.save(kpi);

        return "redirect:/kpis/" + existing.getFactory().getId();
    }

    // =====================================================
    // DELETAR KPI
    // =====================================================
    @GetMapping("/{id}/delete")
    public String deleteKpi(@PathVariable Long id,
                            Principal principal) {

        KpiRecord kpi = kpiRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid KPI Id: " + id));

        validateOwnership(kpi.getFactory().getId(), principal);

        Long factoryId = kpi.getFactory().getId();
        kpiRepo.deleteById(id);

        return "redirect:/kpis/" + factoryId;
    }

    // =====================================================
    // SALVAR KPI (POST ÚNICO)
    // =====================================================
    @PostMapping
    public String saveKpi(@RequestParam Long factoryId,
                          @RequestParam String fy,
                          @RequestParam String quarter,
                          @RequestParam String type,
                          @RequestParam int a,
                          @RequestParam int b,
                          @RequestParam(required = false) Double price,
                          Principal principal) {

        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        Factory factory = factoryRepo.findById(factoryId).orElseThrow();

        if (!factory.getOwner().getId().equals(user.getId())) {
            return "redirect:/factories";
        }

        BestPractice kpi;

        switch (type) {

            case "MQAAS":
                kpi = new Mqaas(fy, quarter, factory.getName(), a, b);
                break;

            case "BTP":
                kpi = new Btp(fy, quarter, factory.getName(), a, b);
                break;

            case "DEFECT":
                if (price == null) {
                    throw new IllegalArgumentException(
                            "Wholesale price is required for Defect Returns");
                }
                kpi = new DefectReturns(fy, quarter, factory.getName(), a, b, price);
                break;

            default:
                throw new IllegalArgumentException("Invalid KPI type");
        }

        double points = kpi.calculateIndex();

        KpiRecord record = new KpiRecord(
                fy,
                quarter,
                type,
                kpi.getPorcentage(),
                points,
                factory
        );

        kpiRepo.save(record);

        return "redirect:/kpis/" + factoryId;
    }

    // =====================================================
    // SEGURANÇA AUXILIAR
    // =====================================================
    private void validateOwnership(Long factoryId, Principal principal) {

        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        Factory factory = factoryRepo.findById(factoryId).orElseThrow();

        if (!factory.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
    }
}
