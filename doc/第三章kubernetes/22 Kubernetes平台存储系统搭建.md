存储系统是个专业性的整个体系，我们只用常见方式搭建，具体优化需要参照公司自己的产品等各种进行调整

# Ceph

https://ceph.io/

## 基本概念

Ceph可以有

- Ceph对象存储：键值存储，其接口就是简单的GET,PUT,DEL等。如七牛，阿里云oss等
- Ceph块设备：**AWS的EBS**，**青云的云硬盘**和**阿里云的盘古系统**，还有**Ceph的RBD**(RBD是Ceph面向块存储的接口)
- Ceph文件系统：它比块存储具有更丰富的接口，需要考虑目录、文件属性等支持，实现一个支持并行化的文件存储应该是最困难的。

一个Ceph存储集群需要

- 至少一个Ceph监视器、Ceph管理器、Ceph OSD（对象存储守护程序）
- 需要运行Ceph文件系统客户端，则需要部署 Ceph Metadata Server。

![image-20240513171218599](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172244133.png)

- **Monitors**:  [Ceph Monitor](https://docs.ceph.com/en/latest/glossary/#term-Ceph-Monitor) (`ceph-mon`) 监视器：维护集群状态信息
  - 维护集群状态的映射，包括监视器映射，管理器映射，OSD映射，MDS映射和CRUSH映射。 
  - 这些映射是Ceph守护程序相互协调所必需的关键群集状态。 
  - 监视器还负责管理守护程序和客户端之间的身份验证。 
  - 通常至少需要三个监视器才能实现冗余和高可用性。
- **Managers**: [Ceph Manager](https://docs.ceph.com/en/latest/glossary/#term-Ceph-Manager) 守护进程(`ceph-mgr`) : 负责跟踪运行时指标和Ceph集群的当前状态
  - Ceph Manager守护进程（ceph-mgr）负责跟踪运行时指标和Ceph集群的当前状态
  - 包括存储利用率，当前性能指标和系统负载。
  - Ceph Manager守护程序还托管基于python的模块，以管理和公开Ceph集群信息，包括基于Web的Ceph Dashboard和REST API。 
  - 通常，至少需要两个管理器才能实现高可用性。
- **Ceph OSDs**: [Ceph OSD](https://docs.ceph.com/en/latest/glossary/#term-Ceph-OSD) (对象存储守护进程, `ceph-osd`) 【存储数据】
  - 通过检查其他Ceph OSD守护程序的心跳来存储数据，处理数据复制，恢复，重新平衡，并向Ceph监视器和管理器提供一些监视信息。 
  - 通常至少需要3个Ceph OSD才能实现冗余和高可用性。
- **MDSs**: [Ceph Metadata Server](https://docs.ceph.com/en/latest/glossary/#term-Ceph-Metadata-Server) (MDS, `ceph-mds`ceph元数据服务器)
  -  存储能代表 [Ceph File System](https://docs.ceph.com/en/latest/glossary/#term-Ceph-File-System) 的元数据(如：Ceph块设备和Ceph对象存储不使用MDS).
  -  Ceph元数据服务器允许POSIX文件系统用户执行基本命令（如ls，find等），而不会给Ceph存储集群带来巨大负担

https://docs.ceph.com/en/latest/install/

![image-20240514095307736](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172244916.png)

安装ceph有多种方式，推荐使用Cephadmin安装ceph集群或者使用Rook部署一个管理ceph集群的系统

# Rook

https://rook.io/

## 基本概念

Rook是云原生平台的存储编排工具

Rook工作原理如下：

![image-20240513171303310](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172244003.png)

Rook架构如下

![image-20240513171316212](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172244635.png)

RGW：为Restapi Gateway



## operator是什么

k8s中operator+CRD（CustomResourceDefinitions【k8s自定义资源类型】），可以快速帮我们部署一些有状态应用集群，如redis，mysql，Zookeeper等。

Rook的operator是我们k8s集群和存储集群之间进行交互的解析器



# 部署

参考：https://rook.io/docs/rook/v1.6/ceph-quickstart.html

## 添加一个硬盘

![image-20240513190301475](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172245420.png)





rook的版本大于1.3，无法使用目录创建集群，要使用单独的裸盘进行创建，也就是创建一个新的磁盘，挂载到宿主机，不进行[格式化](https://so.csdn.net/so/search?q=格式化&spm=1001.2101.3001.7020)，直接使用即可，我们已经准备好了sdb。

```shell
lsblk -f
```

![image-20240513190429797](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172245106.png)

找到自己磁盘的位置

![image-20240514100346053](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172245755.png)



清除磁盘数据，避免出现磁盘不识别问题。云厂商提供的磁盘都这么磁盘清0

```shell
dd if=/dev/zero of=/dev/sdb bs=1M status=progress
```



## 部署&修改operator

克隆rook项目，该项目中存在部署相关的yaml。

```shell
git clone --single-branch --branch v1.6.11 https://github.com/rook/rook.git
```

![image-20240514101553085](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172245002.png)

找到operator.yaml

```shell
cd rook/cluster/examples/kubernetes/ceph
```

![image-20240514101615784](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172245524.png)

1）修改operator.yaml

把以前的默认镜像换成能用的，如下

```shell
## 建议修改以下的东西。在operator.yaml里面,注意代码对齐

  ROOK_CSI_CEPH_IMAGE: "registry.cn-hangzhou.aliyuncs.com/dongguo/cephcsi:v3.3.1"
  ROOK_CSI_REGISTRAR_IMAGE: "registry.cn-hangzhou.aliyuncs.com/dongguo/csi-node-driver-registrar:v2.0.1"
  ROOK_CSI_RESIZER_IMAGE: "registry.cn-hangzhou.aliyuncs.com/dongguo/csi-resizer:v1.0.1"
  ROOK_CSI_PROVISIONER_IMAGE: "registry.cn-hangzhou.aliyuncs.com/dongguo/csi-provisioner:v2.0.4"
  ROOK_CSI_SNAPSHOTTER_IMAGE: "registry.cn-hangzhou.aliyuncs.com/dongguo/csi-snapshotter:v4.0.0"
  ROOK_CSI_ATTACHER_IMAGE: "registry.cn-hangzhou.aliyuncs.com/dongguo/csi-attacher:v3.0.2"
```

![image-20240514101228712](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172245634.png)

修改镜像： `rook/ceph:v1.6.3`   换成  `registry.cn-hangzhou.aliyuncs.com/dongguo/rook-ceph:v1.6.3`

![image-20240514133741995](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172245427.png)

```shell
kubectl create -f crds.yaml -f common.yaml -f operator.yaml
```

- `crds.yaml`：定义了 Rook 所需的 CRDs。 CustomResourceDefinitions (自定义资源)；
- `common.yaml`：包含了常见的配置和 RBAC 规则。
- `operator.yaml`：部署了 Rook 操作员，该操作员基于 `crds.yaml` 中定义的 CRDs 来管理 Rook 集群。



```shell
kubectl -n rook-ceph get pod
```

![image-20240514141638017](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172245727.png)



2）修改`cluster.yaml`使用我们指定的磁盘当做存储节点即可

```
  storage: # cluster level storage configuration and selection
    useAllNodes: false
    useAllDevices: false
    config:
      osdsPerDevice: "2" #每个设备osd数量
    nodes:
    - name: "k8s-node1"
      devices: 
      - name: "sdb"
    - name: "k8s-node2"
      devices: 
      - name: "sdb"
    - name: "k8s-node3"
      devices: 
      - name: "sdb"
```

![image-20240514134126842](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172245674.png)

镜像： `ceph/ceph:v15.2.13` 换成 `registry.cn-hangzhou.aliyuncs.com/dongguo/ceph-ceph:v15.2.13`

![image-20240514135704977](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172245324.png)



```
kubectl create -f cluster.yaml
```

下载镜像需要很长一段时间，部署完成的最终结果一定要有这些组件

```
kubectl -n rook-ceph get pod
```

![image-20240514143328225](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172245957.png)



## 集群总规划

以后访问 k8s.com:88/4443即可

1）创建证书secret

```shell
#自签名的tls证书
openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout tls.key -out tls.crt -subj "/CN=*.k8s.com/O=*.k8s.com"
#创建证书secret
kubectl create secret tls k8s.com --key tls.key --cert tls.crt
```

![image-20240514152531558](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172245328.png)



![image-20240514152618590](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172245291.png)





2）选择一个之前创建的pod，修改pod的index.html页面

```
kubectl exec -it pod/nginx-01-74c5c489d9-qwlrc -- /bin/bash
echo "hello k8s.com" > /usr/share/nginx/html/index.html
```

![image-20240514153354707](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172245501.png)



3）配置域名使用证书

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: k8s-ingress
  namespace: default
spec:
  tls:
   - hosts:
     - k8s.com
     - rook.k8s.com #以后新增添加域名
     secretName: k8s.com
  defaultBackend:
    service: 
      name: nginx-svc  # ingress在这个名称空间，就找default名称空间的
      port: 
        number: 80
```

pod:nginx-01对应的svc:nginx-svc

![image-20240514153653221](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172245628.png)



![image-20240514160209989](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172245290.png)

![image-20240514160135617](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172245683.png)

4)测试

windows hosts新增192.168.122.146 k8s.com的映射(随便一个ingress的 节点)

访问http://k8s.com:80

![image-20240514154242692](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172245593.png)



访问https://k8s.com:443

![image-20240514154353057](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246650.png)

以后每个东西，配置自己的ingress-rule规则即可。总规则在default名称空间以及完成





## 部署dashboard

https://www.rook.io/docs/rook/v1.6/ceph-dashboard.html

1）之前已经自动部署了dashboard。

![image-20240514162646184](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246482.png)

默认的 ceph 已经安装的 ceph-dashboard，其 SVC 地址是 service clusterIP，并不能被外部访问，配置ingress 进行访问

2）配置Ingress

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: rook-ceph-dashboard-ingress
  namespace: rook-ceph
  annotations:
    kubernetes.io/ingress.class: "nginx"
    nginx.ingress.kubernetes.io/backend-protocol: "HTTPS"
    nginx.ingress.kubernetes.io/server-snippet: |
      proxy_ssl_verify off;
spec:
  rules:
  - host: rook.k8s.com #总配置已经配置了，这里直接使用
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: rook-ceph-mgr-dashboard
            port:
              number: 8443
```

![image-20240514172409526](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246402.png)



![image-20240514172713729](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246118.png)

windows hosts新增192.168.122.146 rook.k8s.com的映射(随便一个ingress的 节点)

3）获取访问密码

```shell
kubectl -n rook-ceph get secret rook-ceph-dashboard-password -o jsonpath="{['data']['password']}" | base64 --decode && echo

#默认账号 admin
l4fH4;YK]q%Jgb'__$U4
```

4）访问 https://rook.k8s.com:443 ingress(配置的是https)

![image-20240516115323761](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246207.png)

Cluster Status中的 Health_Warn可以先不关注，强迫症可使用一下解决

AUTH_INSECURE_GLOBAL_ID_RECLAIM_ALLOWED：这个警告表明Ceph监视器允许使用不安全的全局ID回收。全局ID是用于在Ceph集群中唯一标识对象和其他实体的标识符。回收全局ID是指将已删除对象的ID重新分配给新的对象。默认情况下，Ceph监视器不允许不安全的全局ID回收，因为这可能导致数据不一致。如果您看到这个警告，可能是因为配置中启用了该选项。要解决此警告，建议不使用不安全的全局ID回收，确保数据一致性。
官方解决方案：https://docs.ceph.com/en/latest/rados/operations/health-checks/

###  安装ceph客户端工具

```
cd /root/rook/cluster/examples/kubernetes/ceph
kubectl  apply -f toolbox.yaml
```

![image-20240515172515356](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246067.png)

### 禁用不安全模式

```
#进入toolbox
kubectl -n rook-ceph exec -it rook-ceph-tools-65c94d77bb-tvtjj -- bash
#关闭不安全的全局Id回收
ceph config set mon auth_allow_insecure_global_id_reclaim false
```

![image-20240515172703988](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246977.png)

几秒钟后查看ceph集群状态

![image-20240515172911918](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246449.png)

查看控制台

![image-20240515172946319](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246393.png)

修改密码

![image-20240515202627754](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246953.png)

# 卸载

rook集群的清除

```shell
cd /dongguo/rook/cluster/examples/kubernetes/ceph/

kubectl delete -f crds.yaml -f common.yaml -f operator.yaml -f cluster.yaml
```



```shell
kubectl -n rook-ceph get cephcluster
#删除指定 CephCluster 对象的 finalizers 的。finalizers 是 Kubernetes 中用于确保资源被正确清理的机制之一。
kubectl -n rook-ceph patch cephclusters.ceph.rook.io rook-ceph -p '{"metadata":{"finalizers": []}}' --type=merge
```

![image-20240516111014288](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246976.png)

清除所有节点的 /var/lib/rook 目录

```shell
rm -rf /var/lib/rook
```



删除 Rook 创建的特定 Ceph 块池

```
kubectl -n rook-ceph patch cephblockpool.ceph.rook.io replicapool -p '{"metadata":{"finalizers": []}}' --type=merge
```

![image-20240516111110221](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172246505.png)

格式化挂载的磁盘，恢复可用

```
dd if=/dev/zero of=/dev/sdb bs=1M status=progress
```





kubectl get XXX  确保ceph相关资源已全部删除
