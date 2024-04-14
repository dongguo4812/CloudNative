大概步骤：

搜索镜像

拉去镜像

查看镜像

启动镜像 ：服务端口映射

停止容器

移除容器



前提

1.关闭防火墙：

```shell
systemctl stop firewalld
```

2.启动docker

```shell
systemctl start docker
```

# 安装tomcat

## docker hub上面查找tomcat镜像

```shell
docker search tomcat
```

![image-20240407190156494](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150566.png)

## 从docker hub上拉取tomcat镜像到本地

```shell
docker pull tomcat
```

![image-20240407191534917](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150148.png)

## docker images查看是否有拉取到的tomcat

```shell
docker images
```

![image-20240407191551973](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150884.png)

## 使用tomcat镜像创建容器实例(也叫运行镜像)

```shell
docker run -d -p 8080:8080 tomcat
```

说明

-p 小写，主机端口:docker容器端口

i:交互

t:终端

d:后台

![image-20240407191606965](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150804.png)

## 查看docker容器

![image-20240407191625218](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150985.png)

## 访问tomcat首页

访问http://192.168.122.140:8080/ tomcat首页404

![image-20240407191831150](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072151589.png)

### 1.可能没有映射端口或者没有关闭防火墙

已经确定防火墙处于关闭状态，排除

### 2 把webapps.dist目录换成webapps

进入tomcat容器中，**进入到**/usr/local/tomcat 目录下，发现webapps文件是空的

![image-20240407192116906](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150172.png)

新版tomcat，启动tomcat镜像后，webapps为空，文件都在webapps.dist中

![image-20240407192224669](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150947.png)

删除webapps，将webapps.dist重命名为webapps

![image-20240407192328914](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150771.png)

### 重新访问tomcat首页

![image-20240407192347700](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150779.png)



对于最新版的tomcat镜像，版本是tomcat10，还需要修改文件名才能成功访问，而我们想要的是下载的tomcat镜像可以直接使用，那么可以使用docker hub上别人已经设置好的版本

## 删除之前启动的tomcat容器

先使用 `docker stop` 停止容器，然后使用 `docker rm` 删除容器

![image-20240407193749347](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150716.png)

## 下载并启动免修改版

```shell
docker run -d -p 8080:8080 --name mytomcat8 billygoo/tomcat8-jdk8
```

![image-20240407194944681](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150458.png)

## 访问tomcat首页

![image-20240407195019939](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150299.png)

这样就可以直接使用了

# 安装mysql

## docker hub上面查找mysql镜像

```shell
docker search mysql
```

![image-20240407195520902](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150050.png)

## 从docker hub上拉取mysql镜像到本地标签为8.0.20

```shell
docker pull mysql:8.0.20
```

![image-20240407200915646](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150653.png)

## 使用mysql8镜像创建容器(也叫运行镜像)

设置密码root

```shell
docker run -p 3306:3306 --name mysql8 -e MYSQL_ROOT_PASSWORD=root -d mysql:8.0.20
```

![image-20240407201019699](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150023.png)

## 进入mysql

![image-20240407201222668](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150643.png)

## 建库建表插入数据

### 创建库db01

![image-20240407201418506](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150290.png)

### 创建user表，并插入数据

![image-20240407201639852](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150076.png)

## 设置权限（为root分配权限，以便可以远程连接）

由于Mysql5.6以上的版本修改了Password算法，这里需要更新密码算法，便于使用Navicat连接

```
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'root';
FLUSH PRIVILEGES;
```

![image-20240407203002537](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150885.png)

## 外部Win连接运行在dokcer上的mysql容器实例服务

![image-20240407203040848](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072150795.png)



## 插入中文数据乱码问题

![image-20240407203117332](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072151418.png)

虽然在navicat中没有乱码，

![image-20240407203223170](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072151859.png)



但是在docker的mysql中出现中文乱码问题，这是因为docker上默认字符集编码隐患，可见，数据库的编码使用的latin1。由于navicat工具对客户端相关编码进行的改写，所以navicat并没有显示中文乱码。



![image-20240407203313017](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072151068.png)

## 数据持久化问题

现在删除mysql容器后重新创建容器，之前创建的db01库数据就没有了。

