package com.scc.model;

public record Transaction(
        int transaction_id,
        int customer_id,
        String customer_name,
        int user_id,
        String staff_name,
        String transaction_type,
        double amount,
        double deposit_amount,
        double exchange_credit,
        String date
) {}