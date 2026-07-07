#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

need() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

http_json() {
  local method="$1"
  local path="$2"
  local token="${3:-}"
  local body="${4:-}"
  local output="$5"
  local status

  if [[ -n "$token" && -n "$body" ]]; then
    status="$(curl -sS -o "$output" -w '%{http_code}' -X "$method" \
      -H 'Content-Type: application/json' \
      -H "Authorization: Bearer $token" \
      --data "$body" \
      "$BASE_URL$path")"
  elif [[ -n "$token" ]]; then
    status="$(curl -sS -o "$output" -w '%{http_code}' -X "$method" \
      -H "Authorization: Bearer $token" \
      "$BASE_URL$path")"
  elif [[ -n "$body" ]]; then
    status="$(curl -sS -o "$output" -w '%{http_code}' -X "$method" \
      -H 'Content-Type: application/json' \
      --data "$body" \
      "$BASE_URL$path")"
  else
    status="$(curl -sS -o "$output" -w '%{http_code}' -X "$method" "$BASE_URL$path")"
  fi

  echo "$status"
}

assert_status() {
  local label="$1"
  local expected="$2"
  local actual="$3"
  local body_file="$4"

  if [[ "$actual" == "$expected" ]]; then
    echo "PASS $label -> HTTP $actual" >&2
  else
    echo "FAIL $label -> expected HTTP $expected, got HTTP $actual" >&2
    if [[ -s "$body_file" ]]; then
      cat "$body_file" >&2
    else
      echo "No response body. Is the API running at $BASE_URL?" >&2
    fi
    exit 1
  fi
}

login() {
  local label="$1"
  local email="$2"
  local output="$TMP_DIR/login-$label.json"
  local status

  status="$(http_json POST /api/v1/auth/login "" "{\"email\":\"$email\",\"password\":\"Password123!\"}" "$output")"
  assert_status "login $label" 200 "$status" "$output"
  node -e 'const fs=require("fs"); const j=JSON.parse(fs.readFileSync(process.argv[1],"utf8")); process.stdout.write(j.accessToken || "");' "$output"
}

need curl
need node

echo "BankCore smoke test"
echo "BASE_URL=$BASE_URL"

health_file="$TMP_DIR/health.json"
health_status="$(http_json GET /actuator/health "" "" "$health_file")"
assert_status "health" 200 "$health_status" "$health_file"

admin_token="$(login admin admin@bankcore.local)"
employee_token="$(login employee employee@bankcore.local)"
alice_token="$(login alice alice@bankcore.local)"

dashboard_file="$TMP_DIR/dashboard.json"
dashboard_status="$(http_json GET /api/v1/admin/dashboard "$admin_token" "" "$dashboard_file")"
assert_status "admin dashboard" 200 "$dashboard_status" "$dashboard_file"

employee_accounts_file="$TMP_DIR/employee-accounts.json"
employee_accounts_status="$(http_json GET /api/v1/accounts "$employee_token" "" "$employee_accounts_file")"
assert_status "employee accounts" 200 "$employee_accounts_status" "$employee_accounts_file"

alice_accounts_file="$TMP_DIR/alice-accounts.json"
alice_accounts_status="$(http_json GET /api/v1/accounts "$alice_token" "" "$alice_accounts_file")"
assert_status "alice own accounts" 200 "$alice_accounts_status" "$alice_accounts_file"

forbidden_file="$TMP_DIR/alice-dashboard.json"
forbidden_status="$(http_json GET /api/v1/admin/dashboard "$alice_token" "" "$forbidden_file")"
assert_status "alice dashboard forbidden" 403 "$forbidden_status" "$forbidden_file"

echo "PASS smoke test complete"
