#!/bin/bash

# Logstash 동작 검증 스크립트
# 수집, 필터링, Elasticsearch 저장이 제대로 되는지 확인

set -e

ES_HOST="${ELASTICSEARCH_HOST:-localhost:9200}"
LOGSTASH_HOST="${LOGSTASH_HOST:-localhost:9600}"

echo "========================================="
echo "Logstash 동작 검증 시작"
echo "========================================="
echo ""

# 색상 코드
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. Logstash 헬스 체크
echo "1. Logstash 상태 확인..."
if curl -sf "http://${LOGSTASH_HOST}/" > /dev/null; then
    echo -e "${GREEN}✓ Logstash가 실행 중입니다${NC}"
else
    echo -e "${RED}✗ Logstash에 연결할 수 없습니다${NC}"
    exit 1
fi

# 2. Logstash 파이프라인 상태 확인
echo ""
echo "2. Logstash 파이프라인 상태 확인..."
PIPELINE_STATUS=$(curl -s "http://${LOGSTASH_HOST}/_node/stats/pipelines" | jq -r '.pipelines.main.reloads.successes')
if [ "$PIPELINE_STATUS" != "null" ]; then
    echo -e "${GREEN}✓ 파이프라인이 정상적으로 로드되었습니다${NC}"
    curl -s "http://${LOGSTASH_HOST}/_node/stats/pipelines" | jq '.pipelines.main.plugins | {inputs: .inputs, filters: .filters, outputs: .outputs}'
else
    echo -e "${RED}✗ 파이프라인 로드에 실패했습니다${NC}"
fi

# 3. Elasticsearch 연결 확인
echo ""
echo "3. Elasticsearch 연결 확인..."
if curl -sf "http://${ES_HOST}/_cluster/health" > /dev/null; then
    echo -e "${GREEN}✓ Elasticsearch에 연결되었습니다${NC}"
    curl -s "http://${ES_HOST}/_cluster/health" | jq '{status: .status, number_of_nodes: .number_of_nodes}'
else
    echo -e "${RED}✗ Elasticsearch에 연결할 수 없습니다${NC}"
    exit 1
fi

# 4. 인덱스 템플릿 확인
echo ""
echo "4. 인덱스 템플릿 확인..."

# audit-user-activity 템플릿 확인
if curl -sf "http://${ES_HOST}/_index_template/audit-user-activity-template" > /dev/null; then
    echo -e "${GREEN}✓ audit-user-activity 템플릿이 등록되어 있습니다${NC}"
else
    echo -e "${YELLOW}⚠ audit-user-activity 템플릿이 없습니다${NC}"
fi

# event-order 템플릿 확인
if curl -sf "http://${ES_HOST}/_index_template/event-order-template" > /dev/null; then
    echo -e "${GREEN}✓ event-order 템플릿이 등록되어 있습니다${NC}"
else
    echo -e "${YELLOW}⚠ event-order 템플릿이 없습니다${NC}"
fi

# 5. 인덱스 생성 확인
echo ""
echo "5. 생성된 인덱스 확인..."
AUDIT_INDICES=$(curl -s "http://${ES_HOST}/_cat/indices/audit-user-activity-*?format=json" | jq -r '.[].index' | wc -l | tr -d ' ')
ORDER_INDICES=$(curl -s "http://${ES_HOST}/_cat/indices/event-order-*?format=json" | jq -r '.[].index' | wc -l | tr -d ' ')

echo "audit-user-activity 인덱스 수: $AUDIT_INDICES"
if [ "$AUDIT_INDICES" -gt 0 ]; then
    echo -e "${GREEN}✓ audit-user-activity 인덱스가 생성되었습니다${NC}"
    curl -s "http://${ES_HOST}/_cat/indices/audit-user-activity-*?v&h=index,docs.count,store.size"
else
    echo -e "${YELLOW}⚠ audit-user-activity 인덱스가 아직 생성되지 않았습니다${NC}"
fi

echo ""
echo "event-order 인덱스 수: $ORDER_INDICES"
if [ "$ORDER_INDICES" -gt 0 ]; then
    echo -e "${GREEN}✓ event-order 인덱스가 생성되었습니다${NC}"
    curl -s "http://${ES_HOST}/_cat/indices/event-order-*?v&h=index,docs.count,store.size"
else
    echo -e "${YELLOW}⚠ event-order 인덱스가 아직 생성되지 않았습니다${NC}"
fi

# 6. 실제 데이터 확인
echo ""
echo "6. 저장된 데이터 샘플 확인..."

# audit-user-activity 데이터 확인
echo ""
echo "6-1. audit-user-activity 데이터:"
AUDIT_COUNT=$(curl -s "http://${ES_HOST}/audit-user-activity-*/_count" | jq -r '.count')
echo "총 문서 수: ${AUDIT_COUNT:-0}"

