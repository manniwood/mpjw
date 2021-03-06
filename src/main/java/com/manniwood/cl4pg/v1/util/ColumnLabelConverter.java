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
package com.manniwood.cl4pg.v1.util;

public class ColumnLabelConverter {

    private ColumnLabelConverter() {
        // utility class
    }

    /**
     * Convert a column label, such as "updated_on" to a Java bean set method,
     * such as "setUpdatedOn". When we are reading rows back from a database,
     * we have access to the column names, and those column names usually follow
     * the snake-case convention. Java setters generally follow the camel-case
     * convention, so it is trivial to convert column names in result sets
     * to bean setter methods in Java.
     *
     * @param label
     * @return
     */
    public static String convert(String label) {
        StringBuilder sb = new StringBuilder();
        sb.append("set");
        sb.append(Character.toUpperCase(label.charAt(0)));
        boolean needsUpper = false;
        for (int i = 1; i < label.length(); i++) {
            char c = label.charAt(i);
            if (c == '_') {
                needsUpper = true;
                continue;
            }
            if (needsUpper) {
                c = Character.toUpperCase(c);
                needsUpper = false;
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
