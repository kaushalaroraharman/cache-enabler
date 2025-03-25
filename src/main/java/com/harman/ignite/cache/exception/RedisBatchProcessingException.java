package com.harman.ignite.cache.exception;

/**
 * Custom exception in case of batch processing failure.
 */
public class RedisBatchProcessingException extends RuntimeException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new redis batch processing exception.
     *
     * @param message the message
     */
    public RedisBatchProcessingException(String message) {
        super(message);
    }

}
