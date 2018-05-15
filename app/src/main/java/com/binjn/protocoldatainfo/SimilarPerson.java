package com.binjn.protocoldatainfo;


/**
 * Created by xiaomi2 on 2018/4/3 0003.
 */

public class SimilarPerson implements Comparable<SimilarPerson> {
    private String PersonId;
    private String faceId;
    private float score;

    public SimilarPerson(){

    }
    public SimilarPerson(String pid, String fid, float s){
        PersonId = pid;
        faceId = fid;
        score = s;
    }

    public String getPersonId() {
        return PersonId;
    }

    public void setPersonId(String personId) {
        PersonId = personId;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public int compareTo(SimilarPerson similarPerson) {
        if (score < similarPerson.getScore()) {
            return 1;
        }
        return -1;
    }
}
