package com.reactnativethalespaysdkwrapper;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.gemalto.mfs.mwsdk.cdcvm.DeviceCVMCancellationSignal;
import com.gemalto.mfs.mwsdk.cdcvm.DeviceCVMVerifier;
import com.gemalto.mfs.mwsdk.cdcvm.DeviceCVMVerifierInput;
import com.gemalto.mfs.mwsdk.cdcvm.DeviceCVMVerifyListener;
import com.gemalto.mfs.mwsdk.dcm.DigitalizedCard;
import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardManager;
import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardState;
import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardStatus;
import com.gemalto.mfs.mwsdk.dcm.PaymentType;
import com.gemalto.mfs.mwsdk.payment.CHVerificationMethod;
import com.gemalto.mfs.mwsdk.payment.CVMResetTimeoutListener;
import com.gemalto.mfs.mwsdk.payment.PaymentBusinessManager;
import com.gemalto.mfs.mwsdk.payment.PaymentBusinessService;
import com.gemalto.mfs.mwsdk.payment.PaymentServiceErrorCode;
import com.gemalto.mfs.mwsdk.payment.chverification.CHVerificationManager;
import com.gemalto.mfs.mwsdk.payment.engine.ContactlessPaymentServiceListener;
import com.gemalto.mfs.mwsdk.payment.engine.DeactivationStatus;
import com.gemalto.mfs.mwsdk.payment.engine.PaymentService;
import com.gemalto.mfs.mwsdk.payment.engine.TransactionContext;
import com.gemalto.mfs.mwsdk.provisioning.ProvisioningServiceManager;
import com.gemalto.mfs.mwsdk.provisioning.sdkconfig.ProvisioningBusinessService;
import com.gemalto.mfs.mwsdk.sdkconfig.SDKError;
import com.gemalto.mfs.mwsdk.utils.async.AbstractAsyncHandler;
import com.gemalto.mfs.mwsdk.utils.async.AsyncResult;
import com.reactnativethalespaysdkwrapper.app.AppBuildConfigurations;
import com.reactnativethalespaysdkwrapper.app.AppConstants;
import com.reactnativethalespaysdkwrapper.helper.AsyncHelperCardState;
import com.reactnativethalespaysdkwrapper.model.DigitalCard;
import com.reactnativethalespaysdkwrapper.payment.contactless.pfp.PFPHCEService;
import com.reactnativethalespaysdkwrapper.payment.contactless.pfp.PFPHelper;
import com.reactnativethalespaysdkwrapper.util.AppLogger;
import com.reactnativethalespaysdkwrapper.util.SharedPreferenceUtils;
import com.reactnativethalespaysdkwrapper.util.TransactionContextHelper;

import java.util.HashMap;
import java.util.Map;

import static com.gemalto.mfs.mwsdk.sdkconfig.AndroidContextResolver.getApplicationContext;

@ReactModule(name = PayModule.NAME)
public class PayModule extends ReactContextBaseJavaModule implements ContactlessPaymentServiceListener, CVMResetTimeoutListener, LifecycleEventListener {
  public static final String NAME = "PayModule";
  private static final String TAG = "PayModule";
  private static ReactApplicationContext reactApplicationContext;
  private Promise mPromise;
  public static final String DEVICE_AUTH_LISTENER = "DeviceAuthListener";
  public static final String PAYMENT_TIMER_LISTENER = "PaymentTimeListener";
  public static final String PAYMENT_STATE_LISTENER = "PaymentStateListener";
  private int fingerprintErrorCount = 0;
  private static final int MAX_ATTEMPT_COUNT = 5;
  private CharSequence title = "Verify your Fingerprint";
  private CharSequence subTitle = "contactles payment";
  private CharSequence negativeButtonText ="Cancel";
  private String formattedAmount = "";
  private DigitalCard defaultDigitalCard;
  private StringBuilder keyguardDescription;
  private DigitalizedCard mDigitalizedCard;


  public PayModule(ReactApplicationContext reactContext) {
    super(reactContext);
    reactApplicationContext = reactContext;
    keyguardDescription = new StringBuilder();
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }


  private static final Object mLock = new Object();
  private static PayModule mInstance;

  @NonNull
  public static PayModule getInstance() {
    synchronized (mLock) {
      if (mInstance == null) {
        mInstance = new PayModule(reactApplicationContext);
      }
      return mInstance;
    }
  }

