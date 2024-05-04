# 什么是工作负载（Workloads）

在 Kubernetes 中，工作负载是运行在 Kubernetes 上的一个应用程序。无论你的负载是由单个组件还是由多个一同工作的组件构成， 你都可以在一组 Pod 中运行它。 在 Kubernetes 中，Pod 代表的是集群上处于运行状态的一组 容器的集合。

以下是 Kubernetes 中常见的几种工作负载类型：

1. **Deployment（部署）**：Deployment 是 Kubernetes 中最常见的工作负载类型之一。它用于定义一组副本（Pod 实例）的期望状态，并负责在集群中创建和管理这些副本。Deployment 提供了滚动更新、自动扩展和回滚等功能，使应用程序的部署和更新过程更加灵活和可靠。
2. **StatefulSet（有状态集）**：StatefulSet 用于部署有状态应用程序，如数据库或分布式系统。与 Deployment 不同，StatefulSet 提供了稳定的网络标识符、持久化存储和有序部署等特性，以确保每个实例的唯一标识和持久性。
3. **DaemonSet（守护进程集）**：DaemonSet 用于在集群中的每个节点上运行一个副本的 Pod 实例，通常用于部署系统级别的守护进程或日志收集器等应用程序。
4. **Job 和 CronJob**：Job 用于一次性任务，例如批处理作业或数据处理任务。CronJob 则用于定期执行任务，类似于 Linux 中的 cron 定时任务。

![image-20240423140331928](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242147135.png)

# Pod

## 什么是Pod

**Pod** 是可以在 Kubernetes 中创建和管理的、最小的可部署的计算单元。

Pod 是一组（一个或多个） 容器（docker容器）的集合 （就像在豌豆荚中）；这些容器共享存 储、网络、以及怎样运行这些容器的声明。

我们一般不直接创建Pod，而是创建一些工作负载由他们来创建Pod。Pod对容器有自恢复能力（Pod自动重启失败的容器）。

Kubernetes 集群中的 Pod 主要有两种用法：

