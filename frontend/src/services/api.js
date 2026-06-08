// ============================================================
// api.js — The "waiter" between your frontend and the backend
// ============================================================
//
// HOW THIS WORKS:
// Your teammate's Java backend runs on http://localhost:8080
// It provides "endpoints" (URLs) that accept/return JSON data.
//
// This file creates helper functions so your React pages can
// easily call those endpoints without repeating code everywhere.
//
// Example flow:
//   1. User types username + password on the Login page
//   2. LoginPage calls api.login(username, password)
//   3. This file sends an HTTP request to the Java backend
//   4. Backend checks the database and sends back a response
//   5. We return that response to the LoginPage
//   6. LoginPage shows "Welcome!" or "Wrong password"
// ============================================================

// The base URL where your teammate's Spring Boot server runs
const BASE_URL = 'http://localhost:8080/api';

// ------------------------------------------------------------
// HELPER FUNCTION: Makes HTTP requests easier
// ------------------------------------------------------------
// Instead of writing fetch() with headers every time,
// we wrap it in this reusable function.
//
// Parameters:
//   endpoint - the URL path (e.g., '/auth/login')
//   method   - GET, POST, PUT, DELETE
//   body     - the data to send (optional)
// ------------------------------------------------------------
async function request(endpoint, method = 'GET', body = null) {
  const options = {
    method,                        // HTTP method
    headers: {
      'Content-Type': 'application/json',  // Tell backend we're sending JSON
    },
  };

  // If we have data to send, convert it to a JSON string
  if (body) {
    options.body = JSON.stringify(body);
  }

  // Send the request and wait for the response
  const response = await fetch(`${BASE_URL}${endpoint}`, options);

  // Parse the JSON response
  const data = await response.json();

  // If the server returned an error (status 400, 401, etc.)
  if (!response.ok) {
    throw new Error(data.error || 'Something went wrong');
  }

  return data;
}

// ============================================================
// AUTH ENDPOINTS (UC1, UC2, UC3, UC4)
// These match your teammate's AuthController.java
// ============================================================

export const auth = {
  // UC1 — Register a new athlete account
  register: (username, password, email, fullName) =>
    request('/auth/register', 'POST', { username, password, email, fullName }),

  // UC2 — Login with username and password
  login: (username, password) =>
    request('/auth/login', 'POST', { username, password }),

  // UC3 — Logout
  logout: (userId) =>
    request('/auth/logout', 'POST', { userId }),

  // UC4 — Reset password
  resetPassword: (username, email, newPassword) =>
    request('/auth/reset-password', 'POST', { username, email, newPassword }),
};

// ============================================================
// ATHLETE ENDPOINTS (UC5-UC10, UC20)
// These match your teammate's AthleteController.java
// ============================================================

export const athlete = {
  // UC6 — Get athlete profile
  getProfile: (athleteId) =>
    request(`/athlete/${athleteId}/profile`),

  // UC6 — Update athlete profile
  updateProfile: (athleteId, profileData) =>
    request(`/athlete/${athleteId}/profile`, 'PUT', profileData),

  // UC5 — View today's sessions
  getTodaySessions: () =>
    request('/athlete/sessions/today'),

  // UC7 — Book a rehab session
  bookSession: (athleteId, therapistId, facilityId, sessionDate, durationMins, sessionType) =>
    request('/athlete/sessions/book', 'POST', {
      athleteId, therapistId, facilityId, sessionDate, durationMins, sessionType,
    }),

  // UC8 — View session history
  getSessionHistory: (athleteId) =>
    request(`/athlete/${athleteId}/sessions/history`),

  // UC9 — Get upcoming sessions
  getUpcomingSessions: (athleteId) =>
    request(`/athlete/${athleteId}/sessions/upcoming`),

  // UC9 — Cancel a session
  cancelSession: (sessionId) =>
    request(`/athlete/sessions/${sessionId}/cancel`, 'PUT'),

  // UC9 — Reschedule a session
  rescheduleSession: (sessionId, newDate) =>
    request(`/athlete/sessions/${sessionId}/reschedule`, 'PUT', { newDate }),

  // UC10 — View recovery metrics (biomechanical data)
  getRecoveryMetrics: (athleteId) =>
    request(`/athlete/${athleteId}/recovery-metrics`),

  // UC20 — View own invoices
  getInvoices: (athleteId) =>
    request(`/athlete/${athleteId}/invoices`),
};

// ============================================================
// THERAPIST ENDPOINTS (UC11-UC14)
// These match your teammate's TherapistController.java
// ============================================================

export const therapist = {
  // UC11 — View daily roster
  getTodayRoster: (therapistId) =>
    request(`/therapist/${therapistId}/roster/today`),

  // UC12 — Log biomechanical data
  logBiomechanicalData: (sessionId, jumpPower, jointMobility, postureScore, notes) =>
    request('/therapist/biomechanics/log', 'POST', {
      sessionId, jumpPower, jointMobility, postureScore, notes,
    }),

  // UC12 — View biomechanical records for a session
  getBiomechanicalsBySession: (sessionId) =>
    request(`/therapist/biomechanics/session/${sessionId}`),

  // UC13 — Update session status
  updateSessionStatus: (sessionId, status) =>
    request(`/therapist/sessions/${sessionId}/status`, 'PUT', { status }),

  // UC14 — Generate athlete report
  generateReport: (athleteId, therapistId, summary) =>
    request('/therapist/reports/generate', 'POST', { athleteId, therapistId, summary }),

  // UC14 — View athlete reports
  getAthleteReports: (athleteId) =>
    request(`/therapist/reports/${athleteId}`),
};

// ============================================================
// ADMIN ENDPOINTS (UC15-UC20)
// These match your teammate's AdminController.java
// ============================================================

export const admin = {
  // UC15 — Admit new athlete (Facade Pattern!)
  admitNewAthlete: (username, password, email, fullName, therapistId, facilityId) =>
    request('/admin/athletes/admit', 'POST', {
      username, password, email, fullName, therapistId, facilityId,
    }),

  // UC16 — View analytics dashboard
  getAnalytics: () =>
    request('/admin/analytics'),

  // UC16 — View all athletes
  getAllAthletes: () =>
    request('/admin/athletes'),

  // UC18 — Process billing (Strategy Pattern!)
  processBilling: (sessionId, athleteId, billingType) =>
    request('/admin/billing/process', 'POST', { sessionId, athleteId, billingType }),

  // UC19 — Add new staff member
  addStaff: (username, password, email, fullName, role) =>
    request('/admin/staff/add', 'POST', { username, password, email, fullName, role }),

  // UC19 — Deactivate staff
  deactivateStaff: (userId) =>
    request(`/admin/staff/${userId}/deactivate`, 'PUT'),

  // UC20 — View full financial ledger
  getFullLedger: () =>
    request('/admin/ledger'),
};

// ============================================================
// NOTIFICATION ENDPOINTS (UC21)
// These match your teammate's NotificationController.java
// ============================================================

export const notifications = {
  // UC21 — Get unread notifications
  getUnread: (userId) =>
    request(`/notifications/${userId}/unread`),

  // UC21 — Get all notifications
  getAll: (userId) =>
    request(`/notifications/${userId}/all`),

  // UC21 — Mark notification as read
  markAsRead: (notificationId) =>
    request(`/notifications/${notificationId}/read`, 'PUT'),
};
