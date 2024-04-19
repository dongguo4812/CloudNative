**kubernetes 集群有多种安装方式：**

二进制方式（建议生产环境使用）

kubeadm引导方式（官方推荐）

- 大致流程
  - 准备N台服务器，**内网互通**，
  - 安装Docker容器化环境
  - 安装Kubernetes
    - 三台机器安装核心组件（**kubeadm(创建集群的引导工具)**,  ***kubelet***，**kubectl（程序员用的命令行）**  ）
    - kubelet可以直接通过容器化的方式创建出之前的核心组件（api-server）【官方把核心组件做成镜像】
    - 由kubeadm引导创建集群

# 准备机器

准备三台机器

| 角色       | IP              |
| ---------- | --------------- |
| k8s-master | 192.168.122.140 |
| k8s-node1  | 192.168.122.141 |
| k8s-node2  | 192.168.122.142 |

# 安装前置环境

注：==带有都执行的标签标识三台机器都要执行==

三台机器都要执行的操作

## 1.修改 hostname

在kubernetes集群中，节点的主机名是一个很重要的标识，它被用来唯一标识集群中的每个节点。设置正确的主机名可以方便管理和诊断集群中的问题。

```shell
hostnamectl set-hostname <hostname>
```

设置 hostname 解析

```shell
echo "127.0.0.1   $(hostname)" >> /etc/hosts
```

三台机器修改为k8s-master、k8s-node1、k8s-node2，hostname名称显示在修改完重启才会生效



==接下来的操作可以使用发送键输入到所有会话一键操作==

![image-20240417173311861](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191015539.png)

## 2.关闭防火墙（都执行）

防火墙会对网络数据包进行过滤和屏蔽，可能会影响 Kubernetes 集群节点之间的通信。同时，Kubernetes 本身有较为完善的网络策略机制，可以保证集群的网络安全，因此关闭防火墙并不会对 Kubernetes 集群的安全造成影响。

```shell
systemctl stop firewalld

systemctl disable firewalld
```

![image-20240417193844573](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191022164.png)

## 3.关闭 selinux（都执行）

在 Kubernetes 集群中，SELinux 是一个可选的安全模块，它提供了强制访问控制和访问审计功能。但是在搭建 Kubernetes 集群时，为了简化配置和避免可能的问题，很多管理员选择将 SELinux 关闭。这主要是因为：

1. SELinux 对于容器的访问控制较为严格，可能会导致一些应用程序无法正常工作或无法访问必要的资源。
2. 在某些情况下，SELinux 的规则并不能很好地适应 Kubernetes 集群的安装配置，这可能会导致一些问题和错误。
3. 关闭 SELinux 可以简化配置和管理工作，使得集群的部署和维护更加便捷。但是关闭 SELinux 也会降低集群的安全性和可靠性，必须在必要的时候重新启用 SELinux。

因此，关闭 SELinux 可以使 Kubernetes 集群的部署更加简单和可靠，但也会降低集群的安全性和可靠性。在实际应用中，需要根据具体情况来确定是否需要开启或关闭 SELinux。

```shell
sed -i 's/enforcing/disabled/' /etc/selinux/config # 永久

setenforce 0 # 临时
```

![image-20240417193818539](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191023484.png)

## 4.关闭 swap（都执行）

Kubernetes 使用了 cgroup 管理容器资源。而 swap 分区可能会阻止容器使用预期的内存资源，并且可能导致应用程序在容器内部崩溃或出现其他问题。

Kubernetes 本身不会使用 swap，同时，因为容器的使用和交换内存的机制不同，如果应用程序需要使用大量内存时，容器会自动申请更多的内存，而不是使用 swap，避免了性能的损失和不可预测的行为。关闭 swap 分区可以更好地保护 Kubernetes 集群的稳定性和性能，确保容器的内存使用与性能表现的一致性。

```shell
sed -ri 's/.*swap.*/#&/' /etc/fstab # 永久

swapoff -a # 临时
```

![image-20240417193859475](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025693.png)

