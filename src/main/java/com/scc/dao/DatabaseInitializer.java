
package com.scc.dao;

import java.sql.Connection;
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
                        ON DELETE SET NULL
                )
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create users table first
            stmt.execute(createUsersTable);

            // Create activity logs table second
            stmt.execute(createActivityLogsTable);

            System.out.println("Database tables verified.");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            return;
        }

        // Generate default admin after table creation
        new UserDao();
    }
}
