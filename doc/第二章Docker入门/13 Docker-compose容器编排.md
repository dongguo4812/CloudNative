# Docker-Compose介绍

Compose 是 Docker 公司推出的一个工具软件，可以管理多个 Docker 容器组成一个应用。你需要定义一个 YAML 格式的配置文件docker-compose.yml，写好多个容器之间的调用关系。然后，只要一个命令，就能同时启动/关闭这些容器

Docker-Compose是Docker官方的开源项目，负责实现对Docker容器集群的快速编排。

## 作用

docker建议我们每一个容器中只运行一个服务,因为docker容器本身占用资源极少,所以最好是将每个服务单独的分割开来但是这样我们又面临了一个问题:

如果我需要同时部署好多个服务,难道要每个服务单独写Dockerfile然后再构建镜像,构建容器,这样累都累死了,所以docker官方给我们提供了docker-compose多服务部署的工具



例如要实现一个Web微服务项目，除了Web服务容器本身，往往还需要再加上后端的数据库mysql服务容器，redis服务器，注册中心eureka，甚至还包括负载均衡容器等等。

Compose允许用户通过一个单独的docker-compose.yml模板文件（YAML 格式）来定义一组相关联的应用容器为一个项目（project）。

可以很容易地用一个配置文件定义一个多容器的应用，然后使用一条指令安装这个应用的所有依赖，完成构建。Docker-Compose 解决了容器与容器之间如何管理编排的问题。

# Docker-Compose下载

compose-file是Docker Compose使用的文件格式，用于定义所有服务的配置和资源。Compose-file允许您指定应用程序的服务、网络、卷和环境变量等，以及它们之间的依赖关系和交互。Compose-file中定义的服务可以通过Docker Compose工具进行管理和启动。

compose-file是Docker Compose的一部分，是用于定义多容器应用程序的配置文件格式。Docker Compose则是使用该配置文件进行启动和管理多容器应用程序的工具。

## compose-file版本

官网：https://docs.docker.com/compose/compose-file/compose-file-v3/

官网提供了V2、V3版本

这两个版本之间有以下区别：

1. 网络： v2版本使用link指令连接容器，而v3版本使用网络服务连接容器。
2. 部署： v2版本允许部署容器在特定的节点上，而v3版本添加了支持Stacks，允许用户在Docker Swarm模式下创建分布式应用程序，这样可以更好地管理多个容器并部署到多个节点上。
3. 配置： v3版本增加了更多的配置选项，例如配置CPU亲和性、日志记录等，在更高级的方案中提供更多灵活性。
4. 缩放： v3版本可以按名称缩放服务，而不是按容器ID。

总的来说，v3版本相对于v2版本更加强大和灵活，特别是在容器部署和管理方面有更多的选择，同时也提供了更多的功能来管理多个容器。

Compose file version 3 对应 Docker Compose 版本为 1.13.0 或更高版本。

下载地址：https://docs.docker.com/compose/install/

## 安装

当前Docker版本是26.0.0

![image-20240409155916996](F:\note\image\image-20240409155916996.png)

使用Docker Compose版本为2.26.0

![image-20240409155850310](F:\note\image\image-20240409155850310.png)



安装步骤：

### 1.下载

https://github.com/docker/compose/releases/download/v2.26.0/docker-compose-linux-x86_64

```shell
curl -L "https://github.com/docker/compose/releases/download/v2.26.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
```

可以看到花费了13分钟，速度是非常慢的

![image-20240409170950464](F:\note\image\image-20240409170950464.png)

#### 问题：

如果提示Failed connect to github.com:443; 拒绝连接

![image-20240409160439604](F:\note\image\image-20240409160439604.png)

这个错误通常说明你的系统无法连接到 Github.com 上。可能是因为你的网络连接有问题，或者该域名被防火墙阻止。

#### 原因：DNS解析问题

在DNS解析前先会尝试走hosts然后在找不到的的情况下再DNS解析,修改hosts文件域名解析就会先走hosts中的ip和域名的映射关系。我们可以修改hosts文件，修改ip地址和域名的映射关系，

#### 解决方法一：

1）查找github.com对应的IP

通过网址ipaddress.com搜索框输入github.com，查找github.com对应的IP地址

![image-20240409161136508](F:\note\image\image-20240409161136508.png)