## 5.将桥接的 IPv4 流量传递到 iptables 的链（都执行）

在 Kubernetes 集群中，每个 Pod 都会被分配一个 IP 地址，Pod 内的容器也会被分配一个虚拟网卡和 IP 地址。当两个 Pod 之间需要通信时，它们使用这些 IP 地址进行通信。

然而，当 Pod 内的容器尝试与另一个 Pod 进行通信时，它们不会使用其 IP 地址直接发送数据包，而是会使用桥接的方式进行通信。这意味着数据包将通过 Linux 内核中的桥接设备进行传输，而不是通过网络接口发送。

为了确保这些桥接的数据包能够被正确地路由和转发，需要将它们传递到 iptables 的链中进行处理。Iptables 可以用于定义网络规则，使数据包能够正确路由到目的地。通过将桥接的 IPv4 流量传递到 iptables 的链中，可以确保 Kubernetes 集群中的 Pod 能够正确地通信，并且可以实现一些高级的网络功能，如网络策略和负载均衡等。

```shell
#确保系统在启动时能够正确地处理桥接的 IPv4 流量。
cat << EOF | sudo tee /etc/modules-load.d/k8s.conf 
br_netfilter 
EOF

#确保在使用 Kubernetes（K8s）时可以正确地处理桥接的 IPv4 流量
cat << EOF | sudo tee /etc/sysctl.d/k8s.conf 
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
EOF
```

配置生效

```shell
sudo sysctl --system # 生效
```

![image-20240417193947295](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025428.png)

## 6.时间同步（都执行）

Kubernetes 集群中的各个节点需要相互通信进行协作，因此需要保证它们的时间是同步的，以确保它们在进行计划和调度时能够准确地协调工作。如果节点的时间不同步，则可能会出现以下问题：

1. 容器运行情况出现不可预测的错误。
2. 调度器无法准确计算任务的完成时间，导致任务超时或者在不合适的节点上进行调度。
3. 监控和日志收集系统可能会出现时间不对齐的情况，导致数据分析的结果不准确。

因此，为了保证集群的正常运行，需要在集群中的各个节点上同步时间。

```shell
yum install ntpdate -y

ntpdate time.windows.com
```

![image-20240417194105694](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025776.png)

==注配置完以上命令重启linux，确保配置生效==

## 7.安装docker（都执行）

1）卸载已经安装的docker

```shell
sudo yum remove docker*
```

![image-20240417194149540](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025501.png)

2）安装`yum-utils`工具包

```shell
sudo yum install -y yum-utils
```

![image-20240417194207848](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025635.png)

3）设置阿里云提供的镜像仓库

```shell
#配置docker yum 源
sudo yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
```

![image-20240417194224068](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025177.png)

4）安装指定版本docker

```shell
#安装docker 19.03.9   docker-ce  19.03.9
yum install -y docker-ce-3:19.03.9-3.el7.x86_64  docker-ce-cli-3:19.03.9-3.el7.x86_64 containerd.io
```

![image-20240417194310615](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025284.png)

5）启动docker并设置开机自启

```shell
#启动服务
systemctl start docker
systemctl enable docker
```

![image-20240417194333837](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025524.png)

6）配置阿里云镜像加速器

```shell
#1创建目录
sudo mkdir -p /etc/docker
#2配置加速
tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://vpmkvcwz.mirror.aliyuncs.com"]
}
EOF
```

7）重启docker

```shell
sudo systemctl daemon-reload
sudo systemctl restart docker
```

![image-20240417194420549](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025393.png)

# 安装k8s核心kubeadm/kubelet

## 添加 yum 源（都执行）

将 Kubernetes 的 YUM 仓库添加到 CentOS 7 的 YUM 仓库列表中，以便在 CentOS 7 上可以使用 YUM 命令安装 Kubernetes 相关软件包。

其中，baseurl 指定了阿里云提供的 Kubernetes 仓库地址，gpgcheck 为 0 表示不进行 GPG 检查，可以省略 GPG 密钥的导入。

