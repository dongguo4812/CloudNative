代码在本地修改----提交到远程gitee----触发jenkins整个自动化构建流程（打包，测试，发布，部署）

# 准备一个git项目进行测试

我们以gitee为例，github可能太慢了。需要idea安装gitee插件

步骤： 

1、idea创建Spring Boot项目 

2、VCS - 创建git 仓库 

3、gitee创建一个空仓库，示例为public 

4、idea提交内容到gitee 

5、开发项目基本功能，并在项目中创建一个Jenkinsfile文件

6、创建一个名为 devops-demo的流水线项目，使用项目自己的流水线





## 创建Springboot项目

devops-demo

https://gitee.com/dongguo4812_admin/devops-demo

![image-20240415223318285](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162125391.png)

```xml
    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
```



## 下载gitee插件

![image-20240415223402306](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126638.png)

## 创建gitee远程仓库

![image-20240415223502136](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126882.png)

添加账户，通过账号密码或令牌登录

![image-20240415224001954](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162125846.png)

## 点击共享，提交代码

![image-20240415224116883](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162125445.png)

push完之后，查看gitee

![image-20240415224234156](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162125108.png)

## 开发项目基本功能

HelloController

```java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "hello devops";
    }
}
```

## 新建item，创建流水线

定义一个流水线项目，指定项目的git位置

![image-20240416070335945](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162125243.png)

### 新建item：devops-demo

![image-20240416070508528](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162125334.png)

我们的流水线配置都会写入到项目中的Jenkinsfile文件，项目中先创建一个空文件

![image-20240416072340342](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126548.png)

### 配置流水线

![image-20240416072309221](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126292.png)

提示没有权限，配置git的用户名和密码

![image-20240416072430939](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126270.png)

选择添加的凭证用户

![image-20240416072519745](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126724.png)

正常情况，点击保存

![image-20240416072645214](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126892.png)

脚本路径：Jenkinsfile，表示Jenkinsfile文件在项目的根目录下

打开blue ocean

![image-20240416072811440](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126651.png)



![image-20240416072848507](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126510.png)

### 编辑Jenkinsfile文件

流水线语法

https://www.jenkins.io/zh/doc/book/pipeline/syntax/

![image-20240416074710394](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126133.png)

这里先写demo用来测试

```shell
pipeline{
    //全部的CICD流程都定义在这里
    agent any //任何代理都可以被执行

    //定义流水线的加工流程
    stages{
        //阶段1编译
        stage('编译'){
        //要做的事情
        steps{
            echo "编译。。。"
            }
        }
        //阶段2测试
        stage('测试'){
        steps{
            echo "测试。。。"
            }
        }
        //阶段3打包
        stage('打包'){
        steps{
            echo "打包。。。"
            }
        }
        //阶段4部署
        stage('部署'){
        steps{
            echo "部署。。。"
            }
        }
    }
}
```

编辑后，提交git代码



## 流水线启动

流水线启动后

​	1先去git位置自动拉取代码

​	2解析拉取代码中指定的Jenkinsfile文件

​	3按照Jenkinsfile文件指定的流水线开始加工项目

![image-20240416080651152](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126623.png)



![image-20240416080709756](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126209.png)

点击运行

![image-20240416080801303](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126090.png)

点击右下角打开

![image-20240416081219488](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126069.png)

编译阶段前会去git位置自动拉取代码

![image-20240416081243566](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126693.png)

然后按照Jenkinsfile文件指定的流水线开始加工项目编译、测试、打包、部署。

![image-20240416081254790](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126142.png)

demo测试成功后，开始完善每个阶段要做的事。

# Jenkinsfile实践

项目中提供了Jenkins流水线语法帮助

![image-20240416082358868](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126610.png)

## 片段生成器

选择对应的示例，Jenkins可以自动帮你生成对应的片段

![image-20240416082453961](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126580.png)

比如发送邮件

![image-20240416082703336](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126046.png)

## **Declarative Directive Generator**

