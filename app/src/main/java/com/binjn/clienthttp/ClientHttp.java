package com.binjn.clienthttp;


import android.util.Log;

import com.utilcommon.LogFile;
import com.utilcommon.UtilCommon;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Created by xiaomi2 on 2017/12/7 0007.
 */

public class ClientHttp {
    //static
    private static final String mTAG = "ClientHttp";
    public static final String keyBinjn = "binjn";
    public static final String urlBinjn = "https://icbc.binjn.com:59002/phs/";
//    public static final String apikeyBinjn = "1cb59aa2848c402d9c3d86b0b8341f57";
//    public static final String secretkeyBinjn = "d6495004a017a1f5f6dff7c92aee260ef8f91a30e51f1649f385afc3ebc04bd8";
    public static String tokenBinjn = "";
    public static String idMac = "";
    public static String timeTolen = "";
    public final int expireMax = 43000;
    public static final int TIME_OUT = 10*1000;             //超时时间ms
    //var
    private static String strLog = "";

    public String requestGet(HashMap<String, String> paramsMap, String urlAdded) {
        String result = null;
        try {
            String baseUrl = urlBinjn + urlAdded + "?";
            StringBuilder tempParams = new StringBuilder();
            int pos = 0;
            for (String key : paramsMap.keySet()) {
                if (pos > 0) {
                    tempParams.append("&");
                }
                tempParams.append(String.format("%s=%s", key, URLEncoder.encode(paramsMap.get(key),"utf-8")));
                pos++;
            }
            String requestUrl = baseUrl + tempParams.toString();
            strLog = "Get baseUrl: "+baseUrl + " Get params: "+tempParams;
            Log.i(mTAG, strLog);
            LogFile.writeFileSdcardFile(strLog, true);
            // 新建一个URL对象
            URL url = new URL(requestUrl);
            // 打开一个HttpURLConnection连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            // 设置连接主机超时时间
            urlConn.setConnectTimeout(5 * 1000);
            //设置从主机读取数据超时
            urlConn.setReadTimeout(5 * 1000);
            // 设置是否使用缓存  默认是true
            urlConn.setUseCaches(true);
            // 设置为Post请求
            urlConn.setRequestMethod("GET");
            //urlConn设置请求头信息
            //设置请求中的媒体类型信息。
            urlConn.setRequestProperty("Content-Type", "application/json");
            //设置客户端与服务连接类型
            urlConn.addRequestProperty("Connection", "Keep-Alive");
            //token
            urlConn.setRequestProperty("token", tokenBinjn);
            // 开始连接
            urlConn.connect();
            // 判断请求是否成功
            if (urlConn.getResponseCode() == 200) {
                // 获取返回的数据
                result = streamToString(urlConn.getInputStream());
                Log.i(mTAG, "Get方式请求成功，result--->" + result);
            } else {
                Log.e(mTAG, "Get方式请求失败");
            }
            // 关闭连接
            urlConn.disconnect();
        } catch (Exception e) {
            String str = "requestGet exception:" + e.toString();
            Log.e(mTAG, str);
            LogFile.writeFileSdcardFile(str, true);
        }
        return result;
    }

