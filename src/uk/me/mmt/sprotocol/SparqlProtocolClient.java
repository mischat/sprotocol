/*
    sprotocol - Java SPARQL Protocol Client Library

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * A simple sparql protocol client, sparql in, sparql-results out
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

	private static final int TIMEOUT = 500;
	private static final String USER_AGENT  = "sprotocol/1.1";
	private static final String ACCEPT_HEADER = SPARQL_RESULTS_TSV_MIME+", "+SPARQL_RESULTS_XML_MIME+", "+RDF_TTL_MIME+", "+RDF_XML_MIME;

	public SparqlProtocolClient(String sEp) {
		this.sparqlEndpoint = sEp;
	}

	/**
	 * Send a SPARQL Select Query and get back a SelectResultSet
	 */
	public SelectResultSet executeSelect(String query) {

		String xml = sparqlQueryRaw(query);    	
		return parseSparqlResultXML(xml);
	}

	/**
	 * Send a SPARQL Query via POST
	 */
	public String sparqlQueryRaw (String query) {
		return sparqlQueryRawAccept(query, ACCEPT_HEADER);
	}

	/**
	 * Send a SPARQL Query via POST configurable acceptHeader returns a String
	 */
	public String sparqlQueryRawAccept (String query, String acceptHeader) {

		String output = "";
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

			System.err.println("This is the query is '"+query+"' being sent to endpoint '"+this.sparqlEndpoint+"'");
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data);
			wr.close();

			int code = conn.getResponseCode();
			if (code == HttpURLConnection.HTTP_OK) {
				//Set default content-type to be sparql-xml
				String contentType = SPARQL_RESULTS_TSV_MIME;
				for (Entry<String, List<String>> header : conn.getHeaderFields().entrySet()) {
					System.err.println(header.getKey() + "=" + header.getValue());
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
						output = output+line+"\n";
					}
					rd.close();
				} else {
					System.err.println("Mime type not an RDFie related one :"+contentType);
				}
			} else {
				System.err.println("The result of the POST was a '"+code+"' HTTP response");
			}

		} catch (Exception e) {
			System.err.println("There was an error making a SPARQL query via POST: "+e.getMessage());
		}
		return output;
	}

	/**
	 * This parses a result binding and returns one of the SPARQL Results Elements
	 */
	public SelectResult parseSparqlResult(Element resultEl) {
		SelectResult sr = new SelectResult();
		HashMap<String,SparqlResource> result = new HashMap<String,SparqlResource>();

		NodeList bindings = resultEl.getElementsByTagName("binding");
		for (int j = 0 ; j < bindings.getLength();j++) {
			Element bindingElement = (Element) bindings.item(j);
			NodeList iriBindings = bindingElement.getElementsByTagName("uri");
			for (int k = 0 ; k < iriBindings.getLength();k++) {
				Element iriEl = (Element) iriBindings.item(k);
				String iriValue = iriEl.getTextContent();
				IRI iri = new IRI();
				iri.setValue(iriValue);
				result.put(bindingElement.getAttribute("name"),iri);
				sr.setResult(result);
			}
			NodeList literalBindings = bindingElement.getElementsByTagName("literal");
			for (int k = 0 ; k < literalBindings.getLength();k++) {
				Element literalEl = (Element) literalBindings.item(k);
				String literal = literalEl.getTextContent();
				Literal lit = new Literal();
				lit.setValue(literal);
				if (literalEl.getAttribute("datatype") != null && !literalEl.getAttribute("datatype").equals("")) {
					lit.setDatatype(literalEl.getAttribute("datatype"));
				}
				if (literalEl.getAttribute("xml:lang") != null && !literalEl.getAttribute("xml:lang").equals("")) {
					lit.setLanguage(literalEl.getAttribute("xml:lang"));
				}
				result.put(bindingElement.getAttribute("name"),lit);
				sr.setResult(result);
			}   
			NodeList bnodeBindings = bindingElement.getElementsByTagName("bnode");
			for (int k = 0 ; k < bnodeBindings.getLength();k++) {
				Element bnodeEl = (Element) bnodeBindings.item(k);
				String bnodeId = bnodeEl.getTextContent();
				BNode bnode = new BNode();
				bnode.setValue(bnodeId);
				result.put(bindingElement.getAttribute("name"),bnode);
				sr.setResult(result);
			}   
		}
		
		return sr;
	}

		/**
		 * This parses a sparql-results XML into a SparqlResultSet
		 */
		public SelectResultSet parseSparqlResultXML(String xml) {
			SelectResultSet resultSet = new SelectResultSet();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			
			try {
				//create document builder to parse the xml
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document dom = db.parse(new InputSource(new StringReader(xml)));
				//get the root element
				Element docEle = dom.getDocumentElement();

				ArrayList<String> head = new ArrayList<String>();
				NodeList variables = docEle.getElementsByTagName("variable");
				if (head != null && variables.getLength() > 0 ) {
					for (int i = 0 ; i < variables.getLength();i++) {
						//get the variable element
						Element el = (Element) variables.item(i);
						head.add(el.getAttribute("name"));
					}
				}
				
				ArrayList<SelectResult> results = new ArrayList<SelectResult>();
				NodeList result = docEle.getElementsByTagName("result");
				for (int i = 0 ; i < result.getLength();i++) {
					//get the result element
					Element bindingEl = (Element) result.item(i);
					SelectResult sr = parseSparqlResult(bindingEl);
					results.add(sr);
				}

				if (!(head.isEmpty() || results.isEmpty())) {
					resultSet.setHead(head);
					resultSet.setResults(results);
				} else {
					System.err.print("SPARQL Result XML seems to have returned nothing");
				}
				
			} catch(Exception e){
				System.err.println("There was an error parsing the XML sparql-results document: "+e.getMessage());
			}

			return resultSet;
		}
	}

	/* vi:set ts=8 sts=4 sw=4 et: */
