# Development Notes
  
## Setup CDC for account table

Debezium was used to capture the changes in the account table. The connector configuration is stored in the `debezium-connector-config.json` file.
When the account service is up, an initial setup of the connector is required by running the following command:

```shell
curl -X POST -H "Content-Type: application/json" --data @config/debezium-connector-config.json http://localhost:8083/connectors
```

Then you can check the status of the connector on [this page](http://localhost:8083/connectors/account-changes-connector/status).

Command to restart the connector:

```shell
curl -X POST http://localhost:8083/connectors/account-changes-connector/restart
```

Command to pause the connector:

```shell
curl -X PUT http://localhost:8083/connectors/account-changes-connector/pause
```

In order to update the existing connector after the connector is created, the following command:

```shell
curl -X PUT -H "Content-Type: application/json" --data '{
  "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
  "tasks.max": "1",
  "database.hostname": "postgres",
  "database.port": "5432",
  "database.user": "postgres",
  "database.password": "postgres",
  "database.dbname": "account",
  "database.server.name": "database.acct",
  "plugin.name": "pgoutput",
  "slot.name": "debezium_account",
  "publication.name": "account_changes",
  "topic.prefix": "changes.account",
  "key.converter": "org.apache.kafka.connect.json.JsonConverter",
  "value.converter": "org.apache.kafka.connect.json.JsonConverter",
  "key.converter.schemas.enable": "false",
  "value.converter.schemas.enable": "false"
}' http://localhost:8083/connectors/account-changes-connector/config
```

## Useful commands

* Check Docker context
    ```bash
    docker context ls
    ```
* Set Docker context
    ```bash
    docker context use default
    eval $(minikube docker-env)
    ```

## Key References

* [Kubernetes Metrics Server](https://github.com/kubernetes-sigs/metrics-server/tree/master/charts/metrics-server)
* [Ingress-Nginx](https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/)
