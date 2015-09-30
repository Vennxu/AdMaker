package com.ekuater.admaker.ui.fragment.image;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ekuater.admaker.EnvConfig;
import com.ekuater.admaker.R;
import com.ekuater.admaker.util.L;

import java.io.File;
import java.util.List;

/**
 * @author LinYong
 */
public class ImageSelectFragment extends Fragment {

    private static final String TAG = ImageSelectFragment.class.getSimpleName();

    private static final String ARGS_TITLE = "args_title";
    private static final String IS_SHOW_PROGRESS = "is_show_progress";

    private static final String FUNC_TAKE_PHOTO = "take_photo";
    private static final int REQUEST_TAKE_PHOTO = 1001;

    private static final String TEMP_PHOTO_URI_KEY = "temp_photo_uri";

    public static ImageSelectFragment newInstance(String title, boolean isShowProgress) {
        ImageSelectFragment instance = new ImageSelectFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_TITLE, title);
        args.putBoolean(IS_SHOW_PROGRESS, isShowProgress);
        instance.setArguments(args);
        return instance;
    }

    private ImageSelectListener mListener;
    private String mTitle;
    private boolean isShowProgress;
    private ItemAdapter mAdapter;
    private ProgressBar mProgressBar;
    private boolean mLoading = false;
    private Uri mTempPhotoUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();

        Bundle args = getArguments();
        mTitle = (args != null) ? args.getString(ARGS_TITLE) : null;
        isShowProgress = (args != null) && args.getBoolean(IS_SHOW_PROGRESS);
        if (actionBar != null) {
            if (!TextUtils.isEmpty(mTitle)) {
                actionBar.setTitle(mTitle);
            } else {
                actionBar.setTitle(R.string.select_image);
            }
        }

        mAdapter = new ItemAdapter();
        LoadTask loadTask = new LoadTask(activity, new LoadTask.LoadListener() {
            @Override
            public void onPreLoad() {
                mLoading = true;
                updateProgressBar();
            }

            @Override
            public void onPostLoad(List<ImageItem> items) {
                mAdapter.setItems(addFunctionItems(items));
                mLoading = false;
                updateProgressBar();
            }
        });
        loadTask.load();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (activity instanceof ImageSelectListener)
                ? (ImageSelectListener) activity : null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_select, container, false);
        TextView title = (TextView) view.findViewById(R.id.title);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        if (!TextUtils.isEmpty(mTitle)) {
            title.setText(mTitle);
        } else {
            title.setText(R.string.select_image);
        }
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        GridView gridView = (GridView) view.findViewById(R.id.grid);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getAdapter() == mAdapter) {
                    ImageItem item = mAdapter.getItem(position);

                    switch (item.mType) {
                        case ImageItem.TYPE_FUNCTION:
                            if (FUNC_TAKE_PHOTO.equals(item.mExtra)) {
                                onTakePhoto();
                            }
                            break;
                        default:
                            onImageSelect(item);
                            break;
                    }
                }
            }
        });
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
        updateProgressBar();
        view.findViewById(R.id.linear_progress).setVisibility(isShowProgress ? View.VISIBLE : View.GONE);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(TEMP_PHOTO_URI_KEY, mTempPhotoUri);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mTempPhotoUri = savedInstanceState.getParcelable(TEMP_PHOTO_URI_KEY);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        L.v(TAG, "onActivityResult(), requestCode=%1$d,resultCode=%2$d,data=%3$s",
                requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                onPhotoTaken(resultCode);
                break;
            default:
                break;
        }
    }

    private List<ImageItem> addFunctionItems(List<ImageItem> items) {
        items.add(0, new ImageItem(R.drawable.ic_camera_selector, FUNC_TAKE_PHOTO));
        return items;
    }

    private void updateProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(mLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void onTakePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mTempPhotoUri = Uri.fromFile(EnvConfig.genTempFile("jpg"));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempPhotoUri);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    private void onPhotoTaken(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            if (mTempPhotoUri != null && new File(mTempPhotoUri.getPath()).exists()) {
                notifySingleSelect(mTempPhotoUri.getPath(), true);
            }
        } else {
            notifySelectFailure();
        }
    }

    private void onImageSelect(ImageItem item) {
        if (item.mType == ImageItem.TYPE_IMAGE
                && !TextUtils.isEmpty(item.mImagePath)) {
            notifySingleSelect(item.mImagePath, false);
        }
    }

    private void notifySelectListener(SelectListenerNotifier notifier) {
        if (mListener != null) {
            notifier.notify(mListener);
        }
    }

    private void notifySelectFailure() {
        notifySelectListener(new SelectFailedNotifier());
    }

    private void notifySingleSelect(String imagePath, boolean isTemp) {
        notifySelectListener(new SingleSelectNotifier(imagePath, isTemp));
    }

    private class SelectFailedNotifier implements SelectListenerNotifier {

        @Override
        public void notify(ImageSelectListener listener) {
            if (listener != null) {
                listener.onSelectFailure();
            }
        }
    }

    private class SingleSelectNotifier implements SelectListenerNotifier {

        private final String mImagePath;
        private final boolean mIsTemp;

        public SingleSelectNotifier(String imagePath, boolean isTemp) {
            mImagePath = imagePath;
            mIsTemp = isTemp;
        }

        @Override
        public void notify(ImageSelectListener listener) {
            if (listener != null) {
                listener.onSelectSuccess(mImagePath, mIsTemp);
            }
        }
    }

    private class ItemAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;
        private final ThumbnailCache mCache;

        private List<ImageItem> mItemList;

        public ItemAdapter() {
            mInflater = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mCache = ThumbnailCache.getInstance();
        }

        public void setItems(List<ImageItem> items) {
            mItemList = items;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return (mItemList == null) ? 0 : mItemList.size();
        }

        @Override
        public ImageItem getItem(int position) {
            return (mItemList == null) ? null : mItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = newView(parent);
            }
            bindView(position, convertView);
            return convertView;
        }

        private View newView(ViewGroup parent) {
            return mInflater.inflate(R.layout.image_select_item, parent, false);
        }

        private void bindView(int position, View view) {
            ImageItem imageItem = getItem(position);
            ImageView imageView = (ImageView) view.findViewById(R.id.image);

            switch (imageItem.mType) {
                case ImageItem.TYPE_FUNCTION:
                    imageView.setTag(null);
                    imageView.setImageResource(imageItem.mIconId);
                    break;
                default:
                    imageView.setTag(imageItem.mImagePath);
                    mCache.loadThumbnail(imageItem.mThumbnailPath, imageItem.mImagePath,
                            new ImageShowCallback(imageView, imageItem.mImagePath));
                    break;
            }
        }

        private class ImageShowCallback implements ThumbnailCache.LoadCallback {

            private final ImageView mImageView;
            private final String mSourcePath;

            public ImageShowCallback(ImageView imageView, String sourcePath) {
                mImageView = imageView;
                mSourcePath = sourcePath;
            }

            @Override
            public void onThumbnailLoaded(Bitmap thumbnail) {
                if (mImageView != null && mSourcePath.equals(mImageView.getTag())) {
                    if (thumbnail != null) {
                        mImageView.setImageBitmap(thumbnail);
                    } else {
                        mImageView.setImageResource(0);
                    }
                }
            }
        }
    }
}
