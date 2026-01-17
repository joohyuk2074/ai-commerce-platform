# Logstash & Elasticsearch 설정 가이드

이 문서는 Sparta E-commerce Platform의 로그 수집 및 분석을 위한 Logstash와 Elasticsearch 설정을 설명합니다.

## 디렉토리 구조

```
logstash/
├── config/
│   └── logstash.yml          # Logstash 설정 파일
├── pipeline/
│   └── logstash.conf         # Logstash 파이프라인 설정
├── templates/
│   ├── audit-user-activity-template.json   # 사용자 활동 로그 인덱스 템플릿
│   └── event-order-template.json           # 주문 이벤트 인덱스 템플릿
├── scripts/
│   ├── init-templates.sh     # 인덱스 템플릿 자동 등록 스크립트
│   └── verify-logstash.sh    # Logstash 동작 검증 스크립트 (종합)
├── queries/
│   ├── 01-popular-products-by-sales-count.json
│   ├── 02-popular-products-by-revenue.json
│   ├── 03-popular-products-by-category.json
│   ├── 04-popular-products-last-7days.json
│   ├── 05-popular-products-by-user-activity.json
│   ├── 06-real-time-popular-products.json
│   ├── check-data.sh         # 간단한 데이터 확인 스크립트
│   └── check-filters.sh      # 필터링 결과 확인 스크립트
└── README.md                 # 이 문서
```

## Elasticsearch Index 설계

### 1. audit-user-activity-* (사용자 활동 로그)

사용자의 API 호출, 페이지 접근 등의 활동을 추적하는 인덱스입니다.

**주요 필드:**
- `@timestamp`: 이벤트 발생 시간
- `userId`: 사용자 ID
- `action`: 액션 타입
- `service`: 서비스 이름 (products, orders 등)
- `resource`: 리소스 타입
- `method`: HTTP 메서드 (GET, POST, PUT, DELETE)
- `path`: 요청 경로
- `statusCode`: HTTP 상태 코드
- `status_category`: 상태 분류 (success, client_error, server_error 등)
- `responseTime`: 응답 시간 (ms)
- `performance`: 성능 등급 (excellent, good, acceptable, poor)
- `clientIp`: 클라이언트 IP
- `userAgent`: User-Agent 정보

**인덱스 패턴:** `audit-user-activity-YYYY.MM.dd` (일별 인덱스)

### 2. event-order-* (주문 이벤트 데이터)

주문 생성, 취소, 완료 등의 이벤트를 기록하는 인덱스입니다.

**주요 필드:**
- `@timestamp`: 이벤트 발생 시간
- `eventType`: 이벤트 타입 (ORDER_CREATED, ORDER_CANCELLED 등)
- `orderId`: 주문 ID
- `userId`: 사용자 ID
- `orderStatus`: 주문 상태
- `totalAmount`: 총 주문 금액
- `items`: 주문 상품 목록 (nested 타입)
  - `productId`: 상품 ID
  - `productName`: 상품명
  - `categoryId`: 카테고리 ID
  - `categoryName`: 카테고리명
  - `quantity`: 수량
  - `price`: 단가
  - `subtotal`: 소계
- `pointsUsed`: 사용 포인트
- `pointsEarned`: 적립 포인트

**인덱스 패턴:** `event-order-YYYY.MM.dd` (일별 인덱스)

## Index Template 정적 매핑

Index Template을 사용하여 인덱스가 생성될 때 자동으로 정적 매핑을 적용합니다.

### 자동화 방식

docker-compose로 ELK 스택을 시작하면 `es-init` 컨테이너가 자동으로 실행되어 Index Template을 등록합니다.

```yaml
es-init:
  image: alpine:3.19
  depends_on:
    elasticsearch:
      condition: service_healthy
  volumes:
    - ./logstash/templates:/templates:ro
    - ./logstash/scripts:/scripts:ro
  command: sh -c "apk add --no-cache curl jq bash && bash /scripts/init-templates.sh"
  restart: "no"
```

**작동 방식:**
1. Elasticsearch가 healthy 상태가 될 때까지 대기
2. `templates/` 디렉토리의 모든 `.json` 파일을 읽어서 Index Template 등록
3. 등록 완료 후 컨테이너 종료

### 수동 등록 방법

필요시 수동으로 템플릿을 등록할 수 있습니다:

```bash
# 사용자 활동 로그 템플릿 등록
curl -X PUT "http://localhost:9200/_index_template/audit-user-activity-template" \
  -H 'Content-Type: application/json' \
  -d @logstash/templates/audit-user-activity-template.json

# 주문 이벤트 템플릿 등록
curl -X PUT "http://localhost:9200/_index_template/event-order-template" \
  -H 'Content-Type: application/json' \
  -d @logstash/templates/event-order-template.json
```

