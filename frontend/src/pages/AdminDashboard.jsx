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
  contact: '',
  therapistId: '',
  facilityId: '1',
};

const initialBilling = {
  sessionId: '',
  athleteName: '',
  billingType: 'STANDARD',
};

const initialStaff = {
  username: '',
  password: '',
  email: '',
  fullName: '',
  contact: '',
  role: 'THERAPIST',
};

const fallbackFacilities = [
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
  const [completedSessions, setCompletedSessions] = useState([]);
  const [facilities, setFacilities] = useState(fallbackFacilities);
  const [selectedFacilityId, setSelectedFacilityId] = useState('');
  const [equipment, setEquipment] = useState([]);
  const [therapists, setTherapists] = useState([]);
  const [staff, setStaff] = useState([]);
  const [noticeList, setNoticeList] = useState([]);
  const [admissionForm, setAdmissionForm] = useState(initialAdmission);
  const [billingForm, setBillingForm] = useState(initialBilling);
  const [billingResult, setBillingResult] = useState(null);
  const [staffForm, setStaffForm] = useState(initialStaff);
  const [editingStaff, setEditingStaff] = useState(null);

  const showMessage = (type, text) => setMessage({ type, text });

  const loadDashboard = useCallback(async () => {
    if (!user?.userId) return;
    setLoading(true);
    setMessage(null);

    try {
      const [analyticsData, athleteData, ledgerData, facilityData, therapistData, completedSessionData, staffData, userNotifications] = await Promise.all([
        admin.getAnalytics(),
        admin.getAllAthletes(),
        admin.getFullLedger(),
        admin.getFacilities(),
        admin.getTherapists(),
        admin.getCompletedSessions(),
        admin.getAllStaff(),
        notifications.getAll(user.userId),
      ]);

      setAnalytics(analyticsData);
      setAthletes(athleteData);
      setLedger(ledgerData);
      setCompletedSessions(completedSessionData);
      setFacilities(facilityData.length > 0 ? facilityData : fallbackFacilities);
      setTherapists(therapistData);
      setStaff(staffData);
      setSelectedFacilityId((current) => current || String(facilityData[0]?.facilityId || fallbackFacilities[0].id));
      setAdmissionForm((current) => ({
        ...current,
        therapistId: current.therapistId || String(therapistData[0]?.therapistId || ''),
        facilityId: current.facilityId || String(facilityData[0]?.facilityId || fallbackFacilities[0].id),
      }));
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
    setBillingForm((current) => ({
      ...current,
      [name]: value,
      ...(name === 'athleteName' ? { sessionId: '' } : {}),
    }));
  };

  const updateStaff = (event) => {
    const { name, value } = event.target;
    setStaffForm((current) => ({ ...current, [name]: value }));
  };

  const updateEditingStaff = (event) => {
    const { name, value } = event.target;
    setEditingStaff((current) => ({ ...current, [name]: value }));
  };

  const handleAdmission = async (event) => {
    event.preventDefault();
    try {
      const result = await admin.admitNewAthlete(
        admissionForm.username.trim(),
        admissionForm.password,
        admissionForm.email.trim(),
        admissionForm.fullName.trim(),
        admissionForm.contact.trim(),
        Number(admissionForm.therapistId),
        Number(admissionForm.facilityId),
      );
      setAdmissionForm(initialAdmission);
      showMessage('success', `${result.fullname || result.fullName || admissionForm.fullName} admitted successfully.`);
      await loadDashboard();
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const startEditStaff = (staffMember) => {
    setEditingStaff({
      userId: staffMember.userId,
      role: staffMember.role,
      fullName: staffMember.fullname || staffMember.fullName || staffMember.username || '',
      email: staffMember.email || '',
      contact: staffMember.contact || '',
      specialization: staffMember.specialization || '',
      licenseNumber: staffMember.licenseNumber || '',
    });
  };

  const handleUpdateStaff = async (event) => {
    event.preventDefault();
    if (!editingStaff?.userId) return;

    try {
      await admin.updateStaff(editingStaff.userId, {
        fullName: editingStaff.fullName.trim(),
        email: editingStaff.email.trim(),
        contact: editingStaff.contact.trim(),
        specialization: editingStaff.specialization.trim(),
        licenseNumber: editingStaff.licenseNumber.trim(),
      });
      setEditingStaff(null);
      showMessage('success', 'Staff profile updated successfully.');
      await loadDashboard();
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleBilling = async (event) => {
    event.preventDefault();
    const selectedAthlete = findAthleteByName(athletes, billingForm.athleteName);
    if (!selectedAthlete) {
      showMessage('error', 'Please choose an athlete from the name suggestions.');
      return;
    }
    const selectedSession = completedSessions.find((session) =>
      session.sessionId === Number(billingForm.sessionId));
    if (!selectedSession || selectedSession.athleteId !== selectedAthlete.athleteId) {
      showMessage('error', 'Please choose a completed session for the selected athlete.');
      return;
    }
    try {
      const result = await admin.processBilling(
        Number(billingForm.sessionId),
        selectedAthlete.athleteId,
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
      await admin.addStaff(
        staffForm.username.trim(),
        staffForm.password,
        staffForm.email.trim(),
        staffForm.fullName.trim(),
        staffForm.contact.trim(),
        staffForm.role,
      );
      setStaffForm(initialStaff);
      showMessage('success', `${staffForm.fullName || staffForm.username} staff account created.`);
      await loadDashboard();
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleDeactivateStaff = async (userId) => {
    try {
      await admin.deactivateStaff(userId);
      showMessage('success', 'Staff account deactivated successfully.');
      await loadDashboard();
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleDeleteStaff = async (userId) => {
    try {
      await admin.deleteStaff(userId);
      showMessage('success', 'Staff account deleted successfully.');
      await loadDashboard();
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleFacilityStatus = async (facilityId, status) => {
    try {
      await admin.updateFacilityStatus(facilityId, status);
      showMessage('success', 'Facility status updated successfully.');
      await loadDashboard();
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleLoadEquipment = async (facilityId) => {
    setSelectedFacilityId(String(facilityId));
    try {
      setEquipment(await admin.getEquipmentByFacility(facilityId));
    } catch (error) {
      showMessage('error', error.message);
    }
  };

  const handleEquipmentStatus = async (itemId, status) => {
    try {
      await admin.updateEquipmentStatus(itemId, status);
      showMessage('success', 'Equipment status updated successfully.');
      if (selectedFacilityId) {
        setEquipment(await admin.getEquipmentByFacility(Number(selectedFacilityId)));
      }
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
    .reduce((sum, invoice) => sum + Number(invoice.finalAmount ?? invoice.amount ?? 0), 0);

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

      <datalist id="admin-athletes">
        {athletes.map((athleteProfile) => (
          <option value={formatAthleteName(athleteProfile)} key={athleteProfile.athleteId} />
        ))}
      </datalist>

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
                Contact
                <input name="contact" value={admissionForm.contact} onChange={updateAdmission} placeholder="Phone or emergency contact" />
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
                Assigned Therapist
                <select name="therapistId" value={admissionForm.therapistId} onChange={updateAdmission} required>
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
                <select name="facilityId" value={admissionForm.facilityId} onChange={updateAdmission}>
                  {facilities.map((facility) => (
                    <option value={facility.facilityId ?? facility.id} key={facility.facilityId ?? facility.id}>
                      {facility.name} - {facility.location || facility.type}
                    </option>
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
                <article className="stat-card"><span>Completed Sessions</span><strong>{analytics?.completedSessions ?? 0}</strong></article>
                <article className="stat-card"><span>Cancelled Sessions</span><strong>{analytics?.cancelledSessions ?? 0}</strong></article>
                <article className="stat-card"><span>Scheduled Sessions</span><strong>{analytics?.scheduledSessions ?? 0}</strong></article>
                <article className="stat-card"><span>Available Facilities</span><strong>{analytics?.availableFacilities ?? 0}</strong></article>
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
                        <th>Athlete</th>
                        <th>Username</th>
                        <th>Email</th>
                        <th>Injury</th>
                        <th>Body</th>
                      </tr>
                    </thead>
                    <tbody>
                      {athletes.map((athleteProfile) => (
                        <tr key={athleteProfile.athleteId}>
                          <td>{formatAthleteName(athleteProfile)}</td>
                          <td>{athleteProfile.username}</td>
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
                Completed Session
                <select name="sessionId" value={billingForm.sessionId} onChange={updateBilling} required>
                  <option value="">Choose completed session</option>
                  {getSessionsForAthlete(completedSessions, athletes, billingForm.athleteName).map((session) => (
                    <option value={session.sessionId} key={session.sessionId}>
                      {formatSessionOption(session, athletes)}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Athlete Name
                <input name="athleteName" list="admin-athletes" value={billingForm.athleteName} onChange={updateBilling} placeholder="Start typing athlete name" required />
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
                <strong>{formatCurrency(billingResult.finalAmount)}</strong>
              </div>
            )}

            <div className="panel">
              <div className="panel-header">
                <h2>Financial Ledger</h2>
                <span>UC20</span>
              </div>
              <LedgerTable ledger={ledger} athletes={athletes} />
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
                Contact
                <input name="contact" value={staffForm.contact} onChange={updateStaff} placeholder="Phone or staff contact" />
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

            {editingStaff && (
              <form className="panel form-grid" onSubmit={handleUpdateStaff}>
                <label>
                  Full Name
                  <input name="fullName" value={editingStaff.fullName} onChange={updateEditingStaff} required />
                </label>
                <label>
                  Email
                  <input name="email" type="email" value={editingStaff.email} onChange={updateEditingStaff} required />
                </label>
                <label>
                  Contact
                  <input name="contact" value={editingStaff.contact} onChange={updateEditingStaff} />
                </label>
                {editingStaff.role === 'THERAPIST' && (
                  <>
                    <label>
                      Specialization
                      <input name="specialization" value={editingStaff.specialization} onChange={updateEditingStaff} />
                    </label>
                    <label>
                      License Number
                      <input name="licenseNumber" value={editingStaff.licenseNumber} onChange={updateEditingStaff} />
                    </label>
                  </>
                )}
                <div className="card-actions">
                  <button className="btn-primary compact" type="submit">Save Staff</button>
                  <button className="btn-secondary" type="button" onClick={() => setEditingStaff(null)}>Cancel</button>
                </div>
              </form>
            )}

            <div className="panel">
              <div className="panel-header">
                <h2>Staff Directory</h2>
                <span>{staff.length}</span>
              </div>
              {staff.length === 0 ? (
                <p className="empty-state">No staff accounts found.</p>
              ) : (
                <div className="table-wrap">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Staff</th>
                        <th>Role</th>
                        <th>Email</th>
                        <th>Contact</th>
                        <th>Status</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {staff.map((staffMember) => (
                        <tr key={staffMember.userId}>
                          <td>{staffMember.fullname || staffMember.fullName || staffMember.username}</td>
                          <td>{staffMember.role}</td>
                          <td>{staffMember.email}</td>
                          <td>{staffMember.contact || '-'}</td>
                          <td>{staffMember.active === false || staffMember.isActive === false ? 'Inactive' : 'Active'}</td>
                          <td>
                            {staffMember.userId === user.userId ? (
                              <span className="muted-text">Current admin</span>
                            ) : (
                              <>
                                <button className="btn-secondary" type="button" onClick={() => startEditStaff(staffMember)}>Edit</button>
                                <button className="btn-secondary" type="button" onClick={() => handleDeactivateStaff(staffMember.userId)}>Deactivate</button>
                                <button className="btn-danger" type="button" onClick={() => handleDeleteStaff(staffMember.userId)}>Delete</button>
                              </>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
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
                  <article className="card" key={facility.facilityId ?? facility.id}>
                    <div className="card-topline">
                      <h3>{facility.name}</h3>
                      <span className="status-badge status-completed">{facility.status}</span>
                    </div>
                    <p>Facility #{facility.facilityId ?? facility.id}</p>
                    <p>{facility.type}</p>
                    {facility.location && <p>{facility.location}</p>}
                    <div className="card-actions">
                      <button className="btn-secondary" type="button" onClick={() => handleFacilityStatus(facility.facilityId ?? facility.id, 'AVAILABLE')}>Available</button>
                      <button className="btn-secondary" type="button" onClick={() => handleFacilityStatus(facility.facilityId ?? facility.id, 'RESERVED')}>Reserve</button>
                      <button className="btn-danger" type="button" onClick={() => handleFacilityStatus(facility.facilityId ?? facility.id, 'MAINTENANCE')}>Maintenance</button>
                    </div>
                    <button className="btn-secondary" type="button" onClick={() => handleLoadEquipment(facility.facilityId ?? facility.id)}>View Equipment</button>
                  </article>
                ))}
              </div>
              <div className="panel-header">
                <h2>Equipment</h2>
                <span>{equipment.length}</span>
              </div>
              {equipment.length === 0 ? (
                <p className="empty-state">Select a facility to view equipment.</p>
              ) : (
                <div className="card-grid">
                  {equipment.map((item) => (
                    <article className="card" key={item.itemId ?? item.equipmentId}>
                      <div className="card-topline">
                        <h3>{item.itemName || item.name}</h3>
                        <span className={statusClass(item.itemStatus || item.status)}>{item.itemStatus || item.status}</span>
                      </div>
                      <p>Quantity: {item.itemQuantity ?? '-'}</p>
                      <div className="card-actions">
                        <button className="btn-secondary" type="button" onClick={() => handleEquipmentStatus(item.itemId ?? item.equipmentId, 'AVAILABLE')}>Available</button>
                        <button className="btn-secondary" type="button" onClick={() => handleEquipmentStatus(item.itemId ?? item.equipmentId, 'IN_USE')}>In Use</button>
                        <button className="btn-danger" type="button" onClick={() => handleEquipmentStatus(item.itemId ?? item.equipmentId, 'MAINTENANCE')}>Maintenance</button>
                      </div>
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

function LedgerTable({ ledger, athletes }) {
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
              <td>{formatAthleteName(athletes.find((athlete) => athlete.athleteId === invoice.athleteId)) || 'Athlete record'}</td>
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

function getSessionsForAthlete(sessions, athletes, typedName) {
  if (!normalizeName(typedName)) return [];
  const selectedAthlete = findAthleteByName(athletes, typedName);
  if (!selectedAthlete) return [];
  return sessions.filter((session) =>
    session.athleteId === selectedAthlete.athleteId);
}

function formatSessionOption(session, athletes) {
  const athlete = athletes.find((item) => item.athleteId === session.athleteId);
  const athleteName = athlete ? formatAthleteName(athlete) : 'Athlete record';
  return `${athleteName} - ${session.sessionType || 'Session'} - ${formatDateTime(session.sessionDate)}`;
}

export default AdminDashboard;