  private void sendEvent(ReactContext reactContext,
                         String eventName,
                         @Nullable WritableMap params) {
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
  }

  /**********************************************************/
  /*               ContactlessPaymentServiceListener              */

  /**********************************************************/

  @ReactMethod
  public void makePayment(String cardTokenId,String cardState, Promise promise){
    mPromise = promise;
    String defaultCardTokenID = DigitalizedCardManager
      .getDefault(PaymentType.CONTACTLESS, null).waitToComplete().getResult();
    AppLogger.i(TAG, "defaultCardTokenID fetched");
    if (defaultCardTokenID != null && !defaultCardTokenID.isEmpty()) {
      AppLogger.i(TAG, "defaultCardTokenID fetched is Not null or empty");
      SharedPreferenceUtils.saveDefaultCard(getApplicationContext(), defaultCardTokenID);
    } else {
      AppLogger.i(TAG, "defaultCardTokenID fetched is null or empty");
      mPromise.reject("defaultCardTokenID fetched is null or empty");
    }
    if (!cardState.equals(DigitalizedCardState.ACTIVE.name())) {
      //Card is not Active and cannot be used for payment
      mPromise.reject("Card is not Active and cannot be used for payment");
    }else{
      DigitalizedCardManager.getDigitalizedCard(cardTokenId).setDefault(PaymentType.CONTACTLESS, new AbstractAsyncHandler<Void>() {
        @Override
        public void onComplete(AsyncResult<Void> asyncResult) {
          if(asyncResult.isSuccessful()) {
            mPromise.resolve(true);
            final PaymentBusinessService paymentBS = PaymentBusinessManager.getPaymentBusinessService();
            AppLogger.i(TAG, "getAuthenticationFlowPriorToPayment started");
            paymentBS.getAuthenticationFlowPriorToPayment(PayModule.this, PaymentType.CONTACTLESS);
            AppLogger.i(TAG, "getAuthenticationFlowPriorToPayment ended");
          }else{
            mPromise.reject("Card could not be set as default card");
          }
        }
      });

    }






  }

  @Override
  public void onHostResume() {

  }

  @Override
  public void onHostPause() {

  }

  @Override
  public void onHostDestroy() {
    AppLogger.d(TAG, "==PaymentContactlessActivity onDestroy==");
    deactivatePaymentServiceAndResetState();
    releaseWakeLockAndClearFlag();
  }

  enum PaymentState {
    STATE_NONE,
    STATE_ON_TRANSACTION_STARTED,
    STATE_ON_AUTHENTICATION_REQUIRED,
    STATE_ON_READY_TO_TAP,
    STATE_ON_TRANSACTION_COMPLETED,
    STATE_ON_ERROR
  }

  private static final int ERROR_THRESHOLD = 3;
  private static final int ERROR_DELAY = 2000;
  private PaymentState currentState = PaymentState.STATE_NONE;
  private PaymentService paymentService;
  private TransactionContext transactionContext;
  private CHVerificationMethod chVerificationMethod;
  private PaymentServiceErrorCode paymentServiceErrorCode;
  private long cvmResetTimeout;
  private String errorMessage;
  private int posCommDisconnectedErrCount = 0;

  @Nullable
  public PaymentService getPaymentService() {
    return paymentService;
  }

  @Nullable
  public TransactionContext getTransactionContext() {
    return transactionContext;
  }

  @Nullable
  public CHVerificationMethod getChVerificationMethod() {
    return chVerificationMethod;
  }

  @NonNull
  public PaymentState getCurrentState() {
    return currentState;
  }

