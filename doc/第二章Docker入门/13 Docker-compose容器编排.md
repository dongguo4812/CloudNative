# Docker-Compose介绍

Compose 是 Docker 公司推出的一个工具软件，可以管理多个 Docker 容器组成一个应用。你需要定义一个 YAML 格式的配置文件docker-compose.yml，写好多个容器之间的调用关系。然后，只要一个命令，就能同时启动/关闭这些容器

Docker-Compose是Docker官方的开源项目，负责实现对Docker容器集群的快速编排。

## 作用

docker建议我们每一个容器中只运行一个服务,因为docker容器本身占用资源极少,所以最好是将每个服务单独的分割开来但是这样我们又面临了一个问题:

如果我需要同时部署好多个服务,难道要每个服务单独写Dockerfile然后再构建镜像,构建容器,这样累都累死了,所以docker官方给我们提供了docker-compose多服务部署的工具



例如要实现一个Web微服务项目，除了Web服务容器本身，往往还需要再加上后端的数据库mysql服务容器，redis服务器，注册中心eureka，甚至还包括负载均衡容器等等。

Compose允许用户通过一个单独的docker-compose.yml模板文件（YAML 格式）来定义一组相关联的应用容器为一个项目（project）。

可以很容易地用一个配置文件定义一个多容器的应用，然后使用一条指令安装这个应用的所有依赖，完成构建。Docker-Compose 解决了容器与容器之间如何管理编排的问题。

# Docker-Compose下载

compose-file是Docker Compose使用的文件格式，用于定义所有服务的配置和资源。Compose-file允许您指定应用程序的服务、网络、卷和环境变量等，以及它们之间的依赖关系和交互。Compose-file中定义的服务可以通过Docker Compose工具进行管理和启动。

compose-file是Docker Compose的一部分，是用于定义多容器应用程序的配置文件格式。Docker Compose则是使用该配置文件进行启动和管理多容器应用程序的工具。

## compose-file版本

官网：https://docs.docker.com/compose/compose-file/compose-file-v3/

官网提供了V2、V3版本

这两个版本之间有以下区别：

1. 网络： v2版本使用link指令连接容器，而v3版本使用网络服务连接容器。
2. 部署： v2版本允许部署容器在特定的节点上，而v3版本添加了支持Stacks，允许用户在Docker Swarm模式下创建分布式应用程序，这样可以更好地管理多个容器并部署到多个节点上。
3. 配置： v3版本增加了更多的配置选项，例如配置CPU亲和性、日志记录等，在更高级的方案中提供更多灵活性。
4. 缩放： v3版本可以按名称缩放服务，而不是按容器ID。

总的来说，v3版本相对于v2版本更加强大和灵活，特别是在容器部署和管理方面有更多的选择，同时也提供了更多的功能来管理多个容器。

Compose file version 3 对应 Docker Compose 版本为 1.13.0 或更高版本。

下载地址：https://docs.docker.com/compose/install/

## 安装

当前Docker版本是26.0.0

![image-20240409155916996](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101129403.png)

使用Docker Compose版本为2.26.0

![image-20240409155850310](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101129429.png)



安装步骤：

### 1.下载

https://github.com/docker/compose/releases/download/v2.26.0/docker-compose-linux-x86_64

```shell
curl -L "https://github.com/docker/compose/releases/download/v2.26.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
```

可以看到花费了13分钟，速度是非常慢的

![image-20240409170950464](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101129370.png)

#### 问题：

如果提示Failed connect to github.com:443; 拒绝连接

![image-20240409160439604](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101129294.png)

这个错误通常说明你的系统无法连接到 Github.com 上。可能是因为你的网络连接有问题，或者该域名被防火墙阻止。

#### 原因：DNS解析问题

在DNS解析前先会尝试走hosts然后在找不到的的情况下再DNS解析,修改hosts文件域名解析就会先走hosts中的ip和域名的映射关系。我们可以修改hosts文件，修改ip地址和域名的映射关系，

