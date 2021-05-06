package com.amazonaws.wafv2.commons;

public final class CommonVariables {
    public final static int CALLBACK_DELAY_SECONDS = 10;

    // number of retry = 25 min * 60 secPerMin / CALLBACK_DELAY_SECONDS
    public final static int NUMBER_OF_STATE_POLL_RETRIES = 25 * 60 / CALLBACK_DELAY_SECONDS;
}
