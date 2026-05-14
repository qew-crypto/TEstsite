from flask import Flask, render_template_string

app = Flask(__name__)

HTML = r'''<!DOCTYPE html>
<html lang="ru">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>VOX Messenger</title>
  <meta name="description" content="VOX Messenger — красивый и быстрый мессенджер. Скоро на Android и Windows." />
  <style>
    * { box-sizing: border-box; }
    :root {
      --bg: #090914;
      --card: rgba(255,255,255,.08);
      --card2: rgba(255,255,255,.12);
      --text: #fff;
      --muted: rgba(255,255,255,.68);
      --accent: #8b5cf6;
      --accent2: #22d3ee;
    }
    body {
      margin: 0;
      min-height: 100vh;
      font-family: Inter, Arial, sans-serif;
      color: var(--text);
      background:
        radial-gradient(circle at 20% 20%, rgba(139,92,246,.35), transparent 30%),
        radial-gradient(circle at 80% 10%, rgba(34,211,238,.22), transparent 25%),
        linear-gradient(135deg, #090914 0%, #141427 55%, #080811 100%);
      overflow-x: hidden;
    }
    .wrap { width: min(1120px, calc(100% - 40px)); margin: 0 auto; }
    header { padding: 26px 0; display: flex; justify-content: space-between; align-items: center; }
    .logo { display: flex; gap: 12px; align-items: center; font-weight: 800; letter-spacing: .5px; }
    .logo-badge { width: 44px; height: 44px; border-radius: 15px; display: grid; place-items: center; background: linear-gradient(135deg, var(--accent), var(--accent2)); box-shadow: 0 18px 50px rgba(139,92,246,.3); }
    .pill { padding: 10px 16px; border: 1px solid rgba(255,255,255,.12); background: rgba(255,255,255,.06); border-radius: 999px; color: var(--muted); font-size: 14px; }
    main { display: grid; grid-template-columns: 1.05fr .95fr; gap: 44px; align-items: center; min-height: calc(100vh - 100px); padding: 30px 0 70px; }
    h1 { margin: 0; font-size: clamp(46px, 7vw, 92px); line-height: .93; letter-spacing: -4px; }
    .grad { background: linear-gradient(90deg, #fff, #c4b5fd, #67e8f9); -webkit-background-clip: text; color: transparent; }
    .lead { max-width: 610px; margin: 26px 0 0; color: var(--muted); font-size: 20px; line-height: 1.65; }
    .actions { display: flex; flex-wrap: wrap; gap: 14px; margin-top: 34px; }
    .btn { border: 0; border-radius: 18px; padding: 16px 22px; font-weight: 800; font-size: 16px; color: white; background: linear-gradient(135deg, var(--accent), #6d5dfc); box-shadow: 0 18px 45px rgba(109,93,252,.35); cursor: default; }
    .btn.secondary { background: rgba(255,255,255,.08); border: 1px solid rgba(255,255,255,.14); box-shadow: none; color: rgba(255,255,255,.82); }
    .status { margin-top: 22px; color: rgba(255,255,255,.58); font-size: 14px; }
    .phone { justify-self: center; width: min(390px, 100%); padding: 14px; border-radius: 42px; background: linear-gradient(145deg, rgba(255,255,255,.18), rgba(255,255,255,.04)); border: 1px solid rgba(255,255,255,.14); box-shadow: 0 35px 90px rgba(0,0,0,.45); }
    .screen { min-height: 620px; border-radius: 32px; background: rgba(7,7,18,.88); padding: 22px; overflow: hidden; position: relative; }
    .top { display:flex; justify-content:space-between; align-items:center; margin-bottom: 24px; }
    .dot { width: 10px; height: 10px; border-radius: 99px; background: #22c55e; box-shadow: 0 0 20px #22c55e; }
    .chat { display:flex; flex-direction:column; gap:14px; }
    .msg { max-width: 82%; padding: 14px 16px; border-radius: 20px; background: rgba(255,255,255,.1); color: rgba(255,255,255,.88); line-height:1.45; }
    .msg.me { align-self:flex-end; background: linear-gradient(135deg, var(--accent), #4f46e5); }
    .card { margin-top: 24px; padding: 18px; border-radius: 24px; background: linear-gradient(135deg, rgba(139,92,246,.2), rgba(34,211,238,.12)); border: 1px solid rgba(255,255,255,.13); }
    .card b { display:block; font-size: 22px; margin-bottom: 8px; }
    .stores { display:grid; grid-template-columns:1fr 1fr; gap:12px; margin-top: 14px; }
    .store { padding: 13px; border-radius: 16px; background: rgba(255,255,255,.08); text-align:center; color: rgba(255,255,255,.85); font-weight:700; }
    footer { padding: 26px 0; color: rgba(255,255,255,.45); text-align:center; }
    @media (max-width: 860px) {
      .wrap { width: min(100% - 28px, 560px); }
      header { padding-top: 18px; }
      .pill { display:none; }
      main { grid-template-columns: 1fr; gap: 32px; text-align: center; padding-top: 12px; }
      h1 { letter-spacing: -2.5px; }
      .lead { font-size: 17px; margin-left:auto; margin-right:auto; }
      .actions { justify-content:center; }
      .btn { width: 100%; }
      .phone { width: min(350px, 100%); }
      .screen { min-height: 530px; }
    }
  </style>
</head>
<body>
  <div class="wrap">
    <header>
      <div class="logo"><div class="logo-badge">V</div><span>VOX Messenger</span></div>
      <div class="pill">Сайт и приложения в разработке</div>
    </header>

    <main>
      <section>
        <h1><span class="grad">VOX</span><br>Messenger</h1>
        <p class="lead">Новый мессенджер для быстрых сообщений, красивого общения и удобной связи. Мы уже работаем над приложениями.</p>
        <div class="actions">
          <button class="btn">Скоро на Android</button>
          <button class="btn secondary">Скоро на Windows</button>
        </div>
        <div class="status">Скачать пока нельзя — сайт и приложения находятся в разработке.</div>
      </section>

      <section class="phone" aria-label="Preview">
        <div class="screen">
          <div class="top"><b>VOX Chat</b><span class="dot"></span></div>
          <div class="chat">
            <div class="msg">Привет! Это будущий VOX Messenger.</div>
            <div class="msg me">Красивый, быстрый и удобный.</div>
            <div class="msg">Android и Windows версии скоро будут доступны.</div>
          </div>
          <div class="card">
            <b>Скоро запуск</b>
            <span>Следи за обновлениями на voxMessenger.lol</span>
            <div class="stores">
              <div class="store">Android</div>
              <div class="store">Windows</div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <footer>© 2026 VOX Messenger. All rights reserved.</footer>
  </div>
</body>
</html>'''

@app.route('/')
def index():
    return render_template_string(HTML)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=False)
