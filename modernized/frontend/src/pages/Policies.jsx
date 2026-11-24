import { useState } from 'react'
import { api } from '../api.js'

const COMMON_FIELDS = [
  ['customerNumber', 'Customer number'], ['issueDate', 'Issue date (YYYY-MM-DD)'],
  ['expiryDate', 'Expiry date (YYYY-MM-DD)'], ['brokerId', 'Broker ID'],
  ['brokersReference', 'Broker reference'], ['payment', 'Payment'],
]

const TYPE_FIELDS = {
  motor: [
    ['make', 'Make'], ['model', 'Model'], ['value', 'Value'], ['regNumber', 'Registration'],
    ['colour', 'Colour'], ['cc', 'CC'], ['yearOfManufacture', 'Manufactured (YYYY-MM-DD)'],
    ['premium', 'Premium'], ['accidents', 'Accidents'],
  ],
  house: [
    ['propertyType', 'Property type'], ['bedrooms', 'Bedrooms'], ['value', 'Value'],
    ['houseName', 'House name'], ['houseNumber', 'House number'], ['postcode', 'Postcode'],
  ],
  endowment: [
    ['withProfits', 'With profits (Y/N)'], ['equities', 'Equities (Y/N)'], ['managedFund', 'Managed fund (Y/N)'],
    ['fundName', 'Fund name'], ['term', 'Term (years)'], ['sumAssured', 'Sum assured'], ['lifeAssured', 'Life assured'],
  ],
  commercial: [
    ['startDate', 'Start date (YYYY-MM-DD)'], ['renewalDate', 'Renewal date (YYYY-MM-DD)'],
    ['address', 'Address'], ['zipcode', 'Zip/postcode'], ['latitudeN', 'Latitude'], ['longitudeW', 'Longitude'],
    ['customer', 'Business customer'], ['propertyType', 'Property type'],
    ['firePeril', 'Fire peril'], ['firePremium', 'Fire premium'],
    ['crimePeril', 'Crime peril'], ['crimePremium', 'Crime premium'],
    ['floodPeril', 'Flood peril'], ['floodPremium', 'Flood premium'],
    ['weatherPeril', 'Weather peril'], ['weatherPremium', 'Weather premium'],
    ['status', 'Status'], ['rejectionReason', 'Rejection reason'],
  ],
}

const NUMERIC = new Set([
  'customerNumber', 'brokerId', 'payment', 'value', 'cc', 'premium', 'accidents', 'bedrooms',
  'term', 'sumAssured', 'firePeril', 'firePremium', 'crimePeril', 'crimePremium',
  'floodPeril', 'floodPremium', 'weatherPeril', 'weatherPremium', 'status',
])
const DATES = new Set(['issueDate', 'expiryDate', 'yearOfManufacture', 'startDate', 'renewalDate'])

function coerce(key, raw) {
  if (raw === '') return null
  if (NUMERIC.has(key)) return Number(raw)
  if (DATES.has(key)) return raw
  return raw
}

function DetailTable({ policy }) {
  if (!policy) return null
  const rows = []
  const push = (k, v) => v != null && rows.push([k, String(v)])
  const c = policy.common
  push('Policy number', c.policyNumber); push('Customer', c.customerNumber)
  push('Type', c.policyType); push('Issue date', c.issueDate); push('Expiry date', c.expiryDate)
  push('Last changed', c.lastChanged); push('Broker ID', c.brokerId)
  push('Broker ref', c.brokersReference); push('Payment', c.payment)
  const detail = policy.motor || policy.house || policy.endowment || policy.commercial
  if (detail) Object.entries(detail).forEach(([k, v]) => push(k, v))
  return (
    <table className="data" style={{ marginTop: 16 }}>
      <tbody>
        {rows.map(([k, v]) => (
          <tr key={k}><td style={{ color: 'var(--muted)', width: 200 }}>{k}</td><td>{v}</td></tr>
        ))}
      </tbody>
    </table>
  )
}

