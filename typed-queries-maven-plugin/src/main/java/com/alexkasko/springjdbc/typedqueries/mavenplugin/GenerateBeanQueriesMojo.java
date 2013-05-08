package com.alexkasko.springjdbc.typedqueries.mavenplugin;

import com.alexkasko.springjdbc.typedqueries.codegen.CodeGenerator;
import com.alexkasko.springjdbc.typedqueries.common.PlainSqlQueriesParser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.springframework.util.ClassUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.apache.commons.io.FileUtils.openInputStream;
import static org.apache.commons.io.FileUtils.openOutputStream;
import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Maven-plugin, takes file with SQL queries and generates java methods for executing each query.
 * For queries, that take input parameters, also generates an interface containing typed parameters getters.
 *
 * @author alexkasko
 * Date: 12/22/12
 *
 * @goal codegen
 * @phase generate-sources
 */
public class GenerateBeanQueriesMojo extends AbstractMojo {
    /**
     * Queries file
     *
     * @parameter expression="${typedqueries.queriesFile}"
     * @required
     */

    private File queriesFile;
    /**
     * Queries file
     *
     * @parameter expression="${typedqueries.queriesFile}"
     *            default-value="UTF-8"
     */
    private String queriesFileEncoding;
    /**
     * Generated class name, uses query file name bt default
     *
     * @parameter expression="${typedqueries.fullClassName}"
     */
    private String fullClassName;
    /**
     * Whether to check SQL file date and skip code generation
     *
     * @parameter expression="${typedqueries.checkSqlFileDate}"
     *            default-value="true"
     */
     private boolean checkSqlFileDate;
    /**
     * Whether to make generated class and its methods public,
     * package-private by default
     *
     * @parameter expression="${typedqueries.isPublic}"
     */
    private boolean isPublic;
    /**
     * Whether to use iterable jdbc template extensions from this
     * project (https://github.com/alexkasko/springjdbc-iterable),
     * false by default
     *
     * @parameter expression="${typedqueries.useIterableJdbcTemplate}"
     */
    private boolean useIterableJdbcTemplate;
    /**
     * Whether to generate additional update methods, those check that
     * only single row was changed on update, false by default
     *
     * @parameter expression="${typedqueries.useCheckSingleRowUpdates}"
     */
    private boolean useCheckSingleRowUpdates;
    /**
     * Whether to generate additional insert (DML) methods (with parameters), those
     * takes {@link java.util.Iterator} of parameters and execute inserts
     * for the contents of the specified iterator in batch mode, false by default
     *
     * @parameter expression="${typedqueries.useBatchInserts}"
     */
    private boolean useBatchInserts;
    /**
     * Whether to recognize query templates on method generation, false by default
     *
     * @parameter expression="${typedqueries.useTemplateStringSubstitution}"
     */
    private boolean useTemplateStringSubstitution;
    /**
     * Цhether to convert underscored parameter named to camel ones, true by default
     *
     * @parameter expression="${typedqueries.useUnderscoredToCamel}" default-value="true"
     */
    private boolean useUnderscoredToCamel;
    /**
     * Regular expression to use for identifying 'select' queries by name,
     * default: '^select[a-zA-Z][a-zA-Z0-9_$]*$'
     *
     * @parameter expression="${typedqueries.selectRegex}"
     */
    private String selectRegex;
    /**
     * Regular expression to use for identifying 'insert', 'update' and 'delete' queries by name,
     * default: '^(?:insert|update|delete|create|drop)[a-zA-Z][a-zA-Z0-9_$]*$'
     *
     * @parameter expression="${typedqueries.updateRegex}"
     */
    private String updateRegex;
    /**
     * Regular expression to recognize query templates by name,
     * default: '^[a-zA-Z0-9_$]*Template$'
     *
     * @parameter expression="${typedqueries.templateRegex}"
     */
    private String templateRegex;
    /**
     * Regular expression constraint for template substitution values,
     * default: '^[a-zA-Z0-9_$]*$'
     *
     * @parameter expression="${typedqueries.templateValueConstraintRegex}"
     */
    private String templateValueConstraintRegex;
    /**
     * Mapping of query parameter names postfixes to data types in JSON map format, e.g.
     * '{"_long": "long", "_byte_a": "byte[]", "_date": "java.util.Date"}', see default in 'CodeGenerator' javadoc
     *
     * @parameter expression="${typedqueries.typeIdMapJson}"
     */
    private String typeIdMapJson;
    /**
     * FreeMarker template file for generating class
     *
     * @parameter expression="${typedqueries.freemarkerTemplate}"
     */
    private File templateFile;
    /**
     * FreeMarker template file encoding, 'UTF-8' by default
     *
     * @parameter expression="${typedqueries.freemarkerTemplateEncoding}"
     *            default-value="UTF-8"
     */
    private String templateFileEncoding;
    /**
     * @parameter expression="${project.basedir}"
     * @required
     * @readonly
     */
    private File baseDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Writer outWriter = null;
        try {
            if(!(queriesFile.exists() && queriesFile.isFile())) throw new FileNotFoundException(queriesFile.getAbsolutePath());
            String fcn = null != fullClassName ? fullClassName : FilenameUtils.removeExtension(queriesFile.getName());
            File outFile = new File(baseDirectory, "src/main/java/" + fcn.replace(".", "/") + ".java");
            if (checkSqlFileDate && outFile.exists() && outFile.isFile() && outFile.length() > 0 &&
                    outFile.lastModified() >= queriesFile.lastModified()) {
                getLog().info("Typed queries file: [" + outFile.getAbsolutePath() + "] is up to date, skipping code generation");
                return;
            }
            Map<String, String> queries = readFile(queriesFile);
            CodeGenerator.Builder builder = CodeGenerator.builder();
            if(isPublic) builder.setPublic(isPublic);
            if(useIterableJdbcTemplate) builder.setUseIterableJdbcTemplate(true);
            if(useCheckSingleRowUpdates) builder.setUseCheckSingleRowUpdates(true);
            if(useBatchInserts) builder.setUseBatchInserts(true);
            if(useTemplateStringSubstitution) builder.setUseTemplateStringSubstitution(true);
            if(!useUnderscoredToCamel) builder.setUseTemplateStringSubstitution(false);
            if(null != selectRegex) builder.setSelectRegex(selectRegex);
            if(null != updateRegex) builder.setUpdateRegex(updateRegex);
            if(null != templateRegex) builder.setTemplateRegex(templateRegex);
            if(null != templateValueConstraintRegex) builder.setTemplateValueConstraintRegex(templateValueConstraintRegex);
            if(null != typeIdMapJson) builder.setTypeIdMap(parseTypeIdMap(typeIdMapJson));
            if(null != templateFile) builder.setFreemarkerTemplate(FileUtils.readFileToString(templateFile, templateFileEncoding));
            CodeGenerator cg = builder.build();
            CountingOutputStream counter = new CountingOutputStream(openOutputStream(outFile));
            outWriter = new OutputStreamWriter(counter, "UTF-8");
            getLog().info("Generating queries wrapper for file: [" + queriesFile.getAbsolutePath() + "] " +
                    "into java file: [" + outFile.getAbsolutePath() + "]");
            cg.generate(queries, fcn, queriesFile.getName(), outWriter);
            getLog().info("Writing compete, bytes written: [" + counter.getCount() + "]");
        } catch(IOException e) {
            throw new MojoFailureException("IO error", e);
        } catch (ClassNotFoundException e) {
            throw new MojoFailureException("Type id map error", e);
        } finally {
            closeQuietly(outWriter);
        }
    }

    @SuppressWarnings("unchecked") // properties API
    private Map<String, String> readFile(File file) throws IOException {
        if (!file.exists() && file.isFile()) throw new FileNotFoundException(
                "Cannot read queries file: [" + file.getAbsolutePath() + "]");
        InputStream is = null;
        try {
            String fname = file.getName().toLowerCase();
            final Map<String, String> res;
            is = openInputStream(file);
            if (fname.endsWith("sql")) {
                res = new PlainSqlQueriesParser().parse(is, queriesFileEncoding);
            } else if (fname.endsWith("json")) {
                Reader re = new InputStreamReader(is, queriesFileEncoding);
                Type mapType = new TypeToken<LinkedHashMap<String, String>>(){}.getType();
                res = new Gson().fromJson(re, mapType);
            } else if (fname.endsWith("properties")) {
                Properties props = new Properties();
                props.load(new InputStreamReader(is, queriesFileEncoding));
                res = (Map) props;
            } else if (fname.endsWith("xml")) {
                Properties props = new Properties();
                props.loadFromXML(is);
                res = (Map) props;
            } else throw new IOException("Cannot parse queries file: [" + file.getAbsolutePath() +"], " +
                    "only '*.sql', '*.json', '*.properties' and '*.xml' files are supported");
            return res;
        } finally {
            closeQuietly(is);
        }
    }

    private Map<String, Class<?>> parseTypeIdMap(String json) throws ClassNotFoundException {
        Type mapType = new TypeToken<LinkedHashMap<String, String>>(){}.getType();
        Map<String, String> strings = new Gson().fromJson(json, mapType);
        Map<String, Class<?>> res = new LinkedHashMap<String, Class<?>>();
        for(Map.Entry<String, String> en : strings.entrySet()) {
            Class<?> clazz = ClassUtils.forName(en.getValue(), GenerateBeanQueriesMojo.class.getClassLoader());
            res.put(en.getKey(), clazz);
        }
        return res;
    }
}