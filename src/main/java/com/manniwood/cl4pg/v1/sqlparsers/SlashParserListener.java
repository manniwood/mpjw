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
package com.manniwood.cl4pg.v1.sqlparsers;

import java.util.ArrayList;
import java.util.List;

/**
 * Listens for each <code>#{getFoo/setFoo}</code> in a Cl4pg SQL template, and
 * stores it in a List of InOutArgs. For instance, the template
 *
 * <pre>
 * <code>
 * { call add_to_first( #{getFirst/setFirst}, #{getSecond} ) }
 * </code>
 * </pre>
 *
 * will create the List of InOutArgs ( "getFirst", "setFirst" ), ( "getSecond )
 * which can be fetched using getArgs().
 * 
 * @author mwood
 *
 */
public class SlashParserListener implements ParserListener {

    private final List<InOutArg> args = new ArrayList<>();

    @Override
    public String arg(String arg) {

        args.add(new InOutArg(arg));
        return "?";
    }

    public List<InOutArg> getArgs() {
        return args;
    }
}
