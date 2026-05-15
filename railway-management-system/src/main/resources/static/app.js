/* ═══════════════════════════════════════════════════════════════
   CONSTANTS & STATE
═══════════════════════════════════════════════════════════════ */
const BASE = 'http://localhost:8080/api/v1';
let jwtToken = null;
let currentUser = { username: 'User', role: 'STAFF', initials: 'U' };

/* ═══════════════════════════════════════════════════════════════
   CORE API HELPER
═══════════════════════════════════════════════════════════════ */
async function apiCall(path, opts = {}) {
  const headers = opts.body ? { 'Content-Type': 'application/json' } : {};
  if (jwtToken) headers['Authorization'] = `Bearer ${jwtToken}`;
  const res = await fetch(BASE + path, { headers, ...opts });
  let data = {};
  try { data = await res.json(); } catch (_) {}
  return { ok: res.ok, status: res.status, data };
}
// backward-compat wrapper for passenger code
function apiFetch(url, opts = {}) { return apiCall('/passengers' + url, opts); }

/* ═══════════════════════════════════════════════════════════════
   SHARED UTILITIES
═══════════════════════════════════════════════════════════════ */
function escHtml(s) {
  return String(s ?? '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function fmtDateTime(dt) {
  if (!dt) return '—';
  try { return new Date(dt).toLocaleString('en-US', { month:'short', day:'numeric', year:'numeric', hour:'2-digit', minute:'2-digit' }); }
  catch (_) { return String(dt); }
}

function fmtDate(dt) {
  if (!dt) return '—';
  try { return new Date(dt).toLocaleDateString('en-US', { month:'short', day:'numeric', year:'numeric' }); }
  catch (_) { return String(dt); }
}

function toApiDateTime(val) { return val ? val.length === 16 ? val + ':00' : val : null; }
function toInputDateTime(val) { return val ? String(val).substring(0, 16) : ''; }

function setLoading(btn, loading, label) {
  if (!btn) return;
  if (loading) {
    btn.disabled = true;
    btn.innerHTML = `<span class="spinner" style="width:1rem;height:1rem;border-width:2px"></span> ${label}`;
  } else {
    btn.disabled = false;
    btn.textContent = label;
  }
}

// ── Toast ──────────────────────────────────────────────────────
let toastTimer;
function showToast(type, title, msg) {
  const toast = document.getElementById('toast');
  const inner = document.getElementById('toast-inner');
  document.getElementById('toast-title').textContent = title;
  document.getElementById('toast-msg').textContent = msg || '';
  const cls = type === 'success' ? 'green' : type === 'error' ? 'red' : type === 'warning' ? 'amber' : 'blue';
  inner.className = 'alert-box alert-' + cls;
  toast.classList.remove('hidden');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.add('hidden'), 4500);
}

// ── Modal helpers ──────────────────────────────────────────────
function openModal(id)  { document.getElementById(id).classList.add('open'); }
function closeModal(id) { document.getElementById(id).classList.remove('open'); }
document.addEventListener('click', e => {
  if (e.target.classList.contains('modal-overlay')) closeModal(e.target.id);
});

// ── Generic confirm modal ──────────────────────────────────────
let _confirmCb = null;
function openConfirmModal({ title = 'Confirm', heading = 'Are you sure?', msg = '', btnText = 'Confirm', btnClass = 'btn-destructive', iconClass = '', cb }) {
  document.getElementById('confirm-title').textContent = title;
  document.getElementById('confirm-heading').textContent = heading;
  document.getElementById('confirm-msg').textContent = msg;
  const icon = document.getElementById('confirm-icon');
  icon.className = 'confirm-icon' + (iconClass ? ' ' + iconClass : '');
  const btn = document.getElementById('confirm-execute-btn');
  btn.className = 'btn ' + btnClass;
  btn.textContent = btnText;
  _confirmCb = cb;
  openModal('confirm-modal');
}
function executeConfirm() { if (_confirmCb) { _confirmCb(); _confirmCb = null; } }

// ── Tabs (scoped to nearest .card) ────────────────────────────
function switchTab(panelId, btn) {
  const card = btn.closest('.card') || btn.closest('[data-tabs]') || document.body;
  card.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
  card.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
  btn.classList.add('active');
  document.getElementById(panelId).classList.add('active');
}

// ── Error row helper ───────────────────────────────────────────
function errorRow(cols, msg) {
  return `<tr><td colspan="${cols}"><div class="alert-box alert-red" style="margin:.5rem 0">
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
    <div><div class="alert-title">Error</div><div class="alert-message">${escHtml(msg)}</div></div>
  </div></td></tr>`;
}

function loadingRow(cols, label = 'Loading…') {
  return `<tr class="loading-row"><td colspan="${cols}"><span class="spinner"></span><span style="margin-left:.75rem">${label}</span></td></tr>`;
}

/* ═══════════════════════════════════════════════════════════════
   ROUTER
═══════════════════════════════════════════════════════════════ */
const PAGE_TITLES = {
  'dashboard':              'Dashboard',
  'schedule-trip':          'Schedule Trip',
  'manage-trains':          'Manage Trains',
  'manage-routes':          'Manage Routes',
  'train-schedules':        'Train Schedules',
  'passenger-registration': 'Passenger Registration',
  'ticket-reservation':     'Ticket Reservation',
  'booking-history':        'Booking History',
};

const ADMIN_PAGES = ['dashboard', 'schedule-trip', 'manage-trains', 'manage-routes'];

function applyRoleBasedUI(role) {
  const isAdmin = role === 'ADMINISTRATOR';
  document.getElementById('admin-nav-section').style.display  = isAdmin ? '' : 'none';
  document.getElementById('nav-train-schedules').style.display = isAdmin ? 'none' : '';
}

function navigateTo(pageId) {
  if (ADMIN_PAGES.includes(pageId) && currentUser.role !== 'ADMINISTRATOR') {
    pageId = 'train-schedules';
  }
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('#sidenav a').forEach(a => a.classList.remove('active'));
  const page = document.getElementById('page-' + pageId);
  if (page) page.classList.add('active');
  const link = document.querySelector(`#sidenav a[data-page="${pageId}"]`);
  if (link) link.classList.add('active');
  document.getElementById('header-title').textContent = PAGE_TITLES[pageId] || pageId;
  history.pushState({ pageId }, '', '/' + pageId);

  if (pageId === 'dashboard')              loadDashboard();
  if (pageId === 'schedule-trip')          loadSchedules();
  if (pageId === 'manage-trains')          loadTrains();
  if (pageId === 'manage-routes')          loadRoutes();
  if (pageId === 'train-schedules')        loadTrainSchedules();
  if (pageId === 'passenger-registration') loadPassengers();
  if (pageId === 'ticket-reservation')     initTicketReservation();
  if (pageId === 'booking-history')        loadBookings();
}

document.querySelectorAll('#sidenav a[data-page]').forEach(link => {
  link.addEventListener('click', e => { e.preventDefault(); navigateTo(link.dataset.page); });
});
window.addEventListener('popstate', e => { if (e.state?.pageId) navigateTo(e.state.pageId); });

document.getElementById('header-date').textContent =
  new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });

/* ═══════════════════════════════════════════════════════════════
   DASHBOARD
═══════════════════════════════════════════════════════════════ */
async function loadDashboard() {
  const ids = ['dash-trains','dash-routes','dash-schedules','dash-completed','dash-passengers','dash-bookings','dash-bookings-completed','dash-bookings-cancelled','dash-occupancy'];
  ids.forEach(id => { const el = document.getElementById(id); if (el) el.textContent = '…'; });
  const revEl = document.getElementById('dash-revenue'); if (revEl) revEl.textContent = 'SAR …';
  const barEl = document.getElementById('dash-occupancy-bar'); if (barEl) barEl.style.width = '0%';
  const revenueChartEl = document.getElementById('train-revenue-chart');
  if (revenueChartEl) revenueChartEl.innerHTML = '<div class="empty-state" style="padding:1.5rem"><span class="spinner"></span></div>';
  ['daily-booking-report','monthly-booking-report'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.innerHTML = '<div class="empty-state" style="padding:1.5rem"><span class="spinner"></span></div>';
  });
  document.getElementById('dash-recent-tbody').innerHTML = loadingRow(7, 'Loading recent bookings…');

  const [trains, routes, schedules, passengers, bookings, stats, utilization, trainRevenue, dailyReport, monthlyReport] = await Promise.allSettled([
    apiCall('/trains/all'), apiCall('/routes/all'), apiCall('/schedules/all'),
    apiCall('/passengers/all'), apiCall('/bookings/all'), apiCall('/dashboard/stats'),
    apiCall('/dashboard/train-seat-fill-rate'), apiCall('/dashboard/revenue-by-train'),
    apiCall('/dashboard/daily-booking-report'), apiCall('/dashboard/monthly-booking-report')
  ]);

  const tv = v => v.status === 'fulfilled' && v.value.ok ? (v.value.data.data || []) : [];
  const tList = tv(trains), rList = tv(routes), sList = tv(schedules), pList = tv(passengers), bList = tv(bookings);

  const revenue = bList.filter(b => b.status === 'COMPLETED').reduce((sum, b) => sum + (b.finalPrice || 0), 0);

  document.getElementById('dash-trains').textContent      = tList.length;
  document.getElementById('dash-routes').textContent      = rList.length;
  document.getElementById('dash-schedules').textContent   = sList.filter(s => s.status === 'ON_TIME').length + '/' + sList.length;
  document.getElementById('dash-completed').textContent   = sList.filter(s => s.completed).length;
  document.getElementById('dash-passengers').textContent  = pList.length;
  document.getElementById('dash-bookings').textContent            = bList.filter(b => b.status === 'CONFIRMED').length;
  document.getElementById('dash-bookings-completed').textContent  = bList.filter(b => b.status === 'COMPLETED').length;
  document.getElementById('dash-bookings-cancelled').textContent  = bList.filter(b => b.status === 'CANCELLED').length;
  document.getElementById('dash-revenue').textContent     = 'SAR ' + revenue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

  const statsData = stats.status === 'fulfilled' && stats.value.ok ? (stats.value.data.data || null) : null;
  const occupancy = statsData ? (statsData.seatOccupancyRate || 0) : 0;
  const occEl = document.getElementById('dash-occupancy');
  if (occEl) occEl.textContent = occupancy.toFixed(1) + '%';
  const bar = document.getElementById('dash-occupancy-bar');
  if (bar) {
    bar.style.width = Math.min(occupancy, 100).toFixed(1) + '%';
    bar.style.background = occupancy >= 85 ? '#dc2626' : occupancy >= 60 ? '#d97706' : '#7c3aed';
  }

  const fillRateData = utilization.status === 'fulfilled' && utilization.value.ok
    ? (utilization.value.data.data || []) : [];
  renderTrainSeatFillRateChart(fillRateData);

  const revenueData = trainRevenue.status === 'fulfilled' && trainRevenue.value.ok
    ? (trainRevenue.value.data.data || []) : [];
  renderTrainRevenueChart(revenueData);

  const dailyData = dailyReport.status === 'fulfilled' && dailyReport.value.ok
    ? (dailyReport.value.data.data || null) : null;
  renderBookingReport(dailyData, 'daily-booking-report');

  const monthlyData = monthlyReport.status === 'fulfilled' && monthlyReport.value.ok
    ? (monthlyReport.value.data.data || null) : null;
  renderBookingReport(monthlyData, 'monthly-booking-report');

  const recent = bList.slice(0, 8);
  const tbody = document.getElementById('dash-recent-tbody');
  if (!recent.length) {
    tbody.innerHTML = `<tr><td colspan="7"><div class="empty-state" style="padding:1.5rem"><p>No bookings yet.</p></div></td></tr>`;
    return;
  }
  tbody.innerHTML = recent.map(b => `
    <tr>
      <td class="font-medium">${escHtml(b.bookingReference||'—')}</td>
      <td>${escHtml(b.passengerName||'—')}<div class="td-subtext">ID: ${escHtml(String(b.passengerIdentificationNumber||''))}</div></td>
      <td>${escHtml(b.scheduleNumber||'—')}</td>
      <td>${escHtml(b.trainNumber||'—')}</td>
      <td>${classBadge(b.bookedClass)}</td>
      <td>${bookingStatusBadge(b.status)}</td>
      <td>${escHtml(fmtDate(b.bookingDate))}</td>
    </tr>`).join('');
}

