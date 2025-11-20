const jwt = require("jsonwebtoken");

function authAdmin(req, res, next) {
  try {
    const token = req.cookies.token;
    if (!token) {
      return res.status(401).json({ error: "Bạn chưa đăng nhập" });
    }

    // ✅ Giải mã token (không cần verify nếu chỉ decode)
    const decoded = jwt.decode(token);
    const roles = decoded?.roles || [];
    if (!roles.includes("ROLE_ADMIN")) {
      return res.status(403).json({ error: "Bạn không có quyền admin" });
    }
    // ✅ Gắn user vào req để dùng sau
    req.user = decoded;

    next();
  } catch (err) {
    console.log("❌ authAdmin error:", err.message);
    return res.status(500).json({ error: "Lỗi xác thực admin", message: err.message });
  }
}

module.exports = authAdmin;
