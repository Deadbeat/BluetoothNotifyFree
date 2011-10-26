package com.deadbeat.bluetoothalertfree;

import static com.deadbeat.bluetoothalertfree.BluetoothNotifyWorker.BT;
import static com.deadbeat.bluetoothalertfree.BluetoothNotifyWorker.freeVersion;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

public class DeviceOptions extends PreferenceActivity {
	String  deviceName;
	String  deviceAddress;

	// Device Enabled
	private boolean pDeviceEnabled;
	
	// Connect variables
    private boolean pConnectEnable;
    private boolean pConnectRingtoneEnable;
    private boolean pConnectLEDEnable;
    private boolean pConnectVibrateEnable;
    private boolean pConnectNotificationEnable;
    private boolean pConnectToastEnable;
    private String  pConnectRingtone;
    private String  pConnectLEDColor;
    
    // Disconnect Variables
    private boolean pDisconnectEnable;
    private boolean pDisconnectRingtoneEnable;
    private boolean pDisconnectLEDEnable;
    private boolean pDisconnectVibrateEnable;
    private boolean pDisconnectNotificationEnable;
    private boolean pDisconnectToastEnable;
    private String  pDisconnectRingtone;
    private String  pDisconnectLEDColor;
    
    private CheckBoxPreference cRingtoneCheckBoxPreference;
    private CheckBoxPreference dRingtoneCheckBoxPreference;
    
    SharedPreferences prefs;
    PreferenceActivity preferenceActivity;
    OnSharedPreferenceChangeListener ospcListener;
    BluetoothNotifyWorker worker;
    
    private void saveSharedPreferencesToFile() {
    	
    	// Write preferences to properties file
    	worker.doLog("==> Setting properties");
    	Properties properties = new Properties();

    	// Device Enabled
    	properties.setProperty("pDeviceEnabled",new Boolean(pDeviceEnabled).toString());
    	
    	// Connect Properties
    	properties.setProperty("pConnectEnable", new Boolean(pConnectEnable).toString());
    	properties.setProperty("pConnectRingtoneEnable", new Boolean(pConnectRingtoneEnable).toString());
    	properties.setProperty("pConnectRingtone", pConnectRingtone);
    	properties.setProperty("pConnectLEDEnable", new Boolean(pConnectLEDEnable).toString());
    	properties.setProperty("pConnectLEDColor", pConnectLEDColor);
    	properties.setProperty("pConnectNotificationEnable", new Boolean(pConnectNotificationEnable).toString());
    	properties.setProperty("pConnectToastEnable", new Boolean(pConnectToastEnable).toString());
    	properties.setProperty("pConnectVibrateEnable", new Boolean(pConnectVibrateEnable).toString());

    	// Disconnect Properties
    	properties.setProperty("pDisconnectEnable", new Boolean(pDisconnectEnable).toString());
    	properties.setProperty("pDisconnectRingtoneEnable", new Boolean(pDisconnectRingtoneEnable).toString());
    	properties.setProperty("pDisconnectRingtone", pDisconnectRingtone);
    	properties.setProperty("pDisconnectLEDEnable", new Boolean(pDisconnectLEDEnable).toString());
    	properties.setProperty("pDisconnectLEDColor", pDisconnectLEDColor);
    	properties.setProperty("pDisconnectNotificationEnable", new Boolean(pDisconnectNotificationEnable).toString());
    	properties.setProperty("pDisconnectToastEnable", new Boolean(pDisconnectToastEnable).toString());
    	properties.setProperty("pDisconnectVibrateEnable", new Boolean(pDisconnectVibrateEnable).toString());

    	try {
    		String propertiesFileName = deviceAddress+".properties";
    		worker.doLog("==> Setting file name: "+propertiesFileName);
    		worker.doLog("==> Create output stream");
    		FileOutputStream fileOut = openFileOutput(propertiesFileName, Context.MODE_WORLD_READABLE);
			worker.doLog("==> Writing properties");
			properties.storeToXML(fileOut, deviceName);
			worker.doLog("==> Closing stream");
			fileOut.close();
//			Toast.makeText(this, "Device options saved", Toast.LENGTH_SHORT).show();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e(BT,"ER> (DeviceOptions) Preferences File not found for device: "+deviceAddress);
			Toast.makeText(this, "Error writing device options!", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			finish();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "Error writing device options!", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			finish();
		} 
    	
    }
    
    protected void onCreate(Bundle btNotify) {
    	super.onCreate(btNotify);
    	Bundle extras = getIntent().getExtras();
    	if(extras != null) {
    		deviceName = extras.getString("deviceName");
    		deviceAddress = extras.getString("deviceAddress");
    	}

    	worker = new BluetoothNotifyWorker(this);
    	
    	deviceName = deviceName.replaceAll(" ", "");
    	worker.doLog("==> Displaying preferences for device: "+deviceName+" ("+deviceAddress+")");
    	
    	// Strip spaces from device name for preference filename
    	deviceAddress = deviceAddress.replaceAll(":", "-");
    	
    	worker.doLog("==> Setting preferenceManager.sharedPreferenceName: "+deviceAddress);
    	getPreferenceManager().setSharedPreferencesName(deviceAddress);
    	prefs = getPreferenceManager().getSharedPreferences();

		// The only way i can think to do this, is to get preferences and write
		// properties every time a pref is changed... so... hope it's not rough on performance.
    	ospcListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				
				getPrefs();
				saveSharedPreferencesToFile();
				
				// When the user chooses an LED color, we should flash the LED to give them a preview.
				// Must check for Device Enabled before hand though - if we don't check
				// the led will flash when the default preferences are written the first time the app
				// sees a new device.
				Boolean thisDeviceEnabled = sharedPreferences.getBoolean("pref_enable", false);
				
				// Only attempt to flash when the preference that was changed was an LED color
				if(key.contains("_led_color") && thisDeviceEnabled == true) {

					// Set up a notification manager
					NotificationManager nm = ( NotificationManager ) getSystemService( NOTIFICATION_SERVICE );
					Notification notification = new Notification();
			    	// Hack for LEDColor
			    	long longValue = Long.parseLong(sharedPreferences.getString(key, "ff0000ff"), 16);
			    	notification.ledARGB = (int)longValue;
			    	notification.flags = Notification.FLAG_SHOW_LIGHTS; 
			    	notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
			    	notification.ledOnMS = 750; 
			    	notification.ledOffMS = 1500; 

			    	// Throw the notification, sleep for 1 second, and cancel it
			    	// This makes the LED flash only 1 time, instead of forever.
			    	nm.notify(675645342, notification);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					nm.cancel(675645342);
				}
			}
		};

