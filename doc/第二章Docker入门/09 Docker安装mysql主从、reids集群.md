前提

1.关闭防火墙：

```shell
systemctl stop firewalld
```

2.启动docker

```shell
systemctl start docker
```

# 安装mysql主从复制（一主一从）

主从配置：

| ip              | port | 角色   |
| --------------- | ---- | ------ |
| 192.168.122.140 | 3307 | master |
| 192.168.122.140 | 3308 | slave  |

## 新建主服务器容器实例3307

容器实例mysql-master

```shell
docker run -p 3307:3306 --name mysql-master --privileged=true \
-v /mydata/mysql-master/log:/var/log/mysql \
-v /mydata/mysql-master/data:/var/lib/mysql \
-v /mydata/mysql-master/conf:/etc/mysql \
-v /mydata/mysql-master/mysql-files:/var/lib/mysql-files  \
-e MYSQL_ROOT_PASSWORD=root \
-d mysql:8.0.20
```



### 1.进入/mydata/mysql-master/conf目录下新建my.cnf

```shell
cd /mydata/mysql-master/conf
vim my.cnf
```

内容：

```shell
[mysqld]
## 设置server_id，同一局域网中需要唯一
server_id=101 
## 指定不需要同步的数据库名称
binlog-ignore-db=mysql  
## 开启二进制日志功能
log-bin=mall-mysql-bin  
## 设置二进制日志使用内存大小（事务）
binlog_cache_size=1M  
## 设置使用的二进制日志格式（mixed,statement,row）
binlog_format=mixed  
## 二进制日志过期清理时间。默认值为0，表示不自动清理。
expire_logs_days=7  
## 跳过主从复制中遇到的所有错误或指定类型的错误，避免slave端复制中断。
## 如：1062错误是指一些主键重复，1032错误是因为主从数据库数据不一致
slave_skip_errors=1062
##解决中文乱码问题
collation_server = utf8_general_ci
character_set_server = utf8
[client]
default_character_set=utf8
```



![image-20240408112904828](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081618086.png)

### 2.修改完配置后重启master实例

```shell
docker restart mysql-master
```

![image-20240408095649703](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081618734.png)

### 3.进入mysql-master容器并连接mysql

```shell
docker exec -it mysql-master /bin/bash
```

![image-20240408095727669](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081618604.png)

### 4.master容器实例内创建数据同步的slave用户

设置了从服务器所需的基本权限和用户

```shell
CREATE USER 'slave'@'%' IDENTIFIED BY '123456';
GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'slave'@'%';
```

![image-20240408095926158](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081618918.png)

### 5.navicat远程连接问题

设置权限（为root分配权限，以便可以远程连接）

由于Mysql5.6以上的版本修改了Password算法，这里需要更新密码算法，便于使用Navicat连接

```sql
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'root';
ALTER USER 'slave'@'%' IDENTIFIED WITH mysql_native_password BY '123456';
FLUSH PRIVILEGES;
```



## 新建从服务器容器实例3308

新开启一个会话执行新建从服务器操作

容器实例mysql-slave

```shell
docker run -p 3308:3306 --name mysql-slave --privileged=true \
-v /mydata/mysql-slave/log:/var/log/mysql \
-v /mydata/mysql-slave/data:/var/lib/mysql \
-v /mydata/mysql-slave/conf:/etc/mysql \
-v /mydata/mysql-slave/mysql-files:/var/lib/mysql-files  \
-e MYSQL_ROOT_PASSWORD=root  \
-d mysql:8.0.20
```

![image-20240408100121817](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081618320.png)

### 1.进入/mydata/mysql-slave/conf目录下新建my.cnf

```
cd /mydata/mysql-slave/conf
vim my.cnf
```



```shell
[mysqld]
## 设置server_id，同一局域网中需要唯一
server_id=102
## 指定不需要同步的数据库名称
binlog-ignore-db=mysql
## 开启二进制日志功能，以备Slave作为其它数据库实例的Master时使用
log-bin=mall-mysql-slave1-bin
## 设置二进制日志使用内存大小（事务）
binlog_cache_size=1M
## 设置使用的二进制日志格式（mixed,statement,row）
binlog_format=mixed
## 二进制日志过期清理时间。默认值为0，表示不自动清理。
expire_logs_days=7
## 跳过主从复制中遇到的所有错误或指定类型的错误，避免slave端复制中断。
## 如：1062错误是指一些主键重复，1032错误是因为主从数据库数据不一致
slave_skip_errors=1062
## relay_log配置中继日志
relay_log=mall-mysql-relay-bin
## log_slave_updates表示slave将复制事件写进自己的二进制日志
log_slave_updates=1
## slave设置为只读（具有super权限的用户除外）
read_only=1
##解决中文乱码问题
collation_server = utf8_general_ci
character_set_server = utf8
[client]
default_character_set=utf8                  
```



