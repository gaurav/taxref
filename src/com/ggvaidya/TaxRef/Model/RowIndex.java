
/*
 *
 *  RowIndex
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

import java.awt.Component;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

// TODO: This is horribly thread-unsafe. Please fix!

/**
 * A RowIndex indexes rows.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class RowIndex implements TableModel {
	private UUID uuid;
	private int row_count = 0;		// used only for UUID generation.
	
	// TODO: is LinkedLists the best way to do this? Remember that
	// neither rows nor columns are updated much after the first
	// run through.
	private List<String> columns;
	private List<String> columnsLowercase;
	private HashMap<String, Class> columnClasses;
	
	private List<Object[]> rows = new LinkedList<Object[]>();
	
	private HashMap<Name, List<Object[]>> nameIndex = new HashMap<Name, List<Object[]>>();
	
	public RowIndex(List<String> columnNames, List<Class> classes) {
		uuid = UUID.randomUUID();
		
		columns = new LinkedList<String>(columnNames);
		columnsLowercase = new LinkedList<String>();
		columnClasses = new HashMap<String, Class>();
		
		int index = 0;
		for(String colName: columns) {
			
			columnsLowercase.add(colName.toLowerCase());
			columnClasses.put(colName, classes.get(index));
			
			index++;
		}
	}
	
	public void indexName(Name name, Object[] row) {
		// Index the names.
		for(int x = 0; x < getColumnCount(); x++) {
			if(getColumnClass(x).equals(Name.class)) {
				Name nameToIndex = ((Name) row[x]);
				
				if(!nameIndex.containsKey(nameToIndex)) {
					nameIndex.put(nameToIndex, new LinkedList<Object[]>());
				}
				
				nameIndex.get(nameToIndex).add(row);
			}
		}
	}
	
	public Set<Name> getAllNames() {
		return nameIndex.keySet();
	}
	
	public void setColumnClass(String colName, Class colClass) {
		columnClasses.put(colName, colClass);
		// TODO: cast values?
	}
	
	public boolean containsColumn(String colName) {
		return columnsLowercase.contains(colName.toLowerCase());
	}
	
	public int getColumnIndex(String colName) {
		return columnsLowercase.indexOf(colName.toLowerCase());
	}
	
	public List<Object> getColumn(String colName) {
		int colIndex = getColumnIndex(colName);
		
		if(!columnsLowercase.contains(colName.toLowerCase())) {
			throw new RuntimeException("Column '" + colName + "' doesn't exist.");
		} else {
			ArrayList<Object> column = new ArrayList<Object>();
		
			for(Object[] r: rows) {
				column.add(r[colIndex]);
			}
			
			return column;
		}
	}
	
	public void createNewColumn(String newColumn, int insertAt, String fromColumn, MapOperation mop) {
		int fromColumnIndex = getColumnIndex(fromColumn);
		
		columns.add(insertAt, newColumn);
		columnsLowercase.add(insertAt, newColumn);
				
		List<Object[]> new_rows = new LinkedList<Object[]>();
		for(Object[] row: rows) {
			Object[] new_row = new Object[row.length + 1];
			
			// 0...1...2...3...4...5...6...7...8...9...10
			//             ^
			// copy(0..2)->(0..2)
			// insert 3
			// copy(3..10)->(4..11) [7]
			
			System.arraycopy(row, 0, new_row, 0, insertAt);
			new_row[insertAt] = mop.mapTo(row[fromColumnIndex]);
			System.arraycopy(row, insertAt, new_row, insertAt + 1, new_row.length - insertAt - 1);
			
			new_rows.add(new_row);
		}
		
		rows = new_rows;
	}
	
	public void addRow(Object[] row) {
		rows.add(row);
	}
	
	public List<Object[]> getRows() {
		return new ArrayList(rows);
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}
	
	public int size() {
		return rows.size();
	}

	@Override
	public int getColumnCount() {
		return columns.size();
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columns.get(columnIndex);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columnClasses.get(getColumnName(columnIndex));
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return rows.get(rowIndex)[columnIndex];
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		rows.get(rowIndex)[columnIndex] = aValue;
	}
	
	private List<TableModelListener> listeners = new LinkedList<TableModelListener>();

	@Override
	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	/* Matching code */
	public RowIndexMatch matchAgainst(RowIndex against) {
		return new RowIndexMatch(this, against);
	}

	public List<String> getColumnNames() {
		return new ArrayList(columns);
	}
	
	public List<String> getColumnNamesLowercase() {
		return new ArrayList(columnsLowercase);
	}
	
	public boolean hasName(Name n) {
		return nameIndex.containsKey(n);
	}
	
	public boolean hasName(String str) {
		if(str == null)
			return false;
		
		return hasName(new Name(str));
	}
}
