package com.spartaecommerce.auth.domain.port.out;

/**
 * JWT 토큰 생성 및 검증을 위한 Output Port
 */
public interface JwtTokenPort {

    /**
     * Access Token 생성
     *
     * @param userId 사용자 ID
     * @param username 사용자 이름
     * @param role 사용자 권한
     * @return Access Token
     */
    String createAccessToken(Long userId, String username, String role);

    /**
     * Refresh Token 생성
     *
     * @param userId 사용자 ID
     * @return Refresh Token
     */
    String createRefreshToken(Long userId);

    /**
     * JWT 토큰 검증
     *
     * @param token JWT 토큰
     * @return 검증 결과
     */
    boolean validateToken(String token);

    /**
     * JWT 토큰에서 userId 추출
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    Long getUserIdFromToken(String token);

    /**
     * JWT 토큰에서 username 추출
     *
     * @param token JWT 토큰
     * @return 사용자 이름
     */
    String getUsernameFromToken(String token);

    /**
     * JWT 토큰에서 role 추출
     *
     * @param token JWT 토큰
     * @return 사용자 권한
     */
    String getRoleFromToken(String token);
}
