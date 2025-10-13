import React from "react";
import { useUser } from "../context/UserContext";
import "./Avatar.css";
export default function Avatar() {
  const { user, loading } = useUser();

  if (loading) {
    return (
      <div className="w-8 h-8 rounded-full bg-gray-200 animate-pulse" />
    );
  }

  return (
    <div className="navbar-avatar flex items-center gap-2 cursor-pointer">
      {user?.avatar ? (
        <img
          src={user.avatar}
          alt={user.email}
          className="avatar"
        />
      ) : (
        <div className="w-8 h-8 rounded-full bg-gray-300 flex items-center justify-center text-white text-sm font-semibold">
          {user?.email ? user.email[0].toUpperCase() : "?"}
        </div>
      )}
    </div>
  );
}
