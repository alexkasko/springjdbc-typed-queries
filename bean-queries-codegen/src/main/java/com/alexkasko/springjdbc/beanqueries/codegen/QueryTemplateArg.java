package com.alexkasko.springjdbc.beanqueries.codegen;

import java.util.Collection;

/**
 * User: alexkasko
 * Date: 12/23/12
 */
public class QueryTemplateArg {
    private final String name;
    private final Collection<ParamTemplateArg> params;

    QueryTemplateArg(String name, Collection<ParamTemplateArg> params) {
        this.name = name;
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public Collection<ParamTemplateArg> getParams() {
        return params;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("QueryTemplateArg");
        sb.append("{name='").append(name).append('\'');
        sb.append(", params=").append(params);
        sb.append('}');
        return sb.toString();
    }
}
