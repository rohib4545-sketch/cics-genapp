# GenApp Functional Test Cases

Derived from `modernized/docs/technical-analysis.md` (legacy COMMAREA contract, request IDs, return codes, business rules) — not from the implementation code. Executed by `functional_tests.sh` against any running backend (`BASE_URL`).

Legacy mapping: customers 01ACUS/01ICUS/01UCUS; policies 01A*/01I*/01U*/01D* per type (E=endowment, H=house, M=motor, C=commercial); commercial inquire variants 01ICOM (customer+policy), 02ICOM (policy only), 03ICOM (customer only), 05ICOM (postcode). RC 00=ok, 01=not found, 70=customer missing, 98=commarea too short / missing params, 99=unknown/unsupported request.

## Health
| # | Case | Expected |
|---|---|---|
| 1 | GET /health | 200, `status=UP` |
| 2 | GET /health content type | JSON |

## Customers (01ACUS / 01ICUS / 01UCUS)
| # | Case | Expected |
|---|---|---|
| 3 | Add customer, minimal fields | 201, generated number ≥ 1000001 (identity start) |
| 4 | Add with lowercase postcode | stored uppercase (legacy upper-cases postcode) |
| 5 | Add completely blank customer | 201 — legacy performs no validation |
| 6 | Add with over-length fields (firstName>20 etc.) | truncated to Db2 column widths |
| 7 | Security row created on add | state `N`, passChanges 0, default password checksum `5732fec825535eeafb8fac50fee3a8aa` (verified by backend unit test) |
| 8 | Inquire existing customer | 200, fields round-trip |
| 9 | Inquire missing customer | 404, RC `01` |
| 10 | Update customer | 200, changed fields persisted on re-inquire |
| 11 | Update missing customer | 404, RC `01` |
| 12 | Two consecutive adds | numbers increment by 1 |
| 13 | Full 9-field round-trip | all fields preserved |
| 14 | GET /api/customers/{n}/policies initially | 200, empty list |

## Policy add (01AEND/01AHOU/01AMOT/01ACOM)
| # | Case | Expected |
|---|---|---|
| 15 | Add endowment policy | 201, system-generated policyNumber |
| 16 | Add house policy | 201 |
| 17 | Add motor policy | 201 |
| 18 | Add commercial policy | 201, zipcode upper-cased |
| 19 | Add policy for unknown customer | 422, RC `70` (SQLCODE -530 parity) |
| 20 | Add policy with invalid type path | 400, RC `99` |
| 21 | Defaults when brokerId/payment omitted | brokerId 0, payment 0 (driver default parity) |
| 22 | issueDate/expiryDate round-trip | ISO dates preserved |
| 23 | Endowment detail round-trip | term, sumAssured, lifeAssured, withProfit, compensation, pensionsReview |
| 24 | House detail round-trip | propertyType, bedrooms, value, houseName/Number, postcode |
| 25 | Motor detail round-trip | make, model, value, registration, colour, cc, yearOfManufacture, premium, accidents |
| 26 | Commercial detail round-trip | perils (fire/crime/flood/weather + premiums), status, rejectionReason, address fields |
| 27 | Policy numbers unique across types | no collisions |
| 28 | Policy+subtype atomic insert | inquire immediately after add returns detail block |

## Policy inquire (01IEND/01IHOU/01IMOT)
| # | Case | Expected |
|---|---|---|
| 29 | Inquire existing policy by type+number | 200, RC implied 00 |
| 30 | Inquire with mismatched customerNumber | 404, RC `01` |
| 31 | Inquire valid number under wrong type | 404, RC `01` |
| 32 | Inquire missing policy | 404, RC `01` |

## Policy update (01UEND/01UHOU/01UMOT)
| # | Case | Expected |
|---|---|---|
| 33 | Update endowment policy | 200, changes persisted |
| 34 | Update house policy | 200, changes persisted |
| 35 | Update motor policy | 200, changes persisted |
| 36 | Update commercial policy | 400, RC `99` — legacy has no commercial update |
| 37 | Update missing policy | 404, RC `01` |

## Policy delete (01DEND/01DHOU/01DMOT/01DCOM)
| # | Case | Expected |
|---|---|---|
| 38 | Delete motor policy | 204; subsequent inquire → 404 |
| 39 | Delete endowment policy | 204; inquire → 404 |
| 40 | Delete house policy | 204; inquire → 404 |
| 41 | Delete commercial policy | 204; inquire → 404 |
| 42 | Delete missing policy | 404, RC `01` |
| 43 | Delete cascades subtype | re-inquire returns RC `01` (FK cascade parity) |
| 44 | Re-add after delete | new policyNumber accepted |

## Commercial inquire variants (01ICOM/02ICOM/03ICOM/05ICOM)
| # | Case | Expected |
|---|---|---|
| 45 | No parameters | 400, RC `98` (commarea too short parity) |
| 46 | customerNumber+policyNumber (01ICOM) | single matching result |
| 47 | policyNumber only (02ICOM) | single result |
| 48 | customerNumber only (03ICOM) | list of that customer's commercial policies |
| 49 | postcode only (05ICOM), lowercase input | matches uppercase stored zipcode |
| 50 | Variant with no matching rows | 404, RC `01` |

Pass criteria per the pipeline spec: iterate fixes until 100% pass; cases that depend on pure-mainframe behavior with no modern equivalent (e.g. CICS TSQ counters, VSAM mirror files) are excluded by design.
