#!/bin/bash
set -e

# 等待PostgreSQL启动
until pg_isready -U postgres; do
  echo "等待PostgreSQL启动..."
  sleep 1
done

# 创建复制用户
echo "Creating replication user..."
psql -v ON_ERROR_STOP=1 -U postgres -d account <<-EOSQL
    CREATE USER replicator WITH REPLICATION LOGIN ENCRYPTED PASSWORD 'replicator';
EOSQL

# 创建物理复制槽
echo "Creating physical replication slot..."
psql -v ON_ERROR_STOP=1 -U postgres -d account <<-EOSQL
    SELECT * FROM pg_create_physical_replication_slot('physical_slot');
EOSQL

# 修改pg_hba.conf允许复制连接
echo "Updating pg_hba.conf..."
echo "host replication replicator samenet md5" >> /var/lib/postgresql/data/pg_hba.conf

# 修改postgresql.conf启用逻辑复制
echo "Updating postgresql.conf..."
cat >> /var/lib/postgresql/data/postgresql.conf <<EOL
# 复制配置
wal_level = logical
max_wal_senders = 10
max_replication_slots = 10
wal_keep_size = 64
hot_standby = on
EOL

# 重新加载配置
echo "Reloading configuration..."
pg_ctl reload

# 创建发布
echo "Creating publication for logical replication..."
psql -v ON_ERROR_STOP=1 -U postgres -d account <<-EOSQL
    CREATE PUBLICATION all_publication FOR ALL TABLES;
EOSQL

echo "PostgreSQL主库初始化完成"
