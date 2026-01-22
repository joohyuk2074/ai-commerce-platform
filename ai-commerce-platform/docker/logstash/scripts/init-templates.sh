#!/bin/bash

set -e

ELASTICSEARCH_HOST="${ELASTICSEARCH_HOST:-elasticsearch:9200}"
TEMPLATES_DIR="/templates"
MAX_RETRIES=30
RETRY_INTERVAL=2

echo "=========================================="
echo "Elasticsearch Index Template Initializer"
echo "=========================================="
echo "Target: $ELASTICSEARCH_HOST"
echo "Templates Directory: $TEMPLATES_DIR"
echo ""

# Wait for Elasticsearch to be ready
echo "Waiting for Elasticsearch to be ready..."
retry_count=0
until curl -s "http://$ELASTICSEARCH_HOST/_cluster/health" > /dev/null; do
  retry_count=$((retry_count + 1))
  if [ $retry_count -ge $MAX_RETRIES ]; then
    echo "ERROR: Elasticsearch is not available after $MAX_RETRIES retries"
    exit 1
  fi
  echo "Attempt $retry_count/$MAX_RETRIES: Elasticsearch not ready yet, waiting ${RETRY_INTERVAL}s..."
  sleep $RETRY_INTERVAL
done

echo "Elasticsearch is ready!"
echo ""

# Wait for cluster to be at least yellow
echo "Waiting for cluster status to be yellow or green..."
retry_count=0
until [ "$(curl -s "http://$ELASTICSEARCH_HOST/_cluster/health" | grep -o '"status":"[^"]*"' | cut -d':' -f2 | tr -d '"')" != "red" ]; do
  retry_count=$((retry_count + 1))
  if [ $retry_count -ge $MAX_RETRIES ]; then
    echo "ERROR: Cluster status is still red after $MAX_RETRIES retries"
    exit 1
  fi
  echo "Attempt $retry_count/$MAX_RETRIES: Cluster status is red, waiting ${RETRY_INTERVAL}s..."
  sleep $RETRY_INTERVAL
done

echo "Cluster status is healthy!"
echo ""

# Register index templates
echo "Registering index templates..."
echo ""

template_count=0
success_count=0
failure_count=0

for template_file in "$TEMPLATES_DIR"/*.json; do
  if [ ! -f "$template_file" ]; then
    echo "No template files found in $TEMPLATES_DIR"
    break
  fi

  template_count=$((template_count + 1))
  template_name=$(basename "$template_file" .json)

  echo "[$template_count] Processing template: $template_name"
  echo "    File: $template_file"

  # Validate JSON
  if ! jq empty "$template_file" 2>/dev/null; then
    echo "    ERROR: Invalid JSON format"
    failure_count=$((failure_count + 1))
    echo ""
    continue
  fi

  # Register template
  response=$(curl -s -w "\n%{http_code}" -X PUT \
    "http://$ELASTICSEARCH_HOST/_index_template/$template_name" \
    -H 'Content-Type: application/json' \
    -d @"$template_file")

  http_code=$(echo "$response" | tail -n1)
  response_body=$(echo "$response" | sed '$d')

  if [ "$http_code" = "200" ]; then
    echo "    SUCCESS: Template registered (HTTP $http_code)"
    success_count=$((success_count + 1))
  else
    echo "    ERROR: Failed to register template (HTTP $http_code)"
    echo "    Response: $response_body"
    failure_count=$((failure_count + 1))
  fi

  echo ""
done

# Summary
echo "=========================================="
echo "Summary:"
echo "  Total templates: $template_count"
echo "  Success: $success_count"
echo "  Failed: $failure_count"
echo "=========================================="

if [ $failure_count -gt 0 ]; then
  echo "WARNING: Some templates failed to register"
  exit 1
fi

echo "All templates registered successfully!"
exit 0
