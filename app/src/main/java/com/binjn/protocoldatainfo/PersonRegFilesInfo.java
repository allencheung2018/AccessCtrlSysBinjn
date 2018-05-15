package com.binjn.protocoldatainfo;

import java.util.List;

/**
 * Created by xiaomi2 on 2018/4/10 0010.
 */

public class PersonRegFilesInfo {
    private String pid;
    private int picType;
    private String featFileName;
    private List<String> featFilePaths;
    private String faceFileName;
    private List<String> faceFilePaths;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public int getPicType() {
        return picType;
    }

    public void setPicType(int picType) {
        this.picType = picType;
    }

    public String getFeatFileName() {
        return featFileName;
    }

    public void setFeatFileName(String featFileName) {
        this.featFileName = featFileName;
    }

    public List<String> getFeatFilePaths() {
        return featFilePaths;
    }

    public void setFeatFilePaths(List<String> featFilePaths) {
        this.featFilePaths = featFilePaths;
    }

    public String getFaceFileName() {
        return faceFileName;
    }

    public void setFaceFileName(String faceFileName) {
        this.faceFileName = faceFileName;
    }

    public List<String> getFaceFilePaths() {
        return faceFilePaths;
    }

    public void setFaceFilePaths(List<String> faceFilePaths) {
        this.faceFilePaths = faceFilePaths;
    }
}