生成 Jenkins Declarative Pipeline 的工具，在界面上填写需要的信息，比如代理配置、构建触发条件、构建环境设置、构建步骤等。

![image-20240416083010086](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127325.png)

比如设置环境变量

![image-20240416083109958](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127968.png)

### 添加环境变量

$username、${username}两种方式都可以取到环境变量的值

```shell
pipeline{
    //全部的CICD流程都定义在这里
    agent any //任何代理都可以被执行
    environment {
      username = "dongguo"
    }

    //定义流水线的加工流程
    stages{
        //阶段1编译
        stage('编译'){
        //要做的事情
        steps{
            echo "编译。。。"
            }
        }
        //阶段2测试
        stage('测试'){
        steps{
            echo "测试。。。"
            echo "$username"
            echo "${username}"
            }
        }
        //阶段3打包
        stage('打包'){
        steps{
            echo "打包。。。"
            }
        }
        //阶段4部署
        stage('部署'){
        steps{
            echo "部署。。。"
            }
        }
    }
}
```

提交代码后流水线运行，在测试阶段输出了环境变量值

![image-20240416083424087](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127832.png)

## 工作空间与环境变量

新增打印当前工作目录（使用 pwd 命令），然后列出当前目录下所有文件和目录的详细信息（使用 ls -alh 命令）

```shell
pipeline{
    //全部的CICD流程都定义在这里
    agent any //任何代理都可以被执行
    environment {
      username = "dongguo"
    }

    //定义流水线的加工流程
    stages{
        //阶段1编译
        stage('编译'){
        //要做的事情
        steps{
            echo "编译。。。"
            }
        }
        //阶段2测试
        stage('测试'){
        steps{
            echo "测试。。。"
            echo "$username"
            echo "${username}"
            sh 'pwd && ls -alh' //打印当前工作目录（使用 pwd 命令），然后列出当前目录下所有文件和目录的详细信息（使用 ls -alh 命令）。
            sh 'printenv' //打印当前 Shell 环境中的所有环境变量及其取值。
            }
        }
        //阶段3打包
        stage('打包'){
        steps{
            echo "打包。。。"
            }
        }
        //阶段4部署
        stage('部署'){
        steps{
            echo "部署。。。"
            }
        }
    }
}
```

Jenkins WORKSPACE（工作空间）=/var/jenkins_home/workspace/devops-demo

![image-20240416092722469](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127785.png)

jenkins的家目录 /var/jenkins_home 已经被我们docker外部的宿主机挂载了 ； /var/lib/docker/volumes/jenkins-data/_data



每一个流水线项目，占用一个文件夹位置

jenkins配置环境比如构建的次数BUILD_NUMBER=12，以及我们自己配置的username=dongguo

![image-20240416093701944](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127280.png)

工作空间的临时目录：WORKSPACE_TMP=/var/jenkins_home/workspace/devops-demo@tmp

构建期间的文件都放在这个地方，构建完成后清空这个临时目录

![image-20240416094026287](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127997.png)

# git代码发生改变触发流水线自动构建

远程的git代码提交了，jenkins流水线自动触发构建。

实现流程： 

1、保证jenkins所在主机能被远程访问 

2、jenkins中远程触发需要权限，我们应该使用用户进行授权 

3、配置gitee/github，webhook进行触发

点击配置

![image-20240416095048134](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127020.png)

选中触发远程构建，填写身份验证令牌

![image-20240416102905570](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127812.png)

这个身份验证令牌是什么呢？

打开码云对应的项目，点击管理

![image-20240416095515921](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127361.png)

左下角有一个webhooks，点击添加webhooks

Gitee 的 Webhooks 允许你将特定事件（如代码推送、合并请求等）的通知发送到你的 Jenkins 服务器，以触发相应的 Jenkins 任务。

![image-20240416095613278](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127943.png)

WebHook 被触发后，发送 HTTP / HTTPS 的目标通知地址，地址在填写身份验证令牌提示：

