package com.reactnativethalespaysdkwrapper;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.gemalto.mfs.mwsdk.cdcvm.BiometricsSupport;
import com.gemalto.mfs.mwsdk.cdcvm.DeviceCVMEligibilityChecker;
import com.gemalto.mfs.mwsdk.cdcvm.DeviceCVMEligibilityResult;
import com.gemalto.mfs.mwsdk.cdcvm.DeviceKeyguardSupport;
import com.gemalto.mfs.mwsdk.dcm.DigitalizedCard;
import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardDetails;
import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardErrorCodes;
import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardManager;
import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardState;
import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardStatus;
import com.gemalto.mfs.mwsdk.dcm.PaymentType;
import com.gemalto.mfs.mwsdk.dcm.cdcvm.DeviceCVMManager;
import com.gemalto.mfs.mwsdk.exception.DeviceCVMException;
import com.gemalto.mfs.mwsdk.mobilegateway.MGCardEnrollmentService;
import com.gemalto.mfs.mwsdk.mobilegateway.MGConfigurationChangeReceiver;
import com.gemalto.mfs.mwsdk.mobilegateway.MGTransactionRecord;
import com.gemalto.mfs.mwsdk.mobilegateway.MobileGatewayError;
import com.gemalto.mfs.mwsdk.mobilegateway.MobileGatewayManager;
import com.gemalto.mfs.mwsdk.mobilegateway.enrollment.CardArtType;
import com.gemalto.mfs.mwsdk.mobilegateway.enrollment.CardBitmap;
import com.gemalto.mfs.mwsdk.mobilegateway.enrollment.IDVMethodSelector;
import com.gemalto.mfs.mwsdk.mobilegateway.enrollment.InputMethod;
import com.gemalto.mfs.mwsdk.mobilegateway.enrollment.IssuerData;
import com.gemalto.mfs.mwsdk.mobilegateway.enrollment.PendingCardActivation;
import com.gemalto.mfs.mwsdk.mobilegateway.enrollment.TermsAndConditions;
import com.gemalto.mfs.mwsdk.mobilegateway.exception.NoSuchCardException;
import com.gemalto.mfs.mwsdk.mobilegateway.listener.CardEligibilityListener;
import com.gemalto.mfs.mwsdk.mobilegateway.listener.MGCardLifecycleEventListener;
import com.gemalto.mfs.mwsdk.mobilegateway.listener.MGDigitizationListener;
import com.gemalto.mfs.mwsdk.mobilegateway.listener.TransactionHistoryListener;
import com.gemalto.mfs.mwsdk.mobilegateway.utils.MGAbstractAsyncHandler;
import com.gemalto.mfs.mwsdk.mobilegateway.utils.MGAsyncResult;
import com.gemalto.mfs.mwsdk.mobilegateway.utils.MGCardInfoEncryptor;
import com.gemalto.mfs.mwsdk.payment.CHVerificationMethod;
import com.gemalto.mfs.mwsdk.payment.cdcvm.DeviceCVMPreEntryReceiver;
import com.gemalto.mfs.mwsdk.provisioning.ProvisioningServiceManager;
import com.gemalto.mfs.mwsdk.provisioning.exception.ExistingRetrySessionException;
import com.gemalto.mfs.mwsdk.provisioning.exception.NoSessionException;
import com.gemalto.mfs.mwsdk.provisioning.listener.AccessTokenListener;
import com.gemalto.mfs.mwsdk.provisioning.listener.EnrollingServiceListener;
import com.gemalto.mfs.mwsdk.provisioning.listener.PushServiceListener;
import com.gemalto.mfs.mwsdk.provisioning.model.EnrollmentStatus;
import com.gemalto.mfs.mwsdk.provisioning.model.GetAccessTokenMode;
import com.gemalto.mfs.mwsdk.provisioning.model.KnownMessageCode;
import com.gemalto.mfs.mwsdk.provisioning.model.ProvisioningServiceError;
import com.gemalto.mfs.mwsdk.provisioning.model.ProvisioningServiceErrorCodes;
import com.gemalto.mfs.mwsdk.provisioning.model.ProvisioningServiceMessage;
import com.gemalto.mfs.mwsdk.provisioning.model.WalletSecureEnrollmentState;
import com.gemalto.mfs.mwsdk.provisioning.sdkconfig.EnrollingBusinessService;
import com.gemalto.mfs.mwsdk.provisioning.sdkconfig.ProvisioningBusinessService;
import com.gemalto.mfs.mwsdk.sdkconfig.SDKController;
import com.gemalto.mfs.mwsdk.sdkconfig.SDKServiceState;
import com.gemalto.mfs.mwsdk.utils.async.AbstractAsyncHandler;
import com.gemalto.mfs.mwsdk.utils.async.AsyncResult;
import com.gemalto.mfs.mwsdk.utils.chcodeverifier.CHCodeVerifier;
import com.gemalto.mfs.mwsdk.utils.chcodeverifier.SecureCodeInputer;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.reactnativethalespaysdkwrapper.app.AppBuildConfigurations;
import com.reactnativethalespaysdkwrapper.app.AppConstants;
import com.reactnativethalespaysdkwrapper.model.DigitalCard;
import com.reactnativethalespaysdkwrapper.model.DigitalCardDetails;
import com.reactnativethalespaysdkwrapper.model.TransactionHistory;
import com.reactnativethalespaysdkwrapper.util.AppLogger;
import com.reactnativethalespaysdkwrapper.util.EnvConfigs;
import com.reactnativethalespaysdkwrapper.util.JsonToMap;
import com.reactnativethalespaysdkwrapper.util.SDKHelper;
import com.reactnativethalespaysdkwrapper.util.SharedPreferenceUtils;
import com.reactnativethalespaysdkwrapper.util.TokenReplenishmentRequestor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gemalto.mfs.mwsdk.sdkconfig.AndroidContextResolver.getApplicationContext;

