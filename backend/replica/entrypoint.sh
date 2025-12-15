#!/bin/bash
set -e

echo "🚀 Bắt đầu cấu hình PostgreSQL Replica..."

# Đảm bảo PGDATA tồn tại và có quyền
mkdir -p "$PGDATA"
chown -R postgres:postgres "$PGDATA"

# Nếu PostgreSQL đang giữ PGDATA thì dừng lại trước
if [ -f "$PGDATA/postmaster.pid" ]; then
  echo "🛑 Dừng tiến trình PostgreSQL đang chạy..."
  kill $(head -1 "$PGDATA/postmaster.pid") || true
  sleep 2
fi

# Xóa dữ liệu cũ (nếu có)
echo "🧹 Dọn dữ liệu cũ..."
rm -rf "$PGDATA"/*
chown -R postgres:postgres "$PGDATA"

# Thực hiện phần còn lại dưới user postgres
exec gosu postgres bash -c "
set -e
echo '🔁 Đang sao chép dữ liệu từ master...'
PGPASSWORD=123 pg_basebackup -h postgres -U reading_user -D '$PGDATA' -v -P --wal-method=stream

echo '📄 Tạo standby.signal và cấu hình replication...'
touch '$PGDATA/standby.signal'
cat > '$PGDATA/postgresql.auto.conf' <<EOF
primary_conninfo = 'host=postgres port=5432 user=reading_user password=123 application_name=replica sslmode=disable'
EOF

echo '✅ Khởi động PostgreSQL Replica...'
exec postgres
"