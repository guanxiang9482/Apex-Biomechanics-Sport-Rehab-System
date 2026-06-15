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
  getProfileByUserId: (userId) =>
    request(`/athlete/user/${userId}/profile`),
  updateProfile: (athleteId, profileData) =>
    request(`/athlete/${athleteId}/profile`, 'PUT', profileData),
  getTodaySessions: () =>
    request('/athlete/sessions/today'),
  getTherapists: () =>
    request('/athlete/therapists'),
  getFacilities: () =>
    request('/athlete/facilities'),
  getAvailableSlots: (therapistId, facilityId, date, durationMins) =>
    request(`/athlete/sessions/availability?therapistId=${therapistId}&facilityId=${facilityId}&date=${date}&durationMins=${durationMins}`),
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
  getProfileByUserId: (userId) =>
    request(`/therapist/user/${userId}/profile`),
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
  generateReport: (therapistId, reportType, description) =>
    request('/therapist/reports/generate', 'POST', { therapistId, reportType, description }),
  getTherapistReports: (therapistId) =>
    request(`/therapist/reports/${therapistId}`),
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
  getFacilities: () =>
    request('/admin/facilities'),
  getTherapists: () =>
    request('/admin/therapists'),
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
