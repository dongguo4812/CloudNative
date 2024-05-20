# 块存储(RBD)

RBD： RADOS Block Devices    

RADOS： Reliable, Autonomic Distributed Object Store



## 配置

块存储一般用于一个pod挂在一块存储使用，相当于一个服务器新挂了一个盘，只给一个应用使用。



RBD一般使用RWO模式:（ReadWriteOnce）

https://www.rook.io/docs/rook/v1.6/ceph-block.html

RWO模式；STS删除，pvc不会删除，需要自己手动维护



使用Rook支持的持久卷在Kubernetes上创建一个简单的多层web应用程序。

在Rook可以调配存储之前，需要创建StorageClass和CephBlockPool。这将允许Kubernetes在配置持久卷时与Rook进行互操作。

1. 创建存储池

![image-20240516091732348](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172247755.png)

2.创建存储驱动

![image-20240516085757883](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172247426.png)



block-ceph.yaml

```yaml
apiVersion: ceph.rook.io/v1
kind: CephBlockPool
metadata:
  name: replicapool
  namespace: rook-ceph
spec:
  failureDomain: host  #容灾模式，host或者osd
  replicated:
    size: 2  #数据副本数量
---
apiVersion: storage.k8s.io/v1
kind: StorageClass  #存储驱动
metadata:
   name: rook-ceph-block
# Change "rook-ceph" provisioner prefix to match the operator namespace if needed
provisioner: rook-ceph.rbd.csi.ceph.com
parameters:
    # clusterID is the namespace where the rook cluster is running
    clusterID: rook-ceph
    # Ceph pool into which the RBD image shall be created
    pool: replicapool

    # (optional) mapOptions is a comma-separated list of map options.
    # For krbd options refer
    # https://docs.ceph.com/docs/master/man/8/rbd/#kernel-rbd-krbd-options
    # For nbd options refer
    # https://docs.ceph.com/docs/master/man/8/rbd-nbd/#options
    # mapOptions: lock_on_read,queue_depth=1024

    # (optional) unmapOptions is a comma-separated list of unmap options.
    # For krbd options refer
    # https://docs.ceph.com/docs/master/man/8/rbd/#kernel-rbd-krbd-options
    # For nbd options refer
    # https://docs.ceph.com/docs/master/man/8/rbd-nbd/#options
    # unmapOptions: force

    # RBD image format. Defaults to "2".
    imageFormat: "2"

    # RBD image features. Available for imageFormat: "2". CSI RBD currently supports only `layering` feature.
    imageFeatures: layering

    # The secrets contain Ceph admin credentials.
    csi.storage.k8s.io/provisioner-secret-name: rook-csi-rbd-provisioner
    csi.storage.k8s.io/provisioner-secret-namespace: rook-ceph
    csi.storage.k8s.io/controller-expand-secret-name: rook-csi-rbd-provisioner
    csi.storage.k8s.io/controller-expand-secret-namespace: rook-ceph
    csi.storage.k8s.io/node-stage-secret-name: rook-csi-rbd-node
    csi.storage.k8s.io/node-stage-secret-namespace: rook-ceph

    # Specify the filesystem type of the volume. If not specified, csi-provisioner
    # will set default as `ext4`. Note that `xfs` is not recommended due to potential deadlock
    # in hyperconverged settings where the volume is mounted on the same node as the osds.
    csi.storage.k8s.io/fstype: ext4

# Delete the rbd volume when a PVC is deleted
reclaimPolicy: Delete
allowVolumeExpansion: true
```



![image-20240516100355373](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172247641.png)

## 测试

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name:  nginx-deploy
  namespace: default
  labels:
    app:  nginx-deploy
spec:
  selector:
    matchLabels:
      app: nginx-deploy
  replicas: 1
  strategy:
    rollingUpdate:
      maxSurge: 25%
      maxUnavailable: 25%
    type: RollingUpdate
  template:
    metadata:
      labels:
        app:  nginx-deploy
    spec:
      containers:
      - name:  nginx-deploy
        image:  nginx
        volumeMounts:
        - name: localtime
          mountPath: /etc/localtime
        - name: nginx-html-storage
          mountPath: /usr/share/nginx/html
      volumes:
        - name: localtime
          hostPath:
            path: /usr/share/zoneinfo/Asia/Shanghai
        - name: nginx-html-storage
          persistentVolumeClaim:
            claimName: nginx-pv-claim
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: nginx-pv-claim
  labels:
    app:  nginx-deploy
