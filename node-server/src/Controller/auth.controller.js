const axios = require("axios");
const querystring = require("querystring");
const productService = require("../Service/ProductService");
const { AUTH_URL, API_PATHS } = require("../../utils/apiPath");
const dotenv = require('dotenv');
const envFile = process.env.NODE_ENV === 'docker' ? '.env.docker' : '.env';
dotenv.config({ path: envFile });
const CLIENT_ID = "chau";
const CLIENT_SECRET = "123";
const REDIRECT_URI = API_PATHS.AUTH.REDIRECT_URL;
const TOKEN_ENDPOINT = `${AUTH_URL}/oauth2/token`;

// ===================== LOGIN =====================
exports.login = (req, res) => {
  // 🟢 Lấy lại trang mà user muốn vào ban đầu
  const returnUrl = req.query.returnUrl || "/";
  console.log("Redirect URI:", REDIRECT_URI);

  const authorizeUrl =
    `${AUTH_URL}/oauth2/authorize?` +
    querystring.stringify({
      response_type: "code",
      client_id: CLIENT_ID,
      scope: "openid profile email",
      redirect_uri: REDIRECT_URI,
      state: returnUrl, // ✅ Lưu lại trang gốc
    });

  return res.redirect(authorizeUrl);
};

// ===================== CALLBACK =====================
exports.callback = async (req, res) => {
  const code = req.query.code;
  const returnUrl = req.query.state || "/"; // ✅ Lấy lại trang đã lưu
  if (!code) return res.status(400).send("Missing authorization code");

  try {
    const tokenResponse = await axios.post(
      TOKEN_ENDPOINT,
      querystring.stringify({
        grant_type: "authorization_code",
        code,
        redirect_uri: REDIRECT_URI,
      }),
      {
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
          Authorization:
            "Basic " +
            Buffer.from(`${CLIENT_ID}:${CLIENT_SECRET}`).toString("base64"),
        },
      }
    );

    const { access_token } = tokenResponse.data;

    // ✅ Set cookie
    res.cookie("token", access_token, {
      httpOnly: true,
      secure: false,
      sameSite: "lax",
      domain: process.env.DOMAIN_COOKIE,

    });

    console.log("✅ Đăng nhập thành công → Redirect:", returnUrl);

    // ✅ Quay lại đúng trang ban đầu
    return res.redirect(returnUrl);

  } catch (err) {
    console.error("❌ OAuth2 Error:", err.response?.data || err.message);
    return res.status(500).json({ error: "Token exchange failed" });
  }
};

// ===================== PROTECTED =====================
exports.protected = async (req, res) => {
  const result = await productService.getAllProducts(1, 10);
  return res.json({
    message: "Đăng nhập thành công!",
    products: result,
  });
};
