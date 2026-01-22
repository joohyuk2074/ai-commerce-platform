package com.example.spartallm.chat.application.dto.query;

import com.example.spartallm.chat.domain.model.LlmRequestMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ChatQuery 단위 테스트")
class ChatQueryTest {

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorValidation {

        @Test
        @DisplayName("모든 필수값이 있으면 성공")
        void createWithAllRequiredFields_Success() {
            // given
            String model = "gpt-4";
            List<ChatQuery.ChatMessageQuery> messages = List.of(
                new ChatQuery.ChatMessageQuery("user", "Hello")
            );

            // when
            ChatQuery chatQuery = new ChatQuery(model, messages, null, null, null);

            // then
            assertThat(chatQuery.model()).isEqualTo(model);
            assertThat(chatQuery.messages()).hasSize(1);
            assertThat(chatQuery.temperature()).isEqualTo(0.7);  // 기본값
            assertThat(chatQuery.maxTokens()).isEqualTo(500);    // 기본값
            assertThat(chatQuery.stream()).isFalse();            // 기본값
        }

        @Test
        @DisplayName("model이 null이면 예외 발생")
        void createWithNullModel_ThrowsException() {
            // given
            List<ChatQuery.ChatMessageQuery> messages = List.of(
                new ChatQuery.ChatMessageQuery("user", "Hello")
            );

            // when & then
            assertThatThrownBy(() -> new ChatQuery(null, messages, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("model cannot be null");
        }

        @Test
        @DisplayName("messages가 null이면 예외 발생")
        void createWithNullMessages_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> new ChatQuery("gpt-4", null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("messages cannot be null or empty");
        }

        @Test
        @DisplayName("messages가 비어있으면 예외 발생")
        void createWithEmptyMessages_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> new ChatQuery("gpt-4", Collections.emptyList(), null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("messages cannot be null or empty");
        }

        @ParameterizedTest(name = "temperature={0}, maxTokens={1}, stream={2}")
        @MethodSource("provideDefaultValues")
        @DisplayName("기본값이 올바르게 설정됨 - ParameterizedTest")
        void createWithNullOptionalFields_SetsDefaults(
            Double inputTemp,
            Integer inputMaxTokens,
            Boolean inputStream,
            Double expectedTemp,
            Integer expectedMaxTokens,
            Boolean expectedStream
        ) {
            // given
            List<ChatQuery.ChatMessageQuery> messages = List.of(
                new ChatQuery.ChatMessageQuery("user", "Hello")
            );

            // when
            ChatQuery chatQuery = new ChatQuery("gpt-4", messages, inputTemp, inputMaxTokens, inputStream);

            // then
            assertThat(chatQuery.temperature()).isEqualTo(expectedTemp);
            assertThat(chatQuery.maxTokens()).isEqualTo(expectedMaxTokens);
            assertThat(chatQuery.stream()).isEqualTo(expectedStream);
        }

        static Stream<Arguments> provideDefaultValues() {
            return Stream.of(
                // 모두 null → 기본값
                Arguments.of(null, null, null, 0.7, 500, false),
                // temperature만 지정
                Arguments.of(0.9, null, null, 0.9, 500, false),
                // maxTokens만 지정
                Arguments.of(null, 1000, null, 0.7, 1000, false),
                // stream만 지정
                Arguments.of(null, null, true, 0.7, 500, true),
                // 모두 지정
                Arguments.of(0.5, 200, true, 0.5, 200, true)
            );
        }
    }

    @Nested
    @DisplayName("ChatMessageQuery 생성자 검증")
    class ChatMessageQueryValidation {

        @Test
        @DisplayName("role이 null이면 예외 발생")
        void createWithNullRole_ThrowsException() {
            assertThatThrownBy(() -> new ChatQuery.ChatMessageQuery(null, "content"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("role cannot be null");
        }

        @Test
        @DisplayName("content가 null이면 예외 발생")
        void createWithNullContent_ThrowsException() {
            assertThatThrownBy(() -> new ChatQuery.ChatMessageQuery("user", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("content cannot be null");
        }
    }

    @Nested
    @DisplayName("toChatMessage() 변환 메서드")
    class ToChatMessageMethod {

        @Test
        @DisplayName("user 메시지만 있으면 정상 변환")
        void toChatMessage_WithUserMessageOnly_Success() {
            // given
            ChatQuery chatQuery = new ChatQuery(
                "gpt-4",
                List.of(new ChatQuery.ChatMessageQuery("user", "Hello")),
                0.8,
                300,
                false
            );

            // when
            LlmRequestMessage result = chatQuery.toChatMessage();

            // then
            assertThat(result.requestId()).isNotBlank();
            assertThat(result.modelName()).isEqualTo("gpt-4");
            assertThat(result.systemPrompt()).isEmpty();
            assertThat(result.userMessage()).isEqualTo("Hello");
            assertThat(result.chatOptions().temperature()).isEqualTo(0.8);
            assertThat(result.chatOptions().maxTokens()).isEqualTo(300);
        }

        @Test
        @DisplayName("system + user 메시지가 있으면 정상 변환")
        void toChatMessage_WithSystemAndUser_Success() {
            // given
            ChatQuery chatQuery = new ChatQuery(
                "gpt-4",
                List.of(
                    new ChatQuery.ChatMessageQuery("system", "You are a helpful assistant"),
                    new ChatQuery.ChatMessageQuery("user", "안녕하세요")
                ),
                0.7,
                500,
                false
            );

            // when
            LlmRequestMessage result = chatQuery.toChatMessage();

            // then
            assertThat(result.systemPrompt()).isEqualTo("You are a helpful assistant");
            assertThat(result.userMessage()).isEqualTo("안녕하세요");
        }

        @Test
        @DisplayName("여러 system 메시지는 줄바꿈으로 병합")
        void toChatMessage_WithMultipleSystemMessages_MergesWithNewline() {
            // given
            ChatQuery chatQuery = new ChatQuery(
                "gpt-4",
                List.of(
                    new ChatQuery.ChatMessageQuery("system", "First instruction"),
                    new ChatQuery.ChatMessageQuery("system", "Second instruction"),
                    new ChatQuery.ChatMessageQuery("user", "Hello")
                ),
                null,
                null,
                null
            );

            // when
            LlmRequestMessage result = chatQuery.toChatMessage();

            // then
            assertThat(result.systemPrompt()).isEqualTo("First instruction\nSecond instruction");
        }

        @Test
        @DisplayName("여러 user 메시지는 줄바꿈으로 병합")
        void toChatMessage_WithMultipleUserMessages_MergesWithNewline() {
            // given
            ChatQuery chatQuery = new ChatQuery(
                "gpt-4",
                List.of(
                    new ChatQuery.ChatMessageQuery("user", "First message"),
                    new ChatQuery.ChatMessageQuery("user", "Second message")
                ),
                null,
                null,
                null
            );

            // when
            LlmRequestMessage result = chatQuery.toChatMessage();

            // then
            assertThat(result.userMessage()).isEqualTo("First message\nSecond message");
        }

        @Test
        @DisplayName("user 메시지가 없으면 예외 발생")
        void toChatMessage_WithoutUserMessage_ThrowsException() {
            // given
            ChatQuery chatQuery = new ChatQuery(
                "gpt-4",
                List.of(
                    new ChatQuery.ChatMessageQuery("system", "Only system message")
                ),
                null,
                null,
                null
            );

            // when & then
            assertThatThrownBy(chatQuery::toChatMessage)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자 메시지가 없습니다");
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("provideMessageCombinations")
        @DisplayName("다양한 메시지 조합 테스트 - ParameterizedTest")
        void toChatMessage_WithVariousCombinations_Success(
            String testName,
            List<ChatQuery.ChatMessageQuery> messages,
            String expectedSystemPrompt,
            String expectedUserMessage
        ) {
            // given
            ChatQuery chatQuery = new ChatQuery("gpt-4", messages, null, null, null);

            // when
            LlmRequestMessage result = chatQuery.toChatMessage();

            // then
            assertThat(result.systemPrompt()).isEqualTo(expectedSystemPrompt);
            assertThat(result.userMessage()).isEqualTo(expectedUserMessage);
        }

        static Stream<Arguments> provideMessageCombinations() {
            return Stream.of(
                Arguments.of(
                    "user만",
                    List.of(new ChatQuery.ChatMessageQuery("user", "Hi")),
                    "",
                    "Hi"
                ),
                Arguments.of(
                    "system + user",
                    List.of(
                        new ChatQuery.ChatMessageQuery("system", "You are AI"),
                        new ChatQuery.ChatMessageQuery("user", "Hello")
                    ),
                    "You are AI",
                    "Hello"
                ),
                Arguments.of(
                    "system + user + user",
                    List.of(
                        new ChatQuery.ChatMessageQuery("system", "Be helpful"),
                        new ChatQuery.ChatMessageQuery("user", "Q1"),
                        new ChatQuery.ChatMessageQuery("user", "Q2")
                    ),
                    "Be helpful",
                    "Q1\nQ2"
                ),
                Arguments.of(
                    "복잡한 조합",
                    List.of(
                        new ChatQuery.ChatMessageQuery("system", "S1"),
                        new ChatQuery.ChatMessageQuery("user", "U1"),
                        new ChatQuery.ChatMessageQuery("system", "S2"),
                        new ChatQuery.ChatMessageQuery("user", "U2")
                    ),
                    "S1\nS2",
                    "U1\nU2"
                )
            );
        }

        @Test
        @DisplayName("requestId는 매번 다른 UUID 생성")
        void toChatMessage_GeneratesUniqueRequestId() {
            // given
            ChatQuery chatQuery = new ChatQuery(
                "gpt-4",
                List.of(new ChatQuery.ChatMessageQuery("user", "Hello")),
                null,
                null,
                null
            );

            // when
            LlmRequestMessage result1 = chatQuery.toChatMessage();
            LlmRequestMessage result2 = chatQuery.toChatMessage();

            // then
            assertThat(result1.requestId()).isNotEqualTo(result2.requestId());
            assertThat(result1.requestId()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTest {

        @Test
        @DisplayName("Record는 불변이므로 messages 수정 불가")
        void messagesAreImmutable() {
            // given
            List<ChatQuery.ChatMessageQuery> messages = List.of(
                new ChatQuery.ChatMessageQuery("user", "Hello")
            );
            ChatQuery chatQuery = new ChatQuery("gpt-4", messages, null, null, null);

            // when
            List<ChatQuery.ChatMessageQuery> retrievedMessages = chatQuery.messages();

            // then: List.of()는 불변이므로 수정 시도 시 예외 발생
            assertThatThrownBy(() -> retrievedMessages.add(
                new ChatQuery.ChatMessageQuery("user", "New message")
            ))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}