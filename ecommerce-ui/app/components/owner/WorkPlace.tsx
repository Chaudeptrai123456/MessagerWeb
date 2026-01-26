import Dashboard from "./components/Dashboard";
import Manager from "./components/Manager";
import Orders from "./components/Order";
import Products from "./components/Products";
import Warehouse from "./components/Warehouse";
export default function OwnerDashboard({
  children,
  active,
}: {
  children: React.ReactNode;
  active: String;
}) {
  return (
    <div className="w-full h-full">
      {" "}
      {active === "Dashboard" && <div className="w-full h-full"><Dashboard/></div>}
      {active === "Products" && <div className="w-full h-full"><Products/></div>}
      {active === "Orders" && <div className="w-full h-full"><Orders /> </div>}
      {active === "Warehouse" && <div className="w-full h-full"><Warehouse /></div>}
      {active === "Manager" && <div className="w-full h-full"><Manager/></div>}
      {active === "SystemSettings" && <div className="w-full h-full"><Dashboard/></div>}
    </div>
  );
}
