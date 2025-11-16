package com.example.timeboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "TimeBoard – Démo gestion des temps");
        model.addAttribute("user", "Modeste (groupe6)");
        model.addAttribute("day", LocalDate.now().toString());
        model.addAttribute("totalHours", 8);

        List<Map<String, Object>> entries = List.of(
                Map.of("day", "Lundi", "project", "Projet e-Temptation", "hours", 8),
                Map.of("day", "Mardi", "project", "Support interne", "hours", 7),
                Map.of("day", "Mercredi", "project", "CI/CD & DevSecOps", "hours", 9)
        );
        model.addAttribute("entries", entries);
        return "index";
    }
}
