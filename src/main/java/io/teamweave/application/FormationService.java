package io.teamweave.application;

import io.teamweave.domain.constraint.Constraint;
import io.teamweave.domain.formation.MatchResult;
import io.teamweave.domain.formation.Round;
import io.teamweave.domain.matching.GreedyBalancerStrategy;
import io.teamweave.domain.matching.MatchingStrategy;
import io.teamweave.domain.matching.PreferenceFirstStrategy;
import io.teamweave.domain.matching.SkillCoverageStrategy;
import io.teamweave.domain.student.Student;
import io.teamweave.infrastructure.StudentRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Runs and compares the matching strategies over the current students. */
@Service
public class FormationService {

    private final StudentRepository students;
    private final Map<String, MatchingStrategy> strategies = new LinkedHashMap<>();

    public FormationService(StudentRepository students) {
        this.students = students;
        register(new GreedyBalancerStrategy());
        register(new PreferenceFirstStrategy());
        register(new SkillCoverageStrategy());
    }

    private void register(MatchingStrategy strategy) {
        strategies.put(strategy.name(), strategy);
    }

    public List<String> strategyNames() {
        return new ArrayList<>(strategies.keySet());
    }

    @Transactional(readOnly = true)
    public MatchResult form(int teamSize, String strategyName, List<Constraint> constraints) {
        return run(round(teamSize, constraints), strategyName);
    }

    /** A round over the current students, ready for any strategy. */
    @Transactional(readOnly = true)
    public Round round(int teamSize, List<Constraint> constraints) {
        return new Round(loadCandidates(), teamSize, constraints);
    }

    public MatchResult run(Round round, String strategyName) {
        MatchingStrategy strategy =
            strategies.getOrDefault(strategyName, strategies.get("greedy-balancer"));
        return strategy.assign(round);
    }

    /** Look up a student among the current candidates by id. */
    @Transactional(readOnly = true)
    public java.util.Optional<Student> student(long id) {
        return students.findById(id);
    }

    /** Run every strategy on the same round, best overall score first. */
    @Transactional(readOnly = true)
    public List<MatchResult> compare(int teamSize, List<Constraint> constraints) {
        Round round = new Round(loadCandidates(), teamSize, constraints);
        List<MatchResult> results = new ArrayList<>();
        for (MatchingStrategy strategy : strategies.values()) {
            results.add(strategy.assign(round));
        }
        results.sort(Comparator.comparingDouble((MatchResult r) -> r.score().overall()).reversed());
        return results;
    }

    private List<Student> loadCandidates() {
        List<Student> all = students.findAll();
        // Initialise the collections the strategies and scorer read, while the
        // transaction is open.
        all.forEach(s -> {
            s.getSkills().size();
            s.getDesiredRoles().size();
            s.getPreferredPeerIds().size();
        });
        return all;
    }
}
