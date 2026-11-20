package com.banking.app;

import com.banking.exceptions.BankingException;
import com.banking.exceptions.SessionTimeoutException;
import com.banking.model.Account;
import com.banking.model.AccountType;
import com.banking.model.Customer;
import com.banking.security.PasswordUtil;
import com.banking.service.AccountService;
import com.banking.util.AppLogger;
import com.banking.util.InputReader;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.*;
import java.time.YearMonth;
import java.util.*;
import java.util.logging.*;

public class Main {

    // ==============================
    // APPLICATION SETTINGS
    // ==============================

    private static final Map<String, Customer> customers =
            new HashMap<>();

    private static final Logger logger = AppLogger.getLogger();

    private static final SecureRandom random = new SecureRandom();

    private static Customer currentCustomer;


    // ==============================
    // INPUT HANDLING

    private static void startInputReader() {
        InputReader.start();
    }

    private static String ask(String prompt)
            throws InterruptedException, SessionTimeoutException {
        return InputReader.ask(prompt, currentCustomer != null);
    }


    // LOGGING

    private static void setupLogger() {
        AppLogger.setup();
    }


    // PASSWORD HASHING

    private static byte[] generateSalt() {
        return PasswordUtil.generateSalt();
    }

    private static byte[] hashPassword(String password, byte[] salt)
            throws GeneralSecurityException {
        return PasswordUtil.hashPassword(password, salt);
    }


    // REGISTRATION
    // ==============================

    private static void register() throws Exception {

        System.out.println("\n===== CUSTOMER REGISTRATION =====");

        String username = ask("Create username: ");

        if (username == null) return;

        username = username.trim();

        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }

        String key = username.toLowerCase(Locale.ROOT);

        if (customers.containsKey(key)) {
            System.out.println("Username already exists!");
            return;
        }

        String password = ask(
                "Create password (minimum 8 characters): "
        );

        if (password == null) return;

        if (password.length() < 8) {
            System.out.println(
                    "Password must be at least 8 characters."
            );
            return;
        }

        byte[] passwordSalt = generateSalt();

        byte[] passwordHash =
                hashPassword(password, passwordSalt);

        // Generate a recovery code for password reset.
        String recoveryCode =
                String.format("%08d", random.nextInt(100000000));

        byte[] recoverySalt = generateSalt();

        byte[] recoveryHash =
                hashPassword(recoveryCode, recoverySalt);

        Customer customer = new Customer(
                username,
                passwordSalt,
                passwordHash,
                recoverySalt,
                recoveryHash
        );

        // Automatically create savings and checking accounts.
        customer.accounts.add(
                new Account(
                        AccountType.SAVINGS,
                        BigDecimal.ZERO,
                        new BigDecimal("0.04")
                )
        );

        customer.accounts.add(
                new Account(
                        AccountType.CHECKING,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                )
        );

        customers.put(key, customer);

        logger.info("Customer registration successful.");

        System.out.println("\nRegistration successful!");
        System.out.println("Your default accounts were created.");

