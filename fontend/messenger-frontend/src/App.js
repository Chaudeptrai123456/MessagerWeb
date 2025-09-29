import React from 'react';
import { Route, Routes } from 'react-router-dom';
import Login from './components/Login';
import Profile from './components/Profile';
import OAuth2RedirectHandler from './components/OAuth2RedirectHandler';
import './App.css';

// Các hằng số quan trọng
export const API_BASE_URL = 'http://localhost:9999';
export const ACCESS_TOKEN = 'accessToken';
export const OAUTH2_REDIRECT_URI = 'http://localhost:3000/oauth2/redirect';
export const GOOGLE_AUTH_URL = API_BASE_URL + '/oauth2/authorize/google?redirect_uri=' + OAUTH2_REDIRECT_URI;

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/profile" element={<Profile />} />
      <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />
      {/* Route mặc định sẽ là trang login */}
      <Route path="/" element={<Login />} />
    </Routes>
  );
}

export default App;