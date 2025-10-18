import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import About from "./pages/About";
import MainLayout from "./layout/MainLayout";
import { useEffect } from "react";
import { ensureOAuthLogin } from "../src/axios/ensureOAuthLogin";


export default function App() {
    useEffect(() => {
    ensureOAuthLogin(); // 👈 tự login nếu chưa có token
  }, []);
  return (
    <Router>
      <MainLayout >
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/about" element={<About />} />
          <Route path="/products" element={<div>Product</div>} />
        </Routes>
      </MainLayout>
    </Router>
  );
}
