package com.example.musicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private SeekBar mSeekBar;//进度条
    private Timer timer = new Timer();
    private int currentTime = 0;
    private Button btnPlay;//播放暂停
    private Button btnPre;//上一首
    private Button btnNext;//下一首
    private TextView playingSongName,textCurrtTime,textTotalTime;
    private boolean isSeekBarChanged=false,isPlaying=false;


    private List<Song> list=new ArrayList<>();
    private int location=-1;//列表中的位置
    private int num=0;//从打开开始，是否播放过音乐，0为没有，1为有
    private DBHelper dbHelper;

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case 1:
                    int musicTime = mediaPlayer.getCurrentPosition() / 1000;
                    int min=musicTime / 60;//分钟数
                    String minString=String.valueOf(min);
                    int sec=musicTime % 60;//秒数
                    String secString=String.valueOf(sec);
                    //保障显示两位
                    if (min<10){
                        minString="0"+minString;
                    }
                    if (sec<10){
                        secString="0"+secString;
                    }
                    String show =minString+ ":" + secString;
                    textCurrtTime.setText(show);//同步时间
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
        //播放、暂停按钮
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mediaPlayer.isPlaying()&&num==0){//首次进入app，且未播放歌曲
                    initMediaPlayer(list.get(0).getUrlLocal());//从第一首歌开始播放
                    location=0;//更新位置
                    num++;
                    btnPlay.setText("暂停");
                    String na=list.get(0).getName();
                    playingSongName.setText(na);//更新歌曲名
                    Log.d("hwan","开始播放"+playingSongName.getText().toString());
                }else if(!mediaPlayer.isPlaying()){//暂停时
                    mediaPlayer.start();//开始播放
                    btnPlay.setText("暂停");
                }else{  //如果在播放中，立刻暂停。
                    mediaPlayer.pause();
                    btnPlay.setText("播放");
                }
            }
        });
        //下一首
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //暂停
                    mediaPlayer.stop();
                    btnPlay.setText("暂停");
                    location=(location+1)%list.size();//下个位置
                    initMediaPlayer(list.get(location).getUrlLocal());
                    //更新歌曲名
                    playingSongName.setText(list.get(location).getName());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        //上一首
        btnPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    //暂停
                    mediaPlayer.stop();
                    btnPlay.setText("暂停");
                    location=(location-1+list.size())%list.size();//上个位置
                    initMediaPlayer(list.get(location).getUrlLocal());
                    //更新歌曲名
                    playingSongName.setText(list.get(location).getName());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        //SeekBar监听器
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    //加载ListView
    @Override
    protected void onStart() {
        super.onStart();
        dbHelper =new DBHelper(this,"Songs.db",null,1);
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        //清空列表
        list.clear();
        //加载所有日记
        Cursor cursor=db.query("songs",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String id=cursor.getString(cursor.getColumnIndex("id"));
                String name=cursor.getString(cursor.getColumnIndex("name"));
                String singer=cursor.getString(cursor.getColumnIndex("singer"));
                String time=cursor.getString(cursor.getColumnIndex("time"));
                String urlO=cursor.getString(cursor.getColumnIndex("urlOnline"));
                String urlL=cursor.getString(cursor.getColumnIndex("urlLocal"));
                String isDownloaded=cursor.getString(cursor.getColumnIndex("isDownloaded"));
                Song song=new Song(id,name,singer,time,urlO,urlL,isDownloaded);
                list.add(song);
            }while (cursor.moveToNext());
        }

        Adapter adapter=new Adapter(MainActivity.this,R.layout.item,list);
        final ListView listView=(ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        //点击播放
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Song song=list.get(i);
                String url=song.getUrlOnline();//下载地址
                String path=song.getUrlLocal();
                try
                {
                    File f = new File(path);
                    File parent = f.getParentFile();
                    if (!f.exists()){//音乐不存在-->下载
                        //android 6.0之上的系统除了添加权限还要添加请求权限的代码
                        verifyStoragePermissions(MainActivity.this);
                        DownloadUtil.DownloadFile(url,path);
                        Toast.makeText(MainActivity.this,"音乐未下载，开始下载，请稍等!",Toast.LENGTH_SHORT).show();
                    }else{//音乐存在
                        Toast.makeText(MainActivity.this,"音乐已下载，开始播放!",Toast.LENGTH_SHORT).show();
                        //权限判断，如果没有权限就请求权限
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        } else {
                            initMediaPlayer(path);//初始化播放器 MediaPlayer
                            num++;//播放数目加1
                            location=i;//更新在列表中的位置
                            String na=list.get(i).getName();
                            playingSongName.setText(na);//更新歌曲名
                            Log.d("hwan","开始播放"+playingSongName.getText().toString());
                            btnPlay.setText("暂停");
                        }
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

            }
        });
    }

    private void initialize() {//加载组件
        ListView listView=(ListView)findViewById(R.id.list_view);
        mSeekBar=(SeekBar) findViewById(R.id.mSeekbar);
        btnNext=(Button)findViewById(R.id.btnNext);
        btnPlay=(Button)findViewById(R.id.btnPlay);
        btnPre=(Button)findViewById(R.id.btnPre);
        playingSongName=(TextView)findViewById(R.id.playingSongName);
        textCurrtTime=(TextView)findViewById(R.id.currentTime);
        textTotalTime=(TextView)findViewById(R.id.totalTime);
    }

    //初始化播放
    private void initMediaPlayer(String path) {
        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);//指定音频文件路径
            mediaPlayer.setLooping(true);//设置为循环播放
            mediaPlayer.prepare();//初始化播放器MediaPlayer
            mediaPlayer.start();//开始播放
            num++;
            isPlaying=true;
            //设置最大值
            mSeekBar.setMax(mediaPlayer.getDuration());
            int musicTime = mediaPlayer.getDuration() / 1000;
            int min=musicTime / 60;//分钟数
            String minString=String.valueOf(min);
            int sec=musicTime % 60;//秒数
            String secString=String.valueOf(sec);
            //保障显示两位
            if (min<10){
                minString="0"+minString;
            }
            if (sec<10){
                secString="0"+secString;
            }
            String show =minString+ ":" + secString;
            textTotalTime.setText(show);

            currentTime = 0;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!isSeekBarChanged&&mediaPlayer.isPlaying()){//如果进度条未改变，并且当前正在播放
                    mSeekBar.setProgress(mediaPlayer.getCurrentPosition());//同步进度条
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
            }
        },0,1000);

    }


    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isSeekBarChanged = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isSeekBarChanged = false;
        mediaPlayer.seekTo(mSeekBar.getProgress());
    }

    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    //初始化组件
