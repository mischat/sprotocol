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
 * 
 * A simple sparql protocol client, sparql query or sparql update, 
 * sparql-results or a pair of mime-type and update result out, zero dependencies
 * 
 */
public class SparqlProtocolClient {
    
    private final SparqlQueryProtocolClient sparqlQuery;
    private final SparqlUpdateProtocolClient sparqlUpdate;

    /**
     * This construct creates both a SPARQL Query or a SPARQL Update
     * @param qEp
     * @param uEp
     */
    public SparqlProtocolClient(String qEp, String uEp) {
        this.sparqlQuery =  new SparqlQueryProtocolClient(qEp);
        this.sparqlUpdate = new SparqlUpdateProtocolClient(uEp);  
    }

    /**
     * This function will check the mime type of a SPARQL HTTP request to check 
     * where a SPARQL-RESULT or a new RDF graph has been returned
     * 
     * @param a Generic SPARQL Query
     * @return a class of type AnyResult
     * @throws IOException 
     * @throws SprotocolException 
     */
    public AnyResult query(String query) throws SprotocolException, IOException {
        return sparqlQuery.genericQuery(query);
    }
    
    /**
     * This function will make a SPARQL Update request to the SPARQL store 
     * 
     * @param a Generic SPARQL Update
     * @return A Pair, including the mime-type and the content of the response
     * @throws SprotocolException
     * @throws IOException
     */
    public Pair<String,String> update(String update) throws SprotocolException, IOException {
        return sparqlUpdate.genericUpdate(update);
    }

}

/* vi:set ts=8 sts=4 sw=4 et: */