### 템플릿 확인

```bash
# 등록된 템플릿 목록 조회
curl -X GET "http://localhost:9200/_index_template?pretty"

# 특정 템플릿 조회
curl -X GET "http://localhost:9200/_index_template/audit-user-activity-template?pretty"
curl -X GET "http://localhost:9200/_index_template/event-order-template?pretty"
```

## 인기 상품 검색 쿼리

`queries/` 디렉토리에는 다양한 관점에서 인기 상품을 분석하는 Elasticsearch 쿼리가 포함되어 있습니다.

### 1. 판매 횟수 기준 인기 상품 (01-popular-products-by-sales-count.json)

**설명:** 완료된 주문에서 가장 많이 판매된 상품 Top 10을 조회합니다.

**반환 정보:**
- 상품 ID, 상품명
- 판매 횟수 (주문 건수)
- 총 판매 수량
- 총 매출액

**실행 예제:**
```bash
cd logstash/queries

# 쿼리 파일에서 query 부분만 추출하여 실행
jq '.query' 01-popular-products-by-sales-count.json | \
  curl -X POST "http://localhost:9200/event-order-*/_search?pretty" \
  -H 'Content-Type: application/json' -d @-
```

### 2. 매출액 기준 인기 상품 (02-popular-products-by-revenue.json)

**설명:** 총 매출액이 가장 높은 상품 Top 10을 조회합니다.

**반환 정보:**
- 상품 ID, 상품명
- 총 매출액
- 총 판매 수량
- 주문 건수

### 3. 카테고리별 인기 상품 (03-popular-products-by-category.json)

**설명:** 각 카테고리별로 가장 인기 있는 상품 Top 5를 조회합니다.

**반환 정보:**
- 카테고리명
- 카테고리별 Top 5 상품
  - 상품 ID, 상품명
  - 판매 수량
  - 매출액

### 4. 최근 7일간 인기 상품 (04-popular-products-last-7days.json)

**설명:** 최근 7일간 가장 많이 판매된 상품 Top 10을 조회하고, 일별 판매 추이를 제공합니다.

**반환 정보:**
- 상품 ID, 상품명, 카테고리명
- 총 판매 수량
- 총 매출액
- 일별 판매 추이

### 5. 사용자 활동 기반 인기 상품 (05-popular-products-by-user-activity.json)

**설명:** 사용자 활동 로그에서 상품 상세 페이지 조회수가 높은 상품 Top 10을 조회합니다.

**반환 정보:**
- 상품 ID
- 조회 횟수
- 고유 사용자 수 (cardinality)
- 평균 응답 시간

### 6. 실시간 인기 상품 (06-real-time-popular-products.json)

**설명:** 최근 1시간 동안 가장 많이 주문된 상품 Top 10을 실시간으로 조회합니다.

**반환 정보:**
- 상품 ID, 상품명, 카테고리명, 가격
- 판매 수량
- 고유 구매자 수

## Kibana에서 쿼리 실행

1. Kibana 접속: http://localhost:5601
2. 좌측 메뉴에서 "Dev Tools" 선택
3. 쿼리 파일의 `query` 부분을 복사하여 실행

**예제:**
```json
POST /event-order-*/_search
{
  "size": 0,
  "query": {
    "bool": {
      "must": [
        { "term": { "eventType": "ORDER_CREATED" } },
        { "term": { "orderStatus": "COMPLETED" } }
      ]
    }
  },
  "aggs": {
    "popular_products": {
      "nested": { "path": "items" },
      "aggs": {
        "by_product": {
          "terms": {
            "field": "items.productId",
            "size": 10,
            "order": { "_count": "desc" }
          },
          "aggs": {
            "product_name": {
              "top_hits": {
                "size": 1,
                "_source": ["items.productName"]
              }
            }
          }
        }
      }
    }
  }
}
```

## 테스트 및 검증

### 1. ELK 스택 시작

```bash
docker-compose up -d elasticsearch kibana logstash
```

### 2. Logstash 동작 종합 검증 (자동화 스크립트)

Logstash의 수집, 필터링, Elasticsearch 저장이 제대로 동작하는지 한 번에 검증합니다:

```bash
# 컨테이너 내부에서 실행
docker exec sparta-ecommerce-logstash bash /scripts/verify-logstash.sh

# 또는 로컬에서 실행 (Elasticsearch가 localhost:9200으로 접근 가능한 경우)
./logstash/scripts/verify-logstash.sh
```

