import axios from 'axios';
import { useEffect, useState } from 'react';
import axiosInstance from '../utils/axiosInstance';
import { API_PATHS } from '../utils/apiPath';
import Banner from '../components/banner/Banner';

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
  contentType: string;
}

interface Product {
  id: string;
  name: string;
  description: string;
  price:number
  category: string;
  features: Feature[];
  discounts: Discount[];
  currentPrice: number;
  currentDiscountPercentage: number;
  image: Image;
}

const ProductListComponent = () => {
  const [productList, setProductList] = useState<Product[]>([]);

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const response = await axiosInstance.get(API_PATHS.PRODUCT.TOP_DISCOUNT);
        console.log(response.data)
        setProductList(response.data);
      } catch (error) {
        console.error('Lỗi khi gọi API:', error);
      }
    };

    fetchProducts();
  }, []);

  return (
    <div className='home-container'>
        <Banner />
    </div>
  );
};

export default ProductListComponent;
