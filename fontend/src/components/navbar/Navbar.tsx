import SearchInput from "../../components/input/SearchInput";
import "./Navbar.css";
import { useState } from "react";
import logo from "../../assets/logo.png";
import CategoryList from "../categoryList/CategoryList";
import Avatar from "../Avatar";
export default function Navbar() {
  const [menuOpen, setMenuOpen] = useState(false); // ✅ giữ lại để sau dùng cho responsive

  return (
    <header className="navbar">
      <div className="navbar-container">
        <div className="navbar-content">
          <div className="navbar-logo">
            <a href="/home" >
              <img src={logo} alt="Logo" />         
            </a>
          </div>
          <div className="navbar-menu-container">
            <ul className="navbar-menu">
              <li className="menu-item">
                HOME <span className="arrow">▼</span>
                <ul className="dropdown">
                  <li><a href="/products">Home 1</a></li>
                  <li><a href="#">Home 2</a></li>
                </ul>
              </li>

              <li className="menu-item">
                SHOP <span className="arrow">▼</span>
                <ul className="dropdown">
                  <li><a href="/about">Shop Grid</a></li> 
                  <li><a href="#">Shop List</a></li>
                </ul>
              </li>

              <li className="menu-item">
                <CategoryList className="navbar-category"/>
                {/* CATEGORY <span className="arrow">▼</span>
                <ul className="dropdown">
                  <li><a href="#">category 1</a></li>
                  <li><a href="#">category 2</a></li>
                </ul> */}
              </li>

              <li className="menu-item">
                PAGES <span className="arrow">▼</span>
                <ul className="dropdown">
                  <li><a href="#">Page 1</a></li>
                  <li><a href="#">Page 2</a></li>
                </ul>
              </li>

              <li className="menu-item">
                BLOG <span className="arrow">▼</span>
                <ul className="dropdown">
                  <li><a href="#">Blog 1</a></li>
                  <li><a href="#">Blog 2</a></li>
                </ul>
              </li>

              <li className="menu-item">
                ELEMENTS <span className="arrow">▼</span>
                <ul className="dropdown">
                  <li><a href="#">Element 1</a></li>
                  <li><a href="#">Element 2</a></li>
                </ul>
              </li>
            </ul>
          </div>
        </div>

        <div className="navbar-search-cart-container">
          <div className="navbar-search">
            <SearchInput className="navbar__search-btn"/>
          </div>
          <Avatar />
          <div className="navbar-cart">
            <img src="https://cdn-icons-png.flaticon.com/512/107/107831.png" alt="Cart" className="navbar-cart-icon"/>
            <span className="navbar-cart-count">0</span>
          </div>
        </div>
      </div>
    </header>
  );
}
