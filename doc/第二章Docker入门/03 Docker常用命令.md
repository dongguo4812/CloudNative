参考官网：https://docs.docker.com/reference/cli/docker/

# 帮助启动类命令

启动docker： systemctl start docker

停止docker： systemctl stop docker

重启docker： systemctl restart docker

查看docker状态： systemctl status docker

设置开机启动： systemctl enable docker

查看docker概要信息： docker info

查看docker总体帮助文档： docker --help

查看docker总体帮助文档： docker --help

# 镜像命令

## docker images 列出本地主机上的镜像

```shell
docker images 
```

**OPTIONS说明：**

-a :列出本地所有的镜像（含历史映像层）

-q :只显示镜像ID。

![image-20240406204200578](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071035140.png)

**各个表头参数说明:**

REPOSITORY：表示镜像的仓库源

TAG：镜像的标签版本号 不指定显示最新的（latest）一个版本号

IMAGE ID：镜像ID

CREATED：镜像创建时间

SIZE：镜像大小

同一仓库源可以有多个TAG版本，代表这个仓库源的不同个版本，我们使用 REPOSITORY:TAG 来定义不同的镜像。

如果你不指定一个镜像的版本标签，例如你只使用 ubuntu，docker 将默认使用 ubuntu:latest 镜像

## docker search 查找某个镜像

从远程仓库搜索某个镜像

![image-20240406204338498](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071035169.png)

**OPTIONS说明：**

--limit : 只列出N个镜像，默认25个 

**各个表头参数说明:**

![image-20240406204414160](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071034729.png)

## docker pul下载镜像

```shell
docker pull 镜像名字[:TAG]
```

没有TAG就是下载最新版，相当于

```shell
docker pull 镜像名字:latest
```

如下载ubuntu

```shell
[root@dongguo ~]# docker pull ubuntu
Using default tag: latest
latest: Pulling from library/ubuntu
7b1a6ab2e44d: Pull complete 
Digest: sha256:626ffe58f6e7566e00254b638eb7e0f3b11d4da9675088f4781a50ae288f3322
Status: Downloaded newer image for ubuntu:latest
docker.io/library/ubuntu:latest
[root@dongguo ~]# docker images
REPOSITORY    TAG       IMAGE ID       CREATED       SIZE
ubuntu        latest    ba6acccedd29   2 years ago   72.8MB
hello-world   latest    feb5d9fea6a5   2 years ago   13.3kB
```

## docker system df 查看镜像/容器/数据卷所占的空间

```shell
[root@dongguo ~]# docker system df 
TYPE            TOTAL     ACTIVE    SIZE      RECLAIMABLE
Images          2         1         72.79MB   72.78MB (99%)
Containers      1         0         0B        0B
Local Volumes   0         0         0B        0B
Build Cache     0         0         0B        0B
```

## docker rmi删除镜像

###  删除单个镜像

```
docker rmi  镜像ID/镜像名:TAG
```

如删除hello-world镜像

```shell
[root@dongguo ~]# docker images
REPOSITORY    TAG       IMAGE ID       CREATED       SIZE
ubuntu        latest    ba6acccedd29   2 years ago   72.8MB
hello-world   latest    feb5d9fea6a5   2 years ago   13.3kB
[root@dongguo ~]# docker rmi hello-world
Error response from daemon: conflict: unable to remove repository reference "hello-world" (must force) - container ee769566584a is using its referenced image feb5d9fea6a5
[root@dongguo ~]# docker rmi -f hello-world
Untagged: hello-world:latest
Untagged: hello-world@sha256:2498fce14358aa50ead0cc6c19990fc6ff866ce72aeb5546e1d59caac3d0d60f
Deleted: sha256:feb5d9fea6a5e9606aa995e879d862b825965ba48de054caab5ef356dc6b3412
[root@dongguo ~]# docker images
REPOSITORY   TAG       IMAGE ID       CREATED       SIZE
ubuntu       latest    ba6acccedd29   2 years ago   72.8MB
```