```shell
mysql> quit
Bye
root@6edf04572292:/# exit
exit
[root@dongguo ~]# docker rm -f 6edf04572292
6edf04572292
[root@dongguo ~]# docker run -p 3306:3306 --name mysql8 -e MYSQL_ROOT_PASSWORD=root -d mysql:8.0.20
c9227a75cdde55fb6fa4ec5ba3201efd337c34acaa79b8a1b8b70d54b9170bbf
[root@dongguo ~]# docker exec -it 6edf04572292 /bin/bash
Error response from daemon: No such container: 6edf04572292
[root@dongguo ~]# docker ps
CONTAINER ID   IMAGE                   COMMAND                   CREATED          STATUS          PORTS                                                  NAMES
c9227a75cdde   mysql:8.0.20            "docker-entrypoint.s…"   21 seconds ago   Up 20 seconds   0.0.0.0:3306->3306/tcp, :::3306->3306/tcp, 33060/tcp   mysql8
5afca1039887   billygoo/tomcat8-jdk8   "catalina.sh run"         56 minutes ago   Up 56 minutes   0.0.0.0:8080->8080/tcp, :::8080->8080/tcp              mytomcat8
[root@dongguo ~]# docker exec -it c9227a75cdde /bin/bash
[root@dongguo ~]# docker exec -it c9227a75cdde /bin/bash
root@c9227a75cdde:/# mysql -uroot -p
Enter password: 
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 8
Server version: 8.0.20 MySQL Community Server - GPL

Copyright (c) 2000, 2020, Oracle and/or its affiliates. All rights reserved.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql> show databases;
+--------------------+
| Database           |
+--------------------+
| information_schema |
| mysql              |
| performance_schema |
| sys                |
+--------------------+
4 rows in set (0.01 sec)
```

## 解决mysq问题

1 解决中文乱码问题

2 通过容器卷同步mysql

### 新建mysql容器实例，并挂载容器数据卷

```shell
docker run -d -p 3306:3306 --privileged=true -v /dongguo/mysql/log:/var/log/mysql -v /dongguo/mysql/data:/var/lib/mysql -v /dongguo/mysql/conf:/etc/mysql/conf.d -e MYSQL_ROOT_PASSWORD=root  --name mysql mysql:8.0.20
```

执行：

```shell
mysql> quit
Bye
root@c9227a75cdde:/# exit
exit
[root@dongguo ~]# docker rm -f c9227a75cdde
c9227a75cdde
[root@dongguo ~]# docker run -d -p 3306:3306 --privileged=true -v /dongguo/mysql/log:/var/log/mysql -v /dongguo/mysql/data:/var/lib/mysql -v /dongguo/mysql/conf:/etc/mysql/conf.d -e MYSQL_ROOT_PASSWORD=root  --name mysql mysql:8.0.20
41e7025c2b080ea36a5350887c15a1de9b4788472a575bb7c13acbd9cb7f7d95

```

将宿主机目录挂载到容器里,挂载了三个容器数据卷

```shell
-v /dongguo/mysql/log:/var/log/mysql
-v /dongguo/mysql/data:/var/lib/mysql
-v /dongguo/mysql/conf:/etc/mysql/conf.d
```

### 已经挂载实现数据同步，在宿主机/dongguo/mysql/conf下新建my.cnf解决中文乱码问题

添加内容

```shell
[client]
default_character_set=utf8
[mysqld]
collation_server = utf8_general_ci
character_set_server = utf8
```

执行：

```shell
[root@dongguo ~]# cd /dongguo/mysql/conf
[root@dongguo conf]# vim my.cnf
[root@dongguo conf]# cat my.cnf
[client]
default_character_set=utf8
[mysqld]
collation_server = utf8_general_ci
character_set_server = utf8
```



### 重新启动mysql容器实例再重新进入并查看字符编码

```shell
[root@dongguo ~]# docker restart 41e7025c2b08
41e7025c2b08
[root@dongguo ~]# docker exec -it 41e7025c2b08 /bin/bash
root@41e7025c2b08:/# mysql -uroot -p
Enter password: 
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 8
Server version: 8.0.20 MySQL Community Server - GPL

Copyright (c) 2000, 2020, Oracle and/or its affiliates. All rights reserved.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.
mysql> SHOW VARIABLES LIKE 'character%';
+--------------------------+--------------------------------+
| Variable_name            | Value                          |
+--------------------------+--------------------------------+
| character_set_client     | utf8                           |
| character_set_connection | utf8                           |
| character_set_database   | utf8                           |
| character_set_filesystem | binary                         |
| character_set_results    | utf8                           |
| character_set_server     | utf8                           |
| character_set_system     | utf8                           |
| character_sets_dir       | /usr/share/mysql-8.0/charsets/ |
+--------------------------+--------------------------------+
8 rows in set (0.01 sec)
```

