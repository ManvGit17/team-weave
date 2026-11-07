package io.teamweave.web;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Shows where each design pattern lives, the test that proves it, and the
 * domain class diagram generated from the compiled code.
 */
@Controller
public class HowItsBuiltController {

    public record Source(String label, String path) {
    }

    public record Pattern(String name, String intent, String inTeamweave,
                          List<Source> code, Source test) {
    }

    private static final String MAIN = "src/main/java/io/teamweave/";
    private static final String TEST = "src/test/java/io/teamweave/";

    static final List<Pattern> PATTERNS = List.of(
        new Pattern("Strategy",
            "Define a family of interchangeable algorithms behind one interface.",
            "Three team-formation algorithms — greedy-balancer, preference-first and "
                + "skill-coverage — that the coordinator swaps and compares.",
            List.of(new Source("MatchingStrategy", MAIN + "domain/matching/MatchingStrategy.java"),
                    new Source("GreedyBalancerStrategy", MAIN + "domain/matching/GreedyBalancerStrategy.java"),
                    new Source("PreferenceFirstStrategy", MAIN + "domain/matching/PreferenceFirstStrategy.java"),
                    new Source("SkillCoverageStrategy", MAIN + "domain/matching/SkillCoverageStrategy.java")),
            new Source("MatchingStrategyTest", TEST + "domain/matching/MatchingStrategyTest.java")),
        new Pattern("Specification",
            "Express business rules as objects that combine with and, or and not.",
            "Constraints on an assignment — team size, required roles, keep together, "
                + "keep apart — composed into bigger rules and checked after every run.",
            List.of(new Source("Constraint", MAIN + "domain/constraint/Constraint.java"),
                    new Source("RequiredRoles", MAIN + "domain/constraint/RequiredRoles.java"),
                    new Source("KeepTogether", MAIN + "domain/constraint/KeepTogether.java"),
                    new Source("KeepApart", MAIN + "domain/constraint/KeepApart.java")),
            new Source("ConstraintTest", TEST + "domain/constraint/ConstraintTest.java")),
        new Pattern("State",
            "Let an object change its behaviour when its internal state changes.",
            "A team's lifecycle — forming, locked, active, submitted, reviewed. Each state "
                + "allows only its own transition, and only a forming team can change members.",
            List.of(new Source("TeamState", MAIN + "domain/team/TeamState.java"),
                    new Source("Team", MAIN + "domain/team/Team.java")),
            new Source("TeamStateTest", TEST + "domain/team/TeamStateTest.java")),
        new Pattern("Command",
            "Turn a request into an object, so it can be undone, redone and recorded.",
            "Every manual move on the board is a command: undo and redo walk the history.",
            List.of(new Source("Command", MAIN + "domain/command/Command.java"),
                    new Source("MoveStudentCommand", MAIN + "domain/command/MoveStudentCommand.java"),
                    new Source("CommandInvoker", MAIN + "domain/command/CommandInvoker.java")),
            new Source("CommandTest", TEST + "domain/command/CommandTest.java")),
        new Pattern("Observer",
            "Let objects subscribe to changes in another object.",
            "Teams announce every change; the board's activity feed is a listener.",
            List.of(new Source("Team", MAIN + "domain/team/Team.java"),
                    new Source("TeamChangeListener", MAIN + "domain/team/TeamChangeListener.java"),
                    new Source("ActivityFeed", MAIN + "application/ActivityFeed.java")),
            new Source("TeamObserverTest", TEST + "domain/team/TeamObserverTest.java")));

    private final String sourceUrl;

    public HowItsBuiltController(
            @Value("${teamweave.source-url:https://github.com/ManvGit17/team-weave/blob/main}")
            String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    @GetMapping("/how-its-built")
    public String page(Model model) {
        model.addAttribute("patterns", PATTERNS);
        model.addAttribute("sourceUrl", sourceUrl);
        model.addAttribute("diagram", diagram());
        model.addAttribute("architectureTest", TEST + "architecture/ArchitectureTest.java");
        model.addAttribute("diagramTest", TEST + "architecture/DomainDiagramTest.java");
        return "how-its-built";
    }

    /** The Mermaid source of the generated domain diagram, packaged with the app. */
    static String diagram() {
        ClassPathResource resource = new ClassPathResource("diagrams/domain-model.md");
        if (!resource.exists()) {
            return "";
        }
        try (InputStream in = resource.getInputStream()) {
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
            int start = text.indexOf("```mermaid\n");
            int end = text.indexOf("\n```", start + 1);
            return start < 0 || end < 0 ? "" : text.substring(start + "```mermaid\n".length(), end);
        } catch (IOException ex) {
            return "";
        }
    }
}