JENKINS_URL`/job/devops-demo/build?token=`TOKEN_NAME` 或者 /buildWithParameters?token=`TOKEN_NAME

JENKINS_URL： 外网地址，因为我是使用自己centos服务器，只能做内网穿透

![image-20240416102041493](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127215.png)

TOKEN_NAME： 我们自己定义，对应Jenkins中的身份验证令牌 

拼接后：http://vjck7a.natappfree.cc/job/devops-demo/build?token=dongguo

事件push时触发

![image-20240416102130359](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127416.png)



如果url填写错误会报错的

![image-20240416102148824](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162126828.png)

最终能不能访问点击测试，发现Error 403

![image-20240416102407192](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127555.png)

这是因为没有Jenkins的权限，先新建一个用户

![image-20240416103317621](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127965.png)

点击创建的dongguo用户，重新使用该账号登陆

![image-20240416103351328](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127565.png)

点击设置

![image-20240416103404637](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127429.png)

点击添加token，要注意保存：11bc78c5a0dbb8d3fdf3f2f5a1d069c132，因为刷新后就不显示了

![image-20240416103632066](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127676.png)

最终url为：http://dongguo:11bc78c5a0dbb8d3fdf3f2f5a1d069c132@vjck7a.natappfree.cc/job/devops-demo/build?token=dongguo

更新url

![image-20240416103953914](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127796.png)

重新发送显示201

![image-20240416104258113](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127102.png)

内网穿透也显示201

![image-20240416104324510](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127910.png)

流程：

![image-20240416104453409](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127915.png)

查看流水线，心在运行id为13，这个其实是我们gitee测试成功触发的构建

![image-20240416104630108](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162127686.png)

修改代码，提交git

```shell
pipeline{
    //全部的CICD流程都定义在这里
    agent any //任何代理都可以被执行
    environment {
      username = "dongguo"
    }

    //定义流水线的加工流程
    stages{
        //阶段1编译
        stage('编译'){
        //要做的事情
        steps{
            echo "编译。。。"
            echo "正在检查基本信息"
            sh 'java -version'
            sh 'git --version'
            sh 'docker version'
            }
        }
        //阶段2测试
        stage('测试'){
        steps{
            echo "测试。。。"
            echo "$username"
            echo "${username}"
            sh 'pwd && ls -alh' //打印当前工作目录（使用 pwd 命令），然后列出当前目录下所有文件和目录的详细信息（使用 ls -alh 命令）。
            sh 'printenv' //打印当前 Shell 环境中的所有环境变量及其取值。
            }
        }
        //阶段3打包
        stage('打包'){
        steps{
            echo "打包。。。"
            }
        }
        //阶段4部署
        stage('部署'){
        steps{
            echo "部署。。。"
            }
        }
    }
}
```

![image-20240416104911878](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128961.png)

这里报错了，不过已经实现自动构建了。

![image-20240416105010466](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128098.png)

这是因为java -version写错了

![image-20240416105051740](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128558.png)

再次提交

![image-20240416110011211](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128897.png)

Jenkins容器确实没有docker

![image-20240416105951061](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128939.png)

## Jenkins容器内部使用docker

首先安装Docker、Docker Pipeline插件

![image-20240416123630680](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128221.png)

1Jenkins容器安装docker（比较麻烦）

2.Jenkins容器使用宿主机的docker

![image-20240416123700090](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128535.png)

Jenkins容器内部使用docker，必须要有执行docker的权限

我们看到docker.sock文件，拥有者root，所有组docker

![image-20240416123949724](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128834.png)

把权限修改为root用户下root用户组的文件，

![image-20240416124306195](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128708.png)

然后让其他组成员拥有读写权限

![image-20240416124358642](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128853.png)





```shell
docker run \
-u root \
--name=jenkins \
-d \
-p 8080:8080 \
-p 50000:50000 \
-v jenkins-data:/var/jenkins_home \
-v /etc/localtime:/etc/localtime:ro \
-v /var/run/docker.sock:/var/run/docker.sock \
-v /usr/bin/docker:/usr/bin/docker \
-v /etc/docker/deamon.json:/etc/docker/deamon.json \
--restart=always \
jenkins/jenkins:2.453
```

