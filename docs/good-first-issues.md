# Good first issues

Self-contained starting points, each touching one part of the system. Open an issue to claim one,
and see [CONTRIBUTING.md](../CONTRIBUTING.md).

## Engine (domain)

1. **Score schedule overlap.** Students record weekly availability, but no strategy or score uses
   it yet. Add a fourth axis to `Scorer` — the share of each team's members who share at least one
   slot — with a unit test.
2. **Use interests.** Interests are captured but unused. Add an interest-overlap score, or a
   strategy that groups shared interests, and include it in the comparison.
3. **A random baseline strategy.** A seeded random matcher gives the comparison a floor to beat.
   Implement it as a `MatchingStrategy` (the architecture test will check where it lives).
4. **Repair instead of report.** Strategies report broken constraints but don't fix them. Add a
   step that tries swaps until the constraints hold (or gives up and says so).

## Board (application and web)

5. **Edit a student's profile in the UI.** Registration takes a name and email only; skills, roles
   and availability come from the seed data. Add a profile form.
6. **Persist formations.** The board lives in the HTTP session and is lost on restart. Store a
   formation and its teams in PostgreSQL with a Flyway migration.
7. **Swap two students in one command.** A `SwapStudentsCommand` that undoes as a single step.
8. **Export teams as CSV** from the board.

## Accessibility and polish

9. **Announce changes to screen readers** — make the activity feed an `aria-live` region.
10. **Show which constraint each team breaks**, not just the board as a whole.
