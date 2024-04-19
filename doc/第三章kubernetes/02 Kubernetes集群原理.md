# 集群原理

## master-node 架构

master：主节点

node：work节点

![image-20240417154251588](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191024227.png)

> master 和 worker怎么交互
>
> master决定worker里面都有什么
>
> worker只是和master （API） 通信；  每一个节点自己干自己的活

程序员使用UI或者CLI操作k8s集群的master，就可以知道整个集群的状况

## 工作原理

![image-20240417154356540](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191024459.png)

master节点（Control Plane【控制面板】）：master节点控制整个集群

master节点上有一些核心组件：

- Controller  Manager：控制管理器
- etcd：键值数据库（redis）【记账本，记事本】
- scheduler：调度器
- api server：api网关（所有的控制都需要通过api-server）

node节点（worker工作节点）：

- kubelet（监工）：每一个node节点上必须安装的组件。
- kube-proxy：代理。代理网络







## 部署一个应用流程？

程序员：调用CLI告诉master，我们现在要部署一个tomcat应用

- 程序员的所有调用都先去master节点的网关api-server。这是matser的唯一入口（mvc模式中的c层）
- 收到的请求先交给master的api-server。由api-server交给controller-mannager进行控制
- controller-mannager 进行 应用部署
- controller-mannager 会生成一次部署信息。 tomcat --image:tomcat6 --port 8080 ,真正不部署应用
- 部署信息被记录在etcd中
- scheduler调度器从etcd数据库中，拿到要部署的应用，开始调度。看哪个节点合适，
- scheduler把算出来的调度信息再放到etcd中
- 每一个node节点的监控kubelet，随时和master保持联系的（给api-server发送请求不断获取最新数据），所有节点的kubelet就会从master
- 假设node2的kubelet最终收到了命令，要部署。
- kubelet就自己run一个应用在当前机器上，随时给master汇报当前应用的状态信息，分配ip
- node和master是通过master的api-server联系的
- 每一个机器上的kube-proxy能知道集群的所有网络。只要node访问别人或者别人访问node，node上的kube-proxy网络代理自动计算进行流量转发

无论访问哪个机器，都可以访问到真正应用（Service【服务】）【通过api-server】

# 原理分解

![image-20240417153957960](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191024363.png)





## 主节点（master）

在 Kubernetes 架构中，Master node 是集群的控制中心。它负责管理整个集群的状态和资源分配，包括：

1. API Server：是集群的核心组件，负责接收和处理来自其他组件的请求，以及维护整个集群的状态信息。
2. Etcd：一个高可用的分布式键值存储系统，用于存储集群的所有配置信息和状态信息。
3. Controller Manager：管理集群中的各种控制器，比如Replication Controller、Endpoint Controller等，在集群中维护期望状态并对实际状态进行调整来保持它们一致。
4. Scheduler：负责将Pod调度到集群中的节点上，并根据节点上的资源使用情况和Pod的需求来做出最优的调度决策。

Master node 一般不承载应用程序容器，其主要作用是负责集群的管理和控制，并且为 worker node 提供 API Server 和其他必要的组件，是整个 Kubernetes 集群的大脑。

Master节点是Kubernetes集群中最重要的组件之一，必须保证其高可用性，因为如果Master节点宕机，整个集群都将失去管理和协调的能力。因此，在生产环境中，通常会将多个Master节点部署在不同的服务器上，以确保高可用性。

![image-20240417161825257](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191024989.png)

- master也要装kubelet和kubeproxy
- 前端访问（UI\CLI）：

**控制平面组件（Control Plane Components）**

控制平面组件会为集群做出全局决策，比如资源的调度。 以及检测和响应集群事件，例如当不满足部署的 replicas 字段时， 要启动新的 pod）。

### **kubectl**

kubectl是Kubernetes命令行工具，用于与Kubernetes集群进行交互。它允许开发人员和系统管理员管理Kubernetes对象，如Pod、Deployment、Service等。还可以查看集群状态、日志和排查故障。

kubectl的工作原理是将命令行参数解析和API请求封装在一个命令行工具中。它通过调用Kubernetes API服务器来执行请求，并从结果中提取信息并显示给用户。

kubectl的主要功能包括创建、更新和删除Kubernetes对象、查看资源、管理应用程序等。它还提供了一些有用的功能，如日志跟踪、端口转发、执行命令等。

### **api server**

提供 Kubernetes 集群的 API 接口。它作为 Kubernetes 集群中的控制中心，负责各组件之间的协调和管理，包括维护集群状态、接收和处理用户请求、授权和认证等。

Api server 接收客户端的 RESTful 请求，并将其转换为相应的内部操作，然后通过其他核心组件（如 etcd、kube-scheduler、kube-controller-manager、kubelet 等）完成对 Kubernetes 集群的管理和控制。在请求处理期间，api server 还会执行安全检查、身份验证和授权等任务，以确保用户请求的安全性和正确性。



