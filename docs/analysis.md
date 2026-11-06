# teamweave — analysis

**teamweave** forms balanced project teams from students' skills, availability and preferences, and
follows each team through its lifecycle. It grows from a small Spring Boot MVC app that managed a
list of students into an engine whose **design is the product**: the matching logic, the
constraints, the lifecycle and the edit history are each a well-known object-oriented pattern, made
visible in the app itself.

## The problem

Every project course has to put students into teams. Done by hand it's slow and uneven; done by luck
it ignores skills, availability and who works well together. A good assignment should balance skills
across teams, cover the roles each team needs, respect reasonable preferences, and be explainable —
you should be able to see *why* a student landed where they did, and adjust it.

## Actors

- **Coordinator** — sets up a round, runs matching strategies, compares results, makes manual
  adjustments, and advances teams through their lifecycle.
- **Student** — has a profile: skills (with levels), availability, interests, and preferences
  (peers they'd like to work with, roles they want).

## Use cases

```mermaid
flowchart LR
    Coordinator([Coordinator])
    Student([Student])

    Coordinator --> UC1[Manage students and profiles]
    Student --> UC1
    Coordinator --> UC2[Set up a formation round]
    Coordinator --> UC3[Run a matching strategy]
    Coordinator --> UC4[Compare strategies]
    Coordinator --> UC5[Adjust teams manually, with undo]
    Coordinator --> UC6[Advance a team's lifecycle]
    Coordinator --> UC7[See how it's built]
    UC3 -.includes.-> UC8[Score and explain the result]
    UC2 -.includes.-> UC9[Define constraints]
```

1. **Manage students and profiles** — add students; record skills and levels, availability,
   interests, and preferences.
2. **Set up a formation round** — choose the students, the target team size, and the constraints.
3. **Run a matching strategy** — produce an assignment of students to teams, scored and explained.
4. **Compare strategies** — run several strategies over the same round and compare their scores
   side by side.
5. **Adjust teams manually** — move a student between teams; every change is undoable and redoable.
6. **Advance a team's lifecycle** — move a team through valid states only (forming → locked →
   active → submitted → reviewed).
7. **See how it's built** — an in-app page that shows where each design pattern lives, with links to
   the source and a class diagram generated from the code.

## Domain model

```mermaid
classDiagram
    class Student {
        +Long id
        +String name
        +String email
    }
    class Skill { +String name }
    class SkillRating { +Level level }
    class Availability { +Set~Slot~ slots }
    class Preference {
        +Set~Student~ preferredPeers
        +Set~Role~ desiredRoles
    }
    class Round {
        +int targetTeamSize
        +run(MatchingStrategy) MatchResult
    }
    class Team {
        +String name
        +TeamState state
        +skillProfile() SkillProfile
    }
    class TeamMember { +Role role }
    class MatchResult {
        +Score score
        +List~Explanation~ explanations
    }
    class Constraint {
        <<Specification>>
        +isSatisfiedBy(Assignment) boolean
    }
    class MatchingStrategy {
        <<Strategy>>
        +assign(Round) MatchResult
    }

    Student "1" --> "*" SkillRating
    SkillRating "*" --> "1" Skill
    Student "1" --> "1" Availability
    Student "1" --> "1" Preference
    Round "1" --> "*" Student : candidates
    Round "1" --> "*" Constraint
    Round "1" --> "*" Team
    Team "1" --> "*" TeamMember
    TeamMember "*" --> "1" Student
    Round ..> MatchingStrategy : uses
    Round ..> MatchResult : produces
```

## Key interactions

### Running a strategy

```mermaid
sequenceDiagram
    actor Coordinator
    participant C as RoundController
    participant S as FormationService
    participant Strat as MatchingStrategy
    participant Spec as Constraint (Specification)
    participant Score as Scorer

    Coordinator->>C: run(roundId, strategy)
    C->>S: form(round, strategy)
    S->>Strat: assign(round)
    Strat->>Spec: isSatisfiedBy(candidateAssignment)
    Spec-->>Strat: true / false
    Strat-->>S: MatchResult (teams + explanations)
    S->>Score: score(result)
    Score-->>S: Score (coverage, preference, balance)
    S-->>C: scored MatchResult
    C-->>Coordinator: teams, score and reasons
```

### A manual move, with undo

```mermaid
sequenceDiagram
    actor Coordinator
    participant C as TeamController
    participant Inv as CommandInvoker
    participant Cmd as MoveStudentCommand
    participant T as Team
    participant Obs as Observers

    Coordinator->>C: move(student, fromTeam, toTeam)
    C->>Inv: execute(MoveStudentCommand)
    Inv->>Cmd: execute()
    Cmd->>T: remove(student) / add(student)
    T->>Obs: notify(TeamChanged)
    Coordinator->>C: undo
    C->>Inv: undo()
    Inv->>Cmd: undo()
    Cmd->>T: restore previous membership
    T->>Obs: notify(TeamChanged)
```

## Patterns, and where they live

| Pattern | Role in the domain |
| :-- | :-- |
| **Strategy** | Interchangeable matching algorithms (greedy balancer, preference-first, skill-coverage) behind one `MatchingStrategy` interface. |
| **Specification** | Composable constraints (`isSatisfiedBy`): team size, required roles, keep-together, keep-apart — combined with and/or/not. |
| **State** | A team's lifecycle; only valid transitions are allowed. |
| **Command** | Manual adjustments as commands with undo/redo; every change recorded. |
| **Observer** | Notifications when a team changes. |

These are not decoration: each is exercised by a unit test, enforced by architecture tests, and
shown on the in-app "How it's built" page with links to the code.
