package com.binjn.accessctrlsysbinjn;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.audiofx.Visualizer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.UiThread;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.application.MainApplication;
import com.binjn.DataBase.FeatDatabaseHelper;
import com.binjn.DataBase.PlaylistDatabaseHelper;
import com.binjn.clienthttp.ClientHttp;
import com.binjn.clienthttp.ComBusiness;
import com.binjn.clienthttp.OkHttpUtil;
import com.binjn.faceimgresovle.FaceDetRec;
import com.binjn.protocoldatainfo.ADInfo;
import com.binjn.protocoldatainfo.ManageablePersonInfo;
import com.binjn.protocoldatainfo.PersonRegFilesInfo;
import com.binjn.protocoldatainfo.SimilarPerson;
import com.hdos.idCardUartDevice.publicSecurityIDCardLib;
import com.seetatech.toolchainv3.ToolChain;
import com.utilcommon.LogFile;
import com.utilcommon.NetworkUtil;
import com.utilcommon.SharePrefUtil;
import com.utilcommon.TTSUtils;
import com.utilcommon.UtilCommon;
import com.utilcommon.WindowBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.LogRecord;

import FaceAlg.Image;
import IceInternal.Ex;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import vipl.ict.cn.facedetector500jni.VIPLFaceDetectorUtils;
import vipl.ict.cn.facerecognizerNIR402.VIPLFaceRecognizerNIRUtils;
import vipl.ict.cn.pointdetector403jni.VIPLPointDetectorUtils;


public class MainActivity extends AppCompatActivity {
    //static
    public static final String mainDir = "/mnt/sdcard/AccessCtrlSysBinjn/";
    public static final String picDir = mainDir + "Pic/";
    public static final String dirFeatData = picDir + "FeatureData/";
    public static final String vidDir = mainDir + "Vid/";
    public static final String playlistDir = mainDir + "Playlist/";
    public static final String modelDir = "/mnt/sdcard/seeta/";
    public static final String faceDetectorModel = "VIPLFaceDetector5.1.2.NIR.640x480.sta";
    public static final String pointDetectorModel = "VIPLPointDetector5.0.pts5.dat";
    public static final String faceRecognizerModel = "VIPLFaceRecognizer4.3.HE3.dat";
    public static final String modelPath = "/mnt/sdcard/vipl/VIPLFaceDetector5.0.0.dat";
    public static final String pointdetector_modelPath = "/mnt/sdcard/vipl/VIPLPointDetector4.0.3.dat";
    public static final String faceRec_modelPath = "/mnt/sdcard/vipl/data/test_face_recognizernir402/viplfacenet_90x90_HE3.dat";
    public static final String ACTIONPUSHMESSAGE = "com.binjn.accessctrlsysbinjn.pushmessage";
    public static final String NETWORKSTATECHANGE = "com.binjn.accessctrlsysbinjn.CONNECTIVITY_CHANGE";
    private static String mTAG = "MainActivity";
    public static String addrMac = "";
    public static boolean isPersonNear = false;                         //红外人体感应
    public static List<ManageablePersonInfo> mPersonIfos;               //管理人员列表
    public static Map<String, ManageablePersonInfo> mPersonInfoMap;     //管理人员
    public static List<ADInfo> playlistADInfos = new ArrayList<>();
    public static float[] featRegister;
    public static int feat_size;
    public static int TYPEVIDEO = 10201, TYPEIMAGE = 10200;
    public static final int numRegisterPhoto = 10;                      //注册照片数
    public static final int widthPhoto = 960, heightPhoto = 540;
    public static final int regWindowWidth = 640, regWindowHeight = 360;
    public static final String portSerial ="/dev/ttyS2";
    public static boolean isNetwork = false;                            //网络连通标志
    public static String catchPhotoFP = MainActivity.picDir + "Stranger/catchphoto.jpg";
    //view
    private VideoView ADView;
    private ImageView ADImgView;
    private ProgressBar progressBar;
    private EditText editText;
    private TextView versionEt, inputTip, dateEt;
    private FrameLayout cameraViewContainer;
    //class
    public static VIPLFaceDetectorUtils viplFaceDetectorUtils;
    public static VIPLPointDetectorUtils viplPointDetectorUtils;
    public static VIPLFaceRecognizerNIRUtils viplFaceRecognizerUtils;
    public static ToolChain toolChain;
    private MediaController mediaController;
    private com.welbell.hardwaretestdemo.MainActivity hardwareMain;
    private ComBusiness comBusiness;
    private FeatDatabaseHelper fDbHelper;
    private SQLiteDatabase featDb, playListDb;
    private PlaylistDatabaseHelper plDbHelper;
    private RtcEngine mRtcEngine;// Tutorial Step 1
    private Cursor csrPlaylist = null;
    private SurfaceView cameraSfView, sfViewCall, videoSfView;
    private MediaPlayer videoADMediaPlayer;
    private AudioTrack audioTrackplayer;
    private int atBufferSize;
    private SurfaceHolder cameraSfHolder, videoSfHolder;
    private Camera camera0, camera1;
    private FaceDetRec faceDetRec;
    private publicSecurityIDCardLib iDCardDevice;
    private CloudPushService pushServiceCloud;
    ExecutorService showImageSingleThread = Executors.newSingleThreadExecutor();
    ExecutorService readCardSingleThread = Executors.newSingleThreadExecutor();
    ExecutorService uploadRecResultSingleThread = Executors.newSingleThreadExecutor();
    ExecutorService pushMessageSingleThread = Executors.newSingleThreadExecutor();
    ExecutorService showDateSingleThread = Executors.newSingleThreadExecutor();
    ExecutorService controlLightsSingleThread = Executors.newSingleThreadExecutor();
    ExecutorService playVoiceSingleThread = Executors.newSingleThreadExecutor();
    ExecutorService playWaitAudioSingleThread = Executors.newSingleThreadExecutor();
    ExecutorService scheduledThread = Executors.newCachedThreadPool();
    private Timer timerHeart = new Timer();
    ConnectionChangeReceiver netWorkStateReceiver;
    PushMessageBroadcastReceiver pushMessageReceiver;
    //var
    private String versionSys = "Ver 0.0.0";
    private int indexVid = 1;
    private boolean isCameraRun = false;                //摄像头运行标志
    private boolean flagCallOwenr = false;              //呼叫业主事件标志
    private boolean flagJoinChannel = false;            //加入通话通道标志
    private boolean flagGuestCallRecord = false;        //访客呼叫记录标志
    private boolean flagOpenDoorRemote = false;         //远程开门事件标志
    private boolean flagOpenDoorPassword = false;       //密码开门事件标志
    private boolean flagTryPassword = false;            //尝试密码标志
    private boolean needCatchPhoto = false;             //需要抓拍照片标志
    private boolean connectedLiveChat = false;          //呼叫连通标志
    private int typeInput = 0;                          //输入类型0-房号、1-密码
    private boolean isPlaying = false;
    private boolean flagReadIdCard = false;             //停止读取身份证标志
    private boolean flagFirstRun = true;
    private int numADSrc = 0;
    int numPicTaken0 = 0, numPicTaken1=0;               //拍照片数量
    private String namePackage;
    private String pidRemoteOpenDoor = "";              //远程开门操作人ID
    private String idTempPassword = "";                 //临时密码ID
    private List<Integer> chatUsersList;
    private float heartbeatTime = 60;
    private float matchScorePass = 0.738f;
    private float compared = 0.45f;
    private int faceImageSize = 100;
    private float roll=15, pitch=15, yaw=15;
    private int delayReadIdCard = 100;                  //ms
    private byte[] name = new byte[32];
    private byte[] sex = new byte[6];
    private byte[] birth = new byte[18];
    private byte[] nation = new byte[12];
    private byte[] address = new byte[72];
    private byte[] Department = new byte[32];
    private byte[] IDNo = new byte[38];
    private byte[] EffectDate = new byte[18];
    private byte[] ExpireDate = new byte[18];
    private byte[] pErrMsg = new byte[20];
    private byte[] BmpFile = new byte[38556];
    private String strLog = "\n------------AccessCtrlSysBinjn Start------------";
    //
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            strLog = "JoinChannelSuccess channel:"+channel + " uid="+uid + " elapsed="+elapsed;
            Log.i(mTAG, strLog);
            LogFile.getInstance().saveMessage(strLog);
            flagJoinChannel = true;
            waitUtilLiveChat(25);
        }

        @Override
        public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onRejoinChannelSuccess(channel, uid, elapsed);
            strLog = "onRejoinChannelSuccess channel:"+channel + " uid="+uid + " elapsed="+elapsed;
            Log.i(mTAG, strLog);
            LogFile.getInstance().saveMessage(strLog);
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            strLog = "onUserJoined:"+" uid="+uid + " elapsed="+elapsed;
            Log.i(mTAG, strLog);
            LogFile.getInstance().saveMessage(strLog);
            chatUsersList.add(uid);
        }

        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) { // Tutorial Step 5
            strLog = "FirstRemoteVideoDecoded uid="+uid + " width="+width + " height="+height + " elapsed="+elapsed;
            Log.i(mTAG, strLog);
            LogFile.getInstance().saveMessage(strLog);
            connectedLiveChat = true;
        }

        @Override
        public void onFirstRemoteAudioFrame(int uid, int elapsed) {
            super.onFirstRemoteAudioFrame(uid, elapsed);
            strLog = "onFirstRemoteAudioFrame uid="+uid + " elapsed="+elapsed;
            Log.i(mTAG, strLog);
            LogFile.getInstance().saveMessage(strLog);
            connectedLiveChat = true;
        }

        @Override
        public void onUserOffline(int uid, int reason) { // Tutorial Step 7
            super.onUserOffline(uid, reason);
            if (chatUsersList.contains(uid)){
                chatUsersList.remove(uid);
            }
            int size = chatUsersList.size();
            strLog = "Users="+size + " UserOffline uid="+uid + " reason="+reason
                    + " connectedLiveChat:"+connectedLiveChat + " flagCallOwenr:"+flagCallOwenr;
            Log.i(mTAG, strLog);
            LogFile.getInstance().saveMessage(strLog);
            Toast.makeText(MainActivity.this, "业主离开通话", Toast.LENGTH_SHORT).show();
            if (size == 1){
                chatUsersList.clear();
                handler.sendEmptyMessage(OFFLINEREMOTEUSER);
            }
        }

        /**
         * 离开频道回调
         * @param stats 通话相关的统计信息
         */
        @Override
        public void onLeaveChannel(RtcStats stats) {
            super.onLeaveChannel(stats);
            strLog = "LeaveChannel 通话时长="+stats.totalDuration+" s" + " 客户:"+stats.users
                    + " 当前应用程序的 CPU 使用率:"+stats.cpuAppUsage
                    + " 当前系统的 CPU 使用率:"+stats.cpuTotalUsage;
            Log.i(mTAG, strLog);
            LogFile.getInstance().saveMessage(strLog);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sfViewCall.setVisibility(View.GONE);
                    cameraViewContainer.removeView(sfViewCall);
                    cameraSfView.setZOrderMediaOverlay(true);
                    cameraSfView.setVisibility(View.VISIBLE);
                }
            });
            flagCallOwenr = false;
        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) { // Tutorial Step 10
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    onRemoteUserVideoMuted(uid, muted);
                }
            });
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            versionSys = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        strLog += versionSys;
        LogFile.getInstance().saveMessage(strLog);
        //
        int memClass = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        int largeMemClass = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getLargeMemoryClass();
        strLog = "MainActivity onCreate heapsize:"+memClass + " large:"+largeMemClass;
        Log.d(mTAG, strLog);
        LogFile.getInstance().saveMessage(strLog);
        IntentFilter filter3 = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        netWorkStateReceiver = new ConnectionChangeReceiver();
        registerReceiver(netWorkStateReceiver, filter3);
        //取消标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        WindowBar.compat(this);
