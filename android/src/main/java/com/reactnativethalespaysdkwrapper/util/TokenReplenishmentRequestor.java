package com.reactnativethalespaysdkwrapper.util;

import android.os.Bundle;
import android.widget.Toast;

import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardManager;
import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardStatus;
import com.gemalto.mfs.mwsdk.dcm.PaymentType;
import com.gemalto.mfs.mwsdk.provisioning.ProvisioningServiceManager;
import com.gemalto.mfs.mwsdk.provisioning.listener.PushServiceListener;
import com.gemalto.mfs.mwsdk.provisioning.model.ProvisioningServiceError;
import com.gemalto.mfs.mwsdk.provisioning.model.ProvisioningServiceMessage;
import com.gemalto.mfs.mwsdk.provisioning.sdkconfig.ProvisioningBusinessService;
import com.gemalto.mfs.mwsdk.sdkconfig.AndroidContextResolver;
import com.gemalto.mfs.mwsdk.utils.async.AbstractAsyncHandler;
import com.gemalto.mfs.mwsdk.utils.async.AsyncResult;
import com.reactnativethalespaysdkwrapper.R;


public class TokenReplenishmentRequestor {

    private static final String TAG = TokenReplenishmentRequestor.class.getSimpleName();



    /**
     * This method determines is wrapper for checking replenishment is needed or not using transaction context object
     * @param cardStatus
     * @return
     */
    public static boolean needsReplenishment(DigitalizedCardStatus cardStatus) {
        return cardStatus.needsReplenishment();

    }

    /**
     * * This method handles replenishment of tokens of the card after payment.
     *  Both for cases of successful or failed payments.
     *  Works only for default card
     * @param cardStatus
     */
    public static void replenishDefaultCard(final DigitalizedCardStatus cardStatus) {

        boolean needsReplenishment=needsReplenishment(cardStatus);

        //Check if replenishment is needed
        AppLogger.i(TAG, "Replenishment needed: " + needsReplenishment);

        if(needsReplenishment) {
            /*
            For Replenishment after Payment,
            We will be working on the replenishment of the default card only,
            as it will be the one used in the payment.
            Following logic to Get Default Card from wallet:
             *  */

            DigitalizedCardManager.getDefault(PaymentType.CONTACTLESS, new AbstractAsyncHandler<String>() {
                @Override
                public void onComplete(AsyncResult<String> asyncResult) {

                    if(asyncResult.isSuccessful()) {
                        //If result is successful continue to request replenishment
                        //call handle replenishment method with tokenizedCardID of the default card.
                        replenish(cardStatus,asyncResult.getResult());
                    } else {
                        //Error Fetching Default card details
                        AppLogger.e(TAG, "Error fetching Default Card from wallet: " + asyncResult.getErrorCode() + " - " + asyncResult.getErrorMessage() );
                    }
                }
            });
        }
    }

    /**
     * This method handles replenishment of tokens of cards.
     * To be used with following params:
     * @param cardStatus
     * @param tokenizedCardId
     */
    public static void replenish(final DigitalizedCardStatus cardStatus, final String tokenizedCardId) {


        boolean needsReplenishment=needsReplenishment(cardStatus);
        //Check if replenishment is needed
        AppLogger.i(TAG, "Replenishment needed? " + needsReplenishment + "for tokenizedCardId"+tokenizedCardId);

        if (needsReplenishment) {
            replenish(tokenizedCardId);
        } else {
            AppLogger.i(TAG, "Replenishment Not Required for tokenizedCardId: " + tokenizedCardId);
        }


    }

    /**
     * This method handles replenishment of tokens of cards.
     * To be used with following params:
     * @param tokenizedCardId
     */
    public static void forceReplenish(final String tokenizedCardId) {
            replenish(tokenizedCardId,true);
    }


    /**
     * Internal method handles replenishment of tokens of cards.
     * Call this method only when replenishment is needed
     * To be used with following params:
     * @param tokenizedCardId
     */
    private static void replenish(final String tokenizedCardId) {
        replenish(tokenizedCardId,false);


    }
    /**
     * Internal method handles replenishment of tokens of cards.
     * Call this method only when replenishment is needed
     * To be used with following params:
     * @param tokenizedCardId
     */
    private static void replenish(final String tokenizedCardId, boolean isForced) {

        AppLogger.i(TAG, "Initiating Replenishment for tokenizedCardId: " + tokenizedCardId);

            //Initialize ProvisioningBusinessService from ProvisioningServiceManager.
            ProvisioningBusinessService provBs = ProvisioningServiceManager.getProvisioningBusinessService();

            //Initiate Replenishment & setup Push Service Listener callbacks.
            provBs.sendRequestForReplenishment(tokenizedCardId, new PushServiceListener() {
                @Override
                public void onError(ProvisioningServiceError provisioningServiceError) {
                    AppLogger.e(TAG, "Token Replenishment Failed: " + provisioningServiceError.getSdkErrorCode() + " : " + provisioningServiceError.getErrorMessage());
                }

                @Override
                public void onUnsupportedPushContent(Bundle bundle) {
                    AppLogger.d(TAG, "Token Replenishment: Unsupported Push Content");
                }

                @Override
                public void onServerMessage(String s, ProvisioningServiceMessage provisioningServiceMessage) {
                    AppLogger.d(TAG, "Token Replenishment Initiated. " + provisioningServiceMessage.getMsgText());
                }

                @Override
                public void onComplete() {
                    AppLogger.d(TAG, "Token Replenishment Completed.");
                    String toastMessage= AndroidContextResolver.getApplicationContext().getString(R.string.replenish_request)+ ":" +tokenizedCardId;
                    //Toast.makeText(AndroidContextResolver.getApplicationContext(),toastMessage, Toast.LENGTH_SHORT).show();
                }
            },isForced);



    }
}
