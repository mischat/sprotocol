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
