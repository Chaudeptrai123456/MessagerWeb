const express = require("express");
const router = express.Router();
const authController = require("../Controller/auth.controller");

router.get("/login", authController.login);
router.get("/login/oauth2/code/messenger", authController.callback);
router.get("/protected", authController.protected);

module.exports = router;