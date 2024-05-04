# RC（ReplicationController）

**ReplicationController** 确保在任何时候都有特定数量的 Pod 副本处于运行状态。 换句话说，ReplicationController 确保一个 Pod 或一组同类的 Pod 总是可用的。

尽管 ReplicationController 仍然可以使用，并且在一些特定的场景下仍然有其用处，但是 Deployment 提供了更多的功能和优势，因此已经成为更为常用和推荐的副本管理机制。现在推荐使用配置 [`ReplicaSet`](https://kubernetes.io/zh-cn/docs/concepts/workloads/controllers/replicaset/) 的 [`Deployment`](https://kubernetes.io/zh-cn/docs/concepts/workloads/controllers/deployment/) 来建立副本管理机制。

可以理解为RC是旧版，RS是新版



# RS（ReplicaSet）

ReplicaSet 的目的是维护一组在任何时候都处于运行状态的 Pod 副本的稳定集合。 因此，它通常用来保证给定数量的、完全相同的 Pod 的可用性。

ReplicaSet 确保任何时间都有指定数量的 Pod 副本在运行。 然而，Deployment 是一个更高级的概念，它管理 ReplicaSet，并向 Pod 提供声明式的更新以及许多其他有用的功能。 因此，我们建议使用 Deployment 而不是直接使用 ReplicaSet， 除非你需要自定义更新业务流程或根本不需要更新。

这实际上意味着，你可能永远不需要操作 ReplicaSet 对象：而是使用 Deployment，并在 spec 部分定义你的应用。

[`Deployment`](https://kubernetes.io/zh-cn/docs/concepts/workloads/controllers/deployment/) 是一个可以拥有 ReplicaSet 并使用声明式方式在服务器端完成对 Pod 滚动更新的对象。 尽管 ReplicaSet 可以独立使用，目前它们的主要用途是提供给 Deployment 作为编排 Pod 创建、删除和更新的一种机制。当使用 Deployment 时，你不必关心如何管理它所创建的 ReplicaSet，Deployment 拥有并管理其 ReplicaSet。 因此，建议你在需要 ReplicaSet 时使用 Deployment。

# DaemonSet

k8s集群的每个机器(每一个节点)都运行一个程序（默认master除外，master节点默认不会把Pod调度过 去）

无需指定副本数量；因为默认给每个机器都部署一个（master除外）

DaemonSet 控制器确保所有（或一部分）的节点都运行了一个指定的 Pod 副本。

- 每当向集群中添加一个节点时，指定的 Pod 副本也将添加到该节点上

- 当节点从集群中移除时，Pod 也就被垃圾回收了

- 删除一个 DaemonSet 可以清理所有由其创建的 Pod

DaemonSet 的典型使用场景有：

- 在每个节点上运行集群的存储守护进程，例如 glusterd、ceph

- 在每个节点上运行日志收集守护进程，例如 fluentd、logstash

- 在每个节点上运行监控守护进程，例如 Prometheus Node Exporter、Sysdig Agent、collectd、 Dynatrace OneAgent、APPDynamics Agent、Datadog agent、New Relic agent、Ganglia gmond、Instana Agent 等

```yaml
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: logging
  labels:
    app: logging
spec:
  selector:
    matchLabels:
      name: logging
  template:
    metadata:
      labels:
        name: logging
    spec:
      containers:
      - name: logging
        image: nginx
        resources:
          limits:
            memory: 200Mi
          requests:
            cpu: 100m
            memory: 200Mi
      tolerations:
      - key: node-role.kubernetes.io/master
        effect: NoSchedule
```

在 Kubernetes 中，`tolerations`（容忍）是一种用于 Pod 的调度约束的机制。当节点上存在某个污点（taint）时，具有匹配容忍设置的 Pod 将允许被调度到该节点上。这对于在一些特定节点上允许运行特定类型的 Pod 非常有用。

允许 Pod 被调度到具有 `node-role.kubernetes.io/master` 污点的节点上，这意味着该 DaemonSet 中的 Pod 可以在主节点上运行。

![image-20240427141621454](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291624885.png)

查看效果，除了在node节点中创建了pod，master也创建了pod

![image-20240427141639555](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291623812.png)

# StatefulSet

Deployment部署的应用我们一般称为无状态应用

StatefulSet部署的应用我们一般称为有状态应用

无状态应用：网络可能会变，存储可能会变，顺序可能会变。场景就是业务代码（Deployment）

有状态应用：网络不变，存储不变，顺序不变。场景就是中间件（MySQL、Redis、MQ）

StatefulSet 使用场景；对于有如下要求的应用程序，StatefulSet 非常适用：

- 稳定、唯一的网络标识（dnsname）【必须配合Service】

StatefulSet通过与其相关的无头服务为每个pod提供DNS解析条目。假如无头服务的DNS 条目为:"$(service name).$(namespace).svc.cluster.local"，

那么pod的解析条目就是"$(pod name).$(service name).$(namespace).svc.cluster.local"， 每个pod name也是唯一的。

- 稳定的、持久的存储；【每个Pod始终对应各自的存储路径 （PersistantVolumeClaimTemplate）】

- 有序的、优雅的部署和缩放。【按顺序地增加副本、减少副本，并在减少副本时执行清理】

- 有序的、自动的滚动更新。【按顺序自动地执行滚动更新】

限制

- 给定 Pod 的存储必须由 PersistentVolume 驱动 基于所请求的 storage class 来提供，或 者由管理员预先提供。

- 删除或者收缩 StatefulSet 并不会 删除它关联的存储卷。 这样做是为了保证数据安全，它通 常比自动清除 StatefulSet 所有相关的资源更有价值。

- StatefulSet 当前需要无头服务 来负责 Pod 的网络标识。你需要负责创建此服务。

- 当删除 StatefulSets 时，StatefulSet 不提供任何终止 Pod 的保证。 为了实现 StatefulSet 中的 Pod 可以有序地且体面地终止，可以在删除之前将 StatefulSet 缩放为 0。

- 在默认 Pod 管理策略( OrderedReady ) 时使用 滚动更新，可能进入需要人工干预 才能修复 的损坏状态。



![image-20240427143157181](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291624239.png)







```yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx
  labels:
    app: nginx
spec:
  ports:
  - port: 80
    name: nginx
    targetPort: 80
  clusterIP: None
  selector:
    app: nginx

---

apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: statefulset-nginx
  namespace: default
spec:
  serviceName: nginx
  replicas: 3
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: nginx:latest
        ports:
        - containerPort: 80
```

![image-20240427162020022](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291624422.png)

![image-20240427162117082](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291624242.png)

dashboard新增Stateful Sets

![image-20240427163555515](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291624755.png)

可以看到statefulset-nginx 三个pod按照顺序依次创建（0,1,2）

![image-20240427163542380](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291624992.png)

整个状态kubelet（DNS内容同步到Pod）和kube-proxy（整个集群网络负责）会同步



我们随便进入另外一个service下的pod，就可以通过【pod名.service名】访问statefulset的pod

![image-20240427164847580](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291624352.png)

【service名】 负载均衡到sts部署的Pod上

![image-20240427165112416](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291624678.png)

如果一个应用程序不需要稳定的网络标识，或者不需要按顺序部署、删除、增加副本，就应该考虑使用 Deployment 这类无状态（stateless）的控制器

## podManagementPolicy： pod管理策略

podManagementPolicy : 控制Pod创建、升级以及扩缩容逻辑

对于 Deployment / StatefulSet对象来说，`podManagementPolicy` 有两个选项可供选择：

1. **OrderedReady(默认)**：按顺序创建和删除 Pod，并确保新 Pod 只有在旧 Pod 已经变得 Ready 之后才会创建。这是默认的行为。这个选项适用于需要在更新过程中保持应用的稳定性和可用性的场景，比如有状态应用或者需要进行逐个验证的服务。
2. **Parallel**：并行创建和删除 Pod，新 Pod 可以同时与旧 Pod 并行创建，不需要等待旧 Pod 变得 Ready。这个选项适用于对更新速度要求较高，并且可以容忍短暂中断的无状态应用。

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: statefulset-nginx
  namespace: default
spec:
  podManagementPolicy: Parallel #并行创建
  serviceName: nginx
  replicas: 3
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: nginx:latest
        ports:
        - containerPort: 80
```

![image-20240428173219781](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291624354.png)

## updateStrategy： 更新策略

`updateStrategy` 是 Kubernetes 中 Deployment 和 StatefulSet 等控制器对象的一个字段，用于指定控制器如何进行更新操作。

对于 Deployment 对象来说，`updateStrategy` 字段有以下几种选项：

1. **RollingUpdate**：滚动更新策略，这是默认的更新策略。它会逐步替换旧的 Pod 实例为新的 Pod 实例，确保更新过程中应用的稳定性和可用性。具体来说，会根据 Deployment 中指定的副本数和更新期望值逐步替换 Pod。
2. **Recreate**：重建更新策略，这个策略会先删除所有的旧 Pod，然后再创建所有的新 Pod。这种策略会导致更新期间的短暂中断，不适合对应用有较高可用性要求的场景，但在某些情况下可能会更快速地完成更新。

对于 StatefulSet 对象来说，`updateStrategy` 字段通常只有一个选项：

1. **OnDelete**：在删除旧 Pod 时触发更新。这个策略适用于有状态应用，会在手动或者自动删除旧 Pod 时触发创建新的 Pod。这样可以确保每个 Pod 的更新过程都能保持状态的一致性，适用于需要严格控制更新顺序和过程的场景。

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: statefulset-nginx
  namespace: default
spec:
  updateStrategy: # 升级策略
    rollingUpdate:
      partition: 2  #更新大于等于这个索引的pod（当前索引0，1, 2）只更新最后一个
  serviceName: nginx
  replicas: 3
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: nginx:1.18.0
        ports:
        - containerPort: 80
```

![image-20240428174327992](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291624406.png)

查看对应pod的镜像

![image-20240428174400452](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291624824.png)

# Job

Kubernetes中的 Job 对象将创建一个或多个 Pod，并确保指定数量的 Pod 可以成功执行到进程正常 结束：

- 当 Job 创建的 Pod 执行成功并正常结束时，Job 将记录成功结束的 Pod 数量

- 当成功结束的 Pod 达到指定的数量时，Job 将完成执行

- 删除 Job 对象时，将清理掉由 Job 创建的 Pod

Job任务不建议去运行nginx，tomcat，mysql等阻塞式的，否则这些任务永远完不了。

在 Kubernetes 中，Job 的 Pod 默认情况下会等待容器完成，如果容器一直保持运行状态，那么 Job 就会一直等待下去，直到超时（如nginx）。为了避免这个问题，你可以尝试将容器的执行逻辑设计成非阻塞式的。

## 参数说明

```shell
[root@k8s-master ~]# kubectl explain job.spec
KIND:     Job
VERSION:  batch/v1

RESOURCE: spec <Object>

DESCRIPTION:
     Specification of the desired behavior of a job. More info:
     https://git.k8s.io/community/contributors/devel/sig-architecture/api-conventions.md#spec-and-status

     JobSpec describes how the job execution will look like.

FIELDS:
   activeDeadlineSeconds	<integer> #该字段限定了 Job 对象在集群中的存活时长，一旦达到.spec.activeDeadlineSeconds 指定的时长，该 Job 创建的所有的 Pod 都将被终止。但是Job不会删除，Job需要手动删除，或者使用ttl进行清理

   backoffLimit	<integer> #设定 Job 最大的重试次数。该字段的默认值为 6；一旦重试次数达到了backoffLimit 中的值，Job 将被标记为失败，且尤其创建的所有 Pod 将被终止；

   completionMode	<string>  #用于指定 Job 完成的模式。可以选择两种模式：1）IndexedJob: 默认模式，表示 Job 完成时，只有 Job 完成的 Pod 的索引号小于 completions 参数指定的数量时才算作完成。2）NonIndexedJob：表示 Job 完成时，所有 Pod 都完成了即可，不考虑其索引号。

   completions	<integer> #指定了 Job 完成的 Pod 的数量。当 Job 的所有 Pod 都完成时，Job 就被认为是完成了。默认值为 1。可以用来设置 Job 需要执行的任务数量。

   manualSelector	<boolean> #一个布尔值，用于指示是否手动选择 Pod。如果设置为 true，则需要手动选择要控制的 Pod。默认值为 false。

   parallelism	<integer> #指定了 Job 同时运行的 Pod 的最大数量。默认值为 1。当 Job 中的任务需要并行执行时，可以通过设置这个参数来指定并行度。

   selector	<Object> #一个标签选择器，用于选择与 Job 关联的 Pod。只有匹配该选择器的 Pod 才会被考虑为 Job 的一部分。

   suspend	<boolean> #一个布尔值，用于指示是否暂停 Job 的执行。如果设置为 true，则暂停 Job 的执行，不会创建新的 Pod。默认值为 false。

   template	<Object> -required- #定义了 Job 的 Pod 模板，包括 Pod 的容器、卷挂载等信息。template 字段下的 spec 包含了 Pod 的规范，可以在其中定义容器的镜像、命令、环境变量等配置。

   ttlSecondsAfterFinished	<integer> #指定了 Job 完成后存活的时间。完成后经过这段时间后，Kubernetes 会自动删除 Job。默认值为 nil，表示永远不会删除。
```

需要注意的是job目前可以使用两个API组来操作， batch/v1和extensions/v1beta1。当用户需要自定义selector时，使用两种API组时定义的 参数有所差异。 

使用batch/v1时，用户需要将jod的spec.manualSelector设置为true，才可以定制 selector。默认为false。

 使用extensions/v1beta1时，用户不需要额外的操作。因为extensions/v1beta1的 spec.autoSelector默认为false，该项与batch/v1的spec.manualSelector含义正好相反。换 句话说，使用extensions/v1beta1时，用户不想定制selector时，需要手动将 spec.autoSelector设置为true。



```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: job-ping
spec:
  completions: 5
  parallelism: 3
  template:
    spec:
      containers:
      - name: ping
        image: busybox
        command: ["/bin/sh", "-c", "ping -c 10 baidu.com"]
      restartPolicy: Never
  activeDeadlineSeconds: 600
  ttlSecondsAfterFinished: 10
  
```

- `completions: 5`：要求 Job 完成 5 次任务。
- `parallelism: 3`：指定同时运行的 Pod 最大数量为 3。
- `template` 字段定义了 Job 的 Pod 模板，其中的容器使用了 busybox 镜像，并执行了 ping 命令。
- `activeDeadlineSeconds: 600`：整个 Job 的运行时间限制为 600 秒。
- `ttlSecondsAfterFinished: 10`：Job 完成后存活 10 秒后自动删除。

![image-20240429100618085](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291624158.png)

pod执行完成后状态为Completed

![image-20240429100706233](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291624514.png)

当job所有pod执行完成后，job自动清除掉所有pod

![image-20240429100907982](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291624566.png)

# CronJob

CronJob 按照预定的时间计划（schedule）创建 Job（注意：启动的是Job不是Deploy，rs）。一个 CronJob 对象类似于 crontab (cron table) 文件中的一行记录。该对象根据 Cron 格式定义的时间计 划，周期性地创建 Job 对象。

所有 CronJob 的 schedule 中所定义的时间，都是基于 master 所在时区来进行计算的。

一个 CronJob 在时间计划中的每次执行时刻，都创建 大约 一个 Job 对象。这里用到了 大约 ，是 因为在少数情况下会创建两个 Job 对象，或者不创建 Job 对象。尽管 K8S 尽最大的可能性避免这 种情况的出现，但是并不能完全杜绝此现象的发生。因此，Job 程序必须是 幂等的。

当以下两个条件都满足时，Job 将至少运行一次：

- startingDeadlineSeconds 被设置为一个较大的值，或者不设置该值（默认值将被采纳）

- concurrencyPolicy 被设置为 Allow

## 参数说明

```shell
concurrencyPolicy：并发策略

[root@k8s-master ~]# kubectl explain cronjob.spec
KIND:     CronJob
VERSION:  batch/v1

RESOURCE: spec <Object>

DESCRIPTION:
     Specification of the desired behavior of a cron job, including the
     schedule. More info:
     https://git.k8s.io/community/contributors/devel/sig-architecture/api-conventions.md#spec-and-status

     CronJobSpec describes how the job execution will look like and when it will
     actually run.

FIELDS:
   concurrencyPolicy	<string> #concurrencyPolicy：并发策略
            "Allow" (允许，default):
            "Forbid"(禁止): forbids；前个任务没执行完，要并发下一个的话，下一个会被跳过
            "Replace"(替换): 新任务，替换当前运行的任务

   failedJobsHistoryLimit	<integer> #记录失败数的上限，Defaults to 1.

   jobTemplate	<Object> -required- #job定义

   schedule	<string> -required- #cron 表达式；

   startingDeadlineSeconds	<integer> #startingDeadlineSeconds： 表示如果Job因为某种原因无法按调度准时启动，在spec.startingDeadlineSeconds时间段之内，CronJob仍然试图重新启动Job，如果在.spec.startingDeadlineSeconds时间之内没有启动成功，则不再试图重新启动。如果spec.startingDeadlineSeconds的值没有设置，则没有按时启动的任务不会被尝试重新启动。

   successfulJobsHistoryLimit	<integer> #记录成功任务的上限。 Defaults to 3.

   suspend	<boolean> #暂停定时任务，对已经执行了的任务，不会生效； Defaults to false.
```



```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: cron-hello
spec:
  schedule: "*/1 * * * *" # 分、时、日、月、周
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: hello
            image: busybox
            args:
            - /bin/sh
            - -c
            - date; echo Hello from the Kubernetes cluster
          restartPolicy: OnFailure
```

- `schedule: "*/1 * * * *"`：指定了任务的调度时间，表示每分钟执行一次任务。
- `jobTemplate` 字段定义了 CronJob 使用的 Job 模板，包括 Pod 的模板以及运行的容器。
- `template` 字段定义了 Job 的 Pod 模板，其中的容器使用了 busybox 镜像，并执行了一个简单的命令。
- `restartPolicy: OnFailure`：指定了 Job 在失败时重启。

![image-20240429112557525](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291624387.png)

每分钟执行一次任务，执行完成的pod状态为Completed

![image-20240429112738813](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291625567.png)

![image-20240429113037435](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291625347.png)

# GC

https://kubernetes.io/zh-cn/docs/concepts/workloads/controllers/ttlafterfinished/

**特性状态：** `Kubernetes v1.23 [stable]`

job在处理完一个任务以后，状态会变成Completed，job在状态为Completed的时候默认不会自动清理的，还会继续占用系统资源。

## TTL-after-finished控制器

kubernetes中有专门的控制器可以自动清理已完成的job,就是TTL-after-finished控制器。
TTL-after-finished控制器供了一种 TTL 机制来限制已完成执行的 Job 对象的生命期。
TTL-after-finished 控制器只支持 Job，可以通过指定job的.spec.ttlSecondsAfterFinished 字段来自动清理已结束的 Job（Complete 或 Failed）。可以设置指定的时间，在指定的时间完成后的TTL 秒内被清理。一旦 Job 的状态条件发生变化表明该 Job 是 Complete 或 Failed，计时器就会启动；一旦 TTL 已过期，该 Job 就能被级联删除。当 TTL 控制器清理作业时，它将做级联删除操作，即删除 Job 的同时也删除其依赖对象。

## 开启TTLAfterFinished

TTLAfterFinished默认是关闭的，需要手动开启，找到安装的kubernetes的kube-apiserver.yaml和kube-controller-manager.yaml，增加开启TTLAfterFinished的设置。

kube-apiserver.yaml

```
TTLAfterFinished=true
```

![image-20240429150041327](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291625840.png)

![image-20240429150015159](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291625880.png)





kube-controller-manager.yaml

![image-20240429150319487](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291625263.png)

![image-20240429150306499](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291625389.png)



修改完kuber-apiserver和kube-controller-manager的yaml文件以后，需要重启kubelet服务，就可以生效了。

```
systemctl restart kubelet
```





官网的这个例子是当job完成后的100s就会自动清理了。

```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: pi-with-ttl
spec:
  ttlSecondsAfterFinished: 100
  template:
    spec:
      containers:
      - name: pi
        image: perl:5.34.0
        command: ["perl", "-Mbignum=bpi", "-wle", "print bpi(2000)"]
      restartPolicy: Never
         
```

![image-20240429150444095](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291625605.png)



Job `pi-with-ttl` 在结束 100 秒之后，可以成为被自动删除的对象。

![image-20240429152017639](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291625703.png)

如果该字段设置为 `0`，Job 在结束之后立即成为可被自动删除的对象。 如果该字段没有设置，Job 不会在结束之后被 TTL 控制器自动清除。

## 级联删除策略

当删除某个对象时，可以指定该对象的从属对象是否同时被自动删除，这种操作叫做级联删除 （cascading deletion）。级联删除有两种模式：后台（background）和前台（foreground） 如果删除对象时不删除自动删除其从属对象，此时，从属对象被认为是孤儿（或孤立的 orphaned）

通过参数 `--cascade`，`kubectl delete` 命令可以选择不同的级联删除策略。级联删除策略确定了在删除父资源时是否同时删除与之相关联的子资源。

具体来说，`kubectl delete` 命令的 `--cascade` 参数有两个选项：

1. `--cascade=true`：表示启用级联删除。当删除父资源时，与之关联的所有子资源也将被删除。这是默认行为。
2. `--cascade=false/orphan`：表示禁用级联删除。当删除父资源时，与之关联的子资源将被保留，不会被删除。



k8s资源默认使用级联删除，当执行了删除一个Deployment的操作时，与其关联的ReplicaSet和Pod也会被删除。

例如，如果你想删除一个 Deployment 资源，但保留与之关联的 Pod 资源，可以使用以下命令

```shell
kubectl delete deployment nginx-deployment --cascade=orphan
```

![image-20240429134847688](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291625809.png)

pod存在多个副本，同样删除replicaset ，保留pod

```
kubectl delete rs nginx-deployment-7848d4b86f --cascade=orphan
```



此时删除的pod不会被重建

![image-20240429161352785](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291625537.png)

