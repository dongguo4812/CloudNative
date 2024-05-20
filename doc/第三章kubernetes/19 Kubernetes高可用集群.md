# 高可用集群

![image-20240511105941228](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131255170.png)

## 准备机器

准备六台机器，三master三node

| 角色        | IP              |
| ----------- | --------------- |
| k8s-master1 | 192.168.122.143 |
| k8s-master2 | 192.168.122.144 |
| k8s-master3 | 192.168.122.145 |
| k8s-node1   | 192.168.122.146 |
| k8s-node2   | 192.168.122.147 |
| k8s-node3   | 192.168.122.148 |

配置2核2G 

## 内核升级（所有节点）

3.10内核在大规模集群具有不稳定性

内核升级到4.19+

1）查看内核版本

```shell
uname -sr
```

2）升级软件包

更新系统中的所有软件包，但是排除所有以 "kernel" 开头的软件包。

```shell
yum update -y --exclude=kernel*
```

3）安装 ELRepo 存储库

ELRepo 是一个针对 Enterprise Linux 系统（例如 CentOS、Red Hat Enterprise Linux）的第三方软件仓库，提供了额外的软件包。第一个命令导入了 ELRepo 存储库的 GPG 密钥，用于验证软件包的真实性和完整性。第二个命令则安装了 ELRepo 存储库的软件包 elrepo-release

```
rpm --import https://www.elrepo.org/RPM-GPG-KEY-elrepo.org
rpm -Uvh https://www.elrepo.org/elrepo-release-7.el7.elrepo.noarch.rpm
```

![image-20240511102701891](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131255025.png)

4）安装镜像加速

该插件会在运行 YUM 命令时自动检测可用的软件源镜像，并选择下载速度最快的镜像来下载软件包。

```
yum install -y yum-plugin-fastestmirror
```

5）列出向前系统支持的内核列表

```
yum --disablerepo="*" --enablerepo="elrepo-kernel" list available 
```

![image-20240511103145471](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131255257.png)

主要分为两类版本

kernel-lt： long term support：长期支持版
kernel-ml： mainline stable： 主线稳定版

6）选择自己的版本进行安装 如5.4.275-1.el7.elrepo

```shell
yum --enablerepo=elrepo-kernel install -y kernel-lt-5.4.275-1.el7.elrepo
```

7）查看内核位置

```shell
awk -F\' '$1=="menuentry " {print $2}' /etc/grub2.cfg
```

![image-20240511104717162](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131255827.png)

保证安装的5.4.275-1.el7.elrepo排在第一位

如果没有排在第一位，重新创建内核配置。

```shell
grub2-mkconfig -o /boot/grub2/grub.cfg
```

8）修改使用默认内核

```
vi /etc/default/grub
```

将 GRUB_DEFAULT 设置为 0，代表  GRUB 初始化页面的第一个内核将作为默认内核

![image-20240511105142845](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131255112.png)

再重新整理下内核

```shell
grub2-mkconfig -o /boot/grub2/grub.cfg
```

9）重启

```
reboot
```

10）查看内核

```
uname -r
```

![image-20240511105427797](https://gitee.com/dongguo4812_admin/image/raw/master/image/202405131255330.png)



注：在内核4.19+版本nf_conntrack_ipv4已经改为nf_conntrack， 4.18以下使用nf_conntrack_ipv4即可：

