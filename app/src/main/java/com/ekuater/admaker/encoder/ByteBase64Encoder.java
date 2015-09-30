package com.ekuater.admaker.encoder;

/**
 * A Base 64 encoding implementation. Encode byte to Base64 String.
 *
 * @author LinYong
 */
public class ByteBase64Encoder {

    private static ByteBase64Encoder sInstance;

    private static synchronized void initInstance() {
        if (sInstance == null) {
            sInstance = new ByteBase64Encoder();
        }
    }

    public static ByteBase64Encoder getInstance() {
        if (sInstance == null) {
            initInstance();
        }
        return sInstance;
    }

    private ByteBase64Encoder() {
        // Use getInstance()
    }

    public String encode(byte[] source) {
        return Base64.encodeBytes(source);
    }

    public byte[] decode(String str) {
        return Base64.decode(str);
    }
}
