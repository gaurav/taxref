
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

import java.util.*;
import java.util.regex.*;

/**
 * A Name is a scientific name-string. It identifies content which might
 * be an independent taxon name, such as a genus name, family name, etc.
 * It is not designed to be used with name components, such as species or
 * subspecies names.
 * 
 * It can parse binomials and trinomials into their components; eventually,
 * we'll support proper support for parsing names with authorities and so on.
 * It parses strings lazily -- if you never need the components, the Name will
 * just wrap a String and that is that.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class Name {
	/** The namestring represented by this name. */
	private String namestring;
	
	/** 
	 * The parsed components of the name. These are only generated and stored
	 * as necessary. To save more memory, we could just calculate these by hand,
	 * or store these as index pairs or something.
	 */
	String monomial = null;
	String genus = null;
	String species = null;
	String subspecies = null;
	
	/** Has this name been parsed into components already. */
	private boolean parsed = false;
	
	/* Some of the commented out code use these values to calculate cache hit/miss ratios. */
	private static int cache_hit = 0;
	private static int cache_miss = 0;
	
	/* The getName system allows a single Name to be created for each unique namestring. */
	
	/** A static ap of namestring-to-Name mappings cached here. */
	private static Map<String, Name> cache = new HashMap<String, Name>();
	
	/**
	 * Return a (possibly pre-allocated) Name object corresponding to the provided
	 * name-string.
	 * 
	 * @param name The provided name-string.
	 * @return A Name object, either pre-allocated or newly-allocated.
	 */
	public static Name getName(String name) {
		name = name.trim();
		
		if(cache.containsKey(name)) {
			// System.err.println("Cache hit (" + (cache_hit++) + "): " + name);
			return cache.get(name);
		}
		// System.err.println("Cache miss (" + (cache_miss++) + "): " + name);
		
		Name n = new Name(name);
		cache.put(name, n);
		return n;
	}
	
	/** 
	 * Create a new Name. Does absolutely nothing but trim the incoming name.
	 */
	public Name(String name) {
		namestring = name.trim();
	}
	
	/**
	 * A private method to handle parsing the name-string.
	 */
	private void parseName() {
		if(parsed)
			return;
		
		Pattern p_canonical = Pattern.compile("^\\s*([A-Z][a-z]+)\\s+([a-z]+)(?:\\s+([a-z]+))?\\b"); // \\s+[a-z]+(?:\\s+[a-z]+))\\b");
		Pattern p_monomial = Pattern.compile("^\\s*([A-Z](?:[a-z]+|[A-Z]+))\\b");
		
		Matcher m = p_canonical.matcher(namestring);
		//System.err.println(m.find() + " between '" + row[col_scientificname] + "' and '" + p_canonical + "'");
		if(m.lookingAt()) {
			genus = m.group(1);
			species = m.group(2);
			subspecies = m.group(3);	// may be null
			
		} else {
			m = p_monomial.matcher(namestring);
			if(m.lookingAt()) {
				monomial = m.group(1);
			} else {
				monomial = namestring;
			}
		}
		
		parsed = true;
	}
	
	/**
	 * @return The namestring this Name represents. 
	 */
	public String getNamestring() {
		return namestring;
	}
	
	/**
	 * @return The namestring this Name represents, in lowercase.
	 */
	public String getNamestringLC() {
		return namestring.toLowerCase();
	}
	
	/**
	 * @return The genus name if this Name represents a binomial or trinomial.
	 */
	public Name getGenus() {
		if(!parsed) parseName();
		
		return Name.getName(genus);
	}
	
	/**
	 * @return The specific epithet if this Name represents a binomial or trinomial. 
	 */
	public String getSpecificEpithet() {
		if(!parsed) parseName();
		
		return species;
	}
	
	/**
	 * @return The monomial, if this Name represents a monomial.
	 */
	public String getMonomial() {
		if(!parsed) parseName();
		
		return monomial;
	}
	
	/**
	 * @return The binomial (genus + species) combination. A blank string is
	 * returned if either is missing.
	 */
	public String getBinomial() {
		if(!parsed) parseName();
		
		if(genus == null || species == null)
			return "";
		
		return genus + " " + species;
	}
	
	/**
	 * Returns the monomial, binomial or trinomial parsed. If the input name
	 * contained text after these components (such as an authority), this method
	 * will ignore those.
	 * 
	 * @return The monomial, binomial or trinomial.
	 */
	public String getScientificName() {
		if(!parsed) parseName();
		
		if(subspecies != null && genus != null && species != null)
			return genus + " " + species + " " + subspecies;
		else {
			if(genus != null && species != null)
				return genus + " " + species;
			else
				return monomial;
		}
	}
	
	/**
	 * @return The namestring underlying this Name. 
	 */
	@Override
	public String toString() {
		return namestring;
	}

	/**
	 * Modifies the equals(...) method so that it does a case-insensitive
	 * comparison of name-strings. 
	 * 
	 * @param o The object to compare it with
	 * @return Always false unless o is a Name; true if the two name-strings are
	 * case-insensitively identical.
	 */
	@Override
	public boolean equals(Object o) {
		if(o == null)
			return false;
		
		if(Name.class.isAssignableFrom(o.getClass())) {
			Name n = (Name) o;
			
			if(getNamestringLC().equals(n.getNamestringLC()))
				return true;
			else
				return false;
		} else
			return false;
	}

	/**
	 * Overrides the hashCode method so that it uses just the hashCode of the
	 * underlying name-string. 
	 * 
	 * @return a hashcode, identical to that of the underlying name-string.
	 */
	@Override
	public int hashCode() {
		/*
		 * This code was generated by NetBeans! So cool.
		 * 
		int hash = 7;
		hash = 79 * hash + (this.namestring != null ? this.namestring.hashCode() : 0);
		return hash;
		*/
		
		return namestring.toLowerCase().hashCode();
	}
}