docker rmi hello-world提示该镜像被容器ee769566584a使用，无法被删除

使用--force 或-f 表示强制删除该镜像

### 删除多个镜像

```shell
docker rmi -f 镜像名1:TAG 镜像名2:TAG 
```

### 删除全部镜像

```shell
docker rmi -f $(docker images -qa)
```

docker images -qa表示获取全部镜像ID 

将docker images -qa 的结果传递给 docker rmi -f 命令

# 容器命令

有镜像才能创建容器，这是根本前提(下载一个CentOS或者ubuntu镜像演示)，本次使用ubuntu演示

```shell
[root@dongguo ~]# docker images
REPOSITORY   TAG       IMAGE ID       CREATED       SIZE
ubuntu       latest    ba6acccedd29   2 years ago   72.8MB
```

## docker run 新建+启动容器

```shell
docker run [OPTIONS] IMAGE [COMMAND] [ARG...]
```

OPTIONS说明（常用）：

注意有些是一个减号，有些是两个减号

--name="容器新名字"       为容器指定一个名称；

-d: 后台运行容器并返回容器ID，也即启动守护式容器(后台运行)；

-i：以交互模式运行容器，通常与 -t 同时使用；

-t：为容器重新分配一个伪输入终端，通常与 -i 同时使用；

也即启动交互式容器(前台有伪终端，等待交互)；

-P: 随机端口映射，大写P

-p: 指定端口映射，小写p

![image-20240406212804327](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071034526.png)

端口映射-p 8080:80，先访问宿主机的8080端口，才能通过docker找到并访问到容器的80端口

### 启动交互式容器(前台命令行)

```shell
docker run -it ubuntu /bin/bash
```

![image-20240407073720705](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071034848.png)

参数说明：

-i: 交互式操作。

-t: 终端。

-it:以交互模式启动一个容器,

ubuntu : ubuntu 镜像。

/bin/bash：表示在容器内部打开bash终端，用户可以在终端上执行命令和查看容器内部文件系统。放在镜像名后的是命令，这里我们希望有个交互式 Shell，因此用的是 /bin/bash。

要退出终端，直接输入 exit:

### 启动守护式容器（后台运行）

在大部分的场景下，我们希望 docker 的服务是在后台运行的，

我们可以过 -d 指定容器的后台运行模式。

```shell
docker run -d ubuntu
```

![image-20240407073812154](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071034919.png)

使用docker ps 进行查看, 会发现容器已经退出

很重要的要说明的一点: Docker容器如果想要在后台运行,就必须有一个前台进程.

即容器运行的命令如果不是那些一直挂起的命令（比如运行top，tail），就是会自动退出的。



这个是docker的机制问题,比如你的web容器,我们以nginx为例，正常情况下,

我们配置启动服务只需要启动响应的service即可。例如service nginx start

但是,这样做,nginx为后台进程模式运行,就导致docker前台没有运行的应用,

这样的容器后台启动后,会立即停止，因为他觉得他没事可做了.

所以，最佳的解决方案是,将你要运行的程序以前台进程的形式运行，

常见就是**交互式操作**模式，表示我还有交互操作，别中断。



接下来以redis为例演示前后台运行

1.下载redis镜像

![image-20240407074223263](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071034485.png)

2.启动交互式运行redis

![image-20240407074301965](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071034011.png)

对于redis、mysql这些应用，我们一般采用后台守护式启动

3.后台守护式运行redis

![image-20240407074354944](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071034694.png)

## docker ps 列出当前所有正在运行的容器

```shell
docker ps [OPTIONS]
```

OPTIONS说明（常用）：

-a :列出当前所有正在运行的容器+历史上运行过的

-l :显示最近创建的容器。

-n：显示最近n个创建的容器。

-q :静默模式，只显示容器编号。

![image-20240407074527304](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071034997.png)

## exit（Ctrl+d）/ctrl+p+q 退出容器

### exit

run启动容器，exit退出，容器停止

![image-20240407075326225](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071034464.png)

Ctrl+d与exit是同一效果

