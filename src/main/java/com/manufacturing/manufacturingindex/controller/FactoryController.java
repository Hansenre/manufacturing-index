package com.manufacturing.manufacturingindex.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.User;
import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.repository.UserRepository;

@Controller
@RequestMapping("/factories")
public class FactoryController {

    private final FactoryRepository factoryRepository;
    private final UserRepository userRepository;

    public FactoryController(FactoryRepository factoryRepository,
                             UserRepository userRepository) {
        this.factoryRepository = factoryRepository;
        this.userRepository = userRepository;
    }

    // LISTAR FÁBRICAS DO USUÁRIO
    @GetMapping
    public String listFactories(Model model, Principal principal) {

        User user = userRepository
                .findByUsername(principal.getName())
                .orElseThrow();

        List<Factory> factories = factoryRepository.findByOwner(user);

        model.addAttribute("factories", factories);

        return "factories";
    }

    // FORMULÁRIO DE CADASTRO
    @GetMapping("/new")
    public String newFactoryForm(Model model) {
        model.addAttribute("factory", new Factory());
        return "factory-form";
    }

    // SALVAR FÁBRICA
    @PostMapping
    public String saveFactory(@ModelAttribute Factory factory,
                              Principal principal) {

        User user = userRepository
                .findByUsername(principal.getName())
                .orElseThrow();

        factory.setOwner(user);
        factoryRepository.save(factory);

        return "redirect:/factories";
    }
}
