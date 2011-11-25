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
 * An immutable abstract class defining what may be contained in a SPARQL result set
 */
public abstract class SparqlResource {

    private final String value;

    public SparqlResource(String value) {
        if (null == value) {
            throw new IllegalArgumentException("The value can not be 'null'");
        }

        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
    public Literal asLiteral() {
        throw new IllegalStateException(this + " is not literal");
    }

    public IRI asIRI() {
        throw new IllegalStateException(this + " is not IRI");
    }

    public BNode asBnode() {
        throw new IllegalStateException(this + " is not bnode");
    }

    public boolean isLiteral() {
        return false;
    }

    public boolean isIRI() {
        return false;
    }

    public boolean isBnode() {
        return false;
    }

    @Override
    abstract public boolean equals(Object obj);
    
    @Override
    abstract public int hashCode();

    @Override
    abstract public String toString();
}

/* vi:set ts=8 sts=4 sw=4 et: */
