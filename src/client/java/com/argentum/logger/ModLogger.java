package com.argentum.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModLogger {
    private static final String TITLE = "[Speculator] ";
    private static final Logger LOGGER = LoggerFactory.getLogger("Speculator");

    public static void info(String message, Object... args) {
        message = TITLE + message;
        LOGGER.info(message, args);
    }

    public static void warn(String message, Object... args) {
        message = TITLE + message;
        LOGGER.warn("{} {}", TITLE, message);
    }

    public static void error(String message, Object... args) {
        message = TITLE + message;
        LOGGER.error("{} {}", TITLE, message);
    }

    public static void debug(String message, Object... args) {
        message = TITLE + message;
        LOGGER.debug("{} {}", TITLE, message);
    }
}
