#!/bin/sh
# ============================================================
#  vault-init.sh - Carga inicial de secretos en Vault
#  Autor: Juan Carlos Estévez Hidalgo
#  Fecha: 07 may 2026
#
#
#  Para recargar secretos manualmente:
#    docker-compose -f docker-compose-utilitarios.yml up vault-init
# ============================================================

# Resolver HOST según IP_SERVER
if [ -z "$IP_SERVER" ] || [ "$IP_SERVER" = "localhost" ] || [ "$IP_SERVER" = "127.0.0.1" ]; then
  HOST=localhost
else
  HOST=$IP_SERVER
fi

echo '========================================'
echo " Vault Init - Host resuelto: $HOST"
echo '========================================'

vault secrets enable -path=FirmaIessApi kv-v2 2>/dev/null || echo 'KV ya habilitado'

# ── PostgreSQL ────────────────────────────────────────────────
vault kv put FirmaIessApi/database/postgres \
  host=$HOST \
  port=5432 \
  username=postgres \
  password=postgres \
  bdd=base_spring_db
echo '[OK] FirmaIessApi/database/postgres'

# ── Oracle (externo institucional) ───────────────────────────
vault kv put FirmaIessApi/database/oracle \
  host=localhost \
  port=1521 \
  username=oracle_user \
  password=oracle_password \
  service=ORCLPDB1
echo '[OK] FirmaIessApi/database/oracle'

# ── MongoDB ───────────────────────────────────────────────────
vault kv put FirmaIessApi/database/mongo \
  host=$HOST \
  port=27017 \
  bdd=AUDITORIA_IESS \
  username=mongo_user \
  password=mongo_password \
  auth_db=admin
echo '[OK] FirmaIessApi/database/mongo'

# ── MinIO ─────────────────────────────────────────────────────
vault kv put FirmaIessApi/storage/minio \
  url=http://$HOST:9000 \
  accessKey=minioadmin \
  secretKey=minioadmin
echo '[OK] FirmaIessApi/storage/minio'

echo '========================================'
echo ' Secretos cargados exitosamente'
echo '========================================'

echo ''
echo '--- Verificacion de secretos ---'
vault kv get FirmaIessApi/database/postgres
vault kv get FirmaIessApi/database/oracle
vault kv get FirmaIessApi/database/mongo
vault kv get FirmaIessApi/storage/minio
