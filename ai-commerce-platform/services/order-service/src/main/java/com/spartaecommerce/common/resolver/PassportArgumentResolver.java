package com.spartaecommerce.common.web.resolver;

import com.spartaecommerce.common.auth.Passport;
import com.spartaecommerce.common.auth.PassportContext;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.common.web.annotation.CurrentPassport;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @CurrentPassport 어노테이션이 붙은 파라미터에 Passport 객체를 주입하는 ArgumentResolver
 *
 * <p>동작 흐름:</p>
 * 1. PassportInterceptor가 X-Passport 헤더를 파싱하여 PassportContext에 저장
 * 2. 이 Resolver가 PassportContext에서 Passport를 조회
 * 3. 컨트롤러 메서드 파라미터로 주입
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
 *     // ... business logic
 * }
 * }
 * </pre>
 */
@Component
public class PassportArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentPassport.class)
            && Passport.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {
        Passport passport = PassportContext.get();

        if (passport == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return passport;
    }
}
