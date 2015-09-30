package com.ekuater.admaker.ui.holder;

import android.view.View;

import com.ekuater.admaker.datastruct.PortfolioVO;
import com.ekuater.admaker.datastruct.SimpleUserVO;


/**
 * Created by Administrator on 2015/7/6.
 */
public interface ItemListener {

     interface AbsListener{

         void onItemClick(View v, int position);

         void onDeleteItemClick(View v, int position);

         void onCommentClick();

         void onPraiseClick(int position, PortfolioVO portfolioVO);

         void onShareClick();

         void onImageClick();

         void onAvatarImageClick();

         void onHeaderAvatarImageClick(int position);

     }

     class RecyclerItemListener implements AbsListener {

        @Override
        public void onItemClick(View v, int position) {

        }

         @Override
         public void onDeleteItemClick(View v, int position) {

         }

         @Override
        public void onCommentClick() {

        }

        @Override
        public void onPraiseClick(int position, PortfolioVO portfolioVO) {

        }

         @Override
         public void onShareClick() {

         }

         @Override
        public void onImageClick() {

        }

         @Override
         public void onAvatarImageClick() {

         }

         @Override
         public void onHeaderAvatarImageClick(int positon) {

         }
    }




}
