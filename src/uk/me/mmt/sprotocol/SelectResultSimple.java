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

import java.util.Map;

/**
 * A simple implementation of the SelectResult interface 
 * This can not be null
 */
public final class SelectResultSimple implements SelectResultRow {
    
    private Map<String, SparqlResource> result;
    
    protected SelectResultSimple(Map<String, SparqlResource> selectResult) {
        if (null == selectResult) {
            throw new IllegalArgumentException("The value of a 'SelectResultRow' SPARQL Result can not be 'null'");
        }
        this.result = selectResult;
    }

    
    @Override
    public SparqlResource get(String variable) {
        return result.get(variable);
    }

}

/* vi:set ts=8 sts=4 sw=4 et: */
