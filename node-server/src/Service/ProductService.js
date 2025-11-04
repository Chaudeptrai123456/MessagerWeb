// src/Service/product.service.js
const client = require('../Config/data.config');

class ProductService {
  async getAllCategories() {
    const result = await client.query('SELECT * FROM categories');
    return result.rows;
  }

  async getProductById(id) {
    const result = await client.query('SELECT * FROM product WHERE id = $1', [id]);
    return result.rows[0];
  }
  async getAllProducts(page, size) {
  const offset = page * size;
  const countResult = await client.query('SELECT COUNT(*) FROM product');
  const totalItems = parseInt(countResult.rows[0].count);
  const result = await client.query(`
  SELECT 
    p.name,
    p.description,
    p.price,
    p.quantity,
    p.created_at,
    p.update_at,
    
    -- Category (1-1)
    json_build_object(
      'name', c.name,
      'description', c.description
    ) AS category,

    -- Features (1-n)
    COALESCE(
      json_agg(
        DISTINCT jsonb_build_object(
          'name', f.name,
          'value', f.value
        )
      ) FILTER (WHERE f.id IS NOT NULL),
      '[]'
    ) AS features,

    -- Images (1-n)
    COALESCE(
      json_agg(
        DISTINCT jsonb_build_object(
          'filename', i.filename,
          'contentType', i.content_type,
          'url', i.url
        )
      ) FILTER (WHERE i.id IS NOT NULL),
      '[]'
    ) AS images,

    -- Discounts (1-n)
    COALESCE(
      json_agg(
        DISTINCT jsonb_build_object(
          'percentage', d.percentage,
          'startDate', d.start_date,
          'endDate', d.end_date
        )
      ) FILTER (WHERE d.id IS NOT NULL),
      '[]'
    ) AS discounts

    FROM product p
    LEFT JOIN category c ON p.category_id = c.id
    LEFT JOIN feature f ON p.id = f.product_id
    LEFT JOIN image i ON p.id = i.product_id
    LEFT JOIN discount d ON p.id = d.product_id
    GROUP BY p.id, c.id
    ORDER BY p.created_at DESC
    LIMIT $1 OFFSET $2
  `, [size, offset]);


  return {
    products: result.rows,
    currentPage: page,
    totalItems,
    totalPages: Math.ceil(totalItems / size),
  };
}


  async searchProducts({ categoryId, minPrice, maxPrice, featureName, featureValue, page = 0, size = 10 }) {
    const offset = page * size;
    const query = `
      SELECT DISTINCT p.* FROM product p
      LEFT JOIN product_features f ON p.id = f.product_id
      WHERE 
        ($1 IS NULL OR p.category_id = $1)
        AND ($2 IS NULL OR p.price >= $2)
        AND ($3 IS NULL OR p.price <= $3)
        AND ($4 IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', $4, '%')))
        AND ($5 IS NULL OR LOWER(f.value) LIKE LOWER(CONCAT('%', $5, '%')))
      ORDER BY p.created_at DESC
      LIMIT $6 OFFSET $7
    `;

    const result = await client.query(query, [
      categoryId || null,
      minPrice || null,
      maxPrice || null,
      featureName || null,
      featureValue || null,
      size,
      offset,
    ]);

    const countResult = await client.query('SELECT COUNT(*) FROM product');
    const totalItems = parseInt(countResult.rows[0].count);

    return {
      products: result.rows,
      currentPage: page,
      totalItems,
      totalPages: Math.ceil(totalItems / size),
    };
  }

  async getTopDiscountProducts(limit = 7) {
    const result = await client.query(
      'SELECT * FROM product ORDER BY discount DESC NULLS LAST LIMIT $1',
      [limit]
    );
    return result.rows;
  }
}

module.exports = new ProductService();
  // SELECT 
  //   p.id,
  //   p.name,
  //   p.description,
  //   p.price,
  //   p.quantity,
  //   p.created_at,
  //   p.update_at,
    
  //   -- Category (1-1)
  //   json_build_object(
  //     'id', c.id,
  //     'name', c.name,
  //     'description', c.description
  //   ) AS category,

    // -- Features (1-n)
    // COALESCE(
    //   json_agg(
    //     DISTINCT jsonb_build_object(
    //       'id', f.id,
    //       'name', f.name,
    //       'value', f.value
    //     )
    //   ) FILTER (WHERE f.id IS NOT NULL),
    //   '[]'
    // ) AS features,

    // -- Images (1-n)
    // COALESCE(
    //   json_agg(
    //     DISTINCT jsonb_build_object(
    //       'id', i.id,
    //       'filename', i.filename,
    //       'contentType', i.content_type,
    //       'url', i.url
    //     )
    //   ) FILTER (WHERE i.id IS NOT NULL),
    //   '[]'
    // ) AS images,

    // -- Discounts (1-n)
    // COALESCE(
    //   json_agg(
    //     DISTINCT jsonb_build_object(
    //       'id', d.id,
    //       'percentage', d.percentage,
    //       'startDate', d.start_date,
    //       'endDate', d.end_date
    //     )
    //   ) FILTER (WHERE d.id IS NOT NULL),
    //   '[]'
    // ) AS discounts

    // FROM product p
    // LEFT JOIN category c ON p.category_id = c.id
    // LEFT JOIN feature f ON p.id = f.product_id
    // LEFT JOIN image i ON p.id = i.product_id
    // LEFT JOIN discount d ON p.id = d.product_id
    // GROUP BY p.id, c.id
    // ORDER BY p.created_at DESC
    // LIMIT $1 OFFSET $2