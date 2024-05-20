# 1、是什么

https://kustomize.io/  Kubernetes本地的配置管理工具。轻量版的helm；

> 以后我们公司自己部署的一些中间件等，可以封装为 kustomize 管理的文件结构。（可以理解为docker-compose）
>
> 只需要`kubectl apply -k` 即可快速部署不同环境应用

# 2、用法

## 1、文件结构

![image-20240520163820612](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723267.png)

## 2、文件内容

`kustomization.yaml` 用于定义哪些资源需要包含在你的定制化中

```yaml
#kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
metadata:
  name: arbitrary
# Example configuration for the webserver
# at https://github.com/monopole/hello
commonLabels:
  app: hello  # 构建出来的每个资源上都有app=hello标签
resources:
- deployment.yaml
- service.yaml
- configMap.yaml
```

service.yaml：

```yaml
#service.yaml
kind: Service
apiVersion: v1
metadata:
  name: the-service
spec:
  selector:
    deployment: hello
  type: ClusterIP
  ports:
  - protocol: TCP
    port: 8666
    targetPort: 8080
```

configMap.yaml：

```yaml
#configMap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: the-map
data:
  altGreeting: "Good Morning!"
  enableRisky: "false"
```

deployment.yaml：

```yaml
#deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: the-deployment
spec:
  replicas: 3
  selector:
    matchLabels:
      deployment: hello
  template:
    metadata:
      labels:
        deployment: hello
    spec:
      containers:
      - name: the-container
        image: monopole/hello:1
        command: ["/hello",
                  "--port=8080",
                  "--enableRiskyFeature=$(ENABLE_RISKY)"]
        ports:
        - containerPort: 8080
        env:
        - name: ALT_GREETING
          valueFrom:
            configMapKeyRef:
              name: the-map
              key: altGreeting
        - name: ENABLE_RISKY
          valueFrom:
            configMapKeyRef:
              name: the-map
              key: enableRisky
```

## 3、使用

```shell
kubectl apply -k demo/
```

![image-20240520164947620](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723653.png)

## 4、注意事项

- kustomization.yaml 文件名是固定的；
- kubectl apply -k  path 会自动找path下的kustomization.yaml 

## 5、高级-环境分离

