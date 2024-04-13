在了解DockerFile之前我们使用的是docker commit命令构建镜像

基于docker commit命令构建镜像：通过启动容器并对其所做的更改，然后使用docker commit命令将更改保存到新镜像中。这种方法不够灵活且不可移植，因为它需要在特定的环境中运行并对容器进行更改。

现在我们可以使用Dockerfile构建镜像。

# DockerFile简介

https://docs.docker.com/reference/dockerfile/

DockerFile是用于构建Docker镜像的文本文件，其中包含了一系列指令和参数。DockerFile中的指令描述了如何构建镜像，可以指定从哪个镜像启动、安装哪些软件包、配置环境变量、拷贝文件等等。

通过编写DockerFile，可以将应用程序和其依赖打包成一个可移植的镜像，方便在不同的环境中运行。同时，使用DockerFile进行构建还可以保证镜像的一致性和可重复性。

DockerFile通常由以下几个部分组成：

- 基础镜像指令（FROM）
- 维护者信息指令（MAINTAINER）
- 镜像构建指令（RUN、COPY、ADD、CMD、ENTRYPOINT、EXPOSE、ENV、USER、WORKDIR、VOLUME等）
- 容器启动时执行指令（CMD、ENTRYPOINT）

在构建Docker镜像时，可以使用docker build命令来读取DockerFile，并自动执行其中的指令。



## Dockerfile构建镜像三个步骤

1.编写Dockerfile文件

2.build命令读取**Dockerfile**构建镜像

3.docker run运行容器实例

![image-20240408163015994](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082058947.png)



## Dockerfile、Docker镜像与Docker容器之间的关系

1 Dockerfile是Docker镜像的构建文件，Dockerfile定义了进程需要的一切东西。Dockerfile涉及的内容包括执行代码或者是文件、环境变量、依赖包、运行时环境、动态链接库、操作系统的发行版、服务进程和内核进程(当应用进程需要和系统服务和内核进程打交道，这时需要考虑如何设计namespace的权限控制)等等;

2 Docker镜像是一个包含了应用程序运行所需的所有依赖项和配置的静态文件，可以看作是一个应用程序运行时的快照。Docker镜像可以通过Dockerfile构建，也可以从Docker Hub等镜像仓库下载。

通过Dockerfile构建时，在用Dockerfile定义一个文件之后，docker build时会产生一个Docker镜像，当运行 Docker镜像时会真正开始提供服务;

3 Docker容器是基于Docker镜像启动的应用程序运行环境，可以看作是Docker镜像的一个实例。Docker容器可以在任何支持Docker运行的平台上启动，如Windows、MacOS、Linux和云平台。通过Docker容器，开发人员和运维人员可以将应用程序和其依赖项打包成一个独立的单元，方便部署和管理。

**更容易理解的表达是**：

从应用软件的角度来看，Dockerfile、Docker镜像与Docker容器分别代表软件的三个不同阶段，

- Dockerfile是软件的原材料
- Docker镜像是软件的交付品
- Docker容器则可以认为是软件镜像的运行态，也即依照镜像运行的容器实例

Dockerfile面向开发，Docker镜像成为交付标准，Docker容器则涉及部署与运维，三者缺一不可，合力充当Docker体系的基石。

![image-20240408163613327](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082058428.png)



# DockerFile构建过程解析规则

构建centos7镜像的DockerFile举例：

```shell
FROM centos:7
MAINTAINER Dongguo
 
ENV MYPATH /usr/local
WORKDIR $MYPATH
 
#安装vim编辑器
RUN yum -y install vim
#安装ifconfig命令查看网络IP
RUN yum -y install net-tools
#安装java8及lib库
RUN yum -y install glibc.i686
RUN mkdir /usr/local/java
#ADD 是相对路径jar,把jdk-8u371-linux-x64.tar.gz添加到容器中,安装包必须要和Dockerfile文件在同一位置
ADD jdk-8u371-linux-x64.tar.gz /usr/local/java/
#配置java环境变量
ENV JAVA_HOME /usr/local/java/jdk1.8.0_371
ENV JRE_HOME $JAVA_HOME/jre
ENV CLASSPATH $JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar:$JRE_HOME/lib:$CLASSPATH
ENV PATH $JAVA_HOME/bin:$PATH

EXPOSE 80
```

