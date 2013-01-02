
/*
 *
 *  ColumnMatch
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
import javax.swing.event.*;

/**
 * A ColumnMatch matches one column against a RowIndex. This is TaxRef's most
 * common function, so it needs to be fast and furious and all those things.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class ColumnMatch {
	/* The columnName, from and against. */
	private final String columnName;
	private final RowIndex from;
	private final RowIndex against;
	
	public ColumnMatch(RowIndex _from, String colName, RowIndex againstRI) {
		this.columnName = colName;
		this.from = _from;
		this.against = againstRI;
		
		// Set up a table model listener to recalculate match score.
		from.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if(from.getColumnIndex(columnName) == e.getColumn()) {
					calculateMatchScores();
				}
			}
		});
		
		// Calculate match scores.
		calculateMatchScores();
	}
	
	private void calculateMatchScores() {
		// Do the calculation!
	}
	
	
	/*
	public String generateTextSummaryOfColumn(String colName) {
		StringBuilder builder = new StringBuilder();
		
		if(colName == null) {
			// Report on the name and the match.
		
			builder.append("Currently loaded file: ").append(file.getAbsolutePath()).append("\n");
			builder.append("  Number of rows: ").append(data.size()).append("\n");
			builder.append("  Scientific name column: ").append(getColumnInformation(col_scientificname)).append("\n");
			builder.append("  Canonical name column: ").append(getColumnInformation(col_canonicalname)).append("\n");
			
		} else {
			// Report on the column.
			int colIndex = column(colName);
			
			builder.append("Information about column: ").append(getColumnInformation(colIndex)).append("\n");
			
			// This should really be cached!
			int blank_rows = 0;
			int total_non_blank = 0;
			int total_matched = 0;
			int total_genus_matched = 0;
			int total_not_matched = 0;
			HashMap<String, Integer> uniqueValues = new HashMap<String, Integer>();
			HashSet<String> matchedNames = new HashSet<String>();
			HashSet<String> matchedGenusNames = new HashSet<String>();
			boolean matched = false;
			
			for(String[] row: data) {
				String val = row[colIndex];
				if(val == null || val.equals("")) {
					blank_rows++;
				} else {
					total_non_blank++;
					
					if(!uniqueValues.containsKey(val)) {
						uniqueValues.put(val, new Integer(1));
					} else {
						uniqueValues.put(val, new Integer(uniqueValues.get(val).intValue() + 1));
					}
					
					if(matcher != null && (colIndex == col_family || colIndex == col_scientificname || colIndex == col_acceptedname || colIndex == col_canonicalname)) {
						matched = true;
						
						if(matcher.hasName(val)) {
							matchedNames.add(val);
							total_matched++;
						} else if(matcher.hasName(new Name(val).getGenus())) {
							matchedGenusNames.add(val);
							total_genus_matched++;
						} else {
							total_not_matched++;
						}
					}
				}
			}
			
			int possible_values = uniqueValues.size();
			builder.append("  Possible values: ").append(possible_values).append("\n");
			builder.append("    Blank rows: ").append(number_and_percentage(blank_rows, data.size())).append("\n");
			builder.append("    Non-blank rows: ").append(number_and_percentage(total_non_blank, data.size())).append("\n");
			
			if(matched) {
				builder.append("    Names were matched against ").append(matcher.toString()).append(". (percentages are against non-blank rows)\n");
				builder.append("      Matched names: ").append(number_and_percentage(total_matched, total_non_blank)).append("\n");
				builder.append("      Matched genus names: ").append(number_and_percentage(total_genus_matched, total_non_blank)).append("\n");
				builder.append("      Unmatched names: ").append(number_and_percentage(total_not_matched, total_non_blank)).append("\n");
			}
			
			builder.append("\n");
			builder.append("    Values: (percentages refer to total non-blank rows)\n");
		
			ValueComparator<String, Integer> comparator = new ValueComparator<String, Integer>(uniqueValues);
			TreeSet<String> sortByValues = new TreeSet<String>(comparator);
			sortByValues.addAll(uniqueValues.keySet());
			
			for(String val: sortByValues.descendingSet()) {
				if(val == null)
					val = "(null)";
				Integer count = uniqueValues.get(val);
				if(count == null)
					count = new Integer(-1);
				
				String s_matched = "";
				if(matched) {
					if(matchedNames.contains(val))
						s_matched = "\tmatched";
					else if(matchedGenusNames.contains(val))
						s_matched = "\tmatched to genus";
					else
						s_matched = "\tnot matched";
				}
				
				builder.append("\t").append(val).append(s_matched).append("\t").append(number_and_percentage(count.intValue(), total_non_blank)).append("\n");
			}
		}
		
		return builder.toString();
	}
	* */
}
