package com.alexkasko.springjdbc.typedqueries.codegen;

/**
 * Query parameter argument class for freemarker template
 * Made public to conform freemarker's requirements
 *
 * @author alexkasko
 * Date: 12/23/12
 */
public class ParamTemplateArg {
    private final String name;
    private final Class<?> type;

    /**
     * Constructor
     *
     * @param name parameter name
     * @param type parameter type
     */
    ParamTemplateArg(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Parameter name accessor
     *
     * @return parameter name
     */
    public String getName() {
        return name;
    }

    /**
     * Parameter type accessor
     *
     * @return parameter type
     */
    public String getType() {
        return type.getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParamTemplateArg that = (ParamTemplateArg) o;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    /**
     * {@inheritDoc}
     */
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