function renderTrainSeatFillRateChart(data) {
  const container = document.getElementById('train-seat-fill-rate-chart');
  if (!container) return;
  if (!data || !data.length) {
    container.innerHTML = '<div class="empty-state" style="padding:1.5rem"><p>No completed schedule data available yet.</p></div>';
    return;
  }
  const legend = `
    <div style="display:flex;gap:1rem;margin-bottom:1rem;font-size:.75rem;color:#6b7280">
      <span style="display:flex;align-items:center;gap:.35rem"><span style="display:inline-block;width:.75rem;height:.75rem;border-radius:2px;background:#7c3aed"></span>Booked</span>
      <span style="display:flex;align-items:center;gap:.35rem"><span style="display:inline-block;width:.75rem;height:.75rem;border-radius:2px;background:#e5e7eb"></span>Available</span>
    </div>`;
  const rows = data.map(t => {
    const pct     = Math.max(0, Math.min(100, t.fillRate));
    const avail   = 100 - pct;
    const barColor = pct >= 85 ? '#dc2626' : pct >= 60 ? '#d97706' : '#7c3aed';
    const label    = pct >= 85 ? '#dc2626' : pct >= 60 ? '#d97706' : '#6d28d9';
    return `
      <div style="display:grid;grid-template-columns:9rem 1fr 3.5rem;align-items:center;gap:.875rem;padding:.6rem 0;border-bottom:1px solid #f3f4f6">
        <div style="min-width:0">
          <div style="font-size:.8125rem;font-weight:500;white-space:nowrap;overflow:hidden;text-overflow:ellipsis" title="${escHtml(t.trainName)}">${escHtml(t.trainName)}</div>
          <div style="font-size:.6875rem;color:#9ca3af;margin-top:.1rem">${escHtml(t.trainNumber)} &bull; ${t.bookedSeats}/${t.totalSeats} seats</div>
        </div>
        <div title="${t.bookedSeats} booked, ${t.totalSeats - t.bookedSeats} available" style="cursor:default">
          <div style="display:flex;height:1.125rem;border-radius:.25rem;overflow:hidden">
            ${pct > 0   ? `<div style="width:${pct.toFixed(2)}%;background:${barColor};transition:width .7s ease"></div>` : ''}
            ${avail > 0 ? `<div style="width:${avail.toFixed(2)}%;background:#e5e7eb"></div>` : ''}
          </div>
        </div>
        <div style="font-size:.8125rem;font-weight:700;text-align:right;color:${label}">${pct.toFixed(1)}%</div>
      </div>`;
  });
  // Remove bottom border from last row
  const lastFixed = rows[rows.length - 1].replace('border-bottom:1px solid #f3f4f6', 'border-bottom:none');
  rows[rows.length - 1] = lastFixed;
  container.innerHTML = legend + rows.join('');
}

function renderTrainRevenueChart(data) {
  const container = document.getElementById('train-revenue-chart');
  if (!container) return;
  if (!data || !data.length) {
    container.innerHTML = '<div class="empty-state" style="padding:1.5rem"><p>No completed booking data available yet.</p></div>';
    return;
  }

  const maxRevenue = Math.max(...data.map(t => t.revenue));
  const fmtSar = n => 'SAR ' + n.toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 0 });

  const rows = data.map((t, idx) => {
    const pct    = maxRevenue === 0 ? 0 : (t.revenue / maxRevenue) * 100;
    const isLast = idx === data.length - 1;
    return `
      <div style="display:grid;grid-template-columns:9rem 1fr 6.5rem;align-items:center;gap:.875rem;padding:.6rem 0;${isLast ? '' : 'border-bottom:1px solid #f3f4f6'}">
        <div style="min-width:0">
          <div style="font-size:.8125rem;font-weight:500;white-space:nowrap;overflow:hidden;text-overflow:ellipsis" title="${escHtml(t.trainName)}">${escHtml(t.trainName)}</div>
          <div style="font-size:.6875rem;color:#9ca3af;margin-top:.1rem">${escHtml(t.trainNumber)} &bull; ${t.completedBookings} bookings &bull; ${t.completedTrips} trips</div>
        </div>
        <div title="${fmtSar(t.revenue)}" style="cursor:default">
          <div style="display:flex;height:1.125rem;border-radius:.25rem;overflow:hidden">
            ${pct > 0   ? `<div style="width:${pct.toFixed(2)}%;background:#16a34a;transition:width .7s ease"></div>` : ''}
            ${pct < 100 ? `<div style="width:${(100 - pct).toFixed(2)}%;background:#e5e7eb"></div>` : ''}
          </div>
        </div>
        <div style="font-size:.8125rem;font-weight:700;text-align:right;color:#15803d;white-space:nowrap">${fmtSar(t.revenue)}</div>
      </div>`;
  });

  container.innerHTML = rows.join('');
}

function renderBookingReport(data, containerId) {
  const container = document.getElementById(containerId);
  if (!container) return;
  if (!data) {
    container.innerHTML = '<div class="empty-state" style="padding:1.5rem"><p>Could not load report.</p></div>';
    return;
  }
  const fmtSar = n => 'SAR ' + n.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  const rows = [
    { label: 'Completed Bookings', value: data.completedBookings, color: '#15803d', bg: '#dcfce7' },
    { label: 'Confirmed Bookings', value: data.confirmedBookings, color: '#1d4ed8', bg: '#dbeafe' },
    { label: 'Cancelled Bookings', value: data.cancelledBookings, color: '#b91c1c', bg: '#fee2e2' },
    { label: 'Total Bookings',     value: data.totalBookings,     color: '#374151', bg: '#f3f4f6' },
  ];
  const statRows = rows.map((r, i) => `
    <div style="display:flex;justify-content:space-between;align-items:center;padding:.55rem 0;${i < rows.length - 1 ? 'border-bottom:1px solid #f3f4f6' : ''}">
      <span style="font-size:.8125rem;color:#6b7280">${r.label}</span>
      <span style="font-size:.9rem;font-weight:700;color:${r.color};background:${r.bg};padding:.15rem .55rem;border-radius:9999px">${r.value}</span>
    </div>`).join('');
  const revenueRow = `
    <div style="display:flex;justify-content:space-between;align-items:center;padding:.6rem 0 0 0;margin-top:.25rem;border-top:2px solid #e5e7eb">
      <span style="font-size:.8125rem;font-weight:600;color:#374151">Revenue</span>
      <span style="font-size:.9rem;font-weight:700;color:#15803d">${fmtSar(data.revenue)}</span>
    </div>`;
  container.innerHTML = statRows + revenueRow;
}

/* ═══════════════════════════════════════════════════════════════
   MANAGE TRAINS
═══════════════════════════════════════════════════════════════ */
let allTrains = [];

async function loadTrains() {
  document.getElementById('trains-tbody').innerHTML = loadingRow(5);
  try {
    const { ok, data } = await apiCall('/trains/all');
    if (ok) {
      allTrains = Array.isArray(data.data) ? data.data : [];
      document.getElementById('trn-stat-total').textContent     = allTrains.length;
      document.getElementById('trn-stat-intercity').textContent  = allTrains.filter(t => t.type === 'INTERCITY').length;
      document.getElementById('trn-stat-highspeed').textContent  = allTrains.filter(t => t.type === 'HIGH_SPEED_INTERCITY').length;
      document.getElementById('trn-stat-metro').textContent      = allTrains.filter(t => t.type === 'METRO').length;
      renderTrainsTable(allTrains);
    } else {
      document.getElementById('trains-tbody').innerHTML = errorRow(5, data.message || 'Failed to load trains.');
    }
  } catch (_) {
    document.getElementById('trains-tbody').innerHTML = errorRow(5, 'Cannot connect to server.');
  }
}