```shell
# 配置K8S的yum源
cat <<EOF > /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=http://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64
enabled=1
gpgcheck=0
repo_gpgcheck=0
gpgkey=http://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg
       http://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
EOF
```

![image-20240417194620655](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025218.png)

## 卸载旧版本k8s（都执行）

```shell
# 卸载旧版本
yum remove -y kubelet kubeadm kubectl
```

![image-20240417194635569](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025228.png)

## 安装kubelet、kubeadm、kubectl （都执行）

通常情况下，Kubeadm，Kubelet和Kubectl版本需要和Kubernetes Master和Node节点上的Kubernetes版本保持一致。为了避免版本兼容问题，我们建议在安装Kubernetes之前，先查看官方文档中所列的版本兼容关系，并根据官方推荐的Docker版本进行设置。

```shell
# 安装kubelet、kubeadm、kubectl 指定版本
yum install -y kubelet-1.21.0 kubeadm-1.21.0 kubectl-1.21.0
```

![image-20240417194655852](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025090.png)

## 设置开机启动（都执行）

```shell
systemctl enable kubelet && systemctl start kubelet
```

![image-20240417194731372](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025614.png)

# 部署 Kubernetes Master

==接下来只在k8s-master执行==

## 创建k8s集群

kubeadm config images list：查看需要哪些镜像

![image-20240417194805732](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025508.png)

先下载镜像

```shell
vim images.sh



####封装成images.sh文件
#!/bin/bash
images=(
  kube-apiserver:v1.21.0
  kube-proxy:v1.21.0
  kube-controller-manager:v1.21.0
  kube-scheduler:v1.21.0
  coredns:v1.8.0
  etcd:3.4.13-0
  pause:3.4.1
)
for imageName in ${images[@]} ; do
    docker pull registry.cn-hangzhou.aliyuncs.com/dongguo/$imageName
done
```



```shell
#####封装结束

chmod +x images.sh && ./images.sh
```

下载卡住可以重启docker后再次尝试

![image-20240417211104328](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025412.png)

查看镜像

![image-20240417211120868](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025173.png)

注意1.21.0版本的k8s coredns镜像比较特殊，结合阿里云需要特殊处理，重新打标签

```
docker tag registry.cn-hangzhou.aliyuncs.com/dongguo/coredns:v1.8.0 registry.cn-hangzhou.aliyuncs.com/dongguo/coredns/coredns:v1.8.0
```

![image-20240417211342043](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025723.png)

创建一个Kubernetes集群，并配置网络相关的参数，会拉取以上的镜像。

```shell
kubeadm init \
--apiserver-advertise-address=192.168.122.140 \
--image-repository registry.cn-hangzhou.aliyuncs.com/dongguo \
--kubernetes-version v1.21.0 \
--service-cidr=10.96.0.0/16 \
--pod-network-cidr=10.244.0.0/16
```

由于默认拉取镜像地址 k8s.gcr.io 国内无法访问， 这里指定阿里云镜像仓库地址。

- --apiserver-advertise-address：指定Kubernetes API服务器的地址，这里是192.168.122.140。
- --image-repository：指定要使用的容器镜像仓库，这里是阿里云的镜像仓库。
- --kubernetes-version：指定要安装的Kubernetes版本号，这里是v1.21.0。
- --service-cidr：指定负载均衡服务的IP地址段，这里是10.96.0.0/16。
- --pod-network-cidr：指定Pod网络的IP地址段，这里是10.244.0.0/16。

指定一个网络可达范围  pod的子网范围+service负载均衡网络的子网范围+本机ip的子网范围不能有重复域



==问题1：内存或处理器达不到最低配置会报错。==

![image-20240418094514564](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025401.png)

修改为2G内存，2个处理器

![image-20240418094618864](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191026167.png)

## master初始化的日志：

