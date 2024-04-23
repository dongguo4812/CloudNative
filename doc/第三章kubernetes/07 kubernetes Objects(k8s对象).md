# 什么是k8s对象

[Kubernetes 对象 | Kubernetes](https://kubernetes.io/zh-cn/docs/concepts/overview/working-with-objects/)

k8s里面操作的资源实体，就是k8s的对象，可以使用yaml来声明对象。然后让k8s根据yaml的声明 创建出这个对象；

```shell
kubectl apply -f deployment.yaml
```

操作 Kubernetes 对象 —— 无论是创建、修改，或者删除 —— 需要使用 Kubernetes API。比如，当 使用 kubectl 命令行接口时，CLI 会执行必要的 Kubernetes API 调用.

![image-20240420174930218](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212107026.png)



Kubernetes对象指的是Kubernetes系统的持久化实体，所有这些对象合起来，代表了你集群的实际 情况。常规的应用里，我们把应用程序的数据存储在数据库中，**Kubernetes将其数据以Kubernetes 对象的形式通过 api server存储在 etcd 中**。具体来说，这些数据（Kubernetes对象）描述了：

- 集群中运行了哪些容器化应用程序（以及在哪个节点上运行）
- 群中对应用程序可用的资源（网络，存储等）
- 应用程序相关的策略定义，例如，重启策略、升级策略、容错策略
- 其他Kubernetes管理应用程序时所需要的信息

## Pod对象信息

以下是其中一个pod具体的信息，包括状态、容器配置、事件历史以及其他相关信息。：

```shell
[root@k8s-master k8s]# kubectl describe pod/nginx-deployment-6695bf8fd7-5n5pr
Name:         nginx-deployment-6695bf8fd7-5n5pr	#Pod 的名称
Namespace:    default	#所在的命名空间
Priority:     0 #Pod 的优先级
Node:         k8s-node1/192.168.122.141  #节点信息
Start Time:   Sat, 20 Apr 2024 10:06:10 +0800 #Pod 启动的时间
Labels:       app=nginx    #Pod 的标签
              pod-template-hash=6695bf8fd7
Annotations:  cni.projectcalico.org/containerID:   93aa68b140828bc550d7eda8ebbe993ed075607f145541d40b3ef979ed1a26b4 #Pod 的注释信息
              cni.projectcalico.org/podIP: 10.244.36.78/32
              cni.projectcalico.org/podIPs: 10.244.36.78/32
Status:       Running #状态
IP:           10.244.36.78 #IP 地址
IPs:
  IP:           10.244.36.78
Controlled By:  ReplicaSet/nginx-deployment-6695bf8fd7 #控制器信息 由ReplicaSet 控制器管理
Containers: #容器信息
  nginx:
    Container ID:   docker://913e9f6c90ae7f41108e3282016ab5e297da0f78aabbaafecb7d722caa99b79d
    Image:          nginx:1.9.1
    Image ID:       docker-pullable://nginx@sha256:2f68b99bc0d6d25d0c56876b924ec20418544ff28e1fb89a4c27679a40da811b
    Port:           <none>
    Host Port:      <none>
    State:          Running
      Started:      Sat, 20 Apr 2024 10:06:12 +0800
    Ready:          True
    Restart Count:  0
    Environment:    <none>
    Mounts:
      /var/run/secrets/kubernetes.io/serviceaccount from kube-api-access-2wnmc (ro)
Conditions: #环境变量
  Type              Status
  Initialized       True 
  Ready             True 
  ContainersReady   True 
  PodScheduled      True 
Volumes: #挂载的卷
  kube-api-access-2wnmc:
    Type:                    Projected (a volume that contains injected data from multiple sources)
    TokenExpirationSeconds:  3607
    ConfigMapName:           kube-root-ca.crt
    ConfigMapOptional:       <nil>
    DownwardAPI:             true
QoS Class:                   BestEffort
Node-Selectors:              <none>
Tolerations:                 node.kubernetes.io/not-ready:NoExecute op=Exists for 300s
                             node.kubernetes.io/unreachable:NoExecute op=Exists for 300s
Events: #容器事件
  Type    Reason     Age        From               Message
  ----    ------     ----       ----               -------
  Normal  Scheduled  4m54s      default-scheduler  Successfully assigned default/nginx-deployment-6695bf8fd7-5n5pr to k8s-node1  #调度
  Normal  Pulled     <invalid>  kubelet            Container image "nginx:1.9.1" already present on machine #拉取镜像
  Normal  Created    <invalid>  kubelet            Created container nginx #创建容器
  Normal  Started    <invalid>  kubelet            Started container nginx #启动容器
```

## Deployment 对象信息

`nginx-deployment` 的 Deployment 的配置和当前状态。其中包含了 Deployment 的元数据、规范、状态等信息。

```shell
[root@k8s-master k8s]# kubectl get deploy nginx-deployment -o yaml
apiVersion: apps/v1
kind: Deployment
metadata: # 元数据，包括名称、命名空间、UID 等信息。
  annotations: #注释包含一些与该 Deployment 相关的注释，例如修订版本和最后应用的配置。
    deployment.kubernetes.io/revision: "1"
    kubectl.kubernetes.io/last-applied-configuration: |
      {"apiVersion":"apps/v1","kind":"Deployment","metadata":{"annotations":{},"labels":{"app":"nginx"},"name":"nginx-deployment","namespace":"default"},"spec":{"replicas":3,"selector":{"matchLabels":{"app":"nginx"}},"template":{"metadata":{"labels":{"app":"nginx"}},"spec":{"containers":[{"image":"nginx:1.9.1","name":"nginx"}]}}}}
  creationTimestamp: "2024-04-20T01:12:03Z" #创建时间
  generation: 1
  labels:
    app: nginx
  name: nginx-deployment #Deployment 的名称。
  namespace: default #Deployment 所在的命名空间。
  resourceVersion: "176397" #资源版本，用于乐观锁定。
  uid: 97060d17-8782-4423-acfc-c91f2a10e252 #Deployment 的唯一标识符。
spec: # Deployment 的期望状态。
  progressDeadlineSeconds: 600
  replicas: 3 #副本数，即该 Deployment 需要创建的 Pod 的副本数量。
  revisionHistoryLimit: 10
  selector: #选择器用于选择要管理的 Pod。
    matchLabels:
      app: nginx
  strategy: #部署策略，这里使用的是 RollingUpdate 策略，允许控制滚动更新的行为。
    rollingUpdate:
      maxSurge: 25%
      maxUnavailable: 25%
    type: RollingUpdate
  template: #Pod 模板，定义了创建 Pod 的规范。
    metadata:
      creationTimestamp: null
      labels:
        app: nginx
    spec:
      containers:
      - image: nginx:1.9.1
        imagePullPolicy: IfNotPresent
        name: nginx
        resources: {}
        terminationMessagePath: /dev/termination-log
        terminationMessagePolicy: File
      dnsPolicy: ClusterFirst
      restartPolicy: Always
      schedulerName: default-scheduler
      securityContext: {}
      terminationGracePeriodSeconds: 30
status: #状态部分包含有关 Deployment 当前状态的信息。
  availableReplicas: 3 #可用的副本数。
  conditions: #条件包含有关 Deployment 状态的详细信息，如可用性和进度。
  - lastTransitionTime: "2024-04-20T01:12:05Z"
    lastUpdateTime: "2024-04-20T01:12:05Z"
    message: Deployment has minimum availability.
    reason: MinimumReplicasAvailable
    status: "True"
    type: Available
  - lastTransitionTime: "2024-04-20T01:12:03Z"
    lastUpdateTime: "2024-04-20T01:12:05Z"
    message: ReplicaSet "nginx-deployment-6695bf8fd7" has successfully progressed.
    reason: NewReplicaSetAvailable
    status: "True"
    type: Progressing
  observedGeneration: 1 #观察到的生成版本，用于指示观察到的 Deployment 规范的版本。
  readyReplicas: 3 # 就绪的副本数。
  replicas: 3 #总副本数。
  updatedReplicas: 3 # 更新的副本数，即处于最新版本的 Pod 的数量。
```

### 对象的spec和status

每一个 Kubernetes 对象都包含了两个重要的字段：

- spec  规范，描述了对该对象所期望的目标状态

- status 只能由 Kubernetes 系统来修改，描述了该对象在 Kubernetes 系统中的 实际状态



当我删除其中一个pod时

```shell
kubectl delete pod nginx-deployment-6695bf8fd7-78p6b
```

![image-20240420210547082](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212107455.png)

Deployment 的状态中显示可用的副本为2个，不可用的副本为1个

![image-20240420210623293](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212107097.png)

不久后状态恢复正常

![image-20240420210817432](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212107787.png)

Kubernetes通过对应的 控制器，不断地使实际状态趋向于期望的目标状态，保证实际状态与期望状态的最终一致性。这就是为什么Kubernetes能自我修复机制的原因之一。

## Service对象信息

```shell
[root@k8s-master k8s]# kubectl get service nginx-service -o yaml
apiVersion: v1 # Kubernetes 资源的 API 版本
kind: Service #类型
metadata: #元数据，如名称、命名空间、创建时间、资源版本和唯一标识符（UID）等。
  annotations:
    kubectl.kubernetes.io/last-applied-configuration: |
      {"apiVersion":"v1","kind":"Service","metadata":{"annotations":{},"labels":{"app":"nginx"},"name":"nginx-service","namespace":"default"},"spec":{"ports":[{"name":"nginx-port","nodePort":32600,"port":80,"protocol":"TCP","targetPort":80}],"selector":{"app":"nginx"},"type":"NodePort"}}
  creationTimestamp: "2024-04-20T01:13:06Z"
  labels:
    app: nginx
  name: nginx-service
  namespace: default
  resourceVersion: "176488"
  uid: 9bfb4ee4-fd00-4f3e-8c80-d811f9425525
spec: #服务的规范，包括了服务的配置信息。
  clusterIP: 10.96.74.75
  clusterIPs:
  - 10.96.74.75
  externalTrafficPolicy: Cluster
  ipFamilies:
  - IPv4
  ipFamilyPolicy: SingleStack
  ports: #指定了服务暴露的端口
  - name: nginx-port
    nodePort: 32600
    port: 80
    protocol: TCP
    targetPort: 80 #Pod 内容器所监听的端口号
  selector: #服务所选取的 Pod。
    app: nginx
  sessionAffinity: None
  type: NodePort #服务的类型，通过每个节点上的同一端口暴露出来，以便从集群外部访问。
status: #状态
  loadBalancer: {}
```

- 对于 `NodePort` 类型的服务，Kubernetes 不会自动创建外部负载均衡器，因此 `loadBalancer` 字段保持为空。

## Node对象信息

```shell
[root@k8s-master k8s]# kubectl get node k8s-master -o yaml
apiVersion: v1
kind: Node
metadata: #元数据，如创建时间、标签和 UID。
  annotations:
    kubeadm.alpha.kubernetes.io/cri-socket: /var/run/dockershim.sock
    node.alpha.kubernetes.io/ttl: "0"
    projectcalico.org/IPv4Address: 192.168.122.140/24
    projectcalico.org/IPv4IPIPTunnelAddr: 10.244.235.192
    volumes.kubernetes.io/controller-managed-attach-detach: "true"
  creationTimestamp: "2024-04-18T06:13:56Z"
  labels:
    beta.kubernetes.io/arch: amd64
    beta.kubernetes.io/os: linux
    kubernetes.io/arch: amd64
    kubernetes.io/hostname: k8s-master
    kubernetes.io/os: linux
    node-role.kubernetes.io/control-plane: ""
    node-role.kubernetes.io/master: ""
    node.kubernetes.io/exclude-from-external-load-balancers: ""
  name: k8s-master
  resourceVersion: "195771"
  uid: 08f6c984-3e52-45af-88f8-e642eafbdfb2
spec: #节点的规范，包括 podCIDR、taints 等。
  podCIDR: 10.244.0.0/24
  podCIDRs:
  - 10.244.0.0/24
  taints:
  - effect: NoSchedule
    key: node-role.kubernetes.io/master
status: #节点当前状态的信息，如节点的地址、可分配资源、节点条件和容器运行时版本等。
  addresses:
  - address: 192.168.122.140
    type: InternalIP
  - address: k8s-master
    type: Hostname
  allocatable:
    cpu: "2"
    ephemeral-storage: "9654239217"
    hugepages-1Gi: "0"
    hugepages-2Mi: "0"
    memory: 1760628Ki
    pods: "110"
  capacity:
    cpu: "2"
    ephemeral-storage: 10230Mi
    hugepages-1Gi: "0"
    hugepages-2Mi: "0"
    memory: 1863028Ki
    pods: "110"
  conditions:
  - lastHeartbeatTime: "2024-04-19T08:49:40Z"
    lastTransitionTime: "2024-04-19T08:49:40Z"
    message: Calico is running on this node
    reason: CalicoIsUp
    status: "False"
    type: NetworkUnavailable
  - lastHeartbeatTime: "2024-04-20T04:57:54Z"
    lastTransitionTime: "2024-04-18T06:13:56Z"
    message: kubelet has sufficient memory available
    reason: KubeletHasSufficientMemory
    status: "False"
    type: MemoryPressure
  - lastHeartbeatTime: "2024-04-20T04:57:54Z"
    lastTransitionTime: "2024-04-18T06:13:56Z"
    message: kubelet has no disk pressure
    reason: KubeletHasNoDiskPressure
    status: "False"
    type: DiskPressure
  - lastHeartbeatTime: "2024-04-20T04:57:54Z"
    lastTransitionTime: "2024-04-18T06:13:56Z"
    message: kubelet has sufficient PID available
    reason: KubeletHasSufficientPID
    status: "False"
    type: PIDPressure
  - lastHeartbeatTime: "2024-04-20T04:57:54Z"
    lastTransitionTime: "2024-04-18T06:26:00Z"
    message: kubelet is posting ready status
    reason: KubeletReady
    status: "True"
    type: Ready
  daemonEndpoints:
    kubeletEndpoint:
      Port: 10250
  images:
  - names:
    - registry.cn-hangzhou.aliyuncs.com/dongguo/etcd@sha256:bd4d2c9a19be8a492bc79df53eee199fd04b415e9993eb69f7718052602a147a
    - registry.cn-hangzhou.aliyuncs.com/dongguo/etcd:3.4.13-0
    sizeBytes: 253392289
  - names:
    - calico/node@sha256:a85123d1882832af6c45b5e289c6bb99820646cb7d4f6006f98095168808b1e6
    - calico/node:v3.25.0
    sizeBytes: 244681810
  - names:
    - calico/cni@sha256:a38d53cb8688944eafede2f0eadc478b1b403cefeff7953da57fe9cd2d65e977
    - calico/cni:v3.25.0
    sizeBytes: 197853743
  - names:
    - registry.cn-hangzhou.aliyuncs.com/dongguo/kube-apiserver@sha256:1435e167151f90b7f4abfd416726751c46b8672cc7288507fab7cfa5a05b866c
    - registry.cn-hangzhou.aliyuncs.com/dongguo/kube-apiserver:v1.21.0
    sizeBytes: 125591921
  - names:
    - registry.cn-hangzhou.aliyuncs.com/dongguo/kube-proxy@sha256:d33e7f7f7945f6008cba6a8b333f6794cc4661f9534b441d87de9b9b79c32e60
    - registry.cn-hangzhou.aliyuncs.com/dongguo/kube-proxy:v1.21.0
    sizeBytes: 122238196
  - names:
    - registry.cn-hangzhou.aliyuncs.com/dongguo/kube-controller-manager@sha256:d43b09e09bb9b6b2f472293b3239d430b2fc6ce759f023389f1a25b4c036e21a
    - registry.cn-hangzhou.aliyuncs.com/dongguo/kube-controller-manager:v1.21.0
    sizeBytes: 119808868
  - names:
    - calico/kube-controllers@sha256:c45af3a9692d87a527451cf544557138fedf86f92b6e39bf2003e2fdb848dce3
    - calico/kube-controllers:v3.25.0
    sizeBytes: 71618733
  - names:
    - registry.cn-hangzhou.aliyuncs.com/dongguo/kube-scheduler@sha256:70b94430a127377183a107ff90b788232be423d5430c08c4b17b7c82aea9d2ec
    - registry.cn-hangzhou.aliyuncs.com/dongguo/kube-scheduler:v1.21.0
    sizeBytes: 50631537
  - names:
    - registry.cn-hangzhou.aliyuncs.com/dongguo/coredns@sha256:10ecc12177735e5a6fd6fa0127202776128d860ed7ab0341780ddaeb1f6dfe61
    - registry.cn-hangzhou.aliyuncs.com/dongguo/coredns/coredns:v1.8.0
    - registry.cn-hangzhou.aliyuncs.com/dongguo/coredns:v1.8.0
    sizeBytes: 42454755
  - names:
    - registry.cn-hangzhou.aliyuncs.com/dongguo/pause@sha256:9ec1e780f5c0196af7b28f135ffc0533eddcb0a54a0ba8b32943303ce76fe70d
    - registry.cn-hangzhou.aliyuncs.com/dongguo/pause:3.4.1
    sizeBytes: 682696
  nodeInfo:
    architecture: amd64
    bootID: 99e1c84c-e44a-4f65-9ffb-2522a7528e88
    containerRuntimeVersion: docker://19.3.9
    kernelVersion: 3.10.0-1160.el7.x86_64
    kubeProxyVersion: v1.21.0
    kubeletVersion: v1.21.0
    machineID: 2eb14893f04f40a1bc194c1c428cce30
    operatingSystem: linux
    osImage: CentOS Linux 7 (Core)
    systemUUID: 7FAB4D56-DA3B-20AF-E0EE-72EBA595A9BC
```

# 创建k8s对象

我们可以自定义创建任意资源

当在 Kubernetes 中创建一个对象时，必须提供 该对象的 spec 字段，通过该字段描述您期望的 目标状态 该对象的一些基本信息，例如名字 。

## 1.可以使用 kubectl 命令行创建对象，如创建一个pod

```shell
kubectl run my-nginx --image=nginx
```

![image-20240420214117547](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212107483.png)

## 2.也可以编写 .yaml 格式的文件进行创建，如何编写yaml文件呢，可以查看其他pod

```yaml
[root@k8s-master k8s]# kubectl get pod my-nginx -o yaml
apiVersion: v1
kind: Pod
metadata:
  annotations:
    cni.projectcalico.org/containerID: fcc83d6e11e58a2e9144c8251f56bef479204cd31226f1248017b18880454e8c
    cni.projectcalico.org/podIP: 10.244.36.81/32
    cni.projectcalico.org/podIPs: 10.244.36.81/32
  creationTimestamp: "2024-04-20T05:06:40Z"
  labels:
    run: my-nginx
  name: my-nginx
  namespace: default
  resourceVersion: "196560"
  uid: 842e389e-ffb8-4ed2-baa4-f572c56220f7
spec:
  containers:
  - image: nginx
    imagePullPolicy: Always
    name: my-nginx
    resources: {}
    terminationMessagePath: /dev/termination-log
    terminationMessagePolicy: File
    volumeMounts:
    - mountPath: /var/run/secrets/kubernetes.io/serviceaccount
      name: kube-api-access-dxzzd
      readOnly: true
  dnsPolicy: ClusterFirst
  enableServiceLinks: true
  nodeName: k8s-node1
  preemptionPolicy: PreemptLowerPriority
  priority: 0
  restartPolicy: Always
  schedulerName: default-scheduler
  securityContext: {}
  serviceAccount: default
  serviceAccountName: default
  terminationGracePeriodSeconds: 30
  tolerations:
  - effect: NoExecute
    key: node.kubernetes.io/not-ready
    operator: Exists
    tolerationSeconds: 300
  - effect: NoExecute
    key: node.kubernetes.io/unreachable
    operator: Exists
    tolerationSeconds: 300
  volumes:
  - name: kube-api-access-dxzzd
    projected:
      defaultMode: 420
      sources:
      - serviceAccountToken:
          expirationSeconds: 3607
          path: token
      - configMap:
          items:
          - key: ca.crt
            path: ca.crt
          name: kube-root-ca.crt
      - downwardAPI:
          items:
          - fieldRef:
              apiVersion: v1
              fieldPath: metadata.namespace
            path: namespace
status:
  conditions:
  - lastProbeTime: null
    lastTransitionTime: "2024-04-20T06:20:21Z"
    status: "True"
    type: Initialized
  - lastProbeTime: null
    lastTransitionTime: "2024-04-20T06:20:41Z"
    status: "True"
    type: Ready
  - lastProbeTime: null
    lastTransitionTime: "2024-04-20T06:20:41Z"
    status: "True"
    type: ContainersReady
  - lastProbeTime: null
    lastTransitionTime: "2024-04-20T05:06:40Z"
    status: "True"
    type: PodScheduled
  containerStatuses:
  - containerID: docker://d2185ed8e7e063447a75b57d33a3be5aa90a2f2368ecbab57157199b4a9a138c
    image: nginx:latest
    imageID: docker-pullable://nginx@sha256:0d17b565c37bcbd895e9d92315a05c1c3c9a29f762b011a10c54a66cd53c9b31
    lastState: {}
    name: my-nginx
    ready: true
    restartCount: 0
    started: true
    state:
      running:
        startedAt: "2024-04-20T06:20:41Z"
  hostIP: 192.168.122.141
  phase: Running
  podIP: 10.244.36.81
  podIPs:
  - ip: 10.244.36.81
  qosClass: BestEffort
  startTime: "2024-04-20T06:20:21Z"
```

无需复制status，其他的配置信息就是配置一个pod的yaml

```shell
apiVersion: v1
kind: Pod
metadata:
  annotations:
    cni.projectcalico.org/containerID: fcc83d6e11e58a2e9144c8251f56bef479204cd31226f1248017b18880454e8c
    cni.projectcalico.org/podIP: 10.244.36.81/32
    cni.projectcalico.org/podIPs: 10.244.36.81/32
  creationTimestamp: "2024-04-20T05:06:40Z"
  labels:
    run: my-nginx
  name: my-nginx
  namespace: default
  resourceVersion: "196560"
  uid: 842e389e-ffb8-4ed2-baa4-f572c56220f7
spec:
  containers:
  - image: nginx
    imagePullPolicy: Always
    name: my-nginx
    resources: {}
    terminationMessagePath: /dev/termination-log
    terminationMessagePolicy: File
    volumeMounts:
    - mountPath: /var/run/secrets/kubernetes.io/serviceaccount
      name: kube-api-access-dxzzd
      readOnly: true
  dnsPolicy: ClusterFirst
  enableServiceLinks: true
  nodeName: k8s-node1
  preemptionPolicy: PreemptLowerPriority
  priority: 0
  restartPolicy: Always
  schedulerName: default-scheduler
  securityContext: {}
  serviceAccount: default
  serviceAccountName: default
  terminationGracePeriodSeconds: 30
  tolerations:
  - effect: NoExecute
    key: node.kubernetes.io/not-ready
    operator: Exists
    tolerationSeconds: 300
  - effect: NoExecute
    key: node.kubernetes.io/unreachable
    operator: Exists
    tolerationSeconds: 300
  volumes:
  - name: kube-api-access-dxzzd
    projected:
      defaultMode: 420
      sources:
      - serviceAccountToken:
          expirationSeconds: 3607
          path: token
      - configMap:
          items:
          - key: ca.crt
            path: ca.crt
          name: kube-root-ca.crt
      - downwardAPI:
          items:
          - fieldRef:
              apiVersion: v1
              fieldPath: metadata.namespace
            path: namespace
```

## 3.那如果没有其他pod怎么办？

`--dry-run=client` 参数会导致 `kubectl` 仅在本地验证客户端请求的配置是否正确，而不会向 API Server 发送实际的创建请求。这样可以在不实际创建资源的情况下，对资源配置进行测试和验证，以确保它们不会引发意外的行为。

```shell
[root@k8s-master k8s]# kubectl run my-nginx --image=nginx --dry-run=client -o yaml
apiVersion: v1
kind: Pod
metadata:
  creationTimestamp: null
  labels:
    run: my-nginx
  name: my-nginx
spec:
  containers:
  - image: nginx
    name: my-nginx
    resources: {}
  dnsPolicy: ClusterFirst
  restartPolicy: Always
status: {}
```

这样就可以获得pod最基本的 .yaml 格式的配置

```shell
apiVersion: v1 #同一个资源有可能有多个版本。看 kubectl api-resources提示的。
kind: Pod #资源类型 kubectl api-resources:可以获取到所有资源
metadata: #每一个资源定义一些元数据信息
  labels:
    run: my-nginx
  name: my-nginx
spec: #资源的规格（镜像名、镜像的环境变量信息等等）
  containers:
  - image: nginx
    name: my-nginx
    resources: {}
  dnsPolicy: ClusterFirst
  restartPolicy: Always
```

## k8s对象yaml的结构

以下是最基本的一个pod对象yaml的结构，创建一个名为my-tomcat的pod

```yaml
#typeMeta 类型信息
apiVersion: v1
kind: Pod
metadata: #元数据
  name: my-tomcat   #资源名称
spec: #资源的规格信息
  containers:    #容器
  - image: tomcat   #指定镜像
    name: my-tomcat     #容器名称
```

![image-20240421080834214](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212106239.png)

pod：my-tomcat 启动成功

![image-20240421081038242](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212106628.png)

**必填字段**

在上述的 .yaml 文件中，如下字段是必须填写的：

- apiVersion 用来创建对象时所使用的Kubernetes API版

- kind 被创建对象的类型

- metadata 用于唯一确定该对象的元数据：包括 name 和 namespace ，如果 namespace 为空， 则默认值为 default

- spec 描述您对该对象的期望状态

status不用我们写，k8s集群会实时更新状态信息，只要资源变化，kubelet会请求api-server保存最新的资源状态信息

不同类型的 Kubernetes，其 spec 对象的格式不同（含有不同的内嵌字段），通过 [ API 手册](https://kubernetes.io/docs/reference/#api-reference) 可 以查看 Kubernetes 对象的字段和描述。

更多配置信息可以参考https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/ 。例如，假设您想了解 Pod 的 spec 定义，可以在 [这里](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#pod-v1-core) 找到，Deployment 的 spec 定义可以在 [这里](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.30/#daemonset-v1-apps) 找到

# 管理k8s对象

![image-20240421084624327](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212106995.png)

## 命令式

```shell
#这两个命令都可以用来创建一个名为 "nginx" 的部署（Deployment），使用 nginx 镜像作为容器。
kubectl run nginx --image nginx

kubectl create deployment nginx --image nginx
```

## 指令性

使用指令性的对象配置（imperative object configuration）时，需要向 kubectl 命令指定具体 的操作（create,replace,apply,delete等），可选参数以及至少一个配置文件的名字。配置文件中必须 包括一个完整的对象的定义，可以是 yaml 格式，也可以是 json 格式。

### 创建对象

```shell
kubectl create -f nginx.yaml 
```

### 删除对象 

```shell
kubectl delete -f nginx.yaml
```

### 替换对象 

使用yaml文件中的配置来替换集群中现有资源的配置

- 如果资源不存在，则会创建新的资源。
- 如果资源存在，则会删除现有的资源，然后创建新的资源。这意味着旧的资源的所有字段和状态都会被新的资源所替代，包括名称和 UID。

## 变更对象

```shell
kubectl replace -f nginx.yaml
```

通常情况下，推荐使用 `kubectl apply` 来部署和更新资源，因为它更加安全，可以保留部分现有资源的状态。

- 如果资源不存在，则会创建新的资源。
- 如果资源存在，则会根据配置文件中的更新字段来更新现有的资源。这意味着只有在配置文件中明确指定的字段会被更新，其他字段则保持不变。此外，apply 不会更改资源的名称和 UID。

## 对比对象

处理 configs 目录中所有配置文件中的Kubernetes对象，根据情况创建对象、或更新Kubernetes中已 经存在的对象。可以先执行 diff 指令查看具体的变更，然后执行 apply 指令执行变更；

```shell
kubectl diff -f nginx.yaml
kubectl apply -f nginx.yaml
```

# 对象名称

Kubernetes REST API 中，所有的对象都是通过 name 和 UID 唯一性确定 可以通过 namespace + name 唯一性地确定一个 RESTFUL 对象。

## name

同一个名称空间下，同一个类型的对象，可以通过 name 唯一性确定。如果删除该对象之后，可以再 重新创建一个同名对象。

依据命名规则，Kubernetes对象的名字应该：

- 最长不超过 253个字符 

- 必须由小写字母、数字、减号 - 、小数点 . 组成

- 某些资源类型有更具体的要求

例如，下面的配置文件定义了一个 name 为 nginx-demo 的 Pod，该 Pod 包含一个 name 为 nginx 的 容器：

```shell
apiVersion: v1
kind: Pod
metadata:
  name: nginx-demo ##pod的名字
  uid: 7f415d87-6cfe-4e62-bb1e-d2f62f225c4d
spec:
  containers:
  - name: nginx ##容器的名字
	image: nginx:1.7.9
	ports:
 	 - containerPort: 80
```

## uid

UID 是由 Kubernetes 系统生成的，唯一标识某个 Kubernetes 对象的字符串。

Kubernetes集群中，每创建一个对象，都有一个唯一的 UID。用于区分多次创建的同名对象（如前所 述，按照名字删除对象后，重新再创建同名对象时，两次创建的对象 name 相同，但是 UID 不同。）

# 名称空间

```shell
kubectl get namespaces
```



![image-20240421113846811](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212106626.png)

Kubernetes 安装成功后，默认有初始化了三个名称空间：

- default ：默认名称空间，如果 Kubernetes 对象中不定义 metadata.namespace 字段，该对象将放 在此名称空间下

- kube-system ：Kubernetes系统创建的对象放在此名称空间下

- kube-public： 此名称空间自动在安装集群是自动创建，并且所有用户都是可以读取的（即使是那些 未登录的用户）。主要是为集群预留的，例如，某些情况下，某些Kubernetes对象应该被所有集群 用户看到。

名称空间的名字必须与 DNS 兼容： 

- 不能带小数点 . 

- 不能带下划线 _ 

- 使用数字、小写字母和减号 - 组成的字符串 

默认情况下，安装Kubernetes集群时，会初始化一个 default 名称空间，用来将承载那些未指定名称 空间的 Pod、Service、Deployment等对象

1）创建命名空间

```shell
kubectl create namespace hello
```

![image-20240421114622199](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212106800.png)

对象yaml创建namespace

![image-20240421115856476](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212107019.png)



2）删除命名空间

```shell
kubectl delete namespace hello
```

![image-20240421114651903](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212106139.png)

删除命名空间，命名空间中的资源会被全部删除。





3）为当前创建pod请求设置命名空间，使用 --namespace 参数

```shell
kubectl run nginx --image=nginx --namespace=hello
```

![image-20240421115049740](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212107666.png)

在对象yaml中使用命名空间

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx-demo ##pod的名字
  namespace: hello #不写就是default
spec:
  containers:
  - name: nginx ##容器的名字
	image: nginx:1.7.9
	ports:
 	 - containerPort: 80
```

## 名称空间资源隔离

不同的名称空间之间的资源是相互隔离的，每个名称空间中的资源只能被该名称空间中的其他资源访问。

可以将不同的环境（例如开发、测试、生产）部署到不同的名称空间中，以便更好地管理和隔离各个环境的资源。

## 名称空间资源隔离，网络不隔离

即使在不同的名称空间中创建了相同的网络资源（如 Service 或 Endpoint），它们之间仍可以进行通信。

网络隔离通常是通过网络策略（Network Policies）来实现的。网络策略允许您定义哪些 Pod 可以与其他 Pod 进行通信，以及允许的通信方向、协议和端口等规则。但默认情况下，Kubernetes 集群中的 Pod 可以相互通信，即使它们位于不同的名称空间中。



pod与pod之间通过serviceName来访问

在 Kubernetes 中，当创建一个 Service 时，Kubernetes会为该服务创建相应的 DNS 条目。这些 DNS 条目的形式是 `<service-name>.<namespace>.svc.cluster.local`。这样，如果容器只使用 `<service-name>` 来访问服务，它将被解析到本地命名空间的服务。

这种命名约定在许多情况下非常有用，特别是在需要跨多个命名空间（如开发、测试和生产）使用相同的服务配置时。但是，如果您希望跨命名空间访问服务，则需要使用完全限定域名（FQDN），即 `<service-name>.<namespace>.svc.cluster.local`。这样就可以确保正确解析服务的位置，无论在哪个命名空间中使用。

## 并非所有对象都在命名空间中

大多数 kubernetes 资源（例如 Pod、Service、副本控制器等）都位于某些命名空间中。但是命名空间资源本身并不在命名空间中。而且底层资源，例如 nodes 和持久化卷不属于任何命名空间。

查看哪些 Kubernetes 资源在命名空间中，哪些不在命名空间中：

```shell
#In a namespace
kubectl api-resources --namespaced=true
#Not in a namespace
kubectl api-resources --namespaced=false
```

# 标签和选择器

标签（Label）是附加在Kubernetes对象上的一组名值对，其意图是按照对用户有意义的方式来标识 Kubernetes对象，同时，又不对Kubernetes的核心逻辑产生影响。标签可以用来组织和选择一组 Kubernetes对象。您可以在创建Kubernetes对象时为其添加标签，也可以在创建以后再为其添加标签。 每个Kubernetes对象可以有多个标签，同一个对象的标签的 Key 必须唯一。

## 为什么要使用标签

使用标签，用户可以按照自己期望的形式组织 Kubernetes 对象之间的结构，而无需对 Kubernetes 有任 何修改。

应用程序的部署或者批处理程序的部署通常都是多维度的（例如，多个高可用分区、多个程序版本、多 个微服务分层）。管理这些对象时，很多时候要针对某一个维度的条件做整体操作，例如，将某个版本 的程序整体删除，这种情况下，如果用户能够事先规划好标签的使用，再通过标签进行选择，就会非常 地便捷。

### 句法和字符集

标签是一组名值对（key/value pair）。标签的 key 可以有两个部分：可选的前缀和标签名，通过 / 分 隔。

标签名：

- 标签名部分是必须的 

- 不能多于 63 个字符 

- 必须由字母、数字开始和结尾 

- 可以包含字母、数字、减号 - 、下划线 _ 、小数点 .

标签前缀：

- 标签前缀部分是可选

- 如果指定，必须是一个DNS的子域名，例如：k8s.eip.work

- 不能多于 253 个字符 

- 使用 / 和标签名分隔

如果省略标签前缀，则标签的 key 将被认为是专属于用户的。Kubernetes的系统组件（例如，kubescheduler、kube-controller-manager、kube-apiserver、kubectl 或其他第三方组件）向用户的Kubernetes 对象添加标签时，必须指定一个前缀。 kubernetes.io/ 和 k8s.io/ 这两个前缀是 Kubernetes 核心 组件预留的。

标签的 value 必须：

- 不能多于 63 个字符

- 可以为空字符串

- 如果不为空，则

​			必须由字母、数字开始和结尾

​			可以包含字母、数字、减号 - 、下划线 _ 、小数点 .

给pod打标签

```shell
kubectl label pod my-tomcat aaa=bbb
```

![image-20240421140504934](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212107267.png)

yaml方式创建pod时配置标签

```shell
apiVersion: v1
kind: Pod
metadata:
  name: label-demo
  labels:
    environment: production
    app: nginx
spec:
  containers:
  - name: nginx
    image: nginx:1.7.9
    ports:
    - containerPort: 80
```

![image-20240421140708582](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212107129.png)

删除标签

```
kubectl label pod my-tomcat aaa-
```

![image-20240421140836805](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212107650.png)



更新标签

```shell
kubectl label --overwrite pods label-demo environment=test
```

![image-20240421141425719](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212107579.png)



## 标签选择器

通常来讲，会有多个Kubernetes对象包含相同的标签。通过使用标签选择器（label selector），用户/客 户端可以选择一组对象。标签选择器（label selector）是 Kubernetes 中最主要的分类和筛选手段。

Kubernetes api server支持两种形式的标签选择器， equality-based 基于等式的 和 set-based 基于 集合的 。标签选择器可以包含多个条件，并使用逗号分隔，此时只有满足所有条件的 Kubernetes 对象 才会被选中

使用基于等式的选择方式,可以使用三种操作符 =、==、!=。前两个操作符含义是一样的，都代表 相等，后一个操作符代表不相等

![image-20240421141134662](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212107394.png)



![image-20240421141147822](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212107392.png)

# 注解annotation

注解（annotation）可以用来向 Kubernetes 对象的 metadata.annotations 字段添加任意的信息。 Kubernetes 的客户端或者自动化工具可以存取这些信息以实现其自定义的逻辑。

# 字段选择器

字段选择器 （Field selectors ）允许您根据一个或多个资源字段的值筛选 Kubernetes 资源。 

下面是使用字段选择器查询的例子：

```
kubectl get pods --field-selector metadata.name=my-nginx
```

![image-20240421141705312](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404212107442.png)

# 认识kubectl和kubelet

![image-20240422093727639](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231148255.png)

使用 `kubeadm` 安装的 Kubernetes 集群的相关配置文件都存储在 `/etc/kubernetes` 目录下。以下是其中一些重要文件的简要概述：

1. **admin.conf**：用于管理访问集群的配置文件。通常包括 kubectl 与集群通信所需的凭据和集群信息。
2. **controller-manager.conf**：Kubernetes 控制器管理器的配置文件，它管理着控制集群状态的各种控制器。
3. **kubelet.conf**：kubelet 的配置文件，kubelet 负责管理节点上的 pod 和容器。
4. **scheduler.conf**：Kubernetes 调度器的配置文件，负责将 pod 调度到集群中的节点上。

`manifests` 目录通常用于存放各种资源对象的 YAML 文件，这些对象描述了 Kubernetes 集群中要创建的工作负载、服务、配置等内容。

当 Kubernetes 集群启动时，它会检测 `manifests` 目录中的 YAML 文件，并尝试将这些文件中描述的对象部署到集群中。这些对象的创建由 Kubernetes 控制器负责管理，并根据文件中定义的规范确保集群中的状态与文件中描述的状态一致。

![image-20240422094828441](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231148026.png)

后期使用二进制方式安装Kubernetes集群时，就会执行kubectl install etcd.yaml等操作来将这些对象部署到集群中。

kubelet额外参数配置 /etc/sysconfig/kubelet；

kubelet配置位置 /var/lib/kubelet/config.yam



kubectl的所有命令参考：https://kubernetes.io/docs/reference/generated/kubectl/kubectl-commands

# kubelet命令自动补全

使用 Bash shell 的自动补全功能，安装 bash-completion 软件包

```shell
yum install bash-completion
```

![image-20240422101051427](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231148281.png)

启动 Kubernetes 自动补全功能

```shell
#这个命令会将 kubectl completion bash 的输出附加到 ~/.bashrc 文件的末尾。这样，每次你打开一个新的终端窗口时，Bash 都会自动加载 kubectl 的自动补全。
echo 'source <(kubectl completion bash)' >>~/.bashrc
#这个命令将 kubectl completion bash 的输出重定向到 /etc/bash_completion.d/kubectl 文件中。这样可以确保在每个用户的 Bash 自动补全目录中都存在 kubectl 的自动补全。
kubectl completion bash >/etc/bash_completion.d/kubectl
#这个命令用于重新加载 Bash 的自动补全脚本，以便使新添加的 kubectl 自动补全生效。
source /usr/share/bash-completion/bash_completion
```

![image-20240422102002494](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231148052.png)