![image-20240408113303127](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081617416.png)

### 2.修改完配置后重启slave实例

```shell
docker restart mysql-slave
```

![image-20240408100323403](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081617159.png)



### 3.在主数据库mysql-master中查看主从同步状态

```shell
show master status;
```

配置中忽略了mysql库的同步

![image-20240408100512970](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081617704.png)

接下来配置主从

### 4.进入mysql-slave容器

```shell
docker exec -it mysql-slave /bin/bash
```

![image-20240408100612460](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081617105.png)

### 5.在从数据库中配置主从复制

主从关系建立

```sql
change master to master_host='192.168.122.140', master_user='slave', master_password='123456', master_port=3307, master_log_file='mall-mysql-bin.000001', master_log_pos=712, master_connect_retry=30;
```

master_host：主数据库的IP地址；

master_port：主数据库的运行端口；

master_user：在主数据库创建的用于同步数据的用户账号；

master_password：在主数据库创建的用于同步数据的用户密码；

master_log_file：指定从数据库要复制数据的日志文件，通过查看主数据的状态，获取File参数；

master_log_pos：指定从数据库从哪个位置开始复制数据，通过查看主数据的状态，获取Position参数；

master_connect_retry：连接失败重试的时间间隔，单位为秒。

![image-20240408100824237](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081617406.png)

### 6.在从数据库中查看主从同步状态

```sql
show slave status \G;
```

master_port:3307

![image-20240408101012028](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081617006.png)

### 7.在从数据库中开启主从同步

从主从同步状态看到从数据库还没有开启主从同步 

![image-20240408101826965](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081617062.png)

开启

```sql
start slave;
```

![image-20240408101909621](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081617961.png)

### 8.查看从数据库状态发现已经同步

![image-20240408103806200](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081617316.png)

## 主从复制测试

### 1.主机mysql-master新建库-使用库-新建表-插入数据

```shell
mysql> create database db01;
Query OK, 1 row affected (0.05 sec)

mysql> use db01;
Database changed
mysql> create table user(id int,name varchar(20));
Query OK, 0 rows affected (0.04 sec)

mysql> insert into user values(1, 'zhangsan');
Query OK, 1 row affected (0.03 sec)

```

### 2.从机mysql-slave使用库-查看记录

![image-20240408104236140](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081617769.png)

### 查看是否存在中文乱码问题

主机mysql-master

![image-20240408113516853](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081617087.png)

主机mysql-master查看

![image-20240408113536594](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081617219.png)



从机mysql-master查看

![image-20240408113556312](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081617253.png)

# 安装redis集群（3主3从cluster集群模式）

3主3从集群配置：

| ip              | port | 角色    |
| --------------- | ---- | ------- |
| 192.168.122.140 | 6381 | master1 |
| 192.168.122.140 | 6382 | master2 |
| 192.168.122.140 | 6383 | master3 |
| 192.168.122.140 | 6384 | slave1  |
| 192.168.122.140 | 6385 | slave2  |
| 192.168.122.140 | 6386 | slave3  |

## 搭建3主3从redis集群

### 新建6个docker容器redis实例

依次执行：

```shell
docker run -d --name redis-node-1 --net host --privileged=true -v /data/redis/share/redis-node-1:/data redis:6.0.8 --cluster-enabled yes --appendonly yes --port 6381

docker run -d --name redis-node-2 --net host --privileged=true -v /data/redis/share/redis-node-2:/data redis:6.0.8 --cluster-enabled yes --appendonly yes --port 6382

docker run -d --name redis-node-3 --net host --privileged=true -v /data/redis/share/redis-node-3:/data redis:6.0.8 --cluster-enabled yes --appendonly yes --port 6383

docker run -d --name redis-node-4 --net host --privileged=true -v /data/redis/share/redis-node-4:/data redis:6.0.8 --cluster-enabled yes --appendonly yes --port 6384

docker run -d --name redis-node-5 --net host --privileged=true -v /data/redis/share/redis-node-5:/data redis:6.0.8 --cluster-enabled yes --appendonly yes --port 6385

docker run -d --name redis-node-6 --net host --privileged=true -v /data/redis/share/redis-node-6:/data redis:6.0.8 --cluster-enabled yes --appendonly yes --port 6386
```