@ReactModule(name = ThalesPaysdkWrapperModule.NAME)
public class ThalesPaysdkWrapperModule extends ReactContextBaseJavaModule implements PushServiceListener, LifecycleEventListener, CardEligibilityListener, MGDigitizationListener, EnrollingServiceListener {
  public static final String NAME = "ThalesPaysdkWrapper";
  private static final String TAG = "ThalesPayWrapperModule";
  private static ReactApplicationContext reactApplicationContext;
  private MGConfigurationChangeReceiver configurationChangeReceiver;
  private DeviceCVMPreEntryReceiver mPreEntryReceiver;
  private final int MAX_RETRY = 5;
  private int provisionRetryCounter = 0;
  TermsAndConditions termsAndCondition;
  private byte[] activationCode;
  public static final int STORAGE_COMPONENT_ERROR = 1012;
  public static final String STORAGE_COMPONENT_EXCEPTION_KEY = "SECURE_STORAGE_ERROR";
  public static final String CARD_ELIGIBILITY_EVENT = "CardEligibilityListener";
  public static final String MG_DIGITIZATION_EVENT = "MGDigitizationListener";
  public static final String SERVER_MESSAGE_LISTENER = "ServerMessageListener";
  private static final String ERROR = "ERROR";
  private static final  String  KNOWN_MESSAGE_CODE = "KnownMessageCode";
  private Promise mPromise;


  public ThalesPaysdkWrapperModule(ReactApplicationContext reactContext) {
    super(reactContext);
    reactApplicationContext = reactContext;
    registerBroadCasts();
  }

  private void registerBroadCasts() {
    registerMCgConfig();
    registerPreFpEntry();
    registerReceiver();
  }



  @Override
  @NonNull
  public String getName() {
    return NAME;
  }




  private void registerReceiver() {
    LocalBroadcastManager.getInstance(reactApplicationContext).registerReceiver(receiver,
      new IntentFilter(AppConstants.ACTION_START_CPS));
  }

  @ReactMethod
  public void enrollCard(String cardPan, String cardExp, String cardCvv) {
    byte[] pubKeyBytes = MGCardInfoEncryptor.parseHex(EnvConfigs.PUBLIC_KEY_USED);
    byte[] subKeyBytes = MGCardInfoEncryptor.parseHex(EnvConfigs.SUBJECT_IDENTIFIER_USED);

    byte[] panBytes = cardPan.getBytes();
    byte[] expBytes = cardExp.getBytes();
    byte[] cvvBytes = cardCvv.getBytes();
    byte[] encData = MGCardInfoEncryptor.encrypt(pubKeyBytes, subKeyBytes,
      panBytes, expBytes, cvvBytes);

    AppLogger.d(TAG, "Starting enrollment checkCardEligibility");

    MGCardEnrollmentService enrollmentService = MobileGatewayManager.INSTANCE.getCardEnrollmentService();
    //InputMethod.BANK_APP is required for GreenFlow
    enrollmentService.checkCardEligibility(encData, InputMethod.BANK_APP, "en", this, getDeviceSerial());
    AppLogger.d(TAG, "Started enrollment checkCardEligibility");
  }


  private String getDeviceSerial() {
    return Settings.Secure.getString(reactApplicationContext.getContentResolver(), Settings.Secure.ANDROID_ID);
  }

  @ReactMethod
  private void proceedDigitize(String jwtToken) {
    if (termsAndCondition == null) {
      return;
    }
    byte[] jwtTokenBytes = jwtToken.getBytes();
    MGCardEnrollmentService enrollmentService = MobileGatewayManager.INSTANCE.getCardEnrollmentService();
    enrollmentService.digitizeCard(termsAndCondition.accept(), jwtTokenBytes, this);
  }

  @ReactMethod
  private void proceedDigitizeWithoutToken() {
    if (termsAndCondition == null) {
      return;
    }
    MGCardEnrollmentService enrollmentService = MobileGatewayManager.INSTANCE.getCardEnrollmentService();
    enrollmentService.digitizeCard(termsAndCondition.accept(), null, this);
  }


  public MGConfigurationChangeReceiver getConfigurationChangeReceiver() {
    return configurationChangeReceiver;
  }

  public void setConfigurationChangeReceiver(MGConfigurationChangeReceiver configurationChangeReceiver) {
    this.configurationChangeReceiver = configurationChangeReceiver;
  }

