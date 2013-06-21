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
import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Locale.ENGLISH;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.DOTALL;
import static org.springframework.jdbc.core.namedparam.NamedParamsSqlParser$Accessor.parseParamsNames;

/**
 * Queries wrappers code generator. Uses FreeMarker template for generating wrapper class.
 *
 * @author alexkasko
 * Date: 12/22/12
 */
public class CodeGenerator {
    private static final Pattern COLUMNS_FROM_REGEX = Pattern.compile("\\s+from\\s+.*", CASE_INSENSITIVE | DOTALL);
    private static final Pattern COLUMNS_COMMA_REGEX = Pattern.compile(",");
    private static final Pattern COLUMNS_SPACE_REGEX = Pattern.compile("\\s+");
    private static final Pattern COLUMNS_NAME_RESTRICTION_PATTERN = Pattern.compile("^[a-zA-Z0-9_$]+$");

    private final boolean isPublic;
    private final boolean useIterableJdbcTemplate;
    private final boolean useCloseableIterables;
    private final boolean useCheckSingleRowUpdates;
    private final boolean useBatchInserts;
    private final boolean useTemplateStringSubstitution;
    private final boolean useUnderscoredToCamel;
    private final boolean generateInterfacesForColumns;
    private final Pattern selectRegex;
    private final Pattern updateRegex;
    private final Pattern templateRegex;
    private final Pattern templateValueConstraintRegex;
    private final Map<String, Class<?>> typeIdMap;
    private final String freemarkerTemplate;
    private final Configuration freemarkerConf;

