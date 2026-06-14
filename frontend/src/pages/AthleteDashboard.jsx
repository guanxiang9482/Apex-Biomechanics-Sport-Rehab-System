import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { athlete, auth, notifications } from '../services/api';
import { formatCurrency, formatDateTime, statusClass } from '../utils/format';
import './Dashboard.css';

const initialBooking = {
  therapistId: '',
  facilityId: '1',
  sessionDate: '',
  durationMins: 60,
  sessionType: 'Rehab Assessment',
};

const initialProfile = {
  fullName: '',
  phone: '',
  injuryStatus: '',
  bodyWeightKg: '',
  heightCm: '',
  postureNotes: '',
};

function AthleteDashboard() {
  const navigate = useNavigate();
  const [user] = useState(() => JSON.parse(localStorage.getItem('user')));
  const [activeSection, setActiveSection] = useState('overview');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);
  const [todaySessions, setTodaySessions] = useState([]);
  const [upcomingSessions, setUpcomingSessions] = useState([]);
  const [sessionHistory, setSessionHistory] = useState([]);
  const [metrics, setMetrics] = useState([]);
  const [invoices, setInvoices] = useState([]);
  const [noticeList, setNoticeList] = useState([]);
  const [profileForm, setProfileForm] = useState(initialProfile);
  const [bookingForm, setBookingForm] = useState(initialBooking);
  const [reschedule, setReschedule] = useState({ sessionId: '', newDate: '' });

  const showMessage = (type, text) => setMessage({ type, text });

  const loadDashboard = useCallback(async () => {
    if (!user?.userId) return;
    setLoading(true);
    setMessage(null);

    try {
      const [
        today,
        upcoming,
        history,
        recoveryMetrics,
        athleteInvoices,
        userNotifications,
        profile,
      ] = await Promise.all([
        athlete.getTodaySessions(),
        athlete.getUpcomingSessions(user.userId),
        athlete.getSessionHistory(user.userId),
        athlete.getRecoveryMetrics(user.userId),
        athlete.getInvoices(user.userId),
        notifications.getAll(user.userId),
        athlete.getProfile(user.userId),
      ]);

      setTodaySessions(today);
      setUpcomingSessions(upcoming);
      setSessionHistory(history);
      setMetrics(recoveryMetrics);
      setInvoices(athleteInvoices);
      setNoticeList(userNotifications);
      setProfileForm({
        fullName: profile.fullName || '',
        phone: profile.phone || '',
        injuryStatus: profile.injuryStatus || '',
        bodyWeightKg: profile.bodyWeightKg || '',
        heightCm: profile.heightCm || '',
        postureNotes: profile.postureNotes || '',
      });
    } catch (error) {
      showMessage('error', error.message);
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    if (!user || user.role !== 'ATHLETE') {
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

  const updateBooking = (event) => {
    const { name, value } = event.target;
    setBookingForm((current) => ({ ...current, [name]: value }));
  };

  const updateProfile = (event) => {
    const { name, value } = event.target;
    setProfileForm((current) => ({ ...current, [name]: value }));
  };

  const handleBookSession = async (event) => {
    event.preventDefault();
    try {
      await athlete.bookSession(
        user.userId,
        Number(bookingForm.therapistId),
        Number(bookingForm.facilityId),
        bookingForm.sessionDate,
        Number(bookingForm.durationMins),
        bookingForm.sessionType,
      );
      setBookingForm(initialBooking);
      showMessage('success', 'Session booked successfully.');
      await loadDashboard();
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleCancelSession = async (sessionId) => {
    try {
      await athlete.cancelSession(sessionId);
      showMessage('success', 'Session cancelled successfully.');
      await loadDashboard();
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleReschedule = async (event) => {
    event.preventDefault();
    try {
      await athlete.rescheduleSession(Number(reschedule.sessionId), reschedule.newDate);
      setReschedule({ sessionId: '', newDate: '' });
      showMessage('success', 'Session rescheduled successfully.');
      await loadDashboard();
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleProfileSubmit = async (event) => {
    event.preventDefault();

    const payload = {
      fullName: profileForm.fullName,
      phone: profileForm.phone,
      injuryStatus: profileForm.injuryStatus,
      postureNotes: profileForm.postureNotes,
    };

    if (profileForm.bodyWeightKg) payload.bodyWeightKg = Number(profileForm.bodyWeightKg);
    if (profileForm.heightCm) payload.heightCm = Number(profileForm.heightCm);

    try {
      await athlete.updateProfile(user.userId, payload);
      showMessage('success', 'Profile updated successfully.');
      await loadDashboard();
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

  const unreadCount = noticeList.filter((item) => item.is_read === false || item.is_read === 0).length;
  const paidTotal = invoices
    .filter((invoice) => invoice.status === 'PAID')
    .reduce((sum, invoice) => sum + Number(invoice.amount || 0), 0);

  const sectionTitle = {
    overview: 'Athlete Overview',
    sessions: 'Sessions',
    profile: 'Profile',
    metrics: 'Recovery Metrics',
    invoices: 'Invoices',
    notifications: 'Notifications',
  }[activeSection];

  return (
    <div className="dashboard athlete-dashboard">
      <header className="athlete-topbar">
        <div className="athlete-brand">
          <span>APEX</span>
          <div>
            <strong>Client Recovery</strong>
            <small>Sessions & Performance</small>
          </div>
        </div>

        <nav className="athlete-nav" aria-label="Client sections">
          <button className={activeSection === 'overview' ? 'active' : ''} onClick={() => setActiveSection('overview')}>Overview</button>
          <button className={activeSection === 'sessions' ? 'active' : ''} onClick={() => setActiveSection('sessions')}>Sessions</button>
          <button className={activeSection === 'profile' ? 'active' : ''} onClick={() => setActiveSection('profile')}>Profile</button>
          <button className={activeSection === 'metrics' ? 'active' : ''} onClick={() => setActiveSection('metrics')}>Metrics</button>
          <button className={activeSection === 'invoices' ? 'active' : ''} onClick={() => setActiveSection('invoices')}>Invoices</button>
          <button className={activeSection === 'notifications' ? 'active' : ''} onClick={() => setActiveSection('notifications')}>Notifications</button>
        </nav>

        <div className="athlete-topbar-actions">
          <button className="btn-secondary" type="button" onClick={loadDashboard}>Refresh</button>
          <button className="logout-btn athlete-logout" onClick={handleLogout}>Logout</button>
        </div>
      </header>

      <main className="main-content athlete-main">
        <header className="content-header athlete-hero">
          <div>
            <p className="eyebrow">Athlete Portal</p>
            <h1>{sectionTitle}</h1>
            <p>Welcome, {user?.username}. Track sessions, recovery data, invoices, and clinic notifications.</p>
          </div>
          <div className="athlete-hero-panel" aria-label="Client quick summary">
            <span>Recovery Hub</span>
            <strong>{sectionTitle}</strong>
            <small>Client Dashboard</small>
          </div>
        </header>

        {message && <div className={`dashboard-message ${message.type}`}>{message.text}</div>}
        {loading && <div className="dashboard-message">Loading latest data...</div>}

        {activeSection === 'overview' && (
          <section className="section section-stack">
            <div className="stat-grid">
              <article className="stat-card">
                <span>Today</span>
                <strong>{todaySessions.length}</strong>
                <p>scheduled sessions</p>
              </article>
              <article className="stat-card">
                <span>Upcoming</span>
                <strong>{upcomingSessions.length}</strong>
                <p>future bookings</p>
              </article>
              <article className="stat-card">
                <span>Metrics</span>
                <strong>{metrics.length}</strong>
                <p>recovery records</p>
              </article>
              <article className="stat-card">
                <span>Unread</span>
                <strong>{unreadCount}</strong>
                <p>notifications</p>
              </article>
            </div>

            <div className="content-split">
              <div className="panel">
                <div className="panel-header">
                  <h2>Today&apos;s Sessions</h2>
                </div>
                <SessionCards sessions={todaySessions} onCancel={handleCancelSession} compact />
              </div>
              <div className="panel">
                <div className="panel-header">
                  <h2>Billing Summary</h2>
                </div>
                <div className="summary-block">
                  <strong>{formatCurrency(paidTotal)}</strong>
                  <span>paid invoice total</span>
                  <p>{invoices.length} invoice records available in your ledger.</p>
                </div>
              </div>
            </div>
          </section>
        )}

        {activeSection === 'sessions' && (
          <section className="section section-stack">
            <div className="panel">
              <div className="panel-header">
                <h2>Book Rehab Session</h2>
                <span>UC7</span>
              </div>
              <form className="form-grid" onSubmit={handleBookSession}>
                <label>
                  Therapist ID
                  <input name="therapistId" type="number" min="1" value={bookingForm.therapistId} onChange={updateBooking} required />
                </label>
                <label>
                  Facility ID
                  <select name="facilityId" value={bookingForm.facilityId} onChange={updateBooking}>
                    <option value="1">1 - Gait Analysis Lab A</option>
                    <option value="2">2 - Strength Assessment Room</option>
                    <option value="3">3 - Recovery Pool</option>
                  </select>
                </label>
                <label>
                  Session Date
                  <input name="sessionDate" type="datetime-local" value={bookingForm.sessionDate} onChange={updateBooking} required />
                </label>
                <label>
                  Duration
                  <input name="durationMins" type="number" min="15" step="15" value={bookingForm.durationMins} onChange={updateBooking} required />
                </label>
                <label className="full-width">
                  Session Type
                  <input name="sessionType" value={bookingForm.sessionType} onChange={updateBooking} required />
                </label>
                <button className="btn-primary compact" type="submit">Book Session</button>
              </form>
            </div>

            <div className="content-split">
              <div className="panel">
                <div className="panel-header">
                  <h2>Upcoming Sessions</h2>
                  <span>{upcomingSessions.length}</span>
                </div>
                <SessionCards sessions={upcomingSessions} onCancel={handleCancelSession} onPickReschedule={(sessionId) => setReschedule((current) => ({ ...current, sessionId: String(sessionId) }))} />
              </div>
              <div className="panel">
                <div className="panel-header">
                  <h2>Reschedule</h2>
                  <span>UC9</span>
                </div>
                <form className="form-grid single-column" onSubmit={handleReschedule}>
                  <label>
                    Session ID
                    <input type="number" min="1" value={reschedule.sessionId} onChange={(event) => setReschedule((current) => ({ ...current, sessionId: event.target.value }))} required />
                  </label>
                  <label>
                    New Date
                    <input type="datetime-local" value={reschedule.newDate} onChange={(event) => setReschedule((current) => ({ ...current, newDate: event.target.value }))} required />
                  </label>
                  <button className="btn-primary compact" type="submit">Save New Date</button>
                </form>
              </div>
            </div>

            <div className="panel">
              <div className="panel-header">
                <h2>Session History</h2>
                <span>UC8</span>
              </div>
              <SessionTable sessions={sessionHistory} />
            </div>
          </section>
        )}

        {activeSection === 'profile' && (
          <section className="section">
            <form className="panel form-grid" onSubmit={handleProfileSubmit}>
              <label>
                Full Name
                <input name="fullName" value={profileForm.fullName} onChange={updateProfile} required />
              </label>
              <label>
                Phone
                <input name="phone" value={profileForm.phone} onChange={updateProfile} />
              </label>
              <label>
                Injury Status
                <input name="injuryStatus" value={profileForm.injuryStatus} onChange={updateProfile} />
              </label>
              <label>
                Body Weight (kg)
                <input name="bodyWeightKg" type="number" min="1" step="0.1" value={profileForm.bodyWeightKg} onChange={updateProfile} />
              </label>
              <label>
                Height (cm)
                <input name="heightCm" type="number" min="1" step="0.1" value={profileForm.heightCm} onChange={updateProfile} />
              </label>
              <label className="full-width">
                Posture Notes
                <textarea name="postureNotes" rows="5" value={profileForm.postureNotes} onChange={updateProfile} />
              </label>
              <button className="btn-primary compact" type="submit">Update Profile</button>
            </form>
          </section>
        )}

        {activeSection === 'metrics' && (
          <section className="section">
            {metrics.length === 0 ? (
              <p className="empty-state">No recovery metrics available yet.</p>
            ) : (
              <div className="metric-grid">
                {metrics.map((record) => (
                  <article className="metric-card" key={record.recordId}>
                    <div><span>Jump Power</span><strong>{record.jumpPower}</strong></div>
                    <div><span>Joint Mobility</span><strong>{record.jointMobility}</strong></div>
                    <div><span>Posture Score</span><strong>{record.postureScore}</strong></div>
                    <p>{record.notes || 'No notes recorded.'}</p>
                    <small>{formatDateTime(record.recordedAt)}</small>
                  </article>
                ))}
              </div>
            )}
          </section>
        )}

        {activeSection === 'invoices' && (
          <section className="section">
            <InvoiceTable invoices={invoices} />
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

function SessionCards({ sessions, onCancel, onPickReschedule, compact = false }) {
  if (sessions.length === 0) {
    return <p className="empty-state">No sessions found.</p>;
  }

  return (
    <div className={compact ? 'card-list' : 'card-grid'}>
      {sessions.map((session) => (
        <article className="card" key={session.sessionId}>
          <div className="card-topline">
            <h3>{session.sessionType || 'Rehab Session'}</h3>
            <span className={statusClass(session.status)}>{session.status}</span>
          </div>
          <p>{formatDateTime(session.sessionDate)}</p>
          <p>Therapist #{session.therapistId || 'Unassigned'} · Facility #{session.facilityId || '-'}</p>
          <p>{session.durationMins} minutes</p>
          <div className="card-actions">
            {onPickReschedule && <button className="btn-secondary" type="button" onClick={() => onPickReschedule(session.sessionId)}>Reschedule</button>}
            {onCancel && session.status !== 'CANCELLED' && <button className="btn-danger" type="button" onClick={() => onCancel(session.sessionId)}>Cancel</button>}
          </div>
        </article>
      ))}
    </div>
  );
}

function SessionTable({ sessions }) {
  if (sessions.length === 0) {
    return <p className="empty-state">No previous session records found.</p>;
  }

  return (
    <div className="table-wrap">
      <table className="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Date</th>
            <th>Type</th>
            <th>Therapist</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {sessions.map((session) => (
            <tr key={session.sessionId}>
              <td>#{session.sessionId}</td>
              <td>{formatDateTime(session.sessionDate)}</td>
              <td>{session.sessionType || 'Session'}</td>
              <td>#{session.therapistId || '-'}</td>
              <td><span className={statusClass(session.status)}>{session.status}</span></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function InvoiceTable({ invoices }) {
  if (invoices.length === 0) {
    return <p className="empty-state">No financial records found.</p>;
  }

  return (
    <div className="table-wrap">
      <table className="data-table">
        <thead>
          <tr>
            <th>Invoice</th>
            <th>Session</th>
            <th>Billing</th>
            <th>Amount</th>
            <th>Status</th>
            <th>Created</th>
          </tr>
        </thead>
        <tbody>
          {invoices.map((invoice) => (
            <tr key={invoice.invoiceId}>
              <td>#{invoice.invoiceId}</td>
              <td>#{invoice.sessionId}</td>
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

export default AthleteDashboard;
