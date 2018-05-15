package com.binjn.faceimgresovle;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Process;
import android.util.Base64;
import android.util.Log;

import com.application.MainApplication;
import com.binjn.DataBase.FeatDatabaseHelper;
import com.binjn.accessctrlsysbinjn.MainActivity;
import com.binjn.clienthttp.ComBusiness;
import com.binjn.protocoldatainfo.PersonRegFilesInfo;
import com.binjn.protocoldatainfo.SimilarPerson;
import com.binjn.protocoldatainfo.UnregNirPersonInfo;
import com.seetatech.toolchainv3.ToolChain;
import com.utilcommon.LogFile;
import com.utilcommon.UtilCommon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import FaceAlg.FaceAlgSvrPrx;
import FaceAlg.FaceAlgSvrPrxHelper;
import FaceAlg.FaceInfo;
import FaceAlg.FaceInfoEx;
import FaceAlg.Image;
import FaceAlg.RecogResult;
import Ice.Communicator;
import Ice.ObjectPrx;
import Ice.Util;
import anet.channel.c;
import vipl.ict.cn.facedetector500jni.VIPLFaceDetectorUtils;
import vipl.ict.cn.facerecognizerNIR402.VIPLFaceRecognizerNIRUtils;
import vipl.ict.cn.pointdetector403jni.VIPLPointDetectorUtils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import static java.nio.file.Files.copy;

/**
 * Created by xiaomi2 on 2017/12/1 0001.
 */

public class FaceDetRec {
    //static
    private static String mTAG = "FaceDetRec";
    public static String dirPhoto = MainActivity.mainDir + "Photo/";
    public static final String dirFeatData = MainActivity.picDir + "FeatureData/";
    public static final int PICTYPENIR = 10121;
    public static final int PICTYPEVIS = 10122;
    public static final String FeatureVersion = "2.0";
    private static int indexPhoto = 1;
    private static enum enumFlow{IDCARDFlow,REGISTERFlow,RECFlow};
    private static final int numLostPerson = 12;
    //class
    private VIPLFaceDetectorUtils viplFDU;
    private VIPLPointDetectorUtils viplPDU;
    private VIPLFaceRecognizerNIRUtils viplFRNU;
    private ToolChain toolChain;
    private OnFaceDetRecCallBack onFaceDetRecCallBack;
    private ComBusiness comBusiness;
    private FeatDatabaseHelper fDbHelper;
    private SQLiteDatabase featDb;
    private Context context;
    private UnregNirPersonInfo unregNirPersonInfo;
    private FaceAlgSvrPrx faceAlgSvrPrx;
    ExecutorService managePhotoData = Executors.newSingleThreadExecutor();
    ExecutorService savePhotoDataNir = Executors.newSingleThreadExecutor();
    //var
    private String modelPath;
    private String pointdetector_modelPath;
    private String faceRec_modelPath;
    private Queue<byte[]> queue = new LinkedList<byte[]>();
    boolean flagWork = true;
    private int timesLostShadow = 0;                //人脸连续丢失次数
    private int numPicFace = 0;                     //照片数量
    private Bitmap picID;
    private Bitmap picStranger=null;
    private String fpStrangerPic = null;            //最新陌生人脸照片路径
    private String fpStrangerPicNir = null;         //最新陌生人脸照片路径
    private String fpTempPic = null;                //最新临时照片路径
    private String strLog = "";
    private float featIDCard[], featStranger[], featRegister[];
    private boolean isIdCardNumReg = false, isLoop = true;
    private String nameIdCard, numIdCard, personId;
    private enumFlow indexFLow = enumFlow.RECFlow;
    private int indexPicRegister = 1;
    private List<float[]> regFeatList = new ArrayList<>();
    private List<float[]> personFeatList = new ArrayList<>();
    private List<String> featFilePaths = new ArrayList<>();             //特征值文件路径
    private List<String> faceFilePaths = new ArrayList<>();             //人脸照片文件路径
    private float simIDCard = 0;
    private boolean permitedReg = false;                                //是否允许注册
    private boolean hasFace = false;
    private int featSize, numRegisterPhoto;
    private float matchScorePass, roll, pitch, yaw;
    private int faceImageSize;
    private byte[] picNirData;

    public FaceDetRec(Context context, float msp, int fis, float r, float p, float y){
        strLog = "FaceDetRec create.";
        Log.i(mTAG, strLog);
        LogFile.getInstance().saveMessage(strLog);
        this.context = context;
        matchScorePass = msp;
        faceImageSize = fis;
        roll = r;
        pitch = p;
        yaw = y;
        initData();
        initControl();
    }

    public void initData(){
//        faceAlgSvrPrx = getServicePrx();
        viplFDU = MainActivity.viplFaceDetectorUtils;
        viplPDU = MainActivity.viplPointDetectorUtils;
        viplFRNU = MainActivity.viplFaceRecognizerUtils;
//        toolChain = MainActivity.toolChain;
        featSize = 1024;//MainActivity.feat_size;
        numRegisterPhoto = MainActivity.numRegisterPhoto;
        featIDCard = new float[featSize];
        featStranger = new float[featSize];
        featRegister = new float[featSize*numRegisterPhoto];
        comBusiness = new ComBusiness(context);

        File file = new File(dirFeatData);
        if (!file.exists()){
            file.mkdirs();
        }
        fDbHelper = new FeatDatabaseHelper(context);
        unregNirPersonInfo = new UnregNirPersonInfo();
    }

    public void initControl(){
        //detThread.start();
    }