命令解释

docker run      创建并运行docker容器实例

--name redis-node    容器名字

--net host   使用宿主机的IP和端口，默认

--privileged=true    获取宿主机root用户权限

-v /data/redis/share/redis-node:/data    容器卷，宿主机地址:docker内部地址

redis:6.0.8   redis镜像和版本号

--cluster-enabled yes    开启redis集群

--appendonly yes   开启持久化

--port 6386   redis端口号

![image-20240408133716268](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081617116.png)

### 进入容器redis-node-1并为6台机器构建集群关系

进入容器

```shell
docker exec -it redis-node-1 /bin/bash
```

构建主从关系  注意使用自己的真实IP地址

```shell
redis-cli --cluster create 192.168.122.140:6381 192.168.122.140:6382 192.168.122.140:6383 192.168.122.140:6384 192.168.122.140:6385 192.168.122.140:6386 --cluster-replicas 1
```

--cluster-replicas 1 表示为每个master创建一个slave节点

构建主从关系并分配槽位：

![image-20240408134113136](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081617540.png)

当前主从关系

| 192.168.122.140:6381   master | 192.168.122.140:6384  slave |
| ----------------------------- | --------------------------- |
| 192.168.122.140:6382   master | 192.168.122.140:6385  slave |
| 192.168.122.140:6383   master | 192.168.122.140:6386  slave |

### 链接进入6381作为切入点，查看集群状态

没有设置密码，直接连接

```shell
root@dongguo:/data# redis-cli -p 6381

127.0.0.1:6381> cluster info
cluster_state:ok
cluster_slots_assigned:16384
cluster_slots_ok:16384
cluster_slots_pfail:0
cluster_slots_fail:0
cluster_known_nodes:6
cluster_size:3
cluster_current_epoch:6
cluster_my_epoch:1
cluster_stats_messages_ping_sent:350
cluster_stats_messages_pong_sent:363
cluster_stats_messages_sent:713
cluster_stats_messages_ping_received:358
cluster_stats_messages_pong_received:350
cluster_stats_messages_meet_received:5
cluster_stats_messages_received:713

127.0.0.1:6381> cluster nodes
288b5b2d6bbbc4a270006163384fabfd820e7c23 192.168.122.140:6382@16382 master - 0 1712555215000 2 connected 5461-10922
f3e0399d93ccdb31815b9c87a52d6c83fe033544 192.168.122.140:6383@16383 master - 0 1712555215536 3 connected 10923-16383
7a193c2e01f549cd0e9b3033ac8ea4998713e526 192.168.122.140:6386@16386 slave f3e0399d93ccdb31815b9c87a52d6c83fe033544 0 1712555216541 3 connected
22ca25b0ac4b91f4278a02ae7cc5ce040951319f 192.168.122.140:6385@16385 slave 288b5b2d6bbbc4a270006163384fabfd820e7c23 0 1712555214000 2 connected
091c4b0824e44453095f0bc53e98dcf5f25f8d28 192.168.122.140:6384@16384 slave a9ab4aa87f753108b946a78dec37027c8e46466e 0 1712555213000 1 connected
a9ab4aa87f753108b946a78dec37027c8e46466e 192.168.122.140:6381@16381 myself,master - 0 1712555213000 1 connected 0-5460
```

cluster info：获取整个集群的状态信息

- `cluster_state`：集群的当前状态，通常显示为 "ok" 如果集群运行正常。
- `cluster_slots_assigned`：已分配给集群节点的槽位数量。
- `cluster_slots_ok`：正常运行的槽位数量。
- `cluster_slots_pfail`：可能失败的槽位数量（由于与节点通信问题）。
- `cluster_slots_fail`：运行失败的槽位数量。
- `cluster_known_nodes`：集群中已知的节点数量。
- `cluster_size`：集群中活跃的主节点数量。
- `cluster_current_epoch`：集群当前的纪元（epoch），用于标识集群状态变更的版本。

cluster nodes：查看集群中所有节点的信息