//        WindowBar.compat(this, getResources().getColor(R.color.red_bg));

        setContentView(R.layout.activity_main);

        File file = new File(mainDir);
        if (!file.exists()){
            file.mkdirs();
        }

        initView();
        initData();
        initAction();

        initTest();
    }

    private void initTest(){
        //BOARD 主板
String phoneInfo = "BOARD: " + android.os.Build.BOARD;
phoneInfo += ", BOOTLOADER: " + android.os.Build.BOOTLOADER;
//BRAND 运营商
phoneInfo += ", BRAND: " + android.os.Build.BRAND;
phoneInfo += ", CPU_ABI: " + android.os.Build.CPU_ABI;
phoneInfo += ", CPU_ABI2: " + android.os.Build.CPU_ABI2;

//DEVICE 驱动
phoneInfo += ", DEVICE: " + android.os.Build.DEVICE;
//DISPLAY Rom的名字 例如 Flyme 1.1.2（魅族rom） &nbsp;JWR66V（Android nexus系列原生4.3rom）
phoneInfo += ", DISPLAY: " + android.os.Build.DISPLAY;
//指纹
phoneInfo += ", FINGERPRINT: " + android.os.Build.FINGERPRINT;
//HARDWARE 硬件
phoneInfo += ", HARDWARE: " + android.os.Build.HARDWARE;
phoneInfo += ", HOST: " + android.os.Build.HOST;
phoneInfo += ", ID: " + android.os.Build.ID;
//MANUFACTURER 生产厂家
phoneInfo += ", MANUFACTURER: " + android.os.Build.MANUFACTURER;
//MODEL 机型
phoneInfo += ", MODEL: " + android.os.Build.MODEL;
phoneInfo += ", PRODUCT: " + android.os.Build.PRODUCT;
phoneInfo += ", RADIO: " + android.os.Build.RADIO;
phoneInfo += ", RADITAGSO: " + android.os.Build.TAGS;
phoneInfo += ", TIME: " + android.os.Build.TIME;
phoneInfo += ", TYPE: " + android.os.Build.TYPE;
phoneInfo += ", USER: " + android.os.Build.USER;
//VERSION.RELEASE 固件版本
phoneInfo += ", VERSION.RELEASE: " + android.os.Build.VERSION.RELEASE;
phoneInfo += ", VERSION.CODENAME: " + android.os.Build.VERSION.CODENAME;
//VERSION.INCREMENTAL 基带版本
phoneInfo += ", VERSION.INCREMENTAL: " + android.os.Build.VERSION.INCREMENTAL;
//VERSION.SDK SDK版本
phoneInfo += ", VERSION.SDK: " + android.os.Build.VERSION.SDK;
phoneInfo += ", VERSION.SDK_INT: " + android.os.Build.VERSION.SDK_INT;

        Log.d(mTAG, phoneInfo);
        Log.i(mTAG, " ram:"+getTotalRam(this));

//        File video=new File(mianDir + "video/"+3+".mp4");
//        Log.i(mTAG, "video:"+video.exists());
//        SimpleDateFormat formatter = new SimpleDateFormat    ("yyyyMMddHHmmss");
//        Date curDate = new Date();
//        String str = formatter.format(curDate);
//        Log.i(mTAG, "mac:"+ UtilCommon.getMac());
//        getPlaylist();
//        getADSrc(null);
//        getManageablePersons();
//        testUpload();
    }

