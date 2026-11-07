
package com.scc.dao;

import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.scc.model.Transaction;
import java.util.ArrayList;


public class TransactionDao {

    public boolean processSale(
            int customerId,
            int userId,
            List<Integer> cylinderIds) {

        if (cylinderIds == null || cylinderIds.isEmpty()) {
            System.out.println("No cylinders selected.");
            return false;
        }

        // Prevent selecting the same cylinder twice
        Set<Integer> uniqueIds = new HashSet<>(cylinderIds);

        if (uniqueIds.size() != cylinderIds.size()) {
            System.out.println("Duplicate cylinder IDs are not allowed.");
            return false;
        }

        String customerSql =
                "SELECT customer_id FROM customers WHERE customer_id = ?";

        String cylinderSql = """
                SELECT price, size, condition
                FROM lpg_cylinders
                WHERE cylinder_id = ?
                AND status = 'AVAILABLE'
                """;

        String transactionSql = """
                INSERT INTO transactions
                (customer_id, user_id, transaction_type,
                 amount, deposit_amount, exchange_credit)
                VALUES (?, ?, 'SALE', ?, ?, 0)
                """;

        String detailSql = """
                INSERT INTO transaction_details
                (transaction_id, cylinder_id, detail_type,
                 unit_price, quantity)
                VALUES (?, ?, 'SALE', ?, 1)
                """;

        String updateCylinderSql = """
                UPDATE lpg_cylinders
                SET customer_id = ?,
                    status = 'DELIVERED',
                    deposit_paid = 300
                WHERE cylinder_id = ? AND status = 'AVAILABLE'
                """;

        String updateCustomerSql = """
                UPDATE customers
                SET deposit_balance = deposit_balance + ?
                WHERE customer_id = ?
                """;

        String logSql = """
                INSERT INTO activity_logs
                (user_id, action_type, description)
                VALUES (?, 'LPG_SALE', ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                // 1. Verify customer
                try (PreparedStatement pstmt =
                             conn.prepareStatement(customerSql)) {

                    pstmt.setInt(1, customerId);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Customer not found.");
                        }
                    }
                }

                // 2. Calculate sale amount and deposit
                double lpgTotal = 0;
                double depositTotal = 0;

                for (int cylinderId : cylinderIds) {

                    try (PreparedStatement pstmt =
                                 conn.prepareStatement(cylinderSql)) {

                        pstmt.setInt(1, cylinderId);

                        try (ResultSet rs = pstmt.executeQuery()) {

                            if (!rs.next()) {
                                throw new SQLException(
                                        "Cylinder ID " + cylinderId +
                                                " is unavailable."
                                );
                            }

                            if (!"GOOD".equalsIgnoreCase(
                                    rs.getString("condition"))) {

                                throw new SQLException(
                                        "Cylinder ID " + cylinderId +
                                                " is not in good condition."
                                );
                            }

                            lpgTotal += rs.getDouble("price");
                            depositTotal += 300;
                        }
                    }
                }

                double totalAmount = lpgTotal + depositTotal;

                // 3. Save main transaction
                int transactionId;

                try (PreparedStatement pstmt =
                             conn.prepareStatement(
                                     transactionSql,
                                     Statement.RETURN_GENERATED_KEYS)) {

                    pstmt.setInt(1, customerId);
                    pstmt.setInt(2, userId);
                    pstmt.setDouble(3, totalAmount);
                    pstmt.setDouble(4, depositTotal);

                    pstmt.executeUpdate();

                    try (ResultSet keys = pstmt.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Could not generate transaction ID."
                            );
                        }

                        transactionId = keys.getInt(1);
                    }
                }

                // 4. Save details and update each cylinder
                for (int cylinderId : cylinderIds) {

                    double price;

                    try (PreparedStatement pstmt =
                                 conn.prepareStatement(cylinderSql)) {

                        pstmt.setInt(1, cylinderId);

                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (!rs.next()) {
                                throw new SQLException(
                                        "Cylinder is no longer available."
                                );
                            }

                            price = rs.getDouble("price");
                        }
                    }

                    try (PreparedStatement pstmt =
                                 conn.prepareStatement(detailSql)) {

                        pstmt.setInt(1, transactionId);
                        pstmt.setInt(2, cylinderId);
                        pstmt.setDouble(3, price);
                        pstmt.executeUpdate();
                    }

                    try (PreparedStatement pstmt =
                                 conn.prepareStatement(updateCylinderSql)) {

                        pstmt.setInt(1, customerId);
                        pstmt.setInt(2, cylinderId);

                        if (pstmt.executeUpdate() != 1) {
                            throw new SQLException(
                                    "Could not update cylinder " +
                                            cylinderId
                            );
                        }
                    }
                }

                // 5. Update customer deposit balance
                try (PreparedStatement pstmt =
                             conn.prepareStatement(updateCustomerSql)) {

                    pstmt.setDouble(1, depositTotal);
                    pstmt.setInt(2, customerId);
                    pstmt.executeUpdate();
                }

                // 6. Record activity log
                String description =
                        "Staff ID " + userId +
                                " processed an LPG sale for Customer ID " +
                                customerId + ". Transaction ID: " +
                                transactionId + ". Total: ₱" + totalAmount;

                try (PreparedStatement pstmt =
                             conn.prepareStatement(logSql)) {

                    pstmt.setInt(1, userId);
                    pstmt.setString(2, description);
                    pstmt.executeUpdate();
                }

                conn.commit();

                System.out.println("LPG sale successfully processed!");
                System.out.println("Transaction ID: " + transactionId);
                System.out.println("LPG Total: ₱" + lpgTotal);
                System.out.println("Deposit: ₱" + depositTotal);
                System.out.println("Total Amount: ₱" + totalAmount);

                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Sale failed: " + e.getMessage());
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            return false;
        }
    }

    // Retrieve all transactions
    public List<Transaction> getAllTransactions() {

        List<Transaction> transactions = new ArrayList<>();

        String sql = """
            SELECT
                t.transaction_id,
                t.customer_id,
                c.name AS customer_name,
                t.user_id,
                u.name AS staff_name,
                t.transaction_type,
                t.amount,
                t.deposit_amount,
                t.exchange_credit,
                t.date
            FROM transactions t
            JOIN customers c
                ON t.customer_id = c.customer_id
            JOIN users u
                ON t.user_id = u.user_id
            ORDER BY t.transaction_id DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {

                Transaction transaction = new Transaction(
                        rs.getInt("transaction_id"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getInt("user_id"),
                        rs.getString("staff_name"),
                        rs.getString("transaction_type"),
                        rs.getDouble("amount"),
                        rs.getDouble("deposit_amount"),
                        rs.getDouble("exchange_credit"),
                        rs.getString("date")
                );

                transactions.add(transaction);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving transactions: "
                    + e.getMessage());
        }

        return transactions;
    }

