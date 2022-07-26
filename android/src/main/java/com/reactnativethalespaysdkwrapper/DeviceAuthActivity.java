package com.reactnativethalespaysdkwrapper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.WindowManager;

public class DeviceAuthActivity extends AppCompatActivity {

  private PowerManager.WakeLock mWakeLock;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_device_auth);

    unlockAndWake();
  }


  public void unlockAndWake() {


    //Return if mWakelock is non null as it's already initialized and acquired
    if (mWakeLock != null && mWakeLock.isHeld())
      return;

    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

    if (pm != null && !pm.isInteractive()) {
      mWakeLock = pm.newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        | PowerManager.ACQUIRE_CAUSES_WAKEUP, getResources().getString(R.string.app_name) + "CA:wakelock");
      if (mWakeLock != null)
        mWakeLock.acquire();
    }

    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
      | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
      | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
      | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
  }
}
