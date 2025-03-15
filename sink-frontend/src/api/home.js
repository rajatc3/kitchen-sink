import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

export const fetchPosts = async (token) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/posts`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || "Failed to fetch posts";
  }
};

export const createPost = async (token, postData) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/posts`, postData, {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || "Failed to create post";
  }
};

export const addComment = async (token, postId, commentData) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/posts/${postId}/comments`, commentData, {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || "Failed to add comment";
  }
};