  //set up sync between MG and CPS
  public  void registerMCgConfig() {
    setConfigurationChangeReceiver(new MGConfigurationChangeReceiver());
    LocalBroadcastManager.getInstance(reactApplicationContext).registerReceiver(getConfigurationChangeReceiver(),
      new IntentFilter("com.gemalto.mfs.action.MGConfigurationChanged"));
  }

  private void registerPreFpEntry() {
    AppLogger.d("Service", "registerPreFpEntry");
    if (mPreEntryReceiver != null) {
      LocalBroadcastManager.getInstance(reactApplicationContext).unregisterReceiver(mPreEntryReceiver);
      mPreEntryReceiver = null;
    }
    IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
    mPreEntryReceiver = new DeviceCVMPreEntryReceiver();
    mPreEntryReceiver.init();
    LocalBroadcastManager.getInstance(reactApplicationContext).registerReceiver(mPreEntryReceiver, filter);
  }


  /**********************************************************/
  /*                Push Service listener                   */

  /**********************************************************/

  @Override
  public void onError(ProvisioningServiceError provisioningServiceError) {
    showToast(provisioningServiceError.getErrorMessage());

    WritableMap params = Arguments.createMap();
    params.putString("error", provisioningServiceError.getErrorMessage());
    sendEvent(reactApplicationContext, MG_DIGITIZATION_EVENT, params);

    ProvisioningServiceErrorCodes errCode = provisioningServiceError.getSdkErrorCode();
    switch (errCode) {
      case COMMON_COMM_ERROR:
      case COMMON_NO_INTERNET:
        if (provisionRetryCounter == MAX_RETRY) {
          provisionRetryCounter = 0;
          AppLogger.e(TAG, "Error during provisioning session :" + provisioningServiceError.getErrorMessage());
        } else {
          retryProvisioning();
          provisionRetryCounter++;
        }
        break;
      default:
        provisionRetryCounter = 0;
        AppLogger.e(TAG, "Error during provisioning session :" + provisioningServiceError.getErrorMessage());
        break;
    }
  }

  @Override
  public void onUnsupportedPushContent(Bundle bundle) {
    showToast("Impossible .onUnsupportedPushContent()");
  }

  @Override
  public void onServerMessage(String s, ProvisioningServiceMessage provisioningServiceMessage) {
    String messageCode = provisioningServiceMessage.getMsgCode();
    if (messageCode == null) {
      AppLogger.e(TAG, "messageCode is null for some reason");
      return;
    }
    Log.e("ServerMessageCode", provisioningServiceMessage.getMsgCode().toString());
    switch (messageCode) {
      case KnownMessageCode.REQUEST_INSTALL_CARD: {
        AppLogger.i(TAG, "Install Card Complete");
        WritableMap params = Arguments.createMap();
        params.putString(KNOWN_MESSAGE_CODE, messageCode);
        sendEvent(reactApplicationContext, SERVER_MESSAGE_LISTENER, params);
        // 1st push notification for installing card
        }
        break;
      case KnownMessageCode.REQUEST_REPLENISH_KEYS: {
        WritableMap params = Arguments.createMap();
        params.putString(KNOWN_MESSAGE_CODE, messageCode);
        sendEvent(reactApplicationContext, SERVER_MESSAGE_LISTENER, params);
        AppLogger.i(TAG, "Replenish Card Complete");
        // 2nd push notification for installing payment keys and subsequent replenishments
      }
        break;
      case KnownMessageCode.REQUEST_RESUME_CARD: {
        AppLogger.i(TAG, "Resume Card Complete");
        WritableMap params = Arguments.createMap();
        params.putString(KNOWN_MESSAGE_CODE, messageCode);
        sendEvent(reactApplicationContext, SERVER_MESSAGE_LISTENER, params);
        // card resumed
        }
        break;
      case KnownMessageCode.REQUEST_SUSPEND_CARD: {
        AppLogger.i(TAG, "Suspend Card Complete");
        WritableMap params = Arguments.createMap();
        params.putString(KNOWN_MESSAGE_CODE, messageCode);
        sendEvent(reactApplicationContext, SERVER_MESSAGE_LISTENER, params);
        // card suspended
        }
        break;
      case KnownMessageCode.REQUEST_RENEW_CARD:
       break;
      case KnownMessageCode.REQUEST_DELETE_CARD:
       {
        AppLogger.i(TAG, "Delete Card Complete");
        WritableMap params = Arguments.createMap();
        params.putString(KNOWN_MESSAGE_CODE, messageCode);
        sendEvent(reactApplicationContext, SERVER_MESSAGE_LISTENER, params);
        //card deleted.
       }
        LocalBroadcastManager.getInstance(reactApplicationContext).sendBroadcast(new Intent(AppConstants.ACTION_RELOAD_CARDS));
        break;
      default:
        AppLogger.e(TAG, "Other events: Not handling");
    }

  }

  @Override
  public void onComplete() {

    AppLogger.i(TAG, "Completed processing message");
    //showToast("Completed processing message from Module");

  }

  /**********************************************************/
  /*             End Push Service listener                   */

  /**********************************************************/


  /**********************************************************/
  /*               Licycle listener                  */

