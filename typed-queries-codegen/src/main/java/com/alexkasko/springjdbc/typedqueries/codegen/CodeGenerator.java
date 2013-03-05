package com.alexkasko.springjdbc.typedqueries.codegen;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

import static freemarker.ext.beans.BeansWrapper.EXPOSE_PROPERTIES_ONLY;
import static freemarker.template.Configuration.SQUARE_BRACKET_TAG_SYNTAX;
import static java.util.Collections.unmodifiableMap;
import static org.springframework.jdbc.core.namedparam.NamedParamsSqlParser$Accessor.parseParamsNames;

/**
 * Queries wrappers code generator. Uses FreeMarker template for generating wrapper class.
 *
 * @author alexkasko
 * Date: 12/22/12
 */
public class CodeGenerator {

    private final boolean isPublic;
    private final boolean useIterableJdbcTemplate;
    private final Pattern selectRegex;
    private final Pattern updateRegex;
    private final Map<String, Class<?>> typeIdMap;
    private final String freemarkerTemplate;
    private final Configuration freemarkerConf;

    /**
     * Constrcutor
     *
     * @param isPublic whether generated class and its methods will have 'public' access modifier
     * @param useIterableJdbcTemplate whether to use iterable jdbc template extensions from this
     *                                project (https://github.com/alexkasko/springjdbc-iterable)
     * @param selectRegex regular expression to use for identifying 'select' queries by name
     * @param updateRegex regular expression to use for identifying 'insert', 'update' and 'delete' queries by name
     * @param typeIdMap mapping of parameter names postfixes to data types
     * @param freemarkerTemplate freemarker template body
     * @param freemarkerConf freemarker configuration
     * @throws CodeGeneratorException on any error
     */
    public CodeGenerator(boolean isPublic, boolean useIterableJdbcTemplate, String selectRegex,
                         String updateRegex, Map<String, Class<?>> typeIdMap,
                         String freemarkerTemplate, Configuration freemarkerConf) throws CodeGeneratorException {
        if(null == selectRegex) throw new CodeGeneratorException("Provided selectRegex is null");
        if(null == updateRegex) throw new CodeGeneratorException("Provided updateRegex is null");
        if(null == typeIdMap) throw new CodeGeneratorException("Provided typeIdMap is null");
        if(null == freemarkerTemplate) throw new CodeGeneratorException("Provided freemarkerTemplate is null");
        if(null == freemarkerConf) throw new CodeGeneratorException("Provided freemarkerConf is null");
        this.isPublic = isPublic;
        this.useIterableJdbcTemplate = useIterableJdbcTemplate;
        this.selectRegex = Pattern.compile(selectRegex);
        this.updateRegex = Pattern.compile(updateRegex);
        this.typeIdMap = typeIdMap;
        this.freemarkerTemplate = freemarkerTemplate;
        this.freemarkerConf = freemarkerConf;
    }

    /**
     * Generates queries wrappers class
     *
     * @param queries name -> text query mapping
     * @param fullClassName class name with package prefix
     * @param sourceSqlFileName name of source SQL file
     * @param output output writer
     * @throws CodeGeneratorException
     */
    public void generate(Map<String, String> queries, String fullClassName, String sourceSqlFileName, Writer output)
            throws CodeGeneratorException {
        if(null == queries || 0 == queries.size()) throw new CodeGeneratorException("Provided queries map is empty");
        if(null == fullClassName || 0 == fullClassName.length()) throw new CodeGeneratorException("Provided fullClassName is empty");
        if(!fullClassName.contains(".") || (fullClassName.lastIndexOf(".") ==  fullClassName.length())) throw new CodeGeneratorException(
                "Full class name must have non empty package prefix, but was: [" + fullClassName + "]");
        if(null == output) throw new CodeGeneratorException("Provided output is null");
        try {
            RootTemplateArg params = createTemplateArgs(queries, fullClassName, sourceSqlFileName);
            Reader templateReader = new StringReader(freemarkerTemplate);
            Template ftl = new Template(fullClassName, templateReader, freemarkerConf, "UTF-8");
            ftl.process(params, output);
        } catch (TemplateException e) {
            throw new CodeGeneratorException("Error processing FreeMarker template", e);
        } catch (IOException e) {
            throw new CodeGeneratorException("IO error", e);
        } catch (Exception e) {
            if(e instanceof CodeGeneratorException) throw (CodeGeneratorException) e;
            throw new CodeGeneratorException("Error generating queries code", e);
        }
    }

