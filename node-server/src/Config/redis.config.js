const redis = require("redis");
const dotenv = require('dotenv');
const envFile = process.env.NODE_ENV === 'docker' ? '.env.docker' : '.env';
dotenv.config({ path: envFile });

const redisClient = redis.createClient({
  url: process.env.REDIS_URL,
});

redisClient.connect()
  .then(() => console.log("✅ Redis connected"))
  .catch(err => console.error("❌ Redis connection error:", err));

module.exports = redisClient;