字符编码已经设置为utf-8



### 设置权限（为root分配权限，以便可以远程连接）

由于Mysql5.6以上的版本修改了Password算法，这里需要更新密码算法，便于使用Navicat连接

```
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'root';
FLUSH PRIVILEGES;
```

![image-20240407203002537](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072151714.png)



### 再新建库新建表再插入中文测试

![image-20240407211033490](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072151725.png)

![image-20240407211113260](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072151939.png)

![image-20240407211138112](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072151985.png)



数据同步持久化问题其实在配置解决中文乱码时已经验证了。

### 将当前容器实例删除，再重新来一次，之前建的db01实例是否还存在

![image-20240407211335951](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072151764.png)

新启动一个docker容器

```shell
docker run -d -p 3306:3306 --privileged=true -v /dongguo/mysql/log:/var/log/mysql -v /dongguo/mysql/data:/var/lib/mysql -v /dongguo/mysql/conf:/etc/mysql/conf.d -e MYSQL_ROOT_PASSWORD=root  --name mysql mysql:8.0.20
```

挂载容器数据卷后，如果不小心将容器给删除，运行一个新的容器，主机的数据会同步给容器，之前的数据还是存在的。

```shell
mysql> quit
Bye
root@41e7025c2b08:/# exit
exit
[root@dongguo ~]# docker rm -f 41e7025c2b08
41e7025c2b08
[root@dongguo ~]# docker ps
CONTAINER ID   IMAGE                   COMMAND             CREATED             STATUS             PORTS                                       NAMES
5afca1039887   billygoo/tomcat8-jdk8   "catalina.sh run"   About an hour ago   Up About an hour   0.0.0.0:8080->8080/tcp, :::8080->8080/tcp   mytomcat8
[root@dongguo ~]# docker run -d -p 3306:3306 --privileged=true -v /dongguo/mysql/log:/var/log/mysql -v /dongguo/mysql/data:/var/lib/mysql -v /dongguo/mysql/conf:/etc/mysql/conf.d -e MYSQL_ROOT_PASSWORD=root  --name mysql mysql:8.0.20
1ed584f712d67c65dc325103ea3bd48945c1cfc30fca5adc62cfa15610582915
[root@dongguo ~]# docker ps
CONTAINER ID   IMAGE                   COMMAND                   CREATED             STATUS             PORTS                                                  NAMES
1ed584f712d6   mysql:8.0.20            "docker-entrypoint.s…"   11 seconds ago      Up 10 seconds      0.0.0.0:3306->3306/tcp, :::3306->3306/tcp, 33060/tcp   mysql
5afca1039887   billygoo/tomcat8-jdk8   "catalina.sh run"         About an hour ago   Up About an hour   0.0.0.0:8080->8080/tcp, :::8080->8080/tcp              mytomcat8
[root@dongguo ~]# docker exec -it 1ed584f712d6 /bin/bash
root@1ed584f712d6:/# mysql -uroot -p
Enter password: 
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 8
Server version: 8.0.20 MySQL Community Server - GPL

Copyright (c) 2000, 2020, Oracle and/or its affiliates. All rights reserved.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql> show databases;
+--------------------+
| Database           |
+--------------------+
| db01               |
| information_schema |
| mysql              |
| performance_schema |
| sys                |
+--------------------+
5 rows in set (0.02 sec)

mysql> use db01;
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Database changed
mysql> select * from user;
+------+--------+
| id   | name   |
+------+--------+
|    2 | 李四   |
+------+--------+
1 row in set (0.00 sec)
```



# 安装redis

## 从docker hub上拉取redis镜像到本地标签为6.0.8

```shell
docker run -d -p 6379:6379 redis:6.0.8
```

![image-20240407212210568](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072151045.png)

## 使用redis

```shell
[root@dongguo ~]# docker exec -it faef49f662b9 /bin/bash
root@faef49f662b9:/data# redis-cli        
127.0.0.1:6379> set k1 v1
OK
127.0.0.1:6379> get k1
"v1"
```

但是在真正使用时还需要挂载容器数据卷，实现配置的同步、数据的恢复。

## 删除redis容器

![image-20240407212414124](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072151369.png)

## 在CentOS宿主机下新建目录/app/redis，创建redis.conf文件

默认出厂的原始redis.conf在当前文章目录下，以下是需要修改的内容

### 开启redis验证    可选

```
requirepass root
```

### 允许redis外地连接  必须

注释掉 # bind 127.0.0.1

