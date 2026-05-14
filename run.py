#!/usr/bin/env python3
"""
VOX Messenger smart launcher.

First run: prepares the project and prints what was changed.
Second and next runs: starts the website.
"""
from __future__ import annotations

import os
import subprocess
import sys
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent
VENV_DIR = BASE_DIR / "venv"
DONE_FILE = BASE_DIR / ".vox_setup_done"
REQ_FILE = BASE_DIR / "requirements.txt"


def venv_python() -> Path:
    if os.name == "nt":
        return VENV_DIR / "Scripts" / "python.exe"
    return VENV_DIR / "bin" / "python"


def run_command(command: list[str], cwd: Path | None = None) -> None:
    print("$ " + " ".join(command))
    subprocess.check_call(command, cwd=str(cwd or BASE_DIR))


def first_setup() -> None:
    changes: list[str] = []

    print("\nVOX Messenger — первый запуск")
    print("Сейчас подготовлю сайт. После этого запусти этот файл ещё раз.\n")

    if not VENV_DIR.exists():
        run_command([sys.executable, "-m", "venv", str(VENV_DIR)])
        changes.append("создано виртуальное окружение: venv/")
    else:
        changes.append("виртуальное окружение venv/ уже было создано")

    py = venv_python()
    if not py.exists():
        raise RuntimeError("Не нашёл Python внутри venv. Удали папку venv и запусти снова.")

    run_command([str(py), "-m", "pip", "install", "--upgrade", "pip"])
    changes.append("обновлён pip внутри venv")

    if REQ_FILE.exists():
        run_command([str(py), "-m", "pip", "install", "-r", str(REQ_FILE)])
        changes.append("установлены зависимости из requirements.txt: Flask и Gunicorn")
    else:
        run_command([str(py), "-m", "pip", "install", "flask", "gunicorn"])
        changes.append("установлены зависимости: Flask и Gunicorn")

    DONE_FILE.write_text("setup_done=1\n", encoding="utf-8")
    changes.append("создан файл .vox_setup_done — он говорит запускатору, что подготовка уже сделана")

    print("\nГотово. Что поменялось:")
    for item in changes:
        print(f"- {item}")

    print("\nТеперь запусти ещё раз:")
    print("python3 run.py")
    print("\nПосле второго запуска сайт будет доступен тут:")
    print("http://IP_ТВОЕГО_VPS:5000\n")


def start_site() -> None:
    py = venv_python()
    if not py.exists():
        print("venv сломан или удалён. Удаляю отметку первого запуска, запусти ещё раз.")
        try:
            DONE_FILE.unlink()
        except FileNotFoundError:
            pass
        return

    print("\nVOX Messenger запускается...")
    print("Адрес на VPS: http://IP_ТВОЕГО_VPS:5000")
    print("Чтобы остановить сайт: Ctrl + C\n")

    # app.py already listens on 0.0.0.0:5000
    os.execv(str(py), [str(py), str(BASE_DIR / "app.py")])


def main() -> None:
    os.chdir(BASE_DIR)
    if not DONE_FILE.exists():
        first_setup()
    else:
        start_site()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\nСайт остановлен.")
    except subprocess.CalledProcessError as exc:
        print(f"\nОшибка при выполнении команды: {exc}")
        print("Проверь, что на VPS установлен python3-venv:")
        print("sudo apt update && sudo apt install python3-venv python3-pip -y")
        sys.exit(1)
    except Exception as exc:
        print(f"\nОшибка: {exc}")
        sys.exit(1)
