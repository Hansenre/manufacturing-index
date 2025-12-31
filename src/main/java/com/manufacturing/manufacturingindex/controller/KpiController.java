package com.manufacturing.manufacturingindex.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.manufacturing.manufacturingindex.dto.OperationKpiDTO;
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
       FORM
    =============================== */
    @GetMapping("/new/{factoryId}")
    public String newOperationKpi(@PathVariable Long factoryId,
                                  Model model,
                                  Principal principal) {

        validateOwnership(factoryId, principal);

        Factory factory = factoryRepo.findById(factoryId).orElseThrow();

        model.addAttribute("factory", factory);
        model.addAttribute("form", new OperationKpiDTO()); // 🔥 OBRIGATÓRIO

        return "kpi/operation-form";
    }

    /* ===============================
       SAVE
    =============================== */
    @PostMapping("/save/{factoryId}")
    public String saveOperationKpi(@PathVariable Long factoryId,
                                   @ModelAttribute("form") OperationKpiDTO form,
                                   Principal principal) {

        validateOwnership(factoryId, principal);

        Factory factory = factoryRepo.findById(factoryId).orElseThrow();

        save(factory, form, "PAIRS_PRODUCED", form.getPairsProduced());
        save(factory, form, "WORKING_DAYS", form.getWorkingDays());
        save(factory, form, "WORKFORCE_NIKE", form.getWorkforceNike());
        save(factory, form, "PPH", form.getPph());
        save(factory, form, "DR", form.getDr());

        return "redirect:/one-pager/manage?factoryId=" + factoryId;
    }

    /* ===============================
       AUX
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
        r.setMonthRef(dto.getMonthRef());
        r.setKpiValue(value.doubleValue());
        r.setPoints(0.0);

        kpiRepo.save(r);
    }

    private void validateOwnership(Long factoryId, Principal principal) {

        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }

        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        Factory factory = factoryRepo.findById(factoryId).orElseThrow();

        if (!factory.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
    }
}
