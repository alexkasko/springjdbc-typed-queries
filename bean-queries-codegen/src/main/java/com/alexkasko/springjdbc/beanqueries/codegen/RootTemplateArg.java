package com.alexkasko.springjdbc.beanqueries.codegen;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * User: alexkasko
 * Date: 12/22/12
 */
public class RootTemplateArg {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final String packageName;
    private final String className;
    private final String modifier;
    private final Collection<QueryTemplateArg> selects;
    private final Collection<QueryTemplateArg> updates;

    public RootTemplateArg(String packageName, String className, String modifier, Collection<QueryTemplateArg> selects, Collection<QueryTemplateArg> updates) {
        this.packageName = packageName;
        this.className = className;
        this.modifier = modifier;
        this.selects = selects;
        this.updates = updates;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getModifier() {
        return modifier.length() > 0 ? modifier + " " : modifier;
    }

    public Collection<QueryTemplateArg> getSelects() {
        return selects;
    }

    public Collection<QueryTemplateArg> getUpdates() {
        return updates;
    }

    public String getCurrentDate() {
        return new SimpleDateFormat(DATE_FORMAT).format(new Date());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TemplateParams");
        sb.append("{packageName='").append(packageName).append('\'');
        sb.append(", className='").append(className).append('\'');
        sb.append(", selects=").append(selects);
        sb.append(", updates=").append(updates);
        sb.append('}');
        return sb.toString();
    }
}