删除容器、重新运行，数据都在具名卷Jenkins-data中，删除容器对数据是没有影响的

![image-20240416125439207](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128240.png)



![image-20240416125514473](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128121.png)

Jenkins容器内部就可以使用docker了



Jenkins重新编译

![image-20240416125719561](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128511.png)

编译通过

![image-20240416125751874](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128550.png)

## 在Jenkins中使用Docker镜像

https://www.jenkins.io/zh/doc/book/pipeline/docker/

![image-20240416142150564](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128580.png)

### 修改Jenkinsfile

在Jenkins中使用Docker后，我们可以每个阶段都使用不同的Docker镜像来运行任务

比如在Jenkins中是没有mavne的，执行mvn命令肯定是不通过的。

![image-20240416142432793](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128730.png)

当然可以在Jenkins安装maven，但是我们已经可以使用Docker了，简单的方法是通过docker指定镜像来实现mvn命令的操作

```shell
            agent {
                docker { image 'maven:3-alpine' }
            }
            steps {
                sh 'mvn --version'
            }
```

最后修改为

```shell
pipeline{
    //全部的CICD流程都定义在这里
    agent any //任何代理都可以被执行
    environment {
      username = "dongguo"
    }

    //定义流水线的加工流程
    stages{
       //阶段0检查
        stage('环境检查'){
        //要做的事情
        steps{
            echo "正在检查基本信息"
            sh 'java -version'
            sh 'git --version'
            sh 'docker version'
            }
        }
        //阶段1编译
        stage('编译'){
        //要做的事情
            agent {
                docker { image 'maven:3-alpine' }
            }
            steps {
                echo "编译。。。"
                sh 'mvn --version'
            }
        }
        //阶段2测试
        stage('测试'){
        steps {
            echo "测试。。。"
            echo "$username"
            echo "${username}"
            sh 'pwd && ls -alh' //打印当前工作目录（使用 pwd 命令），然后列出当前目录下所有文件和目录的详细信息（使用 ls -alh 命令）。
            sh 'printenv' //打印当前 Shell 环境中的所有环境变量及其取值。
            }
        }


        //阶段3打包
        stage('打包'){
        steps{
            echo "打包。。。"
            }
        }
        //阶段4部署
        stage('部署'){
        steps{
            echo "部署。。。"
            }
        }
    }
}
```

### 安装插件

确保Jenkins安装了Docker Pipeline插件，当然之前已经安装过了。

![image-20240416143450972](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128094.png)

测试通过

![image-20240416151011127](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128108.png)

# Jenkins实现maven打包

## 修改Jenkinsfile

编译阶段实现打包

```shell
pipeline{
    //全部的CICD流程都定义在这里
    agent any //任何代理都可以被执行
    environment {
      username = "dongguo"
    }

    //定义流水线的加工流程
    stages{
       //阶段0检查
        stage('环境检查'){
        //要做的事情
        steps{
            echo "正在检查基本信息"
            sh 'java -version'
            sh 'git --version'
            sh 'docker version'
            }
        }
        //阶段1编译
        stage('编译'){
        //要做的事情
            agent {
                docker { image 'maven:3-alpine' }
            }
            steps {
                echo "编译。。。"
                sh 'mvn --version'
                sh 'mvn clean package -Dmaven.test.skip=true'  //打包
            }
        }
        //阶段2测试
        stage('测试'){
        steps {
            echo "测试。。。"
            echo "$username"
            echo "${username}"
            sh 'pwd && ls -alh' //打印当前工作目录（使用 pwd 命令），然后列出当前目录下所有文件和目录的详细信息（使用 ls -alh 命令）。
            sh 'printenv' //打印当前 Shell 环境中的所有环境变量及其取值。
            }
        }


        //阶段3生成镜像
        stage('生成镜像'){
        steps{
            echo "生成镜像。。。"
            sh 'docker version'
            }
        }
        //阶段4部署
        stage('部署'){
        steps{
            echo "部署。。。"
            }
        }
    }
}
```

