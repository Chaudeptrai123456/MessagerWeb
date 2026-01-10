"use client";
import React from "react";

import { apiClient } from "@/utils/axios.client";
import { useContext, useEffect } from "react"; import { useRouter } from "next/navigation"; import { UserContext } from "@/context/UserContext";

const backendApi = apiClient("BACKEND");

export default function LoginPage() {
  const ctx= useContext(UserContext);
  if (!ctx) return <div>⚠️ UserContext chưa được khởi tạo</div>;

  const { user, loading, refetchUser } = ctx;
  console.log("User data:", user);
  return (
    <div >
      Owner 
    </div>
  );
}
