# Docker镜像介绍

Docker镜像是一种轻量级、可执行的独立软件包，它包含运行某个软件所需的所有内容，我们把应用程序和配置依赖打包好形成一个可交付的运行环境(包括代码、运行时需要的库、环境变量和配置文件等)，这个打包好的运行环境就是image镜像文件。

只有通过这个镜像文件才能生成Docker容器实例(类似Java中new出来一个对象)。

# 镜像的分层

以我们的pull为例，在下载的过程中我们可以看到docker的镜像好像是在一层一层的在下载

```shell
[root@dongguo /]# docker pull tomcat
Using default tag: latest
latest: Pulling from library/tomcat
0e29546d541c: Pull complete 
9b829c73b52b: Pull complete 
cb5b7ae36172: Pull complete 
6494e4811622: Pull complete 
668f6fcc5fa5: Pull complete 
dc120c3e0290: Pull complete 
8f7c0eebb7b1: Pull complete 
77b694f83996: Pull complete 
0f611256ec3a: Pull complete 
4f25def12f23: Pull complete 
Digest: sha256:9dee185c3b161cdfede1f5e35e8b56ebc9de88ed3a79526939701f3537a52324
Status: Downloaded newer image for tomcat:latest
docker.io/library/tomcat:latest
[root@dongguo /]# docker images
REPOSITORY     TAG       IMAGE ID       CREATED          SIZE
tomcat         latest    fb5657adc892   2 years ago      680MB
```

拿tomcat为例，一个单独的tomcat一般只有100多M，但是docker拉取的镜像有600多M，这是为什么？

因为tomcat的运行不仅仅只需要tomcat，还需要java、centos等等依赖，所以docker的tomcat镜像中会包含有java、centos等等，所以会很大。

![image-20240407104219663](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071222247.png)





## UnionFS（联合文件系统）

UnionFS（联合文件系统）：Union文件系统（UnionFS）是一种分层、轻量级并且高性能的文件系统，它支持对文件系统的修改作为一次提交来一层层的叠加，同时可以将不同目录挂载到同一个虚拟文件系统下(unite several directories into a single virtual filesystem)。

Union 文件系统是 Docker 镜像的基础。镜像可以通过分层来进行继承，基于基础镜像（没有父镜像），可以制作各种具体的应用镜像。

特性：一次同时加载多个文件系统，但从外面看起来，只能看到一个文件系统，联合加载会把各层文件系统叠加起来，这样最终的文件系统会包含所有底层的文件和目录



# Docker镜像加载原理

docker的镜像实际上由一层一层的文件系统组成，这种层级的文件系统主要依赖于UnionFS（联合文件系统）。

当Docker需要加载一个镜像时，它会按照镜像的层顺序从上到下依次加载。首先，它会加载最底层的基础镜像层，然后依次加载后续的层，直到整个镜像加载完成。

Docker镜像中bootfs和rootfs的概念：

bootfs(boot file system)主要包含bootloader（加载器）和kernel（内核）, bootloader主要是引导加载kernel,

Linux刚启动时会加载bootfs文件系统，在Docker镜像的最底层是引导文件系统bootfs。这一层与我们典型的Linux/Unix系统是一样的，包含boot加载器和内核。当boot加载完成之后整个内核就都在内存中了，此时内存的使用权已由bootfs转交给内核，此时系统也会卸载bootfs。在Docker镜像中，bootfs是一个只读的层，一旦内核加载完成，系统就会卸载bootfs，将控制权完全交给内核。

rootfs (root file system) ，在bootfs之上。包含的就是典型 Linux 系统中的 /dev, /proc, /bin, /etc 等标准目录和文件。rootfs就是各种不同的操作系统发行版，比如Ubuntu，Centos等等。

![image-20240407104311210](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071222126.png)



思考：平时我们安装进虚拟机的ubuntu都是几个G，为什么docker这里才几十M？？

```shell
[root@dongguo /]# docker images
REPOSITORY     TAG       IMAGE ID       CREATED          SIZE
ubuntu         latest    ba6acccedd29   2 years ago      72.8MB
```

Docker使用了容器化技术，它共享了主机的内核，并且只需要提供应用程序所需的文件系统和其他资源。这意味着Docker镜像不需要包含完整的操作系统内核，而只需要包含应用程序及其依赖的库和文件。这就是为什么Docker镜像通常比虚拟机镜像小得多的原因。

