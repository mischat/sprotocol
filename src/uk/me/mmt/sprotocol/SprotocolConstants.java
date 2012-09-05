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

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Some Constants used by the library
 */
public class SprotocolConstants {

    public static final String UTF_8 = "UTF-8";
    public static final String SPARQL_RESULTS_XML_MIME = "application/sparql-results+xml";
    public static final String SPARQL_RESULTS_JSN_MIME = "application/sparql-results+json";
    public static final String SPARQL_RESULTS_TSV_MIME = "text/tab-separated-values";
    public static final String SPARQL_RESULTS_CSV_MIME = "text/csv";
    public static final String RDF_XML_MIME = "application/rdf+xml";
    public static final String RDF_TTL_MIME = "text/turtle";
    public static final String RDF_NT_MIME = "text/plain";


    //Known SPARQL response mime-types
    public final static List<String> SPARQL_MIME_TYPES;
    static {
        final ArrayList<String> s = new ArrayList<String>();
        s.add(SPARQL_RESULTS_XML_MIME);
        s.add(SPARQL_RESULTS_JSN_MIME);
        s.add(SPARQL_RESULTS_TSV_MIME);
        s.add(SPARQL_RESULTS_CSV_MIME);
        s.add(RDF_XML_MIME);
        s.add(RDF_TTL_MIME);
        s.add(RDF_NT_MIME);
        SPARQL_MIME_TYPES = Collections.unmodifiableList(s);
    };

    // known SPARQL result mime-types
    public final static List<String> SPARQL_RESULTS_MIME_TYPES;
    static {
        final ArrayList<String> s = new ArrayList<String>();
        s.add(SPARQL_RESULTS_XML_MIME);
        s.add(SPARQL_RESULTS_JSN_MIME);
        s.add(SPARQL_RESULTS_TSV_MIME);
        s.add(SPARQL_RESULTS_CSV_MIME);
        SPARQL_RESULTS_MIME_TYPES = Collections.unmodifiableList(s);
    }

    //Known RDF mime-types
    public final static List<String> RDF_MIME_TYPES; 
    static {
        final ArrayList<String> r = new ArrayList<String>();
        r.add(RDF_XML_MIME);
        r.add(RDF_TTL_MIME);
        r.add(RDF_NT_MIME);
        RDF_MIME_TYPES = Collections.unmodifiableList(r);
    }

    //Timeout settings
    public static final int TIMEOUT = 10000;
    
    //Sprotocol's User Agent
    public static final String USER_AGENT  = "sprotocol/1.1";
    
    //Sprotocol's Accept Header
    public static final String ACCEPT_HEADER = SPARQL_RESULTS_XML_MIME+", "+SPARQL_RESULTS_TSV_MIME+", "+RDF_XML_MIME+", "+RDF_TTL_MIME;
}

/* vi:set ts=8 sts=4 sw=4 et: */
