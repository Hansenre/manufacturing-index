package com.manufacturing.manufacturingindex.controller;

import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.HfpiFactory;
import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.repository.HfpiFactoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/hfpi-factory")
public class HfpiFactoryController {

    private final HfpiFactoryRepository hfpiFactoryRepo;
    private final FactoryRepository factoryRepo;

    public HfpiFactoryController(HfpiFactoryRepository hfpiFactoryRepo,
                                 FactoryRepository factoryRepo) {
        this.hfpiFactoryRepo = hfpiFactoryRepo;
        this.factoryRepo = factoryRepo;
    }

    // LISTA
    @GetMapping("/{factoryId}")
    public String list(@PathVariable Long factoryId,
                       @RequestParam(required = false) String fy,
                       @RequestParam(required = false) String quarter,
                       Model model) {

        Factory factory = factoryRepo.findById(factoryId)
                .orElseThrow(() -> new IllegalArgumentException("Factory not found: " + factoryId));

        List<HfpiFactory> rows = hfpiFactoryRepo.findByFactoryAndFyQuarter(factoryId, fy, quarter);

        List<String> fyOptions = hfpiFactoryRepo.listFy(factoryId);
        List<String> quarterOptions = hfpiFactoryRepo.listQuarter(factoryId, fy);

        model.addAttribute("factory", factory);
        model.addAttribute("rows", rows);

        model.addAttribute("fy", fy);
        model.addAttribute("quarter", quarter);
        model.addAttribute("fyOptions", fyOptions);
        model.addAttribute("quarterOptions", quarterOptions);

        return "hfpi-factory/list";
    }

    // NOVO
    @GetMapping("/new/{factoryId}")
    public String createForm(@PathVariable Long factoryId, Model model) {
        Factory factory = factoryRepo.findById(factoryId)
                .orElseThrow(() -> new IllegalArgumentException("Factory not found: " + factoryId));

        HfpiFactory form = new HfpiFactory();
        form.setFactory(factory);

        model.addAttribute("factory", factory);
        model.addAttribute("hfpiFactory", form);

        return "hfpi-factory/form";
    }

    // EDITAR
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        HfpiFactory row = hfpiFactoryRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("HFPI Factory not found: " + id));

        model.addAttribute("factory", row.getFactory());
        model.addAttribute("hfpiFactory", row);

        return "hfpi-factory/form";
    }

    // SALVAR (cria ou atualiza)
    @PostMapping("/save")
    @Transactional
    public String save(@ModelAttribute("hfpiFactory") HfpiFactory hfpiFactory,
                       RedirectAttributes ra) {

        // Validações simples
        if (hfpiFactory.getFactory() == null || hfpiFactory.getFactory().getId() == null) {
            throw new IllegalArgumentException("Factory is required");
        }
        if (isBlank(hfpiFactory.getFy()) || isBlank(hfpiFactory.getQuarter()) || isBlank(hfpiFactory.getMonth())) {
            ra.addFlashAttribute("error", "FY, Quarter and Month are required.");
            return "redirect:/hfpi-factory/" + hfpiFactory.getFactory().getId();
        }
        if (hfpiFactory.getHfpiAprovados() == null) hfpiFactory.setHfpiAprovados(0);
        if (hfpiFactory.getHfpiRealizado() == null) hfpiFactory.setHfpiRealizado(0);

        // Reanexa factory do banco (evita transient)
        Factory factory = factoryRepo.findById(hfpiFactory.getFactory().getId())
                .orElseThrow(() -> new IllegalArgumentException("Factory not found: " + hfpiFactory.getFactory().getId()));
        hfpiFactory.setFactory(factory);

        hfpiFactoryRepo.save(hfpiFactory);

        ra.addFlashAttribute("success", "Saved successfully.");
        return "redirect:/hfpi-factory/" + factory.getId();
    }

    // DELETAR
    @PostMapping("/delete/{id}")
    @Transactional
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        HfpiFactory row = hfpiFactoryRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("HFPI Factory not found: " + id));
        Long factoryId = row.getFactory().getId();

        hfpiFactoryRepo.delete(row);

        ra.addFlashAttribute("success", "Deleted successfully.");
        return "redirect:/hfpi-factory/" + factoryId;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
