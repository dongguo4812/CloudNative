# 简介

![image-20240509111609740](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257255.png)


Helm 是一个用于简化 Kubernetes 应用程序部署和管理的工具。它允许定义、安装和升级 Kubernetes 应用程序，同时提供了一种简单的方式来管理依赖关系、配置和版本控制。

下面是 Helm 的一些主要特点和概念：

1. **Chart（图表）：** Chart 是 Helm 的打包格式，包含了 Kubernetes 应用程序的所有相关文件和元数据。一个 Chart 包括了一个或多个 YAML 文件，用于定义 Kubernetes 资源，以及一个 `values.yaml` 文件，用于配置这些资源。
2. **Release（发布）：** Release 是一个通过 Helm 安装的 Chart 的运行实例。每个 Release 在 Kubernetes 集群中都有一个唯一的名称，并且包含了所定义的 Kubernetes 资源的一组实例。
3. **Repository（仓库）：** Helm 仓库是一个存储 Chart 的地方，类似于软件包管理系统中的软件源。您可以从 Helm 仓库中查找和下载 Chart，也可以将自己的 Chart 发布到仓库中供他人使用。
4. **Values 文件：** Chart 中的 `values.yaml` 文件用于定义配置选项。通过修改 Values 文件，您可以自定义 Chart 的行为，例如指定要部署的 Pod 的副本数量、服务的端口号等。
5. **模板引擎：** Helm 使用 Go 的 `text/template` 和 `sprig` 模板引擎来动态生成 Kubernetes 资源文件。这允许您在 Chart 中使用变量、函数和控制结构，以便根据配置参数生成不同的 Kubernetes 配置。

# 安装

## 用二进制版本安装