    private RootTemplateArg createTemplateArgs(Map<String, String> queries, String fullClassName, String sourceSqlFileName) {
        int dotIndex = fullClassName.lastIndexOf(".");
        String packageName = fullClassName.substring(0, dotIndex);
        String className = fullClassName.substring(dotIndex + 1);
        String modifier = isPublic ? "public" : "";
        List<QueryTemplateArg> selects = new ArrayList<QueryTemplateArg>();
        List<QueryTemplateArg> updates = new ArrayList<QueryTemplateArg>();
        for (Map.Entry<String, String> en : queries.entrySet()) {
            String name = en.getKey();
            String sql = en.getValue();
            List<ParamTemplateArg> params = createNamedParams(sql);
            QueryTemplateArg query = new QueryTemplateArg(name, params);
            if (selectRegex.matcher(name).matches()) selects.add(query);
            else if (updateRegex.matcher(name).matches()) updates.add(query);
            else throw new CodeGeneratorException("Invalid query name, names must match select regex: [" + selectRegex + "] or updateRegex: [" + updateRegex + "]");
        }
        return new RootTemplateArg(packageName, className, modifier, useIterableJdbcTemplate, sourceSqlFileName, selects, updates);
    }

    private List<ParamTemplateArg> createNamedParams(String sql) {
        List<String> paramNames = parseParamsNames(sql);
        List<ParamTemplateArg> args = new ArrayList<ParamTemplateArg>(paramNames.size());
        for (String name : paramNames) {
            args.add(new ParamTemplateArg(name, typeForName(name)));
        }
        return args;
    }

