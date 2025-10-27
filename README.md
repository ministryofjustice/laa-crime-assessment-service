# Crime Assessment Service

The Crime Assessment Service is a Spring Modulith application which handles multiple types of assessments that can be made as part of a criminal legal aid application. These include but are not limited to: means assessments, Interest of Justice (IoJ) appeals, passported benefits and hardship reviews. Each of the different assessment types is encapsulated within a separate application module.

The main consumer of the Crime Assessment Service is the Orchestration Service, which previously made requests to many of the individual microservices that now live within this service.

[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/laa-crime-assessment-service/badge)](https://github-community.service.justice.gov.uk/repository-standards/laa-crime-assessment-service)

## Technical documentation

This service uses Java 21 and is built using Spring Modulith. It is hosted on the Moj Cloud Platform and runs within Kubernetes.

## Getting started

### Application set-up

```
git clone git@github.com:ministryofjustice/laa-crime-assessment-service.git

cd laa-crime-assessment-service
```

### Java set-up

If you do not already have Java 21 installed, you will need to install it. There are many ways to do this, but a recommended approach
is to use [jenv](https://github.com/jenv/jenv) to manage your local Java versions and enable easy switching between them. jenv will
also automatically switch to use the correct Java version when you navigate to a project containing a `.java-version` file (providing
it is already installed).

```sh
# Check your current Java version - if it is Java 21 then the rest of the following steps are optional
java -version

brew update

brew install jenv
brew install openjdk@21

# See the output from the previous command to determine where Java 21 was installed
# You may need to symlink to your JavaVirtualMachines directory from the installed location to be able to use it
jenv add /path/to/Java/Contents/Home

# Check to ensure that you can see Java 21 now listed
jenv versions

# Switch to Java 21 (ensure you're still inside the laa-crime-assessment-service directory)
jenv local 21

# Confirm that Java 21 is now the current Java version
java -version
```

### Running tests

To build the project:

```sh
./gradlew clean build
```

Ensure that all tests are passing by running:

```sh
./gradlew clean test
```

Note: the `clean` task in the above two commands is optional but ensures that the build directory is
removed first, ensuring that there are no leftovers remaining of previous builds.

## Spring Modulith

This application is built using [Spring Modulith](https://spring.io/projects/spring-modulith). 

Nested packages are by default restricted to only the parent module they reside in. For example, in the following setup, classes within the passport module can access classes within its own module or at the root level of another module, but not classes within a nested package inside another module:

* iojappeal (Module)
  * IojAppeal.java <-- **Can** be used by the PassportController.java
  * IojAppealController.java
  * nestedpackage
    * SomeClass.java <-- **Cannot** be used by the PassportController.java, **Can** be used by the IojAppealController.java
* passport (Module)
  * PassportController.java

It's worth noting that you will be able to write code that breaks this restriction, however it'll cause the verification tests to fail (detailed below).

More information can be found [here](https://docs.spring.io/spring-modulith/reference/fundamentals.html).

### Verifying you conform to Spring Modulith restrictions

The `java/uk/gov/justice/laa/crime/assessmentservice/ModularityTests.java` tests will ensure that you are not accessing files outside of the proper scope. These will automatically be ran as part of the gradle build.

## CI/CD Pipeline


[GitHub Actions](https://github.com/ministryofjustice/laa-crime-assessment-service/actions) are used to manage the CI/CD pipeline for this application.

This table shows the automated workflows that are configured for this application.


| Workflow                                                                                                             | Triggered On                                                                 | Actions                                                                                                                                                                                                        |
|----------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Gradle build and test<br/>[gradle-build-and-test.yaml](.github/workflows/gradle-build-and-test.yaml)                 | * Push to `main` branch<br/>* Push to a PR                                   | 1. Run the Gradle Build.  Produce JUnit and test coverage reports<br/>2. Run Snyk scan on the application<br/>3. Run Synk scan on the docker image.<br/>4. Uploads the results into the Security Tab on Github |
| CodeQL<br/>[codeql-analysis.yml](.github/workflows/codeql-analysis.yml)                                              | * Push to `main` branch<br/>* Push to a PR<br/>* 05:34 every Saturday        | 1. Perform CodeQL analysis. <br/>2. Perform Dependency analysis.                                                                                                                                               |
| Build and Deploy to Non-Prod Environments<br/>[cp-deployment-branch.yml](.github/workflows/cp-deployment-branch.yml) | * Push to any branch other than `main`<br/>* Manual invocation via GitHub UI | 1. Build docker image & push to image repository<br/>2. Simultaneously deploy to `dev`, `test` & `uat` upon approval                                                                                           |
| Build and Deploy Crime Assessment Service to CP<br/>[cp-deployment.yml](.github/workflows/cp-deployment.yml)                             | * Push to `main`<br/>* Manual invocation via GitHub UI                       | 1. Build docker image & push to image repository<br/>2. Deploy to `dev` upon approval<br>3. Simultaneously deploy to `test`, `uat` & `prod` upon approval                                                      |

### Deploying Manually

The application can be deployed manually via the GitHub UI from PR branches or from the `main` branch.

1. Go to the Actions page https://github.com/ministryofjustice/laa-crime-assessment-service/actions
2. Click on either the `Build and Deploy to Non-Prod Environments` or `Build and Deploy Crime Assessment Service to CP` workflows
3. Click on the `Run workflow` button
4. Select the branch to deploy from the dropdown
5. Click on the `Run workflow` button

### View workflow logs

1. Go to the Actions page https://github.com/ministryofjustice/laa-crime-assessment-service/actions
2. Click on the workflow in question to show a list of runs
3. Click on the run you want to view to show the jobs in that run. Any failed jobs will have a red X next to them.
4. Click on any individual job to show the steps in that job
5. Drill down in to the individual step to see the logs


### Deployment issues

If a deployment fails on the Helm deployment step, it will display a simple error message and exit.
```text
Run cd helm_deploy/crime-assessment-service
Error: UPGRADE FAILED: context deadline exceeded
Error: Process completed with exit code 1.
```
Use these commands to troubleshoot the issue.

* Look at events in the namespace:

  ```kubectl get events -n laa-crime-assessment-service-<env>```

* Find pods in the environment:

  ```kubectl get pods -n laa-crime-assessment-service-<env>```

* Look at the logs for the application:

  ```kubectl logs -n laa-crime-assessment-service-<env> <pod-name>```

* Check the health and information of a pod:

  ```kubectl describe pod -n laa-crime-assessment-service-<env> <pod-name>```

## Local development

### Obtaining environment variables for running locally

To run the app locally, you will need to download the appropriate environment variables from the team
vault in 1Password. These environment variables are stored as a .env file, which docker-compose uses
when starting up the service. If you don't see the team vault, speak to your tech lead to get access.

To begin with, make sure that you have the 1Password CLI installed:

```sh
op --version
```

If the command is not found, [follow the steps on the 1Password developer docs to get the CLI set-up](https://developer.1password.com/docs/cli/get-started/).

Once you're ready to run the application:

```sh
./start-local.sh
```

### Tests

When you run the tests, Liquibase will run and set up all of the required tables, using the same ```db.changelog-master.yaml``` file as the main application. It also runs using the _test_ context, which means any migration file (e.g. 04-seed-test-ioj-appeal-data.sql) that is marked with that context will run. This will seed the test h2 database with data you can work with.

If you run tests with ```./gradlew clean test --info``` you will get log output showing the table being populated with test data.

### Spotless (Code style)

This application implements the Spotless plugin, which will alert you to any violations when you run `./gradlew build`. We are following the [AOSP Java code style](https://source.android.com/docs/setup/contribute/code-style).

### JaCoCo (Code coverage)

When you complete a `./gradlew build`, a code coverage report is generated. It will display the location of this report in the console.

### Gradle Versions Plugin

This plugin will alert you to dependencies that have updates available. It can be ran by running `./gradlew dependencyUpdates`.
