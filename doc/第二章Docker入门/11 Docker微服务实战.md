通过一个简单的示例Dockerfile，用于发布Spring Boot微服务应用程序



前提

1.关闭防火墙：

```shell
systemctl stop firewalld
```

2.启动docker

```shell
systemctl start docker
```



# 新建一个微服务模块

## 创建Module:docker-boot

![image-20240409081048926](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404090900730.png)

## pom依赖

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
    <relativePath/> <!-- lookup parent from repository -->
  </parent>

  <groupId>com.dongguo</groupId>
  <artifactId>docker-boot</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>jar</packaging>

  <name>docker-boot</name>
  <url>http://maven.apache.org</url>

  <properties>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <!--SpringBoot通用依赖模块-->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <!--test-->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-resources-plugin</artifactId>
        <version>3.1.0</version>
      </plugin>
    </plugins>
  </build>
</project>
```

## YML配置

```yaml
server:
  port: 8081
```

## 主启动

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DockerBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(DockerBootApplication.class, args);
    }
}
```

## 业务类

```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class OrderController {
    @Value("${server.port}")
    private String port;

    @GetMapping("/order/docker")
    public String helloDocker() {
        return "hello docker" + "\t" + port + "\t" + UUID.randomUUID();
    }
}
```

启动项目，测试一下是否能成功访问接口

![image-20240408214258104](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404090900936.png)

# 通过Dockerfile发布微服务部署到docker容器

## 打jar包

打开maven双击package进行打包

![image-20240409081112042](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404090900553.png)





target目录下生成jar包

![image-20240409081134822](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404090900979.png)

上传到宿主机/opt/software/mydocker目录下

![image-20240409081324663](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404090859792.png)

## 编写Dockerfile文件

项目jar包和Dockerfile文件在同一个目录下/opt/software/mydocker

```dockerfile
# 基础镜像使用java
FROM java:8
# 作者
MAINTAINER Dongguo
# VOLUME 指定临时文件目录为/tmp，在主机/var/lib/docker目录下创建了一个临时文件并链接到容器的/tmp
VOLUME /tmp
# 将jar包添加到容器中并更名为dg-docker.jar
ADD docker-boot-1.0-SNAPSHOT.jar dg-docker.jar
# 运行jar包
RUN bash -c 'touch /dg-docker.jar'
ENTRYPOINT ["java","-jar","/dg-docker.jar"]
#暴露8081端口作为微服务
EXPOSE 8081
```

这个Dockerfile使用java:8基础映像，使用ENTRYPOINT指令来运行dg-docker.jar。

![image-20240409085813309](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404090859669.png)

## 构建镜像

```shell
docker build -t dg-docker:1.0 .
```

`dg-docker` 是为镜像起的名字，而 `1.0` 是这个镜像的版本标签。

![image-20240409085257519](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404090859840.png)

## 运行容器

将容器的8081端口映射到主机上的8081端口

```shell
docker run -d -p 8081:8081 dg-docker:1.0
```

![image-20240409085314716](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404090859691.png)

## 访问测试

http://192.168.122.140:8081/order/docker

![image-20240409085926517](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404090859093.png)

# 多阶段构建

https://docs.docker.com/build/building/multi-stage/

一个镜像分为多个大的阶段进行构建，最终的构建结果是最后一个阶段的结果

允许一个 Dockerfile 中定义多个构建阶段，以便优化镜像构建的过程，减少镜像的体积。

1. **减小镜像体积**：通过将构建过程分成多个阶段，可以在一个阶段中生成所需的文件，然后在下一个阶段中拷贝这些文件，从而避免在最终镜像中包含构建工具和依赖，减小镜像的体积。
2. **优化构建速度**：通过只构建所需的部分，可以减少构建所需的时间，提高构建效率。

如官网给出的例子

```dockerfile
# syntax=docker/dockerfile:1
FROM golang:1.21
WORKDIR /src
COPY <<EOF ./main.go
package main

import "fmt"

func main() {
  fmt.Println("hello, world")
}
EOF
RUN go build -o /bin/hello ./main.go

FROM scratch
COPY --from=0 /bin/hello /bin/hello
CMD ["/bin/hello"]
```

在这个示例中，有两个阶段：

1. 第一个阶段使用了 `golang:1.21` 镜像作为基础镜像，编译 main.go 文件为可执行文件 hello，并放置在 /bin 目录下。。
2. 第二个阶段指定了一个空的基础镜像，从第一个阶段的构建结果中复制编译好的 hello 可执行文件到当前阶段。并设置容器启动时执行的默认命令为运行 hello 可执行文件。



之前的项目还需要打包上传后，再构建运行，我们可以通过多阶段构建，dockerfile实现下载项目、打包构建操作。

项目地址：https://github.com/dongguo4812/CloudNative/tree/master/docker-boot

https://gitee.com/dongguo4812_admin/CloudNative

## Dockerfile

/opt/software/mydocker2目录下

```dockerfile
# 第一阶段下载项目
FROM alpine/git AS gitclone
#guthub一直下载不下载，这里改成码云
ARG url=https://gitee.com/dongguo4812_admin/CloudNative.git
ARG appName=docker-boot
RUN git clone $url /git/CloudNative && cd /git/CloudNative/docker-boot
# 第二阶段 构建源码
RUN pwd && ls -l 
FROM maven:3.6.1-jdk-8-alpine AS buildapp
COPY --from=gitclone /git/CloudNative/docker-boot/* /app/
WORKDIR /app/
RUN pwd && ls -l

RUN mvn clean package
RUN pwd && ls -l
RUN cp /app/target/*.jar /dg-docker.jar



# 第三阶段 基础镜像使用java
FROM java:8
# 作者
LABEL author=Dongguo
# 把上一个阶段 dg-docker.jar 复制过来
COPY --from=buildapp /dg-docker.jar /dg-docker.jar
# 运行jar包
RUN bash -c 'touch /dg-docker.jar'
ENTRYPOINT ["java","-jar","/dg-docker.jar"]
# 暴露8081端口作为微服务
EXPOSE 8081
```

![image-20240413150915768](F:\note\image\image-20240413150915768.png)

## 构建镜像

```shell
 docker build --progress=plain --no-cache -t dg-docker:2.0 .
```

可以一个阶段一个阶段测试，慢慢的添加代码虽然慢一点，但是能确定错误的地方

## 运行容器

```
docker run -d -p 8081:8081 dg-docker:2.0
```



## 访问测试

http://192.168.122.141:8081/order/docker











