package com.example.laundry_app;

public class Service {
    private long id;
    private String type;
    private double pricePerKg;
    private int estimatedDays;

    public Service(long id, String type, double pricePerKg, int estimatedDays) {
        this.id = id;
        this.type = type;
        this.pricePerKg = pricePerKg;
        this.estimatedDays = estimatedDays;
    }

    public long getId() { return id; }
    public String getType() { return type; }
    public double getPricePerKg() { return pricePerKg; }
    public int getEstimatedDays() { return estimatedDays; }
}