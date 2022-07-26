package com.reactnativethalespaysdkwrapper.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;


import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardManager;
import com.gemalto.mfs.mwsdk.mobilegateway.MGConnectionConfiguration;
import com.gemalto.mfs.mwsdk.mobilegateway.MGSDKConfigurationState;
import com.gemalto.mfs.mwsdk.mobilegateway.MGTransactionHistoryConfiguration;
import com.gemalto.mfs.mwsdk.mobilegateway.MGWalletConfiguration;
import com.gemalto.mfs.mwsdk.mobilegateway.MobileGatewayManager;
import com.gemalto.mfs.mwsdk.mobilegateway.exception.MGConfigurationException;
import com.gemalto.mfs.mwsdk.mobilegateway.exception.MGStorageConfigurationException;
import com.gemalto.mfs.mwsdk.payment.experience.PaymentExperience;
import com.gemalto.mfs.mwsdk.payment.experience.PaymentExperienceSettings;
import com.gemalto.mfs.mwsdk.payment.sdkconfig.SDKDataController;
import com.gemalto.mfs.mwsdk.payment.sdkconfig.SDKInitializer;
import com.gemalto.mfs.mwsdk.provisioning.ProvisioningServiceManager;
import com.gemalto.mfs.mwsdk.provisioning.listener.PushServiceListener;
import com.gemalto.mfs.mwsdk.provisioning.listener.WalletSecureEnrollmentListener;
import com.gemalto.mfs.mwsdk.provisioning.model.EnrollmentStatus;
import com.gemalto.mfs.mwsdk.provisioning.model.ProvisioningServiceError;
import com.gemalto.mfs.mwsdk.provisioning.model.ProvisioningServiceMessage;
import com.gemalto.mfs.mwsdk.provisioning.model.WalletSecureEnrollmentError;
import com.gemalto.mfs.mwsdk.provisioning.model.WalletSecureEnrollmentState;
import com.gemalto.mfs.mwsdk.provisioning.sdkconfig.ProvisioningBusinessService;
import com.gemalto.mfs.mwsdk.provisioning.sdkconfig.WalletSecureEnrollmentBusinessService;
import com.gemalto.mfs.mwsdk.sdkconfig.AndroidContextResolver;
import com.gemalto.mfs.mwsdk.sdkconfig.SDKControllerListener;
import com.gemalto.mfs.mwsdk.sdkconfig.SDKError;
import com.gemalto.mfs.mwsdk.sdkconfig.SDKInitializeErrorCode;
import com.gemalto.mfs.mwsdk.sdkconfig.SDKSetupProgressState;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.reactnativethalespaysdkwrapper.R;
import com.reactnativethalespaysdkwrapper.app.AppBuildConfigurations;
import com.reactnativethalespaysdkwrapper.app.AppConstants;
import com.reactnativethalespaysdkwrapper.payment.contactless.pfp.PFPHelper;

import java.io.IOException;

import static com.gemalto.mfs.mwsdk.mobilegateway.MGSDKConfigurationState.NOT_CONFIGURED;


public class SDKHelper {

    private static final String TAG = SDKHelper.class.getSimpleName();

    public static void initMGSDKCall(final Context context) {
        AppLogger.d(AppConstants.APP_TAG, "MG SDK init " + AppConstants.STARTED);
        //init MG SDK
        try {
            SDKHelper.initMGSDK(context);
        } catch (MGConfigurationException e) {
            e.printStackTrace();
        }
        AppLogger.d(AppConstants.APP_TAG, "MG SDK init " + AppConstants.ENDED);
    }

  public static void forceReplenish(String digitalCardID) {
    final String tokenizedCardId = DigitalizedCardManager.getTokenizedCardId(digitalCardID);
    if (tokenizedCardId != null && !tokenizedCardId.isEmpty()) {
      TokenReplenishmentRequestor.forceReplenish(tokenizedCardId);
    }
  }



  public interface InitCPSSDKCallback {
        void doAction();
    }

    public static void initFirebase(final Context context) {
        AppLogger.d(AppConstants.APP_TAG, "FirebaseApp.initializeApp " + AppConstants.STARTED);
        FirebaseApp.initializeApp(context);
        AppLogger.d(AppConstants.APP_TAG, "FirebaseApp.initializeApp " + AppConstants.ENDED);

    }

