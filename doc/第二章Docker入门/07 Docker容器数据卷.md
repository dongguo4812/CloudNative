# 介绍

Docker容器数据卷是一种特殊类型的目录，可以在容器和主机之间共享数据。当容器结束时，数据卷不会被删除，因此可以在其他容器中使用。数据卷可以被其他容器连接和共享，这使得容器之间的数据共享和持久保存变得更加容易。

卷就是目录或文件，存在于一个或多个容器中，由docker挂载到容器，但不属于联合文件系统，因此能够绕过Union File System提供一些用于持续存储或共享数据的特性：

卷的设计目的就是数据的持久化，完全独立于容器的生存周期，因此Docker不会在容器删除时删除其挂载的数据卷

一句话：有点类似我们Redis里面的rdb和aof文件，将docker容器内的数据保存进宿主机的磁盘中。

## 运行一个带有容器卷存储功能的容器实例

```shell
 docker run -it --privileged=true -v /宿主机绝对路径目录:/容器内目录      镜像名
```

如在上一章节运行私有库Registry

![image-20240407163553893](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071750772.png)

## 特点

将运用与运行的环境打包镜像，run后形成容器实例运行 ，但是我们对数据的要求希望是持久化的

Docker容器产生的数据，如果不备份，那么当容器实例删除后，容器内的数据自然也就没有了。

为了能保存数据在docker中我们使用容器数据卷。

1. 数据卷提供了可移植性：可以将数据卷挂载到多个不同的容器中，从而实现应用程序的可移植性。
2. 数据卷支持数据的持久化：数据卷中的数据不会随着容器的删除而丢失，即使容器结束，数据依然存在于数据卷中。
3. 数据卷可以被共享：容器之间可以共享同一个数据卷，这使得数据在容器之间的共享变得更加容易。
4. 数据卷可以提高容器性能：通过将容器的数据卷挂载到主机上的目录或一个专用的容器上，可以大大提高容器的性能。



# 1.容器和宿主机之间映射添加容器卷实现数据共享

```shell
docker run -it -v /宿主机目录:/容器内目录 ubuntu /bin/bash
```

启动ubuntu，命名为u1-ubuntu

```shell
docker run -it --name u1-ubuntu --privileged=true -v /tmp/hostData:/tmp/dockerData ubuntu /bin/bash
```

![image-20240407170211182](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071750879.png)

如果宿主机不存在/tmp/hostData目录，会新建该目录，ubuntu容器中/tmp/dockerDat中的数据和主机/tmp/hostData中的数据会进行同步

另外开启一个会话窗口

![image-20240407165939365](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071750924.png)

## 查看数据卷是否挂载成功

```shell
docker inspect 容器ID
```



