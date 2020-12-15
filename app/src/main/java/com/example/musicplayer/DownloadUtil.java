package com.example.musicplayer;


import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Executors;

public class DownloadUtil extends AsyncTask<Object,Integer,Void> {
    private ProgressBar mPgBar;

    private boolean interceptFlag = false;
    /** 获取指定网络文件url，保存至本地文件路径filePath */
    public static void DownloadFile(final String url, final String filePath)
    {
        ConfirmFile(filePath);

        Executors.newCachedThreadPool().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    URL webUrl = new URL(url);
                    URLConnection con = webUrl.openConnection();	// 打开连接
                    InputStream in = con.getInputStream();			// 获取InputStream
                    File f = new File(filePath);					// 创建文件输出流
                    FileOutputStream fo = new FileOutputStream(f);
//                    int count=0;
//                    int length = con.getContentLength();
                    byte[] buffer = new byte[1024 * 1024];
                    int len = 0;
                    while( (len = in.read(buffer)) > 0)		// 读取文件
                    {

                        fo.write(buffer, 0, len); 			// 写入文件
//                        int numread=in.read(buffer);
//                        count+=numread;
//                        publishProgress((int)(((float)count / length) * 100));//更新进度
                    }
                    in.close();
                    fo.flush();
                    fo.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    /** 判断目录和文件是否存在，若不存在则创建 */
    public static void ConfirmFile(String filePath)
    {
        try
        {
            File f = new File(filePath);
            File parent = f.getParentFile();

            if (!parent.exists()) parent.mkdirs();
            if (!f.exists()) f.createNewFile();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(Object... objects) {
        return null;
    }

    protected void onProgressUpdate(Integer... progress) {
        mPgBar.setProgress(progress[0]);
    }


}
