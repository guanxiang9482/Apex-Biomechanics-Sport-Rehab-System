import { Routes, Route } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import AthleteDashboard from './pages/AthleteDashboard';
import TherapistDashboard from './pages/TherapistDashboard';
import AdminDashboard from './pages/AdminDashboard';

function App() {
  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route path="/athlete" element={<AthleteDashboard />} />
      <Route path="/therapist" element={<TherapistDashboard />} />
      <Route path="/admin" element={<AdminDashboard />} />
    </Routes>
  );
}

export default App;
