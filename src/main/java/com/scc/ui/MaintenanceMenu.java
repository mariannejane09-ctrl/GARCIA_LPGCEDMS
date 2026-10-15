package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class MaintenanceMenu {
    private final UserDao userDao;
    private final Scanner scanner;
    public MaintenanceMenu(UserDao userDao, Scanner scanner){
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void display (User maintenance){
        boolean loggedIn = true;
        while(loggedIn){
            System.out.println("\n+=================================+");
            System.out.println("|      MAINTENANCE DASHBOARD      |");
            System.out.println("+=================================+");
            System.out.println("[1] View Cylinders for Maintenance");
            System.out.println("[2] Record Inspection");
            System.out.println("[3] Record Repair");
            System.out.println("[4] Update Maintenance Status");
            System.out.println("[5] View Maintenance Records");
            System.out.println("[6] Logout");
            System.out.println("===================================");
            System.out.print("Enter Choice: ");
            String choice = scanner.nextLine().trim();
            if (choice.equals("6")) {
                loggedIn = false;
                System.out.println("Logging out...");
            }
        }
    }
}
