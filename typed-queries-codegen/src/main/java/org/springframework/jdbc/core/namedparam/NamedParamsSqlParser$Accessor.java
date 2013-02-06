package org.springframework.jdbc.core.namedparam;

import java.util.List;

import static org.springframework.jdbc.core.namedparam.NamedParameterUtils.parseSqlStatement;

/**
 * Helper class for accessing package-private method
 * {@code org.springframework.jdbc.core.namedparam.ParsedSql.getParameterNames()}
 *
 * @author alexkasko
 * Date: 12/23/12
 */
public class NamedParamsSqlParser$Accessor {

    /**
     * Parse list of named parameters from SQL query
     *
     * @param sql query text
     * @return list of named parameters
     */
    public static List<String> parseParamsNames(String sql) {
        return parseSqlStatement(sql).getParameterNames();
    }
}