1：每条保留字指令都必须为大写字母且后面要跟随至少一个参数

2：指令按照从上到下，顺序执行

3：#表示注释

4：每条指令都会创建一个新的镜像层并对镜像进行提交

### Docker执行Dockerfile的大致流程

（1）docker从基础镜像运行一个容器

（2）执行一条指令并对容器作出修改

（3）执行类似docker commit的操作提交一个新的镜像层

（4）docker再基于刚提交的镜像运行一个新容器

（5）执行dockerfile中的下一条指令直到所有指令都执行完成

# DockerFile常用保留字指令

官网 https://docs.docker.com/engine/reference/builder/

通过tomcat的dockerfile：https://github.com/docker-library/tomcat 了解常用指令

![image-20240408164920286](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082058975.png)

选择tomcat10、jdk17的版本

https://github.com/docker-library/tomcat/blob/master/10.1/jdk17/temurin-jammy/Dockerfile

```dockerfile
#
# NOTE: THIS DOCKERFILE IS GENERATED VIA "apply-templates.sh"
#
# PLEASE DO NOT EDIT IT DIRECTLY.
#

FROM eclipse-temurin:17-jdk-jammy

ENV CATALINA_HOME /usr/local/tomcat
ENV PATH $CATALINA_HOME/bin:$PATH
RUN mkdir -p "$CATALINA_HOME"
WORKDIR $CATALINA_HOME

# let "Tomcat Native" live somewhere isolated
ENV TOMCAT_NATIVE_LIBDIR $CATALINA_HOME/native-jni-lib
ENV LD_LIBRARY_PATH ${LD_LIBRARY_PATH:+$LD_LIBRARY_PATH:}$TOMCAT_NATIVE_LIBDIR

# see https://www.apache.org/dist/tomcat/tomcat-10/KEYS
# see also "versions.sh" (https://github.com/docker-library/tomcat/blob/master/versions.sh)
ENV GPG_KEYS 5C3C5F3E314C866292F359A8F3AD5C94A67F707E A9C5DF4D22E99998D9875A5110C01C5A2F6059E7

ENV TOMCAT_MAJOR 10
ENV TOMCAT_VERSION 10.1.20
ENV TOMCAT_SHA512 6728a28b93c4ef457ed90b8fcaa71a60f9739c4531c59a245879d2075db94c6bac336ea5a28748364df0f81a155d52b0839ecdd0c09adafdea29942a85b265b4

RUN set -eux; \
	\
	savedAptMark="$(apt-mark showmanual)"; \
	apt-get update; \
	apt-get install -y --no-install-recommends \
		ca-certificates \
		curl \
		gnupg \
	; \
	\
	ddist() { \
		local f="$1"; shift; \
		local distFile="$1"; shift; \
		local mvnFile="${1:-}"; \
		local success=; \
		local distUrl=; \
		for distUrl in \
# https://apache.org/history/mirror-history.html
			"https://dlcdn.apache.org/$distFile" \
# if the version is outdated, we have to pull from the archive
			"https://archive.apache.org/dist/$distFile" \
# if all else fails, let's try Maven (https://www.mail-archive.com/users@tomcat.apache.org/msg134940.html; https://mvnrepository.com/artifact/org.apache.tomcat/tomcat; https://repo1.maven.org/maven2/org/apache/tomcat/tomcat/)
			${mvnFile:+"https://repo1.maven.org/maven2/org/apache/tomcat/tomcat/$mvnFile"} \
		; do \
			if curl -fL -o "$f" "$distUrl" && [ -s "$f" ]; then \
				success=1; \
				break; \
			fi; \
		done; \
		[ -n "$success" ]; \
	}; \
	\
	ddist 'tomcat.tar.gz' "tomcat/tomcat-$TOMCAT_MAJOR/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz" "$TOMCAT_VERSION/tomcat-$TOMCAT_VERSION.tar.gz"; \
	echo "$TOMCAT_SHA512 *tomcat.tar.gz" | sha512sum --strict --check -; \
	ddist 'tomcat.tar.gz.asc' "tomcat/tomcat-$TOMCAT_MAJOR/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz.asc" "$TOMCAT_VERSION/tomcat-$TOMCAT_VERSION.tar.gz.asc"; \
	export GNUPGHOME="$(mktemp -d)"; \
	for key in $GPG_KEYS; do \
		gpg --batch --keyserver keyserver.ubuntu.com --recv-keys "$key"; \
	done; \
	gpg --batch --verify tomcat.tar.gz.asc tomcat.tar.gz; \
	tar -xf tomcat.tar.gz --strip-components=1; \
	rm bin/*.bat; \
	rm tomcat.tar.gz*; \
	gpgconf --kill all; \
	rm -rf "$GNUPGHOME"; \
	\
# https://tomcat.apache.org/tomcat-9.0-doc/security-howto.html#Default_web_applications
	mv webapps webapps.dist; \
	mkdir webapps; \
# we don't delete them completely because they're frankly a pain to get back for users who do want them, and they're generally tiny (~7MB)
	\
	nativeBuildDir="$(mktemp -d)"; \
	tar -xf bin/tomcat-native.tar.gz -C "$nativeBuildDir" --strip-components=1; \
	apt-get install -y --no-install-recommends \
		dpkg-dev \
		gcc \
		libapr1-dev \
		libssl-dev \
		make \
	; \
	( \
		export CATALINA_HOME="$PWD"; \
		cd "$nativeBuildDir/native"; \
		gnuArch="$(dpkg-architecture --query DEB_BUILD_GNU_TYPE)"; \
		aprConfig="$(command -v apr-1-config)"; \
		./configure \
			--build="$gnuArch" \
			--libdir="$TOMCAT_NATIVE_LIBDIR" \
			--prefix="$CATALINA_HOME" \
			--with-apr="$aprConfig" \
			--with-java-home="$JAVA_HOME" \
		; \
		nproc="$(nproc)"; \
		make -j "$nproc"; \
		make install; \
	); \
	rm -rf "$nativeBuildDir"; \
	rm bin/tomcat-native.tar.gz; \
	\
# reset apt-mark's "manual" list so that "purge --auto-remove" will remove all build dependencies
	apt-mark auto '.*' > /dev/null; \
	[ -z "$savedAptMark" ] || apt-mark manual $savedAptMark > /dev/null; \
	find "$TOMCAT_NATIVE_LIBDIR" -type f -executable -exec ldd '{}' ';' \
		| awk '/=>/ { print $(NF-1) }' \
		| xargs -rt readlink -e \
		| sort -u \
		| xargs -rt dpkg-query --search \
		| cut -d: -f1 \
		| sort -u \
		| tee "$TOMCAT_NATIVE_LIBDIR/.dependencies.txt" \
		| xargs -r apt-mark manual \
	; \
	\
	apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false; \
	rm -rf /var/lib/apt/lists/*; \
	\
# sh removes env vars it doesn't support (ones with periods)
# https://github.com/docker-library/tomcat/issues/77
	find ./bin/ -name '*.sh' -exec sed -ri 's|^#!/bin/sh$|#!/usr/bin/env bash|' '{}' +; \
	\
# fix permissions (especially for running as non-root)
# https://github.com/docker-library/tomcat/issues/35
	chmod -R +rX .; \
	chmod 1777 logs temp work; \
	\
# smoke test
	catalina.sh version

# verify Tomcat Native is working properly
RUN set -eux; \
	nativeLines="$(catalina.sh configtest 2>&1)"; \
	nativeLines="$(echo "$nativeLines" | grep 'Apache Tomcat Native')"; \
	nativeLines="$(echo "$nativeLines" | sort -u)"; \
	if ! echo "$nativeLines" | grep -E 'INFO: Loaded( APR based)? Apache Tomcat Native library' >&2; then \
		echo >&2 "$nativeLines"; \
		exit 1; \
	fi

EXPOSE 8080

# upstream eclipse-temurin-provided entrypoint script caused https://github.com/docker-library/tomcat/issues/77 to come back as https://github.com/docker-library/tomcat/issues/302; use "/entrypoint.sh" at your own risk
ENTRYPOINT []

CMD ["catalina.sh", "run"]
```