#### 解决方法一：

1）查找github.com对应的IP

通过网址ipaddress.com搜索框输入github.com，查找github.com对应的IP地址

![image-20240409161136508](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101129807.png)

2）找到GitHub.com DNS Resource Records，复制ip

![image-20240409161250873](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101129647.png)

3）修改hosts文件

将查询到的GitHub IP地址内容`140.82.114.3 github.com` 追加进hosts文件：

```shell
vi /etc/hosts
```

![image-20240409161540116](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101129112.png)

重新执行curl命令,如果还是有问题重复上面的步骤

#### 解决方法二（推荐）：

1）.下载https://github.com/docker/compose/releases/download/v2.26.0/docker-compose-linux-x86_64

2）.将文件重命名为docker-compose后上传到/usr/local/bin/

![image-20240409171137285](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101129664.png)

### 2.赋予用户文件的读写权限

```shell
chmod +x /usr/local/bin/docker-compose
```

### 3.查看版本

```shell
docker-compose --version
```

![image-20240409171216112](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101129822.png) 

## 卸载

```shell
rm /usr/local/bin/docker-compose
```

![image-20240409171342821](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101128302.png)

# Compose核心概念

一文件、两要素

## docker-compose.yml文件

docker-compose.yml是Compose的配置文件，用于定义服务、网络和数据卷。其格式为YAML，默认路径为./docker-compose.yml，可以使用.yml或.yaml扩展名。这个配置文件是Compose工具的核心，它允许用户定义多个容器间的调用关系，以及每个容器所需的配置。

## 服务（service）

服务（service）是指一个个应用容器实例，比如订单微服务、会员微服务、mysql容器等。在docker-compose.yml配置文件中，可以定义多个服务，每个服务可以由一个或多个容器组成。服务定义了运行容器所需的配置，例如镜像、端口、环境变量、卷等。

## 工程（project）

工程（project）是由一组关联的应用容器组成的一个完整业务单元。简单来说，一个工程就是一个完整的app应用，它包含了所有必要的服务，以便能够一起运行和协作。在docker-compose.yml配置文件中，用户可以定义整个工程的结构和组成。

# Compose常用命令

docker-compose -h                           # 查看帮助

docker-compose up                           # 启动所有docker-compose服务

docker-compose up -d                        # 启动所有docker-compose服务并后台运行

docker-compose down                         # 停止并删除容器、网络、卷、镜像。

docker-compose exec  yml里面的服务id                 # 进入容器实例内部  docker-compose exec docker-compose.yml文件中写的服务id /bin/bash

docker-compose ps                      # 展示当前docker-compose编排过的运行的所有容器

docker-compose top                     # 展示当前docker-compose编排过的容器进程

docker-compose logs  yml里面的服务id     # 查看容器输出日志

docker-compose config     # 检查配置

docker-compose config -q  # 检查配置，有问题才有输出

docker-compose restart   # 重启服务

docker-compose start     # 启动服务

docker-compose stop      # 停止服务

# Compose使用的三个步骤

1.编写Dockerfile定义各个微服务应用并构建出对应的镜像文件

2.使用 docker-compose.yml 定义一个完整业务单元，安排好整体应用中的各个容器服务。

3.最后，执行docker-compose up命令 来启动并运行整个应用程序，完成一键部署上线

# Compose编排微服务案例

对之前通过dockerfile发布微服务部署到docker容器章节创建的docker-boot项目进行改造，来熟悉Compose编排与命令

## 改造升级微服务工程docker-boot

### sql建表

```sql
CREATE TABLE `t_user` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL DEFAULT '' COMMENT '用户名',
  `password` varchar(50) NOT NULL DEFAULT '' COMMENT '密码',
  `sex` tinyint(4) NOT NULL DEFAULT '0' COMMENT '性别 0=女 1=男 ',
  `deleted` tinyint(4) unsigned NOT NULL DEFAULT '0' COMMENT '删除标志，默认0不删除，1删除',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='用户表'
```

