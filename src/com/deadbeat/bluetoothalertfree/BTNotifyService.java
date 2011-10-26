package com.deadbeat.bluetoothalertfree;

import static com.deadbeat.bluetoothalertfree.BluetoothNotifyWorker.BT;
import static com.deadbeat.bluetoothalertfree.BluetoothNotifyWorker.freeVersion;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class BTNotifyService extends Service {
	
	// Context and Constants
	public final String ACTION_ACL_CONNECTED 	= "android.bluetooth.device.action.ACL_CONNECTED";
	public final String ACTION_ACL_DISCONNECTED = "android.bluetooth.device.action.ACL_DISCONNECTED";
	public final String DEVICE_CONNECTED 		= "deviceConnected";
	public final String DEVICE_DISCONNECTED 	= "deviceDisconnected";
	public final String EXTRA_DEVICE 			= "android.bluetooth.device.extra.DEVICE";
	
	// Some (hopefully) unique numbers for our notification
	public final int BTNOTIFY_NOTIFICATION 	= 771892746;
	public final int LED_NOTIFICATION 		= 279827593;
	
    private Properties properties = new Properties();
    
    private Notification notification;
    private Notification ledNotification = new Notification();
    private BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();

    /*
     * getStringProperty
     * Returns: String <property value>
     */
    private String getStringProperty(String propertyKey) {
//    	Log.d(BT,">>> Property Requested: "+propertyKey);
    	if(properties.containsKey(propertyKey)) {
    		String propertyValue = properties.getProperty(propertyKey);
//    		Log.d(BT,">>> Property ("+propertyKey+") found: "+propertyValue);
    		return propertyValue;
    	} else {
    		Log.e(BT,">>> Requested device property ("+propertyKey+") was not found");
    		return null;
    	}
    }
    
    /*
     * getBooleanProperty
     * Returns: Boolean <property value> 
     */
    private Boolean getBooleanProperty(String propertyKey) {
//    	Log.d(BT,">>> Property Requested: "+propertyKey);
    	if(properties.containsKey(propertyKey)) {
    		if(properties.getProperty(propertyKey).equals("true")) {
//        		Log.d(BT,">>> Property ("+propertyKey+") true.");
    			return true;
    		} else {
//        		Log.d(BT,">>> Property ("+propertyKey+") false.");
    			return false;
    		}
    	} else {
    		Log.e(BT,">>> Requested device property ("+propertyKey+") was not found");
    		return false;
    	}

    }
    
    /*
     * loadPropertiesForDevice
     * Desc: Loads XML properties file for specified device
     */
    protected void loadPropertiesForDevice(final String dAddress) {
//    	Log.d(BT,">>> Getting properties for device: "+dAddress);
    	
		String propertiesFileName = dAddress+".properties";
//		Log.d(BT,">>> Setting file name: "+propertiesFileName);
//		Log.d(BT,">>> Create input stream");
		try {
			FileInputStream fileIn = openFileInput(propertiesFileName);
//			Log.d(BT,">>> Reading properties");
			properties.loadFromXML(fileIn);
//			Log.d(BT,">>> Closing stream");
			fileIn.close();
			
		} catch(FileNotFoundException e) {
			Log.w(BT,">>> Bluetooth device connected, but no device config file found.  Ignoring device.");
			e.printStackTrace();
		} catch(IOException e) {
			Log.e(BT,">>> IOException reading device properties");
			e.printStackTrace();
		}
    }
    
    /*
     * doNotificationTone
     * Desc: Plays notification ringtone to the user
     */
    protected void doNotificationTone(String tone) {
    	if(freeVersion != true) {
	    	Uri toneURI = Uri.parse(tone);
	    	RingtoneManager.getRingtone(this, toneURI).play();
//	    	Log.d(BT,">>> Audible Notification sent");
    	}
    }
    
    /*
     * doLEDNotification
     * Desc: Flashes the LED one time in the specified color
     */
    protected void addLEDNotification(String LEDColor) {
    	// Hack for LEDColor
    	long longValue = Long.parseLong(LEDColor, 16);
    	ledNotification.ledARGB = (int)longValue;
    	ledNotification.flags = Notification.FLAG_SHOW_LIGHTS; 
//    	ledNotification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
    	ledNotification.ledOnMS = 750; 
    	ledNotification.ledOffMS = 1500; 
    }
    
    /*
     * doVibrateNotification
     * Desc: Vibrates the phone... duh!
     */
    protected void addVibrateNotification() {
    	
    	notification.defaults |= Notification.DEFAULT_VIBRATE;
    	
    }
    
    protected void doNotification(Boolean includeLED, String ledColor) {
    	// Set up a notification manager
		NotificationManager nm = ( NotificationManager ) getSystemService( NOTIFICATION_SERVICE );
		
		// Send out the notification
		nm.notify(BTNOTIFY_NOTIFICATION, notification);

		// LED Notification needs to be canceled - so we'll send it separately.
		if(includeLED == true) {
			nm.notify(LED_NOTIFICATION, ledNotification);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			nm.cancel(LED_NOTIFICATION);
		}
    }
    
    /*
     * notifyDeviceStateChange
     * Desc: Called when device state changes.  Determines which notifications should be sent.
     */
	protected void notifyDeviceStateChange(String deviceAddress, String stateChange) {

		// Once a device connects, read properties file
		final String devicePropertiesFileName = deviceAddress.replaceAll(":", "-");
		loadPropertiesForDevice(devicePropertiesFileName);
		
		// Now let's check to see if we need to do anything.
		if(getBooleanProperty("pDeviceEnabled") == true) {
//			Log.d(BT,">>> This device is enabled");

			Boolean ledEnable = false;
			String ledColor   = null;
			String deviceName = null;
			
			// Check to see if connect notification is enabled
			if(getBooleanProperty("pConnectEnable") == true && stateChange.equals(ACTION_ACL_CONNECTED)) {

				// Get the device name if we need to throw it later
				Boolean notificationEnable = getBooleanProperty("pConnectNotificationEnable");
				Boolean toastEnable		   = getBooleanProperty("pConnectToastEnable");
				
				if(notificationEnable == true || toastEnable == true) {
					BluetoothDevice btd = BTAdapter.getRemoteDevice(deviceAddress);
					deviceName = btd.getName();
				}
				
				// Connect Toast Notify
				if(getBooleanProperty("pConnectToastEnable")) {
//					Log.d(BT,">>> Device wants toast for breakfast.  YAY TOAST!");
					Toast.makeText(this, deviceName+" connected!", Toast.LENGTH_LONG).show();
				}
				
				// Check for status bar notification NOT SUPPORTED
				if(getBooleanProperty("pConnectNotificationEnable")) {
					// Because status bar notification are a pain in my ass, we're going to do all the work here
					
					int notifyIcon = R.drawable.icon;
					CharSequence tickerText = deviceName+" connected";
					long when = System.currentTimeMillis();
					notification = new Notification(notifyIcon, tickerText, when);
					notification.flags |= Notification.FLAG_AUTO_CANCEL;
//					Log.d(BT,">>> Created notification object");
					Context context = getApplicationContext();
					CharSequence contentTitle = "Bluetooth Notify";
					CharSequence contentText = deviceName+" connected";
					Intent notificationIntent = new Intent(this, BTNotifyService.class);
					notificationIntent.putExtra("notificationId", BTNOTIFY_NOTIFICATION);
					
					PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
					notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
//					Log.d(BT,">>> Updated notification object");
				} else
					
				// We don't have status bar notification - but need to create new empty notification object anyway.
				{
					notification = new Notification();
				}
				
				// Connect Ringtone Notify
				if(getBooleanProperty("pConnectRingtoneEnable")) {
//					Log.d(BT,">>> Device wants notification on connect.  Obliging.");
					doNotificationTone(getStringProperty("pConnectRingtone"));
				}
				
				// Connect LED Notify
				if(getBooleanProperty("pConnectLEDEnable")) {
					ledEnable = true;
					ledColor   = getStringProperty("pConnectLEDColor");
//					Log.d(BT,">>> Device wants LED Flash on connect.  I can do that!");
					addLEDNotification(ledColor);
				}
				// Connect Vibrate Notify
				if(getBooleanProperty("pConnectVibrateEnable")) {
//					Log.d(BT,">>> Device wants to pleasure you.  Bring on the buzz!");
					addVibrateNotification();
				}
			
				// Throw notification
				doNotification(ledEnable,ledColor);
			} else 
			
			// Check for disconnect notification enabled
			// Check to see if connect notification is enabled
			if(getBooleanProperty("pDisconnectEnable") == true && stateChange.equals(ACTION_ACL_DISCONNECTED)) {

				// Get the device name if we need to throw it later
				Boolean notificationEnable = getBooleanProperty("pDisconnectNotificationEnable");
				Boolean toastEnable		   = getBooleanProperty("pDisconnectToastEnable");
				
				if(notificationEnable == true || toastEnable == true) {
					BluetoothDevice btd = BTAdapter.getRemoteDevice(deviceAddress);
					deviceName = btd.getName();
				}

				// Connect Toast Notify
				if(getBooleanProperty("pDisconnectToastEnable")) {
//					Log.d(BT,">>> Device wants toast for breakfast.  YAY TOAST!");
					Toast.makeText(this, deviceName+" disconnected!", Toast.LENGTH_LONG).show();
				}
				
				// Check for status bar notification 
				if(getBooleanProperty("pDisconnectNotificationEnable")) {
					// Because status bar notification are a pain in my ass, we're going to do all the work here
					
					int notifyIcon = R.drawable.icon;
					CharSequence tickerText = deviceName+" disconnected";
					long when = System.currentTimeMillis();
					notification = new Notification(notifyIcon, tickerText, when);
					notification.flags |= Notification.FLAG_AUTO_CANCEL;
//					Log.d(BT,">>> Created notification object");
					Context context = getApplicationContext();
					CharSequence contentTitle = "Bluetooth Notify";
					CharSequence contentText = deviceName+" disconnected";
					Intent notificationIntent = new Intent(this, BTNotifyService.class);
					notificationIntent.putExtra("notificationId", BTNOTIFY_NOTIFICATION);
					
					PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
					notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
//					Log.d(BT,">>> Updated notification object");
				} else
					
				// We don't have status bar notification - but need to create new empty notification object anyway.
				{
					notification = new Notification();
				}
				
				// Ringtone Notify
				if(getBooleanProperty("pDisconnectRingtoneEnable")) {
//					Log.d(BT,">>> Device wants notification on disconnect.  Obliging.");
					doNotificationTone(getStringProperty("pDisconnectRingtone"));
				}
				
				// Connect LED Notify
				if(getBooleanProperty("pDisconnectLEDEnable")) {
					ledEnable = true;
					ledColor   = getStringProperty("pDisconnectLEDColor");
//					Log.d(BT,">>> Device wants LED Flash on disconnect.  I can do that!");
					addLEDNotification(ledColor);
				}
				// Connect Vibrate Notify
				if(getBooleanProperty("pDisconnectVibrateEnable")) {
//					Log.d(BT,">>> Device wants to pleasure you.  Bring on the buzz!");
					addVibrateNotification();
				}
			
				// Throw notification
				doNotification(ledEnable,ledColor);
			}

		} else {
			Log.d(BT,">>> This device is NOT enabled");
		}

	}

	protected void shutdownOnConflict() {
        // We need to exit the app if the other version (free/paid) is installed.
		Log.d(BT,">>> Service shutdown: Multiple versions installed.");
        stopSelf();
	}

	public class BTNotifyBinder extends Binder {
		BTNotifyService getService() {
			return BTNotifyService.this;
		}
	}
	
	private final Handler mHandler = new Handler();
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String mAction = intent.getAction();
			Bundle mExtra = intent.getExtras();
			
			if(mAction.equals(ACTION_ACL_CONNECTED) || mAction.equals(ACTION_ACL_DISCONNECTED)) {
				String notifyConnectedDevice = BTAdapter.getRemoteDevice(mExtra.get(EXTRA_DEVICE).toString()).getAddress();
				Log.d(BT,">>> State changed for device ("+notifyConnectedDevice+")");
				notifyDeviceStateChange(notifyConnectedDevice,mAction);
			}
		}
	};
	
	@Override
	public void onCreate() {
		// Create
		super.onCreate();
		
		AppDetector detect = new AppDetector();
		
		if(detect.isAppInstalledFromService(this, "com.deadbeat.bluetoothalert") == true) {
			shutdownOnConflict();
		}
		
		Log.d(BT,">>> Service Created");

		IntentFilter intentToReceiveFilter = new IntentFilter();
		intentToReceiveFilter.addAction(ACTION_ACL_CONNECTED);
		intentToReceiveFilter.addAction(ACTION_ACL_DISCONNECTED);
		this.registerReceiver(mIntentReceiver, intentToReceiveFilter, null, mHandler);
		Log.d(BT,">>> Bluetooth State Receiver registered");
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(BT,">>> Bluetooth Notify Service starting up");
		// Continue running until explicitly stopped - set sticky
		// NOTE: You REALLY can't kill the process with this flag - Think hard about it.
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		Log.d(BT,">>> Service Destroyed :(");
	}
	
	@Override
	public IBinder onBind(Intent inetent) {
		Log.d(BT,">>> Boobs");
		return null;
	}
}
