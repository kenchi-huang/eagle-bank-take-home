package com.eaglebank.api.exception;

public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message); // FIX: Pass the message to the parent constructor
    }
}
