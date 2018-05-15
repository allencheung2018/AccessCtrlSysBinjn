package com.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.utilcommon.LogFile;

/**
 * Created by xiaomi2 on 2018/4/26 0026.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String mTAG = "BootBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String str = "BootBroadcastReceiver onReceive - action:" + intent.getAction();
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
//        if (intent.getAction().equals(ACTION)) {
//            Intent mainActivityIntent = new Intent(context, MainActivity.class);  // 要启动的Activity
//            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(mainActivityIntent);
//        }
    }
}
