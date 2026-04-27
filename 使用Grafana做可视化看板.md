# 使用Grafana做可视化看板

​       **datetime：2026/4/27     11：09**

## 一、登录与连接数据源

1. 访问Grafana
   - 打开浏览器访问：http://localhost:3000
   - 默认账号：admin
   - 默认密码：admin
2. 添加数据源
   - 点击左侧菜单的Connections中的Data sources
   - 点击Add data source
   - 找到InfluxDB
3. 配置连接信息
   1. URL:
   2. Auth
   3. Version：选择Flux
   4. Organization：myorg
   5. Token：mEAZ0p4mFmKJ0eFN_u0YQ6qMBojkVP4hRgGLatqFASBR-3calYQ5fKq29qMZgwMEuJJbd0CFclLsJFsAMwuPcQ==
   6. Default Bucket：iot_bucket
4. 保存并测试



## 二：创建看板

1. 新建仪表盘

   1. 点击Dashboards
   2. 点击New dashboard
   3. 添加Panel

2. 配置查询

   1. 点击右侧中的配置
   2. Data source：influxDB
   3. Query Language：确保是Flux
   4. 在代码框中输入：

   `from(bucket: "iot_bucket")  `

   `|> range(start: v.timeRangeStart, stop: v.timeRangeStop)    |> filter(fn: (r) => r["_measurement"] == "sensor_data")  |> filter(fn: (r) => r["_field"] == "temperature")  |> filter(fn: (r) => r["device_id"] == "dev_001")  |> aggregateWindow(every: v.windowPeriod, fn: mean, createEmpty: false)  |> yield(name: "mean")`

3. 可视化设置

   - 在右侧的Panel options中：
     - Title：设备温度监控
     - Description：实时温度变化

4. 应用：

   1. 点击右上角的Apply