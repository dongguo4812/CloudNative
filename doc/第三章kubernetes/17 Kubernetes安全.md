https://kubernetes.io/zh-cn/docs/concepts/security/controlling-access/

用户使用 `kubectl`、客户端库或构造 REST 请求来访问 [Kubernetes API](https://kubernetes.io/zh-cn/docs/concepts/overview/kubernetes-api/)。 人类用户和 [Kubernetes 服务账号](https://kubernetes.io/zh-cn/docs/tasks/configure-pod-container/configure-service-account/)都可以被鉴权访问 API。 当请求到达 API 时，它会经历多个阶段，如下图所示：

![Kubernetes API 请求处理步骤示意图](https://kubernetes.io/zh-cn/docs/images/access-control-overview.svg)

# RBAC

RBAC API声明了四种Kubernetes对象: Role、 ClusterRole、 RoleBinding和ClusterRoleBinding

# ClusterRole与Role

RBAC 的 **Role** 或 **ClusterRole** 中包含一组代表相关权限的规则。 这些权限是纯粹累加的（不存在拒绝某操作的规则）。

Role 总是用来在某个[名字空间](https://kubernetes.io/zh-cn/docs/concepts/overview/working-with-objects/namespaces/)内设置访问权限； 在你创建 Role 时，你必须指定该 Role 所属的名字空间。

与之相对，ClusterRole 则是一个集群作用域的资源。这两种资源的名字不同（Role 和 ClusterRole） 是因为 Kubernetes 对象要么是名字空间作用域的，要么是集群作用域的，不可两者兼具。

ClusterRole 有若干用法。你可以用它来：

1. 定义对某名字空间域对象的访问权限，并将在个别名字空间内被授予访问权限；
2. 为名字空间作用域的对象设置访问权限，并被授予跨所有名字空间的访问权限；
3. 为集群作用域的资源定义访问权限。

如果你希望在名字空间内定义角色，应该使用 Role； 如果你希望定义集群范围的角色，应该使用 ClusterRole。

## Role

k8s默认创建以及我们之前创建的Role

![image-20240509095223305](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131254847.png)



下面是一个位于 "default" 名字空间的 Role 的示例，可用来授予对 [Pod](https://kubernetes.io/zh-cn/docs/concepts/workloads/pods/) 的读访问权限：

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: default
  name: pod-reader
rules:
- apiGroups: [""] # "" 标明 core API 组
  resources: ["pods"] #当前角色能操作所有的pod
  verbs: ["get", "watch", "list"] #获取 监听操作动作
```

![image-20240509095357139](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131254043.png)

详细信息

![image-20240509095523201](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131254349.png)

## ClusterRole

![image-20240509095614839](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131254042.png)

ClusterRole 同样可以用于授予 Role 能够授予的权限。 因为 ClusterRole 属于集群范围，所以它也可以为以下资源授予访问权限：

- 集群范围资源（比如[节点（Node）](https://kubernetes.io/zh-cn/docs/concepts/architecture/nodes/)）

- 非资源端点（比如 `/healthz`）

- 跨名字空间访问的名字空间作用域的资源（如 Pod）

  比如，你可以使用 ClusterRole 来允许某特定用户执行 `kubectl get pods --all-namespaces`

下面是一个 ClusterRole 的示例，可用来为任一特定名字空间中的 [Secret](https://kubernetes.io/zh-cn/docs/concepts/configuration/secret/) 授予读访问权限， 或者跨名字空间的访问权限（取决于该角色是如何[绑定](https://kubernetes.io/zh-cn/docs/reference/access-authn-authz/rbac/#rolebinding-and-clusterrolebinding)的）：

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  # "namespace" 被忽略，因为 ClusterRoles 不受名字空间限制
  name: secret-reader
rules:
- apiGroups: [""]
  # 在 HTTP 层面，用来访问 Secret 资源的名称为 "secrets"
  resources: ["secrets"]
  verbs: ["get", "watch", "list"]
```

![image-20240509095758465](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131254435.png)



# RoleBinding、ClusterRoleBinding

角色绑定（Role Binding）是将角色中定义的权限赋予一个或者一组用户。 它包含若干**主体（Subject）**（用户、组或服务账户）的列表和对这些主体所获得的角色的引用。 RoleBinding 在指定的名字空间中执行授权，而 ClusterRoleBinding 在集群范围执行授权。

一个 RoleBinding 可以引用同一的名字空间中的任何 Role。 或者，一个 RoleBinding 可以引用某 ClusterRole 并将该 ClusterRole 绑定到 RoleBinding 所在的名字空间。 如果你希望将某 ClusterRole 绑定到集群中所有名字空间，你要使用 ClusterRoleBinding。

RoleBinding 或 ClusterRoleBinding 对象的名称必须是合法的 [路径分段名称](https://kubernetes.io/zh-cn/docs/concepts/overview/working-with-objects/names#path-segment-names)。

## RoleBinding

下面的例子中的 RoleBinding 将 "pod-reader" Role 授予在 "default" 名字空间中的用户 "jane"。 这样，用户 "jane" 就具有了读取 "default" 名字空间中所有 Pod 的权限。

```yaml
apiVersion: rbac.authorization.k8s.io/v1
# 此角色绑定允许 "jane" 读取 "default" 名字空间中的 Pod
# 你需要在该名字空间中有一个名为 “pod-reader” 的 Role
kind: RoleBinding
metadata:
  name: read-pods
  namespace: default
subjects:
# 你可以指定不止一个“subject（主体）”
- kind: User
  name: jane # "name" 是区分大小写的
  apiGroup: rbac.authorization.k8s.io
roleRef:
  # "roleRef" 指定与某 Role 或 ClusterRole 的绑定关系
  kind: Role        # 此字段必须是 Role 或 ClusterRole
  name: pod-reader  # 此字段必须与你要绑定的 Role 或 ClusterRole 的名称匹配
  apiGroup: rbac.authorization.k8s.io
```

RoleBinding 也可以引用 ClusterRole，以将对应 ClusterRole 中定义的访问权限授予 RoleBinding 所在名字空间的资源。这种引用使得你可以跨整个集群定义一组通用的角色， 之后在多个名字空间中复用。

例如，尽管下面的 RoleBinding 引用的是一个 ClusterRole，"dave"（这里的主体， 区分大小写）只能访问 "development" 名字空间中的 Secret 对象，因为 RoleBinding 所在的名字空间（由其 metadata 决定）是 "development"。

```yaml
apiVersion: rbac.authorization.k8s.io/v1
# 此角色绑定使得用户 "dave" 能够读取 "development" 名字空间中的 Secret
# 你需要一个名为 "secret-reader" 的 ClusterRole
kind: RoleBinding
metadata:
  name: read-secrets
  # RoleBinding 的名字空间决定了访问权限的授予范围。
  # 这里隐含授权仅在 "development" 名字空间内的访问权限。
  namespace: development
subjects:
- kind: User
  name: dave # 'name' 是区分大小写的
  apiGroup: rbac.authorization.k8s.io
roleRef:
  kind: ClusterRole
  name: secret-reader
  apiGroup: rbac.authorization.k8s.io
```

## ClusterRoleBinding

要跨整个集群完成访问权限的授予，你可以使用一个 ClusterRoleBinding。 下面的 ClusterRoleBinding 允许 "manager" 组内的所有用户访问任何名字空间中的 Secret。

```yaml
apiVersion: rbac.authorization.k8s.io/v1
# 此集群角色绑定允许 “manager” 组中的任何人访问任何名字空间中的 Secret 资源
kind: ClusterRoleBinding
metadata:
  name: read-secrets-global
subjects:
- kind: Group
  name: manager      # 'name' 是区分大小写的
  apiGroup: rbac.authorization.k8s.io
roleRef:
  kind: ClusterRole
  name: secret-reader
  apiGroup: rbac.authorization.k8s.io
```

创建了绑定之后，你不能再修改绑定对象所引用的 Role 或 ClusterRole。 试图改变绑定对象的 `roleRef` 将导致合法性检查错误。 如果你想要改变现有绑定对象中 `roleRef` 字段的内容，必须删除重新创建绑定对象。

这种限制有两个主要原因：

1. 将 `roleRef` 设置为不可以改变，这使得可以为用户授予对现有绑定对象的 `update` 权限， 这样可以让他们管理主体列表，同时不能更改被授予这些主体的角色。

1. 针对不同角色的绑定是完全不一样的绑定。要求通过删除/重建绑定来更改 `roleRef`， 这样可以确保要赋予绑定的所有主体会被授予新的角色（而不是在允许或者不小心修改了 `roleRef` 的情况下导致所有现有主体未经验证即被授予新角色对应的权限）。

命令 `kubectl auth reconcile` 可以创建或者更新包含 RBAC 对象的清单文件， 并且在必要的情况下删除和重新创建绑定对象，以改变所引用的角色。 更多相关信息请参照[命令用法和示例](https://kubernetes.io/zh-cn/docs/reference/access-authn-authz/rbac/#kubectl-auth-reconcile)。

# ServiceAccount

![image-20240509102541999](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131254428.png)

创建一个ServiceAccount

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: dongguo
  namespace: default
```

![image-20240509102907309](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131255053.png)

k8s会默认创建一个对应的令牌

![image-20240509103053836](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131255123.png)

```txt
eyJhbGciOiJSUzI1NiIsImtpZCI6Ii1OSHVrbjNqMXM1QVUzVmV2TGZkU1A4NVdVLXNkUXZUQURCNGFVdkFHMVUifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6ImRvbmdndW8tdG9rZW4tcnZwNXgiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiZG9uZ2d1byIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjUyNjcyYzVmLWNmM2YtNGYyNy05MWQ2LWMwYThlYTMxODUyMSIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpkZWZhdWx0OmRvbmdndW8ifQ.sRkXI8Fr7eTC2-mU0yI95cm-PdzaRsQYoTskYtDpAnUJqksbeIjaALgyTi_ynVGUIhcnB74wq0VRrgPd7GgUi5sPVMWDvXDbXqXbGXpzadBxOp0CK4HJsSeAuRHX-vRfBLpeAoDH7NaIP3yjq7t2XF8hw-8Tu09hqsSL6ch-FJApFHCrvyKs-n3zBFV_IeI3sVRFC78aolnC_HjSItKGeDu61DY2gRLGhn4RzMdi3dG7cofuv3X_q0E4yOzz5syArwimlgsTYSVbkKnlwMSG029YYxqaLd_ZrnpnEZv9fVbHZZJM2tjUPaSw_TJap9U6DbMSYFK6ESFgJmEy4h072g
```

使用这个token登录Dashboard，刚创建的account没有权限，无法查看相关资源

![image-20240509104151388](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131255032.png)

创建role 能够获取所有namespace的权限

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: ns-role
rules:
- apiGroups: [""] # "" 标明 core API 组
  resources: ["namespaces"] #当前角色能操作所有的namespace
  verbs: ["get", "watch", "list"] #获取 监听操作动作
```

![image-20240509105354651](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131255647.png)

account绑定role

```yaml
apiVersion: rbac.authorization.k8s.io/v1
# 此集群角色绑定允许 “manager” 组中的任何人访问任何名字空间中的 Secret 资源
kind: ClusterRoleBinding
metadata:
  name: dongguo-role-binding
subjects:
- kind: ServiceAccount
  name: dongguo      # 'name' 是区分大小写的
  namespace: default
  apiGroup: ""
roleRef:
  kind: ClusterRole
  name: ns-role
  apiGroup: rbac.authorization.k8s.io
```

![image-20240509105516588](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131255698.png)

Dashboard中已经能获取所有namespace，但是namespace中的资源还无法查看，因为没有pod相关的权限

![image-20240509105548667](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131255360.png)



每个名称空间都会有一个默认的服务账号，这个服务账号没有绑定任何的角色

![image-20240509110349419](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131255113.png)

随便查看一个pod，可以看到60行 serviceAccountName: default

如果pod不声明serviceAccountName，pod默认会挂载这个默认服务账号default

```yaml
[root@k8s-master security]# kubectl get pod web-server-77f489685-k2dtg -o yaml
apiVersion: v1
kind: Pod
metadata:
  creationTimestamp: "2024-05-07T15:43:27Z"
  generateName: web-server-77f489685-
  labels:
    app: web-store
    pod-template-hash: 77f489685
  name: web-server-77f489685-k2dtg
  namespace: default
  ownerReferences:
  - apiVersion: apps/v1
    blockOwnerDeletion: true
    controller: true
    kind: ReplicaSet
    name: web-server-77f489685
    uid: 6e1beda6-a48b-4f09-bad7-904fa7674636
  resourceVersion: "1484328"
  uid: 0f163986-1f59-421a-8dd6-e1279028f844
spec:
  affinity:
    podAffinity:
      requiredDuringSchedulingIgnoredDuringExecution:
      - labelSelector:
          matchExpressions:
          - key: app
            operator: In
            values:
            - store
        topologyKey: kubernetes.io/hostname
    podAntiAffinity:
      requiredDuringSchedulingIgnoredDuringExecution:
      - labelSelector:
          matchExpressions:
          - key: app
            operator: In
            values:
            - web-store
        topologyKey: kubernetes.io/hostname
  containers:
  - image: nginx:1.16-alpine
    imagePullPolicy: IfNotPresent
    name: web-app
    resources: {}
    terminationMessagePath: /dev/termination-log
    terminationMessagePolicy: File
    volumeMounts:
    - mountPath: /var/run/secrets/kubernetes.io/serviceaccount
      name: kube-api-access-2rrdb
      readOnly: true
  dnsPolicy: ClusterFirst
  enableServiceLinks: true
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
  - name: kube-api-access-2rrdb
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
    lastTransitionTime: "2024-05-07T15:43:27Z"
    message: '0/3 nodes are available: 1 node(s) had taint {key1: value1}, that the
      pod didn''t tolerate, 1 node(s) had taint {key2: value2}, that the pod didn''t
      tolerate, 1 node(s) had taint {node-role.kubernetes.io/master: }, that the pod
      didn''t tolerate.'
    reason: Unschedulable
    status: "False"
    type: PodScheduled
  phase: Pending
  qosClass: BestEffort
```

