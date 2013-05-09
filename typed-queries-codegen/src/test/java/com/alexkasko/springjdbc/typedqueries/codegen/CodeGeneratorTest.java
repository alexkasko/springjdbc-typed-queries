package com.alexkasko.springjdbc.typedqueries.codegen;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

import static com.alexkasko.springjdbc.typedqueries.codegen.CodeGenerator.camelToUnderscored;
import static com.alexkasko.springjdbc.typedqueries.codegen.CodeGenerator.parseColumnNames;
import static com.alexkasko.springjdbc.typedqueries.codegen.CodeGenerator.underscoredToCamel;
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
        CodeGenerator.builder().build().generate(queries, "foo.bar.baz.FooBar", "nope.sql", out);
    }

    @Test
    public void testColumns() {
        List<String> cols = parseColumnNames("select" +
                " total_foo.foo_a as foo,\n" +
                " bar,\n" +
                " coalesce(bazbaz.total_count,0) as baz,\n" +
                " (select foo from (select foo1 from dual)) as boo\n" +
                "from foo_bar_baz_boo left join dual");
        assertEquals("Size fail", 4, cols.size());
        assertEquals("Colname fail", "foo", cols.get(0));
        assertEquals("Colname fail", "bar", cols.get(1));
        assertEquals("Colname fail", "baz", cols.get(2));
        assertEquals("Colname fail", "boo", cols.get(3));
        List<String> c1 = parseColumnNames("select report_oid from sentinel_tasks\n" +
                "    where id = :taskId");
        System.out.println(c1);
    }

    @Test
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

    @Test
    public void testCamelToUnderscore() {
        assertEquals(null, camelToUnderscored(null));
        assertEquals("", camelToUnderscored(""));
        assertEquals("foo", camelToUnderscored("foo"));
        assertEquals("foo_bar", camelToUnderscored("fooBar"));
        assertEquals("Foo_bar", camelToUnderscored("FooBar"));
        assertEquals("foo__bar", camelToUnderscored("foo_Bar"));
    }
}