```shell
[root@k8s-master ~]# kubeadm init \
> --apiserver-advertise-address=192.168.122.140 \
> --image-repository registry.cn-hangzhou.aliyuncs.com/dongguo \
> --kubernetes-version v1.21.0 \
> --service-cidr=10.96.0.0/16 \
> --pod-network-cidr=10.244.0.0/16
[init] Using Kubernetes version: v1.21.0
[preflight] Running pre-flight checks
	[WARNING IsDockerSystemdCheck]: detected "cgroupfs" as the Docker cgroup driver. The recommended driver is "systemd". Please follow the guide at https://kubernetes.io/docs/setup/cri/
[preflight] Pulling images required for setting up a Kubernetes cluster
[preflight] This might take a minute or two, depending on the speed of your internet connection
[preflight] You can also perform this action in beforehand using 'kubeadm config images pull'
[certs] Using certificateDir folder "/etc/kubernetes/pki"
[certs] Generating "ca" certificate and key
[certs] Generating "apiserver" certificate and key
[certs] apiserver serving cert is signed for DNS names [k8s-master kubernetes kubernetes.default kubernetes.default.svc kubernetes.default.svc.cluster.local] and IPs [10.96.0.1 192.168.122.140]
[certs] Generating "apiserver-kubelet-client" certificate and key
[certs] Generating "front-proxy-ca" certificate and key
[certs] Generating "front-proxy-client" certificate and key
[certs] Generating "etcd/ca" certificate and key
[certs] Generating "etcd/server" certificate and key
[certs] etcd/server serving cert is signed for DNS names [k8s-master localhost] and IPs [192.168.122.140 127.0.0.1 ::1]
[certs] Generating "etcd/peer" certificate and key
[certs] etcd/peer serving cert is signed for DNS names [k8s-master localhost] and IPs [192.168.122.140 127.0.0.1 ::1]
[certs] Generating "etcd/healthcheck-client" certificate and key
[certs] Generating "apiserver-etcd-client" certificate and key
[certs] Generating "sa" key and public key
[kubeconfig] Using kubeconfig folder "/etc/kubernetes"
[kubeconfig] Writing "admin.conf" kubeconfig file
[kubeconfig] Writing "kubelet.conf" kubeconfig file
[kubeconfig] Writing "controller-manager.conf" kubeconfig file
[kubeconfig] Writing "scheduler.conf" kubeconfig file
[kubelet-start] Writing kubelet environment file with flags to file "/var/lib/kubelet/kubeadm-flags.env"
[kubelet-start] Writing kubelet configuration to file "/var/lib/kubelet/config.yaml"
[kubelet-start] Starting the kubelet
[control-plane] Using manifest folder "/etc/kubernetes/manifests"
[control-plane] Creating static Pod manifest for "kube-apiserver"
[control-plane] Creating static Pod manifest for "kube-controller-manager"
[control-plane] Creating static Pod manifest for "kube-scheduler"
[etcd] Creating static Pod manifest for local etcd in "/etc/kubernetes/manifests"
[wait-control-plane] Waiting for the kubelet to boot up the control plane as static Pods from directory "/etc/kubernetes/manifests". This can take up to 4m0s
[kubelet-check] Initial timeout of 40s passed.
[apiclient] All control plane components are healthy after 56.503929 seconds
[upload-config] Storing the configuration used in ConfigMap "kubeadm-config" in the "kube-system" Namespace
[kubelet] Creating a ConfigMap "kubelet-config-1.21" in namespace kube-system with the configuration for the kubelets in the cluster
[upload-certs] Skipping phase. Please see --upload-certs
[mark-control-plane] Marking the node k8s-master as control-plane by adding the labels: [node-role.kubernetes.io/master(deprecated) node-role.kubernetes.io/control-plane node.kubernetes.io/exclude-from-external-load-balancers]
[mark-control-plane] Marking the node k8s-master as control-plane by adding the taints [node-role.kubernetes.io/master:NoSchedule]
[bootstrap-token] Using token: km67i8.k783mnpymvzvqts5
[bootstrap-token] Configuring bootstrap tokens, cluster-info ConfigMap, RBAC Roles
[bootstrap-token] configured RBAC rules to allow Node Bootstrap tokens to get nodes
[bootstrap-token] configured RBAC rules to allow Node Bootstrap tokens to post CSRs in order for nodes to get long term certificate credentials
[bootstrap-token] configured RBAC rules to allow the csrapprover controller automatically approve CSRs from a Node Bootstrap Token
[bootstrap-token] configured RBAC rules to allow certificate rotation for all node client certificates in the cluster
[bootstrap-token] Creating the "cluster-info" ConfigMap in the "kube-public" namespace
[kubelet-finalize] Updating "/etc/kubernetes/kubelet.conf" to point to a rotatable kubelet client certificate and key
[addons] Applied essential addon: CoreDNS
[addons] Applied essential addon: kube-proxy

Your Kubernetes control-plane has initialized successfully!

To start using your cluster, you need to run the following as a regular user:

  mkdir -p $HOME/.kube
  sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
  sudo chown $(id -u):$(id -g) $HOME/.kube/config

Alternatively, if you are the root user, you can run:

  export KUBECONFIG=/etc/kubernetes/admin.conf

You should now deploy a pod network to the cluster.
Run "kubectl apply -f [podnetwork].yaml" with one of the options listed at:
  https://kubernetes.io/docs/concepts/cluster-administration/addons/

Then you can join any number of worker nodes by running the following on each as root:

kubeadm join 192.168.122.140:6443 --token km67i8.k783mnpymvzvqts5 \
	--discovery-token-ca-cert-hash sha256:ce92c0fb256bda93a57b92eb72c1aeeed015378ac122fba5a4e1265068164cf9
```



