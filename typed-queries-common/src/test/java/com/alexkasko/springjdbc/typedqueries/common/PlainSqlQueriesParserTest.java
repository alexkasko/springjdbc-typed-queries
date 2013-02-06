package com.alexkasko.springjdbc.typedqueries.common;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * User: alexkasko
 * Date: 12/22/12
 */
public class PlainSqlQueriesParserTest {
    @Test
    public void test() {
        String sql = "" +
                "-- sql file contents example\n" +
                "-- some header notes\n" +
                "\n" +
                "/** myTestSelect */\n" +
                "select foo from bar\n" +
                "   where baz = 1\n" +
                "   and 1 > 0 -- stupid condidion\n" +
                "   limit 42\n";

        byte[] bytes = sql.getBytes(Charset.forName("UTF-8"));
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        Map<String, String> parsed = new PlainSqlQueriesParser().parse(bais);
        assertNotNull("Map craetion fail", parsed);
        assertEquals("Map size fail", 1, parsed.size());
        assertTrue("Query name fail", parsed.containsKey("myTestSelect"));
        assertEquals("Query body fail", "select foo from bar\n" +
                "   where baz = 1\n" +
                "   and 1 > 0 -- stupid condidion\n" +
                "   limit 42\n", parsed.get("myTestSelect"));
    }
}
