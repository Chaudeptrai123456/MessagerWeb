import { useEffect, useState } from "react";
import { getAllCategories } from "../../axios/categoryService";

interface Category {
  id: number;
  name: string;
}

interface CategoryListProps {
  className?: string;
}
    
function CategoryList({ className }: CategoryListProps) {
  const [categories, setCategories] = useState<Category[]>([]);

  useEffect(() => {
    const fetchCategories = async () => {
      const cached = localStorage.getItem("categories");

      if (cached) {
        setCategories(JSON.parse(cached));
        return;
      }

      try {
        const data = await getAllCategories();
        setCategories(data);
        localStorage.setItem("categories", JSON.stringify(data)); // ✅ lưu cache
      } catch (error) {
        console.error("Lỗi khi fetch categories:", error);
      }
    };

    fetchCategories();
  }, []);

  return (
    <div className={`category-wrapper ${className || ""}`}>
      <span className="arrow">CATEGORY ▼</span>
      <ul className="dropdown">
        {categories.map((cat) => (
          <li key={cat.id}>
            <a href="#">{cat.name}</a>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default CategoryList;
