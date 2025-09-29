import React from 'react';
import { GOOGLE_AUTH_URL } from '../App';
// import googleLogo from './google-logo.png'; // Tải một ảnh logo Google và đặt tên là google-logo.png

const Login = () => {
    return (
        <div className="container">
            <h1>Đăng nhập</h1>
            <p>Sử dụng tài khoản Google của bạn để tiếp tục.</p>
            <a className="google-btn" href={GOOGLE_AUTH_URL}>
                <img src="https://upload.wikimedia.org/wikipedia/commons/c/c1/Google_%22G%22_logo.svg" alt="Google logo" />
                Đăng nhập với Google
            </a>
        </div>
    );
};

export default Login;