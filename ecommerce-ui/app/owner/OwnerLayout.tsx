"use client";

import TopBar from "@/components/owner/TopBar";
import RequireRole from "@/components/auth/RequireRole";
import Sidebar from "@/components/owner/SideBar";
export default function OwnerLayout({ children }: { children: React.ReactNode }) {
  return (
    <RequireRole role="ROLE_OWNER">
      <div className="h-screen flex flex-col bg-slate-950 text-white">
        <div className="flex flex-1 overflow-hidden">
          <main className="flex-1 overflow-y-auto p-6">
            {children}
          </main>
        </div>
      </div>
    </RequireRole>
  );
}
