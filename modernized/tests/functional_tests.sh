#!/usr/bin/env bash
# GenApp functional tests — implements modernized/tests/test-cases.md.
# Runnable in Devin's VM and in GitHub Actions against any deployed backend.
# Usage: BASE_URL=http://localhost:8080 ./functional_tests.sh
set -u
BASE_URL="${BASE_URL:-http://localhost:8080}"
PASS=0; FAIL=0; FAILED_CASES=()

jget() { python3 -c "import sys,json;d=json.load(sys.stdin);print(eval(sys.argv[1]))" "$2" <<<"$1" 2>/dev/null; }

# check <case#> <description> <actual> <expected>
check() {
  local n="$1" desc="$2" actual="$3" expected="$4"
  if [ "$actual" = "$expected" ]; then PASS=$((PASS+1)); echo "ok   $n - $desc";
  else FAIL=$((FAIL+1)); FAILED_CASES+=("$n"); echo "FAIL $n - $desc (expected [$expected] got [$actual])"; fi
}

# req <method> <path> [json-body] -> sets STATUS and BODY
req() {
  local method="$1" path="$2" body="${3:-}"
  if [ -n "$body" ]; then
    RESP=$(curl -s -w '\n%{http_code}' -X "$method" "$BASE_URL$path" -H 'Content-Type: application/json' -d "$body")
  else
    RESP=$(curl -s -w '\n%{http_code}' -X "$method" "$BASE_URL$path")
  fi
  STATUS="${RESP##*$'\n'}"; BODY="${RESP%$'\n'*}"
}

# ---------- Health ----------
req GET /health
check 1 "health endpoint" "$STATUS $(jget "$BODY" "d['status']" 2>/dev/null)" "200 UP"
CT=$(curl -s -o /dev/null -w '%{content_type}' "$BASE_URL/health")
check 2 "health content type json" "${CT%%;*}" "application/json"

# ---------- Customers ----------
req POST /api/customers '{"firstName":"John","lastName":"Smith","dateOfBirth":"1970-01-01","houseName":"H1","houseNumber":"10","postcode":"ab12cd","phoneHome":"111","phoneMobile":"222","emailAddress":"j@s.com"}'
C1=$(jget "$BODY" "d['customerNumber']")
check 3 "add customer returns generated number >= 1000001" "$STATUS $(( ${C1:-0} >= 1000001 ))" "201 1"
check 4 "postcode uppercased" "$(jget "$BODY" "d['postcode']")" "AB12CD"

req POST /api/customers '{}'
check 5 "blank customer is legal" "$STATUS" "201"
BLANK=$(jget "$BODY" "d['customerNumber']")

req POST /api/customers '{"firstName":"ABCDEFGHIJKLMNOPQRSTUVWXYZ"}'
LEN=$(jget "$BODY" "len(d['firstName'])")
check 6 "over-length firstName truncated to 10 (FIRSTNAME CHAR(10))" "$LEN" "10"

req GET "/api/customers/$C1"
check 8 "inquire existing customer" "$STATUS $(jget "$BODY" "d['lastName']")" "200 Smith"

req GET /api/customers/99999999
check 9 "inquire missing customer RC01/404" "$STATUS $(jget "$BODY" "d['returnCode']")" "404 01"

req PUT "/api/customers/$C1" '{"lastName":"Jones","postcode":"zz99yy"}'
check 10 "update customer persisted" "$STATUS $(jget "$BODY" "d['lastName']") $(jget "$BODY" "d['postcode']")" "200 Jones ZZ99YY"

req PUT /api/customers/99999999 '{"lastName":"X"}'
check 11 "update missing customer RC01/404" "$STATUS $(jget "$BODY" "d['returnCode']")" "404 01"

req POST /api/customers '{"firstName":"A"}'; N1=$(jget "$BODY" "d['customerNumber']")
req POST /api/customers '{"firstName":"B"}'; N2=$(jget "$BODY" "d['customerNumber']")
check 12 "customer numbers increment" "$(( ${N2:-0} - ${N1:-0} ))" "1"

req POST /api/customers '{"firstName":"F","lastName":"L","dateOfBirth":"1985-05-05","houseName":"HH","houseNumber":"7","postcode":"pc11pc","phoneHome":"h","phoneMobile":"m","emailAddress":"e@e.com"}'
check 13 "full field round-trip" "$(jget "$BODY" "d['dateOfBirth']") $(jget "$BODY" "d['houseNumber']")" "1985-05-05 7"

req GET "/api/customers/$BLANK/policies"
check 14 "customer policies initially empty" "$STATUS $BODY" '200 []'

