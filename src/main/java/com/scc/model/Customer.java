package com.scc.model;

public record Customer(
        int customer_id,
        String name,
        String contact,
        String address,
        double deposit_balance,
        String registration_date
) {}