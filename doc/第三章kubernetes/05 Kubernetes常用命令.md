创建 Deployment 后，k8s创建了一个 **Pod（容器组）** 来放置应用程序实例（container 容器）。

![image-20240419102803869](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959382.png)

# 了解Pod

**Pod （容器组）** 是一个k8s中一个抽象的概念，用于存放一组 container（可包含一个或多个 container 容器，即图上正方体)，以及这些 container （容器）的一些共享资源。这些资源包括：

- 共享存储，称为卷(Volumes)，即图上紫色圆柱
- 网络，每个 Pod（容器组）在集群中有个唯一的 IP，pod（容器组）中的 container（容器）共享该IP地址
- container（容器）的基本信息，例如容器的镜像版本，对外暴露的端口等



**Pod（容器组）是 k8s 集群上的最基本的单元**。当我们在 k8s 上创建 Deployment 时，会在**集群上创建包含容器的 Pod (而不是直接创建容器)**。每个Pod都与运行它的 worker 节点（Node）绑定，并保持在那里直到终止或被删除。如果节点（Node）发生故障，则会在群集中的其他可用节点（Node）上运行相同的 Pod（从同样的镜像创建 Container，使用同样的配置，IP 地址不同，Pod 名字不同）。



> TIP
>
> 重要：
>
> - Pod 是一组容器（可包含一个或多个应用程序容器），以及共享存储（卷 Volumes）、IP 地址和有关如何运行容器的信息。
> - 如果多个容器紧密耦合并且需要共享磁盘等资源，则他们应该被部署在同一个Pod（容器组）中。

# 了解Node

Pod（容器组）总是在 **Node（节点）** 上运行。Node（节点）是 kubernetes 集群中的计算机，可以是虚拟机或物理机。每个 Node（节点）都由 master 管理。一个 Node（节点）可以有多个Pod（容器组），kubernetes master 会根据每个 Node（节点）上可用资源的情况，自动调度 Pod（容器组）到最佳的 Node（节点）上。

每个 Kubernetes Node（节点）至少运行：

- Kubelet，负责 master 节点和 worker 节点之间通信的进程；管理 Pod（容器组）和 Pod（容器组）内运行的 Container（容器）。
- kube-proxy，负责进行流量转发
- 容器运行环境（如Docker）负责下载镜像、创建和运行容器等。

![image-20240419102909831](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959134.png)

# 常用命令

## **kubectl get** - 显示资源列表

### kubectl get deployments获取类型为Deployment的资源列表

![image-20240419104636522](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959385.png)



### kubectl get pods获取类型为Pod的资源列表

![image-20240419104657822](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959356.png)



### kubectl get nodes获取类型为Node的资源列表

![image-20240419104711375](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959734.png)



### kubectl get deployments -A查看所有名称空间的 Deployment
![image-20240419104813594](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959562.png)

或者 kubectl get deployments --all-namespaces

![image-20240419104835804](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959229.png)

### kubectl get deployments -n kube-system查看 kube-system 名称空间的 Deployment
![image-20240419104922843](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959637.png)



## kubectl api-resources列出 Kubernetes 中所有的 API 资源类型及其缩写

![image-20240419105240434](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959546.png)

### kubectl api-resources --namespaced=true在名称空间里
![image-20240419105112332](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959672.png)

### kubectl api-resources --namespaced=false不在名称空间里
![image-20240419105204081](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959654.png)

## **kubectl describe** - 显示有关资源的详细信息

### kubectl describe pod my-nginx查看名称为my-nginx的Pod的信息

![image-20240419105420621](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959564.png)

### kubectl describe deployment my-nginx	查看名称为nginx的Deployment的信息

![image-20240419105505432](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959263.png)

## **kubectl logs** - 查看pod中的容器的打印日志（和命令docker logs 类似）

kubectl logs -f nginx-pod-XXXXXXX查看名称为nginx-pod-XXXXXXX的Pod内的容器打印的日志

![image-20240419105637073](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959022.png)

## **kubectl exec** - 在pod中的容器环境内执行命令(和命令docker exec 类似)

kubectl exec -it my-nginx-6b74b79f57-xxxxx -- /bin/bash在名称为my-nginx-xxxxxx的Pod中运行bash

![image-20240419105800943](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959725.png)

## kubectl run创建Pod

kubectl run my-nginx --image=nginx

# 对外暴露应用

我们之前使用kubectl create deployment my-nginx --image=nginx创建部署my-nginx

![image-20240419113313247](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959684.png)

而且在node2节点看到my-nginx的容器已经启动

![image-20240419133916339](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959484.png)

那么现在该如何访问启动的my-nginx 部署呢

## kubectl expose deployment

`kubectl expose deployment` 命令用于将一个 Deployment 暴露为一个 Kubernetes Service。这个命令为 Deployment 创建一个新的 Service，并且为该 Service 分配一个 ClusterIP（集群内部 IP 地址），允许其他应用程序或服务通过该 IP 地址访问 Deployment 中运行的 Pod。

