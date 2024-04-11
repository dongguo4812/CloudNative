# 前提

![image-20240406180056914](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062024384.png)

## 前提条件

目前，CentOS 仅发行版本中的内核支持 Docker。Docker 运行在CentOS 7 (64-bit)上，
要求系统为64位、Linux系统内核版本为 3.8以上，这里选用Centos7.x

## 查看自己的内核

uname命令用于打印当前系统相关信息（内核版本号、硬件架构、主机名称和操作系统类型等）。

![image-20240406180326140](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062024913.png)

# Docker的基本组成

![image-20240410221804203](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404102218638.png)

## 镜像(image)

Docker 镜像（Image）就是一个只读的模板。镜像可以用来创建 Docker 容器，一个镜像可以创建很多容器。

image 文件生成的容器实例，本身也是一个文件，称为镜像文件。

它也相当于是一个root文件系统。比如官方镜像 centos:7 就包含了完整的一套 centos:7 最小系统的 root 文件系统。
相当于容器的“源代码”，docker镜像文件类似于Java的类模板，而docker容器实例类似于java中new出来的实例对象。

![image-20240406182127554](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062024831.png)

## 容器(container)

 一个容器运行一种服务，当我们需要的时候，就可以通过docker客户端创建一个对应的运行实例，也就是我们的容器


1 从面向对象角度
Docker 利用容器（Container）独立运行的一个或一组应用，应用程序或服务运行在容器里面，容器就类似于一个虚拟化的运行环境，容器是用镜像创建的运行实例。就像是Java中的类和实例对象一样，镜像是静态的定义，容器是镜像运行时的实体。容器为镜像提供了一个标准的和隔离的运行环境，它可以被启动、开始、停止、删除。每个容器都是相互隔离的、保证安全的平台

2 从镜像容器角度
可以把容器看做是一个简易版的 Linux 环境（包括root用户权限、进程空间、用户空间和网络空间等）和运行在其中的应用程序。

## 仓库(repository)

仓库（Repository）是集中存放镜像文件的场所。

类似于Maven仓库，存放各种jar包的地方；github仓库，存放各种git项目的地方；
Docker公司提供的官方registry被称为Docker Hub，存放各种镜像模板的地方。

仓库分为公开仓库（Public）和私有仓库（Private）两种形式。
最大的公开仓库是 Docker Hubhttps://hub.docker.com/

