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

import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * A simple sparql protocol client, sparql query in, sparql-results out, zero dependencies
 */
public class SparqlQueryProtocolClient {
    private final String sparqlEndpoint;

    public SparqlQueryProtocolClient(String sEp) {
        this.sparqlEndpoint = sEp;
    }
    
    private int timeout = SprotocolConstants.TIMEOUT;

    /**
     * This function will check the mime type of a SPARQL HTTP request to check 
     * where a SPARQL-RESULT or a new RDF graph has been returned
     * @throws IOException 
     * @throws SprotocolException 
     */
    public AnyResult genericQuery(String query) throws SprotocolException, IOException {
        
        Pair<String,String> xmlContentType = SparqlProtocolClientUtils.sparqlQueryAccept(query, RequestType.QUERY, SprotocolConstants.ACCEPT_HEADER, this.sparqlEndpoint, true, getTimeout());

        boolean isRDF = false;
        for (String rdfMime : SprotocolConstants.RDF_MIME_TYPES) {
            if (xmlContentType.getSecond().startsWith(rdfMime)) {
                isRDF = true;
                break;
            }
        }

        if (isRDF) {
            return new AnyResult(xmlContentType.getFirst());
        } else if (xmlContentType.getSecond().startsWith(SprotocolConstants.SPARQL_RESULTS_XML_MIME)) {
            Pair<Boolean,Boolean> askResponse = processAskResponse(xmlContentType.getFirst());
            if (askResponse.getFirst().booleanValue()) {
                return new AnyResult(askResponse.getSecond().booleanValue());
            } else {
                return new AnyResult(parseSparqlResultXML(xmlContentType.getFirst()));
            }
        } else {
            throw new SprotocolException("genericQuery couldn't guess the type of result returned"+xmlContentType.getSecond(), null);
        }
    }

    /**
     * Send a SPARQL SELECT Query and get back a SelectResultSet
     * 
     * @param query SPARQL SELECT
     * @return A SelectResultSet with the results of the SELECT Query (mimics SPARQL-RESULTS format)
     * @throws SprotocolException which is a run time exception
     * @throws IOException are also thrown when parsing the XML
     */
    public SelectResultSet executeSelect(String query) throws SprotocolException, IOException {
        String xml = executeSparqlRaw(query);        
        return parseSparqlResultXML(xml);
    }

    /**
     * Send a SPARQL ASK Query and get back a boolean
     * 
     * @param query SPARQL ASK
     * @return boolean 
     * @throws SprotocolException which is a run time exception
     * @throws IOException are also thrown when parsing the XML
     */
    public boolean executeAsk(String query) throws SprotocolException, IOException {
        String xml = executeSparqlRaw(query); 
        Pair<Boolean,Boolean> ask = processAskResponse(xml);
        if (ask.getFirst().booleanValue() == false) {
            throw new SprotocolException("Query not of type SPARQL ASK",null);  
        }
        return ask.getSecond().booleanValue();
    }

    /**
     * Send a SPARQL CONSTRUCT and get back a String
     * 
     * @param query a construct query
     * @return and RDF fragment as a plain old string
     * @throws SprotocolException
     * @throws IOException
     */
    public String executeConstruct(String query) throws SprotocolException, IOException {
        return executeSparqlRaw(query);
    }

    /**
     * Send a SPARQL CONSTRUCT and get back a String
     * 
     * @param query a construct query
     * @param accept MIME-TYPE, i.e. RDF or Turtle, or ...
     * @return and RDF fragment as a plain old string
     * @throws SprotocolException
     * @throws IOException
     */
    public String executeConstruct(String query, String accept) throws SprotocolException, IOException {
        return executeSparqlRawAccept(query, accept);
    }


    /**
     * Send a SPARQL DESCRIBE and get back a String
     * 
     * @param query a construct query
     * @return and RDF fragment as a plain old string
     * @throws SprotocolException
     * @throws IOException
     */
    public String executeDescribe(String query) throws SprotocolException, IOException {
        return executeSparqlRaw(query);
    }

    /**
     * Send a SPARQL DESCRIBE and get back a String
     * 
     * @param query a construct query
     * @return and RDF fragment as a plain old string
     * @throws SprotocolException
     * @throws IOException
     */
    public String executeDescribe(String query, String accept) throws SprotocolException, IOException {
        return executeSparqlRawAccept(query, accept);
    }

    /**
     * Send a SPARQL Query via POST
     * 
     * @throws SprotocolException which is a run time exception
     * @throws IOException are also thrown 
     */
    public String executeSparqlRaw(String query) throws SprotocolException, IOException {
        return executeSparqlRawAccept(query, SprotocolConstants.ACCEPT_HEADER);
    }

    /**
     * Send a SPARQL Query via POST configurable acceptHeader returns a String
     * 
     * @throws SprotocolException which is a run time exception
     * @throws IOException are also thrown 
     */
    public String executeSparqlRawAccept(String query, String acceptHeader) throws SprotocolException, IOException {
        return SparqlProtocolClientUtils.sparqlQueryAccept(query, RequestType.QUERY, acceptHeader, this.sparqlEndpoint, true, getTimeout()).getFirst();
    }

    /**
     * This parses a result binding and returns one of the SPARQL Results Elements
     * 
     * @throws SprotocolException which is a run time exception
     */
    private SelectResultRow parseSparqlResult(Element resultEl) throws SprotocolException {
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
        return new SelectResultRowSimple(result);
    }

    /**
     * This parses a sparql-results XML into a SparqlResultSet
     *
     * @throws SprotocolException which is a run time exception 
     */
    private SelectResultSet parseSparqlResultXML(String xml) throws SprotocolException, IOException {
        ArrayList<String> head = new ArrayList<String>();
        ArrayList<SelectResultRow> results = new ArrayList<SelectResultRow>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);

        try {
            //create document builder to parse the xml
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(new InputSource(new StringReader(xml)));
            //get the root element
            Element docEle = dom.getDocumentElement();

            NodeList variables = docEle.getElementsByTagName("variable");
            if (variables.getLength() > 0 ) {
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
                SelectResultRow sr = parseSparqlResult(bindingEl);
                results.add(sr);
            }
        } catch (IOException e) {
            throw new IOException("IOException caught by sprotocol", e);     
        } catch(Exception e){
            throw new SprotocolException("Error parsing XML returned via SPARQL Endpoint", e);
        }

        return new SelectResultSetSimple(head, results);
    }

    /**
     * @param A String representation of DOM
     * @return Pair<Boolean,Boolean> the first boolean is a check for whether the query was an ask query
     * the second boolean is the return value of
     * @throws IOException
     * @throws SprotocolException
     */
    private Pair<Boolean,Boolean> processAskResponse(String xml) throws IOException, SprotocolException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);

        try {
            //create document builder to parse the xml
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(new InputSource(new StringReader(xml)));
            //get the root element
            Element docEle = dom.getDocumentElement();

            NodeList bool = docEle.getElementsByTagName("boolean");
            if (bool.getLength() == 1 ) {
                String b = bool.item(0).getTextContent();
                if (b.equals("true")) {
                    return new Pair<Boolean,Boolean>(true,true);
                }
                return new Pair<Boolean,Boolean>(true,false);
            }
        } catch (IOException e) {
            throw new IOException("Error parsing XML returned via SPARQL Endpoint", e);
        } catch(Exception e){
            throw new SprotocolException("Error parsing XML returned via SPARQL Endpoint", e);
        }

        return new Pair<Boolean,Boolean>(false,false);
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }
}

/* vi:set ts=8 sts=4 sw=4 et: */
