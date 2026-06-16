const BASE_URL = '/api';

function getStoredUserId() {
  try {
    const user = JSON.parse(localStorage.getItem('user'));
    return user?.userId;
  } catch {
    return null;
  }
}

function getAuthHeaders() {
  const userId = getStoredUserId();
  return {
    'Content-Type': 'application/json',
    ...(userId ? { 'X-User-Id': String(userId) } : {}),
  };
}

async function request(endpoint, method = 'GET', body = null) {
  const options = {
    method,
    headers: getAuthHeaders(),
  };

  if (body) {
    options.body = JSON.stringify(body);
  }

  const response = await fetch(`${BASE_URL}${endpoint}`, options);
  const text = await response.text();
  const data = text ? JSON.parse(text) : {};

  if (!response.ok) {
    throw new Error(data.error || data.message || 'Request failed');
  }

  return data;
}

export const auth = {
  register: (username, password, email, fullName) =>
    request('/auth/register', 'POST', { username, password, email, fullName }),
  login: (username, password) =>
    request('/auth/login', 'POST', { username, password }),
  logout: (userId) =>
    request('/auth/logout', 'POST', { userId }),
  resetPassword: (username, email, newPassword) =>
    request('/auth/reset-password', 'POST', { username, email, newPassword }),
};

export const athlete = {
  getProfile: (athleteId) =>
    request(`/athlete/${athleteId}/profile`),
  getProfileByUserId: (userId) =>
    request(`/athlete/user/${userId}/profile`),
  updateProfile: (athleteId, profileData) =>
    request(`/athlete/${athleteId}/profile`, 'PUT', profileData),
  getTodaySessions: (athleteId) =>
    request(`/athlete/${athleteId}/sessions/today`),
  getTherapists: () =>
    request('/athlete/therapists'),
  getFacilities: () =>
    request('/athlete/facilities'),
  getAvailableSlots: (therapistId, facilityId, date, durationMins, currentSessionId = null) =>
    request(`/athlete/sessions/availability?therapistId=${therapistId}&facilityId=${facilityId}&date=${date}&durationMins=${durationMins}${currentSessionId ? `&currentSessionId=${currentSessionId}` : ''}`),
  bookSession: (athleteId, therapistId, facilityId, sessionDate, durationMins, sessionType) =>
    request('/athlete/sessions/book', 'POST', {
      athleteId, therapistId, facilityId, sessionDate, durationMins, sessionType,
    }),
  getSessionHistory: (athleteId) =>
    request(`/athlete/${athleteId}/sessions/history`),
  getUpcomingSessions: (athleteId) =>
    request(`/athlete/${athleteId}/sessions/upcoming`),
  cancelSession: (sessionId, athleteId) =>
    request(`/athlete/sessions/${sessionId}/cancel`, 'PUT', { athleteId }),
  rescheduleSession: (sessionId, athleteId, newDate) =>
    request(`/athlete/sessions/${sessionId}/reschedule`, 'PUT', { athleteId, newDate }),
  getRecoveryMetrics: (athleteId) =>
    request(`/athlete/${athleteId}/recovery-metrics`),
  getInvoices: (athleteId) =>
    request(`/athlete/${athleteId}/invoices`),
};

export const therapist = {
  getProfileByUserId: (userId) =>
    request(`/therapist/user/${userId}/profile`),
  getAthletes: () =>
    request('/therapist/athletes'),
  getAssignedAthletes: (therapistId) =>
    request(`/therapist/${therapistId}/athletes`),
  getSessions: (therapistId) =>
    request(`/therapist/${therapistId}/sessions`),
  getTodayRoster: (therapistId) =>
    request(`/therapist/${therapistId}/roster/today`),
  logBiomechanicalData: (athleteId, therapistId, sessionId, jumpPower, jointMobility, postureScore, treatmentNote) =>
    request('/therapist/biomechanics/log', 'POST', {
      athleteId, therapistId, sessionId, jumpPower, jointMobility, postureScore, treatmentNote,
    }),
  getBiomechanicalsBySession: (sessionId) =>
    request(`/therapist/biomechanics/session/${sessionId}`),
  updateSessionStatus: (sessionId, therapistId, status) =>
    request(`/therapist/sessions/${sessionId}/status`, 'PUT', { therapistId: String(therapistId), status }),
  compileAthleteReport: (athleteId) =>
    request(`/therapist/reports/athlete/${athleteId}/compile`),
  generateReport: (athleteId, therapistId, reportType, description) =>
    request('/therapist/reports/generate', 'POST', {
      athleteId, therapistId, reportType, description,
    }),
  getTherapistReports: (therapistId) =>
    request(`/therapist/reports/${therapistId}`),
};

export const admin = {
  admitNewAthlete: (username, password, email, fullName, contact, therapistId, facilityId) =>
    request('/admin/athletes/admit', 'POST', {
      username, password, email, fullName, contact, therapistId, facilityId,
    }),
  getAnalytics: () =>
    request('/admin/analytics'),
  getAllAthletes: () =>
    request('/admin/athletes'),
  getFacilities: () =>
    request('/admin/facilities'),
  getTherapists: () =>
    request('/admin/therapists'),
  getCompletedSessions: () =>
    request('/admin/sessions/completed'),
  updateFacilityStatus: (facilityId, status) =>
    request(`/admin/facilities/${facilityId}/status`, 'PUT', { status }),
  getEquipmentByFacility: (facilityId) =>
    request(`/admin/facilities/${facilityId}/equipment`),
  updateEquipmentStatus: (itemId, status) =>
    request(`/admin/equipment/${itemId}/status`, 'PUT', { status }),
  processBilling: (sessionId, athleteId, billingType) =>
    request('/admin/billing/process', 'POST', { sessionId, athleteId, billingType }),
  getAllStaff: () =>
    request('/admin/staff'),
  addStaff: (username, password, email, fullName, contact, role) =>
    request('/admin/staff/add', 'POST', { username, password, email, fullName, contact, role }),
  updateStaff: (userId, staffData) =>
    request(`/admin/staff/${userId}`, 'PUT', staffData),
  deactivateStaff: (userId) =>
    request(`/admin/staff/${userId}/deactivate`, 'PUT'),
  deleteStaff: (userId) =>
    request(`/admin/staff/${userId}`, 'DELETE'),
  getFullLedger: () =>
    request('/admin/ledger'),
  approveReport: (reportId, adminId) =>
    request(`/admin/reports/${reportId}/approve`, 'PUT', { adminId }),
};

export const notifications = {
  getUnread: (userId) =>
    request(`/notifications/${userId}/unread`),
  getAll: (userId) =>
    request(`/notifications/${userId}/all`),
  markAsRead: (notificationId) =>
    request(`/notifications/${notificationId}/read`, 'PUT'),
  markAllAsRead: (userId) =>
    request(`/notifications/user/${userId}/read-all`, 'PUT'),
};
