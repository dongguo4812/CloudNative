为什么需要Ingress？

- Service可以使用NodePort暴露集群外访问端口，但是性能低下不安全
- 缺少**Layer7**(应用层)的统一访问入口，可以负载均衡、限流等
- [Ingress](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.21/#ingress-v1beta1-networking-k8s-io) 公开了从集群外部到集群内[服务](https://kubernetes.io/zh/docs/concepts/services-networking/service/)的 HTTP 和 HTTPS 路由。 流量路由由 Ingress 资源上定义的规则控制。
- 我们使用Ingress作为整个集群统一的入口，配置Ingress规则转到对应的Service

类似GateWay

![image-20240502093747511](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909003.png)

# nginx ingress

这是nginx官方做的，适配k8s的，分为**开源版**和**nginx plus版（收费）**。

[文档地址](https://docs.nginx.com/nginx-ingress-controller/overview/)https://www.nginx.com/products/nginx-ingress-controller

![image-20240502101151073](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908881.png)

# ingress nginx

[Ingress | Kubernetes](https://kubernetes.io/zh-cn/docs/concepts/services-networking/ingress/#ingress-是什么)

这是k8s官方做的，适配nginx的。这个里面会及时更新一些特性，而且性能很高，也被广泛采用。

你必须拥有一个 [Ingress 控制器](https://kubernetes.io/zh-cn/docs/concepts/services-networking/ingress-controllers) 才能满足 Ingress 的要求。仅创建 Ingress 资源本身没有任何效果。

你可能需要部署一个 Ingress 控制器，例如 [ingress-nginx](https://kubernetes.github.io/ingress-nginx/deploy/)。 你可以从许多 [Ingress 控制器](https://kubernetes.io/zh-cn/docs/concepts/services-networking/ingress-controllers)中进行选择。

## ingress nginx  安装

https://kubernetes.github.io/ingress-nginx/deploy/#bare-metal-clusters

```shell
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v0.46.0/deploy/static/provider/baremetal/deploy.yaml
```



需要修改deploy.yaml的配置，所以先将deploy.yaml下载

需要如下修改：

- 修改ingress-nginx-controller镜像为 `registry.cn-hangzhou.aliyuncs.com/dongguo/ingress-nginx-controller:v0.46.0`

  ![image-20240502161058030](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908722.png)

- 修改Service为ClusterIP

![image-20240502161146504](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908417.png)

- 修改Deployment为DaemonSet，DaemonSet模式会让ingress部署到每一个节点

![image-20240502161211343](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908025.png)

- 修改Container使用主机网络，直接在主机上开辟 80,443端口，无需中间解析，速度更快

- Container使用主机网络，对应的dnsPolicy策略也需要改为主机网络的

  ![image-20240502161238375](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908502.png)

- 修改DaemonSet的nodeSelector:  `node-role: ingress` 。==注：这里只需要给node节点打上`node-role: ingress` 标签，即可快速的加入/剔除 ingress-controller的数量==

![image-20240502161312024](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908000.png)

修改好的yaml如下。

ingress-nginx.yaml

```yaml

apiVersion: v1
kind: Namespace
metadata:
  name: ingress-nginx
  labels:
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx

---
# Source: ingress-nginx/templates/controller-serviceaccount.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  labels:
    helm.sh/chart: ingress-nginx-3.30.0
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/version: 0.46.0
    app.kubernetes.io/managed-by: Helm
    app.kubernetes.io/component: controller
  name: ingress-nginx
  namespace: ingress-nginx
automountServiceAccountToken: true
---
# Source: ingress-nginx/templates/controller-configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  labels:
    helm.sh/chart: ingress-nginx-3.30.0
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/version: 0.46.0
    app.kubernetes.io/managed-by: Helm
    app.kubernetes.io/component: controller
  name: ingress-nginx-controller
  namespace: ingress-nginx
data:
---
# Source: ingress-nginx/templates/clusterrole.yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  labels:
    helm.sh/chart: ingress-nginx-3.30.0
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/version: 0.46.0
    app.kubernetes.io/managed-by: Helm
  name: ingress-nginx
rules:
  - apiGroups:
      - ''
    resources:
      - configmaps
      - endpoints
      - nodes
      - pods
      - secrets
    verbs:
      - list
      - watch
  - apiGroups:
      - ''
    resources:
      - nodes
    verbs:
      - get
  - apiGroups:
      - ''
    resources:
      - services
    verbs:
      - get
      - list
      - watch
  - apiGroups:
      - extensions
      - networking.k8s.io   # k8s 1.14+
    resources:
      - ingresses
    verbs:
      - get
      - list
      - watch
  - apiGroups:
      - ''
    resources:
      - events
    verbs:
      - create
      - patch
  - apiGroups:
      - extensions
      - networking.k8s.io   # k8s 1.14+
    resources:
      - ingresses/status
    verbs:
      - update
  - apiGroups:
      - networking.k8s.io   # k8s 1.14+
    resources:
      - ingressclasses
    verbs:
      - get
      - list
      - watch
---
# Source: ingress-nginx/templates/clusterrolebinding.yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  labels:
    helm.sh/chart: ingress-nginx-3.30.0
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/version: 0.46.0
    app.kubernetes.io/managed-by: Helm
  name: ingress-nginx
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: ingress-nginx
subjects:
  - kind: ServiceAccount
    name: ingress-nginx
    namespace: ingress-nginx
---
# Source: ingress-nginx/templates/controller-role.yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  labels:
    helm.sh/chart: ingress-nginx-3.30.0
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/version: 0.46.0
    app.kubernetes.io/managed-by: Helm
    app.kubernetes.io/component: controller
  name: ingress-nginx
  namespace: ingress-nginx
rules:
  - apiGroups:
      - ''
    resources:
      - namespaces
    verbs:
      - get
  - apiGroups:
      - ''
    resources:
      - configmaps
      - pods
      - secrets
      - endpoints
    verbs:
      - get
      - list
      - watch
  - apiGroups:
      - ''
    resources:
      - services
    verbs:
      - get
      - list
      - watch
  - apiGroups:
      - extensions
      - networking.k8s.io   # k8s 1.14+
    resources:
      - ingresses
    verbs:
      - get
      - list
      - watch
  - apiGroups:
      - extensions
      - networking.k8s.io   # k8s 1.14+
    resources:
      - ingresses/status
    verbs:
      - update
  - apiGroups:
      - networking.k8s.io   # k8s 1.14+
    resources:
      - ingressclasses
    verbs:
      - get
      - list
      - watch
  - apiGroups:
      - ''
    resources:
      - configmaps
    resourceNames:
      - ingress-controller-leader-nginx
    verbs:
      - get
      - update
  - apiGroups:
      - ''
    resources:
      - configmaps
    verbs:
      - create
  - apiGroups:
      - ''
    resources:
      - events
    verbs:
      - create
      - patch
---
# Source: ingress-nginx/templates/controller-rolebinding.yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  labels:
    helm.sh/chart: ingress-nginx-3.30.0
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/version: 0.46.0
    app.kubernetes.io/managed-by: Helm
    app.kubernetes.io/component: controller
  name: ingress-nginx
  namespace: ingress-nginx
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: Role
  name: ingress-nginx
subjects:
  - kind: ServiceAccount
    name: ingress-nginx
    namespace: ingress-nginx
---
# Source: ingress-nginx/templates/controller-service-webhook.yaml
apiVersion: v1
kind: Service
metadata:
  labels:
    helm.sh/chart: ingress-nginx-3.30.0
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/version: 0.46.0
    app.kubernetes.io/managed-by: Helm
    app.kubernetes.io/component: controller
  name: ingress-nginx-controller-admission
  namespace: ingress-nginx
spec:
  type: ClusterIP
  ports:
    - name: https-webhook
      port: 443
      targetPort: webhook
  selector:
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/component: controller
---
# Source: ingress-nginx/templates/controller-service.yaml：不要
apiVersion: v1
kind: Service
metadata:
  annotations:
  labels:
    helm.sh/chart: ingress-nginx-3.30.0
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/version: 0.46.0
    app.kubernetes.io/managed-by: Helm
    app.kubernetes.io/component: controller
  name: ingress-nginx-controller
  namespace: ingress-nginx
spec:
  type: ClusterIP  ## 改为clusterIP
  ports:
    - name: http
      port: 80
      protocol: TCP
      targetPort: http
    - name: https
      port: 443
      protocol: TCP
      targetPort: https
  selector:
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/component: controller
---
# Source: ingress-nginx/templates/controller-deployment.yaml
apiVersion: apps/v1
kind: DaemonSet
metadata:
  labels:
    helm.sh/chart: ingress-nginx-3.30.0
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/version: 0.46.0
    app.kubernetes.io/managed-by: Helm
    app.kubernetes.io/component: controller
  name: ingress-nginx-controller
  namespace: ingress-nginx
spec:
  selector:
    matchLabels:
      app.kubernetes.io/name: ingress-nginx
      app.kubernetes.io/instance: ingress-nginx
      app.kubernetes.io/component: controller
  revisionHistoryLimit: 10
  minReadySeconds: 0
  template:
    metadata:
      labels:
        app.kubernetes.io/name: ingress-nginx
        app.kubernetes.io/instance: ingress-nginx
        app.kubernetes.io/component: controller
    spec:
      dnsPolicy: ClusterFirstWithHostNet   ## dns对应调整为主机网络
      hostNetwork: true  ## 直接让nginx占用本机80端口和443端口，所以使用主机网络
      containers:
        - name: controller
          image: registry.cn-hangzhou.aliyuncs.com/dongguo/ingress-nginx-controller:v0.46.0
          imagePullPolicy: IfNotPresent
          lifecycle:
            preStop:
              exec:
                command:
                  - /wait-shutdown
          args:
            - /nginx-ingress-controller
            - --election-id=ingress-controller-leader
            - --ingress-class=nginx
            - --configmap=$(POD_NAMESPACE)/ingress-nginx-controller
            - --validating-webhook=:8443
            - --validating-webhook-certificate=/usr/local/certificates/cert
            - --validating-webhook-key=/usr/local/certificates/key
          securityContext:
            capabilities:
              drop:
                - ALL
              add:
                - NET_BIND_SERVICE
            runAsUser: 101
            allowPrivilegeEscalation: true
          env:
            - name: POD_NAME
              valueFrom:
                fieldRef:
                  fieldPath: metadata.name
            - name: POD_NAMESPACE
              valueFrom:
                fieldRef:
                  fieldPath: metadata.namespace
            - name: LD_PRELOAD
              value: /usr/local/lib/libmimalloc.so
          livenessProbe:
            httpGet:
              path: /healthz
              port: 10254
              scheme: HTTP
            initialDelaySeconds: 10
            periodSeconds: 10
            timeoutSeconds: 1
            successThreshold: 1
            failureThreshold: 5
          readinessProbe:
            httpGet:
              path: /healthz
              port: 10254
              scheme: HTTP
            initialDelaySeconds: 10
            periodSeconds: 10
            timeoutSeconds: 1
            successThreshold: 1
            failureThreshold: 3
          ports:
            - name: http
              containerPort: 80
              protocol: TCP
            - name: https
              containerPort: 443
              protocol: TCP
            - name: webhook
              containerPort: 8443
              protocol: TCP
          volumeMounts:
            - name: webhook-cert
              mountPath: /usr/local/certificates/
              readOnly: true
          resources:
            requests:
              cpu: 100m
              memory: 90Mi
      nodeSelector:  ## 节点选择器
        node-role: ingress #以后只需要给某个node打上这个标签就可以部署ingress-nginx到这个节点上了
        #kubernetes.io/os: linux  ## 修改节点选择
      serviceAccountName: ingress-nginx
      terminationGracePeriodSeconds: 300
      volumes:
        - name: webhook-cert
          secret:
            secretName: ingress-nginx-admission
---
# Source: ingress-nginx/templates/admission-webhooks/validating-webhook.yaml
# before changing this value, check the required kubernetes version
# https://kubernetes.io/docs/reference/access-authn-authz/extensible-admission-controllers/#prerequisites
apiVersion: admissionregistration.k8s.io/v1
kind: ValidatingWebhookConfiguration
metadata:
  labels:
    helm.sh/chart: ingress-nginx-3.30.0
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/version: 0.46.0
    app.kubernetes.io/managed-by: Helm
    app.kubernetes.io/component: admission-webhook
  name: ingress-nginx-admission
webhooks:
  - name: validate.nginx.ingress.kubernetes.io
    matchPolicy: Equivalent
    rules:
      - apiGroups:
          - networking.k8s.io
        apiVersions:
          - v1beta1
        operations:
          - CREATE
          - UPDATE
        resources:
          - ingresses
    failurePolicy: Fail
    sideEffects: None
    admissionReviewVersions:
      - v1
      - v1beta1
    clientConfig:
      service:
        namespace: ingress-nginx
        name: ingress-nginx-controller-admission
        path: /networking/v1beta1/ingresses
---
# Source: ingress-nginx/templates/admission-webhooks/job-patch/serviceaccount.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: ingress-nginx-admission
  annotations:
    helm.sh/hook: pre-install,pre-upgrade,post-install,post-upgrade
    helm.sh/hook-delete-policy: before-hook-creation,hook-succeeded
  labels:
    helm.sh/chart: ingress-nginx-3.30.0
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/version: 0.46.0
    app.kubernetes.io/managed-by: Helm
    app.kubernetes.io/component: admission-webhook
  namespace: ingress-nginx
---
# Source: ingress-nginx/templates/admission-webhooks/job-patch/clusterrole.yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: ingress-nginx-admission
  annotations:
    helm.sh/hook: pre-install,pre-upgrade,post-install,post-upgrade
    helm.sh/hook-delete-policy: before-hook-creation,hook-succeeded
  labels:
    helm.sh/chart: ingress-nginx-3.30.0
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/version: 0.46.0
    app.kubernetes.io/managed-by: Helm
    app.kubernetes.io/component: admission-webhook
rules:
  - apiGroups:
      - admissionregistration.k8s.io
    resources:
      - validatingwebhookconfigurations
    verbs:
      - get
      - update
---
# Source: ingress-nginx/templates/admission-webhooks/job-patch/clusterrolebinding.yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: ingress-nginx-admission
  annotations:
    helm.sh/hook: pre-install,pre-upgrade,post-install,post-upgrade
    helm.sh/hook-delete-policy: before-hook-creation,hook-succeeded
  labels:
    helm.sh/chart: ingress-nginx-3.30.0
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/version: 0.46.0
    app.kubernetes.io/managed-by: Helm
    app.kubernetes.io/component: admission-webhook
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: ingress-nginx-admission
subjects:
  - kind: ServiceAccount
    name: ingress-nginx-admission
    namespace: ingress-nginx
---
# Source: ingress-nginx/templates/admission-webhooks/job-patch/role.yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: ingress-nginx-admission
  annotations:
    helm.sh/hook: pre-install,pre-upgrade,post-install,post-upgrade
    helm.sh/hook-delete-policy: before-hook-creation,hook-succeeded
  labels:
    helm.sh/chart: ingress-nginx-3.30.0
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/version: 0.46.0
    app.kubernetes.io/managed-by: Helm
    app.kubernetes.io/component: admission-webhook
  namespace: ingress-nginx
rules:
  - apiGroups:
      - ''
    resources:
      - secrets
    verbs:
      - get
      - create
---
# Source: ingress-nginx/templates/admission-webhooks/job-patch/rolebinding.yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: ingress-nginx-admission
  annotations:
    helm.sh/hook: pre-install,pre-upgrade,post-install,post-upgrade
    helm.sh/hook-delete-policy: before-hook-creation,hook-succeeded
  labels:
    helm.sh/chart: ingress-nginx-3.30.0
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/version: 0.46.0
    app.kubernetes.io/managed-by: Helm
    app.kubernetes.io/component: admission-webhook
  namespace: ingress-nginx
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: Role
  name: ingress-nginx-admission
subjects:
  - kind: ServiceAccount
    name: ingress-nginx-admission
    namespace: ingress-nginx
---
# Source: ingress-nginx/templates/admission-webhooks/job-patch/job-createSecret.yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: ingress-nginx-admission-create
  annotations:
    helm.sh/hook: pre-install,pre-upgrade
    helm.sh/hook-delete-policy: before-hook-creation,hook-succeeded
  labels:
    helm.sh/chart: ingress-nginx-3.30.0
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/version: 0.46.0
    app.kubernetes.io/managed-by: Helm
    app.kubernetes.io/component: admission-webhook
  namespace: ingress-nginx
spec:
  template:
    metadata:
      name: ingress-nginx-admission-create
      labels:
        helm.sh/chart: ingress-nginx-3.30.0
        app.kubernetes.io/name: ingress-nginx
        app.kubernetes.io/instance: ingress-nginx
        app.kubernetes.io/version: 0.46.0
        app.kubernetes.io/managed-by: Helm
        app.kubernetes.io/component: admission-webhook
    spec:
      containers:
        - name: create
          image: docker.io/jettech/kube-webhook-certgen:v1.5.1
          imagePullPolicy: IfNotPresent
          args:
            - create
            - --host=ingress-nginx-controller-admission,ingress-nginx-controller-admission.$(POD_NAMESPACE).svc
            - --namespace=$(POD_NAMESPACE)
            - --secret-name=ingress-nginx-admission
          env:
            - name: POD_NAMESPACE
              valueFrom:
                fieldRef:
                  fieldPath: metadata.namespace
      restartPolicy: OnFailure
      serviceAccountName: ingress-nginx-admission
      securityContext:
        runAsNonRoot: true
        runAsUser: 2000
---
# Source: ingress-nginx/templates/admission-webhooks/job-patch/job-patchWebhook.yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: ingress-nginx-admission-patch
  annotations:
    helm.sh/hook: post-install,post-upgrade
    helm.sh/hook-delete-policy: before-hook-creation,hook-succeeded
  labels:
    helm.sh/chart: ingress-nginx-3.30.0
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/instance: ingress-nginx
    app.kubernetes.io/version: 0.46.0
    app.kubernetes.io/managed-by: Helm
    app.kubernetes.io/component: admission-webhook
  namespace: ingress-nginx
spec:
  template:
    metadata:
      name: ingress-nginx-admission-patch
      labels:
        helm.sh/chart: ingress-nginx-3.30.0
        app.kubernetes.io/name: ingress-nginx
        app.kubernetes.io/instance: ingress-nginx
        app.kubernetes.io/version: 0.46.0
        app.kubernetes.io/managed-by: Helm
        app.kubernetes.io/component: admission-webhook
    spec:
      containers:
        - name: patch
          image: docker.io/jettech/kube-webhook-certgen:v1.5.1
          imagePullPolicy: IfNotPresent
          args:
            - patch
            - --webhook-name=ingress-nginx-admission
            - --namespace=$(POD_NAMESPACE)
            - --patch-mutating=false
            - --secret-name=ingress-nginx-admission
            - --patch-failure-policy=Fail
          env:
            - name: POD_NAMESPACE
              valueFrom:
                fieldRef:
                  fieldPath: metadata.namespace
      restartPolicy: OnFailure
      serviceAccountName: ingress-nginx-admission
      securityContext:
        runAsNonRoot: true
        runAsUser: 2000
```



```
kubectl apply -f ingress-nginx.yaml 
```

![image-20240502161548015](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908713.png)

因为当前节点标签没有配置node-role=ingress，所以当前状态都是0

![image-20240502122245562](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908075.png)

给node节点打标签

![image-20240502122506115](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908320.png)



![image-20240502122543847](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909686.png)

为两个node节点分别创建一个pod

# 案例实战

创建pod

```yaml
apiVersion: apps/v1
kind: Deployment	
metadata:	       
  name: ingress-deployment		
spec:	       
  replicas: 3	
  selector:	   
    matchLabels: 
      app: ingress-nginx-deployment
  template:	   
    metadata:	
      labels:	
        app: ingress-nginx-deployment
    spec:	    
      containers:	
      - name: nginx	
        image: nginx
```

service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: ingress-service
spec:	    
  selector:	   
    app: ingress-nginx-deployment
  type: NodePort
  ports:
    - protocol: TCP
      port: 80  # service 80
      targetPort: 80  #目标80
```

![image-20240502163813019](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909505.png)

![image-20240502164926683](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909570.png)

编写 ingress-nginx-test.yaml

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ingress-nginx-test
  namespace: default
spec:
  rules:
  - host: dongguo.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:  ## 指定需要响应的后端服务
          service:
            name: ingress-service  ## kubernetes集群的svc名称
            port:
              number: 80  ## service的端口号
```

- [pathType 详细](https://kubernetes.io/zh/docs/concepts/services-networking/ingress/#path-types)：

  - `Prefix`：基于以 `/` 分隔的 URL 路径前缀匹配。匹配区分大小写，并且对路径中的元素逐个完成。 路径元素指的是由 `/` 分隔符分隔的路径中的标签列表。 如果每个 *p* 都是请求路径 *p* 的元素前缀，则请求与路径 *p* 匹配。
  - `Exact`：精确匹配 URL 路径，且区分大小写。
  - `ImplementationSpecific`：对于这种路径类型，匹配方法取决于 IngressClass。 具体实现可以将其作为单独的 `pathType` 处理或者与 `Prefix` 或 `Exact` 类型作相同处理。

  




![image-20240502164009727](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909833.png)

linux内修改/etc/hosts添加10.96.255.15 dongguo.com

![image-20240502165001284](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909729.png)

window修改C:\Windows\System32\drivers\etc\hosts添加192.168.122.141 dongguo.com

![image-20240502165247864](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909928.png)

```
kubectl exec -it ingress-nginx-controller-kx42w -n ingress-nginx -- /bin/sh
```

进入到pod中可以看到我们配置Ingress规则后，就能自动修改nginx的配置，自动更新为最新配置

![image-20240502165354358](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909515.png)

ingress规则会生效到所有按照了IngressController的机器的nginx配置。

## 默认后端

```yaml
apiVersion: apps/v1
kind: Deployment	
metadata:	       
  name: ingress-deployment2		
spec:	       
  replicas: 3	
  selector:	   
    matchLabels: 
      app: ingress-nginx-deployment2
  template:	   
    metadata:	
      labels:	
        app: ingress-nginx-deployment2
    spec:	    
      containers:	
      - name: nginx	
        image: nginx:1.16.0
```



```yaml
apiVersion: v1
kind: Service
metadata:
  name: ingress-service2
spec:	    
  selector:	   
    app: ingress-nginx-deployment2
  type: NodePort
  ports:
    - protocol: TCP
      port: 80  # service 80
      targetPort: 80  #目标80
```

![image-20240502175305025](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909234.png)

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ingress-nginx-test2
  namespace: default
spec:
  defaultBackend:  ## 指定所有未匹配的默认后端
    service:
      name: ingress-service2
      port: 
        number: 80
  rules:
  - host: dongguo.com
    http:
      paths:
      - path: /a
        pathType: Prefix
        backend:  ## 指定需要响应的后端服务
          service:
            name: ingress-service  ## kubernetes集群的svc名称
            port:
              number: 80  ## service的端口号
```

![image-20240502180724765](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909000.png)

效果

- dongguo.com 下的 非 /a 开头的所有请求，都会到defaultBackend
- 非dongguo.com 域名下的所有请求，也会到defaultBackend

![image-20240502180754443](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909458.png)



![image-20240502180744056](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909987.png)



## 所有配置项ConfigMaps

参考 [ConfigMap - Ingress-Nginx Controller (kubernetes.github.io)](https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/configmap/)

![image-20240502182605212](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909675.png)

![image-20240502184149344](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909193.png)

配置加上

data:
  配置项:  配置值 

```shell
kubectl edit cm ingress-nginx-controller -n ingress-nginx
```

![image-20240502184950510](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909530.png)

成功地修改了 ConfigMap，并添加了新的配置项。新的配置项会在 Ingress-Nginx Controller 中生效

![image-20240502185211625](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909738.png)

## 注解配置Annotations

https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/annotations/

实现限流Rate Limiting

https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/annotations/#rate-limiting

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ingress-nginx-test2
  namespace: default
  annotations:  ##注解
    nginx.ingress.kubernetes.io/limit-rps: "1"   ### 限流的配置
spec:
  defaultBackend: ## 只要未指定的映射路径
    service:
      name: ingress-service2
      port:
        number: 80
  rules:
  - host: dongguo.com
    http:
      paths:
      - path: /a
        pathType: Prefix
        backend:
          service:
            name: ingress-service
            port:
              number: 80
```

![image-20240503092803242](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909320.png)

正常访问

![image-20240503092838955](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909930.png)

被限流

![image-20240503092826003](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909542.png)

## 路径重写

https://kubernetes.github.io/ingress-nginx/examples/rewrite/

Rewrite 功能，经常被用于前后分离的场景

- 前端给服务器发送 / 请求映射前端地址。
- 后端给服务器发送 /api 请求来到对应的服务。但是后端服务没有 /api的起始路径，所以需要ingress-controller自动截串

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ingress-nginx-test2
  namespace: default
  annotations:  ##注解
    nginx.ingress.kubernetes.io/rewrite-target: /$2
spec:
  rules:
  - host: dongguo.com
    http:
      paths:
      - backend:
          service:
            name: ingress-service
            port:
              number: 80
        path: /api(/|$)(.*)  #去除/api/
        pathType: Prefix
```

![image-20240503094357495](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909026.png)

http://dongguo.com/api/ 被重写为http://dongguo.com 访问成功

![image-20240503094430771](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040909147.png)



/api/ 前缀的请求都由ingress-service处理请求

![image-20240503095522179](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910041.png)

没有/api/ 前缀的请求都由ingress nginx处理请求

![image-20240503095454120](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908738.png)

## 会话保持-Session亲和性

https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/annotations/#session-affinity

第一次访问，ingress-nginx会返回给浏览器一个Cookie，以后浏览器带着这个Cookie，保证访问总是抵达之前的Pod；

```yaml
## 部署一个三个Pod的Deployment并设置Service
apiVersion: v1
kind: Service
metadata:
  name: session-affinity
  namespace: default
spec:
  selector:
    app: session-affinity
  type: ClusterIP
  ports:
  - name: session-affinity
    port: 80
    targetPort: 80
    protocol: TCP
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name:  session-affinity
  namespace: default
  labels:
    app:  session-affinity
spec:
  selector:
    matchLabels:
      app: session-affinity
  replicas: 3
  template:
    metadata:
      labels:
        app:  session-affinity
    spec:
      containers:
      - name:  session-affinity
        image:  nginx
```

![image-20240503103008459](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910576.png)

修改pod中index.html页面以作区分

![image-20240503103152553](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910314.png)

体现负载均衡效果

![image-20240503103228795](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910905.png)

编写具有会话亲和的ingress

```yaml
### 利用每次请求携带同样的cookie，来标识是否是同一个会话
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: session-test
  namespace: default
  annotations:
    nginx.ingress.kubernetes.io/affinity: "cookie"
    nginx.ingress.kubernetes.io/session-cookie-name: "my-session"
spec:
  rules:
  - host: dongguo.com
    http:
      paths:
      - path: /   ### 如果以前这个域名下的这个路径相同的功能有配置过，以最后一次生效
        pathType: Prefix
        backend:
          service:
            name: session-affinity   ###
            port:
              number: 80
```

![image-20240503105455298](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910553.png)

先注释亲和性配置

![image-20240503105719542](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910150.png)

访问

![image-20240503105732540](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910225.png)

![image-20240503105737633](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910017.png)

![image-20240503105743349](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910537.png)

取消亲和性配置

![image-20240503105826956](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910647.png)

访问，多次请求都是返回同样的333

![image-20240503105837460](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910067.png)![image-20240503105856019](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910136.png)

浏览器保存着cookie，每次请求携带同样的cookie，来标识是否是同一个会话

![image-20240503110101326](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910619.png)

## 配置SSL

https://kubernetes.github.io/ingress-nginx/user-guide/tls/

生成证书：

OpenSSL 命令用于生成自签名的 TLS 证书和私钥

```shell
openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout tls.key -out tls.cert -subj "/CN=dongguo.com/O=dongguo.com"
```

- `req`：执行证书请求操作。
- `-x509`：指定生成自签名的 X.509 证书。
- `-nodes`：不加密生成的私钥。
- `-days 365`：证书的有效期为 365 天。
- `-newkey rsa:2048`：生成一个新的 RSA 密钥，并指定密钥的长度为 2048 位。
- `-keyout tls.key`：指定生成的私钥保存的文件路径和文件名。
- `-out tls.cert`：指定生成的证书保存的文件路径和文件名。
- `-subj "/CN=dongguo.com/O=dongguo.com"`：设置证书的主题信息，这里设置了通用名称 (CN) 为 `dongguo.com`，组织 (O) 为 `dongguo.com`。

执行这条命令后，会生成一个名为 `tls.key` 的私钥文件和一个名为 `tls.cert` 的自签名证书文件，它们可以用于 TLS/SSL 加密通信。

在 Kubernetes 中创建一个 TLS 类型的 Secret 对象，将 `tls.key` 和 `tls.cert` 文件中的内容存储在该 Secret 中，以供 Ingress 资源使用。

```shell
kubectl create secret tls dongguo-tls --key tls.key --cert tls.cert
```

![image-20240503142037510](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910209.png)

查看dongguo-tls这个Secret 的信息

```yaml
[root@k8s-master ssl]# kubectl get secret dongguo-tls -o yaml
apiVersion: v1
data:
  tls.crt: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURLekNDQWhPZ0F3SUJBZ0lKQU9oSHBocFRpVUpaTUEwR0NTcUdTSWIzRFFFQkN3VUFNQ3d4RkRBU0JnTlYKQkFNTUMyUnZibWRuZFc4dVkyOXRNUlF3RWdZRFZRUUtEQXRrYjI1blozVnZMbU52YlRBZUZ3MHlOREExTURNdwpOakl3TURCYUZ3MHlOVEExTURNd05qSXdNREJhTUN3eEZEQVNCZ05WQkFNTUMyUnZibWRuZFc4dVkyOXRNUlF3CkVnWURWUVFLREF0a2IyNW5aM1Z2TG1OdmJUQ0NBU0l3RFFZSktvWklodmNOQVFFQkJRQURnZ0VQQURDQ0FRb0MKZ2dFQkFMNlhVazNTSE9ISUJBWS9qbWpYSUFTdS9lNkJ3VWFOWWVPZDU0dkFDQ3VnYXNabEtQeS9sYWNMbmNwOQpObWxlbFFZRnZqTzFwaUttOWY4ZkNEdHRwMmVJRU9RY211djNkbG9lV01GOTh3OEZXckQ4c3hQeE1IY0ZMVmluCkF2azlod01YbjFkbFU3KzJiN3RsWk5VZHNNK1lvVUd4MitwdUVNUUJ3dFcyRG5oeUFRQjQ5S2g3WXB3cHhadUEKWEFQYU45Z0ltdEg5dFB0RThOUUc3MnFYcWJwRFEyWUdzNmllTHhmZzZtWHlZUlYyMWR6dEhWeE80OE50SVYwTApRV1l5UEpTQjJJNk9nQ0QrNmo3Y1puM21yUGNocXg4b05rNVl1bWgxeWpjeU5rQmUxUlMxcTBJQXNCa0l3ZVlSClRaZFJjN2o5ZytkcmtpMWE0Nm5CaVhNZk9rY0NBd0VBQWFOUU1FNHdIUVlEVlIwT0JCWUVGTE5lTEtTbUppTjIKWXBTS21nQVhBRHROSmpsSU1COEdBMVVkSXdRWU1CYUFGTE5lTEtTbUppTjJZcFNLbWdBWEFEdE5KamxJTUF3RwpBMVVkRXdRRk1BTUJBZjh3RFFZSktvWklodmNOQVFFTEJRQURnZ0VCQUhqWlMreTdoRU94RGx6cmpjZXFkUzBsCjlYZDN6U0MxSDMxbHpVTjJCbjdBTElSdW5vMzhPOHlHSXVFT2JKYUZaZkViUGN1QnVYR0RhZTdaNWlnSDNpNG4KWlhqZ0Z3MUc4TjZVODcvSGNKZ05wYUFaOEwzN1JRZEdxZzB1T1dCeGdWRzFhT3lDOUdNZjhldU5iVFRSQmgyQgppcnlwRU0yd1ZPV01mU1cwcytSeUVxcDZxNDBDRERoRm1VNWZSelhkSlVRM3pSRUhVYXAwTFBLZkRnREpwVUVTCnhuN2pBTjdrcDRDTWIzRjdaenVSQytzN3VzdXU2OG9WYzB3U216cmJVUExXL0pvY3NFZW1pOENmbWNZRlN2RmYKd1BmT2w5SDlUVjhNemdoUXZGQXVNUzRJTmlZRnlRcVBOOW1lRGRjZHhrSVQ5SElQb001ZEZ5aThxeVhLSzZ3PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==
  tls.key: LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JSUV2Z0lCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktnd2dnU2tBZ0VBQW9JQkFRQytsMUpOMGh6aHlBUUcKUDQ1bzF5QUVydjN1Z2NGR2pXSGpuZWVMd0Fncm9HckdaU2o4djVXbkM1M0tmVFpwWHBVR0JiNHp0YVlpcHZYLwpId2c3YmFkbmlCRGtISnJyOTNaYUhsakJmZk1QQlZxdy9MTVQ4VEIzQlMxWXB3TDVQWWNERjU5WFpWTy90bSs3ClpXVFZIYkRQbUtGQnNkdnFiaERFQWNMVnRnNTRjZ0VBZVBTb2UyS2NLY1diZ0Z3RDJqZllDSnJSL2JUN1JQRFUKQnU5cWw2bTZRME5tQnJPb25pOFg0T3BsOG1FVmR0WGM3UjFjVHVQRGJTRmRDMEZtTWp5VWdkaU9qb0FnL3VvKwozR1o5NXF6M0lhc2ZLRFpPV0xwb2RjbzNNalpBWHRVVXRhdENBTEFaQ01IbUVVMlhVWE80L1lQbmE1SXRXdU9wCndZbHpIenBIQWdNQkFBRUNnZ0VBZnordzU5eGRCREh5M2VrWlA4NUJ4bDYvazdwMlBKQ3d4clBxb0ZlNkhNYWkKQTR3TzdaRk1iWmRINXFYcTZqY0g4Rmx0UUVFTGh4OUtKdWVsSzJodTJLVDBBaGxISmp5MmZEeStyWkZWOHJWMApjNjZFTWRCYXQxeHhoanNidFNUck81bE4vWi9kWnFaN3V2WmJlQjYycWlRUnBqY0Z5V05yTmpLbXp1RWFrRGNHCkxkYkZ5encvalltZHRWSEJCRlVGTEo5NlZsZldqRGlLcTNvc1RuSm91c3Bzc010WGYwa3pWYXIza2YwMGtLbUQKWVJFMjV0OUNZeHJGelRoMEo4QzRKWlY2d0VuNkRkMGpJOWVINjR3ZDlVVXo0YlZSTTR0SlZUVFplc2tnL3hySwoya1FIWFFaNk1rOHlJMlNGUHJOUzkvTFhTWk56S0xnZUg1MGRIVTREb1FLQmdRRG8zZ1N0WEhPZzZSSzlHMkc3ClBoTWw3bDZXYzJBN0ZGUTVrNWp5aUlTM0J1SDBFLzdPa1BSdFF6LzRMYnlpK2pRTHZyNExVYmhNMVNKZUQvVWkKcytITlVvWGZCTE5sUHNwRkxzL2RyUVBmN3ZRYUI2QkZzK2hkdUVsMXNqaDAvWXNoK3ZMUkxPUEI1NmNRcHV4awpONWtrbzNwRDlodktnYWJHNy9kREpqQ2d2UUtCZ1FEUmhqQ3J1cEpnYzZPUjFTVGkzMWc2RmRJaWtsSnJUSEU1CnVIVEdtK3U2OHV4QTZMZ3BMYWhLZGdXMXllak51bnE0cFFDUjQ0Uk9DcVlxODJzUUhaWU93WGk3UndtWGtyWkoKN1hmRUg1Q2dPejlHbGEvWTlqS0JEWU5UTUFoYi96M2pnNlAvY3pva0xZa1RmNlk2NmF5bEM3SkxGR3c3clFxdgpVaE1mVWpuaFV3S0JnUUNKRkdEbWVIenNwU2ROd1Blb0ZLT0srYVllcEs1cU9NNVgwbVgvcDVPUWRuRytqNks0CmtLWUNSOHM1V2hzb1NXY24zdEhhc2ZGdThTdzQrT1hSMXRnK1dLekxtdVhMM01tUExqNVkvUWRCNUZVM21YT2sKZElKOTRRVUZUck5qVXZsN09GR0dCd25QMUlFSkVXb29tSDRERG1UajZ0VnpRUjBmRUpXYXBybUxkUUtCZ1FDbAo5VW83MjgzeGcrYjlyZnhIajJ6TXBWZ0tkaWUrUVpBMWpmdEJEV09NY3JuYk4vb1c4OGtuSVpDb1MwT1JJTEh2CjZ4SW1mRFhoc3VHbmg2TTM0cGRuSEt2S2V4d0g2UGRtV3lmUU9zaE5nbUErUW5aRXZjMkhGdkw4UTBGN3pSSnkKVVdUbkE1Zjc2KzExZWxocXRZaEFPcUdBZ3E2d0hnSW1tRFg1U3lINWJRS0JnRjZEcFVCYnB4ZktxTUE2ZkNXZgpFZkxmRXZhV1dMdHpERHlRcnBQNHc4Wkc4eWJ2eWIvcjJkZUIzTzZmck5XRm5XM21JdnFHcWZ0QWpxSVNmSHI1CldaNHVBVWhxRjdyYU83Y2ZpUTBsUklBVXBOQjRXRG5GcjJVM2JTWUNpZHl3blNGVFJ1WVdJaXg1M1VacExvNzEKdC9icEczUHBBSjJYMk1pWExqL2tmOE1JCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K
kind: Secret
metadata:
  creationTimestamp: "2024-05-03T06:20:11Z"
  name: dongguo-tls
  namespace: default
  resourceVersion: "1094413"
  uid: f7a5bd65-5c28-48aa-8208-0d57cd345602
type: kubernetes.io/tls
```

配置域名使用证书；

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: secret-ingress
  namespace: default
spec:
  tls:
   - hosts:
     - dongguo.com
     secretName: dongguo-tls
  rules:
  - host: dongguo.com
    http:
      paths:
      - path: /b
        pathType: Prefix
        backend:
          service:
            name: ingress-service
            port:
              number: 80
```

![image-20240503142813417](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910101.png)

配置好证书，访问域名，就会默认跳转到https；

![image-20240503142825447](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910745.png)

继续访问

![image-20240503142840966](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910494.png)

浏览器提示证书无效，这是因为证书使我们自己生成的，不被浏览器认可

![image-20240503142929087](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910298.png)

## 灰度发布-Canary

以前可以使用k8s的Service配合Deployment进行金丝雀部署。原理如下

![image-20240503150445637](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910522.png)

缺点：

- 不能自定义灰度逻辑，比如指定用户进行灰度

现在可以使用Ingress进行灰度。原理如下

![image-20240503150503851](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040910541.png)

### 准备环境

使用如下文件部署两个service版本。v1版本返回nginx默认页，v2版本返回 11111

```yaml
apiVersion: v1
kind: Service
metadata:
  name: v1-service
  namespace: default
spec:
  selector:
    app: v1-pod
  type: ClusterIP
  ports:
  - name: http
    port: 80
    targetPort: 80
    protocol: TCP
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name:  v1-deploy
  namespace: default
  labels:
    app:  v1-deploy
spec:
  selector:
    matchLabels:
      app: v1-pod
  replicas: 1
  template:
    metadata:
      labels:
        app:  v1-pod
    spec:
      containers:
      - name:  nginx
        image:  nginx
---
apiVersion: v1
kind: Service
metadata:
  name: canary-v2-service
  namespace: default
spec:
  selector:
    app: canary-v2-pod
  type: ClusterIP
  ports:
  - name: http
    port: 80
    targetPort: 80
    protocol: TCP
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name:  canary-v2-deploy
  namespace: default
  labels:
    app:  canary-v2-deploy
spec:
  selector:
    matchLabels:
      app: canary-v2-pod
  replicas: 1
  template:
    metadata:
      labels:
        app:  canary-v2-pod
    spec:
      containers:
      - name:  nginx
        image:  registry.cn-hangzhou.aliyuncs.com/dongguo/nginx-msg-test:v1.0
```

![image-20240503151049636](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040911002.png)

![image-20240503151127410](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040911554.png)

https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/annotations/#canary

### 测试

版本1，v1-service服务

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: canary-ingress1
  namespace: default
spec:
  rules:
  - host: dongguo.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: v1-service
            port:
              number: 80
```

![image-20240503160515298](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040911281.png)

访问

![image-20240503160538664](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040911369.png)

版本二 两个服务同时生效，并制定访问的规则

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: canary-ingress2
  namespace: default
  annotations:
    nginx.ingress.kubernetes.io/canary: "true" #表示金丝雀版本生效
    nginx.ingress.kubernetes.io/canary-by-header: "aaa"  #header中设置aaa=always 使用金丝雀版本，aaa=never不使用
    nginx.ingress.kubernetes.io/canary-by-cookie: "bbb" #cookie中设置bbb=always，使用金丝雀版本，bbb=never不使用
    nginx.ingress.kubernetes.io/canary-weight: "50" #50%的权重使用金丝雀版本   以上配置优先级从上至下 canary-by-header>canary-by-cookie>canary-weight
   
spec:
  rules:
  - host: dongguo.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: canary-v2-service
            port:
              number: 80
```

![image-20240503162317545](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040911362.png)

使用postman访问

1.默认 权重配置生效

![image-20240503162418322](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040911617.png)

![image-20240503162426879](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040911010.png)

2配置cookie

![image-20240503162930239](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040911379.png)





![image-20240503162904051](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040911795.png)

3.测试header

![image-20240503163006473](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040911611.png)

![image-20240503163017176](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040911025.png)

