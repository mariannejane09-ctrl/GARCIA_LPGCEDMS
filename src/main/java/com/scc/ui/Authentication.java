package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;
import com.scc.dao.ActivityLogDao;

public class Authentication {
    private final UserDao userDao;
    private final ActivityLogDao activityLogDao = new ActivityLogDao();
    private Scanner sc;

    public Authentication(UserDao userDao, Scanner sc){
        this.userDao = userDao;
        this.sc = sc;

    }

    public void start(){
        while (true) {
            System.out.println("+=========================================+");
            System.out.println("|  LPG Cylinder Exchange and Distribution |");
            System.out.println("|           Management System             |");
            System.out.println("+=========================================+");
            System.out.println("[1] Login");
            System.out.println("[2] Register");
            System.out.println("[3] Exit");
            System.out.println("+=========================================+");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    System.out.print("Enter Username: ");
                    String user = sc.nextLine();
                    System.out.print("Enter Name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter password: ");
                    String password = sc.nextLine();
                    System.out.print("Enter contact no.: ");
                    String contact = sc.nextLine();

                    System.out.print("Select role: [1] ADMIN | [2] STAFF | [3] MAINTENANCE: ");

                    int roleChoice = Integer.parseInt(sc.nextLine());
                    String role;
                    switch (roleChoice) {
                        case 1:
                            role = "ADMIN";
                            break;
                        case 2:
                            role = "STAFF";
                            break;
                        case 3:
                            role = "MAINTENANCE";
                            break;
                        default:
                            role = null;
                    }

                    if (role != null) {
                        userDao.registerUser(user, name, password, contact, role);
                    } else {
                        System.out.println("Invalid role selection!");
                    }
                    break;
                case "3":
                    System.out.println("Exiting the System");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid option! Please try again");
            }
        }
    }

    private void handleLogin(){
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        User user = userDao.login(username, password);

        if(user != null){

            // Record successful login
            activityLogDao.logAction(
                    user.user_id(),
                    "LOGIN",
                    "User " + user.username() + " (ID: " + user.user_id() + ") logged in."
            );

            System.out.println("\n WELCOME: "+user.name()+" | ROLE: "+user.role());

            switch(user.role()){
                case "ADMIN" -> new AdminMenu(userDao, sc).display(user);
                case "STAFF" -> new StaffMenu(userDao, sc).display(user);
                case "MAINTENANCE" -> new MaintenanceMenu(userDao, sc).display(user);
                default -> System.out.println("Account roles not found!");
            }
        }
    }
}
