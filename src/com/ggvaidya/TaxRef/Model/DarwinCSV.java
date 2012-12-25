
/*
 *
 *  DarwinCSV
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

package com.ggvaidya.TaxRef.Model;

import au.com.bytecode.opencsv.*;
import java.awt.Component;
import java.awt.Color;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import org.apache.commons.lang3.*;
import java.util.regex.*;
import java.text.*;
import com.ggvaidya.TaxRef.Common.*;

/**
 * A DarwinCSV is a CSV file which contains biodiversity data.
 * This code can also parse tab-delimited and semicolon delimited files,
 * so it should be able to handle any file that comes out of a Darwin Core
 * Archive. Eventually, I'll write a wrapper that can process DwC-A files
 * directly.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class DarwinCSV implements TableModel, TableCellRenderer {
	private File file;
	private int filetype;
	private RowIndex index;
	private char separator = ',';
	
	public static final int FILE_CSV_DELIMITED = 0;
	public static final int FILE_TAB_DELIMITED = 1;
	public static final int FILE_SEMI_DELIMITED = 2;
	
	public static final String[] fieldNamesForNames = {
		"canonicalname", "canonical_name",
		"scientificname", "scientific_name", "scientific name", "name",
		"acceptedNameUsage",  "acceptedName", "acceptedname", "accepted_name",
		"family",
		"genus",
		"specificEpithet", 
		"species",
		"infraspecificEpithet", "subspecies"
	};
	
	public String toString() {
		String type = "CSV";
		if(filetype == FILE_TAB_DELIMITED) {
			type = "tab-delimited";
		} else if(filetype == FILE_SEMI_DELIMITED) {
			type = "semicolon-delimited";
		}
		
		return "DarwinCSV from " + type + " file '" + file.getAbsolutePath() + "' containing " + rowIndex.size() + " rows";
	}
	
	public DarwinCSV(File file, int file_type) throws IOException {
		setup(file, file_type);
	}
	
	/**
	 * Set up this Darwin CSV. In particular, it'll read
	 * the file header and double-check if:
	 * 
	 *	(1) every column name is unique (uniquifying them if not),
	 *	(2) assigning a class based on the column name, and finally
	 *	(3) calculating a canonicalName column just because.
	 * 
	 * @param file The file to process.
	 * @param file_type The file type - CSV, tab-delimited, semi-delimited.
	 */
	public final void setup(File f, int file_type) throws IOException {
		if(file_type == FILE_CSV_DELIMITED) {
			separator = ',';
		} else if(file_type == FILE_TAB_DELIMITED) {
			separator = '\t';
		} else if(file_type == FILE_SEMI_DELIMITED) {
			separator = ';';
		}
		
		// Reset the row index.
		index = new RowIndex();
		List<String> listFieldNamesForNames = Arrays.asList(fieldNamesForNames);
		
		CSVReader csvr = new CSVReader(new BufferedReader(new FileReader(f)), separator);
		List<String> columnNames = new ArrayList<String>();
		String[] header = csvr.readNext();
		for(String originalColumnName: header) {
			String column = originalColumnName;
			
			// What class should this be?
			Class colClass = String.class;
			
			// If it's on the list of "name" field names, mark it as such.
			if(listFieldNamesForNames.contains(column.toLowerCase()))
				colClass = Name.class;
			
			// Rename duplicate column names as "_1", "_2", etc.
			int counter = 1;
			while(columnNames.contains(column)) {
				column = originalColumnName + "_" + String.format("%02d", counter);
				counter++;
			}
			
			columnNames.add(column);
			index.addColumn(column, colClass);
		}
		
		List<String[]> data = csvr.readAll();
		for(String[] rowArray: data) {
			Row row = index.createRow();
			
			int columnIndex = 0;
			for(String field: rowArray) {
				row.put(columnNames.get(columnIndex), field);
				columnIndex++;
			}
		}
		
		if(!index.containsColumn("canonicalname")) {
			if(index.containsColumn("scientificname")) {
				index.createNewColumn("canonicalname", "scientificname", new MapOperation() {
					@Override
					public Object mapTo(Object value) {
						Name n = (Name) value;
						
						return n.getGenus() + " " + n.getSpecies();
					}
				});
			}
			// Err, how do we do this for genus/species?
		}
		
		file = f;
		filetype = file_type;
	}
	
			/*
			if(col_genus >= 0 && col_species >= 0) {
				List<String[]> new_data = new ArrayList<String[]>();
				
				for(String[] row: data) {
					String[] new_row = new String[row.length + 1];
					System.arraycopy(row, 0, new_row, 0, row.length);
					
					String genus = row[col_genus];
					String species = row[col_species];
					String subspecies = "";
					
					if(col_subspecies >= 0) {
						subspecies = row[col_subspecies];
					}
					
					if(genus.length() > 0 && species.length() > 0) {
						if(subspecies.length() > 0) {
							new_row[row.length] = genus + " " + species + " " + subspecies;
						} else {
							new_row[row.length] = genus + " " + species;
						}
					} else {
						new_row[row.length] = "";
					}
					
					if(new_row[row.length] != null) {
						new_row[row.length] = new_row[row.length].trim();
					}
					
					new_data.add(new_row);
				}		
				
				columns.add("canonicalName");
				col_canonicalname = columns.size() - 1;
				data = new_data;
				
				if(col_subspecies >= 0) {
					colsUsedToGenerateCanonicalName = column(col_genus) + ", " + column(col_species) + " and " + column(col_subspecies);
				} else {
					colsUsedToGenerateCanonicalName = column(col_genus) + " and " + column(col_species);
				}	
	
			}
			*/

	private DefaultTableCellRenderer defTableCellRenderer = new DefaultTableCellRenderer();
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component c = defTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setBackground(Color.WHITE);
		
		if(Name.class.isAssignableFrom(value.getClass())) {
			Name name = (Name) value;
			String str = name.toString();
			
			if(matcher == null) {
				if(str.length() == 0) {
					c.setBackground(Color.GRAY);
				} else {
					c.setBackground(new Color(137, 207, 230));
				}
			} else {
				if(str.length() == 0) {
					c.setBackground(Color.GRAY);
				} else if(matcher.hasName(str)) {
					c.setBackground(new Color(0, 128, 0));
				} else if(matcher.hasName(name.getGenus())) {
					c.setBackground(new Color(255, 117, 24));
				} else {
					c.setBackground(new Color(226, 6, 44));
				}
			}
		}
		
		if(hasFocus)
			c.setBackground(c.getBackground().darker());
		
		return c;
	}

	private DarwinCSV matcher = null;
	public void match(DarwinCSV csv_matcher) {
		matcher = csv_matcher;
		System.err.println("Matcher set to " + matcher);
		for(TableModelListener tmi: tmiList) {
			tmi.tableChanged(new TableModelEvent(this, 0, getRowCount()));
		}
	}
	
	public DarwinCSV getMatcher() {
		return matcher;
	}

	public boolean hasName(String str) {
		if(str == null)
			return false;
		return names.contains(str.trim().toLowerCase());
	}

	public void saveToFile(File file, int type) throws IOException {
		CSVWriter writer = null;
		
		if(type == FILE_CSV_DELIMITED) {
			writer = new CSVWriter(new FileWriter(file));
		} else {
			throw new UnsupportedOperationException("File type " + type + " not yet supported!");
		}
		
		writer.writeNext(columns.toArray(new String[columns.size()]));
		writer.writeAll(data);
		writer.flush();
		writer.close();
	}

	public int getCanonicalNameColumn() {
		return col_canonicalname;
	}

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
	
	private String number_and_percentage(int number, int total) {
		return number + "\t" + percentage(number, total) + "%";
	}
	
	NumberFormat nf = null;
	private String percentage(double d1, double d2) {
		if(nf == null) {
			nf = NumberFormat.getNumberInstance();
			nf.setMinimumFractionDigits(1);
		}
		
		return nf.format((d1/d2)*100);
	}
	
	private String percentage(double d1) {
		return percentage(d1, 1.0d);
	} 
	
	public String getColumnInformation(int columnIndex) {
		if(columnIndex == -1)
			return "None";
		
		if(columnIndex < 0 || columnIndex > columns.size())
			return "Invalid column identifier";
		
		String ret = columns.get(columnIndex) + " (#" + (columnIndex + 1) + ")";
		
		if(columnIndex == col_canonicalname && colsUsedToGenerateCanonicalName != null)
			ret += " [autogenerated from " + colsUsedToGenerateCanonicalName + "]";
		
		return ret;
	}
}
