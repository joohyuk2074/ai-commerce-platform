package com.spartaecommerce.common.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Passport 직렬화/역직렬화 유틸리티
 *
 * Passport를 HTTP 헤더로 전달하기 위해 직렬화합니다:
 * 1. JSON 직렬화
 * 2. GZIP 압축
 * 3. Base64 인코딩
 *
 * 헤더 크기 제한(8KB)을 고려하여 압축을 적용합니다.
 */
public class PassportSerializer {

    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    /**
     * Passport를 직렬화하여 Base64 문자열로 변환
     *
     * @param passport Passport 객체
     * @return Base64 인코딩된 문자열
     */
    public static String serialize(Passport passport) {
        try {
            // 1. JSON 직렬화
            String json = objectMapper.writeValueAsString(passport);

            // 2. GZIP 압축
            byte[] compressed = compress(json.getBytes(UTF_8));

            // 3. Base64 인코딩
            return Base64.getEncoder().encodeToString(compressed);

        } catch (Exception e) {
            throw new PassportSerializationException("Failed to serialize Passport", e);
        }
    }

    /**
     * Base64 문자열을 역직렬화하여 Passport 객체로 변환
     *
     * @param serialized Base64 인코딩된 문자열
     * @return Passport 객체
     */
    public static Passport deserialize(String serialized) {
        try {
            // 1. Base64 디코딩
            byte[] compressed = Base64.getDecoder().decode(serialized);

            // 2. GZIP 압축 해제
            byte[] decompressed = decompress(compressed);

            // 3. JSON 역직렬화
            String json = new String(decompressed, UTF_8);
            return objectMapper.readValue(json, Passport.class);

        } catch (Exception e) {
            throw new PassportSerializationException("Failed to deserialize Passport", e);
        }
    }

    /**
     * GZIP 압축
     */
    private static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(data);
        }
        return bos.toByteArray();
    }

    /**
     * GZIP 압축 해제
     */
    private static byte[] decompress(byte[] compressed) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try (GZIPInputStream gzip = new GZIPInputStream(bis)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzip.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
        }

        return bos.toByteArray();
    }

    /**
     * Passport 직렬화 예외
     */
    public static class PassportSerializationException extends RuntimeException {
        public PassportSerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