![image-20240407080502418](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071034582.png)

### ctrl+p 和ctrl+p组合键

run启动容器，ctrl+p 和ctrl+p退出，容器不停止

![image-20240407075356756](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071034743.png)

## docker start 启动已停止运行的容器

CONTAINER ID：72d7f47f90e6是使用exit退出的容器，容器已停止

![image-20240407075623187](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071034419.png)

## docker restart 重启容器

![image-20240407075825105](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071034226.png)

## docker stop 停止容器

![image-20240407075910618](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071034793.png)

## docker kill 强制停止容器

![image-20240407080019953](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071034309.png)

## docker rm 删除已停止的容器

![image-20240407080155694](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071034289.png)

启动的容器不允许删除，先停止容器，再进行删除

或者强制删除

![image-20240407080219554](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071033642.png)

### 一次性删除多个容器实例

两种方式

1.将docker ps -a -q返回的结果传递给docker rmi -f 命令

```shell
docker rm -f $(docker ps -a -q)
```

2.将docker ps -a -q 作为参数xargs 执行命令

```shell
docker ps -a -q | xargs docker rm
```

# 其它

## docker logs查看容器日志

使用启动守护式容器（后台运行）启动的容器，可以使用docker logs查看容器日志

![image-20240407081735317](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071033486.png)

## docker top查看容器内运行的进程

![image-20240407081817810](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071033315.png)

## docker inspect查看容器内部细节

通过这个命令，你可以获取对象的各种属性和其值。这些属性可能包括对象的 ID、创建时间、所在的网络、挂载的卷、容器的状态、运行时参数等等。

