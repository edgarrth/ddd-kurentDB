#!/usr/bin/env bash
set -euo pipefail

API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"
DATASET_FILE="${DATASET_FILE:-$(dirname "$0")/payments.json}"

command -v curl >/dev/null 2>&1 || { echo "curl is required"; exit 1; }
command -v python3 >/dev/null 2>&1 || { echo "python3 is required to parse JSON responses"; exit 1; }

post_json() {
  local path="$1"
  local payload="$2"
  curl -sS -X POST "${API_BASE_URL}${path}" \
    -H "Content-Type: application/json" \
    -d "$payload"
}

read_json_field() {
  local json="$1"
  local field="$2"
  python3 -c 'import json,sys; print(json.loads(sys.argv[1]).get(sys.argv[2], ""))' "$json" "$field"
}

count=$(python3 -c 'import json,sys; print(len(json.load(open(sys.argv[1]))))' "$DATASET_FILE")
echo "Loading ${count} payment scenarios from ${DATASET_FILE} into ${API_BASE_URL}"

tmp_file="$(mktemp)"
python3 - <<PY > "$tmp_file"
import json
from pathlib import Path
payments = json.loads(Path("$DATASET_FILE").read_text())
for payment in payments:
    print(json.dumps(payment, separators=(",",":")))
PY

loaded=0
while IFS= read -r payment; do
  scenario=$(read_json_field "$payment" "scenario")

  initiate_payload=$(python3 -c '
import json,sys
p=json.loads(sys.argv[1])
print(json.dumps({
  "merchantId": p["merchantId"],
  "customerId": p["customerId"],
  "amount": p["amount"],
  "currency": p["currency"],
  "paymentMethod": p["paymentMethod"],
  "orderId": p["orderId"]
}))' "$payment")

  response=$(post_json "/payments/v1/payments" "$initiate_payload")
  payment_id=$(read_json_field "$response" "paymentId")
  echo "Created payment ${payment_id} scenario=${scenario}"

  case "$scenario" in
    AUTHORIZED)
      post_json "/payments/v1/payments/${payment_id}/authorizations" "{\"authorizationCode\":\"AUTH-${loaded}\"}" >/dev/null
      ;;
    CAPTURED)
      post_json "/payments/v1/payments/${payment_id}/authorizations" "{\"authorizationCode\":\"AUTH-${loaded}\"}" >/dev/null
      post_json "/payments/v1/payments/${payment_id}/captures" "{\"captureReference\":\"CAP-${loaded}\"}" >/dev/null
      ;;
    CAPTURED_WITH_REFUND)
      refund_amount=$(read_json_field "$payment" "refundAmount")
      refund_reason=$(read_json_field "$payment" "refundReason")
      post_json "/payments/v1/payments/${payment_id}/authorizations" "{\"authorizationCode\":\"AUTH-${loaded}\"}" >/dev/null
      post_json "/payments/v1/payments/${payment_id}/captures" "{\"captureReference\":\"CAP-${loaded}\"}" >/dev/null
      post_json "/payments/v1/payments/${payment_id}/refunds" "{\"amount\":${refund_amount},\"reason\":\"${refund_reason}\"}" >/dev/null
      ;;
    FAILED)
      failure_reason=$(read_json_field "$payment" "failureReason")
      post_json "/payments/v1/payments/${payment_id}/failures" "{\"reason\":\"${failure_reason}\"}" >/dev/null
      ;;
    CANCELLED)
      cancel_reason=$(read_json_field "$payment" "cancelReason")
      post_json "/payments/v1/payments/${payment_id}/cancellations" "{\"reason\":\"${cancel_reason}\"}" >/dev/null
      ;;
    *)
      echo "Unknown scenario: ${scenario}" >&2
      exit 1
      ;;
  esac

  loaded=$((loaded + 1))
done < "$tmp_file"
rm -f "$tmp_file"

echo "Done. Loaded ${loaded} payment scenarios."
echo "Open KurrentDB UI: http://localhost:2113"