## FROM

FROM是用于指定该镜像文件所基于的镜像。

它指定一个已经存在的Docker镜像作为基础镜像构建当前镜像。FROM语句应该在Dockerfile的第一行，必须有且只有一个。

如何确定我需要什么要的基础镜像？ 

Java应用当然是java基础镜像（SpringBoot应用）或者Tomcat基础镜像（War应用） 

JS模块化应用一般用nodejs基础镜像 

其他各种语言用自己的服务器或者基础环境镜像，如python、golang、java、php等



这个语句告诉Docker使用哪个镜像作为基础镜像进行构建新的镜像。

![image-20240408165839915](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082058495.png)

## MAINTAINER

用于指定Docker镜像的维护者信息。

在Dockerfile中，使用MAINTAINER来指定该镜像的作者和联系方式，方便社区用户与作者联系。

在 Docker 1.13 版本之后，MAINTAINER指令已经过时，推荐使用LABEL来替代。

![image-20240408170053268](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082058345.png)

LABEL:标注镜像的一些说明信息。



## RUN

RUN表示在容器中执行指定的命

RUN指令在当前镜像层顶部的新层执行任何命令，并提交结果，生成新的镜像层。

分为两种格式:shell格式、exec格式

![image-20240408170314552](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082058795.png)

https://docs.docker.com/reference/dockerfile/#shell-and-exec-form

