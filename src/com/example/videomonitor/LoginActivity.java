package com.example.videomonitor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.example.videomonitor.IntentServiceUPD.receiveUdpMessage;
import com.example.videomonitor.IntentServiceUPD.sendUdpMessage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity{
	private EditText userName;
	private EditText passWord;
	private Button login;
	private ProgressDialog LoginDialog;
	String Username;
	String Password;
	Boolean isMonitorPhoneInfo=false;
	Boolean isPlacePhoneInfo=false;
	Boolean isGetPlaceInfo=false;
	UdpHelper udphelper;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		WifiManager manager = (WifiManager) this
                .getSystemService(Context.WIFI_SERVICE);
udphelper = new UdpHelper(manager);
		initView();
		login.setOnClickListener(new buttonOnClickListener());
	}
	private void initView(){
		userName=(EditText)findViewById(R.id.username);
		passWord=(EditText)findViewById(R.id.password);
		login=(Button)findViewById(R.id.login_btn);
	}
	public class buttonOnClickListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			//LoginDialog = new ProgressDialog(LoginActivity.this);
			//LoginDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			//LoginDialog.setMessage("正在登入，请稍后……");
			//LoginDialog.setCancelable(true);
			//LoginDialog.show();
			//Runnable r = new LoginHandler();
		    //new Thread(r).start();
			//Intent ServiceIntent = new Intent(LoginActivity.this,IntentServiceUPD.class);
			//startService(ServiceIntent);
			Runnable r1 = new sendUdpMessage();
		    new Thread(r1).start();
			//uphelper.start();
		}
	}
	 public class sendUdpMessage implements Runnable{

			@Override
			public void run() {
				udphelper.send("{\"Client\":\"Register\",\"Username\":\"SXMUser1\",\"MonitorID\":1}");
				udphelper.StartListen();
				Message message=new Message();
				if(udphelper.strMsg.equals("{\"AlarmServer\":\"Register\"}"));{
					message.what=0;
				}
				registerhandler.sendMessage(message);
			}
		}
	 private Handler registerhandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					Intent intentservice=new Intent();
					intentservice.setClass(LoginActivity.this, IntentServiceUPD.class);
					startService(intentservice);
					break;
				default:
					new AlertDialog.Builder(LoginActivity.this)
					.setTitle("登入失败").setMessage("用户密码错误，请重新登入……")
					.setPositiveButton("确定", null).show();
			        break;
				
				}
			}
		};
		
	
	
	public class LoginHandler implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// 命名空间  
	        String nameSpace = "http://monitorSafe/MonitorClient/";  
	        // 调用的方法名称  
	        String methodName = "Login";  
	        // EndPoint  
	        String endPoint = "http://222.25.140.1:8082/axis2/services/MonitorClient";  
	        // SOAP Action  
	        String soapAction = "http://monitorSafe/MonitorClient/Login"; 
	        Username=userName.getText().toString().trim();
			Password=passWord.getText().toString().trim();
	  
	        // 指定WebService的命名空间和调用的方法名  
	        SoapObject rpc = new SoapObject(nameSpace, methodName);  
	  
	        // 设置需调用WebService接口需要传入的两个参数mobileCode、userId  
	        //rpc.addProperty("username", Username);  
	        //rpc.addProperty("password", Password);
	        rpc.addProperty("username", "SXMUser1");  
	        rpc.addProperty("password", "SXMUser1");
	  
	        // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本  
	        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);  
	        envelope.bodyOut = rpc;  
	        SharedPreferences.Editor editor = getSharedPreferences(
					"data", MODE_PRIVATE).edit();
	        editor.putString("username", Username);
	        editor.putString("password", Password);
	        Message message = new Message();

			HttpTransportSE transport = new HttpTransportSE(endPoint);
			try {
				// 调用WebService
				transport.call(soapAction, envelope);
				if (envelope.getResponse() != null) {
					// 第6步：使用getResponse方法获得WebService方法的返回结果
					SoapObject soapObject = (SoapObject) envelope.bodyIn;
					// 通过getProperty方法获得Product对象的属性值
					String result = soapObject.getProperty("monitorID").toString();
					int monitorID=Integer.parseInt(result);
					if(monitorID==-1){
						message.what=-1;
					}
					if(monitorID==-2){
						message.what=-2;
					}
					message.what = 0;
		        	editor.putInt("monitorId", monitorID);
				}

				else {
					message.what=1;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
	        handler.sendMessage(message);
	        editor.putLong("timestamp", System.currentTimeMillis());
			editor.commit();
		}
		
	}
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Runnable r1=new getMapPlaceThread();
				new Thread(r1).start();
				Runnable r2=new getPlacePhoneThread();
				new Thread(r2).start();
				Runnable r3=new getMonitorPhoneThread();
				new Thread(r3).start();
				break;
			case -1:
				new AlertDialog.Builder(LoginActivity.this)
				.setTitle("登入失败").setMessage("用户密码错误，请重新登入……")
				.setPositiveButton("确定", null).show();
		        break;
			case -2:
				new AlertDialog.Builder(LoginActivity.this)
				.setTitle("登入失败").setMessage("该用户名不存在……")
				.setPositiveButton("确定", null).show();
		       break;
			default:
				break;
			}
		}
	};
	public class getMapPlaceThread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				getMapPlace ();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public class getPlacePhoneThread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				getPersonalInfo ("GetPlacePhone");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public class getMonitorPhoneThread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				getPersonalInfo ("GetMonitorPhone");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private Handler PersonelInfoHandler= new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if(isGetPlaceInfo==true&&isMonitorPhoneInfo==true&&isPlacePhoneInfo==true){
					LoginDialog.dismiss();
					Intent intent = new Intent();
					intent.setClass(LoginActivity.this, MainActivity.class);
					startActivity(intent);
				}
				break;
			default:
				LoginDialog.dismiss();
				new AlertDialog.Builder(LoginActivity.this)
						.setTitle("登入信息").setMessage("登入失败，请重新登入……")
						.setPositiveButton("确定", null).show();
				break;
			}
		}
	};
	
	private void getMapPlace() throws JSONException{
		// 命名空间  
        String nameSpace = "http://monitorSafe/MonitorClient/";  
        // 调用的方法名称  
        String methodName = "GetMapPlace";  
        // EndPoint  
        String endPoint = "http://222.25.140.1:8082/axis2/services/MonitorClient";  
        // SOAP Action  
        String soapAction =  "http://monitorSafe/MonitorClient/GetMapPlace";  
  
        // 指定WebService的命名空间和调用的方法名  
        SoapObject rpc = new SoapObject(nameSpace, methodName);  
  
        // 设置需调用WebService接口需要传入的两个参数mobileCode、userId
        Message message = new Message();
		SharedPreferences SharedPreference = getSharedPreferences("data",MODE_PRIVATE);
		String username = SharedPreference.getString("username", "");
        rpc.addProperty("username", username); 
        rpc.addProperty("placeType", 0); 
  
        // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本  
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);  
  
        envelope.bodyOut = rpc;  
        envelope.setOutputSoapObject(rpc);
        HttpTransportSE transport = new HttpTransportSE(endPoint);  
        try {  
            // 调用WebService  
            transport.call(soapAction, envelope); 
            if (envelope.getResponse() != null) {
            	// 获取返回的数据  
                SoapObject object = (SoapObject) envelope.bodyIn;  
                // 获取返回的结果  
                //String result = object.toString();
                String result = object.getProperty(0).toString();
                JSONArray arr = new JSONArray(result); 
                Log.d("aaaaaaaaaaaaaaaaaa", result);
                for (int i = 0; i < arr.length(); i++) {  
                    JSONObject MapPlace = (JSONObject) arr.get(i);  
                    String address = MapPlace.getString("address");  
                    int id= MapPlace.getInt("id");
                    int lat=MapPlace.getInt("lat");
                    int lon=MapPlace.getInt("lon");
                    String name=MapPlace.getString("name");
                    int typeID=MapPlace.getInt("typeID");
                    SharedPreferences.Editor editor = getSharedPreferences(
        					"MapPlace"+i, MODE_PRIVATE).edit();
                    editor.putString("address",address);
                    editor.putInt("id",id );
                    editor.putInt("lat",lat );
                    editor.putInt("lon",lon );
                    editor.putInt("typeID",typeID );
                    editor.putString("name",name);
                    editor.commit();
                }
				message.what = 1;
				isGetPlaceInfo=true;
			}
        } catch (Exception e) {  
            e.printStackTrace();  
            message.what=-1;
        }
        PersonelInfoHandler.sendMessage(message);
	}
	
	private void getPersonalInfo (String MethodName) throws JSONException{
		// 命名空间  
        String nameSpace = "http://monitorSafe/MonitorClient/";  
        // 调用的方法名称  
        String methodName = MethodName;  
        // EndPoint  
        String endPoint = "http://222.25.140.1:8082/axis2/services/MonitorClient";  
        // SOAP Action  
        String soapAction =  "http://monitorSafe/MonitorClient/"+MethodName; 
        Message message = new Message();
  
        // 指定WebService的命名空间和调用的方法名  
        SoapObject rpc = new SoapObject(nameSpace, methodName);  
  
        // 设置需调用WebService接口需要传入的两个参数mobileCode、userId  

		SharedPreferences SharedPreference = getSharedPreferences("data",MODE_PRIVATE);
		int monitorId = SharedPreference.getInt("monitorId", 1);
        rpc.addProperty("monitorID", monitorId); 
  
        // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本  
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);  
  
        envelope.bodyOut = rpc;  
        envelope.setOutputSoapObject(rpc);
        HttpTransportSE transport = new HttpTransportSE(endPoint);  
        try {  
            // 调用WebService  
            transport.call(soapAction, envelope);
            if (envelope.getResponse() != null) {
            	// 获取返回的数据  
                SoapObject object = (SoapObject) envelope.bodyIn;  
                // 获取返回的结果  
                //String result = object.toString();
                String result = object.getProperty(0).toString(); 
                // 将WebService返回的结果显示在TextView中
                JSONArray arr = new JSONArray(result);  
                String id = arr.getString(0);
                SharedPreferences.Editor editor = getSharedPreferences(
        				"data", MODE_PRIVATE).edit();
                editor.putString(methodName, id);
                editor.commit();
				if(MethodName.equals("GetMonitorPhone")){
					isMonitorPhoneInfo=true;
					message.what = 1;
				}else{
					isPlacePhoneInfo=true;
					message.what = 1;
				}
			}
        } catch (Exception e) {  
            e.printStackTrace();  
            message.what=-1;
        }
        PersonelInfoHandler.sendMessage(message);
	}
	@Override
	protected void onDestroy(){
		super.onDestroy();
		ActivityCollector.removeActivity(this);
	}
	/**
	 * 退出程序时提示对话框
	 */
	protected void dialog() { 
        AlertDialog.Builder builder = new Builder(LoginActivity.this); 
        builder.setMessage("确定要退出登入吗?"); 
        builder.setTitle("提示"); 
        builder.setPositiveButton("确认", 
                new android.content.DialogInterface.OnClickListener() { 
                    @Override
                    public void onClick(DialogInterface dialog, int which) { 
                        dialog.dismiss();
                       ActivityCollector.finishAll();
                    } 
                }); 
        builder.setNegativeButton("取消", 
                new android.content.DialogInterface.OnClickListener() { 
                    @Override
                    public void onClick(DialogInterface dialog, int which) { 
                        dialog.dismiss(); 
                    } 
                }); 
        builder.create().show(); 
    }
    
	/**
	 * 按后退键，程序提示对话框
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { 
            dialog(); 
            return false; 
        } 
        return false; 
    }
    */
}
