
/*
 *
 *  RowIndexMatch
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
 * Carries out and stores the result of a match.
 * These are joins basically.
 *
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class RowIndexMatch {
	private RowIndex from;
	private RowIndex against;
	private List<String> columnNamesLowercase;
	private List<ColumnMatch> columnMatches;
	
	public RowIndexMatch(RowIndex from, RowIndex against) {
		this.from = from;
		this.against = against;
		
		columnNamesLowercase = from.getColumnNamesLowercase();
		columnMatches = new LinkedList<ColumnMatch>();
		
		int columnIndex = 0;
		for(String colName: from.getColumnNames()) {
			List<Object> values = from.getColumn(colName);
			
			columnMatches.add(new ColumnMatch(colName, values, against));
			
			columnIndex++;
		}
	}
	
	public RowIndex getFrom() {
		return from;
	}
	
	public RowIndex getAgainst() {
		return against;
	}
	
	public ColumnMatch getColumnMatch(String colName) {
		int index = columnNamesLowercase.indexOf(colName.toLowerCase());
		
		return getColumnMatch(index);
	}
	
	public ColumnMatch getColumnMatch(int x) {
		return columnMatches.get(x);
	}
}