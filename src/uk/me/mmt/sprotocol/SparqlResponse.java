package uk.me.mmt.sprotocol;

/**
 * Encapsulates parts of the response from a SPARQL query.
 *
 * @author Dave Challis
 */
public class SparqlResponse {
    private final String data;
    private final String contentType;
    private final String charset;
    private final String rawContentType;

    public SparqlResponse(String data, String contentType, String charset, String rawContentType) {
        this.data = data;
        this.contentType = contentType;
        this.charset = charset;
        this.rawContentType = rawContentType;
    }

    /**
     * @return Response body from a SPARQL request
     */
    public String getData() {
        return data;
    }

    /**
     * @return MIME type part of Content-type header returned by SPARQL server
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @return Charset part of Content-type header
     */
    public String getCharset() {
        return charset;
    }

    /**
     * @return Full Content-type header returned by SPARQL server
     */
    public String getRawContentType() {
        return rawContentType;
    }
}