# ---------- Policy add ----------
EP='{"customerNumber":'$C1',"issueDate":"2024-01-01","expiryDate":"2025-01-01","endowment":{"term":10,"sumAssured":100000,"lifeAssured":"J Smith","withProfits":"Y","equities":"Y","managedFund":"N","fundName":"FUND1"}}'
req POST /api/policies/endowment "$EP"
EPN=$(jget "$BODY" "d['common']['policyNumber']")
check 15 "add endowment returns generated policy number" "$STATUS $(( ${EPN:-0} > 0 ))" "201 1"
check 23 "endowment detail round-trip" "$(jget "$BODY" "d['endowment']['term']") $(jget "$BODY" "d['endowment']['withProfits']")" "10 Y"
check 28 "policy+subtype atomic" "$(jget "$BODY" "'endowment' in d and d['endowment'] is not None")" "True"

HP='{"customerNumber":'$C1',"issueDate":"2024-01-01","expiryDate":"2025-01-01","house":{"propertyType":"Detached","bedrooms":4,"value":350000,"houseName":"HH","houseNumber":"7","postcode":"pc11pc"}}'
req POST /api/policies/house "$HP"
HPN=$(jget "$BODY" "d['common']['policyNumber']")
check 16 "add house" "$STATUS" "201"
check 24 "house detail round-trip" "$(jget "$BODY" "d['house']['bedrooms']") $(jget "$BODY" "d['house']['value']")" "4 350000"

MP='{"customerNumber":'$C1',"issueDate":"2024-01-01","expiryDate":"2025-01-01","motor":{"make":"Ford","model":"Focus","value":12000,"regNumber":"AB12CDE","colour":"Blue","cc":1600,"yearOfManufacture":"2020-06-01","premium":450,"accidents":0}}'
req POST /api/policies/motor "$MP"
MPN=$(jget "$BODY" "d['common']['policyNumber']")
check 17 "add motor" "$STATUS" "201"
check 25 "motor detail round-trip" "$(jget "$BODY" "d['motor']['make']") $(jget "$BODY" "d['motor']['cc']")" "Ford 1600"

CP='{"customerNumber":'$C1',"issueDate":"2024-01-01","expiryDate":"2025-01-01","commercial":{"zipcode":"ec1a1bb","firePeril":3,"firePremium":100,"crimePeril":3,"crimePremium":0,"floodPeril":5,"floodPremium":50,"weatherPeril":4,"weatherPremium":0,"status":1,"rejectionReason":"ACTIVE","address":"Main St","customer":"IBM","propertyType":"OFFICES"}}'
req POST /api/policies/commercial "$CP"
CPN=$(jget "$BODY" "d['common']['policyNumber']")
check 18 "add commercial, zipcode uppercased" "$STATUS $(jget "$BODY" "d['commercial']['zipcode']")" "201 EC1A1BB"
check 26 "commercial detail round-trip" "$(jget "$BODY" "d['commercial']['firePremium']") $(jget "$BODY" "d['commercial']['floodPeril']")" "100 5"

req POST /api/policies/motor '{"customerNumber":99999999,"issueDate":"2024-01-01","expiryDate":"2025-01-01","motor":{"make":"X"}}'
check 19 "add policy unknown customer RC70/422" "$STATUS $(jget "$BODY" "d['returnCode']")" "422 70"

req POST /api/policies/boat '{"customerNumber":'$C1'}'
check 20 "invalid policy type RC99/400" "$STATUS $(jget "$BODY" "d['returnCode']")" "400 99"

req GET "/api/policies/motor/$MPN"
check 21 "defaults brokerId=0 payment=0" "$(jget "$BODY" "d['common']['brokerId']") $(jget "$BODY" "d['common']['payment']")" "0 0"
check 22 "issue/expiry dates round-trip" "$(jget "$BODY" "d['common']['issueDate']") $(jget "$BODY" "d['common']['expiryDate']")" "2024-01-01 2025-01-01"
check 29 "inquire existing policy" "$STATUS" "200"

req GET "/api/policies/motor/$MPN?customerNumber=$BLANK"
check 30 "inquire mismatched customer RC01" "$STATUS $(jget "$BODY" "d['returnCode']")" "404 01"

req GET "/api/policies/house/$MPN"
check 31 "inquire valid number under wrong type RC01" "$STATUS $(jget "$BODY" "d['returnCode']")" "404 01"

req GET "/api/policies/motor/99999999"
check 32 "inquire missing policy RC01" "$STATUS $(jget "$BODY" "d['returnCode']")" "404 01"

