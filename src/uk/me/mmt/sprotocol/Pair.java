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
 * A Pair class, as everyone loves pears
 * This one is protected and final, another immutable class
 */
public final class Pair<A, B> {
    protected A first;
    protected B second;
    
    protected Pair(A first, B second) {
        super();
        this.first = first;
        this.second = second;
    }

    public int hashCode() {
        int hashFirst = first != null ? first.hashCode() : 0;
        int hashSecond = second != null ? second.hashCode() : 0;

        return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }

    public boolean equals(Object other) {
        if (other instanceof Pair) {
            @SuppressWarnings("rawtypes")
            Pair otherPair = (Pair) other;
            return 
            ((  this.first == otherPair.first ||
                ( this.first != null && otherPair.first != null &&
                    this.first.equals(otherPair.first))) &&
                        (this.second == otherPair.second ||
                            ( this.second != null && otherPair.second != null &&
                                this.second.equals(otherPair.second))) );
        }
        return false;
    }

    public String toString() { 
        return "(" + first + ", " + second + ")"; 
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

}

/* vi:set ts=8 sts=4 sw=4 et: */

