// Nếu chạy với Docker, REACT_APP_API_BASE_URL sẽ là rỗng và Nginx sẽ lo việc proxy.
// Nếu chạy React riêng (npm start), nó sẽ dùng http://localhost:9999
export const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:9999';

// URL đầy đủ mà frontend sẽ được redirect về sau khi đăng nhập thành công ở backend
export const OAUTH2_REDIRECT_URI = 'http://localhost:3000/oauth2/redirect';

// URL đầy đủ để bắt đầu luồng đăng nhập Google
// export const GOOGLE_AUTH_URL = API_BASE_URL + '/oauth2/authorize/google?redirect_uri=' + OAUTH2_REDIRECT_URI;

// Tên của key dùng để lưu JWT token trong localStorage
// export const ACCESS_TOKEN = 'accessToken';
export const GOOGLE_AUTH_URL = 'http://localhost:9999/oauth2/authorize/google?redirect_uri=http://localhost:3000/oauth2/redirect';
export const ACCESS_TOKEN = 'accessToken';
