package com.deadbeat.bluetoothalertfree;

import com.deadbeat.bluetoothalertfree.BluetoothNotifyWorker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class BluetoothNotifyMain extends Activity {

	private final int KILLSVC = Menu.FIRST + 1;	
	private BluetoothNotifyWorker worker;

	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(0,KILLSVC,0,"Stop Bluetooth Notify Service (Not Recommended)");
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case KILLSVC: 
			// Prompt user for confirmation
			DialogInterface.OnClickListener confirmListener = new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch(which) {
					case DialogInterface.BUTTON_POSITIVE:
						// User is an Idiot, and said "Yes"
						// Kill service
						Intent svc = new Intent(getBaseContext(),BTNotifyService.class);
						stopService(svc);
						finish();
						break;
						
					case DialogInterface.BUTTON_NEGATIVE:
						// User said "No".
						break;
					}
					
				}
			};
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Are you sure you want to stop the service?\n\n" +
					"Without BluetoothNotify Service, you will not recieve ANY device notifications.\n\n" +
					"NOTE: The service will be restarted when you re-open this application.")
					.setPositiveButton("Yes", confirmListener)
					.setNegativeButton("No", confirmListener)
					.show();
		}
		return super.onOptionsItemSelected(item);
	}

	/** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle btNotify) {
    	super.onCreate(btNotify);
    	worker = new BluetoothNotifyWorker(this);
    	AppDetector detect = new AppDetector();
    	
    	worker.doLog("-------------------------------");
    	worker.doLog("--> Client starting up");
    	worker.doLog("--> Detecting other versions");

    	if(detect.isAppInstalledFromActivity(this, "com.deadbeat.bluetoothalert") == true) {
    		worker.shutdownOnConflict();
    	}
    	
    	worker.startBTNotifyService();
        
    	setContentView(R.layout.deviceselect);
        
    	worker.getBTDevices();
    	worker.listDevices(detect.getVersion(this,this.getClass().getPackage().getName()));
    }
}
