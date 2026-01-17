package com.spartaecommerce.common.auth;

/**
 * Passport 컨텍스트
 *
 * ThreadLocal을 사용하여 현재 요청의 Passport를 저장하고 조회합니다.
 * Backend 서비스에서 X-Passport 헤더로부터 파싱한 Passport를 저장합니다.
 *
 * 사용 예시:
 * <pre>
 * // Interceptor에서 설정
 * Passport passport = PassportSerializer.deserialize(passportHeader);
 * PassportContext.set(passport);
 *
 * // Service에서 조회
 * Passport passport = PassportContext.get();
 * Long userId = passport.getUserId();
 * </pre>
 */
public class PassportContext {

    private static final ThreadLocal<Passport> PASSPORT_HOLDER = new ThreadLocal<>();

    /**
     * 현재 요청의 Passport 설정
     */
    public static void set(Passport passport) {
        PASSPORT_HOLDER.set(passport);
    }

    /**
     * 현재 요청의 Passport 조회
     *
     * @return Passport 객체 (없으면 null)
     */
    public static Passport get() {
        return PASSPORT_HOLDER.get();
    }

    /**
     * 현재 요청의 Passport 조회 (필수)
     *
     * @return Passport 객체
     * @throws PassportNotFoundException Passport가 없는 경우
     */
    public static Passport require() {
        Passport passport = PASSPORT_HOLDER.get();
        if (passport == null) {
            throw new PassportNotFoundException("Passport not found in context");
        }
        return passport;
    }

    /**
     * 현재 요청의 사용자 ID 조회
     *
     * @return 사용자 ID
     * @throws PassportNotFoundException Passport가 없는 경우
     */
    public static Long getUserId() {
        return require().userId();
    }

    /**
     * 현재 요청의 사용자명 조회
     *
     * @return 사용자명
     * @throws PassportNotFoundException Passport가 없는 경우
     */
    public static String getUsername() {
        return require().username();
    }

    /**
     * Passport 제거 (요청 처리 완료 후)
     */
    public static void clear() {
        PASSPORT_HOLDER.remove();
    }

    /**
     * Passport 존재 여부 확인
     */
    public static boolean exists() {
        return PASSPORT_HOLDER.get() != null;
    }

    /**
     * Passport를 찾을 수 없을 때 발생하는 예외
     */
    public static class PassportNotFoundException extends RuntimeException {
        public PassportNotFoundException(String message) {
            super(message);
        }
    }
}