function trainTypeBadge(type) {
  if (type === 'INTERCITY')           return '<span class="badge badge-blue">Intercity</span>';
  if (type === 'HIGH_SPEED_INTERCITY') return '<span class="badge badge-purple">High Speed</span>';
  if (type === 'METRO')               return '<span class="badge badge-secondary">Metro</span>';
  return `<span class="badge badge-gray">${escHtml(type||'—')}</span>`;
}

function renderTrainsTable(list) {
  const tbody = document.getElementById('trains-tbody');
  if (!list.length) { tbody.innerHTML = ''; document.getElementById('trains-empty').classList.remove('hidden'); return; }
  document.getElementById('trains-empty').classList.add('hidden');
  const isAdmin = currentUser.role === 'ADMINISTRATOR';
  tbody.innerHTML = list.map(t => {
    const seats = (t.seatConfigurations||[]).map(c =>
      `<span class="badge badge-secondary" style="margin-right:.25rem">${escHtml(c.seatClass)} (${c.totalSeats})</span>`).join('');
    return `<tr>
      <td class="font-medium">${escHtml(t.trainNumber||'—')}</td>
      <td>${escHtml(t.name||'—')}</td>
      <td>${trainTypeBadge(t.type)}</td>
      <td>${seats || '<span class="text-gray-500 text-xs">—</span>'}</td>
      <td><div class="td-actions">
        ${isAdmin ? `
        <button class="btn btn-outline btn-sm btn-icon" title="Edit" onclick='openTrainEditModal(${JSON.stringify(t)})'>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:1rem;height:1rem"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
        </button>
        <button class="btn btn-destructive btn-sm btn-icon" title="Delete" onclick="deleteTrain('${escHtml(t.trainNumber||'')}','${escHtml(t.name||'')}')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:1rem;height:1rem"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/></svg>
        </button>` : '<span class="text-xs text-gray-500">View only</span>'}
      </div></td>
    </tr>`;
  }).join('');
}

// Seat config rows
function addSeatConfigRow(containerId, data = {}) {
  const c = document.getElementById(containerId);
  const row = document.createElement('div');
  row.className = 'seat-cfg-row';
  row.innerHTML = `
    <div class="form-group">
      <label>Seat Class</label>
      <select class="select seat-cls">
        <option value="">Select…</option>
        <option value="ECONOMY" ${data.seatClass==='ECONOMY'?'selected':''}>Economy</option>
        <option value="BUSINESS" ${data.seatClass==='BUSINESS'?'selected':''}>Business</option>
        <option value="SLEEPER" ${data.seatClass==='SLEEPER'?'selected':''}>Sleeper</option>
      </select>
    </div>
    <div class="form-group">
      <label>Total Seats</label>
      <input type="number" class="input seat-total" placeholder="e.g. 80" min="1" value="${escHtml(String(data.totalSeats||''))}">
    </div>
    <div class="form-group">
      <label>Base Price (SAR)</label>
      <input type="number" class="input seat-price" placeholder="e.g. 150" min="0" step="0.01" value="${escHtml(String(data.basePrice||''))}">
    </div>
    <button type="button" class="btn btn-destructive btn-icon btn-sm" style="align-self:flex-end" onclick="this.closest('.seat-cfg-row').remove()" title="Remove">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:1rem;height:1rem"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
    </button>`;
  c.appendChild(row);
}

function getSeatConfigs(containerId) {
  return Array.from(document.querySelectorAll(`#${containerId} .seat-cfg-row`)).map(row => ({
    seatClass:  row.querySelector('.seat-cls').value,
    totalSeats: parseInt(row.querySelector('.seat-total').value) || 0,
    basePrice:  parseFloat(row.querySelector('.seat-price').value) || 0,
  })).filter(c => c.seatClass && c.totalSeats > 0);
}

async function submitAddTrain() {
  const name = document.getElementById('trn-name').value.trim();
  const type = document.getElementById('trn-type').value;
  if (!name || !type) { showToast('error', 'Validation Error', 'Name and type are required.'); return; }
  const seatConfigurations = getSeatConfigs('trn-seat-configs');
  if (!seatConfigurations.length) { showToast('error', 'Validation Error', 'Add at least one seat class configuration.'); return; }
  const btn = document.getElementById('trn-add-btn');
  setLoading(btn, true, 'Adding…');
  try {
    const { ok, data } = await apiCall('/trains/add', { method: 'POST', body: JSON.stringify({ name, type, seatConfigurations }) });
    if (ok) {
      showToast('success', 'Train Added', `${name} was registered successfully.`);
      document.getElementById('trn-name').value = '';
      document.getElementById('trn-type').value = '';
      document.getElementById('trn-seat-configs').innerHTML = '';
      loadTrains();
    } else {
      showToast('error', 'Failed', data.message || 'Could not add train.');
    }
  } catch (_) { showToast('error', 'Connection Error', 'Could not reach the server.'); }
  setLoading(btn, false, 'Add Train');
}

function openTrainEditModal(t) {
  document.getElementById('edit-trn-number-display').textContent = t.trainNumber;
  document.getElementById('edit-trn-original').value = t.trainNumber;
  document.getElementById('edit-trn-name').value = t.name || '';
  document.getElementById('edit-trn-type').value = t.type || '';
  const c = document.getElementById('edit-trn-seat-configs');
  c.innerHTML = '';
  (t.seatConfigurations || []).forEach(sc => addSeatConfigRow('edit-trn-seat-configs', sc));
  openModal('train-edit-modal');
}

async function submitUpdateTrain() {
  const trainNumber = document.getElementById('edit-trn-original').value;
  const name = document.getElementById('edit-trn-name').value.trim();
  const type = document.getElementById('edit-trn-type').value;
  if (!name || !type) { showToast('error', 'Validation Error', 'Name and type are required.'); return; }
  const seatConfigurations = getSeatConfigs('edit-trn-seat-configs');
  const btn = document.getElementById('edit-trn-btn');
  setLoading(btn, true, 'Saving…');
  try {
    const { ok, data } = await apiCall(`/trains/update/trainNumber/${encodeURIComponent(trainNumber)}`, { method: 'PUT', body: JSON.stringify({ name, type, seatConfigurations }) });
    if (ok) {
      showToast('success', 'Train Updated', `${name} was updated.`);
      closeModal('train-edit-modal');
      loadTrains();
    } else { showToast('error', 'Failed', data.message || 'Could not update train.'); }
  } catch (_) { showToast('error', 'Connection Error', 'Could not reach the server.'); }
  setLoading(btn, false, 'Save Changes');
}

function deleteTrain(trainNumber, name) {
  openConfirmModal({ title: 'Delete Train', heading: 'Delete Train?',
    msg: `Permanently delete "${name}" (${trainNumber})? All associated schedules may be affected.`,
    btnText: 'Delete Train', cb: async () => {
      try {
        const { ok, data } = await apiCall(`/trains/delete/trainNumber/${encodeURIComponent(trainNumber)}`, { method: 'DELETE' });
        if (ok) { showToast('success', 'Deleted', `Train ${trainNumber} removed.`); closeModal('confirm-modal'); loadTrains(); }
        else showToast('error', 'Failed', data.message || 'Could not delete train.');
      } catch (_) { showToast('error', 'Connection Error', 'Could not reach the server.'); }
    }
  });
}

/* ═══════════════════════════════════════════════════════════════
   MANAGE ROUTES
═══════════════════════════════════════════════════════════════ */
let allRoutes = [];

async function loadRoutes() {
  document.getElementById('routes-tbody').innerHTML = loadingRow(6);
  try {
    const { ok, data } = await apiCall('/routes/all');
    if (ok) {
      allRoutes = Array.isArray(data.data) ? data.data : [];
      document.getElementById('rt-stat-total').textContent = allRoutes.length;
      const totalDist = allRoutes.reduce((s, r) => s + (r.distanceKm || 0), 0);
      document.getElementById('rt-stat-dist').textContent = totalDist.toFixed(0) + ' km';
      renderRoutesTable(allRoutes);
    } else {
      document.getElementById('routes-tbody').innerHTML = errorRow(6, data.message || 'Failed to load routes.');
    }
  } catch (_) { document.getElementById('routes-tbody').innerHTML = errorRow(6, 'Cannot connect to server.'); }
}

function renderRoutesTable(list) {
  const tbody = document.getElementById('routes-tbody');
  if (!list.length) { tbody.innerHTML = ''; document.getElementById('routes-empty').classList.remove('hidden'); return; }
  document.getElementById('routes-empty').classList.add('hidden');
  const isAdmin = currentUser.role === 'ADMINISTRATOR';
  tbody.innerHTML = list.map(r => `
    <tr>
      <td class="font-medium">${escHtml(r.routeNumber||'—')}</td>
      <td>${escHtml(r.routeName||'—')}</td>
      <td>${escHtml(r.startStation||'—')}</td>
      <td>${escHtml(r.endStation||'—')}</td>
      <td>${r.distanceKm != null ? escHtml(String(r.distanceKm)) + ' km' : '—'}</td>
      <td>${r.stops != null ? escHtml(String(r.stops)) : '—'}</td>
      <td><div class="td-actions">
        ${isAdmin ? `
        <button class="btn btn-outline btn-sm btn-icon" title="Edit" onclick='openRouteEditModal(${JSON.stringify(r)})'>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:1rem;height:1rem"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
        </button>
        <button class="btn btn-destructive btn-sm btn-icon" title="Delete" onclick="deleteRoute('${escHtml(r.routeNumber||'')}','${escHtml(r.routeName||'')}')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:1rem;height:1rem"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/></svg>
        </button>` : '<span class="text-xs text-gray-500">View only</span>'}
      </div></td>
    </tr>`).join('');
}

