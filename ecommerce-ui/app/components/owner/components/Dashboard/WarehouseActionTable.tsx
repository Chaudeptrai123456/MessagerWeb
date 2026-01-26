import { useContext, useEffect, useState } from "react";
import { apiClient } from "@/utils/axios.client";
import { API_PATHS } from "@/utils/apiPaths";
import { UserContext } from "@/context/UserContext";
const backend = apiClient("BACKEND");

export default function WarehouseActionTable() {
  const [Warehouses, setWareHouses] = useState<any[]>([]);
  const ctx = useContext(UserContext);
  if (!ctx) return <div>⚠️ UserContext chưa sẵn sàng</div>;
  const { user, loading } = ctx;
  const handleCopy = async (text: string) => {
    try {
      await navigator.clipboard.writeText(text);
      alert("Đã copy ID ✅");
    } catch (err) {
      console.error("Copy failed", err);
    }
  };
  useEffect(() => {
    const fetchWarehouses = async () => {
      try {
        const token = user.token;
        const res = await backend.get(API_PATHS.WAREHOUSE.GET_ALL, {
          headers: { Authorization: token ? `Bearer ${token}` : "" },
        });
        console.log("Fetched warehouses:", res.data);
        setWareHouses(res.data);
      } catch (err) {
        console.error("Error fetching warehouses:", err);
      }
    };
    fetchWarehouses();
  }, [user.token]);
  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[700px] text-sm  text-center">
        <thead>
          <tr className="border-b border-slate-700">
            <th className="py-2">Warehouse</th>
            <th className="py-2">ID</th>
            <th className="py-2">Actions</th>
          </tr>
        </thead>
        <tbody>
          {Warehouses.map((wh) => (
            <tr key={wh.id} className="border-b border-slate-700">
              <td className="py-2">{wh.name}</td>

              <td className="py-2">
                <div className="flex items-center gap-2">
                  <span className="truncate max-w-[220px] md:max-w-[320px]">
                    {wh.id}
                  </span>
                  <button
                    onClick={() => handleCopy(wh.id)}
                    className="px-2 py-1 text-xs bg-slate-700 hover:bg-slate-600 rounded shrink-0"
                  >
                    Copy
                  </button>
                </div>
              </td>

              <td className="py-2">
                <div className="flex flex-wrap gap-2">
                  <button className="px-3 py-1 bg-emerald-600 rounded text-xs">
                    Info
                  </button>
                  <button className="px-3 py-1 bg-blue-600 rounded text-xs">
                    Assign
                  </button>
                  <button className="px-3 py-1 bg-yellow-600 rounded text-xs">
                    Update
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
