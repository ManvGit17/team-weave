package io.teamweave.domain.team;

import io.teamweave.domain.student.SkillRating;
import io.teamweave.domain.student.Student;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** A summary of the skills a team covers: the best level it has for each skill. */
public record SkillProfile(Map<String, Integer> bestLevels) {

    public static SkillProfile of(List<Student> students) {
        Map<String, Integer> best = new TreeMap<>();
        for (Student student : students) {
            for (SkillRating rating : student.getSkills()) {
                best.merge(rating.getSkill().toLowerCase(), rating.getLevel(), Math::max);
            }
        }
        return new SkillProfile(best);
    }

    /** How many of the required skills this team has at all. */
    public long coverage(Set<String> requiredSkills) {
        return requiredSkills.stream()
            .map(String::toLowerCase)
            .filter(bestLevels::containsKey)
            .count();
    }

    public int totalStrength() {
        return bestLevels.values().stream().mapToInt(Integer::intValue).sum();
    }
}
