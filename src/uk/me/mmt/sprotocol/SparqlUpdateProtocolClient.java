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

import java.io.IOException;

/**
 * A simple sparql protocol client, sparql update in, sparql-update result returned along with mime-type, zero dependencies
 */
public class SparqlUpdateProtocolClient {
    
    private final String sparqlEndpoint;
    
    private int timeout = SprotocolConstants.TIMEOUT;

    public SparqlUpdateProtocolClient(String sEp) {
        this.sparqlEndpoint = sEp;
    }

    /**
     * This function will check the mime type of a SPARQL HTTP request to check 
     * where a SPARQL-RESULT or a new RDF graph has been returned
     * 
     * @param A sparql update String
     * @return A Pair, including the mime-type and the content of the response
     * @throws IOException 
     * @throws SprotocolException 
     */
    public Pair<String,String> genericUpdate(String query) throws SprotocolException, IOException {
        final SparqlResponse response = SparqlProtocolClientUtils.sparqlQueryAccept(query, RequestType.UPDATE, SprotocolConstants.ACCEPT_HEADER, sparqlEndpoint, false, getTimeout());
        return new Pair<String,String>(response.getData(), response.getRawContentType());
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

}

/* vi:set ts=8 sts=4 sw=4 et: */