        System.out.println("\nIMPORTANT: Your password recovery code is:");
        System.out.println(recoveryCode);
        System.out.println("Save this code. It will not be shown again.");
    }


    // ==============================
    // LOGIN
    // ==============================

    private static void login() throws Exception {

        System.out.println("\n===== CUSTOMER LOGIN =====");

        String username = ask("Enter username: ");

        if (username == null) return;

        String password = ask("Enter password: ");

        if (password == null) return;

        Customer customer = customers.get(
                username.trim().toLowerCase(Locale.ROOT)
        );

        if (customer == null) {

            logger.warning("Failed login attempt.");
            System.out.println("Invalid username or password.");
            return;
        }

        byte[] enteredHash = hashPassword(
                password,
                customer.passwordSalt
        );

        if (MessageDigest.isEqual(
                enteredHash,
                customer.passwordHash)) {

            currentCustomer = customer;

            logger.info("Customer login successful.");

            System.out.println("\nLogin successful!");
            System.out.println(
                    "Welcome, " + customer.username + "!"
            );

        } else {

            logger.warning("Failed login attempt.");
            System.out.println("Invalid username or password.");
        }
    }


    // ==============================
    // PASSWORD RESET
    // ==============================

    private static void resetPassword() throws Exception {

        System.out.println("\n===== PASSWORD RESET =====");

        String username = ask("Enter username: ");

        if (username == null) return;

        Customer customer = customers.get(
                username.trim().toLowerCase(Locale.ROOT)
        );

        if (customer == null) {
            System.out.println(
                    "Invalid username or recovery code."
            );
            return;
        }

        String recoveryCode = ask("Enter recovery code: ");

        if (recoveryCode == null) return;

        byte[] enteredRecoveryHash = hashPassword(
                recoveryCode,
                customer.recoverySalt
        );

        if (!MessageDigest.isEqual(
                enteredRecoveryHash,
                customer.recoveryHash)) {

            logger.warning("Password reset verification failed.");

            System.out.println(
                    "Invalid username or recovery code."
            );
            return;
        }

        String newPassword = ask("Enter your new password: ");

        if (newPassword == null) return;

        if (newPassword.length() < 8) {
            System.out.println(
                    "Password must be at least 8 characters."
            );
            return;
        }

        customer.passwordSalt = generateSalt();

        customer.passwordHash = hashPassword(
                newPassword,
                customer.passwordSalt
        );

        logger.info("Customer password reset successful.");

        System.out.println("Password reset successful!");
    }


    // ==============================
    // DISPLAY ACCOUNTS
    // ==============================

    private static void displayAccounts() {

        System.out.println("\n===== YOUR BANK ACCOUNTS =====");

        if (currentCustomer.accounts.isEmpty()) {
            System.out.println("No accounts available.");
            return;
        }

        for (Account account : currentCustomer.accounts) {

            System.out.println("------------------------------");
            System.out.println(
                    "Account Number: " + account.accountNumber
            );

            System.out.println("Account Type: " + account.type);

            System.out.println(
                    "Balance: $" + account.balance.setScale(
                            2, RoundingMode.HALF_UP
                    )
            );

            System.out.println(
                    "Annual Interest: "
                            + account.annualInterestRate
                                    .multiply(new BigDecimal("100"))
                            + "%"
            );
        }

        System.out.println("------------------------------");
    }


    // ==============================
    // FIND CUSTOMER ACCOUNT
    // ==============================

    private static Account findAccount(String accountNumber)
            throws BankingException {

        for (Account account : currentCustomer.accounts) {

            if (account.accountNumber.equalsIgnoreCase(
                    accountNumber.trim())) {

                return account;
            }
        }

        throw new BankingException(
                "Account not found or you do not own this account."
        );
    }

    private static Account askForAccount()
            throws Exception {

        displayAccounts();

        String accountNumber =
                ask("\nEnter account number: ");

        if (accountNumber == null) return null;

        return findAccount(accountNumber);
    }


    // ==============================
    // ACCOUNT BALANCE
    // ==============================

    private static void checkBalance() throws Exception {

        System.out.println("\n===== ACCOUNT BALANCE =====");

        Account account = askForAccount();

        if (account == null) return;

        System.out.println(
                "Account balance: $"
                        + account.balance.setScale(
                                2, RoundingMode.HALF_UP
                        )
        );
    }


    // ==============================
    // DEPOSIT
    // ==============================

    private static void deposit() throws Exception {

        System.out.println("\n===== DEPOSIT MONEY =====");

        Account account = askForAccount();

        if (account == null) return;

        String amountText = ask("Enter deposit amount: ");

        if (amountText == null) return;

        BigDecimal amount = parseAmount(amountText);

        AccountService.deposit(account, amount);

        logger.info(
                "Deposit completed. Account: "
                        + account.accountNumber
                        + ", Amount: " + amount
        );

        System.out.println("Deposit successful!");
        System.out.println("New balance: $" + account.balance);
    }


    // ==============================
    // WITHDRAW
    // ==============================

    private static void withdraw() throws Exception {

        System.out.println("\n===== WITHDRAW MONEY =====");

        Account account = askForAccount();

        if (account == null) return;

        String amountText = ask("Enter withdrawal amount: ");

        if (amountText == null) return;

        BigDecimal amount = parseAmount(amountText);

        AccountService.withdraw(account, amount);

        logger.info(
                "Withdrawal completed. Account: "
                        + account.accountNumber
                        + ", Amount: " + amount
        );

        System.out.println("Withdrawal successful!");
        System.out.println("New balance: $" + account.balance);
    }


    // ==============================
    // CREATE ACCOUNTS
    // ==============================

    private static void createAccount() throws Exception {

        System.out.println("\n===== CREATE ACCOUNT =====");

        System.out.println("1. Savings");
        System.out.println("2. Checking");
        System.out.println("3. Deposit");
        System.out.println("4. Loan");

        String choice = ask("Choose account type: ");

        if (choice == null) return;

        AccountType type;
        BigDecimal balance = BigDecimal.ZERO;
        BigDecimal rate;

        switch (choice) {

            case "1":
                type = AccountType.SAVINGS;
                rate = new BigDecimal("0.04");
                break;

            case "2":
                type = AccountType.CHECKING;
                rate = BigDecimal.ZERO;
                break;

            case "3":
                type = AccountType.DEPOSIT;
                rate = new BigDecimal("0.05");
                break;

            case "4":

                type = AccountType.LOAN;

                String principalText =
                        ask("Enter loan principal: ");

                if (principalText == null) return;

                balance = parseAmount(principalText);

                rate = BigDecimal.ZERO;
                break;

            default:
                System.out.println("Invalid account type.");
                return;
        }

        Account account = new Account(type, balance, rate);

        currentCustomer.accounts.add(account);

        logger.info("New account created: " + account.accountNumber);

        System.out.println("\nAccount created successfully!");
        System.out.println("Account Number: " + account.accountNumber);
        System.out.println("Account Type: " + account.type);
        System.out.println("Opening Balance: $" + account.balance);
    }


    // ==============================
    // QUARTERLY SAVINGS INTEREST
    // ==============================

    private static void creditQuarterlyInterest()
            throws Exception {

        System.out.println("\n===== QUARTERLY INTEREST =====");

        Account account = askForAccount();

        if (account == null) return;

        if (account.type != AccountType.SAVINGS) {

            throw new BankingException(
                    "Quarterly interest is only available for savings accounts."
            );
        }

        YearMonth now = YearMonth.now();

        int quarter = (now.getMonthValue() - 1) / 3 + 1;

        String currentPeriod =
                now.getYear() + "-Q" + quarter;

        if (account.lastInterestPeriod.equals(currentPeriod)) {

            System.out.println(
                    "Interest has already been credited this quarter."
            );
            return;
        }

        BigDecimal quarterlyRate =
                account.annualInterestRate.divide(
                        new BigDecimal("4"),
                        10,
                        RoundingMode.HALF_UP
                );

        BigDecimal interest =
                account.balance.multiply(quarterlyRate)
                        .setScale(2, RoundingMode.HALF_UP);

        account.balance = account.balance.add(interest);

        account.lastInterestPeriod = currentPeriod;

        logger.info(
                "Quarterly interest credited to "
                        + account.accountNumber
                        + ": " + interest
        );

        System.out.println("Quarterly interest credited!");
        System.out.println("Interest earned: $" + interest);
        System.out.println("New balance: $" + account.balance);
    }


    // ==============================
    // LOAN EMI CALCULATOR
    // ==============================

    private static void calculateLoanEMI() throws Exception {

        System.out.println("\n===== LOAN EMI CALCULATOR =====");

        String principalText =
                ask("Enter loan amount: ");

        if (principalText == null) return;

        BigDecimal principal = parseAmount(principalText);

        String rateText =
                ask("Enter annual interest rate (%): ");

        if (rateText == null) return;

        BigDecimal annualRate;

        try {
            annualRate = new BigDecimal(rateText.trim());
        } catch (NumberFormatException e) {
            throw new BankingException("Invalid interest rate.");
        }

        if (annualRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new BankingException(
                    "Interest rate cannot be negative."
            );
        }

        String monthsText =
                ask("Enter loan duration in months: ");

        if (monthsText == null) return;

        int months;

        try {
            months = Integer.parseInt(monthsText.trim());
        } catch (NumberFormatException e) {
            throw new BankingException(
                    "Enter a valid number of months."
            );
        }

        if (months <= 0) {
            throw new BankingException(
                    "Loan duration must be greater than zero."
            );
        }

        double p = principal.doubleValue();

        double monthlyRate =
                annualRate.doubleValue() / 1200.0;

        double emi;

        if (monthlyRate == 0) {

            emi = p / months;

        } else {

            double power = Math.pow(1 + monthlyRate, months);

            emi = p * monthlyRate * power / (power - 1);
        }

        BigDecimal monthlyPayment =
                BigDecimal.valueOf(emi).setScale(
                        2, RoundingMode.HALF_UP
                );

        BigDecimal totalPayment =
                monthlyPayment.multiply(
                        BigDecimal.valueOf(months)
                );

        System.out.println("\n===== EMI RESULTS =====");

        System.out.println(
                "Loan Principal: $" + principal
        );

        System.out.println(
                "Monthly EMI: $" + monthlyPayment
        );

        System.out.println(
                "Number of Payments: " + months
        );

        System.out.println(
                "Total Payment: $" + totalPayment
        );

        logger.info("Loan EMI calculated successfully.");
    }


    // ==============================
    // AMOUNT VALIDATION
    // ==============================

    private static BigDecimal parseAmount(String value)
            throws BankingException {

        try {

            BigDecimal amount =
                    new BigDecimal(value.trim());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BankingException(
                        "Amount must be greater than zero."
                );
            }

            return amount.setScale(
                    2, RoundingMode.HALF_UP
            );

        } catch (NumberFormatException e) {

            throw new BankingException(
                    "Please enter a valid money amount."
            );
        }
    }


    // ==============================
    // MAIN APPLICATION
    // ==============================

    public static void main(String[] args) {

        setupLogger();
        startInputReader();

        boolean running = true;

        System.out.println("==================================");
        System.out.println("       WELCOME TO OUR BANK");
        System.out.println("==================================");

        while (running) {

            try {

                if (currentCustomer == null) {

                    System.out.println("\n===== MAIN MENU =====");
                    System.out.println("1. Register");
                    System.out.println("2. Login");
                    System.out.println("3. Reset Password");
                    System.out.println("4. Exit");

                    String choice =
                            ask("Choose an option: ");

                    if (choice == null) break;

                    switch (choice.trim()) {

                        case "1":
                            register();
                            break;

                        case "2":
                            login();
                            break;

                        case "3":
                            resetPassword();
                            break;

                        case "4":
                            System.out.println(
                                    "Thank you for using our bank!"
                            );
                            running = false;
                            break;

                        default:
                            System.out.println(
                                    "Invalid option. Try again."
                            );
                    }

                } else {

                    System.out.println(
                            "\n===== CUSTOMER DASHBOARD ====="
                    );

                    System.out.println(
                            "Welcome, " + currentCustomer.username
                    );

                    System.out.println("1. Display Accounts");
                    System.out.println("2. Check Account Balance");
                    System.out.println("3. Deposit Money");
                    System.out.println("4. Withdraw Money");
                    System.out.println("5. Create Account");
                    System.out.println("6. Credit Quarterly Interest");
                    System.out.println("7. Calculate Loan EMI");
                    System.out.println("8. Logout");

                    String choice =
                            ask("Choose an option: ");

                    if (choice == null) break;

                    switch (choice.trim()) {

                        case "1":
                            displayAccounts();
                            break;

                        case "2":
                            checkBalance();
                            break;

                        case "3":
                            deposit();
                            break;

                        case "4":
                            withdraw();
                            break;

                        case "5":
                            createAccount();
                            break;

                        case "6":
                            creditQuarterlyInterest();
                            break;

                        case "7":
                            calculateLoanEMI();
                            break;

                        case "8":

                            logger.info("Customer logged out.");

                            currentCustomer = null;

                            System.out.println("Logout successful!");
                            break;

                        default:
                            System.out.println(
                                    "Invalid option. Try again."
                            );
                    }
                }

            } catch (SessionTimeoutException e) {

                logger.warning("Customer session timed out.");

                currentCustomer = null;

                System.out.println("\n" + e.getMessage());
                System.out.println("Please log in again.");

            } catch (BankingException e) {

                logger.warning(e.getMessage());

                System.out.println(
                        "Banking Error: " + e.getMessage()
                );

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
                running = false;

            } catch (Exception e) {

                logger.log(
                        Level.SEVERE,
                        "Unexpected application error.",
                        e
                );

                System.out.println(
                        "An unexpected error occurred. Please try again."
                );
            }
        }

        logger.info("Banking application closed.");
        System.out.println("Application closed.");
    }
}
