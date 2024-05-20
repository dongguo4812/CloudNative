harbor、gitlab等一般公司都是专门部署到一些机器上的，可以不用k8s集群管理

# 入门

## 简介

Harbor 是一个开源的容器镜像仓库，用于存储和分发 Docker 镜像。它由 VMware 开发并捐赠给 CNCF（Cloud Native Computing Foundation）。Harbor 提供了多种企业级特性，增强了 Docker 镜像管理的安全性、效率和可管理性。

### 关键功能

1. **多租户内容签名和安全性**：
   - **内容信任**：通过 Notary 进行镜像签名，确保镜像的内容可信。
   - **漏洞扫描**：集成了 Clair 和 Trivy 等漏洞扫描工具，可以自动扫描镜像的漏洞并提供报告。
   - **角色访问控制**：基于角色的访问控制（RBAC），支持多租户管理，可以为不同的项目配置不同的访问权限。
2. **高可用和可扩展性**：
   - **多实例支持**：可以部署为高可用模式，支持多实例部署以提升可靠性和可扩展性。
   - **镜像复制**：支持跨多个 Harbor 实例和其他容器注册中心的镜像复制，实现灾备和分布式部署。
3. **管理和监控**：
   - **图形化用户界面**：提供友好的 Web UI，可以方便地管理镜像和用户权限。
   - **日志审计**：详细的操作日志记录，便于审计和追踪操作历史。
   - **事件通知**：支持事件通知和 Webhook，可以与 CI/CD 工具集成，自动触发构建和部署。
4. **兼容性和标准支持**：
   - **OCI 兼容**：完全兼容 OCI（Open Container Initiative）镜像规范。
   - **镜像清理**：支持垃圾回收功能，可以清理未使用的镜像，释放存储空间。

## 核心组件

![](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190923914.png)

- Nginx(Proxy)：用于代理Harbor的registry,UI, token等服务
- db：负责储存用户权限、审计日志、Dockerimage分组信息等数据。
- UI：提供图形化界面，帮助用户管理registry上的镜像, 并对用户进行授权
- jobsevice：负责镜像复制工作的，他和registry通信，从一个registry pull镜像然后push到另一个registry，并记录job_log
- Adminserver：是系统的配置管理中心附带检查存储用量，ui和jobserver启动时候回需要加载adminserver的配置。
- Registry：原生的docker镜像仓库，负责存储镜像文件。
- Log：为了帮助监控Harbor运行，负责收集其他组件的log，记录到syslog中

## 安装

### helm下载charts

```shell
helm repo add harbor https://helm.goharbor.io
helm pull harbor/harbor
tar -xvf harbor-1.14.2.tgz
```

![image-20240518091144820](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190923771.png)

### 定制配置

由于 Harbor 组件较多，一般我们会采取新建一个 Namespace 专用于部署 Harbor 相关组件，相关资源统一放在devops命名空间

```shell
kubectl create ns devops
```

1）TLS证书

```shell
kubectl create secret tls harbor.k8s.com --key tls.key --cert tls.crt -n devops
kubectl get secret -n devops
```

![image-20240518135133046](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190923554.png)

现在用的是harbor.k8s.com 域名

==之前已经生成tls.key 、 tls.cert，所以不用执行这个openssl命令了==

```shell
 openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout tls.key -out tls.cert -subj "/CN=*.k8s.com/O=*.k8s.com"
```

2）override.yaml配置

旧版本配置；使用自己的证书。自己的证书要兼容harbor里面的组件很麻烦(不使用)

```yaml
expose:
  type: ingress
  tls:
    certSource: "secret"
    secret:
      secretName: "harbor.k8s.com"
      notarySecretName: "harbor.k8s.com"
  ingress:
    hosts:
      core: harbor.k8s.com
      notary: notary-harbor.k8s.com
externalURL: https://harbor.k8s.com
internalTLS:
  enabled: true
  certSource: "secret"   #
  core:
    secretName: "harbor.k8s.com"
  jobservice:
    secretName: "harbor.k8s.com"
  registry:
    secretName: "harbor.k8s.com"
  portal:
    secretName: "harbor.k8s.com"
  chartmuseum:
    secretName: "harbor.k8s.com"
  trivy:
    secretName: "harbor.k8s.com"
persistence:
  enabled: true
  resourcePolicy: "keep"
  persistentVolumeClaim:
    registry:  # 存镜像的
      storageClass: "rook-ceph-block"
      accessMode: ReadWriteOnce
      size: 5Gi
    chartmuseum: #存helm的chart
      storageClass: "rook-ceph-block"
      accessMode: ReadWriteOnce
      size: 5Gi
    jobservice: #
      storageClass: "rook-ceph-block"
      accessMode: ReadWriteOnce
      size: 1Gi
    database: #数据库  pgsql
      storageClass: "rook-ceph-block"
      accessMode: ReadWriteOnce
      size: 1Gi
    redis: #
      storageClass: "rook-ceph-block"
      accessMode: ReadWriteOnce
      size: 1Gi
    trivy: # 漏洞扫描
      storageClass: "rook-ceph-block"
      accessMode: ReadWriteOnce
      size: 5Gi
metrics:
  enabled: true
```

