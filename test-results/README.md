# Test Results

This is a module required by [test-report-aggregation](https://docs.gradle.org/current/userguide/test_report_aggregation_plugin.html) plugin to generate test results.

Its only purpose is to aggregate test results from all subprojects and generate a single test report.
```bash
./gradlew test testAggregateTestReport
```
Then reports will be aggregated into [build folder](build/reports/tests/unit-test/aggregated-results/index.html) of this project.

## Test Reports

If you want to check the latest test report and code coverage:

* Check latest [Overall Test Reports](https://htmlpreview.github.io/?https://github.com/hugogu/rt-balance/blob/test-results/test-results/build/reports/tests/unit-test/aggregated-results/index.html).
* Check [account-service coverage report](https://htmlpreview.github.io/?https://github.com/hugogu/rt-balance/blob/test-results/account-service/build/jacocoHtml/index.html)
* Check [transaction-service coverage report](https://htmlpreview.github.io/?https://github.com/hugogu/rt-balance/blob/test-results/transaction-service/build/jacocoHtml/index.html)
