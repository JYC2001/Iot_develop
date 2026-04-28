import paho.mqtt.client as mqtt
import time
import random
import sensor_pb2  # 导入刚才生成的文件

# 配置 MQTT
MQTT_BROKER = "localhost"
MQTT_PORT = 1883
TOPIC = "env/monitor/data"
DEVICE_ID = "dev_001"

connected = False

def on_connect(client, userdata, flags, rc):
    global connected
    if rc == 0:
        print("✅ 设备已连接到本地 MQTT 服务器")
        connected = True
    else:
        print(f"❌ 连接失败，代码: {rc}")

if __name__ == "__main__":
    client = mqtt.Client()
    client.on_connect = on_connect
    client.connect(MQTT_BROKER, MQTT_PORT, 60)

    client.loop_start()

    print("⏳ 正在等待连接...")
    while not connected:
        time.sleep(0.1)

    print(f"🚀 开始模拟设备 {DEVICE_ID} 发送 Protobuf 数据...")

    try:
        while True:
            # 1. 使用 Protobuf 对象构建数据
            data = sensor_pb2.SensorData()
            data.device_id = DEVICE_ID
            data.timestamp = int(time.time() * 1000)
            data.metrics.temperature = round(random.uniform(20.0, 30.0), 2)
            data.metrics.humidity = round(random.uniform(40.0, 60.0), 2)

            # 2. 序列化成二进制
            payload = data.SerializeToString()

            # 3. 发送二进制数据
            result = client.publish(TOPIC, payload)

            if result.rc == 0:
                print(f"📤 发送成功 (Protobuf): 温度 {data.metrics.temperature}, 湿度 {data.metrics.humidity}")
                # 打印一下数据大小，让你看看 Protobuf 有多省流量
                print(f"   💾 数据大小: {len(payload)} 字节")
            else:
                print(f"⚠️ 发送失败: {result.rc}")

            time.sleep(0.0001)
            
    except KeyboardInterrupt:
        print("\n🛑 模拟结束")
        client.loop_stop()
        client.disconnect()