package com.spartaecommerce.common.web.resolver;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.common.web.annotation.AuthenticatedUserId;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @AuthenticatedUserId 어노테이션이 붙은 파라미터에 userId를 주입하는 ArgumentResolver
 *
 * <p>동작 흐름:</p>
 * 1. Request Attribute에서 X-User-Id 조회 (현재: Interceptor가 설정)
 * 2. 없으면 헤더에서 X-User-Id 조회 (향후: Gateway가 설정)
 * 3. userId를 Long으로 변환하여 컨트롤러에 주입
 *
 * <p>마이그레이션 시나리오:</p>
 * - 현재: Interceptor → Request Attribute → ArgumentResolver → Controller
 * - Gateway 도입 후: Gateway → X-User-Id 헤더 → ArgumentResolver → Controller
 */
@Component
public class AuthenticatedUserArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticatedUserId.class)
            && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        if (request == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 1. Request Attribute에서 조회 (현재: Interceptor가 설정)
        Object userIdAttribute = request.getAttribute(USER_ID_HEADER);
        if (userIdAttribute instanceof Long) {
            return userIdAttribute;
        }

        // 2. 헤더에서 조회 (향후: Gateway가 설정)
        String userIdHeader = request.getHeader(USER_ID_HEADER);
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            try {
                return Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }
        }

        // 3. userId를 찾을 수 없는 경우
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
}
