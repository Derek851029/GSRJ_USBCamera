package com.augumenta.demo.smartmarkerdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.augumenta.agapi.AugumentaManager;
import com.augumenta.agapi.FrameFormatMap;
import com.augumenta.agapi.FrameProvider;
import com.augumenta.agapi.HandPoseEvent;
import com.augumenta.agapi.HandPoseListener;
import com.augumenta.agapi.Poses;
import com.cyberon.DSpotterApplication;
import com.cyberon.dspotterutility.DSpotterRecog;
import com.cyberon.dspotterutility.DSpotterStatus;
import com.jiangdg.usbcamera.UVCCameraHelper;
import com.jiangdg.usbcamera.utils.FileUtils;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.widget.CameraViewInterface;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SmartMarker_Camera extends AppCompatActivity implements CameraDialog.CameraDialogParent {
    private static final String TAG = "Debug";
    public View mTextureView;
    private UVCCameraHelper mCameraHelper;
    private CameraViewInterface mUVCCameraView;
    private AugumentaManager mAugumentaManager;
    private boolean isRequest;
    private boolean isPreview;

    private Toast toast = null;
    private String toast_show = "";
    private static long oneTime = 0;
    private static long twoTime = 0;

    private static final int MSG_INITIALIZE_SUCCESS = 2000;
    private DSpotterRecog m_oDSpotterRecog = null;

    private String Now_url;
    private String medicine_url;

    private String smart_data;
    private String Bed_Num = "";
    private String Bed_SYSID;
    private String Bed_ID;
    private String Bed_MissionID;

    private TextView poseView;
    private String IP;
    // For status callback
    private DSpotterRecog.DSpotterRecogStatusListener m_oRecogStatusListener = new DSpotterRecog.DSpotterRecogStatusListener() {

        @Override
        public void onDSpotterRecogStatusChanged(int nStatus) {
            m_oHandler.sendMessage(m_oHandler.obtainMessage(nStatus, 0, 0));
        }
    };


    private final Handler m_oHandler = new SmartMarker_Camera.DSpotterDemoHandler(this);

    @SuppressLint("HandlerLeak")
    private class DSpotterDemoHandler extends Handler {


        public DSpotterDemoHandler(SmartMarker_Camera mainActivity) {

        }
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_INITIALIZE_SUCCESS:
                    System.out.println("MSG_INITIALIZE_SUCCESS");
                    // Set recognize button enable
                    // Show success message
                    // showToast("Initialize success!");
                    break;
                case DSpotterStatus.STATUS_RECORDER_INITIALIZE_FAIL:
                    System.out.println("DSpotterStatus.STATUS_RECORDER_INITIALIZE_FAIL");
                    m_oDSpotterRecog.stop();

                    // Show message
                    // Show message
//                    showToast("Fail to initialize recorder!");
                    break;
                case DSpotterStatus.STATUS_RECOGNITION_START:
                    System.out.println("DSpotterStatus.STATUS_RECOGNITION_START");
                    break;

                case DSpotterStatus.STATUS_RECOGNITION_OK:
                    System.out.println("DSpotterStatus.STATUS_RECOGNITION_OK");

                    String[] straResult = new String[1];
                    m_oDSpotterRecog.getResult(null, straResult, null, null, null, null, null, null);

                    System.out.println(straResult[0]);
                    String Recog_text = straResult[0].replaceAll("\\s+","");
//                    showToast(Recog_text);
                    switch (Recog_text){
                        case "離開操作":
                            m_oDSpotterRecog.stop();
                            Intent intent = new Intent();
                            intent.setClass(SmartMarker_Camera.this,MainActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("Now_url",Now_url);
                            bundle.putString("medicine_url",medicine_url);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            break;
                    }
                    break;
                case DSpotterStatus.STATUS_RECOGNITION_FAIL:
                    System.out.println("DSpotterStatus.STATUS_RECOGNITION_FAIL");
                    m_oDSpotterRecog.stop();
                    break;
                case DSpotterStatus.STATUS_RECOGNITION_ABORT:
                    System.out.println("DSpotterStatus.STATUS_RECOGNITION_ABORT");
                    break;
                case DSpotterStatus.STATUS_RECORD_FAIL:
                    System.out.println("DSpotterStatus.STATUS_RECORD_FAIL");
                    m_oDSpotterRecog.stop();
                    break;
                case DSpotterStatus.STATUS_RECORD_FINISH:
                    System.out.println("DSpotterStatus.STATUS_RECORD_FINISH");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private AbstractUVCCameraHandler.OnPreViewResultListener mListener = (buffer) ->{
        showShortMsg(buffer.toString());
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        setContentView(R.layout.activity_smart);
        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
        IP = sharedPreferences.getString("IP","");

        poseView = (TextView) findViewById(R.id.posesOutput);
        poseView.setText("請掃描床號條碼");
        mTextureView = findViewById(R.id.camera_view);
        mUVCCameraView = (CameraViewInterface) mTextureView;
        mCameraHelper = UVCCameraHelper.getInstance(640, 480);
        mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_YUYV);
        mCameraHelper.initUSBMonitor(this, mUVCCameraView, listener);
        mUVCCameraView.setCallback(ViewCallback);

        // Create AugumentaManager and initialize it with our CameraFrameProvider
        FrameProvider frameProvider = Create_FrameProvider();
        try {
            mAugumentaManager = AugumentaManager.getInstance(this, frameProvider);
        } catch (IllegalStateException e) {
            // Something went wrong while authenticating license
            Toast.makeText(this, "License error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "License error: " + e.getMessage());

            // Close the app before AugumentaManager is used, otherwise it will cause
            // NullPointerException when trying to use it.
            finish();
            return;
        }

        // Enable the marker detection
        // You can get your markers from: http://getsmartmarker.augumenta.com
        mAugumentaManager.addDefaultMarkerFormatForDetection();

        Bundle bundle = getIntent().getExtras();
        Now_url = bundle.getString("Now_url");
        medicine_url = bundle.getString("medicine_url");

        iniDSpotter();

        if(m_oDSpotterRecog.start(false) == 0){
            System.out.println("success");
        }else {
//            showToast("語音辨識開啟失敗，請重新開啟或聯絡管理員。");
        }

        smart_data = "00410101";
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public FrameProvider Create_FrameProvider(){
        FrameProvider frameProvider = new FrameProvider();
        mCameraHelper.setOnPreviewFrameListener(new AbstractUVCCameraHandler.OnPreViewResultListener() {
            @Override
            public void onPreviewResult(byte[] nv21Yuv) {
                double fovh = 0.0, fovv = 0.0;
                double[] licc = null, nicc = null;
                byte[] data = nv21Yuv;
                long timeStamp = System.nanoTime() / 1000L;
                int width = 640,height = 480;
                fovh = 66.46f;
                fovv = 43.72f;
                licc = new double[]{488.485370f, 488.629483f, 332.390618f, 238.028623f};
                nicc = new double[]{0.020161f, -0.154445f, 0.063651f, 0.279604f, -1.005030f,
                        -0.404068f, 1.200322f, -0.302997f, -0.007036f, 0.015727f, 0.368083f,
                        -0.690509f, 0.935656f, 0.339548f, -1.002108f, 0.508353f };

                frameProvider.push(
                        data,
                        FrameFormatMap.FrameFormat.FORMAT_NV21,
                        width,
                        height,
                        fovh,
                        fovv,
                        licc,
                        nicc,
                        0,
                        timeStamp
                );
            }
        });
        return frameProvider;
    }

    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {

        @Override
        public void onAttachDev(UsbDevice device) {
            // request open permission(must have)
            if (!isRequest) {
                isRequest = true;
                if (mCameraHelper != null) {
//                    mCameraHelper.requestPermission(4);
                    List<UsbDevice> list =  mCameraHelper.getUsbDeviceList();
                    for(int i = 0;i<list.size(); i++){
                        UsbDevice _device = list.get(i);
                        if(_device.getProductName().indexOf("RearCam")>-1){
                            mCameraHelper.requestPermission(i);
                        }
                    }
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            // close camera(must have)
            if (isRequest) {
                isRequest = false;
                mCameraHelper.closeCamera();
            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            if(! isConnected) {
//                showShortMsg("連接失敗，請檢查分辨率參數是否正確");
                isPreview = false;
            }else{
                isPreview = true;
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {

        }

    };

    public final CameraViewInterface.Callback ViewCallback = new CameraViewInterface.Callback() {
        @Override
        public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
            if (!isPreview && mCameraHelper.isCameraOpened()) {
                mCameraHelper.startPreview(mUVCCameraView);
                isPreview = true;
            }
        }

        @Override
        public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) { }

        @Override
        public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
            if (isPreview && mCameraHelper.isCameraOpened()) {
                mCameraHelper.stopPreview();
                isPreview = false;
            }
        }
    };

    private void showShortMsg(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void showShortMsg_Time(String msg) {
        if(toast_show.equals("")){
            toast =  Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
            toast.show();
            oneTime = System.currentTimeMillis();
            toast_show = "show";
        }else {
            twoTime = System.currentTimeMillis();
            if(twoTime - oneTime > 3000){
                toast_show = "";
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        // step.2 register USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.registerUSB();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // step.3 unregister USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
            FileUtils.releaseFile();
            mCameraHelper.release();
        }
    }

    @Override
    public USBMonitor getUSBMonitor() {
        return mCameraHelper.getUSBMonitor();
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            showShortMsg("取消操作");
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

//		mAugumentaManager.registerListener(mPoseListener, Poses.P141);
        // Register for marker detection (x002 as it is a planar not a hand gesture)
        mAugumentaManager.registerListener(mPoseListener, Poses.X002);
        // Check if the Camera permission is already available
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted
            requestCameraPermission();
        } else {
            // Camera permission is already available
            // Start detection when activity is resumed
            startAugumentaManager();
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
//		// This is for Vuzix:
//		if (displayListener != null) {
//			DisplayManager dm = (DisplayManager) getSystemService(DISPLAY_SERVICE);
//			dm.unregisterDisplayListener(displayListener);
//		}

        // Unregister all pose listeners
        mAugumentaManager.unregisterAllListeners();

        // Remember to stop the detection when the activity is paused.
        // Otherwise the camera is not released and other applications are unable to
        // open camera.
        mAugumentaManager.stop();

    }

    public void iniDSpotter(){
        if (m_oDSpotterRecog == null)
            m_oDSpotterRecog = new DSpotterRecog();

        int nRet;
        int[] naErr = new int[1];

        String strCommandFile;
        if (DSpotterApplication.m_sCmdFilePath == null) {
//            strCommandFile = DSpotterApplication.m_sCmdFileDirectoryPath + "/"
//                    + m_oCommandBinAdapter
//                    .getItem(DSpotterApplication.m_nCmdBinListIndex)
//                    + ".bin";
            strCommandFile = DSpotterApplication.m_sCmdFileDirectoryPath + "/" + "glasses_211230_pack_withTxt" + ".bin";
        }
        else {
            strCommandFile = DSpotterApplication.m_sCmdFilePath;
            String strCommandFileName = new File(strCommandFile).getName();
            DSpotterApplication.m_sCmdFilePath = null;
        }

        nRet = m_oDSpotterRecog.initWithFiles(this,strCommandFile,DSpotterApplication.m_sLicenseFile,DSpotterApplication.m_sServerFile,naErr);
        System.out.println(nRet);
        if (nRet != DSpotterRecog.DSPOTTER_RECOG_SUCCESS) {
            Toast.makeText(this,"Fail to initialize DSpotter, " + naErr[0],Toast.LENGTH_LONG).show();
            return;
        }

        m_oDSpotterRecog.setListener(m_oRecogStatusListener);

        m_oDSpotterRecog.getTriggerWord();

        m_oHandler.sendMessage(m_oHandler.obtainMessage(MSG_INITIALIZE_SUCCESS,
                0, 0));
    }

    private void startAugumentaManager() {
        // Start detection when the activity is resumed
        if (!mAugumentaManager.start()) {
            // Failed to start detection, probably failed to open camera
            Toast.makeText(this, "Failed to open camera!", Toast.LENGTH_LONG).show();
            // Close activity
            finish();
            return;
        }
//		// This is for Vuzix:
//		DisplayManager dm = (DisplayManager) getSystemService(DISPLAY_SERVICE);
//		displayListener = new DisplayManager.DisplayListener() {
//			@Override
//			public void onDisplayAdded(int displayId) {}
//
//			@Override
//			public void onDisplayRemoved(int displayId) {}
//
//			@Override
//			public void onDisplayChanged(int displayId) {
//				updateDisplayOrientation();
//			}
//		};
//		dm.registerDisplayListener(displayListener, null);
//		// make sure that we set the screen orientation after camera is started
//		updateDisplayOrientation();
    }

    private void requestCameraPermission() {
        // Request CAMERA permission from user
        Log.d(TAG, "Requesting CAMERA permission");
        ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA }, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            Log.d(TAG, "Received response for CAMERA permission");

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "CAMERA permission granted, starting AugumentaManager");
            } else {
                Log.d(TAG, "CAMERA permission not granted, exiting..");
                Toast.makeText(this, "Camera permission was not granted.", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * HandPoseListener that shows a pose image while pose is detected
     */
    private final HandPoseListener mPoseListener = new HandPoseListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onDetected(final HandPoseEvent handPoseEvent, final boolean newdetect) {
            Log.d(TAG, "onDetected: " + handPoseEvent + " + " + newdetect);
            // If multiple poses are registered to a single listener,
            // you can check which pose triggered the event by examining
            // the handPoseEvent object

            // All interactions to the UI must be done in the UI thread
            runOnUiThread(() -> {
                if(handPoseEvent.handpose.pose()==Poses.X002) {
//                    poseView.setText("SmartMarker: " + handPoseEvent.data);
                    smart_data = handPoseEvent.data;
                    if(smart_data.indexOf("410") == -1){
                        showShortMsg_Time("條碼錯誤，請重新掃描。");
                    }else {
                        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
                        toast.cancel();
                        Thread thread = new Thread(runnable);
                        thread.start();
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            toast_show = "";
                            showShortMsg_Time("網路異常，請聯絡管理員或稍後重試。");
                            e.printStackTrace();
                        }
                    }
                    Log.d(TAG, "Detected marker plane: " + handPoseEvent.plane.toString());
                } else {
//                    poseView.setText(handPoseEvent.handpose.toString());
                }
            });
        }

        @Override
        public void onLost(final HandPoseEvent handPoseEvent) {
            Log.d(TAG, "onLost: " + handPoseEvent);
            // All interactions to the UI must be done in the UI thread
//            runOnUiThread(() -> {
//                // do something when it is lost
//                poseView.setText("");
//            });
        }

        @Override
        public void onMotion(final HandPoseEvent handPoseEvent) {
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(Bed_Num.equals("")){
                try {
                    Bed_Num = smart_data;
                    CloseableHttpClient httpclient = HttpClients.createDefault();
                    HttpGet httpGet = new HttpGet("http://"+IP+"/api/CustomerApi/"+Bed_Num);
                    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                    try(CloseableHttpResponse response = httpclient.execute(httpGet)) {
                        System.out.println(response.getCode() + " " + response.getReasonPhrase());
                        HttpEntity entity = response.getEntity();
                        String res_data  = EntityUtils.toString(entity); //response
                        System.out.println(res_data);

                        JSONObject jsonObject = new JSONObject(res_data);
                        Bed_SYSID =  jsonObject.getString("SYSID");
                        Bed_ID =  jsonObject.getString("Id");
                        Bed_MissionID = jsonObject.getString("MissionID");
                    }
                    SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Bed",Bed_Num);
                    editor.putString("Bed_SYSID",Bed_SYSID);
                    editor.putString("Bed_ID",Bed_ID);
                    editor.putString("Bed_MissionID",Bed_MissionID);
                    editor.apply();
                    m_oDSpotterRecog.stop();
                    Intent intent = new Intent();
                    intent.setClass(SmartMarker_Camera.this,MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("Now_url",Now_url);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } catch (ParseException | IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

//    public void Marker_Data(String data){
//        if(data != null || data != ""){
//            if(Bed_Num.equals("")){
//                try {
//                    Bed_Num = data;
//                    CloseableHttpClient httpclient = HttpClients.createDefault();
//                    HttpGet httpGet = new HttpGet("http://210.68.227.123:8030/api/CustomerApi/"+Bed_Num);
//                    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//                    try(CloseableHttpResponse response = httpclient.execute(httpGet)) {
//                        System.out.println(response.getCode() + " " + response.getReasonPhrase());
//                        HttpEntity entity = response.getEntity();
//                        String res_data  = EntityUtils.toString(entity); //response
//                        System.out.println(res_data);
//
//                        JSONObject jsonObject = new JSONObject(res_data);
//                        Bed_SYSID =  jsonObject.getString("SYSID");
//                        Bed_ID =  jsonObject.getString("Id");
//                    }
//                    SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString("Bed",Bed_Num);
//                    editor.putString("Bed_SYSID",Bed_SYSID);
//                    editor.putString("Bed_ID",Bed_ID);
//                    editor.apply();
//                    m_oDSpotterRecog.stop();
//                    Intent intent = new Intent();
//                    intent.setClass(SmartMarker_Camera.this,MainActivity.class);
//                    Bundle bundle = new Bundle();
//                    bundle.putString("Now_url",Now_url);
//                    intent.putExtras(bundle);
//                    startActivity(intent);
//                } catch (ParseException | IOException | JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }
//    }
}