- 新版本配置，harbor内部组件用默认证书。ingress需要用自己证书(使用这个)
- 自己的证书信息给每个namespace配置同一个

```yaml
expose:  #web浏览器访问用的证书
  type: ingress
  tls:
    certSource: "secret"
    secret:
      secretName: "harbor.k8s.com"
      notarySecretName: "harbor.k8s.com"
  ingress:
    hosts:
      core: harbor.k8s.com
      notary: notary-harbor.k8s.com
externalURL: https://harbor.k8s.com
internalTLS:  #harbor内部组件用的证书
  enabled: true
  certSource: "auto"
persistence:
  enabled: true
  resourcePolicy: "keep"
  persistentVolumeClaim:
    registry:  # 存镜像的
      storageClass: "rook-ceph-block"
      accessMode: ReadWriteOnce
      size: 5Gi
    chartmuseum: #存helm的chart
      storageClass: "rook-ceph-block"
      accessMode: ReadWriteOnce
      size: 5Gi
    jobservice: #
      storageClass: "rook-ceph-block"
      accessMode: ReadWriteOnce
      size: 1Gi
    database: #数据库  pgsql
      storageClass: "rook-ceph-block"
      accessMode: ReadWriteOnce
      size: 1Gi
    redis: #
      storageClass: "rook-ceph-block"
      accessMode: ReadWriteOnce
      size: 1Gi
    trivy: # 漏洞扫描
      storageClass: "rook-ceph-block"
      accessMode: ReadWriteOnce
      size: 5Gi
metrics:
  enabled: true
```



3）安装

```shell
helm install harbor ./ -f values.yaml -f override.yaml  -n devops
```

![image-20240518135211334](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190923316.png)



查看pod harbor-jobservice一直处于Pending

![image-20240518143107122](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190923648.png)

查看原因

![image-20240518143145259](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190923001.png)

定位到pvc的问题，查看pvc

![image-20240518143216993](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190923592.png)

查看原因

![image-20240518143037785](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190923800.png)

原因是pvc harbor-jobservice没有可用的卷或者Storage Class

解决方法：重建pvc

```shell
kubectl delete pvc harbor-jobservice -n devops
vi kubectl-edit.yaml 
kubectl apply -f kubectl-edit.yaml
```

 kubectl-edit.yaml ：

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  annotations:
    helm.sh/resource-policy: keep
    meta.helm.sh/release-name: harbor
    meta.helm.sh/release-namespace: devops
  finalizers:
  - kubernetes.io/pvc-protection
  labels:
    app: harbor
    app.kubernetes.io/managed-by: Helm
    chart: harbor
    component: jobservice
    heritage: Helm
    release: harbor
  name: harbor-jobservice
  namespace: devops
spec:
  accessModes:
  - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi
  volumeMode: Filesystem
  storageClassName: rook-ceph-block
