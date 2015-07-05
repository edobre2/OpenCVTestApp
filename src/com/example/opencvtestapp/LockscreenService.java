package com.example.opencvtestapp;

import android.content.BroadcastReceiver;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class LockscreenService extends Service {
	BroadcastReceiver mReceiver;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onCreate() {
		 KeyguardManager.KeyguardLock kl;
		 KeyguardManager km =(KeyguardManager)getSystemService(KEYGUARD_SERVICE);
	     kl= km.newKeyguardLock("IN");
	     kl.disableKeyguard()	;
	
	     IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
	     filter.addAction(Intent.ACTION_SCREEN_OFF);
	
	     mReceiver = new LockscreenReceiver();
	     registerReceiver(mReceiver, filter);
	
	    super.onCreate();
	}

	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}
}
