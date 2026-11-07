package io.teamweave.web;

import io.teamweave.application.FormationService;
import io.teamweave.application.FormationSession;
import io.teamweave.application.StudentService;
import io.teamweave.domain.constraint.Constraint;
import io.teamweave.domain.constraint.KeepApart;
import io.teamweave.domain.constraint.KeepTogether;
import io.teamweave.domain.constraint.RequiredRoles;
import io.teamweave.domain.formation.Round;
import io.teamweave.domain.student.Role;
import io.teamweave.domain.team.IllegalTransitionException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** The formation board: run a strategy, then adjust the teams by hand. */
@Controller
@RequestMapping("/formation")
public class FormationController {

    private final FormationService formation;
    private final FormationSession board;
    private final StudentService students;

    public FormationController(FormationService formation, FormationSession board,
                               StudentService students) {
        this.formation = formation;
        this.board = board;
        this.students = students;
    }

    @GetMapping
    public String board(Model model) {
        model.addAttribute("strategies", formation.strategyNames());
        model.addAttribute("roles", Role.values());
        model.addAttribute("students", students.getAllStudents());
        model.addAttribute("board", board);
        model.addAttribute("score", board.score().orElse(null));
        return "formation";
    }

    @PostMapping("/run")
    public String run(@RequestParam(defaultValue = "4") int teamSize,
                      @RequestParam(defaultValue = "greedy-balancer") String strategy,
                      @RequestParam(required = false) List<Role> requiredRoles,
                      @RequestParam(required = false) Long togetherA,
                      @RequestParam(required = false) Long togetherB,
                      @RequestParam(required = false) Long apartA,
                      @RequestParam(required = false) Long apartB,
                      RedirectAttributes flash) {
        if (teamSize < 2 || teamSize > 12) {
            flash.addFlashAttribute("error", "Team size must be between 2 and 12.");
            return "redirect:/formation";
        }
        List<Constraint> constraints = new ArrayList<>();
        if (requiredRoles != null && !requiredRoles.isEmpty()) {
            constraints.add(new RequiredRoles(EnumSet.copyOf(requiredRoles)));
        }
        pair(togetherA, togetherB, true).ifPresent(constraints::add);
        pair(apartA, apartB, false).ifPresent(constraints::add);

        Round round = formation.round(teamSize, constraints);
        if (round.candidates().isEmpty()) {
            flash.addFlashAttribute("error", "Add some students first.");
            return "redirect:/formation";
        }
        board.start(round, formation.run(round, strategy));
        return "redirect:/formation";
    }

    @PostMapping("/move")
    public String move(@RequestParam long studentId, @RequestParam String toTeam,
                       RedirectAttributes flash) {
        return attempt(() -> board.move(studentId, toTeam), flash);
    }

    @PostMapping("/undo")
    public String undo(RedirectAttributes flash) {
        return attempt(board::undo, flash);
    }

    @PostMapping("/redo")
    public String redo(RedirectAttributes flash) {
        return attempt(board::redo, flash);
    }

    @PostMapping("/teams/{team}/{action}")
    public String transition(@PathVariable String team, @PathVariable String action,
                             RedirectAttributes flash) {
        return attempt(() -> board.transition(team, action), flash);
    }

    @PostMapping("/reset")
    public String reset() {
        board.clear();
        return "redirect:/formation";
    }

    private String attempt(Runnable action, RedirectAttributes flash) {
        try {
            action.run();
        } catch (IllegalTransitionException | IllegalArgumentException ex) {
            flash.addFlashAttribute("error", capitalise(ex.getMessage()) + ".");
        }
        return "redirect:/formation";
    }

    private java.util.Optional<Constraint> pair(Long a, Long b, boolean together) {
        if (a == null || b == null || a.equals(b)) {
            return java.util.Optional.empty();
        }
        var first = formation.student(a);
        var second = formation.student(b);
        if (first.isEmpty() || second.isEmpty()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(together
            ? new KeepTogether(first.get(), second.get())
            : new KeepApart(first.get(), second.get()));
    }

    private static String capitalise(String s) {
        return s == null || s.isEmpty() ? "Something went wrong"
            : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
