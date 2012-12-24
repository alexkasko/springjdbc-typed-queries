[#ftl encoding="UTF-8"/]
package ${packageName};

import com.alexkasko.springjdbc.beanqueries.common.BeanQueriesException;
import com.alexkasko.springjdbc.iterable.IterableNamedParameterJdbcTemplate;
import com.alexkasko.springjdbc.iterable.IterableNamedParameterJdbcOperations;
import com.alexkasko.springjdbc.iterable.CloseableIterator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.util.Date;
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
${modifier}class ${className} {
    ${modifier}static final RowMapper<String> STRING_ROW_MAPPER = new SingleColumnRowMapper<String>(String.class);
    ${modifier}static final RowMapper<Integer> INT_ROW_MAPPER = new SingleColumnRowMapper<Integer>(Integer.class);
    ${modifier}static final RowMapper<Long> LONG_ROW_MAPPER = new SingleColumnRowMapper<Long>(Long.class);

    private static final Set<String> GENERATED_QUERIES_NAMES;

    private final Map<String, String> queries;
    private final IterableNamedParameterJdbcTemplate jt;

    // static initializer for query names known at generation time
    // added for queries check on instance construction time
    static {
        // fill names array
        List<String> genNamesList = new ArrayList<String>();
[#list selects as query]
        genNamesList.add("${query.name}");
[/#list]
[#list updates as query]
        genNamesList.add("${query.name}");
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
    ${modifier}${className}(Map<String, String> queries, IterableNamedParameterJdbcTemplate jt) {
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
    ${modifier}IterableNamedParameterJdbcTemplate jt() {
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
    private String checkAndGetSql(String name, Object paramsBean, RowMapper<?> mapper) {
        if(null == paramsBean) throw new BeanQueriesException("Provided params object is null");
        if(null == mapper) throw new BeanQueriesException("Provided mapper object is null");
        String sql = queries.get(name);
        if(null == sql) throw new BeanQueriesException("No query found with name: [" + name + "], queries: [" + queries.keySet() + "]");
        return sql;
    }
[#list selects as query]

    // ${query.name} methods

[#if query.params?size > 0]
    ${modifier}interface ${query.name?cap_first}$Params {
[#list query.params as param]
        ${param.type} get${param.name?cap_first}();
[/#list]
    }

    ${modifier}<T> T ${query.name}Object(${query.name?cap_first}$Params paramsBean, RowMapper<T> mapper) {
        String sql = checkAndGetSql("${query.name}", paramsBean, mapper);
        SqlParameterSource params = new BeanPropertySqlParameterSource(paramsBean);
        return jt.queryForObject(sql, params, mapper);
    }

    ${modifier}<T> List<T> ${query.name}(${query.name?cap_first}$Params paramsBean, RowMapper<T> mapper) {
        String sql = checkAndGetSql("${query.name}", paramsBean, mapper);
        SqlParameterSource params = new BeanPropertySqlParameterSource(paramsBean);
        return jt.query(sql, params, mapper);
    }

    ${modifier}<T> CloseableIterator<T> ${query.name}Iter(${query.name?cap_first}$Params paramsBean, RowMapper<T> mapper) {
        String sql = checkAndGetSql("${query.name}", paramsBean, mapper);
        SqlParameterSource params = new BeanPropertySqlParameterSource(paramsBean);
        return jt.queryForIter(sql, params, mapper);
    }

[#else]

    ${modifier}<T> T ${query.name}Object(RowMapper<T> mapper) {
        String sql = checkAndGetSql("${query.name}", "", mapper);
        return jt.getJdbcOperations().queryForObject(sql, mapper);
    }

    ${modifier}<T> List<T> ${query.name}(RowMapper<T> mapper) {
        String sql = checkAndGetSql("${query.name}", "", mapper);
        return jt.getJdbcOperations().query(sql, mapper);
    }

    ${modifier}<T> CloseableIterator<T> ${query.name}Iter(RowMapper<T> mapper) {
        String sql = checkAndGetSql("${query.name}", "", mapper);
        return jt.getIterableJdbcOperations().queryForIter(sql, mapper);
    }

[/#if]
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
    private String checkAndGetSql(String name, Object paramsBean) {
        if(null == paramsBean) throw new BeanQueriesException("Provided params object is null");
        String sql = queries.get(name);
        if(null == sql) throw new BeanQueriesException("No query found with name: [" + name + "], queries: [" + queries.keySet() + "]");
        return sql;
    }

[#list updates as query]
    // ${query.name} methods

[#if query.params?size > 0]
    ${modifier}interface ${query.name?cap_first}$Params {
[#list query.params as param]
        ${param.type} get${param.name?cap_first}();
[/#list]
    }

    ${modifier}int ${query.name}(${query.name?cap_first}$Params paramsBean) {
        String sql = checkAndGetSql("${query.name}", paramsBean);
        SqlParameterSource params = new BeanPropertySqlParameterSource(paramsBean);
        return jt.update(sql, params);
    }

[#else]

    ${modifier}int ${query.name}() {
        String sql = checkAndGetSql(jt, "${query.name}", "");
        return jt.getJdbcOperations().update(sql);
    }

[/#if]
[/#list]
}