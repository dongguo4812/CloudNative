# 什么是Deployment

一个 Deployment 为 [Pod](https://kubernetes.io/zh-cn/docs/concepts/workloads/pods/) 和 [ReplicaSet](https://kubernetes.io/zh-cn/docs/concepts/workloads/controllers/replicaset/) 提供声明式的更新能力。

你负责描述 Deployment 中的**目标状态**，而 Deployment [控制器（Controller）](https://kubernetes.io/zh-cn/docs/concepts/architecture/controller/) 以受控速率更改实际状态， 使其变为期望状态。你可以定义 Deployment 以创建新的 ReplicaSet，或删除现有 Deployment， 并通过新的 Deployment 收养其资源。

不要管理 Deployment 所拥有的 ReplicaSet。Deployment 提供了一个抽象层级，它简化了应用程序的部署和更新。通过管理 Deployment，可以定义应用程序的期望状态，而不必关心底层的 ReplicaSet 或 Pod。

我们部署一个应用一般不直接写Pod，而是部署一个Deployment

# Deployment创建

编写规约 https://kubernetes.io/zh/docs/concepts/workloads/controllers/deployment/#writing-a-deployment-spec

![image-20240425103050176](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291620368.png)

下面是一个 Deployment 示例。其中创建了一个 ReplicaSet，负责启动三个 `nginx` Pod：

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
  labels:
    app: nginx
spec:
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
        image: nginx:1.14.2
        ports:
        - containerPort: 80
```

- .metadata.name 指定deploy名字

  创建名为 `nginx-deployment`（由 `.metadata.name` 字段标明）的 Deployment。 该名称将成为后续创建 ReplicaSet 和 Pod 的命名基础。 参阅[编写 Deployment 规约](https://kubernetes.io/zh-cn/docs/concepts/workloads/controllers/deployment/#writing-a-deployment-spec)获取更多详细信息。

- replicas 指定副本数量

  该 Deployment 创建一个 ReplicaSet，它创建三个（由 `.spec.replicas` 字段标明）Pod 副本。

- selector 指定匹配的Pod模板。

  `.spec.selector` 字段定义所创建的 ReplicaSet 如何查找要管理的 Pod。 在这里，你选择在 Pod 模板中定义的标签（`app: nginx`）。 不过，更复杂的选择规则是也可能的，只要 Pod 模板本身满足所给规则即可。只要 Pod 模板本身满足所给规则即可。

- template 声明一个Pod**模板**

  template字段包含以下子字段：

​			Pod 被使用 `.metadata.labels` 字段打上 `app: nginx` 标签。

​			Pod 模板规约（即 `.template.spec` 字段）指示 Pod 运行一个 `nginx` 容器， 该容器运行版本为 1.14.2 的 `nginx` [Docker Hub](https://hub.docker.com/) 镜像。

​			创建一个容器并使用 `.spec.template.spec.containers[0].name` 字段将其命名为 `nginx`。

![image-20240425110318383](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291620442.png)

查看与nginx-deployment部署相关创建的应用

![image-20240425110417129](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291620162.png)

Deployment 创建一个 ReplicaSet，ReplicaSet根据在Deployment 中定义的副本数(replicas)创建三个pod副本，pod的名称对应的是replicaset名称-xxxxx

# 自我修复机制

当创建一个 Deployment 时，Kubernetes 将会根据您提供的配置创建一个 ReplicaSet，并使用 ReplicaSet 控制器来维护所需的 Pod 副本数。如果有 Pod 副本被删除或出现故障，ReplicaSet 控制器将会启动新的 Pod 来替代它们，以确保总的副本数量与 Deployment 中定义的数量保持一致。

![image-20240425111752067](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291620194.png)

# 更新机制



## 命令行修改的方式

更新 nginx Pod 以使用 `nginx:1.16.1` 镜像

```shell
kubectl set image deployment/nginx-deployment nginx=nginx:1.16.1
```

![image-20240425113914114](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291620181.png)

ReplicaSet 使用滚动更新（Rolling Update）的方式来更新镜像版本。滚动更新是一种逐步替换旧 Pod 的方法，以确保应用程序的高可用性和稳定性。它逐步将新版本的 Pod 逐个替换旧版本的 Pod，直到整个 ReplicaSet 中所有的 Pod 都已经更新为新版本。这种方式可以确保在更新过程中不会导致应用程序的中断。

![image-20240425113805539](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291620812.png)



![image-20240425113831042](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291620878.png)



![image-20240425113843822](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291620136.png)

可以使用--record记录此次的变更记录

```shell
kubectl set image deployment/nginx-deployment nginx=nginx:1.16.1 --record
```



## yaml修改的方式

更新 nginx Pod 以使用 `nginx:1.18.0` 镜像

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
  labels:
    app: nginx
spec:
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

![image-20240425133546121](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291620944.png)

可以使用--record记录此次的变更记录

```shell
kubectl apply -f deployment.yaml --record
```

## 回滚

查看版本历史

```shell
kubectl rollout history deployment nginx-deployment
```

使用--record会将执行的命令记录在CHANGE_CAUSE

![image-20240425131026849](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621712.png)

当前版本nginx是1.18.0，回滚到version=1

```shell
kubectl rollout undo deployment nginx-deployment --to-revision=1
```

不指定--to-revision表示回滚到上个版本

![image-20240425133355603](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621617.png)

## deployment.spec相关参数

```shell
[root@k8s-master k8s]# kubectl explain deployment.spec
KIND:     Deployment
VERSION:  apps/v1

RESOURCE: spec <Object>

DESCRIPTION:
     Specification of the desired behavior of the Deployment.

     DeploymentSpec is the specification of the desired behavior of the
     Deployment.

FIELDS:
   minReadySeconds	<integer>
     Minimum number of seconds for which a newly created pod should be ready
     without any of its container crashing, for it to be considered available.
     Defaults to 0 (pod will be considered available as soon as it is ready)

   paused	<boolean>
     Indicates that the deployment is paused.

   progressDeadlineSeconds	<integer>
     The maximum time in seconds for a deployment to make progress before it is
     considered to be failed. The deployment controller will continue to process
     failed deployments and a condition with a ProgressDeadlineExceeded reason
     will be surfaced in the deployment status. Note that progress will not be
     estimated during the time a deployment is paused. Defaults to 600s.

   replicas	<integer>
     Number of desired pods. This is a pointer to distinguish between explicit
     zero and not specified. Defaults to 1.

   revisionHistoryLimit	<integer>
     The number of old ReplicaSets to retain to allow rollback. This is a
     pointer to distinguish between explicit zero and not specified. Defaults to
     10.

   selector	<Object> -required-
     Label selector for pods. Existing ReplicaSets whose pods are selected by
     this will be the ones affected by this deployment. It must match the pod
     template's labels.

   strategy	<Object>
     The deployment strategy to use to replace existing pods with new ones.

   template	<Object> -required-
     Template describes the pods that will be created.
```

### progressDeadlineSeconds

设置滚动更新的最长时间。在执行滚动更新时，Kubernetes 控制器会持续监视新Pod的部署进度。如果在 `progressDeadlineSeconds` 指定的时间内无法完成更新，控制器将停止滚动更新，并将其标记为失败。

### revisionHistoryLimit

revisionHistoryLimit 指定保留几个版本，默认为10

![image-20240425135602547](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621427.png)

可以根据需求进行修改

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
  labels:
    app: nginx
spec:
  replicas: 3
  revisionHistoryLimit: 15  #保留15个版本历史
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

### pause

暂停Deployment的滚动更新

```shell
kubectl rollout pause deployment nginx-deployment
```

在执行此命令后，新的 ReplicaSet 将不会被创建，但是现有的 ReplicaSet 和 Pod 将保持不变。只是变更了deployment。

![image-20240425143120029](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621190.png)

恢复滚动更新，让暂停期间的更新生效（最后一次的更新）

```shell
kubectl rollout resume deployment nginx-deployment
```

![image-20240425143247145](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621827.png)

yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
  labels:
    app: nginx
spec:
  paused: true #表示要暂停滚动更新
  replicas: 3
  revisionHistoryLimit: 15
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

![image-20240425144139006](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621985.png)

设置  paused: false  或删除，以恢复滚动更新，但是暂停期间的更新的image版本会被yaml中的覆盖

### minReadySeconds

在滚动更新过程中使用的，它指定了一个Pod变得“就绪”之前的最小时间（以秒为单位）。滚动更新期间，如果有足够数量的新Pod已经变得就绪，但是 `minReadySeconds` 指定的时间尚未到达，则旧的Pod将继续保持运行，直到新的Pod达到 `minReadySeconds` 指定的时间。

### strategy

用于指定更新策略。更新策略控制着 Deployment 对象如何执行滚动更新。它包括两种常见的策略：`Recreate` 和 `RollingUpdate`。

1. **Recreate**：在使用 `Recreate` 策略时，Deployment 会先将现有的 Pod 都删除，然后再创建新的 Pod。这意味着在更新期间可能会出现短暂的中断，因为 Deployment 在删除旧 Pod 之后才会创建新 Pod。
2. **RollingUpdate**：在使用 `RollingUpdate` 策略时，Deployment 会逐步更新现有的 Pod，而不会一次性删除所有旧 Pod。它会逐步创建新 Pod，并在新 Pod 就绪后逐步停止并删除旧 Pod。这样可以确保服务的可用性，因为在更新过程中总是有足够的 Pod 可以处理请求。

#### Recreate重新创建

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
  labels:
    app: nginx
spec:
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
          image: nginx:1.16.1
          ports:
            - containerPort: 80
  strategy:
    type: Recreate
```

先删除三个副本

![image-20240425150932794](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621550.png)

再创建三个新的副本

![image-20240425151011543](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621953.png)

#### 比例缩放（Proportional Scaling）

默认配置

![image-20240425154620656](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621526.png)



```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
  labels:
    app: nginx
spec:
  replicas: 5
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
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 2
```

当使用`maxUnavailable`和`maxSurge`参数时，我们需要考虑这两个参数共同决定了更新期间副本的状态和数量。

- `maxSurge` 控制了滚动更新过程中可以超过期望副本数的最大额外副本数。
- `maxUnavailable` 控制了在滚动更新过程中可以同时处于不可用状态的副本的最大数量。





![image-20240425161830585](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621727.png)

`maxUnavailable`被设置为2，这意味着在更新过程中，最多可以有两个副本同时处于不可用状态。因此，在任何时候，至少会有3个副本（期望的副本数5减去`maxUnavailable`的值2）保持可用状态。

`maxSurge` 可以是一个整数，表示额外的 Pod 数量，也可以是一个百分比字符串，表示相对于期望副本数的百分比。如果设置为1，保证新旧 Pod 的总数不超过6。（运行中和正在创建的副本数量，不包括正在终止的副本。）

当此值为 30% 时,保证新旧 Pod 的总数不超过所需 Pod 总数的 130%。

## HPA（动态扩缩容）

概念：https://kubernetes.io/zh-cn/docs/tasks/run-application/horizontal-pod-autoscale/#scaling-policies

实战：https://kubernetes.io/zh-cn/docs/tasks/run-application/horizontal-pod-autoscale-walkthrough/



水平自动扩展（Horizontal Pod Autoscaler，HPA）是 Kubernetes 中的一种机制，用于根据资源使用情况动态调整 Pod 的副本数量，以满足应用程序的需求。HPA会监视部署或副本控制器的CPU利用率或自定义指标，并根据定义的规则自动调整Pod的副本数量。

HorizontalPodAutoscaler 的常见用途是将其配置为从[聚合 API](https://kubernetes.io/zh-cn/docs/concepts/extend-kubernetes/api-extension/apiserver-aggregation/) （`metrics.k8s.io`、`custom.metrics.k8s.io` 或 `external.metrics.k8s.io`）获取指标。 `metrics.k8s.io` API 通常由名为 Metrics Server 的插件提供，需要单独启动。

### 安装Metrics Server 插件

https://github.com/kubernetes-sigs/metrics-server

![image-20240425173024957](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621494.png)

1下载metrics-server文件

需要修改一些配置

https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

2不验证 Kubelet 提供的服务证书的 CA。

![image-20240425173305758](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621103.png)

修改components.yaml配置

![image-20240425173446327](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621914.png)

3.修改镜像metrics-server的地址

如果registry.k8s.io/metrics-server/metrics-server访问不到的，修改为registry.cn-hangzhou.aliyuncs.com/dongguo/metrics-server:v0.7.1

![image-20240426100914766](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621304.png)

4.修改components.yaml为metrics-server.yaml

5.上传到服务器

![image-20240425174817796](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621313.png)

6.部署metrics-server

```shell
kubectl apply -f metrics-server.yaml
```

![image-20240426094610534](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621098.png)

查看创建的pod，metrics-server插件已经安装成功。

![image-20240426101244304](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621387.png)

7.使用Metrics API



1）查看集群中所有节点的资源利用情况，包括 CPU 和内存的使用量。

```
kubectl top nodes
```

![image-20240426101457654](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621175.png)

2）查看集群中所有Pod的资源利用情况，包括CPU和内存的使用量。

```shell
kubectl top pods
```

![image-20240426102709371](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621478.png)

8进入到kubernetes-dashboard：https://192.168.122.140:30908/

可以看到可视化工具中已经展示出CPU和MEMORY的使用情况

![image-20240426103707271](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291621698.png)



### 动态扩缩容实践

https://kubernetes.io/zh-cn/docs/tasks/run-application/horizontal-pod-autoscale-walkthrough/

![image-20240425172007097](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291622177.png)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: php-apache
spec:
  selector:
    matchLabels:
      run: php-apache
  template:
    metadata:
      labels:
        run: php-apache
    spec:
      containers:
      - name: php-apache
        image: registry.cn-hangzhou.aliyuncs.com/dongguo/php-hpa:latest
        ports:
        - containerPort: 80
        resources:
          limits:
            cpu: 500m
          requests:
            cpu: 200m
---
apiVersion: v1
kind: Service
metadata:
  name: php-apache
  labels:
    run: php-apache
spec:
  ports:
  - port: 80
  selector:
    run: php-apache
```

![image-20240426111401368](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291622970.png)



#### 创建 HorizontalPodAutoscaler 

现在服务器正在运行，使用 kubectl 创建自动扩缩器。 kubectl autoscale 子命令是 kubectl 的一部分， 可以帮助你执行此操作。

创建 HorizontalPodAutoscaler：

当 php-apache 应用的 CPU 利用率超过 50% 时，自动扩展相关 Pod 的数量，以确保性能和可用性，并且在不超过 10 个 Pod 的情况下保持 Pod 的数量。

```shell
kubectl autoscale deployment php-apache --cpu-percent=50 --min=1 --max=10
```

当然也可以使用yaml的形式，通过命令得到的yaml

```yaml
apiVersion: autoscaling/v1
kind: HorizontalPodAutoscaler
metadata:
  creationTimestamp: null
  name: php-apache
spec:
  maxReplicas: 10
  minReplicas: 1
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: php-apache
  targetCPUUtilizationPercentage: 50
```

![image-20240426111442835](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291622409.png)

你可以通过运行以下命令检查新制作的 HorizontalPodAutoscaler 的当前状态：

```
kubectl get hpa
```

![image-20240426133735742](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291622657.png)

请注意当前的 CPU 利用率是 0%，这是由于我们尚未发送任何请求到服务器

#### 增加负载 

在测试扩缩容之前先持续监测pod的变化

监测标签run=php-apache的pod

```shell
watch -n 1 kubectl get pod -l run=php-apache
```

初始状态为

![image-20240426134032859](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291623045.png)

或者使用监测随着TARGETS的增加REPLICAS的变化

```shell
watch -n 1 kubectl get hpa php-apache
```

![image-20240426134754010](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291623397.png)

我们启动一个不同的 Pod 作为客户端。 客户端 Pod 中的容器在无限循环中运行，向 php-apache 服务发送查询。

```shell
kubectl run -i --tty load-generator --rm --image=busybox:1.28 --restart=Never -- /bin/sh -c "while sleep 0.01; do wget -q -O- http://php-apache; done"
```

![image-20240426135032741](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291623225.png)

当CPU的利用率超过50%开始扩容pod

![image-20240426135053686](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291623009.png)

随着pod的增加，CPU的利用率慢慢的降低，并趋于稳定，可以看到已经存在了6个副本

![image-20240426135143752](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291623630.png)

请求一段时间后，取消对http://php-apache的请求，输入 `<Ctrl> + C` 来终止负载的产生。

![image-20240426135247868](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291622369.png)

CPU的利用率慢慢的回落到0%

![image-20240426135555000](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291623213.png)

当CPU回落到0% 时，并不会立刻进行缩容，为了排除网络抖动的影响，自动扩缩完成副本数量的改变可能需要几分钟的时间。（大概五分钟左右）

可以看到副本开始缩容

![image-20240426135755422](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291622750.png)

最终HPA 会自动将副本数缩减为 1。

![image-20240426135821713](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291622432.png)



在 `autoscaling/v2` API 版本中，HorizontalPodAutoscaler（HPA）提供了更灵活的选项，可以使用除了 CPU 利用率之外的其他度量指标来自动扩展或收缩相关 Pod 的数量。这些指标可以是任何 Kubernetes 支持的指标类型，例如内存利用率、请求次数、自定义指标等。

通过 `metrics` 字段，你可以指定要用于自动扩缩的度量指标。以下是一个示例配置：

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: php-apache
spec:
  maxReplicas: 10
  minReplicas: 1
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: php-apache
  metrics:
  - type: Resource
    resource:
      name: memory
      targetAverageUtilization: 70
```

在这个示例中，我们使用了 `autoscaling/v2` API 版本的 HPA。`metrics` 字段指定了要使用的度量指标，类型为 `Resource`，表示资源利用率。`resource` 字段指定了要监视的资源类型，这里是内存（`name: memory`），并设置了目标平均利用率为 70%（`targetAverageUtilization: 70`）。

这样，当 PHP Apache 应用的内存利用率超过 70% 时，HPA 将触发自动扩展相关 Pod 的数量，以维持应用的性能和可用性。

# Canary（金丝雀部署）

## 蓝绿部署VS金丝雀部署

蓝绿部署（Blue-Green Deployment）用于无缝地将新版本的应用程序部署到生产环境，同时最大程度地减少对用户的影响。它的基本原理是在两个完全独立的生产环境中交替部署新旧版本的应用程序，使得在切换过程中不会出现任何中断或服务不可用的情况。以下是蓝绿部署的一般流程：

1. **初始部署**：
   - 初始时，生产环境中运行着旧版本的应用程序，称为蓝环境。
2. **部署新版本**：
   - 在蓝环境之外，创建一个全新的环境，用于部署新版本的应用程序，称为绿环境。
3. **验证新版本**：
   - 在绿环境中进行各种测试，确保新版本的稳定性和可靠性。
4. **切换流量**：
   - 当新版本通过了所有测试并且准备好接收用户流量时，可以一次性将流量从蓝环境切换到绿环境。
   - 这样，所有用户的流量都将被引导到新版本，完成部署更新。
5. **清理**：
   - 完成切换后，可以将旧版本的应用程序从蓝环境中移除，并清理掉不再需要的资源。


金丝雀部署（Canary Deployment）用于逐步测试新版本的应用程序或服务，以确保其在生产环境中的稳定性和可靠性，同时最小化对用户的影响。金丝雀部署的基本原理是在生产环境中逐渐引入新版本，只将一小部分流量引导到新版本，然后逐步增加流量比例，观察新版本的性能和稳定性，以便及时发现并解决潜在的问题。以下是金丝雀部署的一般流程：

1. **初始部署**：

   - 初始时，生产环境中运行着旧版本的应用程序。

2. **部署新版本**：

   - 在生产环境中部署新版本的应用程序，但只在一小部分节点上进行部署，而不是所有节点。

3. **逐步增加节点**：

   - 逐步将新版本的应用程序部署到更多的节点上，增加其接收流量的比例。这可以通过自动化的方式进行，例如逐步增加新版本的部署副本数或者逐步将节点添加到负载均衡器中。

4. **监控和观察**：

   - 在逐步增加节点的过程中，持续监控新版本的应用程序，包括性能、稳定性、错误率等指标。
   - 如果发现任何问题，可以立即停止部署新版本，并回滚到旧版本，以减少对用户的影响。

5. **完全部署**：

   - 当确定新版本的应用程序在所有节点上都表现良好，并且通过了所有测试和观察之后，可以完全部署新版本。
   - 这意味着所有节点都在运行新版本的应用程序，并且接收全部流量。

6. **回滚和处理异常**：

   - 如果在金丝雀部署过程中发现了任何问题或异常，可以立即回滚到旧版本，以减少对用户的影响。

   - 同时，对于发现的问题，应该及时调查、诊断和解决，确保下次部署不再出现相同的问题。


红黑部署（Red-Black Deployment），也称为滚动更新、替代部署或平滑部署，是一种部署策略，旨在实现无缝的应用程序更新，同时保证高可用性和稳定性。红黑部署的核心思想是在生产环境中创建一个与当前版本完全相同的新环境，然后将新版本的应用程序部署到这个新环境中，通过逐步切换流量来实现更新。以下是红黑部署的一般流程：

1. **初始部署**：
   - 初始时，生产环境中运行着旧版本的应用程序，称为红环境。
   - 新版本的应用程序在一个单独的环境中进行部署，称为黑环境，==与红环境完全相同。==
2. **部署新版本**：
   - 将新版本的应用程序部署到黑环境中，保证它与旧版本的应用程序在功能和配置上完全一致。
   - 在部署之前，可以进行一些预发布测试，以确保新版本的稳定性和可靠性。
3. **切换流量**：
   - 当新版本的应用程序在黑环境中部署完成，并且通过了预发布测试之后，可以逐步将流量从红环境切换到黑环境。
   - 这可以通过负载均衡器、路由器或其他流量控制机制来实现，逐渐增加黑环境的流量比例，同时逐渐减少红环境的流量比例。
4. **观察和监控**：
   - 在切换流量的过程中，持续观察和监控新版本的应用程序，包括性能、稳定性、错误率等指标。
   - 如果发现了任何异常行为或问题，可以立即回滚到旧版本，以减少对用户的影响。
5. **完全切换**：
   - 当确定新版本的应用程序在黑环境中表现良好，并且所有流量都已成功切换到黑环境时，可以将红环境中的应用程序停止，完成部署更新。
6. **清理**：
   - 完成部署更新后，可以清理掉不再需要的资源，如红环境中的旧版本应用程序和相关的资源。

## 金丝雀的简单测试(基于Deployment)

1）**创建 版本v1的Deployment**

首先，我们创建一个名为 `canary-deployment-v1` 的 Deployment，部署v1版本的应用程序。

spec.selector.matchLabels.app:canary-nginx 只有具有标签 `app: canary-nginx` 的 Pod 才会受到该 Deployment 的管理

spec.template.metadata.labels.app: canary-nginx  Pod 的标签，与 Deployment 的选择器、Service 的选择器相匹配

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: canary-deployment-v1
  namespace: default
spec:
  replicas: 3
  selector:
    matchLabels:
      app: canary-nginx
      version: v1.0
  template:
    metadata:
      labels:
        app: canary-nginx
        version: v1.0
    spec:
      containers:
      - image: registry.cn-hangzhou.aliyuncs.com/dongguo/nginx-msg-test:v1.0
        imagePullPolicy: IfNotPresent
        name: nginx
```



镜像registry.cn-hangzhou.aliyuncs.com/dongguo/nginx-msg-test:v1.0的dockerfile,访问nginx会输出111111111

```dockerfile
From nginx:stable=alpine
ENV msg:"111111111"
RUN echo $msg > /usr/share/nginx/html/index.html
```

当前Deployment匹配标签 `app: canary-nginx` 和 `version: 1.0` 的pod

![image-20240426172603503](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291622847.png)

2）**创建 Service**

为了向外暴露 Deployment 中的 Pod，我们需要创建一个 Service。

Service 使用标签选择器（selector）来确定它将流量转发到哪些 Pod，而这些 Pod 的标签必须与 Deployment 中 Pod 模板的标签匹配。

该service将流量转发到标签为app: canary-nginx的pod

```yaml
apiVersion: v1
kind: Service
metadata:
  name: canary-service
  namespace: default
spec:
  ports:
  - name: canary-nginx
    nodePort: 32601
    port: 80
    protocol: TCP
    targetPort: 80
  selector:
    app: canary-nginx
  type: NodePort
```

![image-20240426172739752](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291622420.png)

访问192.168.122.140:32601

![image-20240426174627215](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291622834.png)

3）**创建 版本v2的Deployment**

registry.cn-hangzhou.aliyuncs.com/dongguo/nginx-msg-test:v2.0访问nginx展示正常的index.html

先部署一个副本

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: canary-deployment-v2
  namespace: default
spec:
  replicas: 1
  selector:
    matchLabels:
      app: canary-nginx
      version: v2.0
  template:
    metadata:
      labels:
        app: canary-nginx
        version: v2.0
    spec:
      containers:
      - image: registry.cn-hangzhou.aliyuncs.com/dongguo/nginx-msg-test:v2.0
        imagePullPolicy: IfNotPresent
        name: nginx   
```

![image-20240426172942124](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291622449.png)

测试访问192.168.122.140:32601

![image-20240426174611615](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291623664.png)

调整replicas：3

![image-20240426174715954](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291623824.png)

测试访问192.168.122.140:32601

```shell
[root@k8s-master k8s]# curl http://192.168.122.140:32601/
<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
<style>
    body {
        width: 35em;
        margin: 0 auto;
        font-family: Tahoma, Verdana, Arial, sans-serif;
    }
</style>
</head>
<body>
<h1>Welcome to nginx!</h1>
<p>If you see this page, the nginx web server is successfully installed and
working. Further configuration is required.</p>

<p>For online documentation and support please refer to
<a href="http://nginx.org/">nginx.org</a>.<br/>
Commercial support is available at
<a href="http://nginx.com/">nginx.com</a>.</p>

<p><em>Thank you for using nginx.</em></p>
</body>
</html>
[root@k8s-master k8s]# curl http://192.168.122.140:32601/
<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
<style>
    body {
        width: 35em;
        margin: 0 auto;
        font-family: Tahoma, Verdana, Arial, sans-serif;
    }
</style>
</head>
<body>
<h1>Welcome to nginx!</h1>
<p>If you see this page, the nginx web server is successfully installed and
working. Further configuration is required.</p>

<p>For online documentation and support please refer to
<a href="http://nginx.org/">nginx.org</a>.<br/>
Commercial support is available at
<a href="http://nginx.com/">nginx.com</a>.</p>

<p><em>Thank you for using nginx.</em></p>
</body>
</html>
[root@k8s-master k8s]# curl http://192.168.122.140:32601/
<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
<style>
    body {
        width: 35em;
        margin: 0 auto;
        font-family: Tahoma, Verdana, Arial, sans-serif;
    }
</style>
</head>
<body>
<h1>Welcome to nginx!</h1>
<p>If you see this page, the nginx web server is successfully installed and
working. Further configuration is required.</p>

<p>For online documentation and support please refer to
<a href="http://nginx.org/">nginx.org</a>.<br/>
Commercial support is available at
<a href="http://nginx.com/">nginx.com</a>.</p>

<p><em>Thank you for using nginx.</em></p>
</body>
</html>
[root@k8s-master k8s]# curl http://192.168.122.140:32601/
111111111
[root@k8s-master k8s]# curl http://192.168.122.140:32601/
111111111
[root@k8s-master k8s]# curl http://192.168.122.140:32601/
111111111
[root@k8s-master k8s]# curl http://192.168.122.140:32601/
```

**4）删除版本v1的Deployment**

如果v2版本运行正常，删除版本v1的Deployment

![image-20240426174911708](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404291623776.png)

# Deployment状态与排错

Deployment 的生命周期中会有许多状态。上线新的 ReplicaSet 期间可能处于 [Progressing（进行中）](https://kubernetes.io/zh-cn/docs/concepts/workloads/controllers/deployment/#progressing-deployment)，可能是 [Complete（已完成）](https://kubernetes.io/zh-cn/docs/concepts/workloads/controllers/deployment/#complete-deployment)，也可能是 [Failed（失败）](https://kubernetes.io/zh-cn/docs/concepts/workloads/controllers/deployment/#failed-deployment)以至于无法继续进行。



你的 Deployment 可能会在尝试部署其最新的 ReplicaSet 受挫，一直处于未完成状态。

kubectl describe 描述一个资源（Pod、Service、Node、Deployment....）来进行排错

Deployment 可能会出现瞬时性的错误，可能因为设置的超时时间过短， 也可能因为其他可认为是临时性的问题。例如，假定所遇到的问题是配额不足。 如果描述 Deployment，你将会注意到以下部分：

```shell
kubectl describe deployment nginx-deployment
```

输出类似于：

```shell
[root@k8s-master k8s]# kubectl describe deployment nginx-deployment
Name:                   nginx-deployment
Namespace:              default
CreationTimestamp:      Wed, 24 Apr 2024 16:32:25 +0800
Labels:                 app=nginx
Annotations:            deployment.kubernetes.io/revision: 2
Selector:               app=nginx
Replicas:               5 desired | 3 updated | 6 total | 3 available | 3 unavailable
StrategyType:           RollingUpdate
MinReadySeconds:        0
RollingUpdateStrategy:  2 max unavailable, 1 max surge
Pod Template:
  Labels:  app=nginx
  Containers:
   nginx:
    Image:        nginx:aaa
    Port:         80/TCP
    Host Port:    0/TCP
    Environment:  <none>
    Mounts:       <none>
  Volumes:        <none>
Conditions:
  Type           Status  Reason
  ----           ------  ------
  Available      True    MinimumReplicasAvailable
  Progressing    True    ReplicaSetUpdated
OldReplicaSets:  nginx-deployment-674c4cbb5f (3/3 replicas created)
NewReplicaSet:   nginx-deployment-5969c77776 (3/3 replicas created)
Events:
  Type    Reason             Age   From                   Message
  ----    ------             ----  ----                   -------
  Normal  ScalingReplicaSet  27s   deployment-controller  Scaled up replica set nginx-deployment-5969c77776 to 1
  Normal  ScalingReplicaSet  27s   deployment-controller  Scaled down replica set nginx-deployment-674c4cbb5f to 3
  Normal  ScalingReplicaSet  27s   deployment-controller  Scaled up replica set nginx-deployment-5969c77776 to 3

```

Conditions中总共有 6 个副本，其中 3 个可用，3 个不可用。

然后查看具体的pod存在的问题。
