#!/usr/bin/env bash
# Створює робочі гілки команди від main. Запуск: bash scripts/setup-branches.sh
set -e
git checkout main
for b in feature/security-accounts feature/inventory feature/settlement feature/repair-announcements feature/frontend feature/devops-db feature/tests-qa; do
  git branch "$b" 2>/dev/null || echo "гілка $b вже існує"
done
git branch -a
