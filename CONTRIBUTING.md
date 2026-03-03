# Contributing to Emergency Ambulance Request System

Thank you for contributing to the **Emergency Ambulance Request System**. This document defines the standards, workflows, and policies that all contributors must follow to maintain a professional, stable, and collaborative codebase.

Please read this guide thoroughly before opening an issue, creating a branch, or submitting a Pull Request.

---

## Table of Contents

1. [Branching Strategy](#1-branching-strategy)
2. [Feature Development Workflow](#2-feature-development-workflow)
3. [Pull Request Policy](#3-pull-request-policy)
4. [Branch Naming Convention](#4-branch-naming-convention)
5. [Commit Message Convention](#5-commit-message-convention)
6. [Code Quality Standards](#6-code-quality-standards)
7. [Merge Policy](#7-merge-policy)
8. [Professional Conduct](#8-professional-conduct)

---

## 1. Branching Strategy

The project maintains two primary long-lived branches:

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready code only. Reflects the latest stable release. |
| `develop` | Integration branch where completed and tested features are merged. |

### Rules

- **Direct commits to `main` are strictly prohibited.**
- **Direct commits to `develop` are strictly prohibited.**
- All changes — regardless of size — must be introduced through a **Pull Request**.
- **Automatic merging is not permitted.** Every Pull Request requires deliberate human review and approval before merging.

---

## 2. Feature Development Workflow

### Issues and Epics

- Every piece of work (feature, fix, refactor, chore) **must be linked to a GitHub Issue** before development begins.
- Related issues may be grouped under **Epics** to track progress on larger bodies of work.
- Do not begin development on a task that does not have a corresponding issue.

### Branch Lifecycle

1. Create a new branch from `develop` for every task (see [Branch Naming Convention](#4-branch-naming-convention)).
2. Develop and test the change locally on that branch.
3. Open a Pull Request targeting `develop` once the work is complete.
4. A feature branch may only be merged into `develop` after **all** of the following conditions are met:
   - Successful local testing with no regressions.
   - Code review approval from the required number of reviewers.
   - All automated checks pass (where CI is configured).

> Branches must remain focused. One branch should correspond to one issue or one logical unit of change.

---

## 3. Pull Request Policy

### Requirements Before Opening a PR

- The feature branch must be up to date with `develop`.
- All local tests must pass.
- The PR must **reference the related Issue** (e.g., `Closes #42` or `Relates to #17`) in the PR description.

### PR Description Template

Every Pull Request must include the following:

```
## Summary
<!-- A clear description of what this PR does and why. -->

## Related Issue
<!-- Reference the issue: e.g., Closes #12 -->

## Changes Made
<!-- List the key changes introduced by this PR. -->

## Screenshots
<!-- If this PR contains UI changes, include before/after screenshots. -->

## Testing Notes
<!-- Describe how the changes were tested. Include device/emulator details if relevant. -->
```

### Review Requirements

- A minimum of **two approving reviews** is required before any PR may be merged.
- **Self-approval is not permitted.** The author of a PR may not approve their own PR.
- Reviewers are expected to assess correctness, code quality, architecture alignment, and test coverage.
- All review comments must be resolved before merging.

---

## 4. Branch Naming Convention

Branch names must be lowercase and use hyphens to separate words. The format is:

```
<type>/<short-description>
```

| Type | Usage |
|------|-------|
| `feat/` | A new feature |
| `fix/` | A bug fix |
| `refact/` | Code refactoring without functional change |
| `chore/` | Maintenance tasks (dependencies, config, tooling) |
| `docs/` | Documentation updates |
| `test/` | Adding or updating tests |
| `hotfix/` | Critical fix applied urgently to `main` via PR |

### Examples

```
feat/emergency-request-flow
fix/ambulance-status-update
refact/request-viewmodel-state
chore/update-compose-dependencies
docs/update-setup-guide
test/hospital-registration-unit-tests
hotfix/crash-on-request-submission
```

> Branch names must be concise, descriptive, and directly reflect the work being done.

---

## 5. Commit Message Convention

This project follows the [Conventional Commits](https://www.conventionalcommits.org/) specification. All commit messages must adhere to the following format:

```
<type>: <short imperative description>
```

| Type | Usage |
|------|-------|
| `feat:` | Introduces a new feature |
| `fix:` | Resolves a bug |
| `refact:` | Refactors existing code without changing behaviour |
| `chore:` | Maintenance or non-functional changes |
| `docs:` | Documentation changes only |
| `test:` | Adds or modifies tests |
| `style:` | Code formatting or style changes (no logic change) |

### Examples

```
feat: add emergency request submission screen
fix: correct ambulance availability status update
refact: improve viewmodel state handling in hospital module
chore: update jetpack compose dependencies
docs: add contributing guidelines
test: add unit tests for request repository
```

### Guidelines

- Use the **imperative mood** in the description (e.g., "add", "fix", "update" — not "added" or "fixes").
- Keep the subject line under **72 characters**.
- For complex changes, add a blank line after the subject and provide a more detailed body.
- Do not end the subject line with a period.

---

## 6. Code Quality Standards

All code contributed to this project must conform to the following standards.

### Architecture

- The application follows **MVVM (Model-View-ViewModel)** architecture.
- Maintain a strict **separation of concerns**:
  - **Model** — data classes, repositories, and data sources.
  - **ViewModel** — state management and business logic.
  - **View** — Jetpack Compose UI components only; no business logic.

### Kotlin and Jetpack Compose

- Use **meaningful, self-explanatory names** for variables, functions, classes, and parameters.
- Avoid **hardcoded strings, colours, and dimensions** — use resource files or design tokens.
- Write **modular and reusable composables**; a composable should do one thing well.
- Ensure **proper state management** using `State`, `StateFlow`, or `LiveData` as appropriate.
- Avoid **side effects** inside composable functions; use `LaunchedEffect`, `SideEffect`, or `DisposableEffect` where necessary.

### General Code Hygiene

- Remove all **unused imports** before committing.
- Remove all **dead code**, commented-out code blocks, and debug logging before opening a PR.
- Do not leave `TODO` comments without an associated GitHub Issue reference.
- Follow the **official Kotlin code style** as configured in the project (`kotlin.code.style=official`).

---

## 7. Merge Policy

| Rule | Detail |
|------|--------|
| Feature branches → `develop` | Via Pull Request with two approvals |
| `develop` → `main` | Via Pull Request only; requires two approvals |
| Direct push to `main` | **Not allowed under any circumstances** |
| Force push | **Not allowed on any protected branch** |
| `main` stability | `main` must always be stable and deployable |

- Only the `develop` branch may be merged into `main`.
- `main` represents the production state of the application at all times.
- Hotfixes that must bypass `develop` must still go through a PR targeting `main` and be back-merged into `develop` immediately after.

---

## 8. Professional Conduct

Collaboration is at the core of this project. All contributors are expected to uphold the following standards in all interactions — including code reviews, issue discussions, and PR comments.

- **Be respectful.** Critique code, not the person who wrote it.
- **Be constructive.** Every piece of feedback should be actionable and clearly explained.
- **Be precise.** When requesting changes, state what needs to change and why.
- **Be responsive.** Address review comments and requested changes in a timely manner.
- **Document complex logic.** Where code is non-obvious, add a concise inline comment or update the relevant documentation.
- **Assume good intent.** Collaboration works best in an environment of mutual trust and respect.

Disrespectful or unconstructive behaviour will not be tolerated and may result in removal from the project.

---

*These guidelines exist to keep our codebase clean, our collaboration effective, and our team aligned. When in doubt, ask — and when reviewing, be the reviewer you would want on your own PRs.*
