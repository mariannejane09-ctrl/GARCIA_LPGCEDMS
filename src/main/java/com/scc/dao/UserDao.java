package com.scc.dao;

import com.scc.model.User;

import java.sql.*;

public class UserDao {

    public UserDao(){
        try(Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement()){
            defaultAdminGenerator(conn);
        } catch (SQLException e){
                System.err.println("Error seeding default admin: " + e.getMessage());
        }
    }
    public void defaultAdminGenerator(Connection conn){
        String checkSql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        String insertSql = "INSERT INTO users(username, name, password, contact, role, status) VALUES('admin', 'admin', 'admin123', '12345678', 'ADMIN', 'APPROVED')";

        try(Statement stat = conn.createStatement();
            ResultSet rs = stat.executeQuery(checkSql)){
            if(rs.next() && rs.getInt(1) == 0) {
                stat.executeUpdate(insertSql);
                System.out.println("-> Default Admin created (Username: admin | Password: admin123)");
            }
        } catch (SQLException e) {
            System.err.println("Error seeding default admin: " + e.getMessage());
        }
    }

    public boolean registerUser (String username, String name, String password, String contact, String role){

        String sql = "INSERT INTO users(username, name, password, contact, role, status) VALUES (?, ?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, username);
            pstmt.setString(2, name);
            pstmt.setString(3, password);
            pstmt.setString(4, contact);
            pstmt.setString(5, role.toUpperCase());
            pstmt.executeUpdate();
            System.out.println("\nSuccessful Registration! Waiting for Approval\n");
            return true;

        } catch (SQLException e){
            System.err.println("Failed: " + e.getMessage());
            return false;
        }
    }

    public User login (String username, String password){
        String sql = "SELECT user_id, username, name, password, contact, role, status FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("PENDING".equalsIgnoreCase(status)) {
                        System.out.println(" - > Account is pending, waiting for Approval");
                        return null;
                    }
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("name"),
                            rs.getString("password"),
                            rs.getString("contact"),
                            rs.getString("role"),
                            status
                    );
                } else {
                    System.out.println(" - > Invalid username or password");
                }
            }
            } catch (SQLException e) {
                System.err.println("Error during login: " + e.getMessage());
            }
            return null;
        }
    }

