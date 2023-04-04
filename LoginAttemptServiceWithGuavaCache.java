package com.altice.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private final int MAX_ATTEMPT = 6;
    private final int EXPIRE_AFTER_HOURS = 3;
    private LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService() {
        attemptsCache = CacheBuilder.newBuilder().
                expireAfterWrite(EXPIRE_AFTER_HOURS, TimeUnit.HOURS)
                .build(new CacheLoader<String, Integer>() {
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    public void loginSucceeded(String username, String ipAddress) {
        attemptsCache.invalidate(asKey(username, ipAddress));
    }

    public void loginFailed(String username, String ipAddress) {
        String key = asKey(username, ipAddress);
        int attempts = 0;
        try {
            attempts = attemptsCache.get(key);
        } catch (ExecutionException e) {
            attempts = 0;
        }
        attempts++;
        attemptsCache.put(key, attempts);
    }

    public boolean isBlocked(String username, String ipAddress) {
        String key = asKey(username, ipAddress);
        try {
            return attemptsCache.get(key) >= MAX_ATTEMPT;
        } catch (ExecutionException e) {
            return false;
        }
    }

    private String asKey(String username, String ipAddress) {
        StringBuilder sb = new StringBuilder();
        sb.append(username);
        sb.append("_");
        sb.append(ipAddress);
        return sb.toString();
    }

}
