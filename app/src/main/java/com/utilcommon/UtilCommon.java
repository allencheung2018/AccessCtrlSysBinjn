package com.utilcommon;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.Log;

import com.binjn.accessctrlsysbinjn.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TooManyListenersException;
import java.util.TreeMap;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by xiaomi2 on 2017/12/7 0007.
 */

public class UtilCommon {
    //
    private static final String mTAG = "UtilCommon";

    public static String getTimeNetwork(String addr){
        URL url = null;//取得资源对象
        String str = "";
        try {
            url = new URL("http://www.baidu.com");
            URLConnection uc = url.openConnection();//生成连接对象
            uc.connect(); //发出连接
            long ld = uc.getDate(); //取得网站日期时间
            str = longToString(ld, "yyyyMMddHHmmss");
            return str;
        }catch (Exception e) {
            str = "getTimeNetwork error:"+e.toString();
            Log.e(mTAG, str);
            LogFile.getInstance().saveMessage(str);
            e.printStackTrace();
        }
        return null;
    }

    static public String getTimeStampCurrent(){
        SimpleDateFormat formatter = new SimpleDateFormat    ("yyyyMMddHHmmss");
        Date curDate = new Date();
        String str = formatter.format(curDate);
        return str;
    }

    static public String getTimeCurrent(){
        SimpleDateFormat formatter = new SimpleDateFormat    ("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date();
        String str = formatter.format(curDate);
        return str;
    }

    static public String getDateString(){
        SimpleDateFormat formatter = new SimpleDateFormat    ("yyyyMMdd");
        Date curDate = new Date();
        String str = formatter.format(curDate);
        return str;
    }
    /*

     */
    static public Date getDateFromString(String strDate){
        SimpleDateFormat formatter = new SimpleDateFormat    ("yyyyMMddHHmmss");
        Date date = null;
        try {
            date = formatter.parse(strDate);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    static public Date getDateFromStringSimple(String strDate){
        SimpleDateFormat formatter = new SimpleDateFormat    ("yyyyMMdd");
        Date date = null;
        try {
            date = formatter.parse(strDate);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    // currentTime要转换的long类型的时间
    // formatType要转换的string类型的时间格式
    public static String longToString(long currentTime, String formatType)
            throws ParseException {
        Date date = longToDate(currentTime, formatType); // long类型转成Date类型
        String strTime = dateToString(date, formatType); // date类型转成String
        return strTime;
    }

    // currentTime要转换的long类型的时间
    // formatType要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    public static Date longToDate(long currentTime, String formatType)
            throws ParseException {
        Date dateOld = new Date(currentTime); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = dateToString(dateOld, formatType); // 把date类型的时间转换为string
        Date date = stringToDate(sDateTime, formatType); // 把String类型转换为Date类型
        return date;
    }

    // formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    // data Date类型的时间
    public static String dateToString(Date data, String formatType) {
        return new SimpleDateFormat(formatType).format(data);
    }

    // strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
    // HH时mm分ss秒，
    // strTime的时间格式必须要与formatType的时间格式相同
    public static Date stringToDate(String strTime, String formatType)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }

    //获得当前年月日时分秒星期
    public static String getDateTimeWeek(){
        java.text.DecimalFormat df = new java.text.DecimalFormat();
        df.applyPattern("00");
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String mYear = String.valueOf(c.get(Calendar.YEAR)); // 获取当前年份
        String mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);// 获取当前月份
        String mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));// 获取当前月份的日期号码
        String mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
        String mHour = df.format(c.get(Calendar.HOUR_OF_DAY));//String.valueOf(c.get(Calendar.HOUR_OF_DAY));//时
        String mMinute = df.format(c.get(Calendar.MINUTE));//分
        String mSecond = df.format(c.get(Calendar.SECOND));//秒

        if("1".equals(mWay)){
            mWay ="天";
        }else if("2".equals(mWay)){
            mWay ="一";
        }else if("3".equals(mWay)){
            mWay ="二";
        }else if("4".equals(mWay)){
            mWay ="三";
        }else if("5".equals(mWay)){
            mWay ="四";
        }else if("6".equals(mWay)){
            mWay ="五";
        }else if("7".equals(mWay)){
            mWay ="六";
        }
        return mYear + "年" + mMonth + "月" + mDay+"日"+"  "+"星期"+mWay+"  "+mHour+":"+mMinute+":"+mSecond;
    }

    /**
     * 复制单个文件
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[100*1024];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    Log.d(mTAG, ""+bytesum);
                    fs.write(buffer, 0, byteread);
                }
                fs.flush();
                fs.close();
                inStream.close();
            }
        }
        catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();
            Log.e(mTAG, "copyFile:"+e.toString());
            LogFile.getInstance().saveMessage("copyFile error:"+e.toString());
        }

    }

    /**
     * 这是使用adb shell命令来获取mac地址的方式
     * @return
     */
    public static String getMac() {
        String macSerial = null;
        String str = "";

        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/eth0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        if (macSerial == null){
            return null;
        }
        str = macSerial.replace(":", "");
        Log.i(mTAG, "str:"+str);
        return str;
    }

    static public boolean savePhoto(String fPath, Bitmap bitmap){
        try {
            File file = new File(fPath);
            if (file.exists()){
                file.delete();
            }
            file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 直接存储文件、必须确保文件夹存在
     * @param path
     * @param data
     * @return
     */
    static public boolean saveByteFile(String path, byte[] data){
        Log.d(mTAG, "saveByteFile:"+path);
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            out.write(data);
            out.flush();
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(mTAG, "saveByteFile:"+e.toString());
            LogFile.getInstance().saveMessage("saveByteFile error:"+e.toString());
            return false;
        }
    }

    static public boolean saveByteFile(String fDir, String fName, byte[] data){
        Log.d(mTAG, "saveByteFile-dir"+fDir + " name:"+fName);
        try {
            File file = new File(fDir);
            if (!file.exists()){
                file.mkdirs();
            }
            file = new File(fDir+fName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            out.write(data);
            out.flush();
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(mTAG, "saveByteFile:"+e.toString());
            LogFile.getInstance().saveMessage("saveByteFile error:"+e.toString());
            return false;
        }
    }

    static public boolean writeDatatoFileByHex(String fPath, List<float[]> list){
        try {
            File file = new File(fPath);
            if (!file.exists()){
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
            objectOutputStream.writeObject(list);
            objectOutputStream.flush();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static public boolean writeFloatDatatoFile(String fPath, float[] data, int size){
        Log.d("writeFloatDatatoFile", "size="+size + " 0:"+data[0] + " 2040:"+data[2040]);
        try {
            File file = new File(fPath);
            if (!file.exists()){
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
            objectOutputStream.writeObject(data);
            objectOutputStream.flush();
            objectOutputStream.close();
            out.close();
            Log.d("writeFloatDatatoFile", "write finish");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static public void writeFloatArray1(float[] arr, String dir){
        FileOutputStream fos = null;
        DataOutputStream dos = null;

        try {
            // create file output stream
            fos = new FileOutputStream(dir);

            // create data output stream
            dos = new DataOutputStream(fos);

            // for each byte in the buffer
            for (float f : arr) {
                // write float to the data output stream
                dos.writeFloat(f);
            }

            // force bytes to the underlying stream
            dos.flush();
            if(fos!=null)
                fos.close();
            if(dos!=null)
                dos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {

        }
    }

    static public float[] readFloatArray(String dir, int size) {
        InputStream is = null;
        DataInputStream dis = null;
        float[] data = new float[size];

        // create file input stream
        try {
            is = new FileInputStream(dir);
            dis = new DataInputStream(is);
            for (int i = 0; i < size; i++) {
                data[i] = dis.readFloat();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static float[] readFloatData(String fPath)
    {
        float[] data = null;// = new float[20480];
        try {
            File file = new File(fPath);
            FileInputStream in = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(in);
            data =(float[])objectInputStream.readObject();
            objectInputStream.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Log.d("readFloatData", "data:"+data.length);
        return data;
    }
    /*
    * 读取特征值文件信息
     */
    public static byte[] readFeatureFileInfos(String fPath){
        if (fPath == null)
            return null;
        String str = "文件路径："+fPath;
        int cnt = 0;
        File file = new File(fPath);
        if (!file.exists()){
            Log.d(mTAG, "文件不存在");
            return null;
        }
        byte[] fdata;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            fdata = new byte[in.available()];
            in.read(fdata);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        str = str + " 读取成功 = "+fdata.length;
        Log.d(mTAG, str);
        return fdata;

    }
    /**
     * 冒泡排序
     * 比较相邻的元素。如果第一个比第二个大，就交换他们两个。
     * 对每一对相邻元素作同样的工作，从开始第一对到结尾的最后一对。在这一点，最后的元素应该会是最大的数。
     * 针对所有的元素重复以上的步骤，除了最后一个。
     * 持续每次对越来越少的元素重复上面的步骤，直到没有任何一对数字需要比较。
     * @param numbers 需要排序的整型数组
     */
    public static void bubbleSort(float[] numbers) {
        float temp = 0;
        int size = numbers.length;
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - 1 - i; j++) {
                if (numbers[j] < numbers[j + 1])  //交换两数位置
                {
                    temp = numbers[j];
                    numbers[j] = numbers[j + 1];
                    numbers[j + 1] = temp;
                }
            }
        }
    }

    public static void bubbleSort(float[] numbers, int[] sort) {
        float temp = 0;
        int itemp = 0;
        int size = numbers.length;
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - 1 - i; j++) {
                if (numbers[j] < numbers[j + 1])  //交换两数位置
                {
                    temp = numbers[j];
                    numbers[j] = numbers[j + 1];
                    numbers[j + 1] = temp;
                    itemp = sort[j];
                    sort[j] = sort[j+1];
                    sort[j+1] = itemp;
                }
            }
        }
    }

    public static float averageArray(float[] floats){
        float temp = 0;
        int size = floats.length;
        Log.d(mTAG, "averageArray-size="+size);
        for (float f:floats){
            temp += f;
        }
        return temp/size;
    }

    public static byte[] int2Bytes(int i){
        byte[] outData=new byte[4];
        outData[0]=(byte)i;
        outData[1]=(byte)(i>>8);
        outData[2]=(byte)(i>>16);
        outData[3]=(byte)(i>>24);
        return outData;
    }

    public static byte[] int2BytesBigEndian(int i){
        byte[] outData=new byte[4];
        outData[0]=(byte)(i>>24);
        outData[1]=(byte)(i>>16);
        outData[2]=(byte)(i>>8);
        outData[3]=(byte)(i);
        return outData;
    }

    public static final byte[] float2ByteLittleEnd(float[] inData) {
        int j=0;
        int length=inData.length;
        Log.d("float2Byte", "len="+length);
        byte[] outData=new byte[length*4];
        for (int i=0;i<length;i++) {
            int data=Float.floatToIntBits(inData[i]);
            outData[j++]=(byte)data;
            outData[j++]=(byte)(data>>8);
            outData[j++]=(byte)(data>>16);
            outData[j++]=(byte)(data>>24);
        }
        return outData;
    }

    public static final byte[] float2ByteBigEnd(float[] inData) {
        int j=0;
        int length=inData.length;
        Log.d("float2Byte", "len="+length);
        byte[] outData=new byte[length*4];
        for (int i=0;i<length;i++) {
            int data=Float.floatToIntBits(inData[i]);
            outData[j++]=(byte)(data>>24);
            outData[j++]=(byte)(data>>16);
            outData[j++]=(byte)(data>>8);
            outData[j++]=(byte)(data);
        }
        return outData;
    }

    public static final float[] byte2FloatLitteEnd(byte[] inData) {
        int j=0;
        int length=inData.length/4;
        Log.d("byte2FloatLitteEnd", "len="+length);
        float[] outData=new float[length];
        for (int i=0;i<length;i++) {
            int index = i*4;
            int l;
            l = inData[index + 0];
            l &= 0xff;
            l |= ((long) inData[index + 1] << 8);
            l &= 0xffff;
            l |= ((long) inData[index + 2] << 16);
            l &= 0xffffff;
            l |= ((long) inData[index + 3] << 24);
            outData[i] = Float.intBitsToFloat(l);
        }
        return outData;
    }

    public static final float[] byte2FloatLitteEnd(byte[] inData, int offset, int len) {
        Log.d("byte2FloatLitteEnd", " offset="+offset + " len="+len);
        float[] outData=new float[len];
        for (int i=0;i<len;i++) {
            int index = i*4 + offset;
            int l;
            l = inData[index + 0];
            l &= 0xff;
            l |= ((long) inData[index + 1] << 8);
            l &= 0xffff;
            l |= ((long) inData[index + 2] << 16);
            l &= 0xffffff;
            l |= ((long) inData[index + 3] << 24);
            outData[i] = Float.intBitsToFloat(l);
        }
        return outData;
    }

    public static final float[] byte2FloatBigEnd(byte[] inData) {
        int j=0;
        int length=inData.length/4;
        Log.d("byte2Float", "len="+length);
        float[] outData=new float[length];
        for (int i=0;i<length;i++) {
            int index = i*4;
//            int l;
//            l = inData[index + 0];
//            l &= 0xff;
//            l |= ((long) inData[index + 1] << 8);
//            l &= 0xffff;
//            l |= ((long) inData[index + 2] << 16);
//            l &= 0xffffff;
//            l |= ((long) inData[index + 3] << 24);

            int l = (inData[index + 0] << 24) & 0xFF000000;
            l |= (inData[index + 1] << 16) & 0xFF0000;
            l |= (inData[index + 2] << 8) & 0xFF00;
            l |= inData[index + 3] & 0xFF;
            outData[i] = Float.intBitsToFloat(l);
        }
        return outData;
    }

    public static final float[] byte2FloatBigEnd(byte[] inData, int offset, int len) {
//        len = len/4;
        Log.d("byte2FloatBigEnd", "len="+len + " offset="+offset);
        float[] outData=new float[len];
        for (int i=0;i<len;i++) {
            int index = i*4 + offset;

            int l = (inData[index + 0] << 24) & 0xFF000000;
            l |= (inData[index + 1] << 16) & 0xFF0000;
            l |= (inData[index + 2] << 8) & 0xFF00;
            l |= inData[index + 3] & 0xFF;
            outData[i] = Float.intBitsToFloat(l);
        }
        return outData;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。和intToBytes2（）配套使用
     */
    public static int bytesToInt2(byte[] src, int offset) {
        int value;
        value = (int) ( ((src[offset] & 0xFF)<<24)
                |((src[offset+1] & 0xFF)<<16)
                |((src[offset+2] & 0xFF)<<8)
                |(src[offset+3] & 0xFF));
        return value;
    }

    public static byte[] getByteArrays(float[] f) {
        byte[] result = new byte[f.length*4];
        for (int i = 0;i < f.length;i ++){
            //将float里面的二进制串解释为int整数
            int ibit = Float.floatToIntBits(f[i]);
            result[4 * i + 0] = (byte) ((ibit & 0xff000000) >> 24);
            result[4 * i + 1] = (byte) ((ibit & 0x00ff0000) >> 16);
            result[4 * i + 2]= (byte) ((ibit & 0x0000ff00) >> 8);
            result[4 * i + 3] = (byte)  (ibit & 0x000000ff);
        }
        return result;
    }

    public static byte[] getByteArraysLittleEnd(float[] f) {
        byte[] result = new byte[f.length*4];
        for (int i = 0;i < f.length;i ++){
            //将float里面的二进制串解释为int整数
            int ibit = Float.floatToIntBits(f[i]);
            result[4 * i + 0] = (byte) ((ibit & 0xff) );
            result[4 * i + 1] = (byte) ((ibit & 0xff) >> 8);
            result[4 * i + 2]= (byte) ((ibit & 0xff) >> 16);
            result[4 * i + 3] = (byte) ((ibit & 0xff) >> 24);
        }
        return result;
    }

    public static byte[] getLong2ByteArrays(Long[] f) {
        byte[] result = new byte[f.length*8];
        for (int i = 0;i < f.length;i ++){
            //将float里面的二进制串解释为int整数
            Long ibit = f[i];
            result[4 * i + 0] = (byte) ((ibit & 0xff000000) >> 56);
            result[4 * i + 1] = (byte) ((ibit & 0xff000000) >> 48);
            result[4 * i + 2] = (byte) ((ibit & 0xff000000) >> 40);
            result[4 * i + 3] = (byte) ((ibit & 0xff000000) >> 32);
            result[4 * i + 4] = (byte) ((ibit & 0xff000000) >> 24);
            result[4 * i + 5] = (byte) ((ibit & 0x00ff0000) >> 16);
            result[4 * i + 6]= (byte) ((ibit & 0x0000ff00) >> 8);
            result[4 * i + 7] = (byte)  (ibit & 0x000000ff);
        }
        return result;
    }

    /**
    * 保存单个人特征值文件
    * no : 特征库编号
    * name : 特征库名称
    * vers : 特征库版本
    * time : 特征库更新时间
    * count : 特征值数量
    * len : 特征值长度
    * feat : 特征值数组
    * faceIds : 特征值ID数组
    * fPath : 文件路径
     */
    public static void saveFeatureFile(String no, String name, String ver, String time, int count,
                                       int len, float[] feat, String faceId, String fPath){
        int lenData = 32 + 128 + 16 + 32 + 256 + 4 + 4 + count*len*4 + faceId.length();
        byte[] data = new byte[lenData];
        int index = 0;
        System.arraycopy(no.getBytes(), 0, data, index, no.length());
        index += 32;
        System.arraycopy(name.getBytes(), 0, data, index, name.length());
        index += 128;
        System.arraycopy(ver.getBytes(), 0, data, index, ver.length());
        index += 16;
        System.arraycopy(time.getBytes(), 0, data, index, time.length());
        index += 32;
        index += 256;
        byte[] temp = UtilCommon.int2BytesBigEndian(count);
        System.arraycopy(temp, 0, data, index, temp.length);
        index += 4;
        temp = UtilCommon.int2BytesBigEndian(len*4);
        System.arraycopy(temp, 0, data, index, temp.length);
        index += 4;
        temp = UtilCommon.float2ByteLittleEnd(feat);
        System.arraycopy(temp, 0, data, index, temp.length);
        index += temp.length;
        if (faceId != null) {
            System.arraycopy(faceId.getBytes(), 0, data, index, faceId.length());
        }

        try {
            File file = new File(fPath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            out.write(data);
            out.flush();
            out.close();
            String str = "存储特征值文件成功-no:"+no + " name:"+name + " time:"+time + " count="+count
                    + " len="+len;
            Log.i(mTAG, str);
            LogFile.getInstance().saveMessage(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearBytes(byte[] data){
        int len = data.length;
        for (int i=0;i<len;i++){
            data[i] = 0;
        }
    }

    /**
     * 更新个人特征值本地文件
     * @param pid
     * @param featData
     * @param fPath
     */
    public static void updateLocalFeatureFile(String pid, float[] featData, String fPath){
        String str = "更新个人特征值本地文件";
        try {
            byte[] oldFeat;
            byte[] newFeat;
            int offset = 0;
            boolean flag = false;       //特征值文件是否已存在
            int count = MainActivity.numRegisterPhoto;
            int len = MainActivity.feat_size;
            File file = new File(fPath);
            if (file.exists()){
                FileInputStream fis = new FileInputStream(file);
                oldFeat = new byte[fis.available()];
                fis.read(oldFeat);
                fis.close();
                offset = 464;       //特征值数量
                count = bytesToInt2(oldFeat, offset);
                offset += 4;        //单个特征值长度8192
                len = bytesToInt2(oldFeat, offset)/4;
                str = str + "-当前特征值数量=" + count + " 单个特征值长度="+len;
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                if (count>0 && count<10000){// && len==MainActivity.feat_size){
                    flag = true;
                    byte[] temp = int2BytesBigEndian(count+MainActivity.numRegisterPhoto);
                    offset = 464;
                    System.arraycopy(temp, 0, oldFeat, offset, temp.length);
                    byte[] featbyte = float2ByteLittleEnd(featData);
                    Log.d(mTAG, "featData="+featData.length + " featbyte="+featbyte.length + " oldFeat="+oldFeat.length);
                    newFeat = new byte[oldFeat.length + featbyte.length];
                    offset += 4 + 4 + count*len*4;
                    System.arraycopy(oldFeat, 0, newFeat, 0, offset);
                    System.arraycopy(featbyte, 0, newFeat, offset, featbyte.length);
                    System.arraycopy(oldFeat, offset, newFeat, offset+featbyte.length, oldFeat.length-offset);
                    saveByteFile(fPath, newFeat);
                }
            }
            /** 无本地文件*/
            if (!flag){
                str = str + "-无本地文件-特征值数量=" + count + " 单个特征值长度="+len;
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                saveFeatureFile("local", pid, "1.0", getTimeCurrent(), count, len, featData, "", fPath);
            }
        } catch (Exception e){
            e.printStackTrace();
            str = "updateLocaoFeatureFile error:"+e.toString();
            Log.e(mTAG, str);
            LogFile.getInstance().saveMessage(str);
        }
    }

    /**
     * 批量人员特征值文件分存个人特征值文件
     * @param fPath
     * @return 存储了特征值的人员ID列表
     */
    public static List<String> savePersonsFeatureFile(String fPath){
        String str = "";
        int index = 0;
        File file = new File(fPath);
        if (!file.exists()){
            return null;
        }
        FileInputStream fis = null;
        byte[] buffer;
        try {
            fis = new FileInputStream(file);
            buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if(buffer[0]!=0x67 || buffer[1]!=0x67 || buffer[2]!=0x67 || buffer[3]!=0x67){
            return null;
        }
        index = 8;      //json长度
        int jsonLen = bytesToInt2(buffer, index);
        byte[] jsonBytes = new byte[jsonLen];
        index += 8;     //jsonBytes数据
        System.arraycopy(buffer, index, jsonBytes, 0, jsonLen);
        String jsonString = null;
        try {
            jsonString = new String(jsonBytes, "UTF-8");
            Log.d(mTAG, "jsonString:"+jsonString);
            JSONArray jsonArray = new JSONArray(jsonString);
            int size = jsonArray.length();
            List<String> plist = new ArrayList<>(size);
            index += jsonLen;       //blobBytes二进制大文件数据
            for (int i=0;i<size;i++){
                String s = (String) jsonArray.get(i);
                Log.d(mTAG, "s : "+s);
                String ss[] = s.split(",");
                String pid = ss[0];
                int len = Integer.parseInt(ss[1]);
                plist.add(ss[0]);
                byte[] tdata = new byte[len];
                System.arraycopy(buffer, index, tdata, 0, len);
                str = MainActivity.dirFeatData+pid+"/";
                boolean flag = saveByteFile(str, pid+".feat", tdata);
                index += len;
                str = "存储人员特征值文件："+pid + ":"+flag + " 文件长度="+len + " offset="+index;
                Log.d(mTAG, str);
                LogFile.getInstance().saveMessage(str);
            }
            if (size > 0){
                str = "分批存储个人特征值文件" + " 完成:"+size;
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                return plist;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    /**
     * md5
     * @param string
     * @return
     */
    public static String md51(String string) {
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

    public static void deleteDirectoryFiles(String dir){
        File file = new File(dir);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                f.delete();
            }
            file.delete();//如要保留文件夹，只删除文件，请注释这行
            String str = "删除文件夹："+dir + " 共 "+files.length+" 个文件";
            Log.i(mTAG, str);
            LogFile.getInstance().saveMessage(str);
        }
    }

    /**
     * 获取总内存
     * @param context
     * @return
     */
    public static String getTotalRam(Context context){//GB
        String path = "/proc/meminfo";
        String firstLine = null;
        int totalRam = 0 ;
        try{
            java.io.FileReader fileReader = new java.io.FileReader(path);
            java.io.BufferedReader br = new java.io.BufferedReader(fileReader,8192);
            firstLine = br.readLine().split("\\s+")[1];
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(firstLine != null){
            totalRam = (int)Math.ceil((new Float(Float.valueOf(firstLine) / (1024 * 1024)).doubleValue()));
        }

        return totalRam + "GB";//返回1GB/2GB/3GB/4GB
    }

    /**
     * 获得机身可用内存
     *
     * @return
     */
    public static String getRomAvailableSize(Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(context, blockSize * availableBlocks);
    }

    /**
     * 获得机身内存总大小
     *
     * @return
     */
    public static String getRomTotalSize(Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return Formatter.formatFileSize(context, blockSize * totalBlocks);
    }

    public static String getRomInfo(Context context){
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(context, blockSize * availableBlocks) + "/"
                + Formatter.formatFileSize(context, blockSize * totalBlocks);
    }

    /**
     * 获取内存情况
     * @param context
     */
    public static String getMemoryInfo(Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(outInfo );
        long availMem = outInfo.availMem;
        long totalMem = outInfo.totalMem;
        return Formatter.formatFileSize(context, availMem) + "/"
                + Formatter.formatFileSize(context, totalMem);
    }

}
