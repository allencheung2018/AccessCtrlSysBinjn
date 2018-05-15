package com.binjn.faceimgresovle;

import android.graphics.ImageFormat;
import android.util.Log;


import com.utilcommon.BlankKit;

import FaceAlg.FaceAlgSvrPrx;
import FaceAlg.FaceAlgSvrPrxHelper;
import FaceAlg.FaceAlgType;
import FaceAlg.FaceInfo;
import FaceAlg.Image;
import Ice.Communicator;
import Ice.ConnectTimeoutException;
import Ice.ObjectNotExistException;
import Ice.ObjectPrx;
import Ice.Util;

/**
 * Created by admin on 2018/4/9.
 */

public class FaceImp {
    private String IceLocator = "FaceAlgSvr:default -h 111.230.249.92 -p 13000";
    private int IceInvocationTimeout = 5000;
    private FaceAlgSvrPrx faceAlgSvrPrx;

    public FaceInfo[] detectFace(int faceType, byte[] imgBytes){

        if (BlankKit.notBlank(imgBytes) ) {
            Image image = new Image();
            image.data = imgBytes;

            FaceAlgSvrPrx faceAlgSvrPrx = getServicePrx();
            FaceInfo[] faceInfos = null;

            faceInfos = faceAlgSvrPrx.DetectNIR(image);

            if (BlankKit.notBlank(faceInfos))
                return faceInfos;
            else
                Log.e("FaceImp", "There is no face in image!");
        }

        return null;
    }

//    public FaceInfo[] faceDect(byte[] imgBytes, String fileExtension){
//        try {
//            Image image = new Image();
//            image.fmt = getImgFmtByFileExtension(fileExtension);
//            if(!BlankKit.isBlank(image.fmt)){
//                image.data = imgBytes;
//                if(faceAlgSvrPrx == null){
//                    faceAlgSvrPrx = getServicePrx();
//                }
//                FaceInfo[] faceInfos = faceAlgSvrPrx.DetectNIR(image);
//                if(BlankKit.notBlank(faceInfos)) {
//                    return faceInfos;
//                }
//            }
//        }catch (Exception e){
//            //Ice.ConnectFailedException
//            Log.e("tag","远程人脸检测错误信息："+ e.toString());
//        }
//        return null;
//    }

    /**
     * 人脸检测（不含特征5点检测及特征数据值提取）
     * @param imgBytes
     * @param fileExtension
     * @return
     */
//    public FaceInfo[] DetectSimpleNIR(byte[] imgBytes,String fileExtension){
//        Image image = new Image();
//        image.fmt = getImgFmtByFileExtension(fileExtension);
//        if(!BlankKit.isBlank(image.fmt)){
//            image.data = imgBytes;
//            if(faceAlgSvrPrx == null){
//                faceAlgSvrPrx = getServicePrx();
//            }
//            FaceInfo[] faceInfos = faceAlgSvrPrx.DetectSimpleNIR(image);
//            if(BlankKit.notBlank(faceInfos))
//                return faceInfos;
//        }
//        return null;
//    }

    /**
     * 匹配两张人脸图像，返回最相似的两张人脸的相似度
     * @param imgBytes1
     * @param imgBytes2
     * @param fileExtension
     * @return
     */
//    public float MatchImageNIR(byte[] imgBytes1,byte[] imgBytes2,String fileExtension){
//        Image image = new Image();
//        Image image2 = new Image();
//        image.fmt = getImgFmtByFileExtension(fileExtension);
//        image2.fmt = getImgFmtByFileExtension(fileExtension);
//        if(!BlankKit.isBlank(image.fmt)){
//            image.data = imgBytes1;
//            image2.data = imgBytes2;
//            if(faceAlgSvrPrx == null){
//                faceAlgSvrPrx = getServicePrx();
//            }
//            float v = faceAlgSvrPrx.MatchImageNIR(image, image2);
//            return v;
//        }
//        return 0;
//    }


    /**
     * 匹配两张人脸的特征数据，返回相似度
     * @param fea1
     * @param fea2
     * @return
     */
    public float MatchFeatureNIR(float[] fea1, float[] fea2){
        if(faceAlgSvrPrx == null){
            faceAlgSvrPrx = getServicePrx();
        }
        float sim = faceAlgSvrPrx.MatchFeaturesNIR(fea1,fea2);
        return sim;
    }

    /**
     * 连接识别服务获取代理类
     * @return  人脸识别服务代理
     */
    private FaceAlgSvrPrx getServicePrx(){
        try {
            Communicator ic = Util.initialize();
            ObjectPrx base = ic.stringToProxy(IceLocator);
            base.ice_invocationTimeout(IceInvocationTimeout);
            FaceAlgSvrPrx faceAlgSvrPrx = FaceAlgSvrPrxHelper.checkedCast(base);
            //Assert.isNull(faceAlgSvrPrx, "Invalid ice proxy!");
            return faceAlgSvrPrx;
        } catch (Exception e){
            Log.e("tag","代理连接对象返回错误信息!"+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据文件后缀名获取识别服务对应图片类型
     * @param fileExtension    文件后缀名
     * @return
     */
//    private ImageFormat getImgFmtByFileExtension(String fileExtension){
//        if(BlankKit.notBlank(fileExtension)){
//            fileExtension = fileExtension.trim().toLowerCase();
//            if(fileExtension.equals("jpg"))return ImageFormat.JPG;
//            if(fileExtension.equals("bmp"))return ImageFormat.BMP;
//            if(fileExtension.equals("png"))return ImageFormat.PNG;
//        }
//        return null;
//    }
}
