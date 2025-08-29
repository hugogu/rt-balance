#!/bin/bash
set -e

# 等待PostgreSQL启动
until pg_isready -U postgres; do
  echo "等待逻辑复制副本PostgreSQL启动..."
  sleep 1
done

# 等待主库可用
until pg_isready -h postgres -U postgres; do
  echo "等待主库PostgreSQL可用..."
  sleep 3
done

echo "等待主库初始化完成..."
sleep 5

# 导入表结构 (pg_dump无法在这个脚本中直接调用，需要预先生成schema.sql或通过其他方式获取)
# 可以使用docker-compose中的healthcheck机制确保主库已初始化完成

# 创建订阅前确保表结构已存在
echo "从主库导入表结构..."
PGPASSWORD=postgres pg_dump -h postgres -U postgres -d account --schema-only | psql -U postgres -d account

# 创建订阅
echo "创建订阅..."
psql -v ON_ERROR_STOP=1 -U postgres -d account <<-EOSQL
  CREATE SUBSCRIPTION all_subscription 
  CONNECTION 'host=postgres port=5432 user=postgres password=postgres dbname=account' 
  PUBLICATION all_publication;
EOSQL

echo "PostgreSQL逻辑复制副本初始化完成"
