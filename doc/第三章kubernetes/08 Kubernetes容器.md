在 Kubernetes 中，Pod 是最小的部署单位，它可以包含一个或多个容器。

思考：我们在k8s里面的容器和docker的容器有什么异同？

# 镜像

在 Kubernetes 的 Pod 中使用容器镜像之前，我们必须将其推送到一个镜像仓库（或者使用仓库中已经 有的容器镜像）。在 Kubernetes 的 Pod 定义中定义容器时，必须指定容器所使用的镜像，容器中的 image 字段支持与 docker 命令一样的语法，包括私有镜像仓库和标签。

![image-20240422104702097](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231146861.png)

如果使用 hub.dokcer.com Registry 中的镜像，可以省略 registry 地址和 registry 端口。例如： nginx:latest



kubectl explain 解析一个资源Pod改怎么编写yaml,比如有关 Pod 中容器containers的字段：

```shell
[root@k8s-master ~]# kubectl explain pod.spec.containers
KIND:     Pod
VERSION:  v1

RESOURCE: containers <[]Object>

DESCRIPTION:
     List of containers belonging to the pod. Containers cannot currently be
     added or removed. There must be at least one container in a Pod. Cannot be
     updated.

     A single application container that you want to run within a pod.

FIELDS:
   args	<[]string>
     Arguments to the entrypoint. The docker image's CMD is used if this is not
     provided. Variable references $(VAR_NAME) are expanded using the
     container's environment. If a variable cannot be resolved, the reference in
     the input string will be unchanged. The $(VAR_NAME) syntax can be escaped
     with a double $$, ie: $$(VAR_NAME). Escaped references will never be
     expanded, regardless of whether the variable exists or not. Cannot be
     updated. More info:
     https://kubernetes.io/docs/tasks/inject-data-application/define-command-argument-container/#running-a-command-in-a-shell

   command	<[]string>
     Entrypoint array. Not executed within a shell. The docker image's
     ENTRYPOINT is used if this is not provided. Variable references $(VAR_NAME)
     are expanded using the container's environment. If a variable cannot be
     resolved, the reference in the input string will be unchanged. The
     $(VAR_NAME) syntax can be escaped with a double $$, ie: $$(VAR_NAME).
     Escaped references will never be expanded, regardless of whether the
     variable exists or not. Cannot be updated. More info:
     https://kubernetes.io/docs/tasks/inject-data-application/define-command-argument-container/#running-a-command-in-a-shell

   env	<[]Object>
     List of environment variables to set in the container. Cannot be updated.

   envFrom	<[]Object>
     List of sources to populate environment variables in the container. The
     keys defined within a source must be a C_IDENTIFIER. All invalid keys will
     be reported as an event when the container is starting. When a key exists
     in multiple sources, the value associated with the last source will take
     precedence. Values defined by an Env with a duplicate key will take
     precedence. Cannot be updated.

   image	<string>
     Docker image name. More info:
     https://kubernetes.io/docs/concepts/containers/images This field is
     optional to allow higher level config management to default or override
     container images in workload controllers like Deployments and StatefulSets.

   imagePullPolicy	<string>
     Image pull policy. One of Always, Never, IfNotPresent. Defaults to Always
     if :latest tag is specified, or IfNotPresent otherwise. Cannot be updated.
     More info:
     https://kubernetes.io/docs/concepts/containers/images#updating-images

   lifecycle	<Object>
     Actions that the management system should take in response to container
     lifecycle events. Cannot be updated.

   livenessProbe	<Object>
     Periodic probe of container liveness. Container will be restarted if the
     probe fails. Cannot be updated. More info:
     https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle#container-probes

   name	<string> -required-
     Name of the container specified as a DNS_LABEL. Each container in a pod
     must have a unique name (DNS_LABEL). Cannot be updated.

   ports	<[]Object>
     List of ports to expose from the container. Exposing a port here gives the
     system additional information about the network connections a container
     uses, but is primarily informational. Not specifying a port here DOES NOT
     prevent that port from being exposed. Any port which is listening on the
     default "0.0.0.0" address inside a container will be accessible from the
     network. Cannot be updated.

   readinessProbe	<Object>
     Periodic probe of container service readiness. Container will be removed
     from service endpoints if the probe fails. Cannot be updated. More info:
     https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle#container-probes

   resources	<Object>
     Compute Resources required by this container. Cannot be updated. More info:
     https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/

   securityContext	<Object>
     Security options the pod should run with. More info:
     https://kubernetes.io/docs/concepts/policy/security-context/ More info:
     https://kubernetes.io/docs/tasks/configure-pod-container/security-context/

   startupProbe	<Object>
     StartupProbe indicates that the Pod has successfully initialized. If
     specified, no other probes are executed until this completes successfully.
     If this probe fails, the Pod will be restarted, just as if the
     livenessProbe failed. This can be used to provide different probe
     parameters at the beginning of a Pod's lifecycle, when it might take a long
     time to load data or warm a cache, than during steady-state operation. This
     cannot be updated. More info:
     https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle#container-probes

   stdin	<boolean>
     Whether this container should allocate a buffer for stdin in the container
     runtime. If this is not set, reads from stdin in the container will always
     result in EOF. Default is false.

   stdinOnce	<boolean>
     Whether the container runtime should close the stdin channel after it has
     been opened by a single attach. When stdin is true the stdin stream will
     remain open across multiple attach sessions. If stdinOnce is set to true,
     stdin is opened on container start, is empty until the first client
     attaches to stdin, and then remains open and accepts data until the client
     disconnects, at which time stdin is closed and remains closed until the
     container is restarted. If this flag is false, a container processes that
     reads from stdin will never receive an EOF. Default is false

   terminationMessagePath	<string>
     Optional: Path at which the file to which the container's termination
     message will be written is mounted into the container's filesystem. Message
     written is intended to be brief final status, such as an assertion failure
     message. Will be truncated by the node if greater than 4096 bytes. The
     total message length across all containers will be limited to 12kb.
     Defaults to /dev/termination-log. Cannot be updated.

   terminationMessagePolicy	<string>
     Indicate how the termination message should be populated. File will use the
     contents of terminationMessagePath to populate the container status message
     on both success and failure. FallbackToLogsOnError will use the last chunk
     of container log output if the termination message file is empty and the
     container exited with an error. The log output is limited to 2048 bytes or
     80 lines, whichever is smaller. Defaults to File. Cannot be updated.

   tty	<boolean>
     Whether this container should allocate a TTY for itself, also requires
     'stdin' to be true. Default is false.

   volumeDevices	<[]Object>
     volumeDevices is the list of block devices to be used by the container.

   volumeMounts	<[]Object>
     Pod volumes to mount into the container's filesystem. Cannot be updated.

   workingDir	<string>
     Container's working directory. If not specified, the container runtime's
     default will be used, which might be configured in the container image.
     Cannot be updated.
```

