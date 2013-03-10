package com.alexkasko.springjdbc.typedqueries.codegen;

import java.util.Collection;

/**
 * Query argument class for freemarker template
 * Made public to conform freemarker's requirements
 *
 * @author alexkasko
 * Date: 12/23/12
 */
public class QueryTemplateArg {
    private final String name;
    private final Collection<ParamTemplateArg> params;
    private final boolean template;

    /**
     * Constructor
     *
     * @param name query name
     * @param params query parameters list
     * @param template whether this query needs template substitutions
     */
    QueryTemplateArg(String name, Collection<ParamTemplateArg> params, boolean template) {
        this.name = name;
        this.params = params;
        this.template = template;
    }

    /**
     * Query name accessor
     *
     * @return query name
     */
    public String getName() {
        return name;
    }

    /**
     * Query parameters list accessor
     *
     * @return query parameters list
     */
    public Collection<ParamTemplateArg> getParams() {
        return params;
    }

    /**
     * Whether this query needs template substitutions
     *
     * @return whether this query needs template substitutions
     */
    public boolean isTemplate() { return template; }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("QueryTemplateArg");
        sb.append("{name='").append(name).append('\'');
        sb.append(", params=").append(params);
        sb.append(", template=").append(template);
        sb.append('}');
        return sb.toString();
    }
}