```shell
[root@dongguo ~]# docker inspect 9db60166fea7
[
    {
        "Id": "9db60166fea76b6bd7956e8c2ec62190173e58ad9f4ea88d6f8ccc4310ef26ca",
        "Created": "2024-04-07T00:17:07.982831332Z",
        "Path": "docker-entrypoint.sh",
        "Args": [
            "redis-server"
        ],
        "State": {
            "Status": "running",
            "Running": true,
            "Paused": false,
            "Restarting": false,
            "OOMKilled": false,
            "Dead": false,
            "Pid": 14171,
            "ExitCode": 0,
            "Error": "",
            "StartedAt": "2024-04-07T00:17:08.104520871Z",
            "FinishedAt": "0001-01-01T00:00:00Z"
        },
        "Image": "sha256:7614ae9453d1d87e740a2056257a6de7135c84037c367e1fffa92ae922784631",
        "ResolvConfPath": "/var/lib/docker/containers/9db60166fea76b6bd7956e8c2ec62190173e58ad9f4ea88d6f8ccc4310ef26ca/resolv.conf",
        "HostnamePath": "/var/lib/docker/containers/9db60166fea76b6bd7956e8c2ec62190173e58ad9f4ea88d6f8ccc4310ef26ca/hostname",
        "HostsPath": "/var/lib/docker/containers/9db60166fea76b6bd7956e8c2ec62190173e58ad9f4ea88d6f8ccc4310ef26ca/hosts",
        "LogPath": "/var/lib/docker/containers/9db60166fea76b6bd7956e8c2ec62190173e58ad9f4ea88d6f8ccc4310ef26ca/9db60166fea76b6bd7956e8c2ec62190173e58ad9f4ea88d6f8ccc4310ef26ca-json.log",
        "Name": "/clever_shannon",
        "RestartCount": 0,
        "Driver": "overlay2",
        "Platform": "linux",
        "MountLabel": "",
        "ProcessLabel": "",
        "AppArmorProfile": "",
        "ExecIDs": null,
        "HostConfig": {
            "Binds": null,
            "ContainerIDFile": "",
            "LogConfig": {
                "Type": "json-file",
                "Config": {}
            },
            "NetworkMode": "default",
            "PortBindings": {},
            "RestartPolicy": {
                "Name": "no",
                "MaximumRetryCount": 0
            },
            "AutoRemove": false,
            "VolumeDriver": "",
            "VolumesFrom": null,
            "ConsoleSize": [
                35,
                184
            ],
            "CapAdd": null,
            "CapDrop": null,
            "CgroupnsMode": "host",
            "Dns": [],
            "DnsOptions": [],
            "DnsSearch": [],
            "ExtraHosts": null,
            "GroupAdd": null,
            "IpcMode": "private",
            "Cgroup": "",
            "Links": null,
            "OomScoreAdj": 0,
            "PidMode": "",
            "Privileged": false,
            "PublishAllPorts": false,
            "ReadonlyRootfs": false,
            "SecurityOpt": null,
            "UTSMode": "",
            "UsernsMode": "",
            "ShmSize": 67108864,
            "Runtime": "runc",
            "Isolation": "",
            "CpuShares": 0,
            "Memory": 0,
            "NanoCpus": 0,
            "CgroupParent": "",
            "BlkioWeight": 0,
            "BlkioWeightDevice": [],
            "BlkioDeviceReadBps": [],
            "BlkioDeviceWriteBps": [],
            "BlkioDeviceReadIOps": [],
            "BlkioDeviceWriteIOps": [],
            "CpuPeriod": 0,
            "CpuQuota": 0,
            "CpuRealtimePeriod": 0,
            "CpuRealtimeRuntime": 0,
            "CpusetCpus": "",
            "CpusetMems": "",
            "Devices": [],
            "DeviceCgroupRules": null,
            "DeviceRequests": null,
            "MemoryReservation": 0,
            "MemorySwap": 0,
            "MemorySwappiness": null,
            "OomKillDisable": false,
            "PidsLimit": null,
            "Ulimits": [],
            "CpuCount": 0,
            "CpuPercent": 0,
            "IOMaximumIOps": 0,
            "IOMaximumBandwidth": 0,
            "MaskedPaths": [
                "/proc/asound",
                "/proc/acpi",
                "/proc/kcore",
                "/proc/keys",
                "/proc/latency_stats",
                "/proc/timer_list",
                "/proc/timer_stats",
                "/proc/sched_debug",
                "/proc/scsi",
                "/sys/firmware",
                "/sys/devices/virtual/powercap"
            ],
            "ReadonlyPaths": [
                "/proc/bus",
                "/proc/fs",
                "/proc/irq",
                "/proc/sys",
                "/proc/sysrq-trigger"
            ]
        },
        "GraphDriver": {
            "Data": {
                "LowerDir": "/var/lib/docker/overlay2/68ac73f29b5daf529f540709303d1003cc8d66b558f3c6882024b44c8196bd38-init/diff:/var/lib/docker/overlay2/8e10503735fb885729e95443befccf10b9c22e383649d9b7de0aa4ec2c3c220b/diff:/var/lib/docker/overlay2/58ed73bd226a49abd5c97d1617aaf07b291048c18cba1de92f00f70b7ebc5b51/diff:/var/lib/docker/overlay2/3ed3e06d09c3c8556551f8b652c37904d42414673641bdebc71052b35c5a2a47/diff:/var/lib/docker/overlay2/c0dcf4e38b9253511bf82dd529e787af476a7aecfaf894a3c52b961ff7bec91d/diff:/var/lib/docker/overlay2/6a933155d2b5c553ababeab1ddcf158788503e4897eeff5e90eb8675dbdc5972/diff:/var/lib/docker/overlay2/9da1b467313707c5572bb6999f10b40e9ba33d55a1e800e34bede5510a3ab0df/diff",
                "MergedDir": "/var/lib/docker/overlay2/68ac73f29b5daf529f540709303d1003cc8d66b558f3c6882024b44c8196bd38/merged",
                "UpperDir": "/var/lib/docker/overlay2/68ac73f29b5daf529f540709303d1003cc8d66b558f3c6882024b44c8196bd38/diff",
                "WorkDir": "/var/lib/docker/overlay2/68ac73f29b5daf529f540709303d1003cc8d66b558f3c6882024b44c8196bd38/work"
            },
            "Name": "overlay2"
        },
        "Mounts": [
            {
                "Type": "volume",
                "Name": "1aab074addde8845497d0f8dab8d46f238890353c883b0f64330fc5038e9aca6",
                "Source": "/var/lib/docker/volumes/1aab074addde8845497d0f8dab8d46f238890353c883b0f64330fc5038e9aca6/_data",
                "Destination": "/data",
                "Driver": "local",
                "Mode": "",
                "RW": true,
                "Propagation": ""
            }
        ],
        "Config": {
            "Hostname": "9db60166fea7",
            "Domainname": "",
            "User": "",
            "AttachStdin": false,
            "AttachStdout": false,
            "AttachStderr": false,
            "ExposedPorts": {
                "6379/tcp": {}
            },
            "Tty": false,
            "OpenStdin": false,
            "StdinOnce": false,
            "Env": [
                "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
                "GOSU_VERSION=1.12",
                "REDIS_VERSION=6.2.6",
                "REDIS_DOWNLOAD_URL=http://download.redis.io/releases/redis-6.2.6.tar.gz",
                "REDIS_DOWNLOAD_SHA=5b2b8b7a50111ef395bf1c1d5be11e6e167ac018125055daa8b5c2317ae131ab"
            ],
            "Cmd": [
                "redis-server"
            ],
            "Image": "redis",
            "Volumes": {
                "/data": {}
            },
            "WorkingDir": "/data",
            "Entrypoint": [
                "docker-entrypoint.sh"
            ],
            "OnBuild": null,
            "Labels": {}
        },
        "NetworkSettings": {
            "Bridge": "",
            "SandboxID": "58611eb6f09ebae014a091304f1f8aa649fd85e2c9132168f3bb266fd025ad93",
            "SandboxKey": "/var/run/docker/netns/58611eb6f09e",
            "Ports": {
                "6379/tcp": null
            },
            "HairpinMode": false,
            "LinkLocalIPv6Address": "",
            "LinkLocalIPv6PrefixLen": 0,
            "SecondaryIPAddresses": null,
            "SecondaryIPv6Addresses": null,
            "EndpointID": "fae4531ab81173ed1d6129081b64646b84873e3d0de64f9223408de250ae6faf",
            "Gateway": "172.17.0.1",
            "GlobalIPv6Address": "",
            "GlobalIPv6PrefixLen": 0,
            "IPAddress": "172.17.0.2",
            "IPPrefixLen": 16,
            "IPv6Gateway": "",
            "MacAddress": "02:42:ac:11:00:02",
            "Networks": {
                "bridge": {
                    "IPAMConfig": null,
                    "Links": null,
                    "Aliases": null,
                    "MacAddress": "02:42:ac:11:00:02",
                    "NetworkID": "19541a4fac975a56c967ca166e0f2a2828f55ad41c583a3e5e7c80d5383d5b1d",
                    "EndpointID": "fae4531ab81173ed1d6129081b64646b84873e3d0de64f9223408de250ae6faf",
                    "Gateway": "172.17.0.1",
                    "IPAddress": "172.17.0.2",
                    "IPPrefixLen": 16,
                    "IPv6Gateway": "",
                    "GlobalIPv6Address": "",
                    "GlobalIPv6PrefixLen": 0,
                    "DriverOpts": null,
                    "DNSNames": null
                }
            }
        }
    }
]
```

