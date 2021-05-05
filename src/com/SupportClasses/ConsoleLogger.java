package com.SupportClasses;

public class ConsoleLogger implements Logger {

    public ConsoleLogger() {
    }

    @Override
    public void logMessage(String message) {
        System.out.println("INFO: " + message);
    }

    @Override
    public void logError(String message) {
        System.err.println("ERROR: " + message);
    }

}
