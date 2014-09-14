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
package com.manniwood.pg4j.v1.argsetters;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SimpleArgSetter extends BaseArgSetter {

    private final static Logger  log  = LoggerFactory
                                              .getLogger(SimpleArgSetter.class);

    protected final List<String> args = new ArrayList<>();

    @Override
    public int extractArg(char[] chrs,
                          int chrsLen,
                          int i) {
        StringBuilder arg = new StringBuilder();
        while (i < chrsLen && chrs[i] != '}') {
            i++;
            if (chrs[i] != '}') {
                arg.append(chrs[i]);
            }
        }
        if (chrs[i] == '}') {
            log.debug("adding arg: {}", arg.toString());
            args.add(arg.toString());
            arg = null; // done with this; hint to gc
        }
        return i;
    }

}