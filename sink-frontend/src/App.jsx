import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { useEffect } from "react";
import Login from "./pages/Login";
import Members from "./pages/Members";
import Register from "./pages/Register";
import Profile from "./pages/Profiles";
import Admin from "./pages/Admin";
import { refreshAccessToken } from "./api/auth";

const REFRESH_INTERVAL = 5 * 60 * 1000; // Refresh every 5 minutes (before expiration)

function App() {
  useEffect(() => {
    const interval = setInterval(async () => {
      try {
        await refreshAccessToken();
      } catch (error) {
        console.error("Failed to refresh token, logging out...");
      }
    }, REFRESH_INTERVAL);

    return () => clearInterval(interval);
  }, []);

  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/members" element={<Members />} />
        <Route path="/register" element={<Register />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/admin" element={<Admin />} />
        <Route path="*" element={<Navigate to="/login" />} />
      </Routes>
    </Router>
  );
}

export default App;