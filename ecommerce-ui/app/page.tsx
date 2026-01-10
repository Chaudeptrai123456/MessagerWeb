"use client";

import { useContext, useEffect } from "react";
import { useRouter } from "next/navigation";
import { UserContext } from "@/context/UserContext";

export default function Home() {
  const ctx = useContext(UserContext);
  const router = useRouter();

  if (!ctx) return <div>⚠️ UserContext chưa sẵn sàng</div>;

  const { user, loading } = ctx;

  // ✅ Hook luôn ở trên
  useEffect(() => {
    if (!loading && user) {
      if (user.roles?.includes("ROLE_OWNER")) {
        router.push("/owner");
      } else if (user.roles?.includes("ROLE_MANAGER")) {
        router.push("/manager");
      } else {
        router.push("/user");
      }
    }
  }, [user, loading, router]);

  if (loading) return <div>Loading...</div>;

  return <div>{user ? "Đang chuyển hướng..." : "❌ Chưa đăng nhập"}</div>;
}
