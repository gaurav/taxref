
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
 * Carries out and stores the result of a match. Right now, this is mostly
 * used to store state on the match (RowIndex from, RowIndex against). 
 *
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class RowIndexMatch {
	private RowIndex from;
	private RowIndex against;
	private Map<String, ColumnMatch> columnMatches;
	
	/**
	 * Construct a RowIndexMatch. It's fantastically important that we don't
	 * actually do ANYTHING at this point. We just don't have the time or the
	 * (memory) space.
	 * 
	 * @param from The RowIndex being matched.
	 * @param against The RowIndex to match against.
	 */
	public RowIndexMatch(RowIndex from, RowIndex against) {
		this.from = from;
		this.against = against;
		
		columnMatches = new HashMap<String, ColumnMatch>(from.getColumnCount());
	}
	
	/**
	 * @return The RowIndex assumed to be possibly incorrect (the 'from'). 
	 */
	public RowIndex getFrom() {
		return from;
	}
	
	/**
	 * @return The RowIndex assumed to be absolutely correct (the 'against').
	 */
	public RowIndex getAgainst() {
		return against;
	}
	
	/**
	 * Retrieve the ColumnMatch object corresponding to the provided column name.
	 * Again, we'll *try* to delay all actual processing as late as possible,
	 * so this should be a relatively cheap operation. Hopefully.
	 * 
	 * @param colName The column name to look up.
	 * @return A ColumnMatch object which matches that column against ALL the names
	 *		in the 'against' RowIndex.
	 */
	public ColumnMatch getColumnMatch(String colName) {
		if(columnMatches.containsKey(colName.toLowerCase()))
			return columnMatches.get(colName);
		
		if(!from.hasColumn(colName))
			throw new RuntimeException("No such column in from '" + from + "': " + colName);
		
		ColumnMatch columnMatch = new ColumnMatch(from, colName, against);
		columnMatches.put(colName.toLowerCase(), columnMatch);
		return columnMatch;
	}
}