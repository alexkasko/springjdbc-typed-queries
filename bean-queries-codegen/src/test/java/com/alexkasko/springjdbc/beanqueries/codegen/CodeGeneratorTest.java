package com.alexkasko.springjdbc.beanqueries.codegen;

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
        queries.put("myTest1Select", "foo");
        queries.put("myTest2Select", "foo");
        queries.put("myTestInsert", "foo");
        queries.put("myTestUpdate", "foo");
        queries.put("myTestDelete", "foo");
//        Writer out = new OutputStreamWriter(System.out);
        Writer out = new OutputStreamWriter(new ByteArrayOutputStream());
        new CodeGenerator().generate("foo.bar.baz", "FooBar", queries, out);
    }
}
