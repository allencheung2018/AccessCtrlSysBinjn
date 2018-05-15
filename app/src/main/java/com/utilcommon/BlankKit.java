package com.utilcommon;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author gaoxiang
 * @date 2017/8/14 14:33
 * @description 判断字符串、集合、哈希、数组对象是否为空
 */
public class BlankKit {
    /**
     * 判断byte[]是否为空
     * @param   bytes
     * @return
     */
    public static boolean isBlank(final byte[] bytes){
        if(bytes == null || bytes.length<=0)
            return true;
        else
            return false;
    }
    /**
     * 判断byte[]是否不为空
     * @param bytes
     * @return
     */
    public static boolean notBlank(final byte[] bytes){
        return !isBlank(bytes);
    }

    /**
     * 判断FASTJSON的json对象是否为空
     * @param   json
     * @return
     */
    public static boolean isBlank(final JSONObject json){
        if(json == null)// || json.isEmpty())
            return true;
        else
            return false;
    }
    /**
     * 判断FASTJSON的json对象是否不为空
     * @param json
     * @return
     */
    public static boolean notBlank(final JSONObject json){
        return !isBlank(json);
    }

    /**
     * 判断FASTJSON的json对象是否为空
     * @param   json
     * @return
     */
    public static boolean isBlank(final JSONArray json){
        if(json == null)// || json.isEmpty())
            return true;
        else
            return false;
    }
    /**
     * 判断FASTJSON的json对象是否不为空
     * @param json
     * @return
     */
    public static boolean notBlank(final JSONArray json){
        return !isBlank(json);
    }

    /**
     * 判断字符串是否为空
     */
    public static boolean isBlank(final String str) {

        if (str == null) {
            return true;
        }
        int len = str.length();
        if (len <= 0) {
            return true;
        }
        for (int i = 0; i < len; i++) {
            switch (str.charAt(i)) {
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                // case '\b':
                // case '\f':
                break;
                default:
                    return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串不为空
     * @param str
     * @return
     */
    public static boolean notBlank(final String str){
        return !isBlank(str);
    }

    /**
     * 判断字符是否为空
     *
     * @param cha
     * @return
     */
    public static boolean isBlank(final Character cha) {
        return (cha == null) || cha.equals(' ');
    }

    /**
     * 判断字符不为空
     * @param cha
     * @return
     */
    public static boolean notBlank(final Character cha){ return !isBlank(cha);}

    /**
     * 判断对象是否为空
     */
    public static boolean isBlank(final Object obj) {
        return (obj == null);
    }

    /**
     * 判断对象不为空
     * @param obj
     * @return
     */
    public static boolean notBlank(final Object obj) { return !isBlank(obj);}

    /**
     * 判断数组是否为空
     *
     * @param objs
     * @return
     */
    public static boolean isBlank(final Object[] objs) {
        return (objs == null) || (objs.length <= 0);
    }

    /**
     * 判断对数组不为空
     * @param objs
     * @return
     */
    public static boolean notBlank(final Object[] objs) { return !isBlank(objs);}
    /**
     * 判断Collection是否为空
     *
     * @param obj
     * @return
     */
    public static boolean isBlank(final Collection<?> obj) {
        return (obj == null) || (obj.size() <= 0);
    }

    /**
     * 判断Collection是否不为空
     * @param obj
     * @return
     */
    public static boolean notBlank(final Collection<?> obj) { return !isBlank(obj);}
    /**
     * 判断Set是否为空
     *
     * @param obj
     * @return
     */
    public static boolean isBlank(final Set<?> obj) {
        return (obj == null) || (obj.size() <= 0);
    }

    /**
     * 判断Set不为空
     * @param obj
     * @return
     */
    public static boolean notBlank(final Set<?> obj) {
        return !isBlank(obj);
    }

    /**
     * 判断Integer是否为空
     * @param i
     * @return
     */
    public static boolean isBlank(Integer i) {
        return i == null || i < 1;
    }

    /**
     * 判断Integer不为空
     * @param i
     * @return
     */
    public static boolean notBlank(Integer i) {
        return !isBlank(i);
    }

    /**
     * 判断Serializable是否为空
     *
     * @param obj
     * @return
     */
    public static boolean isBlank(final Serializable obj) {
        return obj == null;
    }

    /**
     * 判断Serializable不为空
     * @param obj
     * @return
     */
    public static boolean notBlank(final Serializable obj) {
        return !isBlank(obj);
    }
    /**
     * 判断Map是否为空
     *
     * @param obj
     * @return
     */
    public static boolean isBlank(final Map<?, ?> obj) {
        return (obj == null) || (obj.size() <= 0);
    }

    /**
     * 判断Map不为空
     * @param obj
     * @return
     */
    public static boolean notBlank(final Map<?, ?> obj) {
        return !isBlank(obj);
    }
}
