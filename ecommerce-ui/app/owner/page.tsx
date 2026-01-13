"use client";

import React, { useState } from "react";
import TopBar from "@/components/owner/TopBar";
import RequireRole from "@/components/auth/RequireRole";
import Sidebar from "@/components/owner/SideBar";
import OwnerDashboard from "@/components/owner/WorkPlace";

export default function OwnerLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const [open, setOpen] = useState(false);
  const [active, setActive] = React.useState("Dashboard");
  return (
    <RequireRole role="ROLE_OWNER">
      {/* ROOT */}
      <div className="h-screen flex flex-col bg-slate-950 text-white">
        {/* TOP BAR */}
        <div className="bg-slate-900 border-b border-slate-800">
          <TopBar onMenuClick={() => setOpen(true)} />
        </div>

        {/* BODY */}
        <div className="flex flex-1 overflow-hidden relative">
          {/* SIDEBAR DESKTOP */}
          <aside className="hidden md:block w-64 bg-slate-900 border-r border-slate-800">
            Side bar
            <Sidebar onSelect = {setActive}/>
          </aside>

          {/* MAIN CONTENT */}
          <aside className="w-full h-full">
            <OwnerDashboard active={active}>{children}</OwnerDashboard>
          </aside>

          {/* SIDEBAR MOBILE */}
          <aside
            className={`
              fixed top-0 left-0 h-full w-64
              bg-slate-900 z-50
              transform transition-all duration-300
              ${
                open
                  ? "translate-x-0 opacity-100"
                  : "-translate-x-full opacity-0"
              }
              md:hidden
            `}
          >
            <Sidebar onSelect={setActive}/>
          </aside>
          {/* OVERLAY */}
          {open && (
            <div
              onClick={() => setOpen(false)}
              className="fixed inset-0 bg-black/50 backdrop-blur-sm z-40 md:hidden"
            />
          )}
        </div>
      </div>
    </RequireRole>
  );
}
