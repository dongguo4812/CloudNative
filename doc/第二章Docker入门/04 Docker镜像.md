# Docker镜像介绍

Docker镜像是一种轻量级、可执行的独立软件包，它包含运行某个软件所需的所有内容，我们把应用程序和配置依赖打包好形成一个可交付的运行环境(包括代码、运行时需要的库、环境变量和配置文件等)，这个打包好的运行环境就是image镜像文件。

只有通过这个镜像文件才能生成Docker容器实例(类似Java中new出来一个对象)。

# 镜像的分层

以我们的pull为例，在下载的过程中我们可以看到docker的镜像好像是在一层一层的在下载

```shell
[root@dongguo /]# docker pull tomcat
Using default tag: latest
latest: Pulling from library/tomcat
0e29546d541c: Pull complete 
9b829c73b52b: Pull complete 
cb5b7ae36172: Pull complete 
6494e4811622: Pull complete 
668f6fcc5fa5: Pull complete 
dc120c3e0290: Pull complete 
8f7c0eebb7b1: Pull complete 
77b694f83996: Pull complete 
0f611256ec3a: Pull complete 
4f25def12f23: Pull complete 
Digest: sha256:9dee185c3b161cdfede1f5e35e8b56ebc9de88ed3a79526939701f3537a52324
Status: Downloaded newer image for tomcat:latest
docker.io/library/tomcat:latest
[root@dongguo /]# docker images
REPOSITORY     TAG       IMAGE ID       CREATED          SIZE
tomcat         latest    fb5657adc892   2 years ago      680MB
```

拿tomcat为例，一个单独的tomcat一般只有100多M，但是docker拉取的镜像有600多M，这是为什么？

因为tomcat的运行不仅仅只需要tomcat，还需要java、centos等等依赖，所以docker的tomcat镜像中会包含有java、centos等等，所以会很大。

![image-20240407104219663](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071222247.png)





## UnionFS（联合文件系统）

UnionFS（联合文件系统）：Union文件系统（UnionFS）是一种分层、轻量级并且高性能的文件系统，它支持对文件系统的修改作为一次提交来一层层的叠加，同时可以将不同目录挂载到同一个虚拟文件系统下(unite several directories into a single virtual filesystem)。

Union 文件系统是 Docker 镜像的基础。镜像可以通过分层来进行继承，基于基础镜像（没有父镜像），可以制作各种具体的应用镜像。

特性：一次同时加载多个文件系统，但从外面看起来，只能看到一个文件系统，联合加载会把各层文件系统叠加起来，这样最终的文件系统会包含所有底层的文件和目录



# Docker镜像加载原理

docker的镜像实际上由一层一层的文件系统组成，这种层级的文件系统主要依赖于UnionFS（联合文件系统）。

当Docker需要加载一个镜像时，它会按照镜像的层顺序从上到下依次加载。首先，它会加载最底层的基础镜像层，然后依次加载后续的层，直到整个镜像加载完成。

Docker镜像中bootfs和rootfs的概念：

bootfs(boot file system)主要包含bootloader（加载器）和kernel（内核）, bootloader主要是引导加载kernel,

Linux刚启动时会加载bootfs文件系统，在Docker镜像的最底层是引导文件系统bootfs。这一层与我们典型的Linux/Unix系统是一样的，包含boot加载器和内核。当boot加载完成之后整个内核就都在内存中了，此时内存的使用权已由bootfs转交给内核，此时系统也会卸载bootfs。在Docker镜像中，bootfs是一个只读的层，一旦内核加载完成，系统就会卸载bootfs，将控制权完全交给内核。

rootfs (root file system) ，在bootfs之上。包含的就是典型 Linux 系统中的 /dev, /proc, /bin, /etc 等标准目录和文件。rootfs就是各种不同的操作系统发行版，比如Ubuntu，Centos等等。

![image-20240407104311210](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071222126.png)



思考：平时我们安装进虚拟机的ubuntu都是几个G，为什么docker这里才几十M？？

```shell
[root@dongguo /]# docker images
REPOSITORY     TAG       IMAGE ID       CREATED          SIZE
ubuntu         latest    ba6acccedd29   2 years ago      72.8MB
```

