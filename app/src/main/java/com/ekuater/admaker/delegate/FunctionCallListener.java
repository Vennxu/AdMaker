package com.ekuater.admaker.delegate;

/**
 * Function call result listener
 *
 * @author LinYong
 */
public interface FunctionCallListener {

    public static final int RESULT_CALL_SUCCESS = 0;
    public static final int RESULT_NETWORK_ERROR = 1;
    public static final int RESULT_CALL_FAILED = 2;
    public static final int RESULT_RESPONSE_ERROR = 3;
    public static final int RESULT_ILLEGAL_ARGUMENT = 4;
    public static final int RESULT_UNKNOWN_ERROR = 100;

    public void onCallResult(int result, int errorCode, String errorDesc);
}
