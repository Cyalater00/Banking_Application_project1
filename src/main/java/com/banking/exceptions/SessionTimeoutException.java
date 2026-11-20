package com.banking.exceptions;

public class SessionTimeoutException extends Exception {
    public SessionTimeoutException() {
        super("Your session expired due to inactivity.");
    }
}