### 一键生成mybatis-generator

https://github.com/dongguo4812/mybatis-generator.git

替换配置，执行generate

![image-20240409173650869](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101128670.png)

将生成的entity、mapper复制到docker-boot项目中

![image-20240409173350715](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101128915.png)

### 改POM

```xml
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
    <!--SpringBoot与Redis整合依赖-->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <!--Mysql数据库驱动-->
    <dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
      <version>8.0.33</version>
    </dependency>
    <!--SpringBoot集成druid连接池-->
    <dependency>
      <groupId>com.alibaba</groupId>
      <artifactId>druid-spring-boot-starter</artifactId>
      <version>1.1.10</version>
    </dependency>
    <!--mybatis和springboot整合-->
    <dependency>
      <groupId>org.mybatis.spring.boot</groupId>
      <artifactId>mybatis-spring-boot-starter</artifactId>
      <version>2.1.3</version>
    </dependency>
    <!--hutool-->
    <dependency>
      <groupId>cn.hutool</groupId>
      <artifactId>hutool-all</artifactId>
      <version>5.2.3</version>
    </dependency>
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <version>${lombok.version}</version>
      <optional>true</optional>
    </dependency>
    <!--通用Mapper-->
    <dependency>
      <groupId>tk.mybatis</groupId>
      <artifactId>mapper</artifactId>
      <version>4.1.5</version>
    </dependency>
    <!--swagger2-->
    <dependency>
      <groupId>io.springfox</groupId>
      <artifactId>springfox-swagger2</artifactId>
      <version>2.9.2</version>
    </dependency>
    <dependency>
      <groupId>io.springfox</groupId>
      <artifactId>springfox-swagger-ui</artifactId>
      <version>2.9.2</version>
    </dependency>
  </dependencies>
```



### 写YML

redis、mysql先连接本地，测试成功后，再修改为远程宿主机地址

```yaml
server:
  port: 8081
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/docker?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: root
    druid:
      test-while-idle: false
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher
  redis:
    database: 0
    host: localhost
    port: 6379
    password: root
    jedis:
      pool:
        max-active: 8
        max-wait: 1ms
        max-idle: 8
        min-idle: 0
  swagger2:
    enabled: true
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.dongguo.docker.entity
```



### 主启动

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.dongguo.docker.mapper")
public class DockerBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(DockerBootApplication.class, args);
    }
}
```

### 业务类

#### config配置类

RedisConfig

```java
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;


@Configuration
@Slf4j
public class RedisConfig {
    /**
     * @param lettuceConnectionFactory
     * @return redis序列化的工具配置类，下面这个请一定开启配置
     * 127.0.0.1:6379> keys *
     * 1) "ord:102"  序列化过
     * 2) "\xac\xed\x00\x05t\x00\aord:102"   野生，没有序列化过
     */
    @Bean
    public RedisTemplate<String, Serializable> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        //设置key序列化方式string
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //设置value的序列化方式json
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

}
```

SwaggerConfig

```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.*;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;