async function submitAddRoute() {
  const fields = { routeName: 'rt-routeName', startStation: 'rt-startStation', endStation: 'rt-endStation' };
  let valid = true;
  Object.entries(fields).forEach(([, id]) => { if (!document.getElementById(id).value.trim()) valid = false; });
  if (!valid) { showToast('error', 'Validation Error', 'Route name, start, and end station are required.'); return; }
  const body = {
    routeName:    document.getElementById('rt-routeName').value.trim(),
    startStation: document.getElementById('rt-startStation').value.trim(),
    endStation:   document.getElementById('rt-endStation').value.trim(),
    distanceKm:   parseFloat(document.getElementById('rt-distanceKm').value) || null,
    stops:        parseInt(document.getElementById('rt-stops').value) || null,
  };
  const btn = document.getElementById('rt-add-btn');
  setLoading(btn, true, 'Adding…');
  try {
    const { ok, data } = await apiCall('/routes/add', { method: 'POST', body: JSON.stringify(body) });
    if (ok) {
      showToast('success', 'Route Added', `${body.routeName} was added.`);
      ['rt-routeName','rt-startStation','rt-endStation','rt-distanceKm','rt-stops'].forEach(id => document.getElementById(id).value = '');
      loadRoutes();
    } else { showToast('error', 'Failed', data.message || 'Could not add route.'); }
  } catch (_) { showToast('error', 'Connection Error', 'Could not reach the server.'); }
  setLoading(btn, false, 'Add Route');
}

function openRouteEditModal(r) {
  document.getElementById('edit-rt-number-display').textContent = r.routeNumber;
  document.getElementById('edit-rt-original').value    = r.routeNumber;
  document.getElementById('edit-rt-routeName').value   = r.routeName    || '';
  document.getElementById('edit-rt-startStation').value = r.startStation || '';
  document.getElementById('edit-rt-endStation').value   = r.endStation   || '';
  document.getElementById('edit-rt-distanceKm').value   = r.distanceKm  != null ? r.distanceKm : '';
  document.getElementById('edit-rt-stops').value        = r.stops        != null ? r.stops : '';
  openModal('route-edit-modal');
}

async function submitUpdateRoute() {
  const routeNumber = document.getElementById('edit-rt-original').value;
  const body = {
    routeName:    document.getElementById('edit-rt-routeName').value.trim(),
    startStation: document.getElementById('edit-rt-startStation').value.trim(),
    endStation:   document.getElementById('edit-rt-endStation').value.trim(),
    distanceKm:   parseFloat(document.getElementById('edit-rt-distanceKm').value) || null,
    stops:        parseInt(document.getElementById('edit-rt-stops').value) || null,
  };
  if (!body.routeName || !body.startStation || !body.endStation) {
    showToast('error', 'Validation Error', 'Route name, start, and end station are required.'); return;
  }
  const btn = document.getElementById('edit-rt-btn');
  setLoading(btn, true, 'Saving…');
  try {
    const { ok, data } = await apiCall(`/routes/update/routeNumber/${encodeURIComponent(routeNumber)}`, { method: 'PUT', body: JSON.stringify(body) });
    if (ok) { showToast('success', 'Route Updated', `${body.routeName} was updated.`); closeModal('route-edit-modal'); loadRoutes(); }
    else { showToast('error', 'Failed', data.message || 'Could not update route.'); }
  } catch (_) { showToast('error', 'Connection Error', 'Could not reach the server.'); }
  setLoading(btn, false, 'Save Changes');
}

function deleteRoute(routeNumber, name) {
  openConfirmModal({ title: 'Delete Route', heading: 'Delete Route?',
    msg: `Permanently delete route "${name}" (${routeNumber})?`,
    btnText: 'Delete Route', cb: async () => {
      try {
        const { ok, data } = await apiCall(`/routes/delete/routeNumber/${encodeURIComponent(routeNumber)}`, { method: 'DELETE' });
        if (ok) { showToast('success', 'Deleted', `Route ${routeNumber} removed.`); closeModal('confirm-modal'); loadRoutes(); }
        else showToast('error', 'Failed', data.message || 'Could not delete route.');
      } catch (_) { showToast('error', 'Connection Error', 'Could not reach the server.'); }
    }
  });
}

/* ═══════════════════════════════════════════════════════════════
   SCHEDULE TRIP
═══════════════════════════════════════════════════════════════ */
let allSchedules = [];

function scheduleStatusBadge(status) {
  if (status === 'ON_TIME')   return '<span class="badge badge-green">On Time</span>';
  if (status === 'DELAYED')   return '<span class="badge badge-amber">Delayed</span>';
  if (status === 'CANCELLED') return '<span class="badge badge-red">Cancelled</span>';
  return `<span class="badge badge-gray">${escHtml(status||'—')}</span>`;
}

async function loadSchedules() {
  document.getElementById('schedules-tbody').innerHTML = loadingRow(7);
  try {
    const { ok, data } = await apiCall('/schedules/all');
    if (ok) {
      allSchedules = Array.isArray(data.data) ? data.data : [];
      document.getElementById('sch-stat-total').textContent     = allSchedules.length;
      document.getElementById('sch-stat-ontime').textContent    = allSchedules.filter(s => s.status === 'ON_TIME').length;
      document.getElementById('sch-stat-delayed').textContent   = allSchedules.filter(s => s.status === 'DELAYED').length;
      document.getElementById('sch-stat-cancelled').textContent = allSchedules.filter(s => s.status === 'CANCELLED').length;
      document.getElementById('sch-stat-completed').textContent = allSchedules.filter(s => s.completed).length;
      const filter = document.getElementById('sch-completion-filter');
      if (filter) filter.value = '';
      renderSchedulesTable(allSchedules);
    } else {
      document.getElementById('schedules-tbody').innerHTML = errorRow(7, data.message || 'Failed to load schedules.');
    }
  } catch (_) { document.getElementById('schedules-tbody').innerHTML = errorRow(7, 'Cannot connect to server.'); }
}

function filterSchedulesByCompletion() {
  const filter = document.getElementById('sch-completion-filter').value;
  let filtered = allSchedules;
  if (filter === 'completed')     filtered = allSchedules.filter(s => s.completed);
  else if (filter === 'not-completed') filtered = allSchedules.filter(s => !s.completed);
  renderSchedulesTable(filtered);
}

function renderSchedulesTable(list) {
  const tbody = document.getElementById('schedules-tbody');
  if (!list.length) { tbody.innerHTML = ''; document.getElementById('schedules-empty').classList.remove('hidden'); return; }
  document.getElementById('schedules-empty').classList.add('hidden');
  const isAdmin = currentUser.role === 'ADMINISTRATOR';
  const canMarkComplete = currentUser.role === 'ADMINISTRATOR' || currentUser.role === 'STAFF';
  tbody.innerHTML = list.map(s => {
    const canComplete = canMarkComplete && !s.completed && s.status !== 'CANCELLED';
    const hasNoActions = !isAdmin && !canComplete;
    return `
    <tr class="${s.completed ? 'row-completed' : ''}">
      <td class="font-medium">
        ${escHtml(s.scheduleNumber||'—')}
        ${s.completed ? '<span class="badge badge-teal" style="margin-left:.375rem;font-size:.65rem">✓ Completed</span>' : ''}
      </td>
      <td>${escHtml(s.train?.name||'—')}<div class="td-subtext">${escHtml(s.train?.trainNumber||'')}</div></td>
      <td>${escHtml(s.route?.startStation||'—')} → ${escHtml(s.route?.endStation||'—')}<div class="td-subtext">${escHtml(s.route?.routeNumber||'')}</div></td>
      <td>${escHtml(fmtDateTime(s.departureTime))}</td>
      <td>${escHtml(fmtDateTime(s.arrivalTime))}</td>
      <td>${scheduleStatusBadge(s.status)}</td>
      <td><div class="td-actions">
        ${isAdmin ? `
        <button class="btn btn-outline btn-sm btn-icon" title="Edit" onclick='openScheduleEditModal(${JSON.stringify(s)})'>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:1rem;height:1rem"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
        </button>
        <button class="btn btn-destructive btn-sm btn-icon" title="Delete" onclick="deleteSchedule('${escHtml(s.scheduleNumber||'')}')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:1rem;height:1rem"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/></svg>
        </button>` : ''}
        ${canComplete ? `
        <button class="btn btn-green btn-sm btn-icon" title="Mark as Completed" onclick="markScheduleComplete('${escHtml(s.scheduleNumber||'')}')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:1rem;height:1rem"><polyline points="20 6 9 17 4 12"/></svg>
        </button>` : ''}
        ${hasNoActions ? '<span class="text-xs text-gray-500">View only</span>' : ''}
      </div></td>
    </tr>`;
  }).join('');
}

async function submitAddSchedule() {
  const trainNumber  = document.getElementById('sch-trainNumber').value.trim();
  const routeNumber  = document.getElementById('sch-routeNumber').value.trim();
  const depRaw       = document.getElementById('sch-departureTime').value;
  const arrRaw       = document.getElementById('sch-arrivalTime').value;
  if (!trainNumber || !routeNumber || !depRaw || !arrRaw) {
    showToast('error', 'Validation Error', 'All fields are required.'); return;
  }
  const body = { trainNumber, routeNumber, departureTime: toApiDateTime(depRaw), arrivalTime: toApiDateTime(arrRaw) };
  const btn = document.getElementById('sch-add-btn');
  setLoading(btn, true, 'Scheduling…');
  try {
    const { ok, data } = await apiCall('/schedules/add', { method: 'POST', body: JSON.stringify(body) });
    if (ok) {
      showToast('success', 'Schedule Created', `Schedule ${data.data?.scheduleNumber || ''} added.`);
      ['sch-trainNumber','sch-routeNumber','sch-departureTime','sch-arrivalTime'].forEach(id => document.getElementById(id).value = '');
      loadSchedules();
    } else { showToast('error', 'Failed', data.message || 'Could not create schedule.'); }
  } catch (_) { showToast('error', 'Connection Error', 'Could not reach the server.'); }
  setLoading(btn, false, 'Create Schedule');
}

