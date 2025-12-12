package com.example.laundry_app;

public class Transaction {
    private long id;
    private String customerName;
    private String serviceType;
    private double weight;
    private double totalPrice;
    private String transactionDate;
    private String status;

    public Transaction(long id, String customerName, String serviceType, double weight, double totalPrice, String transactionDate, String status) {
        this.id = id;
        this.customerName = customerName;
        this.serviceType = serviceType;
        this.weight = weight;
        this.totalPrice = totalPrice;
        this.transactionDate = transactionDate;
        this.status = status;
    }

    public long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getServiceType() { return serviceType; }
    public double getWeight() { return weight; }
    public double getTotalPrice() { return totalPrice; }
    public String getTransactionDate() { return transactionDate; }
    public String getStatus() { return status; }
}