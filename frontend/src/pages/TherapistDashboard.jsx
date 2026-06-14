import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { auth, notifications, therapist } from '../services/api';
import { formatDateTime, statusClass } from '../utils/format';
import './Dashboard.css';

const initialMetricForm = {
  sessionId: '',
  jumpPower: '',
  jointMobility: '',
  postureScore: '',
  notes: '',
};

const initialReportForm = {
  athleteId: '',
  summary: '',
};

function TherapistDashboard() {
  const navigate = useNavigate();
  const [user] = useState(() => JSON.parse(localStorage.getItem('user')));
  const [activeSection, setActiveSection] = useState('roster');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);
  const [roster, setRoster] = useState([]);
  const [records, setRecords] = useState([]);
  const [reports, setReports] = useState([]);
  const [noticeList, setNoticeList] = useState([]);
  const [metricForm, setMetricForm] = useState(initialMetricForm);
  const [recordLookupId, setRecordLookupId] = useState('');
  const [reportLookupId, setReportLookupId] = useState('');
  const [reportForm, setReportForm] = useState(initialReportForm);

  const showMessage = (type, text) => setMessage({ type, text });

  const loadDashboard = useCallback(async () => {
    if (!user?.userId) return;
    setLoading(true);
    setMessage(null);

    try {
      const [todayRoster, userNotifications] = await Promise.all([
        therapist.getTodayRoster(user.userId),
        notifications.getAll(user.userId),
      ]);
      setRoster(todayRoster);
      setNoticeList(userNotifications);
    } catch (error) {
      showMessage('error', error.message);
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    if (!user || user.role !== 'THERAPIST') {
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

  const updateMetricForm = (event) => {
    const { name, value } = event.target;
    setMetricForm((current) => ({ ...current, [name]: value }));
  };

  const updateReportForm = (event) => {
    const { name, value } = event.target;
    setReportForm((current) => ({ ...current, [name]: value }));
  };

  const handleStatusUpdate = async (sessionId, status) => {
    try {
      await therapist.updateSessionStatus(sessionId, status);
      showMessage('success', `Session status updated to ${status}.`);
      await loadDashboard();
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleMetricSubmit = async (event) => {
    event.preventDefault();
    try {
      await therapist.logBiomechanicalData(
        Number(metricForm.sessionId),
        Number(metricForm.jumpPower),
        Number(metricForm.jointMobility),
        Number(metricForm.postureScore),
        metricForm.notes,
      );
      setMetricForm(initialMetricForm);
      showMessage('success', 'Biomechanical data logged successfully.');
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleRecordLookup = async (event) => {
    event.preventDefault();
    try {
      setRecords(await therapist.getBiomechanicalsBySession(Number(recordLookupId)));
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleReportSubmit = async (event) => {
    event.preventDefault();
    try {
      await therapist.generateReport(Number(reportForm.athleteId), user.userId, reportForm.summary);
      setReportForm(initialReportForm);
      showMessage('success', 'Clinical report generated successfully.');
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleReportLookup = async (event) => {
    event.preventDefault();
    try {
      setReports(await therapist.getAthleteReports(Number(reportLookupId)));
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

  const sectionTitle = {
    roster: 'Daily Roster & Labs',
    biomechanics: 'Biomechanical Data',
    reports: 'Clinical Reports',
    notifications: 'Notifications',
  }[activeSection];

  return (
    <div className="dashboard therapist-dashboard">
      <header className="therapist-topbar">
        <div className="therapist-brand">
          <span>APEX</span>
          <div>
            <strong>Therapist Workspace</strong>
            <small>Movement Lab & Recovery</small>
          </div>
        </div>

        <nav className="therapist-nav" aria-label="Therapist sections">
          <button className={activeSection === 'roster' ? 'active' : ''} onClick={() => setActiveSection('roster')}>Roster</button>
          <button className={activeSection === 'biomechanics' ? 'active' : ''} onClick={() => setActiveSection('biomechanics')}>Biomechanics</button>
          <button className={activeSection === 'reports' ? 'active' : ''} onClick={() => setActiveSection('reports')}>Reports</button>
          <button className={activeSection === 'notifications' ? 'active' : ''} onClick={() => setActiveSection('notifications')}>Notifications</button>
        </nav>

        <div className="therapist-topbar-actions">
          <button className="btn-secondary" type="button" onClick={loadDashboard}>Refresh</button>
          <button className="logout-btn therapist-logout" onClick={handleLogout}>Logout</button>
        </div>
      </header>

      <main className="main-content therapist-main">
        <header className="content-header therapist-hero">
          <div>
            <p className="eyebrow">Therapist Portal</p>
            <h1>{sectionTitle}</h1>
            <p>Welcome, {user?.username}. Review roster flow, capture movement metrics, and prepare clinical reports.</p>
          </div>
          <div className="therapist-hero-panel" aria-label="Therapist quick summary">
            <span>Clinical Focus</span>
            <strong>{sectionTitle}</strong>
            <small>Session Workflow</small>
          </div>
        </header>

        {message && <div className={`dashboard-message ${message.type}`}>{message.text}</div>}
        {loading && <div className="dashboard-message">Loading latest data...</div>}

        {activeSection === 'roster' && (
          <section className="section section-stack">
            <div className="stat-grid">
              <article className="stat-card">
                <span>Today</span>
                <strong>{roster.length}</strong>
                <p>assigned sessions</p>
              </article>
              <article className="stat-card">
                <span>Completed</span>
                <strong>{roster.filter((item) => item.status === 'COMPLETED').length}</strong>
                <p>finished today</p>
              </article>
              <article className="stat-card">
                <span>Follow Up</span>
                <strong>{roster.filter((item) => item.status === 'PENDING_FOLLOWUP').length}</strong>
                <p>needs attention</p>
              </article>
            </div>

            {roster.length === 0 ? (
              <p className="empty-state">No sessions assigned for today.</p>
            ) : (
              <div className="card-grid">
                {roster.map((session) => (
                  <article className="card" key={session.sessionId}>
                    <div className="card-topline">
                      <h3>{session.sessionType || 'Rehab Session'}</h3>
                      <span className={statusClass(session.status)}>{session.status}</span>
                    </div>
                    <p>Athlete #{session.athleteId}</p>
                    <p>{formatDateTime(session.sessionDate)}</p>
                    <p>Facility #{session.facilityId || '-'} · {session.durationMins} minutes</p>
                    <div className="card-actions">
                      <button className="btn-secondary" type="button" onClick={() => handleStatusUpdate(session.sessionId, 'COMPLETED')}>Complete</button>
                      <button className="btn-secondary" type="button" onClick={() => handleStatusUpdate(session.sessionId, 'PENDING_FOLLOWUP')}>Follow Up</button>
                      <button className="btn-danger" type="button" onClick={() => handleStatusUpdate(session.sessionId, 'CANCELLED')}>Cancel</button>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </section>
        )}

        {activeSection === 'biomechanics' && (
          <section className="section section-stack">
            <form className="panel form-grid" onSubmit={handleMetricSubmit}>
              <label>
                Session ID
                <input name="sessionId" type="number" min="1" value={metricForm.sessionId} onChange={updateMetricForm} required />
              </label>
              <label>
                Jump Power
                <input name="jumpPower" type="number" min="0" step="0.1" value={metricForm.jumpPower} onChange={updateMetricForm} required />
              </label>
              <label>
                Joint Mobility
                <input name="jointMobility" type="number" min="0" step="0.1" value={metricForm.jointMobility} onChange={updateMetricForm} required />
              </label>
              <label>
                Posture Score
                <input name="postureScore" type="number" min="0" step="0.1" value={metricForm.postureScore} onChange={updateMetricForm} required />
              </label>
              <label className="full-width">
                Treatment Notes
                <textarea name="notes" rows="4" value={metricForm.notes} onChange={updateMetricForm} />
              </label>
              <button className="btn-primary compact" type="submit">Log Biomechanical Data</button>
            </form>

            <div className="panel">
              <div className="panel-header">
                <h2>View Session Records</h2>
                <span>UC12</span>
              </div>
              <form className="form-inline" onSubmit={handleRecordLookup}>
                <input type="number" min="1" placeholder="Session ID" value={recordLookupId} onChange={(event) => setRecordLookupId(event.target.value)} required />
                <button className="btn-secondary" type="submit">Load Records</button>
              </form>
              {records.length === 0 ? (
                <p className="empty-state">No biomechanical records loaded.</p>
              ) : (
                <div className="metric-grid">
                  {records.map((record) => (
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
            </div>
          </section>
        )}

        {activeSection === 'reports' && (
          <section className="section section-stack">
            <form className="panel form-grid single-column" onSubmit={handleReportSubmit}>
              <label>
                Athlete ID
                <input name="athleteId" type="number" min="1" value={reportForm.athleteId} onChange={updateReportForm} required />
              </label>
              <label>
                Report Summary
                <textarea name="summary" rows="6" value={reportForm.summary} onChange={updateReportForm} required />
              </label>
              <button className="btn-primary compact" type="submit">Generate Report</button>
            </form>

            <div className="panel">
              <div className="panel-header">
                <h2>Report History</h2>
                <span>UC14</span>
              </div>
              <form className="form-inline" onSubmit={handleReportLookup}>
                <input type="number" min="1" placeholder="Athlete ID" value={reportLookupId} onChange={(event) => setReportLookupId(event.target.value)} required />
                <button className="btn-secondary" type="submit">Load Reports</button>
              </form>
              {reports.length === 0 ? (
                <p className="empty-state">No report data loaded.</p>
              ) : (
                <div className="card-list">
                  {reports.map((report) => (
                    <article className="card compact-card" key={report.reportId}>
                      <div className="card-topline">
                        <h3>Report #{report.reportId}</h3>
                        <span className={statusClass(report.status)}>{report.status}</span>
                      </div>
                      <p>{report.summary}</p>
                      <small>{formatDateTime(report.reportDate)}</small>
                    </article>
                  ))}
                </div>
              )}
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

export default TherapistDashboard;
