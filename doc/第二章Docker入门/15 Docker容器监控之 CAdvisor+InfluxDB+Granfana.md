通过docker stats命令可以很方便的看到当前宿主机上所有容器的CPU,内存以及网络流量等数据，一般小公司够用了。

![image-20240410152056403](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101724502.png)

但是，docker stats统计结果只能是当前宿主机的全部容器，而且默认实时刷新的，没有地方存储、没有健康指标过线预警等功能

# 容器监控3剑客（CAdvisor+InfluxDB+Granfana）

CAdvisor监控收集+InfluxDB存储数据+Granfana展示图表

![image-20240410152131968](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101724910.png)

## CAdvisor

CAdvisor是一个容器资源监控工具,包括容器的内存,CPU,网络IO,磁盘IO等监控,同时提供了一个

WEB页面用于查看容器的实时运行状态。CAdvisor默认存储2分钟的数据,而且只是针对单物理机。

不过，CAdvisor提供了很多数据集成接口,支持InfluxDB,Redis,Kafka,Elasticsearch等集成,可以加

上对应配置将监控数据发往这些数据库存储起来。

CAdvisor功能主要有两点

- 展示Host和容器两个层次的监控数据。
- 展示历史变化数据。

## InfluxDB

InfluxDB是用Go语言编写的一个开源分布式时序、事件和指标数据库，无需外部依赖。

CAdvisor默认只在本机保存最近2分钟的数据，为了持久化存储数据和统一收集展示监控数据,需要将数据存储到InfluxDB中。InfluxDB是一个时序数据库,专门用于存储时序相关数据,很适合存储CAdvisor的数据。而且，CAdvisor本身已经提供了InfluxDB的集成方法，丰启动容器时指定配置即可。

InfluxDB主要功能：

- 基于时间序列，支持与时间有关的相关函数(如最大、最小、求和等)；
- 可度量性:你可以实时对大量数据进行计算;
- 基于事件：它支持任意的事件数据；

## Granfana

Grafana是一个开源的数据监控分析可视化平台,支持多种数据源配置(支持的数据源包括InfluxDB, MySQL, Elasticsearch, OpenTSDB、Graphite等)和丰富的插件及模板功能,支持图表权限控制和报警Grafan主要特性

- 灵活丰富的图形化选项
- 可以混合多种风格
- 支持白天和夜间模式
- 多个数据源

# compose容器编排(CAdvisor+InfluxDB+Granfana)

## 编写docker-compose.yml

创建/opt/software/cig目录，并在该目录下编写docker-compose.yml

```yml
volumes:
  grafana_data: {}

services:
 influxdb:
  image: tutum/influxdb
  restart: always
  environment:
    - PRE_CREATE_DB=cadvisor
  ports:
    - "8083:8083"
    - "8086:8086"
  volumes:
    - ./data/influxdb:/data

 cadvisor:
  image: google/cadvisor
  links:
    - influxdb:influxsrv
  command: -storage_driver=influxdb -storage_driver_db=cadvisor -storage_driver_host=influxsrv:8086
  restart: always
  ports:
    - "8080:8080"
  volumes:
    - /:/rootfs:ro
    - /var/run:/var/run:rw
    - /sys:/sys:ro
    - /var/lib/docker/:/var/lib/docker:ro

 grafana:
  user: "104"
  image: grafana/grafana
  restart: always
  links:
    - influxdb:influxsrv
  ports:
    - "3000:3000"
  volumes:
    - grafana_data:/var/lib/grafana
  environment:
    - HTTP_USER=admin
    - HTTP_PASS=admin
    - INFLUXDB_HOST=influxsrv
    - INFLUXDB_PORT=8086
    - INFLUXDB_NAME=cadvisor
    - INFLUXDB_USER=root
    - INFLUXDB_PASS=root
```

![image-20240410153803850](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101724121.png)

## 查看是否存在语法错误

```shell
docker-compose config -q
```

没有提示说明语法没有错误

![image-20240410153834196](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101724327.png)

## 启动并运行定义在 `docker-compose.yml` 文件中的服务

```shell
docker-compose up
```