提交代码，Jenkins自动构建，maven默认镜像使用maven中央仓库下载，速度是非常慢的，就这么一个简单的项目就构建了三分钟多。

![image-20240416164505546](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128389.png)



## 配置maven加速

现在修改为阿里云镜像



宿主机和Jenkins容器数据卷映射，当我们Jenkins执行Jenkinsfile构建时，其实是在宿主机上启动了一个maven容器

![image-20240416170850484](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128273.png)



![image-20240416161355086](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128515.png)

第一种方式，在启动maven容器时-v进行挂载配置，移植性不好，

```
        //阶段1编译
        stage('编译'){
        //要做的事情
            agent {
                docker { 
                image 'maven:3-alpine' 
                args '-v /appconfig/maven/settings.xml:/app/setting.xml'
                }
            }
            steps {
                echo "编译。。。"
                sh 'mvn --version'
                sh 'mvn clean package -Dmaven.test.skip=true'  //打包
            }
        }
```

第二种方式，统一使用Jenkins-data目录内的配置，这样一份Jenkins-data可以在另外一台机器上直接使用。

![image-20240416161844122](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128960.png)

### 统一使用Jenkins-data目录内的配置

在/var/lib/docker/volumes/jenkins-data/_data目录下（对应Jenkins容器中/var/jenkins_home）创建appconfig用来统一存放配置

![image-20240416162223620](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128802.png)

创建maven文件夹用来存放maven的配置，复制配置了阿里云镜像的maven的settings.xml到该为止

![image-20240416162653223](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128687.png)

注意本地仓库localRepository的位置的修改，这里就指定用户目录下的/repository，注意这里是mavne容器中的路径

```shell
 <localRepository>/repository</localRepository>
```

推荐所有可变配置项都配置在jenkins-data目录下



1.修改Jenkinsfile指定settings.xml,位置/var/jenkins_home/appconfig/maven/settings.xml

2.每次mavne容器都是临时创建的，对应本地仓库在maven容器上，构建结束就删除了，这里将将Docker容器内的路径映射到Jenkins上的路径/var/jenkins_home/appconfig/maven/repository   我是为了在宿主机上也同步，这里可以只保存在Jenkins中。

```shell
        //阶段1编译
        stage('编译'){
        //要做的事情
            agent {
                docker {
                    image 'maven:3-alpine'
                    args '-v /var/jenkins_home/appconfig/maven/repository:/repository' // 将Docker容器内的路径映射到Jenkins主机上的路径
                }
            }
            steps {

                echo "编译。。。"
                sh 'mvn --version'
                sh 'mvn clean package -s "/var/jenkins_home/appconfig/maven/settings.xml" -Dmaven.test.skip=true'  //打包
            }
        }
```

提交代码，重新构建花费2分钟

![image-20240416164910316](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162128074.png)

maven依赖已经保存到Jenkins上，再次构建就不用重复拉取依赖了

![image-20240416190045053](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162129190.png)



![image-20240416165335641](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162129229.png)

## 临时容器导致的问题

Maven生成的jar是放在临时文件中的。/var/jenkins_home/workspace/devops-demo@2/target/devops-demo-0.0.1-SNAPSHOT.jar

而后续的阶段会默认回到/var/jenkins_home/workspace/devops-demo目录，这个目录是没有生成的jar包的，那我们后续生成镜像的时候肯定会出问题的。



![image-20240416203753571](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162129353.png)



新增Dockerfile,编辑Jenkinsfile

![image-20240416205642478](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162129521.png)

dockerfile运行jar包

```
# 基础镜像使用java
FROM java:8
# 作者
LABEL user=Dongguo
#复制jar包
COPY target/*.jar /app.jar

RUN bash -c 'touch /app.jar'

# 运行jar包
ENTRYPOINT ["java","-jar","/app.jar"]
#暴露8081端口作为微服务
EXPOSE 8081
```



