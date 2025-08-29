# Database Management

## Information Enquiry

* Query all schemas in an instance.

```shell
docker-compose exec postgres psql -U postgres -d account -c "\dn"
```

* Query all tables in schema named `public`.

```shell
docker-compose exec postgres psql -U postgres -d account -c "\dt public.*"
```

* Query all columns in table named `account`.

```shell
docker-compose exec postgres psql -U postgres -d account -c "\d account"
```

## Copy table schema from master to replica

```shell
# Export table schema from master
docker-compose exec postgres pg_dump -U postgres -d account --schema-only > schema.sql

# Import table schema to replica
docker-compose exec -T postgres-replica psql -U postgres -d account < schema.sql
```

* Import table schema to replica (Windows PowerShell)
```powershell
type schema.sql | docker-compose exec -T postgres-replica psql -U postgres -d account
```

## Setup physical replication

* Set up a user with replication privilege on master.

```shell
docker-compose exec postgres psql -U postgres -d account -c "CREATE USER replicator WITH REPLICATION LOGIN ENCRYPTED PASSWORD 'replicator';"
```
* Set up a physical replication slot on master.

```shell
docker-compose exec postgres psql -U postgres -d account -c "SELECT * FROM pg_create_physical_replication_slot('physical_slot');"
# 重载主库配置：让主库重新加载配置文件，使修改生效。
systemctl reload postgresql  # 或用 pg_ctl reload
```

* Allow replication connections on master by editing `pg_hba.conf`.

```shell
docker-compose exec postgres bash -c "echo 'host replication replicator samenet md5' >> /var/lib/postgresql/data/pg_hba.conf"
docker-compose exec postgres psql -U postgres -d account -c "SELECT pg_reload_conf();"
```
* Enable necessary settings on master by editing `postgresql.conf`.

* Initialize the replica from master using pg_basebackup.

Before bringing up the postgres-slave container, you need to initialize the data directory of the replica server using `pg_basebackup`. This command connects to the master server and creates a base backup of the database cluster.
```shell
docker run -e PGPASSWORD='replicator' --rm -v "${pwd}/tmp/pgdata-slave:/var/lib/postgresql/data" --network="rt-balance_default" postgres:17-alpine pg_basebackup -h postgres -p 5432 -U replicator -D /var/lib/postgresql/data -Fp -Xs -R -P --slot=physical_slot
```
Normally, you will see something like this:
```
waiting for checkpoint
   83/31107 kB (0%), 0/1 tablespace
 6621/31107 kB (21%), 0/1 tablespace
11203/31107 kB (36%), 0/1 tablespace
15198/31107 kB (48%), 0/1 tablespace
20935/31107 kB (67%), 0/1 tablespace
25267/31107 kB (81%), 0/1 tablespace
30393/31107 kB (97%), 0/1 tablespace
31120/31120 kB (100%), 0/1 tablespace
31120/31120 kB (100%), 1/1 tablespace
```
Then you can start the postgres-slave container.

```shell
docker-compose exec postgres-slave pg_basebackup -h postgres -p 5432 -F p -D /var/lib/postgresql/data -U replicator -v -R -P --wal-method=stream --slot=physical_slot
```
The parameters used:
- `-h`: The host name of the master server.
- `-p`: The port number of the master server.
- `-F p`: Specifies the format of the output. `p` stands for plain format.
- `-D`: The directory where the backup will be stored on the replica server.
- `-U`: The username to connect to the master server.
- `-v`: Enables verbose mode, providing detailed output during the backup process.
- `-R`: Creates a `recovery.conf` file in the data directory, which is used to configure the replica server for replication.
- `-P`: Shows progress information during the backup process.
- `-X s`: Specifies how to handle WAL files. `s` stands for streaming, meaning that WAL files will be streamed to the replica server during the backup.
- `--wal-method=stream`: Specifies the method for including WAL files in the backup. `stream` means that WAL files will be streamed to the replica server during the backup.
- `--slot=physical_slot`: Specifies the name of the replication slot to use for the backup, ensuring that the master retains the necessary WAL files for the replica.

* [Optional] Allow read-only queries on the physical replica.

```shell
docker-compose exec postgres-replica bash -c "echo \"hot_standby = on\" >> /var/lib/postgresql/data/postgresql.conf"
docker-compose restart postgres-slave
```

* Check replica status on master.

```shell
docker-compose exec postgres psql -U postgres -d account -c "SELECT * FROM pg_stat_replication;"
```

* Check replica status on replica.

```shell
docker-compose exec postgres-slave psql -U postgres -d account -c "SELECT * FROM pg_stat_wal_receiver;"
docker-compose exec postgres-slave psql -U postgres -d account -c "SELECT pg_is_in_recovery();"
```


## Setup logical replication

* Turns master server to use logical replication.

```shell
# Edit postgresql.conf
docker-compose exec postgres bash -c "echo \"wal_level = logical\" >> /var/lib/postgresql/data/postgresql.conf"
docker-compose exec postgres bash -c "echo \"max_wal_senders = 10\" >> /var/lib/postgresql/data/postgresql.conf"
docker-compose exec postgres bash -c "echo \"max_replication_slots = 10\" >> /var/lib/postgresql/data/postgresql.conf"
docker-compose exec postgres bash -c "echo \"wal_keep_size = 64\" >> /var/lib/postgresql/data/postgresql.conf"
docker-compose exec postgres bash -c "echo \"hot_standby = on\" >> /var/lib/postgresql/data/postgresql.conf"
# Restart postgres
docker-compose restart postgres
```

* Setup publication on master and subscription on replica.

```shell
docker-compose exec postgres psql -U postgres -d account -c "CREATE PUBLICATION all_publication FOR ALL TABLES;"
```

* Check publication on master

```shell
docker-compose exec postgres psql -U postgres -d account -c "\dRp+"
```


* Setup subscription on replica
:memo: Please note the tables must exist on the replica before creating the subscription, otherwise it will fail.
```shell
docker-compose exec postgres-replica psql -U postgres -d account -c "CREATE SUBSCRIPTION all_subscription CONNECTION 'host=postgres port=5432 user=postgres password=postgres dbname=account' PUBLICATION all_publication;"
```

* Check subscription on replica

```shell
docker-compose exec postgres-replica psql -U postgres -d account -c "\dRs+"
```

* Disable and remove subscription on replica

```shell
docker-compose exec postgres-replica psql -U postgres -d account -c "ALTER SUBSCRIPTION account_subscription DISABLE;"
docker-compose exec postgres-replica psql -U postgres -d account -c "DROP SUBSCRIPTION account_subscription;"
```

* 

