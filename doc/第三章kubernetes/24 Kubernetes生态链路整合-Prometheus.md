kube-prometheus-stack 是一个用于监控 Kubernetes 环境的集成解决方案，它包含了一系列工具和组件，主要用于收集、存储和可视化监控数据。以下是 kube-prometheus-stack 中包含的主要工具：

1. **Prometheus**: Prometheus 是一款开源的监控和报警工具，用于收集指标数据、存储时间序列数据，并提供强大的查询语言 PromQL。
2. **kube-state-metrics**: kube-state-metrics 是一个 Kubernetes 的监控指标导出器，它从 Kubernetes 的 API 服务器获取各种对象的状态信息，并将其转换为 Prometheus 指标。
3. **prometheus-node-exporter**: Prometheus Node Exporter 是一个用于导出主机系统级别指标的代理程序。它收集有关主机的诸如 CPU、内存、磁盘和网络等方面的指标，并将其暴露给 Prometheus 进行收集。
4. **Grafana**: Grafana 是一个流行的开源数据可视化工具，它与 Prometheus 集成良好，可以通过配置 Prometheus 数据源来创建丰富的仪表盘，并进行数据分析和监控报告。

 - [prometheus-community/kube-state-metrics](https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-state-metrics)
 - [prometheus-community/prometheus-node-exporter](https://github.com/prometheus-community/helm-charts/tree/main/charts/prometheus-node-exporter)
 - [grafana/grafana](https://github.com/grafana/helm-charts/tree/main/charts/grafana)

# charts下载

https://artifacthub.io/packages/helm/prometheus-community/kube-prometheus-stack



```shell
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

helm pull prometheus-community/kube-prometheus-stack --version 30.0.1  #kube-prometheus-stack 从 30.0.0 版本开始支持 Grafana 8.3.1
```

解压

```shell
tar -xvf kube-prometheus-stack-30.0.1.tgz
```



value.yaml中包含用户可以配置的各种选项和参数，用于定制化安装 kube-prometheus-stack。

## 配置ingress访问证书

>  全局做过的就可以跳过；
>
>  只需要给全局加上域名即可

## 配置定制化文件

value.yaml内容根据需求进行调整为override.yaml

创建三个ingress规则

```shell
alertmanager:
  ingress: 
    enabled: true
    ingressClassName: nginx
    hosts:
      - alertmanager.k8s.com
    paths:
      - /
    pathType: Prefix
    
grafana:
  enabled: true
  defaultDashboardsEnabled: true
  adminPassword: Admin123456
  ingress: 
    enabled: true
    hosts: 
    - grafana.k8s.com
    path: /
    pathType: Prefix
  
prometheus: 
  ingress: 
    enabled: true
    hosts: [prometheus.k8s.com]
    paths:
      - / 
    pathType: Prefix
```



修改grafana版本：grafana 8.3.1

## 安装

```shell
cd kube-prometheus-stack
vi override.yaml
kubectl create ns monitor
helm install -f values.yaml -f override.yaml prometheus-stack ./ -n monitor
```

![image-20240517123107033](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246415.png)





### k8s.gcr.io/相关镜像如果下载失败/超时

查看pod镜像下载失败原因

```shell
[root@k8s-master1 ~]# kubectl get pod -n monitor
NAME                                                READY   STATUS         RESTARTS   AGE
prometheus-stack-kube-prom-admission-create-jb2q4   0/1     ErrImagePull   0          62s

[root@k8s-master1 ~]# kubectl describe pod prometheus-stack-kube-prom-admission-create-jb2q4  -n monitor
Name:         prometheus-stack-kube-prom-admission-create-jb2q4
Namespace:    monitor
Priority:     0
Node:         k8s-node2/192.168.122.147
Start Time:   Fri, 17 May 2024 21:07:47 +0800
Labels:       app=kube-prometheus-stack-admission-create
              app.kubernetes.io/instance=prometheus-stack
              app.kubernetes.io/managed-by=Helm
              app.kubernetes.io/part-of=kube-prometheus-stack
              app.kubernetes.io/version=30.0.1
              chart=kube-prometheus-stack-30.0.1
              controller-uid=903014a4-f8a2-494e-8681-0beaa34fe603
              heritage=Helm
              job-name=prometheus-stack-kube-prom-admission-create
              release=prometheus-stack
Annotations:  <none>
Status:       Pending
IP:           196.16.169.141
IPs:
  IP:           196.16.169.141
Controlled By:  Job/prometheus-stack-kube-prom-admission-create
Containers:
  create:
    Container ID:  
    Image:         k8s.gcr.io/ingress-nginx/kube-webhook-certgen:v1.0@sha256:f3b6b39a6062328c095337b4cadcefd1612348fdd5190b1dcbcb9b9e90bd8068
    Image ID:      
    Port:          <none>
    Host Port:     <none>
    Args:
      create
      --host=prometheus-stack-kube-prom-operator,prometheus-stack-kube-prom-operator.monitor.svc
      --namespace=monitor
      --secret-name=prometheus-stack-kube-prom-admission
    State:          Waiting
      Reason:       ImagePullBackOff
    Ready:          False
    Restart Count:  0
    Environment:    <none>
    Mounts:
      /var/run/secrets/kubernetes.io/serviceaccount from kube-api-access-ltvzw (ro)
Conditions:
  Type              Status
  Initialized       True 
  Ready             False 
  ContainersReady   False 
  PodScheduled      True 
Volumes:
  kube-api-access-ltvzw:
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
  Type     Reason          Age                From               Message
  ----     ------          ----               ----               -------
  Normal   Scheduled       81s                default-scheduler  Successfully assigned monitor/prometheus-stack-kube-prom-admission-create-jb2q4 to k8s-node2
  Normal   SandboxChanged  59s                kubelet            Pod sandbox changed, it will be killed and re-created.
  Warning  Failed          30s (x2 over 59s)  kubelet            Failed to pull image "k8s.gcr.io/ingress-nginx/kube-webhook-certgen:v1.0@sha256:f3b6b39a6062328c095337b4cadcefd1612348fdd5190b1dcbcb9b9e90bd8068": rpc error: code = Unknown desc = Error response from daemon: Get https://k8s.gcr.io/v2/: net/http: request canceled while waiting for connection (Client.Timeout exceeded while awaiting headers)
  Warning  Failed          30s (x2 over 59s)  kubelet            Error: ErrImagePull
  Normal   BackOff         16s (x4 over 58s)  kubelet            Back-off pulling image "k8s.gcr.io/ingress-nginx/kube-webhook-certgen:v1.0@sha256:f3b6b39a6062328c095337b4cadcefd1612348fdd5190b1dcbcb9b9e90bd8068"
  Warning  Failed          16s (x4 over 58s)  kubelet            Error: ImagePullBackOff
  Normal   Pulling         2s (x3 over 74s)   kubelet            Pulling image "k8s.gcr.io/ingress-nginx/kube-webhook-certgen:v1.0@sha256:f3b6b39a6062328c095337b4cadcefd1612348fdd5190b1dcbcb9b9e90bd8068"

```

1）在dokcer仓库里找了一个类似的，通过 `kubectl edit`修改

image: k8s.gcr.io/ingress-nginx/kube-webhook-certgen:v1.0  替换为 ：

registry.cn-hangzhou.aliyuncs.com/dongguo/kube-webhook-certgen:v1.0

![image-20240517220954534](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172247468.png)



2）接下来同样 k8s.gcr.io/kube-state-metrics/kube-state-metrics:v2.3.0 这个镜像没办法拉取

k8s.gcr.io/kube-state-metrics/kube-state-metrics:v2.3.0替换为

registry.cn-hangzhou.aliyuncs.com/dongguo/kube-state-metrics:v2.3.0

![image-20240517124549309](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246601.png)

## 访问

windows hosts新增映射

192.168.122.146 alertmanager.k8s.com 

192.168.122.146 grafana.k8s.com

192.168.122.146 prometheus.k8s.com

### prometheus

https://prometheus.k8s.com:443

如果提示Error fetching server time

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210706152518515.png)

各个节点执行

```
yum install ntpdate
ntpdate ntp.aliyun.com
```

刷新即可

![image-20240517133258019](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246234.png)

![image-20240517133355482](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246122.png)

### grafana

https://grafana.k8s.com:443

账号:admin密码: Admin123456

![image-20240517133728015](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246233.png)

grafana默认提供了各种的监控dashboard

![image-20240517134131212](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246289.png)



![image-20240517134205586](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246802.png)

修改grafana的系统时间

![image-20240517134321651](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246705.png)

#### 引入模板

![image-20240517134523424](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246778.png)

加载模板13105，这个模板要求 Grafana 8.3.1以上

![image-20240517223026793](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246940.png)



![image-20240517134614440](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246723.png)

可以查看k8s集群相关信息

![image-20240517222314278](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246241.png)



![image-20240517222346541](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172247097.png)