function openScheduleEditModal(s) {
  document.getElementById('edit-sch-number-display').textContent = s.scheduleNumber;
  document.getElementById('edit-sch-original').value      = s.scheduleNumber;
  document.getElementById('edit-sch-trainNumber').value   = s.train?.trainNumber || '';
  document.getElementById('edit-sch-routeNumber').value   = s.route?.routeNumber || '';
  document.getElementById('edit-sch-departureTime').value = toInputDateTime(s.departureTime);
  document.getElementById('edit-sch-arrivalTime').value   = toInputDateTime(s.arrivalTime);
  document.getElementById('edit-sch-status').value        = s.status || 'ON_TIME';

  const statusSelect   = document.getElementById('edit-sch-status');
  const completedNote  = document.getElementById('edit-sch-completed-note');
  const cancelledNote  = document.getElementById('edit-sch-cancelled-note');
  if (s.completed) {
    statusSelect.disabled = true;
    statusSelect.dataset.lockReason = 'completed';
    if (completedNote) completedNote.classList.remove('hidden');
    if (cancelledNote) cancelledNote.classList.add('hidden');
  } else if (s.status === 'CANCELLED') {
    statusSelect.disabled = true;
    statusSelect.dataset.lockReason = 'cancelled';
    if (cancelledNote) cancelledNote.classList.remove('hidden');
    if (completedNote) completedNote.classList.add('hidden');
  } else {
    statusSelect.disabled = false;
    delete statusSelect.dataset.lockReason;
    if (completedNote) completedNote.classList.add('hidden');
    if (cancelledNote) cancelledNote.classList.add('hidden');
  }

  openModal('schedule-edit-modal');
}

async function submitUpdateSchedule() {
  const scheduleNumber = document.getElementById('edit-sch-original').value;
  const statusEl = document.getElementById('edit-sch-status');
  if (statusEl.disabled) {
    const reason = statusEl.dataset.lockReason;
    const msg = reason === 'cancelled'
      ? `Schedule ${scheduleNumber} is cancelled and cannot be modified.`
      : `Schedule ${scheduleNumber} is completed and cannot be modified.`;
    showToast('error', 'Edit Not Allowed', msg);
    return;
  }
  const body = {
    trainNumber:   document.getElementById('edit-sch-trainNumber').value.trim(),
    routeNumber:   document.getElementById('edit-sch-routeNumber').value.trim(),
    departureTime: toApiDateTime(document.getElementById('edit-sch-departureTime').value),
    arrivalTime:   toApiDateTime(document.getElementById('edit-sch-arrivalTime').value),
    status:        statusEl.value,
  };
  const btn = document.getElementById('edit-sch-btn');
  setLoading(btn, true, 'Saving…');
  try {
    const { ok, data } = await apiCall(`/schedules/update/scheduleNumber/${encodeURIComponent(scheduleNumber)}`, { method: 'PUT', body: JSON.stringify(body) });
    if (ok) { showToast('success', 'Schedule Updated', `${scheduleNumber} was updated.`); closeModal('schedule-edit-modal'); loadSchedules(); }
    else { showToast('error', 'Failed', data.message || 'Could not update schedule.'); }
  } catch (_) { showToast('error', 'Connection Error', 'Could not reach the server.'); }
  setLoading(btn, false, 'Save Changes');
}

function deleteSchedule(scheduleNumber) {
  openConfirmModal({ title: 'Delete Schedule', heading: 'Delete Schedule?',
    msg: `Permanently delete schedule ${scheduleNumber}? All bookings for this schedule may be affected.`,
    btnText: 'Delete Schedule', cb: async () => {
      try {
        const { ok, data } = await apiCall(`/schedules/delete/scheduleNumber/${encodeURIComponent(scheduleNumber)}`, { method: 'DELETE' });
        if (ok) { showToast('success', 'Deleted', `Schedule ${scheduleNumber} removed.`); closeModal('confirm-modal'); loadSchedules(); }
        else showToast('error', 'Failed', data.message || 'Could not delete schedule.');
      } catch (_) { showToast('error', 'Connection Error', 'Could not reach the server.'); }
    }
  });
}

function markScheduleComplete(scheduleNumber) {
  openConfirmModal({
    title: 'Mark Trip as Completed',
    heading: 'Mark trip as completed?',
    msg: `Mark schedule ${scheduleNumber} as completed? This indicates the trip has arrived at its destination.`,
    btnText: 'Mark Complete',
    btnClass: 'btn-green',
    iconClass: 'green',
    cb: async () => {
      try {
        const { ok, data } = await apiCall(`/schedules/complete/scheduleNumber/${encodeURIComponent(scheduleNumber)}`, { method: 'POST' });
        if (ok) {
          showToast('success', 'Trip Completed', `Schedule ${scheduleNumber} has been marked as completed.`);
          closeModal('confirm-modal');
          loadSchedules();
        } else {
          showToast('error', 'Failed', data.message || 'Could not mark schedule as completed.');
        }
      } catch (_) { showToast('error', 'Connection Error', 'Could not reach the server.'); }
    }
  });
}

/* ═══════════════════════════════════════════════════════════════
   TRAIN SCHEDULES (Staff read-only view)
═══════════════════════════════════════════════════════════════ */
let allTsSchedules = [];

async function loadTrainSchedules() {
  document.getElementById('ts-schedules-tbody').innerHTML = loadingRow(7);
  try {
    const { ok, data } = await apiCall('/schedules/all');
    if (ok) {
      allTsSchedules = Array.isArray(data.data) ? data.data : [];
      document.getElementById('ts-stat-total').textContent     = allTsSchedules.length;
      document.getElementById('ts-stat-ontime').textContent    = allTsSchedules.filter(s => s.status === 'ON_TIME').length;
      document.getElementById('ts-stat-delayed').textContent   = allTsSchedules.filter(s => s.status === 'DELAYED').length;
      document.getElementById('ts-stat-cancelled').textContent = allTsSchedules.filter(s => s.status === 'CANCELLED').length;
      document.getElementById('ts-stat-completed').textContent = allTsSchedules.filter(s => s.completed).length;
      const filter = document.getElementById('ts-completion-filter');
      if (filter) filter.value = '';
      renderTsSchedulesTable(allTsSchedules);
    } else {
      document.getElementById('ts-schedules-tbody').innerHTML = errorRow(7, data.message || 'Failed to load schedules.');
    }
  } catch (_) { document.getElementById('ts-schedules-tbody').innerHTML = errorRow(7, 'Cannot connect to server.'); }
}

function filterTsSchedulesByCompletion() {
  const filter = document.getElementById('ts-completion-filter').value;
  let filtered = allTsSchedules;
  if (filter === 'completed')          filtered = allTsSchedules.filter(s => s.completed);
  else if (filter === 'not-completed') filtered = allTsSchedules.filter(s => !s.completed);
  renderTsSchedulesTable(filtered);
}

function renderTsSchedulesTable(list) {
  const tbody = document.getElementById('ts-schedules-tbody');
  const empty = document.getElementById('ts-schedules-empty');
  if (!list.length) { tbody.innerHTML = ''; empty.classList.remove('hidden'); return; }
  empty.classList.add('hidden');
  tbody.innerHTML = list.map(s => `
    <tr class="${s.completed ? 'row-completed' : ''}">
      <td class="font-medium">
        ${escHtml(s.scheduleNumber||'—')}
        ${s.completed ? '<span class="badge badge-teal" style="margin-left:.375rem;font-size:.65rem">✓ Completed</span>' : ''}
      </td>
      <td>${escHtml(s.train?.name||'—')}<div class="td-subtext">${escHtml(s.train?.trainNumber||'')}</div></td>
      <td>${escHtml(s.route?.startStation||'—')} → ${escHtml(s.route?.endStation||'—')}<div class="td-subtext">${escHtml(s.route?.routeNumber||'')}</div></td>
      <td>${escHtml(fmtDateTime(s.departureTime))}</td>
      <td>${escHtml(fmtDateTime(s.arrivalTime))}</td>
      <td>${scheduleStatusBadge(s.status)}</td>
      <td><span class="text-xs text-gray-500">View only</span></td>
    </tr>`).join('');
}

/* ═══════════════════════════════════════════════════════════════
   PASSENGER REGISTRATION
═══════════════════════════════════════════════════════════════ */
let allPassengers = [];

async function loadPassengers() {
  renderTableLoading();
  try {
    const { ok, data } = await apiFetch('/all');
    if (ok) { allPassengers = Array.isArray(data.data) ? data.data : []; renderTable(allPassengers); updateStats(allPassengers); }
    else { renderTableError(data.message || 'Failed to load passengers.'); }
  } catch (_) { renderTableError('Cannot connect to server. Make sure the API is running on localhost:8080.'); }
}

function updateStats(list) {
  document.getElementById('stat-total').textContent  = list.length;
  document.getElementById('stat-male').textContent   = list.filter(p => (p.gender||'').toUpperCase() === 'MALE').length;
  document.getElementById('stat-female').textContent = list.filter(p => (p.gender||'').toUpperCase() === 'FEMALE').length;
}

function renderTableLoading() {
  document.getElementById('passengers-tbody').innerHTML = loadingRow(6, 'Loading passengers…');
  document.getElementById('table-empty').classList.add('hidden');
}
function renderTableError(msg) { document.getElementById('passengers-tbody').innerHTML = errorRow(6, msg); }

function renderTable(list) {
  const tbody = document.getElementById('passengers-tbody');
  const empty = document.getElementById('table-empty');
  if (!list.length) { tbody.innerHTML = ''; empty.classList.remove('hidden'); return; }
  empty.classList.add('hidden');
  const isAdmin = currentUser.role === 'ADMINISTRATOR';
  tbody.innerHTML = list.map(p => `
    <tr>
      <td class="font-medium">${escHtml(String(p.identificationNumber ?? '—'))}</td>
      <td>${escHtml((p.firstName||'') + ' ' + (p.lastName||''))}</td>
      <td>${genderBadge(p.gender)}</td>
      <td style="color:var(--blue-600)">${escHtml(p.email||'—')}</td>
      <td>${escHtml(p.phoneNumber||'—')}</td>
      <td><div class="td-actions">
        <button class="btn btn-outline btn-sm btn-icon" title="Edit" onclick='openEditModal(${JSON.stringify(p)})'>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:1rem;height:1rem"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
        </button>
        ${isAdmin ? `<button class="btn btn-destructive btn-sm btn-icon" title="Delete" onclick="openDeleteModal(${p.identificationNumber},'${escHtml((p.firstName||'')+' '+(p.lastName||''))}')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:1rem;height:1rem"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>
        </button>` : ''}
      </div></td>
    </tr>`).join('');
}

