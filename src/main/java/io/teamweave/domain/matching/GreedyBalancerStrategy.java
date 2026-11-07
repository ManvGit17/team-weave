package io.teamweave.domain.matching;

import io.teamweave.domain.formation.Explanation;
import io.teamweave.domain.formation.Round;
import io.teamweave.domain.student.SkillRating;
import io.teamweave.domain.student.Student;
import io.teamweave.domain.team.Team;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Spreads skill strength evenly: the strongest students are dealt across teams
 * in a snake order, so no team hoards the talent.
 */
public class GreedyBalancerStrategy extends AbstractMatchingStrategy {

    @Override
    public String name() {
        return "greedy-balancer";
    }

    @Override
    protected List<Team> placeStudents(Round round, List<Explanation> explanations) {
        List<Team> teams = emptyTeams(round);
        List<Student> ordered = new ArrayList<>(round.candidates());
        ordered.sort(Comparator.comparingInt(GreedyBalancerStrategy::strength).reversed());

        int n = teams.size();
        for (int i = 0; i < ordered.size(); i++) {
            int row = i / n;
            int pos = i % n;
            int index = (row % 2 == 0) ? pos : (n - 1 - pos); // snake
            Team team = teams.get(index);
            Student student = ordered.get(i);
            team.addMember(student);
            explanations.add(new Explanation(student.getName(), team.name(),
                "dealt to balance skill strength (" + strength(student) + ")"));
        }
        return teams;
    }

    static int strength(Student s) {
        return s.getSkills().stream().mapToInt(SkillRating::getLevel).sum();
    }
}
