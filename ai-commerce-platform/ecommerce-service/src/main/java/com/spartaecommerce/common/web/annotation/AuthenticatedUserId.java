package com.spartaecommerce.common.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 인증된 사용자의 ID를 컨트롤러 메서드 파라미터에 주입하기 위한 어노테이션
 * X-User-Id 헤더에서 userId를 추출하여 주입합니다.
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @GetMapping("/api/v1/cart-items")
 * public ResponseEntity<?> getCart(@AuthenticatedUserId Long userId) {
 *     // userId를 사용한 비즈니스 로직
 * }
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthenticatedUserId {
}
