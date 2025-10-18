import React, { useState } from "react";
import "./Navbar.css";
import Avatar from "./Avatar/Avatar";
import logo from "../../assets/logo.png";
import SearchInput from "../input/SearchInput";
export default function Navbar() {
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <nav className="navbar">
      <div className="navbar-container">
        {/* 🪪 Logo bên trái */}
        <div className="navbar-logo">
          <img src={logo} alt="My Logo" className="navbar-logo" />
        </div>

        {/* 📋 Menu ở giữa */}
        <ul className={`nav-links ${menuOpen ? "open" : ""}`}>
          <li><a href="#">Home</a></li>
          <li><a href="#">Shop</a></li>
          <li><a href="#">About</a></li>
          <li><a href="#">Contact</a></li>
        </ul>

        {/* 🔍 Search + Avatar bên phải */}
        <div className="navbar-search-avatar">
          <SearchInput />
          <Avatar />
        </div>

        {/* 🍔 Burger (ẩn khi to, hiện khi nhỏ) */}
        <div
          className={`burger ${menuOpen ? "active" : ""}`}
          onClick={() => setMenuOpen(!menuOpen)}
        >
          <div></div>
          <div></div>
          <div></div>
        </div>
      </div>
    </nav>
  );
}
