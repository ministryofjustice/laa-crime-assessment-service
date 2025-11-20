## What

[Link to story](https://dsdmoj.atlassian.net/browse/LCAM-XXX)

Describe what you did and why.

## Checklist

Before you ask people to review this PR:

- [ ] The pull request should not be labelled as *WIP/DRAFT* and should have a title format of *LCAM-XXXX-Description*.
- [ ] A link to the Jira ticket should be provided along with a description of the changes.
- [ ] Github should not be reporting any conflicts with the main branch.
- [ ] All of the pull request triggered checks (such as Build and Test, CodeQL and Snyk) should have passed.
- [ ] Spotless should have been run locally to format the changes according to the Google Java Format.
- [ ] You should have looked at the diff against main and ensured that nothing unexpected is included in your changes.
- [ ] You should have checked that the commit messages say why the change was made.
- [ ] Any necessary documentation (link to any outside of the codebase) should have been updated.
- [ ] Where relevant the changes should have been deployed to the DEV/TEST environment and the Functional Test Suite [run](https://github.com/ministryofjustice/laa-crimeapps-maat-functional-tests/actions/workflows/ExecuteUiTests.yaml) to verify nothing new has been broken.