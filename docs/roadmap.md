# teamweave — roadmap

A team-formation engine built on a Spring Boot MVC foundation, where the object design is the
product: interchangeable algorithms, composable constraints, a team lifecycle, undoable edits and
notifications — each a named pattern, tested and shown in the app.

## Architecture

Layered, with the domain at the centre and nothing in the domain depending on the web or the
database:

```
  web        controllers + Thymeleaf views + "How it's built" page
   │  depends on
   ▼
  application  services: run a round, compare strategies, apply commands
   │  depends on
   ▼
  domain     Student, Round, Team (State), MatchingStrategy (Strategy),
             Constraint (Specification), Command, Observer, Scorer
   ▲  implemented by
   │
  infrastructure  Spring Data repositories, persistence, seed data
```

The layering is enforced by **architecture tests** (ArchUnit): the domain must not import Spring web
or persistence types; controllers must not reach past services into repositories; patterns live in
their expected packages. A broken dependency fails the build.

## Design made visible

- **Generated UML.** A class diagram of the `domain` package is generated from the code in CI and
  published, so it can never drift from the source. Sequence diagrams for the main flows accompany
  it in the docs.
- **Architecture tests.** The layer and dependency rules above run as tests.
- **"How it's built" page.** An in-app page lists each pattern, the class that embodies it, a short
  description, and a link to the source.

## Patterns and where they apply

| Pattern | Applied to |
| :-- | :-- |
| Strategy | `MatchingStrategy`: greedy balancer, preference-first, skill-coverage optimiser. |
| Specification | `Constraint`: team size, required roles, keep-together, keep-apart; composable. |
| State | `TeamState`: forming → locked → active → submitted → reviewed, valid transitions only. |
| Command | Manual moves/swaps with undo and redo; an audit trail of changes. |
| Observer | Listeners notified when a team changes. |

Each run **scores** its result (skill coverage, preference satisfaction, balance) and **explains**
each placement, so strategies can be compared on the same round.

## Database

PostgreSQL, run locally with Docker Compose and in CI as a service container. Flyway owns the
schema and JPA only validates against it, so the two cannot drift, and the tests run against a real
database rather than an in-memory stand-in.

## What ships this round

1. Tests pinning the current behaviour, a green build and CI.
2. A persistent database with migrations and a richer student model (skills, availability,
   preferences), with seeded sample data.
3. The engine: strategies, composable constraints, scoring and explanations, the team lifecycle,
   commands with undo/redo, and notifications — each with unit tests.
4. Generated UML and architecture tests in CI.
5. A clean UI to run and compare strategies, see team skill profiles, and move students between
   teams with undo; plus the "How it's built" page.
6. Landing-page README, docs, changelog, contributor guide, a licence, good first issues, a release.

## Roadmap (later)

Peer reviews and contribution tracking, per-team activity, meeting scheduling, a coordinator
dashboard, a constraint-solver or machine-learning matcher, and use by real classes.

## Delivery — three pull requests

### PR 1 — Foundation
- Tests for the app's current behaviour (controller, service, persistence round-trip).
- The build and GitHub Actions CI (Maven wrapper, run in a container for parity).
- PostgreSQL with Flyway migrations.
- The richer domain model — skills with levels, availability, interests, preferences — and seeded
  sample students.

### PR 2 — The engine
- Strategy (three matchers), Specification (composable constraints), scoring and explanations, the
  State lifecycle, Command with undo/redo, and Observer notifications.
- A unit test per pattern, plus a rerunnable script comparing the strategies on seeded data.
- Generated class diagram and ArchUnit architecture tests wired into CI.

### PR 3 — Experience & launch
- The UI: run and compare strategies, team skill profiles, drag students between teams with undo.
- The "How it's built" page linking each pattern to its code and the generated diagram.
- README as a landing page with screenshots, `docs/`, `CHANGELOG.md`, `CONTRIBUTING.md`, a licence,
  a few good first issues, and a tagged release.

Each pull request opens, waits for CI, and merges before the next begins.