## 进入正在运行的容器并以命令行交互

还是以ubuntu为例

### docker exec

使用ctrl+p 和ctrl+p退出容器，然后使用docker exec进入容器

![image-20240407093129288](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071033217.png)

### docker attach

![image-20240407093524943](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071033149.png)





### 区别

exec 是在容器中打开新的终端，并且可以启动新的进程

用exit退出，不会导致容器的停止。

![image-20240407093459942](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071033374.png)

attach 直接进入容器启动命令的终端，不会启动新的进程

用exit退出，会导致容器的停止。

![image-20240407093547558](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071033228.png)

推荐使用 docker exec 命令，因为退出容器终端，不会导致容器的停止。





一般用-d后台启动的程序，再用exec进入对应容器实例，以redis为例

```shell
[root@dongguo ~]# docker ps
CONTAINER ID   IMAGE     COMMAND                   CREATED             STATUS             PORTS      NAMES
9db60166fea7   redis     "docker-entrypoint.s…"   About an hour ago   Up About an hour   6379/tcp   clever_shannon
[root@dongguo ~]# docker exec -it 9db60166fea7 /bin/bash
root@9db60166fea7:/data# redis-cli -p 6379
127.0.0.1:6379> ping
PONG
127.0.0.1:6379> set k1 v1
OK
127.0.0.1:6379> get k1
"v1"
```



