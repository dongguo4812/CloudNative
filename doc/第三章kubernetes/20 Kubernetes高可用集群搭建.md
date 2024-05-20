所有节点基础环境

192.168.122.x  ：  为机器的网段

10.96.0.0/16:    为Service网段

196.16.0.0/16:  为Pod网段

# 环境准备

## 每个机器准备域名（所有机器）

```shell
vi /etc/hosts
192.168.122.143 k8s-master1
192.168.122.144 k8s-master2
192.168.122.145 k8s-master3
192.168.122.146 k8s-node1
192.168.122.147 k8s-node2
192.168.122.148 k8s-node3
```

该集群并未设置高可用，所有请求都请求到192.168.122.143节点

## 关闭selinux（所有机器）

```shell
#临时
setenforce 0
#永久
sed -i 's#SELINUX=enforcing#SELINUX=disabled#g' /etc/sysconfig/selinux
sed -i 's#SELINUX=enforcing#SELINUX=disabled#g' /etc/selinux/config
```

## 关闭swap（所有机器）

```shell
#临时
swapoff -a && sysctl -w vm.swappiness=0
#永久
sed -ri 's/.*swap.*/#&/' /etc/fstab
```

## 修改系统的limit（所有机器）

这些限制控制了一个用户可以打开的文件数、进程数和内存锁定等。

```shell
#临时
ulimit -SHn 65535
#永久
vi /etc/security/limits.conf
# 末尾添加如下内容
* soft nofile 655360
* hard nofile 131072
* soft nproc 655350
* hard nproc 655350
* soft memlock unlimited
* hard memlock unlimited
```

## 配置ssh免密连接

以后master1不使用密码就可以连接到其他机器，为了方便证书、文件直接传递。

master1运行：

### 生成 RSA 密钥

```shell
ssh-keygen -t rsa
```

三次回车，使用默认即可

![image-20240517085418146](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172244585.png)

### 将公钥添加到所有的远程主机

```shell
for i in k8s-master1 k8s-master2 k8s-master3 k8s-node1 k8s-node2 k8s-node3;do ssh-copy-id -i .ssh/id_rsa.pub $i;done
```

输入6个主机的密码，一次配置之后主机间连接就可以不用输入密码了

```shell
[root@k8s-master1 ~]# for i in k8s-master1 k8s-master2 k8s-master3 k8s-node1 k8s-node2 k8s-node3;do ssh-copy-id -i .ssh/id_rsa.pub $i;done
/usr/bin/ssh-copy-id: INFO: Source of key(s) to be installed: ".ssh/id_rsa.pub"
The authenticity of host 'k8s-master1 (192.168.122.143)' can't be established.
ECDSA key fingerprint is SHA256:yNOxfQBIZv8rDFHkkYVz8ywEeEyRY88lHqAf8EQWAsQ.
ECDSA key fingerprint is MD5:81:e7:de:6f:c5:9a:f6:ff:d4:54:99:12:5e:c8:cb:03.
Are you sure you want to continue connecting (yes/no)? yes
/usr/bin/ssh-copy-id: INFO: attempting to log in with the new key(s), to filter out any that are already installed
/usr/bin/ssh-copy-id: INFO: 1 key(s) remain to be installed -- if you are prompted now it is to install the new keys
root@k8s-master1's password: 

Number of key(s) added: 1

Now try logging into the machine, with:   "ssh 'k8s-master1'"
and check to make sure that only the key(s) you wanted were added.

/usr/bin/ssh-copy-id: INFO: Source of key(s) to be installed: ".ssh/id_rsa.pub"
The authenticity of host 'k8s-master2 (192.168.122.144)' can't be established.
ECDSA key fingerprint is SHA256:yNOxfQBIZv8rDFHkkYVz8ywEeEyRY88lHqAf8EQWAsQ.
ECDSA key fingerprint is MD5:81:e7:de:6f:c5:9a:f6:ff:d4:54:99:12:5e:c8:cb:03.
Are you sure you want to continue connecting (yes/no)? yes
/usr/bin/ssh-copy-id: INFO: attempting to log in with the new key(s), to filter out any that are already installed
/usr/bin/ssh-copy-id: INFO: 1 key(s) remain to be installed -- if you are prompted now it is to install the new keys
root@k8s-master2's password: 

Number of key(s) added: 1

Now try logging into the machine, with:   "ssh 'k8s-master2'"
and check to make sure that only the key(s) you wanted were added.

/usr/bin/ssh-copy-id: INFO: Source of key(s) to be installed: ".ssh/id_rsa.pub"
The authenticity of host 'k8s-master3 (192.168.122.145)' can't be established.
ECDSA key fingerprint is SHA256:yNOxfQBIZv8rDFHkkYVz8ywEeEyRY88lHqAf8EQWAsQ.
ECDSA key fingerprint is MD5:81:e7:de:6f:c5:9a:f6:ff:d4:54:99:12:5e:c8:cb:03.
Are you sure you want to continue connecting (yes/no)? yes
/usr/bin/ssh-copy-id: INFO: attempting to log in with the new key(s), to filter out any that are already installed
/usr/bin/ssh-copy-id: INFO: 1 key(s) remain to be installed -- if you are prompted now it is to install the new keys
root@k8s-master3's password: 

Number of key(s) added: 1

Now try logging into the machine, with:   "ssh 'k8s-master3'"
and check to make sure that only the key(s) you wanted were added.

/usr/bin/ssh-copy-id: INFO: Source of key(s) to be installed: ".ssh/id_rsa.pub"
The authenticity of host 'k8s-node1 (192.168.122.146)' can't be established.
ECDSA key fingerprint is SHA256:0/GiRhTgLQFendXq4lZtt7wlVmTASv1zMwG1uGiOa04.
ECDSA key fingerprint is MD5:9f:6e:fe:85:44:f5:28:37:d9:79:3d:75:d2:b4:fc:a5.
Are you sure you want to continue connecting (yes/no)? yes
/usr/bin/ssh-copy-id: INFO: attempting to log in with the new key(s), to filter out any that are already installed
/usr/bin/ssh-copy-id: INFO: 1 key(s) remain to be installed -- if you are prompted now it is to install the new keys
root@k8s-node1's password: 

Number of key(s) added: 1

Now try logging into the machine, with:   "ssh 'k8s-node1'"
and check to make sure that only the key(s) you wanted were added.

/usr/bin/ssh-copy-id: INFO: Source of key(s) to be installed: ".ssh/id_rsa.pub"
The authenticity of host 'k8s-node2 (192.168.122.147)' can't be established.
ECDSA key fingerprint is SHA256:0/GiRhTgLQFendXq4lZtt7wlVmTASv1zMwG1uGiOa04.
ECDSA key fingerprint is MD5:9f:6e:fe:85:44:f5:28:37:d9:79:3d:75:d2:b4:fc:a5.
Are you sure you want to continue connecting (yes/no)? yes
/usr/bin/ssh-copy-id: INFO: attempting to log in with the new key(s), to filter out any that are already installed
/usr/bin/ssh-copy-id: INFO: 1 key(s) remain to be installed -- if you are prompted now it is to install the new keys
root@k8s-node2's password: 

Number of key(s) added: 1

Now try logging into the machine, with:   "ssh 'k8s-node2'"
and check to make sure that only the key(s) you wanted were added.

/usr/bin/ssh-copy-id: INFO: Source of key(s) to be installed: ".ssh/id_rsa.pub"
The authenticity of host 'k8s-node3 (192.168.122.148)' can't be established.
ECDSA key fingerprint is SHA256:0/GiRhTgLQFendXq4lZtt7wlVmTASv1zMwG1uGiOa04.
ECDSA key fingerprint is MD5:9f:6e:fe:85:44:f5:28:37:d9:79:3d:75:d2:b4:fc:a5.
Are you sure you want to continue connecting (yes/no)? yes
/usr/bin/ssh-copy-id: INFO: attempting to log in with the new key(s), to filter out any that are already installed
/usr/bin/ssh-copy-id: INFO: 1 key(s) remain to be installed -- if you are prompted now it is to install the new keys
root@k8s-node3's password: 

Number of key(s) added: 1

Now try logging into the machine, with:   "ssh 'k8s-node3'"
and check to make sure that only the key(s) you wanted were added.

```

### 测试

通过 SSH 连接到名为 `k8s-node2` 的主机，并使用 `root` 用户身份登录。

```shell
ssh root@k8s-node2
```

使用exit退出

![image-20240511144733533](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131259784.png)

## 安装后续会用到的工具：（所有机器）

- wget：用于从网络上下载文件的命令行工具。
- git：版本控制系统，用于跟踪文件的变化。
- jq：用于处理和分析 JSON 数据的命令行工具。
- psmisc：包含一些杂项的实用工具，如 killall 和 fuser。
- net-tools：提供了一些基本的网络工具，如 ifconfig 和 netstat。
- yum-utils：提供了一些用于扩展和补充 yum 的实用工具。
- device-mapper-persistent-data：提供了设备映射器的持久数据存储。
- lvm2：逻辑卷管理器，用于管理逻辑卷和文件系统。

```shell
yum install wget git jq psmisc net-tools yum-utils device-mapper-persistent-data lvm2  -y
```

## 安装ipvs工具（所有机器）

方便以后操作ipvs，ipset，conntrack等

```shell
yum install ipvsadm ipset sysstat conntrack libseccomp -y
```

### 所有节点配置ipvs模块,

执行以下命令，在内核4.19+版本改为nf_conntrack， 4.18下改为nf_conntrack_ipv4

- `ip_vs`: IPVS（IP Virtual Server）负载均衡模块
- `ip_vs_rr`: IPVS的Round-Robin调度算法
- `ip_vs_wrr`: IPVS的加权Round-Robin调度算法
- `ip_vs_sh`: IPVS的源哈希调度算法
- `nf_conntrack`: 连接跟踪模块

```shell
modprobe -- ip_vs
modprobe -- ip_vs_rr
modprobe -- ip_vs_wrr
modprobe -- ip_vs_sh
modprobe -- nf_conntrack
```

