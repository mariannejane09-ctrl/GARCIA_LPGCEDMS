package com.scc.dao;

import com.scc.model.Cylinder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CylinderDao {

    // Insert initial inventory if the table is empty
    public void initializeInventory() {

        String sql = """
                INSERT INTO lpg_cylinders
                (size, price, condition, status)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Check if inventory already exists
            String countSql = "SELECT COUNT(*) FROM lpg_cylinders";

            try (PreparedStatement countStmt = conn.prepareStatement(countSql);
                 ResultSet rs = countStmt.executeQuery()) {

                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Inventory already exists.");
                    return;
                }
            }

            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // S1 - 7kg (4 cylinders)
                for (int i = 0; i < 4; i++) {
                    pstmt.setString(1, "S1");
                    pstmt.setDouble(2, 600);
                    pstmt.setString(3, "GOOD");
                    pstmt.setString(4, "AVAILABLE");
                    pstmt.addBatch();
                }

                // S2 - 11kg (3 cylinders)
                for (int i = 0; i < 3; i++) {
                    pstmt.setString(1, "S2");
                    pstmt.setDouble(2, 850);
                    pstmt.setString(3, "GOOD");
                    pstmt.setString(4, "AVAILABLE");
                    pstmt.addBatch();
                }

                // S3 - 22kg (3 cylinders)
                for (int i = 0; i < 3; i++) {
                    pstmt.setString(1, "S3");
                    pstmt.setDouble(2, 1700);
                    pstmt.setString(3, "GOOD");
                    pstmt.setString(4, "AVAILABLE");
                    pstmt.addBatch();
                }

                pstmt.executeBatch();
                conn.commit();

                System.out.println("Initial inventory successfully added!");

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.err.println("Error initializing inventory: " + e.getMessage());
        }
    }

    // Retrieve available cylinders
    public List<Cylinder> getAvailableCylinders() {

        List<Cylinder> cylinders = new ArrayList<>();

        String sql = """
                SELECT * FROM lpg_cylinders
                WHERE status = 'AVAILABLE'
                ORDER BY cylinder_id ASC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {

                Cylinder cylinder = new Cylinder(
                        rs.getInt("cylinder_id"),
                        (Integer) rs.getObject("customer_id"),
                        rs.getString("size"),
                        rs.getDouble("price"),
                        rs.getString("condition"),
                        rs.getString("status"),
                        rs.getDouble("deposit_paid")
                );

                cylinders.add(cylinder);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving cylinders: " + e.getMessage());
        }

        return cylinders;
    }

    // Count available cylinders by size
    public int getAvailableCount(String size) {

        String sql = """
            SELECT COUNT(*)
            FROM lpg_cylinders
            WHERE size = ? AND status = 'AVAILABLE'
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, size);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error counting available cylinders: " + e.getMessage());
        }

        return 0;
    }

    // Retrieve available cylinders by LPG size
    public List<Cylinder> getAvailableCylindersBySize(String size) {

        List<Cylinder> cylinders = new ArrayList<>();

        String sql = """
            SELECT * FROM lpg_cylinders
            WHERE size = ? AND status = 'AVAILABLE'
            AND condition = 'GOOD'
            ORDER BY cylinder_id ASC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, size);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    cylinders.add(new Cylinder(
                            rs.getInt("cylinder_id"),
                            (Integer) rs.getObject("customer_id"),
                            rs.getString("size"),
                            rs.getDouble("price"),
                            rs.getString("condition"),
                            rs.getString("status"),
                            rs.getDouble("deposit_paid")
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving cylinders by size: "
                    + e.getMessage());
        }

        return cylinders;
    }

    public List<Cylinder> getCustomerCylinders(int customerId) {

        List<Cylinder> cylinders = new ArrayList<>();

        String sql = """
            SELECT *
            FROM lpg_cylinders
            WHERE customer_id = ?
            AND status = 'DELIVERED'
            ORDER BY cylinder_id
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    cylinders.add(new Cylinder(
                            rs.getInt("cylinder_id"),
                            rs.getInt("customer_id"),
                            rs.getString("size"),
                            rs.getDouble("price"),
                            rs.getString("condition"),
                            rs.getString("status"),
                            rs.getDouble("deposit_paid")
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving customer cylinders: "
                    + e.getMessage());
        }

        return cylinders;
    }
}