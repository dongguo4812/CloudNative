# Portainer简介

Portainer是一个轻量级的管理UI界面，用于管理Docker容器、镜像、卷和网络。它支持端口映射、容器启动、停止、删除、日志查看等功能，同时也提供了可视化的监控和统计功能，可以快速轻松的管理多个Docker主机。Portainer不需要额外安装依赖，所有的安装都可以通过Docker容器完成。同时，Portainer也提供了REST API，可以方便地进行集成和自动化操作。方便地管理Docker环境，包括单机环境和集群环境。

官网传送门：https://www.portainer.io/

https://docs.portainer.io/v/ce-2.9/start/install/server/docker/linux

# 安装

```shell
docker-compose start
```

安装前启动上一节Compose编排的微服务项目，使用portainer进行图形化展示信息

## docker命令安装

```shell
docker run -d -p 8000:8000 -p 9000:9000 --name portainer  \
--restart=always    \
-v /var/run/docker.sock:/var/run/docker.sock   \
-v portainer_data:/data     \
portainer/portainer
```

- -d 表示以后台方式运行容器。
- -p 8000:8000 -p 9000:9000 表示将容器的 8000 和 9000 端口映射到主机的 8000 和 9000 端口。
- --name portainer 表示将容器命名为 portainer。
- --restart=always 表示容器启动失败或者 Docker 守护进程重启后，都会自动重启容器。
- -v /var/run/docker.sock:/var/run/docker.sock 表示将 Docker 宿主机上的 /var/run/docker.sock 文件挂载到容器的 /var/run/docker.sock 文件，使得 Portainer 容器可以与 Docker 宿主机上的 Docker 守护进程通信，进而管理 Docker 环境中的其他容器。
- -v portainer_data:/data 表示将 Portainer 容器内的 /data 目录挂载到 Docker 宿主机上的 portainer_data 数据卷上。这样，Portainer 容器中的数据可以持久化存储在 Docker 宿主机上。
- portainer/portainer 表示使用 Portainer 镜像创建容器。

![image-20240410134740977](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101510135.png)

第一次登录需创建admin，访问地址：ip:9000

![image-20240410134909268](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101510039.png)

选择local选项卡后点击connect

![image-20240410135009060](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101510727.png)

本地docker详细信息展示，**主界面**点击local

![image-20240410135048020](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101510721.png)

## Dashboard

Dashboard：仪表盘功能，您可以在这里查看有关当前系统状况的实时数据和统计信息，例如运行的容器、使用的资源等等。

右侧展示分为五部分：Stacks--堆栈 container--容器 images--镜像 volume--目录映射  networks--网络

![image-20240410135509117](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101510291.png)

## Stack

管理和部署Docker Compose文件的工具。它允许用户上传和部署Docker Compose文件，创建和管理服务、网络、卷等相关资源，以便在Docker环境中轻松地部署和运行应用程序。在Stacks页面中，可以看到当前已经部署的所有Stack及其当前状态，可以选择修改或删除Stack。用户也可以通过点击“Deploy the stack”按钮来上传并启动新的Stack。通过Stacks页面，用户可以轻松地管理和维护其Docker Compose项目。

![image-20240410135754366](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101510662.png)

compose编排的3个服务

![image-20240410135811257](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101510814.png)

## Containers

容器管理功能，您可以在这里查看、创建、启动、停止、重启和删除容器等操作。

![image-20240410140044192](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101509505.png)

查看log

![image-20240410140254760](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101509582.png)

日志信息

![image-20240410150607539](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101509709.png)

## Images

镜像管理功能，您可以在这里查看、拉取、删除、构建和推送镜像等操作。

![image-20240410150709784](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101509808.png)

## Volumes

卷管理功能，您可以在这里创建和删除数据卷，以及查看和配置卷的详细信息。

![image-20240410150817735](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101509263.png)

## Networks

网络管理功能，您可以在这里创建和删除网络，以及查看和配置网络的详细信息。

![image-20240410150845438](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101509174.png)