//    private void initialize() {
//        DBHelper dbHelper =new DBHelper(this,"Songs.db",null,1);
//        final SQLiteDatabase db=dbHelper.getWritableDatabase();
//
//        Button init=(Button)findViewById(R.id.iniButton);
//        init.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //初始化数据库
////                ContentValues values=new ContentValues();
////                String id="1",
////                        name="Rescue Me",
////                        singer="Derek Clegg",
////                        time="03:07",
////                        urlOnline="https://freemusicarchive.org/track/rescue-me/download",
////                        urlLocal="/mnt/sdcard/Music/1.mp3",
////                        isDownloaded="false";
////                values.put("id",id);
////                values.put("name",name);
////                values.put("singer",singer);
////                values.put("time",time);
////                values.put("urlOnline",urlOnline);
////                values.put("urlLocal",urlLocal);
////                values.put("isDownloaded",isDownloaded);
////                db.insert("songs",null,values);
////                Toast.makeText(MainActivity.this,"添加成功1",Toast.LENGTH_SHORT).show();
////
////                id="2";
////                name="Lowest Face";
////                singer="Derek Clegg";
////                time="03:16";
////                urlOnline="https://freemusicarchive.org/track/lowest-face/download";
////                urlLocal="/mnt/sdcard/Music/2.mp3";
////                isDownloaded="false";
////                values.put("id",id);
////                values.put("name",name);
////                values.put("singer",singer);
////                values.put("time",time);
////                values.put("urlOnline",urlOnline);
////                values.put("urlLocal",urlLocal);
////                values.put("isDownloaded",isDownloaded);
////                db.insert("songs",null,values);
////                Toast.makeText(MainActivity.this,"添加成功2",Toast.LENGTH_SHORT).show();
////
////                id="3";
////                name="Psychic";
////                singer="Ketsa";
////                time="03:03";
////                urlOnline="https://freemusicarchive.org/track/psychic/download";
////                urlLocal="/mnt/sdcard/Music/3.mp3";
////                isDownloaded="false";
////                values.put("id",id);
////                values.put("name",name);
////                values.put("singer",singer);
////                values.put("time",time);
////                values.put("urlOnline",urlOnline);
////                values.put("urlLocal",urlLocal);
////                values.put("isDownloaded",isDownloaded);
////                db.insert("songs",null,values);
////                Toast.makeText(MainActivity.this,"添加成功3",Toast.LENGTH_SHORT).show();
////
////                id="4";
////                name="Deuteranomaly";
////                singer="Les Hayden";
////                time="02:23";
////                urlOnline="https://freemusicarchive.org/track/deuteranomaly1727/download";
////                urlLocal="/mnt/sdcard/Music/4.mp3";
////                isDownloaded="false";
////                values.put("id",id);
////                values.put("name",name);
////                values.put("singer",singer);
////                values.put("time",time);
////                values.put("urlOnline",urlOnline);
////                values.put("urlLocal",urlLocal);
////                values.put("isDownloaded",isDownloaded);
////                db.insert("songs",null,values);
////                Toast.makeText(MainActivity.this,"添加成功4",Toast.LENGTH_SHORT).show();
////
////                id="5";
////                name="Hot Pink";
////                singer="Chad Crouch";
////                time="02:30";
////                urlOnline="https://freemusicarchive.org/track/Hot_Pink/download";
////                urlLocal="/mnt/sdcard/Music/5.mp3";
////                isDownloaded="false";
////                values.put("id",id);
////                values.put("name",name);
////                values.put("singer",singer);
////                values.put("time",time);
////                values.put("urlOnline",urlOnline);
////                values.put("urlLocal",urlLocal);
////                values.put("isDownloaded",isDownloaded);
////                db.insert("songs",null,values);
////                Toast.makeText(MainActivity.this,"添加成功5",Toast.LENGTH_SHORT).show();
//
//                //下载测试
////                String url="https://freemusicarchive.org/track/rescue-me/download";
////                String filePath="/mnt/sdcard/Download/1.mp3";
////                //android 6.0之上的系统除了添加权限还要在你报错的代码前面添加请求权限的代码
////                verifyStoragePermissions(MainActivity.this);
////                DownloadUtil.DownloadFile(url,filePath);
////                Toast.makeText(MainActivity.this,"开始下载!",Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

}

class Adapter extends ArrayAdapter<Song> {
    private int resourceId;
    public Adapter(@NonNull Context context, int resource, List<Song> objects) {
        super(context, resource,objects);
        resourceId=resource;
    }
    @Override
    //修改getView，提高性能
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Song song=getItem(position);
        View view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        TextView item_song_name=(TextView)view.findViewById(R.id.song_name);
        TextView item_song_singer=(TextView)view.findViewById(R.id.song_singer);
        item_song_name.setText(song.getName());
        item_song_singer.setText(song.getSinger());
        return view;
    }
}
