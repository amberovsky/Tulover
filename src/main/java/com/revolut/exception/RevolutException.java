package com.revolut.exception;

/**
 * Base revolut exception
 */
public class RevolutException extends Exception {
    /**
     * @param message exception message
     */
    public RevolutException(String message) {
        super(message);
    }

    /**
     * @param message exception message
     * @param cause cause
     */
    public RevolutException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Displayable exception has a message which can be shown to the user
     *
     * @return true if this exception is displayable
     */
    public boolean isDisplayable() { return false; }

    /**
     * @return message to show to the user, if the exception is displayable
     */
    public String toDisplayable() { return ""; }
}
