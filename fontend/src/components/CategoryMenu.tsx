import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

const CategoryMenu = () => {
  const [categories, setCategories] = useState([]);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const res = await fetch('/api/categories');
        const data = await res.json();
        setCategories(data);
      } catch (err) {
        console.error('Lỗi khi lấy danh sách category:', err);
      }
    };
    fetchCategories();
  }, []);

  return (
    <li className="menu-item">
      CATEGORY <span className="arrow">▼</span>
      <ul className="dropdown">
        {categories.map(cat => (
          <li key={cat.id}>
            <Link to={`/category/${cat.id}`}>{cat.name}</Link>
          </li>
        ))}
      </ul>
    </li>
  );
};

export default CategoryMenu;