(https://hub-stage.docker.com/），
存放了数量庞大的镜像供用户下载。国内的公开仓库包括阿里云 、网易云等



## 其他：

Docker_host：Docker主机，安装Docker服务的宿主机。

Docker_deamon:后台进程，运行在Docker服务器的后台进程





当我们在主机安装Docker服务后，会存在一个后台进程Docker_deanon,它负责处理请求。

通过repository拉取镜像、通过镜像启动容器等等操作

# Docker平台架构图解

![image-20240406182424042](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062024410.png)

Docker运行的基本流程：

1. 用户是使用 Docker Client 与 Docker Daemon 建立通信，并发送请求给后者。
2. Docker Daemon 作为 Docker 架构中的主体部分，首先提供 Docker Server 的功能使其可以接受 Docker Client 的请求。
3. Docker Engine 执行 Docker 内部的一系列工作，每一项工作都是以一个 Job 的形式的存在。
4. Job 的运行过程中，当需要容器镜像时，则从 Docker Registry 中下载镜像，并通过镜像管理驱动 Graphdriver 将下载镜像以 Graph 的形式存储。
5. 当需要为 Docker 创建网络环境时，通过网络管理驱动 Networkdriver 创建并配置 Docker容器网络环境。
6. 当需要限制 Docker 容器运行资源或执行用户指令等操作时，则通过 Execdriver 来完成。
7. Libcontainer 是一项独立的容器管理包，Networkdriver 以及 Execdriver 都是通过 Libcontainer 来实现具体对容器进行的操作。

## **架构图解**

**Docker Client**

1 Docker Client 是 和 Docker Daemon 建立通信的客户端。通过Docker客户端，用户可以执行各种Docker命令，如构建、运行和停止容器等。

2 Docker Client 可以通过以下三种方式和 Docker Daemon 建立通信：tcp://host:port、unix://pathtosocket 和 fd://socketfd

3 Docker Client 发送容器管理请求后，由 Docker Daemon 接受并处理请求，当 Docker Client 接收到返回的请求相应并简单处理后，Docker Client 一次完整的生命周期就结束了。(一次完整的请求：发送请求→处理请求→返回结果)，与传统的 C/S 架构请求流程并无不同。

**Docker Daemon**

Docker守护进程是Docker的核心组件，它负责管理整个Docker系统的运行，包括镜像、容器、网络和存储等。

Docker daemon 架构图

![image-20240406185356490](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062024468.png)

Docker Server 架构图

![image-20240406185407521](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062024376.png)

1 Docker Server 相当于 C/S 架构的服务端。功能为接受并调度分发 Docker Client 发送的请求。接受请求后，Docker Server 通过路由与分发调度，找到相应的 Handler 来执行请求。

2 在 Docker 的启动过程中，通过包 gorilla/mux 创建了一个 mux.Router 来提供请求的路由功能。在 Golang 中 gorilla/mux 是一个强大的 URL 路由器以及调度分发器。该 mux.Router 中添加了众多的路由项，每一个路由项由 HTTP 请求方法（PUT、POST、GET 或DELETE）、URL、Handler 三部分组成。

3 创建完 mux.Router 之后，Docker 将 Server 的监听地址以及 mux.Router 作为参数来创建一个 httpSrv=http.Server{}，最终执行 httpSrv.Serve() 为请求服务。

4 在 Docker Server 的服务过程中，Docker Server 在 listener 上接受 Docker Client 的访问请求，并创建一个全新的 goroutine 来服务该请求。在 goroutine 中，首先读取请求内容并做解析工作，接着找到相应的路由项并调用相应的 Handler 来处理该请求，最后 Handler 处理完请求之后回复该请求。

**Docker Engine**

5 Docker Engine 是 Docker 架构中的运行引擎，同时也 Docker 运行的核心模块。它扮演 Docker Container 存储仓库的角色，并且通过执行 Job 的方式来操纵管理这些容器。

6 在 Docker Engine 数据结构的设计与实现过程中，有一个 Handler 对象。该 Handler 对象存储的都是关于众多特定 Job 的 Handler 处理访问。举例说明: Docker Engine 的Handler 对象中有一项为：{“create”: daemon.ContainerCreate,}，则说明当名为”create” 的 Job 在运行时，执行的是 daemon.ContainerCreate 的 Handler。

**Job**

1 一个 Job 可以认为是 Docker 架构中 Docker Engine 内部最基本的工作执行单元。Docker 可以做的每一项工作，都可以抽象为一个 Job。例如：在容器内部运行一个进程，这是一个 Job；创建一个新的容器，这是一个 Job。Docker Server 的运行过程也是一个 Job，名为 ServeApi。

2 Job 的设计者，把 Job 设计得与 Unix 进程相仿。比如说：Job 有一个名称、有参数、有环境变量、有标准的输入输出、有错误处理，有返回状态等。

**Docker Registry**

1 Docker Registry 是一个存储容器镜像的仓库（注册中心），可理解为云端镜像仓库。按 Repository 来分类，docker pull 按照 [repository]:[tag] 来精确定义一个具体的 Image。

2 在 Docker 的运行过程中，Docker Daemon 会与 Docker Registry 通信，并实现搜索镜像、下载镜像、上传镜像三个功能，这三个功能对应的 Job 名称分别为：“search”、”pull” 与 “push”。

3 Docker Registry 可分为公有仓库（ Docker Hub）和私有仓库。

**Graph** 

Graph是指Docker镜像及其所有层的存储方式。Graph由一组只读层和一个可写层组成，每个只读层表示一个已有的镜像或父镜像。这些只读层都是只读文件系统，并且只能通过复制构建新的镜像。可写层则是新建或修改文件的地方，用于创建新的镜像。

当创建一个新镜像时，Docker会在已有的镜像层（只读层）上添加一个新层（可写层）用于存储修改后的文件，这个新镜像层就是由只读层和可写层组成的。

![image-20240406185432271](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062024513.png)

**Repository**

Docker仓库（Docker Registry）：Docker仓库是用于存储和管理Docker镜像的地方。Docker Hub是最常用的Docker仓库，也可以通过搭建自己的私有仓库来管理Docker镜像。

Docker镜像（Docker Image）：Docker镜像是一个只读的模板，可以用来创建容器。镜像可以从Docker Hub或者自己构建，其中包含了应用程序和其依赖的所有文件和配置信息。

1 已下载镜像的保管者（包括下载的镜像和通过 Dockerfile 构建的镜像）。

搜索公众号顶级架构师回复关键字“架构整洁”，获取一份惊喜礼包。

2 一个 Repository 表示某类镜像的仓库（例如：Ubuntu），同一个 Repository 内的镜像用 Tag 来区分（表示同一类镜像的不同标签或版本）。一个 Registry 包含多个Repository，一个 Repository 包含同类型的多个 Image。

3 镜像的存储类型有 Aufs、Devicemapper、Btrfs、Vfs等。其中 CentOS 系统 7.x 以下版本使用 Devicemapper 的存储类型。

4 同时在 Graph 的本地目录中存储有关于每一个的容器镜像具体信息，包含有：该容器镜像的元数据、容器镜像的大小信息、以及该容器镜像所代表的具体 rootfs。

**GraphDB**

1 已下载容器镜像之间关系的记录者。

2 GraphDB 是一个构建在 SQLite 之上的小型数据库，实现了节点的命名以及节点之间关联关系的记录。

**Driver** 

1. Docker网络（Docker Network）：Docker网络是用于容器间通信的网络。Docker可以创建自己的网络，也可以连接到已有网络

Network

1. Docker Network是位于Driver组件中的。Docker Network是一个用于管理Docker容器网络的驱动程序，它允许用户创建自定义的网络，并将容器连接到这些网络中。同时，Docker也提供了多种内置的网络驱动程序（如Bridge、Overlay、MACVLAN等），以方便用户根据不同场景进行网络配置和管理。因此，Docker Network是Driver组件中非常重要的一部分。

Driver 是 Docker 架构中的驱动模块。通过 Driver 驱动，Docker 可以实现对 Docker 容器执行环境的定制。即 Graph 负责镜像的存储，Driver 负责容器的执行。

**Graphdriver**

![image-20240406185455457](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062024464.png)

1 Graphdriver 主要用于完成容器镜像的管理，包括存储与获取。

2 存储：docker pull 下载的镜像由 Graphdriver 存储到本地的指定目录( Graph 中 )。

3 获取：docker run（create）用镜像来创建容器的时候由 Graphdriver 到本地 Graph中获取镜像。

**Networkdriver**

![image-20240406185509702](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062024729.png)

Networkdriver 的用途是完成 Docker 容器网络环境的配置，其中包括:

Docker 启动时为 Docker 环境创建网桥。Docker 容器创建时为其创建专属虚拟网卡设备。Docker 容器分配IP、端口并与宿主机做端口映射，设置容器防火墙策略等。

**Execdriver**

![image-20240406185520770](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062024745.png)

1 Execdriver 作为 Docker 容器的执行驱动，负责创建容器运行命名空间、容器资源使用的统计与限制、容器内部进程的真正运行等。

2 现在 Execdriver 默认使用 Native 驱动，不依赖于 LXC。

**Libcontainer** 

![image-20240406185533912](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062024065.png)

1 Libcontainer 是 Docker 架构中一个使用 Go 语言设计实现的库，设计初衷是希望该库可以不依靠任何依赖，直接访问内核中与容器相关的 API。

2 Docker 可以直接调用 Libcontainer 来操纵容器的 Namespace、Cgroups、Apparmor、网络设备以及防火墙规则等。

3 Libcontainer 提供了一整套标准的接口来满足上层对容器管理的需求。或者说 Libcontainer 屏蔽了 Docker 上层对容器的直接管理。

**Docker Container**

1. Docker容器是通过Docker镜像创建的运行实例，包含了应用程序和其所需的依赖文件等。每个容器都是独立的，相互隔离。Docker容器（Docker Container）：

![image-20240406185546638](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062024488.png)

1 Docker Container（ Docker 容器 ）是 Docker 架构中服务交付的最终体现形式。

2 Docker 按照用户的需求与指令，订制相应的 Docker 容器：

用户通过指定容器镜像，使得 Docker 容器可以自定义 rootfs 等文件系统。用户通过指定计算资源的配额，使得 Docker 容器使用指定的计算资源。用户通过配置网络及其安全策略，使得 Docker 容器拥有独立且安全的网络环境。用户通过指定运行的命令，使得 Docker 容器执行指定的工作。

# CentOS7安装Docker

参考官网：https://docs.docker.com/engine/install/centos/

## 1 确定CentOS7及以上版本

创建CentOS虚拟机系统，确定版本

```shell
[root@dongguo ~]# cat /etc/redhat-release
CentOS Linux release 7.9.2009 (Core)
```

## 2 卸载旧版本

`yum` might report that you have none of these packages installed.

Images, containers, volumes, and networks stored in `/var/lib/docker/` aren't automatically removed when you uninstall Docker.

```shell
yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine
```

当卸载Docker时，存储在/var/lib/docker/中的镜像（Images）、容器（Containers）、卷（Volumes）和网络（Networks）并不会自动被移除。这些资源在卸载过程中需要手动进行清理。

如果怕删除的不干净，可以使用

```shell
yum remove docker*
```

## 3 yum安装gcc相关配置

3.1 确保Centos能连接上外网，可以使用ping命令来测试

3.2安装GNU编译器集合（GNU Compiler Collection，简称GCC）的C语言编译器

```shell
yum -y install gcc
```

3.3安装C++编译器（GCC）

```shell
yum -y install gcc-c++
```

## 4 安装需要的软件包

![image-20240406193402951](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062024016.png)

### 4.1 安装yum-utils软件包

首先，您需要确保您的系统已安装`yum-utils`，这是一个提供`yum-config-manager`实用工具的包，它将用于添加Docker仓库。您可以通过以下命令安装它（如果您还没有安装的话）：

```shell
yum install -y yum-utils
```

### 4.2设置stable版本的Docker镜像仓库

使用`yum-config-manager`添加Docker的官方仓库。请根据您的操作系统版本和稳定性需求选择适当的仓库（比如stable或test）：

```shell
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
```

在6安装Docker环境 一般会遇到以下报错：

1   [Errno 14] curl#35 - TCP connection reset by peer

2   [Errno 12] curl#35 - Timeout

这是因为使用国外的镜像仓库基本都是超时，可以选择使用阿里云提供的镜像仓库

```shell
yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
```

## 5 更新yum软件包索引

快速缓存yum仓库的元数据，重用现有的缓存内容。这通常是在你添加或修改了yum仓库配置后，或者在检查是否有新的软件包可用之前运行的命令。

```shell
yum makecache fast
```

接下来才到了真正安装docker的步骤

## 6 安装Docker环境

安装完整的Docker环境，包括引擎、命令行工具、容器运行时以及两个增强功能的插件。

```shell
yum install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

如果提示Error downloading packages，可以多试几次。

### 安装指定版本的Docker

默认安装的是最新版的Docker，如果想安装指定版本的Docker

![image-20240410222553179](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404102225832.png)

```shell
yum list docker-ce --showduplicates | sort -r
```

![image-20240410222330012](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404102223909.png)

如3:26.0.0-1.el7对应26.0.0版本

然后使用

```shell
yum install docker-ce-VERSION_STRING docker-ce-cli-VERSION_STRING containerd.io docker-buildx-plugin docker-compose-plugin
```

如安装23.0.0版本,经过测试使用官网给出的命令是无法安装Docker的，需要加上.x86_64

```shell
yum install docker-ce-3:23.0.0-1.el7.x86_64 docker-ce-cli-3:23.0.0-1.el7.x86_64 containerd.io docker-buildx-plugin docker-compose-plugin
```

![image-20240410224143229](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404102242266.png)

## 7 启动docker

```shell
systemctl start docker
```

docker开机自启

```shell
systemctl enable docker
```

![image-20240410224603000](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404102246485.png)

## 8 测试是否安装成功

### 8.1 查看docker版本

```shell
[root@dongguo ~]# docker version
Client: Docker Engine - Community
 Version:           26.0.0
 API version:       1.45
 Go version:        go1.21.8
 Git commit:        2ae903e
 Built:             Wed Mar 20 15:21:09 2024
 OS/Arch:           linux/amd64
 Context:           default

Server: Docker Engine - Community
 Engine:
  Version:          26.0.0
  API version:      1.45 (minimum version 1.24)
  Go version:       go1.21.8
  Git commit:       8b79278
  Built:            Wed Mar 20 15:20:06 2024
  OS/Arch:          linux/amd64
  Experimental:     false
 containerd:
  Version:          1.6.28
  GitCommit:        ae07eda36dd25f8a1b98dfbf587313b99c0190bb
 runc:
  Version:          1.1.12
  GitCommit:        v1.1.12-0-g51d5e94
 docker-init:
  Version:          0.19.0
  GitCommit:        de40ad0
[root@dongguo ~]# 

```



如果没有启动Docker，那么会提示你Docker 客户端无法连接到 Docker 守护进程（daemon）。

```shell
[root@dongguo ~]# docker version
Client: Docker Engine - Community
 Version:           26.0.0
 API version:       1.45
 Go version:        go1.21.8
 Git commit:        2ae903e
 Built:             Wed Mar 20 15:21:09 2024
 OS/Arch:           linux/amd64
 Context:           default
Cannot connect to the Docker daemon at unix:///var/run/docker.sock. Is the docker daemon running?
```

### 8.2 运行官方提供的hello-world案例

![image-20240406195527018](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062025811.png)

提示本地找不到hello-world 镜像，尝试从远程仓库 library/hello-world 拉取镜像，并且下载最新的hello-world镜像

显示 Hello from Docker!   表示hello-world镜像启动正常

```shell
[root@dongguo ~]# docker run hello-world
Unable to find image 'hello-world:latest' locally
latest: Pulling from library/hello-world
c1ec31eb5944: Pull complete 
Digest: sha256:53641cd209a4fecfc68e21a99871ce8c6920b2e7502df0a20671c6fccc73a7c6
Status: Downloaded newer image for hello-world:latest

Hello from Docker!
This message shows that your installation appears to be working correctly.

To generate this message, Docker took the following steps:
 1. The Docker client contacted the Docker daemon.
 2. The Docker daemon pulled the "hello-world" image from the Docker Hub.
    (amd64)
 3. The Docker daemon created a new container from that image which runs the
    executable that produces the output you are currently reading.
 4. The Docker daemon streamed that output to the Docker client, which sent it
    to your terminal.

To try something more ambitious, you can run an Ubuntu container with:
 $ docker run -it ubuntu bash

Share images, automate workflows, and more with a free Docker ID:
 https://hub.docker.com/

For more examples and ideas, visit:
 https://docs.docker.com/get-started/
```

### 执行docker run hello-world命令Docker做了什么

![image-20240406195820810](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062024762.png)

## 9 卸载

![image-20240406195915587](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062024105.png)

### 9.1 停止docker

在使用 yum remove 命令卸载 Docker 相关的软件包之前，建议先停止 Docker 服务

```shell
systemctl stop docker
systemctl stop docker.socket
```

### 9.2 删除Docker环境

```shell
sudo yum remove docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin docker-ce-rootless-extras
```

### 9.3 删除对应文件

```shell
rm -rf /var/lib/docker
rm -rf /var/lib/containerd
```

# 配置阿里云镜像加速器

由于网络原因导致docker拉取镜像过慢、超时可以配置阿里云镜像加速器，加快docker拉取镜像的速度

阿里云云原生 https://promotion.aliyun.com/ntms/act/kubernetes.html

## 1 注册阿里云账号（可以用淘宝账号）

## 2 获得加速器地址连接

点击控制台后

### 2.1点击左侧菜单栏，找到容器-容器镜像服务

![image-20240406200639566](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062025273.png)

### 2.2点击镜像加速器

![image-20240406200818262](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062025137.png)

### 2.3获取加速器地址

![image-20240406200847144](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062025312.png)

复制加速器地址

### 2.4配置镜像加速器

![image-20240406200930092](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404062025504.png)

#### 2.4.1创建文件夹

```shell
mkdir -p /etc/docker
```

#### 2.4.2将JSON串写入到daemon.json

/etc/docker/daemon.json是Docker的核心配置文件。

```shell
tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://vpmkvcwz.mirror.aliyuncs.com"]
}
EOF
```

#### 2.4.3重启docker

```shell
#重新加载 systemd 的配置文件
systemctl daemon-reload 
#重启docker
systemctl restart docker
```

