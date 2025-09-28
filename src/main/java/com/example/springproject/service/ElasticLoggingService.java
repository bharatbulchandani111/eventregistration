package com.example.springproject.service;

import com.example.springproject.model.ApiLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticLoggingService {

    private final ElasticsearchOperations elasticsearchOperations;

    @Async
    public void logApiCall(String method, String uri, int statusCode, long durationMs, String clientIp, String userAgent) {
        try {
            ApiLog apiLog = ApiLog.builder()
                    .timestamp(Instant.now().toEpochMilli()) // Use epoch milliseconds
                    .method(method)
                    .uri(uri)
                    .statusCode(statusCode)
                    .durationMs(durationMs)
                    .clientIp(clientIp)
                    .userAgent(userAgent)
                    .message(String.format("%s %s - %d (%dms)", method, uri, statusCode, durationMs))
                    .level("INFO")
                    .logger("API_LOGGER")
                    .build();

            elasticsearchOperations.save(apiLog);
            log.debug("✅ Log sent to Elasticsearch: {} {} - {}ms", method, uri, durationMs);

        } catch (Exception e) {
            log.error("❌ Failed to send log to Elasticsearch", e);
        }
    }

    @Async
    public void logError(String method, String uri, String errorMessage, String clientIp, String userAgent) {
        try {
            ApiLog apiLog = ApiLog.builder()
                    .timestamp(Instant.now().toEpochMilli()) // Use epoch milliseconds
                    .method(method)
                    .uri(uri)
                    .statusCode(500)
                    .clientIp(clientIp)
                    .userAgent(userAgent)
                    .message(String.format("Error in %s %s: %s", method, uri, errorMessage))
                    .level("ERROR")
                    .logger("API_LOGGER")
                    .build();

            elasticsearchOperations.save(apiLog);
            log.debug("✅ Error log sent to Elasticsearch: {} {} - {}", method, uri, errorMessage);

        } catch (Exception e) {
            log.error("❌ Failed to send error log to Elasticsearch", e);
        }
    }
}