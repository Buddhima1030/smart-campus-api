package com.smartcampus.model;

import java.util.UUID;

public class SensorReading {

    private String id;
    private double value;
    private String timestamp;

    public SensorReading() {}

    public SensorReading(double value) {
        this.id = UUID.randomUUID().toString();
        this.value = value;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    public String getId() { return id; }
    public double getValue() { return value; }
    public String getTimestamp() { return timestamp; }
}