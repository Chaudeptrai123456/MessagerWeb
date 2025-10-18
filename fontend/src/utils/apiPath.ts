// src/utils/apiPaths.ts

const BASE_URL = "http://localhost:9999";
const BACKEND_URL = "http://backend:9999";

export const API_PATHS = {
  BASE_URL,
  BACKEND_URL,

  CATEGORY: {
    GET_ALL_CATEGORIES: "/api/categories",
  },

  AUTH: {
    LOGIN: "/api/auth/login", // 👈 endpoint đăng nhập
  },

  USER: {
    PROFILE: "/api/user/profile", // 👈 Đúng endpoint lấy user info (theo ảnh)
    VERIFY_ADMIN: (code: string, email: string) =>
      `/api/verify?code=${code}&email=${email}`,
    GET_ALL_USERS: "/api/users",
    GET_USER_BY_ID: (userId: string) => `/api/users/${userId}`,
    DELETE_USER_BY_ID: (userId: string) => `/api/users/${userId}`,
    UPDATE_USER_BY_ID: (userId: string) => `/api/users/${userId}`,
  },
  ORDER: {
    GET_ORDERS_BY_USER_ID: (userId: string) => `/api/orders/user/${userId}`,
  },
  PRODUCT: {
    TOP_DISCOUNT: "/api/products/top-discount"
  },
};
