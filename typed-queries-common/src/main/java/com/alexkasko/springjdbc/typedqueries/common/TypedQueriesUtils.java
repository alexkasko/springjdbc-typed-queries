package com.alexkasko.springjdbc.typedqueries.common;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Typed queries common utilities
 *
 * @author alexkasko
 * Date: 2/5/13
 */
public class TypedQueriesUtils {
    public static final RowMapper<String> STRING_ROW_MAPPER = new SingleColumnRowMapper<String>(String.class);
    public static final RowMapper<Boolean> BOOLEAN_ROW_MAPPER = new SingleColumnRowMapper<Boolean>(Boolean.class);
    public static final RowMapper<Short> SHORT_ROW_MAPPER = new SingleColumnRowMapper<Short>(Short.class);
    public static final RowMapper<Integer> INT_ROW_MAPPER = new SingleColumnRowMapper<Integer>(Integer.class);
    public static final RowMapper<Long> LONG_ROW_MAPPER = new SingleColumnRowMapper<Long>(Long.class);
    public static final RowMapper<Float> FLOAT_ROW_MAPPER = new SingleColumnRowMapper<Float>(Float.class);
    public static final RowMapper<Double> DOUBLE_ROW_MAPPER = new SingleColumnRowMapper<Double>(Double.class);
    public static final RowMapper<BigDecimal> BIG_DECIMAL_ROW_MAPPER = new SingleColumnRowMapper<BigDecimal>(BigDecimal.class);
    public static final RowMapper<byte[]> BYTE_ARRAY_ROW_MAPPER = new SingleColumnRowMapper<byte[]>(byte[].class);
    public static final RowMapper<Date> DATE_ROW_MAPPER = new SingleColumnRowMapper<Date>(Date.class);
}