  /**********************************************************/


  @Override
  public void onHostResume() {


  }

  @Override
  public void onHostPause() {
    // LocalBroadcastManager.getInstance(this).unregisterReceiver( f );
  }

  @Override
  public void onHostDestroy() {
    LocalBroadcastManager.getInstance(reactApplicationContext).unregisterReceiver(configurationChangeReceiver);
    LocalBroadcastManager.getInstance(reactApplicationContext).unregisterReceiver(receiver);

  }

  /**********************************************************/
  /*           End Licycle listener                   */

  /**********************************************************/

  /**********************************************************/
  /*          CardEligibilityListener                 */

  /**********************************************************/

  @Override
  public void onSuccess(TermsAndConditions termsAndConditions, IssuerData issuerData) {
    termsAndCondition = termsAndConditions;
    String terms = termsAndCondition.getText();

    WritableMap params = Arguments.createMap();
    params.putString("text", terms);
    sendEvent(reactApplicationContext, CARD_ELIGIBILITY_EVENT, params);
  }

  @Override
  public void onError(MobileGatewayError mobileGatewayError) {
    AppLogger.e(TAG, mobileGatewayError.getMessage());
    WritableMap params = Arguments.createMap();
    params.putString("error", mobileGatewayError.getMessage());
    sendEvent(reactApplicationContext, CARD_ELIGIBILITY_EVENT, params);
    showToast(mobileGatewayError.getMessage());

  }

  @ReactMethod
  public void showToast(String msg) {
   // Toast.makeText(reactApplicationContext, msg, Toast.LENGTH_SHORT).show();
//    AppLogger.d(TAG, msg);
  }


  private void sendEvent(ReactContext reactContext,
                         String eventName,
                         @Nullable WritableMap params) {
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
  }

  private void retryProvisioning() {
    AppLogger.d(TAG, "AppPushServiceListener ->  attempting to retry job ");
    ProvisioningBusinessService provBs = ProvisioningServiceManager.getProvisioningBusinessService();
    if (provBs != null) {
      try {
        provBs.retrySession(this);
      } catch (ExistingRetrySessionException e) {
        e.printStackTrace();
      } catch (NoSessionException e) {
        e.printStackTrace();
      }
    }
  }

  /**********************************************************/
  /*                MGDigitizationListener                   */

  /**********************************************************/
  @Override
  public void onCPSActivationCodeAcquired(String s, byte[] code) {

    String firebaseToken = "";
    //TODO: Trigger CPS Enrollment
    if (!TextUtils.isEmpty(SharedPreferenceUtils.getFirebaseId(reactApplicationContext))) {
      firebaseToken = SharedPreferenceUtils.getFirebaseId(reactApplicationContext);
    } else {
      firebaseToken = FirebaseInstanceId.getInstance().getToken();
    }

    if (TextUtils.isEmpty(firebaseToken)) {
      Log.i(TAG, "FireBaseToken is null");
      throw new RuntimeException("Firebase token is null ");
    }
    EnrollingBusinessService enrollingService = ProvisioningServiceManager.getEnrollingBusinessService();
    ProvisioningBusinessService provisioningBusinessService = ProvisioningServiceManager.getProvisioningBusinessService();

    this.activationCode = new byte[code.length];
    for (int i = 0; i < code.length; i++) {
      activationCode[i] = code[i];
    }

    //WalletID of MG SDK is userID of CPS SDK Enrollment process
    String userId = MobileGatewayManager.INSTANCE.getCardEnrollmentService().getWalletId();
    Log.i(TAG, "FireBaseToken is  " + firebaseToken);

    EnrollmentStatus status = enrollingService.isEnrolled();
    switch (status) {
      case ENROLLMENT_NEEDED:
        enrollingService.enroll(userId, firebaseToken, "en", this);
        break;
      case ENROLLMENT_IN_PROGRESS:
        enrollingService.continueEnrollment("en", this);
        break;
      case ENROLLMENT_COMPLETE:
        provisioningBusinessService.sendActivationCode(this);
        break;
    }


  }

  @Override
  public void onSelectIDVMethod(IDVMethodSelector idvMethodSelector) {

  }

  @Override
  public void onActivationRequired(PendingCardActivation pendingCardActivation) {

  }

  @Override
  public void onComplete(String s) {
    WritableMap params = Arguments.createMap();
    params.putString("digitalCardId", s);
    sendEvent(reactApplicationContext, MG_DIGITIZATION_EVENT, params);
    AppLogger.d(TAG, ".onComplete()");
  }



  @Override
  public void onError(String s, MobileGatewayError mobileGatewayError) {
    AppLogger.d(TAG, s  + mobileGatewayError.getMessage());
    WritableMap params = Arguments.createMap();
    params.putString("error", s + mobileGatewayError.getMessage());
    sendEvent(reactApplicationContext, MG_DIGITIZATION_EVENT, params);
    showToast(s + mobileGatewayError.getMessage());

  }

  /**********************************************************/
  /*               EnrollingServiceListener               */

  /**********************************************************/

