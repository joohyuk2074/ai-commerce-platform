package com.spartaecommerce.config;

import com.spartaecommerce.exception.BusinessException;
import com.spartaecommerce.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
public class WebClientConfig {

  @Value("${service.catalog.url:http://localhost:8083}")
  private String catalogServiceUrl;

  @Bean
  public RestClient catalogServiceRestClient() {
    return RestClient.builder()
        .baseUrl(catalogServiceUrl)
        .defaultStatusHandler(
            HttpStatusCode::is4xxClientError,
            (request, response) -> {
              log.error("Catalog service client error: status={}, uri={}",
                  response.getStatusCode(), request.getURI());
              throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
            })
        .defaultStatusHandler(
            HttpStatusCode::is5xxServerError,
            (request, response) -> {
              log.error("Catalog service server error: status={}, uri={}",
                  response.getStatusCode(), request.getURI());
              throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
            })
        .build();
  }
}
