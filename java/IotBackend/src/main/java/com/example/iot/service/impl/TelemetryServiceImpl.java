package com.example.iot.service.impl;

import com.example.iot.repository.InfluxRepository;
import com.example.iot.service.TelemetryService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Service
public class TelemetryServiceImpl implements TelemetryService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryServiceImpl.class);
    private final InfluxRepository influxRepository;

    public TelemetryServiceImpl(InfluxRepository influxRepository) {
        this.influxRepository = influxRepository;
    }

    @Override
    public void processAndStore(String deviceId, Map<String, Object> data) {
        // 1. 数据清洗逻辑
        double temp = ((Number) data.get("temperature")).doubleValue();

        // 模拟异常数据过滤：如果温度大于 60 度，认为是错误的传感器读数，丢弃
        if (temp > 60.0) {
            log.warn("⚠️ 异常数据过滤: 设备 {} 温度过高 {}", deviceId, temp);
            return;
        }

        // 2. 存储数据
        log.info("💾 正在保存数据: {} -> {}", deviceId, data);
        influxRepository.saveTelemetry(deviceId, data);
    }
}