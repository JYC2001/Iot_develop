package com.example.iot.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class MqttSubscriber {

    private final MqttClient mqttClient;
    private final MqttMessageHandler messageHandler;

    @Value("${mqtt.topic}")
    private String topic;

    // 注入 MQTT 客户端和消息处理器
    public MqttSubscriber(MqttClient mqttClient, MqttMessageHandler messageHandler) {
        this.mqttClient = mqttClient;
        this.messageHandler = messageHandler;
    }

    @PostConstruct
    public void subscribe() throws MqttException {
        // 订阅主题，并绑定消息处理器
        mqttClient.subscribe(topic, 1, messageHandler);
        System.out.println("✅ 已订阅主题: " + topic);
    }
}