    public String requestPost(HashMap<String, String> paramsMap, String urlAdded) {
        String result = null;
        try {
            String baseUrl = urlBinjn + urlAdded + "/";
            //合成参数
            StringBuilder tempParams = new StringBuilder();
            JSONObject jsonObject = new JSONObject();
            int pos = 0;
            for (String key : paramsMap.keySet()) {
                if (pos > 0) {
                    tempParams.append("&");
                }
//                tempParams.append(String.format("%s=%s", key,  URLEncoder.encode(paramsMap.get(key),"utf-8")));
                jsonObject.put(key, paramsMap.get(key));
                pos++;
            }
            String params = jsonObject.toString();//tempParams.toString();
            strLog = "Post baseUrl: "+baseUrl;// + " Post params: "+params;
            Log.i(mTAG, strLog);
            LogFile.writeFileSdcardFile(strLog, true);
            // 请求的参数转换为byte数组
            byte[] postData = params.getBytes();
            // 新建一个URL对象
            URL url = new URL(baseUrl);
            // 打开一个HttpURLConnection连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            // 设置连接超时时间
            urlConn.setConnectTimeout(5 * 1000);
            //设置从主机读取数据超时
            urlConn.setReadTimeout(5 * 1000);
            // Post请求必须设置允许输出 默认false
            urlConn.setDoOutput(true);
            //设置请求允许输入 默认是true
            urlConn.setDoInput(true);
            // Post请求不能使用缓存
            urlConn.setUseCaches(false);
            // 设置为Post请求
            urlConn.setRequestMethod("POST");
            //设置本次连接是否自动处理重定向
            urlConn.setInstanceFollowRedirects(true);
            // 配置请求Content-Type
            urlConn.setRequestProperty("Content-Type", "application/json");
            //token
            urlConn.setRequestProperty("token", tokenBinjn);
            // 开始连接
            urlConn.connect();
            // 发送请求参数
            DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
            dos.write(postData);
            dos.flush();
            dos.close();
            // 判断请求是否成功
            if (urlConn.getResponseCode() == 200) {
                // 获取返回的数据
                result = streamToString(urlConn.getInputStream());
                Log.i(mTAG, "Post方式请求成功，result--->" + result);
            } else {
                Log.e(mTAG, "Post方式请求失败");
            }
            // 关闭连接
            urlConn.disconnect();
        } catch (Exception e) {
            String str = "requestPost exception:" + e.toString();
            Log.e(mTAG, str);
            LogFile.writeFileSdcardFile(str, true);
        }
        return result;
    }

    public void requestPostFromWeb(HashMap<String, String> paramsMap, String urlAdded) {
        try {
            String baseUrl = urlBinjn + urlAdded + "/";
            //合成参数
            StringBuilder tempParams = new StringBuilder();
            int pos = 0;
            for (String key : paramsMap.keySet()) {
                if (pos > 0) {
                    tempParams.append("&");
                }
                tempParams.append(String.format("%s=%s", key,  URLEncoder.encode(paramsMap.get(key),"utf-8")));
                pos++;
            }
            String params =tempParams.toString();
            // 请求的参数转换为byte数组
            byte[] postData = params.getBytes();
            // 新建一个URL对象
            URL url = new URL(baseUrl);
            // 打开一个HttpURLConnection连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            // 设置连接超时时间
            urlConn.setConnectTimeout(5 * 1000);
            //设置从主机读取数据超时
            urlConn.setReadTimeout(5 * 1000);
            // Post请求必须设置允许输出 默认false
            urlConn.setDoOutput(true);
            //设置请求允许输入 默认是true
            urlConn.setDoInput(true);
            // Post请求不能使用缓存
            urlConn.setUseCaches(false);
            // 设置为Post请求
            urlConn.setRequestMethod("POST");
            //设置本次连接是否自动处理重定向
            urlConn.setInstanceFollowRedirects(true);
            // 配置请求Content-Type
            urlConn.setRequestProperty("Content-Type", "application/json");
            //token
            urlConn.setRequestProperty("token", tokenBinjn);
            // 开始连接
            urlConn.connect();
            // 发送请求参数
            DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
            dos.write(postData);
            dos.flush();
            dos.close();
            // 判断请求是否成功
            if (urlConn.getResponseCode() == 200) {
                // 获取返回的数据
                String result = streamToString(urlConn.getInputStream());
                Log.e(mTAG, "Post方式请求成功，result--->" + result);
            } else {
                Log.e(mTAG, "Post方式请求失败");
            }
            // 关闭连接
            urlConn.disconnect();
        } catch (Exception e) {
            Log.e(mTAG, e.toString());
        }
    }

