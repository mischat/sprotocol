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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private String acceptHeader = SprotocolConstants.ACCEPT_HEADER;

    /**
     * This function will check the mime type of a SPARQL HTTP request to check
     * where a SPARQL-RESULT or a new RDF graph has been returned
     * @throws IOException
     * @throws SprotocolException
     */
    public AnyResult genericQuery(String query) throws SprotocolException, IOException {

        final SparqlResponse response = SparqlProtocolClientUtils.sparqlQueryAccept(query, RequestType.QUERY, this.acceptHeader, this.sparqlEndpoint, true, getTimeout());
        final String contentType = response.getContentType();

        // check if data returned is actual RDF, as opposed to SPARQL results
        if (SprotocolConstants.RDF_MIME_TYPES.contains(contentType)) {
            return new AnyResult(response.getData());
        }

        // should have SPARQL results format of some kind now, since we don't have RDF
        if (!SprotocolConstants.SPARQL_RESULTS_MIME_TYPES.contains(contentType)) {
            throw new SprotocolException("genericQuery couldn't guess the type of result returned"+response.getContentType(), null);
        }

        // check query string for ASK, not possible to determine from results with some formats
        if (isAskQuery(query)) {
            final Pair<Boolean,Boolean> askResponse = processAskResponse(response);
            if (askResponse.getFirst().booleanValue()) {
                return new AnyResult(askResponse.getSecond().booleanValue());
            } else {
                // should only be called if we guessed wrong about it being an ASK
                return new AnyResult(parseSparqlResponse(response));
            }
        } else {
            // SELECT query assumed
            return new AnyResult(parseSparqlResponse(response));
        }
    }

    // TODO: could probably be a more generic getQueryType method instead?
    /**
     * Check whether a SPARQL query is an ASK query or not.
     * @param query A SPARQL query
     * @return true if query looks like an ASK query, false otherwise
     */
    private boolean isAskQuery(String query) {
        final Pattern askRegex = Pattern.compile("\\bASK\\s*\\{", Pattern.CASE_INSENSITIVE);
        final Matcher askMatcher = askRegex.matcher(query);

        return askMatcher.find();
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
        final SparqlResponse response = SparqlProtocolClientUtils.sparqlQueryAccept(query, RequestType.QUERY, this.acceptHeader, this.sparqlEndpoint, true, getTimeout());
        return parseSparqlResponse(response);
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
        final SparqlResponse response = SparqlProtocolClientUtils.sparqlQueryAccept(query, RequestType.QUERY, acceptHeader, this.sparqlEndpoint, true, getTimeout());
        final Pair<Boolean,Boolean> ask = processAskResponse(response);
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
        return executeSparqlRawAccept(query, this.acceptHeader);
    }

    /**
     * Send a SPARQL Query via POST configurable acceptHeader returns a String
     *
     * @throws SprotocolException which is a run time exception
     * @throws IOException are also thrown
     */
    public String executeSparqlRawAccept(String query, String acceptHeader) throws SprotocolException, IOException {
        final SparqlResponse response = SparqlProtocolClientUtils.sparqlQueryAccept(query, RequestType.QUERY, acceptHeader, this.sparqlEndpoint, true, getTimeout());
        return response.getData();
    }

    private SelectResultSet parseSparqlResponse(SparqlResponse response) throws SprotocolException, IOException{
        final String contentType = response.getContentType();

        if (SprotocolConstants.SPARQL_RESULTS_XML_MIME.equals(contentType)) {
            return parseSparqlResultXML(response.getData());
        }

        if (SprotocolConstants.SPARQL_RESULTS_TSV_MIME.equals(contentType)) {
            return parseSparqlResultTsv(response.getData());
        }

        throw new SprotocolException("No SELECT results parser defined for " + contentType, null);
    }

    /**
     * This parses a result binding and returns one of the SPARQL Results Elements
     *
     * @throws SprotocolException which is a run time exception
     */
    private SelectResultRow parseSparqlResult(Element resultEl) throws SprotocolException {
        final HashMap<String,SparqlResource> result = new HashMap<String,SparqlResource>();

        final NodeList bindings = resultEl.getElementsByTagName("binding");
        for (int j = 0 ; j < bindings.getLength();j++) {
            final Element bindingElement = (Element) bindings.item(j);
            final NodeList iriBindings = bindingElement.getElementsByTagName("uri");
            for (int k = 0 ; k < iriBindings.getLength();k++) {
                final Element iriEl = (Element) iriBindings.item(k);
                final String iriValue = iriEl.getTextContent();
                final IRI iri = new IRI(iriValue);
                result.put(bindingElement.getAttribute("name"),iri);
            }
            final NodeList literalBindings = bindingElement.getElementsByTagName("literal");
            for (int k = 0 ; k < literalBindings.getLength();k++) {
                final Element literalEl = (Element) literalBindings.item(k);
                final String literal = literalEl.getTextContent();

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
            final NodeList bnodeBindings = bindingElement.getElementsByTagName("bnode");
            for (int k = 0 ; k < bnodeBindings.getLength();k++) {
                final Element bnodeEl = (Element) bnodeBindings.item(k);
                final String bnodeId = bnodeEl.getTextContent();
                final BNode bnode = new BNode(bnodeId);
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
        final ArrayList<String> head = new ArrayList<String>();
        final ArrayList<SelectResultRow> results = new ArrayList<SelectResultRow>();

        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);

        try {
            //create document builder to parse the xml
            final DocumentBuilder db = dbf.newDocumentBuilder();
            final Document dom = db.parse(new InputSource(new StringReader(xml)));
            //get the root element
            final Element docEle = dom.getDocumentElement();

            final NodeList variables = docEle.getElementsByTagName("variable");
            if (variables.getLength() > 0 ) {
                for (int i = 0 ; i < variables.getLength();i++) {
                    //get the variable element
                    final Element el = (Element) variables.item(i);
                    head.add(el.getAttribute("name"));
                }
            }

            final NodeList result = docEle.getElementsByTagName("result");
            for (int i = 0 ; i < result.getLength();i++) {
                //get the result element
                final Element bindingEl = (Element) result.item(i);
                final SelectResultRow sr = parseSparqlResult(bindingEl);
                results.add(sr);
            }
        } catch (final IOException e) {
            throw new IOException("IOException caught by sprotocol", e);
        } catch(final Exception e){
            throw new SprotocolException("Error parsing XML returned via SPARQL Endpoint", e);
        }

        return new SelectResultSetSimple(head, results);
    }

    /**
     * Parse TSV results into a result set.
     *
     * @param tsv TSV text returned from a SPARQL select query
     * @return
     * @throws SprotocolException on parse error
     * @throws IOException
     */
    private SelectResultSet parseSparqlResultTsv(String tsv) throws SprotocolException, IOException {
        return new SelectResultSetTsv(tsv);
    }

    /**
     * Sends response from an ASK query to the appropriate handler.
     *
     * @param response Response from a SPARQL ASK query
     * @return Pair, first boolean is whether this was an ASK query, second is the ASK response
     * @throws IOException
     * @throws SprotocolException if ASK results couldn't be parsed
     */
    private Pair<Boolean,Boolean> processAskResponse(SparqlResponse response) throws IOException, SprotocolException {
        final String contentType = response.getContentType();

        if (SprotocolConstants.SPARQL_RESULTS_XML_MIME.equals(contentType)) {
            return processAskResponseXML(response.getData());
        }

        throw new SprotocolException("No ASK results parser defined for " + contentType, null);
    }

    /**
     * @param A String representation of DOM
     * @return Pair<Boolean,Boolean> the first boolean is a check for whether the query was an ask query
     * the second boolean is the return value of
     * @throws IOException
     * @throws SprotocolException
     */
    private Pair<Boolean,Boolean> processAskResponseXML(String xml) throws IOException, SprotocolException {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);

        try {
            //create document builder to parse the xml
            final DocumentBuilder db = dbf.newDocumentBuilder();
            final Document dom = db.parse(new InputSource(new StringReader(xml)));
            //get the root element
            final Element docEle = dom.getDocumentElement();

            final NodeList bool = docEle.getElementsByTagName("boolean");
            if (bool.getLength() == 1 ) {
                final String b = bool.item(0).getTextContent();
                if (b.equals("true")) {
                    return new Pair<Boolean,Boolean>(true,true);
                }
                return new Pair<Boolean,Boolean>(true,false);
            }
        } catch (final IOException e) {
            throw new IOException("Error parsing XML returned via SPARQL Endpoint", e);
        } catch(final Exception e){
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

    /**
     * Sets new HTTP accept header this client will use when making requests.
     *
     * @param acceptHeader Accept header to make requests with
     */
    public void setAcceptHeader(String acceptHeader) {
        this.acceptHeader = acceptHeader;
    }

    /**
     * @return HTTP accept header this client will use
     */
    public String getAcceptHeader() {
        return this.acceptHeader;
    }
}

/* vi:set ts=8 sts=4 sw=4 et: */
