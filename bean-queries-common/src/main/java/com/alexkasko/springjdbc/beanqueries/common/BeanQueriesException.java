package com.alexkasko.springjdbc.beanqueries.common;

/**
 * Library specific runtime exception
 *
 * @author alexkasko
 * Date: 12/22/12
 */
public class BeanQueriesException extends RuntimeException {
    /**
     * {@inheritDoc}
     */
    public BeanQueriesException(String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    public BeanQueriesException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * {@inheritDoc}
     */
    public BeanQueriesException(Throwable cause) {
        super(cause);
    }
}
