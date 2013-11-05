[#ftl encoding="UTF-8"/]
[#if useIterableJdbcTemplate][#assign jtClass="IterableNamedParameterJdbcTemplate"][#else][#assign jtClass="NamedParameterJdbcTemplate"][/#if]
[#if useUnderscoredToCamel][#assign bpspsClass="UnderscoredBeanPropertySqlParameterSource"][#else][#assign bpspsClass="BeanPropertySqlParameterSource"][/#if]
package ${packageName};

[#if useIterableJdbcTemplate]
import com.alexkasko.springjdbc.iterable.IterableNamedParameterJdbcTemplate;
import com.alexkasko.springjdbc.iterable.CloseableIterator;
[#if useCloseableIterables]
import com.alexkasko.springjdbc.iterable.CloseableIterable;
[/#if]
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
[#if useBatchInserts]
import java.util.Iterator;
[/#if]
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
[#if useTemplateStringSubstitution]
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
[/#if]

[#if useUnderscoredToCamel]
import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;
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
    private static final String SUBSTITUTE_KEY_PATTERN_PREFIX = "\\$\\{";
    private static final String SUBSTITUTE_KEY_PATTERN_POSTFIX = "(?:\\(.*?\\))?\\}";
    private static final Pattern SUBSTITUTE_KEY_RESTRICTION_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final Pattern SUBSTITUTE_VALUE_PATTERN = Pattern.compile("${templateValueConstraintRegex}");
[/#if]

    private final Map<String, String> queries;
    private final ${jtClass} jt;
[#if useTemplateStringSubstitution]
    private final Map<String, Pattern> substituteMap = new ConcurrentHashMap<String, Pattern>();
[/#if]

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
[#if generateInterfacesForColumns && query.columns?size > 1]
    [#if useFluentSettersForColumns][#assign columnSetterReturnVal="Object"][#else][#assign columnSetterReturnVal="void"][/#if]

    /**
     * Interface for "${query.name}" result columns
     */
    ${modifier}interface ${query.name?cap_first}$Columns {
[#list query.columns as col]
        ${columnSetterReturnVal} set${col.name?cap_first}(${col.type} ${col.name});
[/#list]
    }
[/#if]
[#if query.params?size > 1]

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
        String sql = substitute(sqlTemplate, substitutions);
[/#if]
        SqlParameterSource params = new ${bpspsClass}(paramsBean);
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
        String sql = substitute(sqlTemplate, substitutions);
[/#if]
        SqlParameterSource params = new ${bpspsClass}(paramsBean);
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
    ${modifier}<T> CloseableIterator<T> ${query.name}Iterator(${query.name?cap_first}$Params paramsBean, RowMapper<T> mapper[#if query.template], Object... substitutions[/#if]) throws DataAccessException {
        String sql[#if query.template]Template[/#if] = checkAndGetSql("${query.name}", paramsBean, mapper);
[#if query.template]
        String sql = substitute(sqlTemplate, substitutions);
[/#if]
        SqlParameterSource params = new ${bpspsClass}(paramsBean);
        return jt.queryForIter(sql, params, mapper);
    }
[#if useCloseableIterables]

    /**
     * Returns closable iterable for "${query.name}" query
     *
     * @param paramsBean parameters object
     * @param mapper row mapper
     * @param <T> row mapper return type
     * @return closeable iterable for "${query.name}" query
     */
    ${modifier}<T> CloseableIterable<T> ${query.name}Iterable(final ${query.name?cap_first}$Params paramsBean, final RowMapper<T> mapper[#if query.template], final Object... substitutions[/#if]) {
        return new CloseableIterable<T>() {
            @Override
            protected CloseableIterator<T> closeableIterator() {
                return ${query.name}Iterator(paramsBean, mapper[#if query.template], substitutions[/#if]);
            }
        };
    }
[/#if]
[/#if]
[#elseif query.params?size == 1]
[#assign singlpar = query.params[0]]

    /**
     * Executes "${query.name}" query, maps results using provided mapper and returns them as list
     *
     * @param ${singlpar.name} query single parameter
     * @param mapper row mapper
     * @param <T> row mapper return type
     * @return list of mapped objects
     * @throws DataAccessException on query error
     */
    ${modifier}<T> List<T> ${query.name}(${singlpar.type} ${singlpar.name}, RowMapper<T> mapper[#if query.template], Object... substitutions[/#if]) throws DataAccessException {
        String sql[#if query.template]Template[/#if] = checkAndGetSql("${query.name}", ${singlpar.name}, mapper);
[#if query.template]
        String sql = substitute(sqlTemplate, substitutions);
[/#if]
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("${singlpar.name}", ${singlpar.name});
[#if useUnderscoredToCamel]
        params.put(camelToUnderscored("${singlpar.name}"), ${singlpar.name});
[/#if]
        return jt.query(sql, params, mapper);
    }

    /**
     * Executes "${query.name}" query that must return exactly one row
     * Maps this row using provided mapper and returns it
     *
     * @param ${singlpar.name} query single parameter
     * @param mapper row mapper
     * @param <T> row mapper return type
     * @return mapped object
     * @throws IncorrectResultSizeDataAccessException if not one row returned
     * @throws DataAccessException on query error
     */
    ${modifier}<T> T ${query.name}Single(${singlpar.type} ${singlpar.name}, RowMapper<T> mapper[#if query.template], Object... substitutions[/#if]) throws DataAccessException {
        String sql[#if query.template]Template[/#if] = checkAndGetSql("${query.name}", ${singlpar.name}, mapper);
[#if query.template]
        String sql = substitute(sqlTemplate, substitutions);
[/#if]
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("${singlpar.name}", ${singlpar.name});
[#if useUnderscoredToCamel]
        params.put(camelToUnderscored("${singlpar.name}"), ${singlpar.name});
[/#if]
        return jt.queryForObject(sql, params, mapper);
    }
[#if useIterableJdbcTemplate]

    /**
     * Executes "${query.name}" query, maps results using provided mapper and returns them as closeable iterator
     *
     * @param ${singlpar.name} query single parameter
     * @param mapper row mapper
     * @param <T> row mapper return type
     * @return iterator of mapped objects
     * @throws DataAccessException on query error
     */
    ${modifier}<T> CloseableIterator<T> ${query.name}Iterator(${singlpar.type} ${singlpar.name}, RowMapper<T> mapper[#if query.template], Object... substitutions[/#if]) throws DataAccessException {
        String sql[#if query.template]Template[/#if] = checkAndGetSql("${query.name}", ${singlpar.name}, mapper);
[#if query.template]
        String sql = substitute(sqlTemplate, substitutions);
[/#if]
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("${singlpar.name}", ${singlpar.name});
[#if useUnderscoredToCamel]
        params.put(camelToUnderscored("${singlpar.name}"), ${singlpar.name});
[/#if]
        return jt.queryForIter(sql, params, mapper);
    }
[#if useCloseableIterables]

    /**
     * Returns closable iterable for "${query.name}" query
     *
     * @param ${singlpar.name} query single parameter
     * @param mapper row mapper
     * @param <T> row mapper return type
     * @return closeable iterable for "${query.name}" query
     */
    ${modifier}<T> CloseableIterable<T> ${query.name}Iterable(final ${singlpar.type} ${singlpar.name}, final RowMapper<T> mapper[#if query.template], final Object... substitutions[/#if]) {
        return new CloseableIterable<T>() {
            @Override
            protected CloseableIterator<T> closeableIterator() {
                return ${query.name}Iterator(${singlpar.name}, mapper[#if query.template], substitutions[/#if]);
            }
        };
    }
[/#if]
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
        String sql = substitute(sqlTemplate, substitutions);
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
        String sql = substitute(sqlTemplate, substitutions);
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
    ${modifier}<T> CloseableIterator<T> ${query.name}Iterator(RowMapper<T> mapper[#if query.template], Object... substitutions[/#if]) throws DataAccessException {
        String sql[#if query.template]Template[/#if] = checkAndGetSql("${query.name}", "", mapper);
[#if query.template]
        String sql = substitute(sqlTemplate, substitutions);
[/#if]
        return jt.getIterableJdbcOperations().queryForIter(sql, mapper);
    }
[#if useCloseableIterables]

    /**
     * Returns closable iterable for "${query.name}" query
     *
     * @param mapper row mapper
     * @param <T> row mapper return type
     * @return closeable iterable for "${query.name}" query
     */
    ${modifier}<T> CloseableIterable<T> ${query.name}Iterable(final RowMapper<T> mapper[#if query.template], final Object... substitutions[/#if]) {
        return new CloseableIterable<T>() {
            @Override
            protected CloseableIterator<T> closeableIterator() {
                return ${query.name}Iterator(mapper[#if query.template], substitutions[/#if]);
            }
        };
    }
[/#if]
[/#if]
[/#if]
[/#list]

    // update methods
[#list updates as query]

    // ${query.name} methods
[#if query.params?size > 1]

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
        String sql = substitute(sqlTemplate, substitutions);
[/#if]
        SqlParameterSource params = new ${bpspsClass}(paramsBean);
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
        String sql = substitute(sqlTemplate, substitutions);
[/#if]
        SqlParameterSource params = new ${bpspsClass}(paramsBean);
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
        String sql = substitute(sqlTemplate, substitutions);
[/#if]
        return batchUpdate(sql, paramsIter, batchSize);
    }
[/#if]
[#elseif query.params?size == 1]
[#assign singlpar = query.params[0]]

    /**
     * Executes "${query.name}" query
     *
     * @param ${singlpar.name} query single parameter
     * @return count of updated rows
     * @throws DataAccessException on query error
     */
    ${modifier}int ${query.name}(${singlpar.type} ${singlpar.name}[#if query.template], Object... substitutions[/#if]) throws DataAccessException {
        String sql[#if query.template]Template[/#if] = checkAndGetSql("${query.name}", ${singlpar.name});
[#if query.template]
        String sql = substitute(sqlTemplate, substitutions);
[/#if]
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("${singlpar.name}", ${singlpar.name});
[#if useUnderscoredToCamel]
        params.put(camelToUnderscored("${singlpar.name}"), ${singlpar.name});
[/#if]
        return jt.update(sql, params);
    }
[#if useCheckSingleRowUpdates]

    /**
     * Executes "${query.name}" query and checks that exactly one row was updated
     *
     * @param ${singlpar.name} query single parameter
     * @throws IncorrectResultSizeDataAccessException if not one row was updated
     * @throws DataAccessException on query error
     */
    ${modifier}void ${query.name}Single(${singlpar.type} ${singlpar.name}[#if query.template], Object... substitutions[/#if]) throws DataAccessException {
        String sql[#if query.template]Template[/#if] = checkAndGetSql("${query.name}", ${singlpar.name});
[#if query.template]
        String sql = substitute(sqlTemplate, substitutions);
[/#if]
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("${singlpar.name}", ${singlpar.name});
[#if useUnderscoredToCamel]
        params.put(camelToUnderscored("${singlpar.name}"), ${singlpar.name});
[/#if]
        int updatedRowsCount = jt.update(sql, params);
        checkSingleRowUpdated(updatedRowsCount);
    }
[/#if]
[#if useBatchInserts]

    /**
     * Interface for "${query.name}" query parameters
     */
    ${modifier}interface ${query.name?cap_first}$Params {
[#list query.params as param]
        ${param.type} get${param.name?cap_first}();
[/#list]
    }

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
        String sql = substitute(sqlTemplate, substitutions);
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
        String sql = substitute(sqlTemplate, substitutions);
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
        String sql = substitute(sqlTemplate, substitutions);
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
        ${bpspsClass}[] params = new ${bpspsClass}[batchSize];
        int updated = 0;
        int index = 0;
        // main cycle
        while(paramsIter.hasNext()) {
            params[index] = new ${bpspsClass}(paramsIter.next());
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
            ${bpspsClass}[] partParArray = new ${bpspsClass}[index];
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
     * Substitute placeholders in sql query template
     *
     * @param template sql query template
     * @param substitutions substitutions array {@code [key1, val2, key2, val2,...]}
     * @return sql query string
     */
    private String substitute(String template, Object[] substitutions) {
        if(null == substitutions) throw new QueryException("Provided substitutions array is null");
        if(0 == substitutions.length) throw new QueryException("Provided substitutions array is empty");
        if(0 != substitutions.length % 2) throw new QueryException("Placeholders vararg array must have even numbers of elements " +
                "(they will be represented as [key1, val2, key2, val2,...]), but length was: [" + substitutions.length + "]");
        String sql = template;
        for (int i = 0; i < substitutions.length; i += 2) {
            Object key = substitutions[i];
            if(null == key) throw new QueryException("Provided substitutions vararg array element (key) " +
                    "is null at position: [" + i + "]");
            String keystr = key.toString();
            if(!SUBSTITUTE_KEY_RESTRICTION_PATTERN.matcher(keystr).matches()) throw new QueryException(
                    "Provided substitutions vararg array element (key): [" + keystr + "] does not match " +
                    "restriction regex: [" + SUBSTITUTE_KEY_RESTRICTION_PATTERN + "] at position: [" + i + "]");
            Object val = substitutions[i+1];
            if(null == val) throw new QueryException("Provided substitutions vararg array element (value) " +
                    "is null at position: [" + i + "]");
            String valstr = val.toString();
            if(!SUBSTITUTE_VALUE_PATTERN.matcher(valstr).matches()) throw new QueryException("Provided substitutions vararg array element " +
                    "(value) does not match SQL injection prevention regex: [" + SUBSTITUTE_VALUE_PATTERN + "] at position: [" + i + "]");
            Pattern existed = substituteMap.get(keystr);
            final Pattern regex;
            if(null == existed) {
                regex = Pattern.compile(SUBSTITUTE_KEY_PATTERN_PREFIX + keystr + SUBSTITUTE_KEY_PATTERN_POSTFIX, Pattern.DOTALL);
                substituteMap.put(keystr, regex);
            } else regex = existed;
            Matcher ma = regex.matcher(sql);
            String replaced = ma.replaceAll(valstr);
            if(sql.equals(replaced)) throw new QueryException("Provided substitutions key: [" + key + "] not found in template: [" + sql + "]");
            sql = replaced;
        }
        return sql;
    }
[/#if]
[#if useUnderscoredToCamel]

    private static String underscoredToCamel(String underscored) {
        if(null == underscored || 0 == underscored.length() || !underscored.contains("_")) return underscored;
        StringBuilder sb = new StringBuilder();
        boolean usFound = false;
        for(int i = 0; i< underscored.length(); i++) {
            char ch = underscored.charAt(i);
            if('_' == ch) {
                if(usFound) { // double underscore
                    sb.append('_');
                } else {
                    usFound = true;
                }
            } else if (usFound) {
                sb.append(toUpperCase(ch));
                usFound = false;
            } else {
                sb.append(ch);
            }
        }
        if(usFound) sb.append("_");
        return sb.toString();
    }

    private static String camelToUnderscored(String camel) {
        if(null == camel || camel.length() < 2) return camel;
        boolean hasUpper = false;
        for (int i = 1; i < camel.length(); i++) {
            char ch = camel.charAt(i);
            if(ch == Character.toUpperCase(ch)) {
                hasUpper = true;
                break;
            }
        }
        if(!hasUpper) return camel;
        StringBuilder sb = new StringBuilder();
        sb.append(camel.charAt(0));
        for (int i = 1; i < camel.length(); i++) {
            char ch = camel.charAt(i);
            char lower = toLowerCase(ch);
            if(ch == toUpperCase(ch) && ch != lower) {
                sb.append("_");
                sb.append(lower);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * {@code BeanPropertySqlParameterSource} extension that maps camelCase properties to under_scored parameters
     */
    private static class UnderscoredBeanPropertySqlParameterSource extends BeanPropertySqlParameterSource {

        /**
         * Create a new BeanPropertySqlParameterSource for the given bean.
         *
         * @param object the bean instance to wrap
         */
        private UnderscoredBeanPropertySqlParameterSource(Object object) {
            super(object);
        }

        /**
         * Determine whether there is a value for the specified named parameter.
         * @param paramName the name of the parameter
         * @return whether there is a value defined
         */
        @Override
        public boolean hasValue(String paramName) {
            return super.hasValue(underscoredToCamel(paramName));
        }

        /**
       	 * Return the parameter value for the requested named parameter.
       	 * @param paramName the name of the parameter
       	 * @return the value of the specified parameter
       	 * @throws IllegalArgumentException if there is no value for the requested parameter
       	 */
        @Override
        public Object getValue(String paramName) throws IllegalArgumentException {
            return super.getValue(underscoredToCamel(paramName));
        }


        /**
       	 * Provide access to the property names of the wrapped bean.
       	 * Uses support provided in the {@link org.springframework.beans.PropertyAccessor} interface.
       	 * @return an array containing all the known property names
       	 */
        @Override
        public String[] getReadablePropertyNames() {
            String[] camel = super.getReadablePropertyNames();
            String[] underscored = new String[camel.length];
            for (int i = 0; i < camel.length; i++) {
                underscored[i] = camelToUnderscored(camel[i]);
            }
            return underscored;
        }

        /**
       	 * Derives a default SQL type from the corresponding property type.
       	 * @see org.springframework.jdbc.core.StatementCreatorUtils#javaTypeToSqlParameterType
       	 */
        @Override
        public int getSqlType(String paramName) {
            return super.getSqlType(underscoredToCamel(paramName));
        }

        /**
       	 * Register a SQL type for the given parameter.
       	 * @param paramName the name of the parameter
       	 * @param sqlType the SQL type of the parameter
       	 */
        @Override
        public void registerSqlType(String paramName, int sqlType) {
            super.registerSqlType(underscoredToCamel(paramName), sqlType);
        }

        /**
       	 * Register a SQL type for the given parameter.
       	 * @param paramName the name of the parameter
       	 * @param typeName the type name of the parameter
       	 */
        @Override
        public void registerTypeName(String paramName, String typeName) {
            super.registerTypeName(underscoredToCamel(paramName), typeName);
        }

        /**
       	 * Return the type name for the given parameter, if registered.
       	 * @param paramName the name of the parameter
       	 * @return the type name of the parameter,
       	 * or <code>null</code> if not registered
       	 */
        @Override
        public String getTypeName(String paramName) {
            return super.getTypeName(underscoredToCamel(paramName));
        }
    }
[/#if]

    /**
     * Exception, that will be thrown out on error
     */
    ${modifier}static class QueryException extends DataAccessException {
        private static final long serialVersionUID = 4365332327519682601L;

        ${modifier}QueryException(String msg) {
            super(msg);
        }
    }
}