package io.teamweave.domain.constraint;

import io.teamweave.domain.formation.Assignment;

/**
 * A rule an assignment must satisfy — the Specification pattern. Constraints
 * compose with {@link #and}, {@link #or} and {@link #negate}, so complex rules
 * are built from simple ones.
 */
public interface Constraint {

    boolean isSatisfiedBy(Assignment assignment);

    /** A human-readable description, used in explanations and the UI. */
    String describe();

    default Constraint and(Constraint other) {
        return new And(this, other);
    }

    default Constraint or(Constraint other) {
        return new Or(this, other);
    }

    default Constraint negate() {
        return new Not(this);
    }

    record And(Constraint left, Constraint right) implements Constraint {
        @Override
        public boolean isSatisfiedBy(Assignment a) {
            return left.isSatisfiedBy(a) && right.isSatisfiedBy(a);
        }

        @Override
        public String describe() {
            return "(" + left.describe() + " and " + right.describe() + ")";
        }
    }

    record Or(Constraint left, Constraint right) implements Constraint {
        @Override
        public boolean isSatisfiedBy(Assignment a) {
            return left.isSatisfiedBy(a) || right.isSatisfiedBy(a);
        }

        @Override
        public String describe() {
            return "(" + left.describe() + " or " + right.describe() + ")";
        }
    }

    record Not(Constraint inner) implements Constraint {
        @Override
        public boolean isSatisfiedBy(Assignment a) {
            return !inner.isSatisfiedBy(a);
        }

        @Override
        public String describe() {
            return "not " + inner.describe();
        }
    }
}
