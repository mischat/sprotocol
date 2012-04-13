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

import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map.Entry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

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
    protected static Pair<String,String> sparqlQueryAccept(final String query, final RequestType requestType, final String acceptHeader, final String endpoint, final boolean checkMimeType, int timeout) throws SprotocolException, IOException {     

        StringBuilder output = new StringBuilder();
        String contentType = SprotocolConstants.SPARQL_RESULTS_XML_MIME;

        try {
            
            //Identify the correct cgi-parameter name
            String cgi = "query";
            if (requestType.equals(RequestType.UPDATE)) {
                cgi = "update";
            }
            
            // Construct POST data packet
            String data = URLEncoder.encode(cgi, SprotocolConstants.UTF_8) + "=" + URLEncoder.encode(query, SprotocolConstants.UTF_8);

            // Send data
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setReadTimeout(timeout);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", SprotocolConstants.USER_AGENT);
            conn.setRequestProperty("Accept", acceptHeader); 

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.close();

            int code = conn.getResponseCode();
            if ( code > 199 && code < 300) {
                /* Set default content-type to be sparql-xml
                 * Assume this to be the case
                 */
                for (Entry<String, List<String>> header : conn.getHeaderFields().entrySet()) {
                    if (header.getKey() != null && header.getKey().toLowerCase().equals("content-type")) {
                        contentType = header.getValue().get(0);
                    } 
                }

                if (checkMimeType) {
                    boolean isRDFie = false;
                    //Here i should be using "guessSparqlQueryType"
                    for (String mime: SprotocolConstants.SPARQL_MIME_TYPES) {
                        if (contentType.startsWith(mime)) isRDFie = true;
                    }

                    if (!isRDFie) {
                        throw new SprotocolException(String.format("Mime type returned by HTTP request: '{}' not recongised ",contentType), null);
                    }
                }

                // Get the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;

                while ((line = rd.readLine()) != null) {
                    // Process line...
                    output.append(line);
                    output.append("\n");
                }
                rd.close();

            } else {
                throw new SprotocolException(String.format("The result of the POST was a '%s' HTTP response",code), null);
            }
        } catch (SocketTimeoutException e) {    
            throw new SprotocolException("SocketTimeoutException caught", e);
        } catch (IOException e) {
            throw new IOException("IOException caught by sprotocol", e);            
        } catch (Exception e) {
            throw new SprotocolException("Error when making HTTP sparql protocol call to the SPARQL endpoint", e);
        }

        return new Pair<String,String>(output.toString(),contentType);
    }

}

/* vi:set ts=8 sts=4 sw=4 et: */
