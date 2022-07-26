/*
 * ------------------------------------------------------------
 * Copyright (C)  2017  -  GEMALTO DEVELOPMENT - R&D
 * ------------------------------------------------------------
 *
 *   GEMALTO MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF * THE SOFTWARE,
 *   EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED * TO THE IMPLIED WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. GEMALTO SHALL NOT
 *   BE * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, * MODIFYING OR
 *   DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. * * THIS SOFTWARE IS NOT DESIGNED OR INTENDED
 *   FOR USE OR RESALE AS ON-LINE * CONTROL EQUIPMENT IN HAZARDOUS ENVIRONMENTS REQUIRING
 *   FAIL-SAFE * PERFORMANCE, SUCH AS IN THE OPERATION OF NUCLEAR FACILITIES, AIRCRAFT *
 *   NAVIGATION OR COMMUNICATION SYSTEMS, AIR TRAFFIC CONTROL, DIRECT LIFE * SUPPORT MACHINES, OR
 *   WEAPONS SYSTEMS, IN WHICH THE FAILURE OF THE * SOFTWARE COULD LEAD DIRECTLY TO DEATH,
 *   PERSONAL INJURY, OR SEVERE * PHYSICAL OR ENVIRONMENTAL DAMAGE ("HIGH RISK ACTIVITIES").
 *   GEMALTO * SPECIFICALLY DISCLAIMS ANY EXPRESS OR IMPLIED WARRANTY OF FITNESS FOR * HIGH RISK
 *   ACTIVITIES. * *
 */
package com.reactnativethalespaysdkwrapper.payment.contactless.pfp;

import android.os.Bundle;
import android.util.Log;

import com.gemalto.mfs.mwsdk.payment.AbstractHCEService;
import com.gemalto.mfs.mwsdk.payment.PaymentServiceListener;
import com.reactnativethalespaysdkwrapper.PayModule;
import com.reactnativethalespaysdkwrapper.app.AppBuildConfigurations;
import com.reactnativethalespaysdkwrapper.app.AppConstants;
import com.reactnativethalespaysdkwrapper.util.AppLogger;
import com.reactnativethalespaysdkwrapper.util.SharedPreferenceUtils;

import static com.reactnativethalespaysdkwrapper.util.Util.bytesToHex;

/**
 * HCE service
 */
public class PFPHCEService extends AbstractHCEService  {

    public static int apduCounter = 0;

    private static final String TAG = PFPHCEService.class.getSimpleName();

    @Override
    public void onCreate() {

        super.onCreate();
    }

    @Override
    public PaymentServiceListener setupListener() {
      return PayModule.getInstance();
    }

    @Override
    public boolean setupCardActivation() {
        //If POS/Plugin (e.g Fidelity) request to change default card, we need to do it here
        return false;
    }

    @Override
    public void setupPluginRegistration() {
        //If there is plugin to be registered

    }

    @Override
    public byte[] processCommandApdu(byte[] bytes, Bundle bundle) {
        apduCounter++;
        if (apduCounter == 1) {
            AppLogger.d(AppConstants.APP_TAG, "T0_Activity " + AppConstants.ENDED);
            AppLogger.d(AppConstants.APP_TAG, "T1_Activity " + AppConstants.STARTED);
            //AppLogger.d(AppConstants.APP_TAG, "T1a_Activity " + AppConstants.STARTED);
        }else{
            AppLogger.i(AppConstants.APP_TAG, "POSResponseTimeForApdu" + apduCounter + " " + AppConstants.ENDED);
        }
        int counterBreakForFirstTap=0;
        String benchmark = SharedPreferenceUtils.getBenchmark(getApplicationContext());
        if (benchmark.equals(AppConstants.Schemes.VISA.name())) {
            counterBreakForFirstTap=4;
            Log.d(TAG,"Benchmark Flavor VISA");
        }else{
            counterBreakForFirstTap=8;
            Log.d(TAG,"Benchmark Flavor MasterCard");
        }
        if (apduCounter == counterBreakForFirstTap) {
            Log.d(TAG, "counterBreakForFirstTap :" + counterBreakForFirstTap);
            AppLogger.d(AppConstants.APP_TAG, "T4_Activity " + AppConstants.ENDED);
            AppLogger.d(AppConstants.APP_TAG, "T5_Activity " + AppConstants.STARTED);
            AppLogger.d(AppConstants.APP_TAG, "T5a_Activity " + AppConstants.STARTED);
        }
        AppLogger.i(AppConstants.APP_TAG, "Apdu" + apduCounter + " " + AppConstants.STARTED);


        AppLogger.i(TAG, "APDU Received from POS::" +bytesToHex(bytes));

        if (bundle == null) bundle = new Bundle();
        // Configure PFP module to be used
        bundle.putBoolean(AppConstants.PFP_ENABLED_FLAG, true);
        if (AppBuildConfigurations.IS_ALLOW_PAYMENT_WHEN_SCREEN_OFF) {
            if (bundle == null) bundle = new Bundle();
            // Configure PFP module to be used
            bundle.putBoolean(AppConstants.ALLOW_PAYMENT_WHEN_SCREEN_OFF_FLAG, true);
        }
        byte[] responseAPDU = super.processCommandApdu(bytes, bundle);
        final String apdu = bytesToHex(bytes);
        if (apdu.toUpperCase().startsWith("80A8")
                && null != responseAPDU
                && bytesToHex(responseAPDU)
                .equalsIgnoreCase("6986")) {
            AppLogger.i(AppConstants.APP_TAG, "GPO command processed, returning SW 6986 ==> End of first TAP");
        }

        if (null != responseAPDU) {
            AppLogger.i(TAG, "APDU sent to POS::" + bytesToHex(responseAPDU));
        } else {
            AppLogger.i(TAG, "APDU sent to POS:: null");
        }
        AppLogger.i(AppConstants.APP_TAG, "Apdu" + apduCounter + " " + AppConstants.ENDED);

        int counterBreakForSecondTap=0;
        if( AppConstants.BENCHMARKING_SCHEME_SELECTED== AppConstants.Schemes.VISA){
            counterBreakForSecondTap=7;
        }else{
            counterBreakForSecondTap=14;
        }

        if (apduCounter == counterBreakForSecondTap){
            AppLogger.d(AppConstants.APP_TAG, "T5a_Activity " + AppConstants.ENDED);
        }else{
            int nextapduCounter=apduCounter+1;
            AppLogger.i(AppConstants.APP_TAG, "POSResponseTimeForApdu" + nextapduCounter + " " + AppConstants.STARTED);
        }
        return responseAPDU;
    }


}
