package com.alexkasko.springjdbc.typedqueries.codegen;

/**
 * Exception that will be thrown out on code generation error
 *
 * @author alexkasko
 * Date: 2/6/13
 */
public class CodeGeneratorException extends RuntimeException {
    /**
     * Constructor
     *
     * @param message error message
     */
    public CodeGeneratorException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param message error message
     * @param cause cause exception
     */
    public CodeGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }
}
