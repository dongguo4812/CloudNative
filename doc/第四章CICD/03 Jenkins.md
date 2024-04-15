Jenkins官网： https://www.jenkins.io/

![image-20240415134603741](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152218500.png)

中文官网：https://www.jenkins.io/zh/

![image-20240415135001282](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152219603.png)

# Jenkins安装

https://www.jenkins.io/zh/doc/book/installing/#docker

docker安装Jenkins

![image-20240415135347200](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152219145.png)



Jenkins镜像：https://hub.docker.com/r/jenkins/jenkins/tags?page=&page_size=&ordering=&name=2.4

![image-20240415221839552](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152218651.png)



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
--restart=always \
jenkins/jenkins:2.453
```

- `-u root`: 指定容器以 root 用户身份运行。

- `-v jenkins-data:/var/jenkins_home`: 将宿主机的 jenkins-data 卷挂载到容器内的 /var/jenkins_home 目录，用于持久化 Jenkins 数据。Jenkins所有配置都在/var/jenkins_home目录下，所以使用具名卷挂载

- `-v /etc/localtime:/etc/localtime:ro`: 将宿主机的时区文件挂载到容器内，以保持容器内外时间的一致性。使用国外的镜像，不可避免的出现时区问题，将时间同步为自己所在的时间。

- `--restart=always`: 指定容器发生异常退出时自动重启。

- `-v /var/run/docker.sock:/var/run/docker.sock`: 将宿主机的 Docker 守护进程的 UNIX 套接字挂载到容器内，以便容器内的 Jenkins 可以与宿主机上的 Docker 守护进程交互。



安装失败可以重启docker再次尝试

## 访问

http://192.168.122.141:8080/

![image-20240415145920547](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152218010.png)



## 查看密码

方式1：进入容器内部，/var/jenkins_home/secrets/initialAdminPassword查看密码

![image-20240415151155409](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152218042.png)

方式2：因为jenkins_home目录已经挂载到宿主机上，当然也可以在宿主机上查看

/var/lib/docker/volumes/jenkins-data/_data

![image-20240415151638738](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152218339.png)

方式3：当然最简单的方法是看日志

```shell
docker logs 02bbe60127f9
```

![image-20240415151857951](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152218827.png)

## 安装插件

![image-20240415151716621](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152218251.png)

正常情况下下载速度还是可以的。

-------------------------------------下载速度可以的话可以跳过--------------------------------------------------------

由于Jenkins需要下载大量内容，但是默认下载地址下载速度较慢，需要重新设置下载地址为国内镜像站。

第一步修改 jenkins 插件安装配置：

/var/lib/docker/volumes/jenkins-data/_data的hudson.model.UpdateCenter.xml  url修改为

```xml
<?xml version='1.1' encoding='UTF-8'?>
<sites>
  <site>
    <id>default</id>
    <url>https://mirrors.tuna.tsinghua.edu.cn/jenkins/updates/update-center.json</url>
  </site>
</sites>
```

![image-20240415164253666](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152218538.png)

**第二步：修改服务器配置。**

/var/lib/docker/volumes/jenkins-data/_data/updates/default.json ，将其中的updates.jenkins-ci.org/download 替换为 mirrors.tuna.tsinghua.edu.cn/jenkins （旧版本）

有些 jenkins 版本当中，是将updates.jenkins.io/download 替换为 mirrors.tuna.tsinghua.edu.cn/jenkins（新版本  确定后再替换）

然后把 www.google.com 修改为 www.baidu.com



/var/lib/docker/volumes/jenkins-data/_data/updates/default.json，经过搜索能够查到的是updates.jenkins.io/download

![image-20240415162807556](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152218387.png)

执行：

```shell
sed -i 's/updates.jenkins-ci.org\/download/mirrors.tuna.tsinghua.edu.cn\/jenkins/g' default.json
sed -i 's/www.google.com/www.baidu.com/g' default.json
```



![image-20240415163207458](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152218377.png)

重启jenkins后，登录即可，速度还算是可以吧

-------------------------------------下载速度可以的话可以跳过--------------------------------------------------------



![image-20240415215740436](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152219346.png)

## 创建管理员用户

以后就可以使用管理员账户登录Jenkins

![image-20240415215630802](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152219983.png)





## 实例配置

保存

![image-20240415215756963](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152219378.png)

## 开始使用

![image-20240415215807826](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152219113.png)

## 首页

![image-20240415220611223](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152219187.png)



## 安装Blue Ocean插件

![image-20240415221128472](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152219409.png)

安装完成后

![image-20240415221409065](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404152219218.png)