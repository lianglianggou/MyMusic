package com.example.mymusic;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaPlayer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Music extends AppCompatActivity{
    MediaPlayer mediaPlayer=new MediaPlayer();
    ListView mylist;
    List<Song> list;
    ArrayList<String> songName=new ArrayList<>();
    MyDatabaseHelper dbmemo;
    Button bn;
    private boolean isSeekBarChanging;//互斥变量，防止进度条与定时器冲突。
    private int currentPosition;//当前音乐播放的进度
    private SeekBar seekBar;
    private Timer timer;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.find:
                final EditText et = new EditText(this);
//                 final EditText et = new EditText(this);
                //final String[] where1={date.get(MID)};
                //final int c=MID;
                et.setText("");
                new AlertDialog.Builder(this).setTitle("请输入：")
                        .setIcon(android.R.drawable.sym_def_app_icon)
                        .setView(et)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String a=et.getText().toString();
                                Intent intent=new Intent(Music.this,find.class);
                                intent.putExtra("song",a);
                                startActivity(intent);
                            }
                        }).setNegativeButton("取消",null).show();
                break;
            case R.id.love:
                                Intent intent=new Intent(Music.this,love.class);
                                startActivity(intent);
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
        setContentView(R.layout.activity_music);
        mylist = (ListView) findViewById(R.id.mylist);
        seekBar = (SeekBar) findViewById(R.id.playSeekBar);
        seekBar.setOnSeekBarChangeListener(new MySeekBar());
        dbmemo=new MyDatabaseHelper(this,"love1.db",null,1);
        /*
        bn=(Button)findViewById(R.id.create);
        bn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                dbmemo.getWritableDatabase();
            }
        });*/
        list = new ArrayList<>();
        list = Utils.getmusic(this);
        for(int z=0;z<list.size();z++){
                songName.add(list.get(z).song);
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(Music.this,R.layout.support_simple_spinner_dropdown_item,songName);
        ListView listView=(ListView)findViewById(R.id.mylist);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s=songName.get(position);
                Intent intent=new Intent(Music.this,bofang.class);
                intent.putExtra("name",s);
                startActivity(intent);
            }
        });
        ItemOnLongClick1();
    }
    public class MyDatabaseHelper extends SQLiteOpenHelper {
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
    private void ItemOnLongClick1() {
        mylist = (ListView) findViewById(R.id.mylist);
        mylist.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0, 0, 0, "播放");
                menu.add(0, 1, 0, "停止");
                menu.add(0, 2, 0, "循环");
                menu.add(0, 3, 0, "暂停");
                menu.add(0, 4, 0, "上一曲");
                menu.add(0, 5, 0, "下一曲");
                menu.add(0, 6, 0, "收藏");
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
                if(loop1){
                    mediaPlayer.setLooping(!loop1);
                }else{
                    mediaPlayer.setLooping(!loop1);
                }
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
                String a=songName.get(MID);
                String[] where={a};
                SQLiteDatabase db = dbmemo.getWritableDatabase();
                // Cursor cursor = db.query("WordTable",null,null,null,null,null,null);
                Cursor cursor =  db.query("love1",new String[]{"song"},"song=?",where,null,null,null);
                if(cursor.moveToFirst()){
                    Toast.makeText(Music.this,"已收藏",Toast.LENGTH_SHORT).show();
                    cursor.close();
                }else{
                    ContentValues values=new ContentValues();
                    values.put("song",a);
                    db.insert("love1",null,values);
                    Toast.makeText(Music.this,"收藏成功",Toast.LENGTH_SHORT).show();}
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }
    public void play(String path) {
        try {
            mediaPlayer.reset();
            //调用方法传进播放地址
            mediaPlayer.setDataSource(path);
//            异步准备资源，防止卡顿
            mediaPlayer.prepareAsync();
//            调用音频的监听方法，音频准备完毕后响应该方法进行音乐播放
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

class Song implements Serializable {
    public String song;//歌曲名
    public String singer;//歌手
    public long size;//歌曲所占空间大小
    public int duration;//歌曲时间长度
    public String path;//歌曲地址

}

class Utils {
    //定义一个集合，存放从本地读取到的内容
    public static List<Song> list;
    public static Song song;
    public static List<Song> getmusic(Context context) {
        list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                , null, null, null, MediaStore.Audio.AudioColumns.IS_MUSIC);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                song = new Song();
                song.song = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                song.singer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                song.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                song.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                song.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                if (song.size > 1000 * 800) {
                    if (song.song.contains("-")) {
                        String[] str = song.song.split("-");
                        song.singer = str[0];
                        song.song = str[1];
                    }
                    list.add(song);
                }
            }
        }
        cursor.close();
        return list;

    }

    //    转换歌曲时间的格式
    public static String formatTime(int time) {
        if (time / 1000 % 60 < 10) {
            String tt = time / 1000 / 60 + ":0" + time / 1000 % 60;
            return tt;
        } else {
            String tt = time / 1000 / 60 + ":" + time / 1000 % 60;
            return tt;
        }
    }


}
