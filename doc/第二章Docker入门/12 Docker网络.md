# Docker网络简介

Docker网络是一种虚拟化的网络，在Docker环境下实现容器间通讯，以及容器和宿主机之间的通讯。Docker引擎为容器提供了多种网络模式，包括Bridge模式、Host模式、None模式、Container模式等。

## 未使用Docker时默认网络情况

该机器中未安装docker

![image-20240409093849837](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091514129.png)

## 使用docker之后，网络情况

该机器安装了docker，查看网络时，显示一个名为docker0的虚拟网桥

![image-20240409093951954](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091514527.png)

# 常用基本命令

在我们了解网络模式之前，让我们先熟悉下常用的基本命令

使用docker network查看所有相关命令

![image-20240409095910771](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091514124.png)

## 查看docker网络模式命令

```shell
docker network ls
```

安装docker后，默认会自动创建3大网络模式：bridge、host、none

![image-20240409095552038](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091514885.png)

## 创建网络

```shell
docker network create XXX网络名字
```

默认情况下，Docker 使用 `bridge` 驱动来创建网络

![image-20240409104803179](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091514707.png)

## 查看网络源数据

```
docker network inspect  XXX网络名字
```

获取指定网络的详细信息，包括其配置、连接的容器以及相关的元数据。

![image-20240409104833724](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091514417.png)

## 删除网络

```shell
docker network rm XXX网络名字
```

![image-20240409104910084](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091514721.png)

# 容器实例内默认网络IP生产规则（动态分配IP）

当使用Docker的默认bridge网络或者自定义的bridge网络时，Docker会动态地为每个新创建的容器分配一个IP地址。

如果容器被停止并重新启动，当之前的IP地址已经被分配给其他容器时，它会从Docker的网络池中获取一个新的IP地址。

## 1 先启动两个ubuntu容器实例

![image-20240409111429015](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091514395.png)

## 2查看网络源数据

```shell
 docker inspect 容器ID or 容器名字
```

### u1

"IPAddress": "172.17.0.2"

