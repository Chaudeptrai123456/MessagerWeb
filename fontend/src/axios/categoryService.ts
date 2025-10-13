import { API_PATHS } from "../utils/apiPath";
import axiosInstance from "../utils/axiosInstance";
export async function getAllCategories() {
  try {
    const response = await axiosInstance.get(API_PATHS.CATEGORY.GET_ALL_CATEGORIES);
    return response.data; // Trả về danh sách category
  } catch (error) {
    console.error("Lỗi khi lấy danh sách category:", error);
    throw error;
  }
}
