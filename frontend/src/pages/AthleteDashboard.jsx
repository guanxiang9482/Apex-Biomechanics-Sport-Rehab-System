import {useState,useEffect} from 'react';
import { useNavigate } from 'react-router-dom';
import {athlete,auth} from'../services/api';
import './Dashboard.css';

function AthleteDashboard(){
        const [activeSection, setActiveSection] =useState('sessions');
    const [todaySessions,setTodaySessions]=useState([]);
    const [profile,setProfile]=useState(null);
    const[message,setMessage]=useState('');
    const navigate = useNavigate();
        const user =JSON.parse(localStorage.getItem('user'));
    useEffect(()=>{
        if(!user){
            navigate('/');
        }
    },[]);

    useEffect(()=>{
        loadTodaySessions();
    }, []);

    const loadTodaySessions = async () =>{
        try{
            const data = await athlete.getTodaySessions();
            setTodaySessions(data);
        }catch(error){
            setMessage(error.message);
        }
        };

        const handleLogout = async () =>{
            try{
                await auth.logout(user.userId);
            }catch(error){
                console.log('Logout error:',error.message);
            }
            localStorage.removeItem('user');
            navigate('/');
            };
        return (
    <div className="dashboard">
      {/* SIDEBAR */}
      <div className="sidebar">
        <div className="sidebar-brand">
          <span>🏥</span>
          <h2>APEX</h2>
        </div>
        <nav className="sidebar-nav">
          <button
            className={activeSection === 'sessions' ? 'active' : ''}
            onClick={() => setActiveSection('sessions')}
          >
            📅 Today's Sessions
          </button>
          <button
            className={activeSection === 'history' ? 'active' : ''}
            onClick={() => setActiveSection('history')}
          >
            📋 Session History
          </button>
          <button
            className={activeSection === 'metrics' ? 'active' : ''}
            onClick={() => setActiveSection('metrics')}
          >
            📊 Recovery Metrics
          </button>
          <button
            className={activeSection === 'profile' ? 'active' : ''}
            onClick={() => setActiveSection('profile')}
          >
            👤 My Profile
          </button>
          <button
            className={activeSection === 'invoices' ? 'active' : ''}
            onClick={() => setActiveSection('invoices')}
          >
            💰 My Invoices
          </button>
        </nav>
        <button className="logout-btn" onClick={handleLogout}>
          🚪 Logout
        </button>
      </div>
      {/* MAIN CONTENT */}
      <div className="main-content">
        <div className="content-header">
          <h1>
            {activeSection === 'sessions' && 'Today\'s Sessions'}
            {activeSection === 'history' && 'Session History'}
            {activeSection === 'metrics' && 'Recovery Metrics'}
            {activeSection === 'profile' && 'My Profile'}
            {activeSection === 'invoices' && 'My Invoices'}
          </h1>
          <p>Welcome, {user?.username}</p>
        </div>
        {/* TODAY'S SESSIONS SECTION */}
        {activeSection === 'sessions' && (
          <div className="section">
            {todaySessions.length === 0 ? (
              <p className="empty-state">No sessions scheduled for today.</p>
            ) : (
              <div className="card-grid">
                {todaySessions.map((session) => (
                  <div key={session.sessionId} className="card">
                    <h3>{session.sessionType}</h3>
                    <p>Status: {session.status}</p>
                    <p>Duration: {session.durationMins} mins</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
        {/* PLACEHOLDER for other sections */}
        {activeSection === 'history' && (
          <div className="section">
            <p className="empty-state">Session history coming soon...</p>
          </div>
        )}
        {activeSection === 'metrics' && (
          <div className="section">
            <p className="empty-state">Recovery metrics coming soon...</p>
          </div>
        )}
        {activeSection === 'profile' && (
          <div className="section">
            <p className="empty-state">Profile page coming soon...</p>
          </div>
        )}
        {activeSection === 'invoices' && (
          <div className="section">
            <p className="empty-state">Invoices coming soon...</p>
          </div>
        )}
      </div>
    </div>
  );
}
export default AthleteDashboard;