Jenkinsfile的生成镜像阶段

```shell
//阶段3生成镜像
stage('生成镜像'){
steps{
    echo "生成镜像。。。"
    sh 'pwd && ls -alh'
    sh 'docker version'
    sh 'docker build -t devops-demo .'
    }
}
```

提交代码自动构建

可以看到生成镜像时所在目录/var/jenkins_home/workspace/devops-demo，是没有target目录的，

![image-20240416205850255](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162129323.png)

在执行docker build时就会报错

![image-20240416205959969](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162129616.png)

办法:可以在打包时指定目录，/var/jenkins_home/workspace/devops-demo这样target就存在了。

我们看到WORKSPACE环境变量是指定的该位置

![image-20240416210230876](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162129807.png)

但是这里是一个坑，因为编译阶段的WORKSPACE是/var/jenkins_home/workspace/devops-demo@2

所以在这里声明一个全局变量WORK_SPACE使用WORKSPACE，并在编译阶段进入到指定目录

修改Jenkinsfile

```shell
    environment {
      username = "dongguo"
      WORK_SPACE = "$WORKSPACE" //全局环境中的WORKSPACE是/var/jenkins_home/workspace/devops-demo
    }
    
        //阶段1编译
        stage('编译'){
        //要做的事情
            agent {
                docker {
                    image 'maven:3-alpine'
                    args '-v /var/jenkins_home/appconfig/maven/repository:/repository' // 将Docker容器内的路径映射到Jenkins主机上的路径
                }
            }
            steps {
                echo "编译。。。"
                sh 'printenv'
                sh 'mvn --version'
                sh 'cd $WORK_SPACE && mvn clean package -s "/var/jenkins_home/appconfig/maven/settings.xml" -Dmaven.test.skip=true'  //打包，这里不能分开写，因为每一行都是基于上下文环境，当前上下文环境是/var/jenkins_home/workspace/devops-demo@2
            }
        }
```

提交代码





验证1：编译阶段的WORK_SPACE：/var/jenkins_home/workspace/devops-demo@2

![image-20240416211215760](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162129449.png)

验证2：通过cd $WORK_SPACE ， Building jar: /var/jenkins_home/workspace/devops-demo/target/devops-demo-0.0.1-SNAPSHOT.jar   已经在正确的位置了

![image-20240416211522733](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162129417.png)

验证3：生成镜像阶段已经生成了targer目录

![image-20240416211253543](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162129264.png)

验证4：生成镜像成功

![image-20240416211321272](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162129525.png)

验证5：在宿主机查看镜像

![image-20240416211356644](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162129837.png)



项目构建镜像成功，接下来就启动容器了。

编辑Jenkinsfile，每次部署时删除容器再重新启动新的容器

```shell
        //阶段4部署
        stage('部署'){
        steps{
            echo "部署。。。"
            sh 'docker rm -f devops-demo'
            sh 'docker run -d -p 8081:8081 --name=devops-demo devops-demo'
            }
        }
```

提交代码，自动构建

部署成功

![image-20240416212306825](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162129505.png)

查看容器

![image-20240416212422399](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162129940.png)

访问http://192.168.122.141:8081/hello

![image-20240416212501598](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404162129142.png)

# POST后置操作

在 Jenkins Pipeline 中，`post` 块用于定义在整个 Pipeline 执行结束后执行的步骤，无论 Pipeline 执行成功或失败都会执行这些步骤。

你可以使用 `success` 块来定义 Pipeline 成功时执行的步骤，使用 `failure` 块来定义 Pipeline 失败时执行的步骤

![image-20240417061520607](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404170928646.png)

## 某个阶段使用POST

修改Jenkinsfile在部署阶段使用post实现成功或失败执行输出

```shell
        // 阶段4部署
        stage('部署') {
            steps {
                echo "部署。。。"
                sh 'docker rm -f devops-demo'
                sh 'docker run -d -p 8081:8081 --name=devops-demo devops-demo'
            }
            post {
                failure {
                    // One or more steps need to be included within each condition's block.
                    echo "执行失败。。。"
                }
                success {
                    // One or more steps need to be included within each condition's block.
                    echo "执行成功。。。"
                }
            }
        }
```

