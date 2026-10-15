package com.scc.model;

public record User(int user_id, String username, String name, String password, String contact, String role, String status) {
    @Override
    public String toString(){
        return String.format("[%d] %s (%s) - Status: %s", user_id, username, role, status);
    }
}
