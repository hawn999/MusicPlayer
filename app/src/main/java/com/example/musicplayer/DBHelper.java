package com.example.musicplayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    public static final String CREATE_Diary="create table songs ("
            +"id text  primary key,"//主键歌曲id
            +"name text,"//歌曲名
            +"singer text,"//艺术家
            +"time text,"//歌曲时间
            +"urlOnline text,"//歌曲下载地址
            +"urlLocal text,"//歌曲本地存放路径
            +"isDownloaded text)";//该歌曲是否已下载
    private Context mContext;
    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_Diary);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}