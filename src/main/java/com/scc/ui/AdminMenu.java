package com.scc.ui;
import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.dao.ActivityLogDao;
import java.util.List;
import java.util.Scanner;
import com.scc.model.ActivityLog;

public class AdminMenu {
    private final UserDao userDao;
    private final ActivityLogDao activityLogDao = new ActivityLogDao();
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner){
        this.userDao = userDao;
        this.scanner = scanner;
    }
    public void display (User admin){
        boolean loggedIn = true;

        while(loggedIn){
            System.out.println("\n+=========================+");
            System.out.println("|     ADMIN DASHBOARD     |");
            System.out.println("+=========================+");
            System.out.println("[1] Manage Users");
            System.out.println("[2] Manage LPG Assets");
            System.out.println("[3] View Transactions");
            System.out.println("[4] Generate Reports");
            System.out.println("[5] View System Audit Logs");
            System.out.println("[6] Logout");
            System.out.println("===========================");
            System.out.print("Enter Choice: ");
            String choice = scanner.nextLine().trim();
            if (choice.equals("6")) {

                activityLogDao.logAction(
                        admin.user_id(),
                        "LOGOUT",
                        "User " + admin.username() + " (ID: " + admin.user_id() + ") logged out."
                );

                loggedIn = false;
                System.out.println("Logging out...");
            }

            switch (choice){
                case "1":
                    boolean manageUsers = true;

                    while (manageUsers){
                        System.out.println("\n+=========================+");
                        System.out.println("|       MANAGE USERS      |");
                        System.out.println("+=========================+");
                        System.out.println("[1] View Users");
                        System.out.println("[2] View Pending User Approvals");
                        System.out.println("[3] Approve User Registration");
                        System.out.println("[4] Add New User");
                        System.out.println("[5] Update User Role/Status");
                        System.out.println("[6] Delete User Account");
                        System.out.println("[7] Back");
                        System.out.println("===========================");
                        System.out.print("Enter Choice: ");
                        String sub_choice = scanner.nextLine().trim();


                        switch(sub_choice){
                            case "1" -> viewALlUser();
                            case "2" -> viewPendingUsers();
                            case "3" -> approvedUser(admin);
                            case "5" -> updateUser(admin);
                            case "7" ->  {
                                manageUsers = false;
                                System.out.println("Returning to Admin Main Menu...");
                            }
                            default -> System.out.println("Invalid Option!");

                        }
                    }
                    break;

                case "5":
                    manageAuditLogs(admin);
                    break;
            }
        }
    }

    private void clearHistoricalLogs(User currentAdmin) {

        System.out.println("\nWARNING: You are about to clear all historical logs.");
        System.out.println("This operation cannot be undone.");
        System.out.print("Are you sure? (Y/N): ");

        String confirmation = scanner.nextLine().trim().toUpperCase();

        if (!confirmation.equals("Y")) {
            System.out.println("-> Operation Cancelled.");
            return;
        }

        if (activityLogDao.clearAllLogs()) {

            System.out.println("-> Historical logs cleared successfully.");

            activityLogDao.logAction(
                    currentAdmin.user_id(),
                    "LOG_MANAGEMENT",
                    "Admin " + currentAdmin.user_id() +
                            " cleared historical activity logs."
            );

        } else {
            System.out.println("-> Failed to clear historical logs.");
        }
    }

    private void displayUserTable(List<User> users, String title){
        System.out.println("\n====================" + title + "=======================");

        if(users.isEmpty()){
            System.out.println("-> No records found");
            return;
        }


        System.out.println("+------+-----------------+----------------------+---------------+--------------------+----------+");
        System.out.printf("| %-4s | %-15s | %-20s | %-13s | %-18s | %-8s |%n",
                "ID", "Username", "Name", "Contact", "Role", "Status");
        System.out.println("+------+-----------------+----------------------+---------------+--------------------+----------+");
            for(User u : users){
                System.out.printf("| %-4s | %-15s | %-20s | %-13s | %-18s | %-8s |%n",
                        u.user_id(), u.username(), u.name(), u.contact(), u.role(), u.status());
            }
        System.out.println("+------+-----------------+----------------------+---------------+--------------------+----------+");
    }
    private void viewALlUser(){
        displayUserTable(userDao.getAllUser(), "ALL REGISTERED USER");
    }

    private void viewPendingUsers(){
        displayUserTable(userDao.getPendingUser(), "PENDING APPROVALS");
    }

    private void viewAllSystemLogs() {
        List<ActivityLog> logs = activityLogDao.getAllLogs();
        displayActivityLogs(logs);
    }

    private void displayActivityLogs(List<ActivityLog> logs) {

        System.out.println("\n========================== SYSTEM AUDIT LOGS ==========================");

        if (logs.isEmpty()) {
            System.out.println("-> No activity records found.");
            return;
        }

        System.out.printf(
                "%-5s | %-8s | %-18s | %-45s | %-20s%n",
                "ID", "User ID", "Action Type", "Description", "Timestamp"
        );

        System.out.println("-------------------------------------------------------------------------------------------------------------");

        for (ActivityLog log : logs) {

            String description = log.description();

            if (description.length() > 45) {
                description = description.substring(0, 42) + "...";
            }

            System.out.printf(
                    "%-5d | %-8d | %-18s | %-45s | %-20s%n",
                    log.id(),
                    log.userId(),
                    log.actionType(),
                    description,
                    log.timestamp()
            );
        }

        System.out.println("-------------------------------------------------------------------------------------------------------------");
    }

    private void filterLogsByUserId() {

        // Display registered users first
        List<User> users = userDao.getAllUser();

        displayUserTable(users, "SELECT USER TO VIEW ACTIVITY LOGS");

        if (users.isEmpty()) {
            return;
        }

        int userId = readIntInput("Enter User ID to view logs (or 0 to cancel): ");

        if (userId == 0) {
            System.out.println("-> Operation Cancelled.");
            return;
        }

        // Check if the selected user exists
        boolean exists = users.stream()
                .anyMatch(u -> u.user_id() == userId);

        if (!exists) {
            System.out.println("-> [ERROR] User ID not found.");
            return;
        }

        // Retrieve and display selected user's logs
        List<ActivityLog> logs = activityLogDao.getLogsByUserId(userId);

        displayActivityLogs(logs);
    }



    private void manageAuditLogs(User currentAdmin) {

        boolean viewingLogs = true;

        while (viewingLogs) {

            System.out.println("\n+=============================+");
            System.out.println("|      SYSTEM AUDIT LOGS      |");
            System.out.println("+=============================+");
            System.out.println("[1] View All System Logs");
            System.out.println("[2] Filter Logs by User ID");
            System.out.println("[3] Clear Historical Logs");
            System.out.println("[4] Back");
            System.out.println("===============================");
            System.out.print("Enter Choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllSystemLogs();
                case "2" -> filterLogsByUserId();
                case "3" -> clearHistoricalLogs(currentAdmin);
                case "4" -> {
                    viewingLogs = false;
                    System.out.println("Returning to Admin Dashboard...");
                }
                default -> System.out.println("Invalid Option!");
            }
        }
    }

    private void approvedUser(User currentAdmin) {
        List<User> pendingUsers = userDao.getPendingUser();
        displayUserTable(pendingUsers, "PENDING APPROVALS");

        if (pendingUsers.isEmpty()) {
            return;
        }

        System.out.print("Enter user ID to approve (or 0 to cancel): ");
        int userId;

        try {
            userId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println(" -> [ERROR] Please enter a valid number.");
            return;
        }

        if (userId == 0) {
            System.out.println(" -> Operation Cancelled");
            return;
        }

        boolean exists = pendingUsers.stream()
                .anyMatch(u -> u.user_id() == userId);

        if (!exists) {
            System.out.println(" -> [ERROR] User ID not found");
            return;
        }

        if (userDao.approveUser(userId)) {
            activityLogDao.logAction(
                    currentAdmin.user_id(),
                    "ACCOUNT_MANAGEMENT",
                    "Admin " + currentAdmin.user_id() +
                            " approved User " + userId + "."
            );
        }
    }

    private void updateUser(User currentAdmin) {
        List<User> users = userDao.getAllUser();
        displayUserTable(users, "SELECT USER TO UPDATE");

        if (users.isEmpty()) {
            return;
        }

        int userId = readIntInput("Enter selected ID to update (or 0 to cancel): ");
        if (userId == 0) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        if (userId == currentAdmin.user_id()) {
            System.out.println(" -> [WARNING] Modifying current active Admin is restricted");
        }

        String newRole = readValidRole();
        if (newRole == null) {
            System.out.println(" ->Operation Cancelled.");
            return;
        }

        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println(" ->Operation Cancelled.");
            return;
        }

        if (userDao.updateUserAndStatus(userId, newRole, newStatus)) {
            activityLogDao.logAction(
                    currentAdmin.user_id(),
                    "ACCOUNT_MANAGEMENT",
                    "Admin " + currentAdmin.user_id() +
                            " updated User " + userId +
                            " role/status to " + newRole + "/" + newStatus + "."
            );
        }
    }

    private String readValidRole(){
        while(true) {
            System.out.print("Enter Role (ADMIN / STAFF / MAINTENANCE) [0 to cancel]: ");
            String role = scanner.nextLine().trim().toUpperCase();
                if (role.equals("0") || role.equals("CANCEL")) {
                    return null;
                }

                if (role.equals("ADMIN") || role.equals("STAFF") || role.equals("MAINTENANCE")) {
                    return role;
                }
                System.out.println(" ->[ERROR] Invalid Role.");
            }
        }


    private String readValidStatus() {
        while (true) {
            System.out.print("Enter Status (APPROVED / PENDING) [0 to cancel]: ");
            String status = scanner.nextLine().trim().toUpperCase();
            if (status.equals("0") || status.equals("CANCEL")) {
                return null;
            }

            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }

            System.out.println("-> [ERROR] Invalid status. Must be APPROVED or PENDING.");
        }
    }

    private int readIntInput(String prompt) {
         while (true) {
              System.out.print(prompt);
              String input = scanner.nextLine().trim();
              try {
                   return Integer.parseInt(input);
              } catch (NumberFormatException e) {
                   System.out.println("-> [ERROR] Please enter a valid numeric ID.");
              }
         }
    }


}

