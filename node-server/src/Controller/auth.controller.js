const axios = require("axios");
const querystring = require("querystring");
const cookieParser = require("cookie-parser");
const { AUTH_URL, API_PATHS } = require("../../utils/apiPath");
const productService = require("../Service/ProductService");

const CLIENT_ID = "chau";
const CLIENT_SECRET = "123";
const REDIRECT_URI = API_PATHS.AUTH.REDIRECT_URL;

const TOKEN_ENDPOINT = `${AUTH_URL}/oauth2/token`;

exports.login = (req, res) => {
  const authorizeUrl =
    `${AUTH_URL}/oauth2/authorize?` +
    querystring.stringify({
      response_type: "code",
      client_id: CLIENT_ID,
      scope: "openid profile email",
      redirect_uri: REDIRECT_URI,
    });
  res.redirect(authorizeUrl);
};

exports.callback = async (req, res) => {
  const code = req.query.code;
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
    res.cookie("token", access_token, {
      httpOnly: true,
      secure: false,
      sameSite: "lax",
    });

    res.redirect("/protected");
  } catch (err) {
    console.error("❌ OAuth2 Error:", err.response?.data || err.message);
    res.status(500).json({ error: "Token exchange failed" });
  }
};

exports.protected = async (req, res) => {
  const result = await productService.getAllProducts(1, 10);
  res.json({
    message: "Đăng nhập thành công!",
    products: result,
  });
};
