export default function Logo() {
  return (
    <div className="flex items-center gap-2">
      <img
        src="/logo.svg"
        alt="Postcrafts logo"
        className="h-8 w-8 rounded"
      />
      <span className="text-xl font-semibold text-white">Postcrafts</span>
    </div>
  );
}