Kubernetes中默认情况下，`imagePullPolicy` 的值取决于镜像标签（tag）：

- 如果镜像标签是 `:latest`，则默认策略是 `Always`。
- 如果镜像标签不是 `:latest`，则默认策略是 `IfNotPresent`。

如果您期望每次启动 Pod 时，都强制从镜像仓库抓取镜像，可以尝试 如下方式：

1.设置 container 中的 imagePullPolicy 为 Always

2.省略 imagePullPolicy 字段，并使用 :latest tag 的镜像



## 下载阿里云私有仓库镜像

在docker中需要docker login登陆后然后docker pull下载私有仓库镜像。

在 Kubernetes 中下载私有仓库中的镜像必须携带密钥

![image-20240422142918532](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231146361.png)

下载镜像前

![image-20240422143704466](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231146679.png)

### 创建 Secret 对象

首先，你需要创建一个 Kubernetes 的 Secret 对象来存储私有仓库的身份验证信息。你可以使用 `kubectl create secret` 命令来创建 Secret，确保它包含了私有仓库的 URL、用户名和密码等信息。例如：

```shell
kubectl create secret docker-registry my-private-registry \
    --docker-server=REGISTRY_SERVER \
    --docker-username=USERNAME \
    --docker-password=PASSWORD \
```