    private String IceLocator = "FaceAlgSvr:default -h 111.230.249.92 -p 13000";
    private int IceInvocationTimeout = 5000;

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
            strLog = "FaceAlgSvrPrx" + " 代理连接对象返回错误信息!"+e.toString();
            Log.e("tag", strLog);
            LogFile.getInstance().saveMessage(strLog);
            e.printStackTrace();
        }
        return null;
    }

    public void renewServicePrx(){
        faceAlgSvrPrx = getServicePrx();
        Log.d(mTAG, "renewServicePrx:"+faceAlgSvrPrx);
    }

    public void setPhotoDataInQueue(byte[] data){
        Log.i(mTAG, "get photo data:"+data.length + " queue:"+queue.size());
        synchronized (queue){
            queue.offer(data);
        }
    }
    //
    private BitmapFactory.Options opts = new BitmapFactory.Options();

    public void recognizerFace(final byte[] jpegData, final int picWidth, final int picHeight){
        managePhotoData.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(mTAG, "recognizerFace - entre");
                String str;
                numPicFace += 1;
                timesLostShadow += 1;
                picStranger = null;
//                Bitmap bitmaptmp=null, bitmap=null;
                Image image = null;
                hasFace = false;
                try {
//                    bitmaptmp = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
//                    fpTempPic = saveStrangerTempPhoto(bitmaptmp);
                    fpTempPic = saveStrangerTempPhoto(jpegData, "");
                    onFaceDetRecCallBack.onPhotoSaved(numPicFace);
//                    bitmap = BitmapFactory.decodeFile(fpTempPic);
//                    Bitmap bmp_src = bitmap.copy(Bitmap.Config.ARGB_8888, true); // true is RGBA
//                    ByteBuffer buffer = ByteBuffer.allocate(bmp_src.getByteCount());
//                    bmp_src.copyPixelsToBuffer(buffer);
//                    byte[] argb = buffer.array();
                    image = new Image();
//                    image.data = UtilCommon.readFeatureFileInfos(fpTempPic);
                    image.data = jpegData;
                }catch (Exception e){
                    str = "recognizerFace bitmap error:"+e.toString();
                    Log.d(mTAG, str);
                    LogFile.getInstance().saveMessage(str);

                }

                Log.d(mTAG, "recognizerFace - entre1");

                FaceInfo[] faceInfos = null;
                int selected = 0, area = 0;
                if(MainActivity.isNetwork) {
                    try {
                        faceInfos = faceAlgSvrPrx.Detect(image);
                        if (faceInfos != null && faceInfos.length > 0) {
                            hasFace = true;
                            for (int i = 0; i < faceInfos.length; i++) {
                                if (faceInfos[i].height * faceInfos[i].width > area) {
                                    area = faceInfos[i].height * faceInfos[i].width;
                                    selected = i;
                                }
                            }
                            str = "faceInfos:" + faceInfos.length + " selected:" + selected
                                    + " x=" + faceInfos[selected].x + " y=" + faceInfos[selected].y
                                    + " width=" + faceInfos[selected].width + " height=" + faceInfos[selected].height
                                    + " roll=" + faceInfos[selected].roll + " pitch=" + faceInfos[selected].pitch
                                    + " yaw=" + faceInfos[selected].yaw;
                            Log.i(mTAG, str);
                            LogFile.getInstance().saveMessage(str);
                        }
                    } catch (Exception e) {
                        Log.i(mTAG, "faceAlgSvrPrx.Detect:" + e.toString());
                        LogFile.getInstance().saveMessage("faceAlgSvrPrx.Detect error:" + e.toString());
                        renewServicePrx();
                    }
                }
                if (permitedReg) {
                    permitedReg = false;
                    featFilePaths.clear();
                    faceFilePaths.clear();
                    indexPicRegister = 1;
                    indexFLow = enumFlow.REGISTERFlow;
                }

                str = "Flow:"+indexFLow + " Pics="+numPicFace + " Lost="+timesLostShadow + " feature:"+hasFace
                        + " tid:"+ Process.myTid();
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                if (hasFace){
                    timesLostShadow = 0;
//                    fpStrangerPic = saveFacePhoto(bitmaptmp, "");
                    fpStrangerPic = saveFacePhoto(jpegData, "");
//                    Cursor cursor = featDb.rawQuery("select * from " + FeatDatabaseHelper.TABLE_NAME
//                            + " order by "+ FeatDatabaseHelper.ENTER_COUNT + " desc", null);
                    String dir = "";
                    List<SimilarPerson> simPers = new ArrayList<>();
                    isLoop = true;
                    while (isLoop){
                        switch (indexFLow){
                            case IDCARDFlow:
                                Cursor csr = featDb.rawQuery("select * from " + FeatDatabaseHelper.TABLE_NAME
                                        + " where " + FeatDatabaseHelper.IDCARD_NO + " = ? " ,new String[]{numIdCard});
                                boolean b = csr.moveToNext();
                                synchronized (toolChain) {
                                    simIDCard = toolChain.CalcSimilarity(featStranger, featIDCard);
                                }
                                str = "身份证照片相似度："+simIDCard + " 本地数据库是否存在："+b;

                                if (simIDCard>=-1.0 && simIDCard<1.0){
                                    indexFLow = enumFlow.REGISTERFlow;
                                    str = str + " 进入注册流程";
                                }
                                else {
                                    indexFLow = enumFlow.RECFlow;
                                    str = str + " 进入识别流程";
                                }
                                Log.i(mTAG, str);
                                LogFile.getInstance().saveMessage(str);
                                isIdCardNumReg = false;
                                break;
                            case REGISTERFlow:
                                isLoop = false;
                                str = "注册照片：" + indexPicRegister + " width="+faceInfos[selected].width
                                        + " height="+faceInfos[selected].height + " roll="+faceInfos[selected].roll
                                        + " pitch="+faceInfos[selected].pitch + " yaw="+faceInfos[selected].yaw;
                                Log.d(mTAG, str);
                                LogFile.getInstance().saveMessage(str);
                                if (faceInfos[selected].width<faceImageSize || faceInfos[selected].height<faceImageSize
                                        || Math.abs(faceInfos[selected].roll)>roll || Math.abs(faceInfos[selected].pitch)>pitch
                                        || Math.abs(faceInfos[selected].yaw)>yaw
                                        || faceInfos[selected].x<5 || faceInfos[selected].y<5
                                        || (faceInfos[selected].x+faceInfos[selected].width)>picWidth
                                        || (faceInfos[selected].y+faceInfos[selected].height)>picHeight){
                                    Log.d(mTAG, "照片不符合注册要求");
                                    LogFile.getInstance().saveMessage("照片不符合注册要求");
                                    onFaceDetRecCallBack.onRegistering(indexPicRegister);
                                    break;
                                }
                                dir = dirFeatData + personId + "/";
                                File file = new File(dir);
                                if (!file.exists()) {
                                    file.mkdirs();
                                }
                                String timeStampCurrent = UtilCommon.getTimeStampCurrent();
                                String pPath = dir + "Reg" + timeStampCurrent + indexPicRegister + ".jpg";
//                                UtilCommon.savePhoto(pPath, bitmaptmp);
                                UtilCommon.saveByteFile(pPath, jpegData);
                                faceFilePaths.add(pPath);
                                String fdPath = dir + "Reg" + timeStampCurrent + indexPicRegister + ".feat";
                                UtilCommon.saveFeatureFile(String.valueOf(indexPicRegister), personId,
                                        FeatureVersion, UtilCommon.getTimeCurrent(), 1, featSize,
                                        faceInfos[selected].features, "", fdPath);
//                                System.arraycopy(featStranger, 0, featRegister, (indexPicRegister-1)*featSize, featSize);
                                System.arraycopy(faceInfos[selected].features, 0, featRegister, (indexPicRegister-1)*featSize, featSize);
                                featFilePaths.add(fdPath);
                                if (indexPicRegister == MainActivity.numRegisterPhoto){
                                    str = personId + " 采集完成";
                                    Log.d(mTAG, str);
                                    LogFile.getInstance().saveMessage(str);
                                    dir = dir + personId + ".feat";
                                    UtilCommon.updateLocalFeatureFile(personId, featRegister, dir);
                                    addRegisterPersontoLoaclDatabase(featFilePaths, faceFilePaths);
                                    onFaceDetRecCallBack.onFinishRegister(setPerRegInfos(personId, featFilePaths, faceFilePaths));
                                    indexFLow = enumFlow.RECFlow;
                                }else {
                                    onFaceDetRecCallBack.onRegistering(indexPicRegister);
                                }
                                indexPicRegister += 1;
                                break;
                            case RECFlow:
                                Log.d(mTAG, "RECFlow - entre");
                                if (faceAlgSvrPrx != null) {
                                    try {
                                        String faceSetId = ComBusiness.idMac + 10122;
                                        Log.d(mTAG, faceSetId + " len=" + faceAlgSvrPrx.GetFaceCount(faceSetId));
                                        RecogResult[] recogResults = faceAlgSvrPrx.RecognizeFeatures(faceSetId,
                                                faceInfos[selected].features, 3);
                                        if (recogResults != null && recogResults.length > 0) {
                                            str = "";
                                            for (int i = 0; i < recogResults.length; i++) {
                                                str = str + " sim-" + i + ":" + recogResults[i].similarity;
                                            }
                                            str = "RecogResult:" + recogResults.length + str;
                                            Log.d(mTAG, str);
                                            LogFile.getInstance().saveMessage(str);
                                            simPers.add(new SimilarPerson("0", recogResults[0].faceId, recogResults[0].similarity));
                                            simPers.add(new SimilarPerson("0", recogResults[1].faceId, recogResults[1].similarity));
                                            simPers.add(new SimilarPerson("0", recogResults[2].faceId, recogResults[2].similarity));
                                            if (recogResults[0].similarity > matchScorePass) {
                                                if (picNirData != null) {
                                                    Bitmap bitmap1 = BitmapFactory.decodeByteArray(picNirData, 0, picNirData.length, opts);
                                                    fpStrangerPicNir = saveFacePhoto(bitmap1, "nir");
                                                }
                                                onFaceDetRecCallBack.onSuccessRecognizerFace("0", recogResults[0].similarity);
                                            }
                                            onFaceDetRecCallBack.onFinishRecognizerFace(simPers, fpStrangerPic,
                                                    recogResults[0].similarity);
                                        } else {
                                            str = "识别失败 RecogResult:" + recogResults;
                                            Log.e(mTAG, str);
                                            LogFile.getInstance().saveMessage(str);
                                            onFaceDetRecCallBack.onFailRecognizerFace(0);
                                        }
                                    }catch (Exception e){
                                        Log.i(mTAG, "faceAlgSvrPrx.RecognizeFeatures:"+e.toString());
                                        LogFile.getInstance().saveMessage("faceAlgSvrPrx.RecognizeFeatures:"+e.toString());
                                    }
                                }

                                isLoop = false;

//                                boolean isNext = cursor.moveToNext();
//                                int pos = cursor.getPosition();
//                                if(isNext){
//                                    dir = cursor.getString(cursor.getColumnIndex(FeatDatabaseHelper.FEATDATA_PATH));
//                                    String pid = cursor.getString(cursor.getColumnIndex(FeatDatabaseHelper.PERSON_ID));
//                                    SimilarPerson sp = calcSimfromFile(featStranger, pid, dir, pos);
//                                    simPers.add();
//                                    //识别成功
//                                    if (sp.getScore() > MainActivity.maxSim){
//                                        int count = cursor.getInt(cursor.getColumnIndex(FeatDatabaseHelper.ENTER_COUNT));
//                                        String idno = cursor.getString(cursor.getColumnIndex(FeatDatabaseHelper.IDCARD_NO));
//                                        ContentValues contentValues = new ContentValues();
//                                        contentValues.put(FeatDatabaseHelper.ENTER_COUNT, count+1);
//                                        String stime = UtilCommon.getTimeCurrent();
//                                        contentValues.put(FeatDatabaseHelper.ENTER_TIME, stime);
//                                        int row = featDb.update(FeatDatabaseHelper.TABLE_NAME, contentValues,
//                                                FeatDatabaseHelper.PERSON_ID + " = ?", new String[]{pid});
//                                        onFaceDetRecCallBack.onSuccessRecognizerFace(idno, sp.getScore());
//                                    }
//                                }else if(!isNext && cursor.getCount()==0){
//                                    str = "无本地记录："+pos;
//                                    Log.i(mTAG, str);
//                                    LogFile.getInstance().saveMessage(str, true);
//                                    onFaceDetRecCallBack.onFailRecognizerFace(pos);
//                                    isLoop = false;
//                                }
//                                else {
//                                    Collections.sort(simPers);
//                                    str = "识别结束："+ cursor.getCount() + " scores:"+printListArray(simPers);
//                                    Log.i(mTAG, str);
//                                    LogFile.getInstance().saveMessage(str, true);
//                                    onFaceDetRecCallBack.onFinishRecognizerFace(simPers, fpStrangerPic, 0);//simPers.get(0).getScore());
//                                    isLoop = false;
//                                }
                                break;
                        }
                    }
//                    cursor.close();
                }else {
                    if (timesLostShadow >= numLostPerson){
                        if (indexFLow == enumFlow.REGISTERFlow)         //未完成注册
                        {
                            indexFLow = enumFlow.RECFlow;
                            onFaceDetRecCallBack.onFailtoRegister(-1);
                        }
                        permitedReg = false;
                        onFaceDetRecCallBack.onLostPerson(timesLostShadow);
                    }else {
                        onFaceDetRecCallBack.onFailDetectFace();
                    }
                }
                Log.d(mTAG, "recognizerFace finish.");
            }
        });
    }

    public void saveFaceDataNir(final byte[] data){
        savePhotoDataNir.execute(new Runnable() {
            @Override
            public void run() {
                String str = "";
                if (hasFace){
                    try {
                        picNirData = new byte[data.length];
                        System.arraycopy(data, 0, picNirData, 0, data.length);
                        str = "nir data:"+ picNirData.length;
                        Log.i(mTAG, str);
                        LogFile.getInstance().saveMessage(str);
                    } catch (Exception e){
                        str = "saveFaceDataNir bitmap error:"+e.toString();
                        Log.d(mTAG, str);
                        LogFile.getInstance().saveMessage(str);
                    }

                }
            }
        });
    }

    /**
     * 计算与指定人员相似度并提取出FaceID
     * @param featdata
     * @param pid
     * @param fileFeat
     * @param num
     * @return
     */
    public SimilarPerson calcSimfromFile(float[] featdata, String pid, String fileFeat, int num){
        String str = "";
        SimilarPerson sp = new SimilarPerson();
        sp.setPersonId(pid);
        byte[] filedata = UtilCommon.readFeatureFileInfos(fileFeat);
        int offset = 464;
        int cnt = UtilCommon.bytesToInt2(filedata, offset);         //特征值数量
        offset += 4;
        int len = UtilCommon.bytesToInt2(filedata, offset);         //单个特征值长度
        len = len/4;                                                //float型数据
        str = num + " - " + pid + " 特征值数量="+cnt + " 单个特征值长度="+len;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        //判断特征值数量及长度是否合理
        if (len!=featSize || cnt>100000){
            return sp;
        }
        offset += 4;
        float[] featData = UtilCommon.byte2FloatLitteEnd(filedata, offset, len*cnt);
        float[] fsim = new float[cnt];          //相似度
        int[] sSort = new int[cnt];             //序号
        if (featData != null && featData.length>0) {
            float[] data = new float[len];
            for (int i = 0; i < cnt; i++) {
                System.arraycopy(featData, i * len, data, 0, len);
//                synchronized (toolChain) {
//                    fsim[i] = toolChain.CalcSimilarity(data, featdata);
//                }
                sSort[i] = i;
            }
        }
        UtilCommon.bubbleSort(fsim, sSort);
        Log.d(mTAG, printfFloatArray(fsim, sSort));
        float avgSim = UtilCommon.averageArray(fsim);
        offset += cnt*len*4;
        len = filedata.length - offset;
        Log.d(mTAG, "filelen="+filedata.length + " offset="+offset + " len="+len);
        String[] faceId = new String[cnt];              //faceIds
        if (len > 0){
            String fid = null;
            try {
                fid = new String(filedata, offset, len, "UTF-8");
                String[] oldFaceId = fid.split(",");
                for (int i=0;i<oldFaceId.length;i++){
                    faceId[i] = oldFaceId[i];
                }
                Log.d(mTAG, "faceId:"+faceId.length + " oldFaceId="+oldFaceId.length);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(mTAG, "calcSimfromFile+"+e.toString());
                LogFile.getInstance().saveMessage("calcSimfromFile+"+e.toString());
            }
        }

        String fid = faceId[sSort[0]];
        if (fid == null){
            fid = ""+sSort[0];
        }
        str = "相似度："+fsim[0] + " 平均值："+avgSim + " faceIds="+faceId.length + " fid："+fid;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        sp.setFaceId(fid);
        sp.setScore(fsim[0]);
        return sp;
    }

    /**
     * 按日期存储有效人脸照片
     * @param data
     * @param suffix
     * @return
     */
    public String saveFacePhoto(byte[] data, String suffix){
        String dir = MainActivity.picDir + "Stranger/"+UtilCommon.getDateString()+"/";
        File file = new File(dir);
        if (!file.exists()){
            file.mkdirs();
        }
        dir += UtilCommon.getTimeStampCurrent() + suffix + ".jpg";
        UtilCommon.saveByteFile(dir, data);
        return dir;
    }

    /**
     * 按日期存储有效人脸照片
     * @param bitmap
     * @return 照片路径名
     */
    public String saveFacePhoto(Bitmap bitmap, String suffix){
        String dir = MainActivity.picDir + "Stranger/"+UtilCommon.getDateString()+"/";
        File file = new File(dir);
        if (!file.exists()){
            file.mkdirs();
        }
        dir += UtilCommon.getTimeStampCurrent() + suffix + ".jpg";
        UtilCommon.savePhoto(dir, bitmap);
        return dir;
    }

    /**
     * 保存临时照片
     * @param data
     * @param suffix
     * @return
     */
    public String saveStrangerTempPhoto(byte[] data, String suffix){
        String dir = MainActivity.picDir + "Stranger/";
        File file = new File(dir);
        if (!file.exists()){
            file.mkdirs();
        }
        dir += "temp" + suffix + ".jpg";
        UtilCommon.saveByteFile(dir, data);
        return dir;
    }
    /**
     * 保存临时照片
     * @param bitmap
     * @return 路径
     */
    public String saveStrangerTempPhoto(Bitmap bitmap){
        String dir = MainActivity.picDir + "Stranger/";
        File file = new File(dir);
        if (!file.exists()){
            file.mkdirs();
        }
        dir += "temp" + ".jpg";
        UtilCommon.savePhoto(dir, bitmap);
        return dir;
    }

    /**
     * 人员注册成功添加本地数据库
     */
    public void addRegisterPersontoLoaclDatabase(List<String> featFilePaths, List<String> faceFilePaths){
        String str = "";
        try {
            featDb = fDbHelper.getReadableDatabase();
        } catch (SQLException s) {
            Log.e(mTAG, "fDbHelper featDb:"+ s.toString());
        }
        ContentValues contentValues = new ContentValues();
        Cursor cursor = featDb.rawQuery("select * from " + FeatDatabaseHelper.TABLE_NAME
                + " where " + FeatDatabaseHelper.PERSON_ID + " = ? ", new String[]{personId});
        if (cursor.moveToNext()){
            contentValues.put(FeatDatabaseHelper.NEEDUPLOAD, 1);
            int row = featDb.update(FeatDatabaseHelper.TABLE_NAME, contentValues,
                    FeatDatabaseHelper.PERSON_ID + " = ?", new String[]{personId});
            str = "更新本地人员数据库 : "+row + " id:"+personId;

        }else {
            contentValues.put(FeatDatabaseHelper.PERSON_ID, personId);
            contentValues.put(FeatDatabaseHelper.NAME, nameIdCard);
            contentValues.put(FeatDatabaseHelper.HASHCODE, "");
            contentValues.put(FeatDatabaseHelper.IDCARD_NO, numIdCard);
            String fPath = MainActivity.dirFeatData+personId+"/" + personId+".feat";
            contentValues.put(FeatDatabaseHelper.FEATDATA_PATH, fPath);
            contentValues.put(FeatDatabaseHelper.ENTER_COUNT, 0);
            String stime = UtilCommon.getTimeCurrent();
            contentValues.put(FeatDatabaseHelper.ENTER_TIME, stime);
            contentValues.put(FeatDatabaseHelper.CREATE_TIME, stime);
            contentValues.put(FeatDatabaseHelper.AVAILABLE, 1);
            contentValues.put(FeatDatabaseHelper.NEEDUPLOAD, 1);
            long row = featDb.insert(FeatDatabaseHelper.TABLE_NAME, null, contentValues);
            str = "新增注册人员至本地数据库 : "+row + " id:"+personId + " time:"+stime;
        }
        cursor.close();

        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        //
        String nameTable = "Person"+personId;
        featDb.execSQL("create table if not exists " + nameTable
                + " (id integer primary key autoincrement," + FeatDatabaseHelper.FEATFILES+" text,"
                + FeatDatabaseHelper.FACEFILES+" text)");
        int size = featFilePaths.size();
        for (int i=0;i<size;i++){
            contentValues.clear();
            contentValues.put(FeatDatabaseHelper.FEATFILES, featFilePaths.get(i));
            contentValues.put(FeatDatabaseHelper.FACEFILES, faceFilePaths.get(i));
            featDb.insert(nameTable, null, contentValues);
        }
        featDb.close();
    }
    /*
    * 保存特征值文件
    * no : 特征库编号
    * name : 特征库名称
    * version : 特征库版本
    * update_time : 特征库更新时间
    * fPath : 文件路径
     */
