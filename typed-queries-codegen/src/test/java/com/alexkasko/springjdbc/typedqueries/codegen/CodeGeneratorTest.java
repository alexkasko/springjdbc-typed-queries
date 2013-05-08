package com.alexkasko.springjdbc.typedqueries.codegen;

import junit.framework.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static java.lang.Character.codePointAt;
import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;
import static java.util.Locale.ENGLISH;
import static junit.framework.Assert.assertEquals;

/**
 * User: alexkasko
 * Date: 12/22/12
 */
public class CodeGeneratorTest {
    @Test
    public void test() {
        Map<String, String> queries = new LinkedHashMap<String, String>();
        queries.put("selectTestStuff1", "foo");
        queries.put("selectTestStuff2", "foo");
        queries.put("insertTestStuff", "foo");
        queries.put("updateTestStuff", "foo");
        queries.put("deleteTestStuff", "foo");
//        Writer out = new OutputStreamWriter(System.out);
        Writer out = new OutputStreamWriter(new ByteArrayOutputStream());
        CodeGenerator.builder().build().generate(queries, "foo.bar.baz.FooBar", "nope.sql",  out);
    }

//    @Test
    public void testUnderscoreToCamel() {
        assertEquals(null, underscoredToCamel(null));
        assertEquals("", underscoredToCamel(""));
        assertEquals("_", underscoredToCamel("_"));
        assertEquals("__", underscoredToCamel("__"));
        assertEquals("foo", underscoredToCamel("foo"));
        assertEquals("Foo", underscoredToCamel("_foo"));
        assertEquals("Foo_", underscoredToCamel("_foo_"));
        assertEquals("_Foo_", underscoredToCamel("__foo_"));
        assertEquals("fooBar", underscoredToCamel("foo_bar"));
        assertEquals("foo_Bar", underscoredToCamel("foo__bar"));
        assertEquals("FooBar", underscoredToCamel("Foo_Bar"));
    }

//    @Test
    public void testCamelToUnderscore() {
        assertEquals(null, camelToUnderscored(null));
        assertEquals("", camelToUnderscored(""));
        assertEquals("foo", camelToUnderscored("foo"));
        assertEquals("foo_bar", camelToUnderscored("fooBar"));
        assertEquals("Foo_bar", camelToUnderscored("FooBar"));
        assertEquals("foo__bar", camelToUnderscored("foo_Bar"));
    }

    private static String underscoredToCamel(String underscored) {
        if(null == underscored || 0 == underscored.length() || !underscored.contains("_")) return underscored;
        StringBuilder sb = new StringBuilder();
        boolean usFound = false;
        for(int i = 0; i< underscored.length(); i++) {
            char ch = underscored.charAt(i);
            if('_' == ch) {
                if(usFound) { // double underscore
                    sb.append('_');
                } else {
                    usFound = true;
                }
            } else if (usFound) {
                sb.append(toUpperCase(ch));
                usFound = false;
            } else {
                sb.append(ch);
            }
        }
        if(usFound) sb.append("_");
        return sb.toString();
    }

    private static String camelToUnderscored(String camel) {
        if(null == camel || camel.length() < 2) return camel;
        boolean hasUpper = false;
        for (int i = 1; i < camel.length(); i++) {
            char ch = camel.charAt(i);
            if(ch == Character.toUpperCase(ch)) {
                hasUpper = true;
                break;
            }
        }
        if(!hasUpper) return camel;
        StringBuilder sb = new StringBuilder();
        sb.append(camel.charAt(0));
        for (int i = 1; i < camel.length(); i++) {
            char ch = camel.charAt(i);
            char lower = toLowerCase(ch);
            if(ch == toUpperCase(ch) && ch != lower) {
                sb.append("_");
                sb.append(lower);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static class UnderscoredBeanPropertySqlParameterSource extends BeanPropertySqlParameterSource {

        /**
         * Create a new BeanPropertySqlParameterSource for the given bean.
         *
         * @param object the bean instance to wrap
         */
        public UnderscoredBeanPropertySqlParameterSource(Object object) {
            super(object);
        }

        /**
         * Determine whether there is a value for the specified named parameter.
         * @param paramName the name of the parameter
         * @return whether there is a value defined
         */
        @Override
        public boolean hasValue(String paramName) {
            return super.hasValue(underscoredToCamel(paramName));
        }

        /**
       	 * Return the parameter value for the requested named parameter.
       	 * @param paramName the name of the parameter
       	 * @return the value of the specified parameter
       	 * @throws IllegalArgumentException if there is no value for the requested parameter
       	 */
        @Override
        public Object getValue(String paramName) throws IllegalArgumentException {
            return super.getValue(underscoredToCamel(paramName));
        }


        /**
       	 * Provide access to the property names of the wrapped bean.
       	 * Uses support provided in the {@link org.springframework.beans.PropertyAccessor} interface.
       	 * @return an array containing all the known property names
       	 */
        @Override
        public String[] getReadablePropertyNames() {
            String[] camel = super.getReadablePropertyNames();
            String[] undercored = new String[camel.length];
            for (int i = 0; i < camel.length; i++) {
                undercored[i] = camelToUnderscored(camel[i]);
            }
            return undercored;
        }

        /**
       	 * Derives a default SQL type from the corresponding property type.
       	 * @see org.springframework.jdbc.core.StatementCreatorUtils#javaTypeToSqlParameterType
       	 */
        @Override
        public int getSqlType(String paramName) {
            return super.getSqlType(underscoredToCamel(paramName));
        }

        /**
       	 * Register a SQL type for the given parameter.
       	 * @param paramName the name of the parameter
       	 * @param sqlType the SQL type of the parameter
       	 */
        @Override
        public void registerSqlType(String paramName, int sqlType) {
            super.registerSqlType(underscoredToCamel(paramName), sqlType);
        }

        /**
       	 * Register a SQL type for the given parameter.
       	 * @param paramName the name of the parameter
       	 * @param typeName the type name of the parameter
       	 */
        @Override
        public void registerTypeName(String paramName, String typeName) {
            super.registerTypeName(underscoredToCamel(paramName), typeName);
        }

        /**
       	 * Return the type name for the given parameter, if registered.
       	 * @param paramName the name of the parameter
       	 * @return the type name of the parameter,
       	 * or <code>null</code> if not registered
       	 */
        @Override
        public String getTypeName(String paramName) {
            return super.getTypeName(underscoredToCamel(paramName));
        }
    }
}
