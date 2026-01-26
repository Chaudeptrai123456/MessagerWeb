export default function Sidebar({
  onSelect,
}: {
  onSelect: (val: string) => void;
}) {
  return (
    <nav className="p-4">
      <ul className="space-y-2">
        <li>
          <button
            onClick={() => onSelect("Dashboard")}
            className="block px-4 py-2 rounded-md hover:bg-slate-800"
          >
            Dashboard
          </button>
        </li>
        <li>
          <button
            onClick={() => onSelect("Products")}
            className="block px-4 py-2 rounded-md hover:bg-slate-800"
          >
            Products
          </button>
        </li>
        <li>
          <button
            onClick={() => onSelect("Orders")}
            className="block px-4 py-2 rounded-md hover:bg-slate-800"
          >
            Orders
          </button>
        </li>
        <li>
          <button
            onClick={() => onSelect("Warehouse")}
            className="block px-4 py-2 rounded-md hover:bg-slate-800"
          >
            Warehouse
          </button>
        </li>
        <li>
          <button
            onClick={() => onSelect("Manager")}
            className="block px-4 py-2 rounded-md hover:bg-slate-800"
          >
            Manager
          </button>
        </li>
        <li>
          <button
            onClick={() => onSelect("SystemSettings")}
            className="block px-4 py-2 rounded-md hover:bg-slate-800"
          >
            Settings
          </button>
        </li>
      </ul>
    </nav>
  );
}
