package com.augumenta.demo.smartmarkerdemo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.cyberon.engine.LoadLibrary;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Splash_Screen extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGHT = 2000;
//    private String IP = "118.163.240.96:9002";
//    private String IP = "192.168.95.126:9002";
    private String IP = "210.68.227.123:8030";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        CheckLicenseFile();

        LoadLibrary.loadLibrary(getApplicationContext());

        Clear_Shared();

        ConnectivityManager conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networInfo = conManager.getActiveNetworkInfo();
        if (networInfo == null || !networInfo.isAvailable()){
            Toast.makeText(this,"裝置網路未連線，請連線後重啟APP。",Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            if (!hasPermissionsGranted(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO})) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO ,Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS, Manifest.permission.SYSTEM_ALERT_WINDOW,Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ,Manifest.permission.READ_EXTERNAL_STORAGE}, 1); }
            else {
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("IP",IP);
                        editor.apply();

                        Intent mainIntent = new Intent(Splash_Screen.this,SmartMarker_Dispatch.class);
                        startActivity(mainIntent);
                        finish();
                    }
                },SPLASH_DISPLAY_LENGHT);
            }

        }

//        Thread thread3 = new Thread(runnable3);
//        thread3.start();
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                // 授權被允許
                if (grantResults.length > 0){
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("IP",IP);
                            editor.apply();

                            Intent mainIntent = new Intent(Splash_Screen.this,SmartMarker_Dispatch.class);
                            startActivity(mainIntent);
                            finish();
                        }
                    },SPLASH_DISPLAY_LENGHT);
                } else {
                }
                return;
            }
        }
    }

    public void CheckLicenseFile(){
        //            String fileNames[] = getAssets().list("");
//            System.out.println(Arrays.toString(fileNames));
        InputStream in = null;
        OutputStream out = null;
        File outFile = null;
        try {
            in = getAssets().open("CybServer.bin");
            String outDir = Environment.getExternalStorageDirectory()
                    .getPath() + "/DCIM/";
            outFile = new File(outDir, "CybServer.bin");
            out = new FileOutputStream(outFile);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            outFile = null;

//            in = getAssets().open("CybLicense.bin");
//            outFile = new File(outDir, "CybLicense.bin");
//            out = new FileOutputStream(outFile);
//            copyFile(in, out);
//            in.close();
//            in = null;
//            out.flush();
//            out.close();
//            out = null;
//            outFile = null;

            in = getAssets().open("glasses_211230_pack_withTxt.bin");
            outFile = new File(outDir, "glasses_211230_pack_withTxt.bin");
            out = new FileOutputStream(outFile);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public void Clear_Shared(){
        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    Runnable runnable3 = new Runnable() {
        @Override
        public void run() {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("http://"+IP+"/api/MissionAPI/");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("MissionID", "71"));
            nvps.add(new BasicNameValuePair("SYSID", "3088"));
            nvps.add(new BasicNameValuePair("Nurse", "A123456789"));
            nvps.add(new BasicNameValuePair("STATUS", "1"));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            try(CloseableHttpResponse response = httpclient.execute(httpPost)) {
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                HttpEntity entity = response.getEntity();
//                String res_data  = EntityUtils.toString(entity); //response
//                System.out.println(res_data);

            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
                    CloseableHttpClient httpclient = HttpClients.createDefault();
                    HttpGet httpGet = new HttpGet("http://"+IP + "/Api/DispatchSystems/1");
                    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                    try(CloseableHttpResponse response = httpclient.execute(httpGet)) {
                        System.out.println(response.getCode() + " " + response.getReasonPhrase());
                        HttpEntity entity = response.getEntity();
                        String res_data  = EntityUtils.toString(entity); //response
                        System.out.println(res_data);
                        JSONObject jsonObject = new JSONObject(res_data);

                        String UserID = jsonObject.getString("UserID");
                        System.out.println("UserID"+UserID);
                    }catch (IOException | JSONException | ParseException e){
                        System.out.println("error123"+e);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getBaseContext(), "網路異常，請重新嘗試或聯絡管理員。", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
    };
}
