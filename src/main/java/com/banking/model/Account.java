package com.banking.model;

import java.math.BigDecimal;

public class Account {
    private static int accountCounter = 100001;

    public String accountNumber;
    public AccountType type;
    public BigDecimal balance;
    public BigDecimal annualInterestRate;
    public String lastInterestPeriod = "";

    public Account(AccountType type, BigDecimal balance, BigDecimal interestRate) {
        this.accountNumber = "ACC" + accountCounter++;
        this.type = type;
        this.balance = balance;
        this.annualInterestRate = interestRate;
    }
}
