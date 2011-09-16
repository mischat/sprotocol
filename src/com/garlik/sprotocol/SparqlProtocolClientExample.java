
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
 * Copyright 2011 Mischa Tuffield for Garlik
 *
 */
package com.garlik.sprotocol;

import com.garlik.sprotocol.SparqlProtocolClient;

public class SparqlProtocolClientExample {

    public static void main(String[] args) {
        System.out.println("Start the main function"); // Display the string.

        SparqlProtocolClient sparql = new SparqlProtocolClient();
        String out = sparql.sparql_query_post("SELECT * {?S ?P ?O}", "http://mmt.me.uk/sparql");
        
        System.out.println("This is the output '"+out+"'");
    }
    
}

/* vi:set ts=8 sts=4 sw=4 et: */