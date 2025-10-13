import axiosInstance from "../utils/axiosInstance";
import { API_PATHS } from "../utils/apiPath";
function getCookie(name: string): string | null {
  const match = document.cookie.match(new RegExp(`(^| )${name}=([^;]+)`));
  return match ? decodeURIComponent(match[2]) : null;
}

export function ensureOAuthLogin() {
  const token = getCookie("token");

  if (!token) {
    console.log("🔐 Chưa có token, chuyển hướng tới OAuth2 login...");
    window.location.href = "http://localhost:9999/login"; // 👈 chuyển hướng tới trang login
  } else {
    console.log("✅ Đã có token trong cookie");
  }
}
export async function fetchUserProfile() {
  try {
    const response = await axiosInstance.get(API_PATHS.USER.PROFILE);
    console.log("👤 User info:", response.data);
    return response.data;
  } catch (error: any) {
    console.error("❌ Lỗi khi lấy profile:", error);

    if (error.response?.status === 401) {
      console.warn("⚠️ Token hết hạn hoặc không hợp lệ, chuyển hướng login...");
      // Xóa token trong cookie (nếu cần)
      document.cookie = "token=; Max-Age=0; path=/;";
      window.location.href = "http://localhost:9999/login";
    }

    return null;
  }
}