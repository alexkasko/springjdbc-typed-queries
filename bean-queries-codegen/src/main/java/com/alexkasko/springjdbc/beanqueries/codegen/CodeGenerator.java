package com.alexkasko.springjdbc.beanqueries.codegen;

import com.alexkasko.springjdbc.beanqueries.common.BeanQueriesException;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static freemarker.ext.beans.BeansWrapper.EXPOSE_PROPERTIES_ONLY;
import static freemarker.template.Configuration.SQUARE_BRACKET_TAG_SYNTAX;
import static org.springframework.jdbc.core.namedparam.NamedParamsSqlParser$Accessor.parseParamsNames;

/**
 * User: alexkasko
 * Date: 12/22/12
 */
public class CodeGenerator {
    private static final String DEFAULT_TEMPLATE_PATH = "/com/alexkasko/springjdbc/beanqueries/codegen/BeanQueries.ftl";
    private static final Pattern TYPE_ID_REGEX = Pattern.compile("^[a-zA-Z0-9_$]*(String|Name|name|Int|Long|Date)");
    private static final Map<String, Class<?>> TYPE_ID_MAP;
    static {
        Map<String, Class<?>> map = new LinkedHashMap<String, Class<?>>();
        map.put("String", String.class);
        map.put("Name", String.class);
        map.put("name", String.class);
        map.put("Int", int.class);
        map.put("Long", long.class);
        map.put("Date", Date.class);
        TYPE_ID_MAP = Collections.unmodifiableMap(map);
    }

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

    public void generate(String packageName, String className, String modifier, Map<String, String> queries, Writer output) {
        try {
            RootTemplateArg params = createTemplateArgs(packageName, className, modifier, queries);
            Reader templateReader = new StringReader(freemarkerTemplate);
            Template ftl = new Template(className, templateReader, freemarkerConf, "UTF-8");
            ftl.process(params, output);
        } catch(TemplateException e) {
            throw new BeanQueriesException(e);
        } catch(IOException e) {
            throw new BeanQueriesException(e);
        }
    }

    private RootTemplateArg createTemplateArgs(String packageName, String className, String modifier, Map<String, String> queries) {
        List<QueryTemplateArg> selects = new ArrayList<QueryTemplateArg>();
        List<QueryTemplateArg> updates = new ArrayList<QueryTemplateArg>();
        for(Map.Entry<String, String> en : queries.entrySet()) {
            String name = en.getKey();
            String sql = en.getValue();
            List<ParamTemplateArg> params = createNamedParams(sql);
            QueryTemplateArg query = new QueryTemplateArg(name, params);
            if(selectRegex.matcher(name).matches()) selects.add(query);
            else if(updateRegex.matcher(name).matches()) updates.add(query);
            else
                throw new BeanQueriesException("Invalid query name, names must match select regex: [" + selectRegex + "] or updateRegex: [" + updateRegex + "]");
        }
        return new RootTemplateArg(packageName, className, modifier, selects, updates);
    }

    private static List<ParamTemplateArg> createNamedParams(String sql) {
        List<String> paramNames = parseParamsNames(sql);
        List<ParamTemplateArg> args = new ArrayList<ParamTemplateArg>(paramNames.size());
        for(String name : paramNames) {
            final Class<?> type;
            Matcher mat = TYPE_ID_REGEX.matcher(name);
            if(!mat.matches()) type = Object.class;
            else {
                if(1 != mat.groupCount()) throw new BeanQueriesException("Invalid type postfix regex: " +
                        "[" + TYPE_ID_REGEX + "], it must have exactly one group containing postfix value");
                String postfix = mat.group(1);
                type = TYPE_ID_MAP.get(postfix);
                if(null == type) throw new BeanQueriesException("Param type postfix regex: [" + TYPE_ID_REGEX + "] " +
                        "is inconsistent with types map: [" + TYPE_ID_MAP + "], missed value: [" + postfix + "]");
            }
            args.add(new ParamTemplateArg(name, type));
        }
        return args;
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
