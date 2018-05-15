package com.hdos.idCardUartDevice;


//import com.welbell.yunjiang.utils.LogUtils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static android.content.ContentValues.TAG;

/**
 * Created by lijiarui on 2017/8/31.
 * 初始化身份证识别的类
 */

/**
 * #define AIX_UNIX     0 操作系统定义:1为AIX_UNIX系统,0为Linux系统
 * #define SUCC   0 操作成功
 * #define SendDataError    -121 发送数据失败
 * #define TermParaError    -122 终端参数错
 * #define TermTypeError    -123 终端类型错误
 * #define RevTimeOut       -100 接收数据超时
 *
 * #define ERROR_HEAD -11
 * #define ERROR_RECV -12
 * #define ERROR_XOR -13
 * #define ERROR_TALL -14
 * #define ERROR_SEND -15
 * #define ERROR_PARAMENT -16
 * #define ERROR_HANDLE -17
 * #define ERROR _TIME -18
 * #define ERROR_LEN -19
 */

public class publicSecurityIDCardLib {
    static {
        System.loadLibrary("IdentityCardUart");
    }

    public native JniReturnData idSamDataExchange(JniReturnData var1, String var2, byte[] var3);

    public native byte[] HdosIdUnpack(byte[] var1, String var2);

    public publicSecurityIDCardLib() {
    }

    public int cmdRequstID(String port) {
        JniReturnData returnData = new JniReturnData();
        byte[] cmdRequst = new byte[]{(byte)-86, (byte)-86, (byte)-86, (byte)-106, (byte)105, (byte)0, (byte)3, (byte)32, (byte)1, (byte)34};
        byte[] Cmd_requst_ans = new byte[]{(byte)-86, (byte)-86, (byte)-86, (byte)-106, (byte)105, (byte)0, (byte)8, (byte)0, (byte)0, (byte)-97, (byte)0, (byte)0, (byte)0, (byte)0, (byte)-105};
        Object response = null;
        returnData = this.idSamDataExchange(returnData, port, cmdRequst);
        if(returnData.result != 15) {
            return -1;
        } else {
            byte[] var7 = this.strToHex(returnData.iDCardData, returnData.result);

            for(int i = 0; i < 15; ++i) {
                if(Cmd_requst_ans[i] != var7[i]) {
                    return -1;
                }
            }

            return 0;
        }
    }

