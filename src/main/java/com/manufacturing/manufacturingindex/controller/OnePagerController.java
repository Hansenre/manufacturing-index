package com.manufacturing.manufacturingindex.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.service.OnePagerService;

@Controller
@RequestMapping("/one-pager")
public class OnePagerController {

    private final FactoryRepository factoryRepo;
    private final OnePagerService service;

    public OnePagerController(FactoryRepository factoryRepo,
                              OnePagerService service) {
        this.factoryRepo = factoryRepo;
        this.service = service;
    }

    /**
     * STEP 1
     * Factory selector (como estava antes)
     */
    @GetMapping
    public String selectFactory(Model model, Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "factories",
                factoryRepo.findByOwnerUsername(principal.getName())
        );

        return "one-pager/select";
    }

    /**
     * STEP 2
     * Management hub for selected factory
     */
    @GetMapping("/manage")
    public String manageFactory(
            @RequestParam Long factoryId,
            Model model) {

        var factory = factoryRepo.findById(factoryId).orElseThrow();

        model.addAttribute("factory", factory);

        return "one-pager/manage";
    }

    /**
     * FINAL ONE PAGER (executive view)
     */
    @GetMapping("/{factoryId}")
    public String view(@PathVariable Long factoryId, Model model) {

        model.addAttribute("factory",
                factoryRepo.findById(factoryId).orElseThrow());

        model.addAttribute("operationKpi",
                service.getOperationKpi(factoryId));

        model.addAttribute("hfpi",
                service.getHfpi(factoryId));

        model.addAttribute("summary",
                service.getSummary(factoryId));

        return "one-pager/view";
    }
}
