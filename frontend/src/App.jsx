import { Routes, Route } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import AthleteDashboard from './pages/AthleteDashboard';

function App() {
  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route path="/athlete" element={<AthleteDashboard />} />
      <Route path="/therapist" element={<h1>Therapist Dashboard (coming soon)</h1>} />
      <Route path="/admin" element={<h1>Admin Dashboard (coming soon)</h1>} />
    </Routes>
  );
}

export default App;