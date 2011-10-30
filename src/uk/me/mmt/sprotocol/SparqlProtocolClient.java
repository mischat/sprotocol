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

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Collections;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * A simple sparql protocol client, sparql in, sparql-results out, zero dependencies
 */
public class SparqlProtocolClient {
    private final String sparqlEndpoint;

    private static final String UTF_8 = "UTF-8";
    private static final String SPARQL_RESULTS_XML_MIME = "application/sparql-results+xml";
    private static final String SPARQL_RESULTS_JSN_MIME = "application/sparql-results+json";
    private static final String SPARQL_RESULTS_TSV_MIME = "text/tab-separated-values";
    private static final String SPARQL_RESULTS_CSV_MIME = "text/csv";
    private static final String RDF_XML_MIME = "application/rdf+xml";
    private static final String RDF_TTL_MIME = "text/turtle";

    //Known RDF mime-types
    public final static List<String> MIME_TYPES;
    static {
        final ArrayList<String> m = new ArrayList<String>();
        m.add(SPARQL_RESULTS_XML_MIME);
        m.add(SPARQL_RESULTS_JSN_MIME);
        m.add(SPARQL_RESULTS_TSV_MIME);
        m.add(SPARQL_RESULTS_CSV_MIME);
        m.add(RDF_XML_MIME);
        m.add(RDF_TTL_MIME);
        MIME_TYPES = Collections.unmodifiableList(m);
    };

    private static final int TIMEOUT = 10000;
    private static final String USER_AGENT  = "sprotocol/1.1";
    private static final String ACCEPT_HEADER = SPARQL_RESULTS_XML_MIME+", "+SPARQL_RESULTS_TSV_MIME+", "+RDF_TTL_MIME+", "+RDF_XML_MIME;

    public SparqlProtocolClient(String sEp) {
        this.sparqlEndpoint = sEp;
    }

    /**
     * Send a SPARQL Select Query and get back a SelectResultSet
     * 
     * @throws SprotocolException which is a run time exception
     * @throws IOException are also thrown 
     */
    public SelectResultSetSimple executeSelect(String query) throws SprotocolException, IOException {

        String xml = sparqlQueryRaw(query);        
        return parseSparqlResultXML(xml);
    }

    /**
     * Send a SPARQL Query via POST
     * 
     * @throws SprotocolException which is a run time exception
     * @throws IOException are also thrown 
     */
    public String sparqlQueryRaw (String query) throws SprotocolException, IOException {
        return sparqlQueryRawAccept(query, ACCEPT_HEADER);
    }

    /**
     * Send a SPARQL Query via POST configurable acceptHeader returns a String
     * 
     * @throws SprotocolException which is a run time exception
     * @throws IOException are also thrown 
     */
    public String sparqlQueryRawAccept (String query, String acceptHeader) throws SprotocolException, IOException {

        StringBuilder output = new StringBuilder();
        try {
            // Construct POST data packet
            String data = URLEncoder.encode("query", UTF_8) + "=" + URLEncoder.encode(query, UTF_8);

            // Send data
            URL url = new URL(this.sparqlEndpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept", acceptHeader); 

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.close();

            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                //Set default content-type to be sparql-xml
                String contentType = SPARQL_RESULTS_XML_MIME;
                for (Entry<String, List<String>> header : conn.getHeaderFields().entrySet()) {
                    if (header.getKey() != null && header.getKey().equals("Content-Type")) {
                        contentType = header.getValue().get(0);
                    } 
                }

                boolean isRDFie = false;
                for (String mime: MIME_TYPES) {
                    if (contentType.startsWith(mime)) isRDFie = true;
                }

                if (isRDFie) {
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
                    throw new SprotocolException(String.format("Mime type returned by HTTP request: '{}' not recongised ",contentType), null);
                }
            } else {
                throw new SprotocolException(String.format("The result of the POST was a '{}' HTTP response",code), null);
            }
        } catch (IOException e) {
            throw new IOException("IOExcetion caught by sprotocol", e);            
        } catch (Exception e) {
            throw new SprotocolException("Error when making HTTP sparql protocol call to the SPARQL endpoint", e);
        }
        return output.toString();
    }

