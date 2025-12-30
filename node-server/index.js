require("./src/Config/tracing"); 
const express = require("express");
const cors = require("cors");
const morgan = require("morgan");
const dotenv = require("dotenv");
const cookieParser = require("cookie-parser");
const client = require("prom-client");

const { connectKafka } = require("./src/Config/kafka.config");
require("./src/Config/data.config");

const productRoute = require("./src/Route/product.route");
const authRoute = require("./src/Route/auth.route");
const orderRoute = require("./src/Route/order.route");

dotenv.config();

const app = express();

/* ================= METRICS ================= */
const register = new client.Registry();
client.collectDefaultMetrics({ register });

app.get('/metrics', async (req, res) => {
  res.set('Content-Type', register.contentType);
  res.end(await register.metrics());
});

/* ================= MIDDLEWARE ================= */
app.use(cors({
  origin: "*",
  methods: "GET,POST,PUT,DELETE",
  credentials: true
}));

app.use(morgan("dev"));
app.use(express.json());
app.use(cookieParser());

/* ================= ROUTES ================= */
app.use("/api/service/products", productRoute);
app.use("/api/service/order", orderRoute);
app.use("/", authRoute);

app.get("/", (req, res) => {
  res.send("Node Service Running");
});

/* ================= START ================= */
const PORT = process.env.PORT || 8081;

app.listen(PORT, "0.0.0.0", async () => {
  await connectKafka();
  console.log(`Node service running on ${PORT}`);
});
