import React from "react";
import "./SearchInput.css";
interface SearchInputProps {
  className?: string;
  placeholder?: string;
}

export default function SearchInput({ className, placeholder }: SearchInputProps) {
  return (
    <div className={`navbar__search-wrapper ${className || ""}`}>
      <img
        src="https://cdn-icons-png.flaticon.com/512/622/622669.png"
        alt="search"
        className="navbar__search-icon"
      />
      <input
        type="text"
        className="navbar__search-input"
        placeholder={placeholder || "Search..."}
      />
    </div>
  );
}