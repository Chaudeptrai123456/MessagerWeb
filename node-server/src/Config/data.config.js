const dotenv = require('dotenv');
const envFile = process.env.NODE_ENV === 'docker' ? '.env.docker' : '.env';
dotenv.config({ path: envFile });

const { Client } = require('pg');

const client = new Client({
  host: process.env.DB_HOST,
  port: parseInt(process.env.DB_PORT),
  user: process.env.DB_USER,
  password: process.env.DB_PASS,
  database: process.env.DB_NAME,
});

client.connect()
  .then(() => console.log('✅ Connected to PostgreSQL' + " host" +  process.env.DB_HOST +" "))
  .catch(err => console.error('❌ DB Connection Error:'+ '✅ Connected to PostgreSQL' + " host " +  process.env.DB_HOST +":"+process.env.DB_USER, err.message));
module.exports = client;