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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Util function used by both Sparql Query and Sparql Update
 */
public final class SparqlProtocolClientUtils {

    //To prevent accidental instances
    private SparqlProtocolClientUtils() {

    }

    /**
     * Send a SPARQL Request via POST configurable acceptHeader returns a String
     * 
     * @param
     * @param
     * @param
     * @param
     * @param
     * @returns Pair<String,String> String of the Result returned and the contentType of the string returned
     * @throws SprotocolException which is a run time exception
     * @throws IOException are also thrown 
     * 
     */
    protected static SparqlResponse sparqlQueryAccept(final String query, final RequestType requestType, final String acceptHeader, final String endpoint, final boolean checkMimeType, int timeout) throws SprotocolException, IOException {     

        final StringBuilder output = new StringBuilder();
        final String contentType;
        final String charset;
        final String rawContentType;

        try {
            //Identify the correct cgi-parameter name
            final String cgi;
            if (requestType.equals(RequestType.UPDATE)) {
                cgi = "update";
            } else {
                cgi = "query";
            }

            // Construct POST data packet
            final String data = URLEncoder.encode(cgi, SprotocolConstants.UTF_8) + "=" + URLEncoder.encode(query, SprotocolConstants.UTF_8);

            // Send data
            // Modified: USE GET for queries
            HttpURLConnection conn;
            if (requestType.equals(RequestType.UPDATE)) {
                final URL url = new URL(endpoint);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setReadTimeout(timeout);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("User-Agent", SprotocolConstants.USER_AGENT);
                conn.setRequestProperty("Accept", acceptHeader); 

                OutputStreamWriter wr = null;
                try {
                    wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                    wr.write(data);
                } finally {
                    if (wr != null) {
                        wr.close();
                    }
                }
            } else {
                final URL url = new URL(endpoint + "?" + data);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(timeout);
                conn.setRequestProperty("User-Agent", SprotocolConstants.USER_AGENT);
                conn.setRequestProperty("Accept", acceptHeader); 
            }

            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new SprotocolException(String.format("The result of the POST was a '%s' HTTP response",code), null);
            }

            /* Set default content-type to be sparql-xml
             * Assume this to be the case
             */
            final String ct = conn.getContentType();
            final Pair<String,String> contentTypeCharset = getContentTypeCharset(ct);
            if (contentTypeCharset.getFirst() != null) {
                contentType = contentTypeCharset.getFirst();
                rawContentType = ct;
            } else {
                contentType = SprotocolConstants.SPARQL_RESULTS_XML_MIME;
                rawContentType = SprotocolConstants.SPARQL_RESULTS_XML_MIME;
            }

            charset = contentTypeCharset.getSecond();

            if (checkMimeType && !SprotocolConstants.SPARQL_MIME_TYPES.contains(contentType)) {
                throw new SprotocolException(String.format("Mime type returned by HTTP request: '{}' not recognised ",contentType), null);
            }

            // Get the response
            BufferedReader rd = null;
            try {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line;

                while ((line = rd.readLine()) != null) {
                    // Process line...
                    output.append(line);
                    output.append("\n");
                }
            } finally {
                if (rd != null) {
                    rd.close();
                }
            }
        } catch (SocketTimeoutException e) {    
            throw new SprotocolException("SocketTimeoutException caught", e);
        } catch (IOException e) {
            throw new IOException("IOException caught by sprotocol", e);
        } catch (Exception e) {
            throw new SprotocolException("Error when making HTTP sparql protocol call to the SPARQL endpoint", e);
        }

        return new SparqlResponse(output.toString(), contentType, charset, rawContentType);
    }

    /**
     * Given a raw content-type header, returns the content-type and charset as
     * lower case strings with whitespace trimmed.
     * 
     * If no charset is specified in the header, charset will be returned as null;
     *
     * @param contentTypeHeader Raw content-type header from an HTTP response
     * @return Pair containing trimmed and lowercased content-type and charset Strings
     */
    private static Pair<String,String> getContentTypeCharset(final String contentTypeHeader) {
        if (null == contentTypeHeader) {
            return new Pair<String, String>(null, null);
        }

        final String[] parts = contentTypeHeader.split(";");
        final String contentType = parts[0].toLowerCase().trim();
        String charset = null;

        if (parts.length > 1) {
            final String[] charsetParts = parts[1].split("=");
            if (charsetParts.length == 2 && charsetParts[0].toLowerCase().trim().equals("charset")) {
                charset = charsetParts[1].toLowerCase().trim();
            }
        }

        return new Pair<String,String>(contentType, charset);
    }

}

/* vi:set ts=8 sts=4 sw=4 et: */
