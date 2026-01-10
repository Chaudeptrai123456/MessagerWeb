"use client";

import { useContext, useState } from "react";
import { useRouter } from "next/navigation";
import { UserContext } from "@/context/UserContext";
import { apiClient } from "@/utils/axios.client";
import { API_PATHS } from "@/utils/apiPaths";
import AuthLoading from "@/components/loading/AuthLoading";
const backendApi = apiClient("BACKEND");

export default function LoginPage() {
  const router = useRouter();
  const ctx = useContext(UserContext);

  if (!ctx) {
    return <div>Initializing...</div>;
  }

  const { refetchUser } = ctx;
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleLogin = async () => {
    setLoading(true);
    setError("");

    try {
      // 🔹 Gọi backend login (cookie sẽ được set)
      await backendApi.get(API_PATHS.AUTH.LOGIN, {
        withCredentials: true,
      });

      // 🔹 Lấy lại user từ cookie
      await refetchUser();

      // 🔹 Redirect về home
      router.replace("/");
    } catch (err) {
      console.error(err);
      setError("Đăng nhập thất bại");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-100">
      <div className="w-full max-w-sm bg-white p-6 rounded shadow space-y-4">
        <h1 className="text-xl font-bold text-center">Đăng nhập</h1>

        {error && (
          <div className="text-red-500 text-sm text-center">{error}</div>
        )}

        <button
          onClick={handleLogin}
          disabled={loading}
          className="w-full bg-black text-white py-2 rounded disabled:opacity-50"
        >
          {loading ? <AuthLoading /> : "Đăng nhập"}
        </button>
      </div>
    </div>
  );
}
