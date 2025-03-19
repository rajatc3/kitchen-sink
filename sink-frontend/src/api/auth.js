import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api/";

// Create Axios instance
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true, // Ensures JSESSIONID is sent
});

export const login = async (userIdentifier, password) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/auth/login`, {
      userIdentifier,
      password,
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || "Login failed";
  }
};

// Function to refresh token
export const refreshAccessToken = async () => {
  try {
    const refreshToken = localStorage.getItem("refreshToken");
    if (!refreshToken) throw new Error("No refresh token available");

    const response = await axios.post(`${API_BASE_URL}/auth/refresh-token`, { refreshToken });
    const { accessToken, refreshToken: newRefreshToken } = response.data;

    // Store new tokens
    localStorage.setItem("accessToken", accessToken);
    localStorage.setItem("refreshToken", newRefreshToken);
    return accessToken;
  } catch (error) {
    console.error("Token refresh failed:", error);
    logoutUser();
    throw error;
  }
};

// Attach interceptor to automatically refresh token
apiClient.interceptors.request.use(
  async (config) => {
    let token = localStorage.getItem("accessToken");

    // Check if token is close to expiration (optional if you handle expiry on the backend)
    if (!token) {
      token = await refreshAccessToken();
    }

    config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error)
);

// Function to logout user
const logoutUser = () => {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
  window.location.href = "/login";
};

// Function to register user
export const register = async (formData) => {
  try {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(formData),
    });

    const data = await response.json();

    if (!response.ok) {
      return {
        success: false,
        errors: data.errors || ["Registration failed"],
        message: data.message || "Registration failed"
      };
    }

    return { success: true, message: "Registration successful" };
  } catch (error) {
    return { success: false, errors: ["Network error. Please try again."] };
  }
};

export const checkUsername = async (username) => {
  const response = await fetch(`${API_BASE_URL}/auth/check-username?username=${username}`);
  return response.json(); // Should return { available: true/false }
};

export default apiClient;