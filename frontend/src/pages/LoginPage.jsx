import { useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {auth} from '../services/api';
import './LoginPage.css';

function LoginPage(){
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');
    const [activeTab, setActiveTab] =useState('login');
    const [email, setEmail] = useState('');
    const [fullName, setFullName] = useState('');

    const navigate = useNavigate();
    const handleLogin = async (e) => {
        e.preventDefault();
        if(!username || !password){
          setMessage('Please fill in all fields');
          return;
        }
        try{
          const data = await auth.login(username ,password);
          localStorage.setItem('user',JSON.stringify({
            userId: data.userId,
            username: data.username,
            role: data.role,
          }));

          if(data.role === 'ATHLETE'){
            navigate('/athlete');
          }else if(data.role === 'THERAPIST'){
            navigate('/therapist');
          
          }else if(data.role === 'ADMIN'){
            navigate('/admin');
          }
        
          }catch (error){
            setMessage(error.message);
          }
        
    };
    const handleRegister = async (e) => {
      e.preventDefault();
      if(!username || !password ||!email ||!fullName){
        setMessage('Please fill in all fields');
        return;
      }
      try{
        await auth.register(username,password,email,fullName);
        setMessage('Registration succesfull,you can now login');
        setActiveTab('login');
      }catch(error){
        setMessage(error.message);
      }
      };
    

   return(
  <div className="login-page">
    <div className="login-container">
      <div className="brand-icon">🏥</div>
      <h1>APEX</h1>
      <p className="subtitle">Biomechanics & Sports Rehab System</p>

      {/* TAB BUTTONS */}
      <div className="form-tabs">
        <button
          className={`tab ${activeTab === 'login' ? 'active' : ''}`}
          onClick={() => { setActiveTab('login'); setMessage(''); }}
        >
          Login
        </button>
        <button
          className={`tab ${activeTab === 'register' ? 'active' : ''}`}
          onClick={() => { setActiveTab('register'); setMessage(''); }}
        >
          Register
        </button>
      </div>

      {/* ERROR/SUCCESS MESSAGE */}
      {message && <p>{message}</p>}

      {/* LOGIN FORM — only shows when activeTab is 'login' */}
      {activeTab === 'login' && (
        <form onSubmit={handleLogin}>
          <input type="text" placeholder="Username" value={username}
            onChange={(e) => setUsername(e.target.value)} />
          <input type="password" placeholder="Password" value={password}
            onChange={(e) => setPassword(e.target.value)} />
          <button type="submit">Sign In</button>
        </form>
      )}

      {/* REGISTER FORM — only shows when activeTab is 'register' */}
      {activeTab === 'register' && (
        <form onSubmit={handleRegister}>
          <input type="text" placeholder="Full Name" value={fullName}
            onChange={(e) => setFullName(e.target.value)} />
          <input type="email" placeholder="Email" value={email}
            onChange={(e) => setEmail(e.target.value)} />
          <input type="text" placeholder="Username" value={username}
            onChange={(e) => setUsername(e.target.value)} />
          <input type="password" placeholder="Password" value={password}
            onChange={(e) => setPassword(e.target.value)} />
          <button type="submit">Create Account</button>
        </form>
      )}
    </div>
  </div>
);
}
export default LoginPage;
    
