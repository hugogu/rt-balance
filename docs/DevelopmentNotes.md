# Development Notes
  
## Setup CDC for account table

Debezium was used to capture the changes in the account table. The connector configuration is stored in the `debezium-connector-config.json` file.
When the account service is up, an initial setup of the connector is required by running the following command:

Please note: the `slot.name` of each connector should be unique.

```shell
curl -X POST -H "Content-Type: application/json" --data @config/debezium-connector-config.json http://localhost:8083/connectors
curl -X POST -H "Content-Type: application/json" --data @config/outbox-connector-config.json http://localhost:8083/connectors
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

Command to delete a connector:

```shell
curl -X DELETE http://localhost:8083/connectors/account-outbox-connector
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

## Setup Oracle Data Guard

```shell
docker-compose up -d oracle-master oracle-slave redis kafka grafana influxdb
```

* [解决Oracle数据库在Docker容器中安装后无法重启的常见问题与技巧](https://www.oryoy.com/news/jie-jue-oracle-shu-ju-ku-zai-docker-rong-qi-zhong-an-zhuang-hou-wu-fa-zhong-qi-de-chang-jian-wen-ti.html)

Connects to oracle:
```shell
sqlplus ODBA/5208@localhost:1521/orcl
sqlplus SYS/5208@localhost:1521/orcl AS SYSDBA
```
或者：
```shell
su - oracle
sqlplus / as sysdba
```

在容器化环境中，需要配置`/etc/oratab`文件，将`orcl`的值设置为`Y`(如果已经存在侧改写)。
```shell
echo 'orcl:/opt/oracle/app/product/11.2.0/dbhome_1:Y' > /etc/oratab
```

Check if ARCHIVELOG is enabled:
```sql
SELECT log_mode FROM v$database;
```
If not enabled, need to enable it:
```oraclesqlplus
SHUTDOWN IMMEDIATE;
STARTUP MOUNT;
ALTER DATABASE ARCHIVELOG;
ALTER DATABASE OPEN;
```


```sql
-- 在主库上查询当前日志序列号
SELECT sequence#, first_time, next_time FROM v$log WHERE status = 'CURRENT';

-- 在从库上查询应用的日志序列号
SELECT sequence#, applied FROM v$archived_log ORDER BY sequence#;

-- 检查主从复制延迟
SELECT ROUND((SYSDATE - MAX(first_time))*24*60,1) DELAY_MINS 
FROM v$archived_log WHERE applied = 'YES';

-- 查看从库的应用进度
SELECT thread#, sequence#, applied, completion_time  
FROM v$archived_log 
ORDER BY sequence# DESC;

-- 在主库上查看Data Guard配置
SELECT db_unique_name, database_role, open_mode FROM v$database;

-- 检查Data Guard传输状态
SELECT * FROM v$dataguard_stats;

-- 查看standby日志应用状态
SELECT process, status, thread#, sequence#, block#, blocks 
FROM v$managed_recovery_progress;

-- 检查保护模式
SELECT protection_mode, protection_level FROM v$database;
```

Docker中的Oracle的安装位置是`/opt/oracle/app/product/11.2.0/dbhome_1/`，查询状态
```shell
docker exec -it real-balance-oracle-master-1 /opt/oracle/app/product/11.2.0/dbhome_1/bin/lsnrctl status
```
```shell
docker exec -it real-balance-oracle-master-1 /opt/oracle/app/product/11.2.0/dbhome_1/bin/lsnrctl status
```

```shell
docker exec -it real-balance-oracle-slave-1 /opt/oracle/app/product/11.2.0/dbhome_1/bin/rman target /
```

* Check if a node is running as standby
```oraclesqlplus
SELECT DATABASE_ROLE FROM V$DATABASE;
```

```shell
./rman target SYS/5208@localhost:1521/orcl 
RESTORE CONTROLFILE FROM '/u01/app/oracle/standby.ctl';

ALTER DATABASE MOUNT STANDBY DATABASE;
ALTER DATABASE OPEN READ ONLY;

ALTER DATABASE RECOVER MANAGED STANDBY DATABASE CANCEL;
ALTER DATABASE RECOVER MANAGED STANDBY DATABASE DISCONNECT FROM SESSION;
ALTER DATABASE RECOVER MANAGED STANDBY DATABASE USING CURRENT LOGFILE DISCONNECT;
```

```oraclesqlplus
SELECT PROCESS, STATUS, SEQUENCE# FROM V$MANAGED_STANDBY;
SELECT SEQUENCE#, APPLIED FROM V$ARCHIVED_LOG ORDER BY SEQUENCE#;
SELECT MAX(SEQUENCE#) AS LAST_APPLIED_LOG FROM V$LOG_HISTORY;
```

## Useful commands

```shell
docker exec -it real-balance-oracle-master-1 bash
echo 'export ORACLE_HOME=/opt/oracle/app/product/11.2.0/dbhome_1' >> /etc/profile
```

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