  @Override
  public void onCodeRequired(CHCodeVerifier chCodeVerifier) {
    AppLogger.d(TAG, ".onCodeRequired called. Providing activation code");

    SecureCodeInputer inputer = chCodeVerifier.getSecureCodeInputer();
    for (byte i : activationCode) {
      inputer.input(i);
    }
    inputer.finish();
    //wipe after use
    for (int i = 0; i < activationCode.length; i++) {
      activationCode[i] = 0;
    }

  }

  @Override
  public void onStarted() {
    AppLogger.d(TAG, ".onStarted()");
  }

  @ReactMethod
  @SuppressLint("HandlerLeak")
  public void loadCards(final Promise promise) {
    mPromise = promise;
    DigitalizedCardManager.getAllCards(new AbstractAsyncHandler<String[]>() {
      @Override
      public void onComplete(AsyncResult<String[]> asyncResult) {

        if (asyncResult.isSuccessful()) {
          List<DigitalizedCard> allCards = new ArrayList<>();
          for (String token : asyncResult.getResult()) {
            allCards.add(DigitalizedCardManager.getDigitalizedCard(token));
          }
          ////javascript model
          List<DigitalCard> dCards = new ArrayList<>();
          for ( DigitalizedCard card : allCards){
            DigitalCard dCard = new DigitalCard();
            dCard.setTokenId(card.getTokenizedCardID());
            dCard.setDigitalizedCardId(DigitalizedCardManager.getDigitalCardId(card.getTokenizedCardID()));
            //check default
            dCard.setDefaultCardFlag(card.isDefault(PaymentType.CONTACTLESS, null).waitToComplete().getResult());

            DigitalizedCardDetails digitalizedCardDetails = card.getCardDetails(null).waitToComplete().getResult();
            dCard.setRemotePaymentSupported(digitalizedCardDetails.isPaymentTypeSupported(PaymentType.DSRP));
            //get  card status
            dCard.setCardState((card.getCardState(null).waitToComplete().getResult()).getState().name());

            DigitalCardDetails digitalCardDetails = new DigitalCardDetails();
            digitalCardDetails.setLastFourDigitsDPAN(digitalizedCardDetails.getLastFourDigitsOfDPAN());
            digitalCardDetails.setLastFourDigitsFPAN(digitalizedCardDetails.getLastFourDigits());
            digitalCardDetails.setPanExpiry(digitalizedCardDetails.getPanExpiry());

            dCard.setDigitalCardDetails(digitalCardDetails);

            DigitalizedCardStatus cardStatus = card.getCardState(null).waitToComplete().getResult();
            //Check if the card needs replenishment of tokens
            TokenReplenishmentRequestor.replenish(cardStatus, card.getTokenizedCardID());

            dCards.add(dCard);

          }

          try {
            WritableArray cardList =  getDigitalCardsAsWritableArray(dCards);
            //AppLogger.i(TAG, "[WritableArray]-" + cardList);
            mPromise.resolve(cardList);


          } catch (JSONException e) {
            e.printStackTrace();
          }


        } else {
          int errorCode = asyncResult.getErrorCode();

          if (STORAGE_COMPONENT_ERROR == errorCode) {

            HashMap<String, Object> additionalInformation = asyncResult.getAdditionalInformation();

            if (additionalInformation != null && additionalInformation.size() > 0) {
              Object additionalObject = additionalInformation.get(STORAGE_COMPONENT_EXCEPTION_KEY);
              if (additionalObject != null && additionalObject instanceof Exception) {
                Exception exception = (Exception) additionalObject;
                AppLogger.e(TAG, "Get All cards failed because" + exception.getMessage());
                mPromise.reject(exception.getMessage());
                exception.printStackTrace();
                //In production app, this event to be sent to Analytics server. Exception stack trace can be sent to analytics, if available.
                //If exception stack trace is not possible, please send atleast the exception message. (e.getMessage() to analytics
              }
            }
            // the production MPA can retry again instead.
            AppLogger.e(TAG, "Failed to reload the card list due to secure storage: " + asyncResult.getErrorMessage());
            // if issue, persists even after certain number of retries. Recommend to do the following
            // 1. Send a specific error event that retry failed
            // 2. SDK APIs cannot be used in this user session anymore. so block all SDK usage from this point onward
          } else if (errorCode == DigitalizedCardErrorCodes.CD_CVM_REQUIRED) {

            AppLogger.d(TAG, "CD_CVM_REQUIRED");

            DeviceCVMEligibilityResult result = DeviceCVMEligibilityChecker.checkDeviceEligibility(reactApplicationContext);

            mPromise.reject(asyncResult.getErrorMessage());

            if (result.getBiometricsSupport() == BiometricsSupport.SUPPORTED) {
              //to use fingerprint. Be sure to check for device support
              try {
                DeviceCVMManager.INSTANCE.initialize(CHVerificationMethod.BIOMETRICS);
              } catch (DeviceCVMException e) {
                e.printStackTrace();
              }
            } else if (result.getDeviceKeyguardSupport() == DeviceKeyguardSupport.SUPPORTED) {
              //to use device key guard
              try {
                DeviceCVMManager.INSTANCE.initialize(CHVerificationMethod.DEVICE_KEYGUARD);
              } catch (DeviceCVMException e) {
                e.printStackTrace();
              }
            } else {
              mPromise.reject("Device not suitable");
              //throw new RuntimeException("Device not suitable for demo");
              //Force To use Wallet PIN
              //enablePin();
            }
          }
        }
      }
    });
  }


