package com.alexkasko.springjdbc.beanqueries.codegen;

/**
 * User: alexkasko
 * Date: 12/23/12
 */
public class ParamTemplateArg {
    private final String name;
    private final Class type;

    ParamTemplateArg(String name, Class type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type.getSimpleName();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ParamTemplateArg");
        sb.append("{name='").append(name).append('\'');
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }
}
