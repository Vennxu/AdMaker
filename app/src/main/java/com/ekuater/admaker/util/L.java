
package com.ekuater.admaker.util;

import android.util.Log;

/**
 * Log util
 *
 * @author LinYong
 */
public class L {

    private static final String LOG_FORMAT = "%1$s\n%2$s";
    private static volatile boolean writeDebugLogs = true;
    private static volatile boolean writeLogs = true;

    public static void writeDebugLogs(boolean writeDebugLogs) {
        L.writeDebugLogs = writeDebugLogs;
    }

    public static void writeLogs(boolean writeLogs) {
        L.writeLogs = writeLogs;
    }

    public static void d(String tag, String message, Object... args) {
        if (writeDebugLogs) {
            log(Log.DEBUG, tag, null, message, args);
        }
    }

    public static void v(String tag, String message, Object... args) {
        log(Log.VERBOSE, tag, null, message, args);
    }

    public static void i(String tag, String message, Object... args) {
        log(Log.INFO, tag, null, message, args);
    }

    public static void w(String tag, String message, Object... args) {
        log(Log.WARN, tag, null, message, args);
    }

    public static void w(String tag, Throwable ex) {
        log(Log.WARN, tag, ex, null);
    }

    public static void e(String tag, Throwable ex) {
        log(Log.ERROR, tag, ex, null);
    }

    public static void e(String tag, String message, Object... args) {
        log(Log.ERROR, tag, null, message, args);
    }

    public static void e(String tag, Throwable ex, String message, Object... args) {
        log(Log.ERROR, tag, ex, message, args);
    }

    private static void log(int priority, String tag, Throwable ex,
                            String message, Object... args) {
        if (!writeLogs) return;
        if (args.length > 0) {
            message = String.format(message, args);
        }

        String log;
        if (ex == null) {
            log = message;
        } else {
            String logMessage = message == null ? ex.getMessage() : message;
            String logBody = Log.getStackTraceString(ex);
            log = String.format(LOG_FORMAT, logMessage, logBody);
        }
        Log.println(priority, tag, log);
    }
}
