package com.binjn.clienthttp;

import android.content.Context;
import android.util.Log;

import com.binjn.accessctrlsysbinjn.MainActivity;
import com.binjn.faceimgresovle.FaceDetRec;
import com.binjn.protocoldatainfo.ADInfo;
import com.binjn.protocoldatainfo.ManageablePersonInfo;
import com.binjn.protocoldatainfo.PersonRegFilesInfo;
import com.binjn.protocoldatainfo.SimilarPerson;
import com.binjn.protocoldatainfo.UnregNirPersonInfo;
import com.utilcommon.LogFile;
import com.utilcommon.SharePrefUtil;
import com.utilcommon.UtilCommon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaomi2 on 2017/12/7 0007.
 */

public class ComBusiness {
    //
    private static final String mTAG = "ComBusiness";
    public static final String urlBinjn = "https://icbc.binjn.com:59002/phs/";//"http://192.168.10.140:59002/phs/";//"https://icbc.binjn.com:59002/phs/";//"http://192.168.10.140:59002/phs/";//"http://skull.vicp.cc:15074/phs/";//"https://icbc.binjn.com:59002/phs/";//";//;
    public static final String keyBinjn = "binjn";
    public static String tokenBinjn = "";
    public static String timeTolen = "";
    public static String idMac = "";                            //门禁ID
    public final int expireMax = 43000;
    public static final String FILE_TYPE_FILE = "file/*";
    public static final String FILE_TYPE_IMAGE = "image/*";
    public static final String FILE_TYPE_AUDIO = "audio/*";
    public static final String FILE_TYPE_VIDEO = "video/*";
    public static final int VISIBLEFACETYPE = 10122;
    public static final int PASSRECRESULT = 10125;
    public static final int DENYRECRESULT = 10126;
    //
    private Context context;
    //var
    private String strLog = "";
    //
    private static enum COMMAND{NONE, MPERSONLIST};

    public ComBusiness(Context context){
        this.context = context;
        idMac = SharePrefUtil.getString(context, SharePrefUtil.MACHINEID, "");
        strLog = "ComBusiness init idMac:"+idMac;
        Log.i(mTAG, strLog);
        LogFile.getInstance().saveMessage(strLog);
    }

