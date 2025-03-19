import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api/";

export const fetchUsers = async (token, page = 0, size = 10) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/admin/users?page=${page}&size=${size}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching users:', error);
    return null;
  }
};

export const fetchAnalytics = async (token) => {
  try {
    const response = await fetch(`${API_BASE_URL}/admin/analytics`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return await response.json();
  } catch (error) {
    console.error("Error fetching analytics:", error);
    return null;
  }
};

export const markAdmin = async (token, username) => {
  try {
    const response = await axios.put(`${API_BASE_URL}/admin/elevate/${username}`,{}, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return await response.data;
  } catch (error) {
    console.error("Error marking user as admin:", error);
    return null;
  }
};