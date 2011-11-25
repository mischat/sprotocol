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
 * Literal class, immutable with option datatype and language
 */
public final class Literal extends SparqlResource {
    private final String datatype;
    private final String language;
    private volatile String forToString;

    public Literal(String literal, String dt, String lang) {
        super(literal);
        datatype = dt;
        language = lang;
    }

    public String getDatatype() {
        return datatype;
    }

    public String getLanguage() {
        return language;
    }
    
    @Override
    public Literal asLiteral() {
        return this;
    }
    
    @Override
    public boolean isLiteral() {
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.datatype == null) ? 0 : this.datatype.hashCode());
        result = prime * result + ((this.language == null) ? 0 : this.language.hashCode());
        result = prime * result + (this.getValue().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Literal))
            return false;
        Literal other = (Literal) obj;
        if (this.datatype == null) {
            if (other.datatype != null)
                return false;
        } else if (!this.datatype.equals(other.datatype))
            return false;
        if (this.language == null) {
            if (other.language != null)
                return false;
        } else if (!this.language.equals(other.language))
            return false;
        
        if (!this.getValue().equals(other.getValue()))
            return false;
        
        return true;
    }

    @Override
    public String toString() {
        if (forToString != null) {
            return forToString;
        }
        
        final String result = prepareToString();
        forToString = result;
        return result;
    }

    private String prepareToString() {
        final String escaped = SprotocolUtils.escapeSparqlString(getValue());
        if (datatype == null && language == null) {
            return escaped;
        }
        
        if (datatype != null) {
            return String.format("%s^^<%s>", escaped, datatype); 
        } else {
            return String.format("%s@%s", escaped, language);
        }
    }
    
    

}

/* vi:set ts=8 sts=4 sw=4 et: */
