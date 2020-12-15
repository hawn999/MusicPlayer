package com.example.musicplayer;

public class Song {
    // 歌名、歌手、时长、url、是否已下载
    private String id;
    private String name;
    private String singer;
    private String time;
    private String urlOnline;
    private String urlLocal;
    private String isDownloaded;

    public Song() {
    }

    public Song(String id, String name, String singer, String time, String urlOnline, String urlLocal, String isDownloaded) {
        this.id = id;
        this.name = name;
        this.singer = singer;
        this.time = time;
        this.urlOnline = urlOnline;
        this.urlLocal = urlLocal;
        this.isDownloaded = isDownloaded;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrlOnline() {
        return urlOnline;
    }

    public void setUrlOnline(String urlOnline) {
        this.urlOnline = urlOnline;
    }

    public String getUrlLocal() {
        return urlLocal;
    }

    public void setUrlLocal(String urlLocal) {
        this.urlLocal = urlLocal;
    }

    public String getIsDownloaded() {
        return isDownloaded;
    }

    public void setIsDownloaded(String isDownloaded) {
        this.isDownloaded = isDownloaded;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