等待比较长的一段时间

![image-20240410154214785](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101724568.png)

## 查看三个服务容器是否启动

![image-20240410163337266](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101724981.png)

执行docker-compose up命令后并不会退出，如果日志没有错误，可以新打开一个会话查看这三个服务容器是否正常启动了。

![image-20240410160912900](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101724692.png)

## cAdvisor收集服务

http://ip:8080/，第一次访问比较慢，

![image-20240410161030255](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101724369.png)



cadvisor也有基础的图形展现功能，这里主要用它来作数据采集

![image-20240410161748822](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101724458.png)

## influxdb存储服务

http://ip:8083/

![image-20240410161118654](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101724277.png)

点击query templates，查询数据库

![image-20240410161952092](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101724745.png)显示的cadvisor是在docker-compose.yml中配置，预先创建的数据库。这样其他的服务（如 `cadvisor` 和 `grafana`)在连接到 `influxdb` 时就可以直接使用这个数据库，而不需要先手动创建它。

![image-20240410162124149](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101724256.png)

## grafana展现服务

http://ip:3000 默认帐户密码（admin/admin）

![image-20240410161240406](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101724472.png)

登录

![image-20240410161309643](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101724546.png)

### 配置数据源

grafana负责数据的展现，需要配置数据源

步骤1：

![image-20240410163654559](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101724470.png)

步骤2：添加数据源

![image-20240410163719598](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101724821.png)

步骤3：选择influxDB

![image-20240410163943560](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101724456.png)

步骤4：配置

![image-20240410164857929](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101723486.png)

1)选择InfluxDB数据源，因为前面的docker-compose 容器编排文件中有配置名称这块 , 且IP有可能随着Docker 的每一次重启而变动，所以Name不要写死IP , 使用名称即可 , Name为InfluxDB，URL就对应http://InfluxDB:8086

2)Database 使用cadvisor 同样是因为前面我们在docker-compose文件中有给influxDB初始化了一个数据库名字叫cadvisor,  账户 root 密码root。

3）点击save&test，没有报错则配置成功，连接到了InfluxDB中cadvisor数据库。

![image-20240410164950598](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101723100.png)

### 配置面板panel

1.创建一个新的仪表盘（dashboard），用于组织和展示各种数据可视化面板（Panel）

![image-20240410165215954](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101723492.png)

添加一个新的面板

![image-20240410165400991](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101723928.png)

2.grafana提供了多种面板展示，如柱状图、折线图等。默认为Time series时间序列

![image-20240410165602701](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101723566.png)

点击下拉，选择一个比较经典的Graph(old),这个是早期版本中的默认时间序列图表面板。

![image-20240410165737917](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101725782.png)

包含折线图与柱状图的展现，也可以为面板设置title和des

![image-20240410170203834](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101723460.png)

3.保存点击save

![image-20240410170331854](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101723245.png)

保存创建的仪表盘

![image-20240410170416706](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101723783.png)

当前面板是这样子的，现在还没有数据，现在需要进行填充数据。

![image-20240410170505592](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101723300.png)



点击下拉，选择Edit，对Dashboard或其组件进行各种自定义和配置。也就是要展现哪些数据。

![image-20240410170637454](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101723944.png)



Query一栏里面的配置类似于SQl的选项, default是默认查询那个字段, 后面是筛选条件 ,  ALIAS 是给这项起个别名, 如果需要一张图展示多个数据项, 可以点击+query添加查询语句 , B 、C之类的 ,

![image-20240410171135329](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101723829.png)

4.比如展现CPU的数据，根据容器名称展示CPU的使用情况

那么根据那个容器呢？CAdvisor负责收集数据，当然是根据它了。

![image-20240410171612102](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101723301.png)

最终配置：

可以看到面板中已经展示出数据了

![image-20240410171946175](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101723193.png)

进行保存

![image-20240410172036565](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101723866.png)

回到仪表盘界面

![image-20240410172104791](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101723946.png)

### 最终展示效果

![image-20240410172124604](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404101723057.png)

到这里cAdvisor+InfluxDB+Grafana容器监控系统就部署完成了