function genderBadge(g) {
  const u = (g||'').toUpperCase();
  if (u === 'MALE')   return '<span class="badge badge-blue">Male</span>';
  if (u === 'FEMALE') return '<span class="badge badge-purple">Female</span>';
  return '<span class="badge badge-secondary">—</span>';
}

function filterTable() {
  const q  = (document.getElementById('table-search').value||'').toLowerCase();
  const gf = (document.getElementById('table-gender-filter').value||'').toUpperCase();
  renderTable(allPassengers.filter(p => {
    const matchGender = !gf || (p.gender||'').toUpperCase() === gf;
    const matchQ = !q || [String(p.identificationNumber), p.firstName, p.lastName, p.email, p.phoneNumber].some(v => (v||'').toLowerCase().includes(q));
    return matchGender && matchQ;
  }));
}

function validatePassengerForm(prefix) {
  const fields = ['identificationNumber','firstName','lastName','gender','email','phoneNumber'];
  let valid = true;
  fields.forEach(f => {
    const el = document.getElementById(prefix + f); const err = document.getElementById(prefix + f + '-err');
    const val = (el?.value||'').trim();
    if (!val) { el?.classList.add('error'); if (err) { err.textContent = 'Required.'; err.classList.remove('hidden'); } valid = false; }
    else if (f === 'email' && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val)) { el?.classList.add('error'); if (err) { err.textContent = 'Invalid email.'; err.classList.remove('hidden'); } valid = false; }
    else { el?.classList.remove('error'); if (err) err.classList.add('hidden'); }
  });
  return valid;
}

function getFormData(prefix) {
  return {
    identificationNumber: parseInt(document.getElementById(prefix+'identificationNumber').value),
    firstName:   document.getElementById(prefix+'firstName').value.trim(),
    lastName:    document.getElementById(prefix+'lastName').value.trim(),
    gender:      document.getElementById(prefix+'gender').value,
    email:       document.getElementById(prefix+'email').value.trim(),
    phoneNumber: document.getElementById(prefix+'phoneNumber').value.trim(),
  };
}

async function submitRegister() {
  if (!validatePassengerForm('reg-')) return;
  const btn = document.getElementById('reg-submit-btn');
  setLoading(btn, true, 'Registering…');
  try {
    const body = getFormData('reg-');
    const { ok, data } = await apiFetch('/add', { method:'POST', body: JSON.stringify(body) });
    if (ok) { showToast('success', 'Passenger Registered', `${body.firstName} ${body.lastName} added.`); clearRegForm(); loadPassengers(); }
    else { showToast('error', 'Registration Failed', data.message || 'An error occurred.'); }
  } catch (_) { showToast('error', 'Connection Error', 'Could not reach the API server.'); }
  setLoading(btn, false, 'Register Passenger');
}

function clearRegForm() {
  ['reg-identificationNumber','reg-firstName','reg-lastName','reg-gender','reg-email','reg-phoneNumber'].forEach(id => { const el = document.getElementById(id); el.value=''; el.classList.remove('error'); });
  ['reg-identificationNumber-err','reg-firstName-err','reg-lastName-err','reg-gender-err','reg-email-err','reg-phoneNumber-err'].forEach(id => document.getElementById(id).classList.add('hidden'));
}

function openEditModal(p) {
  document.getElementById('edit-idnum-display').textContent  = p.identificationNumber;
  document.getElementById('edit-original-idnum').value       = p.identificationNumber;
  document.getElementById('edit-identificationNumber').value = p.identificationNumber;
  document.getElementById('edit-firstName').value  = p.firstName  || '';
  document.getElementById('edit-lastName').value   = p.lastName   || '';
  document.getElementById('edit-gender').value     = (p.gender||'').toUpperCase();
  document.getElementById('edit-email').value      = p.email      || '';
  document.getElementById('edit-phoneNumber').value = p.phoneNumber || '';
  ['identificationNumber','firstName','lastName','gender','email','phoneNumber'].forEach(f => { document.getElementById('edit-'+f)?.classList.remove('error'); document.getElementById('edit-'+f+'-err')?.classList.add('hidden'); });
  openModal('edit-modal');
}

async function submitUpdate() {
  if (!validatePassengerForm('edit-')) return;
  const btn = document.getElementById('edit-submit-btn');
  const origIdnum = document.getElementById('edit-original-idnum').value;
  setLoading(btn, true, 'Saving…');
  try {
    const body = getFormData('edit-');
    const { ok, data } = await apiFetch(`/update/${origIdnum}`, { method:'PUT', body: JSON.stringify(body) });
    if (ok) { showToast('success', 'Passenger Updated', `${body.firstName} ${body.lastName} updated.`); closeModal('edit-modal'); loadPassengers(); }
    else { showToast('error', 'Update Failed', data.message || 'An error occurred.'); }
  } catch (_) { showToast('error', 'Connection Error', 'Could not reach the API server.'); }
  setLoading(btn, false, 'Save Changes');
}

function openDeleteModal(identificationNumber, name) {
  openConfirmModal({ title: 'Delete Passenger', heading: 'Delete Passenger?',
    msg: `Permanently delete "${name}"? This action cannot be undone.`,
    btnText: 'Delete Passenger', cb: async () => {
      try {
        const { ok, data } = await apiFetch(`/delete/identification/${identificationNumber}`, { method:'DELETE' });
        if (ok) { showToast('success', 'Deleted', 'Passenger removed.'); closeModal('confirm-modal'); loadPassengers(); }
        else { showToast('error', 'Delete Failed', data.message || 'An error occurred.'); }
      } catch (_) { showToast('error', 'Connection Error', 'Could not reach the API server.'); }
    }
  });
}

async function lookupByIdNumber() {
  const val = document.getElementById('lookup-idnum').value.trim();
  if (!val) { showToast('error', 'Required', 'Enter an identification number.'); return; }
  const c = document.getElementById('lookup-result');
  c.innerHTML = '<div class="text-sm text-gray-500" style="padding:.5rem 0"><span class="spinner"></span> Searching…</div>';
  try {
    const { ok, data } = await apiFetch(`/identification/${val}`);
    c.innerHTML = ok ? renderPassengerDetail(data.data) : `<div class="alert-box alert-red"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg><div><div class="alert-title">Not Found</div><div class="alert-message">${escHtml(data.message||'No passenger found.')}</div></div></div>`;
  } catch (_) { c.innerHTML = `<div class="alert-box alert-red"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg><div><div class="alert-title">Connection Error</div></div></div>`; }
}

async function searchByName() {
  const fn = document.getElementById('search-firstName').value.trim(), ln = document.getElementById('search-lastName').value.trim();
  if (!fn || !ln) { showToast('error', 'Required', 'Enter both first and last name.'); return; }
  await doSearch(`/search/name?firstName=${encodeURIComponent(fn)}&lastName=${encodeURIComponent(ln)}`, 'search-result');
}
async function searchByEmail() {
  const e = document.getElementById('search-email').value.trim();
  if (!e) { showToast('error', 'Required', 'Enter an email address.'); return; }
  await doSearch(`/search/email?email=${encodeURIComponent(e)}`, 'search-result');
}
async function searchByPhone() {
  const p = document.getElementById('search-phone').value.trim();
  if (!p) { showToast('error', 'Required', 'Enter a phone number.'); return; }
  await doSearch(`/search/phone?phoneNumber=${encodeURIComponent(p)}`, 'search-result');
}

async function doSearch(url, containerId) {
  const c = document.getElementById(containerId);
  c.innerHTML = '<div class="text-sm text-gray-500" style="padding:.5rem 0"><span class="spinner"></span> Searching…</div>';
  try {
    const { ok, data } = await apiFetch(url);
    if (ok) {
      const list = Array.isArray(data.data) ? data.data : (data.data ? [data.data] : []);
      c.innerHTML = list.length ? renderPassengerTable(list) : '<div class="empty-state"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg><p>No passengers matched.</p></div>';
    } else { c.innerHTML = `<div class="alert-box alert-red"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg><div><div class="alert-title">Not Found</div><div class="alert-message">${escHtml(data.message||'No results.')}</div></div></div>`; }
  } catch (_) { c.innerHTML = `<div class="alert-box alert-red"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg><div><div class="alert-title">Connection Error</div></div></div>`; }
}

function renderPassengerTable(list) {
  return `<div class="card" style="border:1px solid var(--border)"><div class="card-header"><div class="card-title" style="font-size:1rem">${list.length} Result${list.length>1?'s':''} Found</div></div><div class="card-content pt-0"><div class="table-wrapper"><table><thead><tr><th>ID</th><th>Name</th><th>Gender</th><th>Email</th><th>Phone</th><th>Actions</th></tr></thead><tbody>${list.map(p=>`<tr><td class="font-medium">${escHtml(String(p.identificationNumber??'—'))}</td><td>${escHtml((p.firstName||'')+' '+(p.lastName||''))}</td><td>${genderBadge(p.gender)}</td><td style="color:var(--blue-600)">${escHtml(p.email||'—')}</td><td>${escHtml(p.phoneNumber||'—')}</td><td><div class="td-actions"><button class="btn btn-outline btn-sm btn-icon" onclick='openEditModal(${JSON.stringify(p)})'><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:1rem;height:1rem"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button></div></td></tr>`).join('')}</tbody></table></div></div></div>`;
}