正常情况输出：执行成功

![image-20240417063235868](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404170928673.png)

## 全阶段统一POST

当然POST可以统一全阶段有效

修改Jenkinsfile

```shell
pipeline {
    // 全部的CICD流程都定义在这里
    agent any // 任何代理都可以被执行

    environment {
        username = "dongguo"
        WORK_SPACE = "$WORKSPACE" // 全局环境中的WORKSPACE是/var/jenkins_home/workspace/devops-demo
    }

    // 定义流水线的加工流程
    stages {
        // 阶段0检查
        stage('环境检查') {
            // 要做的事情
            steps {
                echo "正在检查基本信息"
                sh 'java -version'
                sh 'git --version'
                sh 'docker version'
            }
        }
        // 阶段1编译
        stage('编译') {
            // 要做的事情
            agent {
                docker {
                    image 'maven:3-alpine'
                    args '-v /var/jenkins_home/appconfig/maven/repository:/repository' // 将Docker容器内的路径映射到Jenkins主机上的路径
                }
            }
            steps {
                echo "编译。。。"
                sh 'printenv'
                sh 'mvn --version'
                sh 'cd $WORK_SPACE && mvn clean package -s "/var/jenkins_home/appconfig/maven/settings.xml" -Dmaven.test.skip=true' // 打包
            }
        }
        // 阶段2测试
        stage('测试') {
            steps {
                echo "测试。。。"
                echo "$username"
                echo "${username}"
                sh 'pwd && ls -alh' // 打印当前工作目录（使用 pwd 命令），然后列出当前目录下所有文件和目录的详细信息（使用 ls -alh 命令）。
                sh 'printenv' // 打印当前 Shell 环境中的所有环境变量及其取值。
            }
        }

        // 阶段3生成镜像
        stage('生成镜像') {
            steps {
                echo "生成镜像。。。"
                sh 'pwd && ls -alh'
                sh 'docker version'
                sh 'docker build -t devops-demo .'
            }
        }
        // 阶段4部署
        stage('部署') {
            steps {
                echo "部署。。。"
                sh 'docker rm -f devops-demo'
                sh 'docker run -d -p 8081:8081 --name=devops-demo devops-demo'
            }
//             post {
//                 failure {
//                     // One or more steps need to be included within each condition's block.
//                     echo "执行失败。。。"
//                 }
//                 success {
//                     // One or more steps need to be included within each condition's block.
//                     echo "执行成功。。。"
//                 }
//             }
        }

        // 阶段5发送报告
        stage('发送报告') {
            steps {
                echo "发送报告。。。"
            }
        }
    }

    post {
        failure {
            // One or more steps need to be included within each condition's block.
            echo "执行失败。。。"
        }
        success {
            // One or more steps need to be included within each condition's block.
            echo "执行成功。。。"
        }
    }
}
```

会在所有阶段执行完成后，根据执行情况在最后阶段输出

![image-20240417063904327](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404170928008.png)

失败情况

![image-20240417064521173](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404170928458.png)

# 构建完成发送邮件通知

emailext body: '构建成功', subject: 'Jenkins构建', to: '291320608@qq.com'

## 配置Jenkins管理员邮件地址

插件