### **scheduler**

负责将新建的Pod调度到节点上运行。它通过监控未分配的Pod，并基于节点的可用资源和调度策略来选择合适的节点进行调度。

Scheduler通过调用Kubernetes API来获取未分配的Pod，并使用调度策略对它们进行评估，以决定应该将它们分配到哪个节点上。

Kubernetes中的scheduler支持多种调度算法，包括最佳节点选择算法、负载均衡算法等。它可以根据不同的Pod的需求进行调度，调度决策考虑的因素包括单个 Pod 及 Pods 集合的资源需求、软硬件及策略约束、 亲和性及反亲和性规范、数据位置、工作负载间的干扰及最后时限。同时，scheduler还会监测节点的状态，如节点的可用资源情况、节点的连接状态等，以保证节点的可用性。

一旦确定了最佳节点，Scheduler会将Pod的调度信息写入Kubernetes API服务器，以便kubelet能够获取并启动相关的容器。如果没有合适的节点可用，则Pod将保持未调度状态，直到有合适的节点可用为止。

### **controller manager**

Kubernetes的Controller Manager是一个独立的进程，它负责管理集群中的控制器，以确保集群中的资源状态与期望状态保持一致。控制器管理器由多个控制器组成，每个控制器都是一个独立的进程， 但是为了降低复杂性，它们都被编译到同一个可执行文件，并在同一个进程中运行。它们负责监视和调节集群中资源的状态。

Controller manager 负责监控 Kubernetes 集群中的资源对象，例如 Pods、Services、ReplicationControllers、Endpoints 等，并根据定义好的规则进行控制和管理。它可以执行多种控制器，例如 ReplicaSet、Deployment、StatefulSet、DaemonSet 等，确保它们的行为符合用户定义的期望状态。

控制器管理器中的控制器例如：

1. Replication Controller（RC）控制器：它用于确保Pod的副本数始终符合定义。
2. Node Controller节点控制器：当节点不可用时，它会检测并采取相应措施，如重新分配Pods。
3. Service Controller服务控制器：它将Service Pod与Endpoint的关联保持同步，确保Pod可以被正确路由到。
4. Namespace Controller控制器：它负责确保任何新创建的对象都被分配到正确的命名空间中。
5. PersistentVolume Controller控制器：它监视PersistentVolume的状态，并确保Pod可以正确地访问它们。
6. Job Controller任务控制器：用于管理集群中的任务，例如批处理作业或单次作业。

### **etcd**

一个分布式的键值存储系统，用于存储 Kubernetes 集群的状态和元数据。etcd 中存储的数据包括：

1. Kubernetes 集群的配置信息（如 API server 的地址、容器运行时的地址等）；
2. Kubernetes 对象的定义（如 Pod、Service、ReplicaSet 等）；
3. Kubernetes 对象的状态（如 Pod 的状态、Service 的 IP 地址等）；
4. 一些运行时状态信息（如节点的状态、容器的状态等）。

etcd 的分布式存储模式保证了数据的高可用和一致性，作为 Kubernetes 所有集群数据的后台数据库。。当集群中的一个节点故障时，etcd 会自动将该节点上的数据同步到其他节点上，保证数据不会丢失。同时，etcd 使用 Raft 算法保证数据一致性，即使在网络分区或节点故障的情况下也能保证数据的一致性。

- kubelet+kubeproxy每一个节点的必备+docker（容器运行时环境）

## 工作节点（node）

Worker 节点是 Kubernetes 集群中的工作节点，它承担着容器的运行和管理任务。

Worker node通过Kubernetes节点代理与集群通信，并执行由Kubernetes master指定的Pod的启动、停止和重启操作。每个worker node都运行一个容器运行时环境，比如Docker，以便能够运行Pod中的容器。

Worker 节点包括 Docker 或其他容器运行时、kubelet（负责管理节点上的容器）、kube-proxy（负责服务发现和负载均衡）、Pod 网络接口和一些其他的插件（如 CNI 插件）等组件，这些组件协同工作，通过与 Master 节点交互来确保容器的高可用性和可靠性。Worker 节点的数量可以根据业务负载的大小进行扩展。

![image-20240417161907870](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025001.png)

###  **pod**

Pod 是最小的可部署单元。一个 Pod 包含一个或多个紧密关联的容器，这些容器共享网络和存储资源，并被放置在同一个节点上。Pod 也包含一组配置控制器，用于描述容器如何运行，并可与其他 Pod 通信。每个 Pod 在其生命周期中都有一个唯一的 IP 地址，并且可以被其他容器轻松访问。Pod 是可替换的，当需要更新或重启 Pod 时，可以轻松地进行替换。在 Kubernetes 中，Pod 通常是水平扩展应用程序的主要方式，以满足高负载的要求。

