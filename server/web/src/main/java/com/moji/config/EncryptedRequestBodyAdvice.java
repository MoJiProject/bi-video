package com.moji.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

@ControllerAdvice
public class EncryptedRequestBodyAdvice extends RequestBodyAdviceAdapter {

    private static final String ENCRYPTED_HEADER = "X-Encrypted";

    private final AesCryptoService cryptoService;

    private final ObjectMapper objectMapper;

    public EncryptedRequestBodyAdvice(AesCryptoService cryptoService, ObjectMapper objectMapper) {
        this.cryptoService = cryptoService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        String encryptedHeader = inputMessage.getHeaders().getFirst(ENCRYPTED_HEADER);
        if (!"true".equalsIgnoreCase(encryptedHeader)) {
            return inputMessage;
        }

        String body = StreamUtils.copyToString(inputMessage.getBody(), StandardCharsets.UTF_8);
        if (body.isBlank()) {
            return inputMessage;
        }

        JsonNode jsonNode = objectMapper.readTree(body);
        JsonNode payloadNode = jsonNode.get("payload");
        if (payloadNode == null || !payloadNode.isTextual()) {
            throw new IllegalArgumentException("加密请求缺少 payload 字段");
        }

        String decryptedBody = cryptoService.decrypt(payloadNode.asText());
        return new DecryptedHttpInputMessage(inputMessage.getHeaders(), decryptedBody);
    }

    private static class DecryptedHttpInputMessage implements HttpInputMessage {

        private final HttpHeaders headers;

        private final String body;

        private DecryptedHttpInputMessage(HttpHeaders headers, String body) {
            this.headers = headers;
            this.body = body;
        }

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }
    }
}