  private WritableArray getDigitalCardsAsWritableArray(List<DigitalCard> dCards) throws JSONException {
    WritableArray array = new WritableNativeArray();
    Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    for (DigitalCard card : dCards){
        String json = gson.toJson(card);
        WritableMap cardMap = JsonToMap.convertJsonToMap(new JSONObject(json));
        array.pushMap(cardMap);
    }
    return array;
  }

  @ReactMethod
  public void getCardImage(String digitalizedCardId,String cardArtType, final Promise promise) {
    mPromise  = promise;
    try {
      MobileGatewayManager.INSTANCE.getCardArt(digitalizedCardId).getBitmap(getCardType(cardArtType), new MGAbstractAsyncHandler<CardBitmap>() {
      //MobileGatewayManager.INSTANCE.getCardArt(digitalizedCardId).getBitmap(CardArtType.CO_BRAND_LOGO, new MGAbstractAsyncHandler<CardBitmap>() {
        @Override
        public void onComplete(MGAsyncResult<CardBitmap> mgAsyncResult) {
          if (mgAsyncResult.isSuccessful() && mgAsyncResult.getResult() !=null) {
            CardBitmap cardBitmap  = mgAsyncResult.getResult() ;
            try{
            String cardImageBase64 = Base64.encodeToString(cardBitmap.getResource(), Base64.DEFAULT);
              mPromise.resolve(cardImageBase64);
            } catch (Exception e){
              e.printStackTrace();
            }
          }
        }
      });
    } catch (NoSuchCardException e) {
      mPromise.reject(e.getMessage());
      e.printStackTrace();
    }
  }

  @ReactMethod
  public void unsetDefaultCard(final Promise promise) {
    mPromise = promise;
    DigitalizedCardManager.unsetDefaultCard(PaymentType.CONTACTLESS, new AbstractAsyncHandler<Void>() {
      @Override
      public void onComplete(AsyncResult<Void> asyncResult) {
        if( asyncResult.isSuccessful()){
          mPromise.resolve(asyncResult.getResult());
        }else{
          mPromise.reject("problem occured");
        }
      }

    });
  }

  @ReactMethod
  public void deleteCard(String digitalizedCardId,final Promise promise) {
    mPromise = promise;
    MobileGatewayManager.INSTANCE.getCardLifeCycleManager().deleteCard(digitalizedCardId, new MGCardLifecycleEventListener() {
      @Override
      public void onSuccess(String s) {
        mPromise.resolve(s);
      }
      @Override
      public void onError(String s, final MobileGatewayError mobileGatewayError) {
        mPromise.reject(mobileGatewayError.getMessage());
      }
    });
  }

  @ReactMethod
  public void setDefaultCardAction(String cardTokenId,String cardState,final Promise promise) {
     mPromise  = promise;
    if (cardState.equals(DigitalizedCardState.SUSPENDED.name())) {
      mPromise.reject("The card is Suspended.You cannot use the card to make payment");
    } else {
      DigitalizedCardManager.getDigitalizedCard(cardTokenId).setDefault(PaymentType.CONTACTLESS, new AbstractAsyncHandler<Void>() {
        @Override
        public void onComplete(AsyncResult<Void> asyncResult) {
          String defaultCardTokenID = cardTokenId;
          SharedPreferenceUtils.saveDefaultCard(getApplicationContext(), defaultCardTokenID);
          mPromise.resolve("Card (" + cardTokenId + ") set to default for payment");
        }
      });
    }
  }

  @ReactMethod
  public void checkSDKReady(final Promise promise) {
    mPromise = promise;
    boolean status = true;
    if (SDKController.getInstance().getSDKServiceState() != SDKServiceState.STATE_INITIALIZED) {
      AppLogger.d(TAG, "SDK not initialized");
      status = false;
    } else if (ProvisioningServiceManager.getWalletSecureEnrollmentBusinessService().getState() == WalletSecureEnrollmentState.WSE_REQUIRED) {
      AppLogger.d(TAG, "WSE REQUIRED");
      status = false;
    }
    mPromise.resolve(status);
  }


