package com.reactnativethalespaysdkwrapper.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.react.BuildConfig;
import com.reactnativethalespaysdkwrapper.app.AppConstants;


public abstract class SharedPreferenceUtils {
    private static SharedPreferences sp;
    private static SharedPreferences.Editor editor;
    private static final String TAG= SharedPreferenceUtils.class.getSimpleName();
    private static final String DEFAULT_CARD_TABLE_NAME= "default_card_table_name";
    private static final String DEFAULT_CARD_KEY= "default_card_key";
    private static final String FIREBASE_TOKEN_KEY= "firebase_token_key";
    private static final String IS_PUSHNOTI_DISABLED = "ispushnoti_disabled";
    private static final String BENCHMARK_FLAVOUR_KEY = "benchmark_flavour_key";
    private static final String WIPE_ALL_NEEDED_KEY = "wipe_all_needed_key";
    private static final String FIREBASE_ID = "firebase_id";
    public static final String SUCCESSFUL_SYNC_TIME = "asdklfjjwenas";
    private static final String SHARED_PREFERENCES_NAME = "jkasdkjdjfok";
    public static final String RANDOM_TIME_SLOT = "oihekfweoi";

    public static final String DEBUG_SUCCESSFUL_SYNC_TIME = "lastTokenSync";
    public static final String DEBUG_RANDOM_TIME_SLOT = "tokenSyncSlot";
    public static final String DEBUG_SHAREDPREF_NAME = "tokenSyncSharedPref";

