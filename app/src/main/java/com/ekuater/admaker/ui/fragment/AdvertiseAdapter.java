package com.ekuater.admaker.ui.fragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.ekuater.admaker.delegate.AdElementDisplay;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/7/25.
 */
public abstract class AdvertiseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected LayoutInflater inflater;
    protected Context context;
    private List<Object> list = null;
    protected ItemClickListener itemClickListener;

    public AdvertiseAdapter(Context context) {
        this.context = context;
        list = new ArrayList<>();
        inflater = LayoutInflater.from(context);
    }

    public synchronized void updateData(List<Object> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public synchronized void addData(List<Object> list) {
        list.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return initViews(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AdvertiseViewHolder viewHolder = (AdvertiseViewHolder) holder;
        viewHolder.recender(getItem(position));
    }

    protected abstract int getLayout();

    protected abstract AdvertiseViewHolder initViews(ViewGroup parent, int viewType);

    @Override
    public int getItemCount() {
        return list.size();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public void setOnItemClickListener(ItemClickListener listener) {
        itemClickListener = listener;
    }

    public interface ItemClickListener {

        void onItemClick(Object object, int position);

        void onItemImageClick(Object object, int position);

        void onItemTextClick(Object object, int position);
    }

    public static abstract class AdvertiseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        protected Context context;
        protected Object object;
        protected AdElementDisplay adElementDisplay;
        protected ItemClickListener itemClickListener;

        public AdvertiseViewHolder(Context context, View itemView) {
            super(itemView);
            this.context = context;
            adElementDisplay = AdElementDisplay.getInstance(context);
        }

        public AdvertiseViewHolder(Context context, View itemView, ItemClickListener itemClickListener) {
            super(itemView);
            this.context = context;
            this.itemClickListener = itemClickListener;
            adElementDisplay = AdElementDisplay.getInstance(context);
        }

        protected abstract void recender(Object object);

        protected void setObject(Object object){
            this.object = object;
        }
    }

}
