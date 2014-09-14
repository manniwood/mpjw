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
package com.manniwood.pg4j.v1.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.manniwood.mpjw.test.etc.TwoInts;
import com.manniwood.pg4j.v1.PgSession;
import com.manniwood.pg4j.v1.commands.CallStoredProcB;
import com.manniwood.pg4j.v1.commands.DDL;
import com.manniwood.pg4j.v1.commands.Select;
import com.manniwood.pg4j.v1.resultsethandlers.GuessScalarListHandler;
import com.manniwood.pg4j.v1.test.exceptionmappers.TestExceptionConverter;

/**
 * Please note that these tests must be run serially, and not all at once.
 * Although they depend as little as possible on state in the database, it is
 * very convenient to have them all use the same db session; so they are all run
 * one after the other so that they don't all trip over each other.
 *
 * @author mwood
 *
 */
public class PgSessionStoredProcTest {

    private PgSession pgSession;

    @BeforeClass
    public void init() {
        pgSession = PgSession.configure()
                .exceptionConverter(new TestExceptionConverter())
                .done();
        pgSession.run(DDL.config().file("sql/create_temp_users_table.sql").done());
        pgSession.run(DDL.config().file("sql/create_swap_func.sql").done());
        pgSession.run(DDL.config().file("sql/create_add_to_first.sql").done());
        pgSession.run(DDL.config().file("sql/create_add_to_last.sql").done());
        pgSession.run(DDL.config().file("sql/create_add_and_return.sql").done());
        pgSession.commit();

    }

    @AfterClass
    public void tearDown() {
        pgSession.run(DDL.config().file("sql/drop_swap_func.sql").done());
        pgSession.run(DDL.config().file("sql/drop_add_to_first.sql").done());
        pgSession.run(DDL.config().file("sql/drop_add_to_last.sql").done());
        pgSession.run(DDL.config().file("sql/drop_add_and_return.sql").done());
        pgSession.commit();
    }

    @Test(priority = 0)
    public void testSwapFunc() {

        TwoInts expected = new TwoInts();
        expected.setFirst(2);
        expected.setSecond(1);

        TwoInts actual = new TwoInts();
        actual.setFirst(1);
        actual.setSecond(2);

        pgSession.run(CallStoredProcB.<TwoInts> config()
                .file("sql/swap.sql")
                .arg(actual)
                .done());
        pgSession.rollback();

        Assert.assertEquals(actual, expected, "Swap needs to have happened.");
    }

    @Test(priority = 1)
    public void testAddToFirstFunc() {

        TwoInts expected = new TwoInts();
        expected.setFirst(5);
        expected.setSecond(3);

        TwoInts actual = new TwoInts();
        actual.setFirst(2);
        actual.setSecond(3);

        pgSession.run(CallStoredProcB.<TwoInts> config()
                .file("sql/add_to_first.sql")
                .arg(actual)
                .done());
        pgSession.rollback();

        Assert.assertEquals(actual,
                            expected,
                            "Add to first needs to have happened.");
    }

    @Test(priority = 2)
    public void testAddToLastFunc() {

        TwoInts expected = new TwoInts();
        expected.setFirst(2);
        expected.setSecond(5);

        TwoInts actual = new TwoInts();
        actual.setFirst(2);
        actual.setSecond(3);

        pgSession.run(CallStoredProcB.<TwoInts> config()
                .file("sql/add_to_last.sql")
                .arg(actual)
                .done());
        pgSession.rollback();

        Assert.assertEquals(actual,
                            expected,
                            "Add to last needs to have happened.");
    }

    @Test(priority = 3)
    public void testAddAndReturnV() {
        GuessScalarListHandler<Integer> handler = new GuessScalarListHandler<Integer>();
        pgSession.run(Select.usingVariadicArgs()
                .sql("select add_and_return from add_and_return(#{java.lang.Integer}, #{java.lang.Integer})")
                .args(1, 2)
                .resultSetHandler(handler)
                .done());
        Integer sum = handler.getList().get(0);
        Assert.assertEquals(sum.longValue(),
                            3,
                            "Stored proc needs to return 3");
    }

    @Test(priority = 4)
    public void testAddAndReturnB() {

        TwoInts addends = new TwoInts();
        addends.setFirst(2);
        addends.setSecond(3);

        GuessScalarListHandler<Integer> handler = new GuessScalarListHandler<Integer>();
        pgSession.run(Select.<TwoInts> usingBeanArg()
                .sql("select add_and_return from add_and_return(#{getFirst}, #{getSecond})")
                .arg(addends)
                .resultSetHandler(handler)
                .done());
        Integer sum = handler.getList().get(0);
        Assert.assertEquals(sum.longValue(),
                            5,
                            "Stored proc needs to return 3");
    }
}
