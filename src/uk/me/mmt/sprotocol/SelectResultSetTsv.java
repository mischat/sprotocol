package uk.me.mmt.sprotocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.bind.DatatypeConverter;

/**
 * A result set built on TSV (Tab Separated Values) results.
 *
 * Allows iteration over TSV results row by row, instead of parsing entire
 * results at once.
 *
 * @author Dave Challis
 */
public class SelectResultSetTsv implements SelectResultSet {
    private final String tsv;
    private final List<String> variables;

    /**
     * Create new iterable result set of TSV results.
     *
     * @param tsv Raw TSV string as returned by SPARQL server
     * @throws SprotcolException if TSV header line cannot be parsed
     */
    public SelectResultSetTsv(String tsv) throws SprotocolException, IOException {
        this.tsv = tsv;

        final BufferedReader br = new BufferedReader(new StringReader(tsv));
        try {
            // get first line listing variables
            final String headerLine = br.readLine();

            if (null == headerLine) {
                throw new SprotocolException("No variables line found in TSV output", null);
            }

            final String[] vars = headerLine.split("\\t");
            final List<String> varList = new ArrayList<String>(vars.length);

            try {
                for (int i = 0; i < vars.length; i++) {
                    // strip leading ? or $ from variable names
                    varList.add(vars[i].substring(1));
                }
            } catch(final ArrayIndexOutOfBoundsException e) {
                throw new SprotocolException("Could not parse variables line: " + headerLine, e);
            }

            this.variables = Collections.unmodifiableList(varList);
        } finally {
            br.close();
        }
    }

    @Override
    public List<String> getHead() {
        return this.variables;
    }

    @Override
    public Iterator<SelectResultRow> iterator() {
        return new TsvRowIterator();
    }

    /**
     * Parse a single row of TSV results into a SelectResultRow.
     *
     * @param line TSV line of tab separated terms
     * @return Single row of results
     * @throws SprotocolException on parse error
     */
    private SelectResultRow parseSparqlResultTsvLine(String line) throws SprotocolException{
        final HashMap<String,SparqlResource> results = new HashMap<String,SparqlResource>();

        final String[] terms = line.split("\\t");
        for (int i = 0; i < terms.length; i++) {
            final SparqlResource resource = getTsvTermAsResource(terms[i]);

            if (resource != null) {
                results.put(this.variables.get(i), resource);
            }
        }

        return new SelectResultRowSimple(results);
    }

