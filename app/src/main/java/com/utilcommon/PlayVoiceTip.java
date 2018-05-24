package com.utilcommon;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PlayVoiceTip {
    private final String mTAG = getClass().getSimpleName();
    private ExecutorService playControlST= Executors.newSingleThreadExecutor();
    private Map<String, String> voiceTipMap = new HashMap<>();

    public PlayVoiceTip(){
    }

    public void setTipInfo(String tips){
        try {
            JSONObject jsonObject = new JSONObject(tips);
            voiceTipMap.clear();
            voiceTipMap.put("rec_success", jsonObject.getString("rec_success"));
            voiceTipMap.put("get_persons_success", jsonObject.getString("get_persons_success"));
            voiceTipMap.put("get_adlist", jsonObject.getString("get_adlist"));
            voiceTipMap.put("lock_video_open", jsonObject.getString("lock_video_open"));
            voiceTipMap.put("lock_remote_open", jsonObject.getString("lock_remote_open"));
            voiceTipMap.put("lock_tmppsw_open", jsonObject.getString("lock_tmppsw_open"));
            voiceTipMap.put("lock_idcard_open", jsonObject.getString("lock_idcard_open"));
            voiceTipMap.put("lock_code_open", jsonObject.getString("lock_code_open"));
            voiceTipMap.put("lock_call", jsonObject.getString("lock_call"));                        //正在呼叫
            voiceTipMap.put("lock_reboot", jsonObject.getString("lock_reboot"));
            voiceTipMap.put("idcard_success", jsonObject.getString("idcard_success"));
            voiceTipMap.put("idcard_unauthorized", jsonObject.getString("idcard_unauthorized"));    //未授权身份证
            voiceTipMap.put("register_begin", jsonObject.getString("register_begin"));              //开始注册请正对摄像头
            voiceTipMap.put("register_success", jsonObject.getString("register_success"));          //注册成功
            voiceTipMap.put("register_unsuccess", jsonObject.getString("register_unsuccess"));      //注册失败
            voiceTipMap.put("network_ok", jsonObject.getString("network_ok"));
            voiceTipMap.put("network_expection", jsonObject.getString("network_expection"));
            voiceTipMap.put("network_disconnect", jsonObject.getString("network_disconnect"));
            voiceTipMap.put("lock_video_hangup", jsonObject.getString("lock_video_hangup"));
            voiceTipMap.put("lock_idcheck_fail", jsonObject.getString("lock_idcheck_fail"));        //人证比对失败
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
    }

    public String playVoice(final String key){
        if (voiceTipMap==null || voiceTipMap.size()==0){
            return null;
        }

        final String tip = voiceTipMap.get(key);
        final int len = tip.length();
        String str = "key:"+key + " 语音："+tip + " len="+len;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        playControlST.execute(new Runnable() {
            @Override
            public void run() {
                TTSUtils.getInstance().speak(tip);
                try {
                    Thread.sleep((len*1000)/3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        return tip;
    }
}
