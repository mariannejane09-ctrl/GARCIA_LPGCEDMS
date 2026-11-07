
package com.scc.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {

        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    name TEXT NOT NULL,
                    password TEXT NOT NULL,
                    contact TEXT,
                    role TEXT NOT NULL,
                    status TEXT NOT NULL
                )
                """;

        String createActivityLogsTable = """
                CREATE TABLE IF NOT EXISTS activity_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    action_type TEXT NOT NULL,
                    description TEXT NOT NULL,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id)
                        REFERENCES users(user_id)
                        ON DELETE CASCADE
                )
                """;

        String createCustomersTable = """
                CREATE TABLE IF NOT EXISTS customers (
                    customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    contact TEXT,
                    address TEXT,
                    deposit_balance REAL DEFAULT 0,
                    registration_date DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """;

        String createLpgCylindersTable = """
                CREATE TABLE IF NOT EXISTS lpg_cylinders (
                     cylinder_id INTEGER PRIMARY KEY AUTOINCREMENT,
                     customer_id INTEGER,
                     size TEXT NOT NULL,
                     price REAL NOT NULL,
                     condition TEXT NOT NULL,
                     status TEXT NOT NULL,
                     deposit_paid REAL DEFAULT 0,
                     FOREIGN KEY (customer_id)
                         REFERENCES customers(customer_id)
                         ON DELETE SET NULL
                 )
                """;

        String createTransactionsTable = """
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id INTEGER NOT NULL,
                    user_id INTEGER NOT NULL,
                    transaction_type TEXT NOT NULL,
                    amount REAL NOT NULL,
                    deposit_amount REAL DEFAULT 0,
                    exchange_credit REAL DEFAULT 0,
                    date DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (customer_id)
                        REFERENCES customers(customer_id),
                    FOREIGN KEY (user_id)
                        REFERENCES users(user_id)
                )
                """;

        String createTransactionDetailsTable = """
                CREATE TABLE IF NOT EXISTS transaction_details (
                    detail_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    transaction_id INTEGER NOT NULL,
                    cylinder_id INTEGER,
                    detail_type TEXT NOT NULL,
                    unit_price REAL NOT NULL,
                    quantity INTEGER NOT NULL,
                    exchange_source TEXT,
                    external_brand TEXT,
                    FOREIGN KEY (transaction_id)
                        REFERENCES transactions(transaction_id),
                    FOREIGN KEY (cylinder_id)
                        REFERENCES lpg_cylinders(cylinder_id)
                        ON DELETE SET NULL
                )
                """;

        String createMaintenanceRecordsTable = """
                CREATE TABLE IF NOT EXISTS maintenance_records (
                    maintenance_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    cylinder_id INTEGER NOT NULL,
                    user_id INTEGER NOT NULL,
                    date DATETIME DEFAULT CURRENT_TIMESTAMP,
                    type TEXT NOT NULL,
                    status TEXT NOT NULL,
                    FOREIGN KEY (cylinder_id)
                        REFERENCES lpg_cylinders(cylinder_id),
                    FOREIGN KEY (user_id)
                        REFERENCES users(user_id)
                )
                """;



        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create parent tables first
            stmt.execute(createUsersTable);
            stmt.execute(createActivityLogsTable);
            stmt.execute(createCustomersTable);

            // Create dependent tables
            stmt.execute(createLpgCylindersTable);
            try {
                    stmt.execute("""
            ALTER TABLE lpg_cylinders
            ADD COLUMN deposit_paid REAL DEFAULT 0
            """);



                } catch (SQLException e) {
                    if (!e.getMessage().contains("duplicate column name")) {
                        throw e;
                    }
            }

            stmt.execute(createTransactionsTable);
            stmt.execute(createTransactionDetailsTable);
            stmt.execute(createMaintenanceRecordsTable);

            System.out.println("Database tables verified.");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            return;
        }


        // Generate default admin after table creation
        new UserDao();
    }
}
