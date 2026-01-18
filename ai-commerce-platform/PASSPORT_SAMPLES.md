# Passport 샘플 (Swagger 테스트용)

이 문서는 API 테스트를 위한 등급별 Passport 직렬화 문자열을 제공합니다.

## 사용 방법

1. Swagger UI에서 API를 테스트할 때 `X-Passport` 헤더에 아래 직렬화된 문자열을 붙여넣습니다.
2. 각 등급별로 다른 사용자 정보와 권한을 가집니다.
3. Passport는 1시간 유효기간을 가지며, 생성 시점부터 계산됩니다.

## BRONZE 등급

**사용자 정보:**
- userId: 1
- username: bronze@example.com
- email: bronze@example.com
- grade: BRONZE
- roles: [USER]
- metadata: {"phone": "010-1111-1111"}

**직렬화된 문자열:**
```
H4sIAAAAAAAA/3WPPQvCMBCG/8vNabla+pVJhQ4uChUXxSHaQwtNE5IUisX/blpBcfCG4z7el3tuhN6S2dTAIzaXnZAEHC5GdQ9a0iCkbim8KgkMSIqm/be8GVFPznW12x5LPzCqJQv8BId9WcGZgSQnauEE8BH0XXWTGiMMIh9zgieDxtqe6pXzQFmaZ3EaLzDEIknyFBE9w6AbQ/YrKIpfgRbWamXc9NOnCd7IgSPrAkR/6QU4jYP8+QAAAA==
```

---

## SILVER 등급

**사용자 정보:**
- userId: 2
- username: silver@example.com
- email: silver@example.com
- grade: SILVER
- roles: [USER]
- metadata: {"phone": "010-2222-2222"}

**직렬화된 문자열:**
```
H4sIAAAAAAAA/3WPwQrCMAyG3yXnbcQOOteTHjwMPDn0Ih6KCzpot9J0Mhi+u91EPZlDyJ98IX8mGJh81YASyVJ22hIo4NY8yG9o1NYZyq69hQTI6tb8G968bubNutqfdofY8L0hBnWGYx31JQFLQTc6aFATuHvfzTSuMBUxlgTPBFrmgZptALUq5LrIZS4wE0JKlIgYPYyu9cQ/oCw/QLEATjO73of5p69I35bTQBxSxHjpBX/oa2v5AAAA
```

---

## GOLD 등급

**사용자 정보:**
- userId: 3
- username: gold@example.com
- email: gold@example.com
- grade: GOLD
- roles: [USER, PREMIUM]
- metadata: {"phone": "010-3333-3333", "vipLevel": 1}

**직렬화된 문자열:**
```
H4sIAAAAAAAA/22PwQrCMAyG3yXnbnQrzm0nBYcMNpTJTuKh2DAH61raTgTx3e0G6sUcQn7y5U/yhMmiKQXkjCzlyCVCDp0axAYfXOoBw6uSQAAl74f/rc5wMU/tD9XOS6MGtJCfoT0VjdfHpqjLtoYLAYmOC+445E+497rCO3rPiIC+qXG2oBENmI8lwYtAb+2EYus8tU7SNUtYTMM4TtJsRSn1Zz10b9D+gCz7AMkCaG6tVsbNT35FMH8ROLQuoNTveQP1+ExVCAEAAA==
```

---

## VIP 등급

**사용자 정보:**
- userId: 4
- username: vip@example.com
- email: vip@example.com
- grade: VIP
- roles: [USER, PREMIUM, VIP]
- metadata: {"phone": "010-4444-4444", "vipLevel": 2, "specialBenefits": true}

**직렬화된 문자열:**
```
H4sIAAAAAAAA/21PwYrCQAz9l5ynMq3dVufkCh4KCuKiF/EQbHQHOu0wmYog/vumhdWLOYQ83nt5yQN6plDVYHI1ji06AgM36xd0R+cbmpw7BwrIoW0+MteA9eA5VFtBoWuIwRxh/7PaCd7uVptqv5Fp4E8KHEWsMSKYB/jfrh2sOtVJLjU2kUrImm4keZkC9nS22CyppYuNsjuGnp4KLHNP9XcEk5bFrJwW00xPsqxMZ19aa7n47m0gfgvm839BMQo8MvsuxOH9F0gkO4nEMdE6h+cfDN6xvyEBAAA=
```

