// data.service.js
const client = require('../Config/data.config');

class DataService {
  constructor(tableName) {
    this.table = tableName;
  }

  async findAll() {
    const result = await client.query(`SELECT * FROM ${this.table}`);
    return result.rows;
  }

  async findById(id) {
    const result = await client.query(`SELECT * FROM ${this.table} WHERE id = $1`, [id]);
    return result.rows[0];
  }

  async create(data, fields) {
    const keys = fields.join(', ');
    const values = fields.map((_, i) => `$${i + 1}`).join(', ');
    const params = fields.map((key) => data[key]);

    const result = await client.query(
      `INSERT INTO ${this.table} (${keys}) VALUES (${values}) RETURNING *`,
      params
    );
    return result.rows[0];
  }

  async update(id, data, fields) {
    const updates = fields.map((key, i) => `${key} = $${i + 1}`).join(', ');
    const params = fields.map((key) => data[key]);
    params.push(id); // cuối cùng là id

    const result = await client.query(
      `UPDATE ${this.table} SET ${updates} WHERE id = $${fields.length + 1} RETURNING *`,
      params
    );
    return result.rows[0];
  }

  async deleteById(id) {
    await client.query(`DELETE FROM ${this.table} WHERE id = $1`, [id]);
    return { message: `🗑️ Đã xóa ${this.table} với id ${id}` };
  }
}

module.exports = DataService;
