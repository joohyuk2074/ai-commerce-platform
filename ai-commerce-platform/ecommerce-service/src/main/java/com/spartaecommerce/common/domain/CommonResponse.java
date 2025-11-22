package com.spartaecommerce.common.domain;

import com.spartaecommerce.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonResponse<T> {

    private String code;

    private String message;

    private T data;


    public static CommonResponse<IdResponse> create(Long id) {
        return new CommonResponse<>(null, "Created successfully", new IdResponse(id));
    }

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(null, "Success", data);
    }

    public static <T> CommonResponse<T> success(String message, T data) {
        return new CommonResponse<>(null, message, data);
    }

    public static <T> CommonResponse<T> error(String errorCode, String message) {
        return new CommonResponse<>(errorCode, message, null);
    }

    public static <T> CommonResponse<T> error(ErrorCode errorCode, String message) {
        return new CommonResponse<>(errorCode.getCode(), message, null);
    }

}