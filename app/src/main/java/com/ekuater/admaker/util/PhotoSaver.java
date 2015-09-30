package com.ekuater.admaker.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;

import com.ekuater.admaker.EnvConfig;

import java.io.File;

/**
 * Created by Leo on 2015/6/4.
 *
 * @author LinYong
 */
public final class PhotoSaver {

    public interface OnSaveListener {
        void onSaveCompleted(String path);
    }

    private static File newSaveFile() {
        final File saveDir = EnvConfig.getSaveDir();
        final String fileName = "save_" + System.currentTimeMillis() + ".jpg";
        if (!saveDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            saveDir.mkdirs();
        }
        return new File(saveDir, fileName);
    }

    public static void savePhoto(Context context, Bitmap bitmap, OnSaveListener listener) {
        new SaveTask(context, bitmap, listener).execute((Void) null);
    }

    private static class SaveTask extends AsyncTask<Void, Void, Void> {

        private Context context;
        private Bitmap bitmap;
        private OnSaveListener listener;

        public SaveTask(Context context, Bitmap bitmap, OnSaveListener listener) {
            this.context = context;
            this.bitmap = bitmap;
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... params) {
            File file;
            int count = 10;

            do {
                file = newSaveFile();
                --count;
            } while (file.exists() && count > 0);

            BmpUtils.saveBitmapToFile(bitmap, file);
            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()},
                    null, new ScanCompletedListener(listener));
            return null;
        }
    }

    private static class ScanCompletedListener
            implements MediaScannerConnection.OnScanCompletedListener {

        private OnSaveListener listener;

        public ScanCompletedListener(OnSaveListener listener) {
            this.listener = listener;
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            if (listener != null) {
                listener.onSaveCompleted(path);
            }
        }
    }
}
