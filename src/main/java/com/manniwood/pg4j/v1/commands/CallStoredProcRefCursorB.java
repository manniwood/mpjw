/*
The MIT License (MIT)

Copyright (t) 2014 Manni Wood

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
package com.manniwood.pg4j.v1.commands;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.List;

import com.manniwood.pg4j.v1.argsetters.SpecialFirstArgParserListener;
import com.manniwood.pg4j.v1.argsetters.SqlParser;
import com.manniwood.pg4j.v1.converters.ConverterStore;
import com.manniwood.pg4j.v1.resultsethandlers.ResultSetHandler;
import com.manniwood.pg4j.v1.util.Cllctn;
import com.manniwood.pg4j.v1.util.ResourceUtil;
import com.manniwood.pg4j.v1.util.Str;

public class CallStoredProcRefCursorB<A> implements Command {

    private final String sql;
    private final ResultSetHandler resultSetHandler;
    private final A arg;
    private CallableStatement cstmt;

    private CallStoredProcRefCursorB(Builder<A> builder) {
        this.sql = builder.sql;
        this.resultSetHandler = builder.resultSetHandler;
        this.arg = builder.arg;
    }

    @Override
    public String getSQL() {
        return sql;
    }

    @Override
    public void execute(Connection connection,
                        ConverterStore converterStore) throws Exception {

        SpecialFirstArgParserListener specialFirstArgParserListener = new SpecialFirstArgParserListener();
        SqlParser sqlParser = new SqlParser(specialFirstArgParserListener);
        String transformedSql = sqlParser.transform(sql);

        cstmt = connection.prepareCall(transformedSql);
        String firstArg = specialFirstArgParserListener.getFirstArg();
        List<String> getters = specialFirstArgParserListener.getArgs();

        // The first "getter" needs to be the special keyword "refcursor"
        if (Str.isNullOrEmpty(firstArg)) {
            throw new Pg4jSyntaxException("There needs to be a refcursor argument.");
        }
        if (!firstArg.equals("refcursor")) {
            throw new Pg4jSyntaxException("The first argument, " + firstArg + ", needs to be the special refcursor keyword, not " + firstArg + ".");

        }

        // Register the first parameter as type other;
        // later, it will be cast to a result set.
        cstmt.registerOutParameter(1, Types.OTHER);

        if (!Cllctn.isNullOrEmpty(getters)) {
            // Because the first arg is the refcursor arg,
            // the callable statement arg number we start at is 2, not 1.
            converterStore.setSQLArguments(cstmt, arg, getters, 2);
        }

        cstmt.execute();
        ResultSet rs = (ResultSet) cstmt.getObject(1);
        resultSetHandler.init(converterStore, rs);
        while (rs.next()) {
            resultSetHandler.processRow(rs);
        }
    }

    @Override
    public void cleanUp() throws Exception {
        if (cstmt != null) {
            cstmt.close();
        }
    }

    public static <A> Builder<A> config() {
        return new Builder<A>();
    }

    public static class Builder<A> {
        private String sql;
        private ResultSetHandler resultSetHandler;
        private A arg;

        public Builder() {
            // null constructor
        }

        public Builder<A> sql(String sql) {
            this.sql = sql;
            return this;
        }

        public Builder<A> file(String filename) {
            this.sql = ResourceUtil.slurpFileFromClasspath(filename);
            return this;
        }

        public Builder<A> resultSetHandler(ResultSetHandler resultSetHandler) {
            this.resultSetHandler = resultSetHandler;
            return this;
        }

        public Builder<A> arg(A arg) {
            this.arg = arg;
            return this;
        }

        public CallStoredProcRefCursorB<A> done() {
            if (Str.isNullOrEmpty(sql)) {
                throw new Pg4jConfigException("SQL string or file must be specified.");
            }
            if (resultSetHandler == null) {
                throw new Pg4jConfigException("A result set handler must be specified.");
            }
            // beanArgSetter has a default, so that's OK.
            // arg should be allowed to be null for those times
            // when there really is no bean argument.
            return new CallStoredProcRefCursorB<A>(this);
        }
    }

}
