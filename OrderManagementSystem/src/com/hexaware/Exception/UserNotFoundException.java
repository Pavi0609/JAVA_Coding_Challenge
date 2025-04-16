package com.hexaware.Exception;

@SuppressWarnings("serial")
public class UserNotFoundException extends Exception {
    public UserNotFoundException(String message) {
        super(message);
    }
}