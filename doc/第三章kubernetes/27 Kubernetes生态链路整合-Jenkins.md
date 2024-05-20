手动安装Jenkins

## 编写Jenkins配置文件

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: jenkins
  namespace: devops
spec:
  selector:
    matchLabels:
      app: jenkins # has to match .spec.template.metadata.labels
  serviceName: "jenkins"
  replicas: 1
  template:
    metadata:
      labels:
        app: jenkins # has to match .spec.selector.matchLabels
    spec:
      serviceAccountName: "jenkins"
      terminationGracePeriodSeconds: 10
      containers:
      - name: jenkins
        image: jenkins/jenkins:2.454
        securityContext:                     
          runAsUser: 0                      #设置以ROOT用户运行容器
          privileged: true                  #拥有特权
        ports:
        - containerPort: 8080
          name: web
        - name: jnlp                        #jenkins slave与集群的通信口
          containerPort: 50000
        resources:
          limits:
            memory: 2Gi
            cpu: "2000m"
          requests:
            memory: 700Mi
            cpu: "500m"
        env:
        - name: LIMITS_MEMORY
          valueFrom:
            resourceFieldRef:
              resource: limits.memory
              divisor: 1Mi
        - name: "JAVA_OPTS" #设置变量，指定时区和 jenkins slave 执行者设置
          value: " 
                   -Xmx$(LIMITS_MEMORY)m 
                   -XshowSettings:vm 
                   -Dhudson.slaves.NodeProvisioner.initialDelay=0
                   -Dhudson.slaves.NodeProvisioner.MARGIN=50
                   -Dhudson.slaves.NodeProvisioner.MARGIN0=0.75
                   -Duser.timezone=Asia/Shanghai
                 "  
        volumeMounts:
        - name: home
          mountPath: /var/jenkins_home
  volumeClaimTemplates:
  - metadata:
      name: home
    spec:
      accessModes: [ "ReadWriteOnce" ]
      storageClassName: "rook-ceph-block"
      resources:
        requests:
          storage: 5Gi

---
apiVersion: v1
kind: Service
metadata:
  name: jenkins
  namespace: devops
spec:
  selector:
    app: jenkins
  type: ClusterIP
  ports:
  - name: web
    port: 8080
    targetPort: 8080
    protocol: TCP
  - name: jnlp
    port: 50000
    targetPort: 50000
    protocol: TCP
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: jenkins
  namespace: devops
spec:
  tls:
  - hosts:
      - jenkins.k8s.com
    secretName: k8s.com
  rules:
  - host: jenkins.k8s.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: jenkins
            port:
              number: 8080
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: jenkins
  namespace: devops

---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: jenkins
rules:
  - apiGroups: ["extensions", "apps"]
    resources: ["deployments"]
    verbs: ["create", "delete", "get", "list", "watch", "patch", "update"]
  - apiGroups: [""]
    resources: ["services"]
    verbs: ["create", "delete", "get", "list", "watch", "patch", "update"]
  - apiGroups: [""]
    resources: ["pods"]
    verbs: ["create","delete","get","list","patch","update","watch"]
  - apiGroups: [""]
    resources: ["pods/exec"]
    verbs: ["create","delete","get","list","patch","update","watch"]
  - apiGroups: [""]
    resources: ["pods/log"]
    verbs: ["get","list","watch"]
  - apiGroups: [""]
    resources: ["secrets"]
    verbs: ["get"]

---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: jenkins
roleRef:
  kind: ClusterRole
  name: jenkins
  apiGroup: rbac.authorization.k8s.io
subjects:
- kind: ServiceAccount
  name: jenkins
  namespace: devops
```

参考文章： https://www.cnblogs.com/guguli/p/7827435.html

![image-20240519130706724](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210721460.png)

查看jenkins初始化的密码

```shell
kubectl logs jenkins-0 -n devops

