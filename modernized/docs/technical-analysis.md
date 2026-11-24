# GenApp Detailed Technical Analysis

Source repository: `rohib4545sketch/cics-genapp` — IBM CICS TS General Insurance application ("GenApp", SupportPac CB12 heritage).
Analysis basis: direct read of `base/src` (COBOL/copybooks/BMS), `base/cntl` (JCL), `base/data` (KSDS data), plus the Devin Wiki index of the codebase.

## 1. Application overview

GenApp is a general-insurance policy administration system. End users interact via 3270 "green screen" BMS maps driving CICS pseudo-conversational transactions. Data is stored in Db2 (primary) with a VSAM KSDS mirror written in parallel ("GenApp Lite" style dual write). Core capabilities:

- Customer: add, inquire, update
- Policy: add, inquire, update, delete across four policy types — Endowment, House, Motor, Commercial
- Commercial policy inquiry variants: by customer+policy, by policy only, by customer only, by postcode
- Customer security record (password hash, state, change count) maintained alongside customer
- Named-counter / TSQ bookkeeping of low/high customer numbers (`GENACNTL`), error logging to TDQ `GENAERRS`

## 2. Source inventory

### 2.1 3270 driver programs (presentation layer, use BMS mapset `SSMAP`)

| Program | Transaction | Purpose |
|---|---|---|
| LGTESTC1 | SSC1 | Customer menu: inquire (1), add (2), update (4) |
| LGTESTP1 | SSP1 | Motor policy menu: inquire/add/delete/update |
| LGTESTP2 | SSP2 | Endowment policy menu |
| LGTESTP3 | SSP3 | House policy menu |
| LGTESTP4 | SSP4 | Commercial policy menu (4 inquire variants + add/delete) |

### 2.2 Business-logic layer (invoked via EXEC CICS LINK with COMMAREA)

| Program | Function |
|---|---|
| LGACUS01 | Add customer — validates commarea length only; links LGACDB01 |
| LGICUS01 | Inquire customer — links LGICDB01 |
| LGUCUS01 | Update customer — links LGUCDB01 |
| LGAPOL01 | Add policy — links LGAPDB01 |
| LGIPOL01 | Inquire policy — links LGIPDB01 |
| LGDPOL01 | Delete policy — links LGDPDB01 |
| LGUPOL01 | Update policy — links LGUPDB01 |

### 2.3 Db2 data-access layer

| Program | Function |
|---|---|
| LGACDB01 | INSERT customer row (identity-generated customerNumber), links LGACVS01 (VSAM mirror) and LGACDB02 (security row) |
| LGACDB02 | INSERT customer_secure row (request `02ACUS`); default password BD5 checksum `5732fec825535eeafb8fac50fee3a8aa` |
| LGICDB01 | SELECT customer by customerNumber |
| LGUCDB01 | UPDATE customer; links LGUCVS01 |
| LGAPDB01 | INSERT policy + subtype row (ENDOWMENT/HOUSE/MOTOR/COMMERCIAL) |
| LGIPDB01 | SELECT policy joined with subtype table; scroll cursors for commercial variants |
| LGUPDB01 | UPDATE policy + subtype using `WITH HOLD FOR UPDATE` cursor (types E/H/M only — no commercial update) |
| LGDPDB01 | DELETE policy row; FK ON DELETE CASCADE removes subtype + claims |

### 2.4 VSAM mirror layer (GenApp Lite)

LGACVS01, LGICVS01, LGUCVS01, LGAPVS01, LGIPVS01, LGDPVS01, LGUPVS01 — KSDS read/write mirrors of customer/policy data.

### 2.5 Support programs

| Program | Function |
|---|---|
| LGSETUP | Reset TSQ GENACNTL and named counter GENACUSTNUM after DB restore |
| LGASTAT1 / LGWEBST5 | Business-monitor statistics sampling/display (60 s refresh) |
| LGSTSQ | Append error messages to TDQ (default GENAERRS) |

### 2.6 Copybooks

