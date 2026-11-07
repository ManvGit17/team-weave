package io.teamweave.domain.formation;

/** How good an assignment is, on three axes plus an overall, each in [0, 1]. */
public record Score(double skillCoverage, double preferenceSatisfaction,
                    double balance, double overall) {

    public static Score of(double skillCoverage, double preferenceSatisfaction, double balance) {
        double overall = (skillCoverage + preferenceSatisfaction + balance) / 3.0;
        return new Score(round(skillCoverage), round(preferenceSatisfaction),
                         round(balance), round(overall));
    }

    private static double round(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }
}
