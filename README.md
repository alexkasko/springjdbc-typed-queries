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
                <groupId>com.alexkasko.springjdbc</groupId>
                <artifactId>typed-queries-maven-plugin</artifactId>
                <version>1.0</version>
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

    // consructor, takes queries (name -> text) map and jdbc template, that will be used for query execution
    Foo$Queries(Map<String, String> queries, NamedParameterJdbcTemplate jt)
    // executes select and returns list
    <T> List<T> selectSomeDataFromFoo(SelectSomeDataFromFoo$Params paramsBean, RowMapper<T> mapper)
    // executes select and returns exactly one object
    <T> T selectSomeDataFromFooSingle(SelectSomeDataFromFoo$Params paramsBean, RowMapper<T> mapper)
    // update without parameters
    int updateFooSetCurrentDate()
    // update, checks that exactly one row was updated
    void updateFooSetCurrentDateSingle()

To use these methods from your code you should implement parameters interface (`SelectSomeDataFromFoo$Params`,
it usually may be implemented by existing domain-model objects) and provide a row mapper (
[springjdbc-constructor-mapper](https://github.com/alexkasko/springjdbc-constructor-mapper) may be used).

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
information may be written in postfixes ([TODO direct link: default postfix`->`type` mapping]()):

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

So we've got type-safe queries without harsh restrictions on domain-model classes - they would have some additional
getters as generated interface implementation.

Different mapping may be provided as plugin configuration parameter `typeIdMap` using JSON map, other configuration
parameters may be found in [TODO direct link: plugin docs]().

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
To parse plain sql file [TODO directlink: typed-queries-common library]() may be used.

###Query parameters

Query parameters are parsed using [spring-jdbc API](http://static.springsource.org/spring/docs/3.1.x/javadoc-api/org/springframework/jdbc/core/namedparam/NamedParameterUtils.html#parseSqlStatement%28java.lang.String%29).
Interfaces are created for each query parameter set. Generated method takes an implementation of such interface,
wraps it with with [BeanPropertySqlParameterSource](http://static.springsource.org/spring/docs/3.2.x/javadoc-api/org/springframework/jdbc/core/namedparam/BeanPropertySqlParameterSource.html)
and provide it to `JdbcTemplate`.

###Generating java class

Java class generated using [FreeMarker](http://freemarker.sourceforge.net/) template engine ([TODO direct link: default template]).
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
will be generated. It will require [IterableNamedParameterJdbcTemplate](http://alexkasko.github.com/springjdbc-iterable/javadocs/com/alexkasko/springjdbc/iterable/IterableNamedParameterJdbcTemplate.html)
as constructor argument and additional dependency (from maven central):

    <dependency>
        <groupId>com.alexkasko.springjdbc</groupId>
        <artifactId>springjdbc-iterable</artifactId>
        <version>1.0.1</version>
    </dependency>

###Dynamic queries

Generated class hold query map and jdbc template provided on construction. So instead of using one of generated methods
you may get query (by name) and jdbc template from generated class and execute query directly. This may be
convenient for dynamic queries, when query template is stored in SQL file and translated into actual query
in runtime using library like [query-string-builder](https://github.com/alexkasko/query-string-builder).

License information
-------------------

This project is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

Changelog
---------

**1.0** (2013-02-19)

 * initial public version