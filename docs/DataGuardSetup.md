# Setup Data Guard

## Initial Setup

状态检查
```oraclesqlplus
-- 确认主库处于归档模式
SELECT log_mode FROM v$database;
-- 首先查看主库的日志组大小和数量
SELECT GROUP#, BYTES/1024/1024 MB, MEMBERS FROM V$LOG;

-- 确认从库状态
-- 检查应用进程状态
SELECT PROCESS, STATUS, SEQUENCE#, BLOCK# FROM V$MANAGED_STANDBY;

-- 查看最近应用的日志序列号
SELECT SEQUENCE#, NAME, APPLIED FROM V$ARCHIVED_LOG ORDER BY SEQUENCE# DESC;

-- 检查是否有未应用的日志
SELECT COUNT(*) FROM V$ARCHIVED_LOG WHERE APPLIED = 'NO';

-- 确认standby redo logs是否创建成功
SELECT GROUP#, THREAD#, BYTES/1024/1024 MB FROM V$STANDBY_LOG;

-- 4. 检查从库角色和打开模式
SELECT DATABASE_ROLE, OPEN_MODE FROM V$DATABASE;
-- 应显示为PHYSICAL STANDBY，以及READ ONLY或MOUNTED

-- 5. 确认是否使用了实时应用模式
SELECT RECOVERY_MODE FROM V$ARCHIVE_DEST_STATUS WHERE DEST_ID=1;
```

主库启动归档
```oraclesqlplus
-- 如果不是，需要切换到归档模式
SHUTDOWN IMMEDIATE;
STARTUP MOUNT;
ALTER DATABASE ARCHIVELOG;
ALTER DATABASE OPEN;

-- 强制切换日志，生成归档
ALTER SYSTEM SWITCH LOGFILE;

-- 创建备用控制文件
ALTER DATABASE CREATE STANDBY CONTROLFILE AS '/tmp/standby.ctl';
```
复制控制文件到从库
```shell
# 从主库容器复制控制文件到宿主机
docker cp rt-balance-oracle-master-1:/tmp/standby.ctl ./tmp

# 从宿主机复制到从库容器
docker cp ./tmp/standby.ctl rt-balance-oracle-slave-1:/tmp/
```

从库同步主库数据
```oraclesqlplus
SHUTDOWN IMMEDIATE;
-- 启动到NOMOUNT状态
STARTUP NOMOUNT;
-- 退出sqlplus
exit;
```
```shell
# 使用RMAN
rman target /

# 在RMAN中执行
RESTORE CONTROLFILE FROM '/tmp/standby.ctl';
exit

# 重新进入sqlplus
sqlplus / as sysdba
```

```oraclesqlplus
-- 挂载为standby
ALTER DATABASE MOUNT STANDBY DATABASE;
ALTER DATABASE RECOVER MANAGED STANDBY DATABASE DISCONNECT;

-- 使用实时应用模式启动恢复
ALTER DATABASE RECOVER MANAGED STANDBY DATABASE USING CURRENT LOGFILE DISCONNECT;
-- 检查恢复状态
SELECT PROCESS, STATUS FROM V$MANAGED_STANDBY;
```

```oraclesqlplus
-- 假设主库有3个redo log组，每组100MB
ALTER DATABASE ADD STANDBY LOGFILE GROUP 4 SIZE 50M;
ALTER DATABASE ADD STANDBY LOGFILE GROUP 5 SIZE 50M;
ALTER DATABASE ADD STANDBY LOGFILE GROUP 6 SIZE 50M;
ALTER DATABASE ADD STANDBY LOGFILE GROUP 7 SIZE 50M; -- 额外添加一个
```


在主库配置日志传输服务
```oraclesqlplus
ALTER SYSTEM SET LOG_ARCHIVE_DEST_2='SERVICE=orcl_standby ASYNC VALID_FOR=(ONLINE_LOGFILES,PRIMARY_ROLE) DB_UNIQUE_NAME=orcl_standby';
ALTER SYSTEM SET LOG_ARCHIVE_DEST_STATE_2=ENABLE;
-- 验证参数设置
SHOW PARAMETER LOG_ARCHIVE_DEST_2;
-- 检查状态是否从INACTIVE变为VALID
SELECT DEST_ID, STATUS, ERROR FROM V$ARCHIVE_DEST WHERE DEST_ID = 2;
```

```shell
# 在主库容器中测试到从库的连接
tnsping orcl_standby
```



在从库上打开只读模式
```oraclesqlplus
-- 在从库上执行
ALTER DATABASE RECOVER MANAGED STANDBY DATABASE CANCEL;
ALTER DATABASE OPEN READ ONLY;
-- 这样配置后，从库可以用于查询，但不会继续接收和应用日志，导致逐渐落后于主库
```

```oraclesqlplus
-- 在从库上执行
ALTER DATABASE RECOVER MANAGED STANDBY DATABASE CANCEL;
ALTER DATABASE OPEN READ ONLY;
ALTER DATABASE RECOVER MANAGED STANDBY DATABASE USING CURRENT LOGFILE DISCONNECT;
-- 这种配置使用了Real-Time Apply特性，让从库在保持READ ONLY状态的同时继续应用来自主库的更改
```

