package com.alexkasko.springjdbc.beanqueries.mavenplugin;

import com.alexkasko.springjdbc.beanqueries.codegen.CodeGenerator;
import com.alexkasko.springjdbc.beanqueries.common.SqlQueriesParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alexkasko.springjdbc.beanqueries.codegen.CodeGenerator.DEFAULT_FREEMARKER_TEMPLATE;
import static com.alexkasko.springjdbc.beanqueries.codegen.CodeGenerator.DEFAULT_SELECT_REGEX;
import static com.alexkasko.springjdbc.beanqueries.codegen.CodeGenerator.DEFAULT_UPDATE_REGEX;
import static com.alexkasko.springjdbc.beanqueries.common.SqlQueriesParser.DEFAULT_BODY_LINE_REGEX;
import static com.alexkasko.springjdbc.beanqueries.common.SqlQueriesParser.DEFAULT_COMMENT_REGEX;
import static com.alexkasko.springjdbc.beanqueries.common.SqlQueriesParser.DEFAULT_QUERY_NAME_REGEX;
import static java.lang.Character.toUpperCase;
import static org.apache.commons.io.FileUtils.openOutputStream;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * User: alexkasko
 * Date: 12/22/12
 *
 * @goal codegen
 * @phase generate-sources
 */
public class GenerateBeanQueriesMojo extends AbstractMojo {
    private static final Pattern PACKAGE_TAIL_NAME_REGEX = Pattern.compile("^.*\\.([a-zA-Z0-9_]+)$");

    /**
     * Queries file
     *
     * @parameter expression="${sbqcodegen.queriesFile}"
     * @required
     */
    private File queriesFile;
    /**
     * Regex for parsing query name from queries file
     *
     * @parameter expression="${sbqcodegen.queryNameRegex}"
     */
    private String queryNameRegexParam;
    /**
     * Regex for parsing comments and passed (empty) lines from queries file
     *
     * @parameter expression="${sbqcodegen.commentRegex}"
     */
    private String commentRegexParam;
    /**
     * Regex for parsing sql queries body lines
     *
     * @parameter expression="${sbqcodegen.bodyLineRegex}"
     */
    private String bodyLineRegexParam;
    /**
     * Generated package name
     *
     * @parameter expression="${sbqcodegen.packageName}"
     */
    private String packageNameParam;
    /**
     * Generated class name
     *
     * @parameter expression="${sbqcodegen.className}"
     */
    private String classNameParam;
    /**
     * Template file
     *
     * @parameter expression="${sbqcodegen.freemarkerTemplate}"
     */
    private File templateFile;
    /**
     * Regex for 'select' queries names
     *
     * @parameter expression="${sbqcodegen.selectRegex}"
     */
    private String selectRegexParam;
    /**
     * Regex for 'update' queries names
     *
     * @parameter expression="${sbqcodegen.updateRegex}"
     */
    private String updateRegexParam;
    /**
     * Whether to check SQL file date and skip code generation
     *
     * @parameter expression="${sbqcodegen.checkSqlFileDate}"
     */
     private boolean checkSqlFileDate = true;
    /**
     * @parameter expression="${project.basedir}"
     * @required
     * @readonly
     */
    private File baseDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        FileInputStream is = null;
        Writer outWriter = null;
        try {
            // resolve params
            if(null == queriesFile) throw new MojoFailureException("'queriesFile' parameter is required");
            String queryNameRegex = null != queryNameRegexParam ? queryNameRegexParam : DEFAULT_QUERY_NAME_REGEX;
            String commentRegex = null != commentRegexParam ? commentRegexParam : DEFAULT_COMMENT_REGEX;
            String bodyLineRegex = null != bodyLineRegexParam ? bodyLineRegexParam : DEFAULT_BODY_LINE_REGEX;
            String packageName = null != packageNameParam ? packageNameParam : getBaseName(queriesFile.getName());
            String className = null != classNameParam ? classNameParam :  extractPackageTail(packageName) + "BeanQueries";
            String template = null != templateFile ? readFileToString(templateFile, "UTF-8") : DEFAULT_FREEMARKER_TEMPLATE;
            String selectRegex = null != selectRegexParam ? selectRegexParam : DEFAULT_SELECT_REGEX;
            String updateRegex = null != updateRegexParam ? updateRegexParam : DEFAULT_UPDATE_REGEX;
            // parse queries
            is = FileUtils.openInputStream(queriesFile);
            Map<String, String> queries = new SqlQueriesParser(queryNameRegex, commentRegex, bodyLineRegex).parse(is);
            // create and check file
            File outFile = new File(baseDirectory, "src/main/java/" + packageName.replace(".", "/") + "/" + className + ".java");
            if(checkSqlFileDate && outFile.lastModified() >= queriesFile.lastModified()) {
                getLog().info("Bean queries file: [" + outFile.getAbsolutePath() + "] is up to date, skipping code generation");
                return;
            }
            // generate file
            getLog().info("Writing class: [" + packageName + "." + className + "] to file: [" + outFile.getAbsolutePath() + "]");
            outWriter = new OutputStreamWriter(openOutputStream(outFile), "UTF-8");
            new CodeGenerator(selectRegex, updateRegex, template).generate(packageName, className, queries, outWriter);
        } catch(IOException e) {
            throw new MojoFailureException("IO error", e);
        } finally {
            closeQuietly(is);
            closeQuietly(outWriter);
        }
    }

    private String extractPackageTail(String packageName) throws MojoFailureException {
        Matcher matcher = PACKAGE_TAIL_NAME_REGEX.matcher(packageName);
        String tail = matcher.matches() ? matcher.group(1) : packageName;
        if(1 == tail.length()) return tail.toUpperCase(Locale.ENGLISH);
        char first = Character.toUpperCase(tail.charAt(0));
        return first + tail.substring(1);
    }
}
