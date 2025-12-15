// controller.js
const express = require("express");
const authController = require("./auth.controller");
const auth_user = require("../Middleware/auth_user");
const router = express.Router();

router.use("/",auth_user,authController);
module.exports = router;
