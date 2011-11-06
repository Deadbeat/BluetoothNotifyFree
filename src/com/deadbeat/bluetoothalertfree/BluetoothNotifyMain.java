package com.deadbeat.bluetoothalertfree;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.deadbeat.bluetoothnotifylib.AppDetector;
import com.deadbeat.bluetoothnotifylib.BluetoothNotifyWorker;
import com.deadbeat.bluetoothnotifylib.Globals;

public class BluetoothNotifyMain extends Activity {

	private final int KILLSVC = Menu.FIRST + 1;
	private BluetoothNotifyWorker worker;

	public BluetoothNotifyWorker getWorker() {
		return this.worker;
	}

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle btNotify) {
		super.onCreate(btNotify);
		Globals globals = new Globals();

		// Define free/paid version
		globals.setFreeVersion(true);

		// Enable/Disable logging
		globals.setLoggingEnabled(true);
		setWorker(new BluetoothNotifyWorker(this, globals));
		AppDetector detect = new AppDetector();

		getWorker().doLog("-------------------------------");
		getWorker().doLog("--> Client starting up");

		if (detect.isAppInstalled(this, "com.deadbeat.bluetoothalert") == true) {
			getWorker().shutdownOnConflict();
		}

		getWorker().startBTNotifyService();

		setContentView(R.layout.deviceselect);

		getWorker().getBTDevices();
		getWorker().buildDeviceListView(detect.getVersion(this, this.getClass().getPackage().getName()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, this.KILLSVC, 0, "Stop Bluetooth Notify Service (Not Recommended)");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case KILLSVC:
			// Prompt user for confirmation
			DialogInterface.OnClickListener confirmListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						// User is an Idiot, and said "Yes"
						// Kill service
						Intent svc = new Intent(getBaseContext(), com.deadbeat.bluetoothnotifylib.BTNotifyService.class);
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
			builder.setMessage(
					"Are you sure you want to stop the service?\n\n"
							+ "Without BluetoothNotify Service, you will not recieve ANY device notifications.\n\n"
							+ "NOTE: The service will be restarted when you re-open this application.")
					.setPositiveButton("Yes", confirmListener).setNegativeButton("No", confirmListener).show();
		}
		return super.onOptionsItemSelected(item);
	}

	public void setWorker(BluetoothNotifyWorker worker) {
		this.worker = worker;
	}
}