    /**
     * This parses a result binding and returns one of the SPARQL Results Elements
     * 
     * @throws SprotocolException which is a run time exception
     */
    public SelectResult parseSparqlResult(Element resultEl) throws SprotocolException {
        HashMap<String,SparqlResource> result = new HashMap<String,SparqlResource>();

        NodeList bindings = resultEl.getElementsByTagName("binding");
        for (int j = 0 ; j < bindings.getLength();j++) {
            Element bindingElement = (Element) bindings.item(j);
            NodeList iriBindings = bindingElement.getElementsByTagName("uri");
            for (int k = 0 ; k < iriBindings.getLength();k++) {
                Element iriEl = (Element) iriBindings.item(k);
                String iriValue = iriEl.getTextContent();
                IRI iri = new IRI(iriValue);
                result.put(bindingElement.getAttribute("name"),iri);
            }
            NodeList literalBindings = bindingElement.getElementsByTagName("literal");
            for (int k = 0 ; k < literalBindings.getLength();k++) {
                Element literalEl = (Element) literalBindings.item(k);
                String literal = literalEl.getTextContent();

                Literal lit;
                if (literalEl.getAttribute("datatype") != null && !literalEl.getAttribute("datatype").equals("") 
                        && literalEl.getAttribute("xml:lang") != null && !literalEl.getAttribute("xml:lang").equals("")) {
                    lit = new Literal(literal,literalEl.getAttribute("datatype"),literalEl.getAttribute("xml:lang"));
                } else if ((literalEl.getAttribute("datatype") != null && !literalEl.getAttribute("datatype").equals(""))) {
                    lit = new Literal(literal,literalEl.getAttribute("datatype"), null);
                } else if ((literalEl.getAttribute("xml:lang") != null && !literalEl.getAttribute("xml:lang").equals(""))) {
                    lit = new Literal(literal,null,literalEl.getAttribute("xml:lang"));
                } else {
                    lit = new Literal(literal,null,null);                    
                }
                result.put(bindingElement.getAttribute("name"),lit);
            }   
            NodeList bnodeBindings = bindingElement.getElementsByTagName("bnode");
            for (int k = 0 ; k < bnodeBindings.getLength();k++) {
                Element bnodeEl = (Element) bnodeBindings.item(k);
                String bnodeId = bnodeEl.getTextContent();
                BNode bnode = new BNode(bnodeId);
                result.put(bindingElement.getAttribute("name"),bnode);
            }   
        }
        return new SelectResultSimple(result);
    }

    /**
     * This parses a sparql-results XML into a SparqlResultSet
     *
     * @throws SprotocolException which is a run time exception 
     */
    public SelectResultSetSimple parseSparqlResultXML(String xml) throws SprotocolException {
        ArrayList<String> head = new ArrayList<String>();
        ArrayList<SelectResult> results = new ArrayList<SelectResult>();
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);

        try {
            //create document builder to parse the xml
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(new InputSource(new StringReader(xml)));
            //get the root element
            Element docEle = dom.getDocumentElement();

            NodeList variables = docEle.getElementsByTagName("variable");
            if (head != null && variables.getLength() > 0 ) {
                for (int i = 0 ; i < variables.getLength();i++) {
                    //get the variable element
                    Element el = (Element) variables.item(i);
                    head.add(el.getAttribute("name"));
                }
            }

            NodeList result = docEle.getElementsByTagName("result");
            for (int i = 0 ; i < result.getLength();i++) {
                //get the result element
                Element bindingEl = (Element) result.item(i);
                SelectResult sr = parseSparqlResult(bindingEl);
                results.add(sr);
            }

        } catch(Exception e){
            throw new SprotocolException("Error parsing XML returned via SPARQL Endpoint", e);
        }

        return new SelectResultSetSimple(head,results);
    }
}

/* vi:set ts=8 sts=4 sw=4 et: */
