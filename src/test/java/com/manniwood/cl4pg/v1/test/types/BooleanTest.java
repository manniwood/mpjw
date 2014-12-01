/*
The MIT License (MIT)

Copyright (c) 2014 Manni Wood

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
package com.manniwood.cl4pg.v1.test.types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.manniwood.cl4pg.v1.DataSourceAdapter;
import com.manniwood.cl4pg.v1.PgSession;
import com.manniwood.cl4pg.v1.PgSessionPool;
import com.manniwood.cl4pg.v1.PgSimpleDataSourceAdapter;
import com.manniwood.cl4pg.v1.commands.DDL;
import com.manniwood.cl4pg.v1.commands.Insert;
import com.manniwood.cl4pg.v1.commands.Select;
import com.manniwood.cl4pg.v1.resultsethandlers.GuessScalarListHandler;

/**
 * Please note that these tests must be run serially, and not all at once.
 * Although they depend as little as possible on state in the database, it is
 * very convenient to have them all use the same db session; so they are all run
 * one after the other so that they don't all trip over each other.
 *
 * @author mwood
 *
 */
public class BooleanTest {
    private final static Logger log = LoggerFactory.getLogger(BooleanTest.class);

    private PgSession pgSession;
    private PgSessionPool pool;

    @BeforeClass
    public void init() {

        DataSourceAdapter adapter = PgSimpleDataSourceAdapter.buildFromDefaultConfFile();
        pool = new PgSessionPool(adapter);
        pgSession = pool.getSession();

        pgSession.run(DDL.config().sql("create temporary table test(col boolean)").done());
        pgSession.commit();
    }

    @AfterClass
    public void tearDown() {
        pgSession.close();
        pool.close();
    }

    /**
     * Truncate the users table before each test.
     */
    @BeforeMethod
    public void truncateTable() {
        pgSession.run(DDL.config().sql("truncate table test").done());
        pgSession.commit();
    }

    @Test(priority = 1)
    public void testTrue() {

        Boolean expected = true;

        pgSession.run(Insert.usingVariadicArgs()
                .sql("insert into test (col) values (#{java.lang.Boolean})")
                .args(expected)
                .done());
        pgSession.commit();

        GuessScalarListHandler<Boolean> handler = new GuessScalarListHandler<Boolean>();
        pgSession.run(Select.usingVariadicArgs()
                .sql("select col from test limit 1")
                .resultSetHandler(handler)
                .done());
        pgSession.rollback();
        Boolean actual = handler.getList().get(0);

        Assert.assertEquals(actual, expected, "scalars must match");
    }

    @Test(priority = 2)
    public void testFalse() {

        Boolean expected = false;

        pgSession.run(Insert.usingVariadicArgs()
                .sql("insert into test (col) values (#{java.lang.Boolean})")
                .args(expected)
                .done());
        pgSession.commit();

        GuessScalarListHandler<Boolean> handler = new GuessScalarListHandler<Boolean>();
        pgSession.run(Select.usingVariadicArgs()
                .sql("select col from test limit 1")
                .resultSetHandler(handler)
                .done());
        pgSession.rollback();
        Boolean actual = handler.getList().get(0);

        Assert.assertEquals(actual, expected, "scalars must match");
    }

    @Test(priority = 3)
    public void testNull() {

        Boolean expected = null;

        pgSession.run(Insert.usingVariadicArgs()
                .sql("insert into test (col) values (#{java.lang.Boolean})")
                .args(expected)
                .done());
        pgSession.commit();

        GuessScalarListHandler<Boolean> handler = new GuessScalarListHandler<Boolean>();
        pgSession.run(Select.usingVariadicArgs()
                .sql("select col from test limit 1")
                .resultSetHandler(handler)
                .done());
        pgSession.rollback();
        Boolean actual = handler.getList().get(0);

        Assert.assertEquals(actual, expected, "scalars must match");
    }

}