- `lgcmarea.cpy` — COMMAREA: `CA-REQUEST-ID X(6)`, `CA-RETURN-CODE 9(2)`, `CA-CUSTOMER-NUM 9(10)`, then request-specific redefines (customer, customer-security, policy common + per-type redefines, claim).
- `lgpolicy.cpy` — field lengths and Db2 host-variable record layouts for all tables.
- `pollook.cpy`/`polloo2.cpy`, `soaic01/soaipb1/soaipe1/soaiph1/soaipm1/soavcii/soavcio/soavpii/soavpio.cpy` — lookup and SOAP/web-service interface structures.
- `ssmap.bms` — 3270 mapset defining SSC1/SSP1–SSP5 screens.

## 3. COMMAREA contract (the integration boundary)

Header (18 bytes): request-id, return-code, customer-num. Customer payload (72 bytes): firstName X(10), lastName X(20), dob X(10) `yyyy-mm-dd`, houseName X(20), houseNum X(4), postcode X(8), numPolicies 9(3), phoneMobile X(20), phoneHome X(20), email X(100). Policy payload: policyNum 9(10) + common block (issueDate, expiryDate, lastChanged X(26) timestamp, brokerId 9(10), brokersRef X(10), payment 9(6)) + type-specific block:

- **Endowment**: withProfits, equities, managedFund (X each), fundName X(10), term 99, sumAssured 9(6), lifeAssured X(31), paddingData VARCHAR
- **House**: propertyType X(15), bedrooms 9(3), value 9(8), houseName X(20), houseNumber X(4), postcode X(8)
- **Motor**: make X(15), model X(15), value 9(6), regNumber X(7), colour X(8), cc 9(4), manufactured X(10), premium 9(6), accidents 9(6)
- **Commercial**: address X(255), postcode X(8), lat/long X(11), customer X(255), propType X(255), fire/crime/flood/weather peril 9(4) + premium 9(8), status 9(4), rejectReason X(255)
- **Claim**: claimNum 9(10), date X(10), paid 9(8), value 9(8), cause X(255), observations X(255)

### Request IDs

| ID | Meaning |
|---|---|
| 01ACUS / 01ICUS / 01UCUS | customer add / inquire / update |
| 02ACUS | customer security insert (internal) |
| 01AEND / 01AHOU / 01AMOT / 01ACOM | policy add per type |
| 01IEND / 01IHOU / 01IMOT | policy inquire per type |
| 01ICOM / 02ICOM / 03ICOM / 05ICOM | commercial inquire: cust+policy / policy only / customer only / postcode |
| 01DEND / 01DHOU / 01DMOT / 01DCOM | policy delete per type |
| 01UEND / 01UHOU / 01UMOT | policy update per type (no commercial update in the original) |

### Return codes

| RC | Meaning |
|---|---|
| 00 | success |
| 01 | not found (SQLCODE 100 or -913 deadlock on inquire) |
| 02 | cursor error path in update |
| 70 | customer does not exist (FK violation SQLCODE -530 on policy insert) |
| 90 | Db2/SQL error |
| 98 | commarea too short |
| 99 | unknown/unsupported request id |

## 4. Data model (Db2, `base/cntl/db2cre.jcl`)

| Table | Key | Notes |
|---|---|---|
| CUSTOMER | customerNumber IDENTITY start 1000001 | names, dob, address, phones, email |
| CUSTOMER_SECURE | customerNumber FK→CUSTOMER cascade | customerPass CHAR(32), state_indicator, pass_changes |
| POLICY | policyNumber IDENTITY | customerNumber FK cascade, issueDate, expiryDate, policyType CHAR(1) E/H/M/C, lastChanged TIMESTAMP default, brokerId, brokersReference, payment, commission SMALLINT |
| ENDOWMENT | policyNumber FK cascade | equities/withProfits/managedFund, fundName, term SMALLINT, sumAssured, lifeAssured, paddingData VARCHAR(32606) |
| HOUSE | policyNumber FK cascade | propertyType, bedrooms, value, houseName/Number, postcode |
| MOTOR | policyNumber FK cascade | make, model, value, regNumber, colour, cc, yearOfManufacture DATE, premium, accidents |
| COMMERCIAL | policyNumber FK cascade | requestDate timestamp, startDate, renewalDate, address, zipcode, lat/long, customer, propertyType, 4 peril/premium pairs, status, rejectionReason |
| CLAIM | claimNumber IDENTITY | policyNumber FK cascade, claimDate, paid, value, cause, observations |