function renderPassengerDetail(p) {
  if (!p) return `<div class="alert-box alert-red"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg><div class="alert-title">No data returned.</div></div>`;
  return `<div class="card" style="border:1px solid var(--border)"><div class="card-header flex-row"><div><div class="card-title" style="font-size:1rem">${escHtml((p.firstName||'')+' '+(p.lastName||''))}</div><div class="card-desc">ID: ${escHtml(String(p.identificationNumber??'—'))}</div></div><button class="btn btn-outline btn-sm" onclick='openEditModal(${JSON.stringify(p)})'>Edit</button></div><div class="card-content pt-0"><div style="display:grid;grid-template-columns:repeat(3,1fr);gap:1rem;background:var(--blue-50);border:1px solid #bfdbfe;border-radius:var(--radius);padding:1.25rem"><div><div class="text-xs text-gray-500 mb-1">First Name</div><div class="font-medium">${escHtml(p.firstName||'—')}</div></div><div><div class="text-xs text-gray-500 mb-1">Last Name</div><div class="font-medium">${escHtml(p.lastName||'—')}</div></div><div><div class="text-xs text-gray-500 mb-1">Gender</div>${genderBadge(p.gender)}</div><div><div class="text-xs text-gray-500 mb-1">Email</div><div class="font-medium" style="color:var(--blue-600)">${escHtml(p.email||'—')}</div></div><div><div class="text-xs text-gray-500 mb-1">Phone</div><div class="font-medium">${escHtml(p.phoneNumber||'—')}</div></div></div></div></div>`;
}

/* ═══════════════════════════════════════════════════════════════
   TICKET RESERVATION
═══════════════════════════════════════════════════════════════ */
let _availData = [];
let _selectedClass = null;

function initTicketReservation() {
  document.getElementById('avail-result').innerHTML = '';
  document.getElementById('bk-result').innerHTML = '';
}

async function checkAvailability() {
  const schedNum = document.getElementById('bk-scheduleNumber').value.trim();
  if (!schedNum) { showToast('error', 'Required', 'Enter a schedule number.'); return; }
  const btn = document.getElementById('bk-check-btn');
  setLoading(btn, true, 'Checking…');
  _availData = []; _selectedClass = null;
  document.getElementById('avail-result').innerHTML = '';
  try {
    const { ok, data } = await apiCall(`/schedules/availability/${encodeURIComponent(schedNum)}`);
    if (ok) {
      const payload = data.data || {};
      const isCompleted = payload.completed === true;
      const isCancelled = payload.status === 'CANCELLED';
      _availData = Array.isArray(payload.availability) ? payload.availability : [];
      if (isCompleted) {
        document.getElementById('avail-result').innerHTML = `
          <div class="alert-box alert-amber">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            <div><div class="alert-title">Schedule Completed</div>
            <div class="alert-message">Schedule <strong>${escHtml(schedNum)}</strong> has already been completed and is no longer accepting bookings.</div></div>
          </div>`;
      } else if (isCancelled) {
        document.getElementById('avail-result').innerHTML = `
          <div class="alert-box alert-red">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            <div><div class="alert-title">Schedule Cancelled</div>
            <div class="alert-message">Schedule <strong>${escHtml(schedNum)}</strong> has been cancelled and is not accepting bookings.</div></div>
          </div>`;
      } else {
        renderAvailabilityCards(_availData);
      }
    } else {
      document.getElementById('avail-result').innerHTML = `<div class="alert-box alert-red"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg><div><div class="alert-title">Not Found</div><div class="alert-message">${escHtml(data.message||'Schedule not found.')}</div></div></div>`;
    }
  } catch (_) { document.getElementById('avail-result').innerHTML = `<div class="alert-box alert-red"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg><div><div class="alert-title">Connection Error</div></div></div>`; }
  setLoading(btn, false, 'Check Availability');
}

function renderAvailabilityCards(list) {
  if (!list.length) { document.getElementById('avail-result').innerHTML = '<div class="empty-state"><p>No seat classes found for this schedule.</p></div>'; return; }
  const cards = list.map(a => {
    const soldOut = a.availableSeats <= 0;
    return `<div class="avail-card${soldOut?' sold-out':''}" onclick="${soldOut?'':` selectSeatClass('${escHtml(a.seatClass)}',this)`}" id="avail-${escHtml(a.seatClass)}">
      <div class="avail-class">${escHtml(a.seatClass)}</div>
      <div class="avail-seats-num">${a.availableSeats}</div>
      <div class="avail-seats-label">of ${a.totalSeats} seats available</div>
      <div class="avail-price">SAR ${a.basePrice != null ? Number(a.basePrice).toFixed(2) : '—'}</div>
      ${soldOut ? '<div style="margin-top:.5rem"><span class="badge badge-red">Sold Out</span></div>' : ''}
    </div>`;
  }).join('');
  document.getElementById('avail-result').innerHTML = `
    <div class="alert-box alert-blue" style="margin-bottom:1rem">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <div class="alert-message">Select a seat class below, then fill in the passenger ID and click <strong>Book Ticket</strong>.</div>
    </div>
    <div class="avail-grid">${cards}</div>`;
}

function selectSeatClass(cls, el) {
  _selectedClass = cls;
  document.querySelectorAll('.avail-card').forEach(c => c.classList.remove('selected'));
  el.classList.add('selected');
  document.getElementById('bk-seatClass').value = cls;
}

async function submitBooking() {
  const scheduleNumber              = document.getElementById('bk-scheduleNumber').value.trim();
  const passengerIdentificationNumber = parseInt(document.getElementById('bk-passengerIdNum').value);
  const seatClass                   = document.getElementById('bk-seatClass').value;
  if (!scheduleNumber || !passengerIdentificationNumber || !seatClass) {
    showToast('error', 'Required', 'Fill in all booking fields and select a seat class.'); return;
  }
  const btn = document.getElementById('bk-submit-btn');
  setLoading(btn, true, 'Booking…');
  document.getElementById('bk-result').innerHTML = '';
  try {
    const { ok, data } = await apiCall('/bookings/add', { method: 'POST', body: JSON.stringify({ scheduleNumber, passengerIdentificationNumber, seatClass }) });
    if (ok) {
      const b = data.data;
      showToast('success', 'Booking Confirmed', `Reference: ${b?.bookingReference || '—'}`);
      document.getElementById('bk-result').innerHTML = `
        <div class="alert-box alert-green">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
          <div>
            <div class="alert-title">Booking Confirmed!</div>
            <div class="alert-message">
              Reference: <strong>${escHtml(b?.bookingReference||'—')}</strong> &nbsp;|&nbsp;
              Seat: <strong>${escHtml(b?.seatNumber||'—')}</strong> &nbsp;|&nbsp;
              Class: <strong>${escHtml(b?.bookedClass||'—')}</strong> &nbsp;|&nbsp;
              Price: <strong>SAR ${b?.finalPrice != null ? Number(b.finalPrice).toFixed(2) : '—'}</strong>
            </div>
          </div>
        </div>`;
      document.getElementById('bk-passengerIdNum').value = '';
      checkAvailability();
    } else {
      document.getElementById('bk-result').innerHTML = `<div class="alert-box alert-red"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg><div><div class="alert-title">Booking Failed</div><div class="alert-message">${escHtml(data.message||'An error occurred.')}</div></div></div>`;
    }
  } catch (_) { document.getElementById('bk-result').innerHTML = `<div class="alert-box alert-red"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg><div><div class="alert-title">Connection Error</div></div></div>`; }
  setLoading(btn, false, 'Book Ticket');
}

/* ═══════════════════════════════════════════════════════════════
   BOOKING HISTORY
═══════════════════════════════════════════════════════════════ */
let allBookings = [];

function bookingStatusBadge(s) {
  if (s === 'CONFIRMED') return '<span class="badge badge-green">Confirmed</span>';
  if (s === 'CANCELLED') return '<span class="badge badge-red">Cancelled</span>';
  if (s === 'COMPLETED') return '<span class="badge badge-blue">Completed</span>';
  return `<span class="badge badge-gray">${escHtml(s||'—')}</span>`;
}
function classBadge(c) {
  if (c === 'BUSINESS') return '<span class="badge badge-purple">Business</span>';
  if (c === 'ECONOMY')  return '<span class="badge badge-blue">Economy</span>';
  if (c === 'SLEEPER')  return '<span class="badge badge-secondary">Sleeper</span>';
  return `<span class="badge badge-gray">${escHtml(c||'—')}</span>`;
}

async function loadBookings() {
  document.getElementById('bookings-tbody').innerHTML = loadingRow(9, 'Loading bookings…');
  try {
    const { ok, data } = await apiCall('/bookings/all');
    if (ok) {
      allBookings = Array.isArray(data.data) ? data.data : [];
      document.getElementById('bkh-stat-total').textContent     = allBookings.length;
      document.getElementById('bkh-stat-confirmed').textContent = allBookings.filter(b => b.status === 'CONFIRMED').length;
      document.getElementById('bkh-stat-cancelled').textContent = allBookings.filter(b => b.status === 'CANCELLED').length;
      document.getElementById('bkh-stat-completed').textContent = allBookings.filter(b => b.status === 'COMPLETED').length;
      renderBookingsTable(allBookings, 'bookings-tbody');
    } else {
      document.getElementById('bookings-tbody').innerHTML = errorRow(9, data.message || 'Failed to load bookings.');
    }
  } catch (_) { document.getElementById('bookings-tbody').innerHTML = errorRow(9, 'Cannot connect to server.'); }
}

