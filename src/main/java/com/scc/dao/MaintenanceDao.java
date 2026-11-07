package com.scc.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceDao {
    public record RepairCylinder(
            int cylinderId,
            String size,
            String condition,
            String status,
            String inspectionType,
            String findings
    ) {}
    public boolean recordMaintenance(
            int cylinderId,
            int userId,
            String type,
            String status) {

        String sql = """
                INSERT INTO maintenance_records
                (cylinder_id, user_id, type, status)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cylinderId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, type);
            pstmt.setString(4, status);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error recording maintenance: " + e.getMessage());
            return false;
        }
    }

    public void viewAllMaintenanceRecords() {

        String sql = """
            SELECT
                m.maintenance_id,
                m.cylinder_id,
                c.size,
                u.username,
                m.date,
                m.type,
                m.status
            FROM maintenance_records m
            JOIN lpg_cylinders c
                ON m.cylinder_id = c.cylinder_id
            JOIN users u
                ON m.user_id = u.user_id
            ORDER BY m.maintenance_id DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n===== MAINTENANCE RECORDS =====");

            System.out.printf("%-5s | %-10s | %-6s | %-12s | %-15s | %-20s%n",
                    "ID", "CYLINDER", "SIZE", "STAFF", "TYPE", "STATUS");

            System.out.println("--------------------------------------------------------------------------");

            boolean hasRecords = false;

            while (rs.next()) {
                hasRecords = true;

                System.out.printf("%-5d | %-10d | %-6s | %-12s | %-15s | %-20s%n",
                        rs.getInt("maintenance_id"),
                        rs.getInt("cylinder_id"),
                        rs.getString("size"),
                        rs.getString("username"),
                        rs.getString("type"),
                        rs.getString("status"));
            }

            if (!hasRecords) {
                System.out.println("No maintenance records found.");
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving maintenance records: " + e.getMessage());
        }
    }

    public boolean hasInspectionRecord(int cylinderId) {

        String sql = """
        SELECT COUNT(*)
        FROM maintenance_records
        WHERE cylinder_id = ?
        AND type <> 'REPAIR'
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cylinderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.out.println("Error checking inspection record: " + e.getMessage());
            return false;
        }
    }

    public List<RepairCylinder> getCylindersForRepair() {

        List<RepairCylinder> cylinders = new ArrayList<>();

        String sql = """
        SELECT
            c.cylinder_id,
            c.size,
            c.condition,
            c.status,
            m.type AS inspection_type,
            m.status AS findings
        FROM lpg_cylinders c
        JOIN maintenance_records m
            ON c.cylinder_id = m.cylinder_id
        WHERE c.condition = 'NEEDS REPAIR'
          AND c.status = 'MAINTENANCE'
          AND m.type <> 'REPAIR'
          AND m.maintenance_id = (
              SELECT MAX(m2.maintenance_id)
              FROM maintenance_records m2
              WHERE m2.cylinder_id = c.cylinder_id
                AND m2.type <> 'REPAIR'
          )
          AND m.status = 'REPAIR REQUIRED'
        ORDER BY c.cylinder_id ASC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {

                cylinders.add(new RepairCylinder(
                        rs.getInt("cylinder_id"),
                        rs.getString("size"),
                        rs.getString("condition"),
                        rs.getString("status"),
                        rs.getString("inspection_type"),
                        rs.getString("findings")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving repair cylinders: " + e.getMessage());
        }

        return cylinders;
    }

}