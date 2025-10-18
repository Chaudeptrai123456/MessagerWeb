import React from "react";
import Header from "../components/header/Header";
import Banner from "../components/banner/Banner";

interface MainLayoutProps {
  children: React.ReactNode;
}

export default function MainLayout({ children }: MainLayoutProps) {
  return (
    <div className="layout-container">      
    <Header />
      <main>
        {children}
      </main>
    <div>
    </div>
    </div>
  );
}
