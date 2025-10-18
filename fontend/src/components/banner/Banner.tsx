import { useEffect, useState } from 'react';
import axiosInstance from '../../utils/axiosInstance';
import { API_PATHS } from '../../utils/apiPath';

interface Discount {
  id: string;
  percentage: number;
  previousPrice: number;
  reducedPrice: number;
  endDate: string | null;
}

interface Feature {
  id: string;
  name: string;
  value: string;
}

interface Image {
  id: string;
  url: string;
}

interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  category: string;
  features: Feature[];
  discounts: Discount[];
  currentPrice: number;
  currentDiscountPercentage: number;
  images: Image[];
}

export default function Banner() {
  const [productList, setProductList] = useState<Product[]>([]);

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const localData = localStorage.getItem('productList');
      if (localData) {
        const parsed = JSON.parse(localData);
        const now = Date.now();
        const thirtyMinutes = 30 * 60 * 1000;
        console.log("lấy từ localstorage")
        if (now - parsed.timestamp < thirtyMinutes) {
            // ✅ Dữ liệu còn hạn
            setProductList(parsed.data);
            return;
        } else {
            // ❌ Hết hạn → xóa và gọi API
            localStorage.removeItem('productList');
        }
      }
        const response = await axiosInstance.get(API_PATHS.PRODUCT.TOP_DISCOUNT);
        localStorage.setItem('productList', JSON.stringify(response.data));
        setProductList(response.data);
      } catch (error) {
        console.error('Lỗi khi gọi API:', error);
      }
    };

    fetchProducts();
  }, []);

 return (
  <div className="banner">
    <h2 className="banner-title">🎉 Danh sách sản phẩm giảm giá nhiều nhất</h2>
    <ul className="product-list">
      {productList.map((product) => (
        <li key={product.id} className="product-card">
          <div className="product-info">
            <p><strong>Tên:</strong> {product.name}</p>
            <p><strong>Mô tả:</strong> {product.description}</p>
            <p><strong>Giá gốc:</strong> {product.price} $</p>
            <p><strong>Giá hiện tại:</strong> {product.currentPrice} $</p>
          </div>

          <div className="product-images">
            <strong>📌 Hình ảnh:</strong>
            {product.images.length > 0 ? (
              <ul className="image-list">
                {product.images.map((image) => (
<li className="image-item">
  <img
    className="product-image"
    src={image.url}
    alt={`Ảnh sản phẩm ${product.name}`}
  />
</li>
                ))}
              </ul>
            ) : (
              <p className="no-image">Không có ảnh</p>
            )}
          </div>
        </li>
      ))}
    </ul>
  </div>
);
}