    /**
     * Parse a single TSV term into a resource.
     *
     * Term definitions defined in http://www.w3.org/TeamSubmission/turtle/ .
     *
     * @param term Single item in a TSV row
     * @return Resource matching the term type parsed, or null if term is null
     * @throws SprotocolException if term could not be parsed
     */
    private SparqlResource getTsvTermAsResource(String term) throws SprotocolException {
        // no variable bound
        if (null == term) {
            return null;
        }

        final int termLen = term.length();

        // URI, e.g. <http://example.org/foo>
        if (term.charAt(0) == '<' && term.charAt(termLen-1) == '>') {
            try {
                return new IRI(term.substring(1, termLen-1));
            } catch(final IndexOutOfBoundsException e) {
                throw new SprotocolException("Invalid IRI term: " + term, e);
            }
        }

        // Blank node, e.g. _:bnodeId
        if (term.charAt(0) == '_' && term.charAt(1) == ':') {
            try {
                return new BNode(term.substring(2));
            } catch(final IndexOutOfBoundsException e) {
                throw new SprotocolException("Invalid bnode term: " + term, e);
            }
        }

        // Literal, e.g. "foo", 'bar...'
        if ((term.charAt(termLen-1) == '"' && term.charAt(0) == '"') ||
            (term.charAt(termLen-1) == '\'' && term.charAt(0) == '\'')) {
            try {
                return new Literal(unescapeTsvLiteral(term.substring(1, termLen-1)), null, null);
            } catch(final IndexOutOfBoundsException e) {
                throw new SprotocolException("Invalid literal term: " + term, e);
            }
        }

        // Typed literal, e.g. "foo"^^<bar>, 'foo'^^<bob>
        if (term.charAt(termLen-1) == '>') {
            final int caretPos = term.lastIndexOf("^^");
            if (caretPos == -1) {
                throw new SprotocolException("Invalid typed literal term: " + term, null);
            }

            try {
                final String value = unescapeTsvLiteral(term.substring(1, caretPos-1));
                final String dt = term.substring(caretPos+3, termLen-1);
                return new Literal(value, dt, null);
            } catch(final IndexOutOfBoundsException e) {
                throw new SprotocolException("Invalid typed literal term: " + term, e);
            }
        }

        // Literal with language, e.g. "foo"@en
        final int atPos = term.lastIndexOf('@');
        if (atPos > -1) {
            try {
                final String value = unescapeTsvLiteral(term.substring(1, atPos-1));
                final String lang = term.substring(atPos+1, termLen);
                return new Literal(value, null, lang);
            } catch(final IndexOutOfBoundsException e) {
                throw new SprotocolException("Invalid lang literal term: " + term, e);
            }
        }

        // else look for abbreviated data types

        // true or false
        if ("true".equals(term) || "false".equals(term)) {
            return new Literal(term, "http://www.w3.org/2001/XMLSchema#boolean", null);
        }

        // should be xsd:integer
        if (term.indexOf('.') == -1) {
            try {
                DatatypeConverter.parseInteger(term);
                return new Literal(term, "http://www.w3.org/2001/XMLSchema#integer", null);
            } catch (final NumberFormatException e) {
                throw new SprotocolException("Unable to parse term: " + term, e);
            }
        }

        // should be xsd:double
        if (term.indexOf('e') == -1) {
            try {
                DatatypeConverter.parseDouble(term);
                return new Literal(term, "http://www.w3.org/2001/XMLSchema#double", null);
            } catch (final NumberFormatException e) {
                throw new SprotocolException("Unable to parse term: " + term, e);
            }
        }

        // should be xsd:decimal
        try {
            DatatypeConverter.parseDecimal(term);
            return new Literal(term, "http://www.w3.org/2001/XMLSchema#decimal", null);
        } catch (final NumberFormatException e) {
            throw new SprotocolException("Unable to parse term: " + term, e);
        }
    }

    /**
     * Convert backslash escape sequences in returned result back into appropriate character.
     *
     * @param literal Non-null string to unescape characters from.
     * @return Copy of the input string with escape sequences converted
     * @throws SprotocolException on unexpected escape sequence
     */
    private String unescapeTsvLiteral(String literal) throws SprotocolException {
        final int len = literal.length();
        final StringBuilder sb = new StringBuilder(len);

        boolean foundSlash = false;

        for (int i = 0; i < len; i++) {
            final char c = literal.charAt(i);
            if (!foundSlash) {
                if (c == '\\') {
                    foundSlash = true;
                } else {
                    sb.append(c);
                }
            } else {
                switch (c) {
                case '\\':
                    sb.append('\\');
                    break;
                case '"':
                    sb.append('"');
                    break;
                case 'r':
                    sb.append('\r');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case 'n':
                    sb.append('\n');
                    break;
                default:
                    throw new SprotocolException("Unexpected string escape in: " + literal, null);
                }

                foundSlash = false;
            }
        }

        return sb.toString();
    }

    /**
     * Iterator over TSV results.
     *
     * Parses one row of results from TSV string each time it is iterated over.
     */
    private class TsvRowIterator implements Iterator<SelectResultRow> {
        private final BufferedReader results;
        private String currentLine;

        public TsvRowIterator() {
            this.results = new BufferedReader(new StringReader(tsv));

            // skip header line
            try {
                final String header = this.results.readLine();
                if (null == header) {
                    // if no header line, iteration cannot continue
                    this.currentLine = null;
                } else {
                    // advance to first line of actual results
                    this.currentLine = this.results.readLine();
                }
            } catch (final IOException e) {
                // should be impossible for IOException to occur when reading from String
                this.currentLine = null;
            }
        }

        @Override
        public boolean hasNext() {
            return this.currentLine != null;
        }

        @Override
        public SelectResultRow next() {
            if (null == this.currentLine) {
                throw new NoSuchElementException();
            }

            // parse the current line into a result row
            final SelectResultRow row;
            try {
                row = parseSparqlResultTsvLine(this.currentLine);
            } finally {
                // still advance to next line if current line cannot be parsed
                try {
                    this.currentLine = this.results.readLine();
                } catch (final IOException e) {
                    // should be impossible for IOException to occur when reading from String
                    this.currentLine = null;
                }
            }
            return row;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
