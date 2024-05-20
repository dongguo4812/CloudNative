1. *Elasticsearch**：一个分布式搜索和分析引擎，用于存储、搜索和分析大量数据。它擅长处理结构化和非结构化数据，并提供强大的全文搜索和实时分析能力。
2. **Logstash**：一个数据处理管道工具，用于从各种来源收集、解析、过滤和转换数据，然后将数据发送到目标位置（通常是 Elasticsearch）。它支持多种输入和输出插件，可以处理各种格式的数据。
3. **Kibana**：一个开源的数据可视化和探索工具，用于与 Elasticsearch 配合使用。Kibana 提供了强大的图形界面，可以创建各种图表、仪表板和报告，帮助用户直观地分析和展示数据。

https://www.elastic.co/guide/en/elastic-stack/current/installing-elastic-stack.html#install-order-elastic-stack

![image-20240520173741540](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210722231.png)



使用Es官方Operator方式

# 1、安装operator

https://www.elastic.co/guide/en/cloud-on-k8s/1.6/k8s-quickstart.html

**安装 ECK Operator**： ECK Operator 是一个 Kubernetes Operator，用于管理 Elasticsearch 集群和相关资源。你可以使用 `kubectl` 命令行工具来安装 ECK Operator。

![image-20240520174302273](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723750.png)



![image-20240520174323536](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723468.png)



```shell
kubectl apply -f https://download.elastic.co/downloads/eck/1.6.0/all-in-one.yaml


#查看状态
kubectl -n elastic-system logs -f statefulset.apps/elastic-operator
```

![image-20240520185429215](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723006.png)

[ECK安装的每个组件如何配置](https://www.elastic.co/guide/en/cloud-on-k8s/current/k8s-orchestrating-elastic-stack-applications.html)

# 2、部署ES集群

https://www.elastic.co/guide/en/cloud-on-k8s/1.6/k8s-deploy-elasticsearch.html

elastic.yaml

```yaml
apiVersion: elasticsearch.k8s.elastic.co/v1
kind: Elasticsearch
metadata:
  name: es-cluster
  # 可以指定名称空间
spec:
  version: 7.13.1
  nodeSets:
  - name: masters
    count: 3
    config:
      node.roles: ["master"]
      xpack.ml.enabled: true
    volumeClaimTemplates:
    - metadata:
        name: es-master
      spec:
        accessModes:
        - ReadWriteOnce
        resources:
          requests:
            storage: 5Gi
        storageClassName: "rook-ceph-block"
  - name: data
    count: 4
    config:
      node.roles: ["data", "ingest", "ml", "transform"]
    volumeClaimTemplates:
    - metadata:
        name: es-node
      spec:
        accessModes:
        - ReadWriteOnce
        resources:
          requests:
            storage: 5Gi
        storageClassName: "rook-ceph-block"
```

![image-20240520185633921](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723558.png)



https://www.elastic.co/guide/en/cloud-on-k8s/current/k8s-deploy-elasticsearch.html#k8s_request_elasticsearch_access

## 1、本地访问密码测试

获取密码

```shell
## elastic的访问
kubectl get secret es-cluster-es-elastic-user -o=jsonpath='{.data.elastic}' | base64 --decode; echo
```

![image-20240520191501573](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723398.png)

1）集群内组件访问

```shell
###账号 elastic
###密码 t5upEg4l5J376298kOIPy8Ww
curl -u "elastic:2WC5On8Xio6EK4x4ph1T7Q54" -k "https://es-cluster-es-http:9200"
curl -u "elastic:2WC5On8Xio6EK4x4ph1T7Q54" -k "https://10.96.9.9:9200"
```

2）集群本地访问

```shell
kubectl port-forward service/es-cluster-es-http 9200
curl -u "elastic:$PASSWORD" -k "https://localhost:9200"
```

3）做成下面的Ingress访问

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: elastic-ingress
  annotations:
    kubernetes.io/ingress.class: "nginx"
    nginx.ingress.kubernetes.io/backend-protocol: "HTTPS"
    nginx.ingress.kubernetes.io/server-snippet: |
      proxy_ssl_verify off;
spec:
  tls:
  - hosts:
      - elastic.k8s.com
    secretName: k8s.com
  rules:
  - host: elastic.k8s.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: es-cluster-es-http
            port:
              number: 9200
```

![image-20240520191750748](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723030.png)

windows配置192.168.122.146  elastic.k8s.com

访问 http://elastic.k8s.com

账号 elastic
密码 t5upEg4l5J376298kOIPy8Ww

![image-20240520192001338](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723644.png)

# 3、部署kibana

```yaml
apiVersion: kibana.k8s.elastic.co/v1
kind: Kibana
metadata:
  name: kibana
spec:
  version: 7.13.1
  count: 1
  elasticsearchRef:
    name: es-cluster
```

![image-20240520192113657](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723013.png)

## 1、访问密码

默认使用elastic的账户密码

```shell
kubectl get secret es-cluster-es-elastic-user -o=jsonpath='{.data.elastic}' | base64 --decode; echo
### 账号  elastic 
### 密码  618FAZBH5a269Wqb0Hpan19Y
```

## 2、配置Ingress

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: kibana-ingress
  annotations:
    kubernetes.io/ingress.class: "nginx"
    nginx.ingress.kubernetes.io/backend-protocol: "HTTPS"
    nginx.ingress.kubernetes.io/server-snippet: |
      proxy_ssl_verify off;
spec:
  tls:
  - hosts:
      - kibana.k8s.com
    secretName: k8s.com
  rules:
  - host: kibana.k8s.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: kibana-kb-http
            port:
              number: 5601
```

![image-20240520192226225](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723220.png)

windows配置192.168.122.146  kibana.k8s.com

访问 http://kibana.k8s.com

![image-20240520192444339](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723742.png)

# 4、部署FileBeats

```yaml
apiVersion: beat.k8s.elastic.co/v1beta1
kind: Beat
metadata:
  name: beats
spec:
  type: filebeat
  version: 7.13.1
  elasticsearchRef:
    name: es-cluster
  config:
    filebeat.inputs:
    - type: container
      paths:
      - /var/log/containers/*.log
  daemonSet:
    podTemplate:
      spec:
        dnsPolicy: ClusterFirstWithHostNet
        hostNetwork: true
        securityContext:
          runAsUser: 0
        containers:
        - name: filebeat
          volumeMounts:
          - name: varlogcontainers
            mountPath: /var/log/containers
          - name: varlogpods
            mountPath: /var/log/pods
          - name: varlibdockercontainers
            mountPath: /var/lib/docker/containers
        volumes:
        - name: varlogcontainers
          hostPath:
            path: /var/log/containers
        - name: varlogpods
          hostPath:
            path: /var/log/pods
        - name: varlibdockercontainers
          hostPath:
            path: /var/lib/docker/containers
```

![image-20240520192628906](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210724078.png)

回到kibana查看收集的日志

![image-20240520193102824](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405210723302.png)