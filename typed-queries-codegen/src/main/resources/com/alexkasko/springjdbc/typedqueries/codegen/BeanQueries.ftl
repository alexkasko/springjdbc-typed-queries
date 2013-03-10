[#ftl encoding="UTF-8"/]
[#if useIterableJdbcTemplate][#assign jtClass="IterableNamedParameterJdbcTemplate"][#else][#assign jtClass="NamedParameterJdbcTemplate"][/#if]
package ${packageName};

[#if useIterableJdbcTemplate]
import com.alexkasko.springjdbc.iterable.IterableNamedParameterJdbcTemplate;
import com.alexkasko.springjdbc.iterable.CloseableIterator;
[/#if]
[#if useTemplateStringSubstitution]
import org.apache.commons.lang.text.StrSubstitutor;
[/#if]
import org.springframework.dao.DataAccessException;
[#if useCheckSingleRowUpdates]
import org.springframework.dao.EmptyResultDataAccessException;
[/#if]
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
[#if !useIterableJdbcTemplate]
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
[/#if]

import java.util.Date;
import java.util.ArrayList;
import java.util.Collection;
[#if useTemplateStringSubstitution]
import java.util.HashMap;
[/#if]
[#if useBatchInserts]
import java.util.Iterator;
[/#if]
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
[#if useTemplateStringSubstitution]
import java.util.regex.Pattern;
[/#if]

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * Wrappers for SQL queries using typed parameters.
 * Generated from "${sourceSqlFileName}".
 * Generated on "${currentDate}".
 * Do not change this file, change source SQL file instead.
 * To regenerate this file run {@code mvn generate-sources}.
 */
${modifier}class ${className} {
    private static final Set<String> GENERATED_QUERIES_NAMES;
[#if useTemplateStringSubstitution]
    private static final Pattern SUBSTITUTE_VALUE_PATTERN = Pattern.compile("${templateValueConstraintRegex}");
[/#if]

    private final Map<String, String> queries;
    private final ${jtClass} jt;

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
            if(!unique) throw new QueryException("Duplicate query name: [" + name + "] in list: [" + genNamesList + "]");
        }
        // set names that will be used in constructor check
        GENERATED_QUERIES_NAMES = unmodifiableSet(getNamesSet);
    }

    /**
     * Constructor
     *
     * @param queries 'name'->'sql' map
     * @param jt jdbc template
     * @throws DataAccessException if provided queries names are not consistent with generated ones
     */
    ${modifier}${className}(Map<String, String> queries, ${jtClass} jt) throws DataAccessException {
        if(null == queries) throw new QueryException("Provided queries map is null");
        if(null == jt) throw new QueryException("Provided JdbcTemplate is null");
        if(queries.size() != GENERATED_QUERIES_NAMES.size()) throw new QueryException(
                "Provided queries: [" + queries.keySet() + "] are non consistent with queries " +
                " known on generation time: [" + GENERATED_QUERIES_NAMES + "]: size differs");
        for(String name : queries.keySet()) if(!GENERATED_QUERIES_NAMES.contains(name)) throw new QueryException(
                "Provided queries: [" + queries.keySet() + "] are non consistent with queries " +
                " known on generation time: [" + GENERATED_QUERIES_NAMES + "]: unknown name: [" + name + "]");
        this.queries = unmodifiableMap(queries);
        this.jt = jt;
    }

    /**
     * Jdbc template accessor
     *
     * @return jdbc template
     */
    ${modifier}${jtClass} jt() {
        return this.jt;
    }

    /**
     * Query text accessor
     *
     * @param name query name
     * @return query text
     * @throws DataAccessException on unknown query name
     */
    ${modifier}String queryText(String name) throws DataAccessException {
        String sql = queries.get(name);
        if(null == sql) throw new QueryException("No query found with name: [" + name + "], queries: [" + queries.keySet() + "]");
        return sql;
    }

    // select methods
[#list selects as query]

    // ${query.name} methods
[#if query.params?size > 0]

    /**
     * Interface for "${query.name}" query parameters
     */
    ${modifier}interface ${query.name?cap_first}$Params {
[#list query.params as param]
        ${param.type} get${param.name?cap_first}();
[/#list]
    }

    /**
     * Executes "${query.name}" query, maps results using provided mapper and returns them as list
     *
     * @param paramsBean parameters object
     * @param mapper row mapper
     * @param <T> row mapper return type
     * @return list of mapped objects
     * @throws DataAccessException on query error
     */
    ${modifier}<T> List<T> ${query.name}(${query.name?cap_first}$Params paramsBean, RowMapper<T> mapper[#if query.template], Object... substitutions[/#if]) throws DataAccessException {
        String sql[#if query.template]Template[/#if] = checkAndGetSql("${query.name}", paramsBean, mapper);
[#if query.template]
        Map<String, String> substitutionMap = convertSubstitutionsToMap(substitutions);
        String sql = StrSubstitutor.replace(sqlTemplate, substitutionMap);
[/#if]
        SqlParameterSource params = new BeanPropertySqlParameterSource(paramsBean);
        return jt.query(sql, params, mapper);
    }

    /**
     * Executes "${query.name}" query that must return exactly one row
     * Maps this row using provided mapper and returns it
     * 
     * @param paramsBean parameters object
     * @param mapper row mapper
     * @param <T> row mapper return type
     * @return mapped object
     * @throws IncorrectResultSizeDataAccessException if not one row returned
     * @throws DataAccessException on query error
     */
    ${modifier}<T> T ${query.name}Single(${query.name?cap_first}$Params paramsBean, RowMapper<T> mapper[#if query.template], Object... substitutions[/#if]) throws DataAccessException {
        String sql[#if query.template]Template[/#if] = checkAndGetSql("${query.name}", paramsBean, mapper);
[#if query.template]
        Map<String, String> substitutionMap = convertSubstitutionsToMap(substitutions);
        String sql = StrSubstitutor.replace(sqlTemplate, substitutionMap);
[/#if]
        SqlParameterSource params = new BeanPropertySqlParameterSource(paramsBean);
        return jt.queryForObject(sql, params, mapper);
    }
[#if useIterableJdbcTemplate]

    /**
     * Executes "${query.name}" query, maps results using provided mapper and returns them as closeable iterator
     *
     * @param paramsBean parameters object
     * @param mapper row mapper
     * @param <T> row mapper return type
     * @return iterator of mapped objects
     * @throws DataAccessException on query error
     */
    ${modifier}<T> CloseableIterator<T> ${query.name}Iter(${query.name?cap_first}$Params paramsBean, RowMapper<T> mapper[#if query.template], Object... substitutions[/#if]) throws DataAccessException {
        String sql[#if query.template]Template[/#if] = checkAndGetSql("${query.name}", paramsBean, mapper);
[#if query.template]
        Map<String, String> substitutionMap = convertSubstitutionsToMap(substitutions);
        String sql = StrSubstitutor.replace(sqlTemplate, substitutionMap);
[/#if]
        SqlParameterSource params = new BeanPropertySqlParameterSource(paramsBean);
        return jt.queryForIter(sql, params, mapper);
    }
[/#if]
[#else]

    /**
     * Executes "${query.name}" query, maps results using provided mapper and returns them as list
     *
     * @param mapper row mapper
     * @param <T> row mapper return type
     * @return list of mapped objects
     * @throws DataAccessException on query error
     */
    ${modifier}<T> List<T> ${query.name}(RowMapper<T> mapper[#if query.template], Object... substitutions[/#if]) throws DataAccessException {
        String sql[#if query.template]Template[/#if] = checkAndGetSql("${query.name}", "", mapper);
[#if query.template]
        Map<String, String> substitutionMap = convertSubstitutionsToMap(substitutions);
        String sql = StrSubstitutor.replace(sqlTemplate, substitutionMap);
[/#if]
        return jt.getJdbcOperations().query(sql, mapper);
    }

    /**
     * Executes "${query.name}" query that must return exactly one row
     * Maps this row using provided mapper and returns it
     *
     * @param mapper row mapper
     * @param <T> row mapper return type
     * @return mapped object
     * @throws IncorrectResultSizeDataAccessException if not one row returned
     * @throws DataAccessException on query error
     */
    ${modifier}<T> T ${query.name}Single(RowMapper<T> mapper[#if query.template], Object... substitutions[/#if]) throws DataAccessException {
        String sql[#if query.template]Template[/#if] = checkAndGetSql("${query.name}", "", mapper);
[#if query.template]
        Map<String, String> substitutionMap = convertSubstitutionsToMap(substitutions);
        String sql = StrSubstitutor.replace(sqlTemplate, substitutionMap);
[/#if]
        return jt.getJdbcOperations().queryForObject(sql, mapper);
    }
[#if useIterableJdbcTemplate]

    /**
     * Executes "${query.name}" query, maps results using provided mapper and returns them as closeable iterator
     *
     * @param mapper row mapper
     * @param <T> row mapper return type
     * @return iterator of mapped objects
     * @throws DataAccessException on query error
     */
    ${modifier}<T> CloseableIterator<T> ${query.name}Iter(RowMapper<T> mapper[#if query.template], Object... substitutions[/#if]) throws DataAccessException {
        String sql[#if query.template]Template[/#if] = checkAndGetSql("${query.name}", "", mapper);
[#if query.template]
        Map<String, String> substitutionMap = convertSubstitutionsToMap(substitutions);
        String sql = StrSubstitutor.replace(sqlTemplate, substitutionMap);
[/#if]
        return jt.getIterableJdbcOperations().queryForIter(sql, mapper);
    }
[/#if]
[/#if]
[/#list]

    // update methods
[#list updates as query]

    // ${query.name} methods
[#if query.params?size > 0]

    /**
     * Interface for "${query.name}" query parameters
     */
    ${modifier}interface ${query.name?cap_first}$Params {
[#list query.params as param]
        ${param.type} get${param.name?cap_first}();
[/#list]
    }

    /**
     * Executes "${query.name}" query
     *
     * @param paramsBean parameters object
     * @return count of updated rows
     * @throws DataAccessException on query error
     */
    ${modifier}int ${query.name}(${query.name?cap_first}$Params paramsBean[#if query.template], Object... substitutions[/#if]) throws DataAccessException {
        String sql[#if query.template]Template[/#if] = checkAndGetSql("${query.name}", paramsBean);
[#if query.template]
        Map<String, String> substitutionMap = convertSubstitutionsToMap(substitutions);
        String sql = StrSubstitutor.replace(sqlTemplate, substitutionMap);
[/#if]
        SqlParameterSource params = new BeanPropertySqlParameterSource(paramsBean);
        return jt.update(sql, params);
    }
[#if useCheckSingleRowUpdates]

    /**
     * Executes "${query.name}" query and checks that exactly one row was updated
     *
     * @param paramsBean parameters object
     * @throws IncorrectResultSizeDataAccessException if not one row was updated
     * @throws DataAccessException on query error
     */
    ${modifier}void ${query.name}Single(${query.name?cap_first}$Params paramsBean[#if query.template], Object... substitutions[/#if]) throws DataAccessException {
        String sql[#if query.template]Template[/#if] = checkAndGetSql("${query.name}", paramsBean);
[#if query.template]
        Map<String, String> substitutionMap = convertSubstitutionsToMap(substitutions);
        String sql = StrSubstitutor.replace(sqlTemplate, substitutionMap);
[/#if]
        SqlParameterSource params = new BeanPropertySqlParameterSource(paramsBean);
        int updatedRowsCount = jt.update(sql, params);
        checkSingleRowUpdated(updatedRowsCount);
    }
[/#if]
[#if useBatchInserts]

    /**
     * Executes "${query.name}" query in batch mode
     *
     * @param paramsIter parameters iterator
     * @param batchSize single batch size
     * @return count of updated rows
     * @throws DataAccessException on query error
     */
    ${modifier}int ${query.name}Batch(Iterator<? extends ${query.name?cap_first}$Params> paramsIter, int batchSize[#if query.template], Object... substitutions[/#if]) throws DataAccessException {
        if(batchSize <= 0) throw new QueryException("Provided batchSize must be positive: [" + batchSize + "]");
        String sql[#if query.template]Template[/#if] = checkAndGetSql("${query.name}", paramsIter);
[#if query.template]
        Map<String, String> substitutionMap = convertSubstitutionsToMap(substitutions);
        String sql = StrSubstitutor.replace(sqlTemplate, substitutionMap);
[/#if]
        return batchUpdate(sql, paramsIter, batchSize);
    }
[/#if]
[#else]

    /**
     * Executes "${query.name}" query
     *
     * @return count of updated rows
     * @throws DataAccessException on query error
     */
    ${modifier}int ${query.name}([#if query.template]Object... substitutions[/#if]) {
        String sql[#if query.template]Template[/#if] = checkAndGetSql("${query.name}", "");
[#if query.template]
        Map<String, String> substitutionMap = convertSubstitutionsToMap(substitutions);
        String sql = StrSubstitutor.replace(sqlTemplate, substitutionMap);
[/#if]
        return jt.getJdbcOperations().update(sql);
    }
[#if useCheckSingleRowUpdates]

    /**
     * Executes "${query.name}" query and checks that exactly one row was updated
     *
     * @throws IncorrectResultSizeDataAccessException if not one row was updated
     * @throws DataAccessException on query error
     */
    ${modifier}void ${query.name}Single([#if query.template]Object... substitutions[/#if]) {
        String sql[#if query.template]Template[/#if] = checkAndGetSql("${query.name}", "");
[#if query.template]
        Map<String, String> substitutionMap = convertSubstitutionsToMap(substitutions);
        String sql = StrSubstitutor.replace(sqlTemplate, substitutionMap);
[/#if]
        int updatedRowsCount =  jt.getJdbcOperations().update(sql);
        checkSingleRowUpdated(updatedRowsCount);
    }
[/#if]
[/#if]
[/#list]

    // private helper methods

    /**
     * Checks client-provided arguments and returns query sql text
     *
     * @param name query name
     * @param paramsBean query parameters
     * @param mapper row mapper
     * @return query sql text
     * @throws DataAccessException if provided arguments are null or queries are inconsistent
     */
    private String checkAndGetSql(String name, Object paramsBean, RowMapper<?> mapper) throws DataAccessException {
        if(null == paramsBean) throw new QueryException("Provided params object is null");
        if(null == mapper) throw new QueryException("Provided mapper object is null");
        String sql = queries.get(name);
        if(null == sql) throw new QueryException("No query found with name: [" + name + "], queries: [" + queries.keySet() + "]");
        return sql;
    }

    /**
     * Checks client-provided arguments and returns query sql text
     *
     * @param name query name
     * @param paramsBean query parameters
     * @return query sql text
     * @throws DataAccessException if provided arguments are null or queries are inconsistent
     */
    private String checkAndGetSql(String name, Object paramsBean) throws DataAccessException {
        if(null == paramsBean) throw new QueryException("Provided params object is null");
        String sql = queries.get(name);
        if(null == sql) throw new QueryException("No query found with name: [" + name + "], queries: [" + queries.keySet() + "]");
        return sql;
    }
[#if useCheckSingleRowUpdates]

    /**
     * Checks whether provided results size equals '1'
     *
     * @param updatedRowsCount results size
     * @throws IncorrectResultSizeDataAccessException
     */
    private void checkSingleRowUpdated(int updatedRowsCount) throws IncorrectResultSizeDataAccessException {
        if(0 == updatedRowsCount) throw new EmptyResultDataAccessException(1);
        if(updatedRowsCount > 1) throw new IncorrectResultSizeDataAccessException(1, updatedRowsCount);
    }
[/#if]
[#if useBatchInserts]

    /**
     * Methods for performing batch inserts using provided iterators as parameters
     *
     * @param sql sql query text
     * @param paramsIter query parameters iterator
     * @param batchSize size of single batch
     * @return number of updated rows reported by JDBC driver,
     * {@code -1} if such information is not available
     */
    private int batchUpdate(String sql, Iterator<? extends Object> paramsIter, int batchSize) {
        boolean hasInfoFromDb = true;
        // mutable for lower overhead
        BeanPropertySqlParameterSource[] params = new BeanPropertySqlParameterSource[batchSize];
        int updated = 0;
        int index = 0;
        // main cycle
        while(paramsIter.hasNext()) {
            params[index] = new BeanPropertySqlParameterSource(paramsIter.next());
            index += 1;
            if(0 == index % batchSize) {
                int[] upArr = jt.batchUpdate(sql, params);
                if(hasInfoFromDb) {
                    int up = countUpdatedRows(upArr);
                    if(-1 == up) hasInfoFromDb = false;
                    updated += up;
                }
                index = 0;
            }
        }
        // tail
        if(index > 0) {
            BeanPropertySqlParameterSource[] partParArray = new BeanPropertySqlParameterSource[index];
            System.arraycopy(params, 0, partParArray, 0, index);
            int[] upArr = jt.batchUpdate(sql, partParArray);
            if(hasInfoFromDb) {
                int up = countUpdatedRows(upArr);
                if(-1 == up) hasInfoFromDb = false;
                updated += up;
            }
        }
        return hasInfoFromDb ? updated : -1;
    }

    // returns -1 on no info from db
    private static int countUpdatedRows(int[] dbReturned) {
        int res = 0;
        for(int updated : dbReturned) {
            if(updated < 0) return -1;
            res += updated;
        }
        return res;
    }
[/#if]
[#if useTemplateStringSubstitution]

    /**
     * Coverts substitutions vararg array into {@link HashMap} checking null elements and duplicate keys
     *
     * @param input substitutions vararg array
     * @return substitutions hashmap
     */
    private Map<String, String> convertSubstitutionsToMap(Object[] input) {
        if(null == input) throw new QueryException("Provided substitutions array is null");
        if(0 == input.length) throw new QueryException("Provided substitutions array is empty");
        if(0 != input.length % 2) throw new QueryException("Placeholders vararg array must have even numbers of elements " +
                "(they will be represented as [key1, val2, key2, val2,...]), but length was: [" + input.length + "]");
        Map<String, String> res = new HashMap<String, String>(input.length/2);
        for (int i = 0; i < input.length; i += 2) {
            Object key = input[i];
            if(null == key) throw new QueryException("Provided substitutions vararg array element (key) " +
                    "is null at position: [" + i + "]");
            Object val = input[i+1];
            if(null == val) throw new QueryException("Provided substitutions vararg array element (value) " +
                    "is null at position: [" + i + "]");
            String valstr = val.toString();
            if(!SUBSTITUTE_VALUE_PATTERN.matcher(valstr).matches()) throw new QueryException("Provided substitutions vararg array element " +
                    "(value) does not match SQL injection prevention regex: [" + SUBSTITUTE_VALUE_PATTERN + "] at position: [" + i + "]");
            String existed = res.put(key.toString(), valstr);
            if(null != existed) throw new QueryException("Provided substitutions vararg array contains duplicate key " +
                    "on position: [" + i + "]");
        }
        return res;
    }
[/#if]

    /**
     * Exception, that will be thrown out on error
     */
    static class QueryException extends DataAccessException {
        public QueryException(String msg) {
            super(msg);
        }
    }
}