    public boolean requestPostDownloadFileWithParams(List<String> paramsMap, String urlAdded, String fPath){
        try {
            String baseUrl = urlBinjn + urlAdded + "/";
            URL url = new URL(baseUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("token", tokenBinjn);
            connection.connect();

            //合成参数
            StringBuilder tempParams = new StringBuilder();
            String key = "personIds";
            int pos = 0;
            for (int i=0;i<paramsMap.size();i++) {
                if (pos > 0) {
                    tempParams.append("&");
                }
                tempParams.append(String.format("%s=%s", key,  URLEncoder.encode(paramsMap.get(i),"utf-8")));
                pos++;
            }
            String params = tempParams.toString();
            Log.d(mTAG, "params : "+params);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            writer.write(params);
            writer.close();

            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                InputStream inputStream = connection.getInputStream();
                File descFile = new File(fPath);
                if (!descFile.exists()){
                    descFile.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(descFile);
                byte[] buf = new byte[1024*8];
                int len = -1;
                while ((len = inputStream.read(buf)) != -1){
                    fos.write(buf, 0, len);
                }
                fos.flush();
                Log.d(mTAG, "文件下载成功");
                return true;
            }else {
                Log.e(mTAG, "文件下载失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String upLoadFile(String filePath, HashMap<String, String> paramsMap, String urlAdded) {
        String result = null;
        try {
            String baseUrl = urlBinjn + urlAdded + "/";
            File file = new File(filePath);

            //新建url对象
            URL url = new URL(baseUrl);
            //通过HttpURLConnection对象,向网络地址发送请求
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            //设置该连接允许读取
            urlConn.setDoOutput(true);
            //设置该连接允许写入
            urlConn.setDoInput(true);
            //设置不能适用缓存
            urlConn.setUseCaches(false);
            //设置连接超时时间
            urlConn.setConnectTimeout(5 * 1000);   //设置连接超时时间
            //设置读取超时时间
            urlConn.setReadTimeout(10 * 1000);   //读取超时
            //设置连接方法post
            urlConn.setRequestMethod("POST");
            //设置维持长连接
            urlConn.setRequestProperty("connection", "Keep-Alive");
            //设置文件字符集
            urlConn.setRequestProperty("Accept-Charset", "UTF-8");
            //设置文件类型
            urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + "*****");
            //token
            urlConn.setRequestProperty("token", tokenBinjn);
            String name = file.getName();
            DataOutputStream requestStream = new DataOutputStream(urlConn.getOutputStream());
            requestStream.writeBytes("--" + "*****" + "\r\n");
            //发送文件参数信息
            StringBuilder tempParams = new StringBuilder();
            tempParams.append("Content-Disposition: form-data; name=\"" + "liveImgfiles" + "\"; filename=\"" + name + "\"; ");
            int pos = 0;
            int size=paramsMap.size();
            for (String key : paramsMap.keySet()) {
                tempParams.append( String.format("%s=\"%s\"", key, paramsMap.get(key), "utf-8"));
                if (pos < size-1) {
                    tempParams.append("; ");
                }
                pos++;
            }
            tempParams.append("\r\n");
            tempParams.append("Content-Type: application/octet-stream\r\n");
            tempParams.append("\r\n");
            String params = tempParams.toString();
            String str = "upLoadFile baseUrl: "+baseUrl + " Get params: "+params;
            Log.i(mTAG, str);
            LogFile.writeFileSdcardFile(str, true);
            requestStream.writeBytes(params);
            //发送文件数据
            FileInputStream fileInput = new FileInputStream(file);
            int bytesRead;
            byte[] buffer = new byte[1024];
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            while ((bytesRead = in.read(buffer)) != -1) {
                requestStream.write(buffer, 0, bytesRead);
            }
            requestStream.writeBytes("\r\n");
            requestStream.flush();
            requestStream.writeBytes("--" + "*****" + "--" + "\r\n");
            requestStream.flush();
            fileInput.close();
            int statusCode = urlConn.getResponseCode();
            if (statusCode == 200) {
                // 获取返回的数据
                result = streamToString(urlConn.getInputStream());
                Log.e(mTAG, "上传成功，result--->" + result);
            } else {
                Log.e(mTAG, "上传失败");
            }
        } catch (IOException e) {
            String str = "upLoadFile exception:" + e.toString();
            Log.e(mTAG, str);
            LogFile.writeFileSdcardFile(str, true);
        }
        return result;
    }

    public boolean downloadFile(String urlAdded, String fPath){
        boolean flag = false;
        try {
            String baseUrl = urlBinjn + urlAdded + "/";
            // 新建一个URL对象
            URL url = new URL(baseUrl);
            strLog = "downloadFile baseUrl: "+baseUrl.trim();
            Log.i(mTAG, strLog);
            // 打开一个HttpURLConnection连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            // 设置连接主机超时时间
            urlConn.setConnectTimeout(5 * 1000);
            //设置从主机读取数据超时
            urlConn.setReadTimeout(5 * 1000);
            // 设置是否使用缓存  默认是true
            urlConn.setUseCaches(true);
            // 设置为Post请求
            urlConn.setRequestMethod("GET");
            //urlConn设置请求头信息
            //设置请求中的媒体类型信息。
            urlConn.setRequestProperty("Content-Type", "application/json");
            //设置客户端与服务连接类型
            urlConn.addRequestProperty("Connection", "Keep-Alive");
            //token
            urlConn.setRequestProperty("token", tokenBinjn);
            // 开始连接
            urlConn.connect();
            // 判断请求是否成功
            if (urlConn.getResponseCode() == 200) {
                String filePath=fPath;
                File descFile = new File(filePath);
                if (!descFile.exists()){
                    descFile.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(descFile);
                byte[] buffer = new byte[1024];
                int len;
                InputStream inputStream = urlConn.getInputStream();
                while ((len = inputStream.read(buffer)) != -1) {
                    // 写到本地
                    fos.write(buffer, 0, len);
                }
                flag = true;
            } else {
                Log.e(mTAG, "文件下载失败");
            }
            // 关闭连接
            urlConn.disconnect();
        } catch (Exception e) {
            String str = "downloadFile exception:" + e.toString();
            Log.e(mTAG, str);
            LogFile.writeFileSdcardFile(str, true);
        }
        return flag;
    }

    public static boolean downloadUrlFile(String urlAdded, String fPath){
        boolean flag = false;
        try {
            String baseUrl = urlAdded;
            // 新建一个URL对象
            URL url = new URL(baseUrl);
            strLog = "downloadFile baseUrl: "+baseUrl.trim();
            Log.i(mTAG, strLog);
            LogFile.writeFileSdcardFile(strLog, true);
            // 打开一个HttpURLConnection连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
//            // 设置连接主机超时时间
//            urlConn.setConnectTimeout(5 * 1000);
//            //设置从主机读取数据超时
//            urlConn.setReadTimeout(5 * 1000);
//            // 设置是否使用缓存  默认是true
//            urlConn.setUseCaches(true);
//            // 设置为Post请求
//            urlConn.setRequestMethod("GET");
//            //urlConn设置请求头信息
//            //设置请求中的媒体类型信息。
//            urlConn.setRequestProperty("Content-Type", "application/json");
//            //设置客户端与服务连接类型
//            urlConn.addRequestProperty("Connection", "Keep-Alive");
            //token
//            urlConn.setRequestProperty("token", tokenBinjn);
            // 开始连接
            urlConn.connect();
            // 判断请求是否成功
            if (urlConn.getResponseCode() == 200) {
                String filePath=fPath;
                File descFile = new File(filePath);
                if (!descFile.exists()){
                    descFile.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(descFile);
                byte[] buffer = new byte[1024];
                int len;
                InputStream inputStream = urlConn.getInputStream();
                while ((len = inputStream.read(buffer)) != -1) {
                    // 写到本地
                    fos.write(buffer, 0, len);
                }
                flag = true;
                strLog = "文件下载成功";
            } else {
                strLog = "文件下载失败";
                Log.e(mTAG, strLog);
            }
            // 关闭连接
            urlConn.disconnect();
        } catch (Exception e) {
            String str = "downloadFile exception:" + e.toString();
            Log.e(mTAG, str);
            LogFile.writeFileSdcardFile(str, true);
        }
        strLog = strLog + ":"+ flag;
        Log.i(mTAG, strLog);
        LogFile.writeFileSdcardFile(strLog, true);
        return flag;
    }

    public boolean postDownloadFileWithParam(HashMap<String, Long[]> paramsMap, String urlAdded, String fPath){
        boolean flag = false;
        try {
            String baseUrl = urlBinjn + urlAdded + "/";
            //合成参数
            StringBuilder tempParams = new StringBuilder();
            JSONObject jsonObject = new JSONObject();
            int pos = 0;
//            for (String key : paramsMap.keySet()) {
//                if (pos > 0) {
//                    tempParams.append("&");
//                }
////                tempParams.append(String.format("%s=%s", key,  URLEncoder.encode(paramsMap.get(key),"utf-8")));
//                String t = "149225032399192064";
//                jsonObject.put(key, t);
//                pos++;
//            }
            tempParams.append(String.format("%s:%s", "personIds",  URLEncoder.encode("149225032399192064","utf-8")));
            String params = null;
//            Long[] temp = {149225032399192064l, 111129577958408192l};
            Log.d(mTAG, "postDownloadFileWithParam params:"+params);
            // 请求的参数转换为byte数组
            byte[] postData = params.getBytes();
            // 新建一个URL对象
            URL url = new URL(baseUrl);
            strLog = "downloadFile baseUrl: "+baseUrl.trim();
            Log.i(mTAG, strLog);
            LogFile.writeFileSdcardFile(strLog, true);
            // 打开一个HttpURLConnection连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            // 设置连接主机超时时间
            urlConn.setConnectTimeout(5 * 1000);
            //设置从主机读取数据超时
            urlConn.setReadTimeout(15 * 1000);
            // 设置是否使用缓存  默认是true
            urlConn.setUseCaches(true);
            // 设置为Post请求
            urlConn.setRequestMethod("POST");
            //urlConn设置请求头信息
            //设置请求中的媒体类型信息。
            urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary");
            //设置客户端与服务连接类型
            urlConn.addRequestProperty("Connection", "Keep-Alive");
            //token
            urlConn.setRequestProperty("token", tokenBinjn);
            // 开始连接
            urlConn.connect();
            DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
            dos.write(postData);
            dos.flush();
            dos.close();
            // 判断请求是否成功
            if (urlConn.getResponseCode() == 200) {
                String filePath=fPath;
                File descFile = new File(filePath);
                if (!descFile.exists()){
                    descFile.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(descFile);
                byte[] buffer = new byte[1024];
                int len;
                InputStream inputStream = urlConn.getInputStream();
                while ((len = inputStream.read(buffer)) != -1) {
                    // 写到本地
                    fos.write(buffer, 0, len);
                }
                flag = true;
            } else {
                Log.e(mTAG, "文件下载失败");
            }
            // 关闭连接
            urlConn.disconnect();
        } catch (Exception e) {
            String str = "downloadFile exception:" + e.toString();
            Log.e(mTAG, str);
            LogFile.writeFileSdcardFile(str, true);
        }
        return flag;
    }

    /**
     * 将输入流转换成字符串
     *
     * @param is 从网络获取的输入流
     * @return
     */
    public static String streamToString(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.close();
            is.close();
            byte[] byteArray = baos.toByteArray();
            return new String(byteArray);
        } catch (Exception e) {
            Log.e(mTAG, e.toString());
            return null;
        }
    }

    /**
     * MD5
     */
    public static final String md51(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(
                    string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }
    /**
     * 签名生成算法
     * @param -HashMap<String,String> params 请求参数集，所有参数必须已转换为字符串类型
     * @param -String secret 签名密钥
     * @return 签名
     * @throws IOException
     */
    public static String getSignature(HashMap<String,String> params, String secret) throws IOException {
        // 先将参数以其参数名的字典序升序进行排序
        Map<String, String> sortedParams = new TreeMap<String, String>(params);
        Set<Map.Entry<String, String>> entrys = sortedParams.entrySet();
        // 遍历排序后的字典，将所有参数按"key=value"格式拼接在一起
        StringBuilder basestring = new StringBuilder();
        for (Map.Entry<String, String> param : entrys) {
            basestring.append(param.getKey()).append("=").append(param.getValue());
        }
        basestring.append(secret);
        // 使用MD5对待签名串求签
        byte[] bytes = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            bytes = md5.digest(basestring.toString().getBytes("UTF-8"));
        } catch (GeneralSecurityException ex) {
            throw new IOException(ex);
        }
        // 将MD5输出的二进制结果转换为小写的十六进制
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex);
        }
        return sign.toString();
    }

    /**
     * @param urlAdded 请求地址
     * @param map 请求的参数
     * @param filePath 文件路径
     * @p
     * @return
     */
    public static String doPostSubmitBody(String urlAdded, Map<String, String> map, String filePath) {
        String url = urlBinjn + urlAdded + "/";

        // 设置三个常用字符串常量：换行、前缀、分界线（NEWLINE、PREFIX、BOUNDARY）；
        final String NEWLINE = "\r\n"; // 换行，或者说是回车
        final String PREFIX = "--"; // 固定的前缀
        final String BOUNDARY = "#"; // 分界线，就是上面提到的boundary，可以是任意字符串，建议写长一点，这里简单的写了一个#
        HttpURLConnection httpConn = null;
        BufferedInputStream bis = null;
        DataOutputStream dos = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // 实例化URL对象。调用URL有参构造方法，参数是一个url地址；
            URL urlObj = new URL(url);
            // 调用URL对象的openConnection()方法，创建HttpURLConnection对象；
            httpConn = (HttpURLConnection) urlObj.openConnection();
            // 调用HttpURLConnection对象setDoOutput(true)、setDoInput(true)、setRequestMethod("POST")；
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);
            httpConn.setRequestMethod("POST");
            // 设置Http请求头信息；（Accept、Connection、Accept-Encoding、Cache-Control、Content-Type、User-Agent），不重要的就不解释了，直接参考抓包的结果设置即可
            httpConn.setUseCaches(false);
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("Accept", "*/*");
            httpConn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            httpConn.setRequestProperty("Cache-Control", "no-cache");
            // 这个比较重要，按照上面分析的拼装出Content-Type头的内容
            httpConn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + BOUNDARY);
            // 这个参数可以参考浏览器中抓出来的内容写，用chrome或者Fiddler抓吧看看就行
            httpConn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)");
            // 调用HttpURLConnection对象的connect()方法，建立与服务器的真实连接；
            //token
            httpConn.setRequestProperty("token", tokenBinjn);
            httpConn.connect();

            // 调用HttpURLConnection对象的getOutputStream()方法构建输出流对象；
            dos = new DataOutputStream(httpConn.getOutputStream());
            // 获取表单中上传控件之外的控件数据，写入到输出流对象（根据上面分析的抓包的内容格式拼凑字符串）；
            if (map != null && !map.isEmpty()) { // 这时请求中的普通参数，键值对类型的，相当于上面分析的请求中的username，可能有多个
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String key = entry.getKey(); // 键，相当于上面分析的请求中的username
                    String value = map.get(key); // 值，相当于上面分析的请求中的sdafdsa
                    dos.writeBytes(PREFIX + BOUNDARY + NEWLINE); // 像请求体中写分割线，就是前缀+分界线+换行
                    dos.writeBytes("Content-Disposition: form-data; "
                            + "name=\"" + key + "\"" + NEWLINE); // 拼接参数名，格式就是Content-Disposition: form-data; name="key" 其中key就是当前循环的键值对的键，别忘了最后的换行
                    dos.writeBytes(NEWLINE); // 空行，一定不能少，键和值之间有一个固定的空行
                    dos.writeBytes(URLEncoder.encode(value.toString(), "UTF-8")); // 将值写入
                    // 或者写成：dos.write(value.toString().getBytes(charset));
                    dos.writeBytes(NEWLINE); // 换行
                } // 所有循环完毕，就把所有的键值对都写入了
            }

