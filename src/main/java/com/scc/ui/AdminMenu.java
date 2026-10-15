package com.scc.ui;
import com.scc.dao.UserDao;
import com.scc.model.User;
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
        }
    }
}
