package com.ekuater.admaker.datastruct;

import android.support.annotation.NonNull;

/**
 * Created by Leo on 2015/7/8.
 *
 * @author LinYong
 */
public final class UserVOUtils {

    @NonNull
    public static SimpleUserVO toSimpleUserVO(@NonNull UserVO userVO) {
        SimpleUserVO simpleUserVO = new SimpleUserVO();
        simpleUserVO.setUserId(userVO.getUserId());
        simpleUserVO.setAdMakerCode(userVO.getAdMakerCode());
        simpleUserVO.setNickname(userVO.getNickname());
        simpleUserVO.setGender(userVO.getGender());
        simpleUserVO.setAvatar(userVO.getAvatar());
        simpleUserVO.setAvatarThumb(userVO.getAvatarThumb());
        return simpleUserVO;
    }
}
