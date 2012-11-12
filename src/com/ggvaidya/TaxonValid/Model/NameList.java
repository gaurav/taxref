
/*
 *
 *  NameList
 *  Copyright (C) 2012 Gaurav Vaidya
 *
 *  This file is part of TaxonValid.
 *
 *  TaxonValid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TaxonValid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with TaxonValid.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ggvaidya.TaxonValid.Model;

import java.util.*;

/**
 * A namelist is a list of names.
 * 
 * Eventually, this will parse in information about an entire hierarchy
 * so we can post hierarchy information back out, but for now, it's
 * just a simple list of names.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class NameList {
	private ArrayList<String> all_names = new ArrayList<String>();
	private HashSet<String> indexed_names = new HashSet<String>();
	private boolean all_names_modified = false;
	
	public NameList() {
	}
	
	public void addName(String name) {
		all_names.add(name);
		all_names_modified = true;
	}
	
	public void addNames(List<String> names) {
		all_names.addAll(names);
		all_names_modified = true;
	}
	
	public int count() {
		return all_names.size();
	}
	
	public boolean hasName(String name) {
		if(all_names_modified) {
			System.err.println("Reindexing NameList containing " + count() + " names.");
			indexed_names.addAll(all_names);
			all_names_modified = false;
		}
		
		return indexed_names.contains(name);
	}
}
