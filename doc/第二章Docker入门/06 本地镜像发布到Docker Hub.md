# 1.注册Docker Hub账号

https://hub.docker.com/ 我使用的是github账号进行注册

![image-20240411182912312](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404112019615.png)

# 2.创建一个仓库，选为public

也可以不用创建，推送时会自动创建

# 3.登录远程docker仓库

username就是Docker Hub中的名称

![image-20240411182945811](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404112019506.png)

登陆后会在/root/.docker/config.json将登录信息记录

![image-20240411183102365](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404112019302.png)

当前docker login登录以后 。所有的东西都可以push到Docker Hub个人的仓库

# 4.修改为Docker Hub规范的镜像名

![image-20240411190136716](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404112019330.png)

Docker Hub一个完整镜像的全路径是docker.io/library/imageName:tag

将ubuntu修改为符合Docker Hub规范的镜像名：dongguo274812/myubuntu:1.0 

![image-20240411185011116](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404112019255.png)

# 5.将镜像推送到Docker Hub

![image-20240411185115748](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404112018984.png)



推送失败：denied: requested access to the resource is denied

![image-20240411202000388](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404112020862.png)

在网上查询都是说登录以及镜像命名的问题，但是这些我都确认是正确的。没有办法，只能在docker hub中找下答案。

1）点击我的账号

![image-20240411215701818](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404112209264.png)



2）发现Security安全中又设置token的

![image-20240411215743477](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404112209211.png)

3）创建token，并且这里有配置访问权限

![image-20240411215826764](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404112208493.png)

4）生成token，并且给出了使用token的方式

![image-20240411220049458](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404112208051.png)

5）使用token方式登录

复制token，输入到password位置

![image-20240411220206063](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404112208923.png)

6）重新push

![image-20240411220538419](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404112208732.png)

# 6.到Docker Hub上查看推送的镜像

这里我并没有提前创建myubuntu，推送时会自动帮你创建

![image-20240411220617052](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404112208733.png)

# 7.将本地镜像删除，从Docker Hub上拉取镜像

![image-20240411220827788](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404112208756.png)

