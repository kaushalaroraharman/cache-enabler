package com.harman.ignite.cache.exception;

/**
 * Custom Exception thrown in case of failing to load the class configured against the property: ignite.codec.class. 
 */
public class JacksonCodecException extends RuntimeException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new jackson codec exception.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public JacksonCodecException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
