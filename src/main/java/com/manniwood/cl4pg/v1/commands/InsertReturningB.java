/*
The MIT License (MIT)

Copyright (c) 2015 Manni Wood

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package com.manniwood.cl4pg.v1.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manniwood.cl4pg.v1.datasourceadapters.DataSourceAdapter;
import com.manniwood.cl4pg.v1.exceptions.Cl4pgConfigException;
import com.manniwood.cl4pg.v1.resultsethandlers.ResultSetHandler;
import com.manniwood.cl4pg.v1.sqlparsers.BasicParserListener;
import com.manniwood.cl4pg.v1.sqlparsers.SqlParser;
import com.manniwood.cl4pg.v1.typeconverters.TypeConverterStore;
import com.manniwood.cl4pg.v1.util.SqlCache;
import com.manniwood.cl4pg.v1.util.Str;

/**
 * Run a SQL insert/returning command, filling in the SQL statement's arguments
 * using the getters from a bean.
 *
 * @author mwood
 *
 */
public class InsertReturningB<A, R> implements Command {

    private final static Logger log = LoggerFactory.getLogger(InsertReturningB.class);

    private String sql;
    private final String filename;
    private final ResultSetHandler<R> resultSetHandler;
    private final A arg;
    private PreparedStatement pstmt;

    private InsertReturningB(Builder<A, R> builder) {
        this.sql = builder.sql;
        this.filename = builder.filename;
        this.resultSetHandler = builder.resultSetHandler;
        this.arg = builder.arg;
    }

    @Override
    public String getSQL() {
        return sql;
    }

    @Override
    public void execute(Connection connection,
                        TypeConverterStore converterStore,
                        SqlCache sqlCache,
                        DataSourceAdapter dataSourceAdapter) throws Exception {
        if (Str.isNullOrEmpty(sql)) {
            sql = sqlCache.get(filename);
        }

        BasicParserListener basicParserListener = new BasicParserListener();
        SqlParser sqlParser = new SqlParser(basicParserListener);
        String transformedSql = sqlParser.transform(sql);

        PreparedStatement pstmt = connection.prepareStatement(transformedSql);
        List<String> getters = basicParserListener.getArgs();
        if (getters != null && !getters.isEmpty()) {
            converterStore.setSQLArguments(pstmt, arg, getters);
        }

        log.debug("Final SQL:\n{}", dataSourceAdapter.unwrapPgPreparedStatement(pstmt));
        boolean hasResult = pstmt.execute();
        if (hasResult) {
            ResultSet rs = pstmt.getResultSet();
            resultSetHandler.init(converterStore, rs);
            while (rs.next()) {
                resultSetHandler.processRow(rs);
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (pstmt != null) {
            pstmt.close();
        }
    }

    public static <A, R> Builder<A, R> config() {
        return new Builder<A, R>();
    }

    public static class Builder<A, R> {
        private String sql;
        private String filename;
        private ResultSetHandler<R> resultSetHandler;
        private A arg;

        public Builder() {
            // null constructor
        }

        public Builder<A, R> sql(String sql) {
            this.sql = sql;
            return this;
        }

        public Builder<A, R> file(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder<A, R> resultSetHandler(ResultSetHandler<R> resultSetHandler) {
            this.resultSetHandler = resultSetHandler;
            return this;
        }

        public Builder<A, R> arg(A arg) {
            this.arg = arg;
            return this;
        }

        public InsertReturningB<A, R> done() {
            if (Str.isNullOrEmpty(sql) && Str.isNullOrEmpty(filename)) {
                throw new Cl4pgConfigException("SQL string or file must be specified.");
            }
            if (resultSetHandler == null) {
                throw new Cl4pgConfigException("A result set handler must be specified.");
            }
            return new InsertReturningB<A, R>(this);
        }
    }

}
