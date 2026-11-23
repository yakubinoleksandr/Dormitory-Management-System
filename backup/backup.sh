#!/bin/sh
# NFR-03: щоденна копія PostgreSQL о 03:00 (Europe/Kyiv), зберігається 30 останніх копій.
# Працює всередині контейнера backup (образ postgres:15). Файли — у /backups, квитанції — у /receipts.
set -eu

BACKUP_HOUR="${BACKUP_HOUR:-03}"
KEEP="${BACKUP_KEEP:-30}"
export TZ="${TZ:-Europe/Kyiv}"
export PGPASSWORD="${POSTGRES_PASSWORD}"

do_backup() {
  stamp="$(date +%Y%m%d-%H%M%S)"
  pg_dump -h db -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -Fc -f "/backups/db-${stamp}.dump.tmp"
  mv "/backups/db-${stamp}.dump.tmp" "/backups/db-${stamp}.dump"
  # файли квитанцій копіюються разом з БД, щоб receipt_url лишалися дійсними після відновлення
  tar -czf "/backups/receipts-${stamp}.tar.gz" -C /receipts . 2>/dev/null || true
  # залишаємо останні $KEEP копій кожного типу
  ls -1t /backups/db-*.dump 2>/dev/null | tail -n +$((KEEP + 1)) | xargs -r rm -f
  ls -1t /backups/receipts-*.tar.gz 2>/dev/null | tail -n +$((KEEP + 1)) | xargs -r rm -f
  echo "$(date) backup ${stamp} OK"
}

if [ "${1:-}" = "now" ]; then
  do_backup
  exit 0
fi

echo "Резервування щодня о ${BACKUP_HOUR}:00 (${TZ}), зберігається ${KEEP} копій"
last=""
while true; do
  today="$(date +%Y-%m-%d)"
  if [ "$(date +%H)" = "${BACKUP_HOUR}" ] && [ "${last}" != "${today}" ]; then
    do_backup && last="${today}"
  fi
  sleep 60
done
