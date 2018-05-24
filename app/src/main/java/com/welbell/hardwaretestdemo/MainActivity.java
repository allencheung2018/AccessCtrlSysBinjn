package com.welbell.hardwaretestdemo;

import android.util.Log;
import android.view.ViewOutlineProvider;

import com.utilcommon.LogFile;
import com.welbell.hardware.HardWareUpEvent;
import com.welbell.hardware.HardwareSupport;

/**
 * Created by xiaomi2 on 2017/11/29 0029.
 */

public class MainActivity {
    private static String mTAG = "welbellHardwareMainActivity";
    //class
    private OnHardwreCallBack onHardwreCallBack;
    private String str = "";
    private String version = "0";

    private HardwareSupport hardwareResource = new HardwareSupport();

    public MainActivity(String pkgName, String className){
        //获取系统版本号

        try {
            version = hardwareResource.getHardWareVersion();
        } catch (Exception e){
            Log.e(mTAG, "getHardWareVersion error:"+e.toString());
            e.printStackTrace();
        }
        str = "HardWareVersion:"+version;
        Log.i(mTAG,str);
        LogFile.getInstance().saveMessage(str);
//        hardwareResource.delDaemonServer();
        hardwareResource.addAPPtoDaemon(pkgName, className);
        hardwareResource.addEventCallBack(new HardWareCallBack());
//        int a = a8HardwareControlInit();
//        if (a == 0) {
//            str = str + " a8HardwareControlInit success";
//            Log.d(mTAG, str);
//        }
//        else {
//            str = str + " a8HardwareControlInit fail";
//            Log.d(mTAG, str);
//        }
        LogFile.getInstance().saveMessage(str);
    }


    private class HardWareCallBack implements HardWareUpEvent{

        @Override
        public void someoneCloseEvent() {
            Log.d(mTAG, "HardWareCallBack someoneCloseEvent");
            LogFile.getInstance().saveMessage("HardWareCallBack someoneCloseEvent");
            onHardwreCallBack.onInfrared(2);
        }

        @Override
        public void doorLockKeyEvent(byte b) {
            Log.d(mTAG, "HardWareCallBack doorLockKeyEvent:"+b);
        }

        @Override
        public void doorMagneticEvent(byte b) {
            Log.d(mTAG, "HardWareCallBack doorMagneticEvent:"+b);
        }

        @Override
        public void preventSeparateEvent(byte b) {
            Log.d(mTAG, "HardWareCallBack preventSeparateEvent:"+b);
        }

        @Override
        public void doorCardBandAlgEvent(byte b, String s) {
            Log.d(mTAG, "HardWareCallBack doorCardBandAlgEvent:"+b + " :"+s);
        }

        @Override
        public void doorCardBandRawEvent(byte b, byte[] bytes) {
            Log.d(mTAG, "HardWareCallBack doorCardBandRawEvent");
        }

        @Override
        public void keyBoardEvent(int i, int i1) {
            Log.d(mTAG, "HardWareCallBack keyBoardEvent:"+i + " il:"+i1);
        }
    }

    public void controlVisibleCamereLight(boolean flag){
        hardwareResource.cameraLightControl(flag);
    }

    public void controlNIRCameraLight(boolean flag){
        hardwareResource.ifcameraLightControl(flag);
    }

    public void controlKeyboardLight(boolean flag){
        hardwareResource.keyboardLightControl(flag);
    }

    public void controlDoorLock(boolean flag){
        hardwareResource.doorLockControl(flag);
    }

    public void restartMachine(){
        hardwareResource.reboot();
    }

    private void systemCallBack(byte[] Data) {
        if (Data.length == 0)
            return;
        String sCMD= Integer.toHexString(Data[0]);
        Log.i(mTAG, "systemCallBack CMD:0x"+sCMD);
        switch (Data[0]) {
            case CallBackState.UI_INFRARED_DEVICE: //检测人体红外触发,1：人物靠近 0：人物离开

                if (Data[1] == 1) {
                    Log.d(mTAG, "检测人体红外触发 : 靠近");
                    onHardwreCallBack.onInfrared(Data[1]);
                }
                else{
                    onHardwreCallBack.onInfrared(Data[1]);
                    Log.d(mTAG, "检测人体红外触发 : 离开");
                }
                break;
            default:
                break;
        }
    }

    public void openCameraLight(){
        byte[] valve_key_open = new byte[32];
        valve_key_open[0] = 0x01;
        a8SetKeyValue(controlHardwareCmd.E_KEY_LIGHT, valve_key_open, 1);
    }

    public void closeCameraLight(){
        byte[] valve_key_close = new byte[32];
        valve_key_close[0] = 0x00;
        a8SetKeyValue(controlHardwareCmd.E_KEY_LIGHT, valve_key_close, 1);
    }

    public void openLock(){
        byte[] valve_door_lock = new byte[32];
        valve_door_lock[0] = 0x01;
        a8SetKeyValue(controlHardwareCmd.E_DOOR_LOCK, valve_door_lock, 1);
    }

    public void closeLock(){
        byte[] valve_door_lock_close = new byte[32];
        valve_door_lock_close[0] = 0x00;
        a8SetKeyValue(controlHardwareCmd.E_DOOR_LOCK, valve_door_lock_close, 1);
    }

    public void rebootMachine(){
        byte[] valve_reatart = new byte[32];
        valve_reatart[0] = 0x01;
        a8SetKeyValue(controlHardwareCmd.E_RESTART, valve_reatart, 1);
    }

    public void setOnHardwreCallBack(OnHardwreCallBack onHardwreCallBack){
        this.onHardwreCallBack = onHardwreCallBack;
    }

    public void exit(){
        //JNI库退出函数，在app退出时候调用; 调用成功返回0,调用失败返回-1
        int exitResult = a8HardwareControlExit();
        if (exitResult == 0) {
            Log.d(mTAG, "JNI库退出");
        } else if (exitResult == -1) {
            Log.d(mTAG, "JNI库退出失败");
        }
    }

    static {
        try {
            System.loadLibrary("NativeHardwareSupport");
            Log.i(mTAG, "loadLibrary-HardwareSupport-Success");
        } catch (Exception e) {
            Log.i(mTAG, "loadLibrary-HardwareSupport-Failed");
            e.printStackTrace();
        }
    }

    native int a8HardwareControlInit();

    native int a8SetKeyValue(int key, byte[] valve, int valueLen);

    native int a8HardwareControlExit();

    public interface OnHardwreCallBack
    {
        public void onInfrared(int act);
    }

    public String getVersion() {
        return version;
    }
}
