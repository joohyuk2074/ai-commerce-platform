package com.spartaecommerce.user.adapter.out.event;

import com.spartaecommerce.user.domain.event.UserCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * KafkaUserEventPublisher 통합 테스트
 * <p>
 * Big Tech Testing Practices for Integration Tests:
 * - Uses embedded Kafka (faster than Testcontainers for simple scenarios)
 * - Tests actual message serialization/deserialization
 * - Verifies end-to-end event flow
 * - Uses Awaitility for async verification
 * - Cleans up resources properly
 * <p>
 * Classical Testing Style:
 * - Tests observable behavior (messages in Kafka)
 * - No mocking of Kafka infrastructure
 * - State-based verification
 */
@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
    topics = {"ecommerce.event.user.v1"},
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
    }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("KafkaUserEventPublisher 통합 테스트")
class KafkaUserEventPublisherIntegrationTest {

    @Autowired
    private KafkaUserEventPublisher publisher;

    private KafkaConsumer<String, UserCreatedEvent> consumer;
    private List<ConsumerRecord<String, UserCreatedEvent>> consumedRecords;

    @BeforeEach
    void setUp() {
        consumedRecords = new ArrayList<>();

        // Kafka Consumer 설정
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, UserCreatedEvent.class.getName());

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("ecommerce.event.user.v1"));
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Nested
    @DisplayName("이벤트 발행 성공 시나리오")
    class SuccessScenarios {

        @Test
        @DisplayName("UserCreatedEvent를 발행하면, Kafka 토픽에서 해당 이벤트를 수신할 수 있다")
        void publishUserCreated_MessageIsSentToKafka() {
            // Given - UserCreatedEvent 준비
            UserCreatedEvent event = UserCreatedEvent.create(
                100L,
                "testuser",
                "test@example.com",
                "테스트유저",
                "01012345678",
                "NORMAL",
                LocalDateTime.now()
            );

            // When - 이벤트 발행
            publisher.publishUserCreated(event);

            // Then - Kafka에서 메시지 수신 (비동기이므로 대기)
            await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    ConsumerRecords<String, UserCreatedEvent> records =
                        consumer.poll(Duration.ofMillis(100));

                    records.forEach(consumedRecords::add);

                    assertThat(consumedRecords).isNotEmpty();
                });

            // Then - 수신한 메시지 검증
            ConsumerRecord<String, UserCreatedEvent> record = consumedRecords.get(0);
            assertThat(record.key()).isEqualTo("100");
            assertThat(record.topic()).isEqualTo("ecommerce.event.user.v1");

            UserCreatedEvent receivedEvent = record.value();
            assertThat(receivedEvent.getUserId()).isEqualTo(100L);
            assertThat(receivedEvent.getUsername()).isEqualTo("testuser");
            assertThat(receivedEvent.getEmail()).isEqualTo("test@example.com");
            assertThat(receivedEvent.getName()).isEqualTo("테스트유저");
            assertThat(receivedEvent.getPhoneNumber()).isEqualTo("01012345678");
            assertThat(receivedEvent.getGrade()).isEqualTo("NORMAL");
            assertThat(receivedEvent.getEventType()).isEqualTo("UserCreatedEvent");
            assertThat(receivedEvent.getEventId()).isNotNull();
            assertThat(receivedEvent.getOccurredAt()).isNotNull();
        }

        @Test
        @DisplayName("여러 개의 UserCreatedEvent를 발행하면, 모든 이벤트가 순서대로 Kafka에 기록된다")
        void publishMultipleUserCreatedEvents_AllMessagesAreReceived() {
            // Given - 3개의 이벤트 준비
            UserCreatedEvent event1 = UserCreatedEvent.create(
                1L, "user1", "user1@example.com",
                "사용자1", "01011111111", "NORMAL",
                LocalDateTime.now()
            );
            UserCreatedEvent event2 = UserCreatedEvent.create(
                2L, "user2", "user2@example.com",
                "사용자2", "01022222222", "VIP",
                LocalDateTime.now()
            );
            UserCreatedEvent event3 = UserCreatedEvent.create(
                3L, "user3", "user3@example.com",
                "사용자3", "01033333333", "ADMIN",
                LocalDateTime.now()
            );

            // When - 3개의 이벤트 발행
            publisher.publishUserCreated(event1);
            publisher.publishUserCreated(event2);
            publisher.publishUserCreated(event3);

            // Then - 모든 메시지 수신 대기
            await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    ConsumerRecords<String, UserCreatedEvent> records =
                        consumer.poll(Duration.ofMillis(100));

                    records.forEach(consumedRecords::add);

                    assertThat(consumedRecords).hasSizeGreaterThanOrEqualTo(3);
                });

            // Then - 각 메시지 검증
            assertThat(consumedRecords).hasSize(3);

            List<Long> userIds = consumedRecords.stream()
                .map(record -> record.value().getUserId())
                .toList();
            assertThat(userIds).containsExactly(1L, 2L, 3L);

            List<String> usernames = consumedRecords.stream()
                .map(record -> record.value().getUsername())
                .toList();
            assertThat(usernames).containsExactly("user1", "user2", "user3");
        }

        @Test
        @DisplayName("특수문자가 포함된 데이터도 정상적으로 직렬화/역직렬화된다")
        void publishUserCreated_WithSpecialCharacters_SerializesCorrectly() {
            // Given - 특수문자가 포함된 이벤트
            UserCreatedEvent event = UserCreatedEvent.create(
                999L,
                "user+special@chars",
                "test+tag@sub.example.co.kr",
                "테스트 & 특수문자 <> \"quotes\"",
                "010-1234-5678",
                "NORMAL",
                LocalDateTime.now()
            );

            // When
            publisher.publishUserCreated(event);

            // Then - 특수문자가 그대로 유지되는지 확인
            await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    ConsumerRecords<String, UserCreatedEvent> records =
                        consumer.poll(Duration.ofMillis(100));

                    records.forEach(consumedRecords::add);

                    assertThat(consumedRecords).isNotEmpty();
                });

            UserCreatedEvent receivedEvent = consumedRecords.get(0).value();
            assertThat(receivedEvent.getUsername()).isEqualTo("user+special@chars");
            assertThat(receivedEvent.getEmail()).isEqualTo("test+tag@sub.example.co.kr");
            assertThat(receivedEvent.getName()).isEqualTo("테스트 & 특수문자 <> \"quotes\"");
            assertThat(receivedEvent.getPhoneNumber()).isEqualTo("010-1234-5678");
        }
    }

    @Nested
    @DisplayName("메시지 키 및 파티셔닝 검증")
    class PartitioningScenarios {

        @Test
        @DisplayName("userId가 메시지 키로 사용되어, 같은 userId의 메시지는 같은 파티션에 전송된다")
        void publishUserCreated_UsesUserIdAsKey() {
            // Given
            Long userId = 777L;
            UserCreatedEvent event = UserCreatedEvent.create(
                userId,
                "keyuser",
                "key@example.com",
                "키테스트",
                "01099999999",
                "NORMAL",
                LocalDateTime.now()
            );

            // When
            publisher.publishUserCreated(event);

            // Then - 메시지 키가 userId인지 확인
            await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    ConsumerRecords<String, UserCreatedEvent> records =
                        consumer.poll(Duration.ofMillis(100));

                    records.forEach(consumedRecords::add);

                    assertThat(consumedRecords).isNotEmpty();
                });

            ConsumerRecord<String, UserCreatedEvent> record = consumedRecords.get(0);
            assertThat(record.key()).isEqualTo("777");
            assertThat(record.value().getUserId()).isEqualTo(777L);
        }
    }

    @Nested
    @DisplayName("이벤트 메타데이터 검증")
    class MetadataScenarios {

        @Test
        @DisplayName("발행된 이벤트는 eventId, eventType, occurredAt, schemaVersion을 포함한다")
        void publishUserCreated_ContainsAllMetadata() {
            // Given
            LocalDateTime beforePublish = LocalDateTime.now();

            UserCreatedEvent event = UserCreatedEvent.create(
                500L,
                "metauser",
                "meta@example.com",
                "메타테스트",
                "01055555555",
                "NORMAL",
                LocalDateTime.now()
            );

            // When
            publisher.publishUserCreated(event);

            // Then
            await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    ConsumerRecords<String, UserCreatedEvent> records =
                        consumer.poll(Duration.ofMillis(100));

                    records.forEach(consumedRecords::add);

                    assertThat(consumedRecords).isNotEmpty();
                });

            UserCreatedEvent receivedEvent = consumedRecords.get(0).value();

            // 메타데이터 검증
            assertThat(receivedEvent.getEventId()).isNotNull();
            assertThat(receivedEvent.getEventType()).isEqualTo("UserCreatedEvent");
            assertThat(receivedEvent.getOccurredAt()).isNotNull();
            assertThat(receivedEvent.getOccurredAt())
                .isAfterOrEqualTo(beforePublish)
                .isBeforeOrEqualTo(LocalDateTime.now().plusSeconds(1));
            assertThat(receivedEvent.getSchemaVersion()).isEqualTo(1);
        }

        @Test
        @DisplayName("각 이벤트는 고유한 eventId를 가진다")
        void publishUserCreated_GeneratesUniqueEventIds() {
            // Given - 2개의 이벤트
            UserCreatedEvent event1 = UserCreatedEvent.create(
                10L, "user10", "user10@example.com",
                "사용자10", "01010101010", "NORMAL",
                LocalDateTime.now()
            );
            UserCreatedEvent event2 = UserCreatedEvent.create(
                20L, "user20", "user20@example.com",
                "사용자20", "01020202020", "NORMAL",
                LocalDateTime.now()
            );

            // When
            publisher.publishUserCreated(event1);
            publisher.publishUserCreated(event2);

            // Then
            await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    ConsumerRecords<String, UserCreatedEvent> records =
                        consumer.poll(Duration.ofMillis(100));

                    records.forEach(consumedRecords::add);

                    assertThat(consumedRecords).hasSizeGreaterThanOrEqualTo(2);
                });

            String eventId1 = consumedRecords.get(0).value().getEventId();
            String eventId2 = consumedRecords.get(1).value().getEventId();

            assertThat(eventId1).isNotEqualTo(eventId2);
            assertThat(eventId1).isNotBlank();
            assertThat(eventId2).isNotBlank();
        }
    }
}
