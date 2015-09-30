package com.ekuater.admaker.encoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * String MD5 encoder
 *
 * @author LinYong
 */
public class MD5Encoder implements StringEncoder {

    private static final char[] HEX_DIGIT_CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
    };

    private static MD5Encoder sInstance = new MD5Encoder();

    public static MD5Encoder getInstance() {
        return sInstance;
    }

    private MessageDigest mMessageDigest;

    private MD5Encoder() {
        try {
            mMessageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private String encode(byte[] source) {
        if (mMessageDigest == null) {
            return null;
        }

        mMessageDigest.reset();
        mMessageDigest.update(source);

        return toHexString(mMessageDigest.digest());
    }

    private String toHexString(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        char[] buffer = new char[data.length * 2];
        int idx = 0;

        for (byte tmp : data) {
            buffer[idx++] = HEX_DIGIT_CHARS[(tmp >>> 4) & 0x0F];
            buffer[idx++] = HEX_DIGIT_CHARS[tmp & 0x0F];
        }

        return new String(buffer);
    }

    @Override
    public String encode(String string) {
        return encode(string.getBytes());
    }

    @Override
    public String decode(String string) {
        // MD5 can not be decode.
        return null;
    }
}
