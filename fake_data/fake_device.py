import paho.mqtt.client as mqtt
import json
import time
import random

# 配置 MQTT 服务器地址
MQTT_BROKER = "localhost"
MQTT_PORT = 1883
TOPIC = "env/monitor/data"

# 模拟设备ID
DEVICE_ID = "dev_001"

# 定义一个全局变量来标记连接状态
connected = False

def on_connect(client, userdata, flags, rc):
    global connected
    if rc == 0:
        print("✅ 设备已连接到本地 MQTT 服务器")
        connected = True  # 标记为已连接
    else:
        print(f"❌ 连接失败，代码: {rc}")

if __name__ == "__main__":
    client = mqtt.Client()
    client.on_connect = on_connect
    # 发起连接
    client.connect(MQTT_BROKER, MQTT_PORT, 60)
    
    # 【关键修改】启动网络循环，并等待连接成功
    client.loop_start() 
    
    print("⏳ 正在等待连接...")
    # 死循环等待，直到 connected 变为 True
    while not connected:
        time.sleep(0.1)

    print(f"🚀 开始模拟设备 {DEVICE_ID} 发送数据...")

    try:
        while True:
            # 1. 模拟生成数据
            temp = round(random.uniform(20.0, 30.0), 2)
            hum = round(random.uniform(40.0, 60.0), 2)

            # 2. 封装 JSON
            payload = {
                "deviceId": DEVICE_ID,
                "timestamp": int(time.time() * 1000),
                "metrics": {
                    "temperature": temp,
                    "humidity": hum
                }
            }

            # 3. 发送数据
            # retain=False, qos=0
            result = client.publish(TOPIC, json.dumps(payload))
            
            # 检查发送状态
            if result.rc == 0:
                print(f"📤 发送成功: 温度 {temp}, 湿度 {hum}")
            else:
                print(f"⚠️ 发送失败: {result.rc}")

            time.sleep(2)
            
    except KeyboardInterrupt:
        print("\n🛑 模拟结束")
        client.loop_stop()
        client.disconnect()