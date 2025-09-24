package com.example.cloudlabs.exception;

/**
 * ошибка когда не найдена сущность
 */
public class EntityNotFoundException extends RuntimeException{
    public EntityNotFoundException(String message) {
        super(message);
    }
}
