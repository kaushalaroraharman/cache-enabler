package com.harman.ignite.cache.exception;

/**
 * Custom exception for errors from ignite-cache library.
 */
public class IgniteCacheException extends RuntimeException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new ignite cache exception.
     *
     * @param message the message
     */
    public IgniteCacheException(String message) {
        super(message);
    }

    /**
     * Instantiates a new ignite cache exception.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public IgniteCacheException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
