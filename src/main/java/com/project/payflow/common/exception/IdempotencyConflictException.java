package com.project.payflow.common.exception;

public class IdempotencyConflictException extends RuntimeException{

    public IdempotencyConflictException(String message){
        super(message);
    }
}
