# Security policy

## Supported versions

This project is pre-1.0; fixes land on the default branch.

## Reporting a vulnerability

Please report suspected vulnerabilities privately rather than in a public issue:

- open a [private security advisory](https://docs.github.com/en/code-security/security-advisories/guidance-on-reporting-and-writing-information-about-vulnerabilities/privately-reporting-a-security-vulnerability)
  on this repository, or
- contact the maintainer (**@ManvGit17**) through GitHub.

Include steps to reproduce and the impact you observed.

## Scope note

teamweave is a single-coordinator tool with no authentication: anyone who can reach it can edit
students and teams. Run it on a machine or network you control. The development database
credentials in `docker-compose.yml` and `application.properties` are for local use only — change
them for any shared deployment.
