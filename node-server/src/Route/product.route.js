const express = require("express")
const auth_user = require("../Middleware/auth_user")
const auth_admin = require("../Middleware/auth_admin")
const router = express.Router()
const {
  createProduct,
  getAllCategories,
  getProductById,
  getAllProducts,
  searchProducts,
  getTopDiscountProducts,
  recommendationEngine
} = require("../Controller/product.controller")
router.get("/get",auth_user,getAllProducts)
router.get("/top-discount",auth_user,getTopDiscountProducts)
router.get("/categories",getAllCategories)
router.get("/create",auth_admin,createProduct)
router.get("/recomment",auth_user,recommendationEngine)
router.get("/search",auth_user,searchProducts)
// router.put("/update")
// router.post("/addDiscount")get
// router.post("/addImage")
// router.put("/uploadProfileUrl",protect, updateUserProfile)
module.exports = router 