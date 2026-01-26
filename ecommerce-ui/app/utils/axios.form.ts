"use client";

import axios from "axios";

const axiosForm = axios.create({
  baseURL: process.env.GATEWAY_URL,
  timeout: 300000,
  headers: {
    Accept: "application/json",
  },
});

axiosForm.interceptors.request.use(
  (config) => {
    if (typeof window !== "undefined") {
      const token = localStorage.getItem("token");
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

axiosForm.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401) {
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default axiosForm;
