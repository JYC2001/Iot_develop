# 部署EMQX和InfluxDB

**时间：2026/4/22**

## 搭建“服务器”

1. 安装Docker Desktop
2. 一键启动核心服务：
   - 新建MyIoTPlatform文件夹
   - 创建文件docker-compose.yml
   - 命令行输入：docker-compose up -d
   - 验证：访问http://localhost:18083

## 用python模拟“嵌入式设备”（数据采集）

1. 搭建开发环境：
   1. pip install paho-mqtt numpy -i https://pypi.tuna.tsinghua.edu.cn/simple
   2. 创建工程在E:\java物联网\fake_data文件夹下创建fake_device.py。



## 可视化验证：

1. 打开EMQX控制台：

   - 访问http://localhost:18083(damin,public).
   - 点击左侧菜单“主题监控”或“客户端”。
   - 查看env/monitor/data的主题

2. 配置InfluxDB:

   - 访问http://localhost:8086
   - 账号密码如docker-compose里面的
   - 创建一个Bucket叫iot_data

   修改密码：

   1. ​	进入容器docker exec -it influxdb /bin/sh
   2. influx user password --name admin --password '11111111'



## 编写Java后端

1. 新建Spring Boot项目
2. 引入依赖：
   - spring-boot-starter-web
   - mqtt-client(或者用Eclipse Paho)
   - influxdb-java（官方客户端）
3. 写代码逻辑：
   - 订阅：java程序连接本地localhost:1883，订阅env/monitor/data主题。
   - 处理：收到消息后，解析JSON。
   - 存储：把解析出的温度、湿度写入本地的InfluxDB。