```shell
[root@dongguo hostData]# docker ps
CONTAINER ID   IMAGE      COMMAND                   CREATED          STATUS          PORTS                                       NAMES
be618108f45e   ubuntu     "/bin/bash"               8 minutes ago    Up 8 minutes                                                u1-ubuntu
e3ac17a1f8e1   registry   "/entrypoint.sh /etc…"   57 minutes ago   Up 57 minutes   0.0.0.0:5000->5000/tcp, :::5000->5000/tcp   sharp_morse
[root@dongguo hostData]# docker inspect be618108f45e
[
    {
        "Id": "be618108f45e20dff790604d31aa08ef6dd5a913916a4e2462650885b8ab7828",
        "Created": "2024-04-07T09:01:59.933694559Z",
        "Path": "/bin/bash",
        "Args": [],
        "State": {
            "Status": "running",
            "Running": true,
            "Paused": false,
            "Restarting": false,
            "OOMKilled": false,
            "Dead": false,
            "Pid": 17480,
            "ExitCode": 0,
            "Error": "",
            "StartedAt": "2024-04-07T09:02:00.076478278Z",
            "FinishedAt": "0001-01-01T00:00:00Z"
        },
        "Image": "sha256:ba6acccedd2923aee4c2acc6a23780b14ed4b8a5fa4e14e252a23b846df9b6c1",
        "ResolvConfPath": "/var/lib/docker/containers/be618108f45e20dff790604d31aa08ef6dd5a913916a4e2462650885b8ab7828/resolv.conf",
        "HostnamePath": "/var/lib/docker/containers/be618108f45e20dff790604d31aa08ef6dd5a913916a4e2462650885b8ab7828/hostname",
        "HostsPath": "/var/lib/docker/containers/be618108f45e20dff790604d31aa08ef6dd5a913916a4e2462650885b8ab7828/hosts",
        "LogPath": "/var/lib/docker/containers/be618108f45e20dff790604d31aa08ef6dd5a913916a4e2462650885b8ab7828/be618108f45e20dff790604d31aa08ef6dd5a913916a4e2462650885b8ab7828-json.log",
        "Name": "/u1-ubuntu",
        "RestartCount": 0,
        "Driver": "overlay2",
        "Platform": "linux",
        "MountLabel": "",
        "ProcessLabel": "",
        "AppArmorProfile": "",
        "ExecIDs": null,
        "HostConfig": {
            "Binds": [
                "/tmp/hostData:/tmp/dockerData"
            ],
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
                55,
                201
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
            "Privileged": true,
            "PublishAllPorts": false,
            "ReadonlyRootfs": false,
            "SecurityOpt": [
                "label=disable"
            ],
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
            "MaskedPaths": null,
            "ReadonlyPaths": null
        },
        "GraphDriver": {
            "Data": {
                "LowerDir": "/var/lib/docker/overlay2/c294d1129e1bd138fc2caa54d8b0d58bf286ae9da675b6efd4d1937e63258eab-init/diff:/var/lib/docker/overlay2/99224fcec39ca04850234995db068f7e5b06537f418b83b421ba017112b28530/diff",
                "MergedDir": "/var/lib/docker/overlay2/c294d1129e1bd138fc2caa54d8b0d58bf286ae9da675b6efd4d1937e63258eab/merged",
                "UpperDir": "/var/lib/docker/overlay2/c294d1129e1bd138fc2caa54d8b0d58bf286ae9da675b6efd4d1937e63258eab/diff",
                "WorkDir": "/var/lib/docker/overlay2/c294d1129e1bd138fc2caa54d8b0d58bf286ae9da675b6efd4d1937e63258eab/work"
            },
            "Name": "overlay2"
        },
        "Mounts": [
            {
                "Type": "bind",
                "Source": "/tmp/hostData",
                "Destination": "/tmp/dockerData",
                "Mode": "",
                "RW": true,
                "Propagation": "rprivate"
            }
        ],
        "Config": {
            "Hostname": "be618108f45e",
            "Domainname": "",
            "User": "",
            "AttachStdin": true,
            "AttachStdout": true,
            "AttachStderr": true,
            "Tty": true,
            "OpenStdin": true,
            "StdinOnce": true,
            "Env": [
                "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
            ],
            "Cmd": [
                "/bin/bash"
            ],
            "Image": "ubuntu",
            "Volumes": null,
            "WorkingDir": "",
            "Entrypoint": null,
            "OnBuild": null,
            "Labels": {}
        },
        "NetworkSettings": {
            "Bridge": "",
            "SandboxID": "88af47d78cd68f6246bde20b6dcce4d4605b4adb2a6938674cac8fcf033d08ae",
            "SandboxKey": "/var/run/docker/netns/88af47d78cd6",
            "Ports": {},
            "HairpinMode": false,
            "LinkLocalIPv6Address": "",
            "LinkLocalIPv6PrefixLen": 0,
            "SecondaryIPAddresses": null,
            "SecondaryIPv6Addresses": null,
            "EndpointID": "b58576d96aed7385c7d0f311db31698b73fea3151ac04881fb952e991169c33d",
            "Gateway": "172.17.0.1",
            "GlobalIPv6Address": "",
            "GlobalIPv6PrefixLen": 0,
            "IPAddress": "172.17.0.3",
            "IPPrefixLen": 16,
            "IPv6Gateway": "",
            "MacAddress": "02:42:ac:11:00:03",
            "Networks": {
                "bridge": {
                    "IPAMConfig": null,
                    "Links": null,
                    "Aliases": null,
                    "MacAddress": "02:42:ac:11:00:03",
                    "NetworkID": "c4ad594548a837128106bd4e09b62d8e29e363401bfbb804a2b0062fd6f2e8b4",
                    "EndpointID": "b58576d96aed7385c7d0f311db31698b73fea3151ac04881fb952e991169c33d",
                    "Gateway": "172.17.0.1",
                    "IPAddress": "172.17.0.3",
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

其中Mounts中显示绑定信息

```shell
        "Mounts": [
            {
                "Type": "bind",
                "Source": "/tmp/hostData",
                "Destination": "/tmp/dockerData",
                "Mode": "",
                "RW": true,
                "Propagation": "rprivate"
            }
```



## 在容器中/tmp/dockerDa创建dockerin.txt文件

![image-20240407170516515](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071750917.png)

## 查看宿主机/tmp/hostData是否存在该文件

![image-20240407170551226](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071750806.png)

在/tmp/hostData存在dockerin.txt

## 在宿主机/tmp/hostData创建hostin.txt文件

![image-20240407170654160](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071750012.png)

## 查看容器中/tmp/dockerData是否存在hostin.txt文件

![image-20240407170709235](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071750496.png)

在/tmp/dockerData存在dockerin.txt

实现了容器和宿主机之间的数据同步

# 2.读写规则映射添加说明

## 默认为读写权限

```shell
docker run -it --privileged=true -v /宿主机绝对路径目录:/容器内目录:rw 镜像名
```

/宿主机绝对路径目录:/容器内目录:rw    镜像名；没有:rw的情况下，默认就是rw

```shell
docker run -it --name u1-ubuntu --privileged=true -v /tmp/hostData:/tmp/dockerData ubuntu /bin/bash
```

## 设置为只读权限

### 删除docker容器

![image-20240407171713195](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071750673.png)

### 启动docker容器,设置只有读权限

```shell
docker run -it --name u1-ubuntu --privileged=true -v /tmp/hostData:/tmp/dockerData:ro ubuntu /bin/bash
```

/容器目录:ro 镜像名  : 此时容器自己只能读取不能写,ro = read only

此时如果宿主机写入内容，可以同步给容器内，容器可以读取到。但是容器无法写入内容。

![image-20240407173229127](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071750424.png)

# 3.卷的继承和共享

删除之前启动的ubuntu容器

![image-20240407173608678](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071750773.png)

或者

![image-20240407173716239](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071750327.png)

## 容器u1-ubuntu完成和宿主机的映射

```shell
docker run -it --name u1-ubuntu --privileged=true -v /tmp/host:/tmp/docker  ubuntu /bin/bash
```

### 容器创建a.txt

![image-20240407173819229](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071750855.png)

### 宿主机同步成功

![image-20240407173842737](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071750498.png)



### 宿主机创建b.txt

![image-20240407173928418](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071750987.png)

### 容器同步成功

![image-20240407174003752](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071750906.png)

容器u1-ubuntu和宿主机实现数据共享

## 容器u2-ubuntu继承容器u1-ubuntu的卷规则

传递映射实现数据共享

```shell
docker run -it --privileged=true --volumes-from <源容器名称或ID> <镜像名称> [命令]
```

一个容器挂载另一个容器上的卷，这样两个容器可以共享相同的文件系统部分。

```shell
docker run -it --privileged=true --volumes-from u1-ubuntu --name u2-ubuntu ubuntu
```

`--volumes-from u1`: 这个选项使得新容器（u2-ubuntu）能够访问另一个容器（u1-ubuntu）的卷。所有在u1-ubuntu容器中定义的卷都会被挂载到u2-ubuntu容器中，这样u2-ubuntu就可以访问u1-ubuntu的数据和文件系统了。

### 容器u2-ubuntu创建c.txt

![image-20240407174355706](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071749216.png)

### 宿主机同步成功

![image-20240407174416336](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071749429.png)

这里要注意到：容器u1-ubuntu、容器u2-ubuntu实现数据共享，这是通过容器u1-ubuntu作为中介来实现的，而不是容器u2-ubuntu直接与宿主机交互。