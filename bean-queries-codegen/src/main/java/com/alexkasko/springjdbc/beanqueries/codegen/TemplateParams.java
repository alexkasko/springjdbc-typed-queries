package com.alexkasko.springjdbc.beanqueries.codegen;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * User: alexkasko
 * Date: 12/22/12
 */
public class TemplateParams {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final String packageName;
    private final String className;
    private final Collection<String> selects;
    private final Collection<String> updates;

    public TemplateParams(String packageName, String className, Collection<String> selects, Collection<String> updates) {
        this.packageName = packageName;
        this.className = className;
        this.selects = selects;
        this.updates = updates;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public Collection<String> getSelects() {
        return selects;
    }

    public Collection<String> getUpdates() {
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
