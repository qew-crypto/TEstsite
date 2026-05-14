# VOX Messenger Landing

Структура без папок — можно сразу заливать в корень GitHub репозитория.

## Файлы

- `app.py` — сайт Flask, весь HTML и CSS внутри одного файла
- `run.py` — умный запускатор: первый запуск подготавливает, второй запускает сайт
- `requirements.txt` — зависимости
- `start.sh` — быстрый запуск
- `.gitignore` — чтобы не заливать venv и служебные файлы

## Запуск

```bash
python3 run.py
```

Первый запуск создаст `venv`, установит зависимости и покажет, что изменилось.

Потом запусти ещё раз:

```bash
python3 run.py
```

Сайт будет доступен на:

```text
http://IP_ТВОЕГО_VPS:5000
```

## VPS

Если не установлен venv:

```bash
sudo apt update
sudo apt install python3-venv python3-pip -y
```
