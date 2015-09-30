package com.ekuater.admaker.ui.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;

import com.ekuater.admaker.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author LinYong
 */
public final class DateTimeUtils {

    public static String getTimeString(Context context, long milliseconds) {
        return getDateString(context, milliseconds, false) + " "
                + new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(milliseconds));
    }

    public static String getDateString(Context context, long milliseconds, boolean showToday) {
        final Calendar cdr = Calendar.getInstance();
        final Resources res = context.getResources();
        final long todayDayIdx;
        final long yesterdayDayIdx;
        final long targetDayIdx;
        final String date;

        todayDayIdx = getDayIdx(cdr);
        yesterdayDayIdx = todayDayIdx - 1;
        cdr.setTimeInMillis(milliseconds);
        targetDayIdx = getDayIdx(cdr);

        if (targetDayIdx == todayDayIdx) { // today
            date = showToday ? res.getString(R.string.today) : "";
        } else if (targetDayIdx == yesterdayDayIdx) {// yesterday
            date = res.getString(R.string.yesterday);
//        } else if (todayDayIdx - 7 <= targetDayIdx && targetDayIdx < yesterdayDayIdx) {
//            date = res.getStringArray(R.array.week_days)[cdr.get(Calendar.DAY_OF_WEEK) - 1];
        } else {
            SimpleDateFormat.getDateInstance();
            date = new SimpleDateFormat("MM月dd日", Locale.getDefault())
                    .format(new Date(milliseconds));
        }

        return date;
    }

    private static long getDayIdx(Calendar cdr) {
        return cdr.get(Calendar.YEAR) * 365 + cdr.get(Calendar.DAY_OF_YEAR);
    }

    private static long getDayHourIdx(Context context, Calendar cdr) {
        ContentResolver cv = context.getContentResolver();
        String strTimeFormat = android.provider.Settings.System.getString(cv,
                android.provider.Settings.System.TIME_12_24);
        if ("24".equals(strTimeFormat)) {
            return cdr.get(Calendar.HOUR_OF_DAY);
        } else {
            if (cdr.get(Calendar.AM_PM) == 0) {
                return cdr.get(Calendar.HOUR);
            } else {
                return cdr.get(Calendar.HOUR) + 12;
            }
        }
    }

    private static long getDayMinuteIdx(Calendar cdr) {
        return cdr.get(Calendar.MINUTE);
    }

    public static String getTimeChangeLines(String time) {
        String changeLinesStr = time;
        String strStars = changeLinesStr.substring(0, 2);
        String strEnds = changeLinesStr.substring(2, changeLinesStr.length());
        if (!strStars.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$")) {
            changeLinesStr = strStars + "\n" + strEnds;
        }
        return changeLinesStr;
    }

    private static final int DAY = 24 * 60 * 60;
    private static final int HOUR = 60 * 60;
    private static final int MINUTE = 60;

    public static String getDescriptionTimeFromTimestamp(Context context, long timestamp) {
        final Calendar cdr = Calendar.getInstance();
        final Resources res = context.getResources();
        final long todayDayIdx;
        final long todayHourIdx;
        final long todayMiuteIdx;
        final long yesterdayDayIdx;
        final long targetDayIdx;
        final long targetDayHourIdx;
        final long targetDayMinuteIdx;
        String timeStr;


        todayDayIdx = getDayIdx(cdr);
        todayHourIdx = getDayHourIdx(context, cdr);
        todayMiuteIdx = getDayMinuteIdx(cdr);
        yesterdayDayIdx = todayDayIdx - 1;
        cdr.setTimeInMillis(timestamp);
        targetDayIdx = getDayIdx(cdr);
        targetDayHourIdx = getDayHourIdx(context, cdr);
        targetDayMinuteIdx = getDayMinuteIdx(cdr);

        if (targetDayIdx == yesterdayDayIdx) {
            timeStr = res.getString(R.string.yesterday);
        } else if (targetDayIdx == todayDayIdx) {
            if (targetDayHourIdx == todayHourIdx) {
                if (targetDayMinuteIdx == todayMiuteIdx) {
                    timeStr = res.getString(R.string.now_befor);
                } else {
                    long target = todayMiuteIdx - targetDayMinuteIdx;
                    timeStr = target > 0 ? target + res.getString(R.string.minute_befor) : res.getString(R.string.now_befor);
                }
            } else {
                timeStr = (todayHourIdx - targetDayHourIdx) + res.getString(R.string.hour_befor);
            }

        } else {
            timeStr = (todayDayIdx - targetDayIdx) + res.getString(R.string.day_befor);
        }
        return timeStr;
    }

    public static String getMessageDateString(Context context, long milliseconds) {
        final Calendar cdr = Calendar.getInstance();
        final Resources res = context.getResources();
        final long todayDayIdx;
        final long yesterdayDayIdx;
        final long yesBeforDayIdx;
        final long targetDayIdx;
        String date = null;

        todayDayIdx = getDayIdx(cdr);
        yesterdayDayIdx = todayDayIdx - 1;
        yesBeforDayIdx = yesterdayDayIdx - 1;
        cdr.setTimeInMillis(milliseconds);
        targetDayIdx = getDayIdx(cdr);

        if (targetDayIdx == todayDayIdx) { // today
            long targ = getDayHourIdx(context, cdr);
            if (targ == 24 || targ < 6) {
                date = res.getString(R.string.before_dawn) + new SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(new Date(milliseconds));
            } else if (targ >= 6 && targ < 12) {
                date = res.getString(R.string.forenoon) + new SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(new Date(milliseconds));
            } else if (targ >= 12 && targ < 18) {
                date = res.getString(R.string.afternoon) + new SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(new Date(milliseconds));
            } else if (targ >= 18 && targ < 24) {
                date = res.getString(R.string.evening) + new SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(new Date(milliseconds));
            }
        } else if (targetDayIdx == yesterdayDayIdx) {// yesterday
            date = res.getString(R.string.yesterday) + new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(new Date(milliseconds));
        } else if (targetDayIdx == yesBeforDayIdx) {
            date = res.getString(R.string.yesterday_befor) + new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(new Date(milliseconds));
        } else {
            SimpleDateFormat.getDateInstance();
            date = new SimpleDateFormat("MM-dd", Locale.getDefault())
                    .format(new Date(milliseconds));
        }

        return date;
    }


}
