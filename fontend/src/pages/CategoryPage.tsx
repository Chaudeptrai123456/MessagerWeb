import { useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import axios from 'axios';

// Định nghĩa kiểu Product
type Product = {
  id: string;
  name: string;
  description: string;
  price: number;
};

const CategoryPage = () => {
 

  return (
    <div>
      Category
    </div>
  );
};

export default CategoryPage;
