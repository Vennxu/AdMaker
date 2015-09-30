package com.ekuater.admaker.settings;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.LruCache;

import com.ekuater.admaker.util.L;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author LinYong
 */
public class SettingsProvider extends ContentProvider {

    private static final String TAG = SettingsProvider.class.getSimpleName();
    private static final boolean LOCAL_LOGV = false;

    public static final String _ID = DatabaseHelper._ID;
    public static final String NAME = DatabaseHelper.NAME;
    public static final String VALUE = DatabaseHelper.VALUE;

    private static final String[] COLUMN_VALUE = new String[]{
            VALUE
    };

    // Caches for each user's settings, access-ordered for acting as LRU.
    // Guarded by themselves.
    private static final int MAX_CACHE_ENTRIES = 200;

    // The count of how many known (handled by SettingsProvider)
    // database mutations are currently being handled for this user.
    // Used by file observers to not reload the database when it's ourselves
    // modifying it.
    private static final AtomicInteger sKnownMutationsInFlight = new AtomicInteger(0);

    // Over this size we don't reject loading or saving settings but
    // we do consider them broken/malicious and don't keep them in
    // memory at least:
    private static final int MAX_CACHE_ENTRY_SIZE = 500;

    private static final Bundle NULL_SETTING = forPair("value", null);

    // Used as a sentinel value in an instance equality test when we
    // want to cache the existence of a key, but not store its value.
    private static final Bundle TOO_LARGE_TO_CACHE_MARKER = forPair("_dummy", null);

    private static Bundle forPair(String key, String value) {
        Bundle b = new Bundle(1);
        b.putString(key, value);
        return b;
    }

    /**
     * Decode a content URL into the table, projection, and arguments used to
     * access the corresponding database rows.
     */
    private static class SqlArguments {
        public String table;
        public final String where;
        public final String[] args;

