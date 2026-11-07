package io.teamweave.domain.matching;

import io.teamweave.domain.formation.Explanation;
import io.teamweave.domain.formation.Round;
import io.teamweave.domain.student.SkillRating;
import io.teamweave.domain.student.Student;
import io.teamweave.domain.team.Team;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds well-rounded teams: each student joins the team (with room) that gains
 * the most new skills from them.
 */
public class SkillCoverageStrategy extends AbstractMatchingStrategy {

    @Override
    public String name() {
        return "skill-coverage";
    }

    @Override
    protected List<Team> placeStudents(Round round, List<Explanation> explanations) {
        List<Team> teams = emptyTeams(round);
        int cap = round.targetTeamSize();

        for (Student student : round.candidates()) {
            Set<String> studentSkills = skillsOf(student);

            Team best = null;
            int bestGain = -1;
            for (Team team : teams) {
                if (team.size() >= cap) {
                    continue;
                }
                Set<String> have = team.skillProfile().bestLevels().keySet();
                int gain = (int) studentSkills.stream().filter(s -> !have.contains(s)).count();
                if (gain > bestGain) {
                    bestGain = gain;
                    best = team;
                }
            }
            if (best == null) { // every team full; overflow into the emptiest
                best = teams.stream().min(Comparator.comparingInt(Team::size)).orElseThrow();
                bestGain = 0;
            }

            best.addMember(student);
            explanations.add(new Explanation(student.getName(), best.name(),
                "adds " + bestGain + " new skill(s) to the team"));
        }
        return teams;
    }

    private static Set<String> skillsOf(Student s) {
        return s.getSkills().stream()
            .map(SkillRating::getSkill)
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
    }
}
