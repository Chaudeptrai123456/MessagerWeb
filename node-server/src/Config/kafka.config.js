const { Kafka, logLevel } = require("kafkajs");
const dotenv = require("dotenv")
const envFile = process.env.NODE_ENV === 'docker' ? '.env.docker' : '.env';
dotenv.config({ path: envFile });
const kafka = new Kafka(
    {
        clientId: 'user-service',
        brokers:[`${process.env.KAFKA_HOST}:${process.env.KAFKA_PORT}`],
        logLevel: logLevel.INFO,
    }
)
const producer = kafka.producer({
  allowAutoTopicCreation: true,
});

// Tạo consumer
const consumer = kafka.consumer({
  groupId: process.env.KAFKA_CONSUMER_GROUP_ID, // đổi tùy service
});

// Hàm connect + log
async function connectKafka() {
  try {
    console.log("🔌 Connecting to Kafka...");
    await producer.connect();
    await consumer.connect();
    console.log("✅ Kafka connected!");
  } catch (error) {
    console.error("❌ Kafka connection error:", error);
  }
}

module.exports = {
  kafka,
  producer,
  consumer,
  connectKafka,
};