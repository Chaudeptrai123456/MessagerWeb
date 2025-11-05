const express = require("express")
const auth_user = require("../Middleware/auth_user")
const router = express.Router()
const {
  getAllCategories,
  getProductById,
  getAllProducts,
  searchProducts,
  getTopDiscountProducts,
} = require("../Controller/product.controller")
router.get("/get",auth_user,getAllProducts)
// router.put("/uploadProfileUrl",protect, updateUserProfile)
module.exports = router 