每个Helm [版本](https://github.com/helm/helm/releases)都提供了各种操作系统的二进制版本，这些版本可以手动下载和安装。

- 下载 [需要的版本](https://github.com/helm/helm/releases)

  如：https://get.helm.sh/helm-v3.14.4-linux-amd64.tar.gz

  ![image-20240509112608682](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257512.png)

- 解压(`tar -zxvf helm-v3.14.4-linux-amd64.tar.gz`)

  ![image-20240509113746110](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257022.png)

- 在解压目中找到`helm`程序，移动到需要的目录中(`mv linux-amd64/helm /usr/local/bin/helm`)

![image-20240509113818439](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257176.png)

### 初始化

当您已经安装好了Helm之后，您可以添加一个chart 仓库。从 [Artifact Hub](https://artifacthub.io/packages/search?kind=0)中查找有效的Helm chart仓库。

```shell
helm repo add bitnami https://charts.bitnami.com/bitnami
```

当添加完成，您将可以看到可以被您安装的charts列表：

```shell
helm search repo bitnami
```

![image-20240509133941536](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257319.png)



Helm Hub 是 Helm 的一个在线服务，用于提供各种 Helm Chart

```shell
helm search hub mysql
```

![image-20240509134358912](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131259292.png)

### 安装Chart示例

*更新仓库索引，确定我们可以拿到最新的charts列表*

```shell
helm repo update
```

您可以通过`helm install` 命令安装chart。 Helm可以通过多种途径查找和安装chart， 但最简单的是安装官方的`bitnami` charts。

```
helm install bitnami/mysql --generate-name
```

```
[root@k8s-master ~]# helm install bitnami/mysql --generate-name
NAME: mysql-1715234522
LAST DEPLOYED: Thu May  9 14:02:05 2024
NAMESPACE: default
STATUS: deployed
REVISION: 1
TEST SUITE: None
NOTES:
CHART NAME: mysql
CHART VERSION: 10.2.2
APP VERSION: 8.0.37

** Please be patient while the chart is being deployed **

Tip:

  Watch the deployment status using the command: kubectl get pods -w --namespace default

Services:

  echo Primary: mysql-1715234522.default.svc.cluster.local:3306

Execute the following to get the administrator credentials:

  echo Username: root
  MYSQL_ROOT_PASSWORD=$(kubectl get secret --namespace default mysql-1715234522 -o jsonpath="{.data.mysql-root-password}" | base64 -d)

To connect to your database:

  1. Run a pod that you can use as a client:

      kubectl run mysql-1715234522-client --rm --tty -i --restart='Never' --image  docker.io/bitnami/mysql:8.0.37-debian-12-r0 --namespace default --env MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD --command -- bash

  2. To connect to primary service (read/write):

      mysql -h mysql-1715234522.default.svc.cluster.local -uroot -p"$MYSQL_ROOT_PASSWORD"






WARNING: There are "resources" sections in the chart not set. Using "resourcesPreset" is not recommended for production. For production installations, please set the following values according to your workload needs:
  - primary.resources
  - secondary.resources

```

MySQL Chart 已经成功安装到 Kubernetes 集群中，并且具有名称 `mysql-1715234522`。输出还包含了一些有关已安装 Chart 的信息，如 Chart 名称、版本、应用程序版本等。

![image-20240509141021020](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257743.png)

在上面的例子中，`bitnami/mysql`这个chart被发布，名字是 `mysql-1612624192`

您可以通过执行 `helm show chart bitnami/mysql` 命令简单的了解到这个chart的基本信息。 或者您可以执行 `helm show all bitnami/mysql` 获取关于该chart的所有信息。

每当您执行 `helm install` 的时候，都会创建一个新的发布版本。 所以一个chart在同一个集群里面可以被安装多次，每一个都可以被独立的管理和升级。



卸载

```shell
helm uninstall mysql-1612624192
```

==注意卸载后要清除资源，比如pvc，不然修改value.yaml后再次同名创建会出现问题==

![image-20240509174313620](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257254.png)



# 入门使用

## 三大概念

https://helm.sh/zh/docs/intro/using_helm/

- *Chart* 代表着 Helm 包。它包含在 Kubernetes 集群内部运行应用程序，工具或服务所需的所有资源定义。你可以把它看作是 Homebrew formula，Apt dpkg，或 Yum RPM 在Kubernetes 中的等价物。

- *Repository（仓库）* 是用来存放和共享 charts 的地方。它就像 Perl 的 [CPAN 档案库网络](https://www.cpan.org/) 或是 Fedora 的 [软件包仓库](https://fedorahosted.org/pkgdb2/)，只不过它是供 Kubernetes 包所使用的。

- *Release* 是运行在 Kubernetes 集群中的 chart 的实例。一个 chart 通常可以在同一个集群中安装多次。每一次安装都会创建一个新的 *release*。以 MySQL chart为例，如果你想在你的集群中运行两个数据库，你可以安装该chart两次。每一个数据库都会拥有它自己的 *release* 和 *release name*。

在了解了上述这些概念以后，我们就可以这样来解释 Helm：

> Helm 安装 *charts* 到 Kubernetes 集群中，每次安装都会创建一个新的 *release*。你可以在 Helm 的 chart *repositories* 中寻找新的 chart。

仅下载mysql

```shell
helm pull bitnami/mysql
```

![image-20240509155718878](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257891.png)

解压后得到mysql的chart

![image-20240509155817526](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257014.png)



## charts 结构

![image-20240509113451404](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257461.png)

![image-20240509113500741](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257214.png)



![image-20240509160516058](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257937.png)

## 应用安装

![image-20240509160058007](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258108.png)

1通过chart包

2通过chart引用

之前已经通过chart引用的方式安装过了，现在使用chart包安装

```shell
[root@k8s-master mysql]# ll
总用量 196
-rw-r--r-- 1 root root    226 5月   3 01:36 Chart.lock
drwxr-xr-x 3 root root     20 5月   9 15:56 charts
-rw-r--r-- 1 root root    998 5月   3 01:36 Chart.yaml
-rw-r--r-- 1 root root 117306 5月   3 01:36 README.md
drwxr-xr-x 4 root root    290 5月   9 15:56 templates
-rw-r--r-- 1 root root   5495 5月   3 01:36 values.schema.json
-rw-r--r-- 1 root root  64804 5月   3 01:36 values.yaml
[root@k8s-master mysql]# helm install -f values.yaml mysql8 ./
E0509 16:15:07.766252   58914 memcache.go:287] couldn't get resource list for metrics.k8s.io/v1beta1: the server is currently unable to handle the request
E0509 16:15:07.785806   58914 memcache.go:121] couldn't get resource list for metrics.k8s.io/v1beta1: the server is currently unable to handle the request
E0509 16:15:07.990053   58914 memcache.go:287] couldn't get resource list for metrics.k8s.io/v1beta1: the server is currently unable to handle the request
E0509 16:15:08.003270   58914 memcache.go:121] couldn't get resource list for metrics.k8s.io/v1beta1: the server is currently unable to handle the request
E0509 16:15:08.038937   58914 memcache.go:287] couldn't get resource list for metrics.k8s.io/v1beta1: the server is currently unable to handle the request
E0509 16:15:08.042349   58914 memcache.go:121] couldn't get resource list for metrics.k8s.io/v1beta1: the server is currently unable to handle the request
E0509 16:15:08.080515   58914 memcache.go:287] couldn't get resource list for metrics.k8s.io/v1beta1: the server is currently unable to handle the request
E0509 16:15:08.092886   58914 memcache.go:121] couldn't get resource list for metrics.k8s.io/v1beta1: the server is currently unable to handle the request
E0509 16:15:08.158587   58914 memcache.go:287] couldn't get resource list for metrics.k8s.io/v1beta1: the server is currently unable to handle the request
E0509 16:15:08.204888   58914 memcache.go:121] couldn't get resource list for metrics.k8s.io/v1beta1: the server is currently unable to handle the request
E0509 16:15:08.219384   58914 memcache.go:287] couldn't get resource list for metrics.k8s.io/v1beta1: the server is currently unable to handle the request
E0509 16:15:08.232852   58914 memcache.go:121] couldn't get resource list for metrics.k8s.io/v1beta1: the server is currently unable to handle the request
E0509 16:15:08.245529   58914 memcache.go:287] couldn't get resource list for metrics.k8s.io/v1beta1: the server is currently unable to handle the request
E0509 16:15:08.271610   58914 memcache.go:121] couldn't get resource list for metrics.k8s.io/v1beta1: the server is currently unable to handle the request
E0509 16:15:08.295269   58914 memcache.go:287] couldn't get resource list for metrics.k8s.io/v1beta1: the server is currently unable to handle the request
E0509 16:15:08.311872   58914 memcache.go:121] couldn't get resource list for metrics.k8s.io/v1beta1: the server is currently unable to handle the request
NAME: mysql8
LAST DEPLOYED: Thu May  9 16:15:07 2024
NAMESPACE: default
STATUS: deployed
REVISION: 1
TEST SUITE: None
NOTES:
CHART NAME: mysql
CHART VERSION: 10.2.2
APP VERSION: 8.0.37

** Please be patient while the chart is being deployed **

Tip:

  Watch the deployment status using the command: kubectl get pods -w --namespace default

Services:

  echo Primary: mysql8.default.svc.cluster.local:3306

Execute the following to get the administrator credentials:

  echo Username: root
  MYSQL_ROOT_PASSWORD=$(kubectl get secret --namespace default mysql8 -o jsonpath="{.data.mysql-root-password}" | base64 -d)

To connect to your database:

  1. Run a pod that you can use as a client:

      kubectl run mysql8-client --rm --tty -i --restart='Never' --image  docker.io/bitnami/mysql:8.0.37-debian-12-r0 --namespace default --env MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD --command -- bash

  2. To connect to primary service (read/write):

      mysql -h mysql8.default.svc.cluster.local -uroot -p"$MYSQL_ROOT_PASSWORD"






WARNING: There are "resources" sections in the chart not set. Using "resourcesPreset" is not recommended for production. For production installations, please set the following values according to your workload needs:
  - primary.resources
  - secondary.resources
+info https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/

```

1.helm install -f values.yaml mysql8 ./

- `-f values.yaml`: 这个选项用于指定自定义的值文件，该文件包含了用于配置 Chart 的值。在这种情况下，您使用了名为 `values.yaml` 的文件来配置 Chart。
- `mysql8`: 这是要安装的 Helm Chart 的名称，该名称是您在 Helm Chart 中指定的名称。
- `./`: 这是 Chart 的路径。在这种情况下，`./` 表示当前目录，即 Helm 将从当前目录中加载 Chart 文件。

2.mysql没有设置账号密码，默认账号root，密码  MYSQL_ROOT_PASSWORD=$(kubectl get secret --namespace default mysql8 -o jsonpath="{.data.mysql-root-password}" | base64 -d)

通过命令获取密码为cAgAint5rz

![image-20240509161756698](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258429.png)



![image-20240509163047912](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258375.png)

3查看pod

![image-20240509162959781](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258222.png)

4卸载

![image-20240509163411999](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258032.png)

## 自定义变量值

![image-20240509160141000](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258373.png)

## 通过chart包安装

通过chart包可以直接在values.yaml修改相关配置，比如

type修改为NodePort对外暴露

![image-20240509170901493](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131259152.png)

密码修改为123456

![image-20240509174442423](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258190.png)



```shell
[root@k8s-master mysql]# vi values.yaml 
[root@k8s-master mysql]# helm install -f values.yaml mysql8 ./
NAME: mysql8
LAST DEPLOYED: Thu May  9 16:41:57 2024
NAMESPACE: default
STATUS: deployed
REVISION: 
TEST SUITE: None
NOTES:
CHART NAME: mysql
CHART VERSION: 10.2.2
APP VERSION: 8.0.37

** Please be patient while the chart is being deployed **

Tip:

  Watch the deployment status using the command: kubectl get pods -w --namespace default

Services:

  echo Primary: mysql8.default.svc.cluster.local:3306

Execute the following to get the administrator credentials:

  echo Username: root
  MYSQL_ROOT_PASSWORD=$(kubectl get secret --namespace default mysql8 -o jsonpath="{.data.mysql-root-password}" | base64 -d)

To connect to your database:

  1. Run a pod that you can use as a client:

      kubectl run mysql8-client --rm --tty -i --restart='Never' --image  docker.io/bitnami/mysql:8.0.37-debian-12-r0 --namespace default --env MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD --command -- bash

  2. To connect to primary service (read/write):

      mysql -h mysql8.default.svc.cluster.local -uroot -p"$MYSQL_ROOT_PASSWORD"






WARNING: There are "resources" sections in the chart not set. Using "resourcesPreset" is not recommended for production. For production installations, please set the following values according to your workload needs:
  - primary.resources
  - secondary.resources
+info https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/
```

查看密码

![image-20240509164258103](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258865.png)

远程连接mysql

![image-20240509174410749](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258485.png)

连接成功

![image-20240509174545893](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258567.png)

### 高可用 一主一从

![image-20240509194730283](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258777.png)

修改architecture从standalone改为replication

![image-20240509174834152](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258494.png)

设置从mysql的账号密码，注释掉existingSecret

![image-20240509180350924](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258965.png)

第二个副本设置为NodePort对外暴露

![image-20240509193907825](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258861.png)

安装

![image-20240509193126653](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258489.png)

等待pod  running

查看svc

![image-20240509195111788](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258448.png)

连接成功

![image-20240509195216759](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258128.png)





进入从节点

```shell
kubectl exec -it mysql-secondary-0  -- bash

mysql -uroot -p

show slave status\G
```



```shell
[root@k8s-master mysql]# kubectl exec -it mysql80-secondary-0  -- bash
Defaulted container "mysql" out of: mysql, preserve-logs-symlinks (init)

I have no name!@mysql80-secondary-0:/$  mysql -uroot -p
Enter password: 
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 427
Server version: 8.0.37 Source distribution

Copyright (c) 2000, 2024, Oracle and/or its affiliates.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql> show slave status\G
*************************** 1. row ***************************
               Slave_IO_State: Waiting for source to send event
                  Master_Host: mysql80-primary
                  Master_User: root
                  Master_Port: 3306
                Connect_Retry: 10
              Master_Log_File: mysql-bin.000003
          Read_Master_Log_Pos: 157
               Relay_Log_File: mysql-relay-bin.000006
                Relay_Log_Pos: 373
        Relay_Master_Log_File: mysql-bin.000003
             Slave_IO_Running: Yes
            Slave_SQL_Running: Yes
              Replicate_Do_DB: 
          Replicate_Ignore_DB: 
           Replicate_Do_Table: 
       Replicate_Ignore_Table: 
      Replicate_Wild_Do_Table: 
  Replicate_Wild_Ignore_Table: 
                   Last_Errno: 0
                   Last_Error: 
                 Skip_Counter: 0
          Exec_Master_Log_Pos: 157
              Relay_Log_Space: 799
              Until_Condition: None
               Until_Log_File: 
                Until_Log_Pos: 0
           Master_SSL_Allowed: No
           Master_SSL_CA_File: 
           Master_SSL_CA_Path: 
              Master_SSL_Cert: 
            Master_SSL_Cipher: 
               Master_SSL_Key: 
        Seconds_Behind_Master: 0
Master_SSL_Verify_Server_Cert: No
                Last_IO_Errno: 0
                Last_IO_Error: 
               Last_SQL_Errno: 0
               Last_SQL_Error: 
  Replicate_Ignore_Server_Ids: 
             Master_Server_Id: 907
                  Master_UUID: 13d1d3bc-0dfa-11ef-a0a9-3a5695ef5dbd
             Master_Info_File: mysql.slave_master_info
                    SQL_Delay: 0
          SQL_Remaining_Delay: NULL
      Slave_SQL_Running_State: Replica has read all relay log; waiting for more updates
           Master_Retry_Count: 86400
                  Master_Bind: 
      Last_IO_Error_Timestamp: 
     Last_SQL_Error_Timestamp: 
               Master_SSL_Crl: 
           Master_SSL_Crlpath: 
           Retrieved_Gtid_Set: 
            Executed_Gtid_Set: 
                Auto_Position: 0
         Replicate_Rewrite_DB: 
                 Channel_Name: 
           Master_TLS_Version: 
       Master_public_key_path: 
        Get_master_public_key: 0
            Network_Namespace: 
1 row in set, 1 warning (0.00 sec)
```

进入主节点

```shell
[root@k8s-master ~]# kubectl exec -it mysql80-primary-0  -- bash
Defaulted container "mysql" out of: mysql, preserve-logs-symlinks (init)
I have no name!@mysql80-primary-0:/$ mysql -uroot  -p
Enter password: 
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 498
Server version: 8.0.37 Source distribution

Copyright (c) 2000, 2024, Oracle and/or its affiliates.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql> show databases;
+--------------------+
| Database           |
+--------------------+
| information_schema |
| my_database        |
| mysql              |
| performance_schema |
| sys                |
+--------------------+
5 rows in set (0.00 sec)

mysql> use my_database;
Database changed
mysql> SHOW TABLES;
Empty set (0.00 sec)

mysql> CREATE TABLE my_table (
    ->     id INT AUTO_INCREMENT PRIMARY KEY,
    ->     name VARCHAR(50),
    ->     age INT,
    ->     email VARCHAR(100)
    -> );
Query OK, 0 rows affected (0.06 sec)

mysql> SHOW TABLES;
+-----------------------+
| Tables_in_my_database |
+-----------------------+
| my_table              |
+-----------------------+
1 row in set (0.01 sec)
```

在主节点创建了my_table表



回到从节点查看my_table  表同步成功

```shell
mysql> use my_database;
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Database changed
mysql> SHOW TABLES;
+-----------------------+
| Tables_in_my_database |
+-----------------------+
| my_table              |
+-----------------------+
1 row in set (0.00 sec)
```

删除

![image-20240510085841530](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258069.png)

# 部署mysql有状态服务

![image-20240510112547316](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258930.png)





![image-20240510112716579](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258855.png)

## 远程连接问题

默认mysql8是无法直接远程连接的

![image-20240510112956586](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258638.png)

设置权限（为root分配权限，以便可以远程连接）

由于Mysql5.6以上的版本修改了Password算法，这里需要更新密码算法，便于使用Navicat连接

```shell
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY '123456';
FLUSH PRIVILEGES;
```

![image-20240510113424469](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258189.png)

连接成功

![image-20240510113436054](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258127.png)

## 主从同步问题

默认创建的两个pod，是相互独立的mysql，那就从配置入手实现主从同步，但是conf.d中

![image-20240510133920711](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258957.png)

### mysql-cluster-0（主节点）

由于my.cnf没有配置数据卷，但是自定义的配置可以创建在/etc/mysql/conf.d/目录下

```shell
[root@k8s-master ~]# kubectl exec -it mysql-cluster-0 -- bash
I have no name!@mysql-cluster-0:/$ cd /etc/mysql/
I have no name!@mysql-cluster-0:/etc/mysql$ ls
conf.d	my.cnf	my.cnf.fallback
I have no name!@mysql-cluster-0:/etc/mysql$ cat my.cnf
# Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; version 2 of the License.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA

#
# The MySQL  Server configuration file.
#
# For explanations see
# http://dev.mysql.com/doc/mysql/en/server-system-variables.html

[mysqld]
pid-file        = /var/run/mysqld/mysqld.pid
socket          = /var/run/mysqld/mysqld.sock
datadir         = /var/lib/mysql
secure-file-priv= NULL

# Custom config should go here
!includedir /etc/mysql/conf.d/
```

找到nfs服务器中对应的数据卷

![image-20240510134330135](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258206.png)

创建my.conf

![image-20240510134739716](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258958.png)

如下配置

```shell
[mysqld]
log-bin=mysql-bin
server-id=1
```

### mysql-cluster-1(从节点)

创建my.conf

![image-20240510135056583](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131258622.png)

如下配置

```shell
[mysqld]
log-bin=mysql-bin
server-id=2
```

### **创建用于复制操作的用户**

![image-20240510140740687](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131259539.png)

在主节点创建一个用户slave，用于从节点链接主节点时使用。

slave给全部权限，允许用户从任何主机连接到MySQL服务器，可以根据实际情况进行配置

```
CREATE USER 'slave'@'%' IDENTIFIED WITH mysql_native_password BY '123456';
GRANT ALL PRIVILEGES ON *.* TO 'slave'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
```

![image-20240510161636894](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131259080.png)

![image-20240510161506130](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131300264.png)

### **获取主节点当前binary log文件名和位置（position）**

```
mysql> SHOW MASTER STATUS\G
*************************** 1. row ***************************
             File: mysql-bin.000003
         Position: 4450
     Binlog_Do_DB: 
 Binlog_Ignore_DB: 
Executed_Gtid_Set: 
1 row in set (0.00 sec)
```

### **在从（Slave）节点上设置主节点参数**

```sql
CHANGE MASTER TO
MASTER_HOST='10.96.134.203',
MASTER_USER='slave',
MASTER_PASSWORD='123456',
MASTER_LOG_FILE='mysql-bin.000003',
MASTER_LOG_POS=4450;
```

![image-20240510161748899](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131259284.png)

### **查看主从同步状态**

Slave_IO_Running: No

Slave_SQL_Running: No

```sql
mysql> show slave status\G
*************************** 1. row ***************************
               Slave_IO_State: 
                  Master_Host: 10.96.134.203
                  Master_User: slave
                  Master_Port: 3306
                Connect_Retry: 60
              Master_Log_File: mysql-bin.000003
          Read_Master_Log_Pos: 4450
               Relay_Log_File: mysql-cluster-1-relay-bin.000001
                Relay_Log_Pos: 4
        Relay_Master_Log_File: mysql-bin.000003
             Slave_IO_Running: No
            Slave_SQL_Running: No
              Replicate_Do_DB: 
          Replicate_Ignore_DB: 
           Replicate_Do_Table: 
       Replicate_Ignore_Table: 
      Replicate_Wild_Do_Table: 
  Replicate_Wild_Ignore_Table: 
                   Last_Errno: 0
                   Last_Error: 
                 Skip_Counter: 0
          Exec_Master_Log_Pos: 4450
              Relay_Log_Space: 156
              Until_Condition: None
               Until_Log_File: 
                Until_Log_Pos: 0
           Master_SSL_Allowed: No
           Master_SSL_CA_File: 
           Master_SSL_CA_Path: 
              Master_SSL_Cert: 
            Master_SSL_Cipher: 
               Master_SSL_Key: 
        Seconds_Behind_Master: NULL
Master_SSL_Verify_Server_Cert: No
                Last_IO_Errno: 0
                Last_IO_Error: 
               Last_SQL_Errno: 0
               Last_SQL_Error: 
  Replicate_Ignore_Server_Ids: 
             Master_Server_Id: 1
                  Master_UUID: 7eb09c28-0e8f-11ef-9b71-529a0ab903c2
             Master_Info_File: mysql.slave_master_info
                    SQL_Delay: 0
          SQL_Remaining_Delay: NULL
      Slave_SQL_Running_State: 
           Master_Retry_Count: 86400
                  Master_Bind: 
      Last_IO_Error_Timestamp: 
     Last_SQL_Error_Timestamp: 
               Master_SSL_Crl: 
           Master_SSL_Crlpath: 
           Retrieved_Gtid_Set: 
            Executed_Gtid_Set: 
                Auto_Position: 0
         Replicate_Rewrite_DB: 
                 Channel_Name: 
           Master_TLS_Version: 
       Master_public_key_path: 
        Get_master_public_key: 0
            Network_Namespace: 
1 row in set, 1 warning (0.01 sec)
```

### **开启主从同步**

![image-20240510141509767](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131259879.png)

### **再查看主从同步状态**

Slave_IO_Running: Connecting

Slave_SQL_Running: Yes

```sql
mysql> show slave status\G
*************************** 1. row ***************************
               Slave_IO_State: Waiting for master to send event
                  Master_Host: 10.96.134.203
                  Master_User: slave
                  Master_Port: 3306
                Connect_Retry: 60
              Master_Log_File: mysql-bin.000003
          Read_Master_Log_Pos: 4450
               Relay_Log_File: mysql-cluster-1-relay-bin.000002
                Relay_Log_Pos: 324
        Relay_Master_Log_File: mysql-bin.000003
             Slave_IO_Running: Yes
            Slave_SQL_Running: Yes
              Replicate_Do_DB: 
          Replicate_Ignore_DB: 
           Replicate_Do_Table: 
       Replicate_Ignore_Table: 
      Replicate_Wild_Do_Table: 
  Replicate_Wild_Ignore_Table: 
                   Last_Errno: 0
                   Last_Error: 
                 Skip_Counter: 0
          Exec_Master_Log_Pos: 4450
              Relay_Log_Space: 543
              Until_Condition: None
               Until_Log_File: 
                Until_Log_Pos: 0
           Master_SSL_Allowed: No
           Master_SSL_CA_File: 
           Master_SSL_CA_Path: 
              Master_SSL_Cert: 
            Master_SSL_Cipher: 
               Master_SSL_Key: 
        Seconds_Behind_Master: 0
Master_SSL_Verify_Server_Cert: No
                Last_IO_Errno: 0
                Last_IO_Error: 
               Last_SQL_Errno: 0
               Last_SQL_Error: 
  Replicate_Ignore_Server_Ids: 
             Master_Server_Id: 1
                  Master_UUID: 7eb09c28-0e8f-11ef-9b71-529a0ab903c2
             Master_Info_File: mysql.slave_master_info
                    SQL_Delay: 0
          SQL_Remaining_Delay: NULL
      Slave_SQL_Running_State: Slave has read all relay log; waiting for more updates
           Master_Retry_Count: 86400
                  Master_Bind: 
      Last_IO_Error_Timestamp: 
     Last_SQL_Error_Timestamp: 
               Master_SSL_Crl: 
           Master_SSL_Crlpath: 
           Retrieved_Gtid_Set: 
            Executed_Gtid_Set: 
                Auto_Position: 0
         Replicate_Rewrite_DB: 
                 Channel_Name: 
           Master_TLS_Version: 
       Master_public_key_path: 
        Get_master_public_key: 0
            Network_Namespace: 
1 row in set, 1 warning (0.01 sec)
```

### 验证主从同步

主从节点查看my_database数据库都是空的

![image-20240510142057171](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131259475.png)

主节点创建表

```sql
CREATE TABLE my_table (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    age INT,
    email VARCHAR(100)
);
```



![image-20240510142240552](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131259076.png)

从节点查看，my_table表同步到从节点

![image-20240510153526853](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131259377.png)