秘钥默认在default名称空间，不能被其他名称空间共享

生成hello名称空间的密钥，-n指定命名空间

```shell
kubectl create secret -n hello docker-registry my-aliyun-registry \
    --docker-server=registry.cn-hangzhou.aliyuncs.com \
    --docker-username=17862835796 \
    --docker-password=aaa291320608
```

- 创建一个名为 `my-aliyun-registry` 的 Secret 对象，并将其放置在命名空间（Namespace）`hello` 中。这个 Secret 将用于存储阿里云容器镜像服务的身份验证信息。
- `--docker-server=registry.cn-hangzhou.aliyuncs.com`: 这个选项指定了阿里云容器镜

![image-20240422145552201](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231146988.png)

查看hello命名空间已创建的secret

`default-token-gqz55` 是 Kubernetes 自动为每个命名空间（Namespace）创建的一个 Secret 对象。这个 Secret 对象用于存储与该命名空间中默认的 ServiceAccount 关联的访问令牌。

![image-20240422145759814](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231146466.png)

.dockerconfigjson对应的就是生成的密钥信息

![image-20240422151353857](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231146301.png)

### 在 Pod 中引用 Secret

在 Pod 的配置文件中引用上一步创建的 Secret。你可以将 Secret 的名称和类型（`kubernetes.io/dockerconfigjson`）指定为 `imagePullSecrets` 字段。

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: my-pod
  namespace: hello
spec:
  containers:
  - name: my-container
    image: registry.cn-hangzhou.aliyuncs.com/dongguo/devops-demo:v1.0
    imagePullPolicy: Always
  imagePullSecrets:
  - name: my-aliyun-registry
```

my-aliyun-registry就是上一步创建的 Secret 

这样，Kubernetes 将会自动注入你的私有仓库的身份验证信息到 Pod 中，以便下载私有仓库中的镜像。

![image-20240422151835804](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231146509.png)

查看hello命名空间中的pod

![image-20240422152635830](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231146999.png)



下载镜像时会先尝试从 `registry.cn-hangzhou.aliyuncs.com` 这个私有镜像仓库中拉取镜像，如果该仓库中不存在该镜像，则会尝试从 Docker Hub（公共镜像仓库）中拉取。

公有仓库是默认的，不指定imagePullSecrets会优先从公有仓库中下载。

比如下载一个私有镜像仓库中不存在的镜像

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: my-nginx
  namespace: hello
spec:
  containers:
  - name: nginx
    image: nginx
    imagePullPolicy: Always
  imagePullSecrets:
  - name: my-aliyun-registry
```

![image-20240422153550568](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231146131.png)

查看hello命名空间中的pod

![image-20240422153612937](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231146769.png)

# 环境变量

一般在容器镜像的标签中包含一些环境变量信息。例如mysql镜像

![image-20240422160815113](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231146673.png)

我们可以在 Pod 配置中引用这些标签。`env` 是 Kubernetes Pod 配置中的一个字段，用于指定容器中的环境变量。通过 `env` 字段，你可以向容器中注入静态的环境变量。

mysql镜像设置root用户的密码MYSQL_ROOT_PASSWORD，以及mysql初始创建数据库MYSQL_DATABASE

注意value使用字符串

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: my-mysql
  namespace: hello
spec:
  containers:
  - name: mysql
    image: mysql:8.0.36
    env: 
    - name: MYSQL_ROOT_PASSWORD
      value: "root"
    - name: MYSQL_DATABASE
      value: "devpos"
