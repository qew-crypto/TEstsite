# VOX Messenger website

Готовый Flask-сайт для `voxMessenger.lol`.

## Установка на VPS

```bash
git clone ССЫЛКА_НА_ТВОЙ_РЕПО
cd НАЗВАНИЕ_ПАПКИ
sudo chmod +x install.sh
sudo ./install.sh
```

После установки сайт будет сам запускаться после перезагрузки VPS.

## DNS домена

В панели домена поставь:

```txt
A     @      IP_ТВОЕГО_VPS
A     www    IP_ТВОЕГО_VPS
```

## Если домен другой

```bash
sudo ./install.sh example.com
```

## Команды

```bash
sudo systemctl status voxmessenger
sudo systemctl restart voxmessenger
sudo systemctl stop voxmessenger
```

## HTTPS

Когда DNS уже обновился:

```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d voxMessenger.lol -d www.voxMessenger.lol
```