### shell格式

![image-20240408170607307](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082058538.png)

如创建一个用于Tomcat的目录

![image-20240408170746404](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082058790.png)

在shell形式中，您可以使用\（反斜杠）将一条 RUN指令继续到下一行。

```dockerfile
RUN /bin/bash -c 'source $HOME/.bashrc; \
echo $HOME'
#上面等于下面这种写法
RUN /bin/bash -c 'source $HOME/.bashrc; echo $HOME'
RUN ["/bin/bash", "-c", "echo hello"]
```



### exec格式

![image-20240408170636731](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082058935.png)

Docker在构建镜像时会自动执行所有的RUN命令，并将执行结果保存在镜像中，以便容器启动时可以使用。

RUN命令可以出现多次，每次执行的命令会被按顺序添加到镜像中。

需要注意的是，每次执行RUN命令都会产生新的一层镜像，因此应该尽量将多个命令合并为一个RUN命令以避免构建出过多的镜像层。

如在Windows基础镜像中执行 `tasklist.exe` 程序

![image-20240408171154257](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082058230.png)



## EXPOSE

当前容器对外暴露出的端口。

这并不会实际打开或映射端口，只是让使用Dockerfile的人知道哪些端口是容器应用程序会使用的，以便他们可以在运行容器时进行端口映射。

![image-20240408171559693](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082058359.png)

表示在容器中暴露8080端口号，让使用者知道在这个容器中应该监听8080端口号。

## WORKDIR

WORKDIR用于设置工作目录。

它会在Docker容器中创建一个新的目录，并指定该目录为后续命令（如RUN、CMD、ENTRYPOINT）的工作目录。在Dockerfile中，可以使用多个WORKDIR保留字指定多个工作目录。

通过使用 WORKDIR 指令，可以避免在每个 RUN、CMD 和 ENTRYPOINT 指令中都要使用 cd 命令切换到指定的目录。这不仅可以简化 Dockerfile 的编写，还可以提高容器的效率和可读性。

![image-20240408171852948](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082058614.png)

该指令会将工作目录设置为 /usr/local/tomcat，即在容器内运行以下命令时所处的目录为 /usr/local/tomcat。

## USER

USER指令用于指定要运行Docker容器的用户或用户组。如果都不指定默认是root

它可以通过两种方式来指定：

1.通过用户ID和组ID来指定USER

2.通过用户名和组名来指定USER

### 通过用户ID和组ID来指定USER

在Dockerfile中，可以使用以下命令来指定容器内的用户ID和组ID：

```dockerfile
USER <UID>:<GID>
```

例如，以下代码将容器的用户和组设置为1000：

```dockerfile
USER 1000:1000
```

### 通过用户名和组名来指定USER

也可以使用用户名和组名来指定容器内的用户身份，使用以下命令：

```dockerfile
USER <username>
```

例如，以下代码将容器的用户设置为"foo"：

```dockerfile
USER foo
```

这将更改容器中运行进程的用户身份。当用户ID或用户名在容器内不存在时，Docker将在运行时自动为它分配一个。

## ENV

用来在构建镜像过程中设置环境变量。

这些环境变量可以在容器运行时使用，格式 ENV key=value，  =可省略为空格

![image-20240408173028065](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082058010.png)

```dockerfile
ENV CATALINA_HOME /usr/local/tomcat
```

设置一个名为CATALINA_HOME的环境变量，值为/usr/local/tomcat

