package com.ekuater.admaker.delegate.imageloader;

/**
 * Created by Leo on 2015/2/3.
 *
 * @author LinYong
 */
public class DisplayOptions {

    private final int defaultImageRes;
    private final boolean cacheInMemory;
    private final boolean cacheOnDisk;

    public DisplayOptions(Builder builder) {
        defaultImageRes = builder.defaultImageRes;
        cacheInMemory = builder.cacheInMemory;
        cacheOnDisk = builder.cacheOnDisk;
    }

    public int getDefaultImageRes() {
        return defaultImageRes;
    }

    public boolean isCacheInMemory() {
        return cacheInMemory;
    }

    public boolean isCacheOnDisk() {
        return cacheOnDisk;
    }

    public static class Builder {

        private int defaultImageRes = 0;
        private boolean cacheInMemory = false;
        private boolean cacheOnDisk = true;

        public Builder() {
        }

        public DisplayOptions build() {
            return new DisplayOptions(this);
        }

        public Builder defaultImageRes(int imageRes) {
            defaultImageRes = imageRes;
            return this;
        }

        public Builder cacheInMemory(boolean cacheInMemory) {
            this.cacheInMemory = cacheInMemory;
            return this;
        }

        public Builder cacheOnDisk(boolean cacheOnDisk) {
            this.cacheOnDisk = cacheOnDisk;
            return this;
        }
    }
}
