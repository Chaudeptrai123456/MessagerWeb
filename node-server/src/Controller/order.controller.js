const { API_PATHS } = require("../../utils/apiPath");
const axiosInstance = require("../../utils/axiosInstance");
const {producer}  = require("../Config/kafka.config")
const axios = require("axios")
const client = require("../Config/data.config");
const jwt = require("jsonwebtoken")
const getAllOrderByEmail = async (req, res) => {
  try {
    const email = jwt.decode(req.accessToken).email
    if (!email) {
      return res.status(400).json({ error: "Thiếu email" });
    }
    const query = `
    SELECT 
    o.id AS order_id,
    o.created_at,
    o.customer_name,
    o.customer_email,
    o.address,
    o.status,
    o.total_amount,
    oi.quantity,
    oi.price,
      p.name AS product_name,
      p.description AS product_description,
      p.price,
    i.url as images
    FROM orders o
    JOIN order_item oi ON o.id = oi.order_id
    JOIN product p ON oi.product_id = p.id
    JOIN image i On i.product_id = p.id
    WHERE o.customer_email = $1
    ORDER BY o.created_at DESC;
    `;
    const { rows } = await client.query(query, [email]);
    // Gom nhóm theo order_id
    const grouped = {};
    for (const row of rows) {
      const id = row.order_id;
      if (!grouped[id]) {
        grouped[id] = {
          id,
          createdAt: row.created_at,
          customerName: row.customer_name,
          customerEmail: row.customer_email,
          address: row.address,
          status: row.status,
          totalAmount: row.total_amount,
          items: []
        };
      }
      grouped[id].items.push({
        quantity: row.quantity,
        price: row.price,
        name: row.product_name,
        description: row.product_description,
        currentPrice: row.current_price,
        imageUrl: row.images
      });
    }

    const result = Object.values(grouped);
    res.status(200).json({ result });
  } catch (err) {
    console.error("❌ Lỗi truy vấn orders:", err.message);
    return res.status(500).json({ error: "Lỗi truy vấn", message: err.message });
  } 
};
module.exports = { getAllOrderByEmail };
const handleMakingOrder = async (req, res) => {
  try {
    const token = req.accessToken
    const decode = jwt.decode(token);
    const orderData = {
      "customerName": decode.username,
      "customerEmail": decode.sub,
      "address": "123 Đường ABC, Quận 1, TP.HCM",
      "items": [
        {
        "productId": "iphone_17_pro_plus_2025-11-01_ffebddea",
        "quantity": 2,
        "price": 150000
        },
        {
        "productId": "samsung_galaxy_s24_ultra_2025-11-01_df848063",
          "quantity": 1,
          "price": 200000
        }
      ],
      "token":token
    }   
    // Gửi message lên Kafka
    await producer.send({
      topic: 'analysis-topic',
      messages: [
        {
          key: 'order',
          value: JSON.stringify(orderData), // gửi dưới dạng JSON string
        },
      ],
    });
    console.log("📤 Sent order to Kafka:", orderData);
    res.status(200).json({ message: "Order sent to Kafka", data: orderData });
  } catch (error) {
    console.error("❌ Kafka send error:", error.message);
    res.status(500).json({ error: "Kafka send failed" });
  }
};
module.exports={
    handleMakingOrder,
    getAllOrderByEmail
}