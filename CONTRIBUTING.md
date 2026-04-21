# Development Guide (Autogex)

To maintain the integrity of the automated release system and code quality, follow these steps for each new feature or fix.

## Branch Workflow

1. **New Branch:** Always create a branch from `main` (e.g., `feat/feature-name`).
2. **Development:** Write code and test. **Note:** Do not manually change the version in `pom.xml`.
3. **Pull Request:** Open a PR to `main`.

## Commit and Release Messages

The PR title must follow the [Conventional Commits](https://www.conventionalcommits.org/) to allow `release-please` to correctly generate the changelog and increment the version:

- `feat: ...` -> **Minor** Increment (e.g., 1.3.11 -> 1.4.0)
- `fix: ...` -> **Patch** Increment (e.g., 1.3.11 -> 1.3.12)
- `feat!: ...` -> **Major** Increment (breaking change)

## Finalization

- Once the PR has been merged into `main` (using **Squash and merge**), wait for the bot to create the release PR.
- Merge the bot's PR to officially publish the new version.
