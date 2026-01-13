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
    <div>
      <h2 className="text-lg font-semibold mb-2">Warehouse Actions</h2>
      <table className="w-full text-sm table-fixed">
        
        <thead>
          
          <tr className="text-left border-b border-slate-700">
            
            <th className="py-2 w-1/2">Warehouse</th>
            <th className="py-2 w-1/2">Actions</th>
          </tr>
        </thead>
        <tbody>
          
          {Warehouses.map((wh) => (
            <tr key={wh.id} className="border-b border-slate-700">
              
              <td className="py-2">{wh.name}</td>
              <td className="py-2 flex flex-wrap gap-2">
                
                <button className="px-2 py-1 bg-emerald-600 rounded">
                  Info
                </button>
                <button className="px-2 py-1 bg-blue-600 rounded">
                  Assign
                </button>
                <button className="px-2 py-1 bg-yellow-600 rounded">
                  Update
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
