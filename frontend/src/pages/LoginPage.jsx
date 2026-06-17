import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { auth } from '../services/api';
import rehabHero from '../assets/rehab-hero.jpg';
import './LoginPage.css';

const emptyForm = {
  username: '',
  password: '',
  email: '',
  fullName: '',
  contact: '',
  bodyWeightKg: '',
  heightCm: '',
  newPassword: '',
};

function LoginPage() {
  const [form, setForm] = useState(emptyForm);
  const [activeTab, setActiveTab] = useState('login');
  const [message, setMessage] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const updateForm = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const switchTab = (tab) => {
    setActiveTab(tab);
    setMessage(null);
  };

  const showMessage = (type, text) => {
    setMessage({ type, text });
  };

  const handleLogin = async (event) => {
    event.preventDefault();
    setLoading(true);
    setMessage(null);

    try {
      const data = await auth.login(form.username.trim(), form.password);
      const user = {
        userId: data.userId,
        username: data.username,
        role: data.role,
      };

      localStorage.setItem('user', JSON.stringify(user));
      navigate(`/${String(data.role).toLowerCase()}`);
    } catch (error) {
      showMessage('error', error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (event) => {
    event.preventDefault();
    setLoading(true);
    setMessage(null);

    try {
      await auth.register(
        form.username.trim(),
        form.password,
        form.email.trim(),
        form.fullName.trim(),
        form.contact.trim(),
        form.bodyWeightKg,
        form.heightCm,
      );
      setForm(emptyForm);
      setActiveTab('login');
      showMessage('success', 'Client registration successful. You can now sign in.');
    } catch (error) {
      showMessage('error', error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (event) => {
    event.preventDefault();
    setLoading(true);
    setMessage(null);

    try {
      await auth.resetPassword(
        form.username.trim(),
        form.email.trim(),
        form.newPassword,
      );
      setForm(emptyForm);
      setActiveTab('login');
      showMessage('success', 'Password updated. Sign in with your new password.');
    } catch (error) {
      showMessage('error', error.message);
    } finally {
      setLoading(false);
    }
  };

  const tabTitle = {
    login: 'Welcome back',
    register: 'Create client access',
    reset: 'Reset password',
  }[activeTab];

  const tabSubtitle = {
    login: 'Enter your assigned APEX account to continue.',
    register: 'Client registration is for athletes only.',
    reset: 'Use your username and registered email.',
  }[activeTab];

  return (
    <main className="login-page">
      <div className="login-backdrop" aria-hidden="true">
        <span className="motion-line line-a" />
        <span className="motion-line line-b" />
        <span className="motion-line line-c" />
      </div>

      <section className="login-shell">
        <div className="auth-orbit" aria-hidden="true">
          <span>Force plate sync</span>
          <strong>Live</strong>
        </div>

        <div className="login-visual" aria-hidden="true">
          <img src={rehabHero} alt="" />
          <div className="visual-overlay">
            <div className="visual-kicker">
              <span>Biomechanics Lab</span>
              <small>Live Recovery Desk</small>
            </div>
            <h2>Precision rehab access for sessions, movement data, and clinical follow up.</h2>

            <div className="visual-metric-board">
              <article>
                <span>Gait</span>
                <strong>92%</strong>
              </article>
              <article>
                <span>Mobility</span>
                <strong>74</strong>
              </article>
              <article>
                <span>Load</span>
                <strong>48kg</strong>
              </article>
            </div>

            <div className="visual-session-card">
              <div>
                <span>Next session</span>
                <strong>Movement Screen</strong>
              </div>
              <small>09:00</small>
            </div>
          </div>
        </div>

        <div className="login-container" aria-label="Apex authentication">
          <div className="access-status" aria-hidden="true">
            <span>Clinic Access</span>
            <strong>Secure Portal</strong>
          </div>

          <div className="login-brand">
            <span>Sports Rehabilitation Portal</span>
            <h1>APEX</h1>
            <p className="subtitle">Biomechanics & Sports Rehab System</p>
            <div className="role-strip" aria-label="Supported account roles">
              <span>Admin</span>
              <span>Therapist</span>
              <span>Client</span>
            </div>
          </div>

          <div className="form-tabs" role="tablist" aria-label="Authentication mode">
            <button className={`tab ${activeTab === 'login' ? 'active' : ''}`} type="button" onClick={() => switchTab('login')}>Login</button>
            <button className={`tab ${activeTab === 'register' ? 'active' : ''}`} type="button" onClick={() => switchTab('register')}>Client Register</button>
            <button className={`tab ${activeTab === 'reset' ? 'active' : ''}`} type="button" onClick={() => switchTab('reset')}>Reset</button>
          </div>

          <div className="auth-heading">
            <h2>{tabTitle}</h2>
            <p>{tabSubtitle}</p>
          </div>

          {message && <p className={`message ${message.type}`}>{message.text}</p>}

          {activeTab === 'login' && (
            <form onSubmit={handleLogin}>
              <label>
                Username
                <input name="username" type="text" placeholder="admin" value={form.username} onChange={updateForm} required />
              </label>
              <label>
                Password
                <input name="password" type="password" placeholder="Enter password" value={form.password} onChange={updateForm} required />
              </label>
              <button type="submit" disabled={loading}>{loading ? 'Signing in...' : 'Sign In'}</button>
            </form>
          )}

          {activeTab === 'register' && (
            <form onSubmit={handleRegister}>
              <label>
                Full name
                <input name="fullName" type="text" placeholder="Client full name" value={form.fullName} onChange={updateForm} required />
              </label>
              <label>
                Email
                <input name="email" type="email" placeholder="client@email.com" value={form.email} onChange={updateForm} required />
              </label>
              <label>
                Contact
                <input name="contact" type="tel" placeholder="Phone number" value={form.contact} onChange={updateForm} required />
              </label>
              <label>
                Body weight (kg)
                <input name="bodyWeightKg" type="number" min="1" step="0.1" placeholder="70" value={form.bodyWeightKg} onChange={updateForm} required />
              </label>
              <label>
                Height (cm)
                <input name="heightCm" type="number" min="1" step="0.1" placeholder="175" value={form.heightCm} onChange={updateForm} required />
              </label>
              <label>
                Username
                <input name="username" type="text" placeholder="Choose username" value={form.username} onChange={updateForm} required />
              </label>
              <label>
                Password
                <input name="password" type="password" placeholder="Create password" value={form.password} onChange={updateForm} required />
              </label>
              <button type="submit" disabled={loading}>{loading ? 'Creating...' : 'Create Client Account'}</button>
            </form>
          )}

          {activeTab === 'reset' && (
            <form onSubmit={handleResetPassword}>
              <label>
                Username
                <input name="username" type="text" placeholder="Your username" value={form.username} onChange={updateForm} required />
              </label>
              <label>
                Registered email
                <input name="email" type="email" placeholder="account@email.com" value={form.email} onChange={updateForm} required />
              </label>
              <label>
                New password
                <input name="newPassword" type="password" placeholder="Enter new password" value={form.newPassword} onChange={updateForm} required />
              </label>
              <button type="submit" disabled={loading}>{loading ? 'Updating...' : 'Update Password'}</button>
            </form>
          )}
        </div>
      </section>
    </main>
  );
}

export default LoginPage;