        /**
         * Operate on existing rows.
         */
        public SqlArguments(DatabaseHelper helper, Uri uri, String where, String[] args) {
            if (uri.getPathSegments().size() == 1) {
                // of the form content://settings/secure, arbitrary where clause
                this.table = uri.getPathSegments().get(0);
                if (!helper.isValidTable(this.table)) {
                    throw new IllegalArgumentException("Bad root path: " + this.table);
                }
                this.where = where;
                this.args = args;
            } else if (uri.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + uri);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + uri);
            } else {
                // of the form content://settings/secure/element_name, no where
                // clause
                this.table = uri.getPathSegments().get(0);
                if (!helper.isValidTable(this.table)) {
                    throw new IllegalArgumentException("Bad root path: " + this.table);
                }

                this.where = NAME + "=?";
                final String name = uri.getPathSegments().get(1);
                this.args = new String[]{name};
            }
        }

        /**
         * Insert new rows (no where clause allowed).
         */
        public SqlArguments(DatabaseHelper helper, Uri uri) {
            if (uri.getPathSegments().size() == 1) {
                this.table = uri.getPathSegments().get(0);
                if (!helper.isValidTable(this.table)) {
                    throw new IllegalArgumentException("Bad root path: " + this.table);
                }
                this.where = null;
                this.args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + uri);
            }
        }
    }

    private DatabaseHelper mOpenHelper;
    private final Map<String, SettingsCache> mTableCaches = new HashMap<>();

    @Override
    public boolean onCreate() {
        establishDbTracking();
        return true;
    }

    private void establishDbTracking() {
        mOpenHelper = new DatabaseHelper(getContext());
        for (String table : mOpenHelper.getValidTables()) {
            mTableCaches.put(table, new SettingsCache());
        }
        startAsyncCachePopulation();
    }

    /**
     * Get the content URI of a row added to a table.
     *
     * @param tableUri of the entire table
     * @param values   found in the row
     * @param rowId    of the row
     * @return the content URI for this particular row
     */
    private Uri getUriFor(Uri tableUri, ContentValues values, long rowId) {
        if (tableUri.getPathSegments().size() != 1) {
            throw new IllegalArgumentException("Invalid URI: " + tableUri);
        }
        String table = tableUri.getPathSegments().get(0);
        if (mOpenHelper.isValidTable(table)) {
            String name = values.getAsString(NAME);
            return Uri.withAppendedPath(tableUri, name);
        } else {
            return ContentUris.withAppendedId(tableUri, rowId);
        }
    }

    /**
     * Send a notification when a particular content URI changes. Modify the
     * system property used to communicate the version of this table, for tables
     * which have such a property. (The Settings contract class uses these to
     * provide client-side caches.)
     *
     * @param uri to send notifications for
     */
    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter("notify");
        if (notify == null || "true".equals(notify)) {
            final long oldId = Binder.clearCallingIdentity();
            try {
                getContext().getContentResolver().notifyChange(uri, null, true);
            } finally {
                Binder.restoreCallingIdentity(oldId);
            }
        }
    }

    private class CachePrefetchThread extends Thread {

        CachePrefetchThread() {
            super("populate-settings-caches");
        }

        @Override
        public void run() {
            fullyPopulateCaches();
        }
    }

    private void startAsyncCachePopulation() {
        new CachePrefetchThread().start();
    }

    private void fullyPopulateCaches() {
        for (String table : mTableCaches.keySet()) {
            fullyPopulateCache(mOpenHelper, table, mTableCaches.get(table));
        }
    }

    // Slurp all values (if sane in number & size) into cache.
    private void fullyPopulateCache(DatabaseHelper dbHelper, String table, SettingsCache cache) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                table,
                new String[]{NAME, VALUE},
                null, null, null, null, null,
                "" + (MAX_CACHE_ENTRIES + 1) /* limit */);
        try {
            synchronized (cache) {
                cache.evictAll();
                cache.setFullyMatchesDisk(true); // optimistic
                int rows = 0;
                while (c.moveToNext()) {
                    rows++;
                    String name = c.getString(0);
                    String value = c.getString(1);
                    cache.populate(name, value);
                }
                if (rows > MAX_CACHE_ENTRIES) {
                    // Somewhat redundant, as removeEldestEntry() will
                    // have already done this, but to be explicit:
                    cache.setFullyMatchesDisk(false);
                    L.v(TAG, "row count exceeds max cache entries for table " + table);
                }
                if (LOCAL_LOGV) {
                    L.d(TAG, "cache for settings table '" + table
                            + "' rows=" + rows + "; fully cached=" + cache.fullyMatchesDisk());
                }
            }
        } finally {
            c.close();
        }
    }

    private SettingsCache cacheForTable(String tableName) {
        return mTableCaches.get(tableName);
    }

    /**
     * Used for wiping a whole cache on deletes when we're not sure what exactly
     * was deleted or changed.
     */
    private void invalidateCache(String tableName) {
        SettingsCache cache = cacheForTable(tableName);
        if (cache == null) {
            return;
        }
        synchronized (cache) {
            cache.evictAll();
            cache.mCacheFullyMatchesDisk = false;
        }
    }

    /**
     * Fast path that avoids the use of chatty remoted Cursors.
     */
    @Override
    public Bundle call(String method, String request, Bundle args) {
        final String table = Settings.getCallMethodTable(method);
        final String action = Settings.getCallMethodAction(method);

        L.v(TAG, "call(), method=%1$s, request=%2$s, args=%3$s", method, request, args);

        if (!mOpenHelper.isValidTable(table)) {
            return null;
        } else if (Settings.ACTION_GET.equals(action)) {
            final SettingsCache cache = cacheForTable(table);
            return lookupValue(table, cache, request);
        } else if (Settings.ACTION_PUT.equals(action)) {
            final String newValue = (args == null) ? null : args.getString(VALUE);
            final ContentValues values = new ContentValues();
            final Uri uri = Settings.getUriForTable(table);

            values.put(NAME, request);
            values.put(VALUE, newValue);
            insertInternal(uri, values);
            return args;
        } else {
            return null;
        }
    }

    // Looks up value 'key' in 'table' and returns either a single-pair Bundle,
    // possibly with a null value, or null on failure.
    private Bundle lookupValue(String table, final SettingsCache cache, String key) {
        if (cache == null) {
            L.e(TAG, "cache is null for user: key=" + key);
            return null;
        }
        synchronized (cache) {
            Bundle value = cache.get(key);
            if (value != null) {
                if (value != TOO_LARGE_TO_CACHE_MARKER) {
                    return value;
                }
                // else we fall through and read the value from disk
            } else if (cache.fullyMatchesDisk()) {
                // Fast path (very common). Don't even try touch disk
                // if we know we've slurped it all in. Trying to
                // touch the disk would mean waiting for yaffs2 to
                // give us access, which could takes hundreds of
                // milliseconds. And we're very likely being called
                // from somebody's UI thread...
                return NULL_SETTING;
            }
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(table, COLUMN_VALUE, NAME + "=?", new String[]{
                            key
                    },
                    null, null, null, null);
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                return cache.putIfAbsent(key, cursor.getString(0));
            }
        } catch (SQLiteException e) {
            L.w(TAG, "settings lookup error", e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        cache.putIfAbsent(key, null);
        return NULL_SETTING;
    }

    @Override
    public Cursor query(Uri url, String[] select, String where, String[] whereArgs, String sort) {
        return queryInternal(url, select, where, whereArgs, sort);
    }

    private Cursor queryInternal(Uri url, String[] select, String where, String[] whereArgs,
                                 String sort) {
        SqlArguments args = new SqlArguments(mOpenHelper, url, where, whereArgs);
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);

        Cursor ret = qb.query(db, select, args.where, args.args, null, null, sort);
        // the default Cursor interface does not support per-user observation
        try {
            AbstractCursor c = (AbstractCursor) ret;
            c.setNotificationUri(getContext().getContentResolver(), url);
        } catch (ClassCastException e) {
            // details of the concrete Cursor implementation have changed and
            // this code has
            // not been updated to match -- complain and fail hard.
            L.e(TAG, "Incompatible cursor derivation!");
            throw e;
        }
        return ret;
    }

    @Override
    public String getType(Uri url) {
        // If SqlArguments supplies a where clause, then it must be an item
        // (because we aren't supplying our own where clause).
        SqlArguments args = new SqlArguments(mOpenHelper, url, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        } else {
            return "vnd.android.cursor.item/" + args.table;
        }
    }

    @Override
    public int bulkInsert(Uri uri, @NonNull ContentValues[] values) {
        SqlArguments args = new SqlArguments(mOpenHelper, uri);
        SettingsCache cache = cacheForTable(args.table);

        final AtomicInteger mutationCount = sKnownMutationsInFlight;
        mutationCount.incrementAndGet();
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (ContentValues value : values) {
                if (db.insert(args.table, null, value) < 0) {
                    return 0;
                }
                SettingsCache.populate(cache, value);
                if (LOCAL_LOGV) {
                    L.v(TAG, args.table + " <- " + value);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            mutationCount.decrementAndGet();
        }

        sendNotify(uri);
        return values.length;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        return insertInternal(uri, initialValues);
    }

    // Settings.put*ForUser() always winds up here, so this is where we apply
    // policy around permission to write settings for other users.
    private Uri insertInternal(Uri uri, ContentValues initialValues) {
        SqlArguments args = new SqlArguments(mOpenHelper, uri);
        String name = initialValues.getAsString(NAME);

        SettingsCache cache = cacheForTable(args.table);
        String value = initialValues.getAsString(VALUE);
        if (SettingsCache.isRedundantSetValue(cache, name, value)) {
            return Uri.withAppendedPath(uri, name);
        }

        final AtomicInteger mutationCount = sKnownMutationsInFlight;
        mutationCount.incrementAndGet();
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final long rowId = db.insert(args.table, null, initialValues);
        mutationCount.decrementAndGet();
        if (rowId <= 0) {
            return null;
        }

        SettingsCache.populate(cache, initialValues); // before we notify
        // Note that we use the original url here, not the potentially-rewritten
        // table name
        uri = getUriFor(uri, initialValues, rowId);
        sendNotify(uri);
        return uri;
    }

    @Override
    public int delete(Uri url, String where, String[] whereArgs) {
        SqlArguments args = new SqlArguments(mOpenHelper, url, where, whereArgs);
        final AtomicInteger mutationCount = sKnownMutationsInFlight;
        mutationCount.incrementAndGet();
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(args.table, args.where, args.args);
        mutationCount.decrementAndGet();
        if (count > 0) {
            invalidateCache(args.table); // before we notify
            sendNotify(url);
        }
        startAsyncCachePopulation();
        if (LOCAL_LOGV) {
            L.v(TAG, args.table + ": " + count + " row(s) deleted");
        }
        return count;
    }

    @Override
    public int update(Uri url, ContentValues initialValues, String where, String[] whereArgs) {
        // NOTE: update() is never called by the front-end Settings API, and
        // updates that
        // wind up affecting rows in Secure that are globally shared will not
        // have the
        // intended effect (the update will be invisible to the rest of the
        // system).
        // This should have no practical effect, since writes to the Secure db
        // can only
        // be done by system code, and that code should be using the correct API
        // up front.
        SqlArguments args = new SqlArguments(mOpenHelper, url, where, whereArgs);

        final AtomicInteger mutationCount = sKnownMutationsInFlight;
        mutationCount.incrementAndGet();
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.update(args.table, initialValues, args.where, args.args);
        mutationCount.decrementAndGet();
        if (count > 0) {
            invalidateCache(args.table); // before we notify
            sendNotify(url);
        }
        startAsyncCachePopulation();
        if (LOCAL_LOGV) {
            L.v(TAG, args.table + ": " + count + " row(s) <- " + initialValues);
        }
        return count;
    }

    /**
     * In-memory LRU Cache of system and secure settings, along with associated
     * helper functions to keep cache coherent with the database.
     */
    private static final class SettingsCache extends LruCache<String, Bundle> {

        private boolean mCacheFullyMatchesDisk = false; // has the whole
        // database slurped.

        public SettingsCache() {
            super(MAX_CACHE_ENTRIES);
        }

        /**
         * Is the whole database table slurped into this cache?
         */
        public boolean fullyMatchesDisk() {
            synchronized (this) {
                return mCacheFullyMatchesDisk;
            }
        }

        public void setFullyMatchesDisk(boolean value) {
            synchronized (this) {
                mCacheFullyMatchesDisk = value;
            }
        }

        @Override
        protected void entryRemoved(boolean evicted, String key, Bundle oldValue, Bundle newValue) {
            if (evicted) {
                mCacheFullyMatchesDisk = false;
            }
        }

        /**
         * Atomic cache population, conditional on size of value and if we lost
         * a race.
         *
         * @return a Bundle to send back to the client from call(), even if we
         * lost the race.
         */
        public Bundle putIfAbsent(String key, String value) {
            Bundle bundle = (value == null) ? NULL_SETTING : forPair("value", value);
            if (value == null || value.length() <= MAX_CACHE_ENTRY_SIZE) {
                synchronized (this) {
                    if (get(key) == null) {
                        put(key, bundle);
                    }
                }
            }
            return bundle;
        }

        /**
         * Populates a key in a given (possibly-null) cache.
         */
        public static void populate(SettingsCache cache, ContentValues contentValues) {
            if (cache == null) {
                return;
            }
            String name = contentValues.getAsString(NAME);
            if (name == null) {
                L.w(TAG, "null name populating settings cache.");
                return;
            }
            String value = contentValues.getAsString(VALUE);
            cache.populate(name, value);
        }

        public void populate(String name, String value) {
            synchronized (this) {
                if (value == null || value.length() <= MAX_CACHE_ENTRY_SIZE) {
                    put(name, forPair(VALUE, value));
                } else {
                    put(name, TOO_LARGE_TO_CACHE_MARKER);
                }
            }
        }

        /**
         * For suppressing duplicate/redundant settings inserts early, checking
         * our cache first (but without faulting it in), before going to sqlite
         * with the mutation.
         */
        public static boolean isRedundantSetValue(SettingsCache cache, String name, String value) {
            if (cache == null)
                return false;
            synchronized (cache) {
                Bundle bundle = cache.get(name);
                if (bundle == null) {
                    return false;
                }

                String oldValue = bundle.getString(VALUE);
                return (oldValue == null && value == null)
                        || ((oldValue != null) && oldValue.equals(value));
            }
        }
    }
}
