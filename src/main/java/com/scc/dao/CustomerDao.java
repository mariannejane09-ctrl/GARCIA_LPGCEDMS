
package com.scc.dao;

import com.scc.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    // Register a new customer
    public boolean registerCustomer(String name, String contact, String address) {

        String sql = """
                INSERT INTO customers (name, contact, address)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, contact);
            pstmt.setString(3, address);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error registering customer: " + e.getMessage());
            return false;
        }
    }

    // Retrieve all registered customers
    public List<Customer> getAllCustomers() {

        List<Customer> customers = new ArrayList<>();

        String sql = "SELECT * FROM customers ORDER BY customer_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {

                Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("contact"),
                        rs.getString("address"),
                        rs.getDouble("deposit_balance"),
                        rs.getString("registration_date")
                );

                customers.add(customer);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving customers: " + e.getMessage());
        }

        return customers;
    }

    // Retrieve customer using Customer ID
    public Customer getCustomerById(int customerId) {

        String sql = """
            SELECT * FROM customers
            WHERE customer_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    return new Customer(
                            rs.getInt("customer_id"),
                            rs.getString("name"),
                            rs.getString("contact"),
                            rs.getString("address"),
                            rs.getDouble("deposit_balance"),
                            rs.getString("registration_date")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving customer: " + e.getMessage());
        }

        return null;
    }
}
