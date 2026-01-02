package com.manufacturing.manufacturingindex.controller;

import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.VocRecord;
import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.repository.VocRecordRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/voc")
public class VocController {

    private final VocRecordRepository vocRepo;
    private final FactoryRepository factoryRepo;

    public VocController(VocRecordRepository vocRepo,
                         FactoryRepository factoryRepo) {
        this.vocRepo = vocRepo;
        this.factoryRepo = factoryRepo;
    }

    // =========================
    // PAGE
    // =========================
    @GetMapping("/{factoryId}")
    public String page(@PathVariable Long factoryId, Model model) {

        Factory factory = factoryRepo.findById(factoryId)
                .orElseThrow();

        model.addAttribute("factory", factory);
        model.addAttribute("voc", new VocRecord());
        model.addAttribute(
                "vocList",
                vocRepo.findByFactoryOrderByCreatedAtDesc(factory)
        );

        return "voc-form";
    }

    // =========================
    // SAVE
    // =========================
    @PostMapping("/save/{factoryId}")
    public String save(@PathVariable Long factoryId,
                       @ModelAttribute VocRecord voc,
                       RedirectAttributes ra) {

        Factory factory = factoryRepo.findById(factoryId)
                .orElseThrow();

        voc.setFactory(factory);

        boolean exists = vocRepo.existsByFactoryAndFyAndQuarterAndMonthRef(
                factory,
                voc.getFy(),
                voc.getQuarter(),
                voc.getMonthRef()
        );

        if (exists) {
            ra.addFlashAttribute(
                "error",
                "VOC already exists for this period"
            );
            return "redirect:/voc/" + factoryId;
        }

        vocRepo.save(voc);

        ra.addFlashAttribute(
            "success",
            "VOC saved successfully"
        );

        return "redirect:/voc/" + factoryId;
    }
}