    public boolean processExchange(
            int customerId,
            int userId,
            int oldCylinderId,
            int newCylinderId) {

        String findOldCylinder = """
            SELECT condition, deposit_paid
            FROM lpg_cylinders
            WHERE cylinder_id = ?
            AND customer_id = ?
            AND status = 'DELIVERED'
            """;

        String findNewCylinder = """
            SELECT cylinder_id, size, price
            FROM lpg_cylinders
            WHERE cylinder_id = ?
            AND status = 'AVAILABLE'
            AND condition = 'GOOD'
            """;

        String insertTransaction = """
            INSERT INTO transactions
            (customer_id, user_id, transaction_type,
             amount, deposit_amount, exchange_credit)
            VALUES (?, ?, 'EXCHANGE', ?, 0, 100)
            """;

        String insertDetail = """
            INSERT INTO transaction_details
            (transaction_id, cylinder_id, detail_type,
             unit_price, quantity, exchange_source)
            VALUES (?, ?, ?, ?, 1, 'BUSINESS')
            """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                // 1. Validate the old cylinder.

                String oldCondition;
                double oldDepositPaid;

                try (PreparedStatement pstmt =
                             conn.prepareStatement(findOldCylinder)) {

                    pstmt.setInt(1, oldCylinderId);
                    pstmt.setInt(2, customerId);

                    try (ResultSet rs = pstmt.executeQuery()) {

                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }

                        oldCondition = rs.getString("condition");
                        oldDepositPaid = rs.getDouble("deposit_paid");
                    }
                }

