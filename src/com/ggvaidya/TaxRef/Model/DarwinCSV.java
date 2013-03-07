
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
import com.ggvaidya.TaxRef.Common.*;
import com.ggvaidya.TaxRef.Model.Datatype.*;
import java.io.*;
import java.util.*;

/**
 * A DarwinCSV is a CSV file which contains biodiversity data.
 * This code can also parse tab-delimited and semicolon delimited files,
 * so it should be able to handle any file that comes out of a Darwin Core
 * Archive. Eventually, I'll write a wrapper that can process DwC-A files
 * directly.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class DarwinCSV {
	private File file;				// This file.
	private short filetype;			// The type of file (see FILE_* below).
	private RowIndex index;			// The RowIndex we load from this file.
	private char separator = ',';	// The separator we're using for this file.
	
	/** A standard CSV file with headers. */
	public static final short FILE_CSV_DELIMITED = 0;
	
	/** A standard tab-delimited file with headers. */
	public static final short FILE_TAB_DELIMITED = 1;
	
	/** A standard semicolon-delimited file with headers. */
	public static final short FILE_SEMI_DELIMITED = 2;
	
	/** These field names will be case-insensitively marked as Name columns. */
	public static final String[] fieldNamesForNames = {
		"canonicalname", "canonical_name",
		"scientificname", "scientific_name", "scientific name", "name",
		"acceptedNameUsage",  "acceptedName", "acceptedname", "accepted_name",
		"family",
		"genus",
		// "specificEpithet", 
		// "species",
		// "infraspecificEpithet", "subspecies"
	};
	
	public static final String[] fieldNamesForIDs = {
		"id", "taxonid"
	};
	
	/**
	 * Create a DarwinCSV from a delimited file of the type specified.
	 * 
	 * @param file The file to load.
	 * @param file_type The filetype to load (see FILE_* constants in this class).
	 * 
	 * @throws IOException
	 */
	public DarwinCSV(File f, short file_type) throws IOException {
		
		if(file_type == FILE_CSV_DELIMITED) {
			separator = ',';
		} else if(file_type == FILE_TAB_DELIMITED) {
			separator = '\t';
		} else if(file_type == FILE_SEMI_DELIMITED) {
			separator = ';';
		}
		
		// If there's any memory being used by the index, let's clear
		// it out at this point.
		index = null;	
		
		// For convenient, we load up all the field names which may be names
		// into a list (so we can directly call list.contains(...) on them.
		List<String> listFieldNamesForNames = Arrays.asList(fieldNamesForNames);
		List<String> listFieldNamesForIDs = Arrays.asList(fieldNamesForIDs);
		
		// Store column names and classes.
		List<String> columnNames = new ArrayList<String>();
		List<Class> colClasses = new ArrayList<Class>();
		
		// Read the column headers; figure out what class they should be.
		CSVReader csvr = new CSVReader(new BufferedReader(new FileReader(f)), separator);
		String[] header = csvr.readNext();
		
		for(String originalColumnName: header) {
			String column = originalColumnName;
			
			// What class should this be?
			Class colClass = String.class;
			
			// If it's on the list of "name" field names, mark it as such.
			if(listFieldNamesForNames.contains(column.toLowerCase()))
				colClass = Name.class;
			
			// If it's on the list of "id" field names, mark it as such.
			if(listFieldNamesForIDs.contains(column.toLowerCase()))
				colClass = PrimaryKey.class;
			
			// Rename duplicate column names as "_01", "_02", etc.
			int counter = 1;
			while(columnNames.contains(column)) {
				column = originalColumnName + "_" + String.format("%02d", counter);
				counter++;
			}
			
			// Add column to the lists.
			columnNames.add(column);
			colClasses.add(colClass);
		}
		
		// Create a new RowIndex with these column and class names.
		index = new RowIndex(columnNames, colClasses);
		
		// Release memory.
		listFieldNamesForNames = null;
		columnNames = null;
		colClasses = null;
		header = null;
		
		// Read in all the rows, converting Name columns into Name objects.
		double time1 = System.currentTimeMillis();
		String[] rowArray;
		
		int rows_read = 0;
		while((rowArray = csvr.readNext()) != null) {
			Object[] row = new Object[rowArray.length];

			rows_read++;
			//System.err.println("Reading row " + file_row + ", actual row count: " + index.getRowCount());
			if(rowArray.length != index.getColumnCount()) {
				throw new IndexOutOfBoundsException(
					"Expected " + index.getColumnCount() + " columns, but " +
					"read " + rowArray.length + " columns " +
					"on data row " + rows_read + " " +
					"of file '" + f + "'. Please open this file in a CSV editor " +
					"and re-export as CSV so that all rows have the same number " +
					"of columns."
				);
			}
			
			int columnIndex = 0;
			for(String field: rowArray) {
				Class colClass = index.getColumnClass(columnIndex);
				Object value = field;
				
				// Right now, only Name classes are treated specially.
				if(Name.class.isAssignableFrom(colClass)) {
					// Note that Name.getName(..) will return the same Name object
					// for two identical strings!
					value = Name.getName(field);
					
					// We can't index fields yet, because they need to be indexed
					// against the entire row. Only RowIndex can do that!
				} else if(PrimaryKey.class.isAssignableFrom(colClass)) {
					// Don't do ANYTHING -- that just takes up memory. We operate
					// everything by special casing the heck out of this.
					
					// value = new PrimaryKey(field);
				}
				
				row[columnIndex] = value;
				// System.err.println("Setting columnIndex " + columnIndex + " value to " + value.getClass());
				
				columnIndex++;
			}
			
			index.addRow(row);
		}
		double time2 = System.currentTimeMillis();
		System.err.println("In DarwinCSV, time to load data: " + (time2 - time1) + " ms");
		
		csvr.close();
		csvr = null;
		
		// If there is no canonicalName column, construct one based on either
		// the 'scientificname' column or the 'genus'/'species'/'subspecies' column.
		if(!index.hasColumn("canonicalname")) {
			if(index.hasColumn("scientificname")) {
				index.setColumnClass("canonicalname", Name.class);
				
				index.createNewColumn("canonicalname", index.getColumnIndex("scientificname") + 1, "scientificname", new MapOperation() {
					@Override
					public Object mapTo(Object value) {
						Name n = (Name) value;
						
						if(n == null) return null;
						return Name.getName(n.getScientificName());
					}
				});
			} else if(index.hasColumn("genus") && (index.hasColumn("specificEpithet") || index.hasColumn("species"))) {
				index.setColumnClass("canonicalname", Name.class);
				
				// We can do a final here, since we're in the constructor: this code will only ever be run once.
				final int col_genus = index.getColumnIndex("genus");
				final int col_specificepithet = index.getColumnIndex("specificEpithet");
				final int col_species = index.getColumnIndex("species");
				final int col_subspecies = index.getColumnIndex("subspecies");
				
				// Figure out the last valid column to add after: subspecies, species or specificEpithet.
				// Sorry.
				int insertAfter = (col_subspecies != -1 ? col_subspecies : (col_species != -1 ? col_species : col_specificepithet));
				
				index.createNewColumn("canonicalname", insertAfter + 1, new ArrayMapOperation() {
					@Override
					public Object mapTo(Object[] values) {
						Name genus = (Name) values[col_genus];
						
						if(genus == null)
							return null;
						
						String specificEpithet;
						
						// Which column has the species name? It should be 'specificEpithet',
						// but MSW uses 'species', so we accept that as long as there's a
						// 'genus' as well.
						if(col_specificepithet != -1)
							specificEpithet = (String) values[col_specificepithet];
						else
							specificEpithet = (String) values[col_species];
						
						if(col_subspecies == -1)
							return Name.getName(genus.toString() + " " + specificEpithet);
						else {
							String subspecies = (String) values[col_subspecies];
							
							return Name.getName(genus.toString() + " " + specificEpithet + " " + subspecies);
						}
					}
				});
			}
		}
		
		double time3 = System.currentTimeMillis();
		System.err.println("In DarwinCSV, time for canonical name calculation: " + (time3 - time2) + " ms");
		
		// Save the file and filetype for future reference.
		file = f;
		filetype = file_type;
	}

	/**
	 * Write this DarwinCSV back out into a file.
	 * 
	 * @param file File to write to.
	 * @param type File type to use in creating this file. Currently, we only
	 *		support CSV -- other types should be pretty easy to add!
	 * @throws IOException 
	 */
	public void saveToFile(File file, int type) throws IOException {
		CSVWriter writer = null;
		
		if(type == FILE_CSV_DELIMITED) {
			writer = new CSVWriter(new BufferedWriter(new FileWriter(file)));
		} else {
			throw new UnsupportedOperationException("File type " + type + " not yet supported!");
		}
		
		writer.writeNext(index.getColumnNames().toArray(new String[index.getColumnCount()]));
		for(Object[] row: index.getRows()) {
			String[] strings = new String[row.length];
			
			int x = 0;
			for(Object o: row) {
				strings[x] = o.toString();
				x++;
			}
			
			writer.writeNext(strings);
		}
		writer.flush();
		writer.close();
	}
	
	/**
	 * Returns the row index loaded by this file.
	 * 
	 * @return The row index loaded by this file.
	 */
	public RowIndex getRowIndex() {
		return index;
	}
	
	/**
	 * A short description of this object.
	 * 
	 * @return A short description of this object.
	 */
	@Override
	public String toString() {
		String type = "CSV";
		if(filetype == FILE_TAB_DELIMITED) {
			type = "tab-delimited";
		} else if(filetype == FILE_SEMI_DELIMITED) {
			type = "semicolon-delimited";
		}
		
		return "DarwinCSV from " + type + " file '" + file.getAbsolutePath() + "' containing " + index.size() + " rows";
	}
}