![image-20240511151500176](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131259225.png)

### 修改ipvs配置，加入以下内容

```shell
vi /etc/modules-load.d/ipvs.conf

ip_vs
ip_vs_lc
ip_vs_wlc
ip_vs_rr
ip_vs_wrr
ip_vs_lblc
ip_vs_lblcr
ip_vs_dh
ip_vs_sh
ip_vs_fo
ip_vs_nq
ip_vs_sed
ip_vs_ftp
ip_vs_sh
nf_conntrack
ip_tables
ip_set
xt_set
ipt_set
ipt_rpfilter
ipt_REJECT
ipip
```



### 启动并开机自启`systemd-modules-load.service`服务

```shell
systemctl enable --now systemd-modules-load.service  #--now = enable+start
```

### 检测是否加载ip_vs、nf_conntrack

```shell
lsmod | grep -e ip_vs -e nf_conntrack
```

![image-20240511151857333](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131259447.png)

## 调整网络和系统内核参数（所有机器）

以便更好地适应 Kubernetes 集群的需求，可以根据自己的机器配置调整

```shell
cat <<EOF > /etc/sysctl.d/k8s.conf
net.ipv4.ip_forward = 1
net.bridge.bridge-nf-call-iptables = 1
net.bridge.bridge-nf-call-ip6tables = 1
fs.may_detach_mounts = 1
vm.overcommit_memory=1
net.ipv4.conf.all.route_localnet = 1

vm.panic_on_oom=0
fs.inotify.max_user_watches=89100
fs.file-max=52706963
fs.nr_open=52706963
net.netfilter.nf_conntrack_max=2310720

net.ipv4.tcp_keepalive_time = 600
net.ipv4.tcp_keepalive_probes = 3
net.ipv4.tcp_keepalive_intvl =15
net.ipv4.tcp_max_tw_buckets = 36000
net.ipv4.tcp_tw_reuse = 1
net.ipv4.tcp_max_orphans = 327680
net.ipv4.tcp_orphan_retries = 3
net.ipv4.tcp_syncookies = 1
net.ipv4.tcp_max_syn_backlog = 16768
net.ipv4.ip_conntrack_max = 65536
net.ipv4.tcp_timestamps = 0
net.core.somaxconn = 16768
EOF
```

配置生效

```
sysctl --system
```

![image-20240511152646771](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131259880.png)

## 所有节点配置完内核后，重启服务器

保证重启后内核依旧加载

```
reboot
```

查看重启后加载ip_vs、nf_conntrack是否生效

```shell
lsmod | grep -e ip_vs -e nf_conntrack
```

![image-20240511152831972](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131259296.png)

## 安装Docker（所有机器）

### 安装docker

```shell
yum remove docker*
yum install -y yum-utils
yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
yum install -y docker-ce-19.03.9  docker-ce-cli-19.03.9 containerd.io-1.4.4
```

### 修改docker配置,

新版kubelet建议使用systemd，所以可以把docker的CgroupDriver改成systemd

```shell
//配置镜像加速
mkdir /etc/docker

cat > /etc/docker/daemon.json <<EOF
{
  "exec-opts": ["native.cgroupdriver=systemd"],
  "registry-mirrors": ["https://vpmkvcwz.mirror.aliyuncs.com"]
}
EOF
```

重启+开机自启

```
systemctl daemon-reload && systemctl enable --now docker
```

验证

```
docker ps
```

![image-20240511154415933](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131259472.png)



# 证书工具准备

## PKI