具体到Ubuntu的Docker镜像，它通常只包含了一个精简的Ubuntu文件系统（rootfs），这个文件系统只包含了运行Ubuntu所需的最基本的命令、工具和程序库。因为Docker容器共享了主机的内核，所以不需要在镜像中包含内核。这就大大减小了镜像的大小。



# 为什么 Docker 镜像要采用这种分层结构呢

**共享资源**：Docker镜像的分层设计允许多个镜像共享相同的基础层。这在多个镜像从相同的基础镜像构建时尤为有用。通过在磁盘上仅保存一份基础镜像，并在内存中仅加载一份基础镜像，可以为所有容器服务，从而节省存储空间。

**灵活性**：分层结构使得Docker镜像在构建、部署和更新过程中非常灵活。每个分层都是独立的，包含特定的更改，这允许镜像的构建过程更加模块化。此外，基于现有的镜像构建新的镜像时，只需在现有镜像的基础上添加新的分层，无需重新复制整个镜像，这大大提高了构建效率。

**Copy-on-Write（COW）特性**：Docker利用COW技术确保对容器的修改仅发生在容器层，而镜像层保持只读。这意味着修改被限制在单个容器内，不会影响到其他容器共享的基础镜像。这种设计保证了镜像的一致性和稳定性。





Docker镜像层都是只读的，容器层是可写的。
当容器启动时，一个新的可写层被加载到镜像的顶部。这一层通常被称作“容器层”，“容器层”之下的都叫“镜像层”。

所有对容器的改动 - 无论添加、删除、还是修改文件都只会发生在容器层中。

![image-20240407112602786](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071222703.png)



# Docker镜像存储原理

https://docs.docker.com/storage/storagedriver/

Docker映像由一系列层构建而成。每一层代表映像docker文件中的一条指令。除了最后一层以外的每一层都是只读的

![image-20240412124030900](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404131125875.png)

这个docker文件包含四个命令。修改文件系统的命令会创建一个层。

FROM语句首先从ubuntu:22.04图像创建一个层。“标注”命令仅修改图像的元数据，而不会生成新图层。

COPY命令从Docker客户端的当前目录中添加一些文件。

第一个运行命令使用make命令构建应用程序，并将结果写入新层。第二个运行命令删除缓存目录，并将结果写入新图层。

最后，CMD指令指定在容器中运行什么命令，这只会修改图像的元数据，而不会产生图像层。



每一层都只是与前一层的一组差异。请注意，添加和删除文件都会产生一个新层。在上面的例子中，$HOME/。缓存目录将被移除，但仍将在先前图层中可用，并增加到影像的总大小。

![Layers of a container based on the Ubuntu image](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404131126486.webp)

## nginx镜像是怎么存储的

`nginx` 镜像的详细信息：

