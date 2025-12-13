package com.spartaecommerce.coupon.adapter.in.web;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartaecommerce.coupon.adapter.in.web.dto.request.IssueCouponsRequest;
import com.spartaecommerce.coupon.adapter.in.web.dto.request.RegisterCouponRequest;
import com.spartaecommerce.coupon.application.dto.result.CouponUserResult;
import com.spartaecommerce.coupon.domain.entity.CouponStatus;
import com.spartaecommerce.coupon.domain.port.in.GetCouponUserUseCase;
import com.spartaecommerce.coupon.domain.port.in.IssueCouponsUseCase;
import com.spartaecommerce.coupon.domain.port.in.RegisterCouponUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CouponUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
@DisplayName("CouponUserController")
class CouponUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IssueCouponsUseCase issueCouponsUseCase;

    @MockitoBean
    private RegisterCouponUseCase registerCouponUseCase;

    @MockitoBean
    private GetCouponUserUseCase getCouponUserUseCase;

    @Test
    @DisplayName("쿠폰 대량 발급 - 성공")
    void issueCoupons_ValidRequest_ReturnsSuccess() throws Exception {
        // given
        IssueCouponsRequest request = new IssueCouponsRequest(1L, 100);
        List<Long> couponUserIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);

        given(issueCouponsUseCase.issue(any())).willReturn(couponUserIds);

        // when & then
        mockMvc.perform(post("/api/v1/coupon-users/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.issuedCount").value(5))
                .andExpect(jsonPath("$.data.couponUserIds").isArray())
                .andDo(MockMvcRestDocumentationWrapper.document("coupon-users/issue-coupons-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Coupon Users")
                                .summary("쿠폰 대량 발급")
                                .description("요청된 수량만큼 중복되지 않는 오프라인 쿠폰 코드를 생성하고 PENDING 상태로 저장합니다.")
                                .requestFields(
                                        fieldWithPath("couponId").description("쿠폰 ID (필수)"),
                                        fieldWithPath("quantity").description("발급 수량 (1-10,000, 필수)")
                                )
                                .responseFields(
                                        fieldWithPath("code").description("응답 코드 (성공 시 null)").optional(),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.issuedCount").description("발급된 쿠폰 개수"),
                                        fieldWithPath("data.couponUserIds").description("발급된 쿠폰 사용자 ID 목록")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("쿠폰 대량 발급 - 유효하지 않은 수량")
    void issueCoupons_InvalidQuantity_ReturnsBadRequest() throws Exception {
        // given
        IssueCouponsRequest request = new IssueCouponsRequest(1L, 0);

        // when & then
        mockMvc.perform(post("/api/v1/coupon-users/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("쿠폰 대량 발급 - 수량 초과")
    void issueCoupons_ExceedMaxQuantity_ReturnsBadRequest() throws Exception {
        // given
        IssueCouponsRequest request = new IssueCouponsRequest(1L, 10001);

        // when & then
        mockMvc.perform(post("/api/v1/coupon-users/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("오프라인 쿠폰 등록 - 성공")
    void registerCoupon_ValidRequest_ReturnsSuccess() throws Exception {
        // given
        RegisterCouponRequest request = new RegisterCouponRequest("ABCD-1234-EFGH", 100L);
        LocalDateTime now = LocalDateTime.now();

        CouponUserResult result = new CouponUserResult(
                1L,
                100L,
                1L,
                "ABCD-1234-EFGH",
                CouponStatus.ISSUED,
                now,
                now
        );

        given(registerCouponUseCase.register(any())).willReturn(result);

        // when & then
        mockMvc.perform(post("/api/v1/coupon-users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.couponUserId").value(1L))
                .andExpect(jsonPath("$.data.userId").value(100L))
                .andExpect(jsonPath("$.data.code").value("ABCD-1234-EFGH"))
                .andExpect(jsonPath("$.data.status").value("ISSUED"))
                .andDo(MockMvcRestDocumentationWrapper.document("coupon-users/register-coupon-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Coupon Users")
                                .summary("오프라인 쿠폰 등록")
                                .description("오프라인 쿠폰 코드를 검증하고 사용자에게 발급합니다. 쿠폰 상태가 PENDING에서 ISSUED로 변경됩니다.")
                                .requestFields(
                                        fieldWithPath("couponCode").description("쿠폰 코드 (필수)"),
                                        fieldWithPath("userId").description("사용자 ID (필수)")
                                )
                                .responseFields(
                                        fieldWithPath("code").description("응답 코드 (성공 시 null)").optional(),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.couponUserId").description("쿠폰 사용자 ID"),
                                        fieldWithPath("data.userId").description("사용자 ID"),
                                        fieldWithPath("data.couponId").description("쿠폰 ID"),
                                        fieldWithPath("data.code").description("쿠폰 코드"),
                                        fieldWithPath("data.status").description("쿠폰 상태 (PENDING, ISSUED, USED, EXPIRED)"),
                                        fieldWithPath("data.createdAt").description("생성 일시"),
                                        fieldWithPath("data.updatedAt").description("수정 일시")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("오프라인 쿠폰 등록 - 쿠폰 코드가 없는 경우")
    void registerCoupon_MissingCouponCode_ReturnsBadRequest() throws Exception {
        // given
        RegisterCouponRequest request = new RegisterCouponRequest("", 100L);

        // when & then
        mockMvc.perform(post("/api/v1/coupon-users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("쿠폰 코드로 조회 - 성공")
    void getCouponByCode_ValidCode_ReturnsSuccess() throws Exception {
        // given
        String code = "ABCD-1234-EFGH";
        LocalDateTime now = LocalDateTime.now();

        CouponUserResult result = new CouponUserResult(
                1L,
                100L,
                1L,
                code,
                CouponStatus.ISSUED,
                now,
                now
        );

        given(getCouponUserUseCase.getByCode(code)).willReturn(result);

        // when & then
        mockMvc.perform(get("/api/v1/coupon-users/code/{code}", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.code").value(code))
                .andExpect(jsonPath("$.data.status").value("ISSUED"))
                .andDo(MockMvcRestDocumentationWrapper.document("coupon-users/get-by-code-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Coupon Users")
                                .summary("쿠폰 코드로 조회")
                                .description("쿠폰 코드로 쿠폰 정보를 조회합니다.")
                                .responseFields(
                                        fieldWithPath("code").description("응답 코드 (성공 시 null)").optional(),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("data").description("응답 데이터"),
                                        fieldWithPath("data.couponUserId").description("쿠폰 사용자 ID"),
                                        fieldWithPath("data.userId").description("사용자 ID"),
                                        fieldWithPath("data.couponId").description("쿠폰 ID"),
                                        fieldWithPath("data.code").description("쿠폰 코드"),
                                        fieldWithPath("data.status").description("쿠폰 상태"),
                                        fieldWithPath("data.createdAt").description("생성 일시"),
                                        fieldWithPath("data.updatedAt").description("수정 일시")
                                )
                                .build()
                        )
                ));
    }
}
