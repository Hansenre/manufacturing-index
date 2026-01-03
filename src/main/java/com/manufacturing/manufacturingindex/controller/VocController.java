package com.manufacturing.manufacturingindex.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.User;
import com.manufacturing.manufacturingindex.model.VocRecord;
import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.repository.UserRepository;
import com.manufacturing.manufacturingindex.repository.VocRecordRepository;

@Controller
@RequestMapping("/voc")
public class VocController {

    private final FactoryRepository factoryRepo;
    private final UserRepository userRepo;
    private final VocRecordRepository vocRepo;

    public VocController(FactoryRepository factoryRepo,
                         UserRepository userRepo,
                         VocRecordRepository vocRepo) {
        this.factoryRepo = factoryRepo;
        this.userRepo = userRepo;
        this.vocRepo = vocRepo;
    }

    @GetMapping("/{factoryId}")
    public String page(@PathVariable Long factoryId,
                       Model model,
                       Principal principal) {

        Factory factory = validateOwnership(factoryId, principal);

        List<VocRecord> history = vocRepo.findByFactoryIdOrderByCreatedAtDesc(factoryId);
        Double latestVoc = vocRepo.findTopByFactoryIdOrderByCreatedAtDesc(factoryId)
                .map(VocRecord::getVoc)
                .orElse(null);

        model.addAttribute("factory", factory);
        model.addAttribute("history", history);
        model.addAttribute("latestVoc", latestVoc);

        // form padrão
        model.addAttribute("form", new VocRecord());
        model.addAttribute("isEdit", false);

        return "voc";
    }

    // SAVE / UPSERT (não quebra unique constraint)
    @PostMapping("/save/{factoryId}")
    public String save(@PathVariable Long factoryId,
                       @RequestParam String fy,
                       @RequestParam String quarter,
                       @RequestParam String monthRef,
                       @RequestParam Double voc,
                       Principal principal,
                       RedirectAttributes ra) {

        Factory factory = validateOwnership(factoryId, principal);

        try {
            // upsert pela chave única (factory+fy+quarter+month)
            VocRecord record = vocRepo.findByFactoryIdAndFyAndQuarterAndMonthRef(factoryId, fy, quarter, monthRef)
                    .orElseGet(VocRecord::new);

            record.setFactory(factory);
            record.setFy(fy);
            record.setQuarter(quarter);
            record.setMonthRef(monthRef);
            record.setVoc(voc);

            vocRepo.save(record);

            ra.addFlashAttribute("success", "VOC saved.");
            return "redirect:/voc/" + factoryId;

        } catch (DataIntegrityViolationException ex) {
            // só por segurança
            ra.addFlashAttribute("error", "VOC already exists for this FY/Quarter/Month.");
            return "redirect:/voc/" + factoryId;
        }
    }

    // EDIT FORM
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           Model model,
                           Principal principal) {

        VocRecord record = vocRepo.findById(id).orElseThrow();
        Long factoryId = record.getFactory().getId();

        validateOwnership(factoryId, principal);

        List<VocRecord> history = vocRepo.findByFactoryIdOrderByCreatedAtDesc(factoryId);
        Double latestVoc = vocRepo.findTopByFactoryIdOrderByCreatedAtDesc(factoryId)
                .map(VocRecord::getVoc)
                .orElse(null);

        model.addAttribute("factory", record.getFactory());
        model.addAttribute("history", history);
        model.addAttribute("latestVoc", latestVoc);

        model.addAttribute("form", record);
        model.addAttribute("isEdit", true);

        return "voc";
    }

    // EDIT SAVE
    @PostMapping("/{id}/edit")
    public String editSave(@PathVariable Long id,
                           @RequestParam String fy,
                           @RequestParam String quarter,
                           @RequestParam String monthRef,
                           @RequestParam Double voc,
                           Principal principal,
                           RedirectAttributes ra) {

        VocRecord record = vocRepo.findById(id).orElseThrow();
        Long factoryId = record.getFactory().getId();

        validateOwnership(factoryId, principal);

        record.setFy(fy);
        record.setQuarter(quarter);
        record.setMonthRef(monthRef);
        record.setVoc(voc);

        vocRepo.save(record);

        ra.addFlashAttribute("success", "VOC updated.");
        return "redirect:/voc/" + factoryId;
    }

    // DELETE
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         Principal principal) {

        VocRecord record = vocRepo.findById(id).orElseThrow();
        Long factoryId = record.getFactory().getId();

        validateOwnership(factoryId, principal);

        vocRepo.delete(record);
        return "redirect:/voc/" + factoryId;
    }

    private Factory validateOwnership(Long factoryId, Principal principal) {
        if (principal == null) throw new RuntimeException("User not authenticated");

        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        Factory factory = factoryRepo.findById(factoryId).orElseThrow();

        if (!factory.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        return factory;
    }
}
