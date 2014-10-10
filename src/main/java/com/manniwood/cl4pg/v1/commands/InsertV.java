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
package com.manniwood.cl4pg.v1.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import com.manniwood.cl4pg.v1.converters.ConverterStore;
import com.manniwood.cl4pg.v1.sqlparsers.BasicParserListener;
import com.manniwood.cl4pg.v1.sqlparsers.SqlParser;
import com.manniwood.cl4pg.v1.util.SqlCache;
import com.manniwood.cl4pg.v1.util.Str;

public class InsertV implements Command {

    private final String sql;
    private final String filename;
    private final Object[] args;
    private PreparedStatement pstmt;

    private InsertV(Builder builder) {
        this.sql = builder.sql;
        this.filename = builder.filename;
        this.args = builder.args;
    }

    @Override
    public String getSQL() {
        return sql;
    }

    @Override
    public void execute(Connection connection,
                        ConverterStore converterStore,
                        SqlCache sqlCache) throws Exception {
        String theSql = sql == null ? sqlCache.slurpFileFromClasspath(filename) : sql;

        BasicParserListener basicParserListener = new BasicParserListener();
        SqlParser sqlParser = new SqlParser(basicParserListener);
        String transformedSql = sqlParser.transform(theSql);

        PreparedStatement pstmt = connection.prepareStatement(transformedSql);
        List<String> classNames = basicParserListener.getArgs();

        if (classNames != null && !classNames.isEmpty()) {
            for (int i = 0; i < classNames.size(); i++) {
                converterStore.setSQLArgument(pstmt, i + 1, args[i], classNames.get(i));
            }
        }

        pstmt.execute();
    }

    @Override
    public void cleanUp() throws Exception {
        if (pstmt != null) {
            pstmt.close();
        }
    }

    public static Builder config() {
        return new Builder();
    }

    public static class Builder {
        private String sql;
        private String filename;
        private Object[] args;

        public Builder() {
            // null constructor
        }

        public Builder sql(String sql) {
            this.sql = sql;
            return this;
        }

        public Builder file(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder args(Object... args) {
            this.args = args;
            return this;
        }

        public InsertV done() {
            if (Str.isNullOrEmpty(sql) && Str.isNullOrEmpty(filename)) {
                throw new Cl4pgConfigException("SQL string or file must be specified.");
            }
            return new InsertV(this);
        }
    }

}