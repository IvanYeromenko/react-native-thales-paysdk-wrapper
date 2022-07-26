package com.reactnativethalespaysdkwrapper.app;

public interface AppBuildConfigurations {

    boolean IS_PFP_ENABLED = true;
    boolean IS_ALLOW_PAYMENT_WHEN_SCREEN_OFF = true;
    boolean IS_LOG_ENABLED = true;
    boolean USE_TIMEOUT_AFTER_FIRST_TAP = false;
    boolean IS_BENCHMARK_LOG_NEEDED = true;
    boolean USE_SECURE_KEYPAD = false;
    boolean WEB_UI_CONFIG = true;

    /**
     * Values related to one tap payment
     */
    boolean IS_ONETAP_ENABLED=false;
    int INIT_MG_SDK_DELAY=400;
    //TODO put back to 400 or 500 ms after benchmarking


}
