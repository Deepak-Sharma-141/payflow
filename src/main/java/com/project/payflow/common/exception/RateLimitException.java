package com.project.payflow.common.exception;

import lombok.Getter;

@Getter
public class RateLimitException extends RuntimeException{
    private final int retryAfterSeconds;
    private final int remining;

    public RateLimitException(String message, int retryAfterSeconds){
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
        this.remining = 0;
    }
}
