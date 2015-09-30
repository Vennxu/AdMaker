package com.ekuater.admaker.settings;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.ekuater.admaker.BuildConfig;
import com.ekuater.admaker.util.L;

/**
 * @author LinYong
 */
public final class Settings {

    private static final String TAG = Settings.class.getSimpleName();
    private static final boolean LOCAL_LOGV = true;

    static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".settings";
    static final String ACTION_GET = "GET";
    static final String ACTION_PUT = "PUT";

    static String getCallMethod(String table, String action) {
        return table + ":" + action;
    }

    static String getCallMethodTable(String method) {
        String[] args = method.split(":");

        if (args.length == 2) {
            return args[0];
        } else {
            return null;
        }
    }

    static String getCallMethodAction(String method) {
        String[] args = method.split(":");

        if (args.length == 2) {
            return args[1];
        } else {
            return null;
        }
    }

    static Uri getUriForTable(String table) {
        return Uri.parse("content://" + AUTHORITY + "/" + table);
    }

    /**
     * Common base for tables of name/value settings.
     */
    static class NameValueTable implements BaseColumns {

        public static final String NAME = "name";
        public static final String VALUE = "value";
        public static final String[] SELECT_VALUE = new String[]{VALUE};
        public static final String NAME_EQ_PLACEHOLDER = NAME + "=?";

        protected static boolean putStringInternal(ContentResolver cr, Uri uri, String command,
                                                   String name, String value) {
            try {
                Bundle arg = new Bundle();
                arg.putString(NameValueTable.VALUE, value);
                cr.call(uri, command, name, arg);
            } catch (Exception e) {
                L.w(TAG, "Can't set key " + name + " in " + uri, e);
                return false;
            }
            return true;
        }

        protected static String getStringInternal(ContentResolver cr, Uri uri, String command,
                                                  String name) {

            // Try the fast path first, not using query().  If this
            // fails (alternate Settings provider that doesn't support
            // this interface?) then we fall back to the query/table
            // interface.
            if (!TextUtils.isEmpty(command)) {
                Bundle b = cr.call(uri, command, name, null);
                if (b != null) {
                    return b.getString(VALUE);
                }
            }

            Cursor c = null;
            try {
                c = cr.query(uri, SELECT_VALUE, NAME_EQ_PLACEHOLDER,
                        new String[]{name}, null);
                if (c == null) {
                    L.w(TAG, "Can't get key " + name + " from " + uri);
                    return null;
                }

                String value = c.moveToNext() ? c.getString(0) : null;
                if (LOCAL_LOGV) {
                    L.v(TAG, "cache miss [" + uri.getLastPathSegment() + "]: " +
                            name + " = " + (value == null ? "(null)" : value));
                }
                return value;
            } catch (Exception e) {
                L.w(TAG, "Can't get key " + name + " from " + uri, e);
                return null;  // Return null, but don't cache it.
            } finally {
                if (c != null) c.close();
            }
        }

        protected static Uri getUriFor(Uri uri, String name) {
            return Uri.withAppendedPath(uri, name);
        }
    }

    public static final class Global extends NameValueTable {

        private static final String TABLE = "global";
        private static final Uri CONTENT_URI = getUriForTable(TABLE);
        private static final String GET_COMMAND = getCallMethod(TABLE, ACTION_GET);
        private static final String PUT_COMMAND = getCallMethod(TABLE, ACTION_PUT);

        /**
         * Look up a name in the database.
         *
         * @param resolver to access the database with
         * @param name     to look up in the table
         * @return the corresponding value, or null if not present
         */
        public static String getString(ContentResolver resolver, String name) {
            return getStringInternal(resolver, CONTENT_URI, GET_COMMAND, name);
        }

        /**
         * Store a name/value pair into the database.
         *
         * @param resolver to access the database with
         * @param name     to store
         * @param value    to associate with the name
         * @return true if the value was set, false on database errors
         */
        public static boolean putString(ContentResolver resolver, String name, String value) {
            return putStringInternal(resolver, CONTENT_URI, PUT_COMMAND, name, value);
        }

        /**
         * Convenience function for retrieving a single system settings value
         * as an integer.  Note that internally setting values are always
         * stored as strings; this function converts the string to an integer
         * for you.  The default value will be returned if the setting is
         * not defined or not an integer.
         *
         * @param cr   The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         * @param def  Value to return if the setting is not defined.
         * @return The setting's current value, or 'def' if it is not defined
         * or not a valid integer.
         */
        public static int getInt(ContentResolver cr, String name, int def) {
            String v = getString(cr, name);
            try {
                return v != null ? Integer.parseInt(v) : def;
            } catch (Exception e) {
                return def;
            }
        }

