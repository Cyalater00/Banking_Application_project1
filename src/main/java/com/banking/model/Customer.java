package com.banking.model;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    public String username;
    public byte[] passwordSalt;
    public byte[] passwordHash;
    public byte[] recoverySalt;
    public byte[] recoveryHash;
    public List<Account> accounts = new ArrayList<>();

    public Customer(String username, byte[] passwordSalt, byte[] passwordHash,
                    byte[] recoverySalt, byte[] recoveryHash) {
        this.username = username;
        this.passwordSalt = passwordSalt;
        this.passwordHash = passwordHash;
        this.recoverySalt = recoverySalt;
        this.recoveryHash = recoveryHash;
    }
}
