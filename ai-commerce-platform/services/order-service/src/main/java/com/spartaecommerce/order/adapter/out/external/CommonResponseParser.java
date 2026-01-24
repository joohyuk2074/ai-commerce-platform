package com.spartaecommerce.order.adapter.out.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartaecommerce.api.response.CommonResponse;
import com.spartaecommerce.exception.BusinessException;
import com.spartaecommerce.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 외부 API 응답을 CommonResponse로 파싱하는 유틸리티
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommonResponseParser {

  private final ObjectMapper objectMapper;

  /**
   * JSON 응답을 CommonResponse<T>로 파싱
   *
   * @param response JSON 문자열
   * @param typeRef TypeReference for deserialization
   * @return 파싱된 데이터
   * @throws BusinessException 파싱 실패 시
   */
  public <T> T parse(String response, TypeReference<CommonResponse<T>> typeRef) {
    try {
      CommonResponse<T> commonResponse = objectMapper.readValue(response, typeRef);
      return commonResponse.getData();
    } catch (Exception e) {
      log.error("Failed to parse API response", e);
      throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
    }
  }
}
