package io.teamweave.domain.student;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A student's self-rated proficiency in a named skill, on a 1–5 scale. */
@Entity
@Table(name = "skill_rating")
public class SkillRating {

    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String skill;

    @Column(nullable = false)
    private int level;

    protected SkillRating() {
        // for JPA
    }

    public SkillRating(String skill, int level) {
        this.skill = skill;
        setLevel(level);
    }

    public Long getId() {
        return id;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (level < MIN_LEVEL || level > MAX_LEVEL) {
            throw new IllegalArgumentException(
                "level must be between " + MIN_LEVEL + " and " + MAX_LEVEL);
        }
        this.level = level;
    }
}