```shell
[root@dongguo ~]# docker inspect u1
[
    {
        "Id": "3af6157e9e79735795b9d7d0f175f3072b7858f6474411cc9a1004750286e38f",
        "Created": "2024-04-09T03:13:06.872339284Z",
        "Path": "bash",
        "Args": [],
        "State": {
            "Status": "running",
            "Running": true,
            "Paused": false,
            "Restarting": false,
            "OOMKilled": false,
            "Dead": false,
            "Pid": 30816,
            "ExitCode": 0,
            "Error": "",
            "StartedAt": "2024-04-09T03:13:06.987491438Z",
            "FinishedAt": "0001-01-01T00:00:00Z"
        },
        "Image": "sha256:ba6acccedd2923aee4c2acc6a23780b14ed4b8a5fa4e14e252a23b846df9b6c1",
        "ResolvConfPath": "/var/lib/docker/containers/3af6157e9e79735795b9d7d0f175f3072b7858f6474411cc9a1004750286e38f/resolv.conf",
        "HostnamePath": "/var/lib/docker/containers/3af6157e9e79735795b9d7d0f175f3072b7858f6474411cc9a1004750286e38f/hostname",
        "HostsPath": "/var/lib/docker/containers/3af6157e9e79735795b9d7d0f175f3072b7858f6474411cc9a1004750286e38f/hosts",
        "LogPath": "/var/lib/docker/containers/3af6157e9e79735795b9d7d0f175f3072b7858f6474411cc9a1004750286e38f/3af6157e9e79735795b9d7d0f175f3072b7858f6474411cc9a1004750286e38f-json.log",
        "Name": "/u1",
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
                31,
                122
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
                "LowerDir": "/var/lib/docker/overlay2/005bfa9947fae51b3285c4a8643d1bbf9212e32057864f402b41d53122f0eb22-init/diff:/var/lib/docker/overlay2/99224fcec39ca04850234995db068f7e5b06537f418b83b421ba017112b28530/diff",
                "MergedDir": "/var/lib/docker/overlay2/005bfa9947fae51b3285c4a8643d1bbf9212e32057864f402b41d53122f0eb22/merged",
                "UpperDir": "/var/lib/docker/overlay2/005bfa9947fae51b3285c4a8643d1bbf9212e32057864f402b41d53122f0eb22/diff",
                "WorkDir": "/var/lib/docker/overlay2/005bfa9947fae51b3285c4a8643d1bbf9212e32057864f402b41d53122f0eb22/work"
            },
            "Name": "overlay2"
        },
        "Mounts": [],
        "Config": {
            "Hostname": "3af6157e9e79",
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
                "bash"
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
            "SandboxID": "e94dea235300bc9d6f59d72b7be18b499682e4cdb8326b225df5f6b49051a4ca",
            "SandboxKey": "/var/run/docker/netns/e94dea235300",
            "Ports": {},
            "HairpinMode": false,
            "LinkLocalIPv6Address": "",
            "LinkLocalIPv6PrefixLen": 0,
            "SecondaryIPAddresses": null,
            "SecondaryIPv6Addresses": null,
            "EndpointID": "68e88dfe8cab5bb09afcc5ee41044635fd8e86cbce1d3ebb1aa72ccce7e2337e",
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
                    "NetworkID": "cfde36a5dc5a94cf6336980ddc5b7437ae4cf4c4377492ac7f5f429abda99584",
                    "EndpointID": "68e88dfe8cab5bb09afcc5ee41044635fd8e86cbce1d3ebb1aa72ccce7e2337e",
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

### u2

 "IPAddress": "172.17.0.3",

```shell
[root@dongguo ~]# docker inspect u2
[
    {
        "Id": "0df07ebd358a786b027c613039eb332e9396efab8648ea3153380195878acd60",
        "Created": "2024-04-09T03:13:18.385631834Z",
        "Path": "bash",
        "Args": [],
        "State": {
            "Status": "running",
            "Running": true,
            "Paused": false,
            "Restarting": false,
            "OOMKilled": false,
            "Dead": false,
            "Pid": 30870,
            "ExitCode": 0,
            "Error": "",
            "StartedAt": "2024-04-09T03:13:18.519422178Z",
            "FinishedAt": "0001-01-01T00:00:00Z"
        },
        "Image": "sha256:ba6acccedd2923aee4c2acc6a23780b14ed4b8a5fa4e14e252a23b846df9b6c1",
        "ResolvConfPath": "/var/lib/docker/containers/0df07ebd358a786b027c613039eb332e9396efab8648ea3153380195878acd60/resolv.conf",
        "HostnamePath": "/var/lib/docker/containers/0df07ebd358a786b027c613039eb332e9396efab8648ea3153380195878acd60/hostname",
        "HostsPath": "/var/lib/docker/containers/0df07ebd358a786b027c613039eb332e9396efab8648ea3153380195878acd60/hosts",
        "LogPath": "/var/lib/docker/containers/0df07ebd358a786b027c613039eb332e9396efab8648ea3153380195878acd60/0df07ebd358a786b027c613039eb332e9396efab8648ea3153380195878acd60-json.log",
        "Name": "/u2",
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
                31,
                122
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
                "LowerDir": "/var/lib/docker/overlay2/da95dc6427157a145a04b90fbc8c88105fbe7a8fb76f4b102136eed70dde9b5d-init/diff:/var/lib/docker/overlay2/99224fcec39ca04850234995db068f7e5b06537f418b83b421ba017112b28530/diff",
                "MergedDir": "/var/lib/docker/overlay2/da95dc6427157a145a04b90fbc8c88105fbe7a8fb76f4b102136eed70dde9b5d/merged",
                "UpperDir": "/var/lib/docker/overlay2/da95dc6427157a145a04b90fbc8c88105fbe7a8fb76f4b102136eed70dde9b5d/diff",
                "WorkDir": "/var/lib/docker/overlay2/da95dc6427157a145a04b90fbc8c88105fbe7a8fb76f4b102136eed70dde9b5d/work"
            },
            "Name": "overlay2"
        },
        "Mounts": [],
        "Config": {
            "Hostname": "0df07ebd358a",
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
                "bash"
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
            "SandboxID": "01df6a49e0628ff915e4dec9f51dc0876050e5039eab217f7df712d7da8fd8ff",
            "SandboxKey": "/var/run/docker/netns/01df6a49e062",
            "Ports": {},
            "HairpinMode": false,
            "LinkLocalIPv6Address": "",
            "LinkLocalIPv6PrefixLen": 0,
            "SecondaryIPAddresses": null,
            "SecondaryIPv6Addresses": null,
            "EndpointID": "6cc44990a95e8a33b97adfaff69985901daf68163f1c034b675368099516f128",
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
                    "NetworkID": "cfde36a5dc5a94cf6336980ddc5b7437ae4cf4c4377492ac7f5f429abda99584",
                    "EndpointID": "6cc44990a95e8a33b97adfaff69985901daf68163f1c034b675368099516f128",
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

## 3 关闭u2实例，新建u3

![image-20240409111917165](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091514740.png)



## 4查看网络源数据

### u3

"IPAddress": "172.17.0.3"

```shell
[root@dongguo ~]# docker inspect u3
[
    {
        "Id": "d216515869b027c749f5a52ad823b79549a041de96d241489816da92ecb39e63",
        "Created": "2024-04-09T03:18:59.170914243Z",
        "Path": "bash",
        "Args": [],
        "State": {
            "Status": "running",
            "Running": true,
            "Paused": false,
            "Restarting": false,
            "OOMKilled": false,
            "Dead": false,
            "Pid": 30974,
            "ExitCode": 0,
            "Error": "",
            "StartedAt": "2024-04-09T03:18:59.29241856Z",
            "FinishedAt": "0001-01-01T00:00:00Z"
        },
        "Image": "sha256:ba6acccedd2923aee4c2acc6a23780b14ed4b8a5fa4e14e252a23b846df9b6c1",
        "ResolvConfPath": "/var/lib/docker/containers/d216515869b027c749f5a52ad823b79549a041de96d241489816da92ecb39e63/resolv.conf",
        "HostnamePath": "/var/lib/docker/containers/d216515869b027c749f5a52ad823b79549a041de96d241489816da92ecb39e63/hostname",
        "HostsPath": "/var/lib/docker/containers/d216515869b027c749f5a52ad823b79549a041de96d241489816da92ecb39e63/hosts",
        "LogPath": "/var/lib/docker/containers/d216515869b027c749f5a52ad823b79549a041de96d241489816da92ecb39e63/d216515869b027c749f5a52ad823b79549a041de96d241489816da92ecb39e63-json.log",
        "Name": "/u3",
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
                31,
                122
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
                "LowerDir": "/var/lib/docker/overlay2/373589357b75e9ecf5955eea0aa2de1a4eb238eceb45fad1c9858e45f3023a93-init/diff:/var/lib/docker/overlay2/99224fcec39ca04850234995db068f7e5b06537f418b83b421ba017112b28530/diff",
                "MergedDir": "/var/lib/docker/overlay2/373589357b75e9ecf5955eea0aa2de1a4eb238eceb45fad1c9858e45f3023a93/merged",
                "UpperDir": "/var/lib/docker/overlay2/373589357b75e9ecf5955eea0aa2de1a4eb238eceb45fad1c9858e45f3023a93/diff",
                "WorkDir": "/var/lib/docker/overlay2/373589357b75e9ecf5955eea0aa2de1a4eb238eceb45fad1c9858e45f3023a93/work"
            },
            "Name": "overlay2"
        },
        "Mounts": [],
        "Config": {
            "Hostname": "d216515869b0",
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
                "bash"
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
            "SandboxID": "641a0b424c6b56d86b9d59c3aea5a39b1387b766f49983e3e40b4a76f7ffac0c",
            "SandboxKey": "/var/run/docker/netns/641a0b424c6b",
            "Ports": {},
            "HairpinMode": false,
            "LinkLocalIPv6Address": "",
            "LinkLocalIPv6PrefixLen": 0,
            "SecondaryIPAddresses": null,
            "SecondaryIPv6Addresses": null,
            "EndpointID": "4243d30d8beef291e13fcf9c348ca0eb95d94f57f440a69125c07ccfbeba2687",
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
                    "NetworkID": "cfde36a5dc5a94cf6336980ddc5b7437ae4cf4c4377492ac7f5f429abda99584",
                    "EndpointID": "4243d30d8beef291e13fcf9c348ca0eb95d94f57f440a69125c07ccfbeba2687",
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

docker容器内部的ip是有可能会发生改变的，当Docker容器启动时，它会被分配一个动态IP地址。如果容器被重新启动或者Docker宿主机被重启，则容器的IP地址可能会改变。此外，如果容器被启动时使用了特定的IP地址，那么这个IP地址也可能发生改变。因此，在使用Docker容器时，需要注意容器的IP地址是否发生了更改，以避免出现访问问题。

# 网络模式

介绍Bridge模式、Host模式、None模式、Container模式

![image-20240409112150000](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091514300.png)

## bridge模式

Docker 服务默认会创建一个 docker0 网桥（其上有一个 docker0 内部接口），该桥接网络的名称为docker0，它在内核层连通了其他的物理或虚拟网卡，这就将所有容器和本地主机都放到同一个物理网络。Docker 默认指定了 docker0 接口 的 IP 地址和子网掩码，让主机和容器之间可以通过网桥相互通信。

```shell
[root@dongguo ~]# docker network ls
NETWORK ID     NAME      DRIVER    SCOPE
cfde36a5dc5a   bridge    bridge    local
9154b1d46426   host      host      local
95222adf2137   none      null      local
[root@dongguo ~]# docker inspect bridge
[
    {
        "Name": "bridge",
        "Id": "cfde36a5dc5a94cf6336980ddc5b7437ae4cf4c4377492ac7f5f429abda99584",
        "Created": "2024-04-09T09:39:29.497895666+08:00",
        "Scope": "local",
        "Driver": "bridge",
        "EnableIPv6": false,
        "IPAM": {
            "Driver": "default",
            "Options": null,
            "Config": [
                {
                    "Subnet": "172.17.0.0/16",
                    "Gateway": "172.17.0.1"
                }
            ]
        },
        "Internal": false,
        "Attachable": false,
        "Ingress": false,
        "ConfigFrom": {
            "Network": ""
        },
        "ConfigOnly": false,
        "Containers": {
            "3af6157e9e79735795b9d7d0f175f3072b7858f6474411cc9a1004750286e38f": {
                "Name": "u1",
                "EndpointID": "68e88dfe8cab5bb09afcc5ee41044635fd8e86cbce1d3ebb1aa72ccce7e2337e",
                "MacAddress": "02:42:ac:11:00:02",
                "IPv4Address": "172.17.0.2/16",
                "IPv6Address": ""
            },
            "d216515869b027c749f5a52ad823b79549a041de96d241489816da92ecb39e63": {
                "Name": "u3",
                "EndpointID": "4243d30d8beef291e13fcf9c348ca0eb95d94f57f440a69125c07ccfbeba2687",
                "MacAddress": "02:42:ac:11:00:03",
                "IPv4Address": "172.17.0.3/16",
                "IPv6Address": ""
            }
        },
        "Options": {
            "com.docker.network.bridge.default_bridge": "true",
            "com.docker.network.bridge.enable_icc": "true",
            "com.docker.network.bridge.enable_ip_masquerade": "true",
            "com.docker.network.bridge.host_binding_ipv4": "0.0.0.0",
            "com.docker.network.bridge.name": "docker0",
            "com.docker.network.driver.mtu": "1500"
        },
        "Labels": {}
    }
]
```

1 Docker使用Linux桥接，在宿主机虚拟一个Docker容器网桥(docker0)，Docker启动一个容器时会根据Docker网桥的网段分配给容器一个IP地址，称为Container-IP，同时Docker网桥是每个容器的默认网关。因为在同一宿主机内的容器都接入同一个网桥，这样容器之间就能够通过容器的Container-IP直接通信。

2 docker run 的时候，没有指定network的话默认使用的网桥模式就是bridge，使用的就是docker0。在宿主机ifconfig,就可以看到docker0和自己create的network(后面讲)eth0，eth1，eth2……代表网卡一，网卡二，网卡三……，lo代表127.0.0.1，即localhost，inet addr用来表示网卡的IP地址

3 网桥docker0创建一对对等虚拟设备接口一个叫veth，另一个叫eth0，成对匹配。

3.1 整个宿主机的网桥模式都是docker0，类似一个交换机有一堆接口，每个接口叫veth，在本地主机和容器内分别创建一个虚拟接口，并让他们彼此联通（这样一对接口叫veth pair）；

3.2 每个容器实例内部也有一块网卡，每个接口叫eth0；

3.3 docker0上面的每个veth匹配某个容器实例内部的eth0，两两配对，一一匹配。

通过上述，将宿主机上的所有容器都连接到这个内部网络上，两个容器在同一个网络下,会从这个网关下各自拿到分配的ip，此时两个容器的网络是互通的。

![image-20240409133749200](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091513513.png)

从上图看到每个eth0都对应docker bridge中的veth

### 启动两个tomcat容器，查看对应的ip

```shell
docker run -d -p 8081:8080   --name tomcat81 billygoo/tomcat8-jdk8

docker run -d -p 8082:8080   --name tomcat82 billygoo/tomcat8-jdk8
```

![image-20240409134434141](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091513220.png)

两个tomcat容器分别对应一个veth

![image-20240409134549704](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091513959.png)

分别查看两个tomcat容器内对应的ip，docker0上面的每个veth匹配某个容器实例内部的eth0，两两配对，一 一匹配。

![image-20240409135018091](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091513031.png)



![image-20240409135154929](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091513421.png)

## host模式

容器与主机共享同一网络命名空间。容器将直接使用主机的网络接口。

容器将不会获得一个独立的Network Namespace， 而是和宿主机共用一个Network Namespace。容器将不会虚拟出自己的网卡而是使用宿主机的IP和端口。

![image-20240409135502999](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091513892.png)

### host模式启动tomcat

```shell
docker run -d -p 8083:8080 --network host --name tomcat83 billygoo/tomcat8-jdk8
```

启动成功，但是会有警告:host模式下指定port，ports端口设置不会生效。

docker启动时指定--network=host或-net=host，如果还指定了-p映射端口，那这个时候就会有此警告，并且通过-p设置的参数将不会起到任何作用，端口号会以主机端口号为主，重复时则递增。

![image-20240409135859120](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091513858.png)

当然这个警告是可以无视的。等同于：

```shell
docker run -d --network host --name tomcat83 billygoo/tomcat8-jdk8
```

host模式下启动tomcat容器，宿主机并没有产生新的veth

![image-20240409140054748](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091513933.png)

host模式使用宿主机的 IP 地址与外界进行通信，容器直接使用主机的网络接口，所以网络接口信息是一模一样的。

![image-20240409140230045](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091513514.png)

host模式下容器使用宿主机的 IP 地址，所以本身的网络情况中ipAddress为空

![image-20240409140540193](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091513891.png)

### 没有设置-p的端口映射了，如何访问启动的tomcat83？

使用宿主机的ip访问tomcat，端口号会以主机端口号为主，重复时则递增。

![image-20240409140628251](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091513473.png)

容器共享宿主机网络IP，这样的好处是外部主机与容器可以直接通信。

## none模式

容器没有分配任何网络资源。这意味着容器内部没有网络接口，也没有IP地址。None模式适用于不需要网络连接的容器，例如用于批处理作业或与外部网络完全隔离的容器。

![image-20240409140750296](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091513740.png)



### none模式启动tomcat

![image-20240409141338278](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091513594.png)

容器的详细信息：

```shell
[root@dongguo ~]# docker inspect tomcat84
[
    {
        "Id": "377adc1aa135f4d088cde2137b6a847bd9679d29fa371bfc297ccad2efb1b230",
        "Created": "2024-04-09T06:13:23.046677266Z",
        "Path": "catalina.sh",
        "Args": [
            "run"
        ],
        "State": {
            "Status": "running",
            "Running": true,
            "Paused": false,
            "Restarting": false,
            "OOMKilled": false,
            "Dead": false,
            "Pid": 31695,
            "ExitCode": 0,
            "Error": "",
            "StartedAt": "2024-04-09T06:13:23.163614937Z",
            "FinishedAt": "0001-01-01T00:00:00Z"
        },
        "Image": "sha256:30ef4019761d4aee397841d0b4291a928a4816745e2eba2ead83f1d2cf64d42a",
        "ResolvConfPath": "/var/lib/docker/containers/377adc1aa135f4d088cde2137b6a847bd9679d29fa371bfc297ccad2efb1b230/resolv.conf",
        "HostnamePath": "/var/lib/docker/containers/377adc1aa135f4d088cde2137b6a847bd9679d29fa371bfc297ccad2efb1b230/hostname",
        "HostsPath": "/var/lib/docker/containers/377adc1aa135f4d088cde2137b6a847bd9679d29fa371bfc297ccad2efb1b230/hosts",
        "LogPath": "/var/lib/docker/containers/377adc1aa135f4d088cde2137b6a847bd9679d29fa371bfc297ccad2efb1b230/377adc1aa135f4d088cde2137b6a847bd9679d29fa371bfc297ccad2efb1b230-json.log",
        "Name": "/tomcat84",
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
            "NetworkMode": "none",
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
                "LowerDir": "/var/lib/docker/overlay2/ab8dee2def37ae3780be814823cd6174b4c1e2c30bf97a5ec0a20a5a5b2f6e9b-init/diff:/var/lib/docker/overlay2/02e2f591a3ce2ced7e5cdcdfe998c628033bfe9b22aae0f87abb012d72364cbd/diff:/var/lib/docker/overlay2/73edb6e817fc87b6b5668ea962716cc95dd2f93729e4ce2ed517349239398fe2/diff:/var/lib/docker/overlay2/8876645c80c805611916452d14bcef355a7b00caedf1e68836d1dd97d84ed234/diff:/var/lib/docker/overlay2/f5cb450d211b4e3a5c6952234f7f0f94a65d89edb442b8dd47dafeb65beb3a89/diff:/var/lib/docker/overlay2/16068beb23d59b78b6e5da51a08cb5674bf50045aa706fbc3742e5376ab155d3/diff:/var/lib/docker/overlay2/d66e4a2986e00e5ef238cb9634676dc3e114c87f9889ade515cafdf565aedab5/diff:/var/lib/docker/overlay2/70dd5683f2d44e59572184da0a05178e44740f9c9305042dc4950d5fd8fd18aa/diff:/var/lib/docker/overlay2/df2345b6fbdee4c93e7e1c8a2a38946f3fb1ff52ffc2066735dd9e909ee66555/diff:/var/lib/docker/overlay2/ab999abae25e48d97b2b12af9da07554569c8f9508628527cc1a4a327d64f331/diff:/var/lib/docker/overlay2/d9822f7c719452168c5ce93f250c1a87445874ba83a902474e72f08506a8a46b/diff:/var/lib/docker/overlay2/9627cfe61e9c265170fbb2dcdd8c84f3a3f103b97062945fd237f1a8a959d739/diff:/var/lib/docker/overlay2/128031ee4f72eade85c08411ce9dd95b90a2b56e35f6c32a9bcbfb14c6ff72be/diff:/var/lib/docker/overlay2/2f7466cda029993837c921c5b091f83b5a337358b55f8e4bcd45bfa70616b807/diff",
                "MergedDir": "/var/lib/docker/overlay2/ab8dee2def37ae3780be814823cd6174b4c1e2c30bf97a5ec0a20a5a5b2f6e9b/merged",
                "UpperDir": "/var/lib/docker/overlay2/ab8dee2def37ae3780be814823cd6174b4c1e2c30bf97a5ec0a20a5a5b2f6e9b/diff",
                "WorkDir": "/var/lib/docker/overlay2/ab8dee2def37ae3780be814823cd6174b4c1e2c30bf97a5ec0a20a5a5b2f6e9b/work"
            },
            "Name": "overlay2"
        },
        "Mounts": [],
        "Config": {
            "Hostname": "377adc1aa135",
            "Domainname": "",
            "User": "",
            "AttachStdin": false,
            "AttachStdout": false,
            "AttachStderr": false,
            "ExposedPorts": {
                "8080/tcp": {}
            },
            "Tty": false,
            "OpenStdin": false,
            "StdinOnce": false,
            "Env": [
                "PATH=/usr/local/tomcat/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
                "LANG=C.UTF-8",
                "JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64",
                "JAVA_VERSION=8u171",
                "JAVA_DEBIAN_VERSION=8u171-b11-1~deb9u1",
                "CA_CERTIFICATES_JAVA_VERSION=20170531+nmu1",
                "CATALINA_HOME=/usr/local/tomcat",
                "TOMCAT_NATIVE_LIBDIR=/usr/local/tomcat/native-jni-lib",
                "LD_LIBRARY_PATH=/usr/local/tomcat/native-jni-lib",
                "OPENSSL_VERSION=1.1.0f-3+deb9u2",
                "GPG_KEYS=05AB33110949707C93A279E3D3EFE6B686867BA6 07E48665A34DCAFAE522E5E6266191C37C037D42 47309207D818FFD8DCD3F83F1931D684307A10A5 541FBE7D8F78B25E055DDEE13C370389288584E7 61B832AC2F1C5A90F0F9B00A1C506407564C17A3 713DA88BE50911535FE716F5208B0AB1D63011C7 79F7026C690BAA50B92CD8B66A3AD3F4F22C4FED 9BA44C2621385CB966EBA586F72C284D731FABEE A27677289986DB50844682F8ACB77FC2E86E29AC A9C5DF4D22E99998D9875A5110C01C5A2F6059E7 DCFD35E0BF8CA7344752DE8B6FB21E8933C60243 F3A04C595DB5B6A5F1ECA43E3B7BBB100D811BBE F7DA48BB64BCB84ECBA7EE6935CD23C10D498E23",
                "TOMCAT_MAJOR=8",
                "TOMCAT_VERSION=8.0.53",
                "TOMCAT_SHA512=cd8a4e48a629a2f2bb4ce6b101ebcce41da52b506064396ec1b2915c0b0d8d82123091242f2929a649bcd8b65ecf6cd1ab9c7d90ac0e261821097ab6fbe22df9",
                "TOMCAT_TGZ_URLS=https://www.apache.org/dyn/closer.cgi?action=download&filename=tomcat/tomcat-8/v8.0.53/bin/apache-tomcat-8.0.53.tar.gz \thttps://www-us.apache.org/dist/tomcat/tomcat-8/v8.0.53/bin/apache-tomcat-8.0.53.tar.gz \thttps://www.apache.org/dist/tomcat/tomcat-8/v8.0.53/bin/apache-tomcat-8.0.53.tar.gz \thttps://archive.apache.org/dist/tomcat/tomcat-8/v8.0.53/bin/apache-tomcat-8.0.53.tar.gz",
                "TOMCAT_ASC_URLS=https://www.apache.org/dyn/closer.cgi?action=download&filename=tomcat/tomcat-8/v8.0.53/bin/apache-tomcat-8.0.53.tar.gz.asc \thttps://www-us.apache.org/dist/tomcat/tomcat-8/v8.0.53/bin/apache-tomcat-8.0.53.tar.gz.asc \thttps://www.apache.org/dist/tomcat/tomcat-8/v8.0.53/bin/apache-tomcat-8.0.53.tar.gz.asc \thttps://archive.apache.org/dist/tomcat/tomcat-8/v8.0.53/bin/apache-tomcat-8.0.53.tar.gz.asc"
            ],
            "Cmd": [
                "catalina.sh",
                "run"
            ],
            "Image": "billygoo/tomcat8-jdk8",
            "Volumes": null,
            "WorkingDir": "/usr/local/tomcat",
            "Entrypoint": null,
            "OnBuild": null,
            "Labels": {}
        },
        "NetworkSettings": {
            "Bridge": "",
            "SandboxID": "0c666f39403955045e8d715f6df18a33e18a0c8b3eda95490ccbace658d8cea8",
            "SandboxKey": "/var/run/docker/netns/0c666f394039",
            "Ports": {},
            "HairpinMode": false,
            "LinkLocalIPv6Address": "",
            "LinkLocalIPv6PrefixLen": 0,
            "SecondaryIPAddresses": null,
            "SecondaryIPv6Addresses": null,
            "EndpointID": "",
            "Gateway": "",
            "GlobalIPv6Address": "",
            "GlobalIPv6PrefixLen": 0,
            "IPAddress": "",
            "IPPrefixLen": 0,
            "IPv6Gateway": "",
            "MacAddress": "",
            "Networks": {
                "none": {
                    "IPAMConfig": null,
                    "Links": null,
                    "Aliases": null,
                    "MacAddress": "",
                    "NetworkID": "95222adf2137537575e8b86bc3660f0a6d936a74bc1529426890ebecc00f231e",
                    "EndpointID": "18e47ca432d0a992347ff71022030622e7b82be74cd76cb35e70aa31a6e90e90",
                    "Gateway": "",
                    "IPAddress": "",
                    "IPPrefixLen": 0,
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

查看tomcat84的网络接口信息，只有lo标识(就是127.0.0.1表示本地回环)

![image-20240409141702488](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091513006.png)

## container模式

多个容器共享同一个网络命名空间。

新建的容器和已经存在的一个容器共享一个网络ip配置而不是和宿主机共享。新创建的容器不会创建自己的网卡，配置自己的IP，而是和一个指定的容器共享IP、端口范围等。同样，两个容器除了网络方面，其他的如文件系统、进程列表等还是隔离的。

![image-20240409141812080](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091513849.png)

启动两个tomcat容器tomcat85、tomcat86，如果指定tomcat86使用tomcat85的网络配置，tomcat86和tomcat85公用同一个ip同一个端口，会导致端口冲突，所以接下来使用alpine操作系统举例。

### 启动两个alpine操作系统alpine1、alpine2，并且alpine2使用alpine1的配置

Alpine Linux 是一款独立的、非商业的通用 Linux 发行版，专为追求安全性、简单性和资源效率的用户而设计。 可能很多人没听说过这个 Linux 发行版本，但是经常用 Docker 的朋友可能都用过，因为他小，简单，安全而著称，所以作为基础镜像是非常好的一个选择，可谓是麻雀虽小但五脏俱全，镜像非常小巧，不到 6M的大小，所以特别适合容器打包。

```shell
docker run -it --name alpine1  alpine /bin/sh
#alpine2 容器共享 alpine1 容器的网络命名空间
docker run -it --network container:alpine1 --name alpine2  alpine /bin/sh
```

alpine1

![image-20240409143521234](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091513109.png)

alpine2

![image-20240409143545623](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091513355.png)

两个容器共用eth0@if181网络配置

### 此时alpine2使用alpine1的网络配置，那如果关闭alpine1，那alpine2的网络配置会如何变化呢？

![image-20240409143800710](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091513064.png)

再次查看alpine2的网络配置，只剩下lo本地地址了。

![image-20240409143817643](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091513448.png)

## 自定义网络

### 启动两个tomcat容器（bridge模式），互相通信

8081   ip：172.17.0.4

![image-20240409144456785](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091512825.png)

8082  ip：172.17.0.5

![image-20240409144606781](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091512833.png)

#### 互相通信

8081 ping 8082

![image-20240409144657310](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091512434.png)

8082 ping 8081

![image-20240409144738760](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091512755.png)

但是在使用bridge模式时，docker容器内部的ip是有可能会发生改变的，依赖容器内部IP地址进行通信通常不是一个好的做法。

我们可以通过服务名直接网络通信而不受到影响。但是默认的bridge模式无法实现

![image-20240409145253840](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091512042.png)

### 使用自定义网络解决该痛点

1.新建自定义网络

```shell
docker network create dg_network
```

![image-20240409150509986](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091512024.png)

2.新建容器加入上一步新建的自定义网络

```shell
docker run -d -p 8081:8080 --network dg_network  --name tomcat81 billygoo/tomcat8-jdk8

docker run -d -p 8082:8080 --network dg_network  --name tomcat82 billygoo/tomcat8-jdk8
```

![image-20240409150636909](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091512646.png)

3.ping测试

![image-20240409150953879](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091512210.png)

8081 ping 8082

![image-20240409151055087](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091512523.png)

8082 ping 8081

![image-20240409151126859](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404091512157.png)

自定义网络本身就维护好了主机名和ip的对应关系（ip和域名都能通）

