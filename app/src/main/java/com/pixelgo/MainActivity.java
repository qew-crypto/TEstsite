package com.pixelgo;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.view.*;
import java.util.*;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) { super.onCreate(b); getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); setContentView(new PixelGoView(this)); }

    static class PixelGoView extends View {
        static final int MENU=0, OFFLINE=1, CASES=2, INVENTORY=3, SETTINGS=4;
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); Paint text = new Paint(Paint.ANTI_ALIAS_FLAG); Random rng = new Random(7);
        ArrayList<Button> buttons = new ArrayList<>(); ArrayList<String> inv = new ArrayList<>(); SharedPreferences prefs;
        int screen = MENU, coins = 900, level = 12, xp = 67, rankIndex = 3, selectedSkin = 0;
        String[] ranks = {"WOOD I", "STONE II", "IRON III", "DIAMOND IV", "OBSIDIAN ELITE"};
        String[] tabs = {"Играть", "Онлайн", "Оффлайн", "Кейсы", "Магазин", "Инвентарь", "Боевой пропуск", "Друзья", "Профиль", "Настройки", "Кланы", "Лидеры"};
        String[] loot = {"Common / P250 Grass Block", "Common / MP9 Sand", "Rare / AK-47 Neon Cube", "Rare / AWP Snow Pixel", "Epic / M4A4 Lava Vox", "Epic / Gloves Prism", "Legendary / Karambit Ender", "Mythic / Dragon Operator"};
        int[] lootColor = {0xff60d36b,0xffd9c16b,0xff49b8ff,0xffbfefff,0xffff7a35,0xffb565ff,0xffffd447,0xffff3cc7};
        String lastDrop = "Нажми OPEN CASE"; float caseAnim = 0; long caseStart = 0;
        float px=4.5f, py=4.5f, pa=0; int hp=100, armor=50, ammo=30, score=0, round=1; float moveX=0, moveY=0, look=0; int movePointer=-1, lookPointer=-1; long lastFrame=System.nanoTime();
        ArrayList<Bot> bots = new ArrayList<>(); int[][] map = new int[][]{
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,2,0,0,0,0,0,3,0,1},
            {1,0,0,0,2,0,0,2,0,0,3,0,0,0,0,1},
            {1,0,2,0,0,0,0,0,0,0,3,0,2,0,0,1},
            {1,0,2,0,0,4,4,0,0,0,0,0,2,0,0,1},
            {1,0,0,0,0,4,0,0,2,2,2,0,0,0,0,1},
            {1,0,0,0,0,4,0,0,0,0,0,0,0,3,0,1},
            {1,2,2,0,0,0,0,0,0,5,0,0,0,3,0,1},
            {1,0,0,0,3,3,0,0,0,0,0,2,0,0,0,1},
            {1,0,0,0,0,0,0,2,2,0,0,2,0,0,0,1},
            {1,0,3,0,2,0,0,0,0,0,0,0,0,2,0,1},
            {1,0,3,0,2,0,6,0,0,3,3,0,0,2,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,2,2,0,0,0,4,4,0,0,3,3,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}};

        PixelGoView(Context c) { super(c); setFocusable(true); text.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)); prefs=c.getSharedPreferences("pixelgo",0); load(); resetBots(); }
        void load(){ coins=prefs.getInt("coins",900); String s=prefs.getString("inv","Starter Knife|AK-47 Default|M4A1 Cube"); inv.clear(); inv.addAll(Arrays.asList(s.split("\\|"))); }
        void save(){ prefs.edit().putInt("coins",coins).putString("inv",join(inv)).apply(); }
        String join(ArrayList<String> a){ StringBuilder b=new StringBuilder(); for(int i=0;i<a.size();i++){ if(i>0)b.append('|'); b.append(a.get(i)); } return b.toString(); }
        void resetBots(){ bots.clear(); float[][] pos={{11.5f,2.5f},{13.2f,8.2f},{3.5f,12.5f},{10.5f,13.5f},{6.7f,10.4f}}; for(int i=0;i<pos.length;i++) bots.add(new Bot(pos[i][0],pos[i][1],"BOT-"+(i+1))); }

        @Override protected void onDraw(Canvas c){ super.onDraw(c); long now=System.nanoTime(); float dt=Math.min(.05f,(now-lastFrame)/1_000_000_000f); lastFrame=now; update(dt); if(screen==MENU) drawMenu(c); else if(screen==OFFLINE) drawGame(c); else if(screen==CASES) drawCases(c); else if(screen==INVENTORY) drawInventory(c); else drawSettings(c); postInvalidateDelayed(16); }
        void update(float dt){ if(screen==OFFLINE){ pa += look*dt*2.2f; float sp=2.2f*dt; float nx=px+(float)Math.cos(pa)*moveY*sp+(float)Math.cos(pa+Math.PI/2)*moveX*sp; float ny=py+(float)Math.sin(pa)*moveY*sp+(float)Math.sin(pa+Math.PI/2)*moveX*sp; if(walk(nx,py)) px=nx; if(walk(px,ny)) py=ny; for(Bot b:bots) if(b.alive){ float dx=px-b.x,dy=py-b.y,d=(float)Math.sqrt(dx*dx+dy*dy); if(d<7){ b.x+=dx/d*dt*.55f; b.y+=dy/d*dt*.55f; if(d<.65f){ hp=Math.max(0,hp-(int)(25*dt)); if(hp==0){ hp=100; armor=50; score=Math.max(0,score-1); px=4.5f; py=4.5f; } } } } } if(caseAnim>0){ caseAnim-=dt; if(caseAnim<=0) finishCase(); } }
        boolean walk(float x,float y){ int ix=(int)x, iy=(int)y; return ix>=0&&iy>=0&&iy<map.length&&ix<map[0].length&&map[iy][ix]==0; }

        void drawMenu(Canvas c){ int w=getWidth(),h=getHeight(); buttons.clear(); bg(c,w,h); drawTop(c,w,h); int side=(int)(w*.23f); glass(c,24,80,side,h-40,0x88202032); text(c,"PIXELGO",52,62,42,0xff48f4ff,Paint.Align.LEFT); text(c,"CS2 × Minecraft voxel shooter",52,96,14,0xffd8f8ff,Paint.Align.LEFT); int y=132; for(String t:tabs){ int col=t.equals("Играть")?0xff48f4ff:0xffc9d7ee; rectButton(c,t,44,y,side-28,y+42,col,()->route(t)); y+=48; } drawVoxelOperator(c,w-side/2,h/2+35,Math.min(w,h)/4); glass(c,side+35,88,w-32,h-40,0x66202840); text(c,"БЫСТРЫЙ ПОИСК МАТЧА",side+70,135,27,0xffffffff,Paint.Align.LEFT); text(c,"Competitive 5v5 пока в разработке — офлайн доступен уже сейчас",side+70,166,14,0xffb9d4ff,Paint.Align.LEFT); rectButton(c,"ИГРАТЬ ОФФЛАЙН",side+70,196,side+360,254,0xff49ff9a,()->{screen=OFFLINE;}); rectButton(c,"ОТКРЫТЬ КЕЙСЫ",side+390,196,side+650,254,0xffffd447,()->{screen=CASES;}); card(c,side+70,290,side+360,430,"Ежедневная награда","+120 coins за вход\nСерия: 3 дня",0xff49ff9a); card(c,side+390,290,side+700,430,"Новости","Season 0: Cubic Dawn\nНовая карта: Dust Vox",0xff48f4ff); card(c,side+730,290,w-70,430,"Событие","Zombie Blocks Night\nUltra rare gloves",0xffff3cc7); card(c,side+70,462,w-70,h-72,"Что уже есть в прототипе","Главное меню, вкладки, кейсы, инвентарь, офлайн FPS-арена, боты, стрельба, HP/armor/ammo, процедурные pixel-текстуры.",0xffb565ff); }
        void route(String t){ if(t.equals("Играть")||t.equals("Оффлайн")) screen=OFFLINE; else if(t.equals("Кейсы")) screen=CASES; else if(t.equals("Инвентарь")) screen=INVENTORY; else screen=SETTINGS; }

        void drawTop(Canvas c,int w,int h){ glass(c,w-420,22,w-28,72,0x88304470); text(c,"LVL "+level+"  "+ranks[rankIndex]+"  "+coins+" coins",w-402,55,18,0xffffffff,Paint.Align.LEFT); p.setColor(0xff48f4ff); c.drawRoundRect(w-162,58,w-162+xp*1.25f,64,8,8,p); }
        void bg(Canvas c,int w,int h){ LinearGradient g=new LinearGradient(0,0,w,h,0xff0b1024,0xff221032,Shader.TileMode.CLAMP); p.setShader(g); c.drawRect(0,0,w,h,p); p.setShader(null); for(int i=0;i<90;i++){ int x=(i*97+(int)(System.currentTimeMillis()/45))%Math.max(1,w); int y=(i*53)%Math.max(1,h); p.setColor((i%3==0)?0x3348f4ff:0x2249ff9a); c.drawRect(x,y,x+6+(i%5)*3,y+6+(i%4)*3,p); } }
        void glass(Canvas c,float l,float t,float r,float b,int color){ p.setStyle(Paint.Style.FILL); p.setColor(color); c.drawRoundRect(l,t,r,b,24,24,p); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2); p.setColor(0x55ffffff); c.drawRoundRect(l,t,r,b,24,24,p); p.setStyle(Paint.Style.FILL); }
        void text(Canvas c,String s,float x,float y,float size,int color,Paint.Align a){ text.setTextSize(size); text.setColor(color); text.setTextAlign(a); for(String line:s.split("\\n")){ c.drawText(line,x,y,text); y+=size*1.35f; } }
        void rectButton(Canvas c,String label,float l,float t,float r,float b,int col,Run run){ glass(c,l,t,r,b,0x99252d48); p.setColor(col); c.drawRoundRect(l,t,r,b,18,18,p); p.setColor(0x33000000); c.drawRoundRect(l+4,t+4,r-4,b-4,14,14,p); text(c,label,(l+r)/2,(t+b)/2+7,17,0xff08111f,Paint.Align.CENTER); buttons.add(new Button(l,t,r,b,run)); }
        void card(Canvas c,float l,float t,float r,float b,String title,String body,int accent){ glass(c,l,t,r,b,0x772a304a); p.setColor(accent); c.drawRect(l,t,l+6,b,p); text(c,title,l+24,t+38,22,0xffffffff,Paint.Align.LEFT); text(c,body,l+24,t+72,16,0xffd5e8ff,Paint.Align.LEFT); }
        void drawVoxelOperator(Canvas c,float cx,float cy,float s){ p.setColor(0xff2d3562); c.drawRect(cx-s*.32f,cy-s*.65f,cx+s*.32f,cy+s*.25f,p); p.setColor(0xffefc77d); c.drawRect(cx-s*.22f,cy-s*1.05f,cx+s*.22f,cy-s*.65f,p); p.setColor(0xff111725); c.drawRect(cx-s*.18f,cy-s*.92f,cx+s*.18f,cy-s*.84f,p); p.setColor(0xff48f4ff); c.drawRect(cx-s*.15f,cy-s*.89f,cx+s*.15f,cy-s*.86f,p); p.setColor(0xff49ff9a); c.drawRect(cx-s*.58f,cy-s*.55f,cx-s*.32f,cy+s*.15f,p); c.drawRect(cx+s*.32f,cy-s*.55f,cx+s*.58f,cy+s*.15f,p); p.setColor(0xff1c223a); c.drawRect(cx-s*.27f,cy+s*.25f,cx-s*.05f,cy+s*.95f,p); c.drawRect(cx+s*.05f,cy+s*.25f,cx+s*.27f,cy+s*.95f,p); p.setColor(0xff0f1321); c.drawRect(cx+s*.46f,cy-s*.12f,cx+s*1.05f,cy+s*.02f,p); }

        void drawCases(Canvas c){ int w=getWidth(),h=getHeight(); buttons.clear(); bg(c,w,h); drawTop(c,w,h); rectButton(c,"← МЕНЮ",34,28,180,76,0xff48f4ff,()->screen=MENU); rectButton(c,"ИНВЕНТАРЬ",200,28,390,76,0xff49ff9a,()->screen=INVENTORY); text(c,"КЕЙСЫ PIXELGO",w/2,98,34,0xffffffff,Paint.Align.CENTER); glass(c,80,140,w-80,h-70,0x88304470); for(int i=0;i<5;i++){ float l=130+i*(w-260)/5f, r=l+(w-320)/5f; p.setColor(new int[]{0xff4bb86a,0xff49b8ff,0xffb565ff,0xffffd447,0xffff3cc7}[i]); c.drawRoundRect(l,185,r,330,18,18,p); drawPixelCrate(c,l+20,205,r-20,310,i); text(c,new String[]{"COMMON","RARE","EPIC","LEGENDARY","EVENT"}[i],(l+r)/2,365,18,0xffffffff,Paint.Align.CENTER); }
            rectButton(c,"OPEN CASE — 250 COINS",w/2-190,h-150,w/2+190,h-92,0xffffd447,()->openCase()); text(c,lastDrop,w/2,h-190,24,caseAnim>0?0xff48f4ff:0xffffffff,Paint.Align.CENTER); if(caseAnim>0){ float x=(System.currentTimeMillis()%900)/900f*w; p.setColor(0x99ffffff); c.drawRect(x,400,x+18,500,p); text(c,"ROLLING...",w/2,460,38,0xffffd447,Paint.Align.CENTER); } }
        void drawPixelCrate(Canvas c,float l,float t,float r,float b,int idx){ int[] cs={0xff6bd179,0xff3db7ff,0xff9e5bff,0xffffcf35,0xffff3cc7}; p.setColor(0xff1b2037); c.drawRect(l,t,r,b,p); p.setColor(cs[idx]); for(int y=0;y<5;y++) for(int x=0;x<7;x++) if((x+y+idx)%2==0)c.drawRect(l+x*(r-l)/7,t+y*(b-t)/5,l+(x+1)*(r-l)/7,t+(y+1)*(b-t)/5,p); }
        void openCase(){ if(coins<250){ lastDrop="Недостаточно coins"; return; } coins-=250; caseAnim=1.8f; caseStart=System.currentTimeMillis(); save(); }
        void finishCase(){ int roll=rng.nextInt(1000), idx=roll<520?rng.nextInt(2):roll<800?2+rng.nextInt(2):roll<940?4+rng.nextInt(2):roll<990?6:7; lastDrop=loot[idx]; inv.add(loot[idx]); coins+=idx>=6?80:15; save(); }

        void drawInventory(Canvas c){ int w=getWidth(),h=getHeight(); buttons.clear(); bg(c,w,h); drawTop(c,w,h); rectButton(c,"← МЕНЮ",34,28,180,76,0xff48f4ff,()->screen=MENU); rectButton(c,"КЕЙСЫ",200,28,330,76,0xffffd447,()->screen=CASES); text(c,"ИНВЕНТАРЬ",w/2,95,34,0xffffffff,Paint.Align.CENTER); glass(c,56,125,w-56,h-45,0x88304470); int cols=4; float gap=22, cell=(w-160-gap*(cols-1))/cols; for(int i=0;i<inv.size();i++){ int row=i/cols,col=i%cols; float l=80+col*(cell+gap),t=160+row*126; if(t+104>h-60) break; p.setColor(colorFor(inv.get(i))); c.drawRoundRect(l,t,l+cell,t+104,18,18,p); p.setColor(0x66000000); c.drawRoundRect(l+6,t+6,l+cell-6,t+98,14,14,p); text(c,inv.get(i),l+18,t+40,15,0xffffffff,Paint.Align.LEFT); text(c,"3D preview • equip • favorite",l+18,t+72,12,0xffcfe8ff,Paint.Align.LEFT); } }
        int colorFor(String s){ if(s.contains("Mythic"))return 0xffff3cc7; if(s.contains("Legendary"))return 0xffffd447; if(s.contains("Epic"))return 0xffb565ff; if(s.contains("Rare"))return 0xff49b8ff; return 0xff49aa68; }

        void drawSettings(Canvas c){ int w=getWidth(),h=getHeight(); buttons.clear(); bg(c,w,h); drawTop(c,w,h); rectButton(c,"← МЕНЮ",34,28,180,76,0xff48f4ff,()->screen=MENU); text(c,"РАЗДЕЛ В РАЗРАБОТКЕ",w/2,h/2-20,38,0xffffffff,Paint.Align.CENTER); text(c,"Онлайн, кланы, маркет, боевой пропуск и лидеры будут подключены следующими версиями.",w/2,h/2+28,18,0xffd5e8ff,Paint.Align.CENTER); }

        void drawGame(Canvas c){ int w=getWidth(),h=getHeight(); buttons.clear(); drawWorld(c,w,h); drawBots(c,w,h); drawWeapon(c,w,h); drawHud(c,w,h); drawControls(c,w,h); }
        void drawWorld(Canvas c,int w,int h){ p.setShader(new LinearGradient(0,0,0,h/2,0xff102044,0xff6140a0,Shader.TileMode.CLAMP)); c.drawRect(0,0,w,h/2,p); p.setShader(new LinearGradient(0,h/2,0,h,0xff202022,0xff090a0f,Shader.TileMode.CLAMP)); c.drawRect(0,h/2,w,h,p); p.setShader(null); int rays=w/5; float fov=1.05f; for(int i=0;i<rays;i++){ float a=pa-fov/2+fov*i/rays; float dist=0; int hit=0; while(dist<16 && hit==0){ dist+=.045f; int mx=(int)(px+Math.cos(a)*dist), my=(int)(py+Math.sin(a)*dist); if(my<0||mx<0||my>=map.length||mx>=map[0].length){hit=1;break;} hit=map[my][mx]; } float cd=dist*(float)Math.cos(a-pa); float wallH=Math.min(h, h/(cd+.08f)); float x=i*5; int shade=Math.max(45,220-(int)(cd*18)); int base=wallColor(hit,shade); p.setColor(base); c.drawRect(x,h/2-wallH/2,x+6,h/2+wallH/2,p); if(i%2==0){ p.setColor(0x22000000); c.drawRect(x,h/2-wallH/2,x+2,h/2+wallH/2,p); } } }
        int wallColor(int hit,int sh){ int r=sh,g=sh,b=sh; if(hit==2){r=sh;g=sh/2;b=35;} else if(hit==3){r=45;g=sh;b=sh;} else if(hit==4){r=sh/2;g=sh;b=70;} else if(hit==5){r=sh;g=55;b=45;} else if(hit==6){r=sh;g=sh;b=30;} return 0xff000000|(r<<16)|(g<<8)|b; }
        void drawBots(Canvas c,int w,int h){ ArrayList<Bot> order=new ArrayList<>(bots); Collections.sort(order,(a,b)->Float.compare(dist(b),dist(a))); for(Bot bot:order) if(bot.alive){ float dx=bot.x-px,dy=bot.y-py,d=(float)Math.sqrt(dx*dx+dy*dy); float ang=(float)Math.atan2(dy,dx)-pa; while(ang<-Math.PI)ang+=Math.PI*2; while(ang>Math.PI)ang-=Math.PI*2; if(Math.abs(ang)<.55f){ float sx=w/2+(ang/.55f)*w/2; float size=h/(d+0.2f); p.setColor(0xffc94b4b); c.drawRect(sx-size*.22f,h/2-size*.45f,sx+size*.22f,h/2+size*.28f,p); p.setColor(0xfff2c184); c.drawRect(sx-size*.16f,h/2-size*.72f,sx+size*.16f,h/2-size*.45f,p); p.setColor(0xff111111); c.drawRect(sx-size*.13f,h/2-size*.62f,sx+size*.13f,h/2-size*.56f,p); text(c,bot.name,sx,h/2-size*.82f,12,0xffffffff,Paint.Align.CENTER); } } }
        float dist(Bot b){ float dx=b.x-px,dy=b.y-py; return dx*dx+dy*dy; }
        void drawWeapon(Canvas c,int w,int h){ p.setColor(0xff0f1426); c.drawRect(w*.58f,h*.67f,w*.94f,h*.86f,p); p.setColor(0xff48f4ff); c.drawRect(w*.72f,h*.70f,w*.91f,h*.735f,p); p.setColor(0xff1f2a4a); c.drawRect(w*.62f,h*.79f,w*.76f,h*.93f,p); p.setColor(0xffffd447); c.drawRect(w*.86f,h*.68f,w*.96f,h*.72f,p); }
        void drawHud(Canvas c,int w,int h){ glass(c,20,18,255,78,0x88304470); text(c,"HP "+hp+"  ARM "+armor+"  AMMO "+ammo,38,56,18,0xffffffff,Paint.Align.LEFT); text(c,"PIXELGO OFFLINE • ROUND "+round+" • SCORE "+score,w/2,38,18,0xffffffff,Paint.Align.CENTER); text(c,"+",w/2,h/2+9,34,0xff49ff9a,Paint.Align.CENTER); rectButton(c,"MENU",w-125,18,w-25,68,0xff48f4ff,()->{moveX=moveY=look=0; screen=MENU;}); }
        void drawControls(Canvas c,int w,int h){ p.setColor(0x3348f4ff); c.drawCircle(130,h-125,82,p); text(c,"MOVE",130,h-118,16,0xffd8f8ff,Paint.Align.CENTER); rectButton(c,"FIRE",w-190,h-150,w-60,h-70,0xffff5a5a,()->shoot()); rectButton(c,"RELOAD",w-360,h-125,w-220,h-76,0xffffd447,()->ammo=30); }
        void shoot(){ if(screen!=OFFLINE||ammo<=0)return; ammo--; for(Bot b:bots) if(b.alive){ float dx=b.x-px,dy=b.y-py,d=(float)Math.sqrt(dx*dx+dy*dy); float ang=(float)Math.atan2(dy,dx)-pa; while(ang<-Math.PI)ang+=Math.PI*2; while(ang>Math.PI)ang-=Math.PI*2; if(Math.abs(ang)<.09f && d<12){ b.hp-=45; if(b.hp<=0){ b.alive=false; score++; coins+=10; save(); boolean any=false; for(Bot bb:bots) if(bb.alive) any=true; if(!any){ round++; resetBots(); } } break; } } }

        @Override public boolean onTouchEvent(android.view.MotionEvent e){ int action=e.getActionMasked(), idx=e.getActionIndex(); float x=e.getX(idx), y=e.getY(idx); if(action==MotionEvent.ACTION_DOWN||action==MotionEvent.ACTION_POINTER_DOWN){ for(Button b:buttons) if(b.hit(x,y)){ b.run.go(); return true; } if(screen==OFFLINE){ updateControls(e,-1); return true; } } if(screen==OFFLINE&&action==MotionEvent.ACTION_MOVE){ updateControls(e,-1); return true; } if(action==MotionEvent.ACTION_POINTER_UP&&screen==OFFLINE){ updateControls(e,idx); return true; } if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL){ moveX=moveY=look=0; movePointer=lookPointer=-1; } return true; }
        void updateControls(MotionEvent e,int skip){ moveX=moveY=look=0; movePointer=lookPointer=-1; for(int i=0;i<e.getPointerCount();i++){ if(i==skip) continue; handleControl(e.getPointerId(i),e.getX(i),e.getY(i)); } }
        void handleControl(int id,float x,float y){ int w=getWidth(),h=getHeight(); if(movePointer<0&&y>h*.55f&&x<w*.36f){ movePointer=id; moveY=(h-125-y)/90f; moveX=(x-130)/90f; moveY=Math.max(-1,Math.min(1,moveY)); moveX=Math.max(-1,Math.min(1,moveX)); } else if(lookPointer<0&&y>h*.45f&&x>w*.45f){ lookPointer=id; look=(x>w*.72f)?1:-1; } }

        interface Run{ void go(); } static class Button{ float l,t,r,b; Run run; Button(float l,float t,float r,float b,Run run){this.l=l;this.t=t;this.r=r;this.b=b;this.run=run;} boolean hit(float x,float y){ return x>=l&&x<=r&&y>=t&&y<=b; } }
        static class Bot{ float x,y; int hp=100; boolean alive=true; String name; Bot(float x,float y,String n){this.x=x;this.y=y;this.name=n;} }
    }
}
