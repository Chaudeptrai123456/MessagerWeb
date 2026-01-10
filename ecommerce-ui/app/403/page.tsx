"use client";

import { useRouter } from "next/navigation";

export default function ForbiddenPage() {
  const router = useRouter();

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-xl animate-fade-in">
        {/* Icon */}
        <div className="mx-auto mb-6 flex h-16 w-16 items-center justify-center rounded-full bg-red-100 animate-shake">
          <span className="text-3xl">🚫</span>
        </div>

        {/* Title */}
        <h1 className="mb-2 text-center text-2xl font-semibold text-gray-800">
          Truy cập bị từ chối
        </h1>

        {/* Description */}
        
        <p className="mb-6 text-center text-gray-600">
          Bạn không có quyền truy cập vào trang này.
          <br />
          Vui lòng liên hệ quản trị viên nếu bạn nghĩ đây là lỗi.
        </p>

        {/* Actions */}
        <div className="flex justify-center gap-3">
          <button
            onClick={() => router.back()}
            className="rounded-lg border border-gray-300 px-4 py-2 text-gray-700 hover:bg-gray-100"
          >
            Quay lại
          </button>

          <button
            onClick={() => router.replace("/")}
            className="rounded-lg bg-red-500 px-4 py-2 text-white hover:bg-red-600"
          >
            Trang chủ
          </button>
        </div>
      </div>
    </div>
  );
}