2）找到GitHub.com DNS Resource Records，复制ip

![image-20240409161250873](F:\note\image\image-20240409161250873.png)

3）修改hosts文件

将查询到的GitHub IP地址内容`140.82.114.3 github.com` 追加进hosts文件：

```shell
vi /etc/hosts
```

![image-20240409161540116](F:\note\image\image-20240409161540116.png)

重新执行curl命令,如果还是有问题重复上面的步骤

#### 解决方法二（推荐）：

1）.下载https://github.com/docker/compose/releases/download/v2.26.0/docker-compose-linux-x86_64

2）.将文件重命名为docker-compose后上传到/usr/local/bin/

![image-20240409171137285](F:\note\image\image-20240409171137285.png)

### 2.赋予用户文件的读写权限

```shell
chmod +x /usr/local/bin/docker-compose
```

### 3.查看版本

```shell
docker-compose --version
```

![image-20240409171216112](F:\note\image\image-20240409171216112.png) 

## 卸载

```shell
rm /usr/local/bin/docker-compose
```

![image-20240409171342821](F:\note\image\image-20240409171342821.png)

# Compose核心概念

一文件、两要素

## docker-compose.yml文件

docker-compose.yml是Compose的配置文件，用于定义服务、网络和数据卷。其格式为YAML，默认路径为./docker-compose.yml，可以使用.yml或.yaml扩展名。这个配置文件是Compose工具的核心，它允许用户定义多个容器间的调用关系，以及每个容器所需的配置。

## 服务（service）

服务（service）是指一个个应用容器实例，比如订单微服务、会员微服务、mysql容器等。在docker-compose.yml配置文件中，可以定义多个服务，每个服务可以由一个或多个容器组成。服务定义了运行容器所需的配置，例如镜像、端口、环境变量、卷等。

## 工程（project）

工程（project）是由一组关联的应用容器组成的一个完整业务单元。简单来说，一个工程就是一个完整的app应用，它包含了所有必要的服务，以便能够一起运行和协作。在docker-compose.yml配置文件中，用户可以定义整个工程的结构和组成。

# Compose常用命令

docker-compose -h                           # 查看帮助

docker-compose up                           # 启动所有docker-compose服务

docker-compose up -d                        # 启动所有docker-compose服务并后台运行

docker-compose down                         # 停止并删除容器、网络、卷、镜像。

docker-compose exec  yml里面的服务id                 # 进入容器实例内部  docker-compose exec docker-compose.yml文件中写的服务id /bin/bash

docker-compose ps                      # 展示当前docker-compose编排过的运行的所有容器

docker-compose top                     # 展示当前docker-compose编排过的容器进程

docker-compose logs  yml里面的服务id     # 查看容器输出日志

docker-compose config     # 检查配置

docker-compose config -q  # 检查配置，有问题才有输出

docker-compose restart   # 重启服务

docker-compose start     # 启动服务

docker-compose stop      # 停止服务

# Compose使用的三个步骤

1.编写Dockerfile定义各个微服务应用并构建出对应的镜像文件

2.使用 docker-compose.yml 定义一个完整业务单元，安排好整体应用中的各个容器服务。

3.最后，执行docker-compose up命令 来启动并运行整个应用程序，完成一键部署上线

# Compose编排微服务案例

对之前通过dockerfile发布微服务部署到docker容器章节创建的docker-boot项目进行改造，来熟悉Compose编排与命令

## 改造升级微服务工程docker-boot

### sql建表

```sql
CREATE TABLE `t_user` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL DEFAULT '' COMMENT '用户名',
  `password` varchar(50) NOT NULL DEFAULT '' COMMENT '密码',
  `sex` tinyint(4) NOT NULL DEFAULT '0' COMMENT '性别 0=女 1=男 ',
  `deleted` tinyint(4) unsigned NOT NULL DEFAULT '0' COMMENT '删除标志，默认0不删除，1删除',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='用户表'
```

### 一键生成mybatis-generator

https://github.com/dongguo4812/mybatis-generator.git

替换配置，执行generate

![image-20240409173650869](F:\note\image\image-20240409173650869.png)

将生成的entity、mapper复制到docker-boot项目中

![image-20240409173350715](F:\note\image\image-20240409173350715.png)

### 改POM