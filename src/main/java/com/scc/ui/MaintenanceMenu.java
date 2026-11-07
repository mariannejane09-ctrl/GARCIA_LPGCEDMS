package com.scc.ui;

import com.scc.dao.*;
import com.scc.model.User;

import java.util.Scanner;

import com.scc.model.Cylinder;
import java.util.List;

public class MaintenanceMenu {
    private final UserDao userDao;
    private final ActivityLogDao activityLogDao = new ActivityLogDao();
    private final Scanner scanner;
    private final CylinderDao cylinderDao = new CylinderDao();
    private final TransactionDao transactionDao = new TransactionDao();
    private final MaintenanceDao maintenanceDao = new MaintenanceDao();
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
            switch (choice) {

                case "1":
                    viewCylindersForMaintenance();
                    break;

                case "2":
                    recordInspection(maintenance);
                    break;

                case "3":
                    recordRepair(maintenance);
                    break;

                case "4":
                    updateMaintenanceStatus(maintenance);
                    break;

                case "5":
                    maintenanceDao.viewAllMaintenanceRecords();
                    break;

                case "6":
                    activityLogDao.logAction(
                            maintenance.user_id(),
                            "LOGOUT",
                            "User " + maintenance.username() +
                                    " (ID: " + maintenance.user_id() + ") logged out."
                    );

                    loggedIn = false;
                    System.out.println("Logging out...");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void viewCylindersForMaintenance() {

        System.out.println("\n===== CYLINDERS FOR MAINTENANCE =====");

        List<Cylinder> cylinders = cylinderDao.getCylindersForMaintenance();

        if (cylinders.isEmpty()) {
            System.out.println("No cylinders currently require maintenance.");
            return;
        }

        System.out.printf("%-12s | %-8s | %-20s | %-15s%n",
                "CYLINDER ID", "SIZE", "CONDITION", "STATUS");

        System.out.println("---------------------------------------------------------------");

        for (Cylinder cylinder : cylinders) {
            System.out.printf("%-12d | %-8s | %-20s | %-15s%n",
                    cylinder.cylinder_id(),
                    cylinder.size(),
                    cylinder.condition(),
                    cylinder.status());
        }
    }


    private void recordInspection(User maintenance) {

        System.out.println("\n===== RECORD CYLINDER INSPECTION =====");

        List<Cylinder> cylinders = cylinderDao.getCylindersForMaintenance();

        if (cylinders.isEmpty()) {
            System.out.println("No cylinders require inspection.");
            return;
        }

        for (Cylinder cylinder : cylinders) {
            System.out.printf("ID: %d | Size: %s | Condition: %s | Status: %s%n",
                    cylinder.cylinder_id(),
                    cylinder.size(),
                    cylinder.condition(),
                    cylinder.status());
        }

        System.out.print("\nEnter Cylinder ID: ");
        int cylinderId;

        try {
            cylinderId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid Cylinder ID.");
            return;
        }

        boolean found = cylinders.stream()
                .anyMatch(c -> c.cylinder_id() == cylinderId);

        if (!found) {
            System.out.println("Invalid Cylinder ID.");
            return;
        }

        // Select inspection type
        System.out.println("\n===== INSPECTION TYPE =====");
        System.out.println("[1] VALVE INSPECTION");
        System.out.println("[2] GAS LEAK TEST");
        System.out.println("[3] SEAL INSPECTION");
        System.out.println("[4] CYLINDER BODY INSPECTION");
        System.out.println("[5] GENERAL SAFETY INSPECTION");

        System.out.print("Enter inspection type: ");
        String typeChoice = scanner.nextLine().trim();

        String inspectionType;

        switch (typeChoice) {
            case "1" -> inspectionType = "VALVE INSPECTION";
            case "2" -> inspectionType = "GAS LEAK TEST";
            case "3" -> inspectionType = "SEAL INSPECTION";
            case "4" -> inspectionType = "CYLINDER BODY INSPECTION";
            case "5" -> inspectionType = "GENERAL SAFETY INSPECTION";
            default -> {
                System.out.println("Invalid inspection type.");
                return;
            }
        }

        // Select inspection result
        System.out.println("\n===== INSPECTION FINDINGS =====");
        System.out.println("[1] ISSUE FOUND - REPAIR REQUIRED");
        System.out.println("[2] NO ISSUE FOUND");

        System.out.print("Enter findings: ");
        String resultChoice = scanner.nextLine().trim();

        String findings;

        switch (resultChoice) {
            case "1" -> findings = "REPAIR REQUIRED";
            case "2" -> findings = "NO ISSUE FOUND";
            default -> {
                System.out.println("Invalid inspection findings.");
                return;
            }
        }

        boolean success = maintenanceDao.recordMaintenance(
                cylinderId,
                maintenance.user_id(),
                inspectionType,
                findings
        );
        if (success) {

            String newCondition;

            if (findings.equals("REPAIR REQUIRED")) {
                newCondition = "NEEDS REPAIR";
            } else {
                newCondition = "GOOD";
            }

            cylinderDao.updateCylinderMaintenanceStatus(
                    cylinderId,
                    newCondition,
                    "MAINTENANCE"
            );

            System.out.println("Inspection recorded successfully!");
        }

        if (success) {


            activityLogDao.logAction(
                    maintenance.user_id(),
                    "CYLINDER_INSPECTION",
                    "Staff " + maintenance.user_id()
                            + " performed " + inspectionType
                            + " on Cylinder ID " + cylinderId
                            + ". Result: " + findings
            );

            System.out.println("Inspection recorded successfully!");
        } else {
            System.out.println("Failed to record inspection.");
        }
    }


    private void recordRepair(User maintenance) {

        System.out.println("\n===== RECORD CYLINDER REPAIR =====");

        // Get cylinders that have inspection findings requiring repair
        List<MaintenanceDao.RepairCylinder> cylinders =
                maintenanceDao.getCylindersForRepair();

        if (cylinders.isEmpty()) {
            System.out.println("No inspected cylinders are waiting for repair.");
            return;
        }

        // Display cylinder and inspection details
        for (MaintenanceDao.RepairCylinder cylinder : cylinders) {

            System.out.printf(
                    "\nID: %d | Size: %s%n",
                    cylinder.cylinderId(),
                    cylinder.size()
            );

            System.out.println("Condition: " + cylinder.condition());
            System.out.println("Status: " + cylinder.status());
            System.out.println("Inspection: " + cylinder.inspectionType());
            System.out.println("Findings: " + cylinder.findings());

            System.out.println("----------------------------------");
        }

        // Select cylinder
        System.out.print("\nEnter Cylinder ID to repair: ");
        int cylinderId;

        try {
            cylinderId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid Cylinder ID.");
            return;
        }

        boolean found = cylinders.stream()
                .anyMatch(c -> c.cylinderId() == cylinderId);

        if (!found) {
            System.out.println("Invalid Cylinder ID or cylinder is not awaiting repair.");
            return;
        }

        // Confirm repair
        System.out.print("Confirm that the repair is completed? (Y/N): ");
        String choice = scanner.nextLine().trim();

        if (!choice.equalsIgnoreCase("Y")) {
            System.out.println("Repair cancelled.");
            return;
        }

        // Save repair record
        boolean success = maintenanceDao.recordMaintenance(
                cylinderId,
                maintenance.user_id(),
                "REPAIR",
                "REPAIRED"
        );

        if (success) {

            // Update condition but keep status as MAINTENANCE
            boolean updated = cylinderDao.updateCylinderMaintenanceStatus(
                    cylinderId,
                    "GOOD",
                    "MAINTENANCE"
            );

            if (!updated) {
                System.out.println("Repair record saved, but cylinder update failed.");
                return;
            }

            // Record activity log
            activityLogDao.logAction(
                    maintenance.user_id(),
                    "CYLINDER_REPAIR",
                    "Staff " + maintenance.user_id()
                            + " recorded a repair for Cylinder ID "
                            + cylinderId + ". Result: REPAIRED"
            );

            System.out.println("\nRepair recorded successfully!");
            System.out.println("New condition: GOOD");
            System.out.println("Cylinder is now awaiting release.");

        } else {
            System.out.println("Failed to save repair record.");
        }
    }


    private void updateMaintenanceStatus(User maintenance) {

        System.out.println("\n===== RELEASE CYLINDER TO INVENTORY =====");

        List<Cylinder> cylinders = cylinderDao.getCylindersForRelease();

        if (cylinders.isEmpty()) {
            System.out.println("No cylinders are ready for release.");
            return;
        }

        // Display cylinders in a table
        System.out.println("------------------------------------------------------");
        System.out.printf("%-10s | %-8s | %-20s | %-15s%n",
                "ID", "Size", "Condition", "Status");
        System.out.println("------------------------------------------------------");

        for (Cylinder cylinder : cylinders) {
            System.out.printf("%-10d | %-8s | %-20s | %-15s%n",
                    cylinder.cylinder_id(),
                    cylinder.size(),
                    cylinder.condition(),
                    cylinder.status());
        }

        System.out.println("------------------------------------------------------");

        System.out.print("\nEnter Cylinder ID to release (0 to cancel): ");

        int cylinderId;

        try {
            cylinderId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid Cylinder ID.");
            return;
        }

        if (cylinderId == 0) {
            System.out.println("Release cancelled.");
            return;
        }

        boolean found = cylinders.stream()
                .anyMatch(c -> c.cylinder_id() == cylinderId);

        if (!found) {
            System.out.println("Invalid Cylinder ID.");
            return;
        }

        System.out.print("Are you sure you want to release this cylinder? (Y/N): ");
        String confirm = scanner.nextLine().trim();

        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println("Release cancelled.");
            return;
        }

        boolean success = cylinderDao.releaseCylinder(cylinderId);

        if (success) {

            activityLogDao.logAction(
                    maintenance.user_id(),
                    "CYLINDER_RELEASED",
                    "Maintenance Staff " + maintenance.user_id()
                            + " released Cylinder ID " + cylinderId
                            + " to AVAILABLE inventory."
            );

            System.out.println("\nCylinder released successfully!");
            System.out.println("Condition: GOOD");
            System.out.println("Status: AVAILABLE");

        } else {
            System.out.println("Failed to release cylinder.");
        }
    }

}
