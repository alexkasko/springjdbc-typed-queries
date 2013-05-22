SpringJDBC SQL queries with typed parameters
============================================

Maven-plugin, takes file with SQL queries and generates java methods for executing each query.
For queries, that take input parameters, also generates an interface containing typed parameters getters.

Generated class depends on [spring-jdbc](http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/jdbc.html).

Plugin is available in [Maven central](http://repo1.maven.org/maven2/com/alexkasko/springjdbc/).

Generated docs:

 * [maven-generated site for the plugin](http://alexkasko.github.com/springjdbc-typed-queries/plugin).
 * [javadocs for the optional typed-queries-common library](http://alexkasko.github.com/springjdbc-typed-queries/javadocs/common).
 * [javadocs for the typed-queries-codegen library](http://alexkasko.github.com/springjdbc-typed-queries/javadocs/codegen).

Plugin usage example
--------------------

Assuming we have file named `com.myapp.foo.Foo$Queries.sql` with SQL queries using
[spring-jdbc named parameters syntax](http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/jdbc/core/namedparam/NamedParameterJdbcTemplate.html)
(query names are important - their prefixes will be used to determine query type):

    -- queries for 'foo' module

    /** selectSomeDataFromFoo */
    select * from foo
        where bar = :bar_name
        and baz < :baz_count
        and boo between :boo_from_date and boo

    /** updateFooSetCurrentDate */
    update foo set baz = now()

Maven configuration (you may use multiple `execution` sections to process multiple files with `codegen` goal and
`generate-sources` phase):

    <build>
        <plugins>
            <plugin>
                <groupId>com.alexkasko.springjdbc.typedqueries</groupId>
                <artifactId>typed-queries-maven-plugin</artifactId>
                <version>1.3.2</version>
                <configuration>
                    <queriesFile>src/main/resources/com.myapp.foo.Foo$Queries.sql</queriesFile>
                </configuration>
            </plugin>
        </plugins>
    </build>

Run from command-line:

    mvn typed-queries:codegen

Or this, if you are using multiple files:

    mvn generate-sources

Generated interface for query parameters (`under_score` names will be converted to `camelCase` ones):

    interface SelectSomeDataFromFoo$Params {
        String getBarName();
        long getBazCount();
        Date getBooFromDate();
    }

Generated methods:

    // constructor, takes queries (name -> text) map and jdbc template, that will be used for query execution
    Foo$Queries(Map<String, String> queries, NamedParameterJdbcTemplate jt)
    // executes select and returns list
    <T> List<T> selectSomeDataFromFoo(SelectSomeDataFromFoo$Params paramsBean, RowMapper<T> mapper)
    // executes select and returns exactly one object
    <T> T selectSomeDataFromFooSingle(SelectSomeDataFromFoo$Params paramsBean, RowMapper<T> mapper)
    // update
    int updateFooSetCurrentDate()

To use these methods from your code you should implement parameters interface (`SelectSomeDataFromFoo$Params`,
it usually may be implemented by existing domain-model objects) and provide a row mapper (
[springjdbc-constructor-mapper](https://github.com/alexkasko/springjdbc-constructor-mapper) may be used).

For queries with single parameters methods with that concrete parameter will be generated instead of parameter wrapper
interface:

    <T> T selectSomething(long fooId, RowMapper<T> mapper)

Additional methods may be generated (enabled by plugin config parameters):
 * execute select and return iterator - `useIterableJdbcTemplate` flag
 * execute update and check that exactly one row was updated - `useCheckSingleRowUpdates` flag
 * execute inserts in batch mode (consuming paramters iterator) - `useBatchInserts` flag
 * substitute placeholders (not query parameters) in SQL string before execution - `useTemplateStringSubstitution` flag

See additional information about these extensions below.

Solving SQL queries maintenance problem
---------------------------------------

This project tries to solve SQL queries maintenance problem in java applications, that
have a lot of SQL queries in external files.

The goal is: **changes in SQL queries that require changes in java code must be detected on compile time**

The popular way to solve this problem in java is to mirror every database table with domain classes and then manage
SQL queries with some kind of ORM ([hibernate](http://www.hibernate.org/) etc). Some ORM's provide tools for generating
domain-model objects directly from database schema. Such method reduces amount of boilerplate DAO code and allows
to write queries in java code using specific api (JPA2 criteria API, [querydsl](http://www.querydsl.com/) etc)
that provides type safety for queries parameters.

This project tries to reach the same goal with SQL queries stored in external files and without generation of domain-model
objects or tying them directly to tables. It parses `query_name->query_text` map from external file and generates
java class with methods for executing each query. For query execution it uses `NamedParameterJdbcTemplate` provided in constructor.
For each query, that takes input parameters, it generates an interface where all parameters must be implemented as getter methods.
Because only SQL file is used for input and no database schema information is known on generation time - parameter names
postfixes may be used as hints for parameter types. For query like this:

    select * from foo
        where bar = :bar
        and baz = :baz
        and boo in (:boo)

plugin will generate interface with `Object` getters:

    interface SelectSomeStuff$Params {
        Object getBar();
        Object getBaz();
        Object getBoo();
    }

It may be enough for some cases - if parameters names or numbers will be changed in SQL file interface will be
regenerated and compilation error will be raised. But spring-jdbc parameters parameters have names, and type
information may be written in postfixes ([default postfix`->`type` mapping](http://alexkasko.github.com/springjdbc-typed-queries/javadocs/codegen/com/alexkasko/springjdbc/typedqueries/codegen/CodeGenerator.Builder.html#setTypeIdMap%28java.util.Map%29)):

    select * from foo
        where bar = :bar_count
        and baz = :baz_date
        and boo in (:boo_list)

will be converted into:

    interface SelectSomeStuff$Params {
        long getBar();
        Date getBaz();
        Collection getBoo();
    }

Custom postfixes configuration example:

    <configuration>
        <typeIdMapJson>{"_date":"java.util.Date","_count":"int","_bytes":"byte[]","_load":"float"}</typeIdMapJson>
    </configuration>

So we've got type-safe queries without harsh restrictions on domain-model classes - they would have some additional
getters as generated interface implementation.

Different mapping may be provided as plugin configuration parameter `typeIdMap` using JSON map, other configuration
parameters may be found in [plugin docs](http://alexkasko.github.com/springjdbc-typed-queries/plugin/codegen-mojo.html).

Plugin generates different methods for `select` queries and different for `create|update|delete` queries. These
queries types are determined by query name prefixes, `select` and `create|update|delete` prefixes are used by default.

How does it work
----------------

###Parsing SQL queries from external files

SQL queries map (`query_name - > query_text`) may be parsed from the next file types:

 * `*.sql` files with query names in javadoc-like comments
 * `*.json` files that contain single `string -> string` map
 * `*.properties` plain text properties using [Properties API](http://docs.oracle.com/javase/6/docs/api/java/util/Properties.html#load%28java.io.Reader%29)
 * `*.xml` XML properties using [Properties API](http://docs.oracle.com/javase/6/docs/api/java/util/Properties.html#loadFromXML%28java.io.InputStream%29)

Plain SQL file syntax:

 * sinle line SQL comments `--foo bar` in file header will be ignored
 * multiline SQL comments `/* foo \n bar */` in header are not supported
 * empty lines are ignored
 * query names must be in javadoc-like comments `/** selectSomeStuff */` on one line
 * query body loaded as is preserving whitespaces and line breaks

The same SQL file should be parsed by application and provided to generated class' constructor as `Map<String, String>`.
To parse plain sql file `typed-queries-common library` ([parser sources](https://github.com/alexkasko/springjdbc-typed-queries/blob/master/typed-queries-common/src/main/java/com/alexkasko/springjdbc/typedqueries/common/PlainSqlQueriesParser.java#L1), 
[javadocs](http://alexkasko.github.com/springjdbc-typed-queries/javadocs/common/com/alexkasko/springjdbc/typedqueries/common/PlainSqlQueriesParser.html)) may be used.

###Query parameters

Query parameters are parsed using [spring-jdbc API](http://static.springsource.org/spring/docs/3.1.x/javadoc-api/org/springframework/jdbc/core/namedparam/NamedParameterUtils.html#parseSqlStatement%28java.lang.String%29).
Interfaces are created for each query parameter set. Generated method takes an implementation of such interface,
wraps it with with [BeanPropertySqlParameterSource](http://static.springsource.org/spring/docs/3.2.x/javadoc-api/org/springframework/jdbc/core/namedparam/BeanPropertySqlParameterSource.html)
and provide it to `JdbcTemplate`.

###Generating java class

Java class is generated using [FreeMarker](http://freemarker.sourceforge.net/) template engine ([default template](https://github.com/alexkasko/springjdbc-typed-queries/blob/master/typed-queries-codegen/src/main/resources/com/alexkasko/springjdbc/typedqueries/codegen/BeanQueries.ftl)).
Different template may be provided through plugin configuration. Generated class contains strict checks on runtime query
map consistency comparing to source SQL file using for generation.

###Iterable extensions for JdbcTemplate

By default generated class uses `NamedParameterJdbcTemplate` for query execution. It also supports
[iterable jdbc template extensions](https://github.com/alexkasko/springjdbc-iterable). Additional methods
returning [CloseableIterator](http://alexkasko.github.com/springjdbc-iterable/javadocs/com/alexkasko/springjdbc/iterable/CloseableIterator.html)
will be generated. Configuration:

    <configuration>
        <queriesFile>src/main/resources/com.myapp.foo.Foo$Queries.sql</queriesFile>
        <useIterableJdbcTemplate>true</useIterableJdbcTemplate>
    </configuration>

It will require [IterableNamedParameterJdbcTemplate](http://alexkasko.github.com/springjdbc-iterable/javadocs/com/alexkasko/springjdbc/iterable/IterableNamedParameterJdbcTemplate.html)
as constructor argument and additional dependency (from maven central):

    <dependency>
        <groupId>com.alexkasko.springjdbc</groupId>
        <artifactId>springjdbc-iterable</artifactId>
        <version>1.0.2</version>
    </dependency>

Additional generated method:

    <T> CloseableIterator<T> selectFooIter(SelectFoo$Params paramsBean, RowMapper<T> mapper)

###Updates with checks

Sometimes it's useful to check that exactly one row was updated in DB after update/delete operation (usually by id).
Plugin can generate additional methods for all DML operations:

    <configuration>
        <queriesFile>src/main/resources/com.myapp.foo.Foo$Queries.sql</queriesFile>
        <useCheckSingleRowUpdates>true</useCheckSingleRowUpdates>
    </configuration>

Additional generated method:

    void updateSomethingSingle(UpdateSomething$Params paramsBean)

###Batch inserts

For `insert` (also `update` and `delete`) queries, those take input parameters, additional `*Batch` method
may be generated, that takes `Iterator` and executes inserts in `batch` mode using [batchUpdate](http://static.springsource.org/spring/docs/3.0.x/api/org/springframework/jdbc/core/namedparam/NamedParameterJdbcTemplate.html#batchUpdate%28java.lang.String,%20org.springframework.jdbc.core.namedparam.SqlParameterSource[]%29)
method:

    <configuration>
        <queriesFile>src/main/resources/com.myapp.foo.Foo$Queries.sql</queriesFile>
        <useBatchInserts>true</useBatchInserts>
    </configuration>

Additional generated method:

    int insertSomethingBatch(Iterator<? extends InsertSomethingBatch$Params> paramsIter, int batchSize)

###Dynamic queries

In some situations JDBC parameters are not enough for query parametrization (e.g. parameters in table names etc).
Plugin supports string placeholder substitution when query in file is a template with placeholders, that are substituted
with provided values before query execution.

_Note: despite all provided parameters are checked using regular expression (`templateValueConstraintRegex`, by default: `^[a-zA-Z0-9_$]*$`),
this feature should **NEVER** be used for substituting **USER PROVIDED** values. [Bad things](http://xkcd.com/327/) may happen._

It's better to use it with internal system parameters known only in runtime,
e.g. creating separate table for each object of some kind and parametrizing table names with object ID's.

For "template" queries all generated methods will contain additional `Object... substitutions` vararg parameters that
will be interpreted as `key1, value1, key2, value2,... keyN, valueN`. Query name regular expression matching is used
to distinguish "temlate" queries from regular ones, `templateRegex` parameter, by default: `^[a-zA-Z0-9_$]*Template$`

Configuration:

    <configuration>
        <queriesFile>src/main/resources/com.myapp.foo.Foo$Queries.sql</queriesFile>
        <useTemplateStringSubstitution>true</useTemplateStringSubstitution>
    </configuration>

Complex placeholder values may cause security problems (SQL injections) so by default values are restricted to `^[a-zA-Z0-9_$]*$` symbols.
This regular expression should be adjusted in plugin settings, e.g.:

    <configuration>
        <templateValueConstraintRegex>^[a-zA-Z0-9_$: &lt;&gt;=]*$</templateValueConstraintRegex>
    </configuration>

Query example:

    /** selectFooTemplate */
    select * from foo_tab_${tabId}
        where bar = :barName

Generated method:

    <T> List<T> selectFooTemplate(SelectFooTemplate$Params paramsBean, RowMapper<T> mapper, Object... substitutions)

Usage example:

    int tabId = 42;
    List<Foo> foos = qrs.selectFooTemplate(bean, mapper, "tabId", tabId);

If this support for dynamic queries is "not dynamic enough" and you need something like Hibernate Criteria API
you may use [query-string-builder](https://github.com/alexkasko/query-string-builder)
library to construct actual query string in runtime. Constructed expressions may be used as vararg values.
Also parameter interface may be generated for all possible parameters of dynamic query using additional placeholder syntax, example:

    /** selectFoo */
    select * from foo ${where(:surname_string, :age_int)} order by id

`${where(:surname, :salary)}` will be used for parameter interface generation, but will be stripped to
`${where}` on parameter substitution, so `"where"` literal should be used as substitution key:

    interface Foo$Queries.SelectFoo$Params {
        String getSurnameString();
        int getAgeInt();
    }
    ...
    Expression expr = Expressions.where();
    if(isNotEmpty(paramsObject.getSurname())) expr = expr.and("surname = :surname_string");
    if(paramsObject.getAge() > 0) expr = expr.and("age = :age_int");
    List<Foo> = qrs.selectFoo(paramsObject, mapper, "where", expr);

Generated query will all parameters present look like:

    select * from foo where surname = :surname_string and age = :age_int order by id

If all parameters are absent `where` prefix will be omitted:

    select * from foo order by id

###Interfaces for `RowMapper`'s

Interfaces for input query parameters is only the first part of compile-time type safety.
Second part is the names and types of result set columns. Configuration to enable interface generation
for result set columns:

    <configuration>
        <queriesFile>src/main/resources/com.myapp.foo.Foo$Queries.sql</queriesFile>
        <generateInterfacesForColumns>true</generateInterfacesForColumns>
    </configuration>

Plugin will parse column list from SQL query and generate `Column` - interface:

    /** selectFoo */
    select foo_name, bar_count from baz

    interface SelectFoo$Columns {
        setFooName(String fooName);
        setBarCount(long barCount);
    }

This interface may be used with <a href="http://static.springsource.org/spring/docs/3.1.x/javadoc-api/org/springframework/jdbc/core/BeanPropertyRowMapper.html">BeanPropertyRowMapper</a>
like this:

    public class FooDomainClass implements Foo$Queries.SelectFoo$Columns {
        public static RowMapper<FooDomainClass> FOO_MAPPER = new BeanPropertyRowMapper(FooDomainClass.class);

        private String fooName;
        private long barCount;

        @Override
        public setFooName(String fooName) {
            this.fooName = fooName;
        }

        @Override
        public setBarCount(long barCount) {
            this.barCount = barCount;
        }
    }

So on query columns change you've got compile-time error.

_Note: in version 1.3 simple hand-written parser is used to parse column aliases from SQL query. Complex
expressions in column lists (e.g. `case-when`) may be parsed incorrectly. Use parentheses for complex expressions
and `as` for proper aliases parsing. ANTLR/JavaCC SQL parser may be used in future_

License information
-------------------

This project is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

Changelog
---------

**1.3.2** (2013-05-22)

 * bugfix for single underscored param (both `under_scored` and `camelCase` param names are provided now)

**1.3.1** (2013-05-09)

 * select/update queries with single parameters without object wrapper

**1.3** (2013-05-09)

 * `under_score` to `camelCase` conversion
 * remove commons-lang dependency
 * generated classes are public by default
 * `Column` interfaces for result set columns
 * queries with single parameters without object wrapper

**1.2.1** (2013-03-24)

 * support for duplicate parameters in the same query

**1.2** (2013-03-12)

 * query templates support (dynamic queries)
 * more settings, extensions disabled by default

**1.1** (2013-03-05)

 * batch inserts support

**1.0** (2013-02-19)

 * initial public version
