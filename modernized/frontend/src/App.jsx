import { useState } from 'react'
import Overview from './pages/Overview.jsx'
import Customers from './pages/Customers.jsx'
import Policies from './pages/Policies.jsx'
import {
  ShieldIcon, GridIcon, DocIcon, WalletIcon, HelpIcon, SettingsIcon, UserIcon,
} from './Icons.jsx'

const NAV = [
  { id: 'overview', label: 'Overview', icon: GridIcon },
  { id: 'customers', label: 'Customers', icon: UserIcon },
  { id: 'policies', label: 'My policies', icon: ShieldIcon },
]

export default function App() {
  const [page, setPage] = useState('overview')
  const [customerNumber, setCustomerNumber] = useState(null)

  const selectCustomer = (num) => {
    setCustomerNumber(num)
    setPage('overview')
  }

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark"><ShieldIcon size={25} /></span>
          <span className="brand-name">haven</span>
        </div>
        <nav className="nav">
          {NAV.map(({ id, label, icon: Icon }) => (
            <button
              key={id}
              className={`nav-item ${page === id ? 'active' : ''}`}
              onClick={() => setPage(id)}
            >
              <Icon size={20} />
              {label}
            </button>
          ))}
        </nav>
        <div className="sidebar-spacer" />
        <div className="support-card">
          <HelpIcon size={24} />
          <h4>A little help?</h4>
          <p>We&rsquo;re here when you need us.</p>
          <a href="#" onClick={(e) => e.preventDefault()}>Contact support &rarr;</a>
        </div>
        <div className="sidebar-foot">
          <span><SettingsIcon size={20} /> Settings</span>
          <span><HelpIcon size={20} /> Help center</span>
        </div>
      </aside>

      <main className="main">
        <div className="topbar">
          <span>Your insurance, simplified.</span>
          <div className="account">
            <span className="account-icon"><UserIcon size={20} /></span>
            <span>{customerNumber ? `Customer #${customerNumber}` : 'GenApp admin'}</span>
          </div>
        </div>

        {page === 'overview' && <Overview customerNumber={customerNumber} onPickCustomer={selectCustomer} />}
        {page === 'customers' && <Customers onPickCustomer={selectCustomer} />}
        {page === 'policies' && <Policies customerNumber={customerNumber} />}
      </main>
    </div>
  )
}
