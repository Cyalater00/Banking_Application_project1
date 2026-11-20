package com.banking.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.Level;

public final class AppLogger {
    private static final Logger LOGGER = Logger.getLogger("BankingApplication");

    private AppLogger() { }

    public static Logger getLogger() { return LOGGER; }

    public static void setup() {
        try {
            FileHandler fileHandler = new FileHandler("banking.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.setUseParentHandlers(false);
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.INFO);
        } catch (IOException e) {
            System.out.println("Warning: Could not create log file.");
        }
    }
}