- 创建  [overlay](https://kubectl.docs.kubernetes.io/references/kustomize/glossary/#overlay),分离各个环境。原来的可以抽取为`base`环境。其他环境层可只定义变量覆盖
- 每个环境层定义自己的 kustomization.yaml

- 新的层级结构

![image-20240520165014206](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723878.png)

```yaml
#production/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
namePrefix: production-
commonLabels:
  variant: production
  org: acmeCorporation
commonAnnotations:
  note: Hello, I am production!
bases:
- ../../base
patchesStrategicMerge:
- deployment.yaml
```



```yaml
#production/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: the-deployment
spec:
  replicas: 10
## 只需要定义可变部分  
```



```yaml
#staging/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
namePrefix: staging-   #所有资源的前缀
commonLabels:   #所有资源的标签
  variant: staging   
  org: acmeCorporation
commonAnnotations:  #所有资源的注解
  note: Hello, I am staging!
bases:
- ../../base  #基础配置的位置
patchesStrategicMerge:
- map.yaml  #需要额外引入部署的内容，如果引入的内容基础内容有配置，则使用这个最新的
```



```yaml
#staging/map.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: the-map
data:
  altGreeting: "Have a pineapple!"
  enableRisky: "true"  
```

- [kustomzition文件能写的内容](https://kubectl.docs.kubernetes.io/guides/config_management/)

- 执行命令

- ```sh
  kubectl apply -k overlays/staging -n hello   #可以在部署的时候统一制定名称空间
  ```

基础环境

![image-20240520165913112](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723614.png)

生产环境

![image-20240520170115445](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723981.png)

演示环境

![image-20240520170138849](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723253.png)

## 2、部署mysql

![image-20240520171415878](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723110.png)

mysql-k\base\cm.yaml

```yaml
apiVersion: v1
data:
  my.cnf: |
    # Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
    #
    # The MySQL  Server configuration file.
    #
    # For explanations see
    # http://dev.mysql.com/doc/mysql/en/server-system-variables.html

    [mysqld]
    pid-file        = /var/run/mysqld/mysqld.pid
    socket          = /var/run/mysqld/mysqld.sock
    datadir         = /var/lib/mysql
    secure-file-priv= NULL
    skip-host-cache
    skip-name-resolve

    # Custom config should go here
    !includedir /etc/mysql/conf.d/
kind: ConfigMap
metadata:
  name: myconf
```

mysql-k\base\kustomization.yaml

```yaml
# apiVersion: kustomize.config.k8s.io/v1beta1
# kind: Kustomization
commonLabels:
  app: mysql 
resources:
- cm.yaml
- secret.yaml
- sts.yaml
- service.yaml
```

mysql-k\base\secret.yaml

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: mysql-pass
type: Opaque
data:
  # Default password is "admin".
  password: YWRtaW4=
```

mysql-k\base\service.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: mysql
  labels:
    app: mysql
spec:
  ports:
    - port: 3306
  selector:
    app: mysql
  type: ClusterIP
```

mysql-k\base\sts.yaml

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql
spec:
  selector:
    matchLabels:
      app: mysql 
  serviceName: "mysql"
  replicas: 1
  template:
    metadata:
      labels:
        app: mysql # has to match .spec.selector.matchLabels
    spec:
      terminationGracePeriodSeconds: 10
      containers:
      - name: mysql
        image: mysql:8.0.25
        # args: #mysql5.x版本需要以下设置项，否则不兼容ceph块存储
        # - '--ignore-db-dir=lost+found'
        ports:
        - containerPort: 3306
        env:
        - name: MYSQL_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-pass
              key: password
        volumeMounts:
        - name: localtime
          mountPath: /etc/localtime
        - name: mysql-persistent-storage
          mountPath: /var/lib/mysql
        - name: mycnf
          mountPath: /etc/mysql/my.cnf
          subPath: my.cnf
      volumes:
        - name: localtime
          hostPath:
            path: /usr/share/zoneinfo/Asia/Shanghai
        - name: mycnf
          configMap: 
            name: myconf
            items: 
            - key: my.cnf
              path: my.cnf
  volumeClaimTemplates:
  - metadata:
      name: mysql-persistent-storage
    spec:
      accessModes: [ "ReadWriteOnce" ]
      storageClassName: "rook-ceph-block"
      resources:
        requests:
          storage: 1Gi
```

mysql-k\overlays\dev\kustomization.yaml

```yaml
# apiVersion: kustomize.config.k8s.io/v1beta1
# kind: Kustomization
# namePrefix: dev- 
commonLabels:
  app: mysql
bases:
- ../../base
patchesStrategicMerge:
- service.yaml
```

mysql-k\overlays\dev\service.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: mysql
spec:
  clusterIP: None
```

mysql-k\overlays\prod\kustomization.yaml

```yaml
# apiVersion: kustomize.config.k8s.io/v1beta1
# kind: Kustomization
# namePrefix: prod-
commonLabels:
  app: mysql
bases:
- ../../base
patchesStrategicMerge:
- service.yaml
- sts.yaml
# configMapGenerator:
#   - 
```

mysql-k\overlays\prod\service.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: mysql
spec:
  clusterIP: None
```

mysql-k\overlays\prod\sts.yaml

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql
spec:
  replicas: 3 
  volumeClaimTemplates:  ##vct的内容必须全量重写
  - metadata:
      name: mysql-persistent-storage
    spec:
      accessModes: [ "ReadWriteOnce" ]
      storageClassName: "rook-ceph-block"
      resources:
        requests:
          storage: 5Gi
```



生产环境

![image-20240520172316111](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723037.png)

等待pod

![image-20240520172349005](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723613.png)