```

最终

![image-20240518143444323](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190923162.png)

## 卸载

```shell
helm uninstall harbor -n devops
```

# harbor使用

https://goharbor.io/docs/2.2.0/working-with-projects/

## Host 配置域名

192.168.122.146 harbor.k8s.com



访问： https://harbor.k8s.com:443/

账号：admin  密码：Harbor12345

![image-20240518144407161](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190923482.png)

创建项目public

![image-20240519083118575](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190924233.png)

成功创建

![image-20240519083133361](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190923513.png)

删除项目

![image-20240519083145670](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190923792.png)

成功删除

![image-20240519083156594](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190924023.png)

## node1推送镜像到harbor仓库

1.给docker机器配置/etc/hosts     

192.168.122.146 harbor.k8s.com



2.登录docker  harbor的账号

```
docker login harbor.k8s.com
```

但是自定义域名docker是不信任自签证书的。

docker中关于证书的配置 https://docs.docker.com/engine/security/certificates/

![image-20240518155019365](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190924217.png)

1）创建目录

```shell
mkdir /etc/docker/certs.d
```

2）创建以域名为名称的文件夹

```shell
cd /etc/docker/certs.d
mkdir harbor.k8s.com
```

![image-20240519083342162](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190924736.png)

3)将证书复制到harbor.k8s.com文件夹内

证书在master1节点上

```
scp tls.crt root@k8s-node1:/etc/docker/certs.d/harbor.k8s.com
```

![image-20240519083357622](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190924632.png)

```shell
cd harbor.k8s.com
```

![image-20240519083441581](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190924992.png)

4）登录harbor仓库

```shell
docker login harbor.k8s.com
```

![image-20240519083541694](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190924537.png)

5）登出

```shell
docker logout harbor.k8s.com
```



### 推送镜像到harbor仓库

1.下载镜像（默认从docker仓库下载）

```shell
docker pull busybox
```

2.推送镜像到harbor仓库的public项目

1）修改tag

```shell
docker tag busybox harbor.k8s.com/public/busybox:v1.0
```

3推送镜像到harbor

```shell
docker push harbor.k8s.com/public/busybox:v1.0
```

![image-20240519084022001](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190924642.png)

## 在node2下载镜像从harbor仓库

### 下载公开项目的镜像不需要登录，但是需要信任证书

所以如何使用harbor最好每个节点都要配置信任证书



1.给docker机器配置/etc/hosts     

192.168.122.147 harbor.k8s.com

2.信任自签证书

1）创建目录

```shell
mkdir /etc/docker/certs.d
```

2）创建以域名为名称的文件夹

```shell
cd /etc/docker/certs.d
mkdir harbor.k8s.com
```

3)将证书复制到harbor.k8s.com文件夹内

master1:

```shell
scp tls.crt root@k8s-node2:/etc/docker/certs.d/harbor.k8s.com
```

node2:

```shell
cd harbor.k8s.com
cp /root/tls.crt ./
```

![image-20240519084320987](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190924767.png)

3下载镜像从harbor仓库

```
docker pull harbor.k8s.com/public/busybox:v1.0
```

![image-20240519084355991](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190924145.png)

### 私有项目的镜像需要登录

1.创建私有项目private

![image-20240519084439413](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190924466.png)

2.node1上传镜像(已经登录)

```
docker tag busybox harbor.k8s.com/private/busybox:v1.0
docker push harbor.k8s.com/private/busybox:v1.0
```

![image-20240519084638147](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190924080.png)

3.node2下载镜像(未登录)

```
docker pull harbor.k8s.com/private/busybox:v1.0
```

![image-20240519084738642](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190924784.png)

1）登陆后再次下载

![image-20240519085008319](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190924944.png)

### 镜像代理

1）创建代理目标

![image-20240519085123763](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190924020.png)

创建成功

![image-20240519085138197](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190924464.png)

2）创建项目proxy，选中代理目标

![image-20240519085207030](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190924377.png)

设置镜像代理如Docker Hub，Harbor 会从 Docker Hub 拉取镜像，并缓存到 harbor项目中，之后的拉取操作将从缓存中获取镜像，提高效率。

代理项目（Proxy Project）是专门用于缓存和加速从远程注册表（如 Docker Hub）拉取镜像的。因此，这类项目的设计通常不允许用户推送镜像到其中，以保持其作为只读缓存的特性。

拉取镜像

```shell
# 拉取docker官方镜像。并缓存起来。harbor.k8s.com/自己的仓库名/ + /library + /镜像名：版本
docker pull harbor.k8s.com/proxy/library/nginx
# 第三方。用第三方全名 harbor.k8s.com/objs + 第三方
docker pull harbor.k8s.com/objs/redislabs/redis
```

![image-20240519085952540](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190925624.png)

proxy项目中就有busybox的镜像了。

![image-20240519090015445](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190924073.png)



## 项目创建机器人账号

![image-20240519090058986](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190924191.png)

添加机器人账号，给定推送、下载权限

1）基本信息

![image-20240519090200158](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190925287.png)

2）权限

![image-20240519090236821](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190925671.png)



密码只显示一次，要妥善保存

![image-20240519090325187](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190925587.png)

用户名：robot$proxy+robot01

![image-20240519090407526](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190925792.png)

密码：

```shell
LCIdDN0hm0e7uZpWWdsQlLmAtPiwIgRF
```

使用机器人账户登录

```
docker login harbor.k8s.com
```

![image-20240519091159636](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190925390.png)

机器人账户只能操作所在项目的镜像。无法操作其他项目

这个机器人账户是在代理项目中创建的，所以无法push镜像，只能拉取镜像

```
docker pull harbor.k8s.com/proxy/library/nginx
```

![image-20240519091822290](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405190925167.png)
