type Role = "ROLE_OWNER" | "ROLE_MANAGER" | "ROLE_USER" | string;

const roleBorderMap: Record<string, string> = {
  ROLE_OWNER: "ring-2 ring-red-500",
  ROLE_MANAGER: "ring-2 ring-yellow-400",
  DEFAULT: "ring-2 ring-blue-500",
};

export default function Avatar({
  src,
  role,
}: {
  src?: string;
  role?: Role;
}) {
  const borderClass =
    roleBorderMap[role ?? ""] ?? roleBorderMap.DEFAULT;

  return (
    <div
      className={`w-10 h-10 rounded-full flex items-center justify-center bg-gray-200 ${borderClass}`}
    >
      {src ? (
        <img
          src={src}
          alt="Avatar"
          className="w-full h-full rounded-full object-cover"
        />
      ) : (
        <span className="text-gray-500">👤</span>
      )}
    </div>
  );
}
