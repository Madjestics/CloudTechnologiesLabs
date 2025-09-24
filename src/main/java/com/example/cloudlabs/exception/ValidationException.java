package com.example.cloudlabs.exception;

/**
 * Ошибка валидации
 */
public class ValidationException extends RuntimeException{
    public ValidationException(String message) {
        super(message);
    }
}