**검증 항목:**
- ✓ Logstash 서비스 상태
- ✓ 파이프라인 로드 상태
- ✓ Elasticsearch 연결 상태
- ✓ 인덱스 템플릿 등록 여부
- ✓ 인덱스 생성 여부
- ✓ 실제 데이터 저장 여부
- ✓ 필터링 필드 (performance, status_category) 적용 여부
- ✓ Logstash 처리 통계

### 3. Index Template 등록 확인

```bash
# es-init 컨테이너 로그 확인
docker logs sparta-ecommerce-es-init

# 기대 출력:
# ==========================================
# Elasticsearch Index Template Initializer
# ==========================================
# ...
# [1] Processing template: audit-user-activity-template
#     SUCCESS: Template registered (HTTP 200)
# [2] Processing template: event-order-template
#     SUCCESS: Template registered (HTTP 200)
# ==========================================
# Summary:
#   Total templates: 2
#   Success: 2
#   Failed: 0
# ==========================================
```

### 4. 인덱스 및 데이터 확인

Logstash가 데이터를 전송하면 자동으로 인덱스가 생성되며, Index Template의 매핑이 적용됩니다.

```bash
# 간단하게 데이터 확인
./logstash/queries/check-data.sh

# 생성된 인덱스 목록 확인
curl -X GET "http://localhost:9200/_cat/indices/audit-*?v"
curl -X GET "http://localhost:9200/_cat/indices/event-*?v"

# 특정 인덱스의 매핑 확인
curl -X GET "http://localhost:9200/audit-user-activity-2026.01.18/_mapping?pretty"
```

### 5. Logstash 필터링 결과 확인

Logstash 파이프라인에서 추가한 필터 필드들이 제대로 적용되었는지 확인:

```bash
# 필터링 결과 상세 확인
./logstash/queries/check-filters.sh
```

**확인 내용:**
- `performance` 필드: 응답시간 기반 성능 등급 (excellent/good/acceptable/poor)
- `status_category` 필드: HTTP 상태코드 분류 (success/redirect/client_error/server_error)
- `service` 필드: API 엔드포인트에서 추출한 서비스명
- `resource` 필드: API 엔드포인트에서 추출한 리소스명
- `ua` 필드: User-Agent 파싱 결과 (브라우저, OS 등)

### 6. Logstash 로그 확인

실시간으로 Logstash가 처리하는 로그 확인:

```bash
# Logstash 컨테이너 로그 확인 (stdout 출력 포함)
docker logs -f sparta-ecommerce-logstash

# 에러만 필터링
docker logs sparta-ecommerce-logstash 2>&1 | grep -i error
```

### 7. Kafka → Logstash 연결 확인

Kafka에서 메시지가 제대로 전송되고 있는지 확인:

```bash
# Kafka UI에서 확인
open http://localhost:8088

# 또는 Kafka 컨테이너에서 직접 확인
docker exec -it sparta-ecommerce-kafka \
  kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic ecommerce.audit.user-activity --from-beginning --max-messages 10

docker exec -it sparta-ecommerce-kafka \
  kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic ecommerce.event.order.v1 --from-beginning --max-messages 10
```

### 8. 샘플 쿼리 실행

```bash
cd logstash/queries

# 판매 횟수 기준 인기 상품 조회
jq '.query' 01-popular-products-by-sales-count.json | \
  curl -X POST "http://localhost:9200/event-order-*/_search?pretty" \
  -H 'Content-Type: application/json' -d @-
```

## 트러블슈팅

### 문제: Index Template이 등록되지 않음

**해결:**
1. es-init 컨테이너 로그 확인: `docker logs sparta-ecommerce-es-init`
2. Elasticsearch 상태 확인: `curl http://localhost:9200/_cluster/health?pretty`
3. 수동으로 템플릿 등록 시도

### 문제: 쿼리 실행 시 에러 발생

**해결:**
1. 인덱스 존재 여부 확인: `curl http://localhost:9200/_cat/indices?v`
2. 매핑 확인: `curl http://localhost:9200/event-order-*/_mapping?pretty`
3. Logstash가 데이터를 전송하고 있는지 확인: `docker logs sparta-ecommerce-logstash`

### 문제: 쿼리 결과가 비어있음

**해결:**
1. Kafka에서 이벤트가 발행되고 있는지 확인
2. Logstash가 Kafka에서 메시지를 수신하고 있는지 확인
3. 쿼리의 필터 조건 (시간 범위, orderStatus 등) 확인

## 참고 자료

- [Elasticsearch Index Templates](https://www.elastic.co/guide/en/elasticsearch/reference/current/index-templates.html)
- [Elasticsearch Aggregations](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations.html)
- [Logstash Configuration](https://www.elastic.co/guide/en/logstash/current/configuration.html)
- [Nested Aggregations](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-nested-aggregation.html)
