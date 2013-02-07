package com.alexkasko.springjdbc.typedqueries.common;

import com.alexkasko.springjdbc.typedqueries.common.SqlFileParseException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses SQL queries file (as {@link Reader} or {@link InputStream}) to 'name'->'sql' map.
 * Query name syntax: {@code /** selectSomeData STAR_SYMBOL/ }
 * Won't close provided stream/reader.
 *
 * @author alexkasko
 * Date: 12/22/12
 */
public class PlainSqlQueriesParser {
    private enum State {STARTED, COLLECTING}
    public static final Pattern DEFAULT_QUERY_NAME_REGEX = Pattern.compile("^\\s*/\\*{2}\\s*(.*?)\\s*\\*/\\s*$");

    /**
     * Parses provided stream to 'name'->'sql' map using 'UTF-8' encoding
     *
     * @param is sql queries file
     * @return 'name'->'sql' map.
     */
    public Map<String, String> parse(InputStream is) {
        return parse(is, "UTF-8");
    }

    /**
     * Parses provided stream to 'name'->'sql' map.
     *
     * @param is sql queries file
     * @param encoding sql queries file encoding
     * @return 'name'->'sql' map.
     */
    public Map<String, String> parse(InputStream is, String encoding) {
        return parse(new InputStreamReader(is, Charset.forName(encoding)));
    }

    /**
     * Parses provided reader to 'name'->'sql' map.
     *
     * @param reader sql queries file
     * @return 'name'->'sql' map.
     */
    public Map<String, String> parse(Reader reader) throws SqlFileParseException {
        Map<String, String> res = new LinkedHashMap<String, String>();
        BufferedReader re = null;
        try {
            re = new BufferedReader(reader);
            State state = State.STARTED;
            String name = null;
            StringBuilder sql = new StringBuilder();
            String line;
            while(null != (line = re.readLine())) {
                String trimmed = line.trim();
                if(0 == trimmed.length()) continue;
                switch(state) {
                    case STARTED: // search for first query name
                        if(trimmed.startsWith("--")) continue;
                        Matcher startedMatcher = DEFAULT_QUERY_NAME_REGEX.matcher(line);
                        if(!startedMatcher.matches()) throw new SqlFileParseException(
                                "Query name not found on start, regex: [" + DEFAULT_QUERY_NAME_REGEX + "]");
                        name = startedMatcher.group(1);
                        state = State.COLLECTING;
                        break;
                    case COLLECTING:
                        Matcher nameMatcher = DEFAULT_QUERY_NAME_REGEX.matcher(line);
                        if(nameMatcher.matches()) { // next query name found
                            if(0 == sql.length()) throw new SqlFileParseException(
                                    "No SQL found for request name: [" + name + "]");
                            // save collected sql string
                            String existed = res.put(name, sql.toString());
                            if(null != existed) throw new SqlFileParseException(
                                    "Duplicate SQL query name: [" + name + "]");
                            // clean collected sql string
                            sql = new StringBuilder();
                            name = nameMatcher.group(1);
                        } else {
                            sql.append(line);
                            sql.append("\n");
                        }
                        break;
                    default: throw new SqlFileParseException(state.name());
                }
            }
            // tail
            if(null == name) throw new SqlFileParseException("No queries found");
            String existed = res.put(name, sql.toString());
            if(null != existed) throw new SqlFileParseException("Duplicate SQL query name: [" + name + "]");
        } catch (IOException e) {
            throw new SqlFileParseException(e);
        } catch (Exception e) {
            if(e instanceof SqlFileParseException) throw (SqlFileParseException) e;
            throw new SqlFileParseException(e);
        } finally {
            closeQuietly(re);
        }
        return res;
    }

    private static void closeQuietly(Closeable is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }
}