  @ReactMethod
  @SuppressLint("StaticFieldLeak")
  private void manageCard(String digitalizedCardId,boolean isResume,final Promise promise) {
    mPromise = promise;
    AppLogger.i(TAG,"manageCard");
    new AsyncTask<Object, Object, Object>(){

      @Override
      protected Object doInBackground(Object[] objects) {
        AppLogger.i(TAG,"doInBackground");
        ProvisioningBusinessService provisioningBusinessService=ProvisioningServiceManager.getProvisioningBusinessService();
        if (provisioningBusinessService == null) {
          AppLogger.i(TAG, "provisioningBusinessService is null");
          mPromise.reject("Error(1), cannot suspend card");
        } else {
          AppLogger.i(TAG, "getAccessToken");
          provisioningBusinessService.getAccessToken(digitalizedCardId,
            GetAccessTokenMode.NO_REFRESH, new AccessTokenListener() {
              @Override
              public void onSuccess(String digitalCardID, String accessToken) {
                AppLogger.i(TAG,"getAccessToken::onSuccess");

                if(!isResume){
                  MobileGatewayManager.INSTANCE.getCardLifeCycleManager().suspendCard(
                    digitalizedCardId,
                    new MGCardLifecycleEventListener() {
                      @Override
                      public void onSuccess(String s) {
                        AppLogger.i(TAG,"suspendcard ::onSuccess");
                        mPromise.resolve(s +" Request sent to suspend card");
                      }

                      @Override
                      public void onError(String s, final MobileGatewayError mobileGatewayError) {
                        AppLogger.i(TAG,"suspendcard ::onError");
                        mPromise.reject(mobileGatewayError.getMessage());
                      }
                    },null,null,accessToken);
                }else{
                  MobileGatewayManager.INSTANCE.getCardLifeCycleManager().resumeCard(
                    digitalizedCardId,
                    new MGCardLifecycleEventListener() {
                      @Override
                      public void onSuccess(String s) {
                        AppLogger.i(TAG,"resumeCard ::onSuccess");
                        mPromise.resolve(s +" Request sent to resume card");

                      }

                      @Override
                      public void onError(String s, final MobileGatewayError mobileGatewayError) {
                        AppLogger.i(TAG,"resumeCard ::onError");
                        mPromise.reject(mobileGatewayError.getMessage());
                      }
                    },null,null,accessToken);
                }

              }

              @Override
              public void onError(String s, ProvisioningServiceError provisioningServiceError) {
                AppLogger.i(TAG,"access Token ::onError");
                mPromise.reject(provisioningServiceError.getErrorMessage());

              }
            });
        }
        return null;
      }
    }.execute();
  }


