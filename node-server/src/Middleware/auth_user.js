const axios = require("axios");

async function auth_user(req, res, next) {
  try {
    const token =
      req.cookies?.token ||
      req.headers.authorization?.replace(/^Bearer\s+/i, "");

    if (!token) {
      console.warn("⚠️ Missing token. Redirecting to login...");
      return res.redirect(
        `/login?returnUrl=${encodeURIComponent(req.originalUrl)}`
      );
    }

    // 📌 Gọi auth server lấy user info
    const response = await axios.get(
      `${process.env.AUTH_URL}/api/user/info`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
        timeout: 5000,
      }
    );
    console.log("✅ auth_user success:", response.data);
    const userInfo = response.data;
    // 📌 Gắn user vào req
    req.accessToken = token;
    req.user = {
      email: userInfo.email,
      sub: userInfo.sub,
      name: userInfo.name,
    };

    next();
  } catch (err) {
    console.error(
      "❌ Token invalid or auth server error:",
      err.response?.data || err.message
    );

    // ❌ Token sai / hết hạn → xóa cookie và login lại
    res.clearCookie("token", {
      domain: process.env.DOMAIN_COOKIE,
    });

    return res.redirect(
      `/login?returnUrl=${encodeURIComponent(req.originalUrl)}`
    );
  }
}

module.exports = auth_user;
