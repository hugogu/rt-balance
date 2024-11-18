# RT Balance System Documentation

This folder contains the documentation for the Realtime Balance System.

## Table of Contents

* [System Architecture](./Architecture.md)
* [Development Environment Setup](./DevelopmentEnvironment.md)
  * [Helm Chart Deployment](../helm-chart/README.md)
* Test Reports
  * Unit Test & Integration Test with Coverage 
    * [Unit&Integration Test Reports](https://htmlpreview.github.io/?https://github.com/hugogu/rt-balance/blob/test-results/test-results/build/reports/tests/unit-test/aggregated-results/index.html).
    * [account-service Coverage Report](https://htmlpreview.github.io/?https://github.com/hugogu/rt-balance/blob/test-results/app-account-service/build/jacocoHtml/index.html): 83% line coverage, 66% branch coverage.
    * [transaction-service Coverage Report](https://htmlpreview.github.io/?https://github.com/hugogu/rt-balance/blob/test-results/app-transaction-service/build/jacocoHtml/index.html): 77% line coverage, 62% branch coverage.
    * [lib-common Coverage Report](https://htmlpreview.github.io/?https://github.com/hugogu/rt-balance/blob/test-results/lib-common/build/jacocoHtml/index.html): 79% line coverage, 58% branch coverage.
  * [Performance Benchmark Report](./PerformanceBenchmark.md)
  * [Resilience Test Report](./ResilienceTestReport.md)
* Style Check Reports
  * [Account Service Detekt Report](https://htmlpreview.github.io/?https://github.com/hugogu/rt-balance/blob/test-results/app-account-service/build/reports/detekt/detekt.html)
  * [Transaction Service Detekt Report](https://htmlpreview.github.io/?https://github.com/hugogu/rt-balance/blob/test-results/app-transaction-service/build/reports/detekt/detekt.html)

Please note these reports were executed & generated locally and committed into `test-results` branch of this repo. 
