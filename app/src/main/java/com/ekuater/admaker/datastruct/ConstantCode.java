
package com.ekuater.admaker.datastruct;

import com.ekuater.admaker.R;

/**
 * @author LinYong
 */
public final class ConstantCode {

    // Request command request method
    // Always user POST now
    public static final int REQUEST_GET = 0;
    public static final int REQUEST_POST = 1;
    public static final int REQUEST_PUT = 2;
    public static final int REQUEST_DELETE = 3;

    // for user sex enum
    public static final int USER_SEX_UNKNOWN = 0;
    public static final int USER_SEX_MALE = 1;
    public static final int USER_SEX_FEMALE = 2;
    public static final int USER_SEX_SECRECY = 3;

    public static int getSexImageResource(int sex) {
        int resId;

        switch (sex) {
            case USER_SEX_UNKNOWN:
                resId = R.drawable.icon_male;
                break;
            case USER_SEX_MALE:
                resId = R.drawable.icon_male;
                break;
            case USER_SEX_FEMALE:
                resId = R.drawable.icon_female;
                break;
            case USER_SEX_SECRECY:
                resId = R.drawable.icon_male;
                break;
            default:
                resId = R.drawable.icon_male;
                break;
        }

        return resId;
    }

    // for auth type, normal or oauth
    public static final int AUTH_TYPE_NORMAL = 0;
    public static final int AUTH_TYPE_OAUTH = 1;
    // for OAuth platform
    public static final String OAUTH_PLATFORM_QQ = "QQ";
    public static final String OAUTH_PLATFORM_SINA_WEIBO = "SinaWeibo";
    public static final String OAUTH_PLATFORM_WEIXIN = "WeiXin";

    // command execute code
    public static final int EXECUTE_RESULT_SUCCESS = 0;
    public static final int EXECUTE_RESULT_EMPTY_CMD = 1;
    public static final int EXECUTE_RESULT_EMPTY_PARAM = 2;
    public static final int EXECUTE_RESULT_NETWORK_ERROR = 3;
}
