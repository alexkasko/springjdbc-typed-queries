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
        where bar = :barName
        and baz < :bazCount
        and boo between :booFromDate and boo

    /** updateFooSetCurrentDate */
    update foo set baz = now()

Maven configuration (you may use multiple `execution` sections to process multiple files with `codegen` goal and
`generate-sources` phase):

    <build>
        <plugins>
            <plugin>
                <groupId>com.alexkasko.springjdbc.typedqueries</groupId>
                <artifactId>typed-queries-maven-plugin</artifactId>
                <version>1.2</version>
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

Generated interface for query parameters:

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
        where bar = :barCount
        and baz = :bazDate
        and boo in (:booList)

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

Java class is generated using [FreeMarker](http://freemarker.sourceforge.net/) template engine ([TODO direct link: default template]).
Different template may be provided through plugin configuration. Generated class contains strict checks on runtime query
map consistency comparing to source SQL file using for generation.

By default generated class and its methods are package-private. If you need to implement query parameter interface by
the class from different package you need to create in the same package empty public interface extending target one
and use it instead. Plugin may be configured for generating public class and methods using `isPublic` configuration
parameter.

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
this feature should **NEVER** be used for concatenating **USER PROVIDED** values.
[Bad things](http://xkcd.com/327/) may happen. It was designed to use with internal system parameters known only in runtime,
e.g. creating separate table for each object of some kind and parametrizing table names with object ID's_

For "template" queries all generated methods will contain additional `Object... substitutions` vararg parameters that
will be interpreted as `key1, value1, key2, value2,... keyN, valueN`. Query name regular expression matching is used
to distinguish "temlate" queries from regular ones, `templateRegex` parameter, by default: `^[a-zA-Z0-9_$]*Template$`

Configuration:

    <configuration>
        <queriesFile>src/main/resources/com.myapp.foo.Foo$Queries.sql</queriesFile>
        <useTemplateStringSubstitution>true</useTemplateStringSubstitution>
    </configuration>

Placeholder substitution is done using [StrSubstitutor](http://commons.apache.org/proper/commons-lang/javadocs/api-2.6/org/apache/commons/lang/text/StrSubstitutor.html)
class, so additional maven dependency should be added:

    <dependency>
        <groupId>commons-lang</groupId>
        <artifactId>commons-lang</artifactId>
        <version>2.6</version>
    </dependency>

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
you may get query template string from `*$Queries` class using `queryText` method and use [query-string-builder](https://github.com/alexkasko/query-string-builder)
library to construct actual query string in runtime.

License information
-------------------

This project is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

Changelog
---------

**1.2** (2013-03-12)

 * query templates support (dynamic queries)
 * more settings, extensions disabled by default

**1.1** (2013-03-05)

 * batch inserts support

**1.0** (2013-02-19)

 * initial public version