public static String getTotalRam(Context context){//GB
        String path = "/proc/meminfo";
        String firstLine = null;
        int totalRam = 0 ;
        try{
            java.io.FileReader fileReader = new java.io.FileReader(path);
            java.io.BufferedReader br = new java.io.BufferedReader(fileReader,8192);
            firstLine = br.readLine().split("\\s+")[1];
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(firstLine != null){
            totalRam = (int)Math.ceil((new Float(Float.valueOf(firstLine) / (1024 * 1024)).doubleValue()));
        }

        return totalRam + "GB";//返回1GB/2GB/3GB/4GB
    }

    private void initView() {
        ADView = (VideoView)findViewById(R.id.videoView);
        ADImgView = (ImageView)findViewById(R.id.imageView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar3);
        progressBar.setMax(numRegisterPhoto);
        ADView.setVisibility(View.GONE);
        ADImgView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        editText = (EditText)findViewById(R.id.editText);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b){
                    Log.d(mTAG, "edit focus");
                }else {
                    Log.d(mTAG, "edit lost focus");
                }
            }
        });
        inputTip = (TextView) findViewById(R.id.textView);
        versionEt = (TextView) findViewById(R.id.textView2);
        dateEt = (TextView) findViewById(R.id.textView3);
        dateEt.setText(UtilCommon.getDateTimeWeek());
        versionEt.setText(versionSys);
        cameraViewContainer = (FrameLayout)findViewById(R.id.local_video_view_container);
        cameraViewContainer.setVisibility(View.GONE);
        cameraSfView = (SurfaceView) findViewById(R.id.surfaceView2);
        videoSfView = findViewById(R.id.surfaceView3);
    }

    private void initData(){
        // Initialize;
        String pkgName = getPackageName();
        String className = getClass().getName();
        strLog = "initData packageName:"+pkgName + " activityName:"+className;
        Log.i(mTAG, strLog);
        LogFile.getInstance().saveMessage(strLog);
        comBusiness = new ComBusiness(this);
        mediaController = new MediaController(this);
        hardwareMain = new com.welbell.hardwaretestdemo.MainActivity(getPackageName(), getClass().getName());
        hardwareMain.controlVisibleCamereLight(false);
        hardwareMain.controlKeyboardLight(false);
        hardwareMain.controlDoorLock(false);
        hardwareMain.setOnHardwreCallBack(new com.welbell.hardwaretestdemo.MainActivity.OnHardwreCallBack() {
            @Override
            public void onInfrared(int act) {
                Log.d(mTAG, "HardwreCallBack - onInfrared - act="+act);
                Message message = new Message();
                message.what = INFRAREDDEDECT;
                message.arg1 = act;
                handler.sendMessage(message);
            }
        });
        initLuanchAgoraEngine();

        initFaceRecModule();

        File file = new File(dirFeatData);
        if (!file.exists()){
            file.mkdirs();
        }
        file = new File(vidDir);
        if (!file.exists()){
            file.mkdirs();
        }
        file = new File(playlistDir);
        if (!file.exists()){
            file.mkdirs();
        }
        chatUsersList = new ArrayList<>();
        fDbHelper = new FeatDatabaseHelper(this);
        plDbHelper = new PlaylistDatabaseHelper(this);
        playListDb = plDbHelper.getReadableDatabase();
        initMediaPlayer();

        strLog = "初始化完成.";
        LogFile.getInstance().saveMessage(strLog);

        IntentFilter filter2 = new IntentFilter(ACTIONPUSHMESSAGE);
        pushMessageReceiver = new PushMessageBroadcastReceiver();
        registerReceiver(pushMessageReceiver, filter2);
    }

    private void initAction() {
        showCurrentDate();
        getMacAddress();
        playADSrc();
//        getHttpToken();
//        playVideo();
//        requestMacId();
//        joinChannel();
        luanchCameraView();
    }

    private void showCurrentDate() {
        showDateSingleThread.execute(new Runnable() {
            @Override
            public void run() {
                do {
                    try {
                        Thread.sleep(1000);
                        handler.sendEmptyMessage(DATESHOW);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while(true);
            }
        });
    }

    /**
     * 初始化视频通话接口、整个程序初始化一次即可
     */
    public void initLuanchAgoraEngine(){
//        cameraSfView.setVisibility(View.GONE);
//        stopCameras();
//        isCameraRun = false;
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
            mRtcEngine.setDefaultAudioRoutetoSpeakerphone(true);
            mRtcEngine.setEnableSpeakerphone(true);
            mRtcEngine.setSpeakerphoneVolume(255);
            mRtcEngine.enableVideo();
            mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P, false);
            mRtcEngine.setParameters(" {\"che.video.captureFormatNV21\":true}");
//            sfViewCall = RtcEngine.CreateRendererView(getBaseContext());
//            sfViewCall.setZOrderMediaOverlay(true);
//            cameraViewContainer.addView(sfViewCall);
//            mRtcEngine.setupLocalVideo(new VideoCanvas(sfViewCall, VideoCanvas.RENDER_MODE_ADAPTIVE, 0));
//            mRtcEngine.startPreview();
            strLog = "initLuanchAgoraEngine Success Version:"+RtcEngine.getSdkVersion() + " mRtcEngine:"+mRtcEngine;
            Log.i(mTAG, strLog);
            LogFile.getInstance().saveMessage(strLog);
        } catch (Exception e) {
            Log.e(mTAG, Log.getStackTraceString(e));
            LogFile.getInstance().saveMessage("initLuanchAgoraEngine error:"+e.toString());
        }
    }

    /**
     * 加入通话频道
     * @param channelName
     */
    private void joinChannel(String channelName) {
        cameraSfView.setVisibility(View.GONE);
        stopCameras();
        isCameraRun = false;
        sfViewCall = RtcEngine.CreateRendererView(getBaseContext());
        sfViewCall.setZOrderMediaOverlay(true);
        cameraViewContainer.addView(sfViewCall);
        mRtcEngine.setupLocalVideo(new VideoCanvas(sfViewCall, VideoCanvas.RENDER_MODE_ADAPTIVE, 0));
        mRtcEngine.startPreview();
        //
        int re = mRtcEngine.joinChannel(null, channelName, "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
        String str = "joinChannel:"+re;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
    }

    private void leaveChannel() {
        Log.d(mTAG, "leaveChannel:"+mRtcEngine);
        LogFile.getInstance().saveMessage("leaveChannel:"+mRtcEngine);
        if (mRtcEngine != null) {
            mRtcEngine.stopPreview();
            mRtcEngine.leaveChannel();
//            mRtcEngine = null;
        }
    }

    private void initCloudChannel(){
        strLog = "initCloudChannel";
        Log.i(mTAG, strLog);
        LogFile.getInstance().saveMessage(strLog);
        PushServiceFactory.init(MainApplication.getContext());
        pushServiceCloud = PushServiceFactory.getCloudPushService();
        pushServiceCloud.register(MainApplication.getContext(), new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                strLog = "init cloudchannel success";
                Log.i(mTAG, strLog);
                LogFile.getInstance().saveMessage(strLog);
            }
            @Override
            public void onFailed(String errorCode, String errorMessage) {
                strLog = "init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage;
                Log.e(mTAG, strLog);
                LogFile.getInstance().saveMessage(strLog);
            }
        });
    }

    private void checkAliasIsExist() {
        MainApplication.pushService.listAliases(new CommonCallback() {
            @Override
            public void onSuccess(String s) {
                strLog = "别名 === " + s;
                if(TextUtils.isEmpty(s)){
                    //添加别名
                    strLog = strLog + "需要注册别名";
                    handler.sendEmptyMessage(ALIASNOTEXIST);
                }else {
                    //别名已存在，不添加
                    strLog = strLog + " 别名存在";
                }
                Log.i(mTAG, strLog);
                LogFile.getInstance().saveMessage(strLog);
            }

            @Override
            public void onFailed(String s, String s1) {
                strLog = "checkAliasIsExist Failed-s:"+s + " s1:"+s1;
                Log.e(mTAG, strLog);
                LogFile.getInstance().saveMessage(strLog);
            }
        });
    }
    private void addAliaName() {
        String id = ComBusiness.idMac;
        Log.d(mTAG, "id:"+id);
        MainApplication.pushService.addAlias(id, new CommonCallback() {
            @Override
            public void onSuccess(String s) {
                strLog = "别名添加成功:" + s;
                Log.i(mTAG, strLog);
                LogFile.getInstance().saveMessage(strLog);
            }

            @Override
            public void onFailed(String s, String s1) {
                strLog = "addAliaName Failed-s:"+s + " s1:"+s1;
                Log.e(mTAG, strLog);
                LogFile.getInstance().saveMessage(strLog);
            }
        });
    }

    /**
     * 备份删除用户数据
     */
    public void backupDeleteUserData(){
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                File file = new File(LogFile.nameLogFile);
                long len = file.length();
                String str = "日志文件大小："+len+" bytes";
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                if (len > 10*1024*1024){
                    str = "备份日志文件";
                    Log.i(mTAG, str);
                    LogFile.getInstance().saveMessage(str);
                    UtilCommon.copyFile(LogFile.nameLogFile, LogFile.backupLogFile);
                    file.delete();
                }
                long oldtime = new Date().getTime() - (long) 30*24*3600*1000;
                try {
                    str = UtilCommon.longToString(oldtime, "yyyyMMdd");
                    Log.i(mTAG, "old day:"+str);
                    LogFile.getInstance().saveMessage("old day:"+str);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                file = new File(MainActivity.picDir + "Stranger/"+str);
                if (file.exists() && file.isDirectory()) {
                    File[] files = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        File f = files[i];
                        f.delete();
                    }
                    file.delete();//如要保留文件夹，只删除文件，请注释这行
                    str = "删除文件夹："+str + " 共 "+files.length+" 个文件";
                    Log.i(mTAG, str);
                    LogFile.getInstance().saveMessage(str);
                }
            }
        });

    }

    /**
     * 获取门禁机Mac地址
     */
    public void getMacAddress(){
        addrMac = SharePrefUtil.getString(this, SharePrefUtil.ADDRESSMAC, "");
        String str = "门禁机Mac地址:" + addrMac;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
        if (addrMac.equals("")){
            addrMac = UtilCommon.getMac();
        }else {
            return;
        }
        str = "获取门禁机Mac地址:"+addrMac;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
//        addrMac = "3af936b3e045";
        if (addrMac == null){
            return;
        }
        SharePrefUtil.saveString(this, SharePrefUtil.ADDRESSMAC, addrMac);
    }

    /**
    * 获取机器Mac地址注册门禁机ID
     */
    public void requestMacId(){
        String str = "";
//        ComBusiness.idMac = SharePrefUtil.getString(this, SharePrefUtil.MACHINEID, "");
        str = "门禁机ID："+ ComBusiness.idMac;
        if (ComBusiness.idMac.equals("")){
            scheduledThread.execute(new Runnable() {
                @Override
                public void run() {
                    if (comBusiness.registerMachine(addrMac) != null){
                        handler.sendEmptyMessage(SUCCESSMACID);
                    }
                }
            });
        }else {
            handler.sendEmptyMessage(SUCCESSMACID);
        }
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
    }

    /**
     *  校正时间和Token
     */
    public void checkTimeToken(){
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                String timeStamp = UtilCommon.getTimeNetwork(null);
                boolean flag = comBusiness.isTokenValid();
                String str = "网络时间："+timeStamp + " ValidToken:"+flag;
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                if (!flag){
                    comBusiness.getToken(timeStamp);
                }
                handler.sendEmptyMessage(FINISHNETTOKEN);
            }
        });
    }

    /**
     * 获取管理人员列表如不为空找出需要下载特征值的人员
     */
    public void getManageablePersons(){
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                int[] code = new int[]{0};
                mPersonInfoMap = comBusiness.getManageablePersonList();
                if(mPersonInfoMap!=null){
                    List<ManageablePersonInfo> plist = needDownloadPersons();
                    Message msg = new Message();
                    if (plist!=null && plist.size()>0) {
                        msg.obj = plist;
                        msg.what = SUCCESSMANAGEABLEPERSONS;
                        handler.sendMessage(msg);
                    }
                    else        //无需下载特征值进入
                    {
                        msg.what = SUCCESSBATCHFEATUREFILE;
                        msg.obj = null;
                        msg.arg1 = 2;
                        handler.sendMessage(msg);
                    }
                }
            }
        });
    }

    /**
     * 查询门禁机所有广告更新本地数据库
     * @param playId
     */
    public void getADSrc(final String playId){
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                String str = "";
                playlistADInfos.clear();
                playlistADInfos.addAll(comBusiness.getAllADInfo());
                int size = playlistADInfos.size();
                if (size >= 1){
                    for (int i=0;i<size;i++){
                        ADInfo adInfo = playlistADInfos.get(i);
                        String resId = adInfo.getResId();
                        String dir = playlistDir+adInfo.getFilename();
                        Cursor csr = playListDb.rawQuery("select * from " + PlaylistDatabaseHelper.TABLE_NAME
                                + " where " + PlaylistDatabaseHelper.SRC_ID + " = ?",new String[]{resId});
                        Log.d(mTAG, "ad size = "+size + " i = "+i + " resId:"+resId);
                        if (!csr.moveToNext()){
                            if (OkHttpUtil.getDownloadFile(adInfo.getUrl(), dir)){
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(PlaylistDatabaseHelper.SRC_ID, resId);
                                contentValues.put(PlaylistDatabaseHelper.PLAYTIME, adInfo.getPlaytime());
                                contentValues.put(PlaylistDatabaseHelper.FILEPATH, dir);
                                contentValues.put(PlaylistDatabaseHelper.FILESIZE, adInfo.getFilesize());
                                contentValues.put(PlaylistDatabaseHelper.TYPE, adInfo.getType());
                                long row = playListDb.insert(PlaylistDatabaseHelper.TABLE_NAME, null, contentValues);
                                str = "更新本地广告 : "+row + " name:"+dir + " resId:"+resId;
                                Log.i(mTAG, str);
                                LogFile.getInstance().saveMessage(str);
                            }else {
                                str = "广告下载失败：" + resId;
                                Log.i(mTAG, str);
                                LogFile.getInstance().saveMessage(str);
                            }
                        }else {
                            str = "广告无需更新：" + resId;
                            Log.i(mTAG, str);
                            LogFile.getInstance().saveMessage(str);
                        }
                    }
                    //更新本地数据库
                    List<ADInfo> adInfos = new ArrayList<>();
                    adInfos.addAll(playlistADInfos);
                    Cursor cursor = playListDb.rawQuery("select * from " + PlaylistDatabaseHelper.TABLE_NAME, null);
                    str = "本地共有广告" + cursor.getCount() + " 条";
                    Log.i(mTAG, str);
                    LogFile.getInstance().saveMessage(str);
                    while (cursor.moveToNext()){
                        int size1 = adInfos.size();
                        String sid = cursor.getString(cursor.getColumnIndex(PlaylistDatabaseHelper.SRC_ID));
                        boolean include = false;
                        for (int i=0;i<size1;i++){
                            if (sid.equals(adInfos.get(i).getResId())){
                                adInfos.remove(i);
                                include = true;
                                break;
                            }
                        }
                        if (include == false){
                            playListDb.delete(PlaylistDatabaseHelper.TABLE_NAME,
                                    PlaylistDatabaseHelper.SRC_ID + " = ?",new String[]{sid});
                            String dir = cursor.getString(cursor.getColumnIndex(PlaylistDatabaseHelper.FILEPATH));
                            File file = new File(dir);
                            if (file.exists()){
                                file.delete();
                            }
                            str = "本条广告无效："+ sid + " 本地删除";
                            Log.i(mTAG, str);
                            LogFile.getInstance().saveMessage(str);
                        }
                    }
                }
                //获取广告资源是否成功都进入下一流程
                handler.sendEmptyMessage(SUCCESSGETADSRC);
            }
        });
    }

    /**
     * 把管理人员列表存入本地数据库-old获取需要下载特征值的人员id列表
     * @return
     */
    public List<ManageablePersonInfo> needDownloadPersons(){

        List<ManageablePersonInfo> plist = new ArrayList<>();
        int size = mPersonInfoMap.size();
        String str = "管理人员列表:"+size + " 获取需要下载特征值的人员id列表";
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
////        try {
////            featDb.execSQL("create table if not exists " + FeatDatabaseHelper.TABLE_MANAGEABLEPERSON
////                    + " (id integer primary key autoincrement," + FeatDatabaseHelper.PERSON_ID+" text,"
////                    + FeatDatabaseHelper.NAME+" text," + FeatDatabaseHelper.IDCARD_NO+" text,"
////                    + FeatDatabaseHelper.HASHCODE+" text," + FeatDatabaseHelper.VISFACEREGISTERED+" integer,"
////                    + FeatDatabaseHelper.NIFFACEREGISTERED+" integer)");
////        }catch (SQLException e){
////            Log.e(mTAG, "needDownloadPersons:"+e.toString());
////        }
//
//        if (size > 0){
//            Cursor csr = null;
//            for (int i=0;i<size;i++){
//                ManageablePersonInfo manageablePersonInfo = new ManageablePersonInfo();
//                manageablePersonInfo = mPersonIfos.get(i);
//                String pid = manageablePersonInfo.getPid();
//                String name = manageablePersonInfo.getName();
//                String idno= manageablePersonInfo.getIdNo();
//                String hashcode = manageablePersonInfo.getHashcode();
//                boolean faceVis = manageablePersonInfo.isVisibleFaceRegistered();
//                boolean faceNir = manageablePersonInfo.isNirFaceRegistered();
//                Log.i(mTAG, "pid:"+pid + " hashcode:"+hashcode);
////                csr = featDb.rawQuery("select * from " + FeatDatabaseHelper.TABLE_NAME
////                        + " where " + FeatDatabaseHelper.PERSON_ID + " = ? ", new String[]{pid});
//                csr = featDb.query(FeatDatabaseHelper.TABLE_NAME, new String[]{"*"},
//                        FeatDatabaseHelper.PERSON_ID+" = ?", new String[]{pid},
//                        null, null, null);
//                boolean flag = csr.moveToNext();
//                boolean flagHashcode = hashcode.equals("null");
//                Log.d(mTAG, "flag:"+flag + " hashcode:"+flagHashcode);
//                if (!flag)  //没有记录
//                {
//                    if (!hashcode.equals("null")) {
//                        plist.add(manageablePersonInfo);
//                        Log.d(mTAG, "无记录添加");
//                    }
//                }else       //有记录
//                {
//                    String hc = csr.getString(csr.getColumnIndex(FeatDatabaseHelper.HASHCODE));
//                    if (flagHashcode)            //管理人员列表中hashcode为空需清除本地记录数据
//                    {
//                        String dir = csr.getString(csr.getColumnIndex(FeatDatabaseHelper.FEATDATA_PATH));
//                        int index = dir.lastIndexOf("/");
//                        UtilCommon.deleteDirectoryFiles(dir.substring(0, index));
//                        int row = featDb.delete(FeatDatabaseHelper.TABLE_NAME,
//                                FeatDatabaseHelper.PERSON_ID+"=?", new String[]{pid});
//                        str = "管理人员:"+pid + " hashcode:"+hashcode + " 清除本地文件及数据库:"+row;
//                        Log.i(mTAG, str);
//                        LogFile.getInstance().saveMessage(str, true);
//                    }else if (!hc.equals(hashcode)){
//                        plist.add(manageablePersonInfo);
//                        Log.d(mTAG, "有记录添加:"+hc);
//                    }
//                }
//                //
////                Cursor cursor = featDb.rawQuery("select * from " + FeatDatabaseHelper.TABLE_MANAGEABLEPERSON
////                        + " where " + FeatDatabaseHelper.PERSON_ID + " = ? ", new String[]{pid});
////                boolean flag1 = cursor.moveToNext();
////                if (!flag1){
////                    ContentValues contentValues = new ContentValues();
////                    contentValues.put(FeatDatabaseHelper.PERSON_ID, pid);
////                    contentValues.put(FeatDatabaseHelper.NAME, name);
////                    contentValues.put(FeatDatabaseHelper.IDCARD_NO, idno);
////                    contentValues.put(FeatDatabaseHelper.HASHCODE, hashcode);
////                    contentValues.put(FeatDatabaseHelper.VISFACEREGISTERED, faceVis);
////                    contentValues.put(FeatDatabaseHelper.NIFFACEREGISTERED, faceNir);
////                    long row = featDb.insert(FeatDatabaseHelper.TABLE_MANAGEABLEPERSON, null, contentValues);
////                    str = "添加到本地："+pid;
////                    Log.i(mTAG, str);
////                    LogFile.getInstance().saveMessage(str, true);
////                }
//            }
//            csr.close();
//            size = plist.size();
//            str = "待下载特征值人员数："+size;
//            Log.i(mTAG, str);
//            LogFile.getInstance().saveMessage(str, true);
//            if (size > 0){
//                return plist;
//            }
//        }
        try {
            featDb = fDbHelper.getReadableDatabase();
        } catch (Exception e){
            str = "needDownloadPersons get featDb error:"+e.toString();
            Log.e(mTAG, str);
            LogFile.getInstance().saveMessage(str);
        }

        if(featDb.isOpen() && size>0){
            try{
                featDb.beginTransaction();
                for (String key: mPersonInfoMap.keySet()) {
                    ManageablePersonInfo manageablePersonInfo = new ManageablePersonInfo();
                    manageablePersonInfo = mPersonInfoMap.get(key);
                    String pid = manageablePersonInfo.getPid();
                        String name = manageablePersonInfo.getName();
                    String idno= manageablePersonInfo.getIdNo();
                    String hashcode = manageablePersonInfo.getHashcode();
                    boolean faceVis = manageablePersonInfo.isVisibleFaceRegistered();
                    boolean faceNir = manageablePersonInfo.isNirFaceRegistered();
                    Cursor cursor = featDb.rawQuery("select * from " + FeatDatabaseHelper.TABLE_MANAGEABLEPERSON
                        + " where " + FeatDatabaseHelper.PERSON_ID + " = ? ", new String[]{pid});
                    boolean flag1 = cursor.moveToNext();
                    if (!flag1){
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(FeatDatabaseHelper.PERSON_ID, pid);
                        contentValues.put(FeatDatabaseHelper.NAME, name);
                        contentValues.put(FeatDatabaseHelper.IDCARD_NO, idno);
                        contentValues.put(FeatDatabaseHelper.HASHCODE, hashcode);
                        contentValues.put(FeatDatabaseHelper.VISFACEREGISTERED, faceVis);
                        contentValues.put(FeatDatabaseHelper.NIFFACEREGISTERED, faceNir);
                        long row = featDb.insert(FeatDatabaseHelper.TABLE_MANAGEABLEPERSON, null, contentValues);
                        str = "添加到本地："+pid;
                        Log.i(mTAG, str);
                        LogFile.getInstance().saveMessage(str);
                    }
                    cursor.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                //显示的设置数据事务是否成功
                featDb.setTransactionSuccessful();
                featDb.endTransaction();

                featDb.close();
            }
        }
        return null;
    }

    /**
     * 检测是否有需要上传的特征值
     */
    public void needUploadPersons(){
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                String str = "检测是否有需要上传的特征值";
                try {
                    featDb = fDbHelper.getReadableDatabase();
                } catch (Exception e){
                    str = "needUploadPersons get featDb error:"+e.toString();
                    Log.e(mTAG, str);
                    LogFile.getInstance().saveMessage(str);
                }
                List<PersonRegFilesInfo> prfiList = new ArrayList<>();
                if (featDb.isOpen()) {
                    featDb.beginTransaction();
                    Cursor csr = featDb.rawQuery("select * from " + FeatDatabaseHelper.TABLE_NAME
                            + " where " + FeatDatabaseHelper.NEEDUPLOAD + " = ? ", new String[]{String.valueOf(1)});
                    str = str + ":"+csr.getCount();
                    Log.i(mTAG, str);
                    LogFile.getInstance().saveMessage(str);
                    while (csr.moveToNext()) {
                        String pid = csr.getString(csr.getColumnIndex(FeatDatabaseHelper.PERSON_ID));
                        str = "有特征值需要上传:" + pid;
                        Log.i(mTAG, str);
                        LogFile.getInstance().saveMessage(str);
                        PersonRegFilesInfo prfi = new PersonRegFilesInfo();
                        prfi.setPid(pid);
                        prfi.setPicType(FaceDetRec.PICTYPEVIS);
                        prfi.setFeatFileName("files");
                        prfi.setFaceFileName("images");
                        List<String> featFilePaths = new ArrayList<>();             //特征值文件路径
                        List<String> faceFilePaths = new ArrayList<>();             //人脸照片文件路径
                        Cursor cursor = featDb.rawQuery("select * from " + "Person" + pid, null);
                        while (cursor.moveToNext()) {
                            featFilePaths.add(cursor.getString(cursor.getColumnIndex(FeatDatabaseHelper.FEATFILES)));
                            faceFilePaths.add(cursor.getString(cursor.getColumnIndex(FeatDatabaseHelper.FACEFILES)));
                        }
                        if (featFilePaths.size()==0 || faceFilePaths.size()==0){
                            int row = featDb.delete(FeatDatabaseHelper.TABLE_NAME,
                                    FeatDatabaseHelper.PERSON_ID+" = ?", new String[]{pid});
                            str = "人员:"+pid + " 无对应特征值数据 feats:"+featFilePaths.size()
                                    +" faces:"+faceFilePaths.size() + " 清除本条数据:"+row;
                            Log.i(mTAG, str);
                            LogFile.getInstance().saveMessage(str);
                            continue;
                        }
                        prfi.setFeatFilePaths(featFilePaths);
                        prfi.setFaceFilePaths(faceFilePaths);
                        prfiList.add(prfi);
                        Log.d(mTAG, "featFilePaths:" + featFilePaths.size());
                    }
                    featDb.setTransactionSuccessful();
                    featDb.endTransaction();
                    featDb.close();
                }
                Log.d(mTAG, "prfiList:"+prfiList.size());
                Message msg = new Message();
                //无需上传特征值则更新本地数据库
                if (prfiList.size()>0){
                    msg.what = NEEDUPLOADPERSON;
                    msg.obj = prfiList;
                }else {
                    msg.what = SUCCESSUPLOADFEATURE;
                }
                handler.sendMessage(msg);
            }
        });
    }

    /**
     * 批量获取人员近红外特征值并按personID对应存储
     * @param pers
     */
    public void getBatchFeatureFile(final List<ManageablePersonInfo> pers){
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                String fp = comBusiness.getBatchFeatureFile(pers);
                Message msg = new Message();
                if (fp != null){
                    Log.d(mTAG, "download batch feat file successful:"+fp);
                    List<String> plist = UtilCommon.savePersonsFeatureFile(fp);
                    msg.obj = pers;
                    msg.what = SUCCESSBATCHFEATUREFILE;
                    handler.sendMessage(msg);
                }else {
                    msg.what = FAILEDBATCHFEATUREFILE;
                    handler.sendMessage(msg);
                }
            }
        });
    }

    /**
     * 更新本地数据库
     * @param plist 已经下载了特征值的人员信息列表
     */
    public void updateLocalDatabase(final List<ManageablePersonInfo> plist, int ver){
        //更新本地数据库
        if (ver==2 && plist==null && mPersonInfoMap.size()>0){
            scheduledThread.execute(new Runnable() {
                @Override
                public void run() {
                    String str = "更新管理人员本地数据库";
                    try {
                        featDb = fDbHelper.getReadableDatabase();
                    } catch (Exception e){
                        str = "updateLocalDatabase get featDb error:"+e.toString();
                        Log.e(mTAG, str);
                        LogFile.getInstance().saveMessage(str);
                    }
                    if(featDb.isOpen()) {
                        try {
                            featDb.beginTransaction();
                            Cursor cursor = featDb.query(FeatDatabaseHelper.TABLE_MANAGEABLEPERSON,
                                    null, null, null, null,
                                    null, null);
                            str = str + ":"+cursor.getCount();
                            Log.i(mTAG, str);
                            LogFile.getInstance().saveMessage(str);
                            while (cursor.moveToNext()){
                                String pid = cursor.getString(cursor.getColumnIndex(FeatDatabaseHelper.PERSON_ID));
                                if(!mPersonInfoMap.containsKey(pid)){
                                    int row = featDb.delete(FeatDatabaseHelper.TABLE_MANAGEABLEPERSON,
                                            FeatDatabaseHelper.PERSON_ID+" = ?", new String[]{pid});
                                    str = "删除:"+pid + " 管理人员本地数据:"+row;
                                    Log.e(mTAG, str);
                                    LogFile.getInstance().saveMessage(str);
                                }
                            }
                            cursor = featDb.query(FeatDatabaseHelper.TABLE_MANAGEABLEPERSON,
                                    null, null, null, null,
                                    null, null);
                            str = "更新后管理人员本地数据:"+cursor.getCount();
                            Log.i(mTAG, str);
                            LogFile.getInstance().saveMessage(str);
                            cursor.close();
                        }catch(Exception e){
                            e.printStackTrace();
                        }finally{
                            //显示的设置数据事务是否成功
                            featDb.setTransactionSuccessful();
                            featDb.endTransaction();
                            featDb.close();
                        }
                    }
                }
            });
        }

        int size = 0;
        if (plist != null){
            size = plist.size();
        }
        strLog = "updateLocalDatabase - Persons:"+size;
        Log.i(mTAG, strLog);
        LogFile.getInstance().saveMessage(strLog);
        if (size <= 0){
            return;
        }
        final int finalSize = size;
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                String str = "";
                //1 - 管理人员列表为最新数据,检查本地数据库是否需要更新
                for (int i = 0; i< finalSize; i++){
                    int stateUpdate = 0;
                    ManageablePersonInfo mpi = plist.get(i);
                    String pid = mpi.getPid();
                    String idno = mpi.getIdNo();
                    String hashcode = mpi.getHashcode();
                    str = "pid:"+pid + " hashcode:"+hashcode;
                    Cursor csr = featDb.rawQuery("select * from " + FeatDatabaseHelper.TABLE_NAME
                            + " where " + FeatDatabaseHelper.PERSON_ID + " = ? ", new String[]{pid});
                    if (!csr.moveToNext()) {
                        str = str + " 无本地记录";
                        stateUpdate = 1;
                    }else {
                        String hc = csr.getString(csr.getColumnIndex(FeatDatabaseHelper.HASHCODE));
                        if (hc.equals(hashcode)){
                            str = str + " 本地已有记录";
                            Log.d(mTAG, str);
                            continue;
                        }else {
                            str = str + " 本地记录需要更新" + " hc:"+hc;
                            stateUpdate = 2;
                        }
                    }
                    csr.close();
                    Log.i(mTAG, str);
                    LogFile.getInstance().saveMessage(str);

                    String fPath = FaceDetRec.dirFeatData + pid + "/" + pid + ".feat";
                    ContentValues contentValues = new ContentValues();
                    if (stateUpdate == 1){
                        contentValues.put(FeatDatabaseHelper.PERSON_ID, pid);
                        contentValues.put(FeatDatabaseHelper.NAME, mpi.getName());
                        contentValues.put(FeatDatabaseHelper.HASHCODE, hashcode);
                        contentValues.put(FeatDatabaseHelper.IDCARD_NO, idno);
                        contentValues.put(FeatDatabaseHelper.FEATDATA_PATH, fPath);
                        contentValues.put(FeatDatabaseHelper.ENTER_COUNT, 0);
                        String stime = UtilCommon.getTimeCurrent();
                        contentValues.put(FeatDatabaseHelper.ENTER_TIME, stime);
                        contentValues.put(FeatDatabaseHelper.CREATE_TIME, stime);
                        contentValues.put(FeatDatabaseHelper.AVAILABLE, 1);
                        contentValues.put(FeatDatabaseHelper.NEEDUPLOAD, 0);
                        long row = featDb.insert(FeatDatabaseHelper.TABLE_NAME, null, contentValues);
                        str = "新增数据 : "+row + " id:"+idno + " time:"+stime;
                    }else if (stateUpdate == 2){
                        contentValues.put(FeatDatabaseHelper.HASHCODE, hashcode);
                        contentValues.put(FeatDatabaseHelper.NEEDUPLOAD, 0);
                        int row = featDb.update(FeatDatabaseHelper.TABLE_NAME, contentValues,
                                FeatDatabaseHelper.PERSON_ID + " = ?", new String[]{pid});
                        str = "更新数据 : "+row + " pid:"+pid;
                    }
                    Log.i(mTAG, str);
                    LogFile.getInstance().saveMessage(str);
                }
                //2 - 去除数据库中比人员列表中多于的记录
                List<ManageablePersonInfo> mpiList = new ArrayList<>();
                mpiList.addAll(mPersonIfos);
                Cursor cursor = featDb.rawQuery("select * from " + FeatDatabaseHelper.TABLE_NAME,
                        null);
                str = "更新前本地特征值记录共 " + cursor.getCount() + " 条";
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                while (cursor.moveToNext()){
                    int size = mpiList.size();
                    String pid = cursor.getString(cursor.getColumnIndex(FeatDatabaseHelper.PERSON_ID));
                    boolean include = false;
                    for (int i=0;i<size;i++){
                        if (pid.equals(mpiList.get(i).getPid())){
                            mpiList.remove(i);
                            include = true;
                            break;
                        }
                    }
                    if (include == false){
                        String dir = cursor.getString(cursor.getColumnIndex(FeatDatabaseHelper.FEATDATA_PATH));
                        int index = dir.lastIndexOf("/");
                        UtilCommon.deleteDirectoryFiles(dir.substring(0, index));
                        int row = featDb.delete(FeatDatabaseHelper.TABLE_NAME,
                                FeatDatabaseHelper.PERSON_ID+"=?", new String[]{pid});
                        int row1 = featDb.delete(FeatDatabaseHelper.TABLE_MANAGEABLEPERSON,
                                FeatDatabaseHelper.PERSON_ID+"=?", new String[]{pid});
                        str = "本条无效："+ pid + " 删除本地文件及数据库:"+row + ","+row1;
                        Log.i(mTAG, str);
                        LogFile.getInstance().saveMessage(str);
                    }
                }
                //
                featDb.rawQuery("select * from " + FeatDatabaseHelper.TABLE_NAME,
                        null);
                str = "更新后本地特征值记录共 " + cursor.getCount() + " 条";
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                cursor.close();
                handler.sendEmptyMessage(SUCCESSPERSONDATABASE);
            }
        });
    }

    /**
     * 获取设备的识别通过阈值 - 此接口将取消
     */
    public void getConfigMachine(){
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                String data = comBusiness.getConfigMachine();
                if(data != null){
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        heartbeatTime = (float) jsonObject.getDouble("heartbeatTime");
                        matchScorePass = (float) jsonObject.getDouble("matchScorePass");
                        compared = (float) jsonObject.getDouble("compared");
                        faceImageSize = jsonObject.getInt("faceImageSize");
                        roll = (float) jsonObject.getDouble("roll");
                        pitch = (float) jsonObject.getDouble("pitch");
                        yaw = (float) jsonObject.getDouble("yaw");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(SUCCESSSCOREPASS);
                }else {
                    handler.sendEmptyMessage(SUCCESSSCOREPASS);
                }
            }
        });
    }

    /**
     * 根据人员ID查询人员信息
     * @param pid
     */
    public void getPersonInfo(final String pid){
        new Thread(new Runnable() {
            @Override
            public void run() {
                comBusiness.getPersonInfoWithId(pid);
            }
        }).start();
    }

    public void playADSrc(){
        if (isPlaying){
            return;
        }
        csrPlaylist = playListDb.rawQuery("select * from " + PlaylistDatabaseHelper.TABLE_NAME,
                null);
        numADSrc = csrPlaylist.getCount();
        String str = "本地广告数：" + numADSrc;
        Log.i(mTAG, str);
        LogFile.getInstance().saveMessage(str);
//        setVolume(0.1f, ADView);
        if(numADSrc>0){
            startPlay();
        }

        //
        ADView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                handler.sendEmptyMessage(PLAYCONTINUE);
            }
        });
    }
    Bitmap bitmap = null;
    private void startPlay() {
        String str = "";
        isPlaying = true;

        if (csrPlaylist.moveToNext()){
            int type = csrPlaylist.getInt(csrPlaylist.getColumnIndex(PlaylistDatabaseHelper.TYPE));
            final String path = csrPlaylist.getString(csrPlaylist.getColumnIndex(PlaylistDatabaseHelper.FILEPATH));
            str = "正在播放:"+path;
            Log.i(mTAG, str);
            LogFile.getInstance().saveMessage(str);
            if (type == TYPEVIDEO){
//                ADView.setVisibility(View.VISIBLE);
//                ADView.setFocusable(false);
//                ADView.setFocusableInTouchMode(false);
                videoSfView.setVisibility(View.VISIBLE);
                ADImgView.setVisibility(View.GONE);
                File fVideo = new File(path);
                if (fVideo.exists()){
//                    ADView.setVideoPath(fVideo.getAbsolutePath());
//                    ADView.requestFocus();
//                    setVolume(0.05f, ADView);
//                    ADView.start();
                    try {
                        videoADMediaPlayer.reset();
                        videoADMediaPlayer.setDataSource(path);
                        videoADMediaPlayer.prepare();
                        videoADMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else if (type == TYPEIMAGE){
                videoSfView.setVisibility(View.GONE);
                ADImgView.setVisibility(View.VISIBLE);
                bitmap = BitmapFactory.decodeFile(path);
                ADImgView.setImageBitmap(bitmap);
                final int ptime = csrPlaylist.getInt(csrPlaylist.getColumnIndex(PlaylistDatabaseHelper.PLAYTIME));
                showImageSingleThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(ptime*1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handler.sendEmptyMessage(PLAYCONTINUE);
                    }
                });
            }
        }else {
            handler.sendEmptyMessage(PLAYCOMPLETE);
        }
    }

    private void initMediaPlayer() {
        strLog = "initMediaPlayer";
        Log.i(mTAG, strLog);
        LogFile.getInstance().saveMessage(strLog);
        videoADMediaPlayer = new MediaPlayer();
        videoSfHolder = videoSfView.getHolder();
        videoSfHolder.addCallback(new videoSfHolderCallBack());
        videoADMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.i("mediaPlayer", "onPrepared");

            }
        });
        videoADMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.i("mediaPlayer", "onCompletion");
