package com.alexkasko.springjdbc.beanqueries.codegen;

import java.util.Collection;

/**
 * User: alexkasko
 * Date: 12/22/12
 */
public class TemplateParams {
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
