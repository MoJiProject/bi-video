package com.moji.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Map;

@ControllerAdvice
public class EncryptedResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final String ENCRYPTED_HEADER = "X-Encrypted";

    private static final String PLAIN_RESPONSE_HEADER = "X-Plain-Response";

    private final AesCryptoService cryptoService;

    private final ObjectMapper objectMapper;

    public EncryptedResponseBodyAdvice(AesCryptoService cryptoService, ObjectMapper objectMapper) {
        this.cryptoService = cryptoService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null || body instanceof byte[] || shouldSkip(request)) {
            return body;
        }

        try {
            response.getHeaders().set(ENCRYPTED_HEADER, "true");
            return Map.of("payload", cryptoService.encrypt(objectMapper.writeValueAsString(body)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("接口响应序列化失败", e);
        }
    }

    private boolean shouldSkip(ServerHttpRequest request) {
        if ("true".equalsIgnoreCase(request.getHeaders().getFirst(PLAIN_RESPONSE_HEADER))) {
            return true;
        }

        String path = request.getURI().getPath();
        return path.contains("/v3/api-docs")
                || path.contains("/swagger")
                || path.contains("/doc.html")
                || path.contains("/webjars");
    }
}