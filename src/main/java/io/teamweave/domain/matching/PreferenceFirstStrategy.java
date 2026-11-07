package io.teamweave.domain.matching;

import io.teamweave.domain.formation.Explanation;
import io.teamweave.domain.formation.Round;
import io.teamweave.domain.student.Student;
import io.teamweave.domain.team.Team;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Honours peer preferences in both directions: a student joins a team (with
 * room) that holds someone they want to work with, or someone who wants to work
 * with them. Otherwise they go to the emptiest team.
 */
public class PreferenceFirstStrategy extends AbstractMatchingStrategy {

    @Override
    public String name() {
        return "preference-first";
    }

    @Override
    protected List<Team> placeStudents(Round round, List<Explanation> explanations) {
        List<Team> teams = emptyTeams(round);
        int cap = round.targetTeamSize();
        Map<Long, Team> teamByStudentId = new HashMap<>();
        Map<Long, Set<Long>> wantedBy = reversePreferences(round.candidates());

        for (Student student : round.candidates()) {
            Team target = null;
            String reason = "placed in the emptiest team";

            for (Long peerId : student.getPreferredPeerIds()) {
                Team peerTeam = teamByStudentId.get(peerId);
                if (peerTeam != null && peerTeam.size() < cap) {
                    target = peerTeam;
                    reason = "placed with a peer they asked for";
                    break;
                }
            }
            if (target == null && student.getId() != null) {
                for (Long admirerId : wantedBy.getOrDefault(student.getId(), Set.of())) {
                    Team admirerTeam = teamByStudentId.get(admirerId);
                    if (admirerTeam != null && admirerTeam.size() < cap) {
                        target = admirerTeam;
                        reason = "placed with a peer who asked for them";
                        break;
                    }
                }
            }
            if (target == null) {
                target = emptiest(teams);
            }

            target.addMember(student);
            if (student.getId() != null) {
                teamByStudentId.put(student.getId(), target);
            }
            explanations.add(new Explanation(student.getName(), target.name(), reason));
        }
        return teams;
    }

    /** For each student id, the ids of the students who prefer them. */
    private static Map<Long, Set<Long>> reversePreferences(List<Student> candidates) {
        Map<Long, Set<Long>> wantedBy = new HashMap<>();
        for (Student s : candidates) {
            if (s.getId() == null) {
                continue;
            }
            for (Long peerId : s.getPreferredPeerIds()) {
                wantedBy.computeIfAbsent(peerId, k -> new HashSet<>()).add(s.getId());
            }
        }
        return wantedBy;
    }

    private static Team emptiest(List<Team> teams) {
        return teams.stream().min(Comparator.comparingInt(Team::size)).orElseThrow();
    }
}
