package com.scc.ui;

import com.scc.dao.*;
import com.scc.model.User;

import java.util.Scanner;

import com.scc.dao.CylinderDao;
import com.scc.dao.TransactionDao;
import com.scc.model.Cylinder;
import com.scc.model.Customer;
import com.scc.dao.CustomerDao;
import com.scc.model.Transaction;
import java.util.ArrayList;
import java.util.List;

public class StaffMenu {
    private final UserDao userDao;
    private final ActivityLogDao activityLogDao = new ActivityLogDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final CylinderDao cylinderDao = new CylinderDao();
    private final TransactionDao transactionDao = new TransactionDao();

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

                activityLogDao.logAction(
                        staff.user_id(),
                        "LOGOUT",
                        "User " + staff.username() + " (ID: " + staff.user_id() + ") logged out."
                );

                loggedIn = false;
                System.out.println("Logging out...");
            }

            switch (choice) {

                case "1":
                    registerCustomer(staff);
                    break;

                case "2":
                    viewCustomers();
                    break;

                case "3":
                    processLpgSale(staff);
                    break;

                case "4":
                    exchangeCylinder(staff);
                    break;

                case "5":
                    // Return Cylinder
                    break;

                case "6":
                    viewTransactions();
                    break;

                case "7":
                    activityLogDao.logAction(
                            staff.user_id(),
                            "LOGOUT",
                            "User " + staff.username() + " (ID: " + staff.user_id() + ") logged out."
                    );
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }


        }
    }

    private void registerCustomer(User staff) {

        System.out.println("\n===== REGISTER CUSTOMER =====");

        System.out.print("Enter Customer Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Contact Number: ");
        String contact = scanner.nextLine();

        System.out.print("Enter Address: ");
        String address = scanner.nextLine();

        boolean success = customerDao.registerCustomer(name, contact, address);

        if (success) {
            System.out.println("\nCustomer successfully registered!");

            activityLogDao.logAction(
                    staff.user_id(),
                    "CUSTOMER_REGISTRATION",
                    "Staff " + staff.user_id() + " registered customer " + name + "."
            );

        } else {
            System.out.println("\nFailed to register customer.");
        }
    }

    private void viewCustomers() {

        System.out.println("\n===== REGISTERED CUSTOMERS =====");

        List<Customer> customers = customerDao.getAllCustomers();

        if (customers.isEmpty()) {
            System.out.println("No registered customers found.");
            return;
        }

        System.out.printf("%-5s | %-20s | %-15s | %-25s | %-15s%n",
                "ID", "NAME", "CONTACT", "ADDRESS", "DEPOSIT");

        System.out.println("-----------------------------------------------------------------------------------------");

        for (Customer customer : customers) {
            System.out.printf("%-5d | %-20s | %-15s | %-25s | ₱%-14.2f%n",
                    customer.customer_id(),
                    customer.name(),
                    customer.contact(),
                    customer.address(),
                    customer.deposit_balance());
        }
    }


    private void processLpgSale(User staff) {

        // 1. Select customer
        List<Customer> customers = customerDao.getAllCustomers();

        if (customers.isEmpty()) {
            System.out.println("No registered customers found.");
            return;
        }

        System.out.println("\n========== SELECT CUSTOMER ==========");

        for (Customer customer : customers) {
            System.out.printf(
                    "ID: %d | Name: %s | Contact: %s%n",
                    customer.customer_id(),
                    customer.name(),
                    customer.contact()
            );
        }

        System.out.print("\nEnter Customer ID: ");
        String input = scanner.nextLine().trim();

        int customerId;

        try {
            customerId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Customer ID.");
            return;
        }

        Customer customer = customerDao.getCustomerById(customerId);

        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        // 2. Select LPG quantities
        String[] sizes = {"S1", "S2", "S3"};

        List<Integer> cylinderIds = new ArrayList<>();

        double lpgTotal = 0;
        int totalQuantity = 0;

        System.out.println("\n========== LPG QUANTITY SELECTION ==========");

        for (String size : sizes) {

            List<Cylinder> available =
                    cylinderDao.getAvailableCylindersBySize(size);

            if (available.isEmpty()) {
                System.out.println("\n" + size + " - No available stock.");
                continue;
            }

            Cylinder sample = available.get(0);

            System.out.printf(
                    "\n%s | Price: ₱%.2f | Available: %d%n",
                    size,
                    sample.price(),
                    available.size()
            );

            System.out.print("Enter quantity (0 to skip): ");

            String quantityInput = scanner.nextLine().trim();

            int quantity;

            try {
                quantity = Integer.parseInt(quantityInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid quantity. Sale cancelled.");
                return;
            }

            if (quantity < 0 || quantity > available.size()) {
                System.out.println("Invalid quantity. Sale cancelled.");
                return;
            }

            for (int i = 0; i < quantity; i++) {

                Cylinder selected = available.get(i);

                cylinderIds.add(selected.cylinder_id());
                lpgTotal += selected.price();
            }

            totalQuantity += quantity;
        }

        if (totalQuantity == 0) {
            System.out.println("No cylinders selected. Sale cancelled.");
            return;
        }

        // 3. Calculate total
        double depositTotal = totalQuantity * 300;
        double totalAmount = lpgTotal + depositTotal;

        // 4. Display summary
        System.out.println("\n========== SALE SUMMARY ==========");
        System.out.println("Customer: " + customer.name());
        System.out.println("Total Cylinders: " + totalQuantity);

        System.out.printf("LPG Total: ₱%.2f%n", lpgTotal);
        System.out.printf("Cylinder Deposit: ₱%.2f%n", depositTotal);
        System.out.printf("TOTAL AMOUNT: ₱%.2f%n", totalAmount);

        // 5. Confirm sale
        System.out.print("\nConfirm sale? (Y/N): ");
        String confirm = scanner.nextLine().trim();

        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println("Sale cancelled.");
            return;
        }

        // 6. Process sale
        transactionDao.processSale(
                customerId,
                staff.user_id(),
                cylinderIds
        );
    }

    private void viewTransactions() {

        List<Transaction> transactions =
                transactionDao.getAllTransactions();

        if (transactions.isEmpty()) {
            System.out.println("\nNo transactions found.");
            return;
        }

        System.out.println("\n================ TRANSACTION HISTORY ================");

        System.out.printf(
                "%-5s %-18s %-15s %-12s %-12s %-12s%n",
                "ID", "CUSTOMER", "STAFF", "TYPE",
                "AMOUNT", "DEPOSIT"
        );

        System.out.println(
                "--------------------------------------------------------------------------"
        );

        for (Transaction transaction : transactions) {

            System.out.printf(
                    "%-5d %-18s %-15s %-12s ₱%-11.2f ₱%-11.2f%n",
                    transaction.transaction_id(),
                    transaction.customer_name(),
                    transaction.staff_name(),
                    transaction.transaction_type(),
                    transaction.amount(),
                    transaction.deposit_amount()
            );
        }

        System.out.println(
                "--------------------------------------------------------------------------"
        );
    }


    private void exchangeCylinder(User staff) {

        CustomerDao customerDao = new CustomerDao();

        System.out.println("\n========== EXCHANGE CYLINDER ==========");

        // STEP 1: Display registered customers

        List<Customer> customers = customerDao.getAllCustomers();

        if (customers.isEmpty()) {
            System.out.println("No customers registered.");
            return;
        }

        System.out.println("\nREGISTERED CUSTOMERS");

        for (Customer customer : customers) {
            System.out.printf(
                    "ID: %d | Name: %s%n",
                    customer.customer_id(),
                    customer.name()
            );
        }

        // STEP 2: Select customer

        System.out.print("\nEnter Customer ID: ");

        if (!scanner.hasNextInt()) {
            System.out.println("Invalid customer ID.");
            scanner.nextLine();
            return;
        }

        int customerId = scanner.nextInt();
        scanner.nextLine();

        Customer customer = customerDao.getCustomerById(customerId);

        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        // STEP 3: Display customer's existing cylinders

        List<Cylinder> cylinders =
                cylinderDao.getCustomerCylinders(customerId);

        if (cylinders.isEmpty()) {
            System.out.println("This customer has no cylinders to exchange.");
            return;
        }

        System.out.println("\nCUSTOMER CYLINDERS");

        for (Cylinder cylinder : cylinders) {
            System.out.printf(
                    "ID: %d | Size: %s | Condition: %s%n",
                    cylinder.cylinder_id(),
                    cylinder.size(),
                    cylinder.condition()
            );
        }

        // STEP 4: Select old cylinder

        System.out.print("\nEnter Cylinder ID to exchange: ");

        if (!scanner.hasNextInt()) {
            System.out.println("Invalid cylinder ID.");
            scanner.nextLine();
            return;
        }

        int oldCylinderId = scanner.nextInt();
        scanner.nextLine();

        Cylinder oldCylinder = null;

        for (Cylinder cylinder : cylinders) {
            if (cylinder.cylinder_id() == oldCylinderId) {
                oldCylinder = cylinder;
                break;
            }
        }

        if (oldCylinder == null) {
            System.out.println("Cylinder does not belong to this customer.");
            return;
        }

        // STEP 5: Display ALL replacement sizes

        String[] sizes = {"S1", "S2", "S3"};

        System.out.println("\nAVAILABLE REPLACEMENT SIZES");

        for (String size : sizes) {

            List<Cylinder> available =
                    cylinderDao.getAvailableCylindersBySize(size);

            double price = switch (size) {
                case "S1" -> 600.00;
                case "S2" -> 850.00;
                case "S3" -> 1700.00;
                default -> 0.00;
            };

            System.out.printf(
                    "%s | Price: ₱%.2f | Available: %d%n",
                    size,
                    price,
                    available.size()
            );
        }

        // STEP 6: Select replacement size
        // Different sizes are allowed!

        System.out.print("\nEnter replacement size (S1/S2/S3): ");

        String newSize = scanner.nextLine().trim().toUpperCase();

        if (!newSize.equals("S1") &&
                !newSize.equals("S2") &&
                !newSize.equals("S3")) {

            System.out.println("Invalid cylinder size.");
            return;
        }

        // No comparison with oldCylinder.size() is needed.

        List<Cylinder> replacements =
                cylinderDao.getAvailableCylindersBySize(newSize);

        if (replacements.isEmpty()) {
            System.out.println("No available cylinders for this size.");
            return;
        }

        Cylinder newCylinder = replacements.get(0);

        // STEP 7: Calculate payment
        // Exchange credit is always ₱100.

        double exchangeCredit = 100.00;

        double finalAmount =
                newCylinder.price() - exchangeCredit;

        // STEP 8: Display exchange summary

        System.out.println("\n========== EXCHANGE SUMMARY ==========");

        System.out.println("Customer: " + customer.name());

        System.out.println(
                "Old Cylinder ID: " + oldCylinder.cylinder_id()
        );

        System.out.println(
                "Old Cylinder Size: " + oldCylinder.size()
        );

        System.out.println(
                "Replacement Cylinder ID: " + newCylinder.cylinder_id()
        );

        System.out.println(
                "Replacement Size: " + newCylinder.size()
        );

        System.out.printf(
                "Replacement Price: ₱%.2f%n",
                newCylinder.price()
        );

        System.out.printf(
                "Exchange Credit: -₱%.2f%n",
                exchangeCredit
        );

        System.out.println("--------------------------------------");

        System.out.printf(
                "AMOUNT TO PAY: ₱%.2f%n",
                finalAmount
        );

        System.out.println("Additional Deposit: ₱0.00");

        // STEP 9: Confirm exchange

        System.out.print("\nConfirm exchange? (Y/N): ");

        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println("Exchange cancelled.");
            return;
        }

        // STEP 10: Save exchange transaction

        boolean success = transactionDao.processExchange(
                customerId,
                staff.user_id(),
                oldCylinderId,
                newCylinder.cylinder_id()
        );

        if (success) {

            System.out.println("\nCylinder exchange completed successfully!");

            System.out.printf(
                    "Amount paid: ₱%.2f%n",
                    finalAmount
            );

        } else {

            System.out.println("\nExchange failed. No changes were saved.");
        }
    }

}