    private Class typeForName(String name) {
        for (Map.Entry<String, Class<?>> en : typeIdMap.entrySet()) {
            String postfix = en.getKey();
            if (name.endsWith(postfix)) return en.getValue();
        }
        return Object.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CodeGenerator");
        sb.append("{isPublic=").append(isPublic);
        sb.append(", useIterableJdbcTemplate=").append(useIterableJdbcTemplate);
        sb.append(", selectRegex=").append(selectRegex);
        sb.append(", updateRegex=").append(updateRegex);
        sb.append(", typeIdMap=").append(typeIdMap);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Creates builder instance
     *
     * @return builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for configuring and instantiating {@link CodeGenerator}
     */
    public static class Builder {
        private boolean isPublic = false;
        private boolean useIterableJdbcTemplate = false;
        private String selectRegex = "^select[a-zA-Z][a-zA-Z0-9_$]*$";
        private String updateRegex = "^(?:insert|update|delete)[a-zA-Z][a-zA-Z0-9_$]*$";
        private Map<String, Class<?>> typeIdMap = defaultTypeIdMap();
        private String freemarkerTemplate = defaultFreemarkerTemplate();
        private Configuration freemarkerConf = defaultFreemarkerConf();

        /**
         * Public access modifier setter, false by default
         *
         * @param aPublic whether generated class and its methods will have 'public' access modifier
         * @return builder itself
         */
        public Builder setPublic(boolean aPublic) {
            isPublic = aPublic;
            return this;
        }

        /**
         * Whether to use iterable JDBC extensions (https://github.com/alexkasko/springjdbc-iterable),
         * false by default
         *
         * @param useIterableJdbcTemplate whether to use iterable jdbc template extensions
         * @return builder itself
         */
        public Builder setUseIterableJdbcTemplate(boolean useIterableJdbcTemplate) {
            this.useIterableJdbcTemplate = useIterableJdbcTemplate;
            return this;
        }

        /**
         * Regular expression to use for identifying 'select' queries by name,
         * default: {@code ^select[a-zA-Z][a-zA-Z0-9_$]*$}
         *
         * @param selectRegex regular expression to use for identifying 'select' queries by name
         * @return builder itself
         */
        public Builder setSelectRegex(String selectRegex) {
            this.selectRegex = selectRegex;
            return this;
        }

        /**
         * Regular expression to use for identifying 'insert', 'update' and 'delete' queries by name,
         * default: {@code ^(?:insert|update|delete)[a-zA-Z][a-zA-Z0-9_$]*$}
         *
         * @param updateRegex regular expression to use for identifying 'insert',
         *                    'update' and 'delete' queries by name
         * @return builder itself
         */
        public Builder setUpdateRegex(String updateRegex) {
            this.updateRegex = updateRegex;
            return this;
        }

        /**
         * Mapping of parameter names postfixes to data types, default:
         * <pre>
         * {@code
         *   Map<String, Class<?>> map = new LinkedHashMap<String, Class<?>>();
         *   map.put("String", String.class);
         *   map.put("Text", String.class);
         *   map.put("text", String.class);
         *   map.put("Name", String.class);
         *   map.put("name", String.class);
         *   map.put("Bool", boolean.class);
         *   map.put("Short", short.class);
         *   map.put("Int", int.class);
         *   map.put("Long", long.class);
         *   map.put("Id", long.class);
         *   map.put("id", long.class);
         *   map.put("Number", long.class);
         *   map.put("number", long.class);
         *   map.put("Count", long.class);
         *   map.put("count", long.class);
         *   map.put("Size", long.class);
         *   map.put("size", long.class);
         *   map.put("Float", float.class);
         *   map.put("Double", double.class);
         *   map.put("Numeric", BigDecimal.class);
         *   map.put("Binary", byte[].class);
         *   map.put("binary", byte[].class);
         *   map.put("Date", Date.class);
         *   map.put("date", Date.class);
         *   map.put("List", Collection.class);
         *   map.put("list", Collection.class);
         *   map.put("Collection", Collection.class);
         *   map.put("collection", Collection.class);
         *   map.put("Set", Collection.class);
         *   map.put("set", Collection.class);
         *   return unmodifiableMap(map);
         * }
         * </pre>
         *
         * @param typeIdMap postfix->type mapping
         * @return builder itself
         */
        public Builder setTypeIdMap(Map<String, Class<?>> typeIdMap) {
            this.typeIdMap = typeIdMap;
            return this;
        }

        /**
         * FreeMarker template body for queries class, loaded from classpath
         * {@code /com/alexkasko/springjdbc/typedqueries/codegen/BeanQueries.ftl} by default
         *
         * @param freemarkerTemplate
         * @return builder itself
         */
        public Builder setFreemarkerTemplate(String freemarkerTemplate) {
            this.freemarkerTemplate = freemarkerTemplate;
            return this;
        }

        /**
         * Freemarker configuration instance, default:
         * <pre>
         * {@code
         *   Configuration fc = new Configuration();
         *   fc.setLocalizedLookup(false);
         *   fc.setTagSyntax(SQUARE_BRACKET_TAG_SYNTAX);
         *   fc.setTemplateUpdateDelay(Integer.MAX_VALUE);
         *   fc.setNumberFormat("computer");
         *   BeansWrapper bw = (BeansWrapper) fc.getObjectWrapper();
         *   bw.setExposureLevel(EXPOSE_PROPERTIES_ONLY);
         *   return fc;
         * }
         * </pre>
         *
         * @param freemarkerConf freemarker configuration instance
         * @return builder itself
         */
        public Builder setFreemarkerConf(Configuration freemarkerConf) {
            this.freemarkerConf = freemarkerConf;
            return this;
        }

        /**
         * Creates configured {@link CodeGenerator} instance
         *
         * @return configured {@link CodeGenerator} instance
         */
        public CodeGenerator build() {
            return new CodeGenerator(isPublic, useIterableJdbcTemplate, selectRegex, updateRegex, typeIdMap,
                    freemarkerTemplate, freemarkerConf);
        }

        private static Map<String, Class<?>> defaultTypeIdMap() {
            Map<String, Class<?>> map = new LinkedHashMap<String, Class<?>>();
            map.put("String", String.class);
            map.put("Text", String.class);
            map.put("text", String.class);
            map.put("Name", String.class);
            map.put("name", String.class);
            map.put("Bool", boolean.class);
            map.put("Short", short.class);
            map.put("Int", int.class);
            map.put("Long", long.class);
            map.put("Id", long.class);
            map.put("id", long.class);
            map.put("Number", long.class);
            map.put("number", long.class);
            map.put("Count", long.class);
            map.put("count", long.class);
            map.put("Size", long.class);
            map.put("size", long.class);
            map.put("Float", float.class);
            map.put("Double", double.class);
            map.put("Numeric", BigDecimal.class);
            map.put("Binary", byte[].class);
            map.put("binary", byte[].class);
            map.put("Date", Date.class);
            map.put("date", Date.class);
            map.put("List", Collection.class);
            map.put("list", Collection.class);
            map.put("Collection", Collection.class);
            map.put("collection", Collection.class);
            map.put("Set", Collection.class);
            map.put("set", Collection.class);
            return unmodifiableMap(map);
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
                is = CodeGenerator.class.getResourceAsStream("/com/alexkasko/springjdbc/typedqueries/codegen/BeanQueries.ftl");
                byte[] data = readFull(is);
                return new String(data, Charset.forName("UTF-8"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                closeQuietly(is);
            }
        }

        private static byte[] readFull(InputStream is) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 4];
            int n;
            while (-1 != (n = is.read(buffer))) {
                baos.write(buffer, 0, n);
            }
            return baos.toByteArray();
        }

        private static void closeQuietly(InputStream is) {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }
}
