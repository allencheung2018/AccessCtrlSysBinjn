package com.utilcommon;


import android.text.TextUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author gaoxiang
 * @date 2017/11/27 22:54
 * @description byte 工具类
 *
 * 对数字和字节进行转换。<br>
 * 基础知识：<br>
 * 假设数据存储是以大端模式存储的：<br>
 * byte: 字节类型 占8位二进制 00000000<br>
 * char: 字符类型 占2个字节 16位二进制 byte[0] byte[1]<br>
 * int : 整数类型 占4个字节 32位二进制 byte[0] byte[1] byte[2] byte[3]<br>
 * long: 长整数类型 占8个字节 64位二进制 byte[0] byte[1] byte[2] byte[3] byte[4] byte[5]
 * byte[6] byte[7]<br>
 * float: 浮点数(小数) 占4个字节 32位二进制 byte[0] byte[1] byte[2] byte[3]<br>
 * double: 双精度浮点数(小数) 占8个字节 64位二进制 byte[0] byte[1] byte[2] byte[3] byte[4]
 * byte[5] byte[6] byte[7]<br>
 */
public class ByteKit {
    /**
     * 合并byte数组
     * @param data1
     * @param data2
     * @return data1 与 data2拼接的结果
     */
    public static byte[] addBytes(byte[] data1, byte[] data2) {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;

    }

    /**
     * 字节数组转换为int
     * @param bytes 字节数组
     * @return
     */
    public static int bytesToInt(byte[] bytes){
        // BigEndian
        return ByteBuffer.wrap(bytes).getInt();
        // LittleEndian
        //return ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
    }

    /**
     * int转换为字节数组
     * @param i 整数
     * @return
     */
    public static byte[] intToBytes(int i){
        byte[] bytes = new byte[4];
        // BigEndian
        ByteBuffer.wrap(bytes).putInt(i);
        // LittleEndian
        //ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).putInt(i);
        return bytes;
    }

    /**
     * 16进制的字符串表示转成字节数组
     *
     * @param hexString 16进制格式的字符串
     * @return 转换后的字节数组
     **/
    public static byte[] hexStrtoBytes(String hexString) {
        if (TextUtils.isEmpty(hexString))
            throw new IllegalArgumentException("this hexString must not be empty");

        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {//因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
            byteArray[i] = (byte) (high << 4 | low);
            k += 2;
        }
        return byteArray;
    }

    /**
     * 字节数组转成16进制表示格式的字符串
     *
     * @param byteArray 要转换的字节数组
     * @return 16进制表示格式的字符串
     **/
    public static String bytesToHexStr(byte[] byteArray) {
        if (byteArray == null || byteArray.length < 1)
            throw new IllegalArgumentException("this byteArray must not be null or empty");

        final StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < byteArray.length; i++) {
            if ((byteArray[i] & 0xff) < 0x10)//0~F前面不零
                hexString.append("0");
            hexString.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return hexString.toString().toLowerCase();
    }

    /**
     * 字节数组转float数组
     * @param bytes     字节数据
     * @return
     */
    public static float[] bytesToFloats(byte[] bytes){
        if(BlankKit.notBlank(bytes) && bytes.length >= 4 && bytes.length%4==0){
            float[] floats = new float[bytes.length / 4];
            int j = 0;
            for (int i=0 ; i<bytes.length/4 ; i++){
                byte[] tempBytes = new byte[4];
                tempBytes[0] = bytes[4 * i + 0];
                tempBytes[1] = bytes[4 * i + 1];
                tempBytes[2] = bytes[4 * i + 2];
                tempBytes[3] = bytes[4 * i + 3];

                floats[j++] = bytesToFloat(tempBytes);
            }
            return floats;
        }

        return new float[0];
    }

    /**
     * 字节数组转换为float
     * @param bytes 字节数组
     * @return
     */
    public static float bytesToFloat(byte[] bytes){
        // BigEndian
        //return ByteBuffer.wrap(bytes).getFloat();
        // LittleEndian
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    public static float[] bytesToFloats2(byte[] bytes){
        if(BlankKit.notBlank(bytes) && bytes.length >= 4 && bytes.length%4==0){
            float[] floats = new float[bytes.length / 4];
            int j = 0;
            for (int i=0 ; i<bytes.length/4 ; i++){
                byte[] tempBytes = new byte[4];
                tempBytes[0] = bytes[4 * i + 0];
                tempBytes[1] = bytes[4 * i + 1];
                tempBytes[2] = bytes[4 * i + 2];
                tempBytes[3] = bytes[4 * i + 3];

                floats[j++] = bytesToFloat2(tempBytes);
            }
            return floats;
        }

        return new float[0];
    }
    /**
     * 字节数组转换为float
     * @param bytes 字节数组
     * @return
     */
    public static float bytesToFloat2(byte[] bytes){
        // BigEndian
        //return ByteBuffer.wrap(bytes).getFloat();
        // LittleEndian
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getFloat();
    }

    /**
     * float转换为字节数组
     * @param f 浮点数
     * @return
     */
    public static byte[] floatToBytes(float f){
        byte[] bytes = new byte[4];
        // BigEndian
        //ByteBuffer.wrap(bytes).putFloat(f);
        // LittleEndian
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putFloat(f);
        return bytes;
    }

    /**
     * float数组转字节数组
     * @param floats    float数组
     * @return
     */
    public static byte[] floatsToBytes(float[] floats){
        if(BlankKit.notBlank(floats)){
            byte[] result = new byte[floats.length * 4];
            for (int i=0 ; i<floats.length ; i++){
                byte[] tempBytes = floatToBytes(floats[i]);
                result[4 * i + 0] = tempBytes[0];
                result[4 * i + 1] = tempBytes[1];
                result[4 * i + 2] = tempBytes[2];
                result[4 * i + 3] = tempBytes[3];
            }
            return result;
        }

        return new byte[0];
    }

}
