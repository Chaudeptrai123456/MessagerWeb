"use client";

export default function RedirectLoading() {
  return (
    <div className="fixed inset-0 z-[9999] flex items-center justify-center bg-white">
      <div className="flex items-center gap-3">
        <div className="h-5 w-5 animate-spin rounded-full border-2 border-gray-300 border-t-gray-800" />
        <span className="text-gray-700 text-sm">
          Đang chuyển hướng...
        </span>
      </div>
    </div>
  );
}