@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Value("${spring.swagger2.enabled}")
    private Boolean enabled;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .enable(enabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.dongguo.docker")) //你自己的package
                .paths(PathSelectors.any())
                .build();
    }

    public ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Docker入门 " + "\t" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                .description("docker-compose")
                .version("1.0")
                .termsOfServiceUrl("https://www.dongguo.com/")
                .build();
    }

    /**
     * 增加如下配置可解决Spring Boot 与Swagger 不兼容问题
     * @param webEndpointsSupplier
     * @param servletEndpointsSupplier
     * @param controllerEndpointsSupplier
     * @param endpointMediaTypes
     * @param corsProperties
     * @param webEndpointProperties
     * @param environment
     * @return
     */
    @Bean
    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(WebEndpointsSupplier webEndpointsSupplier, ServletEndpointsSupplier servletEndpointsSupplier, ControllerEndpointsSupplier controllerEndpointsSupplier, EndpointMediaTypes endpointMediaTypes, CorsEndpointProperties corsProperties, WebEndpointProperties webEndpointProperties, Environment environment) {
        List<ExposableEndpoint<?>> allEndpoints = new ArrayList();
        Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
        allEndpoints.addAll(webEndpoints);
        allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
        allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints());
        String basePath = webEndpointProperties.getBasePath();
        EndpointMapping endpointMapping = new EndpointMapping(basePath);
        boolean shouldRegisterLinksMapping = this.shouldRegisterLinksMapping(webEndpointProperties, environment, basePath);
        return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, endpointMediaTypes, corsProperties.toCorsConfiguration(), new EndpointLinksResolver(allEndpoints, basePath), shouldRegisterLinksMapping, null);
    }
    private boolean shouldRegisterLinksMapping(WebEndpointProperties webEndpointProperties, Environment environment, String basePath) {
        return webEndpointProperties.getDiscovery().isEnabled() && (StringUtils.hasText(basePath) || ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT));
    }
}
```

#### entity

User

```java
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 表名：t_user
*/
@Table(name = "t_user")
public class User {
    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 性别 0=女 1=男 
     */
    private Byte sex;

    /**
     * 删除标志，默认0不删除，1删除
     */
    private Byte deleted;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取用户名
     *
     * @return username - 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取密码
     *
     * @return password - 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取性别 0=女 1=男 
     *
     * @return sex - 性别 0=女 1=男 
     */
    public Byte getSex() {
        return sex;
    }

    /**
     * 设置性别 0=女 1=男 
     *
     * @param sex 性别 0=女 1=男 
     */
    public void setSex(Byte sex) {
        this.sex = sex;
    }

    /**
     * 获取删除标志，默认0不删除，1删除
     *
     * @return deleted - 删除标志，默认0不删除，1删除
     */
    public Byte getDeleted() {
        return deleted;
    }

    /**
     * 设置删除标志，默认0不删除，1删除
     *
     * @param deleted 删除标志，默认0不删除，1删除
     */
    public void setDeleted(Byte deleted) {
        this.deleted = deleted;
    }

    /**
     * 获取更新时间
     *
     * @return updateTime - 更新时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置更新时间
     *
     * @param updateTime 更新时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 获取创建时间
     *
     * @return createTime - 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间
     *
     * @param createTime 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
```

UserDTO

```java
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ApiModel(value = "用户信息")
public class UserDTO implements Serializable {
    @ApiModelProperty(value = "用户ID")
    private Integer id;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "性别 0=女 1=男 ")
    private Byte sex;

    @ApiModelProperty(value = "删除标志，默认0不删除，1删除")
    private Byte deleted;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
```

#### mapper

```java
import com.dongguo.docker.entity.User;
import tk.mybatis.mapper.common.Mapper;

public interface UserMapper extends Mapper<User> {
}
```

src\main\resources路径下新建mapper文件夹并新增UserMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.dongguo.docker.mapper.UserMapper">
  <resultMap id="BaseResultMap" type="com.dongguo.docker.entity.User">
    <!--
      WARNING - @mbg.generated
    -->
    <id column="id" jdbcType="INTEGER" property="id" />
    <result column="username" jdbcType="VARCHAR" property="username" />
    <result column="password" jdbcType="VARCHAR" property="password" />
    <result column="sex" jdbcType="TINYINT" property="sex" />
    <result column="deleted" jdbcType="TINYINT" property="deleted" />
    <result column="update_time" jdbcType="TIMESTAMP" property="updateTime" />
    <result column="create_time" jdbcType="TIMESTAMP" property="createTime" />
  </resultMap>
</mapper>
```

#### service

```java
import com.dongguo.docker.entity.User;
import com.dongguo.docker.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @auther zzyy
 * @create 2021-05-01 14:58
 */
@Service
@Slf4j
public class UserService {

