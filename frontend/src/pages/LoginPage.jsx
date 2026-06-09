import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { auth } from '../services/api';
import './LoginPage.css';

const emptyForm = {
  username: '',
  password: '',
  email: '',
  fullName: '',
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
      );
      setForm(emptyForm);
      setActiveTab('login');
      showMessage('success', 'Registration successful. You can now sign in.');
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

  return (
    <main className="login-page">
      <section className="login-container" aria-label="Apex authentication">
        <div className="login-brand">
          <span>Sports Rehabilitation Portal</span>
          <h1>APEX</h1>
          <p className="subtitle">Biomechanics & Sports Rehab System</p>
        </div>

        <div className="form-tabs" role="tablist" aria-label="Authentication mode">
          <button className={`tab ${activeTab === 'login' ? 'active' : ''}`} type="button" onClick={() => switchTab('login')}>Login</button>
          <button className={`tab ${activeTab === 'register' ? 'active' : ''}`} type="button" onClick={() => switchTab('register')}>Register</button>
          <button className={`tab ${activeTab === 'reset' ? 'active' : ''}`} type="button" onClick={() => switchTab('reset')}>Reset</button>
        </div>

        {message && <p className={`message ${message.type}`}>{message.text}</p>}

        {activeTab === 'login' && (
          <form onSubmit={handleLogin}>
            <input name="username" type="text" placeholder="Username" value={form.username} onChange={updateForm} required />
            <input name="password" type="password" placeholder="Password" value={form.password} onChange={updateForm} required />
            <button type="submit" disabled={loading}>{loading ? 'Signing in...' : 'Sign In'}</button>
          </form>
        )}

        {activeTab === 'register' && (
          <form onSubmit={handleRegister}>
            <input name="fullName" type="text" placeholder="Full name" value={form.fullName} onChange={updateForm} required />
            <input name="email" type="email" placeholder="Email" value={form.email} onChange={updateForm} required />
            <input name="username" type="text" placeholder="Username" value={form.username} onChange={updateForm} required />
            <input name="password" type="password" placeholder="Password" value={form.password} onChange={updateForm} required />
            <button type="submit" disabled={loading}>{loading ? 'Creating...' : 'Create Account'}</button>
          </form>
        )}

        {activeTab === 'reset' && (
          <form onSubmit={handleResetPassword}>
            <input name="username" type="text" placeholder="Username" value={form.username} onChange={updateForm} required />
            <input name="email" type="email" placeholder="Registered email" value={form.email} onChange={updateForm} required />
            <input name="newPassword" type="password" placeholder="New password" value={form.newPassword} onChange={updateForm} required />
            <button type="submit" disabled={loading}>{loading ? 'Updating...' : 'Update Password'}</button>
          </form>
        )}

      </section>
    </main>
  );
}

export default LoginPage;
