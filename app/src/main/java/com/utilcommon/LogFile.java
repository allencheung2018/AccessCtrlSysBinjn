package com.utilcommon;

import android.util.Log;

import com.binjn.accessctrlsysbinjn.MainActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xiaomi2 on 2017/12/8 0008.
 */

public class LogFile {
    //static
    public static final String nameLogFile = MainActivity.mainDir + "logFile.log";
    public static final String backupLogFile = MainActivity.mainDir + "bkLogFile.log";

    private static volatile LogFile instance = null;
    private File logFile;
    private FileOutputStream fileOutputStream;

    public static LogFile getInstance(){
        if (instance == null){
            instance = new  LogFile();
        }
        return instance;
    }

    public LogFile(){
        try {
            File file = new File(MainActivity.mainDir);
            if (!file.exists()){
                file.mkdirs();
            }
            file = new File(nameLogFile);
            if (!file.exists()){
                file.createNewFile();
            }
            logFile = new File(nameLogFile);
            fileOutputStream = new FileOutputStream(logFile, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveMessage(String msg){
        SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd  HH:mm:ss.SSS   ");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str  = formatter.format(curDate);
        str += msg+"\r\n";
        byte[] buf = str.getBytes();
        try {
            synchronized (fileOutputStream) {
                fileOutputStream.write(buf);
                fileOutputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void release(){
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFileSdcardFile(String write_str, boolean append)  {
        try {
            //time
            SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd  HH:mm:ss.SSS   ");
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            String str  = formatter.format(curDate);
            str += write_str+"\r\n";
            //file
            File file = new File(nameLogFile);
            if (file.exists() == false){
                file.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(file, append);
            byte[] buf = str.getBytes();
            stream.write(buf);
            stream.close();
        } catch (Exception e) {
            Log.e("LogFile", "writeFileSdcardFile:"+e.toString());
            e.printStackTrace();
        }
    }
}
