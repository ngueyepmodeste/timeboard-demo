package com.example.timeboard.controller;

import com.example.timeboard.TimesheetEntry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "TimeBoard – Démo gestion des temps");
        model.addAttribute("user", "Modeste (groupe6)");
        model.addAttribute("day", LocalDate.now().toString());
        model.addAttribute("totalHours", 8);

        List<TimesheetEntry> entries = List.of(
                new TimesheetEntry("Lundi", "Projet e-Temptation", 8),
                new TimesheetEntry("Mardi", "Support interne", 7),
                new TimesheetEntry("Mercredi", "CI/CD & DevSecOps", 9)
        );
        model.addAttribute("entries", entries);

        return "index";
    }
}
