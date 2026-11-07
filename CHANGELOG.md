# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project aims to follow
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] — 2026-11-07

First release: a team-formation engine whose object design is on show, built in three milestones.

### Foundation
- PostgreSQL with Flyway migrations; JPA validates its mapping against the schema.
- A layered structure (`web` / `application` / `domain` / `infrastructure`).
- A student profile the engine can match on: skills with levels, availability, interests, desired
  roles and peer preferences, plus a seeded sample cohort.
- Tests against a real database and GitHub Actions CI.

### The engine
- Strategy: greedy-balancer, preference-first and skill-coverage matchers.
- Specification: team size, required roles, keep-together and keep-apart constraints that compose.
- State: the team lifecycle, forming → locked → active → submitted → reviewed.
- Command: undoable, redoable moves with history.
- Observer: notifications of every team change.
- Scoring (skill coverage, preference satisfaction, balance) and per-placement explanations.
- Architecture tests and a class diagram generated from the code.

### Experience and launch
- The formation board: drag students between teams, undo and redo (also by keyboard), lock and
  advance teams, live scores and constraint checks, and an activity feed.
- A page comparing every strategy side by side.
- A "How it's built" page linking each pattern to its code and test, with the generated diagram.
- Only a forming team can change members (the State now governs membership), and moves are
  checked before they change anything, so one can never half-happen.
- A light and dark interface that works down to phone width.
- Landing-page README, contributor guide, security policy, code of conduct and the Apache-2.0
  license.

[Unreleased]: https://github.com/ManvGit17/team-weave/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/ManvGit17/team-weave/releases/tag/v0.1.0