  public PaymentServiceErrorCode getPaymentServiceErrorCode() {
    return paymentServiceErrorCode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public long getCvmResetTimeout() {
    return cvmResetTimeout;
  }

  /**
   * First callback indicating payment transaction is started. This is called when the first APDU is received via NFC link
   */
  @Override
  public void onTransactionStarted() {
    AppLogger.i(AppConstants.APP_TAG, ".onTransactionStarted()"+AppConstants.STARTED);
    resetState();
    posCommDisconnectedErrCount = 0;
    currentState = PaymentState.STATE_ON_TRANSACTION_STARTED;
    if (AppBuildConfigurations.IS_PFP_ENABLED) {
      launchPaymentScreen();
    }else{

    }
    AppLogger.i(AppConstants.APP_TAG, ".onTransactionStarted()"+AppConstants.ENDED);
  }


  /**
   * Optional
   * <p>
   * Callback to indicate that CVM had been successfully provided by user. Show timer to user to tap on POS again for transaction.
   * <p>
   * This is called after onPaymentServiceActivated.
   *
   * @param paymentService
   */
  @Override
  public void onReadyToTap(PaymentService paymentService) {
    AppLogger.d(AppConstants.APP_TAG, ".onReadyToTap"+
      " "+AppConstants.STARTED);
    AppLogger.d(AppConstants.APP_TAG, "T3_Activity "+AppConstants.ENDED);
    AppLogger.d(AppConstants.APP_TAG, "T4_Activity "+AppConstants.STARTED);

    resetState();

    posCommDisconnectedErrCount = 0;
    currentState = PaymentState.STATE_ON_READY_TO_TAP;
    this.paymentService = paymentService;
    this.transactionContext = paymentService.getTransactionContext();
    launchPaymentScreen();
    AppLogger.d(AppConstants.APP_TAG, ".onReadyToTap"+
      " "+AppConstants.ENDED);
  }

  /**
   * Last callback for successful sending of payment data to POS. Please note that transaction result is not known to SDK since transactions are
   * online.
   * This only indicates that payment data had been sent to POS successfully.
   *
   * @param transactionContext
   */
  @Override
  public void onTransactionCompleted(TransactionContext transactionContext) {

    PFPHCEService.apduCounter=0;
    AppLogger.d(AppConstants.APP_TAG, "T5_Activity " + AppConstants.ENDED);
    AppLogger.d(AppConstants.APP_TAG, ".onTransactionCompleted"+
      " "+AppConstants.STARTED);

    resetState();
    posCommDisconnectedErrCount = 0;
    currentState = PaymentState.STATE_ON_TRANSACTION_COMPLETED;
    this.transactionContext = transactionContext;
    launchPaymentScreen();
    AppLogger.d(AppConstants.APP_TAG, ".onTransactionCompleted"+
      " "+AppConstants.ENDED);

  }


  /**
   * Conditional
   * callback indicating first tap  transaction is completed. This is called only in case of PFP first tap completed
   *
   */
  @Override
  public void onFirstTapCompleted() {
    AppLogger.i(AppConstants.APP_TAG, "onFirstTapCompleted"+ AppConstants.STARTED);
    AppLogger.d(AppConstants.APP_TAG, "T1_Activity " + AppConstants.ENDED);
    showPaymentTransitionScreenForPFP();
    unlockAndWake();
    AppLogger.d(AppConstants.APP_TAG, "T2_Activity " + AppConstants.STARTED);
    //PFPHelper.INSTANCE.initSDKs(reactApplicationContext,false);
    PFPHelper.INSTANCE.initSDKs(getApplicationContext(),false);
    AppLogger.i(AppConstants.APP_TAG, "onFirstTapCompleted"+ AppConstants.ENDED);
   /* Context context = getApplicationContext();
    Intent myIntent = new Intent(context, HeartbeatEventService.class);
    context.startService(myIntent);
    HeadlessJsTaskService.acquireWakeLockNow(context);*/

  }

  /**
   * Optional
   * <p>
   * Callback to indicate that payment requires CVM method. Use chVerificationMethod to determine what is the cvm type and display appropriate UI.
   */
  @Override
  public void onAuthenticationRequired(PaymentService paymentService, CHVerificationMethod chVerificationMethod, long cvmResetTimeout) {
    AppLogger.d(AppConstants.APP_TAG, ".onAuthenticationRequired() " + AppConstants.STARTED);
    AppLogger.d(AppConstants.APP_TAG, "T2_Activity "+AppConstants.ENDED);
    AppLogger.d(AppConstants.APP_TAG, "T0_TILL_T2_Activity " + AppConstants.ENDED);

    AppLogger.d(AppConstants.APP_TAG, "T3_Activity "+AppConstants.STARTED);
    resetState();
    posCommDisconnectedErrCount = 0;
    currentState = PaymentState.STATE_ON_AUTHENTICATION_REQUIRED;
    this.paymentService = paymentService;
    this.chVerificationMethod = chVerificationMethod;
    this.cvmResetTimeout = cvmResetTimeout;
    this.transactionContext = paymentService.getTransactionContext();
    launchPaymentScreen();
    AppLogger.d(AppConstants.APP_TAG, ".onPaymentServiceActivated() " + AppConstants.ENDED);
  }

  @Override
  public void onError(SDKError<PaymentServiceErrorCode> sdkPaymentServiceErrorCode) {

    AppLogger.d(AppConstants.APP_TAG, ".onError"+
      " "+AppConstants.STARTED);
    resetState();

    currentState = PaymentState.STATE_ON_ERROR;
    this.transactionContext = null;
    this.paymentServiceErrorCode = sdkPaymentServiceErrorCode.getErrorCode();
    this.errorMessage = sdkPaymentServiceErrorCode.getErrorMessage();

    if (sdkPaymentServiceErrorCode == null || paymentServiceErrorCode == PaymentServiceErrorCode.POS_COMM_DISCONNECTED) {
      if (posCommDisconnectedErrCount < ERROR_THRESHOLD) {
        posCommDisconnectedErrCount++;

      } else {
        posCommDisconnectedErrCount = 0;
      }
    } else {
      PFPHCEService.apduCounter=0;
      //other errors
      posCommDisconnectedErrCount = 0;
      launchPaymentScreen();
    }
        AppLogger.d(AppConstants.APP_TAG, ".onError" +
          " " + AppConstants.STARTED);

  }


  /**
   * Reset state variables
   */
  void resetState() {
    this.currentState = PaymentState.STATE_NONE;
    this.paymentService = null;
    this.chVerificationMethod = null;
    this.paymentServiceErrorCode = null;
    this.transactionContext = null;
    this.errorMessage = null;
    AppLogger.d(TAG, "payment resetState is invoked to clear payment data in between states");
  }

  private void launchPaymentScreen() {
    AppLogger.d(AppConstants.APP_TAG, "launchPaymentScreen() with state :" + this.currentState + " " + AppConstants.STARTED);
    handlePaymentState();
  }

  public void showPaymentTransitionScreenForPFP(){
    AppLogger.d(TAG, "showPaymentTransitionScreenForPFP");
    this.currentState=PaymentState.STATE_ON_TRANSACTION_STARTED;
    launchPaymentScreen();
  }

  private PowerManager.WakeLock mWakeLock;


  @ReactMethod
  public void unlockAndWake() {
    AppLogger.d(TAG, "unlockAndWake is invoked");

    //Return if mWakelock is non null as it's already initialized and acquired
    if (mWakeLock != null && mWakeLock.isHeld())
      return;

    PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);

    if (pm != null && !pm.isInteractive()) {
      mWakeLock = pm.newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        | PowerManager.ACQUIRE_CAUSES_WAKEUP, getApplicationContext().getResources().getString(R.string.app_name) + "CA:wakelock");
      if (mWakeLock != null)
        mWakeLock.acquire();
    }

    getCurrentActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
      | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
      | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
      | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

  }


  /**
   * Display UI according to payment state machine.
   */
  private void handlePaymentState() {
    PaymentState state = getCurrentState();
    AppLogger.i(TAG, "state!!"+state.name());

    if (state == PaymentState.STATE_NONE) {
      AppLogger.i(TAG, "Unknown payment state!! Close PaymentContactlessActivity");
      return;
    }
    switch (state) {
      case STATE_ON_TRANSACTION_STARTED:

        WritableMap params = Arguments.createMap();
        params.putString("state",PaymentState.STATE_ON_TRANSACTION_STARTED.name());
        sendEvent(reactApplicationContext, PAYMENT_STATE_LISTENER, params);
        break;
      case STATE_ON_AUTHENTICATION_REQUIRED:
        WritableMap authParams = Arguments.createMap();
        authParams.putString("state",PaymentState.STATE_ON_AUTHENTICATION_REQUIRED.name());
        sendEvent(reactApplicationContext, PAYMENT_STATE_LISTENER, authParams);
        if(!CHVerificationManager.INSTANCE.isFCDCVMSupported()){
          AppLogger.d(TAG,"this is card like, so stay in wait fragment ");
          //  showWaitFragment();
          break;
        }
        CHVerificationMethod chVerificationMethod = getChVerificationMethod();
        if (chVerificationMethod == null) chVerificationMethod = CHVerificationMethod.NONE;
        switch (chVerificationMethod) {
          case WALLET_PIN:
          case BIOMETRICS:
          case DEVICE_KEYGUARD:
            // Regular flow could continue with CHV verification
            triggerCHVverification();
            break;
          default:
            // NOTE: Not supported any other verification
            // deactivatePaymentServiceAndResetState();
            // displayAndLogError(getString(R.string.unsupported_cvm), "Verification method " + chVerificationMethod + " is not supported!");
        }

        break;
      case STATE_ON_READY_TO_TAP:
        showTimerFragment();
        WritableMap tapParams = Arguments.createMap();
        tapParams.putString("state",PaymentState.STATE_ON_READY_TO_TAP.name());
        sendEvent(reactApplicationContext, PAYMENT_STATE_LISTENER, tapParams);
        break;
      case STATE_ON_ERROR:
        WritableMap errorParams = Arguments.createMap();
        errorParams.putString("state",PaymentState.STATE_ON_ERROR.name());
        sendEvent(reactApplicationContext, PAYMENT_STATE_LISTENER, errorParams);

        completeTransaction();

        break;
      case STATE_ON_TRANSACTION_COMPLETED:
        WritableMap completedParams = Arguments.createMap();
        completedParams.putString("state",PaymentState.STATE_ON_TRANSACTION_COMPLETED.name());
        sendEvent(reactApplicationContext, PAYMENT_STATE_LISTENER, completedParams);

        completeTransaction();

        break;
    }

  }

  private DeviceCVMVerifier deviceCVMVerifier;
  private DeviceCVMCancellationSignal cancellationSignal;

  private void triggerCHVverification() {
    AppLogger.i(AppConstants.APP_TAG, ".triggerCHVerification");
    final CHVerificationMethod chVerificationMethod = getChVerificationMethod();
    if (chVerificationMethod == CHVerificationMethod.BIOMETRICS
      || chVerificationMethod == CHVerificationMethod.DEVICE_KEYGUARD) {

      if(TransactionContextHelper.formatAmountWithCurrency(getTransactionContext()) != null) {
        String amount = TransactionContextHelper.formatAmountWithCurrency(getTransactionContext());
       // formattedAmount = TransactionContextHelper.formatAmountWithCurrency(getTransactionContext());
        keyguardDescription.append("Transaction amount: ");
        keyguardDescription.append(amount);
        keyguardDescription.append("\n");
      }
       keyguardDescription.append("Verify using your PIN, Password or Pattern");

       triggerAuth(chVerificationMethod,keyguardDescription.toString());
    }
  }

  private void triggerAuth(CHVerificationMethod cvm,String keyguardDescription) {
    deviceCVMVerifier = (DeviceCVMVerifier) PaymentBusinessManager.getPaymentBusinessService()
      .getActivatedPaymentService().getCHVerifier(cvm);

    deviceCVMVerifier.setDeviceCVMVerifyListener(deviceCVMVerifyListener);
    // deviceCVMVerifier.setKeyguardActivity(this, DeviceKeyguardActivity.class);

    if (cvm == CHVerificationMethod.BIOMETRICS) {
      AppLogger.d(AppConstants.APP_TAG, ".Starting fp authentication prompt for user " + AppConstants.STARTED);

      DeviceCVMVerifierInput deviceCVMVerifierInput = new DeviceCVMVerifierInput(title, subTitle, keyguardDescription, negativeButtonText);
      cancellationSignal = deviceCVMVerifierInput.getDeviceCVMCancellationSignal();
      deviceCVMVerifier.startAuthentication(deviceCVMVerifierInput);

      AppLogger.d(AppConstants.APP_TAG, ".Starting fp authentication prompt for user " + AppConstants.ENDED);

      //If on top of lock screen and in Android9 device,
      // show keyguard button for user to authenticate
    }else if (cvm == CHVerificationMethod.DEVICE_KEYGUARD) {
      AppLogger.d(TAG, "Verify by Keyguard");

      new Handler().postDelayed(() -> {
        AppLogger.i(AppConstants.APP_TAG, ".Starting keyguard authentication");
        deviceCVMVerifier.confirmCredential(title, keyguardDescription);
      }, 100);

    } else {
      throw new IllegalStateException("Invalid Verification method");
    }
  }

  /**
   *
   * @return true means on top of lock screen
   */
  private boolean isRestrictModeEnabled() {
    KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
    //Check if in case the MPA is used for payment without unlocking device
    return keyguardManager != null && keyguardManager.inKeyguardRestrictedInputMode();
  }



  final DeviceCVMVerifyListener deviceCVMVerifyListener = new DeviceCVMVerifyListener() {
    @Override
    public void onVerifySuccess() {
      AppLogger.i(AppConstants.APP_TAG, ".onVerifySuccess");
      WritableMap params = Arguments.createMap();
      params.putString("status", "verified");
      sendEvent(reactApplicationContext, DEVICE_AUTH_LISTENER, params);

    }

    @Override
    public void onVerifyError(SDKError<Integer> sdkErrorInteger) {
      AppLogger.e(TAG, "onVerifyError");
      fingerprintErrorCount++;
      WritableMap params = Arguments.createMap();
      params.putString("status", "failed");
      sendEvent(reactApplicationContext, DEVICE_AUTH_LISTENER, params);
      if (sdkErrorInteger.getErrorCode() == FingerprintManager.FINGERPRINT_ERROR_CANCELED) {
        // When user hits button to switch to keyguard after a FP verification fails
        // then this error handler is triggered with FINGERPRINT_ERROR_CANCELED
        // There is an error situation when OS sends this error in loop during Fingerprint authentication setup
        // Application is to break the loop, don't ask authentication and enable device keyguard button
        cancellationSignal.cancel();

        if (fingerprintErrorCount < MAX_ATTEMPT_COUNT) {
          DeviceCVMVerifierInput deviceCVMVerifierInput = new DeviceCVMVerifierInput(title,
            subTitle, formattedAmount, negativeButtonText);
          cancellationSignal = deviceCVMVerifierInput.getDeviceCVMCancellationSignal();
          deviceCVMVerifier.startAuthentication(deviceCVMVerifierInput);
         // enableFallBackMechanism(sdkErrorInteger.getErrorMessage(), fingerprintErrorCount);

        } else {
          if (Build.VERSION.SDK_INT >= 29) {
            /*
             * rsriniva: For Android Q or up, confirmCredential has no impact.
             * When It reaches here, it also means the fallback was not chosen by user or was not available for Android Q.
             * Therefore, we need to cancel the transaction.
             */
            cancelAction();
          } else {
            deviceCVMVerifier.confirmCredential(title, formattedAmount);
          }
        }

      }
     }

    @Override
    public void onVerifyFailed() {
      AppLogger.e(TAG, "onVerifyFailed");
      WritableMap params = Arguments.createMap();
      params.putString("status", "failed");
      sendEvent(reactApplicationContext, DEVICE_AUTH_LISTENER, params);
    }

    @Override
    public void onVerifyHelp(int i, CharSequence charSequence) {
      AppLogger.e(TAG, "onVerifyHelp");
      WritableMap params = Arguments.createMap();
      params.putString("status", "failed");
      sendEvent(reactApplicationContext, DEVICE_AUTH_LISTENER, params);

    }
  };

  private void cancelAction() {
    if (cancellationSignal != null) {
      cancellationSignal.cancel();
    }
    PaymentBusinessManager.getPaymentBusinessService().deactivate();
  }


  private void showTimerFragment() {
    ///ready to tap
    if (getPaymentService() != null) {
      getPaymentService().setCVMResetTimeoutListener(this);
     String timer = (String.valueOf(getCvmResetTimeout()/1000));
      AppLogger.e(TAG, timer);
    }
    String defaultCardTokenID = DigitalizedCardManager
      .getDefault(PaymentType.CONTACTLESS, null).waitToComplete().getResult();
    DigitalizedCard sdkDigitalizeCard=DigitalizedCardManager.getDigitalizedCard(defaultCardTokenID);
    defaultDigitalCard = new DigitalCard();
    defaultDigitalCard.setTokenId(sdkDigitalizeCard.getTokenizedCardID());
    defaultDigitalCard.setDigitalizedCardId(DigitalizedCardManager.getDigitalCardId(defaultCardTokenID));

  }

  @Override
  public void onCredentialsTimeoutCountDown(int i) {

    AppLogger.e(TAG, String.valueOf(i));
    WritableMap params = Arguments.createMap();
    params.putString("timer", String.valueOf(i));
    sendEvent(reactApplicationContext, PAYMENT_TIMER_LISTENER, params);

  }

  @Override
  public void onCredentialsTimeout(PaymentService paymentService, CHVerificationMethod chVerificationMethod, long l) {
    deactivatePaymentServiceAndResetState();
  }

  public void deactivatePaymentServiceAndResetState() {
    AppLogger.i(TAG, "deactivatePaymentServiceAndResetState");
    deactivatePaymentService();
    resetState();
  }
  public void releaseWakeLockAndClearFlag() {
    AppLogger.d(TAG, "WakeLock is released");

    if (mWakeLock != null) {
      try {
        mWakeLock.release();
      } catch (Exception e) {
        // Ignoring this exception, probably wakeLock was already released
        AppLogger.e(TAG, "Exception observed in releasing wakeLock");
      } finally {
        mWakeLock = null;
      }
    }

    getCurrentActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
      | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
      | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
      | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
  }

  private void deactivatePaymentService() {
    PaymentBusinessService paymentServ = PaymentBusinessManager.getPaymentBusinessService();
    if (paymentServ != null) {
      AppLogger.d(TAG, "paymentServ is deactivated");
      paymentServ.deactivate();
    } else {
      AppLogger.e(TAG, "Failed to get PaymentBusinessService to deactivate");
    }
  }


  private void completeTransaction(){
    // Trying to set back the default card is not needed for payment flow with PFP
    // CPS should take care when deactivate is called.
    PaymentBusinessService pbs = PaymentBusinessManager.getPaymentBusinessService();
    if(pbs != null) {
      pbs.deactivate();
    }
  }

  @ReactMethod
  public void getDefaultDigitalCardId(Promise promise){
    mPromise = promise;
    String currentDefaultCardTokenID = DigitalizedCardManager
      .getDefault(PaymentType.CONTACTLESS, null).waitToComplete().getResult();
    if(currentDefaultCardTokenID != null && currentDefaultCardTokenID.length()>0) {
       mPromise.resolve(DigitalizedCardManager.getDigitalCardId(currentDefaultCardTokenID));
    }else{
      mPromise.reject("No default card set yet");
    }
  }

  @ReactMethod
  public void getPaymentAmount(Promise promise){
    mPromise = promise;
    final TransactionContext transactionContext = getTransactionContext();
    if(transactionContext != null) {
      mPromise.resolve(TransactionContextHelper.formatAmountWithCurrency(transactionContext));
    }else{
      mPromise.reject("error occured");
    }
  }


  @Nullable
  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put("STATE_ON_TRANSACTION_COMPLETED",PaymentState.STATE_ON_TRANSACTION_COMPLETED.name());
    constants.put("STATE_ON_AUTHENTICATION_REQUIRED",PaymentState.STATE_ON_AUTHENTICATION_REQUIRED.name());
    constants.put("STATE_ON_READY_TO_TAP",PaymentState.STATE_ON_READY_TO_TAP.name());
    constants.put("STATE_ON_ERROR",PaymentState.STATE_ON_ERROR.name());

    return constants;
  }

  @Override
  public void onNextTransactionReady(final DeactivationStatus deactivationStatus, final DigitalizedCardStatus digitalizedCardStatus, final DigitalizedCard digitalizedCard) {
    if (digitalizedCard != null && digitalizedCard.getTokenizedCardID() != null) {
      mDigitalizedCard =  DigitalizedCardManager.getDigitalizedCard(digitalizedCard.getTokenizedCardID());
      replenishKeysIfNeeded(false);
    }
  }
  public void getDigitalizedCardState(final AsyncHelperCardState.Delegate delegate) {
    mDigitalizedCard.getCardState(new AsyncHelperCardState(delegate));
  }
  public void replenishKeysIfNeeded(final boolean forcedReplenishment) {
    getDigitalizedCardState(new AsyncHelperCardState.Delegate() {
      @Override
      public void onSuccess(final DigitalizedCardStatus value) {
        if (value.needsReplenishment()) {
          final ProvisioningBusinessService businessService = ProvisioningServiceManager.getProvisioningBusinessService();
          businessService.sendRequestForReplenishment(mDigitalizedCard.getTokenizedCardID(),
                  ThalesPaysdkWrapperModule.getInstance(), forcedReplenishment);
        }
      }
      @Override
      public void onError(final String error) {
        AppLogger.e(TAG, error);
      }
    });
  }






}
