package com.SupportClasses;

/**
 * Класс, который получает сообщения о работе программы и выводит их.
 */
public interface Logger {

    /**
     * Выводит обычное сообщение.
     *
     * @param message - текст сообщения
     */
    void logMessage(String message);

    /**
     * Выводит сообщение об ошибке.
     *
     * @param message - текст ошибки
     */
    void logError(String message);

}
