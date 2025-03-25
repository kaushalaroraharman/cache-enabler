package com.harman.ignite.cache.exception;

/**
 * Custom exception thrown while decoding the value from redis.
 */
public class DecodeException extends RuntimeException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new decode exception.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public DecodeException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
