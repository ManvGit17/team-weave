# Contributing

Thanks for your interest. The [good first issues](docs/good-first-issues.md) are a good place to
start.

## Development setup

You need JDK 17 or newer and Docker (for PostgreSQL).

```bash
docker compose up -d                                       # the database
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev      # the app, with sample data
```

## Running the tests

```bash
./mvnw verify
```

The tests need the Compose database running. CI runs the same command on every push and pull
request, against a PostgreSQL service.

If you change a class in the `domain` package, the generated class diagram may need refreshing:

```bash
./mvnw test -Dtest=DomainDiagramTest -Ddiagram.write=true
```

## Design rules

The architecture is part of the product, and `ArchitectureTest` enforces it:

- The `domain` package must not depend on Spring or on the outer layers.
- `web` talks to `application`; only `application` reaches `infrastructure`.
- Each pattern's implementations live in its package (`matching`, `constraint`, `command`, `team`).

When you add behaviour, add a test that exercises it — every pattern the README mentions has one.

## Pull requests

- Work on a feature branch; keep the default branch clean.
- Keep commits small and in [Conventional Commits](https://www.conventionalcommits.org/) style.
- Make sure `./mvnw verify` passes before asking for review.
