package com.ekuater.admaker.ui;

import android.graphics.Bitmap;
import android.text.TextUtils;

/**
 * Created by Leo on 2015/1/22.
 *
 * @author LinYong
 */
public class ShareContent {

    public enum Platform {

        QQ("QQ"),
        QZONE("QZone"),
        SINA_WEIBO("SinaWeibo"),
        WEIXIN("WeiXin"),
        WEIXIN_CIRCLE("WeiXinCircle");

        private final String platform;

        Platform(String platform) {
            this.platform = platform;
        }

        public String getPlatform() {
            return platform;
        }
    }

    public enum MediaType {
        IMAGE,
        VIDEO,
        MUSIC,
    }

    private String title;
    private Bitmap icon;
    private String url;
    private String content;
    private String mediaFileUrl;
    private MediaType mediaType;
    private Bitmap shareBitmap;

    private Platform sharePlatform;

    public ShareContent() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Platform getSharePlatform() {
        return sharePlatform;
    }

    public void setSharePlatform(Platform sharePlatform) {
        this.sharePlatform = sharePlatform;
    }

    public void setShareMedia(String mediaFileUrl, MediaType mediaType) {
        this.mediaFileUrl = mediaFileUrl;
        this.mediaType = mediaType;
    }

    public void setShareBitmap(Bitmap bitmap) {
        this.shareBitmap = bitmap;
        this.mediaType = MediaType.IMAGE;
    }

    public String getMediaFile() {
        return mediaFileUrl;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public Bitmap getShareBitmap() {
        return shareBitmap;
    }

    public boolean hasShareMedia() {
        return (mediaType != null && !TextUtils.isEmpty(mediaFileUrl))
                || (mediaType == MediaType.IMAGE && shareBitmap != null);
    }
}
