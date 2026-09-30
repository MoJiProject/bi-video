package com.moji.config;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AesCryptoService {

    private final ApiCryptoProperties properties;

    public AesCryptoService(ApiCryptoProperties properties) {
        this.properties = properties;
    }

    public String encrypt(String content) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), getIv());
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("接口响应加密失败", e);
        }
    }

    public String decrypt(String payload) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), getIv());
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(payload));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("接口请求解密失败", e);
        }
    }

    private SecretKeySpec getSecretKey() {
        return new SecretKeySpec(properties.getKey().getBytes(StandardCharsets.UTF_8), "AES");
    }

    private IvParameterSpec getIv() {
        return new IvParameterSpec(properties.getIv().getBytes(StandardCharsets.UTF_8));
    }
}