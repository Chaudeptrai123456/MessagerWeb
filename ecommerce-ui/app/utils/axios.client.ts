"use client";

import axios, { AxiosInstance } from "axios";

type ApiTarget = "BACKEND" | "GATEWAY";

const createAxios = (baseURL?: string): AxiosInstance => {
  const instance = axios.create({
    baseURL,
    timeout: 300000,
    headers: {
      Accept: "application/json",
    },
  });

  instance.interceptors.request.use(
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
  instance.interceptors.request.use(
    (config) => {
      config.withCredentials = true;
      return config;
    },
    (error) => Promise.reject(error)
  );

  instance.interceptors.response.use(
    (res) => res,
    (error) => {
      if (error.response?.status === 401) {
        console.log("🚀 ~ file: axios.client.ts:64 ~ instance.interceptors.response.use ~ 401" + error);
        window.location.href = "http://localhost:9999/login";
      }
      return Promise.reject(error);
    }
  );

  return instance;
};

export const apiClient = (target: ApiTarget = "BACKEND") => {
  const baseURL =
    target === "GATEWAY"
      ? process.env.NEXT_PUBLIC_GATEWAY_URL
      : process.env.NEXT_PUBLIC_BACKEND_URL;

  if (!baseURL) {
    throw new Error(`Missing baseURL for ${target}`);
  }

  return createAxios(baseURL);
};
/**
 * ======================= CÁCH SỬ DỤNG API CLIENT =======================
 *
 * 📌 File này dùng để tạo axios client cho NHIỀU server khác nhau
 *    (Backend thường & Gateway), dùng chung interceptor (token, 401, v.v.)
 *
 * 1️⃣ Khai báo ENV (bắt buộc – client side)
 *    .env.local
 *    -----------------------------------
 *    NEXT_PUBLIC_BACKEND_URL=http://localhost:9999
 *    NEXT_PUBLIC_GATEWAY_URL=http://localhost:8090
 *
 * 2️⃣ Import apiClient
 *    -----------------------------------
 *    import { apiClient } from "@/utils/axiosFactory";
 *
 * 3️⃣ Tạo client theo server muốn dùng
 *    -----------------------------------
 *    // Gọi thẳng backend
 *    const backendApi = apiClient("BACKEND");
 *
 *    // Gọi qua gateway
 *    const gatewayApi = apiClient("GATEWAY");
 *
 * 4️⃣ Gọi API như axios bình thường
 *    -----------------------------------
 *    backendApi.get("/api/users");
 *    gatewayApi.post("/api/orders", data);
 *
 * 5️⃣ Interceptor đã xử lý sẵn:
 *    -----------------------------------
 *    - Tự động gắn Authorization: Bearer <token> (từ localStorage)
 *    - Nếu 401 → redirect về /login
 *
 * 📌 Ghi nhớ:
 *    - "BACKEND"  = service gốc (admin, CRUD, data)
 *    - "GATEWAY"  = service điều phối (order, flow phức tạp)
 *    - KHÔNG hard-code baseURL trong component
 *
 * =======================================================================
 */