spec:
  storageClassName: rook-ceph-block
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 10Mi
```

![image-20240516100623445](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172247754.png)

RBD块存储不允许多节点，即不能是ReadWriteOnce模式

- `ReadWriteOnce`

  卷可以被一个节点以读写方式挂载。 ReadWriteOnce 访问模式仍然可以在同一节点上运行的多个 Pod 访问该卷。 对于单个 Pod 的访问，请参考 ReadWriteOncePod 访问模式。

- `ReadWriteMany`

  卷可以被多个节点以读写方式挂载。



修改为ReadWriteOnce模式

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name:  nginx-deploy2
  namespace: default
  labels:
    app:  nginx-deploy2
spec:
  selector:
    matchLabels:
      app: nginx-deploy2
  replicas: 1
  strategy:
    rollingUpdate:
      maxSurge: 25%
      maxUnavailable: 25%
    type: RollingUpdate
  template:
    metadata:
      labels:
        app:  nginx-deploy2
    spec:
      containers:
      - name:  nginx-deploy2
        image:  nginx
        volumeMounts:
        - name: localtime
          mountPath: /etc/localtime
        - name: nginx-html-storage
          mountPath: /usr/share/nginx/html
      volumes:
        - name: localtime
          hostPath:
            path: /usr/share/zoneinfo/Asia/Shanghai
        - name: nginx-html-storage
          persistentVolumeClaim:
            claimName: nginx-pv-claim
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: nginx-pv-claim2
  labels:
    app:  nginx-deploy2
spec:
  storageClassName: rook-ceph-block
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Mi
```

等待pvc创建完毕

![image-20240516121505274](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172247857.png)



![image-20240516121608942](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172247474.png)

## STS案例实战

块存储一般是做有状态服务的

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: sts-nginx
  namespace: default
spec:
  selector:
    matchLabels:
      app: sts-nginx # has to match .spec.template.metadata.labels
  serviceName: "sts-nginx"
  replicas: 3 # by default is 1
  template:
    metadata:
      labels:
        app: sts-nginx # has to match .spec.selector.matchLabels
    spec:
      terminationGracePeriodSeconds: 10
      containers:
      - name: sts-nginx
        image: nginx
        ports:
        - containerPort: 80
          name: web
        volumeMounts:
        - name: www
          mountPath: /usr/share/nginx/html
  volumeClaimTemplates:
  - metadata:
      name: www
    spec:
      accessModes: [ "ReadWriteOnce" ]
      storageClassName: "rook-ceph-block"
      resources:
        requests:
          storage: 20Mi
---
apiVersion: v1
kind: Service
metadata:
  name: sts-nginx
  namespace: default
spec:
  selector:
    app: sts-nginx
  type: ClusterIP
  ports:
  - name: sts-nginx
    port: 80
    targetPort: 80
    protocol: TCP
```

测试： 创建sts、修改nginx数据、删除sts、重新创建sts。他们的数据丢不丢，共享不共享

1.创建sts

![image-20240516150710271](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172248690.png)

等待pod创建

![image-20240516150932601](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172247290.png)

2任选一个pod修改nginx数据

![image-20240516151223403](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172247058.png)

3.删除sts

有状态应用不会删除pvc

![image-20240516151400026](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172247329.png)

4重建sts

![image-20240516151450065](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172247447.png)

等待pod创建

![image-20240516151744527](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172247449.png)

5查看数据是否丢失

有状态应用（3个副本）使用块存储。自己操作自己的pvc挂载的pv；也不丢失

# 文件存储(CephFS)

## 配置

常用 文件存储。 RWX模式；如：10个Pod共同操作一个地方

https://rook.io/docs/rook/v1.6/ceph-filesystem.html

```yaml
apiVersion: ceph.rook.io/v1
kind: CephFilesystem
metadata:
  name: myfs
  namespace: rook-ceph # namespace:cluster
spec:
  # The metadata pool spec. Must use replication.
  metadataPool:
    replicated:
      size: 3
      requireSafeReplicaSize: true
    parameters:
      # Inline compression mode for the data pool
      # Further reference: https://docs.ceph.com/docs/nautilus/rados/configuration/bluestore-config-ref/#inline-compression
      compression_mode:
        none
        # gives a hint (%) to Ceph in terms of expected consumption of the total cluster capacity of a given pool
      # for more info: https://docs.ceph.com/docs/master/rados/operations/placement-groups/#specifying-expected-pool-size
      #target_size_ratio: ".5"
  # The list of data pool specs. Can use replication or erasure coding.
  dataPools:
    - failureDomain: host
      replicated:
        size: 3
        # Disallow setting pool with replica 1, this could lead to data loss without recovery.
        # Make sure you're *ABSOLUTELY CERTAIN* that is what you want
        requireSafeReplicaSize: true
      parameters:
        # Inline compression mode for the data pool
        # Further reference: https://docs.ceph.com/docs/nautilus/rados/configuration/bluestore-config-ref/#inline-compression
        compression_mode:
          none
          # gives a hint (%) to Ceph in terms of expected consumption of the total cluster capacity of a given pool
        # for more info: https://docs.ceph.com/docs/master/rados/operations/placement-groups/#specifying-expected-pool-size
        #target_size_ratio: ".5"
  # Whether to preserve filesystem after CephFilesystem CRD deletion
  preserveFilesystemOnDelete: true
  # The metadata service (mds) configuration
  metadataServer:
    # The number of active MDS instances
    activeCount: 1
    # Whether each active MDS instance will have an active standby with a warm metadata cache for faster failover.
    # If false, standbys will be available, but will not have a warm cache.
    activeStandby: true
    # The affinity rules to apply to the mds deployment
    placement:
      #  nodeAffinity:
      #    requiredDuringSchedulingIgnoredDuringExecution:
      #      nodeSelectorTerms:
      #      - matchExpressions:
      #        - key: role
      #          operator: In
      #          values:
      #          - mds-node
      #  topologySpreadConstraints:
      #  tolerations:
      #  - key: mds-node
      #    operator: Exists
      #  podAffinity:
      podAntiAffinity:
        requiredDuringSchedulingIgnoredDuringExecution:
          - labelSelector:
              matchExpressions:
                - key: app
                  operator: In
                  values:
                    - rook-ceph-mds
            # topologyKey: kubernetes.io/hostname will place MDS across different hosts
            topologyKey: kubernetes.io/hostname
        preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            podAffinityTerm:
              labelSelector:
                matchExpressions:
                  - key: app
                    operator: In
                    values:
                      - rook-ceph-mds
              # topologyKey: */zone can be used to spread MDS across different AZ
              # Use <topologyKey: failure-domain.beta.kubernetes.io/zone> in k8s cluster if your cluster is v1.16 or lower
              # Use <topologyKey: topology.kubernetes.io/zone>  in k8s cluster is v1.17 or upper
              topologyKey: topology.kubernetes.io/zone
    # A key/value list of annotations
    annotations:
    #  key: value
    # A key/value list of labels
    labels:
    #  key: value
    resources:
    # The requests and limits set here, allow the filesystem MDS Pod(s) to use half of one CPU core and 1 gigabyte of memory
    #  limits:
    #    cpu: "500m"
    #    memory: "1024Mi"
    #  requests:
    #    cpu: "500m"
    #    memory: "1024Mi"
    # priorityClassName: my-priority-class
  mirroring:
    enabled: false
