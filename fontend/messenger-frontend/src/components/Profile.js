import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { API_BASE_URL, ACCESS_TOKEN } from '../App';

const Profile = () => {
    const [user, setUser] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem(ACCESS_TOKEN);
        if (!token) {
            // Nếu không có token trong localStorage, đá về trang login
            navigate('/login');
            return;
        }

        // Tạo một instance của axios với header Authorization
        // để gửi kèm token trong mỗi request
        const apiClient = axios.create({
            baseURL: API_BASE_URL,
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        // Gọi đến một endpoint bảo mật trên backend để lấy thông tin user
        // Em cần đảm bảo backend có endpoint này nhé!
        apiClient.get('/user/me')
            .then(response => {
                setUser(response.data);
            })
            .catch(error => {
                // Nếu có lỗi (ví dụ token hết hạn), xóa token và quay về trang login
                console.error("Lỗi khi lấy thông tin người dùng:", error);
                localStorage.removeItem(ACCESS_TOKEN);
                navigate('/login');
            });

    }, [navigate]);

    const handleLogout = () => {
        localStorage.removeItem(ACCESS_TOKEN);
        navigate('/login');
    };

    if (!user) {
        return <div className="container"><h1>Đang tải...</h1></div>;
    }

    return (
        <div className="container">
            {/* Đây chính là dòng chữ em yêu cầu */}
            <h1>Xin chào {user.email}</h1>
            
            <p>Chào mừng bạn đã đăng nhập thành công!</p>
            <p>Tên của bạn là: <strong>{user.name}</strong></p>
            <button onClick={handleLogout} className="logout-btn">Đăng xuất</button>
        </div>
    );
};

export default Profile;