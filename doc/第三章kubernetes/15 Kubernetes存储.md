# 总览

![image-20240505110319136](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071413274.png)

 Kubernetes 目前支持多达 28 种数据卷类型（其中大部分特定于具体的云环境如 GCE/AWS/Azure 等），如需查阅所有的数据卷类型，请查阅 Kubernetes 官方文档 [Volumes](https://kubernetes.io/docs/concepts/storage/volumes/) 。如：

- 非持久性存储  
  - emptyDir
  - HostPath
- 网络连接性存储
  - SAN：iSCSI、ScaleIO Volumes、FC (Fibre Channel)
  - NFS：nfs，cfs
- 分布式存储
  - Glusterfs
  - RBD (Ceph Block Device)
  - CephFS
  - Portworx Volumes
  - Quobyte Volumes
- 云端存储
  - GCEPersistentDisk
  - AWSElasticBlockStore
  - AzureFile
  - AzureDisk
  - Cinder (OpenStack block storage)
  - VsphereVolume
  - StorageOS
- 自定义存储
  - FlexVolume

# 配置

配置最佳实战: 

- 云原生 应用12要素 中，提出了配置分离。https://www.kdocs.cn/view/l/skIUQnbIc6cJ
- 在推送到集群之前，配置文件应存储在**版本控制**中。 这允许您在必要时快速回滚配置更改。 它还有助于集群重新创建和恢复。
- **使用 YAML 而不是 JSON 编写配置文件**。虽然这些格式几乎可以在所有场景中互换使用，但 YAML 往往更加用户友好。
- 建议相关对象分组到一个文件。比如 [guestbook-all-in-one.yaml](https://github.com/kubernetes/examples/tree/master/guestbook/all-in-one/guestbook-all-in-one.yaml) 
- 除非必要，否则不指定默认值：简单的最小配置会降低错误的可能性。
- 将对象描述放在注释中，以便更好地进行内省。



## Secret

- `Secret` 对象类型用来**保存敏感信息**，例如密码、OAuth 令牌和 SSH 密钥。 将这些信息放在 `secret` 中比放在 [Pod](https://kubernetes.io/docs/concepts/workloads/pods/pod-overview/) 的定义或者 [容器镜像](https://kubernetes.io/zh/docs/reference/glossary/?all=true#term-image) 中来说更加安全和灵活。
- `Secret` 是一种包含少量敏感信息例如密码、令牌或密钥的对象。用户可以创建 Secret，同时系统也创建了一些 Secret。



### Secret种类

![image-20240505111200244](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071413791.png)



细分类型

![image-20240505111213318](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071413535.png)



### Pod如何引用

要使用 Secret，Pod 需要引用 Secret。 Pod 可以用三种方式之一来使用 Secret：

- 作为挂载到一个或多个容器上的 [卷](https://kubernetes.io/zh/docs/concepts/storage/volumes/) 中的[文件](https://kubernetes.io/zh/docs/concepts/configuration/secret/#using-secrets-as-files-from-a-pod)。（volume进行挂载）
- 作为[容器的环境变量](https://kubernetes.io/zh/docs/concepts/configuration/secret/#using-secrets-as-environment-variables)（envFrom字段引用）
- 由 [kubelet 在为 Pod 拉取镜像时使用](https://kubernetes.io/zh/docs/concepts/configuration/secret/#using-imagepullsecrets)（此时Secret是docker-registry类型的）

Secret 对象的名称必须是合法的 [DNS 子域名](https://kubernetes.io/zh/docs/concepts/overview/working-with-objects/names#dns-subdomain-names)。 在为创建 Secret 编写配置文件时，你可以设置 `data` 与/或 `stringData` 字段。 `data` 和 `stringData` 字段都是可选的。`data` 字段中所有键值都必须是 base64 编码的字符串。如果不希望执行这种 base64 字符串的转换操作，你可以选择设置 `stringData` 字段，其中可以使用任何字符串作为其取值。



### 创建Secret

#### generic 类型

##### 使用基本字符串

```shell
kubectl create secret generic dev-db-secret \
  --from-literal=username=devuser \
  --from-literal=password='S!B\*d$zDsb='
```

![image-20240506092428200](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071413859.png)

相当于yaml

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: dev-db-secret  
data:
  password: UyFCXCpkJHpEc2I9  ## base64编码了一下
  username: ZGV2dXNlcg==
```

username、password都是使用base64进行了编码

![image-20240506092555608](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071413415.png)





##### 使用文件内容

```shell
echo -n 'admin' > ./username.txt
echo -n '1f2d1e2e67df' > ./password.txt


kubectl create secret generic db-user-pass \
  --from-file=./username.txt \
  --from-file=./password.txt
```

![image-20240506092808324](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071413641.png)

默认密钥名称是文件名。

你可以选择使用 --from-file=[key=]source 来设置密钥名称。如下

```shell
kubectl create secret generic db-user-pass-02 \
  --from-file=uname=./username.txt \
  --from-file=pword=./password.txt
```

![image-20240506092917839](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071414069.png)

##### 获取Secret内容

获取名为 `dev-db-secret` 的 Secret 对象，并以 JSONPath 的方式输出其数据字段。

```shell
kubectl get secret dev-db-secret -o jsonpath='{.data}'
```

![image-20240506093129202](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071415216.png)

输出 `dev-db-secret` Secret 对象中的数据字段，但是输出格式可能不太易读，因为它是以 base64 编码的形式显示的。如果你想要解码这些数据，可以使用类似下面的命令：

![image-20240506093447821](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071415610.png)

### 使用Secret

#### 环境变量引用

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-secret
spec:
  containers:
  - name: pod-secret
    image: busybox
    command: ["/bin/sh", "-c", "sleep 3600"]
    resources:
      limits:
        memory: 128Mi
      requests:
        memory: 64Mi 
    env:
    - name: MY_USER # 容器中的环境变量名字
      valueFrom:
        secretKeyRef: # 从 Secret 中引用
          name: dev-db-secret # 指定 Secret 的名称
          key: username  # 指定 Secret 中的键
    - name: POD_NAME # 设置环境变量为 Pod 的名称
      valueFrom:
        fieldRef: # 引用资源对象信息
          fieldPath: metadata.name # 指定要引用的字段路径
    - name: POD_LIMIT_MEM # 设置环境变量为 Pod 的 CPU 限制
      valueFrom:
        resourceFieldRef: # 引用容器的相关资源
          containerName: pod-secret # 指定容器的名称
          resource: limits.memory # 指定要引用的资源
```

![image-20240506103101911](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071415689.png)

进入pod中获取指定的资源

![image-20240506105013049](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071415456.png)

134217728 字节等于 128 MiB



修改dev-db-secret这个Secret的username，查看pod中这个环境变量是否自动更新

![image-20240506105323727](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416178.png)

![image-20240506105309334](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416708.png)

环境变量引用的方式不会被自动更新

![image-20240506105344548](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416526.png)

#### 卷挂载

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-secret-volume
spec:
  containers:
  - name: pod-secret-volume
    image: busybox
    command: ["/bin/sh", "-c", "sleep 3600"]
    volumeMounts:
    - name: app
      mountPath: "/app"  #容器内的app文件夹挂载secret的资源
  volumes:
  - name: app
    secret:
      secretName: dev-db-secret
```

![image-20240506113627024](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416351.png)

secret里面的所有key都对应一个文件

![image-20240506113712355](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416008.png)



挂载方式的secret 在secret变化的时候会自动更新**（子路径引用除外）**

![image-20240506113746351](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416509.png)

还原为ZGV2dXNlcg==

![image-20240506113849256](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416362.png)

默认secret的资源的key挂载的文件名，可以使用items指定key和对应挂载的文件名

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-secret-volume2
spec:
  containers:
  - name: pod-secret-volume2
    image: busybox
    command: ["/bin/sh", "-c", "sleep 3600"]
    volumeMounts:
    - name: app
      mountPath: "/app"  #容器内的app文件夹挂载secret的资源
  volumes:
  - name: app
    secret:
      secretName: dev-db-secret
      items:
      - key: password
        path: pword
```

![image-20240506114758272](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416111.png)

password挂载到pword

![image-20240506114834928](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416774.png)

注：默认情况下挂载的文件是只读的

![image-20240506133419461](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416452.png)

## ConfigMap

- ConfigMap 来将你的配置数据和应用程序代码分开。
- ConfigMap 是一种 API 对象，用来将非机密性的数据保存到键值对中。使用时， [Pods](https://kubernetes.io/docs/concepts/workloads/pods/pod-overview/) 可以将其用作环境变量、命令行参数或者存储卷中的配置文件。

configMap保存的内容是普通的明文文本

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: game-demo
data:
  # 类属性键；每一个键都映射到一个简单的值
  player_initial_lives: "3"
  ui_properties_file_name: "user-interface.properties"

  # 类文件键
  game.properties: |
    enemy.types=aliens,monsters
    player.maximum-lives=5
  user-interface.properties: |
    color.good=purple
    color.bad=yellow
    allow.textmode=true
```

- `player_initial_lives` 和 `ui_properties_file_name` 是简单的键值对，分别指定了玩家初始生命值和用户界面属性文件的名称。
- `game.properties` 和 `user-interface.properties` 是类文件键，它们包含了多行数据，使用 `|` 符号进行多行数据的定义。

其中：

- `game.properties` 包含了游戏的配置属性，如敌人类型、玩家最大生命值等。
- `user-interface.properties` 包含了用户界面的配置属性，如颜色设置、是否允许文本模式等。

![image-20240506142600694](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416345.png)

你可以使用四种方式来使用 ConfigMap 配置 Pod 中的容器：

1. 在容器命令和参数内
2. 容器的环境变量
3. 在只读卷里面添加一个文件，让应用来读取
4. 编写代码在 Pod 中运行，使用 Kubernetes API 来读取 ConfigMap

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: configmap-demo-pod
spec:
  containers:
    - name: configmap-demo-pod
      image: busybox
      command: ["/bin/sh", "-c", "sleep 3600"]
      env:
        # 定义环境变量
        - name: PLAYER_INITIAL_LIVES # 请注意这里和 ConfigMap 中的键名是不一样的
          valueFrom:
            configMapKeyRef:
              name: game-demo           # 这个值来自 ConfigMap
              key: player_initial_lives # 需要取值的键
        - name: UI_PROPERTIES_FILE_NAME
          valueFrom:
            configMapKeyRef:
              name: game-demo
              key: ui_properties_file_name
      volumeMounts:
      - name: config
        mountPath: "/config"
  volumes:
    # 你可以在 Pod 级别设置卷，然后将其挂载到 Pod 内的容器中
    - name: config
      configMap:
        # 提供你想要挂载的 ConfigMap 的名字
        name: game-demo
        # 来自 ConfigMap 的一组键，将被创建为文件
        items:
        - key: "game.properties"
          path: "game.properties"
        - key: "user-interface.properties"
          path: "user-interface.properties"
```

![image-20240506143633146](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416350.png)

![image-20240506143956940](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416901.png)

**ConfigMap的修改，可以触发挂载文件的自动更新**

修改game.properties中 player.maximum-lives=10，更新需要一段时间

![image-20240506144913618](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416338.png)

# 临时存储

Kubernetes 为了不同的目的，支持几种不同类型的临时卷：

- [emptyDir](https://kubernetes.io/zh/docs/concepts/storage/volumes/#emptydir)： Pod 启动时为空，存储空间来自本地的 kubelet 根目录（通常是根磁盘）或内存
- [configMap](https://kubernetes.io/zh/docs/concepts/storage/volumes/#configmap)、 [downwardAPI](https://kubernetes.io/zh/docs/concepts/storage/volumes/#downwardapi)、 [secret](https://kubernetes.io/zh/docs/concepts/storage/volumes/#secret)： 将不同类型的 Kubernetes 数据注入到 Pod 中
- [CSI 临时卷](https://kubernetes.io/zh/docs/concepts/storage/volumes/#csi-ephemeral-volumes)： 类似于前面的卷类型，但由专门[支持此特性](https://kubernetes-csi.github.io/docs/drivers.html) 的指定 [CSI 驱动程序](https://github.com/container-storage-interface/spec/blob/master/spec.md)提供
- [通用临时卷](https://kubernetes.io/zh/docs/concepts/storage/ephemeral-volumes/#generic-ephemeral-volumes)： 它可以由所有支持持久卷的存储驱动程序提供

## emptyDir

- 当 Pod 分派到某个 Node 上时，`emptyDir` 卷会被创建
- 在 Pod 在该节点上运行期间，卷一直存在。
- 卷最初是空的。 
- 尽管 Pod 中的容器挂载 `emptyDir` 卷的路径可能相同也可能不同，这些容器都可以读写 `emptyDir` 卷中相同的文件。 
- 当 Pod 因为某些原因被从节点上删除时，`emptyDir` 卷中的数据也会被永久删除。
- 存储空间来自本地的 kubelet 根目录（通常是根磁盘）或内存

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: "multi-container-pod"
  namespace: default
  labels:
    app: "multi-container-pod"
spec:
  volumes:    ### 以后见到的所有名字 都应该是一个合法的域名方式
  - name: nginx-vol
    emptyDir: {}  ### docker匿名挂载，外部创建一个位置  /abc
  containers:  ## kubectl exec -it podName  -c nginx-container（容器名）-- /bin/sh
  - name: nginx-container
    image: "nginx"
    volumeMounts:  #声明卷挂载  -v
      - name: nginx-vol
        mountPath: /usr/share/nginx/html
  - name: content-container
    image: "alpine"
    command: ["/bin/sh","-c","while true;do sleep 1; date > /app/index.html;done;"]
    volumeMounts: 
      - name: nginx-vol
        mountPath: /app
```

## 扩展-hostPath

https://kubernetes.io/zh/docs/concepts/storage/volumes/#hostpath

![image-20240506151542139](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416108.png)



```yaml
apiVersion: v1
kind: Pod
metadata:
  name: busy-box-test
  namespace: default
spec:
  restartPolicy: OnFailure
  containers:
  - name: busy-box-test
    image: busybox
    imagePullPolicy: IfNotPresent
    command: ["sleep", "60000"]
```

![image-20240506152927083](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416003.png)

容器内时间

![image-20240506152958123](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416513.png)

机器时间

![image-20240506153035064](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416943.png)

解决容器时间问题，存在镜像使用的时间与机器时间不同的问题，实现镜像使用机器时间。

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: busy-box-test
  namespace: default
spec:
  restartPolicy: OnFailure
  containers:
  - name: busy-box-test
    image: busybox
    imagePullPolicy: IfNotPresent
    volumeMounts:
    - name: date-config
      mountPath: /etc/localtime
    command: ["sleep", "60000"]
  volumes:
  - name: date-config
    hostPath:
      path: /etc/localtime
```

![image-20240506153344429](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416955.png)

### 使用subPath

有时，在单个 Pod 中共享卷以供多方使用是很有用的。 `volumeMounts.subPath` 属性可用于指定所引用的卷内的子路径，而不是其根路径。

使用subPath挂载是无法自动更新的

# 持久化

![image-20240506154009622](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071416066.png)

- Kubernetes 支持很多类型的卷。 [Pod](https://kubernetes.io/docs/concepts/workloads/pods/pod-overview/) 可以同时使用任意数目的卷类型
- 临时卷类型的生命周期与 Pod 相同，但持久卷可以比 Pod 的存活期长
- 当 Pod 不再存在时，Kubernetes 也会销毁临时卷；
- Kubernetes 不会销毁 持久卷。
- 对于给定 Pod 中**任何类型的卷**，在容器重启期间数据都不会丢失。
- 使用卷时, 在 `.spec.volumes` 字段中设置为 Pod 提供的卷，并在 `.spec.containers[*].volumeMounts` 字段中声明卷在容器中的挂载位置。

## 安装NFS

在 Kubernetes 中使用 NFS（Network File System）作为持久化存储可以帮助您在集群中的多个 Pod 之间共享文件系统。

1.在任意机器安装 NFS 工具包，我的是k8s-master

```shell
yum install -y nfs-utils
```

2.允许 `/nfs/data/` 目录的所有内容被所有主机访问，并具有读写权限，同时禁用了 root 用户的安全性设置。

```shell
echo "/nfs/data/ *(insecure,rw,sync,no_root_squash)" > /etc/exports
```

3.创建`/nfs/data/` 目录

```shell
mkdir -p /nfs/data
```

3.启动 nfs 服务

启用 RPC 绑定服务、启动 NFS 服务器服务、刷新 NFS 导出配置，使更改生效。

```shell
systemctl enable rpcbind
systemctl enable nfs-server
systemctl start rpcbind
systemctl start nfs-server
exportfs -r
```



4.检查配置是否生效

```shell
exportfs
```

![image-20240506155445234](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417976.png)

## 扩展-NFS文件同步

1其他节点安装nfs工具包

```shell
yum install -y nfs-utils
```

2.检查 nfs 服务器端是否有设置共享目录

```shell
showmount -e 192.168.122.140
```

![image-20240506160341845](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417789.png)

3.创建nfsmount目录

```shell
mkdir /nfsmount
```

4.挂载 nfs 服务器上的共享目录到本机路径 /nfsmount

```
mount -t nfs 192.168.122.140:/nfs/data /nfsmount
```

![image-20240506160411345](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417018.png)

5.在nfs服务器上写入一个测试文件

```shell
echo "hello nfs server" > /nfs/data/test.txt
```

![image-20240506160427459](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417297.png)

6验证文件同步成功

```shell
cat /nfsmount/test.txt
```

![image-20240506160451474](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417782.png)

## VOLUME使用nfs进行挂载测试

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: vol-nfs
  namespace: default
spec:
  containers:
  - name: myapp
    image: nginx
    volumeMounts:
    - name: html
      mountPath: /usr/share/nginx/html/
  volumes:
  - name: html
    nfs:
      path: /nfs/data/abc #指定挂载的目录
      server: 192.168.122.140 #nfs服务器地址
```



首先创建abc文件夹

![image-20240506173020510](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417994.png)

![image-20240506170659590](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417035.png)

NFS 服务器上的 `/nfs/data/abc` 目录挂载到 Pod 中的 `/usr/share/nginx/html/` 目录下，所以/usr/share/nginx/html/现在没有数据

![image-20240506173245765](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417054.png)

在nfs服务器/nfs/data/abc中创建index.html

![image-20240506173422966](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417307.png)

pod中同步了数据

![image-20240506173457884](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417047.png)

这样就可以实现修改nfs服务器的数据来操作pod中容器的数据

![image-20240506173538790](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417791.png)

## PV&PVC&StorageClass

### 基础概念

- **存储的管理**是一个与**计算实例的管理**完全不同的问题。
- PersistentVolume 子系统为用户 和管理员提供了一组 API，将存储如何供应的细节从其如何被使用中抽象出来。 
- 为了实现这点，我们引入了两个新的 API 资源：PersistentVolume 和 PersistentVolumeClaim。



**持久卷（PersistentVolume，PV ）：**

- 持久卷（PersistentVolume，PV）是集群中的一块存储，可以由管理员事先供应，或者 使用[存储类（Storage Class）](https://kubernetes.io/zh/docs/concepts/storage/storage-classes/)来动态供应。
- 持久卷是集群资源，就像节点也是集群资源一样。PV 持久卷和普通的 Volume 一样，也是使用 卷插件来实现的，只是它们拥有独立于使用他们的Pod的生命周期。
- 此 API 对象中记述了存储的实现细节，无论其背后是 NFS、iSCSI 还是特定于云平台的存储系统。



**持久卷申请（PersistentVolumeClaim，PVC）：**

- 表达的是用户对存储的请求
- 概念上与 Pod 类似。 Pod 会耗用节点资源，而 PVC 申领会耗用 PV 资源。
- Pod 可以请求特定数量的资源（CPU 和内存）；同样 PVC 申领也可以请求特定的大小和访问模式 （例如，可以要求 PV 卷能够以 ReadWriteOnce、ReadOnlyMany 或 ReadWriteMany 模式之一来挂载，参见[访问模式](https://kubernetes.io/zh/docs/concepts/storage/persistent-volumes/#access-modes)）。



**存储类（Storage Class）**:

- 尽管 PersistentVolumeClaim 允许用户消耗抽象的存储资源，常见的情况是针对不同的 问题用户需要的是具有不同属性（如，性能）的 PersistentVolume 卷。
- 集群管理员需要能够提供不同性质的 PersistentVolume，并且这些 PV 卷之间的差别不 仅限于卷大小和访问模式，同时又不能将卷是如何实现的这些细节暴露给用户。
- 为了满足这类需求，就有了 *存储类（StorageClass）* 资源。

![image-20240506172646793](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417417.png)

![image-20240506172655194](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417475.png)



![image-20240507102511665](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417955.png)

### 实战

https://kubernetes.io/zh/docs/tasks/configure-pod-container/configure-persistent-volume-storage/

1.创建PV

pv-nfs-volume.yaml

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: pv-nfs-volume
  labels:
    type: local
spec:
  storageClassName: pv-nfs
  capacity:
    storage: 1Gi
  accessModes:
    - ReadWriteOnce
  nfs:
    path: /nfs/data/pv #指定挂载的目录
    server: 192.168.122.140 #nfs服务器地址
```

创建pv文件夹

![image-20240507100632545](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417263.png)

创建pv

![image-20240507100738332](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417675.png)

2.创建PVC

pv-claim.yaml

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: task-pv-claim
spec:
  storageClassName: pv-nfs #存储类
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 500m
```

持久卷申请500m，现在有持久卷1G，自动分配最合适的pv，分配持久卷1G

![image-20240507100917934](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417764.png)



3.创建pod，使用 PersistentVolumeClaim 作为存储卷。

pv-nfs.yaml

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pv-nfs
  namespace: default
spec:
  containers:
  - name: myapp
    image: nginx
    volumeMounts:
    - name: html
      mountPath: /usr/share/nginx/html/
  volumes:
  - name: html
    persistentVolumeClaim:
      claimName: task-pv-claim
```

![image-20240507101433173](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417061.png)

验证 Nginx 是否正在从nfs服务器提供 `index.html` 文件

![image-20240507101546407](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417893.png)

4.清理

PV默认会后策略为Retain，意味着当 PersistentVolumeClaim (PVC) 删除时，PV 中的数据不会被自动删除。

删除 Pod 不会影响 PV 或 PVC 的状态。

![image-20240507105527507](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071417501.png)

 删除 PVC 将使 PV 的状态变为 Released。

![image-20240507105627628](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071418453.png)

 删除 PV 将释放 PV 所附加的存储资源，但是，如果 PV 的回收策略设置为 `Retain`，则数据仍然保留在存储中，直到手动清理。

![image-20240507105833005](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071418507.png)

### 访问模式

https://kubernetes.io/zh/docs/concepts/storage/persistent-volumes/#access-modes

访问模式有：

- `ReadWriteOnce`

  卷可以被一个节点以读写方式挂载。 ReadWriteOnce 访问模式仍然可以在同一节点上运行的多个 Pod 访问该卷。 对于单个 Pod 的访问，请参考 ReadWriteOncePod 访问模式。

- `ReadOnlyMany`

  卷可以被多个节点以只读方式挂载。

- `ReadWriteMany`

  卷可以被多个节点以读写方式挂载。

- `ReadWriteOncePod`

  **特性状态：** `Kubernetes v1.29 [stable]`

  卷可以被单个 Pod 以读写方式挂载。 如果你想确保整个集群中只有一个 Pod 可以读取或写入该 PVC， 请使用 ReadWriteOncePod 访问模式。

### 阶段 

https://kubernetes.io/zh/docs/concepts/storage/persistent-volumes/#phase

每个持久卷会处于以下阶段（Phase）之一：

- `Available`

  卷是一个空闲资源，尚未绑定到任何申领

- `Bound`

  该卷已经绑定到某申领

- `Released`

  所绑定的申领已被删除，但是关联存储资源尚未被集群回收

- `Failed`

  卷的自动回收操作失败

### 回收策略

https://kubernetes.io/zh/docs/concepts/storage/persistent-volumes/#reclaim-policy

目前的回收策略有：

- Retain -- 手动回收
- Recycle -- 简单擦除（`rm -rf /thevolume/*`）
- Delete -- 删除存储卷

对于 Kubernetes 1.30 来说，只有 `nfs` 和 `hostPath` 卷类型支持回收（Recycle）。

pv-nfs-volume.yaml：

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: pv-nfs-volume
  labels:
    type: local
spec:
  persistentVolumeReclaimPolicy: Recycle 
  storageClassName: pv-nfs
  capacity:
    storage: 1Gi
  accessModes:
    - ReadWriteOnce
  nfs:
    path: /nfs/data/pv #指定挂载的目录
    server: 192.168.122.140 #nfs服务器地址
```

![image-20240507110153430](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071418251.png)

`Recycle` 回收策略指的是当 PersistentVolumeClaim (PVC) 删除时，与之相关联的 PersistentVolume (PV) 中的数据会被清除。PV 会被重置为初始状态，以便下一个 PVC 可以使用。

 删除 Pod 不会影响 PV 或 PVC 的状态。PV 和 PVC 仍然存在，并且状态保持不变。

![image-20240507110329764](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071418560.png)

删除 PVC ，Recycle 回收策略会清除 PV 中的数据，并将 PV 的状态重置为 Available，以便下一个 PVC 可以使用。

![image-20240507110603279](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071418178.png)

**删除 PV**

![image-20240507110707435](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071418472.png)

## 动态供应

![image-20240507110907340](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071418812.png)

静态供应：

- 集群管理员创建若干 PV 卷。这些卷对象带有真实存储的细节信息，并且对集群 用户可用（可见）。PV 卷对象存在于 Kubernetes API 中，可供用户消费（使用）



动态供应：

- 集群自动根据PVC创建出对应PV进行使用

### 设置nfs动态供应

https://github.com/kubernetes-sigs/nfs-subdir-external-provisioner/tree/master/deploy

按照文档部署，并换成 registry.cn-hangzhou.aliyuncs.com/dongguo/nfs-subdir-external-provisioner:v4.0.2 镜像即可

nfs-provisioner.yaml

```yaml
#class.yaml 创建存储类
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: nfs-client
  # or choose another name, must match deployment's env PROVISIONER_NAME'
provisioner: k8s-sigs.io/nfs-subdir-external-provisioner #指定供应商，必须匹配PROVISIONER_NAME
parameters:
  archiveOnDelete: "false"
---
#deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nfs-client-provisioner
  labels:
    app: nfs-client-provisioner
  # replace with namespace where provisioner is deployed
  namespace: default
spec:
  replicas: 1
  strategy:
    type: Recreate
  selector:
    matchLabels:
      app: nfs-client-provisioner
  template:
    metadata:
      labels:
        app: nfs-client-provisioner
    spec:
      serviceAccountName: nfs-client-provisioner
      containers:
        - name: nfs-client-provisioner
          image: registry.cn-hangzhou.aliyuncs.com/dongguo/nfs-subdir-external-provisioner:v4.0.2
          volumeMounts:
            - name: nfs-client-root
              mountPath: /persistentvolumes
          env:
            - name: PROVISIONER_NAME
              value: k8s-sigs.io/nfs-subdir-external-provisioner
            - name: NFS_SERVER
              value: 192.168.122.140
            - name: NFS_PATH
              value: /nfs/data
      volumes:
        - name: nfs-client-root
          nfs:
            server: 192.168.122.140
            path: /nfs/data
---
# rbac.yaml 权限
apiVersion: v1
kind: ServiceAccount
metadata:
  name: nfs-client-provisioner
  # replace with namespace where provisioner is deployed
  namespace: default
---
kind: ClusterRole
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: nfs-client-provisioner-runner
rules:
  - apiGroups: [""]
    resources: ["nodes"]
    verbs: ["get", "list", "watch"]
  - apiGroups: [""]
    resources: ["persistentvolumes"]
    verbs: ["get", "list", "watch", "create", "delete"]
  - apiGroups: [""]
    resources: ["persistentvolumeclaims"]
    verbs: ["get", "list", "watch", "update"]
  - apiGroups: ["storage.k8s.io"]
    resources: ["storageclasses"]
    verbs: ["get", "list", "watch"]
  - apiGroups: [""]
    resources: ["events"]
    verbs: ["create", "update", "patch"]
---
kind: ClusterRoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: run-nfs-client-provisioner
subjects:
  - kind: ServiceAccount
    name: nfs-client-provisioner
    # replace with namespace where provisioner is deployed
    namespace: default
roleRef:
  kind: ClusterRole
  name: nfs-client-provisioner-runner
  apiGroup: rbac.authorization.k8s.io
---
kind: Role
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: leader-locking-nfs-client-provisioner
  # replace with namespace where provisioner is deployed
  namespace: default
rules:
  - apiGroups: [""]
    resources: ["endpoints"]
    verbs: ["get", "list", "watch", "create", "update", "patch"]
---
kind: RoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: leader-locking-nfs-client-provisioner
  # replace with namespace where provisioner is deployed
  namespace: default
subjects:
  - kind: ServiceAccount
    name: nfs-client-provisioner
    # replace with namespace where provisioner is deployed
    namespace: default
roleRef:
  kind: Role
  name: leader-locking-nfs-client-provisioner
  apiGroup: rbac.authorization.k8s.io
```

![image-20240507133439799](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071418766.png)

SC 是 StorageClass 的缩写. 名为`nfs-client` 的 StorageClass，其由 `k8s-sigs.io/nfs-subdir-external-provisioner` 提供支持。这个 StorageClass 的回收策略为 Delete，意味着当 PersistentVolumeClaim（PVC）被删除时，相关的 PersistentVolume（PV）将被删除。VolumeBindingMode 设置为 Immediate，表示 PVC 与 PV 立即绑定。

![image-20240507133507677](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071418649.png)

PV中storageClassName应设置为nfs-client

### 测试nfs动态供应

pvc-nginx.yaml

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pv-nfs
  namespace: default
spec:
  containers:
  - name: myapp
    image: nginx
    volumeMounts:
    - name: html
      mountPath: /usr/share/nginx/html/
  volumes:
  - name: html
    persistentVolumeClaim:
      claimName: pv-claim
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: pv-claim
spec:
  storageClassName: nfs-client #存储类
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 100m
```

![image-20240507134714119](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071418313.png)

自动创建PV：pvc-f9e4a246-0750-444c-b6a8-48ee4f165339

数据卷自动创建目录default-pv-claim-pvc-f9e4a246-0750-444c-b6a8-48ee4f165339

![image-20240507135125090](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071418327.png)

### 指定一个SC为默认存储类

https://kubernetes.io/zh-cn/docs/tasks/administer-cluster/change-default-storage-class/

默认 StorageClass 以 `(default)` 标记，nfs-client未指定为默认，所以还没有`(default)` 标记

![image-20240507135640237](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071418411.png)

标记一个 StorageClass 为默认的

```shell
kubectl patch storageclass nfs-client -p '{"metadata": {"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'
```

请注意，最多只能有一个 StorageClass 能够被标记为默认。 如果它们中有两个或多个被标记为默认，Kubernetes 将忽略这个注解， 也就是它将表现为没有默认 StorageClass。

验证你选用的 StorageClass 为默认的

![image-20240507135909414](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405071418650.png)

新创建的 PVC 将默认使用 `nfs-client` StorageClass。