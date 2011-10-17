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
 * SparqlResultSet is meant to mimic the Sparql-Results XML format 
 */

public class SelectResultSet {

    //Does nothing
    public SelectResultSet() {
    }

    private List<String> head;

    private List<SelectResult> results;

    public void setResults(List<SelectResult> results) {
        this.results = results;
    }

    public List<SelectResult> getResults() {
        return results;
    }

    public void setHead(List<String> head) {
        this.head = head;
    }

    public List<String> getHead() {
        return head;
    }

}

/* vi:set ts=8 sts=4 sw=4 et: */
