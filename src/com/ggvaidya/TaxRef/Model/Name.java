
/*
 *
 *  Name
 *  Copyright (C) 2012 Gaurav Vaidya
 *
 *  This file is part of TaxRef.
 *
 *  TaxRef is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TaxRef is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with TaxRef.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ggvaidya.TaxRef.Model;

import javax.swing.table.*;

/**
 * A Name is a scientific name-string.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class Name implements TableCellRenderer {
	String namestring = "";
	String monomial = null;
	String genus = null;
	String species = null;
	String subspecies = null;
	
	public Name(String str) {
		namestring = str;
		
		// TODO: Parse out the authority string by this point.
		
		String[] substrings = str.split("\\s+");
		int count = substrings.length;
		
		if(count == 1) {
			monomial = substrings[0];
		} else if(count == 2) {
			genus = substrings[0];
			species = substrings[1];
		} else if(count == 3) {
			genus = substrings[0];
			species = substrings[1];
			subspecies = substrings[2];
		}
	}
	
	public String getGenus() {
		return genus;
	}
	
	public String getSpecificEpithet() {
		return species;
	}
	
	public String getMonomial() {
		return monomial;
	}
	
	public String getSpecies() {
		return genus + " " + species;
	}
	
	public String toString() {
		return namestring;
	}
}