    public static final String CACHE_KEY_USER = "user:";

    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * addUser
     *
     * @param user
     */
    public void addUser(User user) {
        //1 先插入mysql并成功
        int i = userMapper.insertSelective(user);

        if (i > 0) {
            //2 需要再次查询一下mysql将数据捞回来并ok
            user = userMapper.selectByPrimaryKey(user.getId());
            //3 将捞出来的user存进redis，完成新增功能的数据一致性。
            String key = CACHE_KEY_USER + user.getId();
            redisTemplate.opsForValue().set(key, user);
        }
    }

    /**
     * findUserById
     *
     * @param id
     * @return
     */
    public User findUserById(Integer id) {
        User user;
        String key = CACHE_KEY_USER + id;

        //1 先从redis里面查询，如果有直接返回结果，如果没有再去查询mysql
        user = (User) redisTemplate.opsForValue().get(key);

        if (user == null) {
            //2 redis里面无，继续查询mysql
            user = userMapper.selectByPrimaryKey(id);
            if (user == null) {
                //3.1 redis+mysql 都无数据
                //你具体细化，防止多次穿透，我们规定，记录下导致穿透的这个key回写redis
                return user;
            } else {
                //3.2 mysql有，需要将数据写回redis，保证下一次的缓存命中率
                redisTemplate.opsForValue().set(key, user);
            }
        }
        return user;
    }

    public void updateUser(User user) {
        String key = CACHE_KEY_USER + user.getId();
        redisTemplate.delete(key);
        userMapper.updateByPrimaryKey(user);
    }

    public void deleteUser(Integer id) {
        String key = CACHE_KEY_USER + id;
        redisTemplate.delete(key);
        userMapper.deleteByPrimaryKey(id);
    }
}
```

#### controller

```java
import cn.hutool.core.util.IdUtil;
import com.dongguo.docker.entity.User;
import com.dongguo.docker.entity.UserDTO;
import com.dongguo.docker.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Random;

@Api(description = "用户User接口")
@RestController
public class UserController {

    @Resource
    private UserService userService;

    @ApiOperation("数据库新增记录")
    @PostMapping(value = "/user/add")
    public void addUser() {
        for (int i = 1; i <= 3; i++) {
            User user = new User();

            user.setUsername("dongguo" + i);
            user.setPassword(IdUtil.simpleUUID().substring(0, 6));
            user.setSex((byte) new Random().nextInt(2));

            userService.addUser(user);
        }
    }

    @ApiOperation("删除记录")
    @PostMapping(value = "/user/delete/{id}")
    public void deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
    }

    @ApiOperation("修改记录")
    @PostMapping(value = "/user/update")
    public void updateUser(@RequestBody UserDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        userService.updateUser(user);
    }

    @ApiOperation("查询1条记录")
    @GetMapping(value = "/user/find/{id}")
    public User findUserById(@PathVariable Integer id) {
        return userService.findUserById(id);
    }
}
```

### 本地swagger测试

启动项目，访问swagger地址http://localhost:8081/swagger-ui.html

新增

![image-20240410072059204](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101128062.png)

查询

![image-20240410072112113](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101128111.png)

测试通过后修改yml配置

```yaml
server:
  port: 8081
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://192.168.122.140:3306/docker?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: root
    druid:
      test-while-idle: false
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher
  redis:
    database: 0
    host: 192.168.122.140
    port: 6379
    password: root
    jedis:
      pool:
        max-active: 8
        max-wait: 1ms
        max-idle: 8
        min-idle: 0
  swagger2:
    enabled: true
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.dongguo.docker.entity
```

完整项目传送门：https://github.com/dongguo4812/CloudNative/tree/master/docker-boot



### mvn package命令，微服务生成新的jar包

![image-20240410091449630](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101128947.png)

还是在targer目录下

![image-20240410091537338](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101128291.png)

### 上传到Linux服务器/opt/software/mycompose目录下



![image-20240410091940637](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101128804.png)

### 编写Dockerfile

```dockerfile
# 基础镜像使用java
FROM java:8
# 作者
MAINTAINER dongguo
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

