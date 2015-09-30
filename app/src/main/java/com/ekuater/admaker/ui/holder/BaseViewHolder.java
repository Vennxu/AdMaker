package com.ekuater.admaker.ui.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Administrator on 2015/7/6.
 */
public abstract class BaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    protected ItemListener.AbsListener mItemListener;

    public BaseViewHolder(View itemView, ItemListener.AbsListener itemListener) {
        super(itemView);
        mItemListener = itemListener;
    }
    @Override
    public void onClick(View v) {
        if (mItemListener == null){
            return;
        }
        onRecyclerClick(v);
    }

    protected abstract void onRecyclerClick(View v);

}
