#!/usr/bin/env sh
set -eu

if [ -f .env ]; then
  echo "检测到 .env，未覆盖现有本地配置。"
  exit 0
fi

umask 077
db_password=$(openssl rand -hex 18)
root_password=$(openssl rand -hex 18)
redis_password=$(openssl rand -hex 18)
admin_password=$(openssl rand -base64 18 | tr -d '/+=' | cut -c1-20)

{
  echo "DB_PASSWORD=$db_password"
  echo "MYSQL_ROOT_PASSWORD=$root_password"
  echo "REDIS_PASSWORD=$redis_password"
  echo "BOOTSTRAP_ADMIN_USERNAME=admin"
  echo "BOOTSTRAP_ADMIN_PASSWORD=$admin_password"
  echo "BOOTSTRAP_ADMIN_EMAIL=admin@aurora.local"
} > .env

echo "已生成仅限本机使用的 .env。"
echo "管理员用户名：admin"
echo "管理员密码：$admin_password"
echo "请妥善保存；该文件已被 Git 忽略。"