## docker cp 从容器内拷贝文件到主机上

```shell
docker cp  容器ID:容器内路径 目的主机路径
```

在容器内的/tmp目录创建a.txt文件，使用ctrl+p 和ctrl+p退出容器，然后将容器内的/tmp/a.txt拷贝到宿主机的/opt目录下

```shell
[root@dongguo ~]# docker run -it ubuntu /bin/bash
root@6e304bc65175:/# pwd 
/
root@6e304bc65175:/# cd /tmp
root@6e304bc65175:/tmp# touch a.txt
root@6e304bc65175:/tmp# ll
total 0
drwxrwxrwt. 1 root root 19 Apr  7 01:50 ./
drwxr-xr-x. 1 root root 17 Apr  7 01:49 ../
-rw-r--r--. 1 root root  0 Apr  7 01:50 a.txt
root@6e304bc65175:/tmp# [root@dongguo ~]# 
[root@dongguo ~]# docker ps
CONTAINER ID   IMAGE     COMMAND                   CREATED         STATUS         PORTS      NAMES
6e304bc65175   ubuntu    "/bin/bash"               2 minutes ago   Up 2 minutes              frosty_wiles
9db60166fea7   redis     "docker-entrypoint.s…"   2 hours ago     Up 2 hours     6379/tcp   clever_shannon
[root@dongguo opt]# docker cp 6e304bc65175:/tmp/a.txt /opt
Successfully copied 1.54kB to /opt
[root@dongguo opt]# cd /opt
[root@dongguo opt]# ll
总用量 0
-rw-r--r--. 1 root root  0 4月   7 09:50 a.txt
drwx--x--x. 4 root root 28 4月   6 19:54 containerd
drwxr-xr-x. 3 root root 26 2月  18 2023 software
```

## 导入和导出容器

### docker export

导出整个容器的内容留作为一个tar归档文件

```shell
docker export 容器ID > 文件名.tar
```



```shell
[root@dongguo /]# cd /opt
[root@dongguo opt]# docker ps
CONTAINER ID   IMAGE     COMMAND                   CREATED          STATUS          PORTS      NAMES
6e304bc65175   ubuntu    "/bin/bash"               34 minutes ago   Up 34 minutes              frosty_wiles
9db60166fea7   redis     "docker-entrypoint.s…"   2 hours ago      Up 2 hours      6379/tcp   clever_shannon
[root@dongguo opt]# docker export 6e304bc65175 > abc.tar
[root@dongguo opt]# ll
总用量 73400
-rw-r--r--. 1 root root 75158016 4月   7 10:24 abc.tar
-rw-r--r--. 1 root root        0 4月   7 09:50 a.txt
drwx--x--x. 4 root root       28 4月   6 19:54 containerd
drwxr-xr-x. 3 root root       26 2月  18 2023 software
```

### docker import

从tar包中的内容创建一个新的文件系统再导入为镜像

```shell
cat 文件名.tar | docker import - 镜像用户/镜像名:镜像版本号
```

生成一个新的镜像import/ubutu:1.0.0 ，运行容器，/tmp目录下还存在之前创建的a.txt文件

