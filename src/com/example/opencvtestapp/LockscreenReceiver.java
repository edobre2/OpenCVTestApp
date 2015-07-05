package com.example.opencvtestapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.WindowManager;

public class LockscreenReceiver extends BroadcastReceiver {
	private static boolean wasScreenOn = true;
	
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			Intent localIntent = new Intent(context, FDActivity.class);
			localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			localIntent.addFlags(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
			context.startActivity(localIntent);
		}
	} 
	/*
	@Override
	public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {

        	wasScreenOn=false;
        	Intent intent11 = new Intent(context,FDActivity.class);
        	intent11.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        	context.startActivity(intent11);

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {

        	wasScreenOn=true;
        	Intent intent11 = new Intent(context,FDActivity.class);
        	intent11.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
       else if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
        	Intent intent11 = new Intent(context, FDActivity.class);

        	intent11.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
           context.startActivity(intent11);
       }
	}
	*/
	
}