    public static void saveDefaultCard(Context context, String defaultCardID) {
        AppLogger.d(TAG,"saveDefaultCard" +defaultCardID);
        sp = context.getApplicationContext().getSharedPreferences(DEFAULT_CARD_TABLE_NAME, AppCompatActivity.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(DEFAULT_CARD_KEY, defaultCardID);
        editor.apply();
    }

    public static String getDefaultCard(Context context) {
        AppLogger.d(TAG,"getDefaultCard");
        sp = context.getApplicationContext().getSharedPreferences(DEFAULT_CARD_TABLE_NAME, AppCompatActivity.MODE_PRIVATE);
        String default_card = sp.getString(DEFAULT_CARD_KEY, null);
        AppLogger.d(TAG,"getDefaultCard" +default_card);
        return default_card;
    }

    public static void saveCPSFirebaseToken(Context context, String cpsFirebaseToken) {
        AppLogger.d(TAG,"cpsFirebaseToken" +cpsFirebaseToken);
        sp = context.getApplicationContext().getSharedPreferences(DEFAULT_CARD_TABLE_NAME, AppCompatActivity.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(FIREBASE_TOKEN_KEY, cpsFirebaseToken);
        editor.apply();
    }
    public static String getCPSFirebaseToken(Context context) {
        AppLogger.d(TAG,"getCPSFirebaseToken");
        sp = context.getApplicationContext().getSharedPreferences(DEFAULT_CARD_TABLE_NAME, AppCompatActivity.MODE_PRIVATE);
        String cpsFirebaseToken = sp.getString(FIREBASE_TOKEN_KEY, null);
        AppLogger.d(TAG,"getCPSFirebaseToken" +cpsFirebaseToken);
        return cpsFirebaseToken;
    }


    public static void saveBenchmark(Context context, String benchmark) {
        AppLogger.d(TAG, "saveBenchmark" + benchmark);
        sp = context.getApplicationContext().getSharedPreferences(DEFAULT_CARD_TABLE_NAME, AppCompatActivity.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(BENCHMARK_FLAVOUR_KEY, benchmark);
        editor.apply();
    }

    public static String getBenchmark(Context context) {
        AppLogger.d(TAG, "getBenchmark");
        sp = context.getApplicationContext().getSharedPreferences(DEFAULT_CARD_TABLE_NAME, AppCompatActivity.MODE_PRIVATE);
        String benchmark = sp.getString(BENCHMARK_FLAVOUR_KEY, AppConstants.Schemes.MASTERCARD.name() );
        AppLogger.d(TAG, "getBenchmark" + benchmark);
        return benchmark;
    }

    public static void setNeedWipeAll(Context context, boolean wipeNeeded) {
        AppLogger.d(TAG, "saveNeedWipeAll " + wipeNeeded);
        sp = context.getApplicationContext().getSharedPreferences(WIPE_ALL_NEEDED_KEY, Activity.MODE_PRIVATE);
        editor = sp.edit();
        editor.putBoolean(WIPE_ALL_NEEDED_KEY, wipeNeeded);
        editor.apply();
    }

    public static boolean getNeedWipeAll(Context context) {
        AppLogger.d(TAG, "getNeedWipeAll");
        sp = context.getApplicationContext().getSharedPreferences(WIPE_ALL_NEEDED_KEY, Activity.MODE_PRIVATE);
        boolean wipeNeeded = sp.getBoolean(WIPE_ALL_NEEDED_KEY, false);
        AppLogger.d(TAG, "getNeedWipeAll " + wipeNeeded);
        return wipeNeeded;
    }

    public static void setFirebaseId(Context context, String firebaseId){
        AppLogger.d(TAG, "saveFireBaseId" + firebaseId);
        sp = context.getApplicationContext().getSharedPreferences(FIREBASE_ID, AppCompatActivity.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(FIREBASE_ID, firebaseId);
        editor.apply();
    }
    public static String getFirebaseId(Context context){
        AppLogger.d(TAG, "getFirebaseId");
        sp = context.getApplicationContext().getSharedPreferences(FIREBASE_ID, AppCompatActivity.MODE_PRIVATE);
        String fireBaseId = sp.getString(FIREBASE_ID, "" );
        AppLogger.d(TAG, "getFirebaseId" + fireBaseId);
        return fireBaseId;
    }

    public static void disablePushNoti(Context context){
        AppLogger.d(TAG, "disablePushNoti");
        sp = context.getApplicationContext().getSharedPreferences(IS_PUSHNOTI_DISABLED, AppCompatActivity.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(IS_PUSHNOTI_DISABLED, "true");
        editor.apply();
    }
    public static void clearDisablePushNoti(Context context){
        AppLogger.d(TAG, "clearDisablePushNoti");
        sp = context.getApplicationContext().getSharedPreferences(IS_PUSHNOTI_DISABLED, AppCompatActivity.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(IS_PUSHNOTI_DISABLED, "");
        editor.apply();
    }

    public static void enablePushNoti(Context context){
        AppLogger.i(TAG,"enablePushNoti");
        clearDisablePushNoti(context);
    }

    public static String isPushNotiDisabled(Context context){
        AppLogger.d(TAG, "isPushNotiDisabled");
        sp = context.getApplicationContext().getSharedPreferences(IS_PUSHNOTI_DISABLED, AppCompatActivity.MODE_PRIVATE);
        String isPushNotiDisabled = sp.getString(IS_PUSHNOTI_DISABLED, "" );
        AppLogger.d(TAG, "isPushNotiDisabled" + isPushNotiDisabled);
        return isPushNotiDisabled;
    }

    /**
     * Get the value from Shared preference for a particular Key
     * @param key
     * @return
     */
    public static String getStringValue(final Context context, final String key, String sharedPerfName) throws IllegalArgumentException {

        AppLogger.d(TAG, "Storage.getStringValue");
        AppLogger.d(TAG, "Storage.key" + key);

        if(context==null){

            AppLogger.e(TAG, "context is null");

            throw new IllegalArgumentException("context is null");
        }
        if(key==null){
            AppLogger.e(TAG,"key is null");
            throw new IllegalArgumentException("key is null");
        }
        if(key.isEmpty()){
            AppLogger.e(TAG,"key is empty");
            throw new IllegalArgumentException("key is empty");
        }

        SharedPreferences sharedPreferences= context.getSharedPreferences(sharedPerfName, Context.MODE_PRIVATE);
        String returnValue= sharedPreferences.getString(key, null);
        AppLogger.i(TAG,"Storage.returnValue"+returnValue);
        return returnValue;
    }

    /**
     * Set the value(string) in Shared Preference for a key
     * @param key
     * @param value
     */
    public static void setStringValue(final Context context, final String key, final String value, final String sharedPerfName) throws IllegalArgumentException {
        AppLogger.i(TAG,"Storage.setStringValue");
        AppLogger.i(TAG,"Storage.key"+key);
        AppLogger.i(TAG,"Storage.value"+value);
        if(context==null){
            AppLogger.e(TAG,"context is null");
            throw new IllegalArgumentException("context is null");
        }
        if(key==null){
            AppLogger.e(TAG,"key is null");
            throw new IllegalArgumentException("key is null");
        }
        if(key.isEmpty()){
            AppLogger.e(TAG,"key is empty");
            throw new IllegalArgumentException("key is empty");
        }
        if(value==null){
            AppLogger.e(TAG,"value is null");
            throw new IllegalArgumentException("value is null");
        }
        if(value.isEmpty()){
            AppLogger.e(TAG,"value is empty");
            throw new IllegalArgumentException("value is empty");
        }
        SharedPreferences sharedPreferences= context.getSharedPreferences(sharedPerfName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
        editor.commit();
        AppLogger.i(TAG,"Storage.setStringValue::done");
    }
    /**
     * Wipe the shared preference for a particular key
     */
    public static void removeValue(final Context context, final String key, final String sharedPerfName){
        AppLogger.i(TAG,"Storage.removeValue");
        AppLogger.i(TAG,"Storage.key"+key);
        if(context==null){
            AppLogger.e(TAG,"context is null");
            throw new IllegalArgumentException("context is null");
        }
        if(key==null){
            AppLogger.e(TAG,"key is null");
            throw new IllegalArgumentException("key is null");
        }
        if(key.isEmpty()){
            AppLogger.e(TAG,"key is empty");
            throw new IllegalArgumentException("key is empty");
        }
        SharedPreferences sharedPreferences=context.getSharedPreferences(sharedPerfName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
        editor.commit();
        AppLogger.i(TAG,"Storage.removeValue::Done");

    }

    public static void mockTimeStamp(Context context, String lastSuccessCalendar){
        if (BuildConfig.DEBUG){
            setStringValue(context,DEBUG_SUCCESSFUL_SYNC_TIME,lastSuccessCalendar,DEBUG_SHAREDPREF_NAME);
            String lastSuccessString1 = getStringValue(context, DEBUG_SUCCESSFUL_SYNC_TIME,DEBUG_SHAREDPREF_NAME);
            AppLogger.i(TAG,"DEBUG: mockTimeStamp getTimeInMillis1 "+ lastSuccessString1);
        }else{
            setStringValue(context,SUCCESSFUL_SYNC_TIME, lastSuccessCalendar, SHARED_PREFERENCES_NAME);
            String lastSuccessString1 = getStringValue(context, SUCCESSFUL_SYNC_TIME,SHARED_PREFERENCES_NAME);
            AppLogger.i(TAG,"RELEASE: mockTimeStamp getTimeInMillis1 "+ lastSuccessString1);
        }
    }

    public static String getTimeStamp(Context context){
        if (BuildConfig.DEBUG){
            String lastSuccessString1 = getStringValue(context, DEBUG_SUCCESSFUL_SYNC_TIME,DEBUG_SHAREDPREF_NAME);
            AppLogger.i(TAG,"DEBUG: mockTimeStamp getTimeInMillis1 "+ lastSuccessString1);
            return lastSuccessString1;
        }else{
            String lastSuccessString1 = getStringValue(context, SUCCESSFUL_SYNC_TIME,SHARED_PREFERENCES_NAME);
            AppLogger.i(TAG,"RELEASE: mockTimeStamp getTimeInMillis1 "+ lastSuccessString1);
            return lastSuccessString1;
        }
    }

    public static void removeTimeStamp(Context context){
        if (BuildConfig.DEBUG){
            removeValue(context, DEBUG_SUCCESSFUL_SYNC_TIME, DEBUG_SHAREDPREF_NAME);
        }else{
            removeValue(context, SUCCESSFUL_SYNC_TIME,SHARED_PREFERENCES_NAME);
        }
    }

    public static void mockTimeSlot(Context context, String slot){
        AppLogger.i(TAG,"mockTimeSlot "+ slot);
        if (BuildConfig.DEBUG){
            setStringValue(context, DEBUG_RANDOM_TIME_SLOT, slot, DEBUG_SHAREDPREF_NAME);
            AppLogger.i(TAG,"DEBUG: get mockTimeSlot"+ getStringValue(context, DEBUG_RANDOM_TIME_SLOT,DEBUG_SHAREDPREF_NAME));
        }else{
            setStringValue(context, RANDOM_TIME_SLOT, slot,SHARED_PREFERENCES_NAME);
            AppLogger.i(TAG,"RELEASE: get mockTimeSlot"+ getStringValue(context, RANDOM_TIME_SLOT,SHARED_PREFERENCES_NAME));
        }
    }

    public static String getTimeSlot(Context context){
        if (BuildConfig.DEBUG){
            return getStringValue(context, DEBUG_RANDOM_TIME_SLOT, DEBUG_SHAREDPREF_NAME);
        }else{
            return getStringValue(context, RANDOM_TIME_SLOT,SHARED_PREFERENCES_NAME);
        }
    }

    public static void removeTimeSlot(Context context){
        if (BuildConfig.DEBUG){
            removeValue(context, DEBUG_RANDOM_TIME_SLOT, DEBUG_SHAREDPREF_NAME);
        }else{
            removeValue(context, RANDOM_TIME_SLOT,SHARED_PREFERENCES_NAME);
        }
    }
}
