# teamweave

**A team-formation engine for student projects** — where the object-oriented design is the product.

teamweave forms balanced project teams from students' skills, availability and preferences, and
follows each team through its lifecycle. The matching algorithms, constraints, lifecycle and edit
history are each a named design pattern, tested and made visible in the app.

> **Status.** Engine milestone: three interchangeable matching strategies, composable
> constraints, scoring with explanations, a team lifecycle, undoable moves and change
> notifications — each a design pattern with its own test, a class diagram generated from the code,
> and architecture rules enforced in the build. The UI follows. See [`docs/engine.md`](docs/engine.md),
> [`docs/analysis.md`](docs/analysis.md) and [`docs/roadmap.md`](docs/roadmap.md).

## The engine, in five patterns

| Pattern | Role |
| :-- | :-- |
| Strategy | Interchangeable matchers: greedy-balancer, preference-first, skill-coverage. |
| Specification | Constraints that compose: team size, required roles, keep together, keep apart. |
| State | The team lifecycle: forming → locked → active → submitted → reviewed. |
| Command | Manual moves with undo, redo and history. |
| Observer | Listeners notified of every team change. |

Each is mapped to its code and its test in [`docs/engine.md`](docs/engine.md); the
[domain class diagram](docs/diagrams/domain-model.md) is generated from the compiled classes.

## Quick start

Needs Docker (for PostgreSQL) and JDK 17+.

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Open <http://localhost:8080> to see the seeded roster. Stop the database with `docker compose down`.

## Tests

The tests run against a real PostgreSQL (the Compose database), so the Flyway migrations and the JPA
mappings are validated against each other:

```bash
docker compose up -d
./mvnw test
```

## Compare the strategies

Every strategy, run on the seeded cohort and scored on skill coverage, preferences and balance:

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,compare
```

No strategy wins every axis — preference-first satisfies every preference but gives up some skill
coverage, which skill-coverage maximises. The numbers and what they mean are in
[`docs/engine.md`](docs/engine.md#comparing-them).

## Layout

```
io.teamweave
├── web                   controllers and views
├── application           services: run and compare strategies
├── domain
│   ├── student           the student profile (skills, availability, preferences)
│   ├── team              Team (Observer subject), TeamState (State)
│   ├── constraint        Constraint (Specification) and its rules
│   ├── matching          MatchingStrategy (Strategy) and three matchers
│   ├── command           Command, MoveStudentCommand, CommandInvoker (undo/redo)
│   └── formation         Round, Assignment, Scorer, MatchResult, Explanation
└── infrastructure        Spring Data repositories
```

The layering is enforced by ArchUnit tests: the domain cannot depend on Spring or on the outer
layers.

Database schema lives in [`src/main/resources/db/migration`](src/main/resources/db/migration) as
Flyway migrations; JPA runs with `ddl-auto=validate`, so entity and schema can't drift.

## License

Apache-2.0 (added with the launch milestone).
