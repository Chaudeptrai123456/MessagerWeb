const { API_PATHS } = require("../../utils/apiPath");
const axiosInstance = require("../../utils/axiosInstance");
const productService = require("../Service/ProductService");
const recommendationEngine = async (req, res) => {
  try {
    const token =
      req.cookies?.token ||
      req.headers.authorization?.replace(/^Bearer\s+/i, "") ||
      req.accessToken;

    if (!token) {
      return res.status(401).json({ error: "Thiếu token đăng nhập" });
    }

    const response = await axiosInstance.post(API_PATHS.RECOMMENT.GET, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    const result = response.data;
    res.status(200).json({ result });

  } catch (err) {
    console.error("❌ Lỗi gọi Python server:", err.message);
    res.status(500).json({ message: err.message || "Lỗi gì á không biết" });
  }
};
const createProduct = async(req,res)=>{
  try {
    const productRequest = {
        name: req.body.name,
        description: req.body.description,
        price:req.body.price,
        quantity: req.body.quantity,
        categoryId: req.categoryId,
        images: req.body.images,
        features: null,
        imagesBase64:null

    }
    await producer.send({
      topic:"create-udpate-product",
      messages:[
        {
          key: 'product',
          value: productRequest
        }
      ]
    })
    return res.status(200).json({"message":"test create product"})
  }catch(err) {
    return res.status(500).json({err})
  }
}
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
    const token =
      req.cookies?.token ||
      req.headers.authorization?.replace(/^Bearer\s+/i, "") ||
      req.accessToken;
    let description = req.body.description
    const response = await axiosInstance.post(API_PATHS.SEARCH.GET, {
      description: description
    }, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });   
    res.status(200).json(response.data);
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
  recommendationEngine,
  createProduct
};