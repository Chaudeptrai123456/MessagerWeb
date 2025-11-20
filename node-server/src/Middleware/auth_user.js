// function auth_user(req, res, next) {
//   try {
//     const token = req.cookies?.token || req.headers.authorization?.replace(/^Bearer\s+/i, "");
//     if (!token) {
//       return res.redirect(`/login?returnUrl=${encodeURIComponent(req.originalUrl)}`);
//     }
//     req.token = token
//     // ✅ Lấy user từ cookie nếu có
//     if (req.cookies.user) {
//       req.user = JSON.parse(req.cookies.user);
//     }
//     next();
//   } catch (err) {
//     return res.redirect("/login");
//   }
// }

// module.exports = auth_user; // ✅ Export trực tiếp function
function auth_user(req, res, next) {
  try {
    const token =
      req.cookies?.token ||
      req.headers.authorization?.replace(/^Bearer\s+/i, "");
    console.log("auth service token : "+token)
    if (!token) {
      console.warn("⚠️ Missing token. Redirecting to login...");
      return res.redirect(`/login?returnUrl=${encodeURIComponent(req.originalUrl)}`);
    }
    req.accessToken = token;
    next();
  } catch (err) {
    console.error("❌ Error checking token:", err.message);
    return res.redirect("/login");
  }
}
module.exports = auth_user; // ✅ Export trực tiếp function