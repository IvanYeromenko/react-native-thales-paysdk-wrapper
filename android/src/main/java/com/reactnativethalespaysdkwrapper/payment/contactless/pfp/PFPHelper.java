package com.reactnativethalespaysdkwrapper.payment.contactless.pfp;

import android.content.Context;
import android.os.Process;

import com.reactnativethalespaysdkwrapper.app.AppConstants;
import com.reactnativethalespaysdkwrapper.util.AppLogger;
import com.reactnativethalespaysdkwrapper.util.SDKHelper;


public enum PFPHelper {
    //  ## PFP Based Payment flow ####
    //  Major changes are:
    //  * No foreground service => first tap is handled by the PFP
    //  * There appears prompt for authentication WITH transaction info
    //  * User can change card for the second tap AFTER the authentication

    INSTANCE;

    private static final String TAG = PFPHelper.class.getSimpleName();

    public void initSDKs(final Context context, final boolean isUIUpdateNeeded) {
        AppLogger.d(AppConstants.APP_TAG, "initSDKsForPFP");
        SDKHelper.InitCPSSDKCallback initCPSSDKCallback = new SDKHelper.InitCPSSDKCallback() {
            @Override
            public void doAction() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
                        //Firebase API has limitation when there are multiple sender ID, the onNewToken is triggered only for default SENDER_ID.
                        // So it is prudent to check for updatePushToken regularly after SDK initialization as well.
                        //And it is prudent to check for updatePushToken just before card enrollment process begin as well.
                        SDKHelper.updateFirebaseToken(context);
                        //init MG SDK
                        SDKHelper.initMGSDKCall(context);

                        //perform Wallet Secure Enrollment
                        SDKHelper.performWalletSecureEnrollmentFlow(context);
                    }
                }).start();
            }
        };
        SDKHelper.initCPSSDK(context, initCPSSDKCallback,isUIUpdateNeeded);

    }

}


