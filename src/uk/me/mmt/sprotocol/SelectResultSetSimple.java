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

import java.util.List;

/**
 * SparqlResultSet an immutable class implementing SelectResultSet is meant to mimic the Sparql-Results XML format 
 */
public final class SelectResultSetSimple implements SelectResultSet {
    protected List<String> head;
    protected List<SelectResult> results;

    protected SelectResultSetSimple(List<String> head, List<SelectResult> results){
        if (null == head || null == results) {
            throw new IllegalArgumentException("Neither the head or the resulet of the SPARQL-RESULTS xml can be 'null'");
        }
        this.head = head;
        this.results = results;
    }

    public List<SelectResult> getResults() {
        return results;
    }

    public List<String> getHead() {
        return head;
    }

}

/* vi:set ts=8 sts=4 sw=4 et: */
