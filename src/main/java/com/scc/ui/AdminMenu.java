package com.scc.ui;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
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
            System.out.println("[5] Logout");
            System.out.println("===========================");
            System.out.print("Enter Choice: ");
            String choice = scanner.nextLine().trim();
            if (choice.equals("5")) {
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
                            case "3" -> approvedUser();
                            case "5" -> updateUser(admin);
                            case "7" ->  {
                                manageUsers = false;
                                System.out.println("Returning to Admin Main Menu...");
                            }
                            default -> System.out.println("Invalid Option!");

                        }
                    }
            }
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

    private void approvedUser() {
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

        userDao.approveUser(userId);
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

        userDao.updateUserAndStatus(userId, newRole, newStatus);
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

