package com.deadbeat.bluetoothalertfree;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.deadbeat.bluetoothnotifylib.Globals;

public class StartAtBootServiceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.d("BluetoothNotify", "--> Received BOOT_COMPLETED");
			Globals globals = new Globals();
			globals.setFreeVersion(true);
			globals.setLoggingEnabled(true);
			Intent i = new Intent(context, com.deadbeat.bluetoothnotifylib.BTNotifyService.class);
			i.putExtra("Globals", globals);
			context.startService(i);
		}
	}
}
