
const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api/";

export const fetchUsers = async (token) => {
   try {
     const response = await fetch(`${API_BASE_URL}/admin/users`, {
       headers: { Authorization: `Bearer ${token}` },
     });
     return await response.json();
   } catch (error) {
     console.error("Error fetching users:", error);
     return [];
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
 