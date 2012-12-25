
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.table.*;

/**
 * A Name is a scientific name-string.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class Name {
	String namestring = "";
	String monomial = null;
	String genus = null;
	String species = null;
	String subspecies = null;
	String generatedFrom = null;
	
	public Name(String str) {
		this(str, null);
	}
	
	public Name(String str, String generatedFrom) {
		namestring = str;
		
		Pattern p_canonical = Pattern.compile("^\\s*([A-Z][a-z]+)\\s+([a-z]+)(?:\\s+([a-z]+)?)\\b"); // \\s+[a-z]+(?:\\s+[a-z]+))\\b");
		Pattern p_monomial = Pattern.compile("^\\s*([A-Z](?:[a-z]+|[A-Z]+))\\b");
				
		Matcher m = p_canonical.matcher(namestring);
		// System.err.println(m.find() + " between '" + row[col_scientificname] + "' and '" + p_canonical + "'");
		if(m.lookingAt()) {
			genus = m.group(1);
			species = m.group(2);
			subspecies = m.group(3);
		} else {
			m = p_monomial.matcher(namestring);
			if(m.lookingAt()) {
				monomial = m.group(1);
			} else {
				monomial = namestring;
			}
		}
	}
	
	public String getGeneratedFrom() {
		return generatedFrom;
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