        /**
         * Convenience function for updating a single settings value as an
         * integer. This will either create a new entry in the table if the
         * given name does not exist, or modify the value of the existing row
         * with that name.  Note that internally setting values are always
         * stored as strings, so this function converts the given value to a
         * string before storing it.
         *
         * @param cr    The ContentResolver to access.
         * @param name  The name of the setting to modify.
         * @param value The new value for the setting.
         * @return true if the value was set, false on database errors
         */
        public static boolean putInt(ContentResolver cr, String name, int value) {
            return putString(cr, name, Integer.toString(value));
        }

        /**
         * Convenience function for retrieving a single system settings value
         * as a {@code long}.  Note that internally setting values are always
         * stored as strings; this function converts the string to a {@code long}
         * for you.  The default value will be returned if the setting is
         * not defined or not a {@code long}.
         *
         * @param cr   The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         * @param def  Value to return if the setting is not defined.
         * @return The setting's current value, or 'def' if it is not defined
         * or not a valid {@code long}.
         */
        public static long getLong(ContentResolver cr, String name, long def) {
            String v = getString(cr, name);
            try {
                return v != null ? Long.parseLong(v) : def;
            } catch (Exception e) {
                return def;
            }
        }

        /**
         * Convenience function for updating a single settings value as a long
         * integer. This will either create a new entry in the table if the
         * given name does not exist, or modify the value of the existing row
         * with that name.  Note that internally setting values are always
         * stored as strings, so this function converts the given value to a
         * string before storing it.
         *
         * @param cr    The ContentResolver to access.
         * @param name  The name of the setting to modify.
         * @param value The new value for the setting.
         * @return true if the value was set, false on database errors
         */
        public static boolean putLong(ContentResolver cr, String name, long value) {
            return putString(cr, name, Long.toString(value));
        }

        /**
         * Convenience function for retrieving a single system settings value
         * as a floating point number.  Note that internally setting values are
         * always stored as strings; this function converts the string to an
         * float for you. The default value will be returned if the setting
         * is not defined or not a valid float.
         *
         * @param cr   The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         * @param def  Value to return if the setting is not defined.
         * @return The setting's current value, or 'def' if it is not defined
         * or not a valid float.
         */
        public static float getFloat(ContentResolver cr, String name, float def) {
            String v = getString(cr, name);
            try {
                return v != null ? Float.parseFloat(v) : def;
            } catch (Exception e) {
                return def;
            }
        }

        /**
         * Convenience function for updating a single settings value as a
         * floating point number. This will either create a new entry in the
         * table if the given name does not exist, or modify the value of the
         * existing row with that name.  Note that internally setting values
         * are always stored as strings, so this function converts the given
         * value to a string before storing it.
         *
         * @param cr    The ContentResolver to access.
         * @param name  The name of the setting to modify.
         * @param value The new value for the setting.
         * @return true if the value was set, false on database errors
         */
        public static boolean putFloat(ContentResolver cr, String name, float value) {
            return putString(cr, name, Float.toString(value));
        }

        /**
         * Construct the content URI for a particular name/value pair,
         * useful for monitoring changes with a ContentObserver.
         *
         * @param name to look up in the table
         * @return the corresponding content URI, or null if not present
         */
        public static Uri getUriFor(String name) {
            return getUriFor(CONTENT_URI, name);
        }

        public static void clear(ContentResolver cr) {
            cr.delete(CONTENT_URI, null, null);
        }
    }

    public static final class System extends NameValueTable {

        private static final String TABLE = "system";
        private static final Uri CONTENT_URI = getUriForTable(TABLE);
        private static final String GET_COMMAND = getCallMethod(TABLE, ACTION_GET);
        private static final String PUT_COMMAND = getCallMethod(TABLE, ACTION_PUT);

        public static String getString(ContentResolver resolver, String name) {
            return getStringInternal(resolver, CONTENT_URI, GET_COMMAND, name);
        }

        public static boolean putString(ContentResolver resolver, String name, String value) {
            return putStringInternal(resolver, CONTENT_URI, PUT_COMMAND, name, value);
        }

        public static int getInt(ContentResolver cr, String name, int def) {
            String v = getString(cr, name);
            try {
                return v != null ? Integer.parseInt(v) : def;
            } catch (Exception e) {
                return def;
            }
        }

        public static boolean putInt(ContentResolver cr, String name, int value) {
            return putString(cr, name, Integer.toString(value));
        }

        public static long getLong(ContentResolver cr, String name, long def) {
            String v = getString(cr, name);
            try {
                return v != null ? Long.parseLong(v) : def;
            } catch (Exception e) {
                return def;
            }
        }

        public static boolean putLong(ContentResolver cr, String name, long value) {
            return putString(cr, name, Long.toString(value));
        }

        public static float getFloat(ContentResolver cr, String name, float def) {
            String v = getString(cr, name);
            try {
                return v != null ? Float.parseFloat(v) : def;
            } catch (Exception e) {
                return def;
            }
        }