这个环境变量可以在后续的任意RUN指令中使用，这就如同在命令前面指定了环境变量前缀一样；

也可以在其它指令中直接使用这些环境变量，比如：

```dockerfile
WORKDIR $CATALINA_HOME
```

ENV的持久化：

在Docker中，环境变量是持久的。这意味着在容器启动时设置的环境变量将一直存在，直到容器被停止或删除。无论是在构建镜像阶段设置的环境变量，还是在容器启动时通过docker run命令设置的环境变量，都会一直生效。

## ARG

ARG指令定义了一个变量，用户可以在构建时使用--build-arg = 传递，docker build命令会将其传递 给构建器。

 --build-arg 指定参数会覆盖Dockerfile 中指定的同名参数 

如果用户指定了 未在Dockerfile中定义的构建参数 ，则构建会输出 警告 。 

ARG只在构建期有效，运行期无效

## ADD

将宿主机目录下的文件拷贝进镜像且会自动处理URL和解压tar压缩包（相当于COPY+解压）

```dockerfile
ADD src dest
```

src表示构建上下文中的源文件或目录地址，可以是本地文件系统中的路径，或是一个URL，也可以是一个包含多个文件的压缩文件（如tar或gzip）。

dest则表示容器中的目标路径，可以是绝对路径或相对路径。

需要注意的是，如果src是一个压缩文件，Docker会自动解压缩并将其中的所有文件复制到dest目录下。另外，ADD指令也支持使用通配符（如*）来复制一组文件，以及使用--chown参数来指定复制后文件的用户和组。

如

```dockerfile
ADD test.txt /absoluteDir/
```

需要注意的是，如果压缩包是以 `.zip` 格式压缩的，Docker 会自动解压缩该压缩包。但是对于其他格式的压缩包，Docker 会假设它们是以 `.tar` 格式压缩的，并将其解压缩到目标路径。

## COPY

类似ADD，拷贝文件和目录到镜像中。

将从构建上下文目录中 <源路径> 的文件/目录复制到新的一层的镜像内的 <目标路径> 位置

```dockerfile
COPY src dest
```

src源路径：源文件或者源目录

dest目标路径：容器内的指定路径，该路径不用事先建好，路径不存在的话，会自动创建。

## VOLUME

创建挂载点，挂载点可以在运行容器时被主机或其他容器挂载

该值可以是JSON数组、

```dockerfile
VOLUME ["/data"]
```

或普通字符串，如

```dockerfile
VOLUME  /data
```

## CMD

指定容器启动后要运行的命令

注意：每个Dockerfile只能有一个CMD指令，如果有多个则只有最后一个指令会生效。如果在docker run时指定了要运行的命令，则CMD指令会被覆盖。

![image-20240408190856047](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082058779.png)

如：

```shell
docker run -it -p 8080:8080 billygoo/tomcat8-jdk8
```

构建billygoo/tomcat8-jdk8的DockerFile最后一行是CMD ["catalina.sh", "run"]

![image-20240408192010919](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082058848.png)

访问http://192.168.122.140:8080/

![image-20240408192100486](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082058607.png)



那如果在docker run时指定了要运行的命令/bin/bash

```shell
docker run -it -p 8080:8080 billygoo/tomcat8-jdk8 /bin/bash
```

相当于

```shell
EXPOSE 8080
CMD ["catalina.sh", "run"]
CMD ["/bin/bash", "run"]
```

只有最后一个CMD ["/bin/bash", "run"]生效，在容器中启动一个新的 `bash` 会话，并不会启动tomcat

![image-20240408192308636](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082057582.png)

访问http://192.168.122.140:8080/

![image-20240408192325891](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082057460.png)

在Dockerfile中，RUN和CMD命令都是用来执行命令的，但是有不同的用途。

- RUN命令用来在镜像构建过程中执行命令。在构建过程中，每执行一次RUN命令，就会在当前镜像的基础上生成一个新镜像层。这些层会构成镜像的历史记录，可以用来回滚或修改镜像。
- CMD命令用来设置容器启动时默认执行的命令。当使用docker run命令启动容器时，如果没有指定要运行的命令，CMD命令设置的命令将会被执行。CMD命令可以设置一个或多个命令，也可以使用exec形式执行。

