package ru.spice.at.common.utils;

import io.qameta.allure.Step;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Утилитный класс для работы с изображением.
 */
@Log4j2
public final class ImageHelper {

    @Step("Генерируем случайное изображение с разрешением {width} на {height} в формате '{format}'")
    public static byte[] getRandomByteImage(int width, int height, String format) {
        return toByteArray(getRandomImage(width, height), format);
    }

    /**
     * Генерируем изображение.
     *
     * @param width  ширина изображения
     * @param height высота изображения
     * @return изображение типа BufferedImage
     */
    public static BufferedImage getRandomImage(int width, int height) {
        log.info("Генерируем изображение с разрешением {} на {}", width, height);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Random random = new Random();

        // create random values pixel by pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int a = random.nextInt(255);
                int r = random.nextInt(255);
                int g = random.nextInt(255);
                int b = random.nextInt(255);

                //pixel
                int p = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, p);
            }
        }
        return image;
    }

    /**
     * Генерируем байт-код из изображения.
     *
     * @param image  изображение типа BufferedImage
     * @param format формат изображения
     * @return изображение в виде байтового массива
     */
    public static byte[] toByteArray(BufferedImage image, String format) {
        log.info("Преобразовываем изображение в байтовый массив");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, format, outputStream);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка преобразования файла в байт код", e);
        }
        return outputStream.toByteArray();
    }

    /**
     * Генерируем BufferedImage из байт-кода изображения.
     *
     * @param bytes массив байтов изображения
     * @return изображение типа BufferedImage
     */
    public static BufferedImage toBufferedImage(byte[] bytes) {
        log.info("Преобразовываем изображение из байтового массива");
        InputStream is = new ByteArrayInputStream(bytes);
        BufferedImage image;
        try {
            image = ImageIO.read(is);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка преобразования файла из байт кода", e);
        }
        return image;
    }

    @SneakyThrows
    public static Boolean checkSameImage(BufferedImage expImage, File actualFile) {
        return checkSameImage(expImage, ImageIO.read(actualFile));
    }

    @SneakyThrows
    public static Boolean checkSameImage(File expFile, BufferedImage actualImage) {
        return checkSameImage(ImageIO.read(expFile), actualImage);
    }

    @SneakyThrows
    public static Boolean checkSameImage(File expFile, File actualFile) {
        return checkSameImage(ImageIO.read(expFile), ImageIO.read(actualFile));
    }

    /**
     * Проверяем соответствие изображений.
     *
     * @param expImage    изображение типа BufferedImage - эталон
     * @param actualImage изображение типа BufferedImage - для проверки
     * @return true - изображения совпадают попиксельно, false - не совпадают
     */
    public static Boolean checkSameImage(BufferedImage expImage, BufferedImage actualImage) {

        if (expImage.getHeight() != actualImage.getHeight() || expImage.getWidth() != actualImage.getWidth()) {
            log.warn("Размеры не совпадают exp: '{}:{}' act: '{}:{}'",
                    expImage.getHeight(), expImage.getWidth(), actualImage.getHeight(), actualImage.getWidth());
            return false;
        }

        for (int row = 0; row < expImage.getHeight(); row++) {
            for (int column = 0; column < expImage.getWidth(); column++) {
                if (expImage.getRGB(column, row) != actualImage.getRGB(column, row)) {
                    log.warn("Пиксели не совпали x: {} y: {}", column, row);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Проверяем различие изображений с учетом допустимый разницы deltaE
     * <a href="https://en.wikipedia.org/wiki/Color_difference">Формула цветового отличия</a>
     *
     * @param expImage    изображение типа BufferedImage - эталон
     * @param actualImage изображение типа BufferedImage - для проверки
     * @param expDeltaE   допустимый разница deltaE для алгоритма redmean RGB
     *
     * @return процент отклонения от эталонного изображения
     */
    public static Double checkDifImage(BufferedImage expImage, BufferedImage actualImage, Double expDeltaE) {

        if (expImage.getHeight() != actualImage.getHeight() || expImage.getWidth() != actualImage.getWidth()) {
            log.warn("Размеры не совпадают exp: '{}:{}' act: '{}:{}'",
                    expImage.getHeight(), expImage.getWidth(), actualImage.getHeight(), actualImage.getWidth());
            return 100.;
        }

        log.info("Вычисляем процент отклонения от эталона с учетом допустимой разницы expDeltaE = {}", expDeltaE);
        List<Double> difList = new ArrayList<>();

        for (int x = 0; x < expImage.getHeight(); x++) {
            for (int y = 0; y < expImage.getWidth(); y++) {
                Color expColor = new Color(expImage.getRGB(x, y));
                Color actualColor = new Color(actualImage.getRGB(x, y));

                double rMean = (expColor.getRed() + actualColor.getRed()) / 2.;
                int dRed = expColor.getRed() - actualColor.getRed();
                int dGreen = expColor.getGreen() - actualColor.getGreen();
                int dBlue = expColor.getBlue() - actualColor.getBlue();

                double deltaE = Math.sqrt((2 + rMean / 256.) * Math.pow(dRed, 2) +
                        4 * Math.pow(dGreen, 2) +
                        (2 + (255 - rMean) / 256.) * Math.pow(dBlue, 2));
                if (deltaE > expDeltaE)
                    difList.add(deltaE);

            }
        }
        return (difList.size() * 100.) / (expImage.getHeight() * expImage.getWidth());
    }
}
