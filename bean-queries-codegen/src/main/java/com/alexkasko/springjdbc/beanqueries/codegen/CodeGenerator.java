package com.alexkasko.springjdbc.beanqueries.codegen;

import com.alexkasko.springjdbc.beanqueries.common.BeanQueriesException;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

import static freemarker.ext.beans.BeansWrapper.EXPOSE_PROPERTIES_ONLY;
import static freemarker.template.Configuration.SQUARE_BRACKET_TAG_SYNTAX;

/**
 * User: alexkasko
 * Date: 12/22/12
 */
public class CodeGenerator {
    private static final String DEFAULT_TEMPLATE_PATH = "/com/alexkasko/springjdbc/beanqueries/codegen/BeanQueries.ftl";
    //    private final Pattern paramPattern = Pattern.compile("\\s*=\\s*:([a-zA-Z][a-zA-Z_$]*)");
    public static final String DEFAULT_SELECT_REGEX = "^[a-zA-Z][a-zA-Z0-9_$]*Select$";
    public static final String DEFAULT_UPDATE_REGEX = "^[a-zA-Z][a-zA-Z0-9_$]*(?:Insert|Update|Delete)$";
    public static final String DEFAULT_FREEMARKER_TEMPLATE = defaultFreemarkerTemplate();

    private final Pattern selectRegex;
    private final Pattern updateRegex;
    private final String freemarkerTemplate;
    private final Configuration freemarkerConf;

    public CodeGenerator() {
        this(DEFAULT_SELECT_REGEX, DEFAULT_UPDATE_REGEX, defaultFreemarkerTemplate());
    }

    public CodeGenerator(String selectRegex, String updateRegex, String freemarkerTemplate) {
        this(selectRegex, updateRegex, freemarkerTemplate, defaultFreemarkerConf());
    }

    public CodeGenerator(String selectRegex, String updateRegex, String freemarkerTemplate, Configuration freemarkerConf) {
        this.selectRegex = Pattern.compile(selectRegex);
        this.updateRegex = Pattern.compile(updateRegex);
        this.freemarkerTemplate = freemarkerTemplate;
        this.freemarkerConf = freemarkerConf;
    }

    public void generate(String packageName, String className, Map<String, String> queries, Writer output) {
        try {
            TemplateParams params = createParams(packageName, className, queries);
            Reader templateReader = new StringReader(freemarkerTemplate);
            Template ftl = new Template(className, templateReader, freemarkerConf, "UTF-8");
            ftl.process(params, output);
        } catch(TemplateException e) {
            throw new BeanQueriesException(e);
        } catch(IOException e) {
            throw new BeanQueriesException(e);
        }
    }

    private TemplateParams createParams(String packageName, String className, Map<String, String> queries) {
        List<String> selects = new ArrayList<String>();
        List<String> updates = new ArrayList<String>();
        for(String name : queries.keySet()) {
            if(selectRegex.matcher(name).matches()) selects.add(name);
            else if(updateRegex.matcher(name).matches()) updates.add(name);
            else
                throw new BeanQueriesException("Invalid query name, names must match select regex: [" + selectRegex + "] or updateRegex: [" + updateRegex + "]");
        }
        return new TemplateParams(packageName, className, selects, updates);
    }

    private static Configuration defaultFreemarkerConf() {
        Configuration fc = new Configuration();
        fc.setLocalizedLookup(false);
        fc.setTagSyntax(SQUARE_BRACKET_TAG_SYNTAX);
        fc.setTemplateUpdateDelay(Integer.MAX_VALUE);
        fc.setNumberFormat("computer");
        BeansWrapper bw = (BeansWrapper) fc.getObjectWrapper();
        bw.setExposureLevel(EXPOSE_PROPERTIES_ONLY);
        return fc;
    }

    private static String defaultFreemarkerTemplate() {
        InputStream is = null;
        try {
            is = CodeGenerator.class.getResourceAsStream(DEFAULT_TEMPLATE_PATH);
            byte[] data = readFull(is);
            return new String(data, Charset.forName("UTF-8"));
        } catch(IOException e) {
            throw new BeanQueriesException(e);
        } finally {
            closeQuietly(is);
        }
    }

    private static byte[] readFull(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n;
        while(-1 != (n = is.read(buffer))) {
            baos.write(buffer, 0, n);
        }
        return baos.toByteArray();
    }

    private static void closeQuietly(InputStream is) {
        try {
            if(is != null) {
                is.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }
}
