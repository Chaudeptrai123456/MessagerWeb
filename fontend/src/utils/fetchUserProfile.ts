// src/utils/fetchUserProfile.ts
import axiosInstance from "./axiosInstance";
import { API_PATHS } from "./apiPath";
export interface UserProfile {
  username: string;
  email: string;
  roles: string[];
  avatar?: string;
}

export async function fetchUserProfile(): Promise<UserProfile | null> {
  try {
    // 🧠 Gọi API để lấy thông tin user từ token
    const response = await axiosInstance.get(API_PATHS.AUTH.PROFILE, {
      withCredentials: true,
    });

    const user = response.data as UserProfile;

    // 💾 Lưu vào localStorage
    localStorage.setItem("userProfile", JSON.stringify(user));
    console.log("✅ User profile loaded:", user);

    return user;
  } catch (error: any) {
    console.error("❌ Lỗi khi lấy profile:", error.response?.data || error.message);

    // Nếu lỗi 401 → xoá userProfile + chuyển hướng login
    if (error.response?.status === 401) {
      localStorage.removeItem("userProfile");
      window.location.href = "http://localhost:9999/login";
    }
    return null;
  }
}
