function auth_user(req, res, next) {
  try {
    const token =
      req.cookies?.token ||
      req.headers.authorization?.replace(/^Bearer\s+/i, "");

    if (!token) {
      console.warn("⚠️ Missing token. Redirecting to login...");
      return res.redirect("/login");
    }
    console.log("test auth server " + token)
    req.accessToken = token;
    next();
  } catch (err) {
    console.error("❌ Error checking token:", err.message);
    return res.redirect("/login");
  }
}

module.exports = auth_user; // ✅ Export trực tiếp function
