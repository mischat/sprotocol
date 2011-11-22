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

import java.nio.charset.Charset;
import java.util.ArrayList;

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
    
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    /** 
     * reserved    = ";" | "/" | "?" | ":" | "@" | "&" | "=" | "+" | "$" | ","
     * mark        = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
     * alphanum    = [0-9] | [A-Z] | [a-z] 
     * unreserved  = alphanum | mark
     * 
     * The following constant are going to be used to escape IRIs partially encoded IRIs
     * 
     */
    private static final boolean[] RESEVERED_MARKED = new boolean[256];
    static {
        final ArrayList<String> lst = new ArrayList<String>();
        //reserved
        lst.add(";");
        lst.add("/");
        lst.add("?");
        lst.add(":");
        lst.add("@");
        lst.add("&");
        lst.add("=");
        lst.add("+");
        lst.add("$");
        lst.add(",");
        //mark
        lst.add("-");
        lst.add("_");
        lst.add(".");
        lst.add("!");
        lst.add("~");
        lst.add("*");
        lst.add("'");
        lst.add("(");
        lst.add(")");

        for (String string : lst) {
            RESEVERED_MARKED[string.charAt(0) & 0xFF] = true;
        }

        for(char c = 'a'; c <= 'z'; ++c) {
            RESEVERED_MARKED[c] = true;
        }

        for(char c = 'A'; c <= 'Z'; ++c) {
            RESEVERED_MARKED[c] = true;
        }

        for(char c = '0'; c <= '9'; ++c) {
            RESEVERED_MARKED[c] = true;
        }
    }

    /**
     * The Hex's
     */
    private static final char hex[] = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F' };


    /**
     * Checks if something is null, throws exception if it is      * 
     * 
     * @param Any object o
     * @param With a label
     * @throws IllegalArgumentException 
     */
    private static void notNull(Object o, String label) {
        if (null == o) {
            throw new IllegalArgumentException(String.format("A(n) {} to be SPARQL escaped is null", label));
        }
    }
    
    private static void hexEncode(StringBuilder builder, int c) {
        builder.append('%');
        builder.append(hex[c / 16]);
        builder.append(hex[c % 16]);
    }

    /**
     * 
     * @param iri
     * @return
     */
    public static String escapeSparqlIRI(String iri) {
        notNull(iri,"IRI");

        StringBuilder sb = new StringBuilder();
        sb.append("<");
        int len = iri.length();
        for (int i = 0; i < len; i++) {
            char c = iri.charAt(i);
            switch (c) {
            case '<':
                throw new IllegalArgumentException(String.format("The IRI {} contains a '<' ", iri));
            case '>':
                throw new IllegalArgumentException(String.format("The IRI {} contains a '>' ", iri));
            case ' ':
                throw new IllegalArgumentException(String.format("The IRI {} contains a ' ' ", iri));
            case '\t':
                throw new IllegalArgumentException(String.format("The IRI {} contains a '\t' ", iri));
            default:
                sb.append(c);

            }
        }
        sb.append("\"");
        return sb.toString();
    }



    /**
     * The function escapes IRIs, so that they can be used in SPARQL queries
     */
    public static String encodeIRI(String uri) {
        notNull(uri, "uri");

        boolean changed = false;
        //Otherwise escape partially encoded URI
        byte utf8[] = uri.getBytes(UTF_8);
        int len = utf8.length;

        StringBuilder encoded = new StringBuilder(len + 10);

        for (int i = 0; i < len; i++) {
            int b = utf8[i] & 0xFF;
            if (RESEVERED_MARKED[b]) {
                encoded.append((char) b);
            } else {
                changed = true;
                hexEncode(encoded,b);
            }
        }
        return changed ? encoded.toString() : uri;
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
    public static String escapeSparqlString(String sparql) {
        notNull(sparql,"literal");

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