```shell
[root@dongguo dongguo]# docker image inspect nginx
[
    {
        "Id": "sha256:605c77e624ddb75e6110f997c58876baa13f8754486b461117934b24a9dc3a85",
        "RepoTags": [
            "dongguo274812/nginx:1.0",
            "nginx:latest"
        ],
        "RepoDigests": [
            "dongguo274812/nginx@sha256:ee89b00528ff4f02f2405e4ee221743ebc3f8e8dd0bfd5c4c20a2fa2aaa7ede3"
        ],
        "Parent": "",
        "Comment": "",
        "Created": "2021-12-29T19:28:29.892199479Z",
        "DockerVersion": "20.10.7",
        "Author": "",
        "Config": {
            "Hostname": "",
            "Domainname": "",
            "User": "",
            "AttachStdin": false,
            "AttachStdout": false,
            "AttachStderr": false,
            "ExposedPorts": {
                "80/tcp": {}
            },
            "Tty": false,
            "OpenStdin": false,
            "StdinOnce": false,
            "Env": [
                "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
                "NGINX_VERSION=1.21.5",
                "NJS_VERSION=0.7.1",
                "PKG_RELEASE=1~bullseye"
            ],
            "Cmd": [
                "nginx",
                "-g",
                "daemon off;"
            ],
            "Image": "sha256:82941edee2f4d17c55563bb926387c3ae39fa1a99777f088bc9d3db885192209",
            "Volumes": null,
            "WorkingDir": "",
            "Entrypoint": [
                "/docker-entrypoint.sh"
            ],
            "OnBuild": null,
            "Labels": {
                "maintainer": "NGINX Docker Maintainers <docker-maint@nginx.com>"
            },
            "StopSignal": "SIGQUIT"
        },
        "Architecture": "amd64",
        "Os": "linux",
        "Size": 141479488,
        "GraphDriver": {
            "Data": {
                "LowerDir": "/var/lib/docker/overlay2/3a79e94659d539716ec70474a95f848c313744864a918fdc0a6ee1e732f41c8f/diff:/var/lib/docker/overlay2/41b86bb4bb7daf807a4abaa221091e9ca2980e74d4706b4cdee292c96dc0c7f8/diff:/var/lib/docker/overlay2/6082edde72aded43159d5087b571da48bb0539154aa874da71e93cdd00010ddc/diff:/var/lib/docker/overlay2/604d9a83dc7f61ef7b636b6201a3412a45b152139c1c47346ce94e3de14a6f84/diff:/var/lib/docker/overlay2/ebfd67b20364e3bedf510bbd5ed2919ca40a736646eb7493d03cdd1c577c94af/diff",
                "MergedDir": "/var/lib/docker/overlay2/a6c3b7a3e9712e630f59f82646f330f7196d801b77173bdf78c5e1e108e740e3/merged",
                "UpperDir": "/var/lib/docker/overlay2/a6c3b7a3e9712e630f59f82646f330f7196d801b77173bdf78c5e1e108e740e3/diff",
                "WorkDir": "/var/lib/docker/overlay2/a6c3b7a3e9712e630f59f82646f330f7196d801b77173bdf78c5e1e108e740e3/work"
            },
            "Name": "overlay2"
        },
        "RootFS": {
            "Type": "layers",
            "Layers": [
                "sha256:2edcec3590a4ec7f40cf0743c15d78fb39d8326bc029073b41ef9727da6c851f",
                "sha256:e379e8aedd4d72bb4c529a4ca07a4e4d230b5a1d3f7a61bc80179e8f02421ad8",
                "sha256:b8d6e692a25e11b0d32c5c3dd544b71b1085ddc1fddad08e68cbd7fda7f70221",
                "sha256:f1db227348d0a5e0b99b15a096d930d1a69db7474a1847acbc31f05e4ef8df8c",
                "sha256:32ce5f6a5106cc637d09a98289782edf47c32cb082dc475dd47cbf19a4f866da",
                "sha256:d874fd2bc83bb3322b566df739681fbd2248c58d3369cb25908d68e7ed6040a6"
            ]
        },
        "Metadata": {
            "LastTagTime": "2024-04-11T21:51:47.493911151+08:00"
        }
    }
]
```

GraphDriver中指明了怎么存：

LowerDir：列出了组成当前镜像的所有底层目录。这些目录代表了镜像的各个层。每一层都包含了对文件系统的修改，并且这些修改是相对于上一层的。

MergedDir：这个目录是叠加后的文件系统的视图，它包含了所有层的合并结果

UpperDir：这个目录是容器可写层的目录。当容器启动时，Docker会在`UpperDir`中创建一个新的目录，用于存放容器运行时的所有修改。这样，容器对文件系统的任何修改都只影响这个可写层，而不会影响到底层的镜像层。（所以只有容器存在文件）

WorkDir：这是一个临时工作目录，用于在叠加文件系统的操作过程中存储临时文件

`Name`字段显示为`overlay2`，这表示Docker当前使用的是`overlay2`存储驱动。`overlay2`是Docker的一个联合文件系统驱动，它允许Docker将多个文件系统层叠加在一起，形成一个统一的文件系统视图。`overlay2`驱动通过这些目录和文件，高效地管理了Docker镜像和容器的文件系统，使得Docker能够快速地创建和销毁容器，同时保持镜像的不变性。

![image-20240412112240671](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404131126087.png)



一个Nginx镜像是141MB，那么如果我启动多个Nginx容器会占用多少内存呢？

当我们启动多个nginx容器时，可以看到每个容器实际上只有1.09KB，**虚拟磁盘空间**也就是nginx容器镜像的大小是141MB，并不会占用N倍的镜像内存。

