package com.ekuater.admaker.delegate;

/**
 * Created by Leo on 2015/6/18.
 *
 * @author LinYong
 */
class ResCache {

    private String source;
    private int version;
    private String content;
    private long time;

    @SuppressWarnings("unused")
    public ResCache() {
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
