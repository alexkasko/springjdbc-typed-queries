[#ftl encoding="UTF-8"/]
package ${packageName};

import com.alexkasko.springjdbc.beanqueries.common.BeanQueriesException;
import com.alexkasko.springjdbc.iterable.IterableNamedParameterJdbcTemplate;
import com.alexkasko.springjdbc.iterable.CloseableIterator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Generated code, don't change
 */
class ${className} {
    private final Map<String, String> queries;
    private final IterableNamedParameterJdbcTemplate jt;

    ${className}(Map<String, String> queries, IterableNamedParameterJdbcTemplate jt) {
        if(null == queries) throw new BeanQueriesException("Provided queries map is null");
        if(null == jt) throw new BeanQueriesException("Provided JdbcTemplate is null");
        this.queries = queries;
        this.jt = jt;
    }

    // select methods

[#list selects as methodName]
    // ${methodName} methods

    <T> T ${methodName}(Object paramsBean, RowMapper<T> mapper) {
        if(null == paramsBean) throw new BeanQueriesException("Provided params object is null");
        if(null == mapper) throw new BeanQueriesException("Provided mapper object is null");
        String sql = queries.get("${methodName}");
        if(null == sql) throw new BeanQueriesException("No query found with name: [${methodName}], queries: [" + queries.keySet() + "]");
        SqlParameterSource params = new BeanPropertySqlParameterSource(paramsBean);
        return jt.queryForObject(sql, params, mapper);
    }

    <T> List<T> ${methodName}List(Object paramsBean, RowMapper<T> mapper) {
        if(null == paramsBean) throw new BeanQueriesException("Provided params object is null");
        if(null == mapper) throw new BeanQueriesException("Provided mapper object is null");
        String sql = queries.get("${methodName}");
        if(null == sql) throw new BeanQueriesException("No query found with name: [${methodName}], queries: [" + queries.keySet() + "]");
        SqlParameterSource params = new BeanPropertySqlParameterSource(paramsBean);
        return jt.query(sql, params, mapper);
    }

    <T> CloseableIterator<T> ${methodName}Iter(Object paramsBean, RowMapper<T> mapper) {
        if(null == paramsBean) throw new BeanQueriesException("Provided params object is null");
        if(null == mapper) throw new BeanQueriesException("Provided mapper object is null");
        String sql = queries.get("${methodName}");
        if(null == sql) throw new BeanQueriesException("No query found with name: [${methodName}], queries: [" + queries.keySet() + "]");
        SqlParameterSource params = new BeanPropertySqlParameterSource(paramsBean);
        return jt.queryForIter(sql, params, mapper);
    }
[/#list]

    // update methods

[#list updates as methodName]
    // ${methodName} methods

    int ${methodName}(Object paramsBean) {
        if(null == paramsBean) throw new BeanQueriesException("Provided params object is null");
        String sql = queries.get("${methodName}");
        if(null == sql) throw new BeanQueriesException("No query found with name: [${methodName}], queries: [" + queries.keySet() + "]");
        SqlParameterSource params = new BeanPropertySqlParameterSource(paramsBean);
        return jt.update(sql, params);
    }

[/#list]
}