//    public void saveFeatureFile(String no, String name, String ver, String time, String fPath){
//        byte[] data = new byte[32+128+16+32+256+4+4+feat_size*numRegisterPhoto*4];
//        int index = 0;
//        System.arraycopy(no.getBytes(), 0, data, index, no.length());
//        index += 32;
//        System.arraycopy(name.getBytes(), 0, data, index, name.length());
//        index += 128;
//        System.arraycopy(ver.getBytes(), 0, data, index, ver.length());
//        index += 16;
//        System.arraycopy(time.getBytes(), 0, data, index, time.length());
//        index += 32;
//        index += 256;
//        byte[] temp = UtilCommon.int2BytesBigEndian(numRegisterPhoto);
//        System.arraycopy(temp, 0, data, index, temp.length);
//        index += 4;
//        temp = UtilCommon.int2BytesBigEndian(feat_size*4);
//        System.arraycopy(temp, 0, data, index, temp.length);
//        index += 4;
//        temp = UtilCommon.getByteArrays(featRegister);
//        System.arraycopy(temp, 0, data, index, temp.length);
//
//        try {
//            File file = new File(fPath);
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            FileOutputStream out = new FileOutputStream(file);
//            out.write(data);
//            out.flush();
//            out.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public float[] readFeaturefromFile(String fPath){
        Log.d(mTAG, "readFeaturefromFile - fPath:"+fPath);
        byte[] data = null;
        int len;
        try {
            File file = new File(fPath);
            if (!file.exists()){
                return null;
            }
            FileInputStream in = new FileInputStream(file);
            len = 32+128+16+32+256+4+4+MainActivity.feat_size*MainActivity.numRegisterPhoto*4;
            data = new byte[len];
            in.read(data, 0, len);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        len = MainActivity.feat_size*MainActivity.numRegisterPhoto*4;
        byte[] fdata = new byte[len];
        System.arraycopy(data, 472, fdata, 0, len);
        return UtilCommon.byte2FloatBigEnd(fdata);
    }

    public void isIdCardManageale(final String name, final String num){
        nameIdCard = name.trim();
        numIdCard = num;
        String str = "身份证信息："+nameIdCard + "-" + numIdCard + " 查找是否允许注册";
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);

        int size = MainActivity.mPersonIfos.size();
        for (int i=0;i<size;i++){
            String idno = MainActivity.mPersonIfos.get(i).getIdNo();
            boolean isN = MainActivity.mPersonIfos.get(i).isNirFaceRegistered();
            str = "size="+size + " i="+i + " idno:"+idno + " isNir:"+isN;
            Log.d(mTAG, str);
            if (idno.equals(num)){
                personId = MainActivity.mPersonIfos.get(i).getPid();
                isIdCardNumReg = true;
                break;
            }
        }
        //临时允许本地无网络注册
        isIdCardNumReg = true;

        if (isIdCardNumReg){
            str = nameIdCard + " 允许注册";
        }else {
            str = nameIdCard + " 不允许注册";
        }
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
    }

    public void uploadIDCardRecord(HashMap<String,String> param){
        Log.d(mTAG, "uploadIDCardRecord - picStranger:"+picStranger + " simIDCard="+simIDCard);
        if (picStranger != null){
            int bytes = picStranger.getByteCount();
            ByteBuffer buffer = ByteBuffer.allocate(bytes);
            picStranger.copyPixelsToBuffer(buffer);
            byte[] argb = buffer.array();
            String simg = Base64.encodeToString(argb, Base64.DEFAULT);
            param.put("liveImg1", simg);
        }
        param.put("score", String.valueOf(simIDCard));
        comBusiness.saveRecordIDCard(param);
    }

    public void isIdCardReg(final String name, final String num){
        nameIdCard = name.trim();
        numIdCard = num;
        String str = "身份证信息："+nameIdCard + "-" + numIdCard + " 获取未注册近红外人脸的人员信息列表";
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);

        ArrayList<UnregNirPersonInfo> unpi = comBusiness.getUnregisteredNirList();
        int size = unpi.size();
        for (int i=0;i<size;i++){
            unregNirPersonInfo = unpi.get(i);
            String idno = unregNirPersonInfo.getIdNo();
            str = "size="+size + " i="+i + " idno:"+idno;
            Log.d(mTAG, str);
            if (idno.equals(num)){
                isIdCardNumReg = true;
                break;
            }
        }
        personId = unregNirPersonInfo.getId();
        str = unregNirPersonInfo.getName();
        if (isIdCardNumReg){
            str = str + " 允许注册";
        }else {
            str = str + " 不允许注册";
        }
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);

    }
    /*
    public class detectFaceThread extends Thread{
        public void run(){
            Log.i(mTAG, "detectFaceThread - entre");
            BitmapFactory.Options opts = new BitmapFactory.Options();
            byte[] rawData;
            int queuesize;
            while (flagWork) {
                queuesize = queue.size();
                if (queuesize > 0) {
                    synchronized (queue) {
                        if ((rawData = queue.poll()) != null) {
                            Log.i(mTAG, "detectFaceThread - queue:"+queuesize);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(rawData, 0, rawData.length, opts);
                            Bitmap bmp_src = bitmap.copy(Bitmap.Config.ARGB_8888, true); // true is RGBA
                            ByteBuffer buffer = ByteBuffer.allocate(bmp_src.getByteCount());
                            bmp_src.copyPixelsToBuffer(buffer);
                            byte[] argb = buffer.array();
                            int[] num_face = {-1}; // result of face num.
                            int[] face_pts = viplFaceDetectorUtils.FaceDetect(argb,
                                    CameraActivity.widthPhoto, CameraActivity.heightPhoto, num_face);
                            Log.i(mTAG, "Num[0]="+num_face[0] + " faceNum="+num_face.length);
                            timesLostShadow += 1;
                            if (timesLostShadow >=3){
                                onFaceDetRecCallBack.onLostPerson(timesLostShadow);
                            }
                            if(num_face[0] > 0) {
                                timesLostShadow = 0;
                                numPicFace += 1;
                                Log.i(mTAG, "PointDetect start.");
                                int[] face_5_pts = viplPointDetectorUtils.PointDetect(argb,
                                        CameraActivity.widthPhoto, CameraActivity.heightPhoto, num_face[0], face_pts);
                                String str = "";
                                for (int p:face_pts){
                                    str += p + ", ";
                                }
                                Log.i(mTAG, "face points:"+str + " numPicFace="+numPicFace);
                                for (int p:face_5_pts){
                                    str += p + ", ";
                                }
                                Log.i(mTAG, "key points:"+str);
                                String dir = MainActivity.picDir + "Stranger/";
                                File file = new File(dir);
                                if (!file.exists()){
                                    file.mkdir();
                                }
                                dir += UtilCommon.getTimeStampCurrent() + ".jpg";
                                UtilCommon.savePhoto(dir, bitmap);
                                float[] fface_5_pts = new float[face_5_pts.length];
                                for (int i=0;i<num_face.length;i++){
                                    fface_5_pts[i] = face_5_pts[i];
                                }
                                Mat mat = Imgcodecs.imread(dir);
                                viplFaceRecognizerUtils.ExtractFeatures(mat.getNativeObjAddr(), fface_5_pts, featStranger);
                                if (isIdCardPhoto) {
                                    float sim = viplFaceRecognizerUtils.CalcSimilarity(featStranger, featIDCard);
                                    Log.i(mTAG, "sim=" + sim);
                                }
                            }
                        }
                    }
                } else {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    */

    public float calcSimVisPicIdCard_Stranger(String fPath){
        byte[] data1 = UtilCommon.readFeatureFileInfos(fPath);
        byte[] data2 = UtilCommon.readFeatureFileInfos(fpStrangerPic);
        if (data1!=null && data2!=null && data1.length>0 && data2.length>0) {
            Image image1 = new Image();
            image1.data = data1;
            Image image2 = new Image();
            image2.data = data2;
            if (faceAlgSvrPrx != null)
                return faceAlgSvrPrx.MatchImage(image1, image2);
            else
                return 0;
        }

        return 0;
    }
    /**
     * 计算指定照片和现场照片相似度
     * @param fPath
     * @return
     */
    public float calcSimPic_Stranger(String fPath){
        Bitmap bmp1 = BitmapFactory.decodeFile(fPath);
        Bitmap bmp_src1 = bmp1.copy(Bitmap.Config.ARGB_8888, true); // true is RGBA
        int bytes1 = bmp_src1.getByteCount();
        ByteBuffer buffer1 = ByteBuffer.allocate(bytes1);
        bmp_src1.copyPixelsToBuffer(buffer1);
        byte[] img1 = buffer1.array();

        Bitmap bmp2 = BitmapFactory.decodeFile(fpStrangerPic);
        Bitmap bmp_src2 = bmp2.copy(Bitmap.Config.ARGB_8888, true); // true is RGBA
        int bytes2 = bmp_src2.getByteCount();
        ByteBuffer buffer2 = ByteBuffer.allocate(bytes2);
        bmp_src2.copyPixelsToBuffer(buffer2);
        byte[] img2 = buffer2.array();
        float sim;
//        synchronized (toolChain) {
//            sim = toolChain.CalcSimilarityWithTwoImages(img1, bmp_src1.getWidth(), bmp_src1.getHeight(),
//                    img2, bmp_src2.getWidth(), bmp_src2.getHeight());
//        }
        return 0;
    }
    public void extractPic(Bitmap photo, final String fPath){
        picID = photo;

        Log.i(mTAG, "extractPic - entre - bmp:"+picID);
        int[] num_face = {-1}; // result of face num.
        int bytes = picID.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        picID.copyPixelsToBuffer(buffer);
        byte[] argb = buffer.array();
        Log.d(mTAG, "extractPic FaceDetect start.");
        int[] face_pts;
        synchronized (viplFDU) {
            face_pts = viplFDU.FaceDetect(argb, picID.getWidth(), picID.getHeight(), num_face);
        }
        Log.d(mTAG, "extractPic FaceDetect end.numface:"+num_face[0]);
        if(num_face[0] > 0) {
            Log.d(mTAG, "extractPic PointDetect start width:"+picID.getWidth() + " height:"+picID.getHeight());
            int[] face_5_pts;
            synchronized (viplPDU) {
                face_5_pts = viplPDU.PointDetect(argb, picID.getWidth(), picID.getHeight(), num_face[0], face_pts);
            }
            int f5pLen = face_5_pts.length;
            Log.d(mTAG, "extractPic PointDetect end");
            String str = "身份证照片特征点：";
            float[] fface_5_pts = new float[f5pLen];
            for (int i=0;i<f5pLen;i++){
                fface_5_pts[i] = face_5_pts[i];
                str = str + face_5_pts[i] + ",";
            }
            Log.i(mTAG, str);
            LogFile.getInstance().saveMessage(str);
            Mat mat = Imgcodecs.imread(fPath);
            synchronized (viplFRNU) {
                viplFRNU.ExtractFeatures(mat.getNativeObjAddr(), fface_5_pts, featIDCard);
            }
            indexFLow = enumFlow.IDCARDFlow;
        }
    }

    public void extractPic(String dir, HashMap<String, String> hashMap){
        String simg = null;
        Log.d(mTAG, "picStranger:"+picStranger);
        if (picStranger != null){
            Bitmap bmp_src = picStranger.copy(Bitmap.Config.ARGB_8888, true); // true is RGBA
            int bytes = bmp_src.getByteCount();
            ByteBuffer buffer = ByteBuffer.allocate(bytes);
            bmp_src.copyPixelsToBuffer(buffer);
            byte[] argb = buffer.array();
            simg = Base64.encodeToString(argb, Base64.DEFAULT);

            Bitmap photo = BitmapFactory.decodeFile(dir);
            Bitmap bmp_src2 = photo.copy(Bitmap.Config.ARGB_8888, true); // true is RGBA
            int b = bmp_src2.getByteCount();
            ByteBuffer buffer1 = ByteBuffer.allocate(b);
            bmp_src2.copyPixelsFromBuffer(buffer1);
            byte[] argb1 = buffer1.array();
//            synchronized (toolChain){
//                simIDCard = toolChain.CalcSimilarityWithTwoImages(argb1, bmp_src2.getWidth(), bmp_src2.getHeight(),
//                        argb, bmp_src.getWidth(), bmp_src.getHeight());
//            }
        }
        String str = "extractPic - 身份证照片相似度："+simIDCard;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);

        hashMap.put("liveImg1", simg);
        hashMap.put("score", String.valueOf(simIDCard));
        comBusiness.saveRecordIDCard(hashMap);
    }

    public String printfFloatArray(float[] data, int[] sort){
        String str = "";
        int size = data.length;
        for(int i=0;i<size;i++){
            str += sort[i] + "-" + data[i] + ",";
        }
        return str;
    }

    public String printListArray(List<SimilarPerson> spList){
        String str = "";
        for(SimilarPerson sp : spList){
            str += sp.getScore() + ",";
        }
        return str;
    }

    /**
     * 人脸照片特征值合并信息
     * @param pid
     * @param fPaths1
     * @param fPaths2
     * @return
     */
    public PersonRegFilesInfo setPerRegInfos(String pid, List<String> fPaths1, List<String> fPaths2){
        PersonRegFilesInfo personRegFilesInfo = new PersonRegFilesInfo();
        personRegFilesInfo.setPid(pid);
        personRegFilesInfo.setPicType(PICTYPEVIS);
        personRegFilesInfo.setFeatFileName("files");
        personRegFilesInfo.setFeatFilePaths(fPaths1);
        personRegFilesInfo.setFaceFileName("images");
        personRegFilesInfo.setFaceFilePaths(fPaths1);
        return personRegFilesInfo;
    }

    public void closeClass(){
        Log.i(mTAG, "closeClass");
        flagWork = false;
        try {
            Thread.sleep(900);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.i(mTAG, "closeClass  -1");
        fDbHelper.close();
    }

    public interface OnFaceDetRecCallBack
    {
        public void onLostPerson(int num);
        public void onPhotoSaved(int numPic);
        public void onFailDetectFace();
        public void onFailRecognizerFace(float sim);
        public void onSuccessRecognizerFace(String id, float sim);
        public void onFinishRecognizerFace(List<SimilarPerson> spList, String fPath, float maxSim);
        public void onRegistering(int num);
        public void onFinishRegister(PersonRegFilesInfo prfi);
        public void onFailtoRegister(int num);
        public void onFailServers();
    }

    public void setOnFaceDetRecCallBack(OnFaceDetRecCallBack onFaceDetRecCallBack){
        this.onFaceDetRecCallBack = onFaceDetRecCallBack;
    }

    public int getTimesLostShadow(){
        return timesLostShadow;
    }

    public void setTimesLostShadow(int num){
        timesLostShadow = num;
    }

    /**
     * 允许注册的身份证信息
     * @param permitedReg
     * @param pid
     * @param name
     * @param idno
     */
    public void setPermitedReg(boolean permitedReg, String pid, String name, String idno) {
        this.permitedReg = permitedReg;
        personId = pid;
        nameIdCard = name;
        numIdCard = idno;
        Log.d(mTAG, "pid:"+pid + " permitedReg:"+permitedReg);
        LogFile.getInstance().saveMessage("pid:"+pid + " permitedReg:"+permitedReg);
    }

    public void setNumPicFace(int numPicFace) {
        this.numPicFace = numPicFace;
    }

    public String getFpStrangerPic() {
        return fpStrangerPic;
    }

    public String getFpTempPic() {
        return fpTempPic;
    }

    public void setIndexPicRegister(int indexPicRegister) {
        this.indexPicRegister = indexPicRegister;
    }

    public String getFpStrangerPicNir() {
        return fpStrangerPicNir;
    }
}
