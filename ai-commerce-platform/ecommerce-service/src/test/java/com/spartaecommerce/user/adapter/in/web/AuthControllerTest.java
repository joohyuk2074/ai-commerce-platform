package com.spartaecommerce.user.adapter.in.web;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartaecommerce.user.adapter.in.web.dto.RegisterRequest;
import com.spartaecommerce.user.adapter.in.web.dto.response.UserResponse;
import com.spartaecommerce.user.adapter.out.security.config.UsernamePasswordSecurityConfig;
import com.spartaecommerce.user.application.dto.result.UserResult;
import com.spartaecommerce.user.domain.port.in.GetUserUseCase;
import com.spartaecommerce.user.domain.port.in.RegisterUserUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import org.springframework.security.test.context.support.WithMockUser;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(UsernamePasswordSecurityConfig.class)
@DisplayName("AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    @MockitoBean
    private com.spartaecommerce.user.adapter.out.security.handler.LoginSuccessHandler loginSuccessHandler;

    @MockitoBean
    private com.spartaecommerce.user.adapter.out.security.handler.LoginFailureHandler loginFailureHandler;

    @MockitoBean
    private com.spartaecommerce.user.adapter.out.security.handler.LogoutSuccessHandler logoutSuccessHandler;

    @Test
    @DisplayName("회원가입 성공 - 유효한 요청으로 회원가입에 성공한다")
    void register_ValidRequest_ReturnsCreated() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest(
            "testuser",
            "password123!",
            "test@example.com",
            "테스트 사용자",
            "010-1234-5678"
        );

        Long expectedUserId = 1L;
        given(registerUserUseCase.register(any())).willReturn(expectedUserId);

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Created successfully"))
            .andExpect(jsonPath("$.data.id").value(expectedUserId))
            .andDo(MockMvcRestDocumentationWrapper.document("auth/signup-success",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(ResourceSnippetParameters.builder()
                    .tag("Auth")
                    .summary("회원가입")
                    .description("새로운 사용자를 등록합니다.")
                    .requestFields(
                        fieldWithPath("username").description("사용자 아이디 (필수)"),
                        fieldWithPath("password").description("비밀번호 (필수)"),
                        fieldWithPath("email").description("이메일 (필수, 이메일 형식)"),
                        fieldWithPath("name").description("이름 (필수)"),
                        fieldWithPath("phoneNumber").description("전화번호 (선택)").optional()
                    )
                    .responseFields(
                        fieldWithPath("code").description("응답 코드 (성공 시 null)").optional(),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("응답 데이터"),
                        fieldWithPath("data.id").description("생성된 사용자 ID")
                    )
                    .build()
                )
            ));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("현재 로그인한 사용자 정보 조회 - 인증된 사용자의 정보를 성공적으로 조회한다")
    void getCurrentUser_AuthenticatedUser_ReturnsUserInfo() throws Exception {
        // given
        String username = "testuser";
        LocalDateTime now = LocalDateTime.now();

        UserResult userResult = new UserResult(
            1L,
            "test@example.com",
            "테스트 사용자",
            "010-1234-5678",
            now,
            now
        );

        given(getUserUseCase.getByUsername(eq(username))).willReturn(userResult);

        // when & then
        mockMvc.perform(get("/api/v1/auth/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Success"))
            .andExpect(jsonPath("$.data.userId").value(1L))
            .andExpect(jsonPath("$.data.email").value("test@example.com"))
            .andExpect(jsonPath("$.data.name").value("테스트 사용자"))
            .andExpect(jsonPath("$.data.phoneNumber").value("010-1234-5678"))
            .andDo(MockMvcRestDocumentationWrapper.document("auth/me-success",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(ResourceSnippetParameters.builder()
                    .tag("Auth")
                    .summary("현재 사용자 정보 조회")
                    .description("로그인한 사용자의 정보를 조회합니다.")
                    .responseFields(
                        fieldWithPath("code").description("응답 코드 (성공 시 null)").optional(),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("사용자 정보"),
                        fieldWithPath("data.userId").description("사용자 ID"),
                        fieldWithPath("data.email").description("이메일"),
                        fieldWithPath("data.name").description("이름"),
                        fieldWithPath("data.phoneNumber").description("전화번호"),
                        fieldWithPath("data.createdAt").description("생성일시"),
                        fieldWithPath("data.updatedAt").description("수정일시")
                    )
                    .build()
                )
            ));
    }
}