            //发送文件数据数据
            File file = new File(filePath);
            FileInputStream fileInput = new FileInputStream(file);
            byte[] body_data = new byte[fileInput.available()];
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            in.read(body_data);
            fileInput.close();
            // 获取表单中上传附件的数据，写入到输出流对象（根据上面分析的抓包的内容格式拼凑字符串）；
            if (body_data != null && body_data.length > 0) {
                dos.writeBytes(PREFIX + BOUNDARY + NEWLINE);// 像请求体中写分割线，就是前缀+分界线+换行
                String fileName = filePath.substring(filePath
                        .lastIndexOf(File.separatorChar) + 1); // 通过文件路径截取出来文件的名称，也可以作文参数直接传过来
                // 格式是:Content-Disposition: form-data; name="请求参数名"; filename="文件名"
                // 我这里吧请求的参数名写成了uploadFile，是死的，实际应用要根据自己的情况修改
                // 不要忘了换行
                dos.writeBytes("Content-Disposition: form-data; " + "name=\""
                        + "liveImgfiles" + "\"" + "; filename=\"" + fileName
                        + "\"" + NEWLINE);
                // 换行，重要！！不要忘了
                dos.writeBytes(NEWLINE);
                dos.write(body_data); // 上传文件的内容
                dos.writeBytes(NEWLINE); // 最后换行
            }
            dos.writeBytes(PREFIX + BOUNDARY + PREFIX + NEWLINE); // 最后的分割线，与前面的有点不一样是前缀+分界线+前缀+换行，最后多了一个前缀
            dos.flush();

