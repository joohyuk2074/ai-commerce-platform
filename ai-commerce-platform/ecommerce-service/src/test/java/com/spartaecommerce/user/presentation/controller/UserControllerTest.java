package com.spartaecommerce.user.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartaecommerce.user.presentation.controller.dto.request.UserCreateRequest;
import com.spartaecommerce.pointwallet.jpa.entity.PointWalletJpaEntity;
import com.spartaecommerce.pointwallet.jpa.repository.PointWalletJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("UserController")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PointWalletJpaRepository walletJpaRepository;

    @Test
    @DisplayName("사용자 삭제 시 지갑의 active 플래그가 false로 변경된다")
    void delete_User_DeactivatesWallet() throws Exception {
        // given: 사용자 생성
        UserCreateRequest createRequest = new UserCreateRequest(
            "test@example.com",
            "John Doe",
            "010-1234-5678"
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.id").exists())
            .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long userId = objectMapper.readTree(responseBody).get("data").get("id").asLong();

        // when: 사용자 삭제
        mockMvc.perform(delete("/api/v1/users/" + userId))
            .andExpect(status().isOk());

        // then: 지갑의 active 플래그가 false로 변경되었는지 확인
        PointWalletJpaEntity wallet = walletJpaRepository.findByUserId(userId)
            .orElseThrow(() -> new AssertionError("Wallet should exist"));

        assertThat(wallet.isActive()).isFalse();
    }

    @Test
    @DisplayName("사용자 생성 시 지갑도 함께 생성된다")
    void create_User_CreatesWallet() throws Exception {
        // given
        UserCreateRequest createRequest = new UserCreateRequest(
            "newuser@example.com",
            "Jane Doe",
            "010-9876-5432"
        );

        // when
        MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.id").exists())
            .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long userId = objectMapper.readTree(responseBody).get("data").get("id").asLong();

        // then
        PointWalletJpaEntity wallet = walletJpaRepository.findByUserId(userId)
            .orElseThrow(() -> new AssertionError("Wallet should be created"));

        assertThat(wallet.getUserId()).isEqualTo(userId);
        assertThat(wallet.isActive()).isTrue();
    }
}