Your Kubernetes control-plane has initialized successfully! Kubernetes集群部署成功。

日志中提示

### 1）普通用户使用 kubectl 命令来管理 Kubernetes 集群，而不再需要使用 root 权限。

```shell
To start using your cluster, you need to run the following as a regular user:

  mkdir -p $HOME/.kube
  sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
  sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

mkdir -p $HOME/.kube创建一个名为 .kube 的目录，该目录用于存储 Kubernetes 集群配置文件。

sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config：将 Kubernetes 集群管理员配置文件复制到 $HOME/.kube/config 文件中。这个文件是 Kubernetes 命令行工具 kubectl 使用的配置文件。

sudo chown $(id -u):$(id -g) $HOME/.kube/config：将 $HOME/.kube/config 文件的所有权分配给当前用户和用户组，以确保用户可以修改该文件。

![image-20240417212137653](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191026798.png)



### 2）使用 `kubectl` 命令时就会自动查找并使用该配置文件

即使是 `root` 用户，也可以直接使用 `kubectl` 命令来管理 Kubernetes 集群。

暴露环境变量

```shell
Alternatively, if you are the root user, you can run:

  export KUBECONFIG=/etc/kubernetes/admin.conf
```




![image-20240417212829977](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191026046.png)



### 3）部署pod网络

```shell
You should now deploy a pod network to the cluster.
Run "kubectl apply -f [podnetwork].yaml" with one of the options listed at:
  https://kubernetes.io/docs/concepts/cluster-administration/addons/
