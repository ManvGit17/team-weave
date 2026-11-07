# The engine

teamweave's matching engine is built from five object-oriented patterns. Each one is a real part of
the engine, is exercised by a test, and is kept in place by an architecture rule. The class diagram
in [`diagrams/domain-model.md`](diagrams/domain-model.md) is generated from the compiled code, so it
always matches this description.

## The patterns

| Pattern | What it does here | Where it lives | Proved by |
| :-- | :-- | :-- | :-- |
| **Strategy** | Interchangeable team-formation algorithms behind one interface. | [`MatchingStrategy`](../src/main/java/io/teamweave/domain/matching/MatchingStrategy.java) and three implementations in [`domain/matching`](../src/main/java/io/teamweave/domain/matching) | [`MatchingStrategyTest`](../src/test/java/io/teamweave/domain/matching/MatchingStrategyTest.java) |
| **Specification** | Constraints that compose with `and`, `or` and `negate`: team size, required roles, keep-together, keep-apart. | [`Constraint`](../src/main/java/io/teamweave/domain/constraint/Constraint.java) and [`domain/constraint`](../src/main/java/io/teamweave/domain/constraint) | [`ConstraintTest`](../src/test/java/io/teamweave/domain/constraint/ConstraintTest.java) |
| **State** | The team lifecycle — forming → locked → active → submitted → reviewed. Each state allows only its own transition; anything else throws. The state also decides whether membership can change: only a forming team can gain or lose members. | [`TeamState`](../src/main/java/io/teamweave/domain/team/TeamState.java) (a sealed interface with one record per state) | [`TeamStateTest`](../src/test/java/io/teamweave/domain/team/TeamStateTest.java) |
| **Command** | Manual moves as reversible commands, with undo, redo and a history. A move checks both teams before touching either, and a failed undo or redo leaves the history intact. | [`Command`](../src/main/java/io/teamweave/domain/command/Command.java), [`MoveStudentCommand`](../src/main/java/io/teamweave/domain/command/MoveStudentCommand.java), [`CommandInvoker`](../src/main/java/io/teamweave/domain/command/CommandInvoker.java) | [`CommandTest`](../src/test/java/io/teamweave/domain/command/CommandTest.java) |
| **Observer** | Listeners are told about every change to a team's members or state. | [`Team`](../src/main/java/io/teamweave/domain/team/Team.java) (subject), [`TeamChangeListener`](../src/main/java/io/teamweave/domain/team/TeamChangeListener.java), [`LoggingTeamListener`](../src/main/java/io/teamweave/application/LoggingTeamListener.java) | [`TeamObserverTest`](../src/test/java/io/teamweave/domain/team/TeamObserverTest.java) |

## The three strategies

- **greedy-balancer** sorts students by total skill strength and deals them across teams in a snake
  order, so no team hoards the strongest people.
- **preference-first** puts a student on a team (with room) that holds someone they asked for, or
  someone who asked for them; otherwise the emptiest team.
- **skill-coverage** puts each student on the team that gains the most new skills from them.

Every run is **scored** on three axes, each from 0 to 1, by
[`Scorer`](../src/main/java/io/teamweave/domain/formation/Scorer.java):

- *skill coverage* — on average, the share of the cohort's distinct skills each team has;
- *preference satisfaction* — the share of students with a peer preference who got at least one
  preferred teammate;
- *balance* — one minus the spread (coefficient of variation) of the teams' total skill strength.

The overall score is their mean. Every placement also carries a plain-language **explanation**
("placed with a peer who asked for them", "adds 1 new skill(s) to the team"), and any constraint a
result breaks is reported rather than hidden.

## Comparing them

Run every strategy on the seeded cohort of eight students, in teams of four:

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,compare
```

Result:

| Strategy | Overall | Skill coverage | Preferences | Balance |
| :-- | --: | --: | --: | --: |
| preference-first | 0.875 | 0.667 | 1.000 | 0.958 |
| skill-coverage | 0.737 | 0.833 | 0.500 | 0.877 |
| greedy-balancer | 0.667 | 0.611 | 0.500 | 0.889 |

What it shows:

- **No strategy wins everything.** preference-first satisfies every peer preference but gives up
  skill coverage to do it; skill-coverage builds the most well-rounded teams but ignores who wants
  to work with whom. That trade-off is why the strategies are interchangeable rather than one
  hard-coded algorithm.
- **The comparison caught a bug.** The first version of preference-first only followed a student's
  *own* preferences toward peers already placed, so a one-way preference from an earlier student
  was never honoured — it scored 0.500 on preferences, no better than the others. It now also
  follows preferences *toward* the student, and a regression test
  (`preferenceFirstHonoursAOneWayPreferenceFromAnEarlierStudent`) keeps it fixed.
- These are greedy heuristics on a small cohort, not optimal assignments; a constraint solver is on
  the roadmap.
- Availability and interests are recorded on every profile but **not yet used** by any strategy or
  score. Using them is listed in [good first issues](good-first-issues.md).

## Architecture rules

[`ArchitectureTest`](../src/test/java/io/teamweave/architecture/ArchitectureTest.java) enforces, on
every build:

- the layers — `web` → `application` → `domain`, with `infrastructure` reachable only from
  `application`;
- that the **domain is framework-free**: no Spring, and no dependency on the outer layers;
- that each pattern's implementations live in its package (strategies in `matching`, constraints in
  `constraint`, commands in `command`, states in `team`).

[`DomainDiagramTest`](../src/test/java/io/teamweave/architecture/DomainDiagramTest.java) regenerates
the class diagram from the compiled classes and fails if the committed copy is stale. Refresh it
with:

```bash
./mvnw test -Dtest=DomainDiagramTest -Ddiagram.write=true
```
