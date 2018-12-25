package com.example.mymusic;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class love extends AppCompatActivity {
    MediaPlayer mediaPlayer = new MediaPlayer();
    ListView mylist;
    List<Song> list;
    List<Song> list1;
    ArrayList<String> songName=new ArrayList<>();
    ArrayList<String> songName1=new ArrayList<>();
    MyDatabaseHelper dbmemo;
    Song s;
    private boolean isSeekBarChanging;//互斥变量，防止进度条与定时器冲突。
    private int currentPosition;//当前音乐播放的进度
    private SeekBar seekBar;
    private Timer timer;
    int count=0;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back:
                Intent intent = new Intent(love.this, Music.class);
                startActivity(intent);
                finish();
                break;
            default:
        }
        return true;
    }
    protected void onDestroy() {
        mediaPlayer.release();
        timer.cancel();
        timer = null;
        mediaPlayer = null;
        super.onDestroy();
    }
    public class MySeekBar implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }
        /*滚动时,应当暂停后台定时器*/
        public void onStartTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = true;
        }
        /*滑动结束后，重新设置值*/
        public void onStopTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = false;
            mediaPlayer.seekTo(seekBar.getProgress());
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_love);
        seekBar = (SeekBar) findViewById(R.id.playSeekBar);
        seekBar.setOnSeekBarChangeListener(new MySeekBar());
        dbmemo=new MyDatabaseHelper(this,"love1.db",null,1);
        mylist=(ListView) findViewById(R.id.mylist);
        SQLiteDatabase db=dbmemo.getWritableDatabase();
        Cursor cursor=db.query("love1",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String author=cursor.getString(cursor.getColumnIndex("song"));
                songName.add(count++,author);
            }while (cursor.moveToNext());
            cursor.close();
        }
        list1 = new ArrayList<>();
        list = new ArrayList<>();
        list1 = Utils.getmusic(this);
        for(int z=0;z<songName.size();z++){
            for(int zz=0;zz<list1.size();zz++){
                if(songName.get(z).equals(list1.get(zz).song)){
                    list.add(list1.get(zz));
                    songName1.add(songName.get(z));
                }
            }
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(love.this,R.layout.support_simple_spinner_dropdown_item,songName1);
        ListView listView=(ListView)findViewById(R.id.mylist);
        listView.setAdapter(adapter);
        ItemOnLongClick1();
    }
    private void ItemOnLongClick1() {
        mylist = (ListView) findViewById(R.id.mylist);
        mylist.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0, 0, 0, "播放");
                menu.add(0, 1, 0, "停止");
                menu.add(0, 2, 0, "循环");
                menu.add(0, 3, 0, "暂停");
                menu.add(0, 4, 0, "上一曲");
                menu.add(0, 5, 0, "下一曲");
                menu.add(0, 6, 0, "删除");
            }
        });
    }
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        int MID = (int) info.id;// 这里的info.id对应的就是数据库中_id的值
        switch (item.getItemId()) {
            case 0:
                String p = list.get(MID).path;//获得歌曲的地址
                play(p);
                break;
            case 1:
                mediaPlayer.stop();
                break;
            case 2:
                boolean loop1 = mediaPlayer.isLooping();
                mediaPlayer.setLooping(!loop1);
                break;
            case 3:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }else{
                    mediaPlayer.start();
                }
                break;
            case 4:
                String p1 = list.get(MID-1).path;
                play(p1);
                break;
            case 5:
                String p2 = list.get(MID+1).path;
                play(p2);
                break;
            case 6:
                String a=songName1.get(MID);
                SQLiteDatabase db=dbmemo.getWritableDatabase();
                String[] where={a};
                db.delete("love1","song=?",where);
                songName1.remove(a);
                ArrayAdapter<String> adapter=new ArrayAdapter<String>(love.this,R.layout.support_simple_spinner_dropdown_item,songName1);
                ListView listView=(ListView)findViewById(R.id.mylist);
                listView.setAdapter(adapter);
                Toast.makeText(love.this,"删除成功",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }
    public void play(String path) {

        try {

            mediaPlayer.reset();
            // 调用方法传进播放地址
            mediaPlayer.setDataSource(path);
            //异步准备资源，防止卡顿
            mediaPlayer.prepareAsync();
            //调用音频的监听方法，音频准备完毕后响应该方法进行音乐播放
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    mp.seekTo(currentPosition);
                    seekBar.setMax(mediaPlayer.getDuration());
                }
            });
            //监听播放时回调函数
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(!isSeekBarChanging){
                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    }
                }
            },0,50);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
    class MyDatabaseHelper extends SQLiteOpenHelper {
        public static final String CREATE_LOVE = "create table love1 ("
                + "song text"+")";
        private Context mContext;
        public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
            mContext = context;
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_LOVE);
            Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }



