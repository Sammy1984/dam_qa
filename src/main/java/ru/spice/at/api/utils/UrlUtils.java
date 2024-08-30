package ru.spice.at.api.utils;

import lombok.extern.log4j.Log4j2;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Вспомогательный класс для работы с Url
 */
@Log4j2
public final class UrlUtils {

    private UrlUtils() {
        throw new IllegalAccessError("Это утилитный класс. Создание экземпляра не требуется.");
    }

    public static String toBaseURL(String fullUrl) {
        try {
            final URL url = new URL(fullUrl);
            return url.getProtocol() + "://" + url.getHost();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String getHostPort(String baseUrl) {
        final URI uri;
        try {
            uri = new URI(baseUrl);
        } catch (URISyntaxException e) {
            log.error("Ошибка определения хоста и порта: {}", baseUrl);
            throw new IllegalStateException(e);
        }
        return uri.getHost() + ":" + uri.getPort();
    }

    public static String getHostFromUrl(String baseUrl) {
        final URI uri;
        try {
            uri = new URI(baseUrl);
        } catch (URISyntaxException e) {
            log.error("Ошибка определения хоста и порта: {}", baseUrl);
            throw new IllegalStateException(e);
        }
        return uri.getHost();
    }

    public static String getLocalHostIp() {
        String ip = "0.0.0.0";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            log.error("Ошибка при определении IP", ex);
        }
        return ip;
    }

    public static String getHost(String url) {
        String host = "";
        try {
            host = new URL(url).getHost();
        } catch (MalformedURLException e) {
            log.error(e.getMessage());
        }

        return host;
    }

    public static String getSha256DecodeValue(String encodeValue) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            log.error("Ошибка кодировки", e);
            throw new IllegalStateException(e);
        }
        digest.update(encodeValue.getBytes(StandardCharsets.ISO_8859_1));
        byte[] bytes = digest.digest();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
