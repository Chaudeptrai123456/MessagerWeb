const BASE_URL = "http://localhost:8081";
const AUTH_URL = "http://localhost:9999";

const API_PATHS = {
  AUTH: {
    TOKEN_ENDPOINT: `${AUTH_URL}/oauth2/token`,
    LOGIN: `${AUTH_URL}/login`,
    USER_PROFILE: `${AUTH_URL}/userinfo`,
    REDIRECT_URL: `http://localhost:8081/login/oauth2/code/messenger`,
  },
};

module.exports = {
  BASE_URL,
  AUTH_URL,
  API_PATHS,
};
