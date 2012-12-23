package org.springframework.jdbc.core.namedparam;

import java.util.List;

import static org.springframework.jdbc.core.namedparam.NamedParameterUtils.parseSqlStatement;

/**
 * User: alexkasko
 * Date: 12/23/12
 */
public class NamedParamsSqlParser$Accessor {

    public static List<String> parseParamsNames(String sql) {
        return parseSqlStatement(sql).getParameterNames();
    }
}
