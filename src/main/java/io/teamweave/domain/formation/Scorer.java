package io.teamweave.domain.formation;

import io.teamweave.domain.student.Student;
import io.teamweave.domain.team.Team;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Scores an assignment on skill coverage, preference satisfaction and balance. */
public final class Scorer {

    public Score score(Round round, java.util.List<Team> teams) {
        return Score.of(skillCoverage(round, teams),
                        preferenceSatisfaction(teams),
                        balance(teams));
    }

    /** Average fraction of the round's distinct skills each team carries. */
    private double skillCoverage(Round round, java.util.List<Team> teams) {
        Set<String> allSkills = new HashSet<>();
        for (Student s : round.candidates()) {
            s.getSkills().forEach(r -> allSkills.add(r.getSkill().toLowerCase()));
        }
        if (allSkills.isEmpty() || teams.isEmpty()) {
            return 1.0;
        }
        double sum = 0;
        for (Team t : teams) {
            sum += (double) t.skillProfile().bestLevels().size() / allSkills.size();
        }
        return sum / teams.size();
    }

    /** Fraction of students with peer preferences who got a preferred teammate. */
    private double preferenceSatisfaction(java.util.List<Team> teams) {
        int withPrefs = 0;
        int satisfied = 0;
        for (Team t : teams) {
            Set<Long> ids = memberIds(t);
            for (Student s : t.students()) {
                if (s.getPreferredPeerIds().isEmpty()) {
                    continue;
                }
                withPrefs++;
                boolean ok = s.getPreferredPeerIds().stream()
                    .anyMatch(id -> ids.contains(id) && !id.equals(s.getId()));
                if (ok) {
                    satisfied++;
                }
            }
        }
        return withPrefs == 0 ? 1.0 : (double) satisfied / withPrefs;
    }

    /** 1 minus the coefficient of variation of teams' total skill strength. */
    private double balance(java.util.List<Team> teams) {
        if (teams.size() < 2) {
            return 1.0;
        }
        double[] strengths = teams.stream()
            .mapToDouble(t -> t.skillProfile().totalStrength())
            .toArray();
        double mean = Arrays.stream(strengths).average().orElse(0);
        if (mean == 0) {
            return 1.0;
        }
        double variance = Arrays.stream(strengths)
            .map(x -> (x - mean) * (x - mean))
            .average().orElse(0);
        double cv = Math.sqrt(variance) / mean;
        return Math.max(0.0, 1.0 - cv);
    }

    private static Set<Long> memberIds(Team t) {
        Set<Long> ids = new HashSet<>();
        for (Student s : t.students()) {
            if (s.getId() != null) {
                ids.add(s.getId());
            }
        }
        return ids;
    }
}
