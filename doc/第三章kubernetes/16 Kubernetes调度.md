# ResourceQuota资源配额

https://kubernetes.io/zh-cn/docs/concepts/policy/resource-quotas/

当多个用户或团队共享具有固定节点数目的集群时，人们会担心有人使用超过其基于公平原则所分配到的资源量。

资源配额是帮助管理员解决这一问题的工具。

资源配额，通过 `ResourceQuota` 对象来定义，对每个命名空间的资源消耗总量提供限制。 它可以限制命名空间中某种类型的对象的总数目上限，也可以限制命名空间中的 Pod 可以使用的计算资源的总上限。

资源配额的工作方式如下：

- 不同的团队可以在不同的命名空间下工作。这可以通过 [RBAC](https://kubernetes.io/zh-cn/docs/reference/access-authn-authz/rbac/) 强制执行。
- 集群管理员可以为每个命名空间创建一个或多个 ResourceQuota 对象。
- 当用户在命名空间下创建资源（如 Pod、Service 等）时，Kubernetes 的配额系统会跟踪集群的资源使用情况， 以确保使用的资源用量不超过 ResourceQuota 中定义的硬性资源限额。
- 如果资源创建或者更新请求违反了配额约束，那么该请求会报错（HTTP 403 FORBIDDEN）， 并在消息中给出有可能违反的约束。
- 如果命名空间下的计算资源 （如 `cpu` 和 `memory`）的配额被启用， 则用户必须为这些资源设定请求值（request）和约束值（limit），否则配额系统将拒绝 Pod 的创建。 提示: 可使用 `LimitRanger` 准入控制器来为没有设置计算资源需求的 Pod 设置默认值。

## 实战

https://kubernetes.io/zh-cn/docs/tasks/administer-cluster/manage-resources/quota-memory-cpu-namespace/

1.创建命名空间 

创建一个命名空间

```shell
kubectl create namespace quota-mem-cpu
```

![image-20240507151314711](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405081749345.png)

2.创建 ResourceQuota

mem-cpu.yaml

```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: mem-cpu
  namespace: quota-mem-cpu
spec:
  hard:
    requests.cpu: "1"
    requests.memory: 1Gi
    limits.cpu: "2"
    limits.memory: 2Gi
```

![image-20240507151925074](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405081750679.png)

ResourceQuota 在 quota-mem-cpu 命名空间中设置了如下要求：

- 在该命名空间中的每个 Pod 的所有容器都必须要有内存请求和限制，以及 CPU 请求和限制。
- 在该命名空间中所有 Pod 的内存请求总和不能超过 1 GiB。
- 在该命名空间中所有 Pod 的内存限制总和不能超过 2 GiB。
- 在该命名空间中所有 Pod 的 CPU 请求总和不能超过 1 cpu。
- 在该命名空间中所有 Pod 的 CPU 限制总和不能超过 2 cpu。



3.创建 Pod

quota-mem-cpu.yaml

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: quota-mem-cpu
  namespace: quota-mem-cpu  
spec:
  containers:
  - name: quota-mem-cpu-ctr
    image: nginx
    resources:
      limits:
        memory: "800Mi"
        cpu: "800m"
      requests:
        memory: "600Mi"
        cpu: "400m"
```

![image-20240507152028717](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405081750087.png)

输出结果显示了配额以及有多少配额已经被使用。可以看到 Pod 的内存和 CPU 请求值及限制值没有超过配额。

4.创建第二个 Pod

quota-mem-cpu-2.yaml

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: quota-mem-cpu-2
  namespace: quota-mem-cpu 
spec:
  containers:
  - name: quota-mem-cpu-ctr-2
    image: redis
    resources:
      limits:
        memory: "1Gi"
        cpu: "800m"
      requests:
        memory: "700Mi"
        cpu: "400m"
```

可以看到 Pod 的内存请求为 700 MiB。 请注意新的内存请求与已经使用的内存请求之和超过了内存请求的配额： 600 MiB + 700 MiB > 1 GiB。

第二个 Pod 不能被创建成功。输出结果显示创建第二个 Pod 会导致内存请求总量超过内存请求配额。

![image-20240507152418824](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131255448.png)

## 计算资源配额 

用户可以对给定命名空间下的可被请求的 [计算资源](https://kubernetes.io/zh-cn/docs/concepts/configuration/manage-resources-containers/) 总量进行限制。

配额机制所支持的资源类型：

| 资源名称           | 描述                                                         |
| ------------------ | ------------------------------------------------------------ |
| `limits.cpu`       | 所有非终止状态的 Pod，其 CPU 限额总量不能超过该值。          |
| `limits.memory`    | 所有非终止状态的 Pod，其内存限额总量不能超过该值。           |
| `requests.cpu`     | 所有非终止状态的 Pod，其 CPU 需求总量不能超过该值。          |
| `requests.memory`  | 所有非终止状态的 Pod，其内存需求总量不能超过该值。           |
| `hugepages-<size>` | 对于所有非终止状态的 Pod，针对指定尺寸的巨页请求总数不能超过此值。 |
| `cpu`              | 与 `requests.cpu` 相同。                                     |
| `memory`           | 与 `requests.memory` 相同。                                  |

## 存储资源配额 

用户可以对给定命名空间下的[存储资源](https://kubernetes.io/zh-cn/docs/concepts/storage/persistent-volumes/) 总量进行限制。

此外，还可以根据相关的存储类（Storage Class）来限制存储资源的消耗。

| 资源名称                                                     | 描述                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| `requests.storage`                                           | 所有 PVC，存储资源的需求总量不能超过该值。                   |
| `persistentvolumeclaims`                                     | 在该命名空间中所允许的 [PVC](https://kubernetes.io/zh-cn/docs/concepts/storage/persistent-volumes/#persistentvolumeclaims) 总量。 |
| `<storage-class-name>.storageclass.storage.k8s.io/requests.storage` | 在所有与 `<storage-class-name>` 相关的持久卷申领中，存储请求的总和不能超过该值。 |
| `<storage-class-name>.storageclass.storage.k8s.io/persistentvolumeclaims` | 在与 storage-class-name 相关的所有持久卷申领中，命名空间中可以存在的[持久卷申领](https://kubernetes.io/zh-cn/docs/concepts/storage/persistent-volumes/#persistentvolumeclaims)总数。 |

例如，如果一个操作人员针对 `gold` 存储类型与 `bronze` 存储类型设置配额， 操作人员可以定义如下配额：

- `gold.storageclass.storage.k8s.io/requests.storage: 500Gi`
- `bronze.storageclass.storage.k8s.io/requests.storage: 100Gi`

在 Kubernetes 1.8 版本中，本地临时存储的配额支持已经是 Alpha 功能：

| 资源名称                     | 描述                                                         |
| ---------------------------- | ------------------------------------------------------------ |
| `requests.ephemeral-storage` | 在命名空间的所有 Pod 中，本地临时存储请求的总和不能超过此值。 |
| `limits.ephemeral-storage`   | 在命名空间的所有 Pod 中，本地临时存储限制值的总和不能超过此值。 |
| `ephemeral-storage`          | 与 `requests.ephemeral-storage` 相同。                       |

## 对象数量配额

你可以使用以下语法对所有标准的、命名空间域的资源类型进行配额设置：

- `count/<resource>.<group>`：用于非核心（core）组的资源
- `count/<resource>`：用于核心组的资源

这是用户可能希望利用对象计数配额来管理的一组资源示例。

- `count/persistentvolumeclaims`
- `count/services`
- `count/secrets`
- `count/configmaps`
- `count/replicationcontrollers`
- `count/deployments.apps`
- `count/replicasets.apps`
- `count/statefulsets.apps`
- `count/jobs.batch`
- `count/cronjobs.batch`

相同语法也可用于自定义资源。 例如，要对 `example.com` API 组中的自定义资源 `widgets` 设置配额，请使用 `count/widgets.example.com`。

当使用 `count/*` 资源配额时，如果对象存在于服务器存储中，则会根据配额管理资源。 这些类型的配额有助于防止存储资源耗尽。例如，用户可能想根据服务器的存储能力来对服务器中 Secret 的数量进行配额限制。 集群中存在过多的 Secret 实际上会导致服务器和控制器无法启动。 用户可以选择对 Job 进行配额管理，以防止配置不当的 CronJob 在某命名空间中创建太多 Job 而导致集群拒绝服务。

对有限的一组资源上实施一般性的对象数量配额也是可能的。

支持以下类型：

| 资源名称                 | 描述                                                         |
| ------------------------ | ------------------------------------------------------------ |
| `configmaps`             | 在该命名空间中允许存在的 ConfigMap 总数上限。                |
| `persistentvolumeclaims` | 在该命名空间中允许存在的 [PVC](https://kubernetes.io/zh-cn/docs/concepts/storage/persistent-volumes/#persistentvolumeclaims) 的总数上限。 |
| `pods`                   | 在该命名空间中允许存在的非终止状态的 Pod 总数上限。Pod 终止状态等价于 Pod 的 `.status.phase in (Failed, Succeeded)` 为真。 |
| `replicationcontrollers` | 在该命名空间中允许存在的 ReplicationController 总数上限。    |
| `resourcequotas`         | 在该命名空间中允许存在的 ResourceQuota 总数上限。            |
| `services`               | 在该命名空间中允许存在的 Service 总数上限。                  |
| `services.loadbalancers` | 在该命名空间中允许存在的 LoadBalancer 类型的 Service 总数上限。 |
| `services.nodeports`     | 在该命名空间中允许存在的 NodePort 类型的 Service 总数上限。  |
| `secrets`                | 在该命名空间中允许存在的 Secret 总数上限。                   |

例如，`pods` 配额统计某个命名空间中所创建的、非终止状态的 `Pod` 个数并确保其不超过某上限值。 用户可能希望在某命名空间中设置 `pods` 配额，以避免有用户创建很多小的 Pod， 从而耗尽集群所能提供的 Pod IP 地址。

mem-cpu-2.yaml 设置pod不超过1

```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: mem-cpu-2
  namespace: quota-mem-cpu
spec:
  hard:
    count/pods: 1
```

![image-20240507160449013](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256407.png)

在创建一个pod超过上限，提示exceeded quota: mem-cpu-2, requested: count/pods=1, used: count/pods=1, limited: count/pods=1

quota-mem-cpu-3.yaml

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: quota-mem-cpu-3
  namespace: quota-mem-cpu 
spec:
  containers:
  - name: quota-mem-cpu-ctr-3
    image: redis
    resources:
      limits:
        memory: "100Mi"
        cpu: "80m"
      requests:
        memory: "70Mi"
        cpu: "40m"
```

![image-20240507160744929](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131255525.png)

## 优先级

Pod 可以创建为特定的[优先级](https://kubernetes.io/zh-cn/docs/concepts/scheduling-eviction/pod-priority-preemption/#pod-priority)。 通过使用配额规约中的 `scopeSelector` 字段，用户可以根据 Pod 的优先级控制其系统资源消耗。

仅当配额规范中的 `scopeSelector` 字段选择到某 Pod 时，配额机制才会匹配和计量 Pod 的资源消耗。

如果配额对象通过 `scopeSelector` 字段设置其作用域为优先级类， 则配额对象只能跟踪以下资源：

- `pods`
- `cpu`
- `memory`
- `ephemeral-storage`
- `limits.cpu`
- `limits.memory`
- `limits.ephemeral-storage`
- `requests.cpu`
- `requests.memory`
- `requests.ephemeral-storage`

本示例创建一个配额对象，并将其与具有特定优先级的 Pod 进行匹配。 该示例的工作方式如下：

- 集群中的 Pod 可取三个优先级类之一，即 "low"、"medium"、"high"。
- 为每个优先级创建一个配额对象。

将以下 YAML 保存到文件 `quota.yml` 中。

```yaml
apiVersion: v1
kind: List #集合
items:
- apiVersion: v1
  kind: ResourceQuota
  metadata:
    name: pods-high
  spec:
    hard:
      cpu: "1000"
      memory: 200Gi
      pods: "10"
    scopeSelector:
      matchExpressions:
      - operator: In
        scopeName: PriorityClass
        values: ["high"]
- apiVersion: v1
  kind: ResourceQuota
  metadata:
    name: pods-medium
  spec:
    hard:
      cpu: "10"
      memory: 20Gi
      pods: "10"
    scopeSelector:
      matchExpressions:
      - operator: In
        scopeName: PriorityClass
        values: ["medium"]
- apiVersion: v1
  kind: ResourceQuota
  metadata:
    name: pods-low
  spec:
    hard:
      cpu: "5"
      memory: 10Gi
      pods: "10"
    scopeSelector:
      matchExpressions:
      - operator: In
        scopeName: PriorityClass
        values: ["low"]
```



![image-20240507162251035](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131255089.png)

创建这个 "low" 的 `PriorityClass`

low-priority.yaml

```yaml
apiVersion: scheduling.k8s.io/v1  
kind: PriorityClass  
metadata:  
  name: low  
value: 1000  
globalDefault: false  
description: "This priority class is for low priority pods."
```

![image-20240507165716608](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131255238.png)

low-priority-pod.yml创建优先级为 "low" 的 Pod

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: low-priority
spec:
  containers:
  - name: low-priority
    image: ubuntu
    command: ["/bin/sh"]
    args: ["-c", "while true; do echo hello; sleep 10;done"]
    resources:
      requests:
        memory: "10Gi"
        cpu: "500m"
      limits:
        memory: "10Gi"
        cpu: "500m"
  priorityClassName: low #pod使用low优先级的资源配额
```

![image-20240507165812669](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131255977.png)

确认 "low" 优先级配额 `pods-low` 的 "Used" 统计信息已更改，并且其他两个配额未更改。

# LimitRange限制范围

https://kubernetes.io/zh-cn/docs/concepts/policy/limit-range/

默认情况下， Kubernetes 集群上的容器运行使用的[计算资源](https://kubernetes.io/zh-cn/docs/concepts/configuration/manage-resources-containers/)没有限制。 使用 Kubernetes [资源配额](https://kubernetes.io/zh-cn/docs/concepts/policy/resource-quotas/)， 管理员（也称为 **集群操作者**）可以在一个指定的[命名空间](https://kubernetes.io/zh-cn/docs/concepts/overview/working-with-objects/namespaces/)内限制集群资源的使用与创建。 在命名空间中，一个 [Pod](https://kubernetes.io/zh-cn/docs/concepts/workloads/pods/) 最多能够使用命名空间的资源配额所定义的 CPU 和内存用量。 作为集群操作者或命名空间级的管理员，你可能也会担心如何确保一个 Pod 不会垄断命名空间内所有可用的资源。

LimitRange 是限制命名空间内可为每个适用的对象类别 （例如 Pod 或 [PersistentVolumeClaim](https://kubernetes.io/zh-cn/docs/concepts/storage/persistent-volumes/#persistentvolumeclaims)） 指定的资源分配量（限制和请求）的策略对象。

一个 **LimitRange（限制范围）** 对象提供的限制能够做到：

- 在一个命名空间中实施对每个 Pod 或 Container 最小和最大的资源使用量的限制。
- 在一个命名空间中实施对每个 [PersistentVolumeClaim](https://kubernetes.io/zh-cn/docs/concepts/storage/persistent-volumes/#persistentvolumeclaims) 能申请的最小和最大的存储空间大小的限制。
- 在一个命名空间中实施对一种资源的申请值和限制值的比值的控制。
- 设置一个命名空间中对计算资源的默认申请/限制值，并且自动的在运行时注入到多个 Container 中。

当某命名空间中有一个 LimitRange 对象时，将在该命名空间中实施 LimitRange 限制。

## 资源限制和请求的约束

- 管理员在一个命名空间内创建一个 `LimitRange` 对象。
- 用户在此命名空间内创建（或尝试创建） Pod 和 PersistentVolumeClaim 等对象。
- 首先，`LimitRange` 准入控制器对所有没有设置计算资源需求的所有 Pod（及其容器）设置默认请求值与限制值。
- 其次，`LimitRange` 跟踪其使用量以保证没有超出命名空间中存在的任意 `LimitRange` 所定义的最小、最大资源使用量以及使用量比值。
- 若尝试创建或更新的对象（Pod 和 PersistentVolumeClaim）违反了 `LimitRange` 的约束， 向 API 服务器的请求会失败，并返回 HTTP 状态码 `403 Forbidden` 以及描述哪一项约束被违反的消息。
- 若你在命名空间中添加 `LimitRange` 启用了对 `cpu` 和 `memory` 等计算相关资源的限制， 你必须指定这些值的请求使用量与限制使用量。否则，系统将会拒绝创建 Pod。
- `LimitRange` 的验证仅在 Pod 准入阶段进行，不对正在运行的 Pod 进行验证。 如果你添加或修改 LimitRange，命名空间中已存在的 Pod 将继续不变。
- 如果命名空间中存在两个或更多 `LimitRange` 对象，应用哪个默认值是不确定的。

## Pod 的 LimitRange 和准入检查

`LimitRange` **不** 检查所应用的默认值的一致性。 这意味着 `LimitRange` 设置的 **limit** 的默认值可能小于客户端提交给 API 服务器的规约中为容器指定的 **request** 值。 如果发生这种情况，最终 Pod 将无法调度。

problematic-limit-range.yaml

```yaml
apiVersion: v1
kind: LimitRange
metadata:
  name: cpu-resource-constraint
  namespace: hello
spec:
  limits:
  - default: # 此处定义默认限制值
      cpu: 500m
    defaultRequest: # 此处定义默认请求值
      cpu: 500m
    max: # max 和 min 定义限制范围
      cpu: "1"
    min:
      cpu: 100m
    type: Container
```

![image-20240507171739196](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256186.png)

声明 CPU 资源请求为 `700m` 但未声明限制值的 Pod：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: conflict-with-limitrange
  namespace: hello
spec:
  containers:
  - name: demo
    image: registry.k8s.io/pause:2.0
    resources:
      requests:
        cpu: 700m
```

那么该 Pod 将不会被调度，失败并出现类似以下的错误：

![image-20240507171821877](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256573.png)



如果你同时设置了 `request` 和 `limit`，那么即使使用相同的 `LimitRange`，新 Pod 也会被成功调度：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: conflict-with-limitrange
  namespace: hello
spec:
  containers:
  - name: demo
    image: registry.k8s.io/pause:2.0
    resources:
      requests:
        cpu: 700m
      limits:
        cpu: 700m
```

![image-20240507171851893](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256032.png)

# 调度原理

## nodeSelector

`nodeSelector` 是节点选择约束的最简单推荐形式。你可以将 `nodeSelector` 字段添加到 Pod 的规约中设置你希望目标节点所具有的[节点标签](https://kubernetes.io/zh-cn/docs/concepts/scheduling-eviction/assign-pod-node/#built-in-node-labels)。 Kubernetes 只会将 Pod 调度到拥有你所指定的每个标签的节点上。nodeSelector是PodSpec的一个字段。它包含键值对的映射。

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx
  labels:
    env: test
spec:
  containers:
  - name: nginx
    image: nginx
    imagePullPolicy: IfNotPresent
  nodeSelector:
    node-role: ingress #node节点对应的标签名
```

![image-20240507173540385](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256365.png)

可以使用nodeName指定调度到具体的那个节点

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx-k8s-node1
  labels:
    env: test
spec:
  nodeName: k8s-node1
  containers:
  - name: nginx
    image: nginx
    imagePullPolicy: IfNotPresent
```

![image-20240507173902779](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256806.png)



## Affinity and anti-affinity亲和性与反亲和性

`nodeSelector` 提供了一种最简单的方法来将 Pod 约束到具有特定标签的节点上。 亲和性和反亲和性扩展了你可以定义的约束类型。使用亲和性与反亲和性的一些好处有：

- 亲和性、反亲和性语言的表达能力更强。`nodeSelector` 只能选择拥有所有指定标签的节点。 亲和性、反亲和性为你提供对选择逻辑的更强控制能力。
- 你可以标明某规则是“软需求”或者“偏好”，这样调度器在无法找到匹配节点时仍然调度该 Pod。
- 你可以使用节点上（或其他拓扑域中）运行的其他 Pod 的标签来实施调度约束， 而不是只能使用节点本身的标签。这个能力让你能够定义规则允许哪些 Pod 可以被放置在一起。

亲和性功能由两种类型的亲和性组成：

- **节点亲和性**功能类似于 `nodeSelector` 字段，但它的表达能力更强，并且允许你指定软规则。
- Pod 间亲和性/反亲和性允许你根据其他 Pod 的标签来约束 Pod。

```shell
[root@k8s-master rq]# kubectl explain pod.spec.affinity
KIND:     Pod
VERSION:  v1

RESOURCE: affinity <Object>

DESCRIPTION:
     If specified, the pod's scheduling constraints

     Affinity is a group of affinity scheduling rules.

FIELDS:
   nodeAffinity	<Object> #节点亲和性,指定节点
     Describes node affinity scheduling rules for the pod.

   podAffinity	<Object> #pod亲和性，指定pod
     Describes pod affinity scheduling rules (e.g. co-locate this pod in the
     same node, zone, etc. as some other pod(s)).

   podAntiAffinity	<Object> #pod反亲和性
     Describes pod anti-affinity scheduling rules (e.g. avoid putting this pod
     in the same node, zone, etc. as some other pod(s)).
```



### Node Affinity节点亲和性

nodeSelector的升级版。

节点亲和性概念上类似于 `nodeSelector`， 它使你可以根据节点上的标签来约束 Pod 可以调度到哪些节点上。 节点亲和性有两种：

- `requiredDuringSchedulingIgnoredDuringExecution`： 调度器只有在规则被满足的时候才能执行调度。此功能类似于 `nodeSelector`， 但其语法表达能力更强。
- `preferredDuringSchedulingIgnoredDuringExecution`： 调度器会尝试寻找满足对应规则的节点。如果找不到匹配的节点，调度器仍然会调度该 Pod。



with-node-affinity.yaml

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: with-node-affinity
spec:
  affinity:
    nodeAffinity:
      requiredDuringSchedulingIgnoredDuringExecution: #硬性过滤 排除不具备指定label的node
        nodeSelectorTerms:
        - matchExpressions: #满足以下条件
          - key: disktype
            operator: In #你可以使用 In、NotIn、Exists、DoesNotExist、Gt 和 Lt 之一作为操作符。
            values:
            - ssd
            - hdd
      preferredDuringSchedulingIgnoredDuringExecution: #软性评分 不具备指定label的node打低分，降低node被选中的几率
      - preference:
          matchExpressions:
          - key: disktype
            operator: In
            values:
            - ssd
  containers:
  - name: with-node-affinity
    image: busybox
    command: ["sleep", "3600"]
```

- 节点**必须**包含一个键名为 `disktype` 的标签， 并且该标签的取值**必须**为 `ssd` 或 `hdd`。
- 节点**最好**具有一个键名为 `disktype` 且取值为 `ssd` 的标签。

你可以使用 `operator` 字段来为 Kubernetes 设置在解释规则时要使用的逻辑操作符。 你可以使用 `In`、`NotIn`、`Exists`、`DoesNotExist`、`Gt` 和 `Lt` 之一作为操作符。

如果你在与 `nodeSelectorTerms` 中的条件相关联的单个 `matchExpressions` 字段中指定多个表达式， 则只有当所有表达式都满足（各表达式按逻辑与操作组合）时，Pod 才能被调度到节点上。

![image-20240508095019367](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256070.png)

由于节点没有存在标签disktype=ssd或disktype=hdd的，Pod 的调度失败了。所以pod现在处于pending

![image-20240508095116581](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256414.png)

警告指出两个问题

1.node-role.kubernetes.io/master: Master 节点不允许普通 Pod 被调度到上面运行

当节点上存在某个污点（taint）时，具有匹配容忍设置的 Pod 将允许被调度到该节点上。

2.两个node节点都不符合 Pod 定义的节点亲和性或选择器条件，因此 Pod 无法被调度到这两个节点上。

为k8s-node2节点打上disktype=ssd标签，使with-node-affinity这个pod能够调度到k8s-nodes2

![image-20240508101503706](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256692.png)

pod已经被调度到k8s-node2节点上，现在取消disktype=ssd标签，也不影响pod的运行

![image-20240508101606138](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256023.png)

#### 节点亲和性权重

你可以为 `preferredDuringSchedulingIgnoredDuringExecution` 亲和性类型的每个实例设置 `weight` 字段，其取值范围是 1 到 100。 当调度器找到能够满足 Pod 的其他调度请求的节点时，调度器会遍历节点满足的所有的偏好性规则， 并将对应表达式的 `weight` 值加和。

最终的加和值会添加到该节点的其他优先级函数的评分之上。 在调度器为 Pod 作出调度决定时，总分最高的节点的优先级也最高。

with-node-affinity2.yaml

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: with-node-affinity2
spec:
  affinity:
    nodeAffinity:
      preferredDuringSchedulingIgnoredDuringExecution: #软性评分 不具备指定label的node打低分，降低node被选中的几率
      - preference:
          matchExpressions:
          - key: disktype
            operator: In
            values:
            - ssd
        weight: 80
      - preference:        
          matchExpressions:
          - key: disktype
            operator: In
            values:
            - hdd
        weight: 10
  containers:
  - name: with-node-affinity
    image: busybox
    command: ["sleep", "3600"]
```



为k8s-node1节点打上disktype=ssd标签，为k8s-node2节点打上disktype=hdd标签

![image-20240508102912781](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256482.png)

如果存在两个候选节点，都满足 `preferredDuringSchedulingIgnoredDuringExecution` 规则，调度器会考察各个节点的 `weight` 取值，并将该权重值添加到节点的其他得分值之上，k8s-node1权重高，pod被调度到k8s-node1节点

![image-20240508103114038](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256558.png)

### podAffinity和podAntiAffinity

Pod 间亲和性与反亲和性使你可以基于已经在节点上运行的 **Pod** 的标签来约束 Pod 可以调度到的节点，而不是基于节点上的标签。

此类规则的表现形式是：

- 当X已经运行了一个或者多个满足规则Y的Pod时，待调度的Pod应该-亲和性（或者不应该-反亲和性）在X上运行

​	规则Y以LabelSelector的形式表述，附带一个可选的名称空间列表。

​	与节点不一样，pod是在名称空间中（因此pod的标签是在名称空间中的），针对pod的LabelSelector必须同时指定对应的名称空间。

​    X是一个拓扑域的概念，例如节点、机柜、云供应商可用区、云供应商地域等。X以topologyKey的形式表达，该key代表了节点上代表拓扑域（topology domain）的一个标签。

Pod亲和性和反亲和性结合高级别控制器（如ReplicaSet、 StatefulSet、 Deployment等）一起使用时，可以非常实用。此时可以很容易的将一组工作复杂调度到同一个topology，例如调度到同一个节点。



示例：在一个多节点的集群中，部署一个使用redis的web应用程序，并期望web-server尽可能域redis在同一个节点上。

redis-cache：

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis-cache
spec:
  selector:
    matchLabels:
      app: store
  replicas: 2
  template:
    metadata:
      labels:
        app: store
    spec:
      affinity:
        podAntiAffinity: # 符合标准的不会被调度
          requiredDuringSchedulingIgnoredDuringExecution: # 硬性指标
          - labelSelector:
              matchExpressions:
              - key: app
                operator: In
                values:
                - store
            topologyKey: "kubernetes.io/hostname"  # 拓扑键 可以理解为分组划分逻辑区域
      containers:
      - name: redis-server
        image: redis:3.2-alpine
```

- `podAntiAffinity` 用于定义反亲和性规则，指定了禁止将具有相同标签的 Pod 调度到同一个节点上。只有具有 `app: store` 标签的 Pod 才会受到这个反亲和性规则的影响
- topologyKey: "kubernetes.io/hostname"          Kubernetes 将根据节点的主机名来确定节点的拓扑域。

最终保证具有相同标签的 Pod 将不会被调度到具有相同主机名的节点上，以确保它们在不同的拓扑域中运行。副本上设置了标签 `app=store`。 `podAntiAffinity` 规则告诉调度器避免将多个带有 `app=store` 标签的副本部署到同一节点上。 因此，每个独立节点上会创建一个缓存实例。

![image-20240508114527923](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256736.png)





web-server：

Pod 亲和性规则告诉调度器将每个副本放到存在标签为 `app=store` 的 Pod 的节点上。 Pod 反亲和性规则告诉调度器决不要在单个节点上放置多个 `app=web-store` 服务器。

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: web-server
spec:
  selector:
    matchLabels:
      app: web-store
  replicas: 2
  template:
    metadata:
      labels:
        app: web-store
    spec:
      affinity:
        podAntiAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
          - labelSelector:
              matchExpressions:
              - key: app
                operator: In
                values:
                - web-store
            topologyKey: "kubernetes.io/hostname"
        podAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
          - labelSelector:
              matchExpressions:
              - key: app
                operator: In
                values:
                - store
            topologyKey: "kubernetes.io/hostname"
      containers:
      - name: web-app
        image: nginx:1.16-alpine
```



![image-20240508114632678](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256333.png)

最终效果

|    k8s-node1    |    k8s-node2    |
| :-------------: | :-------------: |
| *web-server-1*  | *web-server-2*  |
| *redis-cache-1* | *redis-cache-2* |

#### 区域反亲和策略

k8s-node1、k8s-node2都存在node-role=ingress标签，通过标签进行区域划分

![image-20240508114714365](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256686.png)



```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis-cache2
spec:
  selector:
    matchLabels:
      app: store
  replicas: 2
  template:
    metadata:
      labels:
        app: store
    spec:
      affinity:
        podAntiAffinity: # 符合标准的不会被调度
          requiredDuringSchedulingIgnoredDuringExecution: # 硬性指标
          - labelSelector:
              matchExpressions:
              - key: app
                operator: In
                values:
                - store2
            topologyKey: "node-role"  # 拓扑键 可以理解为分组划分逻辑区域
      containers:
      - name: redis-server
        image: redis:3.2-alpine
```



由于k8s-node1、k8s-node2在同一个拓扑域中，所以两个节点只能创建一个pod另一个pod始终无法创建。

![image-20240508115150554](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256118.png)

## 污点与容忍

https://kubernetes.io/zh-cn/docs/concepts/scheduling-eviction/taint-and-toleration/

[节点亲和性](https://kubernetes.io/zh-cn/docs/concepts/scheduling-eviction/assign-pod-node/#affinity-and-anti-affinity) 是 [Pod](https://kubernetes.io/zh-cn/docs/concepts/workloads/pods/) 的一种属性，它使 Pod 被吸引到一类特定的[节点](https://kubernetes.io/zh-cn/docs/concepts/architecture/nodes/) （这可能出于一种偏好，也可能是硬性要求）。 **污点（Taint）** 则相反——它使节点能够排斥一类特定的 Pod。

**容忍度（Toleration）** 是应用于 Pod 上的。容忍度允许调度器调度带有对应污点的 Pod。 容忍度允许调度但并不保证调度：作为其功能的一部分， 调度器也会[评估其他参数](https://kubernetes.io/zh-cn/docs/concepts/scheduling-eviction/pod-priority-preemption/)。

污点和容忍度（Toleration）相互配合，可以用来避免 Pod 被分配到不合适的节点上。 每个节点上都可以应用一个或多个污点，这表示对于那些不能容忍这些污点的 Pod， 是不会被该节点接受的。

### master默认有污点

Kubernetes 在默认情况下会避免将 Pod 调度到 Master 节点上。这是为了确保 Master 节点的稳定性和安全性，因为 Master 节点通常运行着关键的控制平面组件，如 API Server、Controller Manager、Scheduler 等，如果其他工作负载运行在 Master 节点上，可能会对这些组件的正常运行造成影响。

为了实现这个策略，Kubernetes 通常会在 Master 节点上添加一个 Taint（污点），标记为 `node-role.kubernetes.io/master:NoSchedule`，这意味着除非 Pod 显式容忍这个 Taint，否则不会被调度到 Master 节点上。

![image-20240508140325374](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256169.png)

### 向节点添加污点

污点写法k=v:effect，v可以省略

为 k8s-node1节点添加一个污点，污点是一个键值对，污点的键为haha，值为hehe，污点效果为NoSchedule。意味着不会向节点调度任何pod，除非pod有一个匹配的容忍（toleration）

```shell
kubectl taint nodes k8s-node1 haha=hehe:NoSchedule
```

![image-20240508143701677](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256165.png)

 k8s-node1/ k8s-node节点都添加了污点，pod将不会调度到这两个节点

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-taint
spec:
  containers:
  - name: nginx
    image: nginx
```

![image-20240508145520572](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256204.png)

pod一直处于 pending，三个节点，master有污点node-role.kubernetes.io/master，两个node节点有污点haha: hehe。pod无法容忍这些污点，无法被调度到这些节点。

![image-20240508145742856](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256662.png)

移除污点,移除k8s-node2节点中key为haha的所有污点

```shell
kubectl taint nodes k8s-node2 haha-
```

pod调度到k8s-node2节点

![image-20240508150303204](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131256957.png)



支持的污点效果effect有：

- `NoExecute`

  这会影响已在节点上运行的 Pod，具体影响如下：如果 Pod 不能容忍这类污点，会马上被驱逐。如果 Pod 能够容忍这类污点，但是在容忍度定义中没有指定 `tolerationSeconds`， 则 Pod 还会一直在这个节点上运行。如果 Pod 能够容忍这类污点，而且指定了 `tolerationSeconds`， 则 Pod 还能在这个节点上继续运行这个指定的时间长度。 这段时间过去后，节点生命周期控制器从节点驱除这些 Pod。

- `NoSchedule`

  除非具有匹配的容忍度规约，否则新的 Pod 不会被调度到带有污点的节点上。 当前正在节点上运行的 Pod **不会**被驱逐。

- `PreferNoSchedule`

  `PreferNoSchedule` 是“偏好”或“软性”的 `NoSchedule`。 控制平面将**尝试**避免将不能容忍污点的 Pod 调度到的节点上，但不能保证完全避免。

### 向pod添加容忍

可以在 Pod 规约中为 Pod 设置容忍度,`operator` 的默认值是 `Equal`。

一个容忍度和一个污点相“匹配”是指它们有一样的键名和效果，并且：

- 如果 `operator` 是 `Exists`（此时容忍度不能指定 `value`），或者
- 如果 `operator` 是 `Equal`，则它们的值应该相等。

PodSpec中有一个tolerations字段，可用于向Pod添加容忍。下面的两个例子中定义的容忍都可以匹配上面例子中的污点，包含这些容忍的Pod也都可以被调度到k8s-node1节点上

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-taint2
spec:
  containers:
  - name: nginx
    image: nginx
  tolerations: ## 容忍
  - key: "haha"
    operator: "Equal"
    value: "hehe"
    effect: "NoSchedule"  ## 无调度效果
```

![image-20240508151841679](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257273.png)



```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-taint3
spec:
  containers:
  - name: nginx
    image: nginx
  tolerations: ## 容忍
  - key: "haha"
    operator: "Exists"
    effect: "NoSchedule"
```

![image-20240508164034586](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257424.png)



一个节点上可以有多个污点,同时一个Pod上可以有多个容忍。Kubernetes使用一种类似于过滤器的方法来处理多个节点和容忍:

对于节点的所有污点，检查 Pod 上是否有匹配的容忍，如果存在匹配的容忍，则忽略该污点；

剩下的不可忽略的污点将对该 Pod 起作用



例如：

如果存在至少一个不可忽略的污点带有效果 NoSchedule，则Kubernetes 不会将 Pod 调度到该节点上

如果没有不可忽略的污点带有效果 NoSchedule，但是至少存在一个不可忽略的污点带有效果PreferNoSchedule，则 Kubernetes 将尽量避免将该 Pod 调度到此节点

如果存在至少一个忽略的污点带有效果NoExecute，则：

​	假设 Pod已经在该节点上运行, Kubernetes 将从该节点上驱逐(evict)该Pod

​	假设Pod尚未在该节点上运行, Kubernetes将不会把Pod调度到该节点

给一个节点添加了如下污点：

```shell
kubectl taint nodes k8s-node2 key1=value1:NoSchedule
kubectl taint nodes k8s-node2 key1=value1:NoExecute
kubectl taint nodes k8s-node2 key2=value2:NoSchedule
```

![image-20240508165102138](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257408.png)

假定某个 Pod 有两个容忍度：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-taint4
spec:
  containers:
  - name: nginx
    image: nginx
  tolerations: ## 容忍
  - key: "key1"
    operator: "Equal"
    value: "value1"
    effect: "NoSchedule"
  - key: "key1"
    operator: "Equal"
    value: "value1"
    effect: "NoExecute"  
```

上述 Pod 不会被调度到上述节点，因为其没有容忍度和第三个污点相匹配。

![image-20240508165216439](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257681.png)



通常情况下，如果给一个节点添加了一个 effect 值为 `NoExecute` 的污点， 则任何不能容忍这个污点的 Pod 都会马上被驱逐，任何可以容忍这个污点的 Pod 都不会被驱逐。 但是，如果 Pod 存在一个 effect 值为 `NoExecute` 的容忍度指定了可选属性 `tolerationSeconds` 的值，则表示在给节点添加了上述污点之后， Pod 还能继续在节点上运行的时间。例如，

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-taint5
spec:
  containers:
  - name: nginx
    image: nginx
  tolerations: ## 容忍
  - key: "key1"
    operator: "Equal"
    value: "value1"
    effect: "NoExecute"
    tolerationSeconds: 60
```

先删除k8s-ndoe1d的污点

![image-20240508173505120](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257133.png)



![image-20240508173813801](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257153.png)

添加污点

```shell
kubectl taint nodes k8s-node1 key1=value1:NoExecute
```

如果这个 Pod 正在运行，同时一个匹配的污点被添加到其所在的节点， 那么 Pod 还将继续在节点上运行 60 秒，然后被驱逐。

![image-20240508174049813](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257200.png)

### 基于污点的驱逐

当某种条件为真时，节点控制器会自动给节点添加一个污点。当前内置的污点包括：

- `node.kubernetes.io/not-ready`：节点未准备好。这相当于节点状况 `Ready` 的值为 "`False`"。
- `node.kubernetes.io/unreachable`：节点控制器访问不到节点. 这相当于节点状况 `Ready` 的值为 "`Unknown`"。
- `node.kubernetes.io/memory-pressure`：节点存在内存压力。
- `node.kubernetes.io/disk-pressure`：节点存在磁盘压力。
- `node.kubernetes.io/pid-pressure`：节点的 PID 压力。
- `node.kubernetes.io/network-unavailable`：节点网络不可用。
- `node.kubernetes.io/unschedulable`：节点不可调度。
- `node.cloudprovider.kubernetes.io/uninitialized`：如果 kubelet 启动时指定了一个“外部”云平台驱动， 它将给当前节点添加一个污点将其标志为不可用。在 cloud-controller-manager 的一个控制器初始化这个节点后，kubelet 将删除这个污点。

在节点被排空时，节点控制器或者 kubelet 会添加带有 `NoExecute` 效果的相关污点。 此效果被默认添加到 `node.kubernetes.io/not-ready` 和 `node.kubernetes.io/unreachable` 污点中。 如果异常状态恢复正常，kubelet 或节点控制器能够移除相关的污点。

在某些情况下，当节点不可达时，API 服务器无法与节点上的 kubelet 进行通信。 在与 API 服务器的通信被重新建立之前，删除 Pod 的决定无法传递到 kubelet。 同时，被调度进行删除的那些 Pod 可能会继续运行在分区后的节点上。



你可以为 Pod 设置 `tolerationSeconds`，以指定当节点失效或者不响应时， Pod 维系与该节点间绑定关系的时长。

比如，你可能希望在出现网络分裂事件时，对于一个与节点本地状态有着深度绑定的应用而言， 仍然停留在当前节点上运行一段较长的时间，以等待网络恢复以避免被驱逐。 你为这种 Pod 所设置的容忍度看起来可能是这样：

```yaml
tolerations:
- key: "node.kubernetes.io/unreachable"
  operator: "Exists"
  effect: "NoExecute"
  tolerationSeconds: 6000
```



删除所有污点

1.查看node详情

```
kubectl describe node k8s-node1
```

2.找到对应的污点

![image-20240509162349699](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257482.png)

3去除污点

![image-20240509162440004](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131257686.png)
