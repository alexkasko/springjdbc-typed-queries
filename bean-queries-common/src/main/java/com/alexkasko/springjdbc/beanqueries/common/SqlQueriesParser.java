package com.alexkasko.springjdbc.beanqueries.common;

import org.apache.commons.io.LineIterator;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses SQL queries file (as {@link Reader} or {@link InputStream}) to 'name'->'sql' map.
 * Stream is parsed line by line using regexes (may be configured through class constructor).
 * By default supports one-liner sql comments: {@code some text or nothing -- comment text},
 * query name syntax: {@code /** myTestSelect *REMOVEME/ }
 * Won't close provided stream/reader.
 *
 * @author alexkasko
 * Date: 12/22/12
 */
public class SqlQueriesParser {
    private enum State {STARTED, COLLECTING}
    public static final String DEFAULT_QUERY_NAME_REGEX = "^\\s*/\\*{2}\\s*(.*?)\\s*\\*/\\s*$";
    public static final String DEFAULT_COMMENT_REGEX = "^\\s*(?:--.*)?$";
    public static final String DEFAULT_BODY_LINE_REGEX = "^\\s*(.*?)(?:\\s*--.*)?\\s*$";

    private final Pattern nameRegex;
    private final Pattern commentRegex;
    private final Pattern bodyLineRegex;

    /**
     * Constructor, uses default regexes
     */
    public SqlQueriesParser() {
        this(DEFAULT_QUERY_NAME_REGEX, DEFAULT_COMMENT_REGEX, DEFAULT_BODY_LINE_REGEX);
    }

    /**
     * Configurable constructor
     *
     * @param nameRegex regex for query names
     * @param commentRegex regex for comments and empty strings
     * @param bodyLineRegex regex for query body line
     */
    public SqlQueriesParser(String nameRegex, String commentRegex, String bodyLineRegex) {
        this.nameRegex = Pattern.compile(nameRegex);
        this.commentRegex = Pattern.compile(commentRegex);
        this.bodyLineRegex = Pattern.compile(bodyLineRegex);
    }

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
    public Map<String, String> parse(Reader reader) {
        Map<String, String> res = new LinkedHashMap<String, String>();
        LineIterator li = null;
        try {
            li = new LineIterator(new NoCloseReader(reader));
            State state = State.STARTED;
            String name = null;
            StringBuilder sql = new StringBuilder();
            while(li.hasNext()) {
                String line = li.next();
                if(commentRegex.matcher(line).matches()) continue;
                switch(state) {
                    case STARTED: // search for first query name
                        Matcher startedMatcher = nameRegex.matcher(line);
                        if(!startedMatcher.matches())
                            throw new BeanQueriesException("Query name not found on start, regex: [" + nameRegex + "]");
                        name = startedMatcher.group(1);
                        state = State.COLLECTING;
                        break;
                    case COLLECTING:
                        Matcher nameMatcher = nameRegex.matcher(line);
                        if(nameMatcher.matches()) { // next query name found
                            if(0 == sql.length())
                                throw new BeanQueriesException("No SQL found for request name: [" + name + "]");
                            // save collected sql string
                            String existed = res.put(name, sql.toString());
                            if(null != existed)
                                throw new BeanQueriesException("Duplicate SQL query name: [" + name + "]");
                            // clean collected sql string
                            sql = new StringBuilder();
                            name = nameMatcher.group(1);
                        } else {
                            Matcher matcher = bodyLineRegex.matcher(line);
                            matcher.matches(); // always matches
                            String sqlLine = matcher.group(1);
                            if(sql.length() > 0) sql.append(" ");
                            sql.append(sqlLine);
                        }
                        break;
                    default:
                        throw new BeanQueriesException(state.name());
                }
            }
            // tail
            if(null == name) throw new BeanQueriesException("No queries found");
            String existed = res.put(name, sql.toString());
            if(null != existed) throw new BeanQueriesException("Duplicate SQL query name: [" + name + "]");
        } finally {
            LineIterator.closeQuietly(li);
        }
        return res;
    }
}
