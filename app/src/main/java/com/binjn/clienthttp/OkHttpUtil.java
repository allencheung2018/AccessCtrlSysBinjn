package com.binjn.clienthttp;

import android.arch.lifecycle.ReportFragment;
import android.util.Log;

import com.utilcommon.LogFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by xiaomi2 on 2018/4/9 0009.
 */

public class OkHttpUtil {
    private static final String mTAG = OkHttpUtil.class.getSimpleName();

    /**
     * 同步GET方法
     * @param url
     * @param paramsMap
     * @param headerMap
     * @return
     */
    public static String getDatasync(String url, Map<String, String> paramsMap, Map<String, String> headerMap){
        String sRet = null;
        try {
            OkHttpClient client = new OkHttpClient();
            Request.Builder request = new Request.Builder();
            /** */
            if(paramsMap != null){
                url = url+"?";
                for (String key: paramsMap.keySet()){
                    url = url + key+"="+paramsMap.get(key)+"&";
                }
                url = url.substring(0,url.length()-1);
            }
            if(headerMap != null){
                for (String key: headerMap.keySet()){
                    request.addHeader(key,headerMap.get(key));
                }
            }
            Log.i(mTAG, "get url:"+url);
            request.url(url);
            Request mOkHttpRequest = request.build();
            Response response = null;//得到Response 对象
            response = client.newCall(mOkHttpRequest).execute();
            if (response.isSuccessful()) {
                sRet = response.body().string();
                Log.i(mTAG, "get re:"+sRet);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(mTAG, "getDatasync:"+e.toString());
            LogFile.getInstance().saveMessage("getDatasync error:"+e.toString());
        }
        return sRet;
    }

    /**
     * 同步POST
     * @param url
     * @param paramsMap
     * @param headerMap
     * @return
     */
    public static String postDataSync(String url, Map<String, String> paramsMap, Map<String, String> headerMap){
        String sRet = null;

        try {
            OkHttpClient client = new OkHttpClient();
            Request.Builder request = new Request.Builder();
            FormBody.Builder formBody = new FormBody.Builder();
            if(paramsMap != null) {
                for (String key : paramsMap.keySet()) {
                    formBody.add(key, paramsMap.get(key));
                }
            }
            if(headerMap != null){
                for (String key: headerMap.keySet()){
                    request.addHeader(key,headerMap.get(key));
                }
            }
            Log.i(mTAG, "post url:"+url);
            request.url(url);
            request.post(formBody.build());
            Request mOkHttpRequest = request.build();
            Response response = null;
            response = client.newCall(mOkHttpRequest).execute();
            if (response.isSuccessful()) {
                sRet = response.body().string();
                Log.i(mTAG, "post re:"+sRet);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }

        return sRet;
    }

    /**
     * POST下载多个参数相同键值的文件
     * @param url
     * @param paramsMap
     * @param headerMap
     * @param fPath
     * @param params
     * @param nameParams
     * @return
     */
    public static String postDownloadFilesWithList(
            String url, Map<String, String> paramsMap, Map<String, String> headerMap, String fPath,
            List<String> params, String nameParams){
        String sRet = null;

        try {
            OkHttpClient client = new OkHttpClient();
            Request.Builder request = new Request.Builder();
            FormBody.Builder formBody = new FormBody.Builder();
            if(paramsMap != null) {
                for (String key : paramsMap.keySet()) {
                    formBody.add(key, paramsMap.get(key));
                }
            }
            if(headerMap != null){
                for (String key: headerMap.keySet()){
                    request.addHeader(key,headerMap.get(key));
                }
            }
            if (params != null){
                for (String s : params){
                    formBody.add(nameParams, s);
                }
            }
            Log.i(mTAG, "post url:"+url);
            request.url(url);
            request.post(formBody.build());
            Request mOkHttpRequest = request.build();
            Response response = null;
            response = client.newCall(mOkHttpRequest).execute();
            if (response.isSuccessful()) {
                sRet = "文件下载成功";
                Log.i(mTAG, "post re:"+sRet);
                if (fPath != null){
                    InputStream is = response.body().byteStream();//从服务器得到输入流对象
                    File file = new File(fPath);//根据目录和文件名得到file对象
                    if (!file.exists()){
                        file.createNewFile();
                    }
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buf = new byte[1024*8];
                    int len = 0;
                    while ((len = is.read(buf)) != -1){
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                    fos.close();
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }

        return sRet;
    }

    public static boolean getDownloadFile(String url,String fPath){
        String sRet;
        try {
            OkHttpClient client = new OkHttpClient();
            Request.Builder request = new Request.Builder();
            Log.i(mTAG, "get url:"+url);
            request.url(url);
            Request mOkHttpRequest = request.build();
            Response response = null;//得到Response 对象
            response = client.newCall(mOkHttpRequest).execute();
            if (response.isSuccessful()) {
                sRet = "文件下载成功";
                Log.i(mTAG, "post re:"+sRet);
                if (fPath != null){
                    InputStream is = response.body().byteStream();//从服务器得到输入流对象
                    File file = new File(fPath);//根据目录和文件名得到file对象
                    if (!file.exists()){
                        file.createNewFile();
                    }
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buf = new byte[1024*8];
                    int len = 0;
                    while ((len = is.read(buf)) != -1){
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                    fos.close();
                }
                return true;
            }else {
                sRet = "文件下载失败:"+response;
                Log.e(mTAG, "post re:"+sRet);
                LogFile.getInstance().saveMessage(sRet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 上传单个文件
     * @param url
     * @param paramsMap
     * @param headerMap
     * @param fPath
     * @param fName
     * @param fType
     * @return
     */
    public static String postUploadSingleFile(String url, Map<String, String> paramsMap, Map<String,
            String> headerMap, String fPath, String fName, String fType){
        String sRet = null;
        try {
            OkHttpClient client = new OkHttpClient();
            Request.Builder request = new Request.Builder();
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            if (paramsMap != null) {
                for (String key : paramsMap.keySet()) {
                    builder.addFormDataPart(key, paramsMap.get(key));
                }
            }
            if (headerMap != null) {
                for (String key : headerMap.keySet()) {
                    request.addHeader(key, headerMap.get(key));
                }
            }
            File file = new File(fPath);
            builder.addFormDataPart(fName,file.getName(), RequestBody.create(MediaType.parse(fType), file));

            Log.i(mTAG, "post url:"+url);
            request.url(url);
            request.post(builder.build());
            Request mOkHttpRequest = request.build();
            Response response = null;
            client.newBuilder().writeTimeout(20, TimeUnit.SECONDS);
            response = client.newCall(mOkHttpRequest).execute();
            if (response.isSuccessful()) {
                sRet = response.body().string();
                Log.i(mTAG, "post re:"+sRet);
            }
        }catch (IOException e) {
            e.printStackTrace();
            LogFile.getInstance().saveMessage("postUploadSingleFile error:"+e.toString());
        }

        return sRet;
    }

    /**
     * 多文件传输
     * @param url
     * @param paramsMap
     * @param headerMap
     * @param fp1
     * @param fn1
     * @param ftype1
     * @param fp2
     * @param fn2
     * @param ftype2
     * @return
     */
    public static String postUploadFiles(
            String url, Map<String, String> paramsMap, Map<String, String> headerMap, List<String> fp1,
            String fn1, String ftype1, List<String> fp2, String fn2, String ftype2){
        String sRet = null;
        try {
            OkHttpClient client = new OkHttpClient();
            Request.Builder request = new Request.Builder();
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            if(paramsMap != null) {
                for (String key : paramsMap.keySet()) {
                    builder.addFormDataPart(key, paramsMap.get(key));
                }
            }
            if(headerMap != null){
                for (String key: headerMap.keySet()){
                    request.addHeader(key,headerMap.get(key));
                }
            }
            for (int i=0;i<fp1.size();i++){
                File file1 = new File(fp1.get(i));
                builder.addFormDataPart(fn1,file1.getName(), RequestBody.create(MediaType.parse(ftype1), file1));
                File file2 = new File(fp2.get(i));
                builder.addFormDataPart(fn2,file2.getName(), RequestBody.create(MediaType.parse(ftype2), file2));
            }
            Log.i(mTAG, "post url:"+url);
            request.url(url);
            request.post(builder.build());
            Request mOkHttpRequest = request.build();
            Response response = null;
            response = client.newCall(mOkHttpRequest).execute();
            if (response.isSuccessful()) {
                sRet = response.body().string();
                Log.i(mTAG, "post re:"+sRet);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogFile.getInstance().saveMessage("postUploadFiles error:"+e.toString());
        }
        return sRet;
    }
}
