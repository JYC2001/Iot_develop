package com.example.iot.repository;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Map;

@Repository
public class InfluxRepository {

    private final InfluxDBClient influxDBClient;

    // 通过构造函数注入
    public InfluxRepository(InfluxDBClient influxDBClient) {
        this.influxDBClient = influxDBClient;
    }

    public void saveTelemetry(String deviceId, Map<String, Object> fields) {
        Point point = Point.measurement("sensor_data")
                .addTag("device_id", deviceId)
                .addField("temperature", ((Number) fields.get("temperature")).doubleValue())
                .addField("humidity", ((Number) fields.get("humidity")).doubleValue())
                .time(Instant.now(), WritePrecision.NS);

        // 异步写入，性能更高
        influxDBClient.getWriteApi().writePoint(point);
    }
}