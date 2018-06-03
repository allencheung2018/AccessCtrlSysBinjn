package com.application;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.binjn.accessctrlsysbinjn.MainActivity;
import com.binjn.clienthttp.ComBusiness;
import com.utilcommon.LogFile;
import com.utilcommon.TTSUtils;

import java.util.List;


/**
 * Created by xiaomi2 on 2018/3/28 0028.
 */

public class MainApplication extends Application {
    private static final String mTAG = "MainApplication";
    //
    private static Context context;
    public static CloudPushService pushService;
    private String strLog = "";

    @Override
    public void onCreate() {
        super.onCreate();
        String processName = getProcessName(this);
        strLog = "MainApplication - onCreate:"+processName;
        Log.i(mTAG, strLog);
        LogFile.getInstance().saveMessage(strLog);
        initCloudChannel(this);
        if (processName!= null) {
            if (processName.equals("com.binjn.accessctrlsysbinjn")){
                context = getApplicationContext();
                TTSUtils.getInstance().init();
            }else if (processName.equals("com.binjn.accessctrlsysbinjn:channel")){

            }
        }
    }
    public static Context getContext(){
        return context;
    }

    private String getProcessName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
            if (proInfo.pid == android.os.Process.myPid()) {
                if (proInfo.processName != null) {
                    return proInfo.processName;
                }
            }
        }
        return null;
    }
    /**
     * 初始化云推送通道
     * @param applicationContext
     */
    private void initCloudChannel(Context applicationContext) {
        PushServiceFactory.init(applicationContext);
        pushService = PushServiceFactory.getCloudPushService();
        pushService.register(applicationContext, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                strLog = "init cloudchannel success";
                Log.i(mTAG, strLog);
                LogFile.getInstance().saveMessage(strLog);
                handler.sendEmptyMessage(SUCCESSINIT);
            }
            @Override
            public void onFailed(String errorCode, String errorMessage) {
                strLog = "init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage;
                Log.e(mTAG, strLog);
                LogFile.getInstance().saveMessage(strLog);
            }
        });
    }

    private void checkAliasIsExist() {
        strLog = pushService.getDeviceId();
        Log.i(mTAG, "DeviceId:"+strLog);
        pushService.listAliases(new CommonCallback() {
            @Override
            public void onSuccess(String s) {
                strLog = "别名 === " + s;
                if(TextUtils.isEmpty(s)){
                    //添加别名
                    strLog = strLog + " 需要注册别名";
                    handler.sendEmptyMessage(ALIASNOTEXIST);
                }else if (!s.equals(ComBusiness.idMac)){
                    strLog = strLog + " 需要删除别名";
                    handler.sendEmptyMessage(DELETEALIAS);
                } else {
                    //别名已存在，不添加
                    strLog = strLog + " 别名存在";
                }
                Log.i(mTAG, strLog);
                LogFile.getInstance().saveMessage(strLog);
            }

            @Override
            public void onFailed(String s, String s1) {

            }
        });
    }

    private void addAliaName() {
        String id = ComBusiness.idMac;
        Log.d(mTAG, "id:"+id);
        if (id.equals(""))
            return;
        pushService.addAlias(id, new CommonCallback() {
            @Override
            public void onSuccess(String s) {
                Log.i(mTAG, "别名添加成功===" + s);
            }

            @Override
            public void onFailed(String s, String s1) {

            }
        });
    }

    private void deleteAlias(){
        pushService.removeAlias(null, new CommonCallback() {
            @Override
            public void onSuccess(String s) {
                handler.sendEmptyMessage(ALIASNOTEXIST);
            }

            @Override
            public void onFailed(String s, String s1) {

            }
        });
    }

    private final int SUCCESSINIT = 100;
    private final int ALIASNOTEXIST = 101;
    private final int DELETEALIAS = 102;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SUCCESSINIT:
                    checkAliasIsExist();
                    break;
                case ALIASNOTEXIST:
                    addAliaName();
                    break;
                case DELETEALIAS:
                    deleteAlias();
                    break;
            }
        }
    };
}