    public static void initMGSDK(Context context) throws MGConfigurationException {
        MGSDKConfigurationState configurationState = MobileGatewayManager.INSTANCE.getConfigurationState();
        if (configurationState != NOT_CONFIGURED) {
            return;
        }

        //Configure MG configuration
        MGConnectionConfiguration connectionConfiguration = new MGConnectionConfiguration
                .Builder()
                .setConnectionParameters(EnvConfigs.MG_CONNECTION_URL_USED,
                        Constants.MG_CONNECTION_TIMEOUT,
                        Constants.MG_CONNECTION_READ_TIMEOUT)
                .setRetryParameters(Constants.MG_CONNECTION_RETRY_COUNT,
                        Constants.MG_CONNECTION_RETRY_INTERVAL)
                .build();

        //Configure wallet configuration
        MGWalletConfiguration walletConfiguration = new MGWalletConfiguration
                .Builder()
                .setWalletParameters(EnvConfigs.WALLET_PROVIDER_ID_USED)
                .setNotification(
                        NotificationUtil.getNotification(context,
                                context.getString(R.string.mg_notification_message),
                                context.getString(R.string.mg_notification_channel_id)))
                .build();

        //Configure Transaction History
        MGTransactionHistoryConfiguration transactionConfiguration = new MGTransactionHistoryConfiguration
                .Builder()
                .setConnectionParameters(
                        EnvConfigs.MG_TRANSACTION_HISTORY_CONNECTION_URL_USED)
                .build();
        try {
            if (configurationState == NOT_CONFIGURED) {
                MobileGatewayManager.INSTANCE.configure(context, connectionConfiguration
                        , walletConfiguration, transactionConfiguration);
            }
        } catch (MGStorageConfigurationException e) {
            AppLogger.e("MG Config", "", e);
        } catch (MGConfigurationException exception) {
            AppLogger.e("MG Config", "MG MGConfigurationException " + exception.getLocalizedMessage(), exception);
        }

        AppLogger.d("MG Config", "MG Configuration initialised");
    }

    public static void initCPSSDK(final Context context, final InitCPSSDKCallback initCPSSDKCallback, final boolean isUIUpdateNeeded) {


        SDKControllerListener sdkControllerListener = createSDKControllerListenerObject(context,
                initCPSSDKCallback,
                isUIUpdateNeeded,
                true);

        AppLogger.d(AppConstants.APP_TAG, "Initialization " + AppConstants.STARTED);
        SDKInitializer.INSTANCE.initialize(context, sdkControllerListener, NotificationUtil.getNotification(context, context.getString(R.string.cps_notification_message),context.getString(R.string.cps_notification_channel_id)));
    }

    private static void retryInitialization(Context context, InitCPSSDKCallback initCPSSDKCallback, boolean isUIUpdateNeeded) {
        SDKControllerListener sdkControllerListener = createSDKControllerListenerObject(context, initCPSSDKCallback, isUIUpdateNeeded, false);
        AppLogger.d(AppConstants.APP_TAG, "Retry Initialization " + AppConstants.STARTED);
        SDKInitializer.INSTANCE.initialize(context, sdkControllerListener,
                NotificationUtil.getNotification(context,
                        context.getString(R.string.cps_notification_message),
                        context.getString(R.string.cps_notification_channel_id)));
    }

    private static SDKControllerListener createSDKControllerListenerObject(final Context context, final InitCPSSDKCallback initCPSSDKCallback, final boolean isUIUpdateNeeded, final boolean isRetry) {
        return new SDKControllerListener() {
            @Override
            public void onError(SDKError<SDKInitializeErrorCode> initializeError) {

                if (initializeError.getErrorCode() == SDKInitializeErrorCode.SDK_INITIALIZED) {
                    AppLogger.e("Service", "SDK already initialized ");
                    // There is Snackbar used on the MainActivity to indicate the init result
                    //Toast.makeText(context.getApplicationContext(), "Initialization completed", Toast.LENGTH_LONG).show();
                    broadcastInitComplete(context, isUIUpdateNeeded);
                    //This is for registering Pre-Entry Receiver and trigger wallet Secure Enrollment flow and must be done only after initialization is completed
                    if (initCPSSDKCallback != null) {
                        initCPSSDKCallback.doAction();
                    }

                } else if (SDKInitializeErrorCode.SDK_INITIALIZING_IN_PROGRESS == initializeError.getErrorCode()) {
                    //Ignore as initialization is happening elsewhere
                } else if (SDKInitializeErrorCode.INTERNAL_COMPONENT_ERROR == initializeError.getErrorCode() ||
                        SDKInitializeErrorCode.SDK_INIT_FAILED == initializeError.getErrorCode() ||
                        SDKInitializeErrorCode.STORAGE_COMPONENT_ERROR == initializeError.getErrorCode() ||
                        SDKInitializeErrorCode.INVALID_PREVIOUS_VERSION == initializeError.getErrorCode() ||
                        SDKInitializeErrorCode.ASM_INIT_ERROR == initializeError.getErrorCode() ||
                        SDKInitializeErrorCode.ASM_MIGRATION_ERROR == initializeError.getErrorCode()) {
                    AppLogger.e("Service", "Initialization failed" + initializeError.getErrorCode().name());
                    AppLogger.e("Service", "Initialization failed" + initializeError.getErrorMessage());

                    if (isRetry) {
                        retryInitialization(context, initCPSSDKCallback, isUIUpdateNeeded);
                    } else {
                        //This is already retry....No need to retry again.... Ensure SDK APIs are not used after this point.
                        //In this training app, we close the application.

                        // broadcastInitFailed(context);
                        try {
                            SDKDataController.INSTANCE.wipeAll(context);
                            new Handler().postDelayed(() -> retryInitialization(context, initCPSSDKCallback, isUIUpdateNeeded), 2500);
                        } catch (Exception e) {
                            AppLogger.e(TAG, e.getMessage());
                        }
                    }

                } else {
                    AppLogger.e("Service", "Initialization failed");

                    // There is Snackbar used on the MainActivity to indicate the init result
                    // Toast.makeText(context.getApplicationContext(), "Initialization failed", Toast.LENGTH_LONG).show();
                    broadcastInitFailed(context);
                }
            }

            @Override
            public void onSetupProgress(SDKSetupProgressState sdkSetupProgressState, String s) {
                AppLogger.d("Service", "onSetupProgress completed");
            }

            @Override
            public void onSetupComplete() {

                AppLogger.d(AppConstants.APP_TAG, "Initialization " + AppConstants.ENDED);
                // There is Snackbar used on the MainActivity to indicate the init result
                // Toast.makeText(context.getApplicationContext(), "Initialization completed", Toast.LENGTH_LONG).show();
                broadcastInitComplete(context, isUIUpdateNeeded);
                //This is for registering Pre-Entry Receiver and must be done only after initialization is completed
                if (initCPSSDKCallback != null) {
                    initCPSSDKCallback.doAction();
                }

            }
        };
    }

