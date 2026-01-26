"use client";

import { useContext, useEffect } from "react";
import { useRouter } from "next/navigation";
import { UserContext } from "@/context/UserContext";
import AuthLoading from "@/components/loading/AuthLoading";

interface Props {
  role: string;
  children: React.ReactNode;
}

export default function RequireRole({ role, children }: Props) {
  const ctx = useContext(UserContext);
  const router = useRouter();

  if (!ctx) return null;

  const { user, loading } = ctx;

  useEffect(() => {
    if (loading) return;

    if (!user) {
      // console.log("User not logged in, redirecting to login page." + role);
      router.replace("/login");
      return;
    }
    if (!user.roles?.includes(role)) {
      router.replace("/403"); // hoặc "/"
    }
  }, [user, loading, role, router]);

  if (loading) return <AuthLoading />;

  if (!user || !user.roles?.includes(role)) return null;

  return <>{children}</>;
}
