package com.augumenta.demo.smartmarkerdemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.augumenta.agapi.Poses;
import com.cyberon.DSpotterApplication;
import com.cyberon.dspotterutility.DSpotterRecog;
import com.cyberon.dspotterutility.DSpotterStatus;
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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {
	private static final int MSG_INITIALIZE_SUCCESS = 2000;
	private DSpotterRecog m_oDSpotterRecog = null;
	private ArrayAdapter<String> m_oCommandBinAdapter = null;

	private AlertDialog dialog = null;
	private AlertDialog dialog2 = null;

	private WebView webView;

	private String[] url = {"","",""};
	private String[] customer_list = {"","","",""};
	//    private String[] step = {"http://210.68.227.123:8030/mISSION/MissionContext/1?sysid=1","http://210.68.227.123:8030/mISSION/MissionContext/1?sysid=2","http://210.68.227.123:8030/mISSION/MissionContext/1?sysid=3",
//            "http://210.68.227.123:8030/mISSION/MissionContext/1?sysid=4","http://210.68.227.123:8030/mISSION/MissionContext/1?sysid=5","http://210.68.227.123:8030/mISSION/MissionContext/1?sysid=6",
//            "http://210.68.227.123:8030/mISSION/MissionContext/1?sysid=7"};
	private String Task = "";
	private String Medicine = "";
	private String[] list;
	private String[] MissionID_array;
	private String[] SYSID_array;
	private String[] medicine_list;
	private String[] medicine_SYSID;

	private String[] finish_array = new String[1];

	String Now_url = "";

	private String DispatchSystem;
	private String UserID;

	private String finish = "";
	private int index = 0;
	private int customer_index = 0;
	private int medicine_index = 0;

	private String Bed_Num;
	private String Bed_SYSID;
	private String Bed_ID;
	private String Bed_MissionID;

	private String TakePicture_url;
	private String medicine_url;

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


	private final Handler m_oHandler = new MainActivity.DSpotterDemoHandler(this);

	@SuppressLint("HandlerLeak")
	private class DSpotterDemoHandler extends Handler {


		public DSpotterDemoHandler(MainActivity mainActivity) {

		}
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case DSpotterStatus.STATUS_RECORDER_INITIALIZE_FAIL:
					System.out.println("DSpotterStatus.STATUS_RECORDER_INITIALIZE_FAIL");
					m_oDSpotterRecog.stop();

				case DSpotterStatus.STATUS_RECOGNITION_OK:
					System.out.println("DSpotterStatus.STATUS_RECOGNITION_OK");

					String[] straResult = new String[1];
					m_oDSpotterRecog.getResult(null, straResult, null, null, null, null, null, null);

					System.out.println(straResult[0]);
					String Recog_text = straResult[0].replaceAll("\\s+","");
					showToast(Recog_text);
					soundPool.play(soundID,1,1,0,0,1);
					switch (Recog_text){
						case "身份確認":
							if(Now_url.equals(url[0])){
								init(customer_list[customer_index]);
								Now_url = customer_list[customer_index];
							}
							break;
						case "身份錯誤":
							if(Now_url.equals(url[0])){
								m_oDSpotterRecog.stop();
								Intent intent = new Intent();
								intent.setClass(MainActivity.this,SmartMarker_Dispatch.class);
								startActivity(intent);
							}
							break;
						case "掃描床號":
							if(Arrays.asList(customer_list).contains(Now_url)){
								GoCamera(0);
							}
							break;
						case "住民確認":
							if(Bed_SYSID != null){
								if(Now_url.equals("http://"+IP+"/Customer/Login/"+Bed_SYSID)){
									try {
										Thread thread = new Thread(runnable);
										thread.start();
										thread.join();
										if(Task.equals("No")){
											showToast("未找到任務，請確認是否建立任務。");
											init(customer_list[0]);
											Now_url = customer_list[0];
											break;
										}
										Thread thread5 = new Thread(runnable5);
										thread5.start();
										thread5.join();
										if(Medicine.equals("No")){
											showToast("住民此時段無藥單。");
											init(url[2]);
											Now_url = url[2];
										}else {
											init(url[2]);
											Now_url = url[2];
										}
										break;
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}
						case "確認藥單":
							if(Now_url.equals(medicine_url)){
								if(medicine_list != null){
									init(medicine_list[0]);
									Now_url = medicine_list[0];
									medicine_index = 0;
								}
								else {
									showToast("請確認後台是否有新增藥物。");
								}

							}
							break;
						case "藥物確認":
							if(Arrays.asList(medicine_list).contains(Now_url)){
								try {
									Thread thread6 = new Thread(runnable6);
									thread6.start();
									thread6.join();

									webView.reload();
									change_step_sleep();
								}catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							break;
						case "離開任務":
							if(list != null){
								if(Now_url.equals(url[2])){
									try {
										init(customer_list[customer_index]);
										Now_url = customer_list[customer_index];

										Thread thread4 = new Thread(runnable4);
										thread4.start();
										thread4.join();
										Clear_Shared();
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}
							break;
						case "開啟相機":
							if(TakePicture_url != null){
								if(Now_url.equals(TakePicture_url)){
									dialog2.show();
								}
							}
							break;
						case "拍照":
							if(TakePicture_url != null){
								if(Now_url.equals(TakePicture_url)){
									dialog2.dismiss();
									GoCamera(1);
								}
							}
							break;
						case "攝影":
							if(TakePicture_url != null){
								if(Now_url.equals(TakePicture_url)){
									dialog2.dismiss();
									GoCamera(2);
								}
							}
							break;
						case "下一頁":
							if(Arrays.asList(customer_list).contains(Now_url)){
								if(customer_list.length == customer_index +1){
									customer_index = 0;
									init(customer_list[customer_index]);
								}
								else {
									customer_index += 1;
									init(customer_list[customer_index]);
								}
								break;
							}

							if(list != null){
								if(list.length == index+1){
									showToast("已經在最後一頁");
								}
								else {
									index +=1;
									init(list[index]);
									Now_url = list[index];
								}
							}
						case  "上一頁":
							if(Arrays.asList(customer_list).contains(Now_url)){
								if(customer_index == 0){
									showToast("已經在第一頁");
								}
								else {
									customer_index -= 1;
									init(customer_list[customer_index]);
								}
							}
							break;
						case "第一項":
							if(list != null){
								if(list.length > 0){
									index = 0;
									init(list[0]);
									Now_url = list[0];
								}
							}
							break;
						case "第二項":
							if(list != null){
								if(list.length > 1){
									index = 1;
									init(list[1]);
									Now_url = list[1];
								}
							}
							break;
						case "第三項":
							if(list != null){
								if(list.length > 2){
									index = 2;
									init(list[2]);
									Now_url = list[2];
									break;
								}
							}
							break;
						case "第四項":
							if(list != null){
								if(list.length > 3){
									index = 3;
									init(list[3]);
									Now_url = list[3];
									break;
								}
							}
							break;
						case "第五項":
							if(list != null){
								if(list.length > 4){
									index = 4;
									init(list[4]);
									Now_url = list[4];
									break;
								}
							}
							break;
						case "第六項":
							if(list != null){
								if(list.length > 5){
									index = 5;
									init(list[5]);
									Now_url = list[5];
									break;
								}
							}
							break;
						case "第七項":
							if(list != null){
								if(list.length > 6){
									index = 6;
									init(list[6]);
									Now_url = list[6];
									break;
								}
							}
							break;
						case "第八項":
							if(list != null){
								if(list.length > 7){
									index = 7;
									init(list[7]);
									Now_url = list[7];
									break;
								}
							}
							break;
						case "第九項":
							if(list != null){
								if(list.length > 8){
									index = 8;
									init(list[8]);
									Now_url = list[8];
									break;
								}
							}
							break;
						case "回到總表":
							if(list != null){
								init(url[2]);
								Now_url = url[2];
							}
							break;
						case "項目完成":
							if(finish.equals("")){
								if(list != null){
									if(list.length > 0){
										finish = "finish";
										try {
											Thread thread3 = new Thread(runnable3);
											thread3.start();
											thread3.join();
											webView.reload();
											change_step_sleep();
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
								}
							}
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
				default:
					super.handleMessage(msg);
			}
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	@SuppressLint("WrongThread")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setStatusBarColor(Color.BLACK);
		getWindow().setNavigationBarColor(Color.BLACK);

		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
				View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
		);

		setContentView(R.layout.webview);
		webView = findViewById(R.id.webview);
		soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM,5);
		soundID = soundPool.load(this,R.raw.voice,1);

		iniDSpotter();
		if(m_oDSpotterRecog.start(false) == 0){
			System.out.println("success");
		}else {
			System.out.println("m_oDSpotterRecog fail");
			showToast("語音辨識開啟失敗，請重新開啟或聯絡管理員。");
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("所有任務已完成，語音指令:【離開任務】。");
		dialog = builder.create();

		AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
		builder2.setMessage("請選擇功能:\n 如要拍照請說:【拍照】\n 如要攝影請說:【攝影】");
		dialog2 = builder2.create();

		Shared_Data();
		Change_IP_List();
		init_view();
	}

	public void Change_IP_List(){
		url[1] = "http://"+IP+"/Customer/Index";
		customer_list[0] = "http://"+IP+"/Customer/Index/0";
		customer_list[1] = "http://"+IP+"/Customer/Index/1";
		customer_list[2] = "http://"+IP+"/Customer/Index/2";
		customer_list[3] = "http://"+IP+"/Customer/Index/3";
	}

	public void Shared_Data(){
		SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);

		DispatchSystem = sharedPreferences.getString("DispatchSystem","");
		UserID = sharedPreferences.getString("UserID","");
		IP = sharedPreferences.getString("IP","");

		Bed_Num = sharedPreferences.getString("Bed_Num","");
		Bed_SYSID = sharedPreferences.getString("Bed_SYSID","");
		Bed_ID = sharedPreferences.getString("Bed_ID","");
		Bed_MissionID = sharedPreferences.getString("Bed_MissionID","");

		String list_step = sharedPreferences.getString("list","");
		String MissionID = sharedPreferences.getString("MissionID_array","");
		String SYSID_ar = sharedPreferences.getString("SYSID_array","");
		String finish_ar = sharedPreferences.getString("finish_array","");
		String medicine_list_ar = sharedPreferences.getString("medicine_list","");
		String medicine_SYSID_ar = sharedPreferences.getString("medicine_SYSID","");

		if(list_step.equals("") == false){
			list = list_step.split(",");
			MissionID_array = MissionID.split(",");
			SYSID_array = SYSID_ar.split(",");
			finish_array = finish_ar.split(",");
			medicine_list = medicine_list_ar.split(",");
			medicine_SYSID = medicine_SYSID_ar.split(",");
			url[2] = "http://"+ IP + "/Mission/Index/"+Bed_ID+"?missionID="+Bed_MissionID;
		}
	}

	public void init_view(){
		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			if(bundle.containsKey("Now_url")){
				String url_str = bundle.getString("Now_url");
				if(DispatchSystem.equals("") == false){
					url[0] = "http://"+IP+"/GlassesLogin/Login/"+DispatchSystem;
					if(url_str.equals("http://"+IP+"/GlassesLogin/Login/"+DispatchSystem)){
						init(url[0]);
						Now_url = "http://"+IP+"/GlassesLogin/Login/"+DispatchSystem;
						return;
					}
				}

				if(Bed_SYSID.equals("") == false){
					url[2] = "http://"+IP+"/Mission/Index/"+Bed_ID+"?missionID="+Bed_MissionID;

					if(Arrays.asList(customer_list).contains(url_str)){
						init("http://"+IP+"/Customer/Login/"+Bed_SYSID);
						Now_url = "http://"+IP+"/Customer/Login/"+Bed_SYSID;
					}
					else {
						init(url_str);
						Now_url = url_str;
						for(int i = 0; i<list.length; i++){
							if(list[i].equals(url_str)){
								index = i;
							}
						}
					}
				}
				else {
					init(url_str);
					Now_url = url_str;
					for(int i = 0; i<list.length; i++){
						if(list[i].equals(url_str)){
							index = i;
						}
					}
				}
			}
			else {
				init(url[0]);
				Now_url = url[0];
			}

			if(bundle.containsKey("TakePicture_url")){
				TakePicture_url = bundle.getString("TakePicture_url");
			}

			if(bundle.containsKey("medicine_url")){
				medicine_url = bundle.getString("medicine_url");
			}
		}
		else {
			init(url[0]);
			Now_url = url[0];
		}
	}

	public void init(String path){
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient(){
			public boolean ShouldOverrideUrlLoading(WebView webView, String url){
				webView.loadUrl(path);
				return true;
			}
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				webView.goBack();
			}
		});
		webView.loadUrl(path);
	}

	public void GoCamera(int index){
		m_oDSpotterRecog.stop();

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i< finish_array.length; i++){
			sb.append(finish_array[i]).append(",");
		}

		SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("finish_array",sb.toString());
		editor.apply();

		Intent intent = new Intent();
		switch (index){
			case 0:
				intent.setClass(MainActivity.this, SmartMarker_Camera.class);
				break;
			case 1:
				intent.setClass(MainActivity.this, TakePicture.class);
				break;
			case 2:
				intent.setClass(MainActivity.this, Record.class);
				break;
		}

		Bundle bundle = new Bundle();
		bundle.putString("TakePicture_url",TakePicture_url);
		bundle.putString("medicine_url",medicine_url);
		bundle.putString("Now_url",Now_url);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	public void Clear_Shared(){
		SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.commit();
		medicine_index = 0;
		customer_index = 0;
		index = 0;

		list = null;
		MissionID_array = null;
		SYSID_array = null;
		medicine_list = null;
		medicine_SYSID = null;
		Bed_Num = "";
		Bed_SYSID = "";
		Bed_ID = "";
		Bed_MissionID = "";
		TakePicture_url = "";
		medicine_url = "";

		if(DispatchSystem.equals("") == false){
			editor.putString("DispatchSystem",DispatchSystem);
			editor.putString("IP",IP);
			editor.putString("UserID",UserID);
			editor.apply();
		}
	}

	public void change_step_sleep(){
		Handler handler=new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(Now_url.indexOf("MedicineList") > 1){
					if(medicine_list.length == medicine_index+1){
						init(medicine_url);
						Now_url = medicine_url;
						medicine_index = 0;
					}else {
						medicine_index +=1;
						init(medicine_list[medicine_index]);
						Now_url = medicine_list[medicine_index];
					}
				}else {
					if(list.length == index+1){
						init(url[2]);
						Now_url = url[2];
						index = 0;
						finish = "";
//						if(finish_array.length == list.length){
//							dialog.show();
//						}
					}
					else {
//						//After the project is completed, judge the array length of the completed array, if the length is not enough, increase the length
//						for(int i = 0; i<=finish_array.length; i++){ //是i<=的原因是講一次才會進來一次 所以直接判斷進if沒問題
//							if(i == finish_array.length){
//								String[] newArr =  Arrays.copyOf(finish_array,finish_array.length+1);
//								finish_array = newArr;
//							}
//							finish_array[i] = "finish";
//						}

						index +=1;
						init(list[index]);
						Now_url = list[index];
						finish = "";
					}
				}
			}
		},1500);
	}

	//下面全都是語音辨識用
	private void initCmdBinSpinner() {
		File oFile = new File(DSpotterApplication.m_sCmdFileDirectoryPath);
		String[] strBinFileArray = oFile.list(new MainActivity.CmdBinFilter());

		if (strBinFileArray == null || strBinFileArray.length == 0) {
			showToast("Found no command file.");
			return;
		}

		for (int i = 0; i < strBinFileArray.length; i++)
			strBinFileArray[i] = strBinFileArray[i].substring(0,
					strBinFileArray[i].length() - 4); // skip .bin

		Arrays.sort(strBinFileArray);
		m_oCommandBinAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, strBinFileArray);
		m_oCommandBinAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}

	private class CmdBinFilter implements FilenameFilter {

		@SuppressLint("DefaultLocale")
		private boolean isBinFile(String file) {
			return file.endsWith(".bin");
		}

		@Override
		public boolean accept(File dir, String filename) {
			if (filename.equals("DSpotter_CMS.bin"))
				return false;
			return isBinFile(filename);
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

		nRet = m_oDSpotterRecog.initWithFiles(this,strCommandFile, DSpotterApplication.m_sLicenseFile, DSpotterApplication.m_sServerFile,naErr);
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

	public void showToast(String text){
		Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onResume() {
		super.onResume();

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
				View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
		);
	}

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			if(Bed_MissionID.equals("null") == false){
				CloseableHttpClient httpclient = HttpClients.createDefault();
				HttpGet httpGet = new HttpGet("http://"+IP+"/api/MissionAPI/"+Bed_MissionID);
				try(CloseableHttpResponse response = httpclient.execute(httpGet)) {
					System.out.println(response.getCode() + " " + response.getReasonPhrase());
					HttpEntity entity = response.getEntity();
					String res_data  = EntityUtils.toString(entity); //response
					if(res_data.equals("[]")){
						Task = "No";
					}
					else {
						JSONArray jsonArray = new JSONArray(res_data);

						list = new String[jsonArray.length()];
						MissionID_array = new String[jsonArray.length()];
						SYSID_array = new String[jsonArray.length()];
						finish_array = new String[jsonArray.length()];

						for(int i = 0; i<jsonArray.length(); i++){
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							String MissionID = jsonObject.getString("MissionID");
							String SYSID = jsonObject.getString("SYSID");
							String MissionName = jsonObject.getString("MissionName");

							list[i] = "http://"+IP+"/mISSION/MissionContext/" + MissionID +"?sysid="+ SYSID;
							MissionID_array[i] = MissionID;
							SYSID_array[i] = SYSID;

							if(MissionName.equals("傷口拍照")){
								TakePicture_url = "http://"+IP+"/mISSION/MissionContext/" + MissionID +"?sysid="+ SYSID;
							}

							if(MissionName.equals("用藥三讀五對")){
								medicine_url = "http://"+IP+"/mISSION/MissionContext/" + MissionID +"?sysid="+ SYSID;
							}
						}

						StringBuilder sb = new StringBuilder();
						StringBuilder sb2 = new StringBuilder();
						StringBuilder sb3 = new StringBuilder();
						StringBuilder sb4 = new StringBuilder();
						for (int i = 0; i < list.length; i++) {
							sb.append(list[i]).append(",");
							sb2.append(MissionID_array[i]).append(",");
							sb3.append(SYSID_array[i]).append(",");
							sb4.append(finish_array[i]).append(",");
						}

						SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putString("list",sb.toString());
						editor.putString("MissionID_array",sb2.toString());
						editor.putString("SYSID_array",sb3.toString());
						editor.putString("finish_array",sb4.toString());
						editor.apply();
					}

				}catch (ParseException | IOException | JSONException e) {
					e.printStackTrace();
				}
			}
			else {
				Task = "No";
			}
		}
	};

	Runnable runnable3 = new Runnable() {
		@Override
		public void run() {
			String MID = "";
			String SID = "";
			for(int i = 0; i<list.length; i++){
				if(list[i].equals(Now_url)){
					MID = MissionID_array[i];
					SID = SYSID_array[i];
					break;
				}
			}
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost("http://"+IP+"/api/MissionAPI/");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("MissionID", MID));
			nvps.add(new BasicNameValuePair("SYSID", SID));
			nvps.add(new BasicNameValuePair("Nurse", UserID));
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

	Runnable runnable4 = new Runnable() {
		@Override
		public void run() {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost("http://"+IP+"/api/MissionCleanAPI/");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("MissionID", MissionID_array[0]));
			nvps.add(new BasicNameValuePair("STATUS", "0"));
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

	Runnable runnable5 = new Runnable() {
		@Override
		public void run() {
			if(Bed_MissionID.equals("null") == false){
				CloseableHttpClient httpclient = HttpClients.createDefault();
				HttpGet httpGet = new HttpGet("http://"+IP+"/api/MedicineAPI/"+Bed_MissionID);
				try(CloseableHttpResponse response = httpclient.execute(httpGet)) {
					System.out.println(response.getCode() + " " + response.getReasonPhrase());
					HttpEntity entity = response.getEntity();
					String res_data  = EntityUtils.toString(entity); //response
					if(res_data.equals("[]")){
						Medicine = "No";
					}
					else {
						JSONArray jsonArray = new JSONArray(res_data);
						medicine_list = new String[jsonArray.length()];
						medicine_SYSID = new String[jsonArray.length()];

						for(int i = 0; i<jsonArray.length(); i++){
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							String SYSID = jsonObject.getString("SYSID");
							medicine_list[i] = "http://"+IP+"/Mission/MedicineList/"+SYSID;
							medicine_SYSID[i] = SYSID;
						}

						StringBuilder sb = new StringBuilder();
						StringBuilder sb2 = new StringBuilder();
						for (int i = 0; i < medicine_list.length; i++) {
							sb.append(medicine_list[i]).append(",");
							sb2.append(medicine_SYSID[i]).append(",");
						}

						SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putString("medicine_list",sb.toString());
						editor.putString("medicine_SYSID",sb.toString());
						editor.apply();
					}

				}catch (ParseException | IOException | JSONException e) {
					e.printStackTrace();
				}
			}
			else {
				Medicine = "No";
			}
		}
	};

	Runnable runnable6 = new Runnable() {
		@Override
		public void run() {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost("http://"+IP+"/api/MedicineAPI/");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("MissionID", MissionID_array[0]));
			nvps.add(new BasicNameValuePair("SYSID", medicine_SYSID[medicine_index]));
			nvps.add(new BasicNameValuePair("Nurse", UserID));
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

}
