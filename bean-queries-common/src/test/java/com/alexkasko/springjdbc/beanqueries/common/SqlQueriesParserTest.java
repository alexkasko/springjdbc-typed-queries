package com.alexkasko.springjdbc.beanqueries.common;

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
public class SqlQueriesParserTest {
    @Test
    public void test() {
        String sql = "" +
                "-- sql file contents example\n" +
                "-- some header notes\n" +
                "\n" +
                "/** myTestSelect */\n" +
                "select foo from bar\n" +
                "   where baz = 1\n" +
                " -- add some stupid condition\n" +
                "   and 1 > 0 -- stupid condidion\n" +
                "   limit 42\n";

        byte[] bytes = sql.getBytes(Charset.forName("UTF-8"));
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        Map<String, String> parsed = new SqlQueriesParser().parse(bais);
        assertNotNull("Map craetion fail", parsed);
        assertEquals("Map size fail", 1, parsed.size());
        System.out.println(parsed);
        assertTrue("Query name fail", parsed.containsKey("myTestSelect"));
        assertEquals("Query body fail", "select foo from bar where baz = 1 and 1 > 0 limit 42", parsed.get("myTestSelect"));
    }
}
