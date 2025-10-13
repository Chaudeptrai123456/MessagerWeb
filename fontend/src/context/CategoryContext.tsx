// 📁 contexts/CategoryContext.tsx
import React, { createContext, useContext, useEffect, useState } from "react";
import { getAllCategories } from "../axios/categoryService";

interface Category {
  id: number;
  name: string;
}

interface CategoryContextType {
  categories: Category[];
}

const CategoryContext = createContext<CategoryContextType>({ categories: [] });

export const CategoryProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [categories, setCategories] = useState<Category[]>([]);

  useEffect(() => {
    async function fetchData() {
      try {
        const data = await getAllCategories();
        setCategories(data);
      } catch (error) {
        console.error("Lỗi khi fetch category:", error);
      }
    }

    fetchData();
  }, []);

  return (
    <CategoryContext.Provider value={{ categories }}>
      {children}
    </CategoryContext.Provider>
  );
};

export const useCategory = () => useContext(CategoryContext);