启动了多个nginx容器，它们的镜像层在磁盘上只会被存储一次。每个容器只需要存储它自己独特的容器层，这个层通常很小，因为它只包含了容器运行时的修改。

![image-20240412114044181](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404131126622.png)



下图显示了共享同一Ubuntu 15.04映像的多个容器：

![image-20240412115048982](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404131125087.png)

## The copy-on-write (CoW) strategy 容器与镜像的写时复制策略

以nginx镜像为例，多个容器共享相同镜像的文件系统层或内容（nginx.conf）。只有当某个容器或进程需要修改这个共享内容时，才会触发CoW操作,真正地将该内容(nginx.conf)复制到容器的UpperDir中，并在那里进行修改。这种策略避免了不必要的数据复制，从而提高了存储效率和性能。

![image-20240412135519908](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404131126215.png)

`nginx` 容器的详细信息

```shell
[root@dongguo ~]# docker ps
CONTAINER ID   IMAGE     COMMAND                   CREATED       STATUS       PORTS     NAMES
0335b62bd6a0   nginx     "/docker-entrypoint.…"   2 hours ago   Up 2 hours   80/tcp    sleepy_moser
8f4fc6af91ca   nginx     "/docker-entrypoint.…"   2 hours ago   Up 2 hours   80/tcp    mystifying_maxwell
77b520df55c2   nginx     "/docker-entrypoint.…"   2 hours ago   Up 2 hours   80/tcp    flamboyant_moore
[root@dongguo ~]# docker inspect 0335b62bd6a0
[
    {
        "Id": "0335b62bd6a0efb7a2c379e3ca95b5e1272ed4f75795e0fa27048b16f4919897",
        "Created": "2024-04-12T03:40:21.644744592Z",
        "Path": "/docker-entrypoint.sh",
        "Args": [
            "nginx",
            "-g",
            "daemon off;"
        ],
        "State": {
            "Status": "running",
            "Running": true,
            "Paused": false,
            "Restarting": false,
            "OOMKilled": false,
            "Dead": false,
            "Pid": 3082,
            "ExitCode": 0,
            "Error": "",
            "StartedAt": "2024-04-12T03:40:21.818254973Z",
            "FinishedAt": "0001-01-01T00:00:00Z"
        },
        "Image": "sha256:605c77e624ddb75e6110f997c58876baa13f8754486b461117934b24a9dc3a85",
        "ResolvConfPath": "/var/lib/docker/containers/0335b62bd6a0efb7a2c379e3ca95b5e1272ed4f75795e0fa27048b16f4919897/resolv.conf",
        "HostnamePath": "/var/lib/docker/containers/0335b62bd6a0efb7a2c379e3ca95b5e1272ed4f75795e0fa27048b16f4919897/hostname",
        "HostsPath": "/var/lib/docker/containers/0335b62bd6a0efb7a2c379e3ca95b5e1272ed4f75795e0fa27048b16f4919897/hosts",
        "LogPath": "/var/lib/docker/containers/0335b62bd6a0efb7a2c379e3ca95b5e1272ed4f75795e0fa27048b16f4919897/0335b62bd6a0efb7a2c379e3ca95b5e1272ed4f75795e0fa27048b16f4919897-json.log",
        "Name": "/sleepy_moser",
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
                37,
                148
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
                "LowerDir": "/var/lib/docker/overlay2/b4964b99d6255ccceaa70c06a31687d165ccba119e5487983e9aaccc1a98077c-init/diff:/var/lib/docker/overlay2/a6c3b7a3e9712e630f59f82646f330f7196d801b77173bdf78c5e1e108e740e3/diff:/var/lib/docker/overlay2/3a79e94659d539716ec70474a95f848c313744864a918fdc0a6ee1e732f41c8f/diff:/var/lib/docker/overlay2/41b86bb4bb7daf807a4abaa221091e9ca2980e74d4706b4cdee292c96dc0c7f8/diff:/var/lib/docker/overlay2/6082edde72aded43159d5087b571da48bb0539154aa874da71e93cdd00010ddc/diff:/var/lib/docker/overlay2/604d9a83dc7f61ef7b636b6201a3412a45b152139c1c47346ce94e3de14a6f84/diff:/var/lib/docker/overlay2/ebfd67b20364e3bedf510bbd5ed2919ca40a736646eb7493d03cdd1c577c94af/diff",
                "MergedDir": "/var/lib/docker/overlay2/b4964b99d6255ccceaa70c06a31687d165ccba119e5487983e9aaccc1a98077c/merged",
                "UpperDir": "/var/lib/docker/overlay2/b4964b99d6255ccceaa70c06a31687d165ccba119e5487983e9aaccc1a98077c/diff",
                "WorkDir": "/var/lib/docker/overlay2/b4964b99d6255ccceaa70c06a31687d165ccba119e5487983e9aaccc1a98077c/work"
            },
            "Name": "overlay2"
        },
        "Mounts": [],
        "Config": {
            "Hostname": "0335b62bd6a0",
            "Domainname": "",
            "User": "",
            "AttachStdin": false,
            "AttachStdout": false,
            "AttachStderr": false,
            "ExposedPorts": {
                "80/tcp": {}
            },
            "Tty": false,
            "OpenStdin": false,
            "StdinOnce": false,
            "Env": [
                "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
                "NGINX_VERSION=1.21.5",
                "NJS_VERSION=0.7.1",
                "PKG_RELEASE=1~bullseye"
            ],
            "Cmd": [
                "nginx",
                "-g",
                "daemon off;"
            ],
            "Image": "nginx",
            "Volumes": null,
            "WorkingDir": "",
            "Entrypoint": [
                "/docker-entrypoint.sh"
            ],
            "OnBuild": null,
            "Labels": {
                "maintainer": "NGINX Docker Maintainers <docker-maint@nginx.com>"
            },
            "StopSignal": "SIGQUIT"
        },
        "NetworkSettings": {
            "Bridge": "",
            "SandboxID": "12210d13f573637698a103c70380210772f541d9b3ab6dc87d696dc4b0cc7385",
            "SandboxKey": "/var/run/docker/netns/12210d13f573",
            "Ports": {
                "80/tcp": null
            },
            "HairpinMode": false,
            "LinkLocalIPv6Address": "",
            "LinkLocalIPv6PrefixLen": 0,
            "SecondaryIPAddresses": null,
            "SecondaryIPv6Addresses": null,
            "EndpointID": "4706a423a893512c8fee356387a05b127c957e776e8b50446d72b2a7f7e91931",
            "Gateway": "172.17.0.1",
            "GlobalIPv6Address": "",
            "GlobalIPv6PrefixLen": 0,
            "IPAddress": "172.17.0.4",
            "IPPrefixLen": 16,
            "IPv6Gateway": "",
            "MacAddress": "02:42:ac:11:00:04",
            "Networks": {
                "bridge": {
                    "IPAMConfig": null,
                    "Links": null,
                    "Aliases": null,
                    "MacAddress": "02:42:ac:11:00:04",
                    "NetworkID": "5df016be70fd418d98a01b9db033bc0f0b9f30ce7771e157061634ae28f301cc",
                    "EndpointID": "4706a423a893512c8fee356387a05b127c957e776e8b50446d72b2a7f7e91931",
                    "Gateway": "172.17.0.1",
                    "IPAddress": "172.17.0.4",
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

对应容器相关文件所存储的位置

![image-20240412141031549](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404131126635.png)

删除容器后这些文件都会被删除，在容器内修改的内容就会被删除，所以需要保存就要使用docker commit提交容器为一个新的镜像。

![image-20240412141306308](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404131126789.png)

## Docker storage drivers存储驱动

Docker提供了多种存储驱动

![image-20240412141728606](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404131126485.png)



- overlay2：overlay2 是目前所有支持的 Linux 发行版首选的存储驱动，它不需要额外的配置。

- fuse-overlayfs：fuse-overlayfs 仅推荐在没有为无根 overlay2 提供支持的主机上运行无根 Docker 时使用。在 Ubuntu 和 Debian 10 上，不需要使用 fuse-overlayfs 驱动，overlay2 即使在无根模式下也能工作。有关详细信息，请参阅无根模式文档。

- btrfs 和 zfs：btrfs 和 zfs 存储驱动提供了高级选项，例如创建“快照”，但它们需要更多的维护和设置。每个存储驱动都依赖于底层文件系统的正确配置。

- vfs：vfs 存储驱动主要用于测试目的，以及无法使用写时复制文件系统的情况。这个存储驱动的性能较差，一般不建议用于生产环境。



### 推荐使用的驱动是overlay2

![image-20240412141812818](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404131126461.png)

Docker引擎使用UnionFS的多种变体，包括OverlayFS和Overlay2。这些存储驱动实际上是由UnionFS实现的，它们允许Docker以分层的方式管理镜像和容器的文件系统，从而实现高效的镜像构建和容器运行。因此，当提到Docker中的UnionFS存储驱动时，通常指的是OverlayFS或Overlay2。

OverlayFS

![How Docker constructs map to OverlayFS constructs](https://docs.docker.com/storage/storagedriver/images/overlay_constructs.webp)

镜像层：lowerdir 当前镜像的所有底层目录

容器层：upperdir容器可写层的目录

容器挂载：merged，叠加后的文件系统的视图，采用就近原则，从上至下找到最近层的目录，即file1使用的是lowerdir的文件，file2使用的是upperdir的文件

## 容器如何挂载

![image-20240412144310131](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404131126333.png)

Volumes(卷) ：存储在主机文件系统的一部分中，该文件系统由Docker管理（在Linux上是“ / var / lib / docker / volumes /”）。 非Docker进程不应修改文件系统的这一部分。 卷是在Docker中持久存 储数据的最佳方法。 

Bind mounts(绑定挂载) 可以在任何地方 存储在主机系统上。 它们甚至可能是重要的系统文件或 目录。 Docker主机或Docker容器上的非Docker进程可以随时对其进行修改。 

tmpfs mounts(临时挂载) 仅存储在主机系统的内存中，并且永远不会写入主机系统的文件系统

具体挂载Docker容器数据卷中介绍。

# Docker镜像commit操作

docker commit  提交容器副本使之成为一个新的镜像

```shell
docker commit -m="提交的描述信息" -a="作者" 容器ID 要创建的目标镜像名:[标签名]
```



## 演示将ubuntu提交为新的镜像

### 1 从Hub上下载ubuntu镜像到本地并成功运行

```shell
docker pull ubuntu
docker run -it ubuntu /bin/bash
```

因为之前已经下载过ubuntu镜像，这里直接运行ubuntu

```shell
[root@dongguo /]# docker images
REPOSITORY     TAG       IMAGE ID       CREATED             SIZE
import/ubutu   1.0.0     47658ef456f9   About an hour ago   72.8MB
tomcat         latest    fb5657adc892   2 years ago         680MB
redis          latest    7614ae9453d1   2 years ago         113MB
ubuntu         latest    ba6acccedd29   2 years ago         72.8MB
[root@dongguo /]# docker run -it ubuntu /bin/bash
root@03b17579cd34:/# 
```

### 2 默认的Ubuntu镜像是不带着vim命令的

```shell
root@03b17579cd34:/# vim a.txt
bash: vim: command not found
```

### 3 安装vim

docker容器内执行上述两条命令：

```shell
#先更新包管理工具
apt-get update      
#再安装需要的vim
apt-get -y install vim
```

### 4.使用vim命令验证

再次执行vim a.txt 进入到文本编辑，说明安装成功，使用不保存方式:q!退出即可

### 5 安装完成后，commit我们自己的新镜像

```shell
docker commit -m="提交的描述信息" -a="作者" 容器ID 要创建的目标镜像名:[标签名]
```

如：

```shell
docker commit -m="add vim" -a="dongguo" 03b17579cd34 dongguo/myubuntu:1.0.0
```





这里开启了一个新的会话，执行命令，可以看到dongguo/myubuntu镜像已经达到191M

![image-20240407114448527](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071222711.png)

### 6启动我们的新镜像并和原来的对比

1.启动dongguo/myubuntu:1.0.0镜像

![image-20240407114648597](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071222717.png)

2.vim进行文件编辑并保存

![image-20240407114739654](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071222791.png)

3.查看文件

![image-20240407114807855](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071222556.png)

### 总结

官网默认下载的Ubuntu镜像是没有vim命令

我们自己commit构建新的镜像，新增加了vim功能，可以成功使用。

