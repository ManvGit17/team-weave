package io.teamweave.domain.student;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A student and the profile the engine matches on: skills (with levels),
 * weekly availability, interests, the roles they'd like, and the peers they'd
 * like to work with.
 */
@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "student_id", nullable = false)
    private List<SkillRating> skills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "student_availability",
                     joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "slot", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Availability> availability = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "student_interest",
                     joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "topic", nullable = false)
    private Set<String> interests = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "student_desired_role",
                     joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Role> desiredRoles = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "student_peer_pref",
                     joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "peer_id", nullable = false)
    private Set<Long> preferredPeerIds = new LinkedHashSet<>();

    protected Student() {
        // for JPA
    }

    public Student(String name, String email) {
        this.name = name;
        this.email = email;
    }

    /** Highest level recorded for a skill, or 0 if the student lacks it. */
    public int levelOf(String skill) {
        return skills.stream()
            .filter(s -> s.getSkill().equalsIgnoreCase(skill))
            .mapToInt(SkillRating::getLevel)
            .max()
            .orElse(0);
    }

    public Student addSkill(String skill, int level) {
        this.skills.add(new SkillRating(skill, level));
        return this;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<SkillRating> getSkills() {
        return skills;
    }

    public Set<Availability> getAvailability() {
        return availability;
    }

    public Set<String> getInterests() {
        return interests;
    }

    public Set<Role> getDesiredRoles() {
        return desiredRoles;
    }

    public Set<Long> getPreferredPeerIds() {
        return preferredPeerIds;
    }
}
