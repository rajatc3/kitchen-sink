import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

export const fetchMembers = async (token) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/members`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || "Failed to fetch members";
  }
};

export const addMember = async (token, memberData) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/members`, memberData, {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || "Failed to add member";
  }
};

export const updateMember = async (token, memberId, updateData) => {
   try {
     const response = await axios.put(`${API_BASE_URL}/members/${memberId}`, updateData, {
       headers: {
         Authorization: `Bearer ${token}`,
         "Content-Type": "application/json",
       },
     });
     return response.data;
   } catch (error) {
     if (error.response?.status === 400 && error.response?.data?.errors) {
       throw {
         errors: error.response.data.errors,
         status: error.response.data.status,
         timestamp: error.response.data.timestamp,
       };
     }
     throw error.response?.data || "Failed to update member";
   }
 };

export const deleteMember = async (token, memberId) => {
  try {
    await axios.delete(`${API_BASE_URL}/members/${memberId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  } catch (error) {
    throw error.response?.data || "Failed to delete member";
  }
};