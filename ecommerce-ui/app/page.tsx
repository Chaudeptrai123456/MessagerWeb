"use client";

import { useContext, useEffect } from "react";
import { useRouter } from "next/navigation";
import { UserContext } from "@/context/UserContext";
import RedirectLoading from "@/components/loading/RedirectLoading";
export default function Home() {
  const ctx = useContext(UserContext);
  const router = useRouter();

  if (!ctx) return <div>⚠️ UserContext chưa sẵn sàng</div>;

  const { user, loading } = ctx;

  // ✅ Hook luôn ở trên
  useEffect(() => {
    if (loading) return;

    if (!user) {
      router.replace("/login");
      return;
    }

    if (user.roles?.includes("ROLE_OWNER")) {
      router.replace("/owner");
    } else if (user.roles?.includes("ROLE_MANAGER")) {
      router.replace("/manager");
    } else {
      router.replace("/user");
    }
  }, [user, loading, router]);

  // ✅ LUÔN show loading vì page này chỉ để redirect
  return <RedirectLoading />;
}
