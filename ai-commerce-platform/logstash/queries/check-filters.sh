#!/bin/bash

# Logstash 필터링 결과 검증 스크립트
# performance, status_category 등 필터로 추가된 필드가 제대로 있는지 확인

ES_HOST="${ELASTICSEARCH_HOST:-localhost:9200}"

echo "================================"
echo "Logstash 필터링 결과 검증"
echo "================================"

# 1. performance 필드 집계
echo ""
echo "1. 성능 등급별 분포 (performance 필드):"
echo "   - excellent: < 100ms"
echo "   - good: < 500ms"
echo "   - acceptable: < 1000ms"
echo "   - poor: >= 1000ms"
echo ""
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
}' | jq -r '.aggregations.performance_distribution.buckets[] | "\(.key): \(.doc_count)건"'

# 2. status_category 필드 집계
echo ""
echo "2. HTTP 상태 카테고리별 분포 (status_category 필드):"
echo "   - success: 2xx"
echo "   - redirect: 3xx"
echo "   - client_error: 4xx"
echo "   - server_error: 5xx"
echo ""
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
}' | jq -r '.aggregations.status_distribution.buckets[] | "\(.key): \(.doc_count)건"'

# 3. service 필드 집계
echo ""
echo "3. 서비스별 요청 분포 (service 필드):"
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
}' | jq -r '.aggregations.service_distribution.buckets[] | "\(.key): \(.doc_count)건"'

# 4. resource 필드 집계
echo ""
echo "4. 리소스별 요청 분포 (resource 필드):"
curl -s "http://${ES_HOST}/audit-user-activity-*/_search?size=0" -H 'Content-Type: application/json' -d'
{
  "aggs": {
    "resource_distribution": {
      "terms": {
        "field": "resource",
        "size": 10
      }
    }
  }
}' | jq -r '.aggregations.resource_distribution.buckets[] | "\(.key): \(.doc_count)건"'

# 5. method 필드 집계
echo ""
echo "5. HTTP 메서드별 분포 (method 필드):"
curl -s "http://${ES_HOST}/audit-user-activity-*/_search?size=0" -H 'Content-Type: application/json' -d'
{
  "aggs": {
    "method_distribution": {
      "terms": {
        "field": "method",
        "size": 10
      }
    }
  }
}' | jq -r '.aggregations.method_distribution.buckets[] | "\(.key): \(.doc_count)건"'

# 6. 응답시간 통계
echo ""
echo "6. 응답시간 통계 (responseTime 필드):"
curl -s "http://${ES_HOST}/audit-user-activity-*/_search?size=0" -H 'Content-Type: application/json' -d'
{
  "aggs": {
    "response_time_stats": {
      "stats": {
        "field": "responseTime"
      }
    }
  }
}' | jq -r '.aggregations.response_time_stats | "평균: \(.avg)ms\n최소: \(.min)ms\n최대: \(.max)ms\n합계: \(.sum)ms\n개수: \(.count)건"'

# 7. User-Agent 파싱 확인 (ua 필드)
echo ""
echo "7. User-Agent 파싱 결과 (ua.name 필드):"
curl -s "http://${ES_HOST}/audit-user-activity-*/_search?size=0" -H 'Content-Type: application/json' -d'
{
  "aggs": {
    "browser_distribution": {
      "terms": {
        "field": "ua.name",
        "size": 10
      }
    }
  }
}' | jq -r '.aggregations.browser_distribution.buckets[] | "\(.key): \(.doc_count)건"'

echo ""
echo "8. OS 분포 (ua.os_name 필드):"
curl -s "http://${ES_HOST}/audit-user-activity-*/_search?size=0" -H 'Content-Type: application/json' -d'
{
  "aggs": {
    "os_distribution": {
      "terms": {
        "field": "ua.os_name",
        "size": 10
      }
    }
  }
}' | jq -r '.aggregations.os_distribution.buckets[] | "\(.key): \(.doc_count)건"'

# 9. 필터가 적용된 샘플 데이터 보기
echo ""
echo "9. 필터가 적용된 샘플 데이터 (1건):"
curl -s "http://${ES_HOST}/audit-user-activity-*/_search?size=1&sort=@timestamp:desc" | jq '.hits.hits[0]._source | {
    timestamp: .["@timestamp"],
    userId: .userId,
    method: .method,
    path: .path,
    service: .service,
    resource: .resource,
    statusCode: .statusCode,
    status_category: .status_category,
    responseTime: .responseTime,
    performance: .performance,
    userAgent: .userAgent,
    ua: .ua,
    environment: .environment,
    platform: .platform,
    tags: .tags
}'

echo ""
echo "================================"