    public static void performWalletSecureEnrollmentFlow(final Context context) {

        AppLogger.d(AppConstants.APP_TAG, "performWalletSecureEnrollmentFlow" + AppConstants.STARTED);
        WalletSecureEnrollmentBusinessService wseService = ProvisioningServiceManager.getWalletSecureEnrollmentBusinessService();

        //get State
        WalletSecureEnrollmentState state = wseService.getState();
        AppLogger.d(AppConstants.APP_TAG, "performWalletSecureEnrollmentFlow state" + state.name());
        if (state == WalletSecureEnrollmentState.WSE_REQUIRED) {
            //call renewal
            wseService.startWalletSecureEnrollment(new WalletSecureEnrollmentListener() {
                @Override
                public void onProgressUpdate(WalletSecureEnrollmentState wseState) {
                    AppLogger.d(AppConstants.APP_TAG, "performWalletSecureEnrollmentFlow - onProgressUpdate: " + wseState);

                   /// Toast.makeText(context, "performWalletSecureEnrollmentFlow - " + wseState, Toast.LENGTH_SHORT).show();

                    if (wseState == WalletSecureEnrollmentState.WSE_COMPLETED) {
                        //completed ready to go to cardlist

                       /// Toast.makeText(context, "performWalletSecureEnrollmentFlow - WSE_COMPLETED", Toast.LENGTH_SHORT).show();
                        broadcastInitComplete(context, true);
                    }
                }

                @Override
                public void onError(WalletSecureEnrollmentError wbDynamicKeyRenewalServiceError) {
                    AppLogger.d(AppConstants.APP_TAG, "performWalletSecureEnrollmentFlow onError cps error code: " + wbDynamicKeyRenewalServiceError.getCpsErrorCode());
                    AppLogger.d(AppConstants.APP_TAG, "performWalletSecureEnrollmentFlow onError cps error code: " + wbDynamicKeyRenewalServiceError.getSdkErrorCode());
                    AppLogger.d(AppConstants.APP_TAG, "performWalletSecureEnrollmentFlow onError message: " + wbDynamicKeyRenewalServiceError.getErrorMessage());

                    //display to user, cant enroll
                   // Toast.makeText(context, "performWalletSecureEnrollmentFlow - onError: " + wbDynamicKeyRenewalServiceError.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    broadcastInitComplete(context, true);
                }
            });
        } else if (state == WalletSecureEnrollmentState.WSE_COMPLETED || state == WalletSecureEnrollmentState.WSE_NOT_REQUIRED) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                   // Toast.makeText(context, "performWalletSecureEnrollmentFlow: " + state, Toast.LENGTH_SHORT).show();
                    broadcastInitComplete(context, true);
                }
            });
        }
    }

  public static MGSDKConfigurationState getMgSdkState() {
    try {
      return MobileGatewayManager.INSTANCE.getConfigurationState();
    } catch (final MGStorageConfigurationException exception) {
      AppLogger.e(TAG, exception.getLocalizedMessage(), exception.getCauseException());
    }
    return MGSDKConfigurationState.NOT_CONFIGURED;
  }

    public static void updateFirebaseToken(Context context){

        new Thread(() ->{
    try {
      if( getMgSdkState() == MGSDKConfigurationState.CONFIGURED && ProvisioningServiceManager.getEnrollingBusinessService().isEnrolled() == EnrollmentStatus.ENROLLMENT_COMPLETE ) {
        String cpsFirebaseToken = FirebaseInstanceId.getInstance().getToken(Constants.CPS_SENDER_ID, "FCM");
        AppLogger.d(TAG, "updateFirebase");
        if (cpsFirebaseToken == null) {
          AppLogger.d(PFPHelper.class.getSimpleName(), "Firebase token is null");
        } else {
          if (SDKHelper.isCPSFirebaseTokenChanged(context, cpsFirebaseToken)) {
            AppLogger.d(PFPHelper.class.getSimpleName(), "Firebase token is " + cpsFirebaseToken);
            ProvisioningBusinessService provisioningBusinessService = ProvisioningServiceManager.getProvisioningBusinessService();
            provisioningBusinessService.updatePushToken(cpsFirebaseToken, new PushServiceListener() {
              @Override
              public void onError(ProvisioningServiceError provisioningServiceError) {
                AppLogger.e(TAG, "updatePushToken Failed: " + provisioningServiceError.getSdkErrorCode() + " : " + provisioningServiceError.getErrorMessage());
              }

              @Override
              public void onUnsupportedPushContent(Bundle bundle) {
                AppLogger.e(TAG, "updatePushToken Failed: onUnsupportedPushContent");
              }

              @Override
              public void onServerMessage(String s, ProvisioningServiceMessage provisioningServiceMessage) {
                AppLogger.i(TAG, "updatePushToken : onServerMessage");
                AppLogger.i(TAG, "updatePushToken : onServerMessage" + s);
                AppLogger.i(TAG, "updatePushToken : onServerMessage" + provisioningServiceMessage.getMsgText());
                /// Toast.makeText(context,provisioningServiceMessage.getMsgCode()+"_"+provisioningServiceMessage.getMsgText(), Toast.LENGTH_LONG).show();
              }

              @Override
              public void onComplete() {
                String toastMessage = AndroidContextResolver.getApplicationContext().getString(R.string.firebase_token_update) + ":" + cpsFirebaseToken;
                AppLogger.d(TAG, toastMessage);
//                        Toast.makeText(AndroidContextResolver.getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                SharedPreferenceUtils.saveCPSFirebaseToken(context, cpsFirebaseToken);
              }
            });
          } else {
            AppLogger.d(PFPHelper.class.getSimpleName(), "Firebase token is same as previous, no need to update::::" + cpsFirebaseToken);
          }
        }
      }
    }catch (IOException e ){
        e.printStackTrace();
    } catch (Exception e) {
        e.printStackTrace();
    }
     }).start();
    }


    /**********************************************************/
    /*                Private helpers                         */

    /**********************************************************/
    private static void broadcastInitFailed(Context context) {
        Intent sdkInitDone = new Intent(AppConstants.ACTION_INIT_DONE);
        sdkInitDone.putExtra(AppConstants.INIT_FAILED_EXTRA, true);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(sdkInitDone);
    }

    private static void broadcastInitComplete(Context context, boolean isUIUpdateNeeded) {
        Intent sdkInitDone = new Intent(AppConstants.ACTION_INIT_DONE);
        sdkInitDone.putExtra(AppConstants.INIT_UI_UPDATE_NEEDED, isUIUpdateNeeded);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(sdkInitDone);
    }
    private static boolean isCPSFirebaseTokenChanged(Context context, String newCPSFirebaseToken) {
        String existingCPSFirebaseToken= SharedPreferenceUtils.getCPSFirebaseToken(context);
        if(existingCPSFirebaseToken==null || existingCPSFirebaseToken.isEmpty()){
            return true;
        }
        if(existingCPSFirebaseToken.equalsIgnoreCase(newCPSFirebaseToken)){
            return false;
        }
        return true;
    }


  public static void InitSdk(Context context) {
    SDKHelper.initFirebase(context);
    SDKHelper.InitCPSSDKCallback initCPSSDKCallback = new SDKHelper.InitCPSSDKCallback() {
      @Override
      public void doAction() {
        //init MG SDK
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
          @Override
          public void run() {

            SDKHelper.updateFirebaseToken(context);
            SDKHelper.initMGSDKCall(context);
            SDKHelper.performWalletSecureEnrollmentFlow(context);
            FirebaseInstanceId.getInstance().getToken();
            PaymentExperienceSettings.setPaymentExperience(context, PaymentExperience.TWO_TAP_ALWAYS);
          }
        }, AppBuildConfigurations.INIT_MG_SDK_DELAY);
      }
    };
    SDKHelper.initCPSSDK(context, initCPSSDKCallback, true);
  }

}
