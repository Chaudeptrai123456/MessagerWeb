const express = require("express");
const router = express.Router();
const {handleMakingOrder,getAllOrderByEmail} = require("../Controller/order.controller")
const auth_user = require("../Middleware/auth_user")
router.post("/make",auth_user,handleMakingOrder)
router.get("/get",auth_user,getAllOrderByEmail)
module.exports = router 