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

### Spotless (Code style)

This application implements the Spotless plugin, which will alert you to any violations when you run `./gradlew build`. We are following the [AOSP Java code style](https://source.android.com/docs/setup/contribute/code-style).

### JaCoCo (Code coverage)

When you complete a `./gradlew build`, a code coverage report is generated. It will display the location of this report in the console.

### Gradle Versions Plugin

This plugin will alert you to dependencies that have updates available. It can be ran by running `./gradlew dependencyUpdates`.
