FROM oraclelinux:7-slim

# 替换yum源为阿里源
# RUN if [ -f /etc/yum.repos.d/public-yum-ol7.repo ]; then mv /etc/yum.repos.d/public-yum-ol7.repo /etc/yum.repos.d/public-yum-ol7.repo.bak; fi && \
#    curl -o /etc/yum.repos.d/CentOS-Base.repo http://mirrors.aliyun.com/repo/Centos-7.repo && \
#    yum clean all && \
#    yum makecache

# 添加 Oracle repository
RUN yum install -y oracle-release-el7 && \
    yum-config-manager --enable ol7_oracle_instantclient

# 安装 Oracle 11g 所需的包和依赖项
# RUN yum -y install oracle-database-preinstall-11g
RUN yum -y install oracle-database-11g
RUN yum clean all

# 设置环境变量
ENV ORACLE_BASE=/opt/oracle \
    ORACLE_HOME=/opt/oracle/product/11.2.0/dbhome_1 \
    ORACLE_SID=ORCL \
    PATH=$PATH:/opt/oracle/product/11.2.0/dbhome_1/bin

# 暴露端口
EXPOSE 1521 5500

# 设置数据库
RUN /opt/oracle/scripts/setup/setup.sh

# 启动数据库
CMD ["/opt/oracle/scripts/start/start.sh"]
