[#ftl encoding="UTF-8"/]
package ${packageName};

import com.alexkasko.springjdbc.beanqueries.common.BeanQueriesException;
import com.alexkasko.springjdbc.iterable.IterableNamedParameterJdbcTemplate;
import com.alexkasko.springjdbc.iterable.IterableNamedParameterJdbcOperations;
import com.alexkasko.springjdbc.iterable.CloseableIterator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * Generated on ${currentDate} by {@code com.alexkasko.springjdbc.bean-queries-codegen}
 * Do not change this file, change source SQL file instead.
 * To regenerate this file run {@code mvn bean-queries:codegen} from this maven module directory.
 */
class ${className} {
    private static final Set<String> GENERATED_QUERIES_NAMES;

    private final Map<String, String> queries;
    private final IterableNamedParameterJdbcTemplate jt;

    // static initializer for query names known at generation time
    // added for queries check on instance construction time
    static {
        // fill names array
        List<String> genNamesList = new ArrayList<String>();
[#list selects as methodName]
        genNamesList.add("${methodName}");
[/#list]
[#list updates as methodName]
        genNamesList.add("${methodName}");
[/#list]
        // check duplicate names
        Set<String> getNamesSet = new LinkedHashSet<String>(genNamesList.size());
        for(String name : genNamesList) {
            boolean unique = getNamesSet.add(name);
            if(!unique) throw new BeanQueriesException("Duplicate query name: [" + name + "] in list: [" + genNamesList + "]");
        }
        // set names that will be used in constructor check
        GENERATED_QUERIES_NAMES = unmodifiableSet(getNamesSet);
    }

    /**
     * Constructor
     *
     * @param queries 'name'->'sql' map
     * @param jt jdbc template
     * @throws BeanQueriesException if provided queries names are not consistent with generated ones
     */
    ${className}(Map<String, String> queries, IterableNamedParameterJdbcTemplate jt) {
        if(null == queries) throw new BeanQueriesException("Provided queries map is null");
        if(null == jt) throw new BeanQueriesException("Provided JdbcTemplate is null");
        if(queries.size() != GENERATED_QUERIES_NAMES.size()) throw new BeanQueriesException(
                "Provided queries: [" + queries.keySet() + "] are non consistent with queries " +
                " known on generation time: [" + GENERATED_QUERIES_NAMES + "]: size differs");
        for(String name : queries.keySet()) if(!GENERATED_QUERIES_NAMES.contains(name)) throw new BeanQueriesException(
                "Provided queries: [" + queries.keySet() + "] are non consistent with queries " +
                " known on generation time: [" + GENERATED_QUERIES_NAMES + "]: unknown name: [" + name + "]");
        this.queries = unmodifiableMap(queries);
        this.jt = jt;
    }

    /**
     * JdbcTemplate accessor
     *
     * @return jdbc template
     */
    IterableNamedParameterJdbcTemplate jt() {
        return this.jt;
    }

    // select methods

    /**
     * Checks client-provided arguments and returns query sql text
     *
     * @param jt jdbc template
     * @param name query name
     * @param paramsBean query parameters
     * @param mapper row mapper
     * @return query sql text
     * @throws BeanQueriesException if provided arguments are null or queries are inconsistent
     */
    private String checkAndGetSql(Object jt, String name, Object paramsBean, RowMapper<?> mapper) {
        if(null == jt) throw new BeanQueriesException("Provided JdbcTemplate is null");
        if(null == paramsBean) throw new BeanQueriesException("Provided params object is null");
        if(null == mapper) throw new BeanQueriesException("Provided mapper object is null");
        String sql = queries.get(name);
        if(null == sql) throw new BeanQueriesException("No query found with name: [" + name + "], queries: [" + queries.keySet() + "]");
        return sql;
    }

[#list selects as methodName]
    // ${methodName} methods

    <T> T ${methodName}Object(Object paramsBean, RowMapper<T> mapper) {
        return ${methodName}Object(this.jt, paramsBean, mapper);
    }

    <T> T ${methodName}Object(NamedParameterJdbcOperations jt, Object paramsBean, RowMapper<T> mapper) {
        String sql = checkAndGetSql(jt, "${methodName}", paramsBean, mapper);
        SqlParameterSource params = new BeanPropertySqlParameterSource(paramsBean);
        return jt.queryForObject(sql, params, mapper);
    }

    <T> List<T> ${methodName}(Object paramsBean, RowMapper<T> mapper) {
        return ${methodName}(this.jt, paramsBean, mapper);
    }

    <T> List<T> ${methodName}(NamedParameterJdbcOperations jt, Object paramsBean, RowMapper<T> mapper) {
        String sql = checkAndGetSql(jt, "${methodName}", paramsBean, mapper);
        SqlParameterSource params = new BeanPropertySqlParameterSource(paramsBean);
        return jt.query(sql, params, mapper);
    }

    <T> CloseableIterator<T> ${methodName}Iter(Object paramsBean, RowMapper<T> mapper) {
        return ${methodName}Iter(this.jt, paramsBean, mapper);
    }

    <T> CloseableIterator<T> ${methodName}Iter(IterableNamedParameterJdbcOperations jt, Object paramsBean, RowMapper<T> mapper) {
        String sql = checkAndGetSql(jt, "${methodName}", paramsBean, mapper);
        SqlParameterSource params = new BeanPropertySqlParameterSource(paramsBean);
        return jt.queryForIter(sql, params, mapper);
    }
[/#list]

    // update methods

    /**
     * Checks client-provided arguments and returns query sql text
     *
     * @param jt jdbc template
     * @param name query name
     * @param paramsBean query parameters
     * @return query sql text
     * @throws BeanQueriesException if provided arguments are null or queries are inconsistent
     */
     private String checkAndGetSql(Object jt, String name, Object paramsBean) {
       if(null == jt) throw new BeanQueriesException("Provided JdbcTemplate is null");
       if(null == paramsBean) throw new BeanQueriesException("Provided params object is null");
       String sql = queries.get(name);
       if(null == sql) throw new BeanQueriesException("No query found with name: [" + name + "], queries: [" + queries.keySet() + "]");
       return sql;
     }

[#list updates as methodName]
    // ${methodName} methods

    int ${methodName}(Object paramsBean) {
        return ${methodName}(this.jt, paramsBean);
    }

    int ${methodName}(NamedParameterJdbcOperations jt, Object paramsBean) {
        String sql = checkAndGetSql(jt, "${methodName}", paramsBean);
        SqlParameterSource params = new BeanPropertySqlParameterSource(paramsBean);
        return jt.update(sql, params);
    }

[/#list]
}