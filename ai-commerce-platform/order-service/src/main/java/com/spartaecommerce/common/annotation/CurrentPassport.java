package com.spartaecommerce.common.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 현재 요청의 Passport 객체를 컨트롤러 메서드 파라미터에 주입하기 위한 어노테이션
 * X-Passport 헤더에서 역직렬화된 Passport 객체를 주입합니다.
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @PostMapping("/api/v1/orders")
 * public ResponseEntity<?> createOrder(
 *     @CurrentPassport Passport passport,
 *     @RequestBody OrderCreateRequest request
 * ) {
 *     Long userId = passport.userId();
 *     String username = passport.username();
 *     // ... business logic
 * }
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentPassport {
}
