import os
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
DONE = ROOT / '.vox_setup_done'
VENV = ROOT / 'venv'
PYTHON = VENV / 'bin' / 'python'
GUNICORN = VENV / 'bin' / 'gunicorn'

def sh(cmd):
    print(f'→ {cmd}')
    subprocess.check_call(cmd, shell=True, cwd=ROOT)

if not DONE.exists():
    print('\nVOX Messenger: первый запуск, подготавливаю проект...\n')
    if not VENV.exists():
        sh('python3 -m venv venv')
        print('✓ Создано виртуальное окружение venv')
    sh(f'{PYTHON} -m pip install --upgrade pip')
    sh(f'{PYTHON} -m pip install -r requirements.txt')
    DONE.write_text('setup complete\n', encoding='utf-8')
    print('\nГотово. Что изменено:')
    print('✓ Установлены зависимости Flask и Gunicorn')
    print('✓ Создан файл .vox_setup_done')
    print('\nТеперь запусти python3 run.py ещё раз — сайт стартует.\n')
    sys.exit(0)

print('\nVOX Messenger запущен на http://0.0.0.0:5000\n')
os.execv(str(GUNICORN), [str(GUNICORN), '-w', '2', '-b', '0.0.0.0:5000', 'app:app'])
