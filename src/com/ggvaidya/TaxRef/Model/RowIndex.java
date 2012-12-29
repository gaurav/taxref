
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

import com.ggvaidya.TaxRef.Common.*;
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
	private HashMap<String, List<Object[]>> nameIndex = new HashMap<String, List<Object[]>>();
	
	@Override
	public String toString() {
		return "RowIndex of " + rows.size() + " rows across " + columns.size() + " columns, " + nameIndex.size() + " names indexed.";
	}
	
	public RowIndex(List<String> columnNames, List<Class> classes) {
		uuid = UUID.randomUUID();
		
		columns = new LinkedList<String>(columnNames);
		columnsLowercase = new LinkedList<String>();
		columnClasses = new HashMap<String, Class>();
		
		int index = 0;
		for(String colName: columns) {
			
			Class colClass = classes.get(index);
			
			columnsLowercase.add(colName.toLowerCase());
			columnClasses.put(colName, colClass);
			
			index++;
		}
	}
	
	public void indexValue(Object obj, Object[] row) {
		if(obj == null)
			return;
		
		if(Name.class.isAssignableFrom(obj.getClass())) {
			// Index the names.
			Name nameToIndex = (Name) obj;
				
			if(!nameIndex.containsKey(nameToIndex)) {
				nameIndex.put(nameToIndex.getNamestringLC(), new LinkedList<Object[]>());
			}
			
			nameIndex.get(nameToIndex.getNamestringLC()).add(row);
			// System.err.println("Adding '" + nameToIndex.getNamestringLC() + "' to name index.");
			
		} else {
			// Not a name? then no index for you.
		}
	}
	
	public void setColumnClass(String colName, Class colClass) {
		columnClasses.put(colName, colClass);
		// TODO: cast values?
	}
	
	public boolean hasColumn(String colName) {
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
		
		class ArrayMapToMapOp implements ArrayMapOperation {
			private int column;
			private MapOperation mop;
			
			public ArrayMapToMapOp(MapOperation mop, int column) {
				this.mop = mop;
				this.column = column;
			}
		
			@Override
			public Object mapTo(Object[] values) {
				return mop.mapTo(values[column]);
			}
		}
		
		createNewColumn(newColumn, insertAt, new ArrayMapToMapOp(mop, fromColumnIndex));
	}
	
	public void createNewColumn(String newColumn, int insertAt, ArrayMapOperation mop) {
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
			Object toInsert = mop.mapTo(row);
			indexValue(toInsert, row);
			new_row[insertAt] = toInsert;
			System.arraycopy(row, insertAt, new_row, insertAt + 1, new_row.length - insertAt - 1);
			
			new_rows.add(new_row);
		}
		
		rows = new_rows;
	}
	
	public void addRow(Object[] row) {
		rows.add(row);
		
		for(int x = 0; x < row.length; x++) {
			indexValue(row[x], row);
		}
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
		if(aValue == null)
			aValue = "";
		
		if(Name.class.isAssignableFrom(getColumnClass(columnIndex)) &&
			String.class.isAssignableFrom(aValue.getClass())) {
			aValue = Name.getName((String)aValue);
		}
		
		System.err.println("Updated value at (" + rowIndex + ", " + columnIndex + ") from '" + 
				rows.get(rowIndex)[columnIndex] + "' to '" + aValue + "'");
		rows.get(rowIndex)[columnIndex] = aValue;
		
		for(TableModelListener tml: listeners) {
			tml.tableChanged(new TableModelEvent(this, rowIndex, rowIndex, columnIndex));
		}
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

	// This is a very frequently used method, and should be optimized!
	public boolean hasName(Name n) {
		if(n == null)
			return false;
		
		// System.err.println("And still nothing for '" + n.getNamestringLC() + "': " + nameIndex.containsKey(n.getNamestringLC()));
		
		return nameIndex.containsKey(n.getNamestringLC());
	}
	
	public boolean hasName(String str) {
		if(str == null)
			return false;
		
		// System.err.println("Ooo err: " + nameIndex.containsKey("palmaria palmata"));
		// System.err.println("And yet nothing for '" + str.toLowerCase() + "': " + nameIndex.containsKey(str.toLowerCase()));
		
		return nameIndex.containsKey(str.toLowerCase());
	}

	public List<Object[]> getNameRows(String name) {
		if(nameIndex == null || name == null)
			return new ArrayList<Object[]>();
		
		return nameIndex.get(name.toLowerCase());
	}
}