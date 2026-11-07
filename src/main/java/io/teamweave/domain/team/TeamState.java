package io.teamweave.domain.team;

/**
 * The team lifecycle, modelled with the State pattern: each state knows only
 * the transitions it allows, and returns the next state. Anything else throws.
 *
 * forming → locked → active → submitted → reviewed
 */
public sealed interface TeamState
        permits TeamState.Forming, TeamState.Locked, TeamState.Active,
                TeamState.Submitted, TeamState.Reviewed {

    String label();

    default TeamState lock() {
        throw illegal("lock");
    }

    default TeamState activate() {
        throw illegal("activate");
    }

    default TeamState submit() {
        throw illegal("submit");
    }

    default TeamState review() {
        throw illegal("review");
    }

    default boolean isTerminal() {
        return false;
    }

    /** Whether students may still join or leave a team in this state. */
    default boolean allowsMembershipChanges() {
        return false;
    }

    private IllegalTransitionException illegal(String action) {
        return new IllegalTransitionException(action, label());
    }

    record Forming() implements TeamState {
        @Override
        public String label() {
            return "forming";
        }

        @Override
        public TeamState lock() {
            return new Locked();
        }

        @Override
        public boolean allowsMembershipChanges() {
            return true;
        }
    }

    record Locked() implements TeamState {
        @Override
        public String label() {
            return "locked";
        }

        @Override
        public TeamState activate() {
            return new Active();
        }
    }

    record Active() implements TeamState {
        @Override
        public String label() {
            return "active";
        }

        @Override
        public TeamState submit() {
            return new Submitted();
        }
    }

    record Submitted() implements TeamState {
        @Override
        public String label() {
            return "submitted";
        }

        @Override
        public TeamState review() {
            return new Reviewed();
        }
    }

    record Reviewed() implements TeamState {
        @Override
        public String label() {
            return "reviewed";
        }

        @Override
        public boolean isTerminal() {
            return true;
        }
    }
}