![img](https://img-blog.csdnimg.cn/f598269ae84c4077acd37c4a30f6ee79.png)

系统配置

![image-20240417064907812](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404170928043.png)

配置邮件地址

![image-20240417065010055](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404170928120.png)

## 配置邮件发送的认证信息

1开启邮箱的smtp服务，获取到授权码

![image-20240417070115716](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404170928862.png)

SMTP对应地址smtp.163.com 端口465

![image-20240417065919872](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404170928499.png)

找到Jenkins 系统管理中的邮件通知，填写信息，点击测试

![image-20240417071027522](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404170928614.png)

qq邮箱已经收到邮件

![image-20240417071313248](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404170929531.png)

上面只是测试，真正有效的是Extended E-mail Notification

Credentials添加一个凭据 账号是邮箱的账号，密码是邮箱的授权码

![image-20240417075344710](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404170929425.png)

## 随便搜一个Jenkins邮件发送模板

![image-20240417072229625](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404170929836.png)

复制到片段生成器，输入收件人邮箱生成流水线脚本

![image-20240417072806513](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404170929004.png)

Jenkinsfile

```shell
        // 阶段5发送报告
        stage('发送报告') {
            steps {
                echo "发送报告。。。"
                emailext body: '''<!DOCTYPE html>
                <html>
                <head>
                <meta charset="UTF-8">
                <title>${ENV, var="JOB_NAME"}-第${BUILD_NUMBER}次构建日志</title>
                </head>
                
                <body leftmargin="8" marginwidth="0" topmargin="8" marginheight="4"
                    offset="0">
                    <table width="95%" cellpadding="0" cellspacing="0"
                        style="font-size: 11pt; font-family: Tahoma, Arial, Helvetica, sans-serif">
                        <tr>
                            <td>本邮件是Jenkins自动发送，请勿回复！</td>
                        </tr>
                        <tr>
                            <td><h3>
                                    <font color="#e53935">&nbsp&nbsp&nbsp&nbsp构建结果 - ${BUILD_STATUS}!</font>
                                </h3></td>
                        </tr>
                        <tr>
                            <td><br />
                            <b><font color="#3f51b5">构建信息:</font></b>
                            <hr size="2" width="100%" align="center" /></td>
                        </tr>
                        <tr>
                            <td>
                                <ul>
                                    <li>项目名称：${PROJECT_NAME}</li>
                                    <li>构建编号：第${BUILD_NUMBER}次构建</li>
                                    <li>构建状态：${BUILD_STATUS}</li>
                                    <li>触发原因：${CAUSE}</li>
                                    <li>项目Url：
                                        <a href="${PROJECT_URL}">${PROJECT_URL}</a></li>
                                           <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                                       </a>
                                    </li>
                                    <li>测试报告： <a href="${PROJECT_URL}HTML_20Report">${PROJECT_URL}HTML_20Report</a></li>
                                    <li>所有用例： ${TEST_COUNTS,var="total"}</li>
                                    <li>成功用例： ${TEST_COUNTS,var="pass"}</li>
                                    <li>失败用例： ${TEST_COUNTS,var="fail"}</li>
                                    <li>跳过用例： ${TEST_COUNTS,var="skip"}</li>
                                    <li>构建日志：<a href="${BUILD_URL}console">${BUILD_URL}console</a></li>
                                </ul>
                            </td>
                         <tr>
                            <td><b><font color="#3f51b5">构建日志:</font></b>
                            <hr size="2" width="100%" align="center" /></td>
                        </tr>
                        <tr>
                            <td><textarea cols="160" rows="80" readonly="readonly"
                                    style="font-family: Microsoft YaHei">${BUILD_LOG,maxLines=1000}</textarea>
                            </td>
                        </tr>
                </html>''', subject: '${ENV, var="JOB_NAME"}-第${BUILD_NUMBER}次构建日志', to: '291320608@qq.com'
            }
        }
```

提交代码

![image-20240417073008543](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404170929656.png)

查看邮箱

![image-20240417075434221](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404170929530.png)

## Jenkins日期修改

发现Jenkins显示的日期不是本地时间

![image-20240417093540860](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171423159.png)

找到脚本命令行

![image-20240417093630807](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171423515.png)

输入

```shell
System.setProperty('org.apache.commons.jelly.tags.fmt.timeZone', 'Asia/Shanghai')
```

点击运行

![image-20240417093735029](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171423404.png)

再次构建，时间就正确了

![image-20240417093813332](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171423635.png)

邮件的接收时间也正确了

![image-20240417093833667](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171423551.png)
