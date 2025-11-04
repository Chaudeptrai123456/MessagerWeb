const productService = require("../Service/ProductService");

// 🧠 Lấy tất cả categories
const getAllCategories = async (req, res) => {
  try {
    const categories = await productService.getAllCategories();
    res.status(200).json(categories);
  } catch (error) {
    res.status(500).json({ message: "Lỗi khi lấy danh sách category", error: error.message });
  }
};

// 🧠 Lấy sản phẩm theo ID
const getProductById = async (req, res) => {
  try {
    const product = await productService.getProductById(req.params.id);
    if (!product) return res.status(404).json({ message: "Không tìm thấy sản phẩm" });
    res.status(200).json(product);
  } catch (error) {
    res.status(500).json({ message: "Lỗi khi lấy sản phẩm", error: error.message });
  }
};

// 🧠 Lấy danh sách sản phẩm (có phân trang)
const getAllProducts = async (req, res) => {
  try {
    console.log("✅ test auth_user middleware");
    const { page = 0, size = 10 } = req.query;
    const result = await productService.getAllProducts(parseInt(page), parseInt(size));
    res.status(200).json(result);
  } catch (error) {
    res.status(500).json({ message: "Lỗi khi lấy danh sách sản phẩm", error: error.message });
  }
};

// 🧠 Tìm kiếm sản phẩm nâng cao
const searchProducts = async (req, res) => {
  try {
    const {
      categoryId,
      minPrice,
      maxPrice,
      featureName,
      featureValue,
      page = 0,
      size = 10,
    } = req.query;

    const result = await productService.searchProducts({
      categoryId,
      minPrice: minPrice ? parseFloat(minPrice) : null,
      maxPrice: maxPrice ? parseFloat(maxPrice) : null,
      featureName,
      featureValue,
      page: parseInt(page),
      size: parseInt(size),
    });

    res.status(200).json(result);
  } catch (error) {
    res.status(500).json({ message: "Lỗi khi tìm kiếm sản phẩm", error: error.message });
  }
};

// 🧠 Lấy top sản phẩm giảm giá
const getTopDiscountProducts = async (req, res) => {
  try {
    const products = await productService.getTopDiscountProducts();
    res.status(200).json(products);
  } catch (error) {
    res.status(500).json({ message: "Lỗi khi lấy sản phẩm giảm giá", error: error.message });
  }
};

module.exports = {
  getAllCategories,
  getProductById,
  getAllProducts,
  searchProducts,
  getTopDiscountProducts,
};
