import axios, { AxiosError } from "axios";
import type { AxiosResponse } from "axios";
import { API_PATHS } from "./apiPath";
// 🧱 Tạo instance mặc định
const axiosInstance = axios.create({
  baseURL: API_PATHS.BASE_URL,
  timeout: 11000,
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
  withCredentials: true, // 👈 Cho phép gửi cookie khi gọi API (quan trọng!)
});

// 🧠 Hàm tiện ích: đọc token từ cookie
function getCookie(name: string): string | null {
  const match = document.cookie.match(new RegExp(`(^| )${name}=([^;]+)`));
  return match ? decodeURIComponent(match[2]) : null;
}

// 🧠 Interceptor cho request
axiosInstance.interceptors.request.use(
  (config) => {
    const accessToken = getCookie("token"); // 👈 đọc từ cookie

    if (accessToken && config.headers) {
      config.headers["Authorization"] = `Bearer ${accessToken}`;
    }

    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// ⚙️ Interceptor cho response
axiosInstance.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error: AxiosError) => {
    if (error.response) {
      const status = error.response.status;

      if (status === 401) {
        window.location.href = "/login";
      } else if (status === 500) {
        console.error("Server error. Please try again later.");
      }
    } else if (error.code === "ECONNABORTED") {
      console.error("Request timeout. Please try again later.");
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
