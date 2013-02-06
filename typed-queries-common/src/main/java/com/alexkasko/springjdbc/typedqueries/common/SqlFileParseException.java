package com.alexkasko.springjdbc.typedqueries.common;

/**
 * Module specific exception
 *
 * @author alexkasko
 * Date: 2/6/13
 */
public class SqlFileParseException extends RuntimeException {
    /**
     * Constructor
     *
     * @param message error message
     */
    public SqlFileParseException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param cause error cause
     */
    public SqlFileParseException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     *
     * @param message error message
     * @param cause error cause
     */
    public SqlFileParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
