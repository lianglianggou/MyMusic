package com.example.mymusic;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class bofang extends AppCompatActivity {
    MediaPlayer mediaPlayer = new MediaPlayer();
    ListView mylist;
    List<Song> list;
    ArrayList<String> songName=new ArrayList<>();
    String path1;
    TextView name1;
    Button bn;
    private boolean isSeekBarChanging;//互斥变量，防止进度条与定时器冲突。
    private int currentPosition;//当前音乐播放的进度
    private SeekBar seekBar;
    private Timer timer;
    public class MySeekBar implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
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
        setContentView(R.layout.activity_bofang);
        Intent intent = getIntent();
        final String name = intent.getStringExtra("name");
        seekBar = (SeekBar) findViewById(R.id.playSeekBar);
        seekBar.setOnSeekBarChangeListener(new MySeekBar());
        name1=(TextView)findViewById(R.id.name) ;
        name1.setText(name);
        list = Utils.getmusic(this);
        for(int z=0;z<list.size();z++){
            if(list.get(z).song.equals(name)){
                path1=list.get(z).path;
            }
        }
        bn=(Button)findViewById(R.id.play);
        bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play(path1);
            }
        });
        bn=(Button)findViewById(R.id.stop);
        bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop(path1);
            }
        });
        bn=(Button)findViewById(R.id.pause);
        bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }else{
                    mediaPlayer.start();
                }

            }
        });
        bn=(Button)findViewById(R.id.back);
        bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(bofang.this, Music.class);
                startActivity(intent);
            }
        });

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
    public void stop(String path) {

        try {

            mediaPlayer.reset();
            //        调用方法传进播放地址
            mediaPlayer.setDataSource(path);
//
            mediaPlayer.stop();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public void pause(String path) {

        try {

            mediaPlayer.reset();
            //        调用方法传进播放地址
            mediaPlayer.setDataSource(path);
//            异步准备资源，防止卡顿


                mediaPlayer.pause();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public void restar(String path) {

        try {

            mediaPlayer.reset();
            //        调用方法传进播放地址
            mediaPlayer.setDataSource(path);
//            异步准备资源，防止卡顿


            mediaPlayer.start();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}




