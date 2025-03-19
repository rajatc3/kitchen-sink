import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

// Function to handle logout
const handleLogout = () => {
  localStorage.clear();
  window.location.href = "/login"; // Redirect to login page
};

// Create Axios instance
const apiClient = axios.create({
  baseURL: API_BASE_URL,
});

// Add an interceptor to handle 401 errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      handleLogout();
    }
    return Promise.reject(error);
  }
);

// API Functions
export const fetchPosts = async (token, page = 0, size = 5, sortBy, sortOrder) => {
  try {
    const response = await apiClient.get(`/posts?page=${page}&size=${size}&sort=${sortBy},${sortOrder}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || "Failed to fetch posts";
  }
};

export const createPost = async (token, postData) => {
  try {
    const response = await apiClient.post(`/posts`, postData, {
      headers: { 
        Authorization: `Bearer ${token}`, 
        "Content-Type": "application/json" 
      },
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || "Failed to create post";
  }
};

export const addComment = async (token, postId, commentData) => {
  try {
    const response = await apiClient.post(`/posts/${postId}/comments`, commentData, {
      headers: { 
        Authorization: `Bearer ${token}`, 
        "Content-Type": "application/json" 
      },
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || "Failed to add comment";
  }
};

export const deletePost = async (token, postId) => {
  try {
    await apiClient.delete(`/posts/${postId}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
  } catch (error) {
    throw error.response?.data || "Failed to delete post";
  }
};

export const deleteComment = async (token, commentId) => {
  try {
    await apiClient.delete(`/posts/comments/${commentId}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
  } catch (error) {
    throw error.response?.data || "Failed to delete comment";
  }
};