```

![image-20240422165637698](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231146514.png)

查看hello命名空间中的pod

![image-20240422165930048](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231146296.png)

进入pod中，输入username、password能够顺利进入mysql数据库，能够看到初始生成的devpos数据库

# 启动命令

在 Kubernetes Pod 的配置中，可以使用 `command` 和 `args` 字段来指定容器启动时要执行的命令及其参数。这两个字段通常用于覆盖容器中 Dockerfile 中定义的默认启动命令。

- `command` 字段用于指定容器的启动命令，它是一个字符串数组，表示要执行的命令及其参数。通常情况下，如果 Dockerfile 中已经定义了启动命令，你不需要在 Pod 配置中显式地指定 `command` 字段。
- `args` 字段用于指定容器启动命令的参数，它也是一个字符串数组。你可以使用 `args` 字段来为容器的启动命令提供额外的参数。

![image-20240422173505786](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231146463.png)

### command

创建my-command的pod，使用command覆盖nginx的启动命令，变成输出msg，sleep 3600睡眠3600秒是为了不让nginx执行完echo命令后直接退出

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: my-command
  namespace: hello
spec:
  containers:
  - name: command-test
    image: nginx
    command: 
    - /bin/sh
    - -c
    - "echo ${msg}; sleep 3600"
    env: 
    - name: msg
      value: "hello nginx"
```

![image-20240422172809115](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231146173.png)

查看my-command输出的日志，本来是nginx的启动日志，现在只输出了hello, nginx

![image-20240422172828438](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231147951.png)

### args

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: my-args
  namespace: hello
spec:
  containers:
  - name: args-test
    image: nginx
    command: ["echo"]
    args: ["Hello, Kubernetes!"]
```

![image-20240422174117005](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231147962.png)

查看my-args输出的日志

![image-20240422174147863](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231147814.png)

# 生命周期容器钩子

Kubernetes中为容器启动后、删除前提供了 hook（钩子函数）

![image-20240422175518908](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231147584.png)

- PostStart

此钩子函数在容器创建后（还未运行成功）将立刻执行。但是，并不能保证该钩子函数在容器的 ENTRYPOINT （启动命令）之前 执行。该钩子函数没有输入参数。

- PreStop

此钩子函数在容器被 terminate（终止）之前执行，例如：

​	通过接口调用删除容器所在 Pod

​	某些管理事件的发生：健康检查失败、资源紧缺等

如果容器已经被关闭或者进入了 completed 状态，preStop 钩子函数的调用将失败。该函数的执行是同步的，即kubernetes 将在该函数完成执行之后才删除容器。该钩子函数没有输入参数。

```shell
[root@k8s-master k8s]# kubectl explain pod.spec.containers.lifecycle
KIND:     Pod
VERSION:  v1

RESOURCE: lifecycle <Object>

DESCRIPTION:
     Actions that the management system should take in response to container
     lifecycle events. Cannot be updated.

     Lifecycle describes actions that the management system should take in
     response to container lifecycle events. For the PostStart and PreStop
     lifecycle handlers, management of the container blocks until the action is
     complete, unless the container process fails, in which case the handler is
     aborted.

FIELDS:
   postStart	<Object>
     PostStart is called immediately after a container is created. If the
     handler fails, the container is terminated and restarted according to its
     restart policy. Other management of the container blocks until the hook
     completes. More info:
     https://kubernetes.io/docs/concepts/containers/container-lifecycle-hooks/#container-hooks

   preStop	<Object>
     PreStop is called immediately before a container is terminated due to an
     API request or management event such as liveness/startup probe failure,
     preemption, resource contention, etc. The handler is not called if the
     container crashes or exits. The reason for termination is passed to the
     handler. The Pod's termination grace period countdown begins before the
     PreStop hooked is executed. Regardless of the outcome of the handler, the
     container will eventually terminate within the Pod's termination grace
     period. Other management of the container blocks until the hook completes
     or until the termination grace period is reached. More info:
     https://kubernetes.io/docs/concepts/containers/container-lifecycle-hooks/#container-hooks
