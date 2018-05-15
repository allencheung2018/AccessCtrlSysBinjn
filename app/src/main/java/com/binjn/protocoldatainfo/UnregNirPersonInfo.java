package com.binjn.protocoldatainfo;

/**
 * Created by xiaomi2 on 2017/12/26 0026.
 */

public class UnregNirPersonInfo {
    private String id;
    private String idNo;
    private String name;
    private int sex;
    private int nation;
    private int education;
    private int type;
    private String birthday;
    private String birthPlace;
    private String mobile;
    private String occupation;
    private String address;
    private String employer;
    private int priority;
    private String description;
    private String icbcOpenId;
    private int createPersonId;
    private String createTime;
    private boolean visibleFaceRegistered;
    private boolean nirFaceRegistered;
    private FacesInfo[] faces;
    private FeaturesInfo[] features;
    private OrgansInfo[] organs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
