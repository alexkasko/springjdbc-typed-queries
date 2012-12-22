package com.alexkasko.springjdbc.beanqueries.common;

import java.io.IOException;
import java.io.Reader;

/**
 * Reader transparent wrapper, {@link java.io.Reader#close()} overriden as NOOP
 *
 * @author alexkasko
 * Date: 12/22/12
 */
class NoCloseReader extends Reader {
    private final Reader target;

    /**
     * @param target target reader
     */
    NoCloseReader(Reader target) {
        if(null == target) throw new IllegalArgumentException("Provided reader is null");
        this.target = target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        return target.read(cbuf, off, len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // NOOP
    }
}