[https://baike.baidu.com/item/%E5%85%AC%E9%92%A5%E5%9F%BA%E7%A1%80%E8%AE%BE%E6%96%BD/10881894](https://baike.baidu.com/item/公钥基础设施/10881894)

在 Kubernetes 中，PKI（Public Key Infrastructure，公钥基础设施）用于管理和保护密钥和证书，以确保集群中各个组件之间的安全通信。Kubernetes PKI 主要包括以下几种类型的证书和密钥：

1. **CA 证书和密钥（Certificate Authority）**：用于签发其他证书的根证书和私钥。Kubernetes 集群通常会有一个或多个 CA 来管理证书。
2. **API Server 证书和密钥**：用于保护 API Server（Kubernetes API）的通信。API Server 是 Kubernetes 集群中最重要的组件之一，用于管理和控制整个集群。
3. **Kubelet 证书和密钥**：每个节点上的 Kubelet 都有自己的证书和密钥，用于与 API Server 进行安全通信，并确认节点的身份。
4. **Service Account 证书和密钥**：用于验证 Service Account 的身份，并授予 Pod 访问 Kubernetes API 的权限。
5. **etcd 证书和密钥**：用于保护 etcd 存储的通信。etcd 是 Kubernetes 集群中的分布式键值存储，存储着整个集群的状态信息。
6. **其他组件证书和密钥**：例如，kube-proxy、kube-controller-manager、kube-scheduler 等组件也会有各自的证书和密钥，用于保护它们之间的通信。



Kubernetes 需要 PKI 才能执行以下操作：

- Kubelet 的客户端证书，用于 API 服务器身份验证
- API 服务器端点的证书
- 集群管理员的客户端证书，用于 API 服务器身份认证
- API 服务器的客户端证书，用于和 Kubelet 的会话
- API 服务器的客户端证书，用于和 etcd 的会话
- 控制器管理器的客户端证书/kubeconfig，用于和 API 服务器的会话
- 调度器的客户端证书/kubeconfig，用于和 API 服务器的会话
- [前端代理](https://kubernetes.io/zh/docs/tasks/extend-kubernetes/configure-aggregation-layer/) 的客户端及服务端证书

> **说明：** 只有当你运行 kube-proxy 并要支持 [扩展 API 服务器](https://kubernetes.io/zh/docs/tasks/extend-kubernetes/setup-extension-api-server/) 时，才需要 `front-proxy` 证书

etcd 还实现了双向 TLS 来对客户端和对其他对等节点进行身份验证

https://kubernetes.io/zh/docs/setup/best-practices/certificates/#%E9%9B%86%E7%BE%A4%E6%98%AF%E5%A6%82%E4%BD%95%E4%BD%BF%E7%94%A8%E8%AF%81%E4%B9%A6%E7%9A%84



学习证书： https://www.cnblogs.com/technology178/p/14094375.html

### 证书生成

#### 集群相关证书类型

- **client certificate**： 用于服务端认证客户端,例如etcdctl、etcd proxy、fleetctl、docker客户端
- **server certificate**: 服务端使用，客户端以此验证服务端身份,例如docker服务端、kube-apiserver
- **peer certificate**: 双向证书，用于etcd集群成员间通信

根据认证对象可以将证书分成三类：

- 服务器证书`server cert`，
- 客户端证书`client cert`，
- 对等证书`peer cert`(表示既是`server cert`又是`client cert`



在kubernetes 集群中需要的证书种类如下：

-  `etcd` 节点需要标识自己服务的server cert，也需要client cert与etcd集群其他节点交互，当然可以分别指定2个证书，也可以使用一个对等证书
-  `master` 节点需要标识 apiserver服务的server cert，也需要client cert连接etcd集群，这里也使用一个对等证书
-  `kubectl` `calico` `kube-proxy` 只需要`client cert`，因此证书请求中 hosts 字段可以为空
-  `kubelet`证书比较特殊，不是手动生成，它由node节点`TLS BootStrap`向`apiserver`请求，由`master`节点的`controller-manager` 自动签发，包含一个`client cert` 和一个`server cert`

#### cfssl使用

CFSSL是CloudFlare开源的一款PKI/TLS工具。 CFSSL 包含一个命令行工具 和一个用于 签名，验证并且捆绑TLS证书的 HTTP API 服务。 使用Go语言编写。

Github 地址： https://github.com/cloudflare/cfssl
官网地址： https://pkg.cfssl.org/

CFSSL 组成:

- 自定义构建 TLS PKI 工具
- the `cfssl` program, which is the canonical command line utility using the CFSSL packages.
- the `multirootca` program, which is a certificate authority server that can use multiple signing keys.
- the `mkbundle` program is used to build certificate pool bundles.
- the `cfssljson` program, which takes the JSON output from the `cfssl` and `multirootca` programs and writes certificates, keys, CSRs, and bundles to disk.

安装：去官网下载`cfssl-certinfo_linux-amd64`  `cfssljson_linux-amd64`  `cfssl_linux-amd64`这三个组件

master1下载核心组件

```shell
wget https://github.com/cloudflare/cfssl/releases/download/v1.5.0/cfssl-certinfo_1.5.0_linux_amd64
wget https://github.com/cloudflare/cfssl/releases/download/v1.5.0/cfssl_1.5.0_linux_amd64
wget https://github.com/cloudflare/cfssl/releases/download/v1.5.0/cfssljson_1.5.0_linux_amd64
```

![image-20240511114014488](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131259528.png)

授予执行权限

```shell
chmod +x cfssl*
```

批量重命名，缩短名称

```shell
for name in `ls cfssl*`; do mv $name ${name%_1.5.0_linux_amd64};  done
```

![image-20240511114048062](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131259680.png)

移动到文件/usr/bin

```shell
mv cfssl* /usr/bin
```

这样就可以直接使用命令操作

![image-20240511114223056](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131259760.png)





#### 证书生成

**生成ca配置**

- client certificate： 用于服务端认证客户端,例如etcdctl、etcd proxy、fleetctl、docker客户端
- server certificate:   服务端使用，客户端以此验证服务端身份,例如docker服务端、kube-apiserver
- peer certificate:      双向证书，用于etcd集群成员间通信 

**创建ca配置文件 (ca-config.json)**

- 相当于证书颁发机构的工作规章制度
- "ca-config.json"：可以定义多个 profiles，分别指定不同的过期时间、使用场景等参数；后续在签名证书时使用某个 profile；
- "signing"：表示该证书可用于签名其它证书；生成的 ca.pem 证书中 CA=TRUE；
- "server auth"：表示client可以用该 CA 对server提供的证书进行验证；
- "client auth"：表示server可以用该CA对client提供的证书进行验证；

准备文件夹存放所有证书信息。看看kubeadm 如何组织有序的结构的（三个master节点都先创建出目录）

```shell
mkdir -p /etc/kubernetes/pki
cd /etc/kubernetes/pki
```

以下操作在master1中

打印默认的证书签名请求 (CSR) 配置。CSR 是用于向证书颁发机构请求签名的文件，其中包含有关您的证书的信息，比如公钥和组织信息等。

```shell
[root@k8s-master1 cfssl]# cfssl print-defaults csr

{
    "CN": "example.net",
    "hosts": [
        "example.net",
        "www.example.net"
    ],
    "key": {
        "algo": "ecdsa",
        "size": 256
    },
    "names": [
        {
            "C": "US",
            "ST": "CA",
            "L": "San Francisco"
        }
    ]
}
```





## ca根配置

 CA 配置信息

```shell
vi /etc/kubernetes/pki/ca-config.json

{
    "signing": {
        "default": {
            "expiry": "87600h"
        },
        "profiles": {
            "server": {
                "expiry": "87600h",
                "usages": [
                    "signing",
                    "key encipherment",
                    "server auth"
                ]
            },
            "client": {
                "expiry": "87600h",
                "usages": [
                    "signing",
                    "key encipherment",
                    "client auth"
                ]
            },
            "peer": {
                "expiry": "87600h",
                "usages": [
                    "signing",
                    "key encipherment",
                    "server auth",
                    "client auth"
                ]
            },
            "kubernetes": {
                "expiry": "87600h",
                "usages": [
                    "signing",
                    "key encipherment",
                    "server auth",
                    "client auth"
                ]
            },
            "etcd": {
                "expiry": "87600h",
                "usages": [
                    "signing",
                    "key encipherment",
                    "server auth",
                    "client auth"
                ]
            }
        }
    }
}
```

![image-20240512195257012](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131300022.png)

## ca签名请求

CSR是Certificate Signing Request的英文缩写，即证书签名请求文件

```shell
vi /etc/kubernetes/pki/ca-csr.json

{
  "CN": "kubernetes",
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "ST": "Beijing",
      "L": "Beijing",
      "O": "Kubernetes",
      "OU": "Kubernetes"
    }
  ],
  "ca": {
    "expiry": "87600h"
  }
}
```

- **CN(Common Name)**: 
  - 公用名（Common Name）必须填写，一般可以是网站域
- **O(Organization)**: 
  - Organization（组织名）是必须填写的，如果申请的是OV、EV型证书，组织名称必须严格和企业在政府登记名称一致，一般需要和营业执照上的名称完全一致。不可以使用缩写或者商标。如果需要使用英文名称，需要有DUNS编码或者律师信证明。
- **OU(Organization Unit)**
  - OU单位部门，这里一般没有太多限制，可以直接填写IT DEPT等皆可。
- **C(City)**
  - City是指申请单位所在的城市。
- **ST(State/Province)**
  - ST是指申请单位所在的省份。
- **C(Country Name）**
  - C是指国家名称，这里用的是两位大写的国家代码，中国是CN。

## 生成证书

生成ca证书和私钥

```shell
cfssl gencert -initca ca-csr.json | cfssljson -bare ca -
```

生成ca.csr、 ca.pem(ca公钥)、 ca-key.pem(ca私钥,妥善保管)

这个命令的结果将会生成三个文件：

- `ca.pem`: 包含 CA 证书的 PEM 格式文件。
- `ca-key.pem`: 包含 CA 私钥的 PEM 格式文件。
- `ca.csr`: 包含 CA 证书签名请求的 PEM 格式文件（可选）

![image-20240511163905009](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131300020.png)



# etcd高可用搭建

## etcd文档

etcd示例：https://etcd.io/docs/v3.4/demo/      参照示例学习etcd使用

etcd构建：https://etcd.io/docs/v3.4/dl-build/   参照etcd-k8s集群量规划指南。大家参照这个标准建立集群

etcd部署：https://etcd.io/docs/v3.4/op-guide/  参照部署手册，学习etcd配置和集群部署

## 下载etcd

etc安装到所有master节点

发送etcd包准备部署etcd高可用

```
wget https://github.com/etcd-io/etcd/releases/download/v3.4.16/etcd-v3.4.16-linux-amd64.tar.gz
```

复制到其他master节点的/root/目录下

```
for i in k8s-master1 k8s-master2 k8s-master3;do scp etcd-* root@$i:/root/;done
```

![image-20240511170901955](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131300309.png)

三个master节点执行 解压到 /usr/local/bin

```
tar -zxvf etcd-v3.4.16-linux-amd64.tar.gz --strip-components=1 -C /usr/local/bin etcd-v3.4.16-linux-amd64/etcd{,ctl}
```

验证

```
etcdctl
```

![image-20240511171019147](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131300265.png)

## etcd证书

安装参考https://etcd.io/docs/next/op-guide/hardware/#small-cluster  

### 创建etcd的根机构

```shell
vi /etc/kubernetes/pki/etcd-ca-csr.json

{
  "CN": "etcd",
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "ST": "Beijing",
      "L": "Beijing",
      "O": "etcd",
      "OU": "etcd"
    }
  ],
  "ca": {
    "expiry": "87600h"
  }
}
```

生成etcd根ca证书

```shell
mkdir /etc/kubernetes/pki/etcd

cfssl gencert -initca etcd-ca-csr.json | cfssljson -bare /etc/kubernetes/pki/etcd/ca -
```

![image-20240511172633808](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131300303.png)

生成了根机构的公钥、私钥

### 颁发证书

etcd机构颁发ca根证书，创建证书申请，申请证书

etcd-dongguo-csr.json

```shell
vi /etc/kubernetes/pki/etcd-dongguo-csr.json
{
    "CN": "etcd-dongguo",
    "key": {
        "algo": "rsa",
        "size": 2048
    },
    "hosts": [  
        "127.0.0.1",
        "k8s-master1",
        "k8s-master2",
        "k8s-master3",
        "192.168.122.143",
        "192.168.122.144",
        "192.168.122.145"
    ],
    "names": [
        {
            "C": "CN",
            "L": "beijing",
            "O": "etcd",
            "ST": "beijing",
            "OU": "System"
        }
    ]
}
```

hosts用自己的主机名和ip

签发dongguo的etcd证书

```shell
cfssl gencert \
   -ca=/etc/kubernetes/pki/etcd/ca.pem \
   -ca-key=/etc/kubernetes/pki/etcd/ca-key.pem \
   -config=/etc/kubernetes/pki/ca-config.json \
   -profile=etcd \
   etcd-dongguo-csr.json | cfssljson -bare /etc/kubernetes/pki/etcd/etcd
```

![image-20240511174104477](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131300542.png)

通过使用已有的根证书和私钥来签发 etcd 的证书和私钥

这个命令的结果将会生成四个文件：

- `/etc/kubernetes/pki/etcd/etcd.pem`: 包含 etcd 的证书的 PEM 格式文件。
- `/etc/kubernetes/pki/etcd/etcd-key.pem`: 包含 etcd 的私钥的 PEM 格式文件。
- `/etc/kubernetes/pki/etcd/etcd.csr`: 包含 etcd 的证书签名请求的 PEM 格式文件（可选）。
- `/etc/kubernetes/pki/etcd/etcd-key.json`: 包含 etcd 的私钥的 JSON 格式文件（可选）。





把生成的etcd证书，复制给其他master机器

其他master机器先创建目录

```
mkdir -p /etc/kubernetes/pki
```

master1复制给其他master机器

```shell
for i in k8s-master2 k8s-master3;do scp -r /etc/kubernetes/pki/etcd root@$i:/etc/kubernetes/pki;done
```

![image-20240511174402377](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131300720.png)

验证

![image-20240511174428639](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131300545.png)

## etcd高可用安装

etcd配置文件示例： https://etcd.io/docs/v3.4/op-guide/configuration/

etcd高可用安装示例： https://etcd.io/docs/v3.4/op-guide/clustering/

为了保证启动配置一致性，我们编写etcd配置文件，并将etcd做成service启动

三个etcd机器都创建  /etc/etcd  目录，准备存储etcd配置信息

```shell
mkdir -p /etc/etcd
vi /etc/etcd/etcd.yaml
```

k8s-master1：

```shell
name: 'etcd-master1'  #每个机器可以写自己的域名,不能重复
data-dir: /var/lib/etcd
wal-dir: /var/lib/etcd/wal
snapshot-count: 5000
heartbeat-interval: 100
election-timeout: 1000
quota-backend-bytes: 0
listen-peer-urls: 'https://192.168.122.143:2380'  # 本机ip+2380端口，代表和集群通信
listen-client-urls: 'https://192.168.122.143:2379,http://127.0.0.1:2379' #改为自己的
max-snapshots: 3
max-wals: 5
cors:
initial-advertise-peer-urls: 'https://192.168.122.143:2380' #自己的ip
advertise-client-urls: 'https://192.168.122.143:2379'  #自己的ip
discovery:
discovery-fallback: 'proxy'
discovery-proxy:
discovery-srv:
initial-cluster: 'etcd-master1=https://192.168.122.143:2380,etcd-master2=https://192.168.122.144:2380,etcd-master3=https://192.168.122.145:2380' #这里不一样
initial-cluster-token: 'etcd-k8s-cluster'
initial-cluster-state: 'new'
strict-reconfig-check: false
enable-v2: true
enable-pprof: true
proxy: 'off'
proxy-failure-wait: 5000
proxy-refresh-interval: 30000
proxy-dial-timeout: 1000
proxy-write-timeout: 5000
proxy-read-timeout: 0
client-transport-security:
  cert-file: '/etc/kubernetes/pki/etcd/etcd.pem'
  key-file: '/etc/kubernetes/pki/etcd/etcd-key.pem'
  client-cert-auth: true
  trusted-ca-file: '/etc/kubernetes/pki/etcd/ca.pem'
  auto-tls: true
peer-transport-security:
  cert-file: '/etc/kubernetes/pki/etcd/etcd.pem'
  key-file: '/etc/kubernetes/pki/etcd/etcd-key.pem'
  peer-client-cert-auth: true
  trusted-ca-file: '/etc/kubernetes/pki/etcd/ca.pem'
  auto-tls: true
debug: false
log-package-levels:
log-outputs: [default]
force-new-cluster: false
```

k8s-master2：

```shell
name: 'etcd-master2'  #每个机器可以写自己的域名,不能重复
data-dir: /var/lib/etcd
wal-dir: /var/lib/etcd/wal
snapshot-count: 5000
heartbeat-interval: 100
election-timeout: 1000
quota-backend-bytes: 0
listen-peer-urls: 'https://192.168.122.144:2380'  # 本机ip+2380端口，代表和集群通信
listen-client-urls: 'https://192.168.122.144:2379,http://127.0.0.1:2379' #改为自己的
max-snapshots: 3
max-wals: 5
cors:
initial-advertise-peer-urls: 'https://192.168.122.144:2380' #自己的ip
advertise-client-urls: 'https://192.168.122.144:2379'  #自己的ip
discovery:
discovery-fallback: 'proxy'
discovery-proxy:
discovery-srv:
initial-cluster: 'etcd-master1=https://192.168.122.143:2380,etcd-master2=https://192.168.122.144:2380,etcd-master3=https://192.168.122.145:2380' #这里不一样
initial-cluster-token: 'etcd-k8s-cluster'
initial-cluster-state: 'new'
strict-reconfig-check: false
enable-v2: true
enable-pprof: true
proxy: 'off'
proxy-failure-wait: 5000
proxy-refresh-interval: 30000
proxy-dial-timeout: 1000
proxy-write-timeout: 5000
proxy-read-timeout: 0
client-transport-security:
  cert-file: '/etc/kubernetes/pki/etcd/etcd.pem'
  key-file: '/etc/kubernetes/pki/etcd/etcd-key.pem'
  client-cert-auth: true
  trusted-ca-file: '/etc/kubernetes/pki/etcd/ca.pem'
  auto-tls: true
peer-transport-security:
  cert-file: '/etc/kubernetes/pki/etcd/etcd.pem'
  key-file: '/etc/kubernetes/pki/etcd/etcd-key.pem'
  peer-client-cert-auth: true
  trusted-ca-file: '/etc/kubernetes/pki/etcd/ca.pem'
  auto-tls: true
debug: false
log-package-levels:
log-outputs: [default]
force-new-cluster: false
```

k8s-master3：

```shell
name: 'etcd-master3'  #每个机器可以写自己的域名,不能重复
data-dir: /var/lib/etcd
wal-dir: /var/lib/etcd/wal
snapshot-count: 5000
heartbeat-interval: 100
election-timeout: 1000
quota-backend-bytes: 0
listen-peer-urls: 'https://192.168.122.145:2380'  # 本机ip+2380端口，代表和集群通信
listen-client-urls: 'https://192.168.122.145:2379,http://127.0.0.1:2379' #改为自己的
max-snapshots: 3
max-wals: 5
cors:
initial-advertise-peer-urls: 'https://192.168.122.145:2380' #自己的ip
advertise-client-urls: 'https://192.168.122.145:2379'  #自己的ip
discovery:
discovery-fallback: 'proxy'
discovery-proxy:
discovery-srv:
initial-cluster: 'etcd-master1=https://192.168.122.143:2380,etcd-master2=https://192.168.122.144:2380,etcd-master3=https://192.168.122.145:2380' #这里不一样
initial-cluster-token: 'etcd-k8s-cluster'
initial-cluster-state: 'new'
strict-reconfig-check: false
enable-v2: true
enable-pprof: true
proxy: 'off'
proxy-failure-wait: 5000
proxy-refresh-interval: 30000
proxy-dial-timeout: 1000
proxy-write-timeout: 5000
proxy-read-timeout: 0
client-transport-security:
  cert-file: '/etc/kubernetes/pki/etcd/etcd.pem'
  key-file: '/etc/kubernetes/pki/etcd/etcd-key.pem'
  client-cert-auth: true
  trusted-ca-file: '/etc/kubernetes/pki/etcd/ca.pem'
  auto-tls: true
peer-transport-security:
  cert-file: '/etc/kubernetes/pki/etcd/etcd.pem'
  key-file: '/etc/kubernetes/pki/etcd/etcd-key.pem'
  peer-client-cert-auth: true
  trusted-ca-file: '/etc/kubernetes/pki/etcd/ca.pem'
  auto-tls: true
debug: false
log-package-levels:
log-outputs: [default]
force-new-cluster: false
```

将三台机器的etcd做成service，开机启动

```shell
vi /usr/lib/systemd/system/etcd.service

[Unit]
Description=Etcd Service
Documentation=https://etcd.io/docs/v3.4/op-guide/clustering/
After=network.target

[Service]
Type=notify
ExecStart=/usr/local/bin/etcd --config-file=/etc/etcd/etcd.yaml
Restart=on-failure
RestartSec=10
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
Alias=etcd3.service
```

加载&开机启动

```
systemctl daemon-reload && systemctl enable --now etcd
```

### 测试etcd访问

查看etcd集群状态

```shell
etcdctl --endpoints="192.168.122.143:2379,192.168.122.144:2379,192.168.122.145:2379" --cacert=/etc/kubernetes/pki/etcd/ca.pem --cert=/etc/kubernetes/pki/etcd/etcd.pem --key=/etc/kubernetes/pki/etcd/etcd-key.pem  endpoint status --write-out=table
```

![image-20240512201600535](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131300608.png)

每次命令都要写endpoints、cacert是非常麻烦的，当然也可以暴露一些变量，以后自动用环境变量定义的证书

```shell
export ETCDCTL_API=3
HOST_1=192.168.122.143
HOST_2=192.168.122.144
HOST_3=192.168.122.145
ENDPOINTS=$HOST_1:2379,$HOST_2:2379,$HOST_3:2379

## 导出环境变量，方便测试，参照https://github.com/etcd-io/etcd/tree/main/etcdctl
export ETCDCTL_DIAL_TIMEOUT=3s
export ETCDCTL_CACERT=/etc/kubernetes/pki/etcd/ca.pem
export ETCDCTL_CERT=/etc/kubernetes/pki/etcd/etcd.pem
export ETCDCTL_KEY=/etc/kubernetes/pki/etcd/etcd-key.pem
export ETCDCTL_ENDPOINTS=$HOST_1:2379,$HOST_2:2379,$HOST_3:2379
```



列出当前 etcd 集群的成员

```
etcdctl  member list --write-out=table
```

![image-20240512075116991](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131300558.png)

查看etcd集群状态

```
etcdctl endpoint status --write-out=table
```

![image-20240512080155948](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131300504.png)

更多etcdctl命令，https://etcd.io/docs/v3.4/demo/#access-etcd

# k8s组件与证书

## K8s离线安装包

https://github.com/kubernetes/kubernetes  找到changelog对应版本

如https://github.com/kubernetes/kubernetes/blob/master/CHANGELOG/CHANGELOG-1.21.md

![image-20240512080647969](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131300182.png)

下载kubernetes-server-linux-amd64.tar.gz

![image-20240512080721812](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131300673.png)

或者也可以在线下载

```shell
wget https://dl.k8s.io/v1.21.1/kubernetes-server-linux-amd64.tar.gz
```

![image-20240512081337743](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131300133.png)

## master节点准备

把kubernetes把复制给所有节点

```shell
for i in k8s-master1 k8s-master2 k8s-master3  k8s-node1 k8s-node2 k8s-node3;do scp kubernetes-server-* root@$i:/root/;done
```

![image-20240512081712731](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131300157.png)

所有master节点解压kubelet，kubectl等到 /usr/local/bin。

```shell
tar -xvf kubernetes-server-linux-amd64.tar.gz  --strip-components=3 -C /usr/local/bin kubernetes/server/bin/kube{let,ctl,-apiserver,-controller-manager,-scheduler,-proxy}
```

![image-20240512082058289](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301887.png)

所有node节点

```shell
tar -xvf kubernetes-server-linux-amd64.tar.gz  --strip-components=3 -C /usr/local/bin kubernetes/server/bin/kube{let,-proxy}
```

master需要全部组件，node节点只需要 /usr/local/bin目录下的 kubelet、kube-proxy，为了方便，也给node复制所有

![image-20240512202432596](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301110.png)

接下来的操作在master1节点上操作，生成证书后发给其他master节点即可

## apiserver 证书生成

apiserver 申请证书

### apiserver-csr.json 

10.96.0. 为service网段。可以自定义 如： 66.66.0.1

```json
vi /etc/kubernetes/pki/apiserver-csr.json 
{
    "CN": "kube-apiserver",
    "hosts": [
      "10.96.0.1",
      "127.0.0.1",
      "192.168.122.143",
      "192.168.122.144",
      "192.168.122.145",
      "192.168.122.146",
      "192.168.122.147",
      "192.168.122.148",
      "kubernetes",
      "kubernetes.default",
      "kubernetes.default.svc",
      "kubernetes.default.svc.cluster",
      "kubernetes.default.svc.cluster.local"
    ],
    "key": {
        "algo": "rsa",
        "size": 2048
    },
    "names": [
        {
            "C": "CN",
            "L": "BeiJing",
            "ST": "BeiJing",
            "O": "Kubernetes",
            "OU": "Kubernetes"
        }
    ]
}
```

证书相关统一放在/etc/kubernetes/pki/目录下

![image-20240512083548878](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301230.png)

### 生成apiserver证书

```shell
cfssl gencert   -ca=/etc/kubernetes/pki/ca.pem   -ca-key=/etc/kubernetes/pki/ca-key.pem   -config=/etc/kubernetes/pki/ca-config.json   -profile=kubernetes   apiserver-csr.json | cfssljson -bare /etc/kubernetes/pki/apiserver
```

![image-20240512085021155](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301157.png)



## front-proxy证书生成

https://kubernetes.io/zh/docs/tasks/extend-kubernetes/configure-aggregation-layer/

他是apiserver聚合层，后来支持CRD(自定义的资源文件)

注意：front-proxy不建议用新的CA机构签发证书，可能导致通过他代理的组件如metrics-server权限不可用。

如果用新的，api-server配置添加 --requestheader-allowed-names=front-proxy-client

### front-proxy-ca-csr.json

front-proxy根ca

```json
vi /etc/kubernetes/pki/front-proxy-ca-csr.json
{
  "CN": "kubernetes",
  "key": {
     "algo": "rsa",
     "size": 2048
  }
}
```

front-proxy 根ca生成

```
cfssl gencert   -initca front-proxy-ca-csr.json | cfssljson -bare /etc/kubernetes/pki/front-proxy-ca
```

![image-20240512085133214](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301200.png)

### front-proxy-client证书

```json
vi  /etc/kubernetes/pki/front-proxy-client-csr.json  #准备申请client客户端

{
  "CN": "front-proxy-client",
  "key": {
     "algo": "rsa",
     "size": 2048
  }
}
```

生成front-proxy-client 证书

```shell
cfssl gencert   -ca=/etc/kubernetes/pki/front-proxy-ca.pem   -ca-key=/etc/kubernetes/pki/front-proxy-ca-key.pem   -config=ca-config.json   -profile=kubernetes   front-proxy-client-csr.json | cfssljson -bare /etc/kubernetes/pki/front-proxy-client
```

![image-20240512085248858](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301534.png)

## controller-manage证书生成与配置

### controller-manager-csr.json

```shell
vi /etc/kubernetes/pki/controller-manager-csr.json

{
  "CN": "system:kube-controller-manager",
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "ST": "Beijing",
      "L": "Beijing",
      "O": "system:kube-controller-manager",
      "OU": "Kubernetes"
    }
  ]
}
```

### 生成证书

```shell
cfssl gencert \
   -ca=/etc/kubernetes/pki/ca.pem \
   -ca-key=/etc/kubernetes/pki/ca-key.pem \
   -config=ca-config.json \
   -profile=kubernetes \
  controller-manager-csr.json | cfssljson -bare /etc/kubernetes/pki/controller-manager
```

![image-20240512100524530](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301863.png)

### 生成配置

controller-manager.conf

```shell
kubectl config set-cluster kubernetes \
     --certificate-authority=/etc/kubernetes/pki/ca.pem \
     --embed-certs=true \
     --server=https://192.168.122.143:6443 \
     --kubeconfig=/etc/kubernetes/controller-manager.conf
```

set-cluster：设置一个集群项

![image-20240512125837811](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301011.png)

接下来定义其他配置

设置一个环境项，一个上下文context

```shell
kubectl config set-context system:kube-controller-manager@kubernetes \
    --cluster=kubernetes \
    --user=system:kube-controller-manager \
    --kubeconfig=/etc/kubernetes/controller-manager.conf
```

![image-20240512101130139](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301064.png)

set-credentials 设置一个用户项

```shell
kubectl config set-credentials system:kube-controller-manager \
     --client-certificate=/etc/kubernetes/pki/controller-manager.pem \
     --client-key=/etc/kubernetes/pki/controller-manager-key.pem \
     --embed-certs=true \
     --kubeconfig=/etc/kubernetes/controller-manager.conf
```

![image-20240512101113368](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301942.png)

使用某个环境当做默认环境

```shell
kubectl config use-context system:kube-controller-manager@kubernetes \
     --kubeconfig=/etc/kubernetes/controller-manager.conf
```

![image-20240512101051897](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301129.png)

查看

![image-20240512101201304](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301095.png)

## scheduler证书生成与配置

### scheduler-csr.json

```json
vi /etc/kubernetes/pki/scheduler-csr.json

{
  "CN": "system:kube-scheduler",
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "ST": "Beijing",
      "L": "Beijing",
      "O": "system:kube-scheduler",
      "OU": "Kubernetes"
    }
  ]
}
```

### 签发证书

```shell
cfssl gencert \
   -ca=/etc/kubernetes/pki/ca.pem \
   -ca-key=/etc/kubernetes/pki/ca-key.pem \
   -config=/etc/kubernetes/pki/ca-config.json \
   -profile=kubernetes \
   scheduler-csr.json | cfssljson -bare /etc/kubernetes/pki/scheduler
```

![image-20240512101548656](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301962.png)

### 生成配置

scheduler.conf

```shell
kubectl config set-cluster kubernetes \
     --certificate-authority=/etc/kubernetes/pki/ca.pem \
     --embed-certs=true \
     --server=https://192.168.122.143:6443 \
     --kubeconfig=/etc/kubernetes/scheduler.conf


kubectl config set-credentials system:kube-scheduler \
     --client-certificate=/etc/kubernetes/pki/scheduler.pem \
     --client-key=/etc/kubernetes/pki/scheduler-key.pem \
     --embed-certs=true \
     --kubeconfig=/etc/kubernetes/scheduler.conf

kubectl config set-context system:kube-scheduler@kubernetes \
     --cluster=kubernetes \
     --user=system:kube-scheduler \
     --kubeconfig=/etc/kubernetes/scheduler.conf


kubectl config use-context system:kube-scheduler@kubernetes \
     --kubeconfig=/etc/kubernetes/scheduler.conf
```



查看

![image-20240512101648652](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301850.png)

## admin证书生成与配置

### admin-csr.json

```json
vi /etc/kubernetes/pki/admin-csr.json
{
  "CN": "admin",
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "ST": "Beijing",
      "L": "Beijing",
      "O": "system:masters",
      "OU": "Kubernetes"
    }
  ]
}
```

### 生成证书

```shell
cfssl gencert \
   -ca=/etc/kubernetes/pki/ca.pem \
   -ca-key=/etc/kubernetes/pki/ca-key.pem \
   -config=/etc/kubernetes/pki/ca-config.json \
   -profile=kubernetes \
   admin-csr.json | cfssljson -bare /etc/kubernetes/pki/admin
```

![image-20240512102006203](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301721.png)

### 生成配置

```shell
kubectl config set-cluster kubernetes \
--certificate-authority=/etc/kubernetes/pki/ca.pem \
--embed-certs=true \
--server=https://192.168.122.143:6443 \
--kubeconfig=/etc/kubernetes/admin.conf


kubectl config set-credentials kubernetes-admin \
--client-certificate=/etc/kubernetes/pki/admin.pem \
--client-key=/etc/kubernetes/pki/admin-key.pem \
--embed-certs=true \
--kubeconfig=/etc/kubernetes/admin.conf


kubectl config set-context kubernetes-admin@kubernetes \
--cluster=kubernetes \
--user=kubernetes-admin \
--kubeconfig=/etc/kubernetes/admin.conf

kubectl config use-context kubernetes-admin@kubernetes \
--kubeconfig=/etc/kubernetes/admin.conf
```



查看

![image-20240512102119713](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301601.png)

## kubelet证书

kubelet将使用 bootstrap 引导机制，自动颁发证书，所以我们不用配置了。要不然，1万台机器，一个万kubelet，证书配置到明年去。。。

## ServiceAccount Key生成

k8s底层，每创建一个ServiceAccount，都会分配一个Secret，而Secret里面有秘钥，秘钥就是由我们接下来的sa生成的。所以我们提前创建出sa信息

```shell
openssl genrsa -out /etc/kubernetes/pki/sa.key 2048

openssl rsa -in /etc/kubernetes/pki/sa.key -pubout -out /etc/kubernetes/pki/sa.pub
```

![image-20240512102420904](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301874.png)

## 发送证书到其他节点

/etc/kubernetes

```shell
# 在master1上执行
for NODE in k8s-master2 k8s-master3
do
	for FILE in admin.conf controller-manager.conf scheduler.conf
	do
	scp /etc/kubernetes/${FILE} $NODE:/etc/kubernetes/${FILE}
	done
done
```

![image-20240512102715994](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301929.png)

查看

![image-20240512102758871](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301008.png)





# 组件启动

## 提前创建相关目录

 所有master执行

```shell
mkdir -p /etc/kubernetes/manifests/ /etc/systemd/system/kubelet.service.d /var/lib/kubelet /var/log/kubernetes
```

## 三个master节点kube-xx相关的程序都在 /usr/local/bin

```shell
for NODE in k8s-master2 k8s-master3
do
	scp -r /etc/kubernetes/* root@$NODE:/etc/kubernetes/
done
```

![image-20240512120624963](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301576.png)

## 配置apiserver服务

### 配置

==所有Master节点创建`kube-apiserver.service`，==

以下文档使用的k8s service网段为`10.96.0.0/16`，该网段不能和宿主机的网段、Pod网段的重复

特别注意：docker的网桥默认为 `172.17.0.1/16`。不要使用这个网段

```
vi /usr/lib/systemd/system/kube-apiserver.service
```

每个master节点都需要执行以下内容

```shell
[Unit]
Description=Kubernetes API Server
Documentation=https://github.com/kubernetes/kubernetes
After=network.target

[Service]
ExecStart=/usr/local/bin/kube-apiserver \
      --v=2  \
      --logtostderr=true  \
      --allow-privileged=true  \
      --bind-address=0.0.0.0  \
      --secure-port=6443  \
      --insecure-port=0  \
      --advertise-address=192.168.122.143 \
      --service-cluster-ip-range=10.96.0.0/16  \
      --service-node-port-range=30000-32767  \
      --etcd-servers=https://192.168.122.143:2379,https://192.168.122.144:2379,https://192.168.122.145:2379 \
      --etcd-cafile=/etc/kubernetes/pki/etcd/ca.pem  \
      --etcd-certfile=/etc/kubernetes/pki/etcd/etcd.pem  \
      --etcd-keyfile=/etc/kubernetes/pki/etcd/etcd-key.pem  \
      --client-ca-file=/etc/kubernetes/pki/ca.pem  \
      --tls-cert-file=/etc/kubernetes/pki/apiserver.pem  \
      --tls-private-key-file=/etc/kubernetes/pki/apiserver-key.pem  \
      --kubelet-client-certificate=/etc/kubernetes/pki/apiserver.pem  \
      --kubelet-client-key=/etc/kubernetes/pki/apiserver-key.pem  \
      --service-account-key-file=/etc/kubernetes/pki/sa.pub  \
      --service-account-signing-key-file=/etc/kubernetes/pki/sa.key  \
      --service-account-issuer=https://kubernetes.default.svc.cluster.local \
      --kubelet-preferred-address-types=InternalIP,ExternalIP,Hostname  \
      --enable-admission-plugins=NamespaceLifecycle,LimitRanger,ServiceAccount,DefaultStorageClass,DefaultTolerationSeconds,NodeRestriction,ResourceQuota  \
      --authorization-mode=Node,RBAC  \
      --enable-bootstrap-token-auth=true  \
      --requestheader-client-ca-file=/etc/kubernetes/pki/front-proxy-ca.pem  \
      --proxy-client-cert-file=/etc/kubernetes/pki/front-proxy-client.pem  \
      --proxy-client-key-file=/etc/kubernetes/pki/front-proxy-client-key.pem  \
      --requestheader-allowed-names=aggregator,front-proxy-client  \
      --requestheader-group-headers=X-Remote-Group  \
      --requestheader-extra-headers-prefix=X-Remote-Extra-  \
      --requestheader-username-headers=X-Remote-User
      # --token-auth-file=/etc/kubernetes/token.csv

Restart=on-failure
RestartSec=10s
LimitNOFILE=65535

[Install]
WantedBy=multi-user.target
```

--advertise-address： 需要改为本master节点的ip

--service-cluster-ip-range=10.96.0.0/16： 需要改为自己规划的service网段

--etcd-servers： 改为自己etcd-server的所有地址



其他master节点修改      --advertise-address=192.168.122.143

### 启动apiserver服务

```shell
systemctl daemon-reload && systemctl enable --now kube-apiserver


#查看状态
systemctl status kube-apiserver
```

![image-20240512205109332](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301469.png)

## 配置controller-manager服务

==所有Master节点配置kube-controller-manager.service==

> 文档使用的k8s Pod网段为`196.16.0.0/16`，该网段不能和宿主机的网段、k8s Service网段的重复，请按需修改;
>
> 特别注意：docker的网桥默认为 `172.17.0.1/16`。不要使用这个网段

```shell
# 所有Master节点执行
vi /usr/lib/systemd/system/kube-controller-manager.service

## --cluster-cidr=196.16.0.0/16 ： 为Pod的网段。修改成自己想规划的网段

[Unit]
Description=Kubernetes Controller Manager
Documentation=https://github.com/kubernetes/kubernetes
After=network.target

[Service]
ExecStart=/usr/local/bin/kube-controller-manager \
      --v=2 \
      --logtostderr=true \
      --address=127.0.0.1 \
      --root-ca-file=/etc/kubernetes/pki/ca.pem \
      --cluster-signing-cert-file=/etc/kubernetes/pki/ca.pem \
      --cluster-signing-key-file=/etc/kubernetes/pki/ca-key.pem \
      --service-account-private-key-file=/etc/kubernetes/pki/sa.key \
      --kubeconfig=/etc/kubernetes/controller-manager.conf \
      --leader-elect=true \
      --use-service-account-credentials=true \
      --node-monitor-grace-period=40s \
      --node-monitor-period=5s \
      --pod-eviction-timeout=2m0s \
      --controllers=*,bootstrapsigner,tokencleaner \
      --allocate-node-cidrs=true \
      --cluster-cidr=196.16.0.0/16 \
      --requestheader-client-ca-file=/etc/kubernetes/pki/front-proxy-ca.pem \
      --node-cidr-mask-size=24
      
Restart=always
RestartSec=10s

[Install]
WantedBy=multi-user.target
```

### 启动

```
# 所有master节点执行
systemctl daemon-reload && systemctl enable --now kube-controller-manager

systemctl status kube-controller-manager
```

![image-20240512210528393](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301241.png)

## 配置scheduler

==所有Master节点配置kube-scheduler.service==

```shell
vi /usr/lib/systemd/system/kube-scheduler.service 



[Unit]
Description=Kubernetes Scheduler
Documentation=https://github.com/kubernetes/kubernetes
After=network.target

[Service]
ExecStart=/usr/local/bin/kube-scheduler \
      --v=2 \
      --logtostderr=true \
      --address=127.0.0.1 \
      --leader-elect=true \
      --kubeconfig=/etc/kubernetes/scheduler.conf

Restart=always
RestartSec=10s

[Install]
WantedBy=multi-user.target
```

### 启动

```shell
systemctl daemon-reload && systemctl enable --now kube-scheduler

systemctl status kube-scheduler
```

![image-20240512122335633](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301169.png)

# TLS与引导启动原理

TLS Bootstrapping原理参照:  https://kubernetes.io/zh/docs/reference/command-line-tools-reference/kubelet-tls-bootstrapping/

## master1配置bootstrap

```
#准备一个随机token。但是我们只需要16个字符
head -c 16 /dev/urandom | od -An -t x | tr -d ' '
# 值如下： 737b177d9823531a433e368fcdb16f5f

# 生成16个字符的
head -c 8 /dev/urandom | od -An -t x | tr -d ' '
# d683399b7a553977
```



```shell
#设置集群
kubectl config set-cluster kubernetes \
--certificate-authority=/etc/kubernetes/pki/ca.pem \
--embed-certs=true \
--server=https://192.168.122.143:6443 \
--kubeconfig=/etc/kubernetes/bootstrap-kubelet.conf

#设置秘钥
kubectl config set-credentials tls-bootstrap-token-user \
--token=l6fy8c.d683399b7a553977 \
--kubeconfig=/etc/kubernetes/bootstrap-kubelet.conf 

#设置上下文
kubectl config set-context tls-bootstrap-token-user@kubernetes \
--cluster=kubernetes \
--user=tls-bootstrap-token-user \
--kubeconfig=/etc/kubernetes/bootstrap-kubelet.conf

#使用设置
kubectl config use-context tls-bootstrap-token-user@kubernetes \
--kubeconfig=/etc/kubernetes/bootstrap-kubelet.conf
```

![image-20240513074715165](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301093.png)





## master1设置kubectl执行权限

kubectl 能不能操作集群是看 /root/.kube 下有没有config文件，而config就是我们之前生成的admin.conf，具有操作权限的

```shell
# 只在master1生成，因为生产集群，我们只能让一台机器具有操作集群的权限，这样好控制

mkdir -p /root/.kube;
cp /etc/kubernetes/admin.conf /root/.kube/config
```

### 验证

```
kubectl get nodes
```

![image-20240512130333255](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131301180.png)

No resources found说明已经可以连接apiserver并获取资源

## 创建集群引导权限文件

```
# master1准备这个文件 
vi  /etc/kubernetes/bootstrap.secret.yaml
```



```shell
apiVersion: v1
kind: Secret
metadata:
  name: bootstrap-token-l6fy8c
  namespace: kube-system
type: bootstrap.kubernetes.io/token
stringData:
  description: "The default bootstrap token generated by 'kubelet '."
  token-id: l6fy8c
  token-secret: d683399b7a553977
  usage-bootstrap-authentication: "true"
  usage-bootstrap-signing: "true"
  auth-extra-groups:  system:bootstrappers:default-node-token,system:bootstrappers:worker,system:bootstrappers:ingress
 
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: kubelet-bootstrap
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: system:node-bootstrapper
subjects:
- apiGroup: rbac.authorization.k8s.io
  kind: Group
  name: system:bootstrappers:default-node-token
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: node-autoapprove-bootstrap
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: system:certificates.k8s.io:certificatesigningrequests:nodeclient
subjects:
- apiGroup: rbac.authorization.k8s.io
  kind: Group
  name: system:bootstrappers:default-node-token
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: node-autoapprove-certificate-rotation
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: system:certificates.k8s.io:certificatesigningrequests:selfnodeclient
subjects:
- apiGroup: rbac.authorization.k8s.io
  kind: Group
  name: system:nodes
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  annotations:
    rbac.authorization.kubernetes.io/autoupdate: "true"
  labels:
    kubernetes.io/bootstrapping: rbac-defaults
  name: system:kube-apiserver-to-kubelet
rules:
  - apiGroups:
      - ""
    resources:
      - nodes/proxy
      - nodes/stats
      - nodes/log
      - nodes/spec
      - nodes/metrics
    verbs:
      - "*"
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: system:kube-apiserver
  namespace: ""
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: system:kube-apiserver-to-kubelet
subjects:
  - apiGroup: rbac.authorization.k8s.io
    kind: User
    name: kube-apiserver
```

应用此文件资源内容

```
kubectl create -f /etc/kubernetes/bootstrap.secret.yaml
```

![image-20240512134402730](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302719.png)





## 所有节点配置kubelet

所有节点创建相关目录

```shell
mkdir -p /var/lib/kubelet /var/log/kubernetes /etc/systemd/system/kubelet.service.d /etc/kubernetes/manifests/
```

### 创建kubelet.service

所有节点，配置kubelet服务

```shell
vi  /usr/lib/systemd/system/kubelet.service

[Unit]
Description=Kubernetes Kubelet
Documentation=https://github.com/kubernetes/kubernetes
After=docker.service
Requires=docker.service

[Service]
ExecStart=/usr/local/bin/kubelet

Restart=always
StartLimitInterval=0
RestartSec=10

[Install]
WantedBy=multi-user.target
```

所有节点配置kubelet service配置文件

```shell
vi /etc/systemd/system/kubelet.service.d/10-kubelet.conf

[Service]
Environment="KUBELET_KUBECONFIG_ARGS=--bootstrap-kubeconfig=/etc/kubernetes/bootstrap-kubelet.conf --kubeconfig=/etc/kubernetes/kubelet.conf"
Environment="KUBELET_SYSTEM_ARGS=--network-plugin=cni --cni-conf-dir=/etc/cni/net.d --cni-bin-dir=/opt/cni/bin"
Environment="KUBELET_CONFIG_ARGS=--config=/etc/kubernetes/kubelet-conf.yml --pod-infra-container-image=registry.cn-hangzhou.aliyuncs.com/dongguo/pause:3.4.1"
Environment="KUBELET_EXTRA_ARGS=--node-labels=node.kubernetes.io/node='' "
ExecStart=
ExecStart=/usr/local/bin/kubelet $KUBELET_KUBECONFIG_ARGS $KUBELET_CONFIG_ARGS $KUBELET_SYSTEM_ARGS $KUBELET_EXTRA_ARGS
```

### 创建kubelet-conf.yml文件

所有节点，配置kubelet-conf文件

```shell
vi /etc/kubernetes/kubelet-conf.yml
```



```shell
apiVersion: kubelet.config.k8s.io/v1beta1
kind: KubeletConfiguration
address: 0.0.0.0
port: 10250
readOnlyPort: 10255
authentication:
  anonymous:
    enabled: false
  webhook:
    cacheTTL: 2m0s
    enabled: true
  x509:
    clientCAFile: /etc/kubernetes/pki/ca.pem
authorization:
  mode: Webhook
  webhook:
    cacheAuthorizedTTL: 5m0s
    cacheUnauthorizedTTL: 30s
cgroupDriver: systemd
cgroupsPerQOS: true
clusterDNS:
- 10.96.0.10
clusterDomain: cluster.local
containerLogMaxFiles: 5
containerLogMaxSize: 10Mi
contentType: application/vnd.kubernetes.protobuf
cpuCFSQuota: true
cpuManagerPolicy: none
cpuManagerReconcilePeriod: 10s
enableControllerAttachDetach: true
enableDebuggingHandlers: true
enforceNodeAllocatable:
- pods
eventBurst: 10
eventRecordQPS: 5
evictionHard:
  imagefs.available: 15%
  memory.available: 100Mi
  nodefs.available: 10%
  nodefs.inodesFree: 5%
evictionPressureTransitionPeriod: 5m0s  #缩小相应的配置
failSwapOn: true
fileCheckFrequency: 20s
hairpinMode: promiscuous-bridge
healthzBindAddress: 127.0.0.1
healthzPort: 10248
httpCheckFrequency: 20s
imageGCHighThresholdPercent: 85
imageGCLowThresholdPercent: 80
imageMinimumGCAge: 2m0s
iptablesDropBit: 15
iptablesMasqueradeBit: 14
kubeAPIBurst: 10
kubeAPIQPS: 5
makeIPTablesUtilChains: true
maxOpenFiles: 1000000
maxPods: 110
nodeStatusUpdateFrequency: 10s
oomScoreAdj: -999
podPidsLimit: -1
registryBurst: 10
registryPullQPS: 5
resolvConf: /etc/resolv.conf
rotateCertificates: true
runtimeRequestTimeout: 2m0s
serializeImagePulls: true
staticPodPath: /etc/kubernetes/manifests
streamingConnectionIdleTimeout: 4h0m0s
syncFrequency: 1m0s
volumeStatsAggPeriod: 1m0s
```



所有节点的kubelet需要我们引导启动,避免之前遗漏，全部复制一遍

```shell
for NODE in k8s-master2 k8s-master3 k8s-node1 k8s-node2 k8s-node3
do
	scp -r /etc/kubernetes/* root@$NODE:/etc/kubernetes/
done
```



### 所有节点启动kubelet

```shell
systemctl daemon-reload && systemctl enable --now kubelet


systemctl status kubelet
```

提示 "Unable to update cni config"。接下来配置cni网络即可

![image-20240513080051573](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302955.png)

查看6个机器是否都active

## kube-proxy配置

### 生成kube-proxy.conf

以下操作在master1执行

```shell
#创建kube-proxy的sa
kubectl -n kube-system create serviceaccount kube-proxy

#创建角色绑定
kubectl create clusterrolebinding system:kube-proxy \
--clusterrole system:node-proxier \
--serviceaccount kube-system:kube-proxy

#导出变量，方便后面使用
SECRET=$(kubectl -n kube-system get sa/kube-proxy --output=jsonpath='{.secrets[0].name}')
JWT_TOKEN=$(kubectl -n kube-system get secret/$SECRET --output=jsonpath='{.data.token}' | base64 -d)
PKI_DIR=/etc/kubernetes/pki
K8S_DIR=/etc/kubernetes

# 生成kube-proxy配置
# --server: 指定自己的apiserver地址或者lb地址
kubectl config set-cluster kubernetes \
--certificate-authority=/etc/kubernetes/pki/ca.pem \
--embed-certs=true \
--server=https://192.168.122.143:6443 \
--kubeconfig=${K8S_DIR}/kube-proxy.conf

# kube-proxy秘钥设置
kubectl config set-credentials kubernetes \
--token=${JWT_TOKEN} \
--kubeconfig=/etc/kubernetes/kube-proxy.conf


kubectl config set-context kubernetes \
--cluster=kubernetes \
--user=kubernetes \
--kubeconfig=/etc/kubernetes/kube-proxy.conf


kubectl config use-context kubernetes \
--kubeconfig=/etc/kubernetes/kube-proxy.conf
```

![image-20240513091110883](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302337.png)

所有节点的kubelet需要kube-proxy.conf,避免之前遗漏，全部复制一遍

```shell
for NODE in k8s-master2 k8s-master3 k8s-node1 k8s-node2 k8s-node3
do
	scp -r /etc/kubernetes/* root@$NODE:/etc/kubernetes/
done
```

### 配置kube-proxy.service

==所有节点配置 kube-proxy.service 服务，一会儿设置为开机启动==

```shell
vi /usr/lib/systemd/system/kube-proxy.service

[Unit]
Description=Kubernetes Kube Proxy
Documentation=https://github.com/kubernetes/kubernetes
After=network.target

[Service]
ExecStart=/usr/local/bin/kube-proxy \
  --config=/etc/kubernetes/kube-proxy.yaml \
  --v=2

Restart=always
RestartSec=10s

[Install]
WantedBy=multi-user.target
```

### 准备kube-proxy.yaml

一定注意修改自己的Pod网段范围

==所有机器执行==

```
vi /etc/kubernetes/kube-proxy.yaml
```



```
apiVersion: kubeproxy.config.k8s.io/v1alpha1
bindAddress: 0.0.0.0
clientConnection:
  acceptContentTypes: ""
  burst: 10
  contentType: application/vnd.kubernetes.protobuf
  kubeconfig: /etc/kubernetes/kube-proxy.conf   #kube-proxy引导文件
  qps: 5
clusterCIDR: 196.16.0.0/16  #修改为自己的Pod-CIDR
configSyncPeriod: 15m0s
conntrack:
  max: null
  maxPerCore: 32768
  min: 131072
  tcpCloseWaitTimeout: 1h0m0s
  tcpEstablishedTimeout: 24h0m0s
enableProfiling: false
healthzBindAddress: 0.0.0.0:10256
hostnameOverride: ""
iptables:
  masqueradeAll: false
  masqueradeBit: 14
  minSyncPeriod: 0s
  syncPeriod: 30s
ipvs:
  masqueradeAll: true
  minSyncPeriod: 5s
  scheduler: "rr"
  syncPeriod: 30s
kind: KubeProxyConfiguration
metricsBindAddress: 127.0.0.1:10249
mode: "ipvs"
nodePortAddresses: null
oomScoreAdj: -999
portRange: ""
udpIdleTimeout: 250ms
```

### 启动kube-proxy

所有节点启动

```shell
systemctl daemon-reload && systemctl enable --now kube-proxy
systemctl status kube-proxy
```

![image-20240513091809361](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302541.png)

# 部署calico

下载官网calico

```shell
wget https://docs.projectcalico.org/manifests/calico-etcd.yaml

mv calico-etcd.yaml calico.yaml
```

![image-20240513094839839](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302710.png)

修改etcd集群地址

```shell
sed -i 's#etcd_endpoints: "http://<ETCD_IP>:<ETCD_PORT>"#etcd_endpoints: "https://192.168.122.143:2379,https://192.168.122.144:2379,https://192.168.122.145:2379"#g' calico.yaml
```

etcd的证书内容，需要base64编码设置到yaml中

```
ETCD_CA=`cat /etc/kubernetes/pki/etcd/ca.pem | base64 -w 0 `
ETCD_CERT=`cat /etc/kubernetes/pki/etcd/etcd.pem | base64 -w 0 `
ETCD_KEY=`cat /etc/kubernetes/pki/etcd/etcd-key.pem | base64 -w 0 `
```

替换etcd中的证书base64编码后的内容

```
sed -i "s@# etcd-key: null@etcd-key: ${ETCD_KEY}@g; s@# etcd-cert: null@etcd-cert: ${ETCD_CERT}@g; s@# etcd-ca: null@etcd-ca: ${ETCD_CA}@g" calico.yaml
```

打开 etcd_ca 等默认设置（calico启动后自己生成）。

```
sed -i 's#etcd_ca: ""#etcd_ca: "/calico-secrets/etcd-ca"#g; s#etcd_cert: ""#etcd_cert: "/calico-secrets/etcd-cert"#g; s#etcd_key: "" #etcd_key: "/calico-secrets/etcd-key" #g' calico.yaml
```

修改自己的Pod网段 196.16.0.0/16

```
POD_SUBNET="196.16.0.0/16"
sed -i 's@# - name: CALICO_IPV4POOL_CIDR@- name: CALICO_IPV4POOL_CIDR@g; s@#   value: "192.168.0.0/16"@  value: '"${POD_SUBNET}"'@g' calico.yaml
```

确认calico是否修改好

```
grep "CALICO_IPV4POOL_CIDR" calico.yaml -A 1
```

应用calico配置

```
kubectl apply -f calico.yaml
```

![image-20240513095113749](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302735.png)

等待pod部署完成,因为镜像不在国内镜像仓库，需要一定时间下载

```shell
kubectl get pod -A 
```

![image-20240513101138384](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302901.png)

配置完CNI，这样k8s集群node就准备就绪了

![image-20240513101718060](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302324.png)



# 部署coreDNS

```shell
cd /etc/kubernetes/
git clone https://github.com/coredns/deployment.git

cd deployment/kubernetes
#10.96.0.10 改为 service 网段的 第 10 个ip
./deploy.sh -s -i 10.96.0.10 | kubectl apply -f -
```

![image-20240513103227600](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302165.png)

# 给机器打上role标签

这里需要注意一点，二进制安装的k8s进群。master默认是没有污点的，所以pod可能部署到master节点上。

![image-20240513102018631](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302118.png)

```shell
# 给master1打上污点。二进制部署的集群，默认master是没有污点的，可以任意调度。我们最好给一个master打上污点，保证master最小可用
kubectl taint nodes k8s-master1 node-role.kubernetes.io/master=true:NoSchedule
```



```
kubectl describe nodes k8s-master | grep Taints
```

![image-20240514191959420](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172244045.png)

给节点打标签

```sh
#给master标识为master
kubectl label node k8s-master1 node-role.kubernetes.io/master=''
kubectl label node k8s-master2 node-role.kubernetes.io/master=''
kubectl label node k8s-master3 node-role.kubernetes.io/master=''
# 给两个master标识为worker
kubectl label node k8s-master2 node-role.kubernetes.io/worker=''
kubectl label node k8s-master3 node-role.kubernetes.io/worker=''
kubectl label node k8s-node1 node-role.kubernetes.io/worker=''
kubectl label node k8s-node2 node-role.kubernetes.io/worker=''
kubectl label node k8s-node3 node-role.kubernetes.io/worker=''
```

![image-20240513103538891](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302689.png)

# 集群验证

- 验证Pod网络可访问性
  - 同名称空间，不同名称空间可以使用 ip 互相访问
  - 跨机器部署的Pod也可以互相访问
- 验证Service网络可访问性
  - 集群机器使用serviceIp可以负载均衡访问
  - pod内部可以访问service域名  serviceName.namespace
  - pod可以访问跨名称空间的service

部署以下内容进行测试

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name:  nginx-01
  namespace: default
  labels:
    app:  nginx-01
spec:
  selector:
    matchLabels:
      app: nginx-01
  replicas: 1
  template:
    metadata:
      labels:
        app:  nginx-01
    spec:
      containers:
      - name:  nginx-01
        image:  nginx
---
apiVersion: v1
kind: Service
metadata:
  name: nginx-svc
  namespace: default
spec:
  selector:
    app:  nginx-01
  type: ClusterIP
  ports:
  - name: nginx-svc
    port: 80
    targetPort: 80
    protocol: TCP
---
apiVersion: v1
kind: Namespace
metadata:
  name: hello
spec: {}
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name:  nginx-hello
  namespace: hello
  labels:
    app:  nginx-hello
spec:
  selector:
    matchLabels:
      app: nginx-hello
  replicas: 1
  template:
    metadata:
      labels:
        app:  nginx-hello
    spec:
      containers:
      - name:  nginx-hello
        image:  nginx
---
apiVersion: v1
kind: Service
metadata:
  name: nginx-svc-hello
  namespace: hello
spec:
  selector:
    app:  nginx-hello
  type: ClusterIP
  ports:
  - name: nginx-svc-hello
    port: 80
    targetPort: 80
    protocol: TCP
```



等待pod启动成功

![image-20240513110204982](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302790.png)

## 集群间通信验证

1）访问pod的ip

![image-20240513110458910](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302090.png)

2）访问service的ip

![image-20240513110622145](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302188.png)

3）进入pod中访问其他pod的ip

![image-20240513111009328](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302274.png)

4）通过服务名访问

![image-20240513111319927](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302544.png)

## 集群高可用验证

master2节点关机，k8s集群还能够正常运行

![image-20240513123424093](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302780.png)

master3节点关机，k8s集群不可用

![image-20240513123531551](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302375.png)



master2、master3恢复启动，k8s集群恢复可用

![image-20240513123607360](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302384.png)





/etc/kubernetes/kubelet-conf.yml配置evictionPressureTransitionPeriod: 5m0s

指定了节点从内存压力状态到非内存压力状态的过渡时间。在这段时间内，Kubernetes 不会驱逐容器，即使节点的内存压力超过了预设的阈值。当节点发生宕机时，Kubernetes 不会立刻驱逐容器，等到5分钟后节点还未恢复正常，才会在其他节点重新拉起容器

![image-20240513123643573](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302410.png)

关闭k8s-node2节点，观察nginx-01-5bd9d6df7b-pb6j5 pod

![image-20240513123808507](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131302929.png)

5分钟后查看pod，在k8s-master3节点重新拉起

![image-20240513124431533](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131303352.png)

# 集群优化

## docker配置优化

所有节点执行

```json
vi /etc/docker/daemon.json  

{
  "exec-opts": ["native.cgroupdriver=systemd"],
  "registry-mirrors": ["https://vpmkvcwz.mirror.aliyuncs.com"],
  "max-concurrent-downloads": 10,
  "max-concurrent-uploads": 5,
  "log-opts": {
    "max-size": "300m",
    "max-file": "2"
  },
  "live-restore": true
}

```

详细解释

1. `"exec-opts": ["native.cgroupdriver=systemd"]`: 这个选项指定了 Docker 使用的 cgroup 驱动程序。在这里，它设置为 `systemd`，这意味着 Docker 将使用 systemd 来管理容器的 cgroups。
2. `"registry-mirrors": ["https://vpmkvcwz.mirror.aliyuncs.com"]`: 这个选项用于配置 Docker 镜像的镜像加速器。在这里，指定了一个阿里云的镜像加速器，用于加速 Docker 镜像的拉取和推送操作。
3. `"max-concurrent-downloads": 10`: 这个选项设置 Docker 同时进行的最大下载任务数量。在这里，设置为最大同时进行 10 个镜像下载任务。
4. `"max-concurrent-uploads": 5`: 这个选项设置 Docker 同时进行的最大上传任务数量。在这里，设置为最大同时进行 5 个镜像上传任务。
5. `"log-opts": {...}`: 这个选项用于配置 Docker 日志的选项。在这里，设置了最大日志文件大小为 300MB，最大日志文件数为 2 个。
6. `"live-restore": true`: 这个选项设置了 Docker 是否启用容器的“实时恢复”功能。当设置为 `true` 时，Docker 守护进程会在守护进程重启时保留容器，并在启动时恢复它们的状态。这意味着即使 Docker 守护进程崩溃或重新启动，容器也能够保持运行状态。

"live-restore" 是 Docker 中的一个重要功能，它可以帮助避免因 Docker 守护进程的崩溃或重新启动而导致的容器停止运行或状态丢失的问题。启用此功能可以提高容器的可靠性和稳定性，特别是在生产环境中。

重新加载 systemd 管理的服务配置并启用 Docker 服务

```
systemctl daemon-reload && systemctl enable --now docker
```



## 优化kubelet

更多参照： https://kubernetes.io/zh/docs/reference/config-api/kubelet-config.v1beta1/

```yaml
vi /etc/kubernetes/kubelet-conf.yml

# kubeReserved： kubelet预留资源
kubeReserved:
  cpu: "500m"
  memory: 300m
  ephemeral-storage: 3Gi
systemReserved:
  cpu: "200m"
  memory: 500m
  ephemeral-storage: 3Gi
```

指定了 kubelet 在节点上预留的资源量以及系统级别的资源保留量。以下是各个字段的含义：

- **kubeReserved**：这个部分定义了 kubelet 预留的资源量。kubelet 会为 Kubernetes 的核心系统组件保留这些资源，以确保它们有足够的资源来正常运行。具体来说：

  - **cpu**：指定 kubelet 预留的 CPU 资源量。在这个例子中，预留了 500 毫核（mcore）的 CPU。
  - **memory**：指定 kubelet 预留的内存资源量。在这个例子中，预留了 300 MiB 的内存。
  - **ephemeral-storage**：指定 kubelet 预留的临时存储资源量。在这个例子中，预留了 3 Gib 的临时存储。

- **systemReserved**：这个部分定义了系统级别的资源保留量。它指定了 kubelet 在节点上保留的一般资源量，用于操作系统和其他系统级别的进程。具体来说：

  - **cpu**：指定系统级别的 CPU 资源保留量。在这个例子中，预留了 200 毫核的 CPU。
  - **memory**：指定系统级别的内存资源保留量。在这个例子中，预留了 500 MiB 的内存。
  - **ephemeral-storage**：指定系统级别的临时存储资源保留量。在这个例子中，预留了 3 Gib 的临时存储。

  重启kubelet

  ```shell
  systemctl daemon-reload && systemctl enable --now kubelet
  
  
  systemctl status kubelet
  ```

  

## 镜像时区问题

很多应用镜像时区都是UTC，而不是本机时间（当然，前提是本机时间是对的，云服务器不存在这个问题）

docker hub下载来的几乎所有Pod都是UTC时间。

![image-20240513145452834](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131505782.png)

使用Pod标准模板。挂载时区

```yaml
        volumeMounts:
        - name: localtime
          mountPath: /etc/localtime
      volumes:
      - name: localtime
        hostPath:
          path: /usr/share/zoneinfo/Asia/Shanghai 
```



例如：

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-02
  namespace: default
  labels:
    app: nginx-02
spec:
  selector:
    matchLabels:
      app: nginx-02
  replicas: 1
  template:
    metadata:
      labels:
        app: nginx-02
    spec:
      containers:
      - name: nginx-02
        image: nginx
        volumeMounts:
        - name: localtime
          mountPath: /etc/localtime
      volumes:
      - name: localtime
        hostPath:
          path: /usr/share/zoneinfo/Asia/Shanghai 
---
apiVersion: v1
kind: Service
metadata:
  name: nginx-svc02
  namespace: default
spec:
  selector:
    app:  nginx-02
  type: ClusterIP
  ports:
  - name: nginx-svc02
    port: 80
    targetPort: 80
    protocol: TCP
```

![image-20240513150453895](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131505053.png)
