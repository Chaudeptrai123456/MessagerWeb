const express = require("express");
const router = express.Router();
const {handleMakingOrder} = require("../Controller/order.controller")
const auth_user = require("../Middleware/auth_user")
router.get("/make",auth_user,handleMakingOrder)
module.exports = router 