package com.example.springproject.aspect;

import com.example.springproject.service.ElasticLoggingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiLoggingAspect {

    private final ElasticLoggingService elasticLoggingService;

    @Around("execution(* com.example.springproject.controller..*(..))")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        String method = "";
        String uri = "";
        String clientIp = "";
        String userAgent = "";

        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            method = request.getMethod();
            uri = request.getRequestURI();
            clientIp = getClientIp(request);
            userAgent = request.getHeader("User-Agent");

            result = joinPoint.proceed();
            return result;

        } catch (Exception e) {
            // Log error to Elasticsearch
            if (!method.isEmpty()) {
                elasticLoggingService.logError(method, uri, e.getMessage(), clientIp, userAgent);
            }
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Only log successful requests
            if (result != null && !method.isEmpty()) {
                elasticLoggingService.logApiCall(method, uri, 200, duration, clientIp, userAgent);
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}