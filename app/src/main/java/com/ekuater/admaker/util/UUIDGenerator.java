
package com.ekuater.admaker.util;

import java.util.UUID;

/**
 * Generator UUID
 * 
 * @author LinYong
 */
public final class UUIDGenerator {

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