    public int getToken(String timeStamp){
        int code = -1;
        String str = "获取新 Token";
        HashMap<String,String> params = new HashMap<>();

        String apikey = MainActivity.addrMac;
        params.put("apikey", apikey);
        String ts = null;
        if (timeStamp == null){
            ts = UtilCommon.getTimeStampCurrent();
        }else {
            ts = timeStamp;
        }
        params.put("timestamp", ts);
        String secretkeyBinjn = UtilCommon.md51(keyBinjn + apikey);
        String sign = "";
        try {
            sign = UtilCommon.getSignature(params, secretkeyBinjn);
        } catch (IOException e) {
            e.printStackTrace();
        }
        params.put("sign", sign);
        str = str + "-apikey:"+apikey + " time:"+ts + " sign:"+sign;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        String result = OkHttpUtil.getDatasync(urlBinjn+"security/token", params, null);//requestGet(params, "security/token");
        if (result == null){
            str = "获取token失败：" + result;
            Log.e(mTAG, str);
            LogFile.getInstance().saveMessage(str);
            return -1;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            code = jsonObject.getInt("code");
            if (code == 0) {
                tokenBinjn = jsonObject.getString("token");
                str = jsonObject.getString("expires_in");
                str = "获取token:" + tokenBinjn + " token过期剩余时间:" + str + "s";
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                timeTolen = ts;
                SharePrefUtil.saveString(context, SharePrefUtil.TOKENBINJN, tokenBinjn);
                SharePrefUtil.saveString(context, SharePrefUtil.TIMETOKEN, timeTolen);
            }else if (code == 3007){
                str = "获取token失败:" + code;
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            str = "getToken:"+e.toString();
            Log.e(mTAG, str);
            LogFile.getInstance().saveMessage(str);
        }
        return code;
    }

    /**
     * 计算token是否过期
     * @return
     */
    public boolean isTokenValid(){
        boolean result = false;
//        tokenBinjn = SharePrefUtil.getString(context, SharePrefUtil.TOKENBINJN, "");
//        timeTolen = SharePrefUtil.getString(context, SharePrefUtil.TIMETOKEN, "");
        String str = "token:"+tokenBinjn + " timeToken:"+timeTolen;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (tokenBinjn.equals("") || timeTolen.equals("")){
            return result;
        }
        Date curDate = new Date();
        long timeExpired = (curDate.getTime() - UtilCommon.getDateFromString(timeTolen).getTime())/1000;
        str = "token 已使用：" + timeExpired + " s";
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (timeExpired>=0 && timeExpired<=expireMax){
            result = true;
        }
        return result;
    }

    /*

     */
    public String registerMachine(String addrMac){
        if (!isTokenValid()){
            getToken(null);
        }
        HashMap<String,String> paramsHeader = new HashMap<>();
        paramsHeader.put("token", tokenBinjn);
        String result = OkHttpUtil.postDataSync(urlBinjn+"v1/lock/register/"+addrMac, null, paramsHeader);
        strLog = "门禁机注册接口返回:" + result;
        Log.i(mTAG, strLog);
        LogFile.getInstance().saveMessage(strLog);
        if (result == null){
            return null;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            int code = jsonObject.getInt("code");
            if (code == 0) {
                idMac = jsonObject.getString("id");
                SharePrefUtil.saveString(context, SharePrefUtil.MACHINEID, idMac);
                return idMac;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            strLog = "门禁机注册接口返回异常:" + e.toString();
            Log.i(mTAG, strLog);
            LogFile.getInstance().saveMessage(strLog);
        }
        return null;
    }

    /*

     */
    public List<String> getNirFeatureAll(){
        String str = "获取指定门禁管理所有人员的近红外特征值校验码列表";
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (!isTokenValid()){
            getToken(null);
        }
        List<String> personIds = new ArrayList<>();
        HashMap<String,String> params = new HashMap<>();

        String result = null;//requestGet(params, "v1/lock/" + idMac + "/nir/feature/hashcode");
        str = "近红外特征值校验码列表返回:" + result;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);

        return personIds;
    }

    public Map<String, ManageablePersonInfo> getManageablePersonList(){
        Map<String, ManageablePersonInfo> mpiMap = new HashMap<>();
        String str = "获取门禁设备管理的人员列表";
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (!isTokenValid()){
            getToken(null);
        }
        HashMap<String,String> paramsHeader = new HashMap<>();
        paramsHeader.put("token", tokenBinjn);
        String result = OkHttpUtil.getDatasync(urlBinjn+"v1/lock/"+idMac+"/persons", null, paramsHeader);
        if (result == null){
            return null;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            int code = jsonObject.getInt("code");
            if (code == 0) {
                JSONArray jsonArray = jsonObject.getJSONArray("persons");
                int len = jsonArray.length();
                for (int i = 0; i < len; i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);
                    ManageablePersonInfo mpi = new ManageablePersonInfo();
                    String pid = jo.getString("id");
                    mpi.setPid(pid);
                    Log.d(mTAG, "getManageablePersonList id:" + jo.getLong("id"));
                    mpi.setName(jo.getString("name"));
                    mpi.setIdNo(jo.getString("idNo"));
                    mpi.setHashcode(jo.getString("hashcode"));
                    mpi.setHashcodeNir(jo.getString("hashcodeNir"));
                    mpi.setVisibleFaceRegistered(jo.getBoolean("visibleFaceRegistered"));
                    mpi.setNirFaceRegistered(jo.getBoolean("nirFaceRegistered"));
                    mpiMap.put(pid, mpi);
                }
            } else {
                str = "获取门禁设备管理的人员列表返回错误:"+result;
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        str = "门禁设备管理的人员列表返回:" + mpiMap.size() + " :" + result;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        return mpiMap;
    }

    /**
     * 获取门禁设备管理的人员列表
     * @param recode
     * @return
     */
    public List<ManageablePersonInfo> getManageablePersonList(int[] recode){
        boolean flag = false;
        List<ManageablePersonInfo> manageablePersonInfo = new ArrayList<>();
        String str = "获取门禁设备管理的人员列表:"+manageablePersonInfo.size();
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (!isTokenValid()){
            getToken(null);
        }
        HashMap<String,String> paramsHeader = new HashMap<>();
        paramsHeader.put("token", tokenBinjn);
        HashMap<String,String> params = new HashMap<>();
//        params.put("faceType", String.valueOf(VISIBLEFACETYPE));
        String result = OkHttpUtil.getDatasync(urlBinjn+"v1/lock/"+idMac+"/persons", params, paramsHeader);
                //requestGet(params, "v1/lock/" + idMac + "/persons");
        if (result == null){
            return null;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            int code = jsonObject.getInt("code");
            if (code == 0) {
                JSONArray jsonArray = jsonObject.getJSONArray("persons");
                int len = jsonArray.length();
                for (int i = 0; i < len; i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);
                    ManageablePersonInfo mpi = new ManageablePersonInfo();
                    mpi.setPid(jo.getString("id"));
                    Log.d(mTAG, "getManageablePersonList id:" + jo.getLong("id"));
                    mpi.setName(jo.getString("name"));
                    mpi.setIdNo(jo.getString("idNo"));
                    mpi.setHashcode(jo.getString("hashcode"));
                    mpi.setHashcodeNir(jo.getString("hashcodeNir"));
                    mpi.setVisibleFaceRegistered(jo.getBoolean("visibleFaceRegistered"));
                    mpi.setNirFaceRegistered(jo.getBoolean("nirFaceRegistered"));
                    manageablePersonInfo.add(mpi);
                }
            }else if(code == 401){
                if(jsonObject.get("msg").equals("token失效，请重新登录")){
                    getToken(null);
                    recode[0] = code;
                    str = result;
                    Log.i(mTAG, str);
                    LogFile.getInstance().saveMessage(str);
                    return null;
                }
            }
            else {
                str = "获取门禁设备管理的人员列表返回错误:"+result;
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        str = "门禁设备管理的人员列表返回:" + manageablePersonInfo.size() + " :" + result;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        return manageablePersonInfo;
    }

    /**
     * 根据人员ID查询人员信息
     * @param pid
     * @return
     */
    public ManageablePersonInfo getPersonInfoWithId(String pid){
        ManageablePersonInfo mpi = new ManageablePersonInfo();
        String str = "根据人员ID查询人员信息:" + pid;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (!isTokenValid()){
            getToken(null);
        }

        HashMap<String,String> paramsHeader = new HashMap<>();
        paramsHeader.put("token", tokenBinjn);
        String result = OkHttpUtil.getDatasync(urlBinjn+"/v1/person/info/"+idMac,
                null, paramsHeader);
        if (result == null){
            return null;
        }
        str = str + " 返回："+result;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            int code = jsonObject.getInt("code");
            if (code == 0) {
                JSONObject person = jsonObject.getJSONObject("person");

                return mpi;
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<ADInfo> getAllADInfo(){
        String str = "查询所有广告资源";
        ArrayList<ADInfo> adInfos = new ArrayList<>();

        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (!isTokenValid()){
            getToken(null);
        }

        HashMap<String,String> headerMap = new HashMap<>();
        headerMap.put("token", tokenBinjn);
        String result = OkHttpUtil.getDatasync(urlBinjn+"v1/ad/all/"+idMac, null, headerMap);

        str = str + " 返回："+result;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (result == null){
            return adInfos;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            int code = jsonObject.getInt("code");
            if (code == 0) {
                int total = jsonObject.getInt("total");
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < total; i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);
                    ADInfo adInfo = new ADInfo();
                    adInfo.setResId(jo.getString("resId"));
                    adInfo.setPlaytime(jo.getInt("palytime"));
                    adInfo.setFilename(jo.getString("filename"));
                    adInfo.setUrl(jo.getString("url"));
                    adInfo.setType(jo.getInt("type"));
                    adInfo.setFilesize(jo.getInt("filesize"));
                    adInfos.add(adInfo);
                }
            }else {
                str = str + " 返回错误："+result;
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                return adInfos;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonObject==null){
            return adInfos;
        }
        int size = adInfos.size();
        str = "广告资源获取成功 数量:"+size ;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);

        return adInfos;
    }

    public List<String> getPlaylistInfo(){
        String str = "查询门禁的播放列表";
        String listId = null, bt = null, et = null;
        List<String> playlistInfo = new ArrayList<>();

        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (!isTokenValid()){
            getToken(null);
        }

        HashMap<String,String> params = new HashMap<>();
//        idMac = "92330115148546048";
        String result = null;//requestGet(params, "v1/ad/playlist/all/" + idMac);
        if (result == null){
            return null;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            int total = jsonObject.getInt("total");
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for (int i=0;i<total;i++){
                JSONObject jo = jsonArray.getJSONObject(i);
                listId = jo.getString("id");
                bt = jo.getString("beginTime");
                et = jo.getString("endTime");
            }
            playlistInfo.add(listId);
            playlistInfo.add(bt);
            playlistInfo.add(et);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        str = "门禁的播放列表:"+listId + " start:"+bt + " end:"+et;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        return playlistInfo;
    }

    public ArrayList<ADInfo> getADSrcPlaylist(String playId){
        String str = "查询播放列表的广告资源";
        ArrayList<ADInfo> adInfos = new ArrayList<>();

        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (!isTokenValid()){
            getToken(null);
        }

        HashMap<String,String> params = new HashMap<>();
        String result = null;//requestGet(params, "v1/ad/playlist/" + playId);
        if (result == null){
            return adInfos;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            int total = jsonObject.getInt("total");
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            ADInfo adInfo = new ADInfo();
            for (int i=0;i<total;i++){
                JSONObject jo = jsonArray.getJSONObject(i);
                adInfo.setId(jo.getString("id"));
                adInfo.setFilename(jo.getString("filename"));
                adInfo.setUrl(jo.getString("url"));
                adInfos.add(adInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int size = adInfos.size();
        str = "广告资源:"+size + " 1 filename:"+adInfos.get(0).getFilename() + " 1 url:"+adInfos.get(0).getUrl();
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (jsonObject!=null && size>=1){
            return adInfos;
        }else {
            return null;
        }
    }
    /*

     */
    public ArrayList<UnregNirPersonInfo> getUnregisteredNirList(){
        if (!isTokenValid()){
            getToken(null);
        }

        HashMap<String,String> params = new HashMap<>();
        String result = null;//requestGet(params, "v1/lock/" + idMac + "/unregistered/nir");
        JSONObject jsonObject = null;
        ArrayList<UnregNirPersonInfo> unregNirPersonInfos = new ArrayList<>();
        try {
            jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for (int i=0;i<jsonArray.length();i++){
                JSONObject j = jsonArray.getJSONObject(i);
                UnregNirPersonInfo unpi = new UnregNirPersonInfo();
                unpi.setId(j.getString("id"));
                unpi.setIdNo(j.getString("idNo"));
                unpi.setName(j.getString("name"));
                unregNirPersonInfos.add(unpi);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return unregNirPersonInfos;
    }

    public boolean bindFeatureFile(String personId, String fPath){
        String str = "人员绑定人脸特征值";
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (!isTokenValid()){
            getToken(null);
        }

        HashMap<String,String> params = new HashMap<>();
        String result = null;//upLoadFile(fPath, params, "v1/person/" + personId + "/face/feature/" + 10121 + "/file");
        str = "人员绑定人脸特征值返回:" + result;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            int code = jsonObject.getInt("code");
            if (code == 0){
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getBatchFeatureFile(List<ManageablePersonInfo> pers){
        int size = pers.size();
        String str = "批量获取人员近红外特征值:"+size;
        String fp = MainActivity.dirFeatData + "TempFeat.feat";
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (!isTokenValid()){
            getToken(null);
        }

        List<String> params = new ArrayList<>();
        for (int i=0;i<size;i++){
            params.add(pers.get(i).getPid());
        }

        HashMap<String,String> paramsHeader = new HashMap<>();
        paramsHeader.put("token", tokenBinjn);
        String result = OkHttpUtil.postDownloadFilesWithList(urlBinjn+"v1/person/face/feature/nir/filelist",
                null,paramsHeader, fp, params, "personIds");

        str = str + " 返回：" + result;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (result != null){
            return fp;
        }
        return null;
    }

    /**
     * 获取设备配置信息
     * @return
     */
    public String getConfigMachine(){
        String str = "获取设备配置信息";
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (!isTokenValid()){
            getToken(null);
        }

        HashMap<String,String> paramsHeader = new HashMap<>();
        paramsHeader.put("token", tokenBinjn);
        String result = OkHttpUtil.getDatasync(urlBinjn+"v1/lock/"+idMac+"/config", null,
                paramsHeader);
        str = str + " 返回：" + result;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (result == null){
            return null;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            int code = jsonObject.getInt("code");
            if (code == 0){
                String data = jsonObject.getString("data");
                return data;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取设备的识别通过阈值
     * @return
     */
    public float getMatchScorePass(){
        String str = "获取设备的识别通过阈值";
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (!isTokenValid()){
            getToken(null);
        }

        HashMap<String,String> paramsHeader = new HashMap<>();
        paramsHeader.put("token", tokenBinjn);

        String result = OkHttpUtil.getDatasync(urlBinjn+"v1/lock/"+idMac+"/matchscorepass", null,
                paramsHeader);

        str = str + " 返回：" + result;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (result == null){
            return 0;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            int code = jsonObject.getInt("code");
            if (code == 0){
                double data = jsonObject.getDouble("data");
                return (float) data;
            }else if (code == 401){
                if (jsonObject.getString("msg").equals("token失效，请重新登录")){
                    getToken(null);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean getPersonFaceNir(String personId, String fPath){
        String str = "获取人员近红外特征值:"+personId;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (!isTokenValid()){
            getToken(null);
        }
        return false;//downloadFile("v1/person/"+personId+"/face/feature/nir/file", fPath);
    }

    public void saveRecordIDCard(HashMap<String,String> params){
        String str = "保存身份证读取记录";
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (!isTokenValid()){
            getToken(null);
        }
        String result = null;//requestPost(params, "v1/upload/lock/"+idMac + "/guest");
        str = "保存身份证读取记录返回:" + result;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
    }

    /**
     * 保存身份证读取记录(照片以文件形式上传)
     * @param params
     * @param fPath
     */
    public void saveRecordIDCardFile(HashMap<String,String> params, String fPath){
        String str = "保存身份证读取记录(照片以文件形式上传):"+fPath;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (!isTokenValid()){
            getToken(null);
        }
        HashMap<String,String> paramsHeader = new HashMap<>();
        paramsHeader.put("token", tokenBinjn);
        String result = OkHttpUtil.postUploadSingleFile(urlBinjn+"v1/upload/idcard/file/"+idMac, params,
                paramsHeader, fPath, "file", FILE_TYPE_IMAGE);
        str = str + " 返回:" + result;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
    }

    /**
     * 保存门禁机人脸识别记录(照片以文件形式上传)
     * @param spList
     * @param fp1
     * @param fp2
     * @param msp
     */
    public void saveFaceRecord(List<SimilarPerson> spList, String fp1, String fp2, float msp){
        String str = "保存门禁机人脸识别记录(照片以文件形式上传):"+fp1 + " nir:"+fp2;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (!isTokenValid()){
            getToken(null);
        }
        String result;
        if (spList.get(0).getScore() > msp){
            result = String.valueOf(PASSRECRESULT);
        }else {
            result = String.valueOf(DENYRECRESULT);
        }
        HashMap<String, String> params = new HashMap<>();
        params.put("result", result);
        params.put("similarPersonId1", new String(spList.get(0).getPersonId()));
        params.put("similarFaceId1", spList.get(0).getFaceId());
        params.put("score1", String.valueOf(spList.get(0).getScore()));
        params.put("similarPersonId2", spList.get(1).getPersonId());
        params.put("similarFaceId2", spList.get(1).getFaceId());
        params.put("score2", String.valueOf(spList.get(1).getScore()));
        params.put("similarPersonId3", spList.get(2).getPersonId());
        params.put("similarFaceId3", spList.get(2).getFaceId());
        params.put("score3", String.valueOf(spList.get(2).getScore()));
        params.put("captureTime", UtilCommon.getTimeCurrent());
        params.put("liveImg1", "");
//        Log.d(mTAG, "params:"+params.toString());
        HashMap<String, String> paramsHeader = new HashMap<>();
        paramsHeader.put("token", tokenBinjn);
        String re;
        if (Integer.parseInt(result)==PASSRECRESULT && fp2!=null){
            List<String> fp1s = new ArrayList<>();
            fp1s.add(fp1);
            List<String> fp2s = new ArrayList<>();
            fp2s.add(fp2);
            re = OkHttpUtil.postUploadFiles(urlBinjn + "v1/upload/lock/" + idMac + "/face/file",
                    params, paramsHeader, fp1s, "liveImgfiles", FILE_TYPE_IMAGE, fp2s,
                    "nirImgfile", FILE_TYPE_IMAGE);
        }else {
            re = OkHttpUtil.postUploadSingleFile(urlBinjn+"v1/upload/lock/"+idMac+"/face/file",
                    params, paramsHeader, fp1, "liveImgfiles", FILE_TYPE_IMAGE);
        }
        str = str + " 返回:" + re;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
    }

    /**
     * 人脸照片特征值合并上传（多文件传输）
     * @param prfi
     * @return
     */
    public boolean sendFace_FeatureFiles(PersonRegFilesInfo prfi){
        String str = "人脸照片特征值合并上传（多文件传输）:"+prfi.getPid();
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("token", tokenBinjn);
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("type", String.valueOf(prfi.getPicType()));
        paramsMap.put("lockId", idMac);

        String result = OkHttpUtil.postUploadFiles(urlBinjn+"v1/person/"+prfi.getPid()+"/face/nir/imageandfile",
                paramsMap, headerMap, prfi.getFeatFilePaths(), prfi.getFeatFileName(), FILE_TYPE_FILE,
                prfi.getFaceFilePaths(), prfi.getFaceFileName(), FILE_TYPE_IMAGE);
        str = str + " 返回："+result;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (result == null){
            return false;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            int code = jsonObject.getInt("code");
            if (code == 0){
                return true;
            }else {
                str = "错误："+str;
                Log.e(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 保存访客呼叫业主记录(照片以文件形式上传)
     * @param cNum
     * @param fPath
     * @return
     */
    public String saveGuestCallOwnerRecord(String cNum, String fPath){
        String str = "保存访客呼叫业主记录(照片以文件形式上传)-呼叫号："+cNum + " imgPath:"+fPath;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("token", tokenBinjn);
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("personId", "");
        paramsMap.put("callTime", UtilCommon.getTimeCurrent());
        paramsMap.put("callLength", "0");
        paramsMap.put("callNum", cNum);
        paramsMap.put("liveImg1", "");

        String result = OkHttpUtil.postUploadSingleFile(urlBinjn+"v1/upload/lock/"+idMac+"/call/file",
                paramsMap, headerMap, fPath, "liveImgfiles", FILE_TYPE_IMAGE);
        str = "保存访客呼叫业主记录(照片以文件形式上传)" + " 返回："+result;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (result == null){
            return null;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            int code = jsonObject.getInt("code");
            if (code == 0){
                return jsonObject.getString("id");
            }else {
                str = "错误："+str;
                Log.e(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(mTAG, "saveGuestCallOwnerRecord:"+e.toString());
            LogFile.getInstance().saveMessage("saveGuestCallOwnerRecord error:"+e.toString());
        }

        return null;
    }

    public String dialMobile(String cNum, String callRecId){
        String str = "门禁设备拨号呼叫移动端-呼叫号："+cNum;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("token", tokenBinjn);
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("callrecId", callRecId);
        String result = OkHttpUtil.postDataSync(urlBinjn+"v1/video/"+idMac+"/call/"+cNum,
                paramsMap, headerMap);
        str = "门禁设备拨号呼叫移动端" + " 返回："+result;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (result == null){
            return null;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            int code = jsonObject.getInt("code");
            if (code == 0){
                return jsonObject.getString("channelName");
            }else {
                str = "错误："+str;
                Log.e(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(mTAG, "dialMobile:"+e.toString());
            LogFile.getInstance().saveMessage("dialMobile error:"+e.toString());
        }

        return null;
    }

    /**
     * 保存远程一键开门记录(照片以文件形式上传)
     * @param pid
     * @param fPath
     * @return
     */
    public String saveRecordRemoteOpenDoorWithPhoto(String pid, String fPath){
        String str = "保存远程一键开门记录(照片以文件形式上传) - pid:"+pid + " path:"+fPath;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("token", tokenBinjn);
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("personId", pid);
        paramsMap.put("operateTime", UtilCommon.getTimeCurrent());
        paramsMap.put("liveImg1", "");

        String result = OkHttpUtil.postUploadSingleFile(urlBinjn+"v1/upload/lock/"+idMac+"/remote/open/file",
                paramsMap, headerMap, fPath, "liveImgfiles", FILE_TYPE_IMAGE);
        str = "保存远程一键开门记录(照片以文件形式上传)" + " 返回："+result;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (result == null){
            return null;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            int code = jsonObject.getInt("code");
            if (code == 0){
                return jsonObject.getString("id");
            }else {
                str = "错误："+str;
                Log.e(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(mTAG, "saveRecordRemoteOpenDoorWithPhoto:"+e.toString());
            LogFile.getInstance().saveMessage("saveRecordRemoteOpenDoorWithPhoto error:"+e.toString());
        }

        return null;
    }

    /**
     * 保存临时密码开门记录(照片以文件形式上传)
     * @param pid
     * @param score
     * @param fPath
     * @return
     */
    public String saveRecordOpenDoorPasswordWithPhoto(String pid, float score, String fPath, float msp){
        String str = "保存临时密码开门记录(照片以文件形式上传) - pid:"+pid + " score="+score;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("token", tokenBinjn);
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("personTempPswId", pid);
        paramsMap.put("result", String.valueOf((score>msp ? PASSRECRESULT:DENYRECRESULT)));
        paramsMap.put("score", String.valueOf(score));
        paramsMap.put("liveImg1", "");

        String result = OkHttpUtil.postUploadSingleFile(urlBinjn+"v1/temppsw/"+idMac+"/open/file",
                paramsMap, headerMap, fPath, "liveImgfiles", FILE_TYPE_IMAGE);
        str = "保存临时密码开门记录(照片以文件形式上传)" + " 返回："+result;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (result == null){
            return null;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            int code = jsonObject.getInt("code");
            if (code == 0){
                return jsonObject.getString("id");
            }else {
                str = "错误："+str;
                Log.e(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(mTAG, "saveRecordOpenDoorPasswordWithPhoto:"+e.toString());
            LogFile.getInstance().saveMessage("saveRecordOpenDoorPasswordWithPhoto error:"+e.toString());
        }

        return null;
    }

    public List<String> getSystemId(int num){
        List<String> listId = new ArrayList<>();

        String str = "使用系统ID生成器生成ID";
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (!isTokenValid()){
            getToken(null);
        }

        HashMap<String,String> params = new HashMap<>();
        params.put("count", String.valueOf(num));
        String result = null;//requestGet(params, "v1/common/id/new");
        if (result == null){
            return null;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            int code = jsonObject.getInt("code");
            if (code == 0){
                int total = jsonObject.getInt("total");
                JSONArray jsonArray = jsonObject.getJSONArray("id");
                for (int i=0;i<total;i++){
                    listId.add(jsonArray.getString(i));
                }
            }else {
                str = str + " 返回错误："+result;
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int size = listId.size();
        str = str + " 返回id数量："+size;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);

        return listId;
    }

    /**
     * 设备心跳接口
     * @param flagToken
     * @return
     */
    public String heartbeatMachine(byte[] flagToken){
        String str = "设备心跳接口:"+idMac + " flagToken:"+flagToken[0];
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        //token无效或门禁机ID为空则则重新获取
//        if (!isTokenValid()){
//            if(getToken(null) == 0){
//                flagToken[0] = 1;
//            }
//        }
        //只有门禁机ID为空则则重新获取
        if (idMac.equals("")){
            str = registerMachine(MainActivity.addrMac);
            if (str != null){
                flagToken[0] = 1;
            }
        }
        Log.d(mTAG, "flagToken:"+flagToken[0]);
        String result = null;
        if (!idMac.equals("")) {
            result = OkHttpUtil.getDatasync(urlBinjn+"security/heartbeat/"+idMac, null, null);
        }
        str = "设备心跳接口 返回："+result;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (result == null){
            return null;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            int code = jsonObject.getInt("code");
            if (code == 0){
                return jsonObject.getString("time");
            }else {
                str = "错误："+str;
                Log.e(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(mTAG, "heartbeatMachine:"+e.toString());
            LogFile.getInstance().saveMessage("heartbeatMachine error:"+e.toString());
        }

        return null;
    }

    /**
     * 保存设备日志上传
     * @param fPath
     */
    public void saveLogfile(String fPath){
        String newFilePath = MainActivity.mainDir+idMac+".log";
        UtilCommon.copyFile(fPath, newFilePath);
        String str = "保存设备日志上传:"+newFilePath;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (!isTokenValid()){
            getToken(null);
        }

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("token", tokenBinjn);
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("remark", "");
        String result = OkHttpUtil.postUploadSingleFile(urlBinjn+"v1/lock/"+idMac+"/log",
                paramsMap, headerMap, newFilePath, "file", FILE_TYPE_FILE);
        str = "保存设备日志上传 返回："+result;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (result == null){
            return ;
        }
    }

    public void uploadDeviceVersionInfo(String av, String rv, String di){
        String str = "设备版本号信息上传:" + av +","+rv + ","+di;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (!isTokenValid()){
            getToken(null);
        }

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("token", tokenBinjn);
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("appversion", av);
        paramsMap.put("romversion", rv);
        paramsMap.put("deviceinfo", di);
        String result = OkHttpUtil.postDataSync(urlBinjn+"v1/lock/"+idMac+"/version",
                paramsMap, headerMap);

        str = "设备版本号信息上传 返回："+result;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
    }
}