- Pod：

  - docker run 启动的是一个container（容器），**容器是docker的基本单位**，一个应用是一个容器
  - kubelet run 启动的一个应用称为一个Pod；**Pod是k8s的基本单位。**
    - Pod是容器的一个再封装
    - 一个容器往往代表不了一个基本应用。博客（php+mysql合起来完成）
    - 准备一个Pod 可以包含多个 container；一个Pod代表一个基本的应用。

  

### **kubelet**

监工，负责交互master的api-server以及当前机器的应用启停等，在master机器就是master的小助手。每一台机器真正干活的都是这个 Kubelet

负责管理和维护节点上的容器化应用程序。Kubelet运行在每个节点上，与master节点通信，管理节点上的容器和它们的生命周期，确保他们按照规定运行。

Kubelet的主要职责包括：

1. 与API服务器通信，接收来自于Scheduler的Pod调度信息，并创建该节点上的Pod。
2. 与Docker等容器运行时进行交互，确保Pod中的容器正确启动、运行、停止和删除。
3. 持续监视节点上的容器和Pod，确保它们处于健康状态，如果出现故障，则进行重启或清理。
4. 与CRI（Container Runtime Interface）进行交互，以支持不同的容器运行时，比如Docker、rkt和CRI-O等。

### **kube-proxy**

kube-proxy 是一个网络代理，它负责将 Kubernetes 集群内部的网络流量转发到正确的目的地。kube-proxy 运行在每个节点上，对外暴露一个虚拟IP地址，在Kubernetes中被称为"Service IP"。当有新的服务被创建时，kube-proxy会监视服务的变化，并动态调整负载均衡规则，确保流量能够被正确地路由到正确的Pod上。

Kube-proxy的具体实现方式有多种，包括iptables、IPVS和userspace等方式。其中，iptables是最常使用的一种方式，它可以利用Linux内核提供的网络功能，实现基础的负载均衡和端口转发。但随着集群规模的增大，iptables的性能和可扩展性会越来越受限。因此，IPVS成为了一种更为高效的替代方案，它使用Linux内核的高级网络功能，可以实现更加复杂的负载均衡需求，并支持动态调整规则。而userspace的方式则是一种较为灵活的解决方案，可以通过自定义程序实现各种负载均衡算法和协议。

### **docker** 

Kubernetes 利用 Docker 来运行和管理容器化的应用程序。在 Kubernetes 架构中，Docker 扮演着一个重要的角色，它负责创建和运行应用程序的容器，并与 Kubernetes 运行时环境交互。

具体来说，Kubernetes 使用 Docker 来实现以下功能：

1. 容器化应用程序：Kubernetes 允许用户将应用程序打包成 Docker 容器，并将它们发布到一个容器仓库中，方便部署和管理。
2. 管理容器：Kubernetes 可以通过 Docker 镜像来创建和管理容器，并确保容器的健康状态。它可以监控容器运行状态并重启故障容器，也可以调整容器的资源限制和分配。
3. 负载均衡和服务发现：Kubernetes 可以通过 Docker 容器来实现负载均衡和服务发现。它可以自动将容器分配到可用节点上，并提供一组负载均衡规则来确保应用程序的高可用性和可伸缩性。
4. 网络管理：Kubernetes 可以使用 Docker 提供的网络服务来管理容器网络。它可以创建和管理容器间的网络，并允许容器之间进行通信。

# Master、Node组件交互原理

![image-20240417163756649](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191025494.png)

1、程序员使用命令行工具： kubectl ； kubectl create deploy tomcat --image=tomcat8（告诉master让集群使用tomcat8镜像，部署一个tomcat应用）

2、kubectl命令行内容发给api-server，api-server保存此次创建信息到etcd

3、etcd给api-server上报事件，说刚才有人给我里面保存一个信息。（部署Tomcat[deploy]）

4、controller-manager监听到api-server的事件，是 （部署Tomcat[deploy]）

5、controller-manager 处理这个 （部署Tomcat[deploy]）的事件。controller-manager会生成Pod的部署信息【pod信息】

6、controller-manager 把Pod的信息交给api-server，再保存到etcd

7、etcd上报事件【pod信息】给api-server。

8、scheduler专门监听 【pod信息】 ，拿到 【pod信息】的内容，计算，看哪个节点合适部署这个Pod【pod调度过后的信息（node: node-02）】，

9、scheduler把 【pod调度过后的信息（node: node-02）】交给api-server保存给etcd

10、etcd上报事件【pod调度过后的信息（node: node-02）】，给api-server

11、其他节点的kubelet专门监听 【pod调度过后的信息（node: node-02）】 事件，集群所有节点kubelet从api-server就拿到了 【pod调度过后的信息（node: node-02）】 事件

12、每个节点的kubelet判断是否属于自己的事情；node-02的kubelet发现是他的事情

13、node-02的kubelet启动这个pod。汇报给master当前启动好的所有信息