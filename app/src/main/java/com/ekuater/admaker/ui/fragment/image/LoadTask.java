package com.ekuater.admaker.ui.fragment.image;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LinYong
 */
/*package*/ class LoadTask extends AsyncTask<Void, Void, List<ImageItem>> {

    public interface LoadListener {

        void onPreLoad();

        void onPostLoad(List<ImageItem> items);
    }

    private interface ListenerNotifier {
        void notify(LoadListener listener);
    }

    private final Context mContext;
    private final LoadListener mListener;

    public LoadTask(Context context, LoadListener listener) {
        mContext = context;
        mListener = listener;
    }

    public void load() {
        executeOnExecutor(THREAD_POOL_EXECUTOR, (Void) null);
    }

    @Override
    protected List<ImageItem> doInBackground(Void... params) {
        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        final String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
        };
        final Cursor cursor = cr.query(uri, projection,
                MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[]{"image/jpeg", "image/png"},
                MediaStore.Images.Media.DATE_MODIFIED + " DESC");
        final SparseArrayCompat<String> thumbnails = loadThumbnails();
        final List<ImageItem> items = new ArrayList<>();

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                int idIdx = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                int pathIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

                cursor.moveToFirst();
                do {
                    int _id = cursor.getInt(idIdx);
                    String thumb = thumbnails.get(_id, null);
                    String path = cursor.getString(pathIdx);

                    if (!TextUtils.isEmpty(path) && new File(path).exists()) {
                        items.add(new ImageItem(thumb, path));
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return items;
    }

    @Override
    protected void onPreExecute() {
        notifyPreLoad();
    }

    @Override
    protected void onPostExecute(List<ImageItem> items) {
        notifyPostLoad(items);
    }

    private SparseArrayCompat<String> loadThumbnails() {
        final ContentResolver cr = mContext.getContentResolver();
        final String[] projection = {
                MediaStore.Images.Thumbnails.IMAGE_ID,
                MediaStore.Images.Thumbnails.DATA,
        };
        final Cursor cursor = cr.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        final SparseArrayCompat<String> thumbnails = new SparseArrayCompat<>();

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                int imageIdIdx = cursor.getColumnIndex(MediaStore.Images.Thumbnails.IMAGE_ID);
                int dataIdx = cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);

                cursor.moveToFirst();
                do {
                    int imageId = cursor.getInt(imageIdIdx);
                    String data = cursor.getString(dataIdx);
                    thumbnails.put(imageId, data);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return thumbnails;
    }

    private void notifyListener(ListenerNotifier notifier) {
        notifier.notify(mListener);
    }

    private void notifyPreLoad() {
        notifyListener(new PreLoadNotifier());
    }

    private void notifyPostLoad(List<ImageItem> items) {
        notifyListener(new PostLoadNotifier(items));
    }

    private static class PreLoadNotifier implements ListenerNotifier {

        public PreLoadNotifier() {
        }

        @Override
        public void notify(LoadListener listener) {
            if (listener != null) {
                listener.onPreLoad();
            }
        }
    }

    private static class PostLoadNotifier implements ListenerNotifier {

        private final List<ImageItem> mItems;

        public PostLoadNotifier(List<ImageItem> items) {
            mItems = items;
        }

        @Override
        public void notify(LoadListener listener) {
            if (listener != null) {
                listener.onPostLoad(mItems);
            }
        }
    }
}