- **运行单个容器的 Pod**。"每个 Pod 一个容器"模型是最常见的 Kubernetes 用例； 在这种情况下，可以将 Pod 看作单个容器的包装器，并且 Kubernetes 直接管理 Pod，而不是容器。
- **运行多个协同工作的容器的 Pod**。 Pod 可以封装由紧密耦合且需要共享资源的[多个并置容器](https://kubernetes.io/zh-cn/docs/concepts/workloads/pods/#how-pods-manage-multiple-containers)组成的应用。 这些位于同一位置的容器构成一个内聚单元。在 Kubernetes 中，使用 Sidecar 模式可以通过在 Pod 中定义多个容器来实现。主应用程序容器通常运行应用程序本身，而 Sidecar 容器运行额外的辅助功能。

如图中可以看出，Pod 内有两个容器：

- **Main container**：主容器内运行着一个http服务，它会读取磁盘上需要对外服务的内容。
- **Sidecar container**：sidecar容器内运行着一个git定时同步服务，它会定期从远端同步最新的服务内容到磁盘上。

![image-20240423142928488](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148439.png)

**sidecar的思想核心就是：不侵入主容器的前提下，可以进行服务功能扩展。**

### 用于管理 Pod 的工作负载资源

通常你不需要直接创建 Pod，甚至单实例 Pod。相反，你会使用诸如 [Deployment](https://kubernetes.io/zh-cn/docs/concepts/workloads/controllers/deployment/) 或 [Job](https://kubernetes.io/zh-cn/docs/concepts/workloads/controllers/job/) 这类工作负载资源来创建 Pod。 如果 Pod 需要跟踪状态，可以考虑 [StatefulSet](https://kubernetes.io/zh-cn/docs/concepts/workloads/controllers/statefulset/) 资源。



### Pod 天生为其成员容器提供了两种共享资源：网络和 存储。

1. **网络**：Pod 内的所有容器共享相同的网络命名空间和 IP 地址，它们可以通过 localhost 相互通信，就像在同一台主机上运行的进程一样。这使得容器之间的通信变得简单高效，它们可以通过标准的本地主机网络接口进行通信，无需额外的配置。
2. **存储**：Pod 内的所有容器共享相同的存储卷（Volume），这使得它们能够共享数据和文件系统状态。这种共享存储卷可以用于在容器之间传递数据、共享配置文件、持久化日志等。Kubernetes 提供了多种类型的存储卷，如空目录卷、主机路径卷、持久卷等，可以根据需求选择合适的存储卷类型。

通过这种共享网络和存储的机制，Pod 内的容器可以更加灵活地协同工作，实现各种复杂的应用场景，如多容器协同工作、Sidecar 模式、应用程序和数据库的配对部署等。

### systemctl status可以观测到Pod和容器进程关系

kubelet启动一个Pod，准备N+1个容器，N是Pod声明的应用容器（如nginx），1是 Pause容器。Pause给当前应用容器设置好网络空间各种的。

![image-20240423144348094](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148540.png)

比如my-nginx这个pod在k8s-node2节点

![image-20240423145249347](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148011.png)

在k8s-node2节点查看和my-nginx有关的容器有两个，一个是nginx容器，另外一个就是pause

![image-20240423145203899](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148945.png)

## Pod的多容器协同

创建一个Pod，其中包含了一个运行 nginx 服务器的容器和一个在后台运行的 Alpine 容器。这两个容器都共享一个空目录卷（匿名卷），可以用于在它们之间共享数据。Alpine 容器每过一秒就会将当前时间写入到/app/index.html中

```shell
apiVersion: v1
kind: Pod
metadata:
  name: pod-multi-container
spec:
  volumes:
  - name: common-volume
    emptyDir: {}
  containers:
  - name: nginx-container
    image: nginx
    volumeMounts:
    - name: common-volume
      mountPath: /usr/share/nginx/html
  - name: content-container
    image: alpine
    command: ["/bin/sh", "-c", "while true; do sleep 1; date > /app/index.html; done;"]
    volumeMounts:
    - name: common-volume
      mountPath: /app
```



![image-20240423161424752](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148675.png)

### Pod的多容器协同-如何进入指定容器

由于没有指定要进入的容器，因此 Kubernetes 默认选择了第一个容器作为默认容器。

![image-20240423215838916](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148000.png)

如果你想要进入其他容器，需要使用 `-c` 标志指定容器的名称

![image-20240423215819674](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148174.png)

## Pod生命周期-容器类型

Pod 的生命周期包括以下几个阶段：

1. Pending（挂起）：Pod 已被 Kubernetes 系统接受，但容器尚未创建。这可能是因为某些原因，例如节点资源不足或正在等待其他 Pod 被调度。
2. Running（运行中）：Pod 中的所有容器都已经成功创建并且至少一个容器在运行中。如果有多个容器，那么只要有一个容器处于运行状态，整个 Pod 就被认为是运行中的。
3. Succeeded（成功）：Pod 中的所有容器已成功运行并且已经退出，不再重启。
4. Failed（失败）：Pod 中的所有容器都已经退出，并且至少有一个容器以错误状态退出。
5. Unknown（未知）：无法获取 Pod 的状态。



Kubernetes 中的容器生命周期，通常可以将容器分为三种类型，分别是主容器 (Main Container)、Init 容器和临时容器 (Ephemeral Container)。让我更详细地介绍一下它们：

1. **主容器 (Main Container)**/应用容器:
   - 主容器是 Pod 中的核心组件，负责运行应用程序或服务。
   - 一个 Pod 可以有一个或多个主容器，但通常情况下只有一个。
   - 主容器定义了 Pod 的主要功能和用途，它们的生命周期与 Pod 相关联。
   - 主容器的生命周期包括创建、启动、运行、停止和删除等阶段，与 Pod 生命周期紧密相连。
2. **Init 容器**:
   - Init 容器是在主容器启动之前执行的容器。
   - Init 容器负责在主容器启动之前执行初始化任务，例如预加载数据、设置环境变量、检查依赖项等。
   - 一个 Pod 可以有多个 Init 容器，它们按照定义顺序依次执行，直到全部成功完成后主容器才会启动。
   - Init 容器与主容器共享同一网络命名空间，但不共享存储空间。
3. **临时容器 (Ephemeral Container)**:
   - 临时容器是在运行中的 Pod 中动态创建的容器，通常用于临时调试、诊断或维护目的。
   - 它们不是 Pod 的一部分，而是在需要时作为 Pod 的一部分创建的。
   - 临时容器通常在调试或故障排除期间使用，不会持续存在于系统中。
   - 临时容器可以在 Pod 运行时创建，并与其他容器共享同一网络和存储空间

![image-20240423162041160](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148743.png)

Pod启动，会先依次执行所有初始化容器，有一个失败，则Pod不能启动

接下来启动所有的应用容器（每一个应用容器都必须能一直运行起来），Pod开始正式工作，一个 启动失败就会尝试重启Pod内的这个容器，Pod只要是NotReady，Pod就不对外提供服务了

### 应用容器不应该直接退出

应用容器是 Pod 中的核心组件，负责运行应用程序或服务。应用容器的生命周期与 Pod 相关联，通常会持续运行直到 Pod 终止。因此，应用容器不应该直接退出，除非是在正常运行结束或出现了无法恢复的错误情况下。

```
apiVersion: v1
kind: Pod
metadata:
  name: pod-lifecycle
spec:
  containers:
  - name: nginx-container
    image: nginx
  - name: alpine-container
    image: alpine #alpine容器启动后会直接退出  
```

![image-20240423203153593](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148458.png)

使用watch持续监视pod情况

```shell
watch -n 1 kubectl get pod
```

首先pod的状态为ContainerCreating，然后状态变为NotReady

![image-20240423203122186](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148132.png)

最后pod变成不断失败不断重试的CrashLoopBackOff，READY为1/2

![image-20240423203143044](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148991.png)

查看pod详情，alpine容器启动是成功的，接下来记录容器重启失败

```shell
kubectl describe pod pod-lifecycle
```

![image-20240423203812452](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148017.png)

这是因为alpine容器启动后就退出了，容器一直在启动、退出循环。

在alpine容器启动后让它睡眠30s，不让他直接退出，那又是什么情况？

```shell
apiVersion: v1
kind: Pod
metadata:
  name: pod-lifecycle
spec:
  containers:
  - name: nginx-container
    image: nginx
  - name: alpine-container
    image: alpine #alpine容器启动后会直接退出  
    command: ["/bin/sh", "-c", "sleep 30"]
```

![image-20240423204456015](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148789.png)

使用watch持续监视pod情况，发现pod启动成功

![image-20240423204355746](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148494.png)

但是在pod启动成功30s后(alpine容器睡眠30S后退出)变成了NotReady，

![image-20240423204420343](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148358.png)

然后alpine容器重新启动。alpine容器每隔30秒重启一次。

![image-20240423204436337](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148363.png)

### 初始化容器不应该阻塞

初始化容器（Init Container）是在主容器启动之前运行的容器，用于执行初始化任务，例如加载配置文件、准备数据、等待外部服务就绪等。初始化容器不允许阻塞主容器的启动，它们的生命周期是独立于主容器的，一旦初始化任务完成，它们就会退出。

```shell
apiVersion: v1
kind: Pod
metadata:
  name: pod-lifecycle
spec:
  initContainers:
  - name: init-nginx
    image: nginx
  containers:
  - name: nginx-container
    image: nginx
  - name: alpine-container
    image: alpine #alpine容器启动后会直接退出  
    command: ["/bin/sh", "-c", "sleep 30"]
```



![image-20240423205353325](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148274.png)

使用watch持续监视pod情况，发现pod状态一直为init:0/1

![image-20240423205405242](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148038.png)

查看pod详细信息

```shell
kubectl describe pod pod-lifecycle
```

发现pod在执行启动nginx后就一直阻塞在这里，不再运行接下来的两个容器了。

![image-20240423205621299](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148151.png)

初始化容器必须有终结的时候。



初始化容器启动时准备数据后退出，使用卷挂载查看效果,查看应用容器是否被修改了内容。

```shell
apiVersion: v1
kind: Pod
metadata:
  name: pod-lifecycle
spec:
  volumes:
  - name: common-volume
    emptyDir: {}
  initContainers:
  - name: init-alpine
    image: alpine
    command: ["/bin/sh", "-c", "echo 123456 > /app/index.html; sleep 10"]
    volumeMounts:
    - name: common-volume
      mountPath: /app
  containers:
  - name: nginx-container
    image: nginx
    volumeMounts:
    - name: common-volume
      mountPath: /usr/share/nginx/html
  - name: alpine-container
    image: alpine #alpine容器启动后会直接退出  
    command: ["/bin/sh", "-c", "sleep 30"]
```

![image-20240423210634358](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148479.png)

等待pod启动成功

![image-20240423210746370](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148521.png)

访问nginx，发现index.html已经成功被修改为123456.

![image-20240423210754801](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148429.png)

### 临时容器线上排错

https://kubernetes.io/zh-cn/docs/concepts/workloads/pods/ephemeral-containers/

![image-20240424075015936](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242148578.png)

如果 [容器镜像](https://kubernetes.io/zh-cn/docs/reference/glossary/?all=true#term-image) 包含调试程序， 比如从 Linux 和 Windows 操作系统基础镜像构建的镜像，你可以使用 `kubectl exec` 命令 在特定的容器中进行排查，但是当由于容器崩溃或容器镜像不包含调试程序（例如[无发行版镜像](https://github.com/GoogleContainerTools/distroless)等） 而导致 `kubectl exec` 无法运行时，[临时容器](https://kubernetes.io/zh-cn/docs/concepts/workloads/pods/ephemeral-containers/)对于排除交互式故障很有用。

![image-20240424075720317](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149968.png)



临时容器通常在调试或故障排除期间使用，不会持续存在于系统中。

临时容器共享了Pod的所有，临时容器 有Debug的一些命令，排错完成以后，只要exit退出容器，临时容器会自动删除

#### 开启临时容器配置

临时容器功能需要 Kubernetes 版本为 1.16 或更高版本，并且需要在集群中启用此功能才能够使用。kubeadm部署开启方式，临时容器开启特性门控 --feature-gates="EphemeralContainers=true" 在所有组件，修改对应配置文件：kube-apiserver 、kube-scheduler、 kubelet配置文件

1.编辑kube-apiserver配置，/etc/kubernetes/manifests/kube-apiserver.yaml

在spec.containers.command 部分增加如下一行

```shell
- --feature-gates=EphemeralContainers=true
```

![image-20240424090635778](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149760.png)



2/etc/kubernetes/manifests/kube-scheduler.yaml

在spec.containers.command 部分增加如下一行

```shell
- --feature-gates=EphemeralContainers=true
```

![image-20240424090828042](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149276.png)

3==所有节点==配置/etc/sysconfig/kubelet

```shell
KUBELET_EXTRA_ARGS="--feature-gates=EphemeralContainers=true"
```

![image-20240424091012009](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149342.png)

4.==所有节点==修改完毕后需要重启kubelet

```shell
systemctl restart kubelet
```

#### 使用临时容器来调试的例子

![image-20240424091412602](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149899.png)

有一个多实例的pod，如果我需要调测该pod中某个容器（nginx-container容器），可以使用如下命令直接在pod里面建立临时容器

```
kubectl debug -it pod-multi-container  --image=busybox:1.28 --target=nginx-container
```

此命令添加一个新的 busybox 容器并将其挂接到该容器。`--target` 参数指定另一个容器的进程命名空间。 这个指定进程命名空间的操作是必需的，因为 `kubectl run` 不能在它创建的 Pod 中启用[共享进程命名空间](https://kubernetes.io/zh-cn/docs/tasks/configure-pod-container/share-process-namespace/)。

![image-20240424092019677](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149023.png)



pod里会记录临时容器信息，且不能复用这些临时容器。具体如下：

pod中会保留Ephemeral Container信息，以及临时容器的创建的信息

```shell
[root@k8s-master k8s]# kubectl describe pod pod-multi-container 
Name:         pod-multi-container
Namespace:    default
Priority:     0
Node:         k8s-node1/192.168.122.141
Start Time:   Tue, 23 Apr 2024 02:12:56 +0800
Labels:       <none>
Annotations:  cni.projectcalico.org/containerID: f99752834f938347fa2a78c4fd9b2124a3eadf24c9704e18815e4b3d7135267e
              cni.projectcalico.org/podIP: 10.244.36.110/32
              cni.projectcalico.org/podIPs: 10.244.36.110/32
Status:       Running
IP:           10.244.36.110
IPs:
  IP:  10.244.36.110
Containers:
  nginx-container:
    Container ID:   docker://69472b1d0e05376e1da948e7e38ab2fc4d8639ebb88a92d774dd9ad1fd6670db
    Image:          nginx
    Image ID:       docker-pullable://nginx@sha256:0d17b565c37bcbd895e9d92315a05c1c3c9a29f762b011a10c54a66cd53c9b31
    Port:           <none>
    Host Port:      <none>
    State:          Running
      Started:      Tue, 23 Apr 2024 02:13:15 +0800
    Ready:          True
    Restart Count:  0
    Environment:    <none>
    Mounts:
      /usr/share/nginx/html from common-volume (rw)
      /var/run/secrets/kubernetes.io/serviceaccount from kube-api-access-tsm6t (ro)
  content-container:
    Container ID:  docker://45f273fe225301821b56d4bf6fff3f49e92d6eab2b3d87fcc24b0c95c1744145
    Image:         alpine
    Image ID:      docker-pullable://alpine@sha256:21a3deaa0d32a8057914f36584b5288d2e5ecc984380bc0118285c70fa8c9300
    Port:          <none>
    Host Port:     <none>
    Command:
      /bin/sh
      -c
      while true; do sleep 1; date > /app/index.html; done;
    State:          Running
      Started:      Tue, 23 Apr 2024 02:13:33 +0800
    Ready:          True
    Restart Count:  0
    Environment:    <none>
    Mounts:
      /app from common-volume (rw)
      /var/run/secrets/kubernetes.io/serviceaccount from kube-api-access-tsm6t (ro)
Ephemeral Containers:
  debugger-rf9t2:
    Container ID:   docker://61af8cc817b19d17d48e48ee9d6cbee9862109589f4533919fcf613e01ec07a1
    Image:          busybox:1.28
    Image ID:       docker-pullable://busybox@sha256:141c253bc4c3fd0a201d32dc1f493bcf3fff003b6df416dea4f41046e0f37d47
    Port:           <none>
    Host Port:      <none>
    State:          Terminated
      Reason:       Completed
      Exit Code:    0
      Started:      Tue, 23 Apr 2024 11:29:18 +0800
      Finished:     Tue, 23 Apr 2024 11:29:34 +0800
    Ready:          False
    Restart Count:  0
    Environment:    <none>
    Mounts:         <none>
  debugger-lg9wx:
    Container ID:   docker://d0fb252f96e15d2a3dc1067ff22f2d53ec92c03f9ffe6b40aa39da809e1b1b71
    Image:          busybox:1.28
    Image ID:       docker-pullable://busybox@sha256:141c253bc4c3fd0a201d32dc1f493bcf3fff003b6df416dea4f41046e0f37d47
    Port:           <none>
    Host Port:      <none>
    State:          Terminated
      Reason:       Completed
      Exit Code:    0
      Started:      Tue, 23 Apr 2024 11:32:42 +0800
      Finished:     Tue, 23 Apr 2024 11:33:46 +0800
    Ready:          False
    Restart Count:  0
    Environment:    <none>
    Mounts:         <none>
Conditions:
  Type              Status
  Initialized       True 
  Ready             True 
  ContainersReady   True 
  PodScheduled      True 
Volumes:
  common-volume:
    Type:       EmptyDir (a temporary directory that shares a pod's lifetime)
    Medium:     
    SizeLimit:  <unset>
  kube-api-access-tsm6t:
    Type:                    Projected (a volume that contains injected data from multiple sources)
    TokenExpirationSeconds:  3607
    ConfigMapName:           kube-root-ca.crt
    ConfigMapOptional:       <nil>
    DownwardAPI:             true
QoS Class:                   BestEffort
Node-Selectors:              <none>
Tolerations:                 node.kubernetes.io/not-ready:NoExecute op=Exists for 300s
                             node.kubernetes.io/unreachable:NoExecute op=Exists for 300s
Events:
  Type    Reason   Age        From     Message
  ----    ------   ----       ----     -------
  Normal  Pulling  <invalid>  kubelet  Pulling image "busybox:1.28"
  Normal  Pulled   <invalid>  kubelet  Successfully pulled image "busybox:1.28" in 18.825451642s
  Normal  Created  <invalid>  kubelet  Created container debugger-rf9t2
  Normal  Started  <invalid>  kubelet  Started container debugger-rf9t2
```

当执行exit退出容器时，临时容器就会被立刻删除。





#### 通过 Pod 副本调试 的例子

```shell
kubectl debug  pod-multi-container -it --share-processes --image=busybox:1.28 --copy-to=nginx-container --attach=false
```

这个命令共享的是名为 `nginx-container` 的容器的进程。也就是说，它会允许你在 `nginx-container` 中查看和操作进程，而不需要复制容器。

如果你没有使用 `--container` 指定新的容器名，`kubectl debug` 会自动生成的，默认为debugger-xxxxx.

会在 Pod 中启动一个临时容器，使用 busybox:1.28 镜像，并且共享进程命名空间。它将被命名为 "tomcat-debug"，但不会自动连接到指定的 nginx-container。

默认情况下，`-i` 标志使 `kubectl debug` 附加到新容器上。 你可以通过指定 `--attach=false` 来防止这种情况。 如果你的会话断开连接，你可以使用 `kubectl attach` 重新连接。

`--share-processes` 允许在此 Pod 中的其他容器中查看该容器的进程

![image-20240424094945895](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149990.png)

创建pod名为nginx-container 容器名为 debugger-dgkvg，等待启动成功，使用exec参数进入临时容器当中，可以看到业务pod的进程，进行一些调测活动

```shell
kubectl exec -it nginx-container -- /bin/bash
```

可以看到新创建的pod中有三个容器：nginx-container, content-container, debugger-dgkvg

![image-20240424095557442](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149786.png)

这种方式创建的临时容器的pod不会自动删除，不要忘了清理调试 Pod：

![image-20240424095726735](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149746.png)



需要快速排除容器中的问题时，可以通过在现有 Pod 中创建临时容器来实现。这种方法可以快速提供一个与原始容器相同的环境，并在其中执行调试操作。

当需要对生产环境中的应用程序进行故障排除时，通过创建 Pod 的副本来模拟问题并进行调试是一种常见的方法。这种方法可以提供一个安全且独立的环境，用于排除问题，而不会影响实际生产环境中的用户。



## 静态Pod

在 /etc/kubernetes/manifests 位置放的所有Pod.yaml文件，机器启动kubelet自己就把他启动起来。 静态Pod一直守护在他的这个机器上，即使使用kubectl delete的命令删除这个静态pod，k8s还是会重新创建，只能将该目录下的对应的Pod.yaml删除，才能够删除这个静态pod。

![image-20240424101703692](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149734.png)

创建一个静态pod

```shell
apiVersion: v1
kind: Pod
metadata:
  name: pod-static
spec:
  containers:
  - name: nginx-container
    image: nginx
```

k8s会自动创建这个静态pod，后缀为k8s-master意味着是在k8s-master节点上的静态pod

![image-20240424102008028](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149823.png)

在k8s-node2节点再创建一个静态pod

![image-20240424102326969](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149703.png)

查看pod

![image-20240424102349914](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149610.png)

## Probe 探针机制（健康检查机制）

https://kubernetes.io/zh-cn/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/#configure-probes

在 Kubernetes 中，探针（Probes）用于检查容器的健康状态以及确定何时应该将容器标记为准备就绪或者需要重启。Kubernetes 支持三种类型的探针：

1. **Liveness Probe（存活探针）**：用于确定容器是否在运行。如果存活探针失败，Kubernetes 将重启容器。

   对于存活探针（Liveness Probe）：如果存活探针失败，Kubernetes 将自动重启容器。它会尝试将容器恢复到健康状态，并继续运行应用程序。如果存活探针失败，Kubernetes 将首先重启容器，希望通过重新启动容器来恢复应用程序的健康状态。如果重启容器仍然无法解决问题，Kubernetes 可能会根据配置的重启策略进一步采取行动。重启策略包括：

   Always（默认）：始终重启容器，无限次数地尝试恢复应用程序的健康状态。
   OnFailure：仅在容器失败（退出状态码非零）时重启容器，尝试恢复应用程序的健康状态。
   Never：永不重启容器，不会尝试恢复应用程序的健康状态。

2. **Readiness Probe（就绪探针）**：用于确定容器是否准备好接收流量。

   对于就绪探针（Readiness Probe）：如果就绪探针失败，Kubernetes 将从服务负载均衡的池中剔除该容器。这意味着新的流量将不会被路由到该容器，直到就绪探针成功为止。这可以确保只有健康的容器能够接收流量，避免将流量发送到尚未准备好的容器上。一旦就绪探针成功，Kubernetes 将再次将容器纳入服务负载均衡，并开始将新的流量路由到该容器。

3. **Startup Probe（启动探针）**：用于确定容器是否已经启动并准备好接收正常的流量。与就绪探针类似，但在容器启动时进行一次性检查，而不会影响其后续状态。它主要用于检测应用程序是否成功启动，并在启动过程中提供一定的等待时间。
   如果启动探针失败，Kubernetes 不会采取任何特殊行动。这是因为启动探针失败只意味着应用程序尚未成功启动，并且不会触发容器的重启或负载均衡操作

Probe配置项：

- initialDelaySeconds ：容器启动后要等待多少秒后存活和就绪探测器才被初始化，默认 是 0 秒，最小值是 0。这是针对以前没有

- periodSeconds ：执行探测的时间间隔（单位是秒）。默认是 10 秒。最小值是 1。

- successThreshold ：探测器在失败后，被视为成功的最小连续成功数。默认值是 1，最小值是 1。 存活和启动探针的这个值必须是 1。

- failureThreshold ：当探测失败时，Kubernetes 的重试次数。 存活探测情况下的放弃就 意味着重新启动容器。 就绪探测情况下的放弃 Pod 会被打上未就绪的标签。默认值是 3。最 小值是 1。

- timeoutSeconds ：探测的超时后等待多少秒。默认值是 1 秒。最小值是 1。



探针可以使用以下三种方式之一定义：

- 执行命令（Exec）：通过在容器内执行特定的命令来检查应用程序的状态。如果命令的返回状态码是 0，探针被认为是成功的；否则，探针被认为是失败的。
- 发送 HTTP 请求（HTTP GET）：通过发送 HTTP GET 请求到容器内的指定端点来检查应用程序的状态。如果返回的 HTTP 状态码在 2xx 或 3xx 范围内，探针被认为是成功的；否则，探针被认为是失败的。
- TCP 套接字（TCP Socket）：通过尝试建立到容器内指定端口的 TCP 连接来检查应用程序的状态。如果连接成功建立，探针被认为是成功的；否则，探针被认为是失败的。

### 存活探针 livenessProbe

#### exec

```shell
apiVersion: v1
kind: Pod
metadata:
  name: liveness-exec
spec:
  containers:
  - name: liveness
    image: busybox
    args:
    - /bin/sh
    - -c
    - touch /tmp/healthy; sleep 30; rm -f /tmp/healthy; sleep 600
    livenessProbe:
      exec:
        command:
        - cat
        - /tmp/healthy
      initialDelaySeconds: 5
      periodSeconds: 5
```

在这个配置文件中，可以看到 Pod 中只有一个 `Container`。 `periodSeconds` 字段指定了 kubelet 应该每 5 秒执行一次存活探测。 `initialDelaySeconds` 字段告诉 kubelet 在执行第一次探测前应该等待 5 秒。 kubelet 在容器内执行命令 `cat /tmp/healthy` 来进行探测。 如果命令执行成功并且返回值为 0，kubelet 就会认为这个容器是健康存活的。 如果这个命令返回非 0 值，kubelet 会杀死这个容器并重新启动它。

当容器启动时，执行如下的命令：

```shell
/bin/sh -c "touch /tmp/healthy; sleep 30; rm -f /tmp/healthy; sleep 600"
```

这个容器生命的前 30 秒，`/tmp/healthy` 文件是存在的。 所以在这最开始的 30 秒内，执行命令 `cat /tmp/healthy` 会返回成功代码。 30 秒之后，执行命令 `cat /tmp/healthy` 就会返回失败代码。

```shell
kubectl apply -f exec-liveness.yaml
```

![image-20240424133631853](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149495.png)

在 30 秒内，查看 Pod 的事件：

```shell
kubectl describe pod liveness-exec
```

输出结果表明还没有存活探针失败：

![image-20240424133643202](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149462.png)

35 秒之后，再来看 Pod 的事件：

有信息显示存活探针失败了

![image-20240424133717218](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149178.png)

再过5秒，来看 Pod 的事件，这个失败的容器被杀死并且被重建了。：

![image-20240424133810379](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149770.png)

输出结果显示 `RESTARTS` 的值增加了 1。 请注意，一旦失败的容器恢复为运行状态，`RESTARTS` 计数器就会增加 1：

![image-20240424133818976](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149376.png)

#### httpGet

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: liveness-http-get
spec:
  volumes:
    - name: common-html
      hostPath:
        path: /dongguo/tmp
  containers:
    - name: nginx-liveness
      image: nginx
      volumeMounts:
        - name: common-html
          mountPath: /usr/share/nginx/html
      livenessProbe:
        httpGet:
          path: /abc.html
          port: 80
        periodSeconds: 10
        successThreshold: 1
        failureThreshold: 10
```

存活探针会在 Pod 启动时以每 5 秒的周期发送 HTTP GET 请求到 `http://Pod的IP:80/abc.html`，如果在 5 次重试中至少有 1 次请求成功，就认为探针成功。

```
kubectl get pod -o wide
```

pod创建在k8s-node2节点

![image-20240424202452122](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149744.png)

```shell
watch -n 1 kubectl get pod
```

持续监测pod的创建，pod已经创建，但是存活探针还未执行成功

![image-20240424202510087](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149574.png)

存活探针执行失败进入重试阶段

![image-20240424202556776](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149433.png)

```
echo "abc" > abc.html
```

在k8s-node2节点创建abc.html

![image-20240424202614822](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149475.png)

监测到存活探针执行成功，READY变为1/1

![image-20240424202626879](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149305.png)



### 启动探针Startup Probe

#### exec

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: exec-start-up
spec:
  volumes:
    - name: common-html
      hostPath:
        path: /dongguo/tmp
  containers:
    - name: nginx-start-up
      image: nginx
      volumeMounts:
        - name: common-html
          mountPath: /app
      startupProbe:
          exec: 
              command: [ "/bin/sh", "-c", "cat /app/a.txt" ]
          periodSeconds: 5
          timeoutSeconds: 5
          successThreshold: 1
          failureThreshold: 5
```

pod创建在k8s-node2节点

![image-20240424203937028](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149852.png)

持续监测pod，启动探针还未执行成功

![image-20240424203948896](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149870.png)

在k8s-node2节点创建a.txt

![image-20240424204007497](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149218.png)

查看到启动探针执行成功

![image-20240424204017594](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242149455.png)

#### httpGet

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx-start-probe
spec:
  containers:
    - name: nginx-start-up
      image: nginx
      startupProbe:
        httpGet:
          path: /
          port: 80
        initialDelaySeconds: 5  
        periodSeconds: 5
        timeoutSeconds: 5
        successThreshold: 1
        failureThreshold: 5
```

启动探针将尝试在容器的 80 端口进行 HTTP GET 请求，检查是否可以成功连接到该端口。

![image-20240424172508708](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242150497.png)

容器启动后，启动探针还未就绪，READY为0/1

![image-20240424172841886](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242150914.png)

容器启动后，启动探针延迟5秒执行

![image-20240424172701367](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242150262.png)

查看日志，显示启动探针执行的请求。启动探针探测成功后就不再执行了。

![image-20240424173406044](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242150623.png)

### 就绪探针Readiness Probe

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx-readiness-probe
spec:
  volumes:
    - name: common-html
      hostPath:
        path: /dongguo/tmp
  containers:
    - name: nginx-readiness
      image: nginx
      volumeMounts:
        - name: common-html
          mountPath: /usr/share/nginx/html
      readinessProbe:
        httpGet:
          path: /111.html
          port: 80
        initialDelaySeconds: 5  
        periodSeconds: 5
        timeoutSeconds: 5
        successThreshold: 1
        failureThreshold: 5
```

![image-20240424205318058](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242150288.png)

/usr/share/nginx/html没有/111.html，就绪探针执行失败

![image-20240424214642890](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242150266.png)

访问403

![image-20240424211948711](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242150609.png)





![image-20240424205937238](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242150089.png)



将yaml修改为一下内容

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx-readiness-probe
spec:
  containers:
    - name: nginx-readiness
      image: nginx
      readinessProbe:
        httpGet:
          path: /
          port: 80
        initialDelaySeconds: 5  
        periodSeconds: 5
        timeoutSeconds: 5
        successThreshold: 1
        failureThreshold: 5
```

![image-20240424214454641](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242150606.png)

容器启动成功，就绪探针执行成功后，请求成功

![image-20240424214504286](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404242150358.png)