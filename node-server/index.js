const express = require("express");
const cors = require("cors");
const morgan = require("morgan");
const dotenv = require("dotenv");
const cookieParser = require("cookie-parser");
const {connectKafka}= require("./src/Config/kafka.config")
const productRoute = require("./src/Route/product.route");
const authRoute = require("./src/Route/auth.route");
const orderRoute = require("./src/Route/order.route")
require("./src/Config/data.config");
const client = require('prom-client');
const app = express();
const register = new client.Registry();

client.collectDefaultMetrics({ register });

app.get('/metrics', async (req, res) => {
  res.set('Content-Type', register.contentType);
  res.end(await register.metrics());
});

dotenv.config();
app.use(cors({
  origin: "*",      // Hoặc domain FE
  methods: "GET,POST,PUT,DELETE",
  credentials: true
}));

app.use(morgan("dev"));
app.use(express.json());
app.use(cookieParser());

// Routes
app.use("/api/service/products", productRoute);
app.use("/api/service/order",orderRoute)
app.use("/", authRoute);

app.get("/", (req, res) => {
  res.send(`
    <h2>Node.js OAuth2 Client</h2>
    <a href="/login">Đăng nhập với Authorization Server</a>
  `);
});
const PORT = process.env.PORT || 8081;
app.listen(PORT, "0.0.0.0", async () => {
  await connectKafka();
  console.log(`Running on 0.0.0.0:${PORT}`);
});

