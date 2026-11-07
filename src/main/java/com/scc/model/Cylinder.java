package com.scc.model;

public record Cylinder(
        int cylinder_id,
        Integer customer_id,
        String size,
        double price,
        String condition,
        String status,
        double deposit_paid
) {}