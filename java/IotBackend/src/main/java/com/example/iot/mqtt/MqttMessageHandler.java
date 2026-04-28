package com.example.iot.mqtt;

import com.alibaba.fastjson.JSON;
import com.example.iot.repository.InfluxRepository;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class MqttMessageHandler implements IMqttMessageListener {
    private final InfluxRepository influxRepository;

    //模拟边缘缓存：记录每个设备上一次上报的有效温度
    private final Map<String, Double> lastKonwnTemp = new ConcurrentHashMap<>();

    public MqttMessageHandler(InfluxRepository influxRepository) {
        this.influxRepository = influxRepository;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());

        try {
            Map<String, Object> jsonMap = JSON.parseObject(payload, Map.class);
            String deviceId = (String) jsonMap.get("deviceId");
            Map<String, Object> metrics = (Map<String, Object>) jsonMap.get("metrics");

            if (deviceId != null && metrics != null) {
                // --- 边缘过滤逻辑开始 ---
                double currentTemp = ((Number) metrics.get("temperature")).doubleValue();
                Double lastTemp = lastKonwnTemp.get(deviceId);

                //死区策略：如果温度变化小于0.5度，认为是噪声，直接丢弃，不入库
                if(lastTemp != null && Math.abs(currentTemp - lastTemp) < 0.5) {
                    System.out.println(" 边缘过滤：温度变化微小，忽略 " + deviceId);
                    return;
                }

                // 更新缓存
                lastKonwnTemp.put(deviceId, currentTemp);
                // --- 边缘过滤逻辑结束 ---

                // 调用异步写入
                influxRepository.saveTelemetryAsync(deviceId, metrics);
            }
        } catch (Exception e) {
            System.err.println(" 消息解析失败：" + e.getMessage());
        }
    }
}