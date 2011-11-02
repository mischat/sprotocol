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

/**
 * This is a Generic Result for the unspecified SPARQL Query
 */
public final class AnyResult {

    protected ResultType resultType;

    protected String rdfResult;
    
    protected boolean booleanResult;
    
    protected SelectResultSet sparqlResult;
    
    public AnyResult(String resValue) {
        resultType = ResultType.RDF;
        rdfResult = resValue;
    }
    
    public AnyResult(boolean resValue) {
        resultType = ResultType.BOOLEAN;
        booleanResult = resValue;
    }
    
    public AnyResult(SelectResultSet resValue) {
        resultType = ResultType.SPARQLRESULTS;
        sparqlResult = resValue;
    }
    
    public ResultType getResultType() {
        return resultType;
    }

    public String getRdfResult() {
        return rdfResult;
    }

    public boolean getBooleanResult() {
        return booleanResult;
    }
    
    public SelectResultSet getSparqlResult() {
        return sparqlResult;
    }

}

/* vi:set ts=8 sts=4 sw=4 et: */