![image-20240410092954987](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101127644.png)

### 构建镜像

```shell
docker build -t dg-docker:1.0 .
```

![image-20240410093019354](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101127915.png)

重启Docker，将之前启动的容器停掉

![image-20240410093200170](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101127188.png)

# 不使用Compose编排

不使用Compose编排的话启动tomcat mysql redis。需要分别启动这三个容器实例

## 单独的mysql容器实例

1.新建mysql容器实例

```shell
docker run -p 3306:3306 --name mysql8 --privileged=true \
-v /dongguo/mysql/conf:/etc/mysql/conf.d \
-v /dongguo/mysql/logs:/logs \
-v /dongguo/mysql/data:/var/lib/mysql \
-v /dongguo/mysql/mysql-files:/var/lib/mysql-files  \
-e MYSQL_ROOT_PASSWORD=root \
-d mysql:8.0.20
```

![image-20240410104202711](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101127453.png)

2.进入mysql容器实例并新建库docker新建表t_user

```shell
docker exec -it mysql8 /bin/bash

mysql -uroot -p
create database docker;
use docker;
```

![image-20240410104245887](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101127694.png)

新建表

```sql
CREATE TABLE `t_user` (
  `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '用户名',
  `password` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '密码',
  `sex` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '性别 0=女 1=男 ',
  `deleted` TINYINT(4) UNSIGNED NOT NULL DEFAULT '0' COMMENT '删除标志，默认0不删除，1删除',
  `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

![image-20240410104423660](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101127500.png)

## 单独的redis容器实例

新建redis容器实例

```shell
docker run  -p 6379:6379 --name redis6 --privileged=true \
-v /app/redis/redis.conf:/etc/redis/redis.conf \
-v /app/redis/data:/data \
-d redis:6.0.8 redis-server \
/etc/redis/redis.conf
```

![image-20240410104502002](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101127126.png)

## 启动微服务工程容器

```shell
docker run -d -p 8081:8081 dg-docker:1.0
```



上面三个容器实例依次顺序启动成功

![image-20240410104535694](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101127509.png)



## swagger测试

[http://你的ip:](http://localhost:)你的端口/swagger-ui.html

![image-20240410104559853](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101127778.png)



## 问题

能够访问swagger说明项目搭建的没有问题，但是这样启动有很多的问题：

先后顺序要求固定，先mysql+redis启动后才能微服务访问成功；

容器间的启停或宕机，有可能导致IP地址对应的容器实例变化，映射出错，要么生产IP写死(可以但是不推荐，很少这样)，要么通过服务名调用

如果需要启动多个容器，可能需要手动运行多个docker run指令，每个容器都需要单独设置不同的配置和参数，这会增加管理难度。

如果需要更新容器的配置或参数，需要手动修改每个容器的启动指令，这会增加更新难度和错误的风险。

# 使用Compose编排

使用服务编排一次性启动和管理多个容器实例

## 删除构建的容器

![image-20240410105054269](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101127347.png)

## 删除之前构建的dg-docker 镜像

![image-20240410105212669](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101127848.png)

## 编写docker-compose.yml文件

/opt/software/mycompose目录下

```shell
version: "3"  #compose V3
 
