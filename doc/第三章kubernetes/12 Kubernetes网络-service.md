# Kubernetes网络

Kubernetes 网络解决四方面的问题：

- 一个 Pod 中的容器之间通过**本地回路（loopback）通信。**
- 集群网络在不同 pod 之间提供通信。Pod和Pod之间互通
- Service 资源允许你对外暴露 Pods 中运行的应用程序，以支持来自于集群外部的访问。Service和Pod要通
- 可以使用 Services 来发布仅供集群内部使用的服务。

## k8s网络架构图

![image-20240429163413025](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907564.png)

### 访问流程

![image-20240429163429030](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040906140.png)

## 网络连通原理

### 1、Container To Container

![image-20240429163457558](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040906410.png)

### 2、Pod To Pod

1、同节点

![image-20240504090149711](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040906305.png)



2、跨节点

![跨节点上Pod之间发送数据包](file://F:\BaiduNetdiskDownload\%E4%BA%91%E5%8E%9F%E7%94%9F\2(1).%E8%B5%84%E6%96%99\day15\assets\pod-to-pod-different-nodes.4187b249.gif?lastModify=1714379729)

### Pod-To-Service

Pod To Service

![Pod-to-Service](file://F:\BaiduNetdiskDownload\%E4%BA%91%E5%8E%9F%E7%94%9F\2(1).%E8%B5%84%E6%96%99\day15\assets\pod-to-service.6718b584.gif?lastModify=1714379766)

Service-To-Pod

![image-20240504090200747](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040906274.png)

### Internet-To-Service

Pod-To-Internet

![image-20240504090309677](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040906681.png)

Internet-To-Pod（LoadBalancer -- Layer4）

![image-20240504090317470](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040906370.png)

Internet-To-Pod（Ingress-- Layer7）

![image-20240504090325368](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040906843.png)

# Service

负载均衡服务。让一组Pod可以被别人进行服务发现。

别人只需要访问这个Service。Service还会基于Pod的探针机制（ReadinessProbe：就绪探针）完成Pod的自动剔除和上线工作。

- Service即使无头服务。别人（Pod）不能用ip访问，但是可以用service名当成域名访问。

- **Service的名字还能当成域名被Pod解析**

service中的type可选值如下，代表四种不同的服务发现类型

- **`ClusterIP`** ：通过集群的内部 IP 暴露服务，选择该值时服务只能够在集群内部访问。 这也是默认的 `ServiceType`。
- [`NodePort`](https://kubernetes.io/zh/docs/concepts/services-networking/service/#nodeport)：通过每个节点上的 IP 和静态端口（`NodePort`）暴露服务。 `NodePort` 服务会路由到自动创建的 `ClusterIP` 服务。 通过请求 `<节点 IP>:<节点端口>`，你可以从集群的外部访问一个 `NodePort` 服务。
- [`LoadBalancer`](https://kubernetes.io/zh/docs/concepts/services-networking/service/#loadbalancer)：使用云提供商的负载均衡器向外部暴露服务。 外部负载均衡器可以将流量路由到自动创建的 `NodePort` 服务和 `ClusterIP` 服务上。
- [`ExternalName`](https://kubernetes.io/zh/docs/concepts/services-networking/service/#externalname)：通过返回 `CNAME` 和对应值，可以将服务映射到 `externalName` 字段的内容（例如，`foo.bar.example.com`）。 无需创建任何类型代理。

## Service字段解析

```shell
[root@k8s-master service]# kubectl explain service.spec
KIND:     Service
VERSION:  v1

RESOURCE: spec <Object>

DESCRIPTION:
     Spec defines the behavior of a service.
     https://git.k8s.io/community/contributors/devel/sig-architecture/api-conventions.md#spec-and-status

     ServiceSpec describes the attributes that a user creates on a service.

FIELDS:
   allocateLoadBalancerNodePorts	<boolean>
#如果设置为 true，则允许为 LoadBalancer 类型的服务分配节点端口。
   clusterIP	<string>
#指定服务在 Kubernetes 集群内部的 IP 地址。只能在集群内部访问。
   clusterIPs	<[]string>
#允许为服务指定多个集群 IP 地址。列表中的每个 IP 将具有与主集群 IP 相同的行为。

   externalIPs	<[]string>
#指定一个 IP 地址列表，集群中的节点也会接受该地址的流量，并将其路由到相应的 Pod。

   externalName	<string>
#当你希望通过 DNS 名称而不是 IP 地址访问外部服务时使用。将服务映射到一个 DNS 名称（例如，externalName: my.database.example.com）。

   externalTrafficPolicy	<string>
#指定外部流量的负载均衡策略。可设置为 "Cluster" 或 "Local"。"Cluster" 将流量均匀分布在集群中的所有节点上，而 "Local" 仅将流量发送到运行服务的节点上。

   healthCheckNodePort	<integer> #type为LoadBalancer时才生效
#指定健康检查的节点端口。用于在外部负载均衡器上配置健康检查。

   internalTrafficPolicy	<string>
#指定内部流量的负载均衡策略。可设置为 "Cluster" 或 "Local"。"Cluster" 将流量均匀分布在集群中的所有 Pod 上，而 "Local" 仅将流量发送到与客户端位于同一节点上的 Pod 上。

   ipFamilies	<[]string> #默认为IPv4
#指定 LoadBalancer 类型的服务应使用的 IP 家族。

   ipFamilyPolicy	<string> #默认为SingleStack
#指定用于 LoadBalancer 类型服务的 IP 家族选择策略

   loadBalancerClass	<string> #云厂商实现
#指定服务使用的负载均衡器的类别。用于选择特定的负载均衡器实现。

   loadBalancerIP	<string>
#指定负载均衡器的 IP 地址。当你想为负载均衡器分配特定的 IP 地址时使用。

   loadBalancerSourceRanges	<[]string> #type为LoadBalancer时设置
#指定允许访问负载均衡器的客户端 IP 范围。用于支持基于源 IP 的过滤的负载均衡器。

   ports	<[]Object>
#定义一个端口的数组。每个端口定义指定了服务应该监听的端口号和使用的协议。
   publishNotReadyAddresses	<boolean>
#如果设置为 true，则允许包含尚未就绪的终端节点的服务端点列表。

   selector	<map[string]string>
#标签映射，定义了该服务负责的一组 Pod。type为ClusterIP, NodePort, and LoadBalancer时设置

   sessionAffinity	<string>
#指定要使用的会话亲和性类型。可设置为 "None"、"ClientIP" 或 "ClientIP"（基于源 IP 哈希的会话亲和性）。

   sessionAffinityConfig	<Object>
#指定会话亲和性的其他配置，如客户端 IP 超时。

   topologyKeys	<[]string>
#指定用于选择服务应该暴露在哪些节点上的节点标签的键。

   type	<string>
#指定服务的类型。可设置为ExternalName, ClusterIP, NodePort, and LoadBalancer
```



## ClusterIP

1）部署创建3个副本

```yaml
apiVersion: apps/v1
kind: Deployment	
metadata:	       
  name: nginx-deployment	
  labels:	   
    app: nginx-deployment	
spec:	       
  replicas: 3	
  selector:	   
    matchLabels: 
      app: nginx-deployment
  template:	   
    metadata:	
      labels:	
        app: nginx-deployment
    spec:	    
      containers:	
      - name: nginx	
        image: nginx
```

![image-20240429191143437](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040906842.png)

Service 通过 `selector` 字段选择具有标签 `app: nginx-deployment	` 的 Pod，并将流量路由到这些 Pod 上的端口 80。

```yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx-service	
spec:	    
  selector:	   
    app: nginx-deployment	
  ports:
  - name: nginx-port		    
    port: 80	          
    targetPort: 80	
  type: ClusterIP	
```

![image-20240429191248337](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907600.png)

为了测试负载均衡的效果，修改三个pod中index.html文件

![image-20240429191456048](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907290.png)

三个pod分别修改为111,222,333



访问nginx-service 

![image-20240429191906521](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907257.png)

在任意pod中可以通过服务名进行通信

![image-20240429192251422](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907393.png)

2）通过上面的方式创建的service的ip是10.96.xxx.xxx随机生成的，我们也可以指定service的ip

```yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx-service	
spec:	    
  selector:	   
    app: nginx-deployment	
  ports:
  - name: nginx-port		    
    port: 80	          
    targetPort: 80	
  type: ClusterIP	
  clusterIP: 10.96.100.100
```

![image-20240430114526058](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907862.png)

3）clusterIP不仅可以指定ip，还可以设置为clusterIP: None，不给service分配ip，叫做Headless Service，即无头服务。Headless Service 不会分配 Cluster IP，而是直接将 DNS 记录映射到与 Service 匹配的 Pod 的 IP 地址。这使得可以直接访问每个 Pod，而不经过 Service 的负载均衡。

![image-20240430133637309](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907929.png)

其他pod仍然可以通过服务名进行通信

![image-20240430133834244](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907798.png)

## NodePort

1）

```yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx-service
  namespace: default
spec:	    
  selector:	   
    app: nginx-deployment
  type: NodePort
  ports:
    - protocol: TCP
      port: 80  # service 80
      targetPort: 80  #目标80
```

![image-20240430135951669](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907181.png)

可以看到service对外暴露端口32387

![image-20240430140232669](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907145.png)

每个节点都会监听这个端口，所以访问任意节点的32387端口都能得到响应。

![image-20240430140528821](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907969.png)

2）NodePort默认自动生成的端口位于 30000 到 32767 之间，这是 Kubernetes 预留给 NodePort 的端口范围。

provided port is not in the valid range. The range of valid ports is 30000-32767。

也可以指定端口nodePort: xxxxx

```yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx-service
  namespace: default
spec:	    
  selector:	   
    app: nginx-deployment
  type: NodePort
  ports:
    - protocol: TCP
      port: 80  # service 80
      targetPort: 80  #目标80
      nodePort: 30001  #自定义
```

![image-20240430141601691](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907306.png)

Endpoint 包含了可以处理该 Service 请求的 Pod 的 IP 地址和端口号。

当你创建一个 Service 时，Kubernetes 会自动创建与之关联的 Endpoint。这个 Endpoint 会根据 Service 的选择器（selector）来查找相应的 Pod，并将它们的 IP 地址和端口号添加到 Endpoint 中。

Endpoint 的作用是将请求从 Service 转发到后端的 Pod。当 Service 接收到请求时，它会查找 Endpoint，然后将请求转发到 Endpoint 中列出的 Pod 地址和端口。

![image-20240430141959770](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907006.png)

endpoint也是k8s种的一种资源，可以通过 kubectl get ep查看。

![image-20240430142115004](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907371.png)

当pod出现问题时，会自动删除其对应的endpoint信息

![image-20240430144809408](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907657.png)

## 创建无Selector的Service

- 我们可以创建Service不指定Selector

```yaml
# 无selector的svc
apiVersion: v1
kind: Service
metadata:
  name: service-no-selector
spec:
  type: ClusterIP
  ports:
  - protocol: TCP
    name: http  ###一定注意，name可以不写，但是这里如果写了name，那么endpoint里面的ports必须有同名name才能绑定
    port: 80  # service 80
    targetPort: 80  #目标80
```

![image-20240430150204446](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907262.png)

Service不指定Selector，没有与之关联的pod，就无法进行访问

![image-20240430150305510](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907672.png)

- 手动创建EndPoint，指定一组Pod地址。

Endpoints中有两处要和service保持一致。subsets中ip可以指定任意pod的ip，

![image-20240430145907738](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907097.png)

指定百度的ip：110.242.68.3，和两个pod的ip

![image-20240430150600186](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907401.png)



```yaml
apiVersion: v1
kind: Endpoints
metadata:
  name: service-no-selector  ## 和service同名
  namespace: default
subsets:
- addresses:
  - ip: 10.244.36.82
  - ip: 10.244.169.141
  - ip: 110.242.68.3
  ports:
  - name: http  ## ep和service要是一样的
    port: 80
    protocol: TCP    
```

![image-20240430150646232](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907680.png)

尝试访问service

![image-20240430151038547](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907786.png)

场景：Pod要访问 MySQL。 MySQL单独部署到很多机器，每次记ip麻烦

我们可以创建了一个名为 `mysql-service` 的 Service，没有指定选择器，并将端口 3306 暴露出去。然后，我们手动创建了一个名为 `mysql-service` 的 Endpoint，并指定了 MySQL 实例的 IP 地址和端口。你可以根据实际情况更新 Endpoint，以反映当前运行的 MySQL 实例的 IP 地址。

## ExternalName

- 其他的Pod可以通过访问这个service而访问其他的域名服务

```yaml
apiVersion: v1
kind: Service
metadata:
  name: service-external-name
  namespace: default
spec:
  type: ExternalName
  externalName: nginx-service.default.svc.cluster.local
```

![image-20240430155314042](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040907162.png)

可以通过 Kubernetes 集群内的 Service 名称 `service-external-name` 来访问外部服务 `baidu.com`，而不必直接使用 `nginx-service.default.svc.cluster.local` 这个域名。

但是需要注意目标服务的跨域问题,这里nginx-service.default.svc.cluster.local为服务tnginx-service的内网域名



![image-20240430161155204](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908961.png)

## LoadBalancer

当你在 Kubernetes 中使用 LoadBalancer 服务时，它将为你的服务创建一个外部负载均衡器，允许外部流量通过该负载均衡器访问你的服务。

Kubernetes本身提供了Load Balancer类型的服务，但没有提供该服务的实现。目前除了使用公有云的LB，已经有MetalLB、OpenELB等可用的私有化第三方实现。本文将使用MetalLB在私有实验环境中安装MetalLB，并建立可用的Load Balancer服务。

MetalLB官网：https://metallb.universe.tf/installation/

### 安装MetalLB

下载MetalLB的yaml声明文件到本地

```shell
curl -O https://raw.githubusercontent.com/metallb/metallb/v0.13.12/config/manifests/metallb-native.yaml
```

![image-20240430163354065](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908456.png)

apply

![image-20240430163417415](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908155.png)

安装后，会建立一个专用的metallb-system命名空间，并开始下载并拉起1个controller和3个speaker

controller位于某个worker节点，3个speaker则在所有worker和master节点上各一个，由于要从外网拉镜像，需要耐心等待其完成

![image-20240430163922199](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908641.png)



metallb还会建立一个webhook-service的service

![image-20240430163942465](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908434.png)

metallb有一套自己的api资源，安装后查看kubectl的api-resources，多了下面的内容

![image-20240430164009639](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908346.png)

注意这里的APIVERSION，虽然都显示beta版本，但基本可以放心地在对外服务不极限的生产环境上使用。

###  LB预定义IP池

在使用LB的service之前，先要预定义一个IPAddressPool，pool里定义可以以LB方式做服务的IP段。

这个段和节点的IP是同网段的其他IP，作为预留出来的池子，每个池子里的IP，和KeepAlive的服务IP非常类似。

之后还需要定义个L2Advertisement，将上面IPAddressPool里的每个IP广播出去，让每个节点都知道。

注意IPAddressPool和L2Advertisement，都要放到metal的ns中。

lb_ippool.yaml

```yaml
apiVersion: metallb.io/v1beta1
kind: IPAddressPool
metadata:
  name: lb-ip-pool
  namespace: metallb-system
spec:
  addresses:
    - 192.168.122.110-192.168.122.119
---
apiVersion: metallb.io/v1beta1
kind: L2Advertisement
metadata:
  name: l2adver
  namespace: metallb-system
spec:
  ipAddressPools:
    - lb-ip-pool
```

这里虚拟机的NAT网段为192.168.122.0，在该网段上，规划没有使用的110-119为LB服务IP。

![image-20240430164234121](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908896.png)

### 应用LB前的应用服务

假设已经有了一个无状态应用nginx，容器暴露了nginx默认的80端口，它的pod和现有服务状态如下：

![image-20240430164351765](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908632.png)

在定义LB类型的service之前，已经为nginx定义了一个名为nginx-service的NodePort类型的服务。

该服务在集群内，可以通过http://10.96.155.83:80访问；

该服务在集群外，可以通过http://<任意集群节点的IP>:30001访问。

该服务还没有EXTERNAL-IP。

### 建立LB服务

为一个已有的应用pod，定义并应用LoadBalance类型的Service。

lb_service.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: nginxlb
spec:
  type: LoadBalancer
  loadBalancerIP: 192.168.122.111
  ports:
  - port: 80
    targetPort: 80
    protocol: TCP
    name: http
  selector:
    app: nginx-deployment
```

这里定义了一个典型的，类型为LoadBalancer，名为nginxlb的服务，显式指定了IP为192.168.122.111，该IP必须在前面定义的pood的地址段中。如果不指定，则该service会从预定义的IP池中拿一个IP，作为loadBalancerIP。

spec.selector.app，指向运行中的应用nginx-deployment。

![image-20240430164742752](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908169.png)

这时，原来NodePort类型的nginx服务依然有效，多了一个nginxlb服务。

nginxlb服务是处于第三级的LoadBalancer类型服务，兼容处于第二级的NodePort类型，兼容处于第一级的ClusterIP类型。

nginxlb提供的第一级服务：集群内，通过10.96.203.159:80访问。

nginxlb提供的第二级服务：集群外，通过<任意集群节点的IP>:30001访问。

nginxlb提供的第三级服务：集群外，通过192.168.122.111:80访问。

外部访问：http://192.168.122.111:80

![image-20240430164921684](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405040908121.png)