```

Kubernetes 在容器启动后立刻发送 postStart 事件，但是并不能确保 postStart 事件处理程序在容器 的 EntryPoint 之前执行。postStart 事件处理程序相对于容器中的进程来说是异步的（同时执行）， 然而，Kubernetes 在管理容器时，将一直等到 postStart 事件处理程序结束之后，才会将容器的状 态标记为 Running。

Kubernetes 在决定关闭容器时，立刻发送 preStop 事件，并且，将一直等到 preStop 事件处理程序 结束或者 Pod 的 --grace-period 超时，才删除容器

在 Kubernetes 中，`PostStart` 和 `PreStop` 钩子函数可以使用以下三种方式之一定义：

1. **exec**: 使用 `exec` 可以在容器内部执行一个命令或者脚本。该命令或脚本可以是容器内部可执行的任意命令，比如 Shell 脚本或者其他可执行文件。你可以指定要执行的命令及其参数。
2. **httpGet**: 使用 `httpGet` 可以发送一个 HTTP GET 请求到指定的目标地址。该目标地址可以是容器内部的一个服务或者一个外部服务。你可以指定要发送的 HTTP 请求的目标地址、端口号和路径等信息。
3. **tcpSocket**:`tcpSocket` 类型的钩子函数用于向容器发送 TCP Socket 连接请求。

## exec

参数:

- `command`: 一个字符串数组，表示要在容器内部执行的命令或者脚本。这个数组的第一个元素应该是要执行的命令或者脚本的路径，后面的元素是命令或者脚本的参数。例如：`["/bin/sh", "-c", "echo 'Container started'"]`。



```shell
apiVersion: v1
kind: Pod
metadata:
  name: lifecycle-exec
spec:
  containers:
  - name: my-nginx
    image: nginx
    lifecycle:
      postStart:
        exec:
          command: ["/bin/sh","-c","echo postStart... > /usr/share/nginx/html/index.html"] #修改index.html内容
      preStop:
        exec:
          command: ["/bin/sh", "-c", "sleep 10"] #容器在终止之前休眠 10 秒钟
```

![image-20240423094921122](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231147722.png)

1）访问容器中的nginx查看首页是否是容器启动后执行的钩子函数

![image-20240423101517524](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231147305.png)

2）查看preStop钩子函数，删除pod

![image-20240423104422258](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231147786.png)

pod中日志等待10秒后才执行终止操作。

![image-20240423104347560](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231147719.png)

## httpGet

参数:

- `path`: 要发送 HTTP 请求的路径。例如：`"/healthz"`。
- `port`: 要发送 HTTP 请求的目标端口号,必填。例如：`8080`。
- `host`: 要发送 HTTP 请求的目标主机名。例如：`"localhost"`。此参数是可选的，如果未指定，则使用 Pod 的 IP 地址。
- `scheme`: 要使用的协议方案。默认为 `HTTP`。可以设置为 `HTTPS`。



实现在容器创建后、容器关闭前访问my-nginx的nginx

![image-20240423111501998](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231147169.png)

在创建pod前持续先监控my-nginx的日志

![image-20240423111519291](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231147852.png)



```shell
apiVersion: v1
kind: Pod
metadata:
  name: lifecycle-httpget
spec:
  containers:
  - name: my-nginx
    image: nginx
    lifecycle:
      postStart:
        httpGet:
          host: "10.244.169.180"
          path: "/postStart"
          port: 80
          scheme: "HTTP"
      preStop:
        httpGet:
          host: "10.244.169.180"
          path: "/preStop"
          port: 80
          scheme: "HTTP"
```

![image-20240423111651215](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231147229.png)

在容器创建后、容器关闭前，my-nginx的日志中都打印出http请求

![image-20240423111757382](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404231147463.png)

# 资源限制

在 Kubernetes 中，你可以使用资源限制来限制容器可以使用的 CPU 和内存资源。这可以帮助你控制容器在节点上的资源使用情况，防止某个容器使用过多的资源导致节点性能下降或者其他容器受到影响。

资源限制分为两种类型：请求（Requests）和限制（Limits）。

1. **资源请求（Requests）**：指定容器所需的最小资源量。Kubernetes 将根据这些请求值为容器分配节点资源。如果节点资源不足，容器可能会被延迟调度或者拒绝。
2. **资源限制（Limits）**：指定容器可使用的最大资源量。这是 Kubernetes 使用的关键指标，用于控制容器的资源消耗。如果容器超出了限制，Kubernetes 可能会将其终止或者限制其使用率。

```shell
apiVersion: v1
kind: Pod
metadata:
  name: qos-demo
