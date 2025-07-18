package org.beaconfire.authentication.controller;

import org.beaconfire.authentication.dto.response.ApiResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.net.URI;

@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Value("${spring.application.name:beaconfire}")
    private String appName;

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        // This method is primarily for filtering based on return type,
        // but we'll use the beforeBodyWrite to check the request path.
        // For now, let it always support, and filter in beforeBodyWrite for path.
        return true; // We'll handle filtering by path in beforeBodyWrite
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        URI uri = request.getURI();
        String path = uri.getPath();

        // Check if the path should be excluded from wrapping
        if (path.startsWith("/actuator") || path.startsWith("/openapi/api-docs") || path.startsWith("/swagger-ui")
                || path.startsWith("/login")) {
            return body;
        }

        if (body instanceof ApiResponse) {
            return body;
        }

        String traceId = MDC.get("traceId");
        String host = request.getURI().getHost();

        return ApiResponse.builder()
                .success(true)
                .data(body)
                .traceId(traceId)
                .host(host)
                .build();
    }
}