```

常用的pod网络插件：

- Flannel: 使用 Linux 内核中的 VXLAN 模块为每个 Pod 创建一个虚拟网络。
- Calico: 可以提供高级网络策略和安全性。
- Weave Net: 轻量级网络插件，可以轻松管理网络。
- Cilium: 支持多种网络通信模型和高级网络策略。

这里选择Calico，k8s官方推荐的网络插件，部署Calico网络插件能让pod网络直接通信，不需要经过转发

![image-20240417190643991](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191026661.png)

这个命令会应用一个YAML文件，该文件包含了Calico网络插件的部署和配置。该插件可以用于在Kubernetes集群中创建 overlay 网络，，它可以在不同的计算机、容器或虚拟机之间创建一个逻辑网络，使它们可以像在同一个物理网络中一样相互通信。这使得不同节点上的Pod可以通过内部IP地址相互通信。使用这个命令来部署Calico，你需要确保你已经安装了kubectl和一个运行着Kubernetes的集群。

```shell
kubectl apply -f https://docs.projectcalico.org/manifests/calico.yaml
```

![image-20240417212855228](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191026494.png)



获取集群中所有命名空间的 Pod 列表，即当前所有部署好的应用

```shell
kubectl get pod -A
```

 `-A` 参数表示获取所有命名空间的资源。

![image-20240418142739159](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191026262.png)

等待所有状态都为running表示正常了，大约需要几分钟的时间



## 获取当前Kubernetes集群中的所有节点信息

它将返回每个节点的名称、状态、IP地址和版本信息等。

```
kubectl get nodes
```

![image-20240418142759788](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191026597.png)

Kubernetes集群节点状态为Ready





如果出现问题可以**重置 Kubernetes 集群**重新初始化

```shell
#重置 Kubernetes 集群
kubeadm reset 
#清理节点上的一些残留文件和目录
sudo rm -rf /etc/kubernetes
sudo rm -rf /var/lib/etcd
#集群初始化
kubeadm init [options]
```



# 加入 Kubernetes Node

## 向集群添加新节点

向集群添加新节点， 执行 kubeadm join 命令：

在Master 执行kubeadm init命令行最后部分已经给出该命令需要的token，复制即可：

1） 将192.168.122.141（ Node）节点、192.168.122.144（ Node）节点加入到 Kubernetes 集群中

```shell
kubeadm join 192.168.122.140:6443 --token km67i8.k783mnpymvzvqts5 \
	--discovery-token-ca-cert-hash sha256:ce92c0fb256bda93a57b92eb72c1aeeed015378ac122fba5a4e1265068164cf9 
