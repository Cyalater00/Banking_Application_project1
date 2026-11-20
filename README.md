# Banking Application

A Java command line banking application. Customers can register, sign in, manage accounts, and use basic banking features.

## Features

- Customer registration, login, and password recovery
- Savings, checking, deposit, and loan accounts
- View balances, deposit, and withdraw
- Savings interest and loan payment calculations
- Password hashing and session timeout

## Run

Requires Java 17 or later. From the project directory:

```sh
mkdir -p out
javac -d out $(find src/main/java -name '*.java') Main.java
java -cp out Main
```

Account data is stored in memory and is cleared when the program exits.
