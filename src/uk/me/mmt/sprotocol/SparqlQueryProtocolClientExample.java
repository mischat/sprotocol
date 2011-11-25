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

import uk.me.mmt.sprotocol.SparqlQueryProtocolClient;
import uk.me.mmt.sprotocol.SprotocolException;

/**
 * 
 * Example class using SparqlQueryProtocolClient
 *
 */
public class SparqlQueryProtocolClientExample {

    public static void main(String[] args) {
        if (args.length == 2) {
            if (args[0].startsWith("http")) {
                SparqlQueryProtocolClient sparql = new SparqlQueryProtocolClient( args[0] );

                try {
                    AnyResult sparqlResult = sparql.genericQuery( args[1] );

                    ResultType sparqlResultType = sparqlResult.getResultType();
                    if (ResultType.BOOLEAN == sparqlResultType) {
                        System.out.println("ASK result sent the answer to the ASK is: '"+sparqlResult.getBooleanResult()+"'");
                    } else if (ResultType.RDF == sparqlResultType) {
                        System.out.println("RDF returned by the SPARQL query");
                        System.out.println(sparqlResult.getRdfResult());
                    } else if (ResultType.SPARQLRESULTS == sparqlResultType) {
                        SelectResultSet sparqlResults = sparqlResult.getSparqlResult();
                        for (SelectResultRow result : sparqlResults) {
                            for (String variable : sparqlResults.getHead() ) {
                                SparqlResource resource =  result.get(variable);
                                System.out.print("This variable '"+variable+"' with this result: '"+resource.getValue()+"' was returned");
                                if (resource instanceof Literal) {
                                    Literal lit = (Literal) resource;
                                    if (lit.getDatatype() != null) {
                                        System.out.print(" with a datatype of "+lit.getDatatype());
                                    }
                                    if (lit.getLanguage() != null) {
                                        System.out.print(" with a language of "+lit.getLanguage());
                                    }
                                }
                                System.out.println();
                            }
                            System.out.println("---------------");
                        }
                    }
                    System.out.println("Finished - awesome");
                } catch (SprotocolException e) {
                    System.err.println(String.format("SPROTOCOL threw one of its own SprotocolException: '%s'",e));
                } catch (IOException e) {
                    System.err.println(String.format("SPROTOCOL threw an IOException: '%s'",e));
                }
            } else {
                System.err.println("The sparql endpoint needs to an http one");
            }
        } else {
            System.err.println("Two parameters please: SparqlProtocolClientExample <sparql query endpoint> <sparql query>");
        }
    }
}

/* vi:set ts=8 sts=4 sw=4 et: */