```

![image-20240516135132599](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172247697.png)

指定rook-cephfs为默认的StorageClass

```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: rook-cephfs
  annotations:
    storageclass.kubernetes.io/is-default-class: "true"
# Change "rook-ceph" provisioner prefix to match the operator namespace if needed
provisioner: rook-ceph.cephfs.csi.ceph.com
parameters:
  # clusterID is the namespace where operator is deployed.
  clusterID: rook-ceph

  # CephFS filesystem name into which the volume shall be created
  fsName: myfs
  # Ceph pool into which the volume shall be created
  # Required for provisionVolume: "true"
  pool: myfs-data0
  # The secrets contain Ceph admin credentials. These are generated automatically by the operator
  # in the same namespace as the cluster.
  csi.storage.k8s.io/provisioner-secret-name: rook-csi-cephfs-provisioner
  csi.storage.k8s.io/provisioner-secret-namespace: rook-ceph
  csi.storage.k8s.io/controller-expand-secret-name: rook-csi-cephfs-provisioner
  csi.storage.k8s.io/controller-expand-secret-namespace: rook-ceph
  csi.storage.k8s.io/node-stage-secret-name: rook-csi-cephfs-node
  csi.storage.k8s.io/node-stage-secret-namespace: rook-ceph

reclaimPolicy: Delete
allowVolumeExpansion: true
```

![image-20240516135340377](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172248962.png)

## 测试

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name:  nginx-deploy2
  namespace: default
  labels:
    app:  nginx-deploy2
spec:
  selector:
    matchLabels:
      app: nginx-deploy2
  replicas: 3
  strategy:
    rollingUpdate:
      maxSurge: 25%
      maxUnavailable: 25%
    type: RollingUpdate
  template:
    metadata:
      labels:
        app:  nginx-deploy2
    spec:
      containers:
      - name:  nginx-deploy2
        image:  nginx
        volumeMounts:
        - name: localtime
          mountPath: /etc/localtime
        - name: nginx-html-storage
          mountPath: /usr/share/nginx/html
      volumes:
        - name: localtime
          hostPath:
            path: /usr/share/zoneinfo/Asia/Shanghai
        - name: nginx-html-storage
          persistentVolumeClaim:
            claimName: nginx-pv-claim2
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: nginx-pv-claim2
  labels:
    app:  nginx-deploy2
spec:
  storageClassName: rook-cephfs
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 10Mi
```

![image-20240516135616183](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172248529.png)

测试，创建deploy、修改页面、删除deploy，新建deploy是否绑定成功，数据是否在。

![image-20240516141646062](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172248378.png)



![image-20240516141725562](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172248234.png)

## pvc扩容

参照CSI（容器存储接口）文档： 

卷扩容：https://www.rook.io/docs/rook/v1.6/ceph-csi-drivers.html#dynamically-expand-volume

### 动态卷扩容

![image-20240516141930367](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172247674.png)

修改pvc的spec下storage

![image-20240516145607623](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172247610.png)

![image-20240516145654070](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405172247234.png)



无状态应用（3个副本）使用共享存储。很多人操作一个pvc挂载的一个pv；也不丢失

- 其他Pod可以对数据进行修改
- MySQL 有状态做成主节点。。。MySQL - Master  ---- pv
- MySQL 无状态只读 挂载master的 pvc。