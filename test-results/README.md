# Test Results

This is a module required by [test-report-aggregation](https://docs.gradle.org/current/userguide/test_report_aggregation_plugin.html) plugin to generate test results.

Its only purpose is to aggregate test results from all subprojects and generate a single test report.
```bash
./gradlew test testAggregateTestReport
```
Then reports will be aggregated into [build folder](build/reports/tests/unit-test/aggregated-results/index.html) of this project.

## Test Reports

Go to the [docs folder](../docs/README.md) to find the actual test reports.
