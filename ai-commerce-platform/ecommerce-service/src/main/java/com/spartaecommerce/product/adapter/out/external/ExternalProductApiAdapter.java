package com.spartaecommerce.product.adapter.out.external;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.product.application.dto.external.ExternalProductApiResponse;
import com.spartaecommerce.product.application.dto.external.ExternalProductData;
import com.spartaecommerce.product.application.dto.result.ExternalProductSyncResult;
import com.spartaecommerce.product.domain.port.out.LoadExternalProductPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ExternalProductApiAdapter implements LoadExternalProductPort {

    private final RestClient restClient;
    private final String externalApiUrl;
    private final String vendor;

    public ExternalProductApiAdapter(
        RestClient.Builder restClientBuilder,
        @Value("${external.product.api.url:https://api.example.com/products}") String externalApiUrl,
        @Value("${external.product.api.vendor:external-vendor}") String vendor
    ) {
        this.restClient = restClientBuilder.build();
        this.externalApiUrl = externalApiUrl;
        this.vendor = vendor;
    }

    @Override
    @Retryable(
        retryFor = {RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 1.5)
    )
    public ExternalProductSyncResult fetchExternalProducts(int page, int size) {
        log.info("Fetching external products from: {} (page={}, size={})", externalApiUrl, page, size);

        try {
            ExternalProductApiResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(externalApiUrl)
                    .queryParam("page", page)
                    .queryParam("size", size)
                    .build())
                .retrieve()
                .body(ExternalProductApiResponse.class);

            if (response == null) {
                log.error("External API returned null response");
                throw new BusinessException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    "External API returned null response"
                );
            }

            // API 응답의 result 필드 확인
            if (!Boolean.TRUE.equals(response.getResult())) {
                String errorMessage = response.getError() != null && response.getError().getErrorMessage() != null
                    ? response.getError().getErrorMessage()
                    : "External API returned failed result";
                log.error("External API request failed: {}", errorMessage);
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, errorMessage);
            }

            // message와 contents 확인
            if (response.getMessage() == null || response.getMessage().getContents() == null) {
                log.error("External API returned null message or contents");
                throw new BusinessException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    "External API returned null message or contents"
                );
            }

            log.info("Successfully fetched {} products from external API",
                response.getMessage().getContents().size());

            // 외부 API 응답을 도메인 Result로 변환
            return convertToDomainResult(response);
        } catch (RestClientException e) {
            log.error("Failed to fetch external products from: {}, error: {}", externalApiUrl, e.getMessage());
            throw e;
        }
    }

    private ExternalProductSyncResult convertToDomainResult(ExternalProductApiResponse apiResponse) {
        List<ExternalProductSyncResult.ExternalProductItem> items = apiResponse.getMessage()
            .getContents()
            .stream()
            .map(this::convertToProductItem)
            .collect(Collectors.toList());

        var pageable = apiResponse.getMessage().getPageable();
        var pageInfo = new ExternalProductSyncResult.PageInfo(
            pageable.getPageNumber() != null ? pageable.getPageNumber() : 0,
            pageable.getPageSize() != null ? pageable.getPageSize() : 0,
            pageable.getTotalPages() != null ? pageable.getTotalPages() : 0,
            pageable.getTotalElements() != null ? pageable.getTotalElements().longValue() : 0L,
            pageable.getLast() != null ? pageable.getLast() : true
        );

        return new ExternalProductSyncResult(items, pageInfo);
    }

    private ExternalProductSyncResult.ExternalProductItem convertToProductItem(ExternalProductData data) {
        return new ExternalProductSyncResult.ExternalProductItem(
            vendor,
            data.getId(),
            data.getName(),
            data.getDescription(),
            data.getPrice(),
            data.getStock()
        );
    }
}
