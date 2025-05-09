## What

[Link to story](https://dsdmoj.atlassian.net/browse/LCAM-XXX)

Describe what you did and why.

## Checklist

Before you ask people to review this PR:

- [ ] Tests should be passing: `./gradlew test`
- [ ] Github should not be reporting conflicts; you should have recently run `git rebase main`.
- [ ] Avoid mixing whitespace changes with code changes in the same commit. These make diffs harder to read and conflicts more likely.
- [ ] You should have looked at the diff against main and ensured that nothing unexpected is included in your changes.
- [ ] You should have checked that the commit messages say why the change was made.

## PR acceptance criteria

Pull requests must meet the following criteria to be considered for approval:

- PR contains a meaningful title
- PR contains a meaningful description
- at least one Jira work item has been linked in the description

Likewise, all commits in the PR should adhere to the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/#summary) specification.

## Additional checks

- Don’t forget to [run](https://github.com/ministryofjustice/laa-crimeapps-maat-functional-tests/actions/workflows/ExecuteUiTests.yaml) the MAAT functional test suite after deploying your changes to the DEV or TEST environments to ensure your changes haven’t broken any of the functional tests.
