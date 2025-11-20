const dotenv = require('dotenv');
const envFile = process.env.NODE_ENV === 'docker' ? '.env.docker' : '.env';
dotenv.config({ path: envFile });
const BASE_URL = process.env.BASE_URL;
const AUTH_URL = process.env.AUTH_URL;
const AI_URL = process.env.AI_URL;
const API_PATHS = {
  RECOMMENT:{
    GET: `${AI_URL}/recomments`,
  },
  SEARCH:{
    GET: `${AI_URL}/search`
  },
  AUTH: {
    TOKEN_ENDPOINT: `${AUTH_URL}/oauth2/token`,
    LOGIN: `${AUTH_URL}/login`,
    USER_PROFILE: `${AUTH_URL}/api/user/profile`,
    REDIRECT_URL: `${process.env.BASE_URL_CALLBACK}8081/login/oauth2/code/messenger`,
  },
  ORDER:{
    GET_BY_EMAIL: `${AUTH_URL}/api/orders/user`
  }
};  

module.exports = {
  BASE_URL,
  AUTH_URL,
  API_PATHS,
};
  