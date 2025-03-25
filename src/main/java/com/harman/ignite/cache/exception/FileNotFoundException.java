package com.harman.ignite.cache.exception;

/**
 * Custom exception for file not found.
 */
public class FileNotFoundException extends RuntimeException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new file not found exception.
     *
     * @param message the message
     */
    public FileNotFoundException(String message) {
        super(message);
    }
}