		// Listen for preference changes
		prefs.registerOnSharedPreferenceChangeListener(ospcListener);
		
    	addPreferencesFromResource(R.xml.devicepreferences);

    	if(freeVersion == true) {
	    	cRingtoneCheckBoxPreference = (CheckBoxPreference)getPreferenceScreen().findPreference("pref_connect_ringtone_enable");
			cRingtoneCheckBoxPreference.setSummary("Disabled in free version.\nGet the full version to enable this option.");
			cRingtoneCheckBoxPreference.setSelectable(false);
			cRingtoneCheckBoxPreference.setChecked(false);

	    	dRingtoneCheckBoxPreference = (CheckBoxPreference)getPreferenceScreen().findPreference("pref_disconnect_ringtone_enable");
			dRingtoneCheckBoxPreference.setSummary("Disabled in free version.\nGet the full version to enable this option.");
			dRingtoneCheckBoxPreference.setSelectable(false);
			dRingtoneCheckBoxPreference.setChecked(false);
			
			Editor prefsEditor = prefs.edit();
			prefsEditor.putBoolean("pref_connect_ringtone_enable", false);
			prefsEditor.putBoolean("pref_disconnect_ringtone_enable", false);
			prefsEditor.commit();
    	}
    }

    protected void onDestroy() {
    	super.onDestroy();
    	getPrefs();
    	saveSharedPreferencesToFile();
    	prefs.unregisterOnSharedPreferenceChangeListener(ospcListener);
    }
    protected void onPause() {
    	super.onPause();

    	// Just to make sure we get all prefs written - we'll write them when we leave the pref screen
    	getPrefs();
    	saveSharedPreferencesToFile();
    	
    	// Stop listening for changes when not on this screen
    	prefs.unregisterOnSharedPreferenceChangeListener(ospcListener);
    	worker.doLog("==> Unregister Change Listener");

    }
    
    protected void onResume() {
    	super.onResume();
    	prefs.registerOnSharedPreferenceChangeListener(ospcListener);
    }
    
    private void getPrefs() {
    	
    	// Device Enabled
    	pDeviceEnabled      			= prefs.getBoolean("pref_enable", false);
    	
    	// Connect Preferences
    	pConnectEnable    				= prefs.getBoolean("pref_connect_enable", true);
    	pConnectRingtoneEnable			= prefs.getBoolean("pref_connect_ringtone_enable", true);
    	pConnectRingtone				= prefs.getString("pref_connect_ringtone", "content://settings/system/notification_sound");
    	pConnectLEDEnable				= prefs.getBoolean("pref_connect_led_enable", false);
    	pConnectLEDColor				= prefs.getString("pref_connect_led_color", "ff0000ff");
    	pConnectNotificationEnable		= prefs.getBoolean("pref_connect_notification_enable", false);
    	pConnectToastEnable				= prefs.getBoolean("pref_connect_toast_enable", false);
    	pConnectVibrateEnable			= prefs.getBoolean("pref_connect_vibrate_enable", false);

    	// Disconnect Preferences
    	pDisconnectEnable    			= prefs.getBoolean("pref_disconnect_enable", false);
    	pDisconnectRingtoneEnable		= prefs.getBoolean("pref_disconnect_ringtone_enable", true);
    	pDisconnectRingtone				= prefs.getString("pref_disconnect_ringtone", "content://settings/system/notification_sound");
    	pDisconnectLEDEnable			= prefs.getBoolean("pref_disconnect_led_enable", false);
    	pDisconnectLEDColor				= prefs.getString("pref_disconnect_led_color", "ff0000ff");
    	pDisconnectNotificationEnable	= prefs.getBoolean("pref_disconnect_notification_enable", false);
    	pDisconnectToastEnable			= prefs.getBoolean("pref_disconnect_toast_enable", false);
    	pDisconnectVibrateEnable		= prefs.getBoolean("pref_disconnect_vibrate_enable", false);

    }
}