                // 2. Validate the replacement cylinder.

                double replacementPrice;

                try (PreparedStatement pstmt =
                             conn.prepareStatement(findNewCylinder)) {

                    pstmt.setInt(1, newCylinderId);

                    try (ResultSet rs = pstmt.executeQuery()) {

                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }

                        replacementPrice = rs.getDouble("price");
                    }
                }

                // 3. Calculate exchange payment.

                double exchangeCredit = 100.00;
                double amount = replacementPrice - exchangeCredit;

                // 4. Create transaction.

                int transactionId;

                try (PreparedStatement pstmt = conn.prepareStatement(
                        insertTransaction)) {

                    pstmt.setInt(1, customerId);
                    pstmt.setInt(2, userId);
                    pstmt.setDouble(3, amount);

                    pstmt.executeUpdate();
                }

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(
                             "SELECT last_insert_rowid()")) {

                    rs.next();
                    transactionId = rs.getInt(1);
                }

                // 5. Record the old cylinder.

                try (PreparedStatement pstmt =
                             conn.prepareStatement(insertDetail)) {

                    pstmt.setInt(1, transactionId);
                    pstmt.setInt(2, oldCylinderId);
                    pstmt.setString(3, "EXCHANGE_RETURN");
                    pstmt.setDouble(4, 0);

                    pstmt.executeUpdate();
                }

                // 6. Record the replacement cylinder.

                try (PreparedStatement pstmt =
                             conn.prepareStatement(insertDetail)) {

                    pstmt.setInt(1, transactionId);
                    pstmt.setInt(2, newCylinderId);
                    pstmt.setString(3, "EXCHANGE_ISSUE");
                    pstmt.setDouble(4, replacementPrice);

                    pstmt.executeUpdate();
                }

                // 7. Return old cylinder to inventory.

                String oldStatus = oldCondition.equalsIgnoreCase("GOOD")
                        ? "AVAILABLE"
                        : "MAINTENANCE";

                String updateOldCylinder = """
                    UPDATE lpg_cylinders
                    SET customer_id = NULL,
                        status = ?,
                        deposit_paid = 0
                    WHERE cylinder_id = ?
                    AND customer_id = ?
                    AND status = 'DELIVERED'
                    """;

                try (PreparedStatement pstmt =
                             conn.prepareStatement(updateOldCylinder)) {

                    pstmt.setString(1, oldStatus);
                    pstmt.setInt(2, oldCylinderId);
                    pstmt.setInt(3, customerId);

                    if (pstmt.executeUpdate() != 1) {
                        throw new SQLException("Old cylinder update failed.");
                    }
                }

                // 8. Assign replacement cylinder to customer.

                String updateNewCylinder = """
                    UPDATE lpg_cylinders
                    SET customer_id = ?,
                        status = 'DELIVERED',
                        deposit_paid = ?
                    WHERE cylinder_id = ?
                    AND status = 'AVAILABLE'
                    AND condition = 'GOOD'
                    """;

                try (PreparedStatement pstmt =
                             conn.prepareStatement(updateNewCylinder)) {

                    pstmt.setInt(1, customerId);
                    pstmt.setInt(2, newCylinderId);
                    pstmt.setInt(3, newCylinderId);

                    if (pstmt.executeUpdate() != 1) {
                        throw new SQLException(
                                "Replacement cylinder update failed.");
                    }
                }

                // 9. Record activity log.

                String logSql = """
                    INSERT INTO activity_logs
                    (user_id, action_type, description)
                    VALUES (?, ?, ?)
                    """;

                try (PreparedStatement pstmt =
                             conn.prepareStatement(logSql)) {

                    pstmt.setInt(1, userId);
                    pstmt.setString(2, "CYLINDER_EXCHANGE");

                    pstmt.setString(
                            3,
                            "Customer ID " + customerId
                                    + " exchanged cylinder "
                                    + oldCylinderId
                                    + " for cylinder "
                                    + newCylinderId
                    );

                    pstmt.executeUpdate();
                }

                // 10. Save everything.

                conn.commit();

                return true;

            } catch (SQLException e) {

                conn.rollback();
                throw e;

            } finally {

                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {

            System.err.println("Exchange error: " + e.getMessage());
            return false;
        }
    }
}