6362d3a398f64eff8279c7606b0bf08a
```

![image-20240519113834185](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210721942.png)

windows  C:\Windows\system32\drivers\etc\hosts添加192.168.122.146 jenkins.k8s.com

访问 https://jenkins.k8s.com:443

![image-20240519120059652](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210721211.png)



输入密码

![image-20240519123830579](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210721055.png)

安装推荐的插件

![image-20240519123955809](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210722691.png)

如果插件下载失败，重试或者

https://plugins.jenkins.io/ 手动下载上传到/var/jenkins_home/plugins/

```shell
#以上插件可能无法下载，可以手动去jenkins-plugins下载并上传.
kubectl cp /root/other/kubernetes-client-api.hpi devops/jenkins-0:/var/jenkins_home/plugins/
```

也可以直接选择继续，登陆后在插件管理中下载对应的插件

创建管理员用户

![image-20240519124555321](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210722421.png)

重启

![image-20240519124632001](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210722934.png)

登录

![image-20240519124954800](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210722670.png)

首页

![image-20240519125041909](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210722486.png)

## 配置镜像源

系统管理->插件管理->高级->升级站点

```shell
#配置镜像源
https://updates.jenkins.io/update-center.json   默认的
http://updates.jenkins-ci.org/update-center.json
#或者国内源
https://mirrors.tuna.tsinghua.edu.cn/jenkins/updates/update-center.json
```

提交后点击立即获取更新信息

![image-20240519125218377](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210722459.png)

## 下载核心插件

下载docker、Kubernetes插件

![image-20240519125554005](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210722127.png)

# 配置集群整合

## 动态slave架构

![image-20240519111053212](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210722942.png)

我们安装的jenkins作为master，然后配置打包机，构建任务执行时会创建slave执行构建，构建完成后slave就不存在了。

## 配置整合

点击《系统管理》—>《Configure System系统配置》—>《配置一个云》

![image-20240520110744154](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210722667.png)

选择Kubernetes

![image-20240519111110752](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210722414.png)

## 配置kubernetes集群信息

![image-20240519111123875](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210722878.png)



Kubernetes地址是固定的写法，Kubernetes暴露端口443，所以地址是https开头。htts://Kubernetes.default.svc.cluster.local

![image-20240520110934004](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210722614.png)

## 配置slave的pod模板

slave-pod(打包机)，流水线的某些任务可以在打包机中运行，不同的打包机运行不同的label的pod任务

![image-20240519111134879](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210722101.png)

Slave就是动态运行起来的容器环境.

jenkins的所有构建命令会在这个容器里面运行

- 注意配置以下内容
  - 名称: `自定义`
  - 命名空间 :  `devops`
  - 标签列表:  `自定义`
  - 容器名称、镜像： `jenkins/inbound-agent:4.7-1-alpine`
  - serviceAccount挂载项: `jenkins`
  - `运行命令`: 改为 `jenkins-slave`

注意：

- jenkins-url如果是一个域名，测试环境下可能不能访问，此时需要给各个主机配置域名转发到vpc网络的ip
- 修改各个主机的 /etc/hosts文件即可
- 也可以直接设置jenkins-url为公网ip地址

添加pod模版，并填写模板细节

![image-20240519111148238](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210722326.png)

更详细配置查看slave参考配置

## 测试动态slave

### 1、自由风格

![image-20240520141220644](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210722798.png)



![image-20240520141234266](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210722701.png)

### 2、流水线写法

```shell
pipeline {
    agent {
      label 'maven-jnlp-slave'
    }

    stages {
        stage('Hello') {
            steps {
                echo 'Hello World'
            }
        }
    }
}
```



## 配置更多的slave

| slave-label | 镜像                                                         | 集成工具                                                |
| ----------- | ------------------------------------------------------------ | ------------------------------------------------------- |
| maven       | registry.cn-hangzhou.aliyuncs.com/lfy_k8s_images/jnlp-maven:3.6.3 | jq、curl、maven                                         |
| nodejs      | registry.cn-hangzhou.aliyuncs.com/lfy_k8s_images/jnlp-nodejs:14.16.1 | jq、curl、nodejs、npm（已经设置全局目录在 /root/npm下） |
| kubectl     | registry.cn-hangzhou.aliyuncs.com/lfy_k8s_images/jnlp-kubectl:1.21.1 | kubectl、helm、helm-push、jq、curl、                    |
| allin       | registry.cn-hangzhou.aliyuncs.com/lfy_k8s_images/jnlp-all:v1.0 | kubectl、helm、maven、nodejs、jq、curl                  |
| docker      | registry.cn-hangzhou.aliyuncs.com/lfy_k8s_images/jnlp-docker:20.10.2 | jq、curl、docker                                        |

> 根据自己环境可以有很多定制的slave

# slave参考配置

下面的截图忘了选中 user,group 都应该为 0,0  也就是root用户

每个打包机都应该hostPath模式挂载/etc/hosts文件。方便统一域名管理。或者全系统内部都不用域名，都使用ip进行交互也可以【但是推荐域名，域名可以统一修改，ip变化所有引用的地方都来修改很麻烦】

## 1、maven配置

![image-20240520133327189](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210724238.png)





为woker节点打上标签

```shell
kubectl label node k8s-master2 jnlp-node=true
kubectl label node k8s-master3 jnlp-node=true
kubectl label node k8s-node1 jnlp-node=true
kubectl label node k8-node2 jnlp-node=true
kubectl label node k8-node3 jnlp-node=true
```

使用要求

- 1、提前创建好maven的settings.xml，并且以configMap的形式保存到k8s集群的devops名称空间。configmap名叫`maven-conf`，里面有一个键名`settings.xml`，值为 `maven配置文件的值`

- 2、准备名为`maven-jar-pvc` 的pvc 在 devops名称空间下。为RWX模式

- 例如：

```shell
kubectl create configmap maven-conf --from-file=settings.xml=/root/settings.xml -n devops`
```

