const {producer}  = require("../Config/kafka.config")
const handleMakingOrder = async (req, res) => {
  try {
    // const orderData = {
    // //   orderId: req.body.orderId,
    //   userId: req.body.userId,
    //   items: req.body.items,
    //   total: req.body.total,
    //   createdAt: new Date().toISOString(),
    // };
    const orderData = {
  "customerName": "Nguyễn Văn A",
  "customerEmail": "phamchaugiatu123@gmail.com",
  "address": "123 Đường ABC, Quận 1, TP.HCM",
  "items": [
    {
      "productId": "P001",
      "quantity": 2,
      "price": 150000
    },
    {
      "productId": "P002",
      "quantity": 1,
      "price": 200000
    }
  ]
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
    handleMakingOrder
}