```shell
[root@dongguo opt]# docker rm -f 6e304bc65175
6e304bc65175
[root@dongguo opt]# cat abc.tar | docker import - import/ubutu:1.0.0
sha256:47658ef456f938dab279d4faa412285035e38fd0f0c897fb095fce4a6608ac12
9db60166fea7   redis     "docker-entrypoint.s…"   2 hours ago   Up 2 hours   6379/tcp   clever_shannon
[root@dongguo opt]# docker images
REPOSITORY     TAG       IMAGE ID       CREATED          SIZE
import/ubutu   1.0.0     47658ef456f9   58 seconds ago   72.8MB
redis          latest    7614ae9453d1   2 years ago      113MB
ubuntu         latest    ba6acccedd29   2 years ago      72.8MB
[root@dongguo opt]# docker run -it 47658ef456f9 /bin/bash
root@cc6379aa1d31:/# cd /tmp
root@cc6379aa1d31:/tmp# ll
total 0
drwxrwxrwt. 2 root root 19 Apr  7 01:50 ./
drwxr-xr-x. 1 root root  6 Apr  7 02:30 ../
-rw-r--r--. 1 root root  0 Apr  7 01:50 a.txt
```



# 常用命令汇总

![image-20240407103209255](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071033928.png)

attach    Attach to a running container                 # 当前 shell 下 attach 连接指定运行镜像
build     Build an image from a Dockerfile              # 通过 Dockerfile 定制镜像
commit    Create a new image from a container changes   # 提交当前容器为新的镜像
cp        Copy files/folders from the containers filesystem to the host path   #从容器中拷贝指定文件或者目录到宿主机中
create    Create a new container                        # 创建一个新的容器，同 run，但不启动容器
diff      Inspect changes on a container's filesystem   # 查看 docker 容器变化
events    Get real time events from the server          # 从 docker 服务获取容器实时事件
exec      Run a command in an existing container        # 在已存在的容器上运行命令
export    Stream the contents of a container as a tar archive   # 导出容器的内容流作为一个 tar 归档文件[对应 import ]
history   Show the history of an image                  # 展示一个镜像形成历史
images    List images                                   # 列出系统当前镜像
import    Create a new filesystem image from the contents of a tarball # 从tar包中的内容创建一个新的文件系统映像[对应export]
info      Display system-wide information               # 显示系统相关信息
inspect   Return low-level information on a container   # 查看容器详细信息
kill      Kill a running container                      # kill 指定 docker 容器
load      Load an image from a tar archive              # 从一个 tar 包中加载一个镜像[对应 save]
login     Register or Login to the docker registry server    # 注册或者登陆一个 docker 源服务器
logout    Log out from a Docker registry server          # 从当前 Docker registry 退出
logs      Fetch the logs of a container                 # 输出当前容器日志信息
port      Lookup the public-facing port which is NAT-ed to PRIVATE_PORT    # 查看映射端口对应的容器内部源端口
pause     Pause all processes within a container        # 暂停容器
ps        List containers                               # 列出容器列表
pull      Pull an image or a repository from the docker registry server   # 从docker镜像源服务器拉取指定镜像或者库镜像
push      Push an image or a repository to the docker registry server    # 推送指定镜像或者库镜像至docker源服务器
restart   Restart a running container                   # 重启运行的容器
rm        Remove one or more containers                 # 移除一个或者多个容器
rmi       Remove one or more images       # 移除一个或多个镜像[无容器使用该镜像才可删除，否则需删除相关容器才可继续或 -f 强制删除]
run       Run a command in a new container              # 创建一个新的容器并运行一个命令
save      Save an image to a tar archive                # 保存一个镜像为一个 tar 包[对应 load]
search    Search for an image on the Docker Hub         # 在 docker hub 中搜索镜像
start     Start a stopped containers                    # 启动容器
stop      Stop a running containers                     # 停止容器
tag       Tag an image into a repository                # 给源中镜像打标签
top       Lookup the running processes of a container   # 查看容器中运行的进程信息
unpause   Unpause a paused container                    # 取消暂停容器
version   Show the docker version information           # 查看 docker 版本号
wait      Block until a container stops, then print its exit code   # 截取容器停止时的退出状态值