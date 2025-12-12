package com.example.laundry_app;

public class Customer {
    private long id;
    private String name;
    private String address;
    private String phone;

    // Buat constructor
    public Customer(long id, String name, String address, String phone) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    // Buat getter
    public long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
}