package com.example.videomonitor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

public class IntentServiceUPD extends IntentService {
    private Lock lock = new ReentrantLock();
	WifiManager manager = (WifiManager) this
            .getSystemService(Context.WIFI_SERVICE);

	UdpHelper udphelper = new UdpHelper(manager);
    public IntentServiceUPD() {  
        //必须实现父类的构造方法  
        super("IntentService");  
    }  
      
    @Override  
    public IBinder onBind(Intent intent) {  
        System.out.println("onBind");  
        return super.onBind(intent);  
    }

    @Override  
    public void onCreate() {  
        System.out.println("onCreate");  
        super.onCreate();  
    }  
  
    @Override  
    public void onStart(Intent intent, int startId) {  
        System.out.println("onStart");  
        super.onStart(intent, startId);  
    }  
    
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) {  
        System.out.println("onStartCommand");  
        return super.onStartCommand(intent, flags, startId);  
    }  
    @Override  
    protected void onHandleIntent(Intent intent) {  
        //Intent是从Activity发过来的，携带识别参数，根据参数不同执行不同的任务
            Runnable r1 = new sendUdpMessage();
            new Thread(r1).start();
            Runnable r2= new receiveUdpMessage();
            new Thread(r2).start();
    }
	    
      
  
    @Override  
    public void onDestroy() {  
        System.out.println("onDestroy");  
        super.onDestroy();  
    } 
    public class sendUdpMessage implements Runnable{

		@Override
		public void run() {
            while(udphelper.count<5){
                try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                udphelper.send("{\"Client\":\"Ping\"}");
                lock.lock();
                udphelper.count++;
                lock.unlock();
            }
		}
	}
	public class receiveUdpMessage implements Runnable{

		@Override
		public void run() {
            while(true){
            	udphelper.StartListen();
                if(udphelper.strMsg.equals("{\"AlarmServer\":\"Ping\"}")) {
                    lock.lock();
                    udphelper.count=0;
                    lock.unlock();
                }
            }
		}
	}
  
}  