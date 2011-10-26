package com.deadbeat.bluetoothalertfree;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


public class BluetoothNotifyWorker extends Activity {
	public static final String BT = "BluetoothNotify";

	// ***** enable logging ? ***** //
	public static final Boolean loggingEnabled = false;

	// ***** free version ? ***** //
	public static final Boolean freeVersion = true;

	public ArrayList<String> btDeviceName_ar = new ArrayList<String>();
	public ArrayList<String> btDeviceAddress_ar = new ArrayList<String>();
	public ListView btDeviceList;
	private Activity parent;
	
	BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();

	public BluetoothNotifyWorker(Activity parent) {
		this.parent=parent;
	}
	protected void listDevices(String version) {
		doLog("--> Display device list");
	    // Display list of devices
	    btDeviceList = (ListView)parent.findViewById(R.id.list_bluetooth_devices);        
	    btDeviceList.setAdapter(new ArrayAdapter<String>(parent, android.R.layout.simple_list_item_1, btDeviceName_ar));
	
	    TextView versionDisplay = (TextView)parent.findViewById(R.id.text_main_screen_version);
	    versionDisplay.setText("v."+version);
	    // Make device list clickable
	    btDeviceList.setTextFilterEnabled(true);
	    btDeviceList.setOnItemClickListener(new OnItemClickListener() {
	    	@Override
	    	public void onItemClick(AdapterView<?> a, View v, int position, long id) {
	    		// Do something when clicked
	
	    		Intent devicePreferencesActivity = new Intent(parent.getBaseContext(), DeviceOptions.class);
	    		devicePreferencesActivity.putExtra("deviceName", btDeviceName_ar.get(position));
	    		devicePreferencesActivity.putExtra("deviceAddress", btDeviceAddress_ar.get(position));
	    		parent.startActivity(devicePreferencesActivity);
	    		
	    	}
	    });
	}

	protected void startBTNotifyService() {

    	// Start the Notify Service
    	doLog("--> Starting BTNotifyService");
    	try {
    		Intent svc = new Intent(parent.getBaseContext(), BTNotifyService.class);
    		parent.startService(svc);
    	} catch (Exception e) {
    		Log.e(BT,"ER> Error starting BluetoothNotifyService");
    		e.printStackTrace();
    	}	
	}
	
	protected void doLog(String string) {
		if(loggingEnabled == true) {
			Log.d(BT,string);
		}
	}
	
	protected void getBTDevices() {
		// Get paired devices
		doLog("--> Getting bonded devices");
		try {
			Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();	
			doLog("--> Found ("+pairedDevices.size()+") devices");
			if(pairedDevices.size() > 0) {
				for(BluetoothDevice device : pairedDevices)	{
					// Store the device in an ArrayList
					doLog("--> Adding device ("+device.getName()+") to device list");
					btDeviceName_ar.add(device.getName());
					btDeviceAddress_ar.add(device.getAddress());
				}
			}
		} catch(Exception e) {
			Log.e(BT,"--> There was an error fetching paired devices");
			e.printStackTrace();
		}
	}
	
	protected void shutdownOnConflict() {
        // We need to exit the app if the other version (free/paid) is installed.
        AlertDialog.Builder alertbox = new AlertDialog.Builder(parent);
 
        if(freeVersion == true) {
        	alertbox.setMessage("WARNING: Another version of Bluetooth Notify detected!\n\n" +
        			"You are trying to launch Bluetooth Notify (Free) while " +
        			"the full version of Bluetooth Notify is installed on this device.\n\n" +
        			"Neither version can function while both are installed at the same time.  " +
        			"It is recommended that you uninstall Bluetooth Notify (Free).");
        } else {
        	alertbox.setMessage("WARNING: Another version of Bluetooth Notify detected!\n\n" +
        			"You are trying to launch Bluetooth Notify while " +
        			"the free version of Bluetooth Notify is installed on this device.\n\n" +
        			"Neither version can function while both are installed at the same time.  " +
        			"It is recommended that you uninstall Bluetooth Notify (Free).");
        }
        alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) {
                // the button was clicked
                parent.finish();
            }
        });

        alertbox.show();
	}
}
