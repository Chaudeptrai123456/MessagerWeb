#!/bin/bash
set -e

# ====== Config cơ bản ======
REPL_USER="repuser"
REPL_PASS="replica_pass"

echo "🚀 Bắt đầu cấu hình master cho replication..."

# Chờ PostgreSQL sẵn sàng trước khi chạy lệnh psql
until pg_isready -U "$POSTGRES_USER" > /dev/null 2>&1; do
  echo "⏳ Đang chờ PostgreSQL khởi động..."
  sleep 2
done

# ==== 1️⃣ Tạo replication user nếu chưa có ====
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
DO \$\$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '$REPL_USER') THEN
      CREATE ROLE $REPL_USER WITH REPLICATION LOGIN PASSWORD '$REPL_PASS';
   END IF;
END
\$\$;
EOSQL

echo "✅ Đã tạo xong replication user: $REPL_USER"

# ==== 2️⃣ Cấu hình postgresql.conf nếu chưa có ====
CONF_FILE="$PGDATA/postgresql.conf"

if ! grep -q "wal_level" "$CONF_FILE"; then
  echo "🔧 Đang cấu hình postgresql.conf cho replication..."
  cat >> "$CONF_FILE" <<-EOF

# ==== Replication Settings (Master) ====
listen_addresses = '*'
wal_level = replica
max_wal_senders = 10
wal_keep_size = 64MB
hot_standby = on
EOF
fi

# ==== 3️⃣ Cấu hình pg_hba.conf nếu chưa có ====
HBA_FILE="$PGDATA/pg_hba.conf"

if ! grep -q "$REPL_USER" "$HBA_FILE"; then
  echo "🔧 Đang cấu hình pg_hba.conf cho replication..."
  cat >> "$HBA_FILE" <<-EOF

# Cho phép replica (Docker network)
host replication $REPL_USER 0.0.0.0/0 md5
host all all 0.0.0.0/0 md5
EOF
fi

echo "✅ Master init script đã cấu hình xong replication!"
