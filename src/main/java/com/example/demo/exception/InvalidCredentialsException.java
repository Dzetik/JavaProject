package com.example.demo.exception;

// пользователь не прошёл аутентификацию
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}