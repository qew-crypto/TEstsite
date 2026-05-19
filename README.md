# VOX Messenger website

Готовый Flask-сайт для `voxMessenger.lol`.

## PixelGo Android prototype

В репозиторий добавлен первый playable APK-прототип `PixelGo` — офлайн voxel FPS в стиле CS2 × Minecraft.

Что есть в версии `0.1.0-prototype`:

- нативный Android-проект без Steam/UE5-зависимостей;
- главное меню с вкладками, профилем, уровнем, рангом, новостями и быстрым стартом;
- офлайн FPS-арена: псевдо-3D voxel/raycast карта, боты, HP/armor/ammo, стрельба, раунды и награды;
- кейсы с выпадением Common/Rare/Epic/Legendary/Mythic;
- инвентарь со списком предметов и сохранением через SharedPreferences;
- процедурные pixel/voxel-текстуры, без внешних ассетов и лицензионного мусора.

Сборка debug APK:

```bash
echo 'sdk.dir=/path/to/android-sdk' > local.properties
gradle :app:assembleDebug
```

Готовый файл после сборки: `app/build/outputs/apk/debug/app-debug.apk`.

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
