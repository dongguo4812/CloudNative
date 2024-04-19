



# Kubernetes 中部署一个应用

在 Kubernetes 集群中创建一个运行 Nginx 的部署，使得 Nginx 服务可以在集群中运行和访问。

```shell
kubectl create deployment my-nginx --image=nginx
```

1. `my-nginx`: 这是部署的名称，用于标识部署对象。在这种情况下，部署的名称是 `my-nginx`。
2. `--image=nginx`: 这是部署中要运行的容器镜像的名称和版本。在这个命令中，我们使用了 Nginx 镜像作为容器的基础镜像。Kubernetes 将会根据这个镜像创建并运行一个或多个 Pod。

![image-20240418191714040](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191023246.png)

注在 Kubernetes 1.18 版本及以后，`kubectl run` 和 `kubectl create deployment` 命令创建的资源类型是一样的，都是 Deployment。因此，无论你使用哪个命令，创建的结果都是一个 Deployment 对象



**当前 Kubernetes 集群中所有的 Pod**

```
kubectl get pod
```

可以看到创建了一个名为my-nginx-6b74b79f57-vwx66的pod

![image-20240418191823560](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191023203.png)



**获取当前 Kubernetes 集群中的所有资源**

这包括了 Pods、Services、Deployments、ReplicaSets、StatefulSets 等等。

```
kubectl get all
```

![image-20240418202703571](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191023123.png)

可以看到当执行 `kubectl create deployment my-nginx --image=nginx` 命令时，Kubernetes 会创建以下几个资源：

1. **Deployment（部署）**：部署对象用于描述所需的 Pod 配置，并确保实际运行的 Pod 符合这个配置。在这个例子中，Deployment 名称是 `my-nginx`。
2. **ReplicaSet（副本集）**：ReplicaSet 是 Deployment 控制 Pod 实例数量的底层控制器。它确保在集群中运行指定数量的 Pod 副本，并且可以根据需要进行扩展或缩减。
3. **Pod（容器）**：Pod 是容器运行的实际单元。当创建 Deployment 时，ReplicaSet 会自动创建一个或多个 Pod 实例，这些 Pod 实例会根据 Deployment 中指定的容器镜像进行运行。



**service/kubernetes** ：是 Kubernetes 集群内部的一个特殊的 Service，用于提供 Kubernetes API Server 的访问。

**显示所有命名空间（all namespaces）中的 Pod**

```
kubectl get pod -A -o wide
```

![image-20240418202732834](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191023060.png)

可以看到my-nginx应用部署在k8s-node1节点中，那么我们可以在k8s-node1节点看到对应的容器（k8s中对应的是pod，docker中对应的是容器,pod可以看作为容器组），name为k8s_nginx_my-nginx-6b74b79f57-vwx66_default_db69f3f6-297a-4af9-93c1-ba6b004f6415_0

名称命名规则进行解释：

- `<k8s_namespace>`：`k8s`
- `<app_name>`：`nginx`
- `<controller_name>`：`my-nginx-6b74b79f57-vwx66`
- `<random_suffix>`：`default`
- `<namespace>`：`db69f3f6-297a-4af9-93c1-ba6b004f6415`
- `<pod_uid>`：`0`

![image-20240418204128357](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191023506.png)



## kubectl create deployment原理

命令行会给master的api-server发送要部署nginx的请求，aip-server把这个请求保存到etcd

node节点的kubelet监听到应用的部署，创建pod

![image-20240418202619911](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191023173.png)





## 自我修复机制

==Kubernetes Deployment Controller 提供了一种自我修复机制，用于解决节点故障或维护问题。==

当你创建一个 Deployment 时，Deployment Controller 会持续监控 Pod 的运行状态。如果发现某个 Pod 的节点发生故障或被删除，Deployment Controller 将会在集群中的其他节点上重新创建一个新的 Pod 实例，以确保应用程序的高可用性和稳定性。

### master删除pod自动重新创建

删除pod后，再次查看会发现正在创建一个新的pod

![image-20240419091516291](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191023240.png)

等待一会就创建完成。

![image-20240419091652023](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191023508.png)

可以看到这个新创建的pod是在k8s-node2及诶单上创建的。

![image-20240419091607315](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191023935.png)

### 在k8s-node2节点删除容器

![image-20240419092751532](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191023138.png)

既然删除pod会重新创建，那在node2节点删除my-nginx容器，会是什么情况呢

![image-20240419092844851](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191023154.png)

可以看到，这里master中my-nginx的pod仍然存在，pod：my-nginx-6b74b79f57-bvvxx没有变，pod会自动重新创建容器

![image-20240419093351002](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191023966.png)

从node2显示的容器id可以确定新创建了容器

![image-20240419093421696](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191023261.png)

这就是k8s提供的自我修复机制

## k8s中进入pod命令

在docker中进入容器的命令是 docker exec 

在k8s中进入pod的命令是 kubectl exec，和docker命令是极为相似

```shell
kubectl exec -it my-nginx-6b74b79f57-bvvxx /bin/bash
```

![image-20240419093958090](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191023178.png)

进入pod时会有一个提示，告诉我们使用kubectl exec [POD] [COMMAND]已经被废弃，要求使用kubectl exec [POD] -- [COMMAND]

> kubectl exec [POD] [COMMAND] is DEPRECATED and will be removed in a future version. Use kubectl exec [POD] -- [COMMAND] instead.

这样就没有提示了：

```shell
kubectl exec -it my-nginx-6b74b79f57-bvvxx -- /bin/bash
```

![image-20240419094244294](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191024180.png)



# kubectl run创建pod

`kubectl create deployment` 和 `kubectl run` 都是用于在 Kubernetes 集群中创建应用程序的命令。

`kubectl create deployment` 命令创建的资源类型是一个 Deployment 对象，其中运行一个或多个 Pod。具有自我修复功能

`kubectl run` 命令默认创建的资源类型是 Pod，而不是 Deployment。不具有自我修复功能

## 这是之前kubectl create deployment创建的deploy部署和pod

![image-20240419095110411](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191024426.png)

## kubectl run创建pod

```shell
kubectl run my-nginx2 --image=nginx
```

![image-20240419095347886](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191024924.png)

可以看到只是创建了一个pod：my-nginx2

删除pod：my-nginx

![image-20240419100206028](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191024038.png)

再次查看pod：my-nginx已经不存在了，可见kubectl run创建的pod是不具备自我修复机制的。

![image-20240419100225856](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404191024261.png)

推荐使用kubectl create deployment创建部署和pod