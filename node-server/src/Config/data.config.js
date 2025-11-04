const { Client } = require('pg')

const client = new Client ({
    host: 'localhost',
    port: 5432,
    user: 'readonly_user',
    password: 'password123',
    database: 'ecommerce'
});
client.connect().then(()=>{
    console.log('connect to PostgreSQL successfull');
})
.catch(err=>{
    console.log('error ' + err.message)
})

module.exports = client;