    /**
     * Constructor
     *
     *
     * @param isPublic whether generated class and its methods will have 'public' access modifier
     * @param useIterableJdbcTemplate whether to use iterable jdbc template extensions from this
     *                                project (https://github.com/alexkasko/springjdbc-iterable)
     * @param useCheckSingleRowUpdates whether to generate additional update methods, those check that
     *                                 only single row was changed on update
     * @param useBatchInserts whether to generate additional insert (DML) methods (with parameters), those
     *                        takes {@link java.util.Iterator} of parameters and execute inserts
     *                        for the contents of the specified iterator in batch mode
     * @param useTemplateStringSubstitution whether to recognize query templates on method generation
     * @param selectRegex regular expression to use for identifying 'select' queries by name
     * @param updateRegex regular expression to use for identifying 'insert', 'update' and 'delete' queries by name
     * @param templateRegex regular expression to recognize query templates by name
     * @param templateValueConstraintRegex regular expression constraint for template substitution values
     * @param useCloseableIterables whether to generate {@code CloseableIterable} methods
     * @param useUnderscoredToCamel whether to convert underscored parameter named to camel ones
     * @param generateInterfacesForColumns whether to generate interfaces for columns
     * @param typeIdMap mapping of parameter names postfixes to data types
     * @param freemarkerTemplate freemarker template body
     * @param freemarkerConf freemarker configuration    @throws CodeGeneratorException on any error
     */
    public CodeGenerator(boolean isPublic, boolean useIterableJdbcTemplate, boolean useCloseableIterables,
                         boolean useCheckSingleRowUpdates, boolean useBatchInserts,
                         boolean useTemplateStringSubstitution, String selectRegex,
                         String updateRegex, String templateRegex, String templateValueConstraintRegex,
                         boolean useUnderscoredToCamel, boolean generateInterfacesForColumns,
                         Map<String, Class<?>> typeIdMap, String freemarkerTemplate,
                         Configuration freemarkerConf) throws CodeGeneratorException {
        if(null == selectRegex) throw new CodeGeneratorException("Provided selectRegex is null");
        if(null == updateRegex) throw new CodeGeneratorException("Provided updateRegex is null");
        if(null == templateRegex) throw new CodeGeneratorException("Provided templateRegex is null");
        if(null == templateValueConstraintRegex) throw new CodeGeneratorException("Provided templateValueConstraintRegex is null");
        if(null == typeIdMap) throw new CodeGeneratorException("Provided typeIdMap is null");
        if(null == freemarkerTemplate) throw new CodeGeneratorException("Provided freemarkerTemplate is null");
        if(null == freemarkerConf) throw new CodeGeneratorException("Provided freemarkerConf is null");
        this.isPublic = isPublic;
        this.useIterableJdbcTemplate = useIterableJdbcTemplate;
        this.useCloseableIterables = useCloseableIterables;
        this.useCheckSingleRowUpdates = useCheckSingleRowUpdates;
        this.useBatchInserts = useBatchInserts;
        this.useTemplateStringSubstitution = useTemplateStringSubstitution;
        this.useUnderscoredToCamel = useUnderscoredToCamel;
        this.generateInterfacesForColumns = generateInterfacesForColumns;
        this.selectRegex = Pattern.compile(selectRegex);
        this.updateRegex = Pattern.compile(updateRegex);
        this.templateRegex = Pattern.compile(templateRegex);
        this.templateValueConstraintRegex = Pattern.compile(templateValueConstraintRegex);
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
            List<String> paramNames = parseParamsNames(sql);
            Set<ParamTemplateArg> params = createNamedParams(paramNames);
            List<String> colNames = parseColumnNames(sql);
            Set<ParamTemplateArg> columns = createNamedParams(colNames);
            boolean isTemplate = useTemplateStringSubstitution && templateRegex.matcher(name).matches();
            QueryTemplateArg query = new QueryTemplateArg(name, params, columns, isTemplate);
            if (selectRegex.matcher(name).matches()) selects.add(query);
            else if (updateRegex.matcher(name).matches()) updates.add(query);
            else throw new CodeGeneratorException("Invalid query name: [" + name + "], names must match select regex: " +
                        "[" + selectRegex + "] or updateRegex: [" + updateRegex + "]");
        }
        return new RootTemplateArg(packageName, className, modifier, useIterableJdbcTemplate, useCloseableIterables, useCheckSingleRowUpdates,
                useBatchInserts, useTemplateStringSubstitution, useUnderscoredToCamel, generateInterfacesForColumns, sourceSqlFileName, templateValueConstraintRegex.pattern(),
                selects, updates);
    }

    private Set<ParamTemplateArg> createNamedParams(List<String> rawParamNames) {
        final List<String> paramNames;
        if(useUnderscoredToCamel) {
            paramNames = new ArrayList<String>(rawParamNames.size());
            for(String pa : rawParamNames) paramNames.add(underscoredToCamel(pa));
        } else paramNames = rawParamNames;
        Set<ParamTemplateArg> args = new LinkedHashSet<ParamTemplateArg>(paramNames.size());
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

    // todo: use proper SQL parser (javacc/antlr)
    static List<String> parseColumnNames(String sql) {
        if(!sql.toLowerCase(ENGLISH).startsWith("select")) return emptyList();
        // clean from parenthesed contents
        int level = 0;
        StringBuilder cleanSb = new StringBuilder();
        for (int i = 0; i < sql.length(); i++) {
            char ch = sql.charAt(i);
            if ('(' == ch) {
                if (0 == level) cleanSb.append('(');
                level += 1;
            } else if (')' == ch) {
                level -= 1;
                if (0 == level) cleanSb.append(')');
            } else if (0 == level) cleanSb.append(ch);
        }
        // remove all after first from
        String colstring = COLUMNS_FROM_REGEX.matcher(cleanSb.toString()).replaceFirst("");
        // parse last word before comma
        String[] selectParts = COLUMNS_COMMA_REGEX.split(colstring);
        List<String> res = new ArrayList<String>(selectParts.length);
        for(String pa : selectParts) {
            String[] colParts = COLUMNS_SPACE_REGEX.split(pa);
            String colname = colParts[colParts.length - 1];
            if(COLUMNS_NAME_RESTRICTION_PATTERN.matcher(colname).matches()) res.add(colname);
        }
        return res;
    }

    static String underscoredToCamel(String underscored) {
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

    static String camelToUnderscored(String camel) {
        if (null == camel || camel.length() < 2) return camel;
        boolean hasUpper = false;
        for (int i = 1; i < camel.length(); i++) {
            char ch = camel.charAt(i);
            if (ch == Character.toUpperCase(ch)) {
                hasUpper = true;
                break;
            }
        }
        if (!hasUpper) return camel;
        StringBuilder sb = new StringBuilder();
        sb.append(camel.charAt(0));
        for (int i = 1; i < camel.length(); i++) {
            char ch = camel.charAt(i);
            char lower = toLowerCase(ch);
            if (ch == toUpperCase(ch) && ch != lower) {
                sb.append("_");
                sb.append(lower);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CodeGenerator");
        sb.append("{isPublic=").append(isPublic);
        sb.append(", useIterableJdbcTemplate=").append(useIterableJdbcTemplate);
        sb.append(", useCheckSingleRowUpdates=").append(useCheckSingleRowUpdates);
        sb.append(", useBatchInserts=").append(useBatchInserts);
        sb.append(", useTemplateStringSubstitution=").append(useTemplateStringSubstitution);
        sb.append(", useUnderscoredToCamel=").append(useUnderscoredToCamel);
        sb.append(", selectRegex=").append(selectRegex);
        sb.append(", updateRegex=").append(updateRegex);
        sb.append(", templateRegex=").append(templateRegex);
        sb.append(", templateValueConstraintRegex=").append(templateValueConstraintRegex);
        sb.append(", typeIdMap=").append(typeIdMap);
        sb.append(", freemarkerTemplate='").append(freemarkerTemplate).append('\'');
        sb.append(", freemarkerConf=").append(freemarkerConf);
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
        private boolean isPublic = true;
        private boolean useIterableJdbcTemplate = false;
        private boolean useCloseableIterables = false;
        private boolean useCheckSingleRowUpdates = false;
        private boolean useBatchInserts = false;
        private boolean useTemplateStringSubstitution = false;
        private boolean useUnderscoredToCamel = true;
        private boolean generateInterfacesForColumns = false;
        private String selectRegex = "^select[a-zA-Z][a-zA-Z0-9_$]*$";
        private String updateRegex = "^(?:insert|update|delete|create|drop)[a-zA-Z][a-zA-Z0-9_$]*$";
        private String templateRegex = "^[a-zA-Z0-9_$]*Template$";
        private String templateValueConstraintRegex = "^[a-zA-Z0-9_$]*$";
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
         * Whether to generate {@code CloseableIterable} methods, false by default
         *
         * @param useCloseableIterables whether to generate {@code CloseableIterable} methods
         */
        public void setUseCloseableIterables(boolean useCloseableIterables) {
            this.useCloseableIterables = useCloseableIterables;
        }

        /**
         * Whether to generate additional update methods, those check that
         * only single row was changed on update
         *
         * @param useCheckSingleRowUpdates whether to generate additional update methods, those check that
         *                                 only single row was changed on update
         * @return builder itself
         */
        public Builder setUseCheckSingleRowUpdates(boolean useCheckSingleRowUpdates) {
            this.useCheckSingleRowUpdates = useCheckSingleRowUpdates;
            return this;
        }

        /**
         * Whether to generate additional insert (DML) methods (with parameters), those
         * takes {@link java.util.Iterator} of parameters and execute inserts
         *
         * @param useBatchInserts whether to generate additional insert (DML) methods (with parameters), those
         *                        takes {@link java.util.Iterator} of parameters and execute inserts
         * @return builder itself
         */
        public Builder setUseBatchInserts(boolean useBatchInserts) {
            this.useBatchInserts = useBatchInserts;
            return this;
        }

        /**
         * Whether to recognize query templates on method generation
         *
         * @param useTemplateStringSubstitution whether to recognize query templates on method generation
         * @return builder itself
         */
        public Builder setUseTemplateStringSubstitution(boolean useTemplateStringSubstitution) {
            this.useTemplateStringSubstitution = useTemplateStringSubstitution;
            return this;
        }

        /**
         * Whether to convert underscored parameter named to camel ones
         *
         * @param useUnderscoredToCamel whether to convert underscored parameter named to camel ones
         * @return builder itself
         */
        public Builder setUseUnderscoredToCamel(boolean useUnderscoredToCamel) {
            this.useUnderscoredToCamel = useUnderscoredToCamel;
            return this;
        }

        /**
         * Whether to generate interfaces for columns
         *
         * @param generateInterfacesForColumns whether to generate interfaces for columns
         */
        public void setGenerateInterfacesForColumns(boolean generateInterfacesForColumns) {
            this.generateInterfacesForColumns = generateInterfacesForColumns;
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
         * default: {@code ^(?:insert|update|delete|create|drop)[a-zA-Z][a-zA-Z0-9_$]*$}
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
         * <pre>{@code
         * {
         *      "String": "java.lang.String",
         *      "Text": "java.lang.String",
         *      "_text": "java.lang.String",
         *      "Name": "java.lang.String",
         *      "_name": "java.lang.String",
         *      "Bool": "boolean"",
         *      "_bool": "boolean"",
         *      "Short": "short",
         *      "_short": "short",
         *      "Int": "int",
         *      "_int": "int",
         *      "Long": "long",
         *      "_long": "long",
         *      "Id": "long",
         *      "_id": "long",
         *      "Number": "long",
         *      "_number": "long",
         *      "Count": "long",
         *      "_count": "long",
         *      "Size": "long",
         *      "_size": "long",
         *      "Float": "float",
         *      "_float": "float",
         *      "Double": "double",
         *      "_double": "double",
         *      "Numeric": "java.math.BigDecimal",
         *      "_numeric": "java.math.BigDecimal",
         *      "Binary": "byte[]",
         *      "_binary": "byte[]",
         *      "Date": "java.util.Date",
         *      "_date": "java.util.Date",
         *      "List": "java.util.Collection",
         *      "_list": "java.util.Collection",
         *      "Collection": "java.util.Collection",
         *      "_collection": "java.util.Collection",
         *      "Set": "java.util.Collection",
         *      "_set": "java.util.Collection"
         * }
         * }</pre>
         *
         * @param typeIdMap postfix->type mapping
         * @return builder itself
         */
        public Builder setTypeIdMap(Map<String, Class<?>> typeIdMap) {
            this.typeIdMap = typeIdMap;
            return this;
        }

        /**
         * Regular expression to recognize query templates by name,
         * default value: {@code ^[a-zA-Z0-9_$]*Template$}
         *
         * @param templateRegex template recognition regex
         * @return builder itself
         */
        public Builder setTemplateRegex(String templateRegex) {
            this.templateRegex = templateRegex;
            return this;
        }

        /**
         * Regular expression constraint for template substitution values,
         * default value: {@code ^[a-zA-Z0-9_$]*$}
         *
         * @param templateValueConstraintRegex regular expression constraint for template substitution values
         * @return builder itself
         */
        public Builder setTemplateValueConstraintRegex(String templateValueConstraintRegex) {
            this.templateValueConstraintRegex = templateValueConstraintRegex;
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
            return new CodeGenerator(isPublic, useIterableJdbcTemplate, useCloseableIterables, useCheckSingleRowUpdates,
                    useBatchInserts, useTemplateStringSubstitution, selectRegex, updateRegex, templateRegex,
                    templateValueConstraintRegex, useUnderscoredToCamel, generateInterfacesForColumns, typeIdMap,
                    freemarkerTemplate, freemarkerConf);
        }

        private static Map<String, Class<?>> defaultTypeIdMap() {
            Map<String, Class<?>> map = new LinkedHashMap<String, Class<?>>();
            map.put("String", String.class);
            map.put("_string", String.class);
            map.put("Text", String.class);
            map.put("_text", String.class);
            map.put("Name", String.class);
            map.put("_name", String.class);
            map.put("Bool", boolean.class);
            map.put("_bool", boolean.class);
            map.put("Short", short.class);
            map.put("_short", short.class);
            map.put("Int", int.class);
            map.put("_int", int.class);
            map.put("Long", long.class);
            map.put("_long", long.class);
            map.put("Id", long.class);
            map.put("_id", long.class);
            map.put("Number", long.class);
            map.put("_number", long.class);
            map.put("Count", long.class);
            map.put("_count", long.class);
            map.put("Size", long.class);
            map.put("_size", long.class);
            map.put("Float", float.class);
            map.put("_float", float.class);
            map.put("Double", double.class);
            map.put("_double", double.class);
            map.put("Numeric", BigDecimal.class);
            map.put("_numeric", BigDecimal.class);
            map.put("Binary", byte[].class);
            map.put("_binary", byte[].class);
            map.put("Date", Date.class);
            map.put("_date", Date.class);
            map.put("List", Collection.class);
            map.put("_list", Collection.class);
            map.put("Collection", Collection.class);
            map.put("_collection", Collection.class);
            map.put("Set", Collection.class);
            map.put("_set", Collection.class);
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