Seed data: 10+ customers (Andrew Pandy #1, Scott Tracey #2, …) each with the same default password hash; KSDS files `ksdscust.txt`/`ksdspoly.txt` mirror a subset in uppercase fixed-width format.

## 5. Business rules & behaviors (verified in source)

- Add customer performs **no required-field validation** — blank customers are legal; only a commarea shorter than 18+72 bytes returns RC 98. Postcode is upper-cased by the driver; `x'00'` bytes replaced by spaces.
- CA-NUM-POLICIES is always set to `'00'` by the customer programs — policy counting is not implemented.
- Policy insert lets Db2 generate the policy number (`IDENTITY_VAL_LOCAL()`), sets `lastChanged` to CURRENT TIMESTAMP; drivers pass brokerId=0, payment=0, brokersRef=spaces.
- If the subtype insert fails after the POLICY row inserted, the program abends (`LGSQ`) to force rollback — i.e. policy + subtype are atomic.
- Delete relies on FK cascade; only the POLICY row is explicitly deleted.
- Update covers endowment/house/motor only; uses scrollable cursor WITH HOLD FOR UPDATE; -501 (cursor not open) is treated as success (00).
- Commercial inquire supports lookup by customer+policy, policy alone, customer alone (cursor list), or postcode (cursor list).
- Dates are ISO `yyyy-mm-dd` strings; timestamps 26-byte Db2 format.

## 6. Dependencies & integrations

- CICS TS (LINK, SEND/RECEIVE MAP, TSQ/TDQ, ENQ/DEQ, named counters), Db2 for z/OS (embedded SQL, identity columns), VSAM KSDS, BMS/3270, JCL batch (compile/bind/create), REXX customization (cust1.rexx), WSIM workload scripts.
- No external network integrations.

## 7. Modernization risks & constraints

- EBCDIC/uppercase conventions in seed data; postcode upper-casing must be preserved.
- Fixed-width numeric fields are zero-padded 10-digit strings in the commarea — REST contract should use plain integers.
- VSAM mirror layer and TSQ/TDQ bookkeeping have no modern equivalent — replaced by the relational store; TSQ low/high tracking is derivable with MIN/MAX queries.
- Commercial update does not exist in the legacy app — adding one would be an extension, not parity. Parity scope: create/inquire/delete for all four types; update for E/H/M.
- paddingData/large VARCHAR fields are irrelevant to business function — carry as optional text or drop.
- Customer security: legacy stores an unsalted MD5-like hash with no verification flow; modern version keeps the record for parity but need not expose password logic in the UI.

## 8. Recommended target architecture

- **Backend**: Spring Boot 3 / Java 17 / Maven. REST resources: `/api/customers` (POST/GET/PUT), `/api/customers/{id}/policies`, `/api/policies/**` per type with a shared policy envelope + typed detail object, `/api/policies/commercial/search` supporting the four inquire variants, DELETE `/api/policies/{type}/{policyNum}`. H2 (file/embedded) for local parity with schema mirroring Db2; JPA entities per table. Error mapping preserving legacy RC semantics as HTTP codes + `{ returnCode }` body: 00→200, 01→404, 70→422, 90→500, 98/99→400.
- **Frontend**: React (JS) implementing the Figma "Insurance" design; screens: customer search/add/update, policy inquiry/add/update/delete for the four types, commercial search variants.
- **Packaging**: Docker multi-stage builds; GitHub Actions CI (build+test) and CD to Cloud Run (region us-central1) using the repo's GCP service-account secret.
- **Testing**: functional test scripts generated from this document (not from the code), runnable locally and in CI; post-deploy smoke tests against the live Cloud Run URL inside GitHub Actions.
