package com.augumenta.demo.smartmarkerdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.augumenta.agapi.AugumentaManager;
import com.cyberon.DSpotterApplication;
import com.cyberon.dspotterutility.DSpotterRecog;
import com.cyberon.dspotterutility.DSpotterStatus;
import com.jiangdg.usbcamera.UVCCameraHelper;
import com.jiangdg.usbcamera.utils.FileUtils;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.widget.CameraViewInterface;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class TakePicture extends AppCompatActivity implements CameraDialog.CameraDialogParent {
    private TextView textView;
    private String[] test = new String[10];
    private static final String TAG = "Debug";
    public View mTextureView;
    private UVCCameraHelper mCameraHelper;
    private CameraViewInterface mUVCCameraView;
    private Bitmap bm;
    private ByteArrayOutputStream baos;

    private static final int MSG_INITIALIZE_SUCCESS = 2000;
    private DSpotterRecog m_oDSpotterRecog = null;
    private ArrayAdapter<String> m_oCommandBinAdapter = null;

    private Handler handler = new Handler();
    private Runnable runnable2;

    private boolean isRequest;
    private boolean isPreview;

    public String Bed_ID;
    public String Bed_MissionID;

    private String TakePicture_url;
    private String medicine_url;
    private String Now_url;
    private String base64;

    private SoundPool soundPool;
    private int soundID;

    private String IP;
    // For status callback
    private DSpotterRecog.DSpotterRecogStatusListener m_oRecogStatusListener = new DSpotterRecog.DSpotterRecogStatusListener() {

        @Override
        public void onDSpotterRecogStatusChanged(int nStatus) {
            m_oHandler.sendMessage(m_oHandler.obtainMessage(nStatus, 0, 0));
        }
    };


    private final Handler m_oHandler = new TakePicture.DSpotterDemoHandler(this);

    @SuppressLint("HandlerLeak")
    private class DSpotterDemoHandler extends Handler {


        public DSpotterDemoHandler(TakePicture mainActivity) {

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
                    soundPool.play(soundID,1,1,0,0,1);
                    showShortMsg(Recog_text);
                    switch (Recog_text){
//                        case "離開操作":
//                            m_oDSpotterRecog.stop();
//                            Intent intent = new Intent();
//                            intent.setClass(TakePicture.this,MainActivity.class);
//                            Bundle bundle = new Bundle();
//                            bundle.putString("TakePicture_url",TakePicture_url);
//                            bundle.putString("medicine_url",medicine_url);
//                            bundle.putString("Now_url",Now_url);
//                            intent.putExtras(bundle);
//                            startActivity(intent);
//                            break;
                        case "拍照":
                            Take_Picture();
                            break;
                        case "重新拍照":
//                            Sleep("stop");
                            refresh();
                            break;
                        case "項目完成":
                            Upload_image();
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

    public void refresh(){
        setContentView(R.layout.activity_takepicture);
        mTextureView = findViewById(R.id.camera_view);
        mCameraHelper = UVCCameraHelper.getInstance(640, 480);

        mUVCCameraView = (CameraViewInterface) mTextureView;
        mUVCCameraView.setCallback(ViewCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takepicture);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM,5);
        soundID = soundPool.load(this,R.raw.voice,1);
        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
        IP = sharedPreferences.getString("IP","");

//        textView = findViewById(R.id.text123);
        mTextureView = findViewById(R.id.camera_view);
        mUVCCameraView = (CameraViewInterface) mTextureView;
        mCameraHelper = UVCCameraHelper.getInstance(640, 480);
        mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_YUYV);
        mCameraHelper.initUSBMonitor(this, mUVCCameraView, listener);
        mUVCCameraView.setCallback(ViewCallback);
        mCameraHelper.setOnPreviewFrameListener(nv21Yuv -> Log.d(TAG, "onPreviewResult: "+nv21Yuv.length));

        iniDSpotter();

        if(m_oDSpotterRecog.start(false) == 0){
            System.out.println("success");
        }else {
//            showToast("語音辨識開啟失敗，請重新開啟或聯絡管理員。");
        }

        if (!hasPermissionsGranted(new String[]{
                Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO})) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS, Manifest.permission.SYSTEM_ALERT_WINDOW,Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ,Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }

        Share_Data();

        Bundle bundle = getIntent().getExtras();
        Now_url = bundle.getString("Now_url");
        TakePicture_url = bundle.getString("TakePicture_url");
        medicine_url = bundle.getString("medicine_url");
    }

    public void Share_Data(){
        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
        Bed_ID = sharedPreferences.getString("Bed_ID","");
        Bed_MissionID = sharedPreferences.getString("Bed_MissionID","");
    }

    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void Take_Picture(){
        String path = Environment.getExternalStorageDirectory().getPath() + "/USBCamera/test.jpg";
        mCameraHelper.capturePicture(path, new AbstractUVCCameraHandler.OnCaptureListener() {
            @Override
            public void onCaptureResult(String picPath) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bm = BitmapFactory.decodeFile(picPath);
                        baos = new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); // bm is the bitmap object

                        setContentView(R.layout.image);
                        ImageView imageView = findViewById(R.id.image1);
                        imageView.setImageBitmap(bm);

                        mCameraHelper.stopPreview();
                        isPreview = false;
                    }
                });
            }
        });
    }

    public void Sleep(String status){
        if(status.equals("sleep")){
            runnable2 = new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Upload_image();
                            refresh();
                        }
                    });
                }
            };
            handler.postDelayed(runnable2,2000);
        }
        else {
            handler.removeCallbacks(runnable2);
        }
    }

    public void Upload_image(){
        try {
            byte[] b = baos.toByteArray();
            String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            base64 = encodedImage;
            Thread thread = new Thread(runnable);
            thread.start();
            thread.join();

            m_oDSpotterRecog.stop();
            showShortMsg("上傳成功。");
            Intent intent = new Intent();
            intent.setClass(TakePicture.this,MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("TakePicture_url",TakePicture_url);
            bundle.putString("medicine_url",medicine_url);
            bundle.putString("Now_url",Now_url);
            intent.putExtras(bundle);
            startActivity(intent);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("http://"+IP+"/api/WoundAPI/");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("CusID", Bed_ID));
            nvps.add(new BasicNameValuePair("MissionID", Bed_MissionID));
            nvps.add(new BasicNameValuePair("PICTUREN", base64));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            try(CloseableHttpResponse response = httpclient.execute(httpPost)) {
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                HttpEntity entity = response.getEntity();
                String res_data  = EntityUtils.toString(entity); //response
                System.out.println(res_data);
            }catch (ParseException | IOException e) {
                e.printStackTrace();
            }
        }
    };

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
//            startDecoding();
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

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        FileUtils.releaseFile();
//        // step.4 release uvc camera resources
//        if (mCameraHelper != null) {
//            mCameraHelper.release();
//        }
//    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
    }

//    @Override
//    public void onPause() {
//        Log.d(TAG, "onPause");
//        super.onPause();
//        // This is for Vuzix:
//        if (displayListener != null) {
//            DisplayManager dm = (DisplayManager) getSystemService(DISPLAY_SERVICE);
//            dm.unregisterDisplayListener(displayListener);
//        }
//
//        // Unregister all pose listeners
//        mAugumentaManager.unregisterAllListeners();
//
//        // Remember to stop the detection when the activity is paused.
//        // Otherwise the camera is not released and other applications are unable to
//        // open camera.
//        mAugumentaManager.stop();
//
//    }

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
}
