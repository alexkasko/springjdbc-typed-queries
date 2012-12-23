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
import static org.springframework.jdbc.core.namedparam.NamedParamsSqlParser$Accessor.parseParamsNames;

/**
 * User: alexkasko
 * Date: 12/22/12
 */
public class CodeGenerator {
    private static final String DEFAULT_TEMPLATE_PATH = "/com/alexkasko/springjdbc/beanqueries/codegen/BeanQueries.ftl";
//    str
    private static final String TYPE_ID_PREFIX = "$";
    private static final Map<String, Class<?>> TYPE_ID_MAP;
    static {
        Map<String, Class<?>> map = new LinkedHashMap<String, Class<?>>();
        map.put(TYPE_ID_PREFIX + 'S', String.class);
        map.put(TYPE_ID_PREFIX + 'Z', boolean.class);
        map.put(TYPE_ID_PREFIX + 'I', int.class);
        map.put(TYPE_ID_PREFIX + 'J', long.class);
        map.put(TYPE_ID_PREFIX + 'T', Date.class);
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
        for(String fullName : paramNames) {
            int postLen = TYPE_ID_PREFIX.length() + 1;
            final ParamTemplateArg arg;
            if(fullName.length() <= postLen) arg = new ParamTemplateArg(fullName, Object.class);
            else {
                String prefix = fullName.substring(0, fullName.length() - postLen);
                String postfix = fullName.substring(fullName.length() - postLen);
                Class<?> found = TYPE_ID_MAP.get(postfix);
                if(null != found) arg = new ParamTemplateArg(prefix, found);
                else arg = new ParamTemplateArg(fullName, Object.class);
            }
            args.add(arg);
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