1. **节点ID**：唯一标识一个节点的字符串。
2. **IP地址和端口号**：节点监听的IP地址和端口号，客户端可以通过这些信息连接到节点。
3. **标志位（flags）**：描述节点状态的标志位，可能包括 `myself`（当前连接的节点），`master`（主节点），`slave`（从节点），`fail?`（可能失败的节点），`fail`（已失败的节点），`handshake`（正在握手加入集群的节点），`noaddr`（不知道节点地址），`noflags`（无特殊标志）等。
4. **主节点ID**：如果该节点是从节点，这里会显示它所复制的主节点的ID；如果是主节点，这里通常显示为 `-`。
5. **负责的槽位范围**：如果节点是主节点，这里会显示它所负责的槽位范围。Redis 集群使用槽位（slot）来分配数据，每个槽位负责一部分数据。

槽位分配：

![image-20240408140559087](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081617678.png)

### 数据读写存储

尝试在6381中新增数据，报错(error) MOVED 12706 192.168.122.140:6383

![image-20240408140919905](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081617672.png)

不是已经搭建集群成功了吗，为什么存不进去呢？

是因为使用`redis-cli -p 6381`命令，是连接单机版使用的，如果是集群环境需要使用`redis-cli -p 6381 -c`命令

```shell
redis-cli -p 6381 -c
```

- `-c`：这个参数表示以集群模式（cluster mode）连接 Redis。当使用 `-c` 参数时，`redis-cli` 会自动执行重定向操作，如果初始连接的节点不是数据所在的节点，它会根据集群的元数据自动转发请求到正确的节点。

k1路由存储到6383中

![image-20240408141249366](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081616029.png)

k2路由存储到6381中

![image-20240408141316021](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081616342.png)



### 查看集群状态

通过6381节点查看整个集群的信息

```
redis-cli --cluster check 192.168.122.140:6381
```

检查Redis 集群节点及其集群的整体健康状态

3个master中存储了2个key，其中一个在6381中，另一个在6383中

![image-20240408150656535](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081616458.png)

## 主从容错切换迁移案例

主6381和从机6384切换，先停止主机6381，从机6384上位变成主机6384，6381重启后作为主机6384的从机

### 停止主机6381

```shell
[root@dongguo ~]# docker stop redis-node-1
redis-node-1
```

### 查看集群信息

一般来说，Redis主从切换的时间是几秒到几分钟不等。经过一段时间后，再次查看集群信息

![image-20240408151343012](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081616482.png)

在之前的集群信息中能够看出master6381的slave是6384，此时master6381停机，slave6384上位成为新的master

### 重启6381

![image-20240408151530486](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081616018.png)

### 再次查看集群信息

![image-20240408151648863](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081616559.png)

6381重启后作为master6384的slave

### 停止6384

为了恢复原来集群的主从关系，停止6384，过一段时间再重启6384，重新变成master6381，slave6384

```shell
docker stop redis-node-4
```

![image-20240408152110741](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081616936.png)

### 查看集群信息，等到6381变成master

![image-20240408152214531](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081616517.png)

### 重启6384

![image-20240408152237576](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081616805.png)

### 再次查看集群信息

已经恢复原状，6381成为master，6383再次成为slave

![image-20240408152316738](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081616804.png)

## 主从扩容案例

扩容为4主4从集群

| ip              | port | 角色    |
| --------------- | ---- | ------- |
| 192.168.122.140 | 6381 | master1 |
| 192.168.122.140 | 6382 | master2 |
| 192.168.122.140 | 6383 | master3 |
| 192.168.122.140 | 6384 | slave1  |
| 192.168.122.140 | 6385 | slave2  |
| 192.168.122.140 | 6386 | slave3  |
| 192.168.122.140 | 6387 | master4 |
| 192.168.122.140 | 6388 | slave4  |

### 新建6387、6388两个节点

```shell
docker run -d --name redis-node-7 --net host --privileged=true -v /data/redis/share/redis-node-7:/data redis:6.0.8 --cluster-enabled yes --appendonly yes --port 6387

docker run -d --name redis-node-8 --net host --privileged=true -v /data/redis/share/redis-node-8:/data redis:6.0.8 --cluster-enabled yes --appendonly yes --port 6388
```

![image-20240408152703888](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081616046.png)

### 进入6387容器实例内部，将新增的6387节点(空槽号)作为master节点加入原集群

```
redis-cli --cluster add-node 192.168.122.140:6387 192.168.122.140:6381
```

将一个Redis节点添加到已经运行的Redis群集中。它需要指定要添加的节点的 IP 地址和端口号，以及任意一个已经运行的节点的 IP 地址和端口号。

![image-20240408153049968](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081616592.png)

### 查看集群信息

此时6387已经加入集群中，并且作为master，此时6387还没有被分配槽位

