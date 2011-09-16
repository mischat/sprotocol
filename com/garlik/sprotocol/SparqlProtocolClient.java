/*
    sprotocol - Java SPARQL Protocol Client Library

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/**
 * Copyright 2011 Mischa Tuffield for Garlik
 *
 */
package com.garlik.sprotocol;

public class SparqlProtocolClient {
    
    private static final int timeout = 60;
    private static final String USER_AGENT  = "sprotocol/1.1";
    private static final String ACCEPT_HEADER = "text/tab-separated-values, application/sparql-results+xml, application/x-turtle, text/rdf+n3";

    public SparqlProtocolClient() {
        System.out.println("Hello");
    }

    public String sparql_query_post (String triples, String sparqlEndpoint) {
        String output = "This is a test";

        return output;
    }
}

/* vi:set ts=8 sts=4 sw=4 et: */
