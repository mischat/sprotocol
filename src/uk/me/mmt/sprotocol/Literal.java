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

/**
 * A simple sparql protocol client, sparql in, sparql-results out
 */

public final class Literal extends SparqlResource {

    private String datatype;
    private String language;

    public Literal(String literal, String dt, String lang) {
        if (null == literal) {
            throw new IllegalArgumentException("The value of a Literal can not be 'null'");
        }
        value = literal;
        datatype = dt;
        language = lang;
    }

    public String getDatatype() {
        return datatype;
    }

    public String getLanguage() {
        return language;
    }

}

/* vi:set ts=8 sts=4 sw=4 et: */