![image-20240408153222154](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081616070.png)

记录下6387的nodeID:3de1bd4b8e99ed037f8e516a4e89d4a656b6170a , 稍后分配槽位时会用到

### 重新分配槽位（为6387分派槽位）

Redis集群中对槽位进行重分配操作  指定Redis集群中任意一个节点的地址和端口号即可。

1重新分配槽位号

```shell
redis-cli --cluster reshard 192.168.122.140:6381
```

![image-20240408154202870](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081616882.png)

2 询问分配多少槽位号，这里平均分配(16384/4 = 4096),每个节点分配4096个槽位

![image-20240408154352177](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081616248.png)

3询问分配到哪个节点上，选择6387对应的node ID：3de1bd4b8e99ed037f8e516a4e89d4a656b6170a

![image-20240408154440334](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081616727.png)

Redis会把源节点中的槽位移动到目标节点中，以达到槽位的重分配。需要注意的是，该命令执行时需要保证集群处于正常的运行状态，否则可能会导致数据丢失或者其他问题。

4.分配策略

- all：表示将所有槽位都分配给目标实例。这意味着目标实例将处理分配给它的所有key，包括之前已经分配给其他实例的key。
- done：表示已经完成了槽位的重新分配，并且目标实例已经开始处理分配给它的key。此时，其他Redis实例已经不再处理该实例的key，而是将这些key转移到目标实例中。注意，这并不意味着已经将所有key都转移完毕，仍然可能存在一些key正在转移中。

这里输入all，将4096个槽位分配给6387

5.询问是否要继续执行槽位的重分配？

![image-20240408154646944](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081616696.png)

#### 再次查看集群信息

![image-20240408154934003](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081616216.png)

此时6387分配完槽位

#### 为什么6387是3个新的区间，以前的还是连续？

重新分配成本太高，所以从三个主节点（6381/6382/6383分别匀出1364个坑位给新节点638

### 为主节点6387分配从节点6388

```
redis-cli --cluster add-node ip:新slave端口 ip:新master端口 --cluster-slave --cluster-master-id 新主机节点ID
```

6387的node ID：3de1bd4b8e99ed037f8e516a4e89d4a656b6170a

```shell
redis-cli --cluster add-node 192.168.122.140:6388 192.168.122.140:6387 --cluster-slave --cluster-master-id 3de1bd4b8e99ed037f8e516a4e89d4a656b6170a
```

![image-20240408155551611](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081616388.png)

#### 再次查看集群信息

![image-20240408155641152](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081615805.png)

扩容完成，当前为4主4从集群

## 主从缩容案例

将6387和6388下线，槽位又要重新分配，这里为了方便，将空闲的槽位统一分配给6381

注意：先删从节点，再删主节点

### 查看集群信息

6381的node ID： a9ab4aa87f753108b946a78dec37027c8e46466e

6387的node ID： 3de1bd4b8e99ed037f8e516a4e89d4a656b6170a

6388的node ID: 2e18407cd09e554bbdec577efd06e12bcbe13fd5

![image-20240408160052000](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081615359.png)

### 集群中将从节点6388删除

```shell
redis-cli --cluster del-node ip:从机端口 从机6388节点ID
```

删除6388

```shell
redis-cli --cluster del-node 192.168.122.140:6388 2e18407cd09e554bbdec577efd06e12bcbe13fd5
```

![image-20240408160335705](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081615267.png)

#### 查看集群信息

6388从节点已经被删除

![image-20240408160404737](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081615348.png)

### 重新分配槽位，将6387的槽号清空

重新分配槽位，将6387：3de1bd4b8e99ed037f8e516a4e89d4a656b6170a的4096个槽位全部分配给6381：a9ab4aa87f753108b946a78dec37027c8e46466e

```shell
redis-cli --cluster reshard 192.168.122.140:6381
```

![image-20240408161002156](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081615987.png)

#### 检查集群信息

6387的4096个槽位已经分配给了6381，6387还存在，只不过已经没有了槽位

![image-20240408161134588](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081615439.png)

### 集群中将主节点6387删除

```shell
redis-cli --cluster del-node ip:端口 6387节点ID
```

删除6387

```shell
redis-cli --cluster del-node 192.168.122.140:6387 3de1bd4b8e99ed037f8e516a4e89d4a656b6170a
```

![image-20240408161347529](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081615849.png)

#### 再次检查集群信息

此时只剩下3主3从，集群缩容完毕

![image-20240408161511022](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404081615141.png)