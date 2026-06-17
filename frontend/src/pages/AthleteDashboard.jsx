import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { athlete, auth, notifications } from '../services/api';
import { formatCurrency, formatDateTime, statusClass } from '../utils/format';
import { getNotificationId, getNotificationMessage, getNotificationTimestamp, isNotificationRead } from '../utils/notifications';
import './Dashboard.css';

const initialBooking = {
  therapistId: '',
  facilityId: '1',
  sessionDay: '',
  sessionDate: '',
  durationMins: 60,
  sessionType: 'Rehab Assessment',
};

const initialProfile = {
  fullName: '',
  injuryStatus: '',
  sport: '',
  bodyWeightKg: '',
  heightCm: '',
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
  const [athleteProfile, setAthleteProfile] = useState(null);
  const [therapists, setTherapists] = useState([]);
  const [facilities, setFacilities] = useState([]);
  const [availableSlots, setAvailableSlots] = useState([]);
  const [availabilityLoading, setAvailabilityLoading] = useState(false);
  const [rescheduleSlots, setRescheduleSlots] = useState([]);
  const [rescheduleLoading, setRescheduleLoading] = useState(false);
  const [noticeList, setNoticeList] = useState([]);
  const [profileForm, setProfileForm] = useState(initialProfile);
  const [bookingForm, setBookingForm] = useState(initialBooking);
  const [reschedule, setReschedule] = useState({ sessionId: '', sessionDay: '', newDate: '' });

  const showMessage = (type, text) => setMessage({ type, text });

  const loadDashboard = useCallback(async () => {
    if (!user?.userId) return;
    setLoading(true);
    setMessage(null);

    try {
      const [profile, therapistData, facilityData, userNotifications] = await Promise.all([
        athlete.getProfileByUserId(user.userId),
        athlete.getTherapists(),
        athlete.getFacilities(),
        notifications.getAll(user.userId),
      ]);

      const athleteId = profile.athleteId;
      const [today, upcoming, history, recoveryMetrics, athleteInvoices] = await Promise.all([
        athlete.getTodaySessions(athleteId),
        athlete.getUpcomingSessions(athleteId),
        athlete.getSessionHistory(athleteId),
        athlete.getRecoveryMetrics(athleteId),
        athlete.getInvoices(athleteId),
      ]);

      setAthleteProfile(profile);
      setTherapists(therapistData);
      setFacilities(facilityData);
      setTodaySessions(today);
      setUpcomingSessions(upcoming);
      setSessionHistory(history);
      setMetrics(recoveryMetrics);
      setInvoices(athleteInvoices);
      setNoticeList(userNotifications);
      setProfileForm({
        fullName: profile.fullName || profile.fullname || '',
        injuryStatus: profile.injuryStatus || '',
        sport: profile.sport || '',
        bodyWeightKg: profile.bodyWeightKg || '',
        heightCm: profile.heightCm || '',
      });
      setBookingForm((current) => ({
        ...current,
        therapistId: current.therapistId || String(therapistData[0]?.therapistId || ''),
        facilityId: current.facilityId || String(facilityData[0]?.facilityId || '1'),
      }));
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
    setBookingForm((current) => ({
      ...current,
      [name]: value,
      ...(['therapistId', 'facilityId', 'sessionDay', 'durationMins'].includes(name)
        ? { sessionDate: '' }
        : {}),
    }));
  };

  const updateProfile = (event) => {
    const { name, value } = event.target;
    setProfileForm((current) => ({ ...current, [name]: value }));
  };

  const loadAvailableSlots = useCallback(async () => {
    if (!bookingForm.therapistId || !bookingForm.facilityId || !bookingForm.sessionDay) {
      setAvailableSlots([]);
      return;
    }

    setAvailabilityLoading(true);
    try {
      const result = await athlete.getAvailableSlots(
        Number(bookingForm.therapistId),
        Number(bookingForm.facilityId),
        bookingForm.sessionDay,
        Number(bookingForm.durationMins),
      );
      const slots = result.availableSlots || [];
      setAvailableSlots(slots);
      setBookingForm((current) => ({
        ...current,
        sessionDate: slots.includes(current.sessionDate) ? current.sessionDate : (slots[0] || ''),
      }));
    } catch (error) {
      setAvailableSlots([]);
      showMessage('error', error.message);
    } finally {
      setAvailabilityLoading(false);
    }
  }, [bookingForm.durationMins, bookingForm.facilityId, bookingForm.sessionDay, bookingForm.therapistId]);

  useEffect(() => {
    const timer = window.setTimeout(loadAvailableSlots, 0);
    return () => window.clearTimeout(timer);
  }, [loadAvailableSlots]);

  const loadRescheduleSlots = useCallback(async () => {
    if (!reschedule.sessionId || !reschedule.sessionDay) {
      setRescheduleSlots([]);
      return;
    }

    const selectedSession = upcomingSessions.find((session) =>
      session.sessionId === Number(reschedule.sessionId));
    if (!selectedSession) {
      setRescheduleSlots([]);
      return;
    }

    setRescheduleLoading(true);
    try {
      const result = await athlete.getAvailableSlots(
        selectedSession.therapistId,
        selectedSession.facilityId,
        reschedule.sessionDay,
        selectedSession.durationMins || 60,
        selectedSession.sessionId,
      );
      const slots = result.availableSlots || [];
      setRescheduleSlots(slots);
      setReschedule((current) => ({
        ...current,
        newDate: slots.includes(current.newDate) ? current.newDate : (slots[0] || ''),
      }));
    } catch (error) {
      setRescheduleSlots([]);
      showMessage('error', error.message);
    } finally {
      setRescheduleLoading(false);
    }
  }, [reschedule.sessionDay, reschedule.sessionId, upcomingSessions]);

  useEffect(() => {
    const timer = window.setTimeout(loadRescheduleSlots, 0);
    return () => window.clearTimeout(timer);
  }, [loadRescheduleSlots]);

  const handleBookSession = async (event) => {
    event.preventDefault();
    if (!athleteProfile?.athleteId) {
      showMessage('error', 'Athlete profile is still loading. Please try again.');
      return;
    }
    try {
      await athlete.bookSession(
        athleteProfile.athleteId,
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
    if (!athleteProfile?.athleteId) {
      showMessage('error', 'Athlete profile is still loading. Please try again.');
      return;
    }
    try {
      await athlete.cancelSession(sessionId, athleteProfile.athleteId);
      showMessage('success', 'Session cancelled successfully.');
      await loadDashboard();
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleReschedule = async (event) => {
    event.preventDefault();
    if (!athleteProfile?.athleteId) {
      showMessage('error', 'Athlete profile is still loading. Please try again.');
      return;
    }
    try {
      await athlete.rescheduleSession(Number(reschedule.sessionId), athleteProfile.athleteId, reschedule.newDate);
      setReschedule({ sessionId: '', sessionDay: '', newDate: '' });
      showMessage('success', 'Session rescheduled successfully.');
      await loadDashboard();
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleProfileSubmit = async (event) => {
    event.preventDefault();
    if (!athleteProfile?.athleteId) {
      showMessage('error', 'Athlete profile is still loading. Please try again.');
      return;
    }

    const payload = {
      fullName: profileForm.fullName,
      injuryStatus: profileForm.injuryStatus,
      sport: profileForm.sport,
    };

    if (profileForm.bodyWeightKg) payload.bodyWeightKg = Number(profileForm.bodyWeightKg);
    if (profileForm.heightCm) payload.heightCm = Number(profileForm.heightCm);

    try {
      await athlete.updateProfile(athleteProfile.athleteId, payload);
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

  const unreadCount = noticeList.filter((item) => !isNotificationRead(item)).length;
  const paidTotal = invoices
    .filter((invoice) => invoice.status === 'PAID')
    .reduce((sum, invoice) => sum + Number(invoice.finalAmount ?? invoice.amount ?? 0), 0);

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
                <SessionCards sessions={todaySessions} therapists={therapists} facilities={facilities} onCancel={handleCancelSession} compact />
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
                  Therapist
                  <select name="therapistId" value={bookingForm.therapistId} onChange={updateBooking} required>
                    <option value="">Choose therapist</option>
                    {therapists.map((therapistItem) => (
                      <option value={therapistItem.therapistId} key={therapistItem.therapistId}>
                        {therapistItem.fullname || therapistItem.username} - {therapistItem.specialization || 'Physiotherapist'}
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  Facility
                  <select name="facilityId" value={bookingForm.facilityId} onChange={updateBooking}>
                    {facilities.map((facility) => (
                      <option value={facility.facilityId} key={facility.facilityId}>
                        {facility.name} - {facility.location || facility.type}
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  Session Date
                  <input name="sessionDay" type="date" value={bookingForm.sessionDay} onChange={updateBooking} required />
                </label>
                <label>
                  Duration
                  <input name="durationMins" type="number" min="15" step="15" value={bookingForm.durationMins} onChange={updateBooking} required />
                </label>
                <label>
                  Available Time
                  <select name="sessionDate" value={bookingForm.sessionDate} onChange={updateBooking} required>
                    <option value="">{availabilityLoading ? 'Checking times...' : 'Choose time'}</option>
                    {availableSlots.map((slot) => (
                      <option value={slot} key={slot}>{formatDateTime(slot)}</option>
                    ))}
                  </select>
                </label>
                <label className="full-width">
                  Session Type
                  <input name="sessionType" value={bookingForm.sessionType} onChange={updateBooking} required />
                </label>
                <button className="btn-primary compact" type="submit" disabled={!bookingForm.sessionDate}>Book Session</button>
              </form>
              {bookingForm.sessionDay && !availabilityLoading && availableSlots.length === 0 && (
                <p className="empty-state">No available times for this therapist and facility. Try another date or room.</p>
              )}
            </div>

            <div className="content-split">
              <div className="panel">
                <div className="panel-header">
                  <h2>Upcoming Sessions</h2>
                  <span>{upcomingSessions.length}</span>
                </div>
                <SessionCards sessions={upcomingSessions} therapists={therapists} facilities={facilities} onCancel={handleCancelSession} onPickReschedule={(sessionId) => setReschedule((current) => ({ ...current, sessionId: String(sessionId), sessionDay: '', newDate: '' }))} />
              </div>
              <div className="panel">
                <div className="panel-header">
                  <h2>Reschedule</h2>
                  <span>UC9</span>
                </div>
                <form className="form-grid single-column" onSubmit={handleReschedule}>
                  <label>
                    Session
                    <select value={reschedule.sessionId} onChange={(event) => setReschedule((current) => ({ ...current, sessionId: event.target.value, sessionDay: '', newDate: '' }))} required>
                      <option value="">Choose session</option>
                      {upcomingSessions.map((session) => (
                        <option value={session.sessionId} key={session.sessionId}>
                          {session.sessionType || 'Rehab Session'} - {formatDateTime(session.sessionDate)}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label>
                    New Day
                    <input type="date" value={reschedule.sessionDay} onChange={(event) => setReschedule((current) => ({ ...current, sessionDay: event.target.value, newDate: '' }))} required />
                  </label>
                  <label>
                    Available Time
                    <select value={reschedule.newDate} onChange={(event) => setReschedule((current) => ({ ...current, newDate: event.target.value }))} required>
                      <option value="">{rescheduleLoading ? 'Checking times...' : 'Choose time'}</option>
                      {rescheduleSlots.map((slot) => (
                        <option value={slot} key={slot}>{formatDateTime(slot)}</option>
                      ))}
                    </select>
                  </label>
                  <button className="btn-primary compact" type="submit" disabled={!reschedule.newDate}>Save New Date</button>
                </form>
                {reschedule.sessionDay && !rescheduleLoading && rescheduleSlots.length === 0 && (
                  <p className="empty-state">No available times for this session. Try another day.</p>
                )}
              </div>
            </div>

            <div className="panel">
              <div className="panel-header">
                <h2>Session History</h2>
                <span>UC8</span>
              </div>
              <SessionTable sessions={sessionHistory} therapists={therapists} />
            </div>
          </section>
        )}

        {activeSection === 'profile' && (
          <section className="section">
            <form className="panel form-grid" onSubmit={handleProfileSubmit}>
              <label>
                Full Name
                <input name="fullName" value={profileForm.fullName} readOnly />
              </label>
              <label>
                Injury Status
                <input name="injuryStatus" value={profileForm.injuryStatus} onChange={updateProfile} />
              </label>
              <label>
                Sport
                <input name="sport" value={profileForm.sport} onChange={updateProfile} />
              </label>
              <label>
                Body Weight (kg)
                <input name="bodyWeightKg" type="number" min="1" step="0.1" value={profileForm.bodyWeightKg} onChange={updateProfile} />
              </label>
              <label>
                Height (cm)
                <input name="heightCm" type="number" min="1" step="0.1" value={profileForm.heightCm} onChange={updateProfile} />
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
                    <p>{record.treatmentNote || record.notes || 'No notes recorded.'}</p>
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

function SessionCards({ sessions, therapists = [], facilities = [], onCancel, onPickReschedule, compact = false }) {
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
          <p>{getTherapistLabel(therapists, session.therapistId)} - {getFacilityLabel(facilities, session.facilityId)}</p>
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

function SessionTable({ sessions, therapists = [] }) {
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
              <td>{getTherapistLabel(therapists, session.therapistId)}</td>
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
              <td>{formatCurrency(invoice.finalAmount ?? invoice.amount)}</td>
              <td><span className={statusClass(invoice.status)}>{invoice.status}</span></td>
              <td>{formatDateTime(invoice.createdAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function getTherapistLabel(therapists, therapistId) {
  const therapistItem = therapists.find((item) => item.therapistId === therapistId);
  return therapistItem ? (therapistItem.fullname || therapistItem.username) : `Therapist #${therapistId || '-'}`;
}

function getFacilityLabel(facilities, facilityId) {
  const facility = facilities.find((item) => item.facilityId === facilityId);
  return facility ? facility.name : `Facility #${facilityId || '-'}`;
}

export default AthleteDashboard;
