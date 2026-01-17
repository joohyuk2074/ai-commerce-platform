#!/bin/bash

# Elasticsearch 데이터 간단 조회 스크립트

ES_HOST="${ELASTICSEARCH_HOST:-localhost:9200}"

echo "================================"
echo "Elasticsearch 데이터 조회"
echo "================================"

# 인덱스 목록
echo ""
echo "1. 모든 인덱스 목록:"
curl -s "http://${ES_HOST}/_cat/indices?v&h=index,docs.count,store.size&s=index:desc"

# audit 데이터 최근 10건
echo ""
echo "2. audit-user-activity 최근 10건:"
curl -s "http://${ES_HOST}/audit-user-activity-*/_search?size=10&sort=@timestamp:desc" -H 'Content-Type: application/json' | jq '.hits.hits[] | {
    timestamp: ._source["@timestamp"],
    user: ._source.userId,
    action: ._source.action,
    path: ._source.path,
    status: ._source.statusCode,
    responseTime: ._source.responseTime,
    performance: ._source.performance
}'

# order 이벤트 최근 10건
echo ""
echo "3. event-order 최근 10건:"
curl -s "http://${ES_HOST}/event-order-*/_search?size=10&sort=@timestamp:desc" -H 'Content-Type: application/json' | jq '.hits.hits[] | {
    timestamp: ._source["@timestamp"],
    eventType: ._source.eventType,
    orderId: ._source.orderId,
    userId: ._source.userId,
    status: ._source.orderStatus,
    amount: ._source.totalAmount
}'
