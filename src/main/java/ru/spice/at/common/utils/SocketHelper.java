package ru.spice.at.common.utils;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Утилитный класс для работы с сокетами.
 */
public class SocketHelper {

    /**
     * Поиск доступного порта
     *
     * @return доступный для подключения порт
     */
    public static int findAvailablePort() {
        int port = 0;
        try (final ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            port = socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Порт не найден", e);
        }
        if (port > 0) {
            return port;
        }
        throw new RuntimeException("Порт не найден");
    }
}