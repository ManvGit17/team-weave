package io.teamweave.web;

import io.teamweave.application.FormationService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Every strategy on the same round, side by side. */
@Controller
public class CompareController {

    private final FormationService formation;

    public CompareController(FormationService formation) {
        this.formation = formation;
    }

    @GetMapping("/compare")
    public String compare(@RequestParam(defaultValue = "4") int teamSize, Model model) {
        int size = Math.max(2, Math.min(12, teamSize));
        model.addAttribute("teamSize", size);
        model.addAttribute("results", formation.compare(size, List.of()));
        return "compare";
    }
}
