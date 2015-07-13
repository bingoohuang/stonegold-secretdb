package stonegold;

import com.google.common.base.Charsets;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.security.SecureRandom;

public class Aes {
    private static final String KEY_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    public static byte[] base64Decode(String base64) {
        return DatatypeConverter.parseBase64Binary(base64);
    }

    public static String base64Encode(byte[] bytes) {
        return DatatypeConverter.printBase64Binary(bytes);
    }

    public static String encrypt(String value, String key) {
        String iv = randomIV();
        String encrypt = encrypt(value, key, iv);
        return iv + ":" + encrypt;
    }

    public static String decrypt(String value, String key) {
        String iv = StringUtils.substringBefore(value, ":");
        String content = StringUtils.substringAfter(value, ":");
        return decrypt(content, key, iv);
    }


    public static String encrypt(String value, String key, String iv) {
        SecretKeySpec skeySpec = new SecretKeySpec(base64Decode(key), KEY_ALGORITHM);
        return encrypt(value, skeySpec, iv);
    }

    public static String decrypt(String value, String key, String iv) {
        SecretKeySpec skeySpec = new SecretKeySpec(base64Decode(key), KEY_ALGORITHM);
        return decrypt(value, skeySpec, iv);
    }

    public static String randomIV() {
        // build the initialization vector (randomly).
        SecureRandom random = new SecureRandom();
        byte iv[] = new byte[16]; // generate random 16 byte IV AES is always 16bytes
        random.nextBytes(iv);

        return base64Encode(iv);
    }

    public static String decrypt(String value, Key key, String iv) {
        IvParameterSpec ivParameterSpec = new IvParameterSpec(base64Decode(iv));
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
            byte[] decrypted = cipher.doFinal(base64Decode(value));
            return bytesToString(decrypted);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToString(byte[] decrypted) {
        return new String(decrypted, Charsets.UTF_8);
    }

    private static byte[] stringToBytes(String value) {
        return value.getBytes(Charsets.UTF_8);
    }

    public static String encrypt(String value, Key key, String iv) {
        IvParameterSpec ivParameterSpec = new IvParameterSpec(base64Decode(iv));
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(stringToBytes(value));
            return base64Encode(encrypted);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateAesKey() {
        KeyGenerator kg;
        try {
            kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        kg.init(128);
        SecretKey secretKey = kg.generateKey();
        return base64Encode(secretKey.getEncoded());
    }


    public static void main(String[] args) {
        System.out.println(Aes.generateAesKey());
//        String key = "uzTnFINhcCOYmzwdI9VkXA";
//        System.out.println(encrypt("hello world, it's me, bingoo huang", key));
//        System.out.println(encrypt("hello world, it's me, holly wolf", key));
//
//        System.out.println(decrypt("Bv-RMDpEL-h9C9tYSDBavA:KZpF4UjfrHjxQ_321-P59Yn46ATCJ54hyf_BTlgotbfSKDD7TiXoRonf36XlutCJ", key));
//        System.out.println(decrypt("osVFk4Ct14StOhrgF-UJdg:LJVndkjwCmj7pD1mANPejNMG0qL4eXeMg7w0ofH_7zMPsKnQbykci6MZEO-tlDVR", key));
    }
}