  @ReactMethod
  public void initSDK() {
    AppLogger.d(TAG, "SDKHelper init");
    Thread initThread = new Thread(() -> {
      SDKHelper.InitCPSSDKCallback initCPSSDKCallback = new SDKHelper.InitCPSSDKCallback() {
        @Override
        public void doAction() {
          //init MG SDK
          new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
              //Firebase API has limitation when there are multiple sender ID, the onNewToken is triggered only for default SENDER_ID.
              // So it is prudent to check for updatePushToken regularly after SDK initialization as well.
              //And it is prudent to check for updatePushToken just before card enrollment process begin as well.
              SDKHelper.updateFirebaseToken(reactApplicationContext);
              SDKHelper.initMGSDKCall(reactApplicationContext);
              registerPreFpEntry();
              SDKHelper.performWalletSecureEnrollmentFlow(reactApplicationContext);
            }
          }, AppBuildConfigurations.INIT_MG_SDK_DELAY);
        }
      };
      SDKHelper.initCPSSDK(reactApplicationContext, initCPSSDKCallback, true);
    });
    initThread.start();
  }

  @ReactMethod
  public static void initialiseSDK(){
    AppLogger.d(TAG, "SDKHelper init");
    SDKHelper.InitCPSSDKCallback initCPSSDKCallback = () -> {
      //Firebase API has limitation when there are multiple sender ID, the onNewToken is triggered only for default SENDER_ID.
      // So it is prudent to check for updatePushToken regularly after SDK initialization as well.
      //And it is prudent to check for updatePushToken just before card enrollment process begin as well.
      SDKHelper.updateFirebaseToken(reactApplicationContext);
      SDKHelper.performWalletSecureEnrollmentFlow(reactApplicationContext);
    };
    SDKHelper.initCPSSDK(reactApplicationContext, initCPSSDKCallback, true);
  }

  @ReactMethod
  public void updateFirebaseToken(){
    SDKHelper.updateFirebaseToken(reactApplicationContext);
  }

  @ReactMethod
  public void DeviceCVMEligibilityChecker(Promise promise) {
    mPromise = promise;
    DeviceCVMEligibilityResult result = DeviceCVMEligibilityChecker.checkDeviceEligibility(reactApplicationContext);
    if (result.getBiometricsSupport() == BiometricsSupport.SUPPORTED) {
      //to use fingerprint. Be sure to check for device support
      try {
        DeviceCVMManager.INSTANCE.initialize(CHVerificationMethod.BIOMETRICS);
        mPromise.resolve(true);
      } catch (DeviceCVMException e) {
        e.printStackTrace();
      }
    } else if (result.getDeviceKeyguardSupport() == DeviceKeyguardSupport.SUPPORTED) {
      //to use device key guard
      try {
        DeviceCVMManager.INSTANCE.initialize(CHVerificationMethod.DEVICE_KEYGUARD);
        mPromise.resolve(true);
      } catch (DeviceCVMException e) {
        e.printStackTrace();
      }
    } else {
      mPromise.resolve(false);
    }
  }

  @ReactMethod
  public void fetchTransactionHistory(String digitalCardId,Promise promise){
    mPromise = promise;
    AppLogger.i(TAG, "[HistoryActivity fetchHistory]");
    ProvisioningServiceManager.getProvisioningBusinessService().getAccessToken(digitalCardId, GetAccessTokenMode.REFRESH,
      new AccessTokenListener() {
        @Override
        public void onSuccess(String digitalCardId, String accessToken) {
          AppLogger.i(TAG, "[HistoryActivity fetchHistory onSuccess]");
          getTransactionHistory(digitalCardId, accessToken,promise);
        }

        @Override
        public void onError(String digitalCardId, ProvisioningServiceError provisioningServiceError) {
          AppLogger.i(TAG, "[HistoryActivity fetchHistory onError]");
          mPromise.reject(provisioningServiceError.getErrorMessage());
        }
      });

  }

  private void getTransactionHistory(String digitalCardId, String accessToken, Promise promise) {
    mPromise = promise;
    AppLogger.i(TAG, "[HistoryActivity getTransactionHistory]");
    MobileGatewayManager.INSTANCE.getTransactionHistoryService().refreshHistory(accessToken, digitalCardId, null,
      new TransactionHistoryListener() {
        @Override
        public void onSuccess(final List<MGTransactionRecord> transactionRecord, String digitalCardId, String timeStamp) {
          AppLogger.i(TAG, "[HistoryActivity getTransactionHistory onSuccess]");
             final List<TransactionHistory> history = new ArrayList<>();
              for (final MGTransactionRecord allRecords : transactionRecord) {
                final TransactionHistory mRecords = new TransactionHistory(allRecords);
                mRecords.setTransactionId(allRecords.getTransactionId());
                mRecords.setTransactionAmount(allRecords.getDisplayAmount());
                mRecords.setMerchantName(allRecords.getMerchantName());
                mRecords.setTransactionStatus(allRecords.getTransactionStatus().getValue());
                mRecords.setTransactionDate(allRecords.getTransactionDate());
                history.add(mRecords);
              }

          try {
            WritableArray historyList = getHistoryAsWritableArray(history);
            mPromise.resolve(historyList);
          } catch (JSONException e) {
            e.printStackTrace();
          }

        }

        @Override
        public void onError(String digitalCardId, MobileGatewayError mobileGatewayError) {
          switch (mobileGatewayError.getHTTPStatusCode()) {
            // to retry
            //case TOKEN_NOT_VALID:
            case 401:
               //fetchTransactionHistory(digitalCardId,promise);
                break;

          }
          mPromise.reject(mobileGatewayError.getMessage());
        }
      });
  }

  private WritableArray getHistoryAsWritableArray(List<TransactionHistory> history) throws JSONException {
    WritableArray array = new WritableNativeArray();
    Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    for (TransactionHistory historyItem : history){
      String json = gson.toJson(historyItem);
      WritableMap historyMap = JsonToMap.convertJsonToMap(new JSONObject(json));
      array.pushMap(historyMap);
    }
    return array;
  }

  private BroadcastReceiver receiver = new BroadcastReceiver()  {
    @Override
    public void onReceive(Context contexts, Intent intent) {

        Bundle bundle =  intent.getExtras();
        ProvisioningBusinessService provService = ProvisioningServiceManager.getProvisioningBusinessService();
        provService.processIncomingMessage(bundle, ThalesPaysdkWrapperModule.this);

    }
  };

  private static final Object mLock = new Object();
  private static ThalesPaysdkWrapperModule mInstance;

  @NonNull
  public static ThalesPaysdkWrapperModule getInstance() {
    synchronized (mLock) {
      if (mInstance == null) {
        mInstance = new ThalesPaysdkWrapperModule(reactApplicationContext);
      }
      return mInstance;
    }
  }


  @Nullable
  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put("REQUEST_INSTALL_CARD","CBP.info.tokenProvisioned");
    constants.put("REQUEST_DELETE_CARD","CBP.info.tokenDeleted");
    constants.put("REQUEST_SUSPEND_CARD","CBP.info.tokenSuspended");
    constants.put("REQUEST_RESUME_CARD","CBP.info.tokenResumed");
    constants.put("REQUEST_REPLENISH_KEYS","CBP.info.tokenReplenished");
    constants.put("BANK_LOGO","CardArtType.BANK_LOGO");
    constants.put("CARD_BACKGROUND_COMBINED","CardArtType.CARD_BACKGROUND_COMBINED");
    constants.put("CARD_BACKGROUND","CardArtType.CARD_BACKGROUND");
    constants.put("CO_BRAND_LOGO","CardArtType.CO_BRAND_LOGO");
    constants.put("SCHEME_LOGO","CardArtType.SCHEME_LOGO");
    return constants;
  }

  public CardArtType getCardType(String cardType){
    CardArtType value = CardArtType.CARD_BACKGROUND_COMBINED;
    switch (cardType)
    {
      case ("CardArtType.CARD_BACKGROUND_COMBINED"):
        value = CardArtType.CARD_BACKGROUND_COMBINED;
        break;
      case ("CardArtType.BANK_LOGO"):
        value = CardArtType.BANK_LOGO;
        break;
      case ("CardArtType.CARD_BACKGROUND"):
        value = CardArtType.CARD_BACKGROUND;
        break;
      case ("CardArtType.SCHEME_LOGO"):
        value = CardArtType.SCHEME_LOGO;
        break;
    }
    return value;
  }



}