UNIQ=$(python3 -c "print(len(set('$EPN,$HPN,$MPN,$CPN'.split(',')))==4)")
check 27 "policy numbers unique" "$UNIQ" "True"

# ---------- Policy update ----------
req PUT "/api/policies/endowment/$EPN" '{"customerNumber":'$C1',"issueDate":"2024-01-01","expiryDate":"2026-06-06","endowment":{"term":20,"sumAssured":200000}}'
check 33 "update endowment persisted" "$STATUS $(jget "$BODY" "d['endowment']['term']") $(jget "$BODY" "d['common']['expiryDate']")" "200 20 2026-06-06"

req PUT "/api/policies/house/$HPN" '{"customerNumber":'$C1',"issueDate":"2024-01-01","expiryDate":"2025-01-01","house":{"propertyType":"Flat","bedrooms":2,"value":150000,"houseName":"HH","houseNumber":"7","postcode":"pc11pc"}}'
check 34 "update house persisted" "$STATUS $(jget "$BODY" "d['house']['bedrooms']")" "200 2"

req PUT "/api/policies/motor/$MPN" '{"customerNumber":'$C1',"issueDate":"2024-01-01","expiryDate":"2025-01-01","motor":{"make":"Ford","model":"Fiesta","value":9000,"regNumber":"AB12CDE","colour":"Red","cc":1200,"yearOfManufacture":"2019-01-01","premium":300,"accidents":1}}'
check 35 "update motor persisted" "$STATUS $(jget "$BODY" "d['motor']['model']") $(jget "$BODY" "d['motor']['accidents']")" "200 Fiesta 1"

req PUT "/api/policies/commercial/$CPN" '{"customerNumber":'$C1',"issueDate":"2024-01-01","expiryDate":"2025-01-01","commercial":{"zipcode":"EC1A1BB"}}'
check 36 "commercial update unsupported RC99" "$STATUS $(jget "$BODY" "d['returnCode']")" "400 99"

req PUT "/api/policies/motor/99999999" '{"customerNumber":'$C1',"issueDate":"2024-01-01","expiryDate":"2025-01-01","motor":{"make":"X"}}'
check 37 "update missing policy RC01" "$STATUS $(jget "$BODY" "d['returnCode']")" "404 01"

# ---------- Commercial inquire variants ----------
req GET "/api/policies/commercial/search"
check 45 "commercial search no params RC98" "$STATUS $(jget "$BODY" "d['returnCode']")" "400 98"

req GET "/api/policies/commercial/search?customerNumber=$C1&policyNumber=$CPN"
check 46 "01ICOM cust+policy single result" "$STATUS $(jget "$BODY" "len(d)")" "200 1"

req GET "/api/policies/commercial/search?policyNumber=$CPN"
check 47 "02ICOM policy only" "$STATUS $(jget "$BODY" "len(d)")" "200 1"

req GET "/api/policies/commercial/search?customerNumber=$C1"
check 48 "03ICOM customer only list" "$STATUS $(( $(jget "$BODY" "len(d)") >= 1 ))" "200 1"

req GET "/api/policies/commercial/search?postcode=ec1a1bb"
check 49 "05ICOM postcode lowercase input matches" "$STATUS $(( $(jget "$BODY" "len(d)") >= 1 ))" "200 1"

req GET "/api/policies/commercial/search?postcode=NOPE123"
check 50 "commercial search empty RC01" "$STATUS $(jget "$BODY" "d['returnCode']")" "404 01"

# ---------- Policy delete ----------
req DELETE "/api/policies/motor/$MPN"
check 38 "delete motor then inquire 404" "$STATUS" "204"
req GET "/api/policies/motor/$MPN"
check 43 "delete cascades subtype (inquire RC01)" "$STATUS $(jget "$BODY" "d['returnCode']")" "404 01"

req DELETE "/api/policies/endowment/$EPN"
check 39 "delete endowment" "$STATUS" "204"
req DELETE "/api/policies/house/$HPN"
check 40 "delete house" "$STATUS" "204"
req DELETE "/api/policies/commercial/$CPN"
check 41 "delete commercial" "$STATUS" "204"

req DELETE "/api/policies/motor/99999999"
check 42 "delete missing policy RC01" "$STATUS $(jget "$BODY" "d['returnCode']")" "404 01"

req POST /api/policies/motor "$MP"
check 44 "re-add after delete" "$STATUS $(( $(jget "$BODY" "d['common']['policyNumber']") > 0 ))" "201 1"

echo
TOTAL=$((PASS+FAIL))
echo "PASSED $PASS/$TOTAL"
if [ "$FAIL" -gt 0 ]; then echo "Failed cases: ${FAILED_CASES[*]}"; exit 1; fi
