import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { useEffect } from "react";
import Login from "./pages/Login";
import Members from "./pages/Members";
import Register from "./pages/Register";
import { refreshAccessToken } from "./api/auth";

const REFRESH_INTERVAL = 1 * 60 * 1000; // Refresh every 9 minutes (before expiration)

function App() {
  useEffect(() => {
    const interval = setInterval(async () => {
      try {
        await refreshAccessToken();
      } catch (error) {
        console.error("Failed to refresh token, logging out...");
      }
    }, REFRESH_INTERVAL); // Refresh token every 9 minutes

    return () => clearInterval(interval);
  }, []);

  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/members" element={<Members />} />
        <Route path="/register" element={<Register />} />
        <Route path="*" element={<Login />} /> {/* Redirect unknown routes */}
      </Routes>
    </Router>
  );
}

export default App;