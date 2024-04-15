在CODE阶段，我们需要将不同版本的代码存储到一个仓库中，常见的版本控制工具就是SVN或者Git，这里我们采用Git作为版本控制工具，GitLab作为远程仓库。



# GitLab安装

单独准备服务器，采用Docker安装

## 1下载GitLab镜像

```shell
docker pull gitlab/gitlab-ce
```

镜像比较大

![image-20240415102350207](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152219306.png)

## 2准备docker-compose.yml文件

创建目录/dongguo/docker/gitlab_docker，并在该目录下创建docker-compose.yml文件。

```yaml
version: '3.1'
services:
  gitlab:
    image: 'gitlab/gitlab-ce:latest'
    container_name: gitlab
    restart: always
    environment:
      GITLAB_OMNIBUS_CONFIG: |
        external_url 'http://192.168.122.141:8929'
        gitlab_rails['gitlab_shell_ssh_port'] = 2224
    ports:
      - '8929:8929'
      - '2224:2224'
    volumes:
      - './config:/etc/gitlab'
      - './logs:/var/log/gitlab'
      - './data:/var/opt/gitlab'
```

在/dongguo/docker/gitlab_docker目录下执行

```shell
docker-compose up -d
```

![image-20240415104130659](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152219474.png)

## 3.访问gitlab

启动gitlab需要一段时间，可以查看log

```shell
docker logs gitlab
```

![image-20240415105721499](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152219139.png)

日志滚动停止，访问http://192.168.122.141:8929/

![image-20240415105643253](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152219918.png)

gitlab提供默认账户为：root，密码是在容器内提供，位置在/etc/gitlab/initial_root_password

![image-20240415110132212](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152220702.png)

## 4登录

![image-20240415110247859](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152220675.png)

## 5修改密码

1）右上角点击Preferences

![image-20240415110329085](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152220583.png)

2）左侧点击password

![image-20240415110425729](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152220137.png)

3）复制旧密码，填写新密码

![image-20240415110554733](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152220994.png)

## 6重新登陆

重置密码自动跳转到登录页面，登录

![image-20240415110658344](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152220738.png)

# maven安装

将tar.gz文件上传到linux

![image-20240415112342432](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152220855.png)

## 解压

解压到/usr/local目录下

```
tar -zxvf apache-maven-3.8.8-bin.tar.gz -C /usr/local
```

## 重命名文件名

![image-20240415112609501](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152220835.png)

## 修改settings.xml配置

![image-20240415113647444](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152220051.png)

1配置阿里云仓库地址

```xml
    <mirror>
      <id>nexus-aliyun</id>
      <mirrorOf>*</mirrorOf>
      <name>Nexus aliyun</name>
      <url>http://maven.aliyun.com/nexus/content/groups/public</url>
    </mirror>
```





![image-20240415113134881](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152220416.png)

2配置jdk1.8编译插件

```xml
    <profile>
     <id>jdk8</id>
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
   
   
   
   <activeProfiles>
     <activeProfile>jdk8</activeProfile>
   </activeProfiles>
```



![image-20240415113628412](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152220595.png)

指定激活的profile：jdk8

# jdk安装

## 解压

```shell
tar -zxvf jdk-8u381-linux-x64.tar.gz -C /usr/local
```

## 重命名

![image-20240415114220565](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152220022.png)

