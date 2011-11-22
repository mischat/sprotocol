/*
    sprotocol - Java SPARQL Protocol Client Library

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
/**
 * Copyright 2011 Mischa Tuffield
 *
 */
package uk.me.mmt.sprotocol;

/**
 * Static class of utility functions used when interacting with the 
 * SPARQL Protocol
 */
public final class SprotocolUtils {

    /**
     * To prevent the accidental instances
     */
    private SprotocolUtils() {
        //empty
    }

    /**
     * This function was written by Cezary Biernacki
     * 
     * Returns a string escaped to be used in a SPARQL query
     *
     * @param sparql - string to be escaped
     *
     * @return escaped string, not null.
     * @throws IllegalArgumentException
     */
    public static String escapeSparqlString(String sparql) throws IllegalArgumentException {
        if (null == sparql) {
            throw new IllegalArgumentException("String to be SPARQL escaped is 'null'");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        int len = sparql.length();
        for (int i = 0; i < len; i++) {
            char c = sparql.charAt(i);
            switch (c) {
                case '\0':
                    // we remove '\0' from strings, as that character hardly would appear in normal data stream
                    // and most of tools will break (including 5store/Keep, Jena etc.) when 0 appears in strings
                    break;
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                default:
                    if (' ' <= c && c < '\u0080') {
                        sb.append(c);
                    } else {
                        sb.append(String.format("\\u%04x", (int)c));
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}

/* vi:set ts=8 sts=4 sw=4 et: */
