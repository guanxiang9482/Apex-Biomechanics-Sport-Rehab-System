import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { auth, notifications, therapist } from '../services/api';
import { formatDateTime, statusClass } from '../utils/format';
import { getNotificationId, getNotificationMessage, getNotificationTimestamp, isNotificationRead } from '../utils/notifications';
import './Dashboard.css';

const initialMetricForm = {
  athleteName: '',
  sessionId: '',
  jumpPower: '',
  jointMobility: '',
  postureScore: '',
  notes: '',
};

const initialReportForm = {
  athleteName: '',
  reportType: 'Progress Review',
  description: '',
};

function TherapistDashboard() {
  const navigate = useNavigate();
  const [user] = useState(() => JSON.parse(localStorage.getItem('user')));
  const [activeSection, setActiveSection] = useState('roster');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);
  const [roster, setRoster] = useState([]);
  const [sessionList, setSessionList] = useState([]);
  const [records, setRecords] = useState([]);
  const [reports, setReports] = useState([]);
  const [therapistProfile, setTherapistProfile] = useState(null);
  const [athletes, setAthletes] = useState([]);
  const [noticeList, setNoticeList] = useState([]);
  const [metricForm, setMetricForm] = useState(initialMetricForm);
  const [recordLookupId, setRecordLookupId] = useState('');
  const [reportForm, setReportForm] = useState(initialReportForm);

  const showMessage = (type, text) => setMessage({ type, text });

  const loadDashboard = useCallback(async () => {
    if (!user?.userId) return;
    setLoading(true);
    setMessage(null);

    try {
      const profile = await therapist.getProfileByUserId(user.userId);
      const [todayRoster, sessionData, athleteData, userNotifications] = await Promise.all([
        therapist.getTodayRoster(profile.therapistId),
        therapist.getSessions(profile.therapistId),
        therapist.getAssignedAthletes(profile.therapistId),
        notifications.getAll(user.userId),
      ]);
      setTherapistProfile(profile);
      setRoster(todayRoster);
      setSessionList(sessionData);
      setAthletes(athleteData);
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
    setMetricForm((current) => ({
      ...current,
      [name]: value,
      ...(name === 'athleteName' ? { sessionId: '' } : {}),
    }));
  };

  const updateReportForm = (event) => {
    const { name, value } = event.target;
    setReportForm((current) => ({ ...current, [name]: value }));
  };

  const handleStatusUpdate = async (sessionId, status) => {
    if (!therapistProfile?.therapistId) {
      showMessage('error', 'Therapist profile is still loading. Please try again.');
      return;
    }
    try {
      await therapist.updateSessionStatus(sessionId, therapistProfile.therapistId, status);
      showMessage('success', `Session status updated to ${status}.`);
      await loadDashboard();
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleMetricSubmit = async (event) => {
    event.preventDefault();
    if (!therapistProfile?.therapistId) {
      showMessage('error', 'Therapist profile is still loading. Please try again.');
      return;
    }
    const selectedAthlete = findAthleteByName(athletes, metricForm.athleteName);
    if (!selectedAthlete) {
      showMessage('error', 'Please choose an athlete from the name suggestions.');
      return;
    }
    try {
      await therapist.logBiomechanicalData(
        selectedAthlete.athleteId,
        therapistProfile.therapistId,
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
    if (!therapistProfile?.therapistId) {
      showMessage('error', 'Therapist profile is still loading. Please try again.');
      return;
    }
    const selectedAthlete = findAthleteByName(athletes, reportForm.athleteName);
    if (!selectedAthlete) {
      showMessage('error', 'Please choose an athlete from the name suggestions.');
      return;
    }
    try {
      await therapist.generateReport(
        selectedAthlete.athleteId,
        therapistProfile.therapistId,
        reportForm.reportType,
        reportForm.description,
      );
      setReportForm(initialReportForm);
      showMessage('success', 'Clinical report generated successfully.');
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleReportLookup = async (event) => {
    event.preventDefault();
    if (!therapistProfile?.therapistId) {
      showMessage('error', 'Therapist profile is still loading. Please try again.');
      return;
    }
    try {
      setReports(await therapist.getTherapistReports(therapistProfile.therapistId));
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

      <datalist id="therapist-athletes">
        {athletes.map((athleteItem) => (
          <option value={formatAthleteName(athleteItem)} key={athleteItem.athleteId} />
        ))}
      </datalist>

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
                    <p>{formatSessionAthleteName(session, athletes)}</p>
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

            <div className="panel">
              <div className="panel-header">
                <h2>All Assigned Sessions</h2>
                <span>{sessionList.length}</span>
              </div>
              {sessionList.length === 0 ? (
                <p className="empty-state">No assigned sessions found.</p>
              ) : (
                <div className="card-grid">
                  {sessionList.map((session) => (
                    <article className="card" key={session.sessionId}>
                      <div className="card-topline">
                        <h3>{session.sessionType || 'Rehab Session'}</h3>
                        <span className={statusClass(session.status)}>{session.status}</span>
                      </div>
                      <p>{formatSessionAthleteName(session, athletes)}</p>
                      <p>{formatDateTime(session.sessionDate)}</p>
                      <p>Facility #{session.facilityId || '-'} - {session.durationMins} minutes</p>
                      <div className="card-actions">
                        <button className="btn-secondary" type="button" onClick={() => handleStatusUpdate(session.sessionId, 'COMPLETED')}>Complete</button>
                        <button className="btn-secondary" type="button" onClick={() => handleStatusUpdate(session.sessionId, 'PENDING_FOLLOWUP')}>Follow Up</button>
                        <button className="btn-danger" type="button" onClick={() => handleStatusUpdate(session.sessionId, 'CANCELLED')}>Cancel</button>
                      </div>
                    </article>
                  ))}
                </div>
              )}
            </div>
          </section>
        )}

        {activeSection === 'biomechanics' && (
          <section className="section section-stack">
              <form className="panel form-grid" onSubmit={handleMetricSubmit}>
              <label>
                Athlete Name
                <input name="athleteName" list="therapist-athletes" value={metricForm.athleteName} onChange={updateMetricForm} placeholder="Start typing athlete name" required />
              </label>
              <label>
                Session
                <select name="sessionId" value={metricForm.sessionId} onChange={updateMetricForm} required>
                  <option value="">Choose session</option>
                  {getSessionsForAthlete(sessionList, athletes, metricForm.athleteName).map((session) => (
                    <option value={session.sessionId} key={session.sessionId}>
                      {formatSessionOption(session, athletes)}
                    </option>
                  ))}
                </select>
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
                <select value={recordLookupId} onChange={(event) => setRecordLookupId(event.target.value)} required>
                  <option value="">Choose session</option>
                  {sessionList.map((session) => (
                    <option value={session.sessionId} key={session.sessionId}>
                      {formatSessionOption(session, athletes)}
                    </option>
                  ))}
                </select>
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
                      <p>{record.treatmentNote || record.notes || 'No notes recorded.'}</p>
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
                Athlete Name
                <input name="athleteName" list="therapist-athletes" value={reportForm.athleteName} onChange={updateReportForm} placeholder="Start typing athlete name" required />
              </label>
              <label>
                Report Type
                <input name="reportType" value={reportForm.reportType} onChange={updateReportForm} required />
              </label>
              <label>
                Description
                <textarea name="description" rows="6" value={reportForm.description} onChange={updateReportForm} required />
              </label>
              <button className="btn-primary compact" type="submit">Generate Report</button>
            </form>

            <div className="panel">
              <div className="panel-header">
                <h2>Report History</h2>
                <span>UC14</span>
              </div>
              <form className="form-inline" onSubmit={handleReportLookup}>
                <button className="btn-secondary" type="submit">Load My Reports</button>
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
                      <p>{report.reportType}</p>
                      <p>{report.description}</p>
                      <small>{formatDateTime(report.submittedAt)}</small>
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
                  const id = getNotificationId(notice);
                  const isRead = isNotificationRead(notice);
                  return (
                    <article className={`card compact-card ${isRead ? '' : 'highlight-card'}`} key={id ?? `${getNotificationMessage(notice)}-${getNotificationTimestamp(notice)}`}>
                      <h3>{isRead ? 'Read' : 'Unread'} Notification</h3>
                      <p>{getNotificationMessage(notice)}</p>
                      <small>{formatDateTime(getNotificationTimestamp(notice))}</small>
                      {!isRead && id && <button className="btn-secondary" type="button" onClick={() => handleReadNotification(id)}>Mark as Read</button>}
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

function formatAthleteName(athlete) {
  if (!athlete) return '';
  const name = athlete.fullname || athlete.fullName || athlete.username || 'Unnamed athlete';
  return athlete.username && athlete.username !== name ? `${name} (${athlete.username})` : name;
}

function normalizeName(value) {
  return String(value || '').trim().toLowerCase();
}

function findAthleteByName(athletes, typedName) {
  const needle = normalizeName(typedName);
  if (!needle) return null;

  return athletes.find((athlete) => {
    const fullLabel = normalizeName(formatAthleteName(athlete));
    const fullname = normalizeName(athlete.fullname || athlete.fullName);
    const username = normalizeName(athlete.username);
    return needle === fullLabel || needle === fullname || needle === username;
  }) || null;
}

function getSessionsForAthlete(roster, athletes, typedName) {
  if (!normalizeName(typedName)) return [];
  const selectedAthlete = findAthleteByName(athletes, typedName);
  if (!selectedAthlete) return [];
  return roster.filter((session) => session.athleteId === selectedAthlete.athleteId);
}

function formatSessionOption(session, athletes) {
  const athlete = athletes.find((item) => item.athleteId === session.athleteId);
  const athleteName = athlete ? formatAthleteName(athlete) : 'Assigned athlete';
  return `${athleteName} - ${session.sessionType || 'Rehab Session'} - ${formatDateTime(session.sessionDate)}`;
}

function formatSessionAthleteName(session, athletes) {
  const athlete = athletes.find((item) => item.athleteId === session.athleteId);
  return athlete ? formatAthleteName(athlete) : 'Assigned athlete';
}

export default TherapistDashboard;
