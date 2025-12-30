package com.manufacturing.manufacturingindex.controller;

import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.HfpiEvent;
import com.manufacturing.manufacturingindex.model.HfpiItem;
import com.manufacturing.manufacturingindex.repository.DefectTypeRepository;
import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.repository.HfpiEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/hfpi")
public class HfpiController {

    private final HfpiEventRepository eventRepo;
    private final FactoryRepository factoryRepo;
    private final DefectTypeRepository defectTypeRepo;

    public HfpiController(HfpiEventRepository eventRepo,
                          FactoryRepository factoryRepo,
                          DefectTypeRepository defectTypeRepo) {

        this.eventRepo = eventRepo;
        this.factoryRepo = factoryRepo;
        this.defectTypeRepo = defectTypeRepo;
    }

    /* =========================
       FY / QUARTER
       ========================= */
    private String gerarFy(LocalDate date) {
        int ano = date.getYear();
        if (date.getMonthValue() >= 6) {
            return "FY" + String.valueOf(ano + 1).substring(2);
        }
        return "FY" + String.valueOf(ano).substring(2);
    }

    private String calcularQuarter(LocalDate date) {
        int mes = date.getMonthValue();
        if (mes >= 6 && mes <= 8) return "Q1";
        if (mes >= 9 && mes <= 11) return "Q2";
        if (mes == 12 || mes <= 2) return "Q3";
        return "Q4";
    }

    private String calcularSeverity(List<String> ratings) {
        if (ratings.contains("SEVERO")) return "SEVERO";
        if (ratings.contains("MODERADO")) return "MODERADO";
        if (ratings.contains("MENOR")) return "MENOR";
        return "BOM";
    }

    /* =========================
       LISTAR HFPI (POR FÁBRICA)
       ========================= */
    @GetMapping("/{factoryId}")
    public String list(@PathVariable Long factoryId, Model model) {

        Factory factory = factoryRepo.findById(factoryId).orElseThrow();

        List<HfpiEvent> events = eventRepo.findByFactory(factory);

        model.addAttribute("factory", factory);
        model.addAttribute("factoryId", factoryId);
        model.addAttribute("events", events);

        // 🔽 ESSENCIAL PARA O SELECT DE FÁBRICAS
        model.addAttribute("factories", factoryRepo.findAll());

        return "hfpi/list";
    }

    /* =========================
       NOVO EVENTO (FORM)
       ========================= */
    @GetMapping("/new/{factoryId}")
    public String newEvent(@PathVariable Long factoryId, Model model) {

        Factory factory = factoryRepo.findById(factoryId).orElseThrow();

        HfpiEvent event = new HfpiEvent();
        event.setEventDate(LocalDate.now());
        event.setEventName("HFPI - " + factory.getName());
        event.setFactory(factory);
        event.setItems(new ArrayList<>());

        model.addAttribute("hfpiEvent", event);
        model.addAttribute("factory", factory);
        model.addAttribute("factories", factoryRepo.findAll());
        model.addAttribute("defectTypes", defectTypeRepo.findAll());

        return "hfpi/form";
    }

    /* =========================
       SALVAR
       ========================= */
    @PostMapping("/save")
    @Transactional
    public String save(@ModelAttribute HfpiEvent event,
                       @RequestParam Long factoryId,
                       @RequestParam List<String> ratings,
                       @RequestParam(required = false) List<Long> defectTypeIds1,
                       @RequestParam(required = false) List<Long> defectTypeIds2,
                       @RequestParam(required = false) List<Long> defectTypeIds3) {

        Factory factory = factoryRepo.findById(factoryId).orElseThrow();
        event.setFactory(factory);

        if (event.getEventName() == null || event.getEventName().isBlank()) {
            event.setEventName("HFPI - " + factory.getName() + " - " + LocalDate.now());
        }

        List<HfpiItem> items = new ArrayList<>();

        for (int i = 0; i < ratings.size(); i++) {

            HfpiItem item = new HfpiItem();
            item.setEvent(event);
            item.setItemNumber(i + 1);
            item.setRating(ratings.get(i));

            if (!"BOM".equals(ratings.get(i))) {

                if (defectTypeIds1 != null && i < defectTypeIds1.size()) {
                    Long id = defectTypeIds1.get(i);
                    if (id != null)
                        item.setDefect1(defectTypeRepo.findById(id).orElse(null));
                }

                if (defectTypeIds2 != null && i < defectTypeIds2.size()) {
                    Long id = defectTypeIds2.get(i);
                    if (id != null)
                        item.setDefect2(defectTypeRepo.findById(id).orElse(null));
                }

                if (defectTypeIds3 != null && i < defectTypeIds3.size()) {
                    Long id = defectTypeIds3.get(i);
                    if (id != null)
                        item.setDefect3(defectTypeRepo.findById(id).orElse(null));
                }
            }

            items.add(item);
        }

        event.setItems(items);
        event.setSeverity(calcularSeverity(ratings));
        event.setStatus("OPEN");
        event.setFy(gerarFy(event.getEventDate()));
        event.setQuarter(calcularQuarter(event.getEventDate()));

        eventRepo.save(event);

        return "redirect:/hfpi/" + factoryId;
    }

    /* =========================
       EDITAR
       ========================= */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {

        HfpiEvent event = eventRepo.findById(id).orElseThrow();

        model.addAttribute("hfpiEvent", event);
        model.addAttribute("factory", event.getFactory());
        model.addAttribute("factories", factoryRepo.findAll());
        model.addAttribute("defectTypes", defectTypeRepo.findAll());

        return "hfpi/form";
    }

    /* =========================
       UPDATE
       ========================= */
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("hfpiEvent") HfpiEvent formEvent) {

        HfpiEvent existing = eventRepo.findById(id).orElseThrow();

        existing.setEventDate(formEvent.getEventDate());
        existing.setEventName(formEvent.getEventName());
        existing.setStatus(formEvent.getStatus());
        existing.setDescription(formEvent.getDescription());

        eventRepo.save(existing);

        return "redirect:/hfpi/" + existing.getFactory().getId();
    }

    /* =========================
       DELETE
       ========================= */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {

        HfpiEvent event = eventRepo.findById(id).orElseThrow();
        Long factoryId = event.getFactory().getId();

        eventRepo.delete(event);

        return "redirect:/hfpi/" + factoryId;
    }
}