---

## ADMIN 권한

**사용자 정보:**
- userId: 99
- username: admin@example.com
- email: admin@example.com
- grade: VIP
- roles: [USER, ADMIN]
- metadata: {"phone": "010-9999-9999", "department": "IT"}

**직렬화된 문자열:**
```
H4sIAAAAAAAA/3WPywrCMBBF/2XWbUkftqYrC7roQhFfG3ExmEELTRuSFArFfzcpqCtnMcxlzp3HBIMhXQsoOQ/mukNJUAIK2XQrGlGqlqJ7LyEAkti0f3oPjcL7LvXeKd23ZKC8wvm4OThdrbf1Dm4BSLIo0CKUEwhSqK2kzjpffXKYevadH8JiFnIXc4JXAI0xA4nKgXGRL4s0TxMWJUmR5RljzF02qkaT+QGcf4DFDCg0RvXa+k+/IpwfCS0ZGzK/6A2aKL5WDgEAAA==
```

---

## Swagger UI에서 사용하기

### 방법 1: 직접 헤더 추가

1. Swagger UI에서 테스트하려는 API 엔드포인트를 선택합니다.
2. "Try it out" 버튼을 클릭합니다.
3. 헤더 섹션에서 `X-Passport` 헤더를 추가합니다.
4. 위의 등급별 직렬화된 문자열 중 하나를 값으로 붙여넣습니다.
5. "Execute" 버튼을 클릭하여 API를 테스트합니다.

### 방법 2: Swagger 설정에 예제 추가

SwaggerConfig에 아래와 같이 Parameter 예제를 추가할 수 있습니다:

```java
@Bean
public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("public")
        .pathsToMatch("/**")
        .addOperationCustomizer((operation, handlerMethod) -> {
            // X-Passport 헤더에 예제 추가
            Parameter passportHeader = new Parameter()
                .in("header")
                .name("X-Passport")
                .description("사용자 인증 정보 (Passport)")
                .required(false)
                .schema(new StringSchema()
                    .example("H4sIAAAAAAAA/3WPTQvCMAyG/0vOnbTqnO6kwg5eFBQviodoow7WtTQVRPG/282vkzmEvMmT8OYOFyY/05Ar0ZY1GoIc9t7WNxrTFY2rqHOwBgSQwbL6Nzx51M3mdLmYb4rY8LYihnwL61WxhJ0AQwE1BoT8Du5s64aWSiYqRpvgIaBkvpCehGgoGwyzXppmqtOXo2FfSSmjh6srPfEPGKkP0G0Bh8zO+tD89BXJy3ISiEMipYLPoQgdsWISgNqU9Vs9noEh2foXAQAA"));

            operation.addParametersItem(passportHeader);
            return operation;
        })
        .build();
}
```

## 주의사항

1. **유효기간**: 생성된 Passport는 생성 시점부터 1시간 유효합니다. 만료된 경우 새로 생성해야 합니다.
2. **환경**: 이 샘플들은 테스트 환경용입니다. 프로덕션 환경에서는 실제 인증 시스템을 통해 Passport를 발급받아야 합니다.
3. **권한**: 각 등급별로 접근 가능한 API가 다를 수 있습니다.

## Passport 재생성하기

새로운 Passport가 필요한 경우 `PassportGenerator.java`를 실행하여 생성할 수 있습니다:

```bash
javac -cp "common/build/classes/java/main:$(find ~/.gradle/caches/modules-2/files-2.1 -name 'jackson-*.jar' | tr '\n' ':' | sed 's/:$//')" PassportGenerator.java

java -cp ".:common/build/classes/java/main:$(find ~/.gradle/caches/modules-2/files-2.1 -name 'jackson-*.jar' | tr '\n' ':' | sed 's/:$//')" PassportGenerator
```