//                mediaPlayer.reset();
                handler.sendEmptyMessage(PLAYCONTINUE);
            }
        });
        //
//        waitSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        // 获取最小缓冲区
        atBufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
        // 实例化AudioTrack(设置缓冲区为最小缓冲区的2倍，至少要等于最小缓冲区)
        audioTrackplayer = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, atBufferSize*2, AudioTrack.MODE_STREAM);
//        jetPlayStream();
    }

    private class videoSfHolderCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i("MyCallBack", "surfaceCreated");
            videoADMediaPlayer.setDisplay(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.i("MyCallBack", "surfaceChanged");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i("MyCallBack", "surfaceDestroyed");
        }
    }

    /**
     * 处理推送消息
     * @param content
     */
    public void resolvePushMessage(final String content){
        pushMessageSingleThread.execute(new Runnable() {
            @Override
            public void run() {
                String str = "处理推送消息:";
                String action = null;
                int pushtype = 0;
                JSONObject jsonData=null, jsonObject=null;
                try {
                    jsonObject = new JSONObject(content);

                    action = jsonObject.getString("action");
                    pushtype = jsonObject.getInt("pushtype");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Message msg = new Message();
                Log.d(mTAG, " pushtype="+pushtype);
                switch (pushtype){
                    case 10500:         //人员信息
                        str = str + " 人员信息：" + action;
                        Log.i(mTAG, str);
                        LogFile.getInstance().saveMessage(str);

                        if (action.equals("add") || action.equals("update") || action.equals("delete")){
                            msg.what = PUSHMESSAGEPERSON;
                            handler.sendMessage(msg);
                        }
                        break;
                    case 10501:         //广告信息
                        str = str + " 广告信息：" + action;
                        Log.i(mTAG, str);
                        LogFile.getInstance().saveMessage(str);
                        if (action.equals("add") || action.equals("update") || action.equals("delete")){
                            msg.what = PUSHMESSAGEAD;
                            handler.sendMessage(msg);
                        }
                        break;
                    case 10502:         //开门、重启
                        str = str + " 设备动作：" + action;
                        Log.i(mTAG, str);
                        LogFile.getInstance().saveMessage(str);
                        if (action.equals("open")){
                            try {
                                jsonData = jsonObject.getJSONObject("data");
                                pidRemoteOpenDoor = jsonData.getString("personId");
                                str = "远程开门成功 - lockId："+jsonData.getString("lockId")
                                    + " personId:"+pidRemoteOpenDoor;
                                Log.i(mTAG, str);
                                LogFile.getInstance().saveMessage(str);
                                handler.sendEmptyMessage(OPENDOORBYREMOTE);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(mTAG, "远程开门:"+e.toString());
                                LogFile.getInstance().saveMessage("远程开门 error:"+e.toString());
                            }
                        }else if (action.equals("reboot")){
                            hardwareMain.restartMachine();
                        }
                        break;
                    case 10503:         //临时密码
                        str = str + " 临时密码生成：" + action;
                        Log.i(mTAG, str);
                        LogFile.getInstance().saveMessage(str);
                        if (action.equals("temppsw")){
                            try {
                                jsonData = jsonObject.getJSONObject("data");
                                String createTime = jsonData.getString("createTime");
                                String pwd = jsonData.getString("psw");
                                String expTime = jsonData.getString("expTime");
                                String id = jsonData.getString("id");
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(PlaylistDatabaseHelper.PWD_ID, id);
                                contentValues.put(PlaylistDatabaseHelper.PWD, pwd);
                                contentValues.put(PlaylistDatabaseHelper.CREATETIME, createTime);
                                contentValues.put(PlaylistDatabaseHelper.EXPTIME, expTime);
                                long row = playListDb.insert(PlaylistDatabaseHelper.PWD_TABLE, null,
                                        contentValues);
                                str = "收到临时密码 - id："+id + " expTime:"+expTime + " 加入数据库："+row;
                                Log.i(mTAG, str);
                                LogFile.getInstance().saveMessage(str);
                                handler.sendEmptyMessage(RECEIVEDTEMPPWD);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case 10504:         //呼叫移动端
                        str = str + " 挂断：" + action;
                        Log.i(mTAG, str);
                        LogFile.getInstance().saveMessage(str);
                        if (action.equals("hangup"))
                        {
//                            flagCallOwenr = false;
                            flagJoinChannel = false;
                        }
                        break;
                    case 10506:
                        str = str + " 上传日志推送接：" + action;
                        Log.i(mTAG, str);
                        LogFile.getInstance().saveMessage(str);
                        if (action.equals("upload")){
                            handler.sendEmptyMessage(NEEDUPLOADLOGFILE);
                        }
                        break;
                    default:
                        str = str + content;
                        Log.i(mTAG, str);
                        LogFile.getInstance().saveMessage(str);
                        break;
                }
            }
        });
    }

    /**
     * 处理键盘事件
     */
    public void resolveKeyboardEvent(){
        String iContent = editText.getText().toString();
        strLog = "键盘事件 输入内容："+iContent;
        Log.i(mTAG, strLog);
        LogFile.getInstance().saveMessage(strLog);
        if (iContent.length()==4 && typeInput==0){
            if (!NetworkUtil.isNetworkAvailable(this)){
                strLog = "当前网络不可用";
                Log.i(mTAG, strLog);
                LogFile.getInstance().saveMessage(strLog);
                Toast.makeText(this, strLog, Toast.LENGTH_SHORT).show();
                return;
            }
            callOwenr(iContent);
        }else if(iContent.length()==6 && typeInput==1){
            verifyPasswordOpenDoor(iContent);
        }else {
            strLog = "输入有误";
            Log.i(mTAG, strLog);
            LogFile.getInstance().saveMessage(strLog);
            Toast.makeText(this, strLog, Toast.LENGTH_SHORT).show();
        }
    }

    private static final int SUCCESSINTERNET = 100;
    private static final int FAILINTERNET = 101;
    private static final int FINISHNETTOKEN = 102;              //检查网络时间和token
    private static final int ALIASNOTEXIST = 103;
    private static final int FAILSERVERS = 104;
    private static final int DATESHOW = 105;
    private static final int INFRAREDDEDECT = 110;

    private static final int SUCCESSREADIDCARD = 111;           //身份证读取成功
    private static final int SUCCESSMACID = 120;                //获取门禁机ID
    private static final int SUCCESSMANAGEABLEPERSONS = 121;    //
    private static final int SUCCESSPERSONDATABASE = 122;
    private static final int SUCCESSGETADSRC = 123;
    private static final int SUCCESSPLAYLIST = 124;
    private static final int SUCCESSBATCHFEATUREFILE = 125;
    private static final int FAILEDBATCHFEATUREFILE = 126;
    private static final int SUCCESSSCOREPASS = 127;
    private static final int NEEDUPLOADPERSON = 128;
    private static final int SUCCESSUPLOADFEATURE = 129;        //上传注册信息
    private static final int FAILEDUPLOADFEATURE = 161;
    private static final int PHOTOTAKEN = 130;                  //拍照存储照片
    private static final int SUCCESSRECOGNIZER = 131;
    private static final int FINISHRECOGNIZER = 132;
    private static final int LOSTPERSON = 133;
    private static final int STILLHAVEPERSON = 134;
    private static final int SUCCESSREGISTER = 141;
    private static final int REGISTERING = 142;
    private static final int FAILEDREGISTER = 143;
    private static final int REJECTREGISTER = 144;
    private static final int EXISTEDREGISTER = 145;
    private static final int PLAYCONTINUE = 150;
    private static final int PLAYCOMPLETE = 151;

    private static final int PUSHMESSAGERECEIVE = 162;
    private static final int PUSHMESSAGEPERSON = 163;
    private static final int PUSHMESSAGEAD = 164;
    private static final int SUCCESSDIALMOBILE = 165;
    private static final int FAILDIALMOBILE = 166;
    private static final int SUCCESSLIVECHAT = 167;
    private static final int TIMEOUTLIVECHAT = 168;
    private static final int TIMELIVECHAT = 169;
    private static final int SUCCESSREADCARD = 170;
    private static final int FAILREADCARD = 171;
    private static final int SUCCESSVERIFYCARD = 172;       //
    private static final int FAILEDVERIFYCARD = 173;        //
    private static final int IDCARDVERIFYWITHPERSON = 174;  //
    private static final int OPENDOORBYREMOTE = 180;
    private static final int RECEIVEDTEMPPWD = 181;
    private static final int VALIDPASSWORD = 182;
    private static final int INVALIDPASSWORD = 183;
    private static final int WRONGPASSWORD = 184;
    private static final int OFFLINEREMOTEUSER = 185;
    private static final int NEEDUPLOADLOGFILE = 186;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SUCCESSINTERNET:
                    checkTimeToken();
                    startTimer();
                    break;
                case FAILINTERNET:
                    break;
                case FINISHNETTOKEN:
                    flagFirstRun = true;
                    if (ComBusiness.idMac.equals("") || mPersonInfoMap.size()==0) {
                        faceDetRec.renewServicePrx();
//                        initCloudChannel();
                        requestMacId();
                    }
                    break;
                case ALIASNOTEXIST:
                    addAliaName();
                    break;
                case FAILSERVERS:

                    break;
                case DATESHOW:
                    dateEt.setText(UtilCommon.getDateTimeWeek());
                    break;
                case INFRAREDDEDECT:
                    int act = msg.arg1;
                    strLog = "红外探测 act = "+act + " isPersonNear:"+isPersonNear
                            + " isCameraRun:"+isCameraRun + " flagCallOwenr:"+flagCallOwenr;
                    Log.i(mTAG, strLog);
                    LogFile.getInstance().saveMessage(strLog);
                    if (act == 2){
                        isPersonNear = true;
                        if (!isCameraRun && !flagCallOwenr) {
                            startPreviewFaceRec();
                        }
                    }
                    else if (act == 1){
                        isPersonNear = true;
                        if (!isCameraRun && !flagCallOwenr) {
                            startPreviewFaceRec();
                        }
                    }else {
                        isPersonNear = false;
                    }
                    break;
                case SUCCESSMACID:
                    getConfigMachine();
//                    checkAliasIsExist();
                    break;
                case LOSTPERSON:
                    stopPreviewFaceRec(false);
                    if (flagOpenDoorPassword){
                        flagOpenDoorPassword = false;
                        saveRecordPasswordOpenDoor(0.0f, faceDetRec.getFpTempPic());
                    }
                    break;
                case STILLHAVEPERSON:
                    takePictureDataWithCamera1();
//                    takePictureData();
                    break;
                case SUCCESSMANAGEABLEPERSONS:
                    getBatchFeatureFile((List<ManageablePersonInfo>) msg.obj);
                    break;
                case SUCCESSPERSONDATABASE:

                    break;
                case SUCCESSSCOREPASS:
                    getADSrc(null);
                    break;
                case SUCCESSGETADSRC:
                    //重新查询广告数据库
                    csrPlaylist = playListDb.rawQuery("select * from " + PlaylistDatabaseHelper.TABLE_NAME, null);
                    if (!isPlaying){
                        startPlay();
                    }
                    strLog = "更新播放本地广告数据库 ："+csrPlaylist.getCount();
                    Log.i(mTAG, strLog);
                    LogFile.getInstance().saveMessage(strLog);
                    if (flagFirstRun) {
                        needUploadPersons();
                    }
                    break;
                case NEEDUPLOADPERSON:
                    sendFaceFeatureFiles((List<PersonRegFilesInfo>) msg.obj);
                    break;
                case SUCCESSBATCHFEATUREFILE:
                    updateLocalDatabase((List<ManageablePersonInfo>) msg.obj, msg.arg1);
                    break;
                case SUCCESSRECOGNIZER:
                    openLockWithPulse();
                    DecimalFormat df = new DecimalFormat("0.00");
                    float sim = Float.intBitsToFloat(msg.arg1);
                    strLog = "识别成功"+ " 欢迎您";
                    Log.i(mTAG, strLog);
                    LogFile.getInstance().saveMessage(strLog);
                    TTSUtils.getInstance().speak(strLog);
                    Toast.makeText(MainActivity.this, strLog, Toast.LENGTH_SHORT).show();
                    break;
                case FINISHRECOGNIZER:
                    sendFaceRecord((List<SimilarPerson>) msg.obj, faceDetRec.getFpStrangerPic(),
                            faceDetRec.getFpStrangerPicNir());
                    /** 识别成功不关闭窗口 */
                    if (Float.intBitsToFloat(msg.arg1)>matchScorePass && !flagCallOwenr) {
                        backupDeleteUserData();
                        delayTakePhoto(4);
                        //延时启动摄像头
                    }else {
                        delayTakePhoto(1);
                    }
                    if(flagOpenDoorPassword){
                        flagOpenDoorPassword = false;
                        saveRecordPasswordOpenDoor(((List<SimilarPerson>) msg.obj).get(0).getScore(),
                                faceDetRec.getFpStrangerPic());
                    }
                    if (flagTryPassword){
                        flagTryPassword = false;
                        saveRecordPasswordOpenDoor(((List<SimilarPerson>) msg.obj).get(0).getScore(),
                                faceDetRec.getFpStrangerPic());
                    }
                    break;
                case PHOTOTAKEN:
                    /** 获取抓拍照片上传访客呼叫记录 */
                    if (needCatchPhoto){
                        needCatchPhoto = false;
                        UtilCommon.copyFile(faceDetRec.getFpTempPic(), catchPhotoFP);
                        /** 注意对讲过程中会出现远程开门 并行判断开门在前*/
                        if (flagOpenDoorRemote){
                            flagOpenDoorRemote = false;
                            saveRecordRemoteOpenDoor(catchPhotoFP);
                        }else if (flagGuestCallRecord) {
                            flagGuestCallRecord = false;
                            sendGuestCallOwnerRecord(editText.getText().toString(), catchPhotoFP);
                        }
                    }
                    break;
                case SUCCESSREADIDCARD:
                    Toast.makeText(MainActivity.this,"身份证读取成功",Toast.LENGTH_SHORT).show();
                    break;
                case REGISTERING:
//                    takePictureData(1);
                    takePictureDataWithCamera1();
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(msg.arg1);
                    progressBar.setSecondaryProgress(msg.arg1+1);
                    break;
                case SUCCESSREGISTER:
                    ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) cameraViewContainer.getLayoutParams();
                    layoutParams.width = regWindowWidth;
                    layoutParams.height = regWindowHeight;
                    cameraViewContainer.setLayoutParams(layoutParams);
                    progressBar.setVisibility(View.GONE);
                    stopPreviewFaceRec(true);
                    needUploadPersons();
                    strLog = "注册成功";
                    TTSUtils.getInstance().speak(strLog);
                    Toast.makeText(MainActivity.this, strLog, Toast.LENGTH_SHORT).show();
                    //延时后打开摄像窗口
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(4000);
                                isCameraRun = false;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                case FAILEDREGISTER:
                    layoutParams = (ConstraintLayout.LayoutParams) cameraViewContainer.getLayoutParams();
                    layoutParams.width = regWindowWidth;
                    layoutParams.height = regWindowHeight;
                    cameraViewContainer.setLayoutParams(layoutParams);
                    progressBar.setVisibility(View.GONE);
                    strLog = "注册未完成，请重新尝试";
                    TTSUtils.getInstance().speak(strLog);
                    Log.i(mTAG, strLog);
                    LogFile.getInstance().saveMessage(strLog);
                    Toast.makeText(MainActivity.this, strLog, Toast.LENGTH_SHORT).show();
//                    takePictureDataWithCamera1();
//                    readIDCard();
                    break;
                case REJECTREGISTER:
                    strLog = "网络异常无法注册";
                    Toast.makeText(MainActivity.this,strLog,Toast.LENGTH_SHORT).show();
                    break;
                case EXISTEDREGISTER:
                    Toast.makeText(MainActivity.this,"此身份证已经注册",Toast.LENGTH_SHORT).show();
                    break;
                case PLAYCONTINUE:
                    startPlay();
                    break;
                case PLAYCOMPLETE:
                    csrPlaylist.moveToPosition(-1);
                    startPlay();
                    break;
                case SUCCESSUPLOADFEATURE:
                    getManageablePersons();
                    break;
                case FAILEDUPLOADFEATURE:

                    break;
                case PUSHMESSAGERECEIVE:
                    resolvePushMessage((String) msg.obj);
                    break;
                case PUSHMESSAGEPERSON:
                    getManageablePersons();
                    break;
                case PUSHMESSAGEAD:
                    flagFirstRun = false;
                    getADSrc(null);
                    break;
                case SUCCESSDIALMOBILE:
                    flagReadIdCard = false;
                    joinChannel((String) msg.obj);
                    jetPlayStream();
                    break;
                case FAILDIALMOBILE:
                    Toast.makeText(MainActivity.this, "呼叫失败请重试", Toast.LENGTH_SHORT).show();
                    TTSUtils.getInstance().speak("呼叫失败请重试");
                    editText.setText("");
                    break;
                case SUCCESSLIVECHAT:
//                    waitSoundPool.stop(waitSoundId);
                    audioTrackplayer.pause();
                    limitCallTime(58);
                    Toast.makeText(MainActivity.this, "呼叫业主已经连通", Toast.LENGTH_SHORT).show();
                    break;
                case TIMEOUTLIVECHAT:
                    connectedLiveChat = false;
                    audioTrackplayer.pause();
                    leaveChannel();
                    if (msg.arg1 == 1){
                        TTSUtils.getInstance().speak("呼叫等待超时");
                    }else if (msg.arg1 == 2){
//                        TTSUtils.getInstance().speak("对方离开对话");
                        playVoiceInThread("对方离开通话", 2);
                    }
                    editText.setText("");
                    Toast.makeText(MainActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case TIMELIVECHAT:
                    Toast.makeText(MainActivity.this, "通话还剩 "+msg.arg1+" 秒结束", Toast.LENGTH_SHORT).show();
                    break;
                case SUCCESSREADCARD:
                    saveIdCardInfo(msg.obj);
                    if (msg.arg1 == 1){
                        strLog = "证件已授权";
                    }else if (msg.arg1 == 0){
                        strLog = "证件未授权";
                        TTSUtils.getInstance().speak(strLog);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                readIDCard();
                            }
                        });
                    }
                    Log.i(mTAG, strLog);
                    LogFile.getInstance().saveMessage(strLog);
                    Toast.makeText(MainActivity.this, strLog, Toast.LENGTH_SHORT).show();
                    break;
                case FAILREADCARD:
                    Log.d(mTAG, " FAILREADCARD flagReadIdCard:"+flagReadIdCard);
                    if (flagReadIdCard) {
                        readIDCard();
                    }
                    break;
                case IDCARDVERIFYWITHPERSON:
                    if (msg.arg1 == 1) {
                        layoutParams = (ConstraintLayout.LayoutParams) cameraViewContainer.getLayoutParams();
                        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
                        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
                        cameraViewContainer.setLayoutParams(layoutParams);
                        progressBar.bringToFront();
                    } else {
                        if (flagReadIdCard) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    readIDCard();
                                }
                            });
                        }
                    }
                    TTSUtils.getInstance().speak((String) msg.obj);
                    break;
                case OPENDOORBYREMOTE:
                    /** 在远程开门记录未上传前不允许再次调用 */
                    if (flagOpenDoorRemote){
                        break;
                    }else {
                        flagOpenDoorRemote = true;
                    }
                    capturePhoto();
                    openLockWithPulse();
                    Toast.makeText(MainActivity.this,"远程开门成功",Toast.LENGTH_LONG).show();
                    break;
                case RECEIVEDTEMPPWD:
                    Toast.makeText(MainActivity.this,"收到临时密码",Toast.LENGTH_LONG).show();
                    break;
                case VALIDPASSWORD:
                    /** 在密码开门记录未上传前不允许再次调用 */
                    if (flagOpenDoorPassword){
                        break;
                    }else {
                        flagOpenDoorPassword = true;
                    }
                    openLockWithPulse();
                    editText.setText("");
                    Toast.makeText(MainActivity.this,"密码开门成功",Toast.LENGTH_LONG).show();
                    break;
                case INVALIDPASSWORD:
                    flagTryPassword = true;
                    editText.setText("");
                    Toast.makeText(MainActivity.this,"密码已过期",Toast.LENGTH_LONG).show();
                    break;
                case WRONGPASSWORD:
                    flagTryPassword = true;
                    editText.setText("");
                    Toast.makeText(MainActivity.this,"无效密码",Toast.LENGTH_LONG).show();
                    break;
                case OFFLINEREMOTEUSER:
                    leaveChannel();
                    connectedLiveChat = false;
                    break;
                case NEEDUPLOADLOGFILE:
                    uploadLogfile();
                    break;
            }
        }
    };

    /**
     * 保存身份证读取记录(照片以文件形式上传)
     * @param obj
     */
    private void saveIdCardInfo(Object obj) {
        final HashMap<String, String> params = (HashMap<String, String>) obj;
        final String dir = params.get("picIdCardPath");
        params.remove("picIdCardPath");
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                comBusiness.saveRecordIDCardFile(params, dir);          //上传身份证读取记录
            }
        });
    }

    /**
     * 启动预览和人脸识别
     */
    public void startPreviewFaceRec(){
        controlLights(true);
        numPicTaken0 = 0;
        numPicTaken1 = 0;
        cameraSfView.setZOrderMediaOverlay(true);
        cameraViewContainer.bringToFront();
        cameraSfView.setVisibility(View.VISIBLE);
        cameraViewContainer.setVisibility(View.VISIBLE);
        faceDetRec.setTimesLostShadow(0);
        faceDetRec.setNumPicFace(0);
        videoADMediaPlayer.setVolume(0.1f, 0.1f);
        strLog = "startPreviewFaceRec";
        Log.i(mTAG, strLog);
        LogFile.getInstance().saveMessage(strLog);
    }
    /*
    * 停止预览和人脸识别
     */
    public void stopPreviewFaceRec(boolean isShow){
        controlLights(false);
//        mRtcEngine.stopPreview();
        cameraSfView.setVisibility(View.GONE);
        cameraViewContainer.setVisibility(View.GONE);
        videoADMediaPlayer.setVolume(0.8f, 0.8f);
        strLog = "stopPreviewFaceRec camera0:"+camera0;
        Log.i(mTAG, strLog);
        LogFile.getInstance().saveMessage(strLog);
        isCameraRun = isShow;
        flagReadIdCard = false;
    }

    /**
     * 控制键盘、摄像头灯
     * @param flag
     */
    public void controlLights(final boolean flag){
        controlLightsSingleThread.execute(new Runnable() {
            @Override
            public void run() {
                String str;
                try {
                    hardwareMain.controlVisibleCamereLight(flag);
                    Thread.sleep(100);
                    //        hardwareMain.controlNIRCameraLight(true);
                    hardwareMain.controlKeyboardLight(flag);
                    str = "Lights state:"+flag;
                    Log.i(mTAG, str);
                    LogFile.getInstance().saveMessage(str);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    str = "controlLights error:"+flag;
                    Log.e(mTAG, str);
                    LogFile.getInstance().saveMessage(str);
                }
            }
        });
    }

    /**
     * 脉冲开锁：800ms
     */
    public void openLockWithPulse(){
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                String str = "openLockWithPulse.";
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                hardwareMain.controlDoorLock(true);//hardwareMain.openLock();
                long delta=0, delay=750;
                long st = System.currentTimeMillis();
                while (delta < delay){
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    delta = System.currentTimeMillis() - st;
                }
                hardwareMain.controlDoorLock(false);//hardwareMain.closeLock();
            }
        });
    }

    public void playVoiceInThread(final String speech, final int delay){
        playVoiceSingleThread.execute(new Runnable() {
            @Override
            public void run() {
                TTSUtils.getInstance().speak(speech);
                try {
                    Thread.sleep(delay*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     *
     */
    private void jetPlayStream(){
        playWaitAudioSingleThread.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                String str = "读取拨号等待音 volume:"+AudioTrack.getMaxVolume();
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                // 设置音量
//                audioTrack.setVolume(AudioTrack.getMaxVolume()) ;
                // 设置播放频率
//                audioTrack.setPlaybackRate(10) ;
                audioTrackplayer.flush();
                audioTrackplayer.play();
                InputStream is = getResources().openRawResource(R.raw.mqms);
                byte[] buffer = new byte[atBufferSize*2] ;
                int len, total=0;
                try {
                    while((len=is.read(buffer,0,buffer.length)) != -1){
                        total += len;
                        str = "拨号音数据写入:"+total;
                        audioTrackplayer.write(buffer,0,len) ;
                        Log.i(mTAG, str);
                        LogFile.getInstance().saveMessage(str);
                    }
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 人脸照片特征值合并上传
     * @param prfilist
     */
    public void sendFaceFeatureFiles(final List<PersonRegFilesInfo> prfilist){
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                for (PersonRegFilesInfo prfi : prfilist){
                    if(comBusiness.sendFace_FeatureFiles(prfi)){
                        String str = "";
                        String pid = FeatDatabaseHelper.PERSON_TABLE_PREFIX+prfi.getPid();
                        try {
                            featDb = fDbHelper.getReadableDatabase();
                        } catch (Exception e){
                            str = "sendFaceFeatureFiles get featDb error:"+e.toString();
                            Log.e(mTAG, str);
                            LogFile.getInstance().saveMessage(str);
                        }
                        featDb.delete(pid, null, null);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(FeatDatabaseHelper.NEEDUPLOAD, 0);
                        int row = featDb.delete(FeatDatabaseHelper.TABLE_NAME,
                                FeatDatabaseHelper.PERSON_ID + " = ?", new String[]{pid});
                        featDb.close();
                        str = "清除数据表："+pid + " 清除数据条:"+row;
                        Log.i(mTAG, str);
                        LogFile.getInstance().saveMessage(str);
                    }
                }
                try {
                    Thread.sleep(2*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(SUCCESSUPLOADFEATURE);
            }
        });
    }

    /**
     * 保存门禁机人脸识别记录
     * @param spList
     * @param pathPic
     */
    public void sendFaceRecord(final List<SimilarPerson> spList, final String pathPic, final String fp2){
        uploadRecResultSingleThread.execute(new Runnable() {
            @Override
            public void run() {
                comBusiness.saveFaceRecord(spList, pathPic, fp2, matchScorePass);
            }
        });
    }

    /**
     * 保存访客呼叫业主记录
     * @param cNum
     * @param fPath
     */
    public void sendGuestCallOwnerRecord(final String cNum, final String fPath){
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                String str = "";
                String rid = comBusiness.saveGuestCallOwnerRecord(cNum, fPath);
                if (rid == null){
                    str = "保存访客呼叫业主记录 失败:"+rid;
                    Log.e(mTAG, str);
                    LogFile.getInstance().saveMessage(str);
                    flagCallOwenr = false;
                    handler.sendEmptyMessage(FAILDIALMOBILE);
                }else {
                    String channelName = comBusiness.dialMobile(cNum, rid);
                    str = "门禁设备拨号呼叫移动端";
                    if (channelName == null){
                        str = str + " 失败："+channelName;
                        Log.e(mTAG, str);
                        LogFile.getInstance().saveMessage(str);
                        flagCallOwenr = false;
                        handler.sendEmptyMessage(FAILDIALMOBILE);
                    }else {
                        Message msg = new Message();
                        msg.what = SUCCESSDIALMOBILE;
                        msg.obj = channelName;
                        handler.sendMessage(msg);
                        str = str + " 成功："+channelName;
                        Log.i(mTAG, str);
                        LogFile.getInstance().saveMessage(str);
                    }
                }
            }
        });
    }

    /**
     * 保存远程一键开门记录(照片以文件形式上传)
     * @param fPath
     */
    public void saveRecordRemoteOpenDoor(final String fPath){
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                String str = comBusiness.saveRecordRemoteOpenDoorWithPhoto(pidRemoteOpenDoor, fPath);
                if (str != null){
                    str = "保存远程一键开门记录(照片以文件形式上传) 成功:"+str;
                }else {
                    str = "保存远程一键开门记录(照片以文件形式上传) 失败:"+str;
                }
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
            }
        });
    }
    public void saveRecordPasswordOpenDoor(final float score, final String fPath){
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                String str = comBusiness.saveRecordOpenDoorPasswordWithPhoto(idTempPassword, score, fPath, matchScorePass);
                if (str != null){
                    str = "保存临时密码开门记录(照片以文件形式上传) 成功:"+str;
                }else {
                    str = "保存临时密码开门记录(照片以文件形式上传) 失败:"+str;
                }
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
            }
        });
    }

    /**
     * 连接服务器心跳
     */
    public void connectServer(){
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                boolean flagToken = true;
                String str = comBusiness.heartbeatMachine(flagToken);
                //重新获取token则重新更新广告和人员列表
                if (str!=null && flagToken==false){
                    flagFirstRun = true;
                    handler.sendEmptyMessage(SUCCESSGETADSRC);
                }
                str = "设备心跳返回服务器时间："+str + " flagToken:"+flagToken;
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
            }
        });
    }

    public void uploadLogfile(){
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                comBusiness.saveLogfile(LogFile.nameLogFile);
            }
        });
    }
    /*
    * 启动摄像头预览、拍照、读取身份证
     */
    private void luanchCameraView() {
        Log.d(mTAG, "luanchCameraView");
        cameraSfHolder = cameraSfView.getHolder();
        cameraSfHolder.setKeepScreenOn(true);
        cameraSfHolder.addCallback(new SurfaceHolder.Callback(){
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                String str = "surfaceCreated :"+camera1;
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                initCamera();
                if (camera1 == null){
                    return;
                }
                try {
                    camera1.setPreviewDisplay(surfaceHolder);
                } catch (IOException e) {
                    str = "luanchCameraView surfaceChanged error:"+e.toString();
                    Log.e(mTAG, str);
                    LogFile.getInstance().saveMessage(str);
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                String str = "luanchCameraView - surfaceChanged - format="+i + " width="+i1 + " height="+i2
                        + " flagReadIdCard:"+flagReadIdCard + " isCameraRun:"+isCameraRun;
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                //防止surface改变后再次启动拍照
                if (isCameraRun){
                    return;
                }
                isCameraRun = true;
                if (camera1 == null){
                    stopPreviewFaceRec(true);
                    return;
                }
                try {
                    camera1.startPreview();
                } catch (Exception e){
                    str = "luanchCameraView surfaceChanged error:"+e.toString();
                    Log.e(mTAG, str);
                    LogFile.getInstance().saveMessage(str);
                    e.printStackTrace();
                }
                takePictureDataWithCamera1();
//                camera0.startPreview();
//                takePictureData();
                flagReadIdCard = true;
                readIDCard();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                strLog = "surfaceDestroyed isCameraRun:"+isCameraRun;
                Log.i(mTAG, strLog);
                LogFile.getInstance().saveMessage(strLog);
//                camera0.stopPreview();
//                camera1.stopPreview();
//                if (null != camera0) {
//                    camera0.stopPreview();
//                    camera0.release();
//                    camera0 = null;
//                }
//                if (null != camera1) {
//                    camera1.stopPreview();
//                    camera1.release();
//                    camera1 = null;
//                }
            }
        });

//        initCamera();
//        startPreviewFaceRec();
    }

    private void initCamera() {
        Log.d(mTAG, "camera0:"+camera0 + " camera1:"+camera1);
        if (camera0 == null) {
            try {
                Log.d(mTAG, "initCamera");
                camera0 = Camera.open(0);
                camera1 = Camera.open(1);
                setCamera0Param();
                Log.d(mTAG, "camera0 init successful.");
            } catch (Exception e) {
                String str = "initCamera error:"+e.toString();
                Log.e(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "启动摄像头失败,请开启摄像头权限", Toast.LENGTH_SHORT).show();
            }
        }else {
            Log.d(mTAG, "camera0 is not null");
        }
    }

    public void setCamera0Param(){
        Log.d(mTAG, "setCamera0Param");
        Camera.Parameters parameters = camera0.getParameters();
        int camIndex = camera0.getParameters().getSupportedPictureSizes().size();
        List<Camera.Size> CamPicSize = camera0.getParameters().getSupportedPictureSizes();
        int width = CamPicSize.get(1).width;
        int height = CamPicSize.get(1).height;
        parameters.setPictureSize(widthPhoto, heightPhoto);
        List<String> focusModes = parameters.getSupportedFocusModes();
        camera0.setParameters(parameters);

        Camera.Parameters parameters1 = camera1.getParameters();
        List<Camera.Size> CamPicSize1 = camera1.getParameters().getSupportedPictureSizes();
        Log.d(mTAG, "size="+CamPicSize1.size());
        width = CamPicSize1.get(0).width;
        height = CamPicSize1.get(0).height;
        Log.d(mTAG, "size="+CamPicSize1.size()
                + " 0width="+CamPicSize1.get(0).width + " 0height="+CamPicSize1.get(0).height);
        parameters1.setPictureSize(widthPhoto, heightPhoto);
        parameters1.setJpegQuality(70);
        camera1.setParameters(parameters1);
    }

    public void stopCameras(){
        if (null != camera0) {
            camera0.stopPreview();
            camera0.release();
            camera0 = null;
        }
        if (null != camera1) {
            camera1.stopPreview();
            camera1.release();
            camera1 = null;
        }
    }

    /**
     * 红外摄像头拍照处理流程
     */
    public void takePictureData(){
        if (camera0 == null){
            Log.e(mTAG, "takePicture - camera0:"+camera0);
            return;
        }
        try{
            camera0.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    numPicTaken0 += 1;
                    String str = "camera0 onPictureTaken:"+numPicTaken0 + " raw pic data="+bytes.length;
                    Log.i(mTAG, str);
                    LogFile.getInstance().saveMessage(str);
                    if (bytes.length > 0) {
//                        faceDetRec.saveFaceDataNir(bytes);
                    }
                    try {
                        camera.startPreview();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 可见光摄像头拍照进入人脸识别流程
     */
    public void takePictureDataWithCamera1(){
        if (camera1 == null){
            Log.e(mTAG, "takePicture - camera1:"+camera1);
            LogFile.getInstance().saveMessage("takePicture - camera1:"+camera1);
            return;
        }
        try{
            camera1.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    Camera.Size size = camera.getParameters().getPictureSize();
                    numPicTaken1 += 1;
                    String str = "camera1 onPictureTaken:"+numPicTaken1 + " raw pic data="+bytes.length
                            + " width="+size.width + " height="+size.height;
                    Log.i(mTAG, str);
                    LogFile.getInstance().saveMessage(str);
                    if (bytes.length > 0) {
                        faceDetRec.recognizerFace(bytes, size.width, size.height);
                    }
                    try {
//                        camera.startPreview();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }catch (Exception e){
            e.printStackTrace();
            Log.e(mTAG, "takePictureDataWithCamera1 error:"+e.toString());
            LogFile.getInstance().saveMessage("takePictureDataWithCamera1 error:"+e.toString());
        }
    }

    /**
     * 先启动抓图呼叫前
     * @param num
     */
    public void callOwenr(String num){
        if (flagCallOwenr){
            Toast.makeText(this, "正在呼叫请等待", Toast.LENGTH_SHORT).show();
            TTSUtils.getInstance().speak("正在呼叫请等待");
            return;
        }else {
            flagCallOwenr = true;
            flagGuestCallRecord = true;
        }
        capturePhoto();
    }

    /**
     * 启动抓图
     */
    public void capturePhoto(){
        strLog = "capturePhoto flagCallOwenr："+flagCallOwenr + " flagOpenDoorRemote:"+flagOpenDoorRemote
            + " flagOpenDoorPassword:"+flagOpenDoorPassword + " needCatchPhoto:"+needCatchPhoto;
        Log.i(mTAG, strLog);
        LogFile.getInstance().saveMessage(strLog);
        //正在通话则不允许启动识别
        if ((flagOpenDoorRemote&&flagCallOwenr) || (flagOpenDoorPassword&&flagCallOwenr)) {
            //通话期间直接上传临时照片
            if(flagOpenDoorRemote){
                flagOpenDoorRemote = false;
                UtilCommon.copyFile(faceDetRec.getFpTempPic(), catchPhotoFP);
                saveRecordRemoteOpenDoor(catchPhotoFP);
            }
            return;
        }
        needCatchPhoto = true;
        if (!isCameraRun){
            startPreviewFaceRec();
        }
    }

    /**
     * 验证密码开门
     * @param input
     */
    public void verifyPasswordOpenDoor(final String input){
        capturePhoto();
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = playListDb.rawQuery("select * from "+PlaylistDatabaseHelper.PWD_TABLE
                + " where "+PlaylistDatabaseHelper.PWD + " =? ", new String[]{input});
                boolean flag = cursor.moveToNext();
                String str = "本地是否存在密码："+flag;
                Log.i(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                if (flag){
                    String expTime = cursor.getString(cursor.getColumnIndex(PlaylistDatabaseHelper.EXPTIME));
                    idTempPassword = cursor.getString(cursor.getColumnIndex(PlaylistDatabaseHelper.PWD_ID));
                    long timeLeft = (Long.parseLong(expTime) - new Date().getTime())/1000;
                    str = "密码有效 剩余 = "+timeLeft+" s";
                    if (timeLeft > 0){
                        handler.sendEmptyMessage(VALIDPASSWORD);
                    }else {
                        int row = playListDb.delete(PlaylistDatabaseHelper.PWD_TABLE,
                                PlaylistDatabaseHelper.PWD+" =?", new String[]{input});
                        handler.sendEmptyMessage(INVALIDPASSWORD);
                        str = "密码已过期 删除此条记录:"+row;
                    }
                    Log.i(mTAG, str);
                    LogFile.getInstance().saveMessage(str);
                }else {
                    handler.sendEmptyMessage(WRONGPASSWORD);
                }
            }
        });
    }

    /**
     * 延时启动拍照
     * @param secs
     */
    public void delayTakePhoto(final int secs){
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                String str = "";
                int cnt = 0;
                while (true) {
                    try {
                        Thread.sleep(1000);
                        cnt += 1;
                        if (cnt == secs){
                            str = "延时 "+cnt + " s 启动拍照";
                            Log.i(mTAG, str);
                            LogFile.getInstance().saveMessage(str);
                            handler.sendEmptyMessage(STILLHAVEPERSON);
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    /**
     * 等待连通业主
     * @param times
     */
    public void waitUtilLiveChat(final int times){
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                String str = "";
                int cnt = 0;
                Message msg = new Message();
                while (true){
                    try {
                        Thread.sleep(1000);
                        cnt += 1;
                        if (cnt == times){
                            str = "呼叫业主等待超时 = "+cnt + " s";
                            Log.i(mTAG, str);
                            LogFile.getInstance().saveMessage(str);
                            msg.what = TIMEOUTLIVECHAT;
                            msg.obj = str;
                            msg.arg1 = 1;
                            handler.sendMessage(msg);
                            break;
                        }
                        if (connectedLiveChat){
                            str = "呼叫业主已经连通 = "+cnt + " s";
                            Log.i(mTAG, str);
                            LogFile.getInstance().saveMessage(str);
                            handler.sendEmptyMessage(SUCCESSLIVECHAT);
                            break;
                        }
                        if (!flagJoinChannel){
                            str = "业主离开通话 = "+cnt + " s";
                            Log.i(mTAG, str);
                            LogFile.getInstance().saveMessage(str);
                            msg.what = TIMEOUTLIVECHAT;
                            msg.obj = str;
                            msg.arg1 = 2;
                            handler.sendMessage(msg);
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 设置通话时长
     * @param times
     */
    public void limitCallTime(final int times){
        scheduledThread.execute(new Runnable() {
            @Override
            public void run() {
                String str = "";
                int cnt = 0;
                while (true){
                    try {
                        Thread.sleep(1000);
                        cnt += 1;
                        if (cnt == times){
                            str = "通话超时 = "+cnt + " s";
                            Log.i(mTAG, str);
                            LogFile.getInstance().saveMessage(str);
                            Message msg = new Message();
                            msg.what = TIMEOUTLIVECHAT;
                            msg.obj = str;
                            msg.arg1 = 1;
                            handler.sendMessage(msg);
                            break;
                        }else if (cnt == (times-10)){
                            str = "通话时长 = "+cnt + " s";
                            Log.i(mTAG, str);
                            LogFile.getInstance().saveMessage(str);
                            Message msg = new Message();
                            msg.what = TIMELIVECHAT;
                            msg.arg1 = 10;
                            handler.sendMessage(msg);
                        }
                        if (!flagJoinChannel){
                            str = "业主离开通话 = "+cnt + " s";
                            Log.i(mTAG, str);
                            LogFile.getInstance().saveMessage(str);
                            Message msg = new Message();
                            msg.what = TIMEOUTLIVECHAT;
                            msg.obj = str;
                            msg.arg1 = 2;
                            handler.sendMessage(msg);
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public boolean readIDCard(){
        Log.d(mTAG, "readIDCard - entre");
        readCardSingleThread.execute(manageIDCardInfos);
        return false;
    }

    /**
     * 读取管理身份证信息
     */
    private Runnable manageIDCardInfos = new Runnable() {
        @Override
        public void run() {
//            publicSecurityIDCardLib iDCardDevice1 = new publicSecurityIDCardLib();
//            String namePackage1 = "/data/data/"+getPackageName()+"/lib/libwlt2bmp.so";
            String str = "";
            try {
                UtilCommon.clearBytes(BmpFile);
                UtilCommon.clearBytes(name);
                UtilCommon.clearBytes(sex);
                UtilCommon.clearBytes(nation);
                UtilCommon.clearBytes(birth);
                UtilCommon.clearBytes(address);
                UtilCommon.clearBytes(IDNo);
                UtilCommon.clearBytes(Department);
                UtilCommon.clearBytes(EffectDate);
                UtilCommon.clearBytes(ExpireDate);
                //read method
                int retReadIdCard = iDCardDevice.readBaseMsg(portSerial,namePackage,BmpFile, name,
                        sex, nation, birth, address, IDNo, Department, EffectDate, ExpireDate,pErrMsg);
                if (retReadIdCard>=1000){
                    str = "身份证读取成功 readIDCard:" + retReadIdCard;
                    Log.i(mTAG, str);
                    LogFile.getInstance().saveMessage(str);
                    String nm=null, se=null, na=null, no=null, ad=null, bi=null, au=null, vab=null, vae=null;
                    nm = new String(name,"Unicode").trim();
                    ad = new String(address, "Unicode").trim();
                    se = new String(sex, "Unicode").trim();
                    na = new String(nation, "Unicode").trim();
                    no = new String(IDNo, "Unicode").trim();
                    bi = new String(birth, "Unicode").trim();
                    au = new String(Department, "Unicode").trim();
                    vab = new String(EffectDate, "Unicode").trim();
                    vae = new String(ExpireDate, "Unicode").trim();
                    if (vae.equals("长期")){
                        int year = Integer.parseInt(vab.substring(0,4));
                        year += 50;
                        vae = year + vab.substring(4,8);
                    }
                    //
                    int []colors = iDCardDevice.convertByteToColor(BmpFile);
                    Bitmap bm = Bitmap.createBitmap(colors, 102, 126, Bitmap.Config.ARGB_8888);
                    String dir = MainActivity.picDir + "IdCard/";
                    File file = new File(dir);
                    if (!file.exists()){
                        file.mkdirs();
                    }
                    dir += no + ".jpg";
                    UtilCommon.savePhoto(dir,bm);                           //存身份证照片
                    HashMap<String, String> params = new HashMap<>();
                    params.put("face", "");
                    params.put("name", nm);
                    params.put("sex", se);
                    params.put("nation", na);
                    params.put("no", no);
                    params.put("address", ad);
                    params.put("birthday", bi);
                    params.put("authority", au);
                    params.put("validBegin", vab);
                    params.put("validEnd", vae);
                    params.put("picIdCardPath", dir);                         //身份证照片路径

                    //
                    Message msg = new Message();
                    msg.obj = params;
                    try {
                        featDb = fDbHelper.getReadableDatabase();
                    } catch (Exception e){
                        str = "needDownloadPersons get featDb error:"+e.toString();
                        Log.e(mTAG, str);
                        LogFile.getInstance().saveMessage(str);
                    }
                    Cursor cursor = featDb.rawQuery("select * from "
                            + FeatDatabaseHelper.TABLE_MANAGEABLEPERSON
                            + " where " + FeatDatabaseHelper.IDCARD_NO + " = ? " ,new String[]{no});
                    boolean flag = cursor.moveToNext();
                    String pid = null;
                    //是否在管理人员列表
                    if (flag){
                        msg.what = SUCCESSREADCARD;
                        msg.arg1 = 1;
                        handler.sendMessage(msg);
                        pid = cursor.getString(cursor.getColumnIndex(FeatDatabaseHelper.PERSON_ID));
                        str = no + " 身份证已授权" + " pid:" + pid + " nation:"+na
                                + " effectDate:"+vab  + " expireDate:"+vae + " isNetwork:"+isNetwork;
                        Log.i(mTAG, str);
                        LogFile.getInstance().saveMessage(str);
                        if (isNetwork){
                            float sim = faceDetRec.calcSimVisPicIdCard_Stranger(dir);        //对比相似度
                            str = "身份证相似度="+sim;
                            Log.i(mTAG, str);
                            LogFile.getInstance().saveMessage(str);
                            Message message = new Message();
                            message.what = IDCARDVERIFYWITHPERSON;
                            if (sim > compared)
                            {
                                faceDetRec.setPermitedReg(true, pid, nm, no);
                                message.arg1 = 1;
                                message.obj = "开始注册";
                                handler.sendMessage(message);
                            }else {
                                message.arg1 = 0;
                                message.obj = "身份证比对失败";
                                handler.sendMessage(message);
                            }
                            Log.i(mTAG, (String) message.obj);
                            LogFile.getInstance().saveMessage((String) message.obj);
                        }else {
                            str = "当前无网络："+isNetwork + " 临时允许开门:"+pid;
                            Log.i(mTAG, str);
                            LogFile.getInstance().saveMessage(str);
                            openLockWithPulse();
                        }
                    }else {
                        msg.what = SUCCESSREADCARD;
                        msg.arg1 = 0;
                        handler.sendMessage(msg);
                    }
                    cursor.close();
                    featDb.close();
                }else {
                    str = "身份证读取返回 retReadIdCard="+retReadIdCard;
                    Log.i(mTAG, str);
                    LogFile.getInstance().saveMessage(str);
                    Thread.sleep(delayReadIdCard);
                    handler.sendEmptyMessage(FAILREADCARD);
                }
            } catch (Exception e) {
                str = "身份证 readIDCard error:" + e.toString();
                Log.e(mTAG, str);
                LogFile.getInstance().saveMessage(str);
                e.printStackTrace();
                handler.sendEmptyMessage(FAILREADCARD);
            }
        }
    };

    public void initFaceRecModule(){
        faceDetRec = new FaceDetRec(this, matchScorePass, faceImageSize, roll, pitch, yaw);
        faceDetRec.setOnFaceDetRecCallBack(new FaceDetRec.OnFaceDetRecCallBack() {
            /** 连续丢失人脸次数超过N，N=8*/
            @Override
            public void onLostPerson(int num) {
                strLog = "isPersonNear:"+isPersonNear + " onLostPerson - times:"+num
                        + " flagCallOwenr:"+flagCallOwenr;
                Log.i(mTAG, strLog);
                LogFile.getInstance().saveMessage(strLog);
                /** 红外或呼叫流程存活则不结束识别流程*/
                if (flagCallOwenr){
                    faceDetRec.setTimesLostShadow(0);
                    handler.sendEmptyMessage(STILLHAVEPERSON);
                }else
                {
                    handler.sendEmptyMessage(LOSTPERSON);
                }
            }

            /** 拍照回调无论有无人脸*/
            @Override
            public void onPhotoSaved(int numPic) {
                strLog = "onPhotoSaved = "+numPic;
                Log.i(mTAG, strLog);
                LogFile.getInstance().saveMessage(strLog);
                handler.sendEmptyMessage(PHOTOTAKEN);
            }

            /** 未检测到人脸*/
            @Override
            public void onFailDetectFace() {
                handler.sendEmptyMessage(STILLHAVEPERSON);
            }

            @Override
            public void onFailRecognizerFace(float sim) {
                handler.sendEmptyMessage(STILLHAVEPERSON);
            }

            @Override
            public void onSuccessRecognizerFace(String idno, float sim) {
                Message msg = new Message();
                msg.what = SUCCESSRECOGNIZER;
                msg.obj = idno;
                msg.arg1 = Float.floatToIntBits(sim);
                handler.sendMessage(msg);
            }
            /** 一个有人脸的识别流程结束 */
            @Override
            public void onFinishRecognizerFace(List<SimilarPerson> spList, String fPath, float msim) {
                Message msg = new Message();
                msg.what = FINISHRECOGNIZER;
                msg.obj = spList;
                msg.arg1 = Float.floatToIntBits(msim);
                handler.sendMessage(msg);
            }

            @Override
            public void onRegistering(int num) {
                Message msg = new Message();
                msg.arg1 = num;
                msg.what = REGISTERING;
                handler.sendMessage(msg);
            }

            @Override
            public void onFinishRegister(PersonRegFilesInfo prfi) {
                Message msg = new Message();
                msg.obj = prfi;
                msg.what = SUCCESSREGISTER;
                handler.sendMessage(msg);
            }

            @Override
            public void onFailtoRegister(int num) {
                handler.sendEmptyMessage(FAILEDREGISTER);
            }

            @Override
            public void onFailServers() {
                handler.sendEmptyMessage(FAILSERVERS);
            }
        });

        iDCardDevice = new publicSecurityIDCardLib();
        namePackage = "/data/data/"+getPackageName()+"/lib/libwlt2bmp.so";
    }

    /**
     * 推送消息接收类
     */
    class PushMessageBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String content = intent.getStringExtra("content");
            Log.i(mTAG, "PushMessageBroadcastReceiver content:"+content + " package:"+intent.getPackage());
            LogFile.getInstance().saveMessage("PushMessageBroadcastReceiver content:"+content);
            Message msg = new Message();
            msg.obj = content;
            msg.what = PUSHMESSAGERECEIVE;
            handler.sendMessage(msg);
        }
    }

    public class ConnectionChangeReceiver extends BroadcastReceiver{
        private final String TAG = ConnectionChangeReceiver.class.getSimpleName();
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(mTAG, "ConnectionChangeReceiver onReceive entre isNetwork:"+isNetwork);
//            if (isNetwork)
//                return;
            Log.d(TAG, "CONNECTIVITY_ACTION");
            String str = "当前网络可用";
            boolean success = false;
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null){
                if (networkInfo.isConnected()){
                    if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET
                            || networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
                        isNetwork = true;
                        TTSUtils.getInstance().speak(str);
                        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                        str = str + ":"+networkInfo.getType();
                        Log.i(TAG, str);
                        LogFile.getInstance().saveMessage(str);
                        handler.sendEmptyMessage(SUCCESSINTERNET);
                    }
                }
            }else {
                isNetwork = false;
                str = "当前网络异常";
                TTSUtils.getInstance().speak(str);
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                str = str + ":" + networkInfo;
                Log.e(TAG, str);
                LogFile.getInstance().saveMessage(str);
            }
        }
    }

    private void startTimer(){
        timerHeart.schedule(new TimerTask() {
            @Override
            public void run() {
                connectServer();
            }
        },20*1000, (long) (heartbeatTime*1000));
    }
    // 停止定时器
    private void stopTimer(){
        if(timerHeart != null){
            timerHeart.cancel();
            // 一定设置为null，否则定时器不会被回收
            timerHeart = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(mTAG, "Down keyCode:"+keyCode + " KeyEvent:"+event);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.i(mTAG, "Up keyCode:"+keyCode + " KeyEvent:"+event);
        switch (keyCode){
            case 66:        //# 号
                resolveKeyboardEvent();
                break;
            case 67:        //* 号
                if (editText.getText().toString().equals("") && typeInput==0){
                    typeInput = 1;
                    inputTip.setText("请输入密码：");
                    Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
                }else {
                    typeInput = 0;
                    inputTip.setText("请输入房号：");
                    Toast.makeText(this, "请输入房号", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
//        toolChain.Destroy();
        RtcEngine.destroy();
        mRtcEngine = null;
        featDb.close();
        TTSUtils.getInstance().release();
        unregisterReceiver(netWorkStateReceiver);
        unregisterReceiver(pushMessageReceiver);
        stopCameras();
    }
}
