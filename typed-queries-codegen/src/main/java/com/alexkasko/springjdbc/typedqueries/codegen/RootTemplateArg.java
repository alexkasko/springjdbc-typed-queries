package com.alexkasko.springjdbc.typedqueries.codegen;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * Root argument class for freemarker template.
 * Made public to conform freemarker's requirements
 *
 * @author alexkasko
 * Date: 12/22/12
 */
public class RootTemplateArg {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final String packageName;
    private final String className;
    private final String modifier;
    private final boolean useIterableJdbcTemplate;
    private final boolean useCheckSingleRowUpdates;
    private final boolean useBatchInserts;
    private final boolean useTemplateStringSubstitution;
    private final boolean useUnderscoredToCamel;
    private final boolean generateInterfacesForColumns;
    private final String sourceSqlFileName;
    private final String templateValueConstraintRegex;
    private final Collection<QueryTemplateArg> selects;
    private final Collection<QueryTemplateArg> updates;

    /**
     * Constructor
     *
     * @param packageName package name for generated class
     * @param className generated class name
     * @param modifier class and methods modifier, may be 'public', empty (package-private) by default
     * @param useIterableJdbcTemplate whether to use JdbcTemplate extension from this project
*                                (https://github.com/alexkasko/springjdbc-iterable)
     * @param useCheckSingleRowUpdates whether to generate additional update methods, those check that
*                                 only single row was changed on update
     * @param useBatchInserts whether to generate additional insert (DML) methods (with parameters), those
*                        takes {@link java.util.Iterator} of parameters and execute inserts
*                        for the contents of the specified iterator in batch mode
     * @param useTemplateStringSubstitution whether to recognize query templates on method generation
     * @param useUnderscoredToCamel whether to convert underscored parameter named to camel ones
     * @param generateInterfacesForColumns whether to generate interfaces for columns
     * @param sourceSqlFileName name of source SQL file
     * @param templateValueConstraintRegex regular expression constraint for template substitution values
     * @param selects list of 'select' queries
     * @param updates list of 'update' queries
     */
    RootTemplateArg(String packageName, String className, String modifier, boolean useIterableJdbcTemplate,
                    boolean useCheckSingleRowUpdates, boolean useBatchInserts, boolean useTemplateStringSubstitution, boolean useUnderscoredToCamel, boolean generateInterfacesForColumns, String sourceSqlFileName,
                    String templateValueConstraintRegex, Collection<QueryTemplateArg> selects, Collection<QueryTemplateArg> updates) {
        this.packageName = packageName;
        this.className = className;
        this.modifier = modifier;
        this.useIterableJdbcTemplate = useIterableJdbcTemplate;
        this.useCheckSingleRowUpdates = useCheckSingleRowUpdates;
        this.useBatchInserts = useBatchInserts;
        this.useTemplateStringSubstitution = useTemplateStringSubstitution;
        this.useUnderscoredToCamel = useUnderscoredToCamel;
        this.generateInterfacesForColumns = generateInterfacesForColumns;
        this.sourceSqlFileName = sourceSqlFileName;
        this.templateValueConstraintRegex = templateValueConstraintRegex;
        this.selects = selects;
        this.updates = updates;
    }

    /**
     * Package name accessor
     *
     * @return package name
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Class name accessor
     *
     * @return class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Modifier accessor
     *
     * @return modifier
     */
    public String getModifier() {
        return modifier.length() > 0 ? modifier + " " : modifier;
    }

    /**
     * Whether to use iterable extensions flag accessor
     *
     * @return whether to use iterable extensions flag
     */
    public boolean isUseIterableJdbcTemplate() {
        return useIterableJdbcTemplate;
    }

    /**
     * Whether to generate additional update methods, those check that
     * only single row was changed on update
     *
     * @return whether to generate update-check methods
     */
    public boolean isUseCheckSingleRowUpdates() { return useCheckSingleRowUpdates; }

    /**
     * Whether to generate additional insert (DML) methods (with parameters), those
     * takes {@link java.util.Iterator} of parameters and execute inserts
     * for the contents of the specified iterator in batch mode
     *
     * @return whether to generate batch-insert methods
     */
    public boolean isUseBatchInserts() { return useBatchInserts; }

    /**
     * Whether to recognize query templates on method generation
     *
     * @return whether to recognize query templates on method generation
     */
    public boolean isUseTemplateStringSubstitution() { return useTemplateStringSubstitution; }

    /**
     * Whether to convert underscored parameter named to camel ones
     *
     * @return whether to convert underscored parameter named to camel ones
     */
    public boolean isUseUnderscoredToCamel() {
        return useUnderscoredToCamel;
    }

    /**
     * Whether to generate interfaces for columns
     *
     * @return whether to generate interfaces for columns
     */
    public boolean isGenerateInterfacesForColumns() {
        return generateInterfacesForColumns;
    }

    /**
     * Source SQL file name accessor
     *
     * @return source SQL file name
     */
    public String getSourceSqlFileName() {
        return sourceSqlFileName;
    }

    /**
     * Regular expression constraint for template substitution values
     *
     * @return regular expression constraint for template substitution values
     */
    public String getTemplateValueConstraintRegex() {
        return templateValueConstraintRegex;
    }

    /**
     * 'Select' queries accessor
     *
     * @return 'select' queries list
     */
    public Collection<QueryTemplateArg> getSelects() {
        return selects;
    }

    /**
     * 'Update' queries accessor
     *
     * @return 'update' queries list
     */
    public Collection<QueryTemplateArg> getUpdates() {
        return updates;
    }

    /**
     * Current date accessor
     *
     * @return current date
     */
    public String getCurrentDate() {
        return new SimpleDateFormat(DATE_FORMAT).format(new Date());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("RootTemplateArg");
        sb.append("{packageName='").append(packageName).append('\'');
        sb.append(", className='").append(className).append('\'');
        sb.append(", modifier='").append(modifier).append('\'');
        sb.append(", useIterableJdbcTemplate=").append(useIterableJdbcTemplate);
        sb.append(", useCheckSingleRowUpdates=").append(useCheckSingleRowUpdates);
        sb.append(", useBatchInserts=").append(useBatchInserts);
        sb.append(", useTemplateStringSubstitution=").append(useTemplateStringSubstitution);
        sb.append(", sourceSqlFileName='").append(sourceSqlFileName).append('\'');
        sb.append(", templateValueConstraintRegex='").append(templateValueConstraintRegex).append('\'');
        sb.append(", selects=").append(selects);
        sb.append(", updates=").append(updates);
        sb.append('}');
        return sb.toString();
    }
}