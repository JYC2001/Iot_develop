package com.example.iot.mqtt;


import com.example.iot.proto.SensorProtos; // 1. 导入生成的 Protobuf 类
import com.example.iot.repository.InfluxRepository;
import com.google.protobuf.InvalidProtocolBufferException; // 2. 导入 Protobuf 异常类
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class MqttMessageHandler implements IMqttMessageListener {
    private final InfluxRepository influxRepository;


    // 边缘缓存：记录每个设备上一次上报的有效温度
    private final Map<String, Double> lastKnownTemp = new ConcurrentHashMap<>();


    public MqttMessageHandler(InfluxRepository influxRepository) {
        this.influxRepository = influxRepository;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // 1. 直接获取二进制 payload，不要转成 String
        byte[] payload = message.getPayload();

        try {
            // 2. 使用 Protobuf 解析二进制数据
            // 这一步如果数据格式不对，会抛出 InvalidProtocolBufferException
            SensorProtos.SensorData sensorData = SensorProtos.SensorData.parseFrom(payload);

            String deviceId = sensorData.getDeviceId();
            SensorProtos.SensorData.Metrics metricsProto = sensorData.getMetrics();

            if (deviceId != null && metricsProto != null) {
                // 3. 提取数值
                float currentTemp = metricsProto.getTemperature();
                float currentHum = metricsProto.getHumidity();

                // --- 边缘过滤逻辑开始 (保持不变) ---
                Double lastTemp = lastKnownTemp.get(deviceId);

                // 死区策略：如果温度变化小于 0.5 度，认为是噪声，直接丢弃
                if (lastTemp != null && Math.abs(currentTemp - lastTemp) < 0.5) {
                    System.out.println("🤫 边缘过滤：温度变化微小 (" + currentTemp + "), 忽略 " + deviceId);
                    return;
                }

                // 更新缓存
                lastKnownTemp.put(deviceId, (double) currentTemp);
                // --- 边缘过滤逻辑结束 ---

                // 4. 构建 Map 传给 Service 层 (为了复用现有的异步写入逻辑)
                Map<String, Object> metricsMap = new HashMap<>();
                metricsMap.put("temperature", currentTemp);
                metricsMap.put("humidity", currentHum);

                // 5. 调用异步写入
                influxRepository.saveTelemetryAsync(deviceId, metricsMap);

                System.out.println("✅ 处理成功: " + deviceId + " -> " + currentTemp + "°C");
            }

        } catch (InvalidProtocolBufferException e) {
            // 专门捕获 Protobuf 解析错误
            System.err.println("❌ Protobuf 解析失败: 数据格式不匹配，请检查 Python 端是否发送了 Protobuf 数据");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ 消息处理异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}