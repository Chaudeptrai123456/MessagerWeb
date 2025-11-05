const express = require("express");
const cors = require("cors");
const morgan = require("morgan");
const dotenv = require("dotenv");
const cookieParser = require("cookie-parser");

const productRoute = require("./src/Route/product.route");
const authRoute = require("./src/Route/auth.route");
require("./src/Config/data.config");

dotenv.config();
const app = express();
app.use(cors());
app.use(morgan("dev"));
app.use(express.json());
app.use(cookieParser());

// Routes
app.use("/products", productRoute);
app.use("/", authRoute);

app.get("/", (req, res) => {
  res.send(`
    <h2>Node.js OAuth2 Client</h2>
    <a href="/login">Đăng nhập với Authorization Server</a>
  `);
});

const PORT = process.env.PORT || 8081;
app.listen(PORT, () =>
  console.log(`✅ Node OAuth2 client running at http://localhost:${PORT}`)
);
