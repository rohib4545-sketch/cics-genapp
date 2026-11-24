import { useEffect, useState } from 'react'
import { api } from '../api.js'
import { ShieldIcon, CarIcon, HomeIcon, HeartIcon, BriefcaseIcon, DocIcon, WalletIcon, CheckIcon, ArrowIcon } from '../Icons.jsx'

const TYPE_META = {
  M: { label: 'Auto insurance', icon: CarIcon },
  H: { label: 'Home insurance', icon: HomeIcon },
  E: { label: 'Life & endowment', icon: HeartIcon },
  C: { label: 'Commercial', icon: BriefcaseIcon },
}

function policySubtitle(p) {
  if (p.motor) return `${p.motor.make || ''} ${p.motor.model || ''}`.trim() || 'Motor policy'
  if (p.house) return p.house.houseName || p.house.postcode || 'House policy'
  if (p.endowment) return p.endowment.fundName || 'Endowment policy'
  if (p.commercial) return p.commercial.propertyType || 'Commercial policy'
  return 'Policy'
}

export default function Overview({ customerNumber, onPickCustomer }) {
  const [policies, setPolicies] = useState([])
  const [error, setError] = useState(null)
  const [lookup, setLookup] = useState('')

  useEffect(() => {
    if (!customerNumber) { setPolicies([]); return }
    api.customerPolicies(customerNumber)
      .then(setPolicies)
      .catch((e) => { setPolicies([]); setError(e.message) })
  }, [customerNumber])

  const doLookup = () => {
    if (!lookup) return
    api.getCustomer(lookup)
      .then((c) => { setError(null); onPickCustomer(c.customerNumber) })
      .catch((e) => setError(e.message))
  }

  const totalPremium = policies.reduce((s, p) => s + (p.common.payment || 0), 0)

  return (
    <>
      <h1>Life happens. You&rsquo;re covered.</h1>
      <p className="subtitle">
        {customerNumber
          ? `Viewing customer #${customerNumber}. Here's your peace of mind at a glance.`
          : 'Enter a customer number to load their insurance overview.'}
      </p>

      <div className="section" style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
        <div className="field" style={{ width: 220 }}>
          <label htmlFor="cust-lookup">Customer number</label>
          <input id="cust-lookup" value={lookup} onChange={(e) => setLookup(e.target.value)}
                 placeholder="e.g. 1000001" inputMode="numeric" />
        </div>
        <button className="btn" style={{ marginTop: 18 }} onClick={doLookup}>Load</button>
        {error && <span className="message err">{error}</span>}
      </div>

      <div className="section banner">
        <div className="banner-msg">
          <span className="banner-icon"><ShieldIcon size={28} /></span>
          <div>
            <h3>Good things are protected.</h3>
            <p>{policies.length ? 'All listed policies are stored in the modernized GenApp backend.' : 'No policies loaded yet.'}</p>
          </div>
        </div>
        <div style={{ display: 'flex', gap: 32 }}>
          <div className="stat">
            <div className="stat-num">{String(policies.length).padStart(2, '0')}</div>
            <div className="stat-label">Active policies</div>
          </div>
          <div className="stat">
            <div className="stat-num">${totalPremium.toFixed(2)}</div>
            <div className="stat-label">Total payment</div>
          </div>
        </div>
      </div>

      <div className="section">
        <div className="section-head">
          <h2>Your policies <span className="count-badge">{policies.length}</span></h2>
        </div>
        <div className="policy-grid">
          {policies.map((p) => {
            const meta = TYPE_META[p.common.policyType] || TYPE_META.M
            const Icon = meta.icon
            return (
              <div className="policy-card" key={p.common.policyNumber}>
                <div className="policy-card-head">
                  <span className="policy-icon"><Icon size={23} /></span>
                  <span className="pill">Active</span>
                </div>
                <div className="policy-title">{meta.label}</div>
                <div className="policy-sub">{policySubtitle(p)}</div>
                <div className="policy-price">
                  ${(p.common.payment || 0).toFixed(0)} <small>/ policy #{(p.common.policyNumber)}</small>
                </div>
                <div className="policy-detail-line">
                  Issued {p.common.issueDate || '—'} · Expires {p.common.expiryDate || '—'}
                </div>
              </div>
            )
          })}
          {!policies.length && (
            <div className="card" style={{ color: 'var(--muted)' }}>
              No policies found for this customer yet — use the Customers and My policies pages to create some.
            </div>
          )}
        </div>
      </div>

      <div className="section" style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 16 }}>
        <div className="card">
          <div className="section-head" style={{ marginBottom: 8 }}>
            <h2 style={{ fontSize: 16 }}>Recent claims</h2>
            <button className="link-arrow">File a claim <ArrowIcon size={14} /></button>
          </div>
          <div className="claim-row">
            <span className="claim-doc"><DocIcon size={22} /></span>
            <div>
              <div style={{ fontWeight: 600, fontSize: 14 }}>No claims filed</div>
              <div className="policy-sub">Claims are tracked in the CLAIM table; filing UI is a planned extension.</div>
            </div>
          </div>
          <div className="progress"><div className="done" /><div /><div /></div>
          <div className="milestones"><span>Submitted</span><span>In review</span><span>Resolved</span></div>
        </div>
        <div className="card">
          <div className="section-head" style={{ marginBottom: 8 }}>
            <h2 style={{ fontSize: 16 }}>Upcoming payment</h2>
            <WalletIcon size={20} />
          </div>
          <div className="stat-num" style={{ fontSize: 32 }}>${totalPremium.toFixed(2)}</div>
          <div className="policy-sub" style={{ marginTop: 6 }}>Across {policies.length} policies</div>
          <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginTop: 18, color: 'var(--muted)', fontSize: 12 }}>
            <CheckIcon size={15} /> Autopay not configured
          </div>
          <button className="btn secondary" style={{ marginTop: 18, width: '100%', justifyContent: 'center' }}>
            Manage payments
          </button>
        </div>
      </div>
    </>
  )
}
