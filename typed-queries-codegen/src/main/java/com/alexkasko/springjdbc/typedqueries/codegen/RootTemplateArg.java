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
    private final String sourceSqlFileName;
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
     * @param sourceSqlFileName name of source SQL file
     * @param selects list of 'select' queries
     * @param updates list of 'update' queries
     */
    RootTemplateArg(String packageName, String className, String modifier, boolean useIterableJdbcTemplate,
                           String sourceSqlFileName, Collection<QueryTemplateArg> selects, Collection<QueryTemplateArg> updates) {
        this.packageName = packageName;
        this.className = className;
        this.modifier = modifier;
        this.useIterableJdbcTemplate = useIterableJdbcTemplate;
        this.sourceSqlFileName = sourceSqlFileName;
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
     * Source SQL file name accessor
     *
     * @return source SQL file name
     */
    public String getSourceSqlFileName() {
        return sourceSqlFileName;
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
        sb.append(", sourceSqlFileName='").append(sourceSqlFileName).append('\'');
        sb.append(", selects=").append(selects);
        sb.append(", updates=").append(updates);
        sb.append('}');
        return sb.toString();
    }
}