if [ "${AUDIT_COUNT:-0}" -gt 0 ]; then
    echo -e "${GREEN}✓ audit 데이터가 저장되어 있습니다${NC}"
    echo ""
    echo "최근 데이터 1건:"
    curl -s "http://${ES_HOST}/audit-user-activity-*/_search?size=1&sort=@timestamp:desc" | jq '.hits.hits[0]._source | {
        timestamp: .["@timestamp"],
        userId: .userId,
        action: .action,
        method: .method,
        path: .path,
        statusCode: .statusCode,
        status_category: .status_category,
        responseTime: .responseTime,
        performance: .performance,
        service: .service,
        resource: .resource
    }'
else
    echo -e "${YELLOW}⚠ audit 데이터가 아직 저장되지 않았습니다${NC}"
fi

# event-order 데이터 확인
echo ""
echo "6-2. event-order 데이터:"
ORDER_COUNT=$(curl -s "http://${ES_HOST}/event-order-*/_count" | jq -r '.count')
echo "총 문서 수: ${ORDER_COUNT:-0}"

if [ "${ORDER_COUNT:-0}" -gt 0 ]; then
    echo -e "${GREEN}✓ order 이벤트 데이터가 저장되어 있습니다${NC}"
    echo ""
    echo "최근 데이터 1건:"
    curl -s "http://${ES_HOST}/event-order-*/_search?size=1&sort=@timestamp:desc" | jq '.hits.hits[0]._source | {
        timestamp: .["@timestamp"],
        eventType: .eventType,
        orderId: .orderId,
        userId: .userId,
        orderStatus: .orderStatus,
        totalAmount: .totalAmount,
        items: .items
    }'
else
    echo -e "${YELLOW}⚠ order 이벤트 데이터가 아직 저장되지 않았습니다${NC}"
fi

# 7. 필터링 검증 (성능, 상태 카테고리 등이 제대로 추가되었는지)
echo ""
echo "7. 필터링 기능 검증..."

if [ "${AUDIT_COUNT:-0}" -gt 0 ]; then
    echo ""
    echo "7-1. 성능 등급별 분포 (performance 필드):"
    curl -s "http://${ES_HOST}/audit-user-activity-*/_search?size=0" -H 'Content-Type: application/json' -d'
    {
      "aggs": {
        "performance_distribution": {
          "terms": {
            "field": "performance",
            "size": 10
          }
        }
      }
    }' | jq '.aggregations.performance_distribution.buckets[] | {performance: .key, count: .doc_count}'

    echo ""
    echo "7-2. HTTP 상태 카테고리별 분포 (status_category 필드):"
    curl -s "http://${ES_HOST}/audit-user-activity-*/_search?size=0" -H 'Content-Type: application/json' -d'
    {
      "aggs": {
        "status_distribution": {
          "terms": {
            "field": "status_category",
            "size": 10
          }
        }
      }
    }' | jq '.aggregations.status_distribution.buckets[] | {status: .key, count: .doc_count}'

    echo ""
    echo "7-3. 서비스별 분포 (service 필드):"
    curl -s "http://${ES_HOST}/audit-user-activity-*/_search?size=0" -H 'Content-Type: application/json' -d'
    {
      "aggs": {
        "service_distribution": {
          "terms": {
            "field": "service",
            "size": 10
          }
        }
      }
    }' | jq '.aggregations.service_distribution.buckets[] | {service: .key, count: .doc_count}'

    if curl -s "http://${ES_HOST}/audit-user-activity-*/_search?size=1&q=performance:*" | jq -e '.hits.total.value > 0' > /dev/null; then
        echo -e "${GREEN}✓ 필터링(performance)이 제대로 적용되었습니다${NC}"
    else
        echo -e "${YELLOW}⚠ 필터링(performance)이 적용되지 않았습니다${NC}"
    fi

    if curl -s "http://${ES_HOST}/audit-user-activity-*/_search?size=1&q=status_category:*" | jq -e '.hits.total.value > 0' > /dev/null; then
        echo -e "${GREEN}✓ 필터링(status_category)이 제대로 적용되었습니다${NC}"
    else
        echo -e "${YELLOW}⚠ 필터링(status_category)이 적용되지 않았습니다${NC}"
    fi
fi

# 8. Logstash 처리 통계
echo ""
echo "8. Logstash 처리 통계..."
curl -s "http://${LOGSTASH_HOST}/_node/stats/pipelines" | jq '.pipelines.main.events | {
    in: .in,
    filtered: .filtered,
    out: .out,
    duration_in_millis: .duration_in_millis
}'

echo ""
echo "========================================="
echo "검증 완료"
echo "========================================="
