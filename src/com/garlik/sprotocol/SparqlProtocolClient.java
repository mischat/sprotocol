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

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
//import java.io.PrintWriter;
//import java.io.StringReader;
import java.net.Socket;


public class SparqlProtocolClient {
    
    private static final int timeout = 60;
    private static final String USER_AGENT  = "sprotocol/1.1";
    private static final String ACCEPT_HEADER = "text/tab-separated-values, application/sparql-results+xml, application/x-turtle, text/rdf+n3";
    private static final String UTF_8 = "UTF-8";

    public SparqlProtocolClient() {
        System.err.println("This is the constructor");
    }

    public String sparql_query_post (String query, String sparqlEndpoint) {
        String output = "";
        try {
            // Construct data
            String data = URLEncoder.encode("query", UTF_8) + "=" + URLEncoder.encode(query, UTF_8);

            // Send data
            URL url = new URL(sparqlEndpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.close();

            System.err.println("This is the query is '"+query+"' being sent to endpoint '"+sparqlEndpoint+"'");

            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                System.err.println("The result of the POST query to the KB resulted in !200 HTTP response :"+code);
            }

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;

            while ((line = rd.readLine()) != null) {
                // Process line...
                output = output+line+"\n";
            }
            rd.close();

        } catch (Exception e) {
            System.err.println("There was an error post'ing triples to the KB"+e.getMessage());
        }

        return output;
    }
}

/* vi:set ts=8 sts=4 sw=4 et: */
