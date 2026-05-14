#!/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SERVICE_NAME="voxmessenger"
DOMAIN="${1:-voxMessenger.lol}"

if [ "$EUID" -ne 0 ]; then
  echo "Запусти установку через sudo: sudo ./install.sh"
  exit 1
fi

echo "VOX Messenger auto-install"
echo "Папка проекта: $PROJECT_DIR"
echo "Домен: $DOMAIN"

apt update -y
apt install python3 python3-pip python3-venv nginx -y

cd "$PROJECT_DIR"
python3 -m venv venv
./venv/bin/python -m pip install --upgrade pip
./venv/bin/python -m pip install -r requirements.txt

touch .vox_setup_done

cat > /etc/systemd/system/${SERVICE_NAME}.service <<EOF
[Unit]
Description=VOX Messenger website
After=network.target

[Service]
User=root
WorkingDirectory=$PROJECT_DIR
ExecStart=$PROJECT_DIR/venv/bin/gunicorn -w 2 -b 127.0.0.1:5000 app:app
Restart=always
RestartSec=3

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable ${SERVICE_NAME}
systemctl restart ${SERVICE_NAME}

cat > /etc/nginx/sites-available/${SERVICE_NAME} <<EOF
server {
    listen 80;
    server_name $DOMAIN www.$DOMAIN;

    location / {
        proxy_pass http://127.0.0.1:5000;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF

ln -sf /etc/nginx/sites-available/${SERVICE_NAME} /etc/nginx/sites-enabled/${SERVICE_NAME}
rm -f /etc/nginx/sites-enabled/default
nginx -t
systemctl restart nginx

echo ""
echo "Готово. Что сделано:"
echo "✓ Установлены python3, pip, venv, nginx"
echo "✓ Создано виртуальное окружение venv"
echo "✓ Установлены Flask и Gunicorn"
echo "✓ Создан systemd сервис ${SERVICE_NAME}"
echo "✓ Включён автозапуск после перезагрузки VPS"
echo "✓ Настроен Nginx для домена $DOMAIN"
echo ""
echo "Теперь поменяй DNS домена:"
echo "A  @    IP_ТВОЕГО_VPS"
echo "A  www  IP_ТВОЕГО_VPS"
echo ""
echo "Проверка статуса: systemctl status ${SERVICE_NAME}"
echo "Перезапуск сайта: systemctl restart ${SERVICE_NAME}"
