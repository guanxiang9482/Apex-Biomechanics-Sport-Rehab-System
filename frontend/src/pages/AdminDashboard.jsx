import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { admin, auth, notifications } from '../services/api';
import { formatCurrency, formatDateTime, statusClass } from '../utils/format';
import './Dashboard.css';

const initialAdmission = {
  username: '',
  password: '',
  email: '',
  fullName: '',
  therapistId: '',
  facilityId: '1',
};

const initialBilling = {
  sessionId: '',
  athleteId: '',
  billingType: 'STANDARD',
};

const initialStaff = {
  username: '',
  password: '',
  email: '',
  fullName: '',
  role: 'THERAPIST',
};

const facilities = [
  { id: 1, name: 'Gait Analysis Lab A', type: 'Biomechanics', status: 'Available' },
  { id: 2, name: 'Strength Assessment Room', type: 'Performance', status: 'Available' },
  { id: 3, name: 'Recovery Pool', type: 'Hydrotherapy', status: 'Available' },
];

function AdminDashboard() {
  const navigate = useNavigate();
  const [user] = useState(() => JSON.parse(localStorage.getItem('user')));
  const [activeSection, setActiveSection] = useState('overview');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);
  const [analytics, setAnalytics] = useState(null);
  const [athletes, setAthletes] = useState([]);
  const [ledger, setLedger] = useState([]);
  const [noticeList, setNoticeList] = useState([]);
  const [admissionForm, setAdmissionForm] = useState(initialAdmission);
  const [billingForm, setBillingForm] = useState(initialBilling);
  const [billingResult, setBillingResult] = useState(null);
  const [staffForm, setStaffForm] = useState(initialStaff);
  const [deactivateId, setDeactivateId] = useState('');

  const showMessage = (type, text) => setMessage({ type, text });

  const loadDashboard = useCallback(async () => {
    if (!user?.userId) return;
    setLoading(true);
    setMessage(null);

    try {
      const [analyticsData, athleteData, ledgerData, userNotifications] = await Promise.all([
        admin.getAnalytics(),
        admin.getAllAthletes(),
        admin.getFullLedger(),
        notifications.getAll(user.userId),
      ]);

      setAnalytics(analyticsData);
      setAthletes(athleteData);
      setLedger(ledgerData);
      setNoticeList(userNotifications);
    } catch (error) {
      showMessage('error', error.message);
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    if (!user || user.role !== 'ADMIN') {
      navigate('/');
      return;
    }
    const timer = window.setTimeout(loadDashboard, 0);
    return () => window.clearTimeout(timer);
  }, [loadDashboard, navigate, user]);

  const handleLogout = async () => {
    try {
      await auth.logout(user.userId);
    } catch (error) {
      console.log('Logout error:', error.message);
    }
    localStorage.removeItem('user');
    navigate('/');
  };

  const updateAdmission = (event) => {
    const { name, value } = event.target;
    setAdmissionForm((current) => ({ ...current, [name]: value }));
  };

  const updateBilling = (event) => {
    const { name, value } = event.target;
    setBillingForm((current) => ({ ...current, [name]: value }));
  };

  const updateStaff = (event) => {
    const { name, value } = event.target;
    setStaffForm((current) => ({ ...current, [name]: value }));
  };

  const handleAdmission = async (event) => {
    event.preventDefault();
    try {
      const result = await admin.admitNewAthlete(
        admissionForm.username.trim(),
        admissionForm.password,
        admissionForm.email.trim(),
        admissionForm.fullName.trim(),
        Number(admissionForm.therapistId),
        Number(admissionForm.facilityId),
      );
      setAdmissionForm(initialAdmission);
      showMessage('success', `${result.fullName} admitted successfully. Athlete ID: ${result.athleteId}`);
      await loadDashboard();
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleBilling = async (event) => {
    event.preventDefault();
    try {
      const result = await admin.processBilling(
        Number(billingForm.sessionId),
        Number(billingForm.athleteId),
        billingForm.billingType,
      );
      setBillingResult(result);
      showMessage('success', `Billing processed using ${result.strategy}.`);
      await loadDashboard();
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleAddStaff = async (event) => {
    event.preventDefault();
    try {
      const result = await admin.addStaff(
        staffForm.username.trim(),
        staffForm.password,
        staffForm.email.trim(),
        staffForm.fullName.trim(),
        staffForm.role,
      );
      setStaffForm(initialStaff);
      showMessage('success', `Staff account created. User ID: ${result.userId}`);
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleDeactivateStaff = async (event) => {
    event.preventDefault();
    try {
      await admin.deactivateStaff(Number(deactivateId));
      setDeactivateId('');
      showMessage('success', 'Staff account deactivated successfully.');
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleReadNotification = async (notificationId) => {
    try {
      await notifications.markAsRead(notificationId);
      await loadDashboard();
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const paidRevenue = ledger
    .filter((invoice) => invoice.status === 'PAID')
    .reduce((sum, invoice) => sum + Number(invoice.amount || 0), 0);

  const sectionTitle = {
    overview: 'Admin Overview',
    admission: 'Admit Athlete',
    analytics: 'Analytics & Athletes',
    billing: 'Billing & Ledger',
    staff: 'Staff Management',
    facilities: 'Facilities & Equipment',
    notifications: 'Notifications',
  }[activeSection];

  return (
    <div className="dashboard admin-dashboard">
      <header className="admin-topbar">
        <div className="admin-brand">
          <span>APEX</span>
          <div>
            <strong>Clinical Operations</strong>
            <small>Biomechanics & Sports Rehab</small>
          </div>
        </div>

        <nav className="admin-nav" aria-label="Admin sections">
          <button className={activeSection === 'overview' ? 'active' : ''} onClick={() => setActiveSection('overview')}>Overview</button>
          <button className={activeSection === 'admission' ? 'active' : ''} onClick={() => setActiveSection('admission')}>Admission</button>
          <button className={activeSection === 'analytics' ? 'active' : ''} onClick={() => setActiveSection('analytics')}>Analytics</button>
          <button className={activeSection === 'billing' ? 'active' : ''} onClick={() => setActiveSection('billing')}>Billing</button>
          <button className={activeSection === 'staff' ? 'active' : ''} onClick={() => setActiveSection('staff')}>Staff</button>
          <button className={activeSection === 'facilities' ? 'active' : ''} onClick={() => setActiveSection('facilities')}>Facilities</button>
          <button className={activeSection === 'notifications' ? 'active' : ''} onClick={() => setActiveSection('notifications')}>Notifications</button>
        </nav>

        <div className="admin-topbar-actions">
          <button className="btn-secondary" type="button" onClick={loadDashboard}>Refresh</button>
          <button className="logout-btn admin-logout" onClick={handleLogout}>Logout</button>
        </div>
      </header>

      <main className="main-content admin-main">
        <header className="content-header admin-hero">
          <div>
            <p className="eyebrow">Administrator Portal</p>
            <h1>{sectionTitle}</h1>
            <p>Welcome, {user?.username}. Manage admissions, billing, staff access, and clinic operations.</p>
          </div>
          <div className="admin-hero-panel" aria-label="Admin quick summary">
            <span>Active Module</span>
            <strong>{sectionTitle}</strong>
            <small>Operations Console</small>
          </div>
        </header>

        {message && <div className={`dashboard-message ${message.type}`}>{message.text}</div>}
        {loading && <div className="dashboard-message">Loading latest data...</div>}

        {activeSection === 'overview' && (
          <section className="section section-stack">
            <div className="stat-grid">
              <article className="stat-card">
                <span>Athletes</span>
                <strong>{analytics?.totalAthletes ?? athletes.length}</strong>
                <p>registered profiles</p>
              </article>
              <article className="stat-card">
                <span>Invoices</span>
                <strong>{analytics?.totalInvoices ?? ledger.length}</strong>
                <p>ledger records</p>
              </article>
              <article className="stat-card">
                <span>Pending</span>
                <strong>{analytics?.pendingInvoices ?? ledger.filter((item) => item.status === 'PENDING').length}</strong>
                <p>unpaid invoices</p>
              </article>
              <article className="stat-card">
                <span>Paid Revenue</span>
                <strong>{formatCurrency(analytics?.totalRevenue ?? paidRevenue)}</strong>
                <p>confirmed income</p>
              </article>
            </div>

            <div className="pattern-grid">
              <article className="pattern-card">
                <span>Facade</span>
                <h3>Admission Workflow</h3>
                <p>One form calls the backend admission facade to create athlete profile and initial evaluation flow.</p>
              </article>
              <article className="pattern-card">
                <span>Strategy</span>
                <h3>Billing Method</h3>
                <p>The selected billing type changes how the backend calculates session fees.</p>
              </article>
              <article className="pattern-card">
                <span>Observer</span>
                <h3>Notifications</h3>
                <p>Booking, status, and billing events create notification records for related users.</p>
              </article>
            </div>
          </section>
        )}

        {activeSection === 'admission' && (
          <section className="section">
            <form className="panel form-grid" onSubmit={handleAdmission}>
              <label>
                Full Name
                <input name="fullName" value={admissionForm.fullName} onChange={updateAdmission} required />
              </label>
              <label>
                Email
                <input name="email" type="email" value={admissionForm.email} onChange={updateAdmission} required />
              </label>
              <label>
                Username
                <input name="username" value={admissionForm.username} onChange={updateAdmission} required />
              </label>
              <label>
                Password
                <input name="password" type="password" value={admissionForm.password} onChange={updateAdmission} required />
              </label>
              <label>
                Assigned Therapist ID
                <input name="therapistId" type="number" min="1" value={admissionForm.therapistId} onChange={updateAdmission} required />
              </label>
              <label>
                Facility
                <select name="facilityId" value={admissionForm.facilityId} onChange={updateAdmission}>
                  {facilities.map((facility) => (
                    <option value={facility.id} key={facility.id}>{facility.id} - {facility.name}</option>
                  ))}
                </select>
              </label>
              <button className="btn-primary compact" type="submit">Admit Athlete</button>
            </form>
          </section>
        )}

        {activeSection === 'analytics' && (
          <section className="section section-stack">
            <div className="panel">
              <div className="panel-header">
                <h2>Clinic Analytics</h2>
                <span>UC16</span>
              </div>
              <div className="stat-grid compact-stats">
                <article className="stat-card"><span>Total Athletes</span><strong>{analytics?.totalAthletes ?? 0}</strong></article>
                <article className="stat-card"><span>Total Invoices</span><strong>{analytics?.totalInvoices ?? 0}</strong></article>
                <article className="stat-card"><span>Pending Invoices</span><strong>{analytics?.pendingInvoices ?? 0}</strong></article>
                <article className="stat-card"><span>Total Revenue</span><strong>{formatCurrency(analytics?.totalRevenue ?? 0)}</strong></article>
              </div>
            </div>

            <div className="panel">
              <div className="panel-header">
                <h2>Athlete Profiles</h2>
                <span>{athletes.length}</span>
              </div>
              {athletes.length === 0 ? (
                <p className="empty-state">No athletes found.</p>
              ) : (
                <div className="table-wrap">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Injury</th>
                        <th>Body</th>
                      </tr>
                    </thead>
                    <tbody>
                      {athletes.map((athleteProfile) => (
                        <tr key={athleteProfile.userId}>
                          <td>#{athleteProfile.userId}</td>
                          <td>{athleteProfile.fullName}</td>
                          <td>{athleteProfile.email}</td>
                          <td>{athleteProfile.injuryStatus || 'None'}</td>
                          <td>{athleteProfile.heightCm || '-'} cm / {athleteProfile.bodyWeightKg || '-'} kg</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </section>
        )}

        {activeSection === 'billing' && (
          <section className="section section-stack">
            <form className="panel form-grid" onSubmit={handleBilling}>
              <label>
                Session ID
                <input name="sessionId" type="number" min="1" value={billingForm.sessionId} onChange={updateBilling} required />
              </label>
              <label>
                Athlete ID
                <input name="athleteId" type="number" min="1" value={billingForm.athleteId} onChange={updateBilling} required />
              </label>
              <label>
                Billing Strategy
                <select name="billingType" value={billingForm.billingType} onChange={updateBilling}>
                  <option value="STANDARD">Standard</option>
                  <option value="INSURANCE">Insurance</option>
                  <option value="SPONSORSHIP">Sponsorship</option>
                </select>
              </label>
              <button className="btn-primary compact" type="submit">Process Billing</button>
            </form>

            {billingResult && (
              <div className="panel result-panel">
                <h2>Billing Result</h2>
                <p>Invoice #{billingResult.invoiceId} created with {billingResult.strategy}.</p>
                <strong>{formatCurrency(billingResult.amount)}</strong>
              </div>
            )}

            <div className="panel">
              <div className="panel-header">
                <h2>Financial Ledger</h2>
                <span>UC20</span>
              </div>
              <LedgerTable ledger={ledger} />
            </div>
          </section>
        )}

        {activeSection === 'staff' && (
          <section className="section section-stack">
            <form className="panel form-grid" onSubmit={handleAddStaff}>
              <label>
                Full Name
                <input name="fullName" value={staffForm.fullName} onChange={updateStaff} required />
              </label>
              <label>
                Email
                <input name="email" type="email" value={staffForm.email} onChange={updateStaff} required />
              </label>
              <label>
                Username
                <input name="username" value={staffForm.username} onChange={updateStaff} required />
              </label>
              <label>
                Password
                <input name="password" type="password" value={staffForm.password} onChange={updateStaff} required />
              </label>
              <label>
                Role
                <select name="role" value={staffForm.role} onChange={updateStaff}>
                  <option value="THERAPIST">Therapist</option>
                  <option value="ADMIN">Admin</option>
                </select>
              </label>
              <button className="btn-primary compact" type="submit">Add Staff</button>
            </form>

            <form className="panel form-inline" onSubmit={handleDeactivateStaff}>
              <input type="number" min="1" placeholder="Staff user ID" value={deactivateId} onChange={(event) => setDeactivateId(event.target.value)} required />
              <button className="btn-danger" type="submit">Deactivate Staff</button>
            </form>
          </section>
        )}

        {activeSection === 'facilities' && (
          <section className="section">
            <div className="panel">
              <div className="panel-header">
                <h2>Facility Reference</h2>
                <span>UC17</span>
              </div>
              <div className="card-grid">
                {facilities.map((facility) => (
                  <article className="card" key={facility.id}>
                    <div className="card-topline">
                      <h3>{facility.name}</h3>
                      <span className="status-badge status-completed">{facility.status}</span>
                    </div>
                    <p>Facility #{facility.id}</p>
                    <p>{facility.type}</p>
                    <p>Used by admission and booking forms.</p>
                  </article>
                ))}
              </div>
            </div>
          </section>
        )}

        {activeSection === 'notifications' && (
          <section className="section">
            {noticeList.length === 0 ? (
              <p className="empty-state">No notifications found.</p>
            ) : (
              <div className="card-list">
                {noticeList.map((notice) => {
                  const id = notice.notification_id ?? notice.notificationId;
                  const isRead = notice.is_read === true || notice.is_read === 1;
                  return (
                    <article className={`card compact-card ${isRead ? '' : 'highlight-card'}`} key={id}>
                      <h3>{isRead ? 'Read' : 'Unread'} Notification</h3>
                      <p>{notice.event_message ?? notice.eventMessage}</p>
                      <small>{formatDateTime(notice.created_at ?? notice.createdAt)}</small>
                      {!isRead && <button className="btn-secondary" type="button" onClick={() => handleReadNotification(id)}>Mark as Read</button>}
                    </article>
                  );
                })}
              </div>
            )}
          </section>
        )}
      </main>
    </div>
  );
}

function LedgerTable({ ledger }) {
  if (ledger.length === 0) {
    return <p className="empty-state">No financial records found.</p>;
  }

  return (
    <div className="table-wrap">
      <table className="data-table">
        <thead>
          <tr>
            <th>Invoice</th>
            <th>Session</th>
            <th>Athlete</th>
            <th>Billing</th>
            <th>Amount</th>
            <th>Status</th>
            <th>Created</th>
          </tr>
        </thead>
        <tbody>
          {ledger.map((invoice) => (
            <tr key={invoice.invoiceId}>
              <td>#{invoice.invoiceId}</td>
              <td>#{invoice.sessionId}</td>
              <td>#{invoice.athleteId}</td>
              <td>{invoice.billingType}</td>
              <td>{formatCurrency(invoice.amount)}</td>
              <td><span className={statusClass(invoice.status)}>{invoice.status}</span></td>
              <td>{formatDateTime(invoice.createdAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default AdminDashboard;
