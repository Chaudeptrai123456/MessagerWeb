#!/bin/bash
set -e

echo "🚀 Bắt đầu cấu hình replica..."

# ====== Config cơ bản ======
MASTER_HOST="postgres"      # trùng service name bên docker-compose
MASTER_PORT=5432
REPL_USER="repuser"
REPL_PASS="replica_pass"
PGDATA="/pgdata"            # trùng với biến môi trường của Châu

# Nếu thư mục data trống -> clone dữ liệu từ master
if [ -z "$(ls -A $PGDATA 2>/dev/null)" ]; then
  echo "📦 Data directory trống. Bắt đầu clone dữ liệu từ master ($MASTER_HOST)..."

  # Đợi master sẵn sàng
  until pg_isready -h "$MASTER_HOST" -p "$MASTER_PORT" -U master >/dev/null 2>&1; do
    echo "⏳ Chờ master sẵn sàng..."
    sleep 2
  done

  echo "✅ Master sẵn sàng. Thực hiện pg_basebackup..."
  export PGPASSWORD="$REPL_PASS"
  pg_basebackup -h "$MASTER_HOST" -p "$MASTER_PORT" -D "$PGDATA" -U "$REPL_USER" -Fp -Xs -P -R

  # Thêm cấu hình primary_conninfo để đảm bảo replica biết kết nối đến master
  echo "primary_conninfo = 'host=$MASTER_HOST port=$MASTER_PORT user=$REPL_USER password=$REPL_PASS application_name=replica1'" >> "$PGDATA/postgresql.auto.conf"
  touch "$PGDATA/standby.signal"

  chown -R postgres:postgres "$PGDATA"
  chmod 700 "$PGDATA"

  echo "🎉 Replica đã hoàn tất việc clone dữ liệu!"
else
  echo "📁 Replica data directory đã tồn tại, bỏ qua bước clone."
fi

# Chạy postgres server mặc định
exec docker-entrypoint.sh postgres