spec:
  containers:
  - name: my-nginx
    image: nginx
    resources:
      limits: # 限制最大大小 -Xmx
        memory: "200Mi"
        cpu: "700m"
      # 启动默认给分配的大小 -Xms
      requests:
        memory: "200Mi"
        cpu: "700m"
```

# 探针

在 Kubernetes 中，探针（Probes）用于检查容器的健康状态以及确定何时应该将容器标记为准备就绪或者需要重启。Kubernetes 支持三种类型的探针：

1. **Liveness Probe（存活探针）**：用于确定容器是否在运行。如果存活探针失败，Kubernetes 将重启容器。

   对于存活探针（Liveness Probe）：如果存活探针失败，Kubernetes 将自动重启容器。它会尝试将容器恢复到健康状态，并继续运行应用程序。如果存活探针失败，Kubernetes 将首先重启容器，希望通过重新启动容器来恢复应用程序的健康状态。如果重启容器仍然无法解决问题，Kubernetes 可能会根据配置的重启策略进一步采取行动。重启策略包括：

   Always（默认）：始终重启容器，无限次数地尝试恢复应用程序的健康状态。
   OnFailure：仅在容器失败（退出状态码非零）时重启容器，尝试恢复应用程序的健康状态。
   Never：永不重启容器，不会尝试恢复应用程序的健康状态。

2. **Readiness Probe（就绪探针）**：用于确定容器是否准备好接收流量。

   对于就绪探针（Readiness Probe）：如果就绪探针失败，Kubernetes 将从服务负载均衡的池中剔除该容器。这意味着新的流量将不会被路由到该容器，直到就绪探针成功为止。这可以确保只有健康的容器能够接收流量，避免将流量发送到尚未准备好的容器上。一旦就绪探针成功，Kubernetes 将再次将容器纳入服务负载均衡，并开始将新的流量路由到该容器。

3. **Startup Probe（启动探针）**：用于确定容器是否已经启动并准备好接收正常的流量。与就绪探针类似，但在容器启动时进行一次性检查，而不会影响其后续状态。它主要用于检测应用程序是否成功启动，并在启动过程中提供一定的等待时间。
   如果启动探针失败，Kubernetes 不会采取任何特殊行动。这是因为启动探针失败只意味着应用程序尚未成功启动，并且不会触发容器的重启或负载均衡操作。



探针同样可以使用以下三种方式之一定义：

执行命令（Exec）：通过在容器内执行特定的命令来检查应用程序的状态。如果命令的返回状态码是 0，探针被认为是成功的；否则，探针被认为是失败的。
发送 HTTP 请求（HTTP GET）：通过发送 HTTP GET 请求到容器内的指定端点来检查应用程序的状态。如果返回的 HTTP 状态码在 2xx 或 3xx 范围内，探针被认为是成功的；否则，探针被认为是失败的。
TCP 套接字（TCP Socket）：通过尝试建立到容器内指定端口的 TCP 连接来检查应用程序的状态。如果连接成功建立，探针被认为是成功的；否则，探针被认为是失败的。
你还可以调整探针的配置参数，例如 `initialDelaySeconds`（初始延迟时间）、`periodSeconds`（检查周期）、`failureThreshold`（失败阈值）等。

```shell
apiVersion: v1
kind: Pod
metadata:
  name: qos-demo
spec:
  containers:
  - name: my-nginx
    image: nginx
    livenessProbe:
      httpGet:
        path: /healthz
        port: 8080
      initialDelaySeconds: 15
      periodSeconds: 10
    readinessProbe:
      httpGet:
        path: /healthz
        port: 8080
      initialDelaySeconds: 5
      periodSeconds: 3
    startupProbe:
      httpGet:
        path: /healthz
        port: 8080
      failureThreshold: 30
      periodSeconds: 10
```

