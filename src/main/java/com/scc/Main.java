package com.scc;
import java.util.Scanner;
import com.scc.dao.userDao;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final userDao userDao = new userDao();
    public static void main(String[] args){

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
                    break;
                case "2":
                    System.out.print("Enter Username: ");
                    String user = sc.nextLine();
                    System.out.print("Enter Name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter password: ");
                    String password = sc.nextLine();
                    System.out.print("Enter contact no.: ");
                    int contact = Integer.parseInt(sc.nextLine());

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
}
