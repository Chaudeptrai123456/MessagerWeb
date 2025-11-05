const dotenv = require('dotenv');
const envFile = process.env.NODE_ENV === 'docker' ? '.env.docker' : '.env';
dotenv.config({ path: envFile });
const BASE_URL = process.env.BASE_URL;
const AUTH_URL = process.env.AUTH_URL;
const API_PATHS = {
  AUTH: {
    TOKEN_ENDPOINT: `${AUTH_URL}/oauth2/token`,
    LOGIN: `${AUTH_URL}/login`,
    USER_PROFILE: `${AUTH_URL}/userinfo`,
    REDIRECT_URL: `${process.env.BASE_URL_CALLBACK}/login/oauth2/code/messenger`,
  },
};

module.exports = {
  BASE_URL,
  AUTH_URL,
  API_PATHS,
};
  