因此，RUN命令用于构建镜像时(docker build)执行命令，而CMD命令用于设置容器启动时(docker run)默认执行的命令。

## ENTRYPOINT

用于指定容器启动时执行的默认命令或程序。

类似于 CMD 指令，但是ENTRYPOINT不会被docker run后面的命令覆盖，

ENTRYPOINT指令可以接收任意数量的参数，这些参数将作为默认命令的参数传递给容器的运行时环境。可以类比为在命令行中执行程序时，命令行参数作为程序参数传递给程序。

ENTRYPOINT可以有两种形式：SHELL格式、EXEC格式。

### SHELL格式

即将命令和参数作为字符串传递给shell来运行：

```dockerfile
ENTRYPOINT command param1 param2
```

例如：

```dockerfile
ENTRYPOINT echo hello world
```

### EXEC格式

即将命令和参数当作一个可执行文件来运行：

例如：

```dockerfile
ENTRYPOINT ["echo", "hello world"]
```



ENTRYPOINT可以和CMD都可以作为启动容器的入口

### ENTRYPOINT可以和CMD一起用

一般是变参才会使用 CMD ，这里的 CMD 等于是在给 ENTRYPOINT 传参。

当指定了ENTRYPOINT后，CMD的含义就发生了变化，不再是直接运行其命令而是将CMD的内容作为参数传递给ENTRYPOINT指令

 1.例如：假设已通过 Dockerfile 构建了 nginx:test 镜像

dockerfile为

```dockerfile
FROM nginx
ENTRYPOINT ["nginx", "-c"]   #定参
CMD ["/etc/nginx/nginx.conf"]  #变参
```

相当于

```dockerfile
FROM nginx
ENTRYPOINT ["nginx", "-c", "/etc/nginx/nginx.conf"]
```

Docker命令 

```shell
docker run  nginx:test
```

按照dockerfile编写执行后相当于 

```shell
nginx -c /etc/nginx/nginx.conf
```

2.如果故意在Docker命令后面添加/etc/nginx/new.conf

```shell
docker run  nginx:test -c /etc/nginx/new.conf
```

按照dockerfile编写执行后相当于命令覆盖，/etc/nginx/new.conf覆盖了CMD的指令，变成：

```
nginx -c /etc/nginx/new.conf
```



# DockerFile指令使用案例

![image-20240408194716151](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082057227.png)

## 自定义镜像 centos-java8

自定义一个centos镜像，具备vim+ifconfig+jdk8环境的配置

### 拉取centos镜像

拉取centos镜像与使用DockerFile构建的centos-java8镜像作对比

```shell
docker pull centos
```

![image-20240408202808168](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082057970.png)

该默认镜像此时还没有vim、ifconfig和jdk的配置或功能

![image-20240408203003114](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082057167.png)

### jdk8的下载

地址：https://www.oracle.com/java/technologies/downloads/#java8

将压缩包传输到/opt/software文件下，注意当前使用的版本

![image-20240408195937578](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082057989.png)

### 编写Dockerfile文件

在/opt/software编辑Dockerfile

![image-20240408200222495](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082057776.png)

Dockerfile文件：

```dockerfile
FROM centos
MAINTAINER Dongguo
 
ENV MYPATH /usr/local
WORKDIR $MYPATH
 
#首先,进入到yum的repos目录
RUN cd /etc/yum.repos.d/
#其次,修改centos文件内容
RUN sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-*
RUN sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-*

#然后,生成缓存更新(第一次更新，速度稍微有点慢，耐心等待两分钟左右)
RUN yum makecache

#最后,运行yum update并重新安装vim
RUN yum update -y

#安装vim编辑器
RUN yum -y install vim
#安装ifconfig命令查看网络IP
RUN yum -y install net-tools
#安装java8及lib库
RUN yum -y install glibc.i686
RUN mkdir /usr/local/java
#ADD 是相对路径jar,把jdk-8u381-linux-x64.tar.gz添加到容器中,安装包必须要和Dockerfile文件在同一位置
ADD jdk-8u381-linux-x64.tar.gz /usr/local/java/
#配置java环境变量
ENV JAVA_HOME /usr/local/java/jdk1.8.0_381
ENV JRE_HOME $JAVA_HOME/jre
ENV CLASSPATH $JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar:$JRE_HOME/lib:$CLASSPATH
ENV PATH $JAVA_HOME/bin:$PATH

EXPOSE 80
```

