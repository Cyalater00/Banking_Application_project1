package com.banking.service;

import com.banking.exceptions.BankingException;
import com.banking.model.Account;
import com.banking.model.AccountType;
import java.math.BigDecimal;

/** Applies the existing deposit and withdrawal rules to an account. */
public class AccountService {
    public static void deposit(Account account, BigDecimal amount) throws BankingException {
        if (account.type == AccountType.LOAN) {
            throw new BankingException("Cannot deposit into a loan account.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Amount must be greater than zero.");
        }
        account.balance = account.balance.add(amount);
    }

    public static void withdraw(Account account, BigDecimal amount) throws BankingException {
        if (account.type == AccountType.LOAN) {
            throw new BankingException("Withdrawals are not allowed from loan accounts.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Amount must be greater than zero.");
        }
        if (amount.compareTo(account.balance) > 0) {
            throw new BankingException("Insufficient funds.");
        }
        account.balance = account.balance.subtract(amount);
    }
}
