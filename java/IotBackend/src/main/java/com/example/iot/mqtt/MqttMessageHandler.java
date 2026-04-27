package com.example.iot.mqtt;

import com.alibaba.fastjson.JSON;
import com.example.iot.service.TelemetryService;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MqttMessageHandler implements IMqttMessageListener {

    private final TelemetryService telemetryService;

    // 注入业务层
    public MqttMessageHandler(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        System.out.println("📩 收到消息: " + payload);

        try {
            // 1. 解析 JSON
            Map<String, Object> jsonMap = JSON.parseObject(payload, Map.class);
            String deviceId = (String) jsonMap.get("deviceId");
            Map<String, Object> metrics = (Map<String, Object>) jsonMap.get("metrics");

            if (deviceId != null && metrics != null) {
                // 2. 调用业务层处理
                telemetryService.processAndStore(deviceId, metrics);
            }
        } catch (Exception e) {
            System.err.println("❌ 消息解析失败: " + e.getMessage());
        }
    }
}