services:
  #项目服务名 dockerService
  dockerService:
    image: dg-docker:1.0
    container_name: boot
    ports:
      - "8081:8081"
    volumes:
      - /app/microService:/data
    networks: 
      - dg_net 
    depends_on: 
      - redis
      - mysql
  #服务名 reids
  redis:
    image: redis:6.0.8
    ports:
      - "6379:6379"
    volumes:
      - /app/redis/redis.conf:/etc/redis/redis.conf
      - /app/redis/data:/data
    networks: 
      - dg_net
    command: redis-server /etc/redis/redis.conf
  #服务名 mysql
  mysql:
    image: mysql:8.0.20
    environment:
      MYSQL_ROOT_PASSWORD: 'root'
      MYSQL_ALLOW_EMPTY_PASSWORD: 'no'
      MYSQL_DATABASE: 'docker'
      MYSQL_USER: 'root'
      MYSQL_PASSWORD: 'root'
    ports:
       - "3306:3306"
    volumes:
      - /dongguo/mysql/conf:/etc/mysql/conf.d  
      - /dongguo/mysql/logs:/logs  
      - /dongguo/mysql/data:/var/lib/mysql  
      - /dongguo/mysql/mysql-files:/var/lib/mysql-files 
    networks:
      - dg_net
    command: --default-authentication-plugin=mysql_native_password #解决外部无法访问
#网络 
networks: 
   dg_net: 
```



## 再次修改微服务工程docker-boot

yml配置将ip访问修改为服务名访问

```yaml
server:
  port: 8081
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
#    url: jdbc:mysql://192.168.122.140:3306/docker?useUnicode=true&characterEncoding=utf-8&useSSL=false
    url: jdbc:mysql://mysql:3306/docker?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: root
    druid:
      test-while-idle: false
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher
  redis:
    database: 0
#    host: 192.168.122.140
    host: redis
    port: 6379
    password: root
    jedis:
      pool:
        max-active: 8
        max-wait: 1ms
        max-idle: 8
        min-idle: 0
  swagger2:
    enabled: true
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.dongguo.docker.entity
```

再次mvn package命令将微服务形成新的jar包，并上传到Linux服务器/opt/software/mycompose目录下

## Dockerfile

这里和之前的内容是一样的，无需修改



/opt/software/mycompose目录

![image-20240410110837852](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101126152.png)

## 构建镜像

```shell
docker build -t dg-docker:1.0 .
```

![image-20240410110914508](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101126509.png)

1.先确认docker-compose文件的配置是否正确

这里有个警告表示在 `docker-compose.yml` 文件中使用的 `version` 字段已经过时了，这是因为我使用的compose较新，这只是一个警告，可以忽略。没有返回其他错误说明docker-compose.yml文件配置没有语法问题。

![image-20240410111116060](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101126231.png)

2.执行 docker-compose up或者执行 docker-compose up -d一次性完成构建并启动 `docker-compose.yml` 文件中定义的所有服务

从控制台能够看出如果不定义容器名container_name，会生成mycompose开头的名称

![image-20240410111318197](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101126396.png)

3.查看创建的网络mycompose_dg_net

![image-20240410111530447](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101126283.png)

4.查看已经的启动容器

![image-20240410111610489](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101126052.png)

## 进入mysql容器实例

因为配置了容器数据卷的原因，之前创建的docker数据库和t_user表依然存在

![image-20240410111833261](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101126221.png)

## swagger测试

访问正常

![image-20240410111907376](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101126426.png)

新增数据

![image-20240410111943308](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101126957.png)

查看

![image-20240410112008226](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101126243.png)

# 使用Compose编排后，一键启动一键停止

**docker-compose up之后，可以使用docker-compose stop和docker-compose start来停止和启动所有容器服务**。

停止容器

```shell
docker-compose stop
```

![image-20240410112229142](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101126606.png)



启动容器

```shell
docker-compose start
```

![image-20240410112254356](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101125069.png)



通过compose编排，可以通过一个YAML文件一次性启动和管理多个容器实例，而不需要手动逐个启动和管理。这样可以简化管理操作，并确保容器实例的一致性。

启动、关闭多个容器实例变得如此简单！