            // 调用HttpURLConnection对象的getInputStream()方法构建输入流对象；
            byte[] buffer = new byte[8 * 1024];
            int c = 0;
            // 调用HttpURLConnection对象的getResponseCode()获取客户端与服务器端的连接状态码。如果是200，则执行以下操作，否则返回null；
            if (httpConn.getResponseCode() == 200) {
                bis = new BufferedInputStream(httpConn.getInputStream());
                while ((c = bis.read(buffer)) != -1) {
                    baos.write(buffer, 0, c);
                    baos.flush();
                }
            }
            // 将输入流转成字节数组，返回给客户端。
            return new String(baos.toByteArray(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dos != null)
                    dos.close();
                if (bis != null)
                    bis.close();
                if (baos != null)
                    baos.close();
                httpConn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String upload(String filePath,Map<String,String> params, String urlAdded){
        String host = urlBinjn + urlAdded + "/";


        String CHARSET = "utf-8"; //设置编码
        String PREFIX = "--";
        String LINE_END = "\r\n";

        String BOUNDARY = UUID.randomUUID().toString(); //边界标识 随机生成 String PREFIX = "--" , LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data"; //内容类型
        try {
            URL url = new URL(host);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setRequestMethod("POST"); //请求方式
            conn.setRequestProperty("Charset", CHARSET);//设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            conn.setDoInput(true); //允许输入流
            conn.setDoOutput(true); //允许输出流
            conn.setUseCaches(false); //不允许使用缓存
            conn.setRequestProperty("token", tokenBinjn);

            File file = new File(filePath);
            if(file!=null) {
                /** * 当文件不为空，把文件包装并且上传 */
                OutputStream outputSteam=conn.getOutputStream();
                DataOutputStream dos = new DataOutputStream(outputSteam);
                StringBuffer sb = new StringBuffer();
                sb.append(LINE_END);
                if(params!=null){//根据格式，开始拼接文本参数
                    for(Map.Entry<String,String> entry:params.entrySet()){
                        sb.append(PREFIX).append(BOUNDARY).append(LINE_END);//分界符
                        sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_END);
                        sb.append("Content-Type: text/plain; charset=" + CHARSET + LINE_END);
                        sb.append("Content-Transfer-Encoding: 8bit" + LINE_END);
                        sb.append(LINE_END);
                        sb.append(entry.getValue());
                        sb.append(LINE_END);//换行！
                    }
                }
                sb.append(PREFIX);//开始拼接文件参数
                sb.append(BOUNDARY); sb.append(LINE_END);
                /**
                 * 这里重点注意：
                 * name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名的 比如:abc.png
                 */
                sb.append("Content-Disposition: form-data; name=\"file\"; filename=\""+file.getName()+"\""+LINE_END);
                sb.append("Content-Type: application/octet-stream; charset="+CHARSET+LINE_END);
                sb.append(LINE_END);
                //写入文件数据
                dos.write(sb.toString().getBytes());
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                long totalbytes = file.length();
                long curbytes = 0;
                Log.i("cky","total="+totalbytes);
                int len = 0;
                while((len=is.read(bytes))!=-1){
                    curbytes += len;
                    dos.write(bytes, 0, len);
                }
                is.close();
                dos.write(LINE_END.getBytes());//一定还有换行
                byte[] end_data = (PREFIX+BOUNDARY+PREFIX+LINE_END).getBytes();
                dos.write(end_data);
                dos.flush();
                /**
                 * 获取响应码 200=成功
                 * 当响应成功，获取响应的流
                 */
                // 判断请求是否成功
                String result = null;
                if (conn.getResponseCode() == 200) {
                    // 获取返回的数据
                    result = streamToString(conn.getInputStream());
                    Log.i(mTAG, "Post方式请求成功，result--->" + result);
                } else {
                    Log.e(mTAG, "Post方式请求失败");
                }
                // 关闭连接
                conn.disconnect();
                return result;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 人脸照片特征值合并上传（多文件传输）
     * @param filePath
     * @param params
     * @param urlAdded
     * @param f1p
     * @param f2p
     * @return
     */
    public static String uploadFace_FeatureFiles(List<String> filePath,Map<String,String> params, String urlAdded, String f1p, String f2p){
        String re = "";
        String host = urlBinjn + urlAdded + "/";

        String CHARSET = "utf-8"; //设置编码
        String PREFIX = "--";
        String LINE_END = "\r\n";
        String BOUNDARY = UUID.randomUUID().toString(); //边界标识 随机生成 String PREFIX = "--" , LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data"; //内容类型

        try {
            URL url = new URL(host);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setRequestMethod("POST"); //请求方式
            conn.setRequestProperty("Charset", CHARSET);//设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            conn.setDoInput(true); //允许输入流
            conn.setDoOutput(true); //允许输出流
            conn.setUseCaches(false); //不允许使用缓存
            conn.setRequestProperty("token", tokenBinjn);
            /** 拼接参数*/
            OutputStream outputSteam=conn.getOutputStream();
            DataOutputStream dos = new DataOutputStream(outputSteam);
            StringBuffer sb = new StringBuffer();
            sb.append(LINE_END);
            if(params!=null){//根据格式，开始拼接文本参数
                for(Map.Entry<String,String> entry:params.entrySet()){
                    sb.append(PREFIX).append(BOUNDARY).append(LINE_END);//分界符
                    sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_END);
                    sb.append("Content-Type: text/plain; charset=" + CHARSET + LINE_END);
                    sb.append("Content-Transfer-Encoding: 8bit" + LINE_END);
                    sb.append(LINE_END);
                    sb.append(entry.getValue());
                    sb.append(LINE_END);//换行！
                }
            }
            /** 开始拼接文件参数*/
            for (String fp : filePath) {
                File file = new File(fp);
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                sb.append("Content-Disposition: form-data; name=\"" + f1p +"\"; filename=\""+file.getName()+"\""+LINE_END);
                sb.append("Content-Type: application/octet-stream; charset="+CHARSET+LINE_END);
                sb.append(LINE_END);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return re;
    }
}