### 构建名为centos-java8的镜像

```shell
docker build -t 新镜像名字:TAG .
```

当你运行 `docker build` 命令时，Docker 需要知道从哪里获取构建镜像所需的指令和配置。这些指令和配置通常位于一个名为 `Dockerfile` 的文件中。`.` 告诉Docker以当前目录为基准构建镜像，上下文的文件路径。

执行

```shell
docker build -t centos-java8:1.0 .
```

![image-20240408201653212](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082057640.png)

默认执行执行Dockerfile中的指令，如果创建的dockerfile文件名为Dockerfile1，可使用-f 指定文件名

```shell
docker build -t centos-java8:1.0  -f Dockerfile1 .
```



`.` 是上下文的文件目录，假如执行的目录不是在software目录下，或者dockerfile不是和上下文文件在同一目录，就需要指定jdk、tomcat所在的目录

```shell
docker build -t centos-java8:1.0  -f /opt/software/Dockerfile1 /opt/software
```



### 自定义镜像 centos-java8与centos镜像对比

![image-20240408203054902](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082057511.png)

可以看到centos镜像从200多M变成了centos-java8镜像的1.23G

### 运行镜像

```shell
docker run -it centos-java8:1.0 /bin/bash
```

vim命令正常使用、ifconfig命令正常使用、当前java版本为1.8.0_381

![image-20240408203317723](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082057037.png)

此时就可以确认centosj-ava8镜像构建成功

## 自定义镜像myubuntu

### 编写Dockerfile文件

```dockerfile
FROM ubuntu:14.04
MAINTAINER Dongguo
 
ENV MYPATH /usr/local
WORKDIR $MYPATH
 
RUN apt-get update
RUN apt-get install net-tools
#RUN apt-get install -y iproute2
#RUN apt-get install -y inetutils-ping
 
EXPOSE 80
```

在/opt/software/myubuntu编写Dockerfile文件

![image-20240408203905873](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082057103.png)

### 构建名为myubuntu的镜像

```shell
docker build -t myubuntu:1.0 .
```

![image-20240408204047000](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082057546.png)

### 运行镜像

```shell
docker run -it myubuntu:1.0 /bin/bash
```



![image-20240409073903511](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404090740446.png)

最初的ubuntu镜像是没有ifconfig命令的

![image-20240409074022817](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404090740234.png)

## 虚悬镜像

### 什么是虚悬镜像？

虚悬镜像（dangling image）是指在 Docker 中存在的一种镜像，它已经被创建，但是没有被任何容器所引用。这通常发生在当你在构建镜像的过程中，因为一些原因（例如构建取消或构建失败），你创建了一个镜像，但是没有将其命名或标记。这些镜像被称为虚悬镜像，因为它们“悬浮”在 Docker 中，没有被任何容器所使用，而且也不会被 Docker 清理工具删除。

虚悬镜像占用磁盘空间，可以通过命令docker image prune来清理不再使用的虚悬镜像。

仓库名、标签都是none的镜像，俗称虚悬镜像dangling image

![image-20240408204400041](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082057212.png)

### 创建一个虚悬镜像

Dockerfile:

```shell
from ubuntu

CMD echo 'action is success' 
```

在/opt/software/dangling 创建Dockerfile文件

```shell
[root@dongguo software]# cd dangling/
[root@dongguo dangling]# vim Dockerfile
[root@dongguo dangling]# cat Dockerfile 
from ubuntu

CMD echo 'action is success' 
```

### 构建镜像

```shell
docker build .
```

![image-20240408205240372](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082057024.png)

### 查看docker容器中存在的虚悬镜像

```shell
docker images
```

此时可以使用 docker images 命令查看所有镜像，可以看到一个none 标签的镜像出现在列表中，这就是刚刚创建的虚悬镜像。

![image-20240408205351091](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082057951.png)

也可以dangling=true指定查询存在的虚悬镜像

```shell
docker image ls -f dangling=true
```

![image-20240408205419814](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082057156.png)

### 删除docker容器中的虚悬镜像

可以使用docker image prune命令清除所有虚悬镜像。

```shell
docker image prune
```

或者 直接删除该镜像也可以

```shell
docker rmi -f 镜像id
```

再次查看镜像，虚悬镜像已经被删除

![image-20240408205526377](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404082056585.png)

