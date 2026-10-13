package com.scc.model;

public record User(int id, String username, String name, String password, int contact, String role, String status) {
    @Override
    public String toString(){
        return String.format("[%d] %s (%s) - Status: %s", id, username, role, status);
    }
}
