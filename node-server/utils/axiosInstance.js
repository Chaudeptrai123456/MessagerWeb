// axiosInstance.js
import axios from "axios";
import { AUTH_URL } from "./apiPath.js"; // ✅ Dùng ES import cho đồng bộ

const axiosInstance = axios.create({
  baseURL: AUTH_URL,
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
});

// 🧩 Request Interceptor
axiosInstance.interceptors.request.use(
  (config) => {
    const accessToken = localStorage.getItem("token");

    if (accessToken) {
      // ✅ Sửa header chính xác: "Authorization: Bearer <token>"
      config.headers.Authorization = `Bearer ${accessToken}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// ⚙️ Response Interceptor
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const { status } = error.response;

      switch (status) {
        case 401:
          console.warn("Unauthorized. Redirecting to login...");
          window.location.href = "/login";
          break;
        case 403:
          console.warn("Forbidden. You don’t have access to this resource.");
          break;
        case 500:
          console.error("Server error. Please try again later.");
          break;
        default:
          console.error(`HTTP error ${status}: ${error.message}`);
      }
    } else if (error.code === "ECONNABORTED") {
      console.error("⏱️ Request timeout. Please try again later.");
    } else {
      console.error("⚠️ Network error or unknown error occurred:", error.message);
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
