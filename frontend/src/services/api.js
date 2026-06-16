const BASE_URL = '/api';

async function request(endpoint, method = 'GET', body = null) {
  const options = {
    method,
    headers: {
      'Content-Type': 'application/json',
    },
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
  updateProfile: (athleteId, profileData) =>
    request(`/athlete/${athleteId}/profile`, 'PUT', profileData),
  getTodaySessions: () =>
    request('/athlete/sessions/today'),
  bookSession: (athleteId, therapistId, facilityId, sessionDate, durationMins, sessionType) =>
    request('/athlete/sessions/book', 'POST', {
      athleteId, therapistId, facilityId, sessionDate, durationMins, sessionType,
    }),
  getSessionHistory: (athleteId) =>
    request(`/athlete/${athleteId}/sessions/history`),
  getUpcomingSessions: (athleteId) =>
    request(`/athlete/${athleteId}/sessions/upcoming`),
  cancelSession: (sessionId) =>
    request(`/athlete/sessions/${sessionId}/cancel`, 'PUT'),
  rescheduleSession: (sessionId, newDate) =>
    request(`/athlete/sessions/${sessionId}/reschedule`, 'PUT', { newDate }),
  getRecoveryMetrics: (athleteId) =>
    request(`/athlete/${athleteId}/recovery-metrics`),
  getInvoices: (athleteId) =>
    request(`/athlete/${athleteId}/invoices`),
};

export const therapist = {
  getTodayRoster: (therapistId) =>
    request(`/therapist/${therapistId}/roster/today`),
  logBiomechanicalData: (sessionId, jumpPower, jointMobility, postureScore, notes) =>
    request('/therapist/biomechanics/log', 'POST', {
      sessionId, jumpPower, jointMobility, postureScore, notes,
    }),
  getBiomechanicalsBySession: (sessionId) =>
    request(`/therapist/biomechanics/session/${sessionId}`),
  updateSessionStatus: (sessionId, status) =>
    request(`/therapist/sessions/${sessionId}/status`, 'PUT', { status }),
  generateReport: (athleteId, therapistId, summary) =>
    request('/therapist/reports/generate', 'POST', { athleteId, therapistId, summary }),
  getAthleteReports: (athleteId) =>
    request(`/therapist/reports/${athleteId}`),
};

export const admin = {
  admitNewAthlete: (username, password, email, fullName, therapistId, facilityId) =>
    request('/admin/athletes/admit', 'POST', {
      username, password, email, fullName, therapistId, facilityId,
    }),
  getAnalytics: () =>
    request('/admin/analytics'),
  getAllAthletes: () =>
    request('/admin/athletes'),
  processBilling: (sessionId, athleteId, billingType) =>
    request('/admin/billing/process', 'POST', { sessionId, athleteId, billingType }),
  addStaff: (username, password, email, fullName, role) =>
    request('/admin/staff/add', 'POST', { username, password, email, fullName, role }),
  deactivateStaff: (userId) =>
    request(`/admin/staff/${userId}/deactivate`, 'PUT'),
  getFullLedger: () =>
    request('/admin/ledger'),
};

export const notifications = {
  getUnread: (userId) =>
    request(`/notifications/${userId}/unread`),
  getAll: (userId) =>
    request(`/notifications/${userId}/all`),
  markAsRead: (notificationId) =>
    request(`/notifications/${notificationId}/read`, 'PUT'),
};

/*
const BASE_URL = '/api';

// Reads userId from localStorage after login
// Frontend stores it on login: localStorage.setItem('userId', user.userId)
function getAuthHeaders() {
  const userId = localStorage.getItem('userId');
  return {
    'Content-Type': 'application/json',
    ...(userId ? { 'X-User-Id': userId } : {}),
  };
}

async function request(endpoint, method = 'GET', body = null) {
  const options = {
    method,
    headers: getAuthHeaders(),  // Fix 1: always send X-User-Id for RBAC
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
  getTodaySessions: () =>
    request('/athlete/sessions/today'),
  bookSession: (athleteId, therapistId, facilityId, sessionDate, durationMins, sessionType) =>
    request('/athlete/sessions/book', 'POST', {
      athleteId, therapistId, facilityId, sessionDate, durationMins, sessionType,
    }),
  // UC8 — returns COMPLETED sessions only (fixed)
  getSessionHistory: (athleteId) =>
    request(`/athlete/${athleteId}/sessions/history`),
  getUpcomingSessions: (athleteId) =>
    request(`/athlete/${athleteId}/sessions/upcoming`),
  cancelSession: (sessionId) =>
    request(`/athlete/sessions/${sessionId}/cancel`, 'PUT'),
  rescheduleSession: (sessionId, newDate) =>
    request(`/athlete/sessions/${sessionId}/reschedule`, 'PUT', { newDate }),
  getRecoveryMetrics: (athleteId) =>
    request(`/athlete/${athleteId}/recovery-metrics`),
  getInvoices: (athleteId) =>
    request(`/athlete/${athleteId}/invoices`),
};

export const therapist = {
  getTodayRoster: (therapistId) =>
    request(`/therapist/${therapistId}/roster/today`),
  logBiomechanicalData: (athleteId, therapistId, sessionId, jumpPower, jointMobility, postureScore, treatmentNote) =>
    request('/therapist/biomechanics/log', 'POST', {
      athleteId, therapistId, sessionId, jumpPower, jointMobility, postureScore, treatmentNote,
    }),
  getBiomechanicalsBySession: (sessionId) =>
    request(`/therapist/biomechanics/session/${sessionId}`),
  updateSessionStatus: (sessionId, status) =>
    request(`/therapist/sessions/${sessionId}/status`, 'PUT', { status }),
  // UC14 — compile athlete report
  compileAthleteReport: (athleteId) =>
    request(`/therapist/reports/athlete/${athleteId}/compile`),
  generateReport: (athleteId, therapistId, reportType, description) =>
    request('/therapist/reports/generate', 'POST', {
      athleteId, therapistId, reportType, description,
    }),
  getReports: (therapistId) =>
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
  // UC17 — update facility status (fixed)
  updateFacilityStatus: (facilityId, status) =>
    request(`/admin/facilities/${facilityId}/status`, 'PUT', { status }),
  getEquipmentByFacility: (facilityId) =>
    request(`/admin/facilities/${facilityId}/equipment`),
  updateEquipmentStatus: (itemId, status) =>
    request(`/admin/equipment/${itemId}/status`, 'PUT', { status }),
  // UC18 — billing (fixed)
  processBilling: (sessionId, athleteId, billingType) =>
    request('/admin/billing/process', 'POST', { sessionId, athleteId, billingType }),
  getFullLedger: () =>
    request('/admin/ledger'),
  // UC19 — staff management (fixed)
  getAllStaff: () =>
    request('/admin/staff'),
  addStaff: (username, password, email, fullName, contact, role) =>
    request('/admin/staff/add', 'POST', { username, password, email, fullName, contact, role }),
  deactivateStaff: (userId) =>
    request(`/admin/staff/${userId}/deactivate`, 'PUT'),
  deleteStaff: (userId) =>
    request(`/admin/staff/${userId}`, 'DELETE'),
};

export const notifications = {
  getUnread: (userId) =>
    request(`/notifications/${userId}/unread`),
  getAll: (userId) =>
    request(`/notifications/${userId}/all`),
  markAsRead: (notificationId) =>
    request(`/notifications/${notificationId}/read`, 'PUT'),
};
*/ 