function renderBookingsTable(list, tbodyId) {
  const tbody = document.getElementById(tbodyId);
  if (!list.length) { tbody.innerHTML = `<tr><td colspan="9"><div class="empty-state" style="padding:2rem"><p>No bookings found.</p></div></td></tr>`; return; }
  const isAdmin = currentUser.role === 'ADMINISTRATOR';
  tbody.innerHTML = list.map(b => `
    <tr>
      <td class="font-medium">${escHtml(b.bookingReference||'—')}</td>
      <td>${escHtml(b.passengerName||'—')}<div class="td-subtext">ID: ${escHtml(String(b.passengerIdentificationNumber||''))}</div></td>
      <td>${escHtml(b.scheduleNumber||'—')}<div class="td-subtext">${escHtml(b.trainNumber||'')}</div></td>
      <td>${classBadge(b.bookedClass)}</td>
      <td>${escHtml(b.seatNumber||'—')}</td>
      <td>SAR ${b.finalPrice != null ? Number(b.finalPrice).toFixed(2) : '—'}</td>
      <td>${bookingStatusBadge(b.status)}</td>
      <td>${escHtml(fmtDate(b.bookingDate))}</td>
      <td><div class="td-actions">
        ${b.status === 'CONFIRMED' ? `<button class="btn btn-amber btn-sm" onclick="cancelBooking('${escHtml(b.bookingReference||'')}')">Cancel</button>` : ''}
        ${isAdmin && b.status !== 'CONFIRMED' ? `<button class="btn btn-destructive btn-sm btn-icon" title="Delete" onclick="deleteBooking('${escHtml(b.bookingReference||'')}')"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:1rem;height:1rem"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/></svg></button>` : ''}
      </div></td>
    </tr>`).join('');
}

function filterBookings() {
  const q = (document.getElementById('bkh-search').value||'').toLowerCase();
  const sf = document.getElementById('bkh-status-filter').value;
  renderBookingsTable(allBookings.filter(b => {
    const matchStatus = !sf || b.status === sf;
    const matchQ = !q || [b.bookingReference, b.passengerName, b.scheduleNumber, b.trainNumber, String(b.passengerIdentificationNumber)].some(v => (v||'').toLowerCase().includes(q));
    return matchStatus && matchQ;
  }), 'bookings-tbody');
}

async function searchBookingsByPassenger() {
  const idNum = document.getElementById('bkh-passenger-search').value.trim();
  if (!idNum) { showToast('error', 'Required', 'Enter a passenger identification number.'); return; }
  const c = document.getElementById('bkh-passenger-result');
  c.innerHTML = '<div style="padding:.5rem 0"><span class="spinner"></span> Searching…</div>';
  try {
    const { ok, data } = await apiCall(`/bookings/search/passengerIdentificationNumber?passengerIdentificationNumber=${encodeURIComponent(idNum)}`);
    if (ok) {
      const list = Array.isArray(data.data) ? data.data : [];
      if (!list.length) { c.innerHTML = '<div class="empty-state" style="padding:1.5rem"><p>No bookings found for this passenger.</p></div>'; return; }
      c.innerHTML = `<div class="table-wrapper"><table><thead><tr><th>Reference</th><th>Passenger</th><th>Schedule</th><th>Class</th><th>Seat</th><th>Price</th><th>Status</th><th>Date</th><th></th></tr></thead><tbody id="bkh-passenger-tbody"></tbody></table></div>`;
      renderBookingsTable(list, 'bkh-passenger-tbody');
    } else { c.innerHTML = `<div class="alert-box alert-red"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg><div><div class="alert-title">Not Found</div><div class="alert-message">${escHtml(data.message||'No bookings found.')}</div></div></div>`; }
  } catch (_) { c.innerHTML = `<div class="alert-box alert-red"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg><div><div class="alert-title">Connection Error</div></div></div>`; }
}

async function searchBookingsBySchedule() {
  const schedNum = document.getElementById('bkh-schedule-search').value.trim();
  if (!schedNum) { showToast('error', 'Required', 'Enter a schedule number.'); return; }
  const c = document.getElementById('bkh-schedule-result');
  c.innerHTML = '<div style="padding:.5rem 0"><span class="spinner"></span> Searching…</div>';
  try {
    const { ok, data } = await apiCall(`/bookings/search/scheduleNumber?scheduleNumber=${encodeURIComponent(schedNum)}`);
    if (ok) {
      const list = Array.isArray(data.data) ? data.data : [];
      if (!list.length) { c.innerHTML = '<div class="empty-state" style="padding:1.5rem"><p>No bookings found for this schedule.</p></div>'; return; }
      c.innerHTML = `<div class="table-wrapper"><table><thead><tr><th>Reference</th><th>Passenger</th><th>Schedule</th><th>Class</th><th>Seat</th><th>Price</th><th>Status</th><th>Date</th><th></th></tr></thead><tbody id="bkh-schedule-tbody"></tbody></table></div>`;
      renderBookingsTable(list, 'bkh-schedule-tbody');
    } else { c.innerHTML = `<div class="alert-box alert-red"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg><div><div class="alert-title">Not Found</div><div class="alert-message">${escHtml(data.message||'No bookings found.')}</div></div></div>`; }
  } catch (_) { c.innerHTML = `<div class="alert-box alert-red"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg><div><div class="alert-title">Connection Error</div></div></div>`; }
}

function cancelBooking(bookingReference) {
  openConfirmModal({ title: 'Cancel Booking', heading: 'Cancel this booking?',
    msg: `Cancel booking ${bookingReference}? The seat will be released.`,
    btnText: 'Cancel Booking', btnClass: 'btn-amber', iconClass: 'amber', cb: async () => {
      try {
        const { ok, data } = await apiCall(`/bookings/cancel/bookingReference/${encodeURIComponent(bookingReference)}`, { method: 'PATCH' });
        if (ok) { showToast('success', 'Booking Cancelled', `${bookingReference} has been cancelled.`); closeModal('confirm-modal'); loadBookings(); }
        else { showToast('error', 'Failed', data.message || 'Could not cancel booking.'); }
      } catch (_) { showToast('error', 'Connection Error', 'Could not reach the server.'); }
    }
  });
}

function deleteBooking(bookingReference) {
  openConfirmModal({ title: 'Delete Booking', heading: 'Delete booking record?',
    msg: `Permanently delete booking ${bookingReference}?`,
    btnText: 'Delete', cb: async () => {
      try {
        const { ok, data } = await apiCall(`/bookings/delete/bookingReference/${encodeURIComponent(bookingReference)}`, { method: 'DELETE' });
        if (ok) { showToast('success', 'Deleted', `Booking ${bookingReference} removed.`); closeModal('confirm-modal'); loadBookings(); }
        else { showToast('error', 'Failed', data.message || 'Could not delete booking.'); }
      } catch (_) { showToast('error', 'Connection Error', 'Could not reach the server.'); }
    }
  });
}

/* ═══════════════════════════════════════════════════════════════
   LOGIN & AUTH
═══════════════════════════════════════════════════════════════ */
const AUTH_BASE = 'http://localhost:8080/api/v1/auth';

function logout() {
  jwtToken = null;
  currentUser = { username: 'User', role: 'STAFF', initials: 'U' };
  applyRoleBasedUI('STAFF');
  document.getElementById('app').style.display = 'none';
  const ls = document.getElementById('login-screen');
  ls.style.display = 'flex'; ls.classList.remove('fade-out');
  document.getElementById('userInput').value = '';
  document.getElementById('userPassword').value = '';
  document.getElementById('loginMsg').textContent = '';
  const btn = document.getElementById('loginBtn');
  btn.disabled = false;
  btn.textContent = 'Login';
}

document.getElementById('togglePassword').addEventListener('click', function() {
  const pwd = document.getElementById('userPassword');
  const hidden = pwd.type === 'password';
  pwd.type = hidden ? 'text' : 'password';
  this.textContent = hidden ? 'Hide' : 'Show';
});

['userInput', 'userPassword'].forEach(id => {
  document.getElementById(id).addEventListener('keydown', e => { if (e.key === 'Enter') submitLogin(); });
});

async function submitLogin() {
  const username = document.getElementById('userInput').value.trim();
  const password = document.getElementById('userPassword').value.trim();
  const usernameErr = document.getElementById('userInputError');
  const passwordErr = document.getElementById('userPasswordError');
  const msg = document.getElementById('loginMsg');
  const btn = document.getElementById('loginBtn');

  usernameErr.textContent = ''; passwordErr.textContent = ''; msg.textContent = ''; msg.className = 'login-msg';

  let valid = true;
  if (!username) { usernameErr.textContent = 'Please enter your username.'; valid = false; }
  if (!password) { passwordErr.textContent = 'Please enter your password.'; valid = false; }
  if (!valid) return;

  btn.disabled = true;
  btn.innerHTML = '<span class="spinner" style="width:1rem;height:1rem;border-width:2px;border-color:rgba(255,255,255,.3);border-top-color:#fff"></span> Signing in…';

  try {
    const res = await fetch(AUTH_BASE + '/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ username, password }) });
    const data = await res.json();

    if (res.ok) {
      const d = data.data || data;
      jwtToken = d.jwt || d.token || d.accessToken || d.access_token || null;

      // Decode JWT to get role and username
      try {
        const payload = JSON.parse(atob(jwtToken.split('.')[1].replace(/-/g,'+').replace(/_/g,'/')));
        const uname = payload.sub || payload.username || username;
        const authStr = JSON.stringify(payload);
        const role = authStr.includes('ADMINISTRATOR') ? 'ADMINISTRATOR' : 'STAFF';
        const initials = uname.substring(0, 2).toUpperCase();
        currentUser = { username: uname, role, initials };
        document.getElementById('sidebar-username').textContent = uname;
        document.getElementById('sidebar-role').textContent = role === 'ADMINISTRATOR' ? 'Administrator' : 'Staff';
        document.getElementById('sidebar-avatar').textContent = initials;
        document.getElementById('header-avatar').textContent = initials;
        applyRoleBasedUI(role);
      } catch (_) {
        currentUser = { username, role: 'STAFF', initials: username.substring(0,2).toUpperCase() };
        applyRoleBasedUI('STAFF');
      }

      const loginScreen = document.getElementById('login-screen');
      loginScreen.classList.add('fade-out');
      const landingPage = currentUser.role === 'ADMINISTRATOR' ? 'dashboard' : 'train-schedules';
      setTimeout(() => {
        loginScreen.style.display = 'none';
        document.getElementById('app').style.display = 'flex';
        navigateTo(landingPage);
        history.replaceState({ pageId: landingPage }, '', '/' + landingPage);
      }, 350);
    } else {
      msg.textContent = data.message || 'Invalid username or password.';
      msg.className = 'login-msg error';
      btn.disabled = false; btn.textContent = 'Login';
    }
  } catch (_) {
    msg.textContent = 'Cannot connect to server. Make sure the API is running.';
    msg.className = 'login-msg error';
    btn.disabled = false; btn.textContent = 'Login';
  }
}
