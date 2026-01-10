"use client";

import React, {
  createContext,
  useState,
  useCallback,
  useEffect,
  ReactNode,
} from "react";
import { apiClient } from "@/utils/axios.client";
import { API_PATHS } from "@/utils/apiPaths";

type User = any;

interface UserContextType {
  user: User | null;
  loading: boolean;
  updateUser: (userData: User) => void;
  clearUser: () => void;
  refetchUser: () => Promise<void>;
}

export const UserContext = createContext<UserContextType | undefined>(
  undefined
);

const backendApi = apiClient("BACKEND");

export function UserProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(false);
  function getCookie(name: string): string | undefined {
    if (typeof document === "undefined") return undefined; // tránh lỗi khi SSR
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
      return parts.pop()?.split(";").shift();
    }
    return undefined;
  }
  const refetchUser = useCallback(async () => {
    setLoading(true);
    try {
      const token = getCookie("token"); // lấy token từ cookie
      const res = await backendApi.get(API_PATHS.AUTH.GET_PROFILE, {
        headers: {
          Authorization: token ? `Bearer ${token}` : "",
        },
      });
      console.log("Fetched user:", res.data);
      setUser(res.data);
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);
  useEffect(() => {
    refetchUser();
  }, []); // chỉ chạy 1 lần khi component mount

  const updateUser = useCallback((userData: User) => {
    setUser(userData);
  }, []);

  const clearUser = useCallback(async () => {
    try {
      await backendApi.post(
        API_PATHS.AUTH.LOGOUT,
        {},
        { withCredentials: true }
      );
    } catch {}
    setUser(null);
  }, []);

  return (
    <UserContext.Provider
      value={{ user, loading, updateUser, clearUser, refetchUser }}
    >
      {children}
    </UserContext.Provider>
  );
}