Docker使用了容器化技术，它共享了主机的内核，并且只需要提供应用程序所需的文件系统和其他资源。这意味着Docker镜像不需要包含完整的操作系统内核，而只需要包含应用程序及其依赖的库和文件。这就是为什么Docker镜像通常比虚拟机镜像小得多的原因。

具体到Ubuntu的Docker镜像，它通常只包含了一个精简的Ubuntu文件系统（rootfs），这个文件系统只包含了运行Ubuntu所需的最基本的命令、工具和程序库。因为Docker容器共享了主机的内核，所以不需要在镜像中包含内核。这就大大减小了镜像的大小。



# 为什么 Docker 镜像要采用这种分层结构呢

**共享资源**：Docker镜像的分层设计允许多个镜像共享相同的基础层。这在多个镜像从相同的基础镜像构建时尤为有用。通过在磁盘上仅保存一份基础镜像，并在内存中仅加载一份基础镜像，可以为所有容器服务，从而节省存储空间。

**灵活性**：分层结构使得Docker镜像在构建、部署和更新过程中非常灵活。每个分层都是独立的，包含特定的更改，这允许镜像的构建过程更加模块化。此外，基于现有的镜像构建新的镜像时，只需在现有镜像的基础上添加新的分层，无需重新复制整个镜像，这大大提高了构建效率。

**Copy-on-Write（COW）特性**：Docker利用COW技术确保对容器的修改仅发生在容器层，而镜像层保持只读。这意味着修改被限制在单个容器内，不会影响到其他容器共享的基础镜像。这种设计保证了镜像的一致性和稳定性。





Docker镜像层都是只读的，容器层是可写的。
当容器启动时，一个新的可写层被加载到镜像的顶部。这一层通常被称作“容器层”，“容器层”之下的都叫“镜像层”。

所有对容器的改动 - 无论添加、删除、还是修改文件都只会发生在容器层中。

![image-20240407112602786](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071222703.png)

# Docker镜像commit操作

docker commit  提交容器副本使之成为一个新的镜像

```shell
docker commit -m="提交的描述信息" -a="作者" 容器ID 要创建的目标镜像名:[标签名]
```



## 演示将ubuntu提交为新的镜像

### 1 从Hub上下载ubuntu镜像到本地并成功运行

```shell
docker pull ubuntu
docker run -it ubuntu /bin/bash
```

因为之前已经下载过ubuntu镜像，这里直接运行ubuntu

```shell
[root@dongguo /]# docker images
REPOSITORY     TAG       IMAGE ID       CREATED             SIZE
import/ubutu   1.0.0     47658ef456f9   About an hour ago   72.8MB
tomcat         latest    fb5657adc892   2 years ago         680MB
redis          latest    7614ae9453d1   2 years ago         113MB
ubuntu         latest    ba6acccedd29   2 years ago         72.8MB
[root@dongguo /]# docker run -it ubuntu /bin/bash
root@03b17579cd34:/# 
```

### 2 默认的Ubuntu镜像是不带着vim命令的

```shell
root@03b17579cd34:/# vim a.txt
bash: vim: command not found
```

### 3 安装vim

docker容器内执行上述两条命令：

```shell
#先更新包管理工具
apt-get update      
#再安装需要的vim
apt-get -y install vim
```

### 4.使用vim命令验证

再次执行vim a.txt 进入到文本编辑，说明安装成功，使用不保存方式:q!退出即可

### 5 安装完成后，commit我们自己的新镜像

```shell
docker commit -m="提交的描述信息" -a="作者" 容器ID 要创建的目标镜像名:[标签名]
```

如：

```shell
docker commit -m="add vim" -a="dongguo" 03b17579cd34 dongguo/myubuntu:1.0.0
```





这里开启了一个新的会话，执行命令，可以看到dongguo/myubuntu镜像已经达到191M

![image-20240407114448527](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071222711.png)

### 6启动我们的新镜像并和原来的对比

1.启动dongguo/myubuntu:1.0.0镜像

![image-20240407114648597](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071222717.png)

2.vim进行文件编辑并保存

![image-20240407114739654](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071222791.png)

3.查看文件

![image-20240407114807855](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404071222556.png)

### 总结

官网默认下载的Ubuntu镜像是没有vim命令

我们自己commit构建新的镜像，新增加了vim功能，可以成功使用。