        public static boolean putFloat(ContentResolver cr, String name, float value) {
            return putString(cr, name, Float.toString(value));
        }

        public static Uri getUriFor(String name) {
            return getUriFor(CONTENT_URI, name);
        }

        public static void clear(ContentResolver cr) {
            cr.delete(CONTENT_URI, null, null);
        }
    }

    public static final class Personal extends NameValueTable {

        private static final String TABLE = "personal";
        private static final Uri CONTENT_URI = getUriForTable(TABLE);
        private static final String GET_COMMAND = getCallMethod(TABLE, ACTION_GET);
        private static final String PUT_COMMAND = getCallMethod(TABLE, ACTION_PUT);

        public static String getString(ContentResolver resolver, String name) {
            return getStringInternal(resolver, CONTENT_URI, GET_COMMAND, name);
        }

        public static boolean putString(ContentResolver resolver, String name, String value) {
            return putStringInternal(resolver, CONTENT_URI, PUT_COMMAND, name, value);
        }

        public static int getInt(ContentResolver cr, String name, int def) {
            String v = getString(cr, name);
            try {
                return v != null ? Integer.parseInt(v) : def;
            } catch (Exception e) {
                return def;
            }
        }

        public static boolean putInt(ContentResolver cr, String name, int value) {
            return putString(cr, name, Integer.toString(value));
        }

        public static long getLong(ContentResolver cr, String name, long def) {
            String v = getString(cr, name);
            try {
                return v != null ? Long.parseLong(v) : def;
            } catch (Exception e) {
                return def;
            }
        }

        public static boolean putLong(ContentResolver cr, String name, long value) {
            return putString(cr, name, Long.toString(value));
        }

        public static float getFloat(ContentResolver cr, String name, float def) {
            String v = getString(cr, name);
            try {
                return v != null ? Float.parseFloat(v) : def;
            } catch (Exception e) {
                return def;
            }
        }

        public static boolean putFloat(ContentResolver cr, String name, float value) {
            return putString(cr, name, Float.toString(value));
        }

        public static boolean putBoolean(ContentResolver cr, String name, boolean def) {
            return putString(cr, name, Boolean.toString(def));
        }

        public static boolean getBoolean(ContentResolver cr, String name, boolean def) {
            String v = getString(cr, name);
            try {
                return v != null ? Boolean.parseBoolean(v) : def;
            } catch (Exception e) {
                return def;
            }
        }

        public static Uri getUriFor(String name) {
            return getUriFor(CONTENT_URI, name);
        }

        public static void clear(ContentResolver cr) {
            cr.delete(CONTENT_URI, null, null);
        }
    }

    public static final class Volatile extends NameValueTable {

        private static final String TABLE = "volatile";
        private static final Uri CONTENT_URI = getUriForTable(TABLE);
        private static final String GET_COMMAND = getCallMethod(TABLE, ACTION_GET);
        private static final String PUT_COMMAND = getCallMethod(TABLE, ACTION_PUT);

        public static String getString(ContentResolver resolver, String name) {
            return getStringInternal(resolver, CONTENT_URI, GET_COMMAND, name);
        }

        public static boolean putString(ContentResolver resolver, String name, String value) {
            return putStringInternal(resolver, CONTENT_URI, PUT_COMMAND, name, value);
        }

        public static int getInt(ContentResolver cr, String name, int def) {
            String v = getString(cr, name);
            try {
                return v != null ? Integer.parseInt(v) : def;
            } catch (Exception e) {
                return def;
            }
        }

        public static boolean putInt(ContentResolver cr, String name, int value) {
            return putString(cr, name, Integer.toString(value));
        }

        public static long getLong(ContentResolver cr, String name, long def) {
            String v = getString(cr, name);
            try {
                return v != null ? Long.parseLong(v) : def;
            } catch (Exception e) {
                return def;
            }
        }

        public static boolean putLong(ContentResolver cr, String name, long value) {
            return putString(cr, name, Long.toString(value));
        }

        public static float getFloat(ContentResolver cr, String name, float def) {
            String v = getString(cr, name);
            try {
                return v != null ? Float.parseFloat(v) : def;
            } catch (Exception e) {
                return def;
            }
        }

        public static boolean putFloat(ContentResolver cr, String name, float value) {
            return putString(cr, name, Float.toString(value));
        }

        public static boolean putBoolean(ContentResolver cr, String name, boolean def) {
            return putString(cr, name, Boolean.toString(def));
        }

        public static boolean getBoolean(ContentResolver cr, String name, boolean def) {
            String v = getString(cr, name);
            try {
                return v != null ? Boolean.parseBoolean(v) : def;
            } catch (Exception e) {
                return def;
            }
        }

        public static Uri getUriFor(String name) {
            return getUriFor(CONTENT_URI, name);
        }

        public static void clear(ContentResolver cr) {
            cr.delete(CONTENT_URI, null, null);
        }
    }
}
