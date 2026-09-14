package com.project.payflow.common.ratelimit;

public interface RateLimiter {

    public RateLimitResult check(String key, int maxRequestAllowed, long windowSeconds);
}