package com.project.payflow.common.idempotency;

import com.project.payflow.common.exception.IdempotencyConflictException;
import com.project.payflow.merchant.security.MerchantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Set<String> GUARDED_METHODS = Set.of("POST", "PUT", "PATCH");
    private static final Duration IN_PROGRESS_TTL = Duration.ofSeconds(30);
    private static final Duration COMPLETED_TTL = Duration.ofHours(24);
    private static final String SEPARATOR = "|";

    private final MerchantContext merchantContext;
    private final IdempotencyStore idempotencyStore;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if(!GUARDED_METHODS.contains(request.getMethod())){
            filterChain.doFilter(request, response);
            return;
        }

        String rawKey = request.getHeader("X-Idempotency-Key");
        if(rawKey == null || rawKey.isBlank()){
            filterChain.doFilter(request, response);
            return;
        }

        UUID merchantId = merchantContext.getMerchantId();
        String key = merchantId != null ? merchantId+":"+rawKey : rawKey;

        boolean claimed = idempotencyStore.setIfAbsent(key, IN_PROGRESS_TTL);

        if(!claimed){
            // another thread has already claimed the key
            Optional<String> existing = idempotencyStore.get(key);

            if(existing.isPresent() && !IdempotencyStore.IN_PROGRESS.equals(existing.get())){
                    // it's not in-progress, but coming from the actual value stored in redis
                replay(request, response, existing.get());
            }else {
                // it's in progress by another thread
                var ex = new IdempotencyConflictException("A request with this idempotency key is in progress");
                handlerExceptionResolver.resolveException(request, response, null, ex);
            }
            return;
        }
        // first time claim
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(request, wrapper);
        }finally {
            int status = wrapper.getStatus();
            byte[] bodyBytes = wrapper.getContentAsByteArray();
            String body = new String(bodyBytes, StandardCharsets.UTF_8);

            if(status < 400 && bodyBytes.length > 0){
                String stored = status + SEPARATOR + body;
                idempotencyStore.store(key, stored, COMPLETED_TTL);
                log.debug("IdempotencyFilter: stored response status={} key={}", status, key);
            }else {
                idempotencyStore.delete(key);
                log.debug("IdempotencyFilter: deleted palceholder after error status={} key={}", status, key);
            }
            wrapper.copyBodyToResponse();
        }
    }
    private void replay(HttpServletRequest request, HttpServletResponse response, String stored) throws IOException {
        int separatorIndex = stored.indexOf(SEPARATOR);
        if(separatorIndex < 0){
            var ex = new IdempotencyConflictException("A request with this idempotency key is in progress");
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }

        int status = Integer.parseInt(stored.substring(0, separatorIndex));
        String body = stored.substring(separatorIndex+1);

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8);
        response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
    }
}
