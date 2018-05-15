package com.binjn.protocoldatainfo;

/**
 * Created by xiaomi2 on 2018/1/2 0002.
 */

public class ManageablePersonInfo {
    private String pid;
    private String name;
    private String idNo;
    private String hashcode;
    private String hashcodeNir;
    private boolean visibleFaceRegistered;
    private boolean nirFaceRegistered;

    public ManageablePersonInfo(){

    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public String getIdNo() {
        return idNo;
    }

    public String getHashcode() {
        return hashcode;
    }

    public boolean isVisibleFaceRegistered() {
        return visibleFaceRegistered;
    }

    public boolean isNirFaceRegistered() {
        return nirFaceRegistered;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public void setHashcode(String hashcode) {
        this.hashcode = hashcode;
    }

    public void setVisibleFaceRegistered(boolean visibleFaceRegistered) {
        this.visibleFaceRegistered = visibleFaceRegistered;
    }

    public void setNirFaceRegistered(boolean nirFaceRegistered) {
        this.nirFaceRegistered = nirFaceRegistered;
    }

    public String getHashcodeNir() {
        return hashcodeNir;
    }

    public void setHashcodeNir(String hashcodeNir) {
        this.hashcodeNir = hashcodeNir;
    }
}
