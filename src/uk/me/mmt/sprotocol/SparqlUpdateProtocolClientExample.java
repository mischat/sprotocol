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

import uk.me.mmt.sprotocol.SparqlUpdateProtocolClient;
import uk.me.mmt.sprotocol.SprotocolException;

/**
 * 
 * Example class using SparqlUpdateProtocolClient
 *
 */
public class SparqlUpdateProtocolClientExample {

    public static void main(String[] args) {
        if (args.length == 2) {
            if (args[0].startsWith("http")) {
                SparqlUpdateProtocolClient sparql = new SparqlUpdateProtocolClient( args[0] );

                try {
                    Pair<String, String> sparqlResult = sparql.genericUpdate( args[1] );
                    System.out.println("The mime-type returned '"+sparqlResult.getFirst()+"' and the content of the response is '"+sparqlResult.getSecond()+"'");
                    System.out.println("Finished - awesome");
                } catch (SprotocolException e) {
                    System.err.println(String.format("SPROTOCOL threw one of its own SprotocolException: '%s'",e));
                } catch (IOException e) {
                    System.err.println(String.format("SPROTOCOL threw an IOException: '%s'",e));
                }
            } else {
                System.err.println("The sparql endpoint needs to be an http one");
            }
        } else {
            System.err.println("Two parameters please: SparqlUpdateProtocolClientExample <sparql update endpoint> <sparql update>");
        }
    }
}

/* vi:set ts=8 sts=4 sw=4 et: */
