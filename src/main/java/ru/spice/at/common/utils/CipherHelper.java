package ru.spice.at.common.utils;

import lombok.extern.log4j.Log4j2;
import ru.spice.at.common.StandProperties;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Утилитный класс для работы с шифрованием данных
 */
@Log4j2
public class CipherHelper {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = ALGORITHM + "/ECB/PKCS5Padding";
    private static final String NO_KEY_MESSAGE = "Отсутствует ключ для шифрования";
    private static final StandProperties standProperties = new StandProperties();

    /**
     * Генерируем ключ для шифра
     *
     * @return ключ
     */
    public static String generateKey() {
        log.info("Генерируем ключ для шифра");
        KeyGenerator keygen;
        try {
            keygen = KeyGenerator.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            log.error("Не найден алгоритм, ключ не сгенерирован");
            throw new RuntimeException("Не найден алгоритм, ключ не сгенерирован", e);
        }
        keygen.init(256);
        SecretKey key = keygen.generateKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static String encrypt(String value) {
        return encrypt(value, standProperties.getSettings().encodedKey());
    }

    /**
     * Шифруем строку
     *
     * @param value      строка для шифра
     * @param encodedKey ключ
     * @return зашифрованная строка
     */
    public static String encrypt(String value, String encodedKey) {
        log.info("Шифруем значение");
        if (encodedKey == null || encodedKey.isEmpty()){
            log.error(NO_KEY_MESSAGE);
            throw new RuntimeException(NO_KEY_MESSAGE);
        }

        byte[] encrypted;
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encrypted = cipher.doFinal(value.getBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            log.error("Ошибка в шифровании");
            throw new RuntimeException("Ошибка в шифровании", e);
        }
        return DatatypeConverter.printHexBinary(encrypted);
    }

    public static String decrypt(String value) {
        return decrypt(value, standProperties.getSettings().encodedKey());
    }

    /**
     * Расшифровываем строку
     *
     * @param value      строка для расшифровки
     * @param encodedKey ключ
     * @return расшифрованная строка
     */
    public static String decrypt(String value, String encodedKey) {
        log.info("Расшифровываем закодированное значение");
        if (encodedKey == null || encodedKey.isEmpty()){
            log.error(NO_KEY_MESSAGE);
            throw new RuntimeException(NO_KEY_MESSAGE);
        }

        String decryptValue;
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            decryptValue = new String(cipher.doFinal(DatatypeConverter.parseHexBinary(value)));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            log.error("Ошибка в расшифровке");
            throw new RuntimeException("Ошибка в расшифровке", e);
        }
        return decryptValue;
    }
}
