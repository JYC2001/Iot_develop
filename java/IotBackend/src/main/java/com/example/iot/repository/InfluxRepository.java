package com.example.iot.repository;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Repository
public class InfluxRepository {

    private InfluxDBClient influxDBClient;
    //1、创建一个有界队列，防止内存溢出（容量10000）
    private final BlockingQueue<Point> pointQueue = new LinkedBlockingQueue<>(10000);

    //2、创建后台写入线程池
    private final ExecutorService writerExecutor = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder().setNameFormat("influx-writer-%d").build()
    );

    private volatile boolean running = true;

    public InfluxRepository(InfluxDBClient influxDBClient) {
        this.influxDBClient = influxDBClient;
    }

    @PostConstruct
    public void startBatchWriter(){
        //启动后台线程，不断从队列取数据写入
        writerExecutor.submit(this::batchWriteLoop);
        System.out.println("异步批量写入线程已启动");
    }

    private void batchWriteLoop() {
        List<Point> batch = new ArrayList<>(50); //批次大小50

        while(running){
            try {
                // 从队列取数据，如果队列空了，等待1秒（用于触发定时写入）
                Point point = pointQueue.poll(1, TimeUnit.SECONDS);
                if(point == null){
                    batch.add(point);
                    // 如果攒够了50条，或者队列里还有很多，立即写入
                    if(batch.size() >= 50 || pointQueue.size() >100){
                        flushBatch(batch);
                    }
                } else{
                    // 超时未取到数据，如果批次里也有残留数据，也写入（防止数据积压在内存）
                    if(!batch.isEmpty()){
                        flushBatch(batch);
                    }
                }
            } catch(InterruptedException e){
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void flushBatch(List<Point> batch){
        try{
            //一次性写入一批数据
            influxDBClient.getWriteApi().writePoints(batch);
            System.out.println("批量写入成功：" + batch.size() + " 条记录");
            batch.clear();
        } catch(Exception e){
            System.err.println(" 批量写入失败：" + e.getMessage());
            // 生产环境这里需要更复杂的重试或降级逻辑
        }
    }

    //对外的写入方法：只负责扔进队列，瞬间返回
    public void saveTelemetryAsync(String deviceId, Map<String, Object> fields){
        if(pointQueue.remainingCapacity() == 0){
            System.err.println(" 写入队列已满，丢弃数据！");
            return; // 背压机制：队列满了直接丢弃，保护系统不崩
        }

        Point point = Point.measurement("sensor_data")
                .addTag("device_id", deviceId)
                .addField("temperature", ((Number)fields.get("temperature")).doubleValue())
                .addField("humidity", ((Number)fields.get("humidity")).doubleValue())
                .time(Instant.now(), WritePrecision.NS);

        pointQueue.offer(point);
    }

    @PreDestroy
    public void stop(){
        running = false;
        writerExecutor.shutdown();
        try{
            writerExecutor.awaitTermination(5, TimeUnit.SECONDS);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }
}