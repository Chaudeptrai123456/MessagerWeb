import WarehouseActionTable from "./Dashboard/WarehouseActionTable";
import { useContext, useEffect, useState } from "react";
import { apiClient } from "@/utils/axios.client";
import { UserContext } from "@/context/UserContext";
import { API_PATHS } from "@/utils/apiPaths";
const backend = apiClient("BACKEND");
type ListProductInAllWarehouse = {
  id: string;
  name: string;
  price: number;
  description: string;
  totalQuantity: number;
};

export default function Warehouse() {
  const [isVisible, setIsVisible] = useState(false);
  const ctx = useContext(UserContext);
  if (!ctx) return null;
  const { user, loading } = ctx;
  const [data, setData] = useState<ListProductInAllWarehouse | null>(null);
  const fetchData = async () => {
    try {
      const token = user.token;
      const res = await backend.get(API_PATHS.WAREHOUSE.GET_ALL_PRODUCT, {
        headers: { Authorization: token ? `Bearer ${token}` : "" },
      });
      console.log("Fetched warehouses:", res.data);
      setData(res.data);
    } catch (err) {
      console.error("Error fetching warehouses:", err);
    }
  };
  useEffect(() => {
    fetchData();
  }, [user.token]);
  const handleClick = () => {
    setIsVisible(!isVisible);
  };
  return (
    <div className="flex flex-col gap-3 w-full h-full relative">
      {/* Danh sách kho */}
      <div className="flex-1 bg-slate-800 p-4 rounded">List Warehouse</div>

      {/* Biểu đồ lợi nhuận */}
      <div className="flex-8 bg-slate-700 p-4 rounded">
        Product Profits and Warehouse
      </div>
      {/* Nút toggle hiển thị bảng */}
      <div className="absolute bottom-4 right-4">
        <button
          onClick={handleClick}
          className="px-4 py-2 bg-emerald-700 text-white rounded hover:bg-emerald-600"
        >
          {isVisible ? "Ẩn bảng thao tác" : "Hiện bảng thao tác"}
        </button>
      </div>
      {/* Bảng thao tác cố định góc dưới bên phải */}
      {isVisible && (
        <div className="fixed  bottom-4 right-1 w-96 bg-slate-900 p-4 rounded shadow-lg">
          <div className="flex justify-end">
            <button
              onClick={handleClick}
              className="px-2 py-1 bg-red-600 text-white rounded hover:bg-red-500"
            >
              X
            </button>
          </div>
          <WarehouseActionTable />
        </div>
      )}
    </div>
  );
}
