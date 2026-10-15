package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class StaffMenu {
    private final UserDao userDao;
    private final Scanner scanner;

    public StaffMenu(UserDao userDao, Scanner scanner){
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void display (User staff){
        boolean loggedIn = true;
        while(loggedIn) {
            System.out.println("\n+=========================+");
            System.out.println("|     STAFF DASHBOARD     |");
            System.out.println("+=========================+");
            System.out.println("[1] Register Customer");
            System.out.println("[2] View Customers");
            System.out.println("[3] Process LPG Sale");
            System.out.println("[4] Exchange Cylinder");
            System.out.println("[5] Return Cylinder");
            System.out.println("[6] View Transactions");
            System.out.println("[7] Logout");
            System.out.println("===========================");
            System.out.print("Enter Choice: ");
            String choice = scanner.nextLine().trim();
            if (choice.equals("7")) {
                loggedIn = false;
                System.out.println("Logging out...");
            }
        }
    }
}
