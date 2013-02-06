package com.alexkasko.springjdbc.typedqueries.codegen;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

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
}
