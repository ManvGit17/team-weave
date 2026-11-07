package io.teamweave.config;

import io.teamweave.application.FormationService;
import io.teamweave.domain.formation.MatchResult;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * A rerunnable comparison of the strategies on the current students:
 *
 *   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,compare
 *
 * It prints a table and exits.
 */
@Component
@Profile("compare")
public class StrategyComparison implements CommandLineRunner {

    private final FormationService formation;
    private final ConfigurableApplicationContext context;

    public StrategyComparison(FormationService formation, ConfigurableApplicationContext context) {
        this.formation = formation;
        this.context = context;
    }

    @Override
    public void run(String... args) {
        int teamSize = 4;
        List<MatchResult> results = formation.compare(teamSize, List.of());

        System.out.printf("%nStrategy comparison (team size %d)%n", teamSize);
        System.out.printf("%-18s %8s %9s %8s %9s%n",
            "strategy", "overall", "coverage", "pref", "balance");
        System.out.println("-".repeat(56));
        for (MatchResult r : results) {
            System.out.printf("%-18s %8.3f %9.3f %8.3f %9.3f%n",
                r.strategy(), r.score().overall(), r.score().skillCoverage(),
                r.score().preferenceSatisfaction(), r.score().balance());
        }
        System.out.println();

        System.exit(SpringApplication.exit(context, () -> 0));
    }
}
