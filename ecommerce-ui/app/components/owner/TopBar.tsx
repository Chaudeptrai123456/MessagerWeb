"use client";
import React from "react";
import { useContext, useState } from "react";
import { UserContext } from "@/context/UserContext";
import { useRouter } from "next/navigation";
import { apiClient } from "@/utils/axios.client";
import { API_PATHS } from "@/utils/apiPaths";
const backend = apiClient("BACKEND");
import Avatar from "../images/avatar";

export default function TopBar({ onMenuClick }: { onMenuClick: () => void }) {
  const ctx = useContext(UserContext);
  if (!ctx) return null;
  const router = useRouter();
  const { user,clearUser } = ctx;
  console.log("TopBar user:", user);
  const [open, setOpen] = useState(false);
  const handleLogout = async () => {
    await backend.get("http://localhost:9999/logout",  {
      withCredentials: true,
    });
      clearUser();        // ❗ QUAN TRỌNG

    router.push("/login");
  };
  return (
    <header className="h-14 flex items-center justify-between px-4 border-b border-slate-800 bg-slate-900">
      <div className="flex items-center gap-3">
        {/* MOBILE MENU BUTTON */}
        <button
          onClick={onMenuClick}
          className="md:hidden p-2 rounded-md hover:bg-slate-800"
        >
          ☰
        </button>

        <span className="font-semibold text-lg">Owner Panel</span>
      </div>

      <div className="text-sm text-slate-400 relative">
        <button
          onClick={() => setOpen(!open)}
          className="px-3 py-1 rounded-md hover:bg-slate-800"
        >
          <Avatar src={user?.avatar} role={user?.roles} />
        </button>
        {open && (
          <div className="absolute right-0 mt-2 w-48 bg-slate-800 border border-slate-700 rounded-lg shadow-xl z-50 bg-color-slate-900">
            <ul className="py-1 px-1 text-sm">
              <li className="px-4 py-2 hover:bg-slate-700 cursor-pointer">
                👤 Hồ sơ cá nhân
              </li>
              <li className="px-4 py-2 hover:bg-slate-700 cursor-pointer">
                ⚙️ Cài đặt
              </li>
              <li
                className="px-4 py-2 hover:bg-red-600 text-red-400 cursor-pointer"
                onClick={handleLogout}
              >
                🚪Logout
              </li>
            </ul>
          </div>
        )}
      </div>
    </header>
  );
}
