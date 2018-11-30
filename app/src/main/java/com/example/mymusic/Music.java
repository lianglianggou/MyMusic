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
import android.media.MediaPlayer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Music extends AppCompatActivity{
    MediaPlayer mediaPlayer=new MediaPlayer();
    ListView mylist;
    List<Song> list;
    ArrayList<String> songName=new ArrayList<>();
    Button bn;

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
//                Intent intent = new Intent(MainActivity.this, CheckActivity.class);//实现点击菜单选项启动相应活动
//                startActivity(intent);
                //checkDialog();
//                Toast.makeText(this,"check",Toast.LENGTH_SHORT).show();
                break;

            default:
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        mylist = (ListView) findViewById(R.id.mylist);

        list = new ArrayList<>();

        list = Utils.getmusic(this);
        for(int z=0;z<list.size();z++){


                songName.add(list.get(z).song);

        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(Music.this,R.layout.support_simple_spinner_dropdown_item,songName);
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
            default:
                break;
        }

        return super.onContextItemSelected(item);

    }
    public void play(String path) {

        try {

            mediaPlayer.reset();
            //        调用方法传进播放地址
            mediaPlayer.setDataSource(path);
//            异步准备资源，防止卡顿
            mediaPlayer.prepareAsync();
//            调用音频的监听方法，音频准备完毕后响应该方法进行音乐播放
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });

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
//              把歌曲名字和歌手切割开

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