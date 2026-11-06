# teamweave

**A team-formation engine for student projects** — where the object-oriented design is the product.

teamweave forms balanced project teams from students' skills, availability and preferences, and
follows each team through its lifecycle. The matching algorithms, constraints, lifecycle and edit
history are each a named design pattern, tested and made visible in the app.

> **Status.** Foundation milestone: the richer student model persists in PostgreSQL with Flyway
> migrations, seeded sample data, and tests covering the web and persistence layers. The engine
> (strategies, constraints, lifecycle, commands, notifications) and the UI follow. See
> [`docs/roadmap.md`](docs/roadmap.md) and [`docs/analysis.md`](docs/analysis.md).

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

## Layout

```
io.teamweave
├── web             controllers and views
├── application     services
├── domain.student  the student aggregate (skills, availability, preferences)
└── infrastructure  Spring Data repositories
```

Database schema lives in [`src/main/resources/db/migration`](src/main/resources/db/migration) as
Flyway migrations; JPA runs with `ddl-auto=validate`, so entity and schema can't drift.

## License

Apache-2.0 (added with the launch milestone).
