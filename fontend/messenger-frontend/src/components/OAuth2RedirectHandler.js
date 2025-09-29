import React, { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ACCESS_TOKEN } from '../App';

const OAuth2RedirectHandler = () => {
    const location = useLocation();
    const navigate = useNavigate();

    useEffect(() => {
        const getUrlParameter = (name) => {
            name = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
            const regex = new RegExp('[\\?&]' + name + '=([^&#]*)');
            const results = regex.exec(location.search);
            return results === null ? '' : decodeURIComponent(results[1].replace(/\+/g, ' '));
        };

        const token = getUrlParameter('token');

        if (token) {
            localStorage.setItem(ACCESS_TOKEN, token);
            // Sau khi lưu token, chuyển hướng đến trang profile
            navigate('/profile', { replace: true });
        } else {
            // Nếu không có token, quay về trang login
            navigate('/login', { replace: true });
        }
    }, [location, navigate]);

    return (
        <div className="container">
            <h1>Đang xử lý...</h1>
            <p>Vui lòng chờ trong giây lát.</p>
        </div>
    );
};

export default OAuth2RedirectHandler;