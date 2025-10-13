import { createContext, useContext, useState, useEffect } from "react";
import type { ReactNode } from "react";
import axiosInstance from "../utils/axiosInstance";
import { API_PATHS } from "../utils/apiPath";

// 🧩 Kiểu dữ liệu cho user
export interface User {
  id: string;
  name: string;
  email: string;
  avatar?: string;
  role?: string;
}

// 🔧 Kiểu cho context
interface UserContextType {
  user: User | null;
  loading: boolean;
  fetchUserProfile: () => Promise<void>;
  logout: () => void;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

export const useUser = (): UserContextType => {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error("useUser must be used within a UserProvider");
  }
  return context;
};

export const UserProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  function getCookie(name: string): string | null {
    const match = document.cookie.match(new RegExp(`(^| )${name}=([^;]+)`));
    return match ? decodeURIComponent(match[2]) : null;
  }

  const fetchUserProfile = async () => {
    const token = getCookie("token");
    if (!token) {
      console.warn("⚠️ Không có token — user chưa đăng nhập");
      setLoading(false);
      return;
    }

    try {
      const response = await axiosInstance.get(API_PATHS.USER.PROFILE);
      console.log("👤 User info:", response.data);
      setUser(response.data);
    } catch (error) {
      console.error("❌ Lỗi khi lấy user profile:", error);
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    document.cookie = "token=; Max-Age=0; path=/;";
    setUser(null);
    window.location.href = "/login";
  };

  useEffect(() => {
    fetchUserProfile();
  }, []);

  return (
    <UserContext.Provider value={{ user, loading, fetchUserProfile, logout }}>
      {children}
    </UserContext.Provider>
  );
};
