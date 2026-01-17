package com.spartaecommerce.common.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PassportSerializer {

    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    public static String serialize(Passport passport) {
        try {
            String json = objectMapper.writeValueAsString(passport);
            byte[] compressed = compress(json.getBytes(UTF_8));
            return Base64.getEncoder().encodeToString(compressed);
        } catch (Exception e) {
            throw new PassportSerializationException("Failed to serialize Passport", e);
        }
    }

    public static Passport deserialize(String serialized) {
        try {
            byte[] compressed = Base64.getDecoder().decode(serialized);
            byte[] decompressed = decompress(compressed);
            String json = new String(decompressed, UTF_8);
            return objectMapper.readValue(json, Passport.class);
        } catch (Exception e) {
            throw new PassportSerializationException("Failed to deserialize Passport", e);
        }
    }

    private static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(data);
        }
        return bos.toByteArray();
    }

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

    public static class PassportSerializationException extends RuntimeException {
        public PassportSerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
