# Realtime Balance System ![CICD Status](https://github.com/hugogu/rt-balance/actions/workflows/build-and-test.yml/badge.svg)

You may need to refer to 
* [Development Environment](./docs/DevelopmentEnvironment.md) for setting up the local environment.
* [Architecture Document](docs/Architecture.md) to get to know the overall architecture and key tradeoffs made.

## Build

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

## Deploy

### Helm Chart Deployment

Refer to the [Helm Chart Readme](./helm-chart/README.md) for more details.

### Docker-Compose Deployment

Having images get built successfully. You can bring up services by running. 

```bash
docker-compose up -d account-service-api transaction-service-api
```

## Test

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
./gradlew :account-service:bootRun
./gradlew :transaction-service:test jacocoTestReport testAggregateTestReport -DincludeTags=require-account,integration
```

#### Test Reports

* Check latest [Overall Test Reports](https://htmlpreview.github.io/?https://github.com/hugogu/rt-balance/blob/test-results/test-results/build/reports/tests/unit-test/aggregated-results/index.html). 
* Check [account-service coverage report](https://htmlpreview.github.io/?https://github.com/hugogu/rt-balance/blob/test-results/app-account-service/build/jacocoHtml/index.html)
* Check [transaction-service coverage report](https://htmlpreview.github.io/?https://github.com/hugogu/rt-balance/blob/test-results/app-transaction-service/build/jacocoHtml/index.html)

:warning: Please note this requires committing reports in `build` to `test-results` branch of this repo.

### Stress Test
Please refer to the [load-testing](./load-testing/README.md) for usage information & benchmark result.
