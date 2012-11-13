
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

package com.ggvaidya.TaxonValid.Model;

import au.com.bytecode.opencsv.*;
import java.io.*;
import java.util.*;
import org.apache.commons.lang3.*;

/**
 * A DarwinCSV is a CSV file with unique, non-repeating column names.
 * This code can also parse tab-delimited and semicolon delimited files,
 * so it should be able to handle any file that comes out of a Darwin Core
 * Archive. Eventually, I'll write a wrapper that can process DwC-A files
 * directly.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class DarwinCSV {
	private char separator = ',';
	private char quotechar = '"';
	
	private HashMap<String, Integer> columnNames;
	private String[] columns;
	private List<String[]> data;
	
	private int col_scientificname = -1;
	private int col_canonicalname = -1;
	
	public static final int FILE_CSV_DELIMITED = 0;
	public static final int FILE_TAB_DELIMITED = 1;
	public static final int FILE_SEMI_DELIMITED = 2;

	public DarwinCSV(File file) throws IOException {
		// Guess the file type from the first line.
		BufferedReader r = new BufferedReader(new FileReader(file));
		String firstLine = r.readLine();
		
		/* TODO: Eventually, like this will figure out what kind
		 of file we have. But right now, we do not care. */
		
		int parts_csv = firstLine.split(",").length;
		int parts_tabs = firstLine.split("\t").length;
		int parts_semi = firstLine.split(";").length;
		
		if((parts_semi > parts_tabs) && (parts_semi > parts_csv)) {
			setup(file, FILE_SEMI_DELIMITED);
		} else if((parts_tabs > parts_semi) && (parts_tabs > parts_csv)) {
			setup(file, FILE_TAB_DELIMITED);
		} else {
			setup(file, FILE_CSV_DELIMITED);
		}
	}
	
	public DarwinCSV(File file, int file_type) throws IOException {
		setup(file, file_type);
	}
	
	/**
	 * Set up this Darwin CSV. In particular, it'll read
	 * the file header and double-check if:
	 * 
	 *	(1) every column name is unique, and
	 *  (2) see if we can identify any columns that are
	 *		useful for us.
	 * 
	 * @param file The file to process.
	 * @param file_type The file type - CSV, tab-delimited, semi-delimited.
	 */
	public final void setup(File file, int file_type) throws IOException {
		if(file_type == FILE_CSV_DELIMITED) {
			// Default.
		} else if(file_type == FILE_TAB_DELIMITED) {
			separator = '\t';
		} else if(file_type == FILE_SEMI_DELIMITED) {
			separator = ';';
		}
		
		CSVReader csvr = new CSVReader(new BufferedReader(new FileReader(file)));
		columns = csvr.readNext();
		checkColumns();
		
		data = csvr.readAll();
	}
	
	private void checkColumns() throws IOException {
		// 1. All columns must be unique.
		columnNames = new HashMap<String, Integer>(columns.length);
		
		int x = 0;
		for(String colName: columns) {
			columnNames.put(colName, x);
			x++;
		}
		
		if(columnNames.size() < columns.length) {
			throw new IOException("Duplicate column names: " + StringUtils.join(columnNames, ", "));
		}
		
		// 2. See if we can identify any scientific names.
		col_canonicalname = column("canonicalname", "canonicalName", "canonical_name");
		col_scientificname = column("scientificname", "scientificName", "scientific_name");
	}
	
	public List<String> columns() {
		return Arrays.asList(columns);
	}
	
	public int column(String colName) {
		Integer i = columnNames.get(colName);
		if(i == null)
			return i.intValue();
		else
			return -1;
	}
	
	public int column(String... colNames) {
		for(String colName: colNames) {
			int c = column(colName);
			if(c != -1)
				return c;
		}
		return -1;
	}
	
	public List<String[]> rows() {
		return data;
	}
	
	public String[] row(int row) {
		return data.get(row);
	}
	
	public int colcount() {
		return columns.length;
	}
	
	public int rowcount() {
		return data.size();
	}
	
	public int col_scientificname() {
		return col_scientificname;
	}
	
	public int col_canonicalname() {
		return col_canonicalname;
	}
	
}
