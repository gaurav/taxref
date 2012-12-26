
/*
 *
 *  Row
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

/**
 * Represents a single row. Designed to be easy to sort, index and
 * use.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class Row {
	private RowIndex index;
	private String uuid;
	
	// Setting up one-to-many mappings makes RowIndex unnecessarily 
	// complicated :-(. One-to-one is all we can do for now.
	private HashMap<String, Object> data = new HashMap<String, Object>();
	
	public Row(RowIndex i) {
		index = i;
		uuid = UUID.randomUUID().toString();
		index.addRow(this);
	}
	
	public Row(RowIndex i, String existing_uuid) {
		index = i;
		uuid = existing_uuid;
		index.addRow(this);
	}
	
	public String getUUID() {
		return uuid;
	}

	public void put(String key, Object value) {
		data.put(key.toLowerCase(), value);
		if(!index.containsColumn(key))
			index.addColumn(key);
		
		if(Name.class.isAssignableFrom(value.getClass())) {
			index.addName(this, (Name) value);
		}
	}
	
	public Object get(String key) {
		return data.get(key.toLowerCase());
	}
	
	public void createNewColumn(String newColumn, String fromColumn, MapOperation op) {
		put(newColumn, op.mapTo(get(fromColumn)));
	}
	
	/**
	 * Duplicates a column. If this gets too slow, I'll set up a quicker
	 * lookup.
	 * 
	 * @param newColumn
	 * @param fromColumn 
	 */
	public void addColumnAlias(String newColumn, String fromColumn) {
		createNewColumn(newColumn, fromColumn, new MapOperation() {
			@Override
			public Object mapTo(Object value) {
				return value;
			}
		});
	}

	public String[] asArray() {
		String[] results = new String[index.getColumnCount()];
		
		int x = 0;
		for(String colName: index.getColumnNames()) {
			results[x] = get(colName).toString();
		}
		
		return results;
	}
}
