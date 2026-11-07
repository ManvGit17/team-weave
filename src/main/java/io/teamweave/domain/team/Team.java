package io.teamweave.domain.team;

import io.teamweave.domain.student.Role;
import io.teamweave.domain.student.Student;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A team under formation. It is the Subject of the Observer pattern (listeners
 * are told about every change) and delegates its lifecycle to a {@link TeamState}
 * (the State pattern).
 */
public class Team {

    private final String name;
    private final List<TeamMember> members = new ArrayList<>();
    private final List<TeamChangeListener> listeners = new ArrayList<>();
    private TeamState state = new TeamState.Forming();

    public Team(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String name() {
        return name;
    }

    public TeamState state() {
        return state;
    }

    public List<TeamMember> members() {
        return List.copyOf(members);
    }

    public List<Student> students() {
        return members.stream().map(TeamMember::student).toList();
    }

    public int size() {
        return members.size();
    }

    public SkillProfile skillProfile() {
        return SkillProfile.of(students());
    }

    // -- Observer ----------------------------------------------------------
    public void addListener(TeamChangeListener listener) {
        listeners.add(listener);
    }

    private void notifyChange(String change) {
        for (TeamChangeListener listener : listeners) {
            listener.onTeamChanged(this, change);
        }
    }

    // -- Membership --------------------------------------------------------
    public void addMember(Student student, Role role) {
        requireMembershipChanges();
        members.add(new TeamMember(student, role));
        notifyChange("added " + student.getName());
    }

    public void addMember(Student student) {
        addMember(student, null);
    }

    public boolean removeMember(Student student) {
        requireMembershipChanges();
        boolean removed = members.removeIf(m -> sameStudent(m.student(), student));
        if (removed) {
            notifyChange("removed " + student.getName());
        }
        return removed;
    }

    public boolean contains(Student student) {
        return members.stream().anyMatch(m -> sameStudent(m.student(), student));
    }

    // -- Lifecycle (State) -------------------------------------------------
    public void lock() {
        transition(state.lock(), "locked");
    }

    public void activate() {
        transition(state.activate(), "activated");
    }

    public void submit() {
        transition(state.submit(), "submitted");
    }

    public void review() {
        transition(state.review(), "reviewed");
    }

    private void transition(TeamState next, String change) {
        this.state = next;
        notifyChange(change);
    }

    /** The State decides whether membership may change (only while forming). */
    private void requireMembershipChanges() {
        if (!state.allowsMembershipChanges()) {
            throw new IllegalTransitionException("change the members of", state.label());
        }
    }

    private static boolean sameStudent(Student a, Student b) {
        if (a.getId() != null && b.getId() != null) {
            return a.getId().equals(b.getId());
        }
        return a == b;
    }
}
