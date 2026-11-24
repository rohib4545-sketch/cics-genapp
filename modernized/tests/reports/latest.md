# Post-deployment functional test report
- Commit: de0bb02915a65d288c33993afc2ce610f11b4308
- Timestamp: 2026-11-24T19:12:01Z
- Backend: https://genapp-backend-ziqswljzga-uc.a.run.app
- Frontend: https://genapp-frontend-ziqswljzga-uc.a.run.app
- Total: 49 — passed: 49 — failed: 0 — skipped: 0
- Pass rate: 100%

```
ok   1 - health endpoint
ok   2 - health content type json
ok   3 - add customer returns generated number >= 1000001
ok   4 - postcode uppercased
ok   5 - blank customer is legal
ok   6 - over-length firstName truncated to 10 (FIRSTNAME CHAR(10))
ok   8 - inquire existing customer
ok   9 - inquire missing customer RC01/404
ok   10 - update customer persisted
ok   11 - update missing customer RC01/404
ok   12 - customer numbers increment
ok   13 - full field round-trip
ok   14 - customer policies initially empty
ok   15 - add endowment returns generated policy number
ok   23 - endowment detail round-trip
ok   28 - policy+subtype atomic
ok   16 - add house
ok   24 - house detail round-trip
ok   17 - add motor
ok   25 - motor detail round-trip
ok   18 - add commercial, zipcode uppercased
ok   26 - commercial detail round-trip
ok   19 - add policy unknown customer RC70/422
ok   20 - invalid policy type RC99/400
ok   21 - defaults brokerId=0 payment=0
ok   22 - issue/expiry dates round-trip
ok   29 - inquire existing policy
ok   30 - inquire mismatched customer RC01
ok   31 - inquire valid number under wrong type RC01
ok   32 - inquire missing policy RC01
ok   27 - policy numbers unique
ok   33 - update endowment persisted
ok   34 - update house persisted
ok   35 - update motor persisted
ok   36 - commercial update unsupported RC99
ok   37 - update missing policy RC01
ok   45 - commercial search no params RC98
ok   46 - 01ICOM cust+policy single result
ok   47 - 02ICOM policy only
ok   48 - 03ICOM customer only list
ok   49 - 05ICOM postcode lowercase input matches
ok   50 - commercial search empty RC01
ok   38 - delete motor then inquire 404
ok   43 - delete cascades subtype (inquire RC01)
ok   39 - delete endowment
ok   40 - delete house
ok   41 - delete commercial
ok   42 - delete missing policy RC01
ok   44 - re-add after delete

PASSED 49/49
```