maven配置文件   /root/settings.xml:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <!-- 这个目录是被maven打包机使用pvc挂载出去的
  -->
  <localRepository>/root/maven/.m2</localRepository>

  <pluginGroups>

  </pluginGroups>

  <proxies>

  </proxies>


  <servers>

  </servers>

  <mirrors>
	 <mirror>
        <id>nexus-aliyun</id>
        <mirrorOf>central</mirrorOf>
        <name>Nexus aliyun</name>
        <url>http://maven.aliyun.com/nexus/content/groups/public</url>
	 </mirror>
  </mirrors>
  <profiles>
		<profile>
			 <id>jdk-1.8</id>
			 <activation>
			   <activeByDefault>true</activeByDefault>
			   <jdk>1.8</jdk>
			 </activation>
			 <properties>
			   <maven.compiler.source>1.8</maven.compiler.source>
			   <maven.compiler.target>1.8</maven.compiler.target>
			   <maven.compiler.compilerVersion>1.8</maven.compiler.compilerVersion>
			 </properties>
		</profile>
  </profiles>
</settings>
```

![image-20240520161941550](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210724573.png)

maven-jar-pvc.yaml

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: maven-jar-pvc
  namespace: devops
  labels:
    app: maven-jar-pvc
spec:
  storageClassName: rook-cephfs
  accessModes:
  - ReadWriteMany
  resources:
    requests:
      storage: 5Gi
```

![image-20240520162111140](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210724758.png)

## 2、kubectl配置

在容器内 /root/.kube/config，config文件的内容是我们集群之前的admin.conf的内容

![image-20240520140737963](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210724854.png)

使用要求

- 必须提前给集群创建一个ConfigMap，名叫 `kubectl-admin.conf`，里面有一个键名叫`config`，键值可以是master节点 /root/.kube/config的内容

- 例如

  ```shell
  kubectl create configmap kubectl-admin.conf --from-file=config=/root/.kube/config -n devops
  ```

  

  ![image-20240520162638101](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210724107.png)

## 3、nodejs配置

![image-20240520140904438](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210724590.png)

使用说明

- 准备名为`npm-modules-pvc` 的pvc 在 devops名称空间下。为RWX模式

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
    name: npm-modules-pvc
    namespace: devops
    labels:
       app: npm-modules-pvc
spec:
    storageClassName: rook-cephfs
    accessModes:
    - ReadWriteMany
    resources:
       requests:
         storage: 5Gi
```

![image-20240520162323785](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210724539.png)

## 4、docker配置

使用注意：

- docker访问harbor之类的私有仓库且是https，要注意配置证书受信任。提前各个机器配置好

![image-20240520140958133](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210724053.png)



# 示例流水线

## 1、打包机检查

```shell
pipeline {
    //无代理，各阶段声明自己的代理
    agent none
    stages {
        stage('检查nodejs打包机') {
            //使用nodejs代理
            agent {
                label 'nodejs'
            }
            steps {
                echo "nodejs版本："
                sh 'node -v'
                echo "npm modules目录位置"
                sh 'npm config ls -l | grep prefix'
                echo "检查完成..."
            }
        }

        stage('检查maven打包机') {
            //使用nodejs代理
            agent {
                label 'maven'
            }
            steps {
                echo "maven版本："
                sh 'mvn -v'
                echo "maven配置文件"
                sh 'cat /app/maven/settings.xml'

                echo "maven目录位置信息"
                sh 'ls -al /app/maven/'
            }
        }
        stage('检查docker打包机') {
            //使用nodejs代理
            agent {
                label 'docker'
            }
            steps {
                echo "docker版本："
                sh 'docker version'
                sh 'docker images'
            }
        }

        stage('检查kubectl打包机') {
            //使用nodejs代理
            agent {
                label 'kubectl'
            }
            steps {
                echo "kubectl版本："
                sh ' kubectl version'
                echo "kubectl操作集群: 所有Pod"
                sh 'kubectl get pods'

                echo "kubectl操作集群: 所有nodes"
                sh 'kubectl get nodes'
            }
        }
    }
}
```

新建任务->构建流水线任务

![image-20240520163143003](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210724066.png)

开始构建，构建完成

![image-20240520163518174](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210724125.png)



![image-20240520163558159](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210724734.png)

## 2、复杂流水线

### 1、java-Dockerfile模板

```dockerfile
#这个也得有
FROM openjdk:8-jre-alpine
LABEL maintainer="534096094@qq.com"
#复制打好的jar包
COPY target/*.jar /app.jar
RUN  apk add -U tzdata; \
ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime; \
echo 'Asia/Shanghai' >/etc/timezone; \
touch /app.jar;

ENV JAVA_OPTS=""
ENV PARAMS=""

EXPOSE 8080

ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom $JAVA_OPTS -jar /app.jar $PARAMS" ]
```

### 2、准备示例项目

https://gitee.com/macrozheng/mall

1、准备数据库镜像

```dockerfile
FROM mysql:5.7

# 所有在 /docker-entrypoint-initdb.d 下的sql，数据库会自己初始化
COPY mall.sql /docker-entrypoint-initdb.d
```

