package com.spartaecommerce.gateway.audit;

import com.spartaecommerce.common.auth.Passport;
import com.spartaecommerce.gateway.filter.auth.strategy.PassportAuthenticationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.http.HttpMethod.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogGlobalFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_START_TIME_ATTR = "requestStartTime";
    private static final String REQUEST_ID_ATTR = "requestId";
    private static final Pattern PATH_PATTERN = Pattern.compile("^/([^/]+)(?:/([^/]+))?");

    private final AuditLogService auditLogService;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1; // 인증 필터 다음에 실행
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        exchange.getAttributes().put(REQUEST_ID_ATTR, requestId);
        exchange.getAttributes().put(REQUEST_START_TIME_ATTR, startTime);

        return chain.filter(exchange)
            .then(Mono.fromRunnable(() -> {
                try {
                    logAuditEvent(exchange, requestId, startTime);
                } catch (Exception e) {
                    log.error("Failed to log audit event", e);
                }
            }));
    }

    private void logAuditEvent(ServerWebExchange exchange, String requestId, long startTime) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        long responseTime = System.currentTimeMillis() - startTime;

        // Passport에서 사용자 정보 추출
        Passport passport = exchange.getAttribute(PassportAuthenticationStrategy.PASSPORT_ATTRIBUTE_KEY);

        // 경로에서 service와 resource 추출
        String path = request.getPath().value();
        PathInfo pathInfo = extractPathInfo(path);

        // 감사 로그 이벤트 생성
        AuditLogEvent event = AuditLogEvent.builder()
            .requestId(requestId)
            .timestamp(Instant.now().toString())
            .userId(extractUserId(request, passport))
            .username(extractUsername(passport))
            .method(request.getMethod().name())
            .path(path)
            .query(request.getURI().getQuery())
            .statusCode(response.getStatusCode() != null ? response.getStatusCode().value() : null)
            .responseTime(responseTime)
            .clientIp(extractClientIp(request))
            .userAgent(request.getHeaders().getFirst("User-Agent"))
            .action(determineAction(request.getMethod(), path))
            .service(pathInfo.service())
            .resource(pathInfo.resource())
            .metadata(buildMetadata(exchange, request))
            .build();

        auditLogService.sendAuditLog(event);
    }

    private String extractUserId(ServerHttpRequest request, Passport passport) {
        if (passport != null) {
            return String.valueOf(passport.userId());
        }
        return request.getHeaders().getFirst("X-User-Id");
    }

    private String extractUsername(Passport passport) {
        return passport != null ? passport.username() : null;
    }

    private String extractClientIp(ServerHttpRequest request) {
        String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }

        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }

    private PathInfo extractPathInfo(String path) {
        Matcher matcher = PATH_PATTERN.matcher(path);
        if (matcher.find()) {
            return new PathInfo(matcher.group(1), matcher.group(2));
        }
        return new PathInfo(null, null);
    }

    private String determineAction(HttpMethod method, String path) {
        if (method == null) {
            return "UNKNOWN";
        }

        if (GET.equals(method)) {
            return "READ";
        } else if (POST.equals(method)) {
            return path.contains("login") || path.contains("auth") ? "LOGIN" : "CREATE";
        } else if (PUT.equals(method) || PATCH.equals(method)) {
            return "UPDATE";
        } else if (DELETE.equals(method)) {
            return "DELETE";
        } else {
            return method.name();
        }
    }

    private Map<String, Object> buildMetadata(ServerWebExchange exchange, ServerHttpRequest request) {
        Map<String, Object> metadata = new HashMap<>();

        // 특정 헤더 추가
        String contentType = request.getHeaders().getFirst("Content-Type");
        if (contentType != null) {
            metadata.put("contentType", contentType);
        }

        String referer = request.getHeaders().getFirst("Referer");
        if (referer != null) {
            metadata.put("referer", referer);
        }

        return metadata.isEmpty() ? null : metadata;
    }

    private record PathInfo(String service, String resource) {
    }
}
