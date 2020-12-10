package com.tk.fcmb.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 *
 * @author joel.eze
 */
@Slf4j
public class AES {

    //KEd4gDNSDdMBxCGliZaC8w==

    private static final String KEY = "KEd4gDNSDdMBxCGliZaC8w==";
    private static final String ALGORITHM = "AES";

    public static String encrypt(String value) {
        try {
            Key key = generateKey(KEY);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedByteValue = cipher.doFinal(value.getBytes("utf-8"));
            return Base64.encodeBase64String(encryptedByteValue);
            //return encryptedValue64;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return value;
        }

    }

    public static String decrypt(String value) {
        try {
            Key key = generateKey(KEY);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedValue64 = Base64.decodeBase64(value);
            byte[] decryptedByteValue = cipher.doFinal(decryptedValue64);
            String decryptedValue = new String(decryptedByteValue, "utf-8");
            return decryptedValue;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return value;
        }

    }

    private static Key generateKey(String KEY) throws Exception {
        Key key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        return key;
    }

//    public static void main(String[] args) {
//        System.out.println(encrypt("1982"));
//    }

}
