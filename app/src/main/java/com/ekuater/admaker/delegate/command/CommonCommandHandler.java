package com.ekuater.admaker.delegate.command;

import com.ekuater.admaker.command.BaseCommand;
import com.ekuater.admaker.command.CommandErrorCode;
import com.ekuater.admaker.datastruct.ConstantCode;
import com.ekuater.admaker.delegate.FunctionCallListener;
import com.ekuater.admaker.util.JsonUtils;
import com.ekuater.admaker.util.L;

/**
 * Created by Leo on 2015/1/26.
 *
 * @author LinYong
 */
/*package*/ class CommonCommandHandler implements ICommandHandler {

    private static final String TAG = CommonCommandHandler.class.getSimpleName();

    private final FunctionCallListener listener;

    public CommonCommandHandler(FunctionCallListener listener) {
        this.listener = listener;
    }

    @Override
    public void onResponse(int result, String response) {
        if (listener == null) {
            // no available listener, so do not need to care about the
            // response result.
            return;
        }

        int callResult = FunctionCallListener.RESULT_UNKNOWN_ERROR;
        int errorCode = CommandErrorCode.EXECUTE_FAILED;
        String errorDesc = null;

        switch (result) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS: {
                try {
                    BaseCommand.Response cmdResp = JsonUtils.fromJson(response,
                            BaseCommand.Response.class);
                    if (cmdResp != null) {
                        if (cmdResp.requestSuccess()) {
                            callResult = FunctionCallListener.RESULT_CALL_SUCCESS;
                        } else {
                            callResult = FunctionCallListener.RESULT_CALL_FAILED;
                        }
                        errorCode = cmdResp.getCode();
                        errorDesc = cmdResp.getDesc();
                    }
                } catch (Exception e) {
                    L.w(TAG, e);
                    callResult = FunctionCallListener.RESULT_RESPONSE_ERROR;
                }
                break;
            }
            case ConstantCode.EXECUTE_RESULT_NETWORK_ERROR:
                callResult = FunctionCallListener.RESULT_NETWORK_ERROR;
                break;
            default:
                break;
        }
        listener.onCallResult(callResult, errorCode, errorDesc);
    }
}
