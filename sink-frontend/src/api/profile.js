import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

export const fetchProfile = async (userId, token) => {
   try {
      const response = await fetch(`${API_BASE_URL}/dashboard/profile/${userId}`, {
         method: "GET",
         headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json",
         },
      });

      if (!response.ok) throw new Error("Failed to fetch profile");
      
      return await response.json();
   } catch (err) {
      return { error: err.message };
   }
};

export const updateProfile = async (userId, token, updatePayload) => {
   try {
      const response = await fetch(`${API_BASE_URL}/dashboard/profile/${userId}`, {
         method: "PUT",
         headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json",
         },
         body: JSON.stringify(updatePayload),
      });

      if (!response.ok) throw new Error("Failed to update profile");

      return { success: true };
   } catch (err) {
      return { error: err.message };
   }
};
