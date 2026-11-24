import { useState } from 'react'
import { api } from '../api.js'

const EMPTY = {
  firstName: '', lastName: '', dateOfBirth: '', houseName: '', houseNumber: '',
  postcode: '', phoneMobile: '', phoneHome: '', emailAddress: '',
}

const FIELDS = [
  ['firstName', 'First name'], ['lastName', 'Last name'], ['dateOfBirth', 'Date of birth (YYYY-MM-DD)'],
  ['houseName', 'House name'], ['houseNumber', 'House number'], ['postcode', 'Postcode'],
  ['phoneMobile', 'Mobile phone'], ['phoneHome', 'Home phone'], ['emailAddress', 'Email'],
]

export default function Customers({ onPickCustomer }) {
  const [form, setForm] = useState(EMPTY)
  const [searchNum, setSearchNum] = useState('')
  const [mode, setMode] = useState('add') // add | edit
  const [message, setMessage] = useState(null)

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const notify = (ok, text) => setMessage({ ok, text })

  const search = () => {
    if (!searchNum) return
    api.getCustomer(searchNum)
      .then((c) => {
        setMode('edit')
        setForm({
          firstName: c.firstName || '', lastName: c.lastName || '',
          dateOfBirth: c.dateOfBirth || '', houseName: c.houseName || '',
          houseNumber: c.houseNumber || '', postcode: c.postcode || '',
          phoneMobile: c.phoneMobile || '', phoneHome: c.phoneHome || '',
          emailAddress: c.emailAddress || '',
        })
        notify(true, `Customer #${c.customerNumber} loaded — editing below.`)
      })
      .catch((e) => notify(false, e.message))
  }

  const submit = () => {
    const payload = { ...form, dateOfBirth: form.dateOfBirth || null }
    const action = mode === 'add'
      ? api.addCustomer(payload)
      : api.updateCustomer(Number(searchNum), payload)
    action
      .then((c) => {
        notify(true, mode === 'add' ? `New customer inserted — #${c.customerNumber}` : 'Customer details updated')
        if (mode === 'add') { setForm(EMPTY); setTimeout(() => onPickCustomer(c.customerNumber), 1500) }
      })
      .catch((e) => notify(false, e.message))
  }

  return (
    <>
      <h1>Customers</h1>
      <p className="subtitle">Add, inquire and update GenApp customers — parity with the SSC1 transaction.</p>

      <div className="section card">
        <div className="section-head">
          <h2 style={{ fontSize: 16 }}>Find a customer</h2>
        </div>
        <div style={{ display: 'flex', gap: 12, alignItems: 'flex-end' }}>
          <div className="field" style={{ width: 220 }}>
            <label htmlFor="cust-search">Customer number</label>
            <input id="cust-search" value={searchNum} inputMode="numeric"
                   onChange={(e) => setSearchNum(e.target.value)} placeholder="e.g. 1000001" />
          </div>
          <button className="btn" onClick={search}>Inquire</button>
          <button className="btn secondary" onClick={() => { setMode('add'); setForm(EMPTY); setMessage(null) }}>
            New customer
          </button>
        </div>
      </div>

      <div className="section card">
        <div className="section-head">
          <h2 style={{ fontSize: 16 }}>{mode === 'add' ? 'Add customer' : `Update customer #${searchNum}`}</h2>
        </div>
        <div className="form-grid">
          {FIELDS.map(([key, label]) => (
            <div className="field" key={key}>
              <label htmlFor={`f-${key}`}>{label}</label>
              <input id={`f-${key}`} value={form[key]} onChange={set(key)} />
            </div>
          ))}
        </div>
        <div style={{ marginTop: 16, display: 'flex', gap: 10 }}>
          <button className="btn" onClick={submit}>{mode === 'add' ? 'Add customer' : 'Update customer'}</button>
        </div>
        {message && <div className={`message ${message.ok ? 'ok' : 'err'}`}>{message.text}</div>}
      </div>
    </>
  )
}
