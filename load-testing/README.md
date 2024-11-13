# Stress Test Advanced Suite

## Pre-requisites

* Install [Docker Desktop](https://www.docker.com/products/docker-desktop/)
* Install [Docker Compose](https://docs.docker.com/compose/install/)

## Problem Statement

The official release of K6 does not support InfluxDB V2 output plugin. 
Need to build a customized k6 executable with InfluxDB output plugin.

This component can be a shared library for all projects that need to run load testing with InfluxDB.

## How to use

1. Build a customized k6 executable with InfluxDB output plugin. (:warning: This may require VPN to download images)
    ```shell
    docker build --build-arg PLUGIN=grafana/xk6-output-influxdb -t k6-influxdb ./load-testing/k6
    ```
1. Or you can build k6 binary in local (Requires go installed)
   ```shell
   go install go.k6.io/xk6/cmd/xk6@latest
   export PATH=$(go env GOPATH)/bin:$PATH
   xk6 build --with "github.com/grafana/xk6-output-influxdb" --output /tmp/k6
   ```
1. Bring up prometheus, influxdb and grafana services.
    ```shell
    docker-compose up -d
    ```
1. Execute test scripts in `./scripts` directory. You can update the scripts as needed.
   ```shell
    /tmp/k6 run -o xk6-influxdb  \
    -e K6_INFLUXDB_ORGANIZATION='Hugo' \
    -e K6_INFLUXDB_BUCKET='k6' -e K6_INFLUXDB_TOKEN='secret_token' - < ./load-testing/scripts/transaction-stress.js
   ```
   OR run from docker (:warning: need to update the address in script to make it accessible from local)
   ```shell
   docker run --network="host" --rm -i k6-influxdb run -o xk6-influxdb \
   -e K6_INFLUXDB_ORGANIZATION='Hugo' \
   -e K6_INFLUXDB_BUCKET='k6' -e K6_INFLUXDB_TOKEN='secret_token' - < ./load-testing/scripts/create-account-stress.js
   ```
1. Check test result and performance metrics.
   * Access [built-in dashboard](http://localhost:3000/d/dba00ead-0f0a-4c1d-a3f6-505d886ab946/k6-built-in-load-testing-results?orgId=1&refresh=5s)
      * Access Prometheus: http://localhost:9090
      * Access InfluxDB: http://localhost:8086
        * Login with root/password
   * [Optional] Import new dashboard from Grafana
     * [K6-Prometheus dashboard](https://grafana.com/grafana/dashboards/19665-k6-prometheus/)
     * [K6 Load Testing Result dashboard](https://grafana.com/grafana/dashboards/2587-k6-load-testing-results/)
   * Import more useful dashboards from [Grafana marketplace](https://grafana.com/grafana/dashboards/).