### daemonize no 必须

将daemonize yes注释起来或者 daemonize no设置，因为该配置和docker run中-d参数冲突，会导致容器一直启动失败

### 开启redis数据持久化   可选

```
 appendonly yes
```

## 使用redis6.0.8镜像创建容器(也叫运行镜像)

```shell
docker run  -p 6379:6379 --name redis --privileged=true -v /app/redis/redis.conf:/etc/redis/redis.conf -v /app/redis/data:/data -d redis:6.0.8 redis-server /etc/redis/redis.conf
```

![image-20240407214534642](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072151948.png)

## 测试redis-cli连接

```shell
[root@dongguo ~]# docker exec -it 202b3fb83c8a /bin/bash
root@202b3fb83c8a:/data# redis-cli -a root
Warning: Using a password with '-a' or '-u' option on the command line interface may not be safe.
127.0.0.1:6379> set k1 v1
OK
127.0.0.1:6379> get k1
"v1"
```

其实在连接redis时已经明确了同步配置成功，输入密码才能操作redis

## 使用图形化工具连接

![image-20240407214820631](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404072151757.png)

# 安装Nginx

## 从docker hub上拉取nginx镜像

```shell
docker pull nginx
```

![image-20240414180019206](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404142052611.png)

## 第一次运行 nginx 镜像

第一次运行镜像，用于拷贝配置文件和 html 目录到宿主机。

```shell
docker run -p 80:80 -d --name nginx nginx
```

![image-20240414181027167](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404142052230.png)

拷贝 Nginx 配置文件

```shell
# nginx容器名、/etc/nginx/nginx.conf 配置文件容器内的地址
[root@dongguo ~]# docker cp nginx:/etc/nginx /app/nginx
Successfully copied 16.9kB to /app/nginx
[root@dongguo ~]# docker cp nginx:/usr/share/nginx/html /app/nginx/html
Successfully copied 4.1kB to /app/nginx/html
```

复制的目录是nginx修改为conf

![image-20240414184229369](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404142052320.png)



删除容器nginx

```shell
docker rm -f nginx
```

## 正式运行镜像

```shell
# 注意 外部的/nginx/conf下面的内容必须存在，否则挂载会覆盖
docker run -p 80:80 --name nginx \
-v /app/nginx/html:/usr/share/nginx/html:ro \
-v /app/nginx/conf:/etc/nginx \
-d nginx
```

![image-20240414184259635](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404142052353.png)

## 测试

![image-20240414184309160](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404142052444.png)

# 安装ElasticSearch

## 拉取镜像

 以安装Elasticsearch 8.6.0版本为例

## 创建挂载点目录

```shell
mkdir -p /app/es/data /app/es/plugins
chmod 777  /app/es/data
chmod 777  /app/es/plugins
```

## 部署单机es，创建es容器

```shell
#准备文件和文件夹，并chmod -R 777 xxx
docker run --name=elasticsearch -p 9200:9200 -p 9300:9300 \
-e "discovery.type=single-node" \
-e ES_JAVA_OPTS="-Xms300m -Xmx300m" \
-v /app/es/data:/usr/share/elasticsearch/data \
-v /app/es/plugins:/usr/shrae/elasticsearch/plugins \
-v es8config:/usr/share/elasticsearch/config \
-d elasticsearch:8.6.0
```

-v es8config:/usr/share/elasticsearch/config  具名卷将/usr/share/elasticsearch/config的数据映射到es8config卷

## 编写elasticsearch.yml

### 注意！ es8.0以上默认开启了 ssl 认证

直接访问 : http://192.168.122.140:9200是无法访问的，需要访问 https,或者关闭 SSL认证

```
cd  /var/lib/docker/volumes
```

打开 elasticsearch.yml 文件 找到 xpack.security.enabled: true 改为 xpack.security.enabled: false,这样就可以直接 使用http访问，并且不需要账号密码鉴权,这个设置看个人情况，如果是生产环境建议开始开启 https和账号密码鉴权
![image-20240414204057263](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404142052451.png)



```shell
echo 'xpack.security.enabled: false' >> es8config/_data/elasticsearch.yml
echo 'xpack.security.transport.ssl.enabled: false' >> es8config/_data/elasticsearch.yml
```





进入容器确认是否同步

![image-20240414205125202](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404142052634.png)



### 重启es容器

```
docker restart elasticsearch
```

## 测试Elasticsearch是否安装成功

```
http://192.168.122.140:9200
```

![image-20240414205007421](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404142051024.png)
