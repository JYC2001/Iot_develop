package com.example.iot.service;

import java.util.Map;

public interface TelemetryService {
    /**
     * 处理并存储遥测数据
     * @param deviceId 设备ID
     * @param data 数据内容（包含温度、湿度等）
     */
    void processAndStore(String deviceId, Map<String, Object> data);
}