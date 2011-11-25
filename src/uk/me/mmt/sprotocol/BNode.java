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
 * An immutable BNode class disallows null values
 */
public final class BNode extends SparqlResource {
    
    public BNode(String id) {
        super(id);
    }
    
    @Override
    public BNode asBnode() {
        return this;
    }
    
    @Override
    public boolean isBnode() {
        return true;
    }

    

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.getValue().hashCode();
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        
        if (obj == null)
            return false;
        
        if (!(obj instanceof BNode))
            return false;
        
        BNode other = (BNode) obj;
        if (!this.getValue().equals(other.getValue()))
            return false;
        return true;
    }


    @Override
    public String toString() {
        return String.format("<%s>", this.getValue());
    }
    

}

/* vi:set ts=8 sts=4 sw=4 et: */
