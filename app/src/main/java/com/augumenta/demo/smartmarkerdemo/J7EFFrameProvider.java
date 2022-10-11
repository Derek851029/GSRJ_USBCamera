//package com.augumenta.demo.smartmarkerdemo;
//
//import android.content.Context;
//import android.util.Log;
//import android.view.SurfaceView;
//import android.widget.Toast;
//
//import com.augumenta.agapi.FrameFormatMap;
//import com.augumenta.agapi.FrameProvider;
//import com.jorjin.jjsdk.camera.CameraManager;
//import com.jorjin.jjsdk.camera.FrameListener;
//
//public class J7EFFrameProvider extends FrameProvider {
//    private static final String TAG = J7EFFrameProvider.class.getSimpleName();
//
//    private CameraManager mCameraManager;
//
//    private FrameListener mFrameListener = (byteBuffer, width, height, format) -> {
//        double fovh = 0.0, fovv = 0.0;
//        double[] licc = null, nicc = null;
//        byte[] data = byteBuffer.array();
//        long timeStamp = System.nanoTime() / 1000L;
//
//        if (width == 1920 && height == 1080) {
//            fovh = 70.97;
//            fovv = 43.64;
//            licc = new double[]{1346.609196f, 1348.320658f, 967.382447f, 534.827196f};
//            nicc = new double[]{-0.066144f, 0.422228f, -1.021575f, 0.804669f,
//                    -3.605751f, 0.741320f, 5.282295f, -0.948467f, 0.065719f, -0.417881f,
//                    1.006086f, -0.788311f, 3.555635f, -0.730718f, -5.165532f, 0.923239f};
//        }
//        if (width == 1280 && height == 720) {
//            fovh = 70.90f;
//            fovv = 43.72f;
//            licc = new double[]{898.966887f, 897.288769f, 655.845169f, 361.700094f};
//            nicc = new double[]{-0.054703f, 0.355821f, -0.846304f, 0.648503f, -2.684271f,
//                    -0.391773f, 2.627683f, 0.925244f, 0.054625f, -0.355459f, 0.844317f,
//                    -0.645861f, 2.656408f, 0.390341f, -2.586680f, -0.918308f};
//        }
//        if (width == 640 && height == 480) {
//            fovh = 66.46f;
//            fovv = 43.72f;
//            licc = new double[]{488.485370f, 488.629483f, 332.390618f, 238.028623f};
//            nicc = new double[]{0.020161f, -0.154445f, 0.063651f, 0.279604f, -1.005030f,
//                    -0.404068f, 1.200322f, -0.302997f, -0.007036f, 0.015727f, 0.368083f,
//                    -0.690509f, 0.935656f, 0.339548f, -1.002108f, 0.508353f };
//        }
//
//        push(   data,
//                FrameFormatMap.FrameFormat.FORMAT_NV21,
//                width,
//                height,
//                fovh,
//                fovv,
//                licc,
//                nicc,
//                0,
//                timeStamp);
//    };
//
//    public void initProvider(Context context) {
//        Log.d(TAG, "initProvider");
//
//        mCameraManager = new CameraManager(context);
//        if (mCameraManager != null) {
//            mCameraManager.setCameraFrameListener(mFrameListener);
//            mCameraManager.setResolutionIndex(0);
//        } else {
//            Log.e(TAG, "Error init cameramanager");
//        }
//    }
//
//    public void setSurfaceView(SurfaceView view) {
//        if (mCameraManager != null) {
//            mCameraManager.addSurfaceHolder(view.getHolder());
//        }
//    }
//
//    @Override
//    protected boolean onStart() {
//        Log.d(TAG, "onStart");
//        if(mCameraManager != null) {
//            Log.d(TAG, "startCamera");
//            mCameraManager.startCamera(CameraManager.COLOR_FORMAT_NV21);
//        } else { return false; }
//        return true;
//    }
//
//    @Override
//    protected void onStop() {
//        Log.d(TAG, "onStop");
//        if(mCameraManager != null) {
//            Log.d(TAG, "stopCamera");
//            mCameraManager.stopCamera();
//        }
//    }
//}
