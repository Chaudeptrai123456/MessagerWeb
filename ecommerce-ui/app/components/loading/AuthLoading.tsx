"use client";

export default function AuthLoading() {
  return (
    <div className="fixed inset-0 z-[9999] flex items-center justify-center bg-black/40 backdrop-blur-md">
      <div className="flex flex-col items-center gap-4">
        <div className="h-14 w-14 animate-spin rounded-full border-4 border-white/30 border-t-white" />
        <p className="text-white text-sm tracking-wide">
          Đang xác thực tài khoản...
        </p>
      </div>
    </div>
  );
}
