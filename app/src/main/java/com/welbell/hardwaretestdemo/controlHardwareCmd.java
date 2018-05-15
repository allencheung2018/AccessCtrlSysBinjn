package com.welbell.hardwaretestdemo;

/**
 * Created by lijiarui on 2017/3/31.
 */
public class controlHardwareCmd {

    public static final byte E_DOOEBEL = 0X10; // 有线门铃
    public static final byte E_SMART_HOME = 0x11; // 智能家居(西安郑楠项目)
    public static final byte E_DOOR_LOCK = 0x12; // 锁
    public static final byte E_INFRARED = 0x13; // 红外
    /* 控制摄像头灯 例：开灯：buf[] = {0x14,0x01} */
    public static final byte E_CAMERA_LIGHT = 0x14; // 摄像头灯
    /* 控制键盘灯 例：开灯：buf[] = {0x15,0x01} */
    public static final byte E_KEY_LIGHT = 0x15; // 键盘灯
    /* 控制屏幕背光 例：灭屏：buf[] = {0x16,0x01} */
    public static final byte E_LCD_BACKLIGHT = 0x16; // 屏幕背光
    /* 指纹识别 ，方法见fingerprintCmd */
    public static final byte E_FINGERPRINT = 0x17; // 指纹
    /*
     * 设置IP地址+子网掩码+网关+MACaddr
     *
     * 例：buf[] =
     * {192,168,1,100,255,255,255,255,192,168,1,1,88,88,88,88,88,88};
     */
    public static final byte E_SET_IPADDR = 0x18; // 设置IP地址
    public static final byte E_RESTART = 0x19; // 重启机器


    //        /*获取图像*/
    //        value[0]=0x01;//（包标识）
    //        value[1]=0x01;//（CMD）
    //        a8SetKeyValue(0x17, value, 2);
    //        //等待systemcallback上报data

    //        //当data[1] == 00 时 则可以把指纹数据存储到缓冲区中
    //        value[0]=0x01; //（包标识）
    //        value[1]=0x02; //（CMD）
    //        value[2]=0x01; //（缓冲区号）
    //        a8SetKeyValue(0x17, value, 3);
    //        //等待systemcallback上报data

    //        //当data[1] == 00 时 则可以把指纹数据存储到缓冲区中
    //          /*获取图像*/
    //        value[0]=0x01;//（包标识）
    //        value[1]=0x01;//（CMD）
    //        a8SetKeyValue(0x17, value, 2);
    //
    //        //等待systemcallback上报data
    //        //当data[1] == 00 时 则可以把指纹数据存储到缓冲区中
    //        value[0]=0x01; //（包标识）
    //        value[1]=0x02; //（CMD）
    //        value[2]=0x02; //（缓冲区号）
    //        a8SetKeyValue(0x17, value, 3);

    //        //等待systemcallback上报data
    //        //当data[1] == 00 时 则可以把指纹数据存储到缓冲区中
    //
    //        //合成模块
    //        value[0]=0x01; //（包标识）
    //        value[1]=0x05; //（CMD）
    //        a8SetKeyValue(0x17, value, 2);
    //
    //        //等待systemcallback上报data
    //        //当data[1] == 00 时 则可以把指纹数据存储到缓冲区中
    //        value[0]=0x01; //（包标识）
    //        value[1]=0x06; //（CMD）
    //        value[2]=0x01; //(缓冲区号)
    //        value[3]=0x00; //(800&0xff00); //(位置号[H])
    //        value[4]=0x01; //(800&0xff); //(位置号[L])
    //        a8SetKeyValue(0x17, value, 5);

}