    public int cmdSelectID(String port) {
        JniReturnData returnData = new JniReturnData();
        byte[] cmdSelect = new byte[]{(byte)-86, (byte)-86, (byte)-86, (byte)-106, (byte)105, (byte)0, (byte)3, (byte)32, (byte)2, (byte)33};
        byte[] Cmd_select_ans = new byte[]{(byte)-86, (byte)-86, (byte)-86, (byte)-106, (byte)105, (byte)0, (byte)12, (byte)0, (byte)0, (byte)-112, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)-100};
        Object response = null;
        returnData = this.idSamDataExchange(returnData, port, cmdSelect);
        if(returnData.result != 19) {
            return -2;
        } else {
            byte[] var7 = this.strToHex(returnData.iDCardData, returnData.result);

            for(int i = 0; i < 19; ++i) {
                if(Cmd_select_ans[i] != var7[i]) {
                    return -2;
                }
            }

            return 0;
        }
    }

    public int cmdReadID(String port, byte[] response) {
        boolean re = false;
        JniReturnData returnData = new JniReturnData();
        byte[] cmdRead = new byte[]{(byte)-86, (byte)-86, (byte)-86, (byte)-106, (byte)105, (byte)0, (byte)3, (byte)48, (byte)1, (byte)50};
        this.cmdRequstID(port);
        this.cmdSelectID(port);
        Object rebuf = null;
        returnData = this.idSamDataExchange(returnData, port, cmdRead);
//        if(returnData.result > 0 && returnData.result < 1000) {
//            return -3;
//        LogUtils.i("MainActivity" , "returnData.result = "+returnData.result);
        if(returnData.result < 1000) {
            return returnData.result;
//            return -3;
        } else {
            byte[] var8 = this.strToHex(returnData.iDCardData, returnData.result);

            for(int i = 0; i < returnData.result - 14; ++i) {
                response[i] = var8[i + 14];
            }

            return returnData.result;
        }
    }

    public int readBaseMsg(String port, String pkName, byte[] pBmpFile, byte[] pName, byte[] pSex, byte[] pNation, byte[] pBirth, byte[] pAddress, byte[] pIDNo, byte[] pDepartment, byte[] pEffectDate, byte[] pExpireDate, byte[] pErrMsg) throws UnsupportedEncodingException {
        byte[] Response1 = new byte[1500];
        int ret = this.cmdReadID(port, Response1);
        if(ret == -1) {
            pErrMsg = "寻卡失败".getBytes("Unicode");
            System.out.println("寻卡失败了。。。。");
            return ret;
        } else if(ret == -2) {
            pErrMsg = "选卡失败".getBytes("Unicode");
            return ret;
        } else if(ret == -3) {
            pErrMsg = "选卡失败".getBytes("Unicode");
            return ret;
        }else if(ret == -100){
            pErrMsg = "ret == -100".getBytes("Unicode");
//            LogUtils.e("publicSecurityIDCardLib"," ret == -100...");
            return ret;
        }else if(ret == 11){
            pErrMsg = "ret == -100".getBytes("Unicode");
            return ret;
        }else {
            byte[] name = new byte[32];
            name[0] = -1;
            name[1] = -2;
            System.arraycopy(Response1, 0, name, 2, 30);
            System.arraycopy(name, 0, pName, 0, 32);
            byte[] sex = new byte[2];
            System.arraycopy(Response1, 30, sex, 0, 2);
            Object sexout = null;
            byte[] var45;
            if(sex[0] == 49) {
                var45 = "男".getBytes("Unicode");
            } else if(sex[0] == 50) {
                var45 = "女".getBytes("Unicode");
            } else {
                var45 = "其他".getBytes("Unicode");
            }

            System.arraycopy(var45, 0, pSex, 0, var45.length);
            byte[] nation = new byte[4];
            byte[] nationasc = new byte[2];
            Object nationout = null;
            System.arraycopy(Response1, 32, nation, 0, nation.length);

            int na;
            for(na = 0; na < nationasc.length; ++na) {
                nationasc[na] = nation[2 * na];
            }

            na = Integer.parseInt(new String(nationasc));
            String m_nation = null;
            switch(na) {
                case 1:
                    m_nation = "汉";
                    break;
                case 2:
                    m_nation = "蒙古";
                    break;
                case 3:
                    m_nation = "回";
                    break;
                case 4:
                    m_nation = "藏";
                    break;
                case 5:
                    m_nation = "维吾尔";
                    break;
                case 6:
                    m_nation = "苗";
                    break;
                case 7:
                    m_nation = "彝";
                    break;
                case 8:
                    m_nation = "壮";
                    break;
                case 9:
                    m_nation = "布依";
                    break;
                case 10:
                    m_nation = "朝鲜";
                    break;
                case 11:
                    m_nation = "满";
                    break;
                case 12:
                    m_nation = "侗";
                    break;
                case 13:
                    m_nation = "瑶";
                    break;
                case 14:
                    m_nation = "白";
                    break;
                case 15:
                    m_nation = "土家";
                    break;
                case 16:
                    m_nation = "哈尼";
                    break;
                case 17:
                    m_nation = "哈萨克";
                    break;
                case 18:
                    m_nation = "傣";
                    break;
                case 19:
                    m_nation = "黎";
                    break;
                case 20:
                    m_nation = "傈僳";
                    break;
                case 21:
                    m_nation = "佤";
                    break;
                case 22:
                    m_nation = "畲";
                    break;
                case 23:
                    m_nation = "高山";
                    break;
                case 24:
                    m_nation = "拉祜";
                    break;
                case 25:
                    m_nation = "水";
                    break;
                case 26:
                    m_nation = "东乡";
                    break;
                case 27:
                    m_nation = "纳西";
                    break;
                case 28:
                    m_nation = "景颇";
                    break;
                case 29:
                    m_nation = "柯尔克孜";
                    break;
                case 30:
                    m_nation = "土";
                    break;
                case 31:
                    m_nation = "达斡尔";
                    break;
                case 32:
                    m_nation = "仫佬";
                    break;
                case 33:
                    m_nation = "羌";
                    break;
                case 34:
                    m_nation = "布朗";
                    break;
                case 35:
                    m_nation = "撒拉";
                    break;
                case 36:
                    m_nation = "毛南";
                    break;
                case 37:
                    m_nation = "仡佬";
                    break;
                case 38:
                    m_nation = "锡伯";
                    break;
                case 39:
                    m_nation = "阿昌";
                    break;
                case 40:
                    m_nation = "普米";
                    break;
                case 41:
                    m_nation = "塔吉克";
                    break;
                case 42:
                    m_nation = "怒";
                    break;
                case 43:
                    m_nation = "乌孜别克";
                    break;
                case 44:
                    m_nation = "俄罗斯";
                    break;
                case 45:
                    m_nation = "鄂温克";
                    break;
                case 46:
                    m_nation = "德昂";
                    break;
                case 47:
                    m_nation = "保安";
                    break;
                case 48:
                    m_nation = "裕固";
                    break;
                case 49:
                    m_nation = "京";
                    break;
                case 50:
                    m_nation = "塔塔尔";
                    break;
                case 51:
                    m_nation = "独龙";
                    break;
                case 52:
                    m_nation = "鄂伦春";
                    break;
                case 53:
                    m_nation = "赫哲";
                    break;
                case 54:
                    m_nation = "门巴";
                    break;
                case 55:
                    m_nation = "珞巴";
                    break;
                case 56:
                    m_nation = "基诺";
                    break;
                default:
                    m_nation = "其他";
            }

            byte[] var46 = m_nation.getBytes("Unicode");
            System.arraycopy(var46, 0, pNation, 0, var46.length);
            byte[] birth = new byte[16];
            byte[] birthasc = new byte[8];
            Object birthout = null;
            System.arraycopy(Response1, 36, birth, 0, birth.length);

            for(int address = 0; address < birthasc.length; ++address) {
                birthasc[address] = birth[2 * address];
            }

            byte[] var47 = (new String(birthasc)).getBytes("Unicode");
            System.arraycopy(var47, 0, pBirth, 0, var47.length);
            byte[] var48 = new byte[72];
            var48[0] = -1;
            var48[1] = -2;
            System.arraycopy(Response1, 52, var48, 2, var48.length - 2);
            System.arraycopy(var48, 0, pAddress, 0, var48.length);
            byte[] IDNo = new byte[36];
            byte[] IDNoasc = new byte[18];
            Object IDNoout = null;
            System.arraycopy(Response1, 122, IDNo, 0, IDNo.length);

            for(int Department = 0; Department < IDNoasc.length; ++Department) {
                IDNoasc[Department] = IDNo[2 * Department];
            }

            byte[] var49 = (new String(IDNoasc)).getBytes("Unicode");
            System.arraycopy(var49, 0, pIDNo, 0, var49.length);
            byte[] var50 = new byte[32];
            var50[0] = -1;
            var50[1] = -2;
            System.arraycopy(Response1, 158, var50, 2, var50.length - 2);
            System.arraycopy(var50, 0, pDepartment, 0, var50.length);
            byte[] EffectDate = new byte[16];
            byte[] EffectDateasc = new byte[8];
            Object EffectDateout = null;
            System.arraycopy(Response1, 188, EffectDate, 0, EffectDate.length);

            for(int ExpireDate = 0; ExpireDate < EffectDateasc.length; ++ExpireDate) {
                EffectDateasc[ExpireDate] = EffectDate[2 * ExpireDate];
            }

            byte[] var51 = (new String(EffectDateasc)).getBytes("Unicode");
            System.arraycopy(var51, 0, pEffectDate, 0, var51.length);
            byte[] var52 = new byte[16];
            byte[] ExpireDateasc = new byte[8];
            Object ExpireDateout = null;
            byte[] ExpireDatecq = new byte[18];
            ExpireDatecq[0] = -1;
            ExpireDatecq[1] = -2;
            System.arraycopy(Response1, 204, var52, 0, var52.length);
            if(var52[1] != 0) {
                System.arraycopy(var52, 0, ExpireDatecq, 2, var52.length);
                System.arraycopy(ExpireDatecq, 0, pExpireDate, 0, ExpireDatecq.length);
            } else {
                for(int tupian = 0; tupian < ExpireDateasc.length; ++tupian) {
                    ExpireDateasc[tupian] = var52[2 * tupian];
                }

                byte[] var53 = (new String(ExpireDateasc)).getBytes("Unicode");
                System.arraycopy(var53, 0, pExpireDate, 0, var53.length);
            }

            byte[] var54 = new byte[1025];
            Object tupianShow = null;
            Arrays.fill(var54, (byte)0);
            System.arraycopy(Response1, 256, var54, 0, 1024);
            byte[] var55 = this.HdosIdUnpack(var54, pkName);
            if(var55 != null) {
                byte tmp;
                for(int i = 0; i < 19278; ++i) {
                    tmp = var55[i];
                    var55[i] = var55['際' - i];
                    var55['際' - i] = tmp;
                }

                for(int row = 0; row < 126; ++row) {
                    for(int col = 0; col < 153; ++col) {
                        tmp = var55[col + row * 102 * 3];
                        var55[col + row * 102 * 3] = var55[305 - col + row * 102 * 3];
                        var55[305 - col + row * 102 * 3] = tmp;
                    }
                }

                System.arraycopy(var55, 0, pBmpFile, 0, '障');
            }

            return ret;
        }
    }

    public int[] convertByteToColor(byte[] data) {
        int size = data.length;
        if(size == 0) {
            return null;
        } else {
            byte arg = 0;
            if(size % 3 != 0) {
                arg = 1;
            }

            int[] color = new int[size / 3 + arg];
            int i;
            if(arg == 0) {
                for(i = 0; i < color.length; ++i) {
                    color[i] = (data[i * 3] << 16 & 16711680 | data[i * 3 + 1] << 8 & '\uff00' | data[i * 3 + 2] & 255 | -16777216);
                }
            } else {
                for(i = 0; i < color.length - 1; ++i) {
                    color[i] =(data[i * 3] << 16 & 16711680 | data[i * 3 + 1] << 8 & '\uff00' | data[i * 3 + 2] & 255 | -16777216);
                }

                color[color.length - 1] = (byte) -16777216;
            }

            return color;
        }
    }

    public static String byteToString(byte b) {
        byte maskHigh = -16;
        byte maskLow = 15;
        byte high = (byte)((b & maskHigh) >> 4);
        byte low = (byte)(b & maskLow);
        StringBuffer buf = new StringBuffer();
        buf.append(findHex(high));
        buf.append(findHex(low));
        return buf.toString();
    }

    private static char findHex(byte b) {
        int t = (new Byte(b)).intValue();
        t = t < 0?t + 16:t;
        return t >= 0 && t <= 9?(char)(t + 48):(char)(t - 10 + 65);
    }

    public byte chartoint(byte c) {
        switch(c) {
            case 48:
                return (byte)0;
            case 49:
                return (byte)1;
            case 50:
                return (byte)2;
            case 51:
                return (byte)3;
            case 52:
                return (byte)4;
            case 53:
                return (byte)5;
            case 54:
                return (byte)6;
            case 55:
                return (byte)7;
            case 56:
                return (byte)8;
            case 57:
                return (byte)9;
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
            case 95:
            case 96:
            default:
                return (byte)0;
            case 65:
            case 97:
                return (byte)10;
            case 66:
            case 98:
                return (byte)11;
            case 67:
            case 99:
                return (byte)12;
            case 68:
            case 100:
                return (byte)13;
            case 69:
            case 101:
                return (byte)14;
            case 70:
            case 102:
                return (byte)15;
        }
    }

    public byte[] strToHex(String indata, int len) {
        byte[] result = new byte[len];
        byte[] buf = indata.getBytes();

        for(int i = 0; i < len; ++i) {
            result[i] = (byte)((this.chartoint(buf[i * 2]) << 4) + this.chartoint(buf[i * 2 + 1]));
        }

        return result;
    }
}
