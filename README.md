# Realtime Balance System ![CICD Status](https://github.com/hugogu/rt-balance/actions/workflows/build-and-test.yml/badge.svg)

You may need to refer to 
* [Development Environment](./docs/DevelopmentEnvironment.md) for setting up the local environment.
* [Architecture Document](docs/Architecture.md) to get to know the overall architecture and key tradeoffs made.

## Project Structure

* **app-account-service**: Account Service Module, the core service module of this project.
* **app-transaction-service**: Transaction Service Module, a sample client of the Account Service.
* **helm-chart**: Helm Chart for deploying the services into Kubernetes.
* **docs**: Documentation for the project.
* **test-results**: An empty module used to aggregate Test reports of all submodules.
* **load-testing**: Load test toolset and scripts and also used to generate mock data.
* **lib-common**: Common library shared by all services.

## How to Build

:warning: **Having docker & minikube running is a prerequisite for building the docker image.**

* Build the docker image. :note: Minikube itself used a separate docker context, so the image needs to be built twice.
    ```bash
    # For docker-compose
    ./gradlew :app-account-service:bootBuildImage
    ./gradlew :app-transaction-service:bootBuildImage
    # For minikube
    eval $(minikube docker-env)
    ./gradlew :app-account-service:bootBuildImage
    ./gradlew :app-transaction-service:bootBuildImage
    ```
  
:warning: The image built is for amd64 architecture, it may not perform well for Mac Apple Silicon Chipset users. Spring Boot 3.4 will provide a native support to it.

## How to Deploy

### Helm Chart Deployment

Refer to the [Helm Chart Readme](./helm-chart/README.md) for more details.

### Docker-Compose Deployment

Having images get built successfully. You can bring up services by running. 

```bash
docker-compose up -d account-service-api transaction-service-api
```

## How to config

Refer to the [Development Notes](./docs/DevelopmentNotes.md) for more details.

## How to Test

### Unit Test & Integration Test

* To run all unit test
```bash
./gradlew test jacocoTestReport testAggregateTestReport -DexcludeTags=integration
```
* To run all integration test
```bash
# Bring up local environment
docker-compose up -d postgres redis kafka
./gradlew test jacocoTestReport testAggregateTestReport -DincludeTags=integration
# Open test report
open test-results/build/reports/tests/unit-test/aggregated-results/index.html
```
* To run dependent integration test (some tests in transaction-service requires account-service to be running)
```bash
./gradlew :app-account-service:bootRun
./gradlew :app-transaction-service:test jacocoTestReport testAggregateTestReport -DincludeTags=require-account,integration
```

### Test Reports

Refer to the [docs folder](./docs/README.md) for test reports.

## Special Branches

There are several special branches in this repo for special use.

* **test-results**: Branch to store test results generated during build and make it accessible directly in GitHub without build.
* **chaos-monkey**: Branch to with chaos monkey framework integrated. The build made here shouldn't be used in production. 

They should be removed when there is more appropriate way to make them.