示例用法：

```shell
kubectl expose deployment <deployment-name> --port=<port> --target-port=<target-port> --type=<service-type>
```

- `<deployment-name>`: 要暴露的 Deployment 的名称。
- `--port=<port>`: 暴露的 Service 的端口号。
- `--target-port=<target-port>`: 要映射到的 Pod 的端口号。
- `--type=<service-type>`: Service 的类型，可以是 ClusterIP、NodePort、LoadBalancer 或 ExternalName。

例如，要将名为 `my-nginx` 的 Deployment 暴露为一个 Service，并在端口 80 上监听，可以执行以下命令：

```shell
 kubectl expose deployment my-nginx --port=81 --target-port=80 --type=ClusterIP
```

ClusterIP：在集群内部分配一个 IP 地址

分配的ip:10.96.118.50，这个ip是在kubeadm init初始化集群时定义的--service-cidr=10.96.0.0/16，随机分配给my-service服务在这个网段的一个ip

![image-20240419134923482](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959589.png)

`10.96.118.50` 是一个私有 IP 地址，属于 Kubernetes 集群内部网络的范围。在 Kubernetes 中，这种私有 IP 地址是专门用于集群内部通信的，通常不会被路由器或防火墙允许从集群外部访问。因此，`10.96.118.50` 是一个内部 IP 地址，无法从集群外部直接访问。

在集群内使用curl进行访问

![image-20240419135119623](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959269.png)

--type=ClusterIP暴露的服务只能在集群内部访问，那我怎么可以在浏览器中访问呢？

使用--type=NodePort，这意味着 Service 将在每个节点上公开一个随机端口来公开服务。



```shell
kubectl expose deployment my-nginx --port=81 --target-port=80 --type=NodePort
```

可以看到对外暴露的端口是31129

![image-20240419140709591](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959069.png)



在 Kubernetes 中，如果使用 `kubectl expose deployment` 命令将一个 Deployment 暴露为 Service，并指定了端口号 `31129`，那么 `netstat` 命令可以用来验证是否有进程在监听该端口。

使用netstat -nlpt|grep 31129发现所有节点都在监控这个端口

![image-20240419141612289](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959241.png)

访问地址： http://NodeIP:Port，在任意节点都能够访问成功

![image-20240419141930082](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959083.png)



集群内部依然可以使用私有 IP进行访问

![image-20240419143422963](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959240.png)

# 伸缩应用程序-扩缩容

## 扩容

我们创建了一个 [Deployment ](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/)，然后通过 [服务](https://kubernetes.io/docs/concepts/services-networking/service/)提供访问 Pod 的方式。我们发布的 Deployment 只创建了一个 Pod 来运行我们的应用程序。当流量增加时，我们需要对应用程序进行伸缩操作以满足系统性能需求。

![image-20240419144156328](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191959923.png)

将名为 `my-nginx` 的 Deployment 的副本数量扩展到 3

```shell
kubectl scale --replicas=3  deployment my-nginx
```

![image-20240419144542107](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404192000589.png)

可以看到当前Deployment副本数量变为3，新创建了两个pod

![image-20240419144638358](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404192000732.png)

这三个pod，分配到node1一个，node2两个。

![image-20240419144841086](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404192000379.png)

## 缩容

其实就是设置Deployment副本数量

```shell
kubectl scale --replicas=1  deployment my-nginx
```

将副本数量设置为1，并删除了两个pod，只保留一个pod

![image-20240419145214357](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404192000744.png)

## 负载均衡

如果现在Deployment副本数量为3，在kubectl expose deployment对外暴露端口后，我们可以通过http://NodeIP:Port进行访问，那访问的是哪个pod的资源呢？

![image-20240419172224053](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404192000835.png)

### 为每个pod做标识

修改index.html，作区分

![image-20240419172138145](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404192000453.png)

### 访问测试

![image-20240419172058632](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404192000416.png)

使用浏览器访问时要注意，访问后是存在缓存的，那么默认就直接304 Not Modified返回上次访问的结果，要在每次访问后清除浏览记录

![image-20240419195351136](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404192000725.png)







![image-20240419195406663](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404192000436.png)

![image-20240419195423696](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404192000238.png)

service-api默认的负载均衡是轮询算法

Kube-Proxy在K8s集群中所有Worker节点上都部署有一个，它掌握Service网络的所有信息，知道怎么和Service网络以及Pod网络互通互联。如果要将Kube-Proxy和节点网络打通(从而将某个服务通过Kube-Proxy暴露出去)，只需要让Kube-Proxy在节点上暴露一个监听端口即可。这种通过Kube-Proxy在节点上暴露一个监听端口，将K8s内部服务通过Kube-Proxy暴露出去的方式，术语就叫NodePort(顾名思义，端口暴露在节点上)。下图是通过NodePort暴露服务的简化概念模型。

![在这里插入图片描述](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404192000840.png)

