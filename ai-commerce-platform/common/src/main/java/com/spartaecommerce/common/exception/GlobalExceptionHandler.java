package com.spartaecommerce.common.exception;

import com.spartaecommerce.common.domain.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + " : " + error.getDefaultMessage())
            .orElse("Invalid request");

        return ResponseEntity
            .badRequest()
            .body(CommonResponse.error(ErrorCode.INVALID_REQUEST.getCode(), message));
    }


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse<Void>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        String errorCodeMessage = errorCode.getMessage();
        String customMessage = ex.getMessage();

        String errorMessage = errorCodeMessage + customMessage;

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(CommonResponse.error(errorCode.getCode(), errorMessage));
    }
}