```

1. **kubeadm join**: 这是一个 kubeadm 命令，用于将一个节点加入到 Kubernetes 集群中。
2. **192.168.122.140:6443**: 这是主节点的 IP 地址和端口号，是 API 服务器的地址。新的工作节点将通过此地址连接到集群。
3. **--token km67i8.k783mnpymvzvqts5**: 这是一个用于验证工作节点加入请求的令牌。令牌是一个安全凭据，用于验证节点的身份和权限。
4. **--discovery-token-ca-cert-hash sha256:ce92c0fb256bda93a57b92eb72c1aeeed015378ac122fba5a4e1265068164cf9**: 这是用于验证加入请求证书的 CA 证书哈希。在加入集群时，节点需要验证主节点发放的证书的有效性。

k8s-node1:

![image-20240418144800588](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191026083.png)

k8s-node2:

![image-20240418143435556](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191026861.png)

## 获取当前Kubernetes集群中的所有节点

```shell
kubectl get pod -A
```

![image-20240418143704713](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191026204.png)

```shell
kubectl get nodes
```

![image-20240418143537862](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191026636.png)

再次查看节点  两个worker node节点已经加入集群，状态是Ready

## 令牌有效期问题

对于 `kubeadm init` 创建的令牌，默认情况下是有过期时间的，有效期为 24 小时。

因此，`kubeadm join` 命令使用的令牌会在 24 小时后过期。

![image-20240418150232092](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191026390.png)

**当kubeadm join生成的令牌过期后，之前使用过期令牌成功join到集群的节点仍然是有效的**。这些节点已经成功加入了集群，并可以正常参与集群的工作。

但是新的节点拿着过期的令牌想要join到集群中就不可以了

### 那如何解决这个问题呢？

1. **重新生成令牌**：当令牌过期后，可以在master节点上运行`kubeadm token create --print-join-command`命令来生成一个新的令牌。
2. 这个命令会提供一个包含新令牌和CA证书散列的完整`kubeadm join`命令，这样就可以直接在新节点上运行这个命令来加入集群。

```shell
[root@k8s-master ~]# kubeadm token create --print-join-command
kubeadm join 192.168.122.140:6443 --token 72m4y0.0gskrlje6p0u3izo --discovery-token-ca-cert-hash sha256:ce92c0fb256bda93a57b92eb72c1aeeed015378ac122fba5a4e1265068164cf9 
```

![image-20240418150627085](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191027999.png)

新的节点使用新生成的令牌加入集群中。可以看到存在多个令牌时，这些令牌只要在有效期内都是可以使用的。



# 验证集群

## 给 Kubernetes 节点添加标签

我们看到k8s集群中 k8s-master同时具有控制平面和主节点的角色，而k8s-node1、k8s-node1默认是没有设置角色标签的。

![image-20240418152427574](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191026041.png)

为了表示节点的职责，使用kubectl label命令为这两个工作节点添加标签

```shell
kubectl label node k8s-node1 node-role.kubernetes.io/worker=''
kubectl label node k8s-node2 node-role.kubernetes.io/worker=''
```

想要删除标签，如：

```shell
kubectl label node k8s-node1 node-role.kubernetes.io/worker1-
```

![image-20240418154348287](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191026832.png)

## kube-proxy设置ipvs模式

在 Kubernetes 中，kube-proxy 是一个网络代理，负责处理集群内部的网络流量，并帮助路由流量到正确的目的地。kube-proxy 可以以不同的模式运行，其中两种常见的模式是 iptables 模式和 IPVS（IP Virtual Server）模式。这两种模式之间有一些区别，

具体如下：

1. **iptables 模式**：
   - 在 iptables 模式下，kube-proxy 使用 Linux 的 iptables 工具来实现流量转发和负载均衡。
   - kube-proxy 为每个 Service 创建一个 iptables 规则，该规则将流量重定向到后端 Pod。这些规则存储在每个节点上的 iptables 规则表中。
   - 由于 iptables 规则是在每个节点上独立处理的，因此该模式对于小规模的集群和较少数量的 Service 是非常适用的。
2. **IPVS 模式**：
   - IPVS 是 Linux 内核提供的一种负载均衡机制，相比 iptables，它提供了更高效的负载均衡性能和更低的系统资源消耗。
   - 在 IPVS 模式下，kube-proxy 使用 IPVS 工具来实现流量转发和负载均衡。
   - kube-proxy 为每个 Service 创建一个 IPVS 虚拟服务器，它们被集中存储在一个专用的内核数据结构中，而不是存储在每个节点的 iptables 规则表中。
   - IPVS 提供了更灵活的负载均衡算法和会话保持功能，以及更高的性能和可伸缩性。



对于小规模集群或资源受限的情况，iptables 模式可能更简单和实用。

对于大规模集群或需要更高性能的场景，IPVS 模式通常更适合。

### 查看默认kube-proxy 使用的模式

```
kubectl logs -n kube-system NAME
```

![image-20240418160903595](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191026505.png)

图中的三个kube-proxy分别对应不同的节点

![image-20240418161347906](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191026909.png)

### 修改 kube-proxy 的配置文件

```shell
kubectl edit cm kube-proxy -n kube-system
```

![image-20240418162322772](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191026091.png)

修改mode 为ipvs

![image-20240418162305637](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191026837.png)





### 重启kube-proxy生效

在修改 `kube-proxy` 的配置后，Kubernetes 并不会自动地重新加载或生效新的配置。需要手动触发 `kube-proxy` 的重新启动或重新加载配置，以使新的配置生效。

### 删除三个节点中的kube-proxy

使用 `kubectl delete pod` 命令删除 `kube-proxy` Pod，这将触发 Kubernetes 控制平面自动创建一个新的 Pod，并且新的 Pod 将使用新的 配置。

```
kubectl delete pod kube-proxy-29lfq  -n kube-system
kubectl delete pod kube-proxy-2jh4r  -n kube-system
kubectl delete pod kube-proxy-v5pb6  -n kube-system
```

![image-20240418162710341](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191026669.png)

当删除 `kube-proxy` Pod 后，Kubernetes 控制平面会注意到该 Pod 的缺失，并根据 Pod 的副本集规则尝试重新创建它，以确保集群的网络功能不受影响。

![image-20240418163654259](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191027526.png)

