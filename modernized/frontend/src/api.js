const BASE = import.meta.env.VITE_API_BASE || ''

async function req(path, options = {}) {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  if (res.status === 204) return null
  const body = await res.json().catch(() => ({}))
  if (!res.ok) {
    const err = new Error(body.message || `Request failed (${res.status})`)
    err.returnCode = body.returnCode
    err.status = res.status
    throw err
  }
  return body
}

export const api = {
  health: () => req('/health'),

  addCustomer: (c) => req('/api/customers', { method: 'POST', body: JSON.stringify(c) }),
  getCustomer: (num) => req(`/api/customers/${num}`),
  updateCustomer: (num, c) => req(`/api/customers/${num}`, { method: 'PUT', body: JSON.stringify(c) }),
  customerPolicies: (num) => req(`/api/customers/${num}/policies`),

  addPolicy: (type, p) => req(`/api/policies/${type}`, { method: 'POST', body: JSON.stringify(p) }),
  getPolicy: (type, policyNum, customerNum) =>
    req(`/api/policies/${type}/${policyNum}${customerNum ? `?customerNumber=${customerNum}` : ''}`),
  updatePolicy: (type, policyNum, p) =>
    req(`/api/policies/${type}/${policyNum}`, { method: 'PUT', body: JSON.stringify(p) }),
  deletePolicy: (type, policyNum) => req(`/api/policies/${type}/${policyNum}`, { method: 'DELETE' }),
  commercialSearch: (params) => {
    const q = new URLSearchParams()
    if (params.customerNumber) q.set('customerNumber', params.customerNumber)
    if (params.policyNumber) q.set('policyNumber', params.policyNumber)
    if (params.postcode) q.set('postcode', params.postcode)
    return req(`/api/policies/commercial/search?${q}`)
  },
}