export default function Policies({ customerNumber }) {
  const [type, setType] = useState('motor')
  const [common, setCommon] = useState({})
  const [detail, setDetail] = useState({})
  const [policyNum, setPolicyNum] = useState('')
  const [result, setResult] = useState(null)
  const [results, setResults] = useState([])
  const [message, setMessage] = useState(null)

  const setC = (k) => (e) => setCommon({ ...common, [k]: e.target.value })
  const setD = (k) => (e) => setDetail({ ...detail, [k]: e.target.value })
  const notify = (ok, text) => setMessage({ ok, text })

  const payload = () => {
    const body = {}
    Object.entries(common).forEach(([k, v]) => { body[k] = coerce(k, v) })
    body[type] = Object.fromEntries(Object.entries(detail).map(([k, v]) => [k, coerce(k, v)]))
    return body
  }

  const fillForm = (p) => {
    setCommon({
      customerNumber: p.common.customerNumber ?? '', issueDate: p.common.issueDate ?? '',
      expiryDate: p.common.expiryDate ?? '', brokerId: p.common.brokerId ?? '',
      brokersReference: (p.common.brokersReference ?? '').trim(), payment: p.common.payment ?? '',
    })
    setDetail(p[type] ? Object.fromEntries(Object.entries(p[type]).map(([k, v]) => [k, v ?? ''])) : {})
    setPolicyNum(String(p.common.policyNumber))
  }

  const run = (promise, okText) => {
    promise
      .then((p) => { setResult(p); setResults([]); fillForm(p); notify(true, okText(p)) })
      .catch((e) => notify(false, e.message))
  }

  const add = () => run(api.addPolicy(type, payload()), (p) => `New ${type} policy inserted — #${p.common.policyNumber}`)
  const inquire = () => run(api.getPolicy(type, policyNum, common.customerNumber || null), () => 'Policy loaded')
  const update = () => run(api.updatePolicy(type, policyNum, payload()), () => `${type} policy updated`)
  const remove = () => {
    api.deletePolicy(type, policyNum)
      .then(() => { setResult(null); notify(true, `${type} policy deleted`) })
      .catch((e) => notify(false, e.message))
  }

  const commercialSearch = () => {
    api.commercialSearch({
      customerNumber: common.customerNumber || null,
      policyNumber: policyNum || null,
      postcode: detail.zipcode || null,
    })
      .then((list) => {
        setResults(list)
        setResult(list[0] || null)
        if (list[0]) fillForm(list[0])
        notify(true, `${list.length} commercial polic${list.length === 1 ? 'y' : 'ies'} found`)
      })
      .catch((e) => notify(false, e.message))
  }

  return (
    <>
      <h1>My policies</h1>
      <p className="subtitle">
        Add, inquire, update and delete policies — parity with the SSP1–SSP4 transactions.
      </p>

      <div className="section card">
        <div className="tabs">
          {Object.keys(TYPE_FIELDS).map((t) => (
            <button key={t} className={`tab ${type === t ? 'active' : ''}`}
                    onClick={() => { setType(t); setDetail({}); setResult(null); setResults([]); setMessage(null) }}>
              {t[0].toUpperCase() + t.slice(1)}
            </button>
          ))}
        </div>

        <h2 style={{ fontSize: 15, marginTop: 0 }}>Common details</h2>
        <div className="form-grid">
          {COMMON_FIELDS.map(([key, label]) => (
            <div className="field" key={key}>
              <label htmlFor={`c-${key}`}>{label}</label>
              <input id={`c-${key}`} value={common[key] ?? ''} onChange={setC(key)} />
            </div>
          ))}
          <div className="field">
            <label htmlFor="policy-num">Policy number (for inquire/update/delete)</label>
            <input id="policy-num" value={policyNum} inputMode="numeric" onChange={(e) => setPolicyNum(e.target.value)} />
          </div>
        </div>

        <h2 style={{ fontSize: 15, marginTop: 20 }}>{type[0].toUpperCase() + type.slice(1)} details</h2>
        <div className="form-grid">
          {TYPE_FIELDS[type].map(([key, label]) => (
            <div className="field" key={key}>
              <label htmlFor={`d-${key}`}>{label}</label>
              <input id={`d-${key}`} value={detail[key] ?? ''} onChange={setD(key)} />
            </div>
          ))}
        </div>

        <div style={{ marginTop: 18, display: 'flex', gap: 10, flexWrap: 'wrap' }}>
          <button className="btn" onClick={add}>Add policy</button>
          <button className="btn secondary" onClick={type === 'commercial' ? commercialSearch : inquire}>
            {type === 'commercial' ? 'Search' : 'Inquire'}
          </button>
          {type !== 'commercial' && <button className="btn secondary" onClick={update}>Update</button>}
          <button className="btn danger" onClick={remove}>Delete</button>
        </div>
        {type === 'commercial' && (
          <p className="policy-sub" style={{ marginTop: 10 }}>
            Commercial search mirrors the legacy variants: customer+policy, policy only, customer only, or postcode.
            Update is not supported for commercial policies in the original application.
          </p>
        )}
        {message && <div className={`message ${message.ok ? 'ok' : 'err'}`}>{message.text}</div>}
      </div>

      {result && (
        <div className="section card">
          <div className="section-head"><h2 style={{ fontSize: 16 }}>Policy #{result.common.policyNumber}</h2></div>
          <DetailTable policy={result} />
        </div>
      )}

      {results.length > 1 && (
        <div className="section card">
          <div className="section-head"><h2 style={{ fontSize: 16 }}>Matches <span className="count-badge">{results.length}</span></h2></div>
          <table className="data">
            <thead><tr><th>Policy</th><th>Customer</th><th>Zip</th><th>Property type</th><th>Status</th></tr></thead>
            <tbody>
              {results.map((p) => (
                <tr key={p.common.policyNumber} style={{ cursor: 'pointer' }} onClick={() => { setResult(p); fillForm(p) }}>
                  <td>{p.common.policyNumber}</td>
                  <td>{p.common.customerNumber}</td>
                  <td>{p.commercial?.zipcode}</td>
                  <td>{p.commercial?.propertyType}</td>
